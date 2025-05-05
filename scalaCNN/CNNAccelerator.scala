import chisel3._
import chisel3.util._


import ChiselLib._
import cnnPE._
import matrixLoad._
import SyncMEM._

package CNNAccelerator{
    class CNNAccelerator(dataWidth:Int=32,instructionWidth:Int=64,vecNum:Int=16,n:Int=8,depth: Int=8192*2) extends Module{
        val io=IO(new Bundle{
            val instructions=Input(Vec(vecNum,UInt((2*instructionWidth).W)))
            val subMatrixSteps=Input(Vec(vecNum,UInt((4*dataWidth).W)))
            val loadBase=Input(Vec(vecNum,UInt((dataWidth).W)))
            val loadNum=Input(Vec(vecNum,UInt((dataWidth).W)))
            val instruction_valid = Input(UInt(1.W))
            val storeBase=Input(Vec(vecNum,UInt((dataWidth).W)))
            val storeStep=Input(Vec(vecNum,UInt((dataWidth).W)))
            val storeBase_valid = Input(UInt(1.W))
            val useCompareOrMac=Input(UInt(2.W))
            val use_valid=Input(UInt(1.W))
            val rerun = Output(UInt(1.W))
            val start=Input(UInt(1.W))
            val memWrite=Input(UInt(1.W))
            val memAddr=Input(UInt(dataWidth.W))
            val memWriteData=Input(UInt(dataWidth.W))
            val memReadData=Output(UInt(dataWidth.W))
        })
        val instructionsMem=Reg(Vec(vecNum,UInt((2*instructionWidth).W)))
        val subMatrixStepsMem=Reg(Vec(vecNum,UInt((4*dataWidth).W)))
        val storeBaseMem=Reg(Vec(vecNum,UInt((dataWidth).W)))
        val storeStepMem=Reg(Vec(vecNum,UInt((dataWidth).W)))
        val loadBaseMem=Reg(Vec(vecNum,UInt((dataWidth).W)))
        // val loadNumMem=Reg(Vec(vecNum,UInt((dataWidth).W)))
        val loadNumMem = RegInit(VecInit(Seq.fill(vecNum)(0.U(dataWidth.W))))
        // val loadNumCnt=Reg(Vec(vecNum,UInt((dataWidth).W)))
        val loadNumCnt = RegInit(VecInit(Seq.fill(vecNum)(0.U(dataWidth.W))))
        val useCompareOrMacMem=RegInit(0.U(2.W))
        useCompareOrMacMem:=Mux(io.use_valid.asBool,io.useCompareOrMac,useCompareOrMacMem)
        val rdFIFOReady=Wire(Vec(vecNum,UInt(1.W)))
        val loadU=VecInit(Seq.fill(vecNum)(Module(new matrixLoad.matrixLoadStore(dataWidth, instructionWidth)).io))
        val cntRerun=Wire(Vec(vecNum,UInt(1.W)))
        val validMask=Wire(Vec(vecNum,UInt(1.W)))
        val hasRuns=Wire(UInt(1.W))
        hasRuns:= ~validMask.reduceTree(_ & _)
        for(i<- 0 to vecNum-1){
            instructionsMem(i):=Mux(io.instruction_valid.asBool,io.instructions(i),instructionsMem(i))
            subMatrixStepsMem(i):=Mux(io.instruction_valid.asBool,io.subMatrixSteps(i),subMatrixStepsMem(i))
            loadBaseMem(i):=Mux(io.instruction_valid.asBool,io.loadBase(i),loadBaseMem(i))
            loadNumMem(i):=Mux(io.instruction_valid.asBool,io.loadNum(i),loadNumMem(i))
            storeBaseMem(i):=Mux(io.storeBase_valid.asBool,io.storeBase(i),storeBaseMem(i))
            storeStepMem(i):=Mux(io.storeBase_valid.asBool,io.storeStep(i),storeStepMem(i))
            loadNumCnt(i):=Mux(io.instruction_valid.asBool,0.U,Mux(loadU(i).predicate_in0.ready.asBool&&loadU(i).predicate_in0.valid.asBool,loadNumCnt(i)+1.U,loadNumCnt(i)))
            loadU(i).predicate_in0.valid:=(~cntRerun(i))&io.start
            loadU(i).predicate_in0.bits:= (~cntRerun(i))&io.start
            cntRerun(i):= ~(loadNumCnt(i)<loadNumMem(i))
            validMask(i):=cntRerun(i)
        }
        
        val storeU=VecInit(Seq.fill(vecNum)(Module(new matrixLoad.resultStore(dataWidth)).io))
        // val memSysInst = Module(new SyncMEM.memSysDirect(n,vecNum,vecNum,vecNum,vecNum,vecNum,dataWidth, depth))//n is the fifo depth,depth is the SRAM depth
        val memSysInst = Module(new SyncMEM.memSys(n,vecNum,vecNum,vecNum,vecNum,vecNum,dataWidth, depth))//n is the fifo depth,depth is the SRAM depth
        
        val vecMACInst = Module(new cnnPE.VectorMAC(vecNum/2,dataWidth))
        val vecADDInst = Module(new cnnPE.VectorADD(vecNum/2,dataWidth))
        val vecCompareInst = Module(new cnnPE.VectorCompare(vecNum,dataWidth))
        val distinctU=Module(new SyncMEM.distinctReadWrite(2*vecNum,vecNum, dataWidth,vecNum))
        val MACInputC=Wire(Vec(vecNum/2,UInt(1.W)))
        for(i<-0 to vecNum-1){
            distinctU.io.Address(i+vecNum):=loadU(i).load_port.bits
            distinctU.io.Valid(i+vecNum):=loadU(i).load_port.valid
            distinctU.io.rdFIFOReady(i):=rdFIFOReady(i)
            
            memSysInst.io.rdValid(i):=distinctU.io.ValidOut(i+vecNum)
            rdFIFOReady(i):=memSysInst.io.rdFIFOReady(i)

            distinctU.io.Address(i):=storeU(i).store_port.bits
            distinctU.io.Valid(i):=storeU(i).store_port.valid
            
            memSysInst.io.wrValid(i):=distinctU.io.ValidOut(i)
            
            if(i<vecNum/2){
                MACInputC(i):=loadU(i).predicate_out.bits
            }
        } 

        memSysInst.io.rdAddress1:=io.memAddr
        memSysInst.io.wrAddress1:=io.memAddr
        memSysInst.io.data_in1:=io.memWriteData
        memSysInst.io.wr_en1:=io.memWrite
        memSysInst.io.start:=io.start
        io.memReadData:=memSysInst.io.data_out1
        val loadRerun=Wire(Vec(vecNum,UInt(1.W)))
        val storeRerun=Wire(Vec(vecNum,UInt(1.W)))
        val compareReady=Wire(Vec(vecNum,UInt(1.W)))
        val MACReady=Wire(Vec(vecNum/2,UInt(1.W)))
        val ADDReady=Wire(Vec(vecNum/2,UInt(1.W)))
        val StoreReady=Wire(Vec(vecNum,UInt(1.W)))
        val CMPReady=Wire(Vec(vecNum,UInt(1.W)))
        for(i<- 0 to vecNum-1) {
            memSysInst.io.rdAddress(i):=loadU(i).load_port.bits
            // memSysInst.io.rdValid(i):=loadU(i).load_port.valid
            loadU(i).load_port.ready:=memSysInst.io.rdReady(i)
            loadU(i).load_data.valid:=memSysInst.io.outValid(i)
            loadU(i).load_data.bits:=memSysInst.io.data_out(i)
            memSysInst.io.outReady(i):=loadU(i).load_data.ready

            memSysInst.io.wrAddress(i):=storeU(i).store_port.bits
            // memSysInst.io.wrValid(i):=storeU(i).store_port.valid
            storeU(i).store_port.ready:=memSysInst.io.wrReady(i)
            memSysInst.io.data_in(i):=storeU(i).store_data

            storeU(i).clear:=io.storeBase_valid
            storeU(i).storeBase:=storeBaseMem(i)
            storeRerun(i):=storeU(i).rerun
            // storeU(i).storeStep:=storeStepMem(i)
            loadU(i).newCol:=storeStepMem(i)

            loadU(i).subMatrixSteps:=subMatrixStepsMem(i)
            loadU(i).instructions:=instructionsMem(i)
            loadU(i).baseAddr:=loadBaseMem(i)
            loadU(i).instruction_valid:=io.instruction_valid
            loadU(i).start:=io.start
            // loadU(i).predicate_in0.bits:=0.U
            // loadU(i).predicate_in0.valid:=0.U
            loadU(i).predicate_in1.bits:=0.U
            loadU(i).predicate_in1.valid:=0.U
            loadRerun(i):=Mux(io.start===1.U&&loadNumMem(i)===0.U,1.U,loadU(i).rerun)

            // val inputA=Input(Vec(vecNum,UInt(dataWidth.W)))
            // val inputC=Input(Vec(vecNum,UInt(1.W)))
            // val inputValid=Input(Vec(vecNum,UInt(1.W)))
            // val inputReady=Output(Vec(vecNum,UInt(1.W)))
            // val outputD=Output(Vec(vecNum,UInt(dataWidth.W)))
            // val outputValid=Output(UInt(1.W))
            // val outputReady=Input(UInt(1.W))
            // val outReady=Output(UInt(1.W))
            vecCompareInst.io.inputA(i):=Mux(useCompareOrMacMem===1.U,loadU(i).data_out.bits,0.U)
            vecCompareInst.io.inputC(i):=Mux(useCompareOrMacMem===1.U,Mux((!validMask(i).asBool),loadU(i).predicate_out.bits,CMPReady.reduceTree(_|_)),0.U)
            vecCompareInst.io.inputValid(i):=Mux(useCompareOrMacMem===1.U,loadU(i).predicate_out.valid&loadU(i).data_out.valid| (hasRuns&(validMask(i))),0.U)
            // loadU(i).data_out.ready:=Mux(useCompareOrMacMem.asBool,vecCompareInst.io.inputReady(i),0.U)
            // loadU(i).predicate_out.ready:=Mux(useCompareOrMacMem.asBool,vecCompareInst.io.inputReady(i),0.U)
            if(i<vecNum/2){
                vecMACInst.io.inputA(i):=Mux(useCompareOrMacMem===0.U,loadU(i).data_out.bits,0.U)
                vecMACInst.io.inputB(i):=Mux(useCompareOrMacMem===0.U,Mux((!validMask(i).asBool)&&(!validMask(i+vecNum/2).asBool),loadU(i+vecNum/2).data_out.bits,0.U),0.U)
                vecMACInst.io.inputC(i):=Mux(useCompareOrMacMem===0.U,Mux((!validMask(i).asBool)&&(!validMask(i+vecNum/2).asBool),loadU(i).predicate_out.bits,MACInputC.reduceTree(_ | _)),0.U)
                vecMACInst.io.inputValid(i):=Mux(useCompareOrMacMem===0.U,loadU(i).predicate_out.valid&loadU(i).data_out.valid&loadU(i+vecNum/2).predicate_out.valid&loadU(i+vecNum/2).data_out.valid | (hasRuns&(validMask(i)|validMask(i+vecNum/2))),0.U)
                
                vecADDInst.io.inputA(i):=Mux(useCompareOrMacMem===2.U,loadU(i).data_out.bits,0.U)
                vecADDInst.io.inputB(i):=Mux(useCompareOrMacMem===2.U,Mux((!validMask(i).asBool)&&(!validMask(i+vecNum/2).asBool),loadU(i+vecNum/2).data_out.bits,0.U),0.U)
                vecADDInst.io.inputC(i):=Mux(useCompareOrMacMem===2.U,Mux((!validMask(i).asBool)&&(!validMask(i+vecNum/2).asBool),loadU(i).predicate_out.bits,MACInputC.reduceTree(_ | _)),0.U)
                vecADDInst.io.inputValid(i):=Mux(useCompareOrMacMem===2.U,loadU(i).predicate_out.valid&loadU(i).data_out.valid&loadU(i+vecNum/2).predicate_out.valid&loadU(i+vecNum/2).data_out.valid | (hasRuns&(validMask(i)|validMask(i+vecNum/2))),0.U)

                loadU(i).data_out.ready:=Mux(useCompareOrMacMem===0.U,vecMACInst.io.inputReady(i),Mux(useCompareOrMacMem===1.U,vecCompareInst.io.inputReady(i),Mux(useCompareOrMacMem===2.U,vecADDInst.io.inputReady(i),0.U)))
                loadU(i).predicate_out.ready:=Mux(useCompareOrMacMem===0.U,vecMACInst.io.inputReady(i),Mux(useCompareOrMacMem===1.U,vecCompareInst.io.inputReady(i),Mux(useCompareOrMacMem===2.U,vecADDInst.io.inputReady(i),0.U)))
                loadU(i+vecNum/2).data_out.ready:=Mux(useCompareOrMacMem===0.U,vecMACInst.io.inputReady(i),Mux(useCompareOrMacMem===1.U,vecCompareInst.io.inputReady(i+vecNum/2),Mux(useCompareOrMacMem===2.U,vecADDInst.io.inputReady(i),0.U)))
                loadU(i+vecNum/2).predicate_out.ready:=Mux(useCompareOrMacMem===0.U,vecMACInst.io.inputReady(i),Mux(useCompareOrMacMem===1.U,vecCompareInst.io.inputReady(i+vecNum/2),Mux(useCompareOrMacMem===2.U,vecADDInst.io.inputReady(i),0.U)))
                vecMACInst.io.store_offset(i):=loadU(i).store_offset

                vecADDInst.io.store_offset(i):=loadU(i).store_offset
            }
            vecCompareInst.io.store_offset(i):=loadU(i).store_offset
            CMPReady(i):=loadU(i).predicate_out.bits
            compareReady(i):=Mux(useCompareOrMacMem===1.U,Mux((!validMask(i).asBool),storeU(i).data_in0.ready,StoreReady.reduceTree(_|_)),0.U)
            StoreReady(i):=storeU(i).data_in0.ready
            if(i>=vecNum/2){
                storeU(i).data_in0.bits:=Mux(useCompareOrMacMem===1.U,vecCompareInst.io.outputD(i),0.U)
                storeU(i).data_in0.valid:=Mux(useCompareOrMacMem===1.U,vecCompareInst.io.outputValid&(!(validMask(i))),0.U)
                storeU(i).store_offset:=Mux(useCompareOrMacMem===1.U,vecCompareInst.io.store_offsetOut(i),0.U)
            }
            else{
                storeU(i).store_offset:=Mux(useCompareOrMacMem===1.U,vecCompareInst.io.store_offsetOut(i),Mux(useCompareOrMacMem===2.U,vecADDInst.io.store_offsetOut(i),vecMACInst.io.store_offsetOut(i)))
                storeU(i).data_in0.bits:=Mux(useCompareOrMacMem===1.U,vecCompareInst.io.outputD(i),Mux(useCompareOrMacMem===2.U,vecADDInst.io.outputD(i),vecMACInst.io.outputD(i)))
                storeU(i).data_in0.valid:=Mux(useCompareOrMacMem===1.U,vecCompareInst.io.outputValid&(!(validMask(i))),Mux(useCompareOrMacMem===2.U,vecADDInst.io.outputValid&(!(validMask(i)|validMask(i+vecNum/2))),vecMACInst.io.outputValid&(!(validMask(i)|validMask(i+vecNum/2)))))
                // vecCompareInst.io.outputReady(i):=Mux(useCompareOrMacMem.asBool,storeU(i).data_in0.ready,0.U)
                
                ADDReady(i):=Mux(useCompareOrMacMem===2.U,Mux((!validMask(i).asBool)&&(!validMask(i+vecNum/2).asBool),storeU(i).data_in0.ready,StoreReady.reduceTree(_|_)),0.U)
            
                MACReady(i):=Mux(useCompareOrMacMem===0.U,Mux((!validMask(i).asBool)&&(!validMask(i+vecNum/2).asBool),storeU(i).data_in0.ready,StoreReady.reduceTree(_|_)),0.U)
            }
            // storeU(i).store_offset:=loadU(i).store_offset
        }
        vecCompareInst.io.outputReady:=compareReady.reduceTree(_&_)
        vecMACInst.io.outputReady:=MACReady.reduceTree(_&_)
        vecADDInst.io.outputReady:=ADDReady.reduceTree(_&_)
        io.rerun:=loadRerun.reduceTree(_&_)&storeRerun.reduceTree(_&_)&cntRerun.reduceTree(_&_)
    }

    object myCNN extends App {
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new CNNAccelerator())));
        // (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new ArbiterArbitrary(4,32))));
    }
}