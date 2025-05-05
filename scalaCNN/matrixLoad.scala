import chisel3._
import chisel3.util._


import ChiselLib._


package matrixLoad{
    class matrixLoadStore(dataWidth:Int,instructionWidth:Int) extends Module{
        val io=IO(new Bundle{
            // val data_in0 = Flipped(Decoupled(UInt(dataWidth.W)))
            // val data_in1 = Flipped(Decoupled(UInt(dataWidth.W)))
            val predicate_in0 = Flipped(Decoupled(UInt(1.W)))
            val predicate_in1 = Flipped(Decoupled(UInt(1.W)))
            val subMatrixSteps=Input(UInt((4*dataWidth).W))
            val instructions=Input(UInt((2*instructionWidth).W))
            val baseAddr=Input(UInt((dataWidth).W))
            val newCol=Input(UInt(dataWidth.W))
            // val phiInst=Input(UInt(1.W))
            val instruction_valid = Input(UInt(1.W))
            val data_out = Decoupled(UInt(dataWidth.W))
            val predicate_out = Decoupled(UInt())
            val rerun = Output(UInt())
            val start=Input(UInt(1.W))
            // val noSelf=Output(UInt(1.W))
            val load_port = Decoupled(UInt(dataWidth.W))
            val load_data = Flipped(Decoupled(UInt(dataWidth.W)))
            val store_offset=Output(UInt(dataWidth.W))
        })
        val inst_sel=RegInit(0.U((1).W))
        val inst_valid=RegInit(0.U((1).W))
        val instruction_mem_last = Wire(UInt((instructionWidth).W))
        val instruction_mem_next = Wire(UInt((instructionWidth).W))
        // val instruction_mem=io.instruction_mem_in
        // val mulResult = Wire(UInt(dataWidth.W))
        inst_valid:=Mux(io.instruction_valid.asBool(),io.instruction_valid,inst_valid)
        inst_sel:=Mux(io.start===1.U,inst_valid,0.U)
        io.load_port.bits:=0.U
        io.load_port.valid:=0.U
        io.load_data.ready:=0.U
        // val startPointRow.asUInt/stepsy.asUInt=Reg(UInt(dataWidth.W))
        
        // val data0 = RegInit(0.U((dataWidth).W))
        // data0 := Mux(//io.data_in0.valid, //io.data_in0.bits, data0)
        // val data1 = RegInit(0.U((dataWidth).W))
        // data1 := Mux(//io.data_in1.valid, //io.data_in1.bits, data1)
        val instlow=Wire(Vec(instructionWidth,UInt()))
        val insthigh=Wire(Vec(instructionWidth,UInt()))
        for(i<-0 to instructionWidth-1){
            insthigh(i):=io.instructions(i+instructionWidth)
            instlow(i):=io.instructions(i)
        }
        // io.instruction_in.ready:=0.U
        instruction_mem_last := instlow.asUInt
        instruction_mem_next := insthigh.asUInt
        val typeFunc = Wire(Vec(instructionWidth-8-dataWidth,UInt()))
        val function=typeFunc.asUInt
        val hasDin=Wire(Vec(2,UInt(1.W)))
        val instruction_mem_tmp=Wire(Vec(3,UInt(1.W)))
        for(i<-0 to 2){
            instruction_mem_tmp(i):=Mux(1.B,instruction_mem_last(i),instruction_mem_next(i))
            if(i<2){
                hasDin(i):=Mux(1.B,instruction_mem_last(instructionWidth-dataWidth-4+i),instruction_mem_next(instructionWidth-dataWidth-4+i))
            }
        }
        val instruction_mem=instruction_mem_tmp.asUInt
        for(i <- 0 to instructionWidth - 9-dataWidth) {
            typeFunc(i) := Mux(1.B,instruction_mem_last(i+3),instruction_mem_next(i+3))
        }
        // val forout=Mux(sel.asBool,instruction_mem_last(instructionWidth-dataWidth-2),instruction_mem_next(instructionWidth-dataWidth-2))
        
        val forRowCnt=RegInit(0.U(dataWidth.W))
        val nxtforRowCnt=Wire(UInt(dataWidth.W))
        forRowCnt:=nxtforRowCnt
        val forColCnt=RegInit(0.U(dataWidth.W))
        val nxtforColCnt=Wire(UInt(dataWidth.W))
        forColCnt:=nxtforColCnt

        val forLRowCnt=RegInit(0.U(dataWidth.W))
        val storeReg=RegInit(0.U(dataWidth.W))
        val nxtStoreReg=Wire(UInt(dataWidth.W))
        storeReg:=nxtStoreReg
        val nxtforLRowCnt=Wire(UInt(dataWidth.W))
        forLRowCnt:=nxtforLRowCnt
        val forLRowCnt1=RegInit(0.U(dataWidth.W))
        // val storeReg=RegInit(0.U(dataWidth.W))
        val nxtforLRowCnt1=Wire(UInt(dataWidth.W))
        forLRowCnt1:=nxtforLRowCnt1
        val forLColCnt=RegInit(0.U(dataWidth.W))
        val nxtforLColCnt=Wire(UInt(dataWidth.W))
        forLColCnt:=nxtforLColCnt
        val forLColCnt1=RegInit(0.U(dataWidth.W))
        val nxtforLColCnt1=Wire(UInt(dataWidth.W))
        forLColCnt1:=nxtforLColCnt1
        val stepsx=Wire(Vec(dataWidth/2,UInt(1.W)))
        val stepsy=Wire(Vec(dataWidth/2,UInt(1.W)))
        val subRow=Wire(Vec(dataWidth,UInt(1.W)))
        val subCol=Wire(Vec(dataWidth,UInt(1.W)))
        val Row=Wire(Vec(dataWidth,UInt(1.W)))
        val Col=Wire(Vec(dataWidth,UInt(1.W)))
        val startPointRow=Wire(Vec(dataWidth/2,UInt(1.W)))
        val startPointReduced=Wire(Vec(dataWidth,UInt(1.W)))
        val startPointCol=Wire(Vec(dataWidth,UInt(1.W)))
        // startPointRow.asUInt/stepsy.asUInt:=Mux(io.instruction_valid.asBool,startPointRow.asUInt/stepsy.asUInt,startPointRow.asUInt/stepsy.asUInt)
        for(i<-0 to dataWidth-1){
            if(i<dataWidth/2){
                stepsx(i):=io.subMatrixSteps(i)
                startPointRow(i):=startPointReduced(i)
            }
            else{
                stepsy(i-dataWidth/2):=io.subMatrixSteps(i)
            }
            subRow(i):=io.subMatrixSteps(i+dataWidth)
            subCol(i):=io.subMatrixSteps(i+2*dataWidth)
            startPointCol(i):=io.subMatrixSteps(i+3*dataWidth)
            startPointReduced(i):=io.instructions(i+instructionWidth-dataWidth)
            Row(i):=io.instructions(i+instructionWidth)
            Col(i):=io.instructions(i+2*instructionWidth-dataWidth)
        }
        // val matrixNum=Reg(UInt(dataWidth.W))
        // matrixNum:=
        val predicate_mux1 = io.predicate_in0.bits&io.predicate_in1.bits&io.predicate_in0.valid&io.predicate_in1.valid
        val predicate_mux0 = io.predicate_in0.bits&io.predicate_in0.valid
        val predicate_mux1bit = io.predicate_in0.valid || io.predicate_in1.valid
        // val data_mux0bit = //io.data_in0.valid || //io.data_in1.valid
        val predicate_mux0bit = io.predicate_in0.valid
        val phiInst=typeFunc.asUInt()===86.U||typeFunc.asUInt()===87.U
        val isLoad=typeFunc.asUInt()===86.U
        val isStore=typeFunc.asUInt()===87.U
        
        // io.predicate_in0.ready := io.predicate_in0.valid
        // io.predicate_in1.ready := io.predicate_in1.valid
        val predicate_restart=/*(~instruction_mem(1))&*/Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0bit, predicate_mux1bit)
        // val data_restart=(//io.data_in0.valid)
        val predicate = instruction_mem(1) | Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0, predicate_mux1) 
        val runningState = RegInit(UInt(8.W), 0.U)
        val nextrunningState = Wire(UInt(8.W))
        runningState:=nextrunningState
        io.rerun:=0.U
        io.store_offset:=storeReg
        nxtStoreReg:=storeReg
        // io.noSelf:=0.U
        when (runningState === 0.U) {
            io.rerun:=0.U
            when(phiInst===0.U){
                nextrunningState:=0.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //////////////////////////////io.data_in0.ready:=0.U
                ////io.data_in1.ready:=0.U
                nxtforColCnt:=0.U
                nxtforRowCnt:=0.U
                nxtforLColCnt:=0.U
                nxtforLColCnt1:=0.U
                nxtforLRowCnt:=0.U
                nxtforLRowCnt1:=0.U
            }
            .elsewhen((predicate === 1.U)&&inst_sel===1.U&&isLoad===1.U){
                when(startPointRow.asUInt+subRow.asUInt<=Row.asUInt && startPointCol.asUInt+subCol.asUInt<=Col.asUInt){
                    nextrunningState:=3.U
                    io.predicate_in0.ready:=0.U
                    io.predicate_in1.ready:=0.U
                    io.data_out.bits:=0.U
                    io.data_out.valid:=0.U
                    io.predicate_out.bits:=0.U
                    io.predicate_out.valid:=0.U
                    ////io.data_in0.ready:=0.U
                    ////io.data_in1.ready:=0.U
                    nxtforColCnt:=0.U
                    nxtforRowCnt:=0.U
                    nxtforLRowCnt:=startPointRow.asUInt
                    nxtforLRowCnt1:=startPointRow.asUInt/stepsy.asUInt
                    nxtforLColCnt:=startPointCol.asUInt
                    nxtforLColCnt1:=startPointCol.asUInt
                    nxtStoreReg:=(startPointCol.asUInt+0.U)*io.newCol.asUInt+startPointRow.asUInt/stepsy.asUInt+0.U+io.baseAddr
                }
                .otherwise{
                    nextrunningState:=2.U
                    io.predicate_in0.ready:= ~instruction_mem(1)
                    io.predicate_in1.ready:= (~instruction_mem(1))&(~instruction_mem(0))
                    io.data_out.bits:=0.U
                    io.data_out.valid:=0.U
                    io.predicate_out.bits:=0.U
                    io.predicate_out.valid:=0.U
                    ////io.data_in0.ready:=0.U
                    ////io.data_in1.ready:=0.U
                    nxtforColCnt:=0.U
                    nxtforRowCnt:=0.U
                    nxtforLColCnt:=0.U
                    nxtforLColCnt1:=0.U
                    nxtforLRowCnt:=0.U
                    nxtforLRowCnt1:=0.U
                }
            }
            .otherwise{
                nextrunningState:=0.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                ////io.data_in0.ready:=0.U
                ////io.data_in1.ready:=0.U
                nxtforColCnt:=0.U
                nxtforRowCnt:=0.U
                nxtforLColCnt:=0.U
                nxtforLColCnt1:=0.U
                nxtforLRowCnt:=0.U
                nxtforLRowCnt1:=0.U
            }
        }
        .elsewhen(runningState===1.U){
            when(forColCnt+1.U<subCol.asUInt){
                nextrunningState:=3.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=forColCnt+1.U
                nxtforRowCnt:=0.U
                nxtforLColCnt:=forLColCnt
                nxtforLColCnt1:=forLColCnt1
                nxtforLRowCnt:=forLRowCnt
                nxtforLRowCnt1:=forLRowCnt1
            }
            .otherwise{
                nextrunningState:=4.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=0.U
                nxtforRowCnt:=0.U
                nxtforLColCnt:=forLColCnt//+steps.asUInt
                nxtforLColCnt1:=forLColCnt1
                nxtforLRowCnt:=forLRowCnt+stepsx.asUInt
                nxtforLRowCnt1:=forLRowCnt1+stepsx.asUInt/stepsy.asUInt
            }
        }
        .elsewhen(runningState===3.U){
            when(forRowCnt<subRow.asUInt){
                nextrunningState:=Mux(io.load_port.ready.asBool,5.U,3.U)
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=forColCnt
                nxtforRowCnt:=forRowCnt//+io.load_port.ready
                io.load_port.valid:=1.U
                io.load_port.bits:=(forLColCnt+forColCnt)*Row.asUInt+forRowCnt+forLRowCnt+io.baseAddr
                nxtforLColCnt:=forLColCnt
                nxtforLColCnt1:=forLColCnt1
                nxtforLRowCnt:=forLRowCnt
                nxtforLRowCnt1:=forLRowCnt1
            }
            .otherwise{
                nextrunningState:=1.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=forColCnt
                nxtforRowCnt:=0.U
                nxtforLColCnt:=forLColCnt
                nxtforLColCnt1:=forLColCnt1
                nxtforLRowCnt:=forLRowCnt
                nxtforLRowCnt1:=forLRowCnt1
            }
        }
        .elsewhen(runningState===4.U){
            when(forLRowCnt+subRow.asUInt<=Row.asUInt && forLColCnt+subCol.asUInt<=Col.asUInt){
                nextrunningState:=3.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtStoreReg:=(forLColCnt1+0.U)*io.newCol.asUInt+forLRowCnt1+0.U+io.baseAddr
                nxtforColCnt:=0.U
                nxtforRowCnt:=0.U
                nxtforLRowCnt:=forLRowCnt
                nxtforLRowCnt1:=forLRowCnt1
                nxtforLColCnt:=forLColCnt
                nxtforLColCnt1:=forLColCnt1
            }
            .otherwise{
                nextrunningState:=8.U
                io.predicate_in0.ready:= 0.U//~instruction_mem(1)
                io.predicate_in1.ready:= 0.U//(~instruction_mem(1))&(~instruction_mem(0))
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=0.U
                nxtforRowCnt:=0.U
                nxtforLColCnt:=forLColCnt+stepsy.asUInt
                nxtforLColCnt1:=forLColCnt1+1.U
                nxtforLRowCnt:=startPointRow.asUInt
                nxtforLRowCnt1:=startPointRow.asUInt/stepsy.asUInt
            }
        }
        .elsewhen(runningState===8.U){
            when(forLRowCnt+subRow.asUInt<=Row.asUInt && forLColCnt+subCol.asUInt<=Col.asUInt){
                nextrunningState:=3.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=0.U
                nxtforRowCnt:=0.U
                nxtStoreReg:=(forLColCnt1+0.U)*io.newCol.asUInt+startPointRow.asUInt/stepsy.asUInt+0.U+io.baseAddr
                nxtforLRowCnt:=startPointRow.asUInt
                nxtforLRowCnt1:=startPointRow.asUInt/stepsy.asUInt
                nxtforLColCnt:=forLColCnt
                nxtforLColCnt1:=forLColCnt1
            }
            .otherwise{
                nextrunningState:=2.U
                io.predicate_in0.ready:= ~instruction_mem(1)
                io.predicate_in1.ready:= (~instruction_mem(1))&(~instruction_mem(0))
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=0.U
                nxtforRowCnt:=0.U
                nxtforLColCnt:=0.U
                nxtforLColCnt1:=0.U
                nxtforLRowCnt:=0.U
                nxtforLRowCnt1:=0.U
            }
        }
        .elsewhen(runningState===5.U){
            when(io.load_data.valid.asBool){
                
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=io.load_data.bits
                io.data_out.valid:=1.U
                io.predicate_out.bits:=Mux(forColCnt===subCol.asUInt-1.U&&forRowCnt===subRow.asUInt-1.U,1.U,0.U)
                io.predicate_out.valid:=instruction_mem(2)
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=forColCnt
                nxtforRowCnt:=forRowCnt
               
                nxtforLColCnt:=forLColCnt
                nxtforLColCnt1:=forLColCnt1
                nxtforLRowCnt:=forLRowCnt
                nxtforLRowCnt1:=forLRowCnt1
                when(io.predicate_out.valid.asBool&&io.predicate_out.ready.asBool&&io.data_out.valid.asBool&&io.data_out.ready.asBool|| !instruction_mem(2)&&io.data_out.valid.asBool&&io.data_out.ready.asBool){
                    nextrunningState:=3.U
                    io.load_data.ready:=1.U
                    nxtforRowCnt:=forRowCnt+1.U
                }
                .elsewhen(io.predicate_out.valid.asBool&&io.predicate_out.ready.asBool&&instruction_mem(2)){
                    nextrunningState:=6.U
                }
                .elsewhen(io.data_out.valid.asBool&&io.data_out.ready.asBool&&instruction_mem(2)){
                    nextrunningState:=7.U
                    io.load_data.ready:=1.U
                }
                .otherwise{
                    nextrunningState:=5.U
                }
            }
            .otherwise{
                nextrunningState:=5.U
                io.predicate_in0.ready:=0.U
                io.predicate_in1.ready:=0.U
                io.data_out.bits:=0.U
                io.data_out.valid:=0.U
                io.predicate_out.bits:=0.U
                io.predicate_out.valid:=0.U
                //io.data_in0.ready:=0.U
                //io.data_in1.ready:=0.U
                nxtforColCnt:=forColCnt
                nxtforRowCnt:=forRowCnt
                nxtforLColCnt:=forLColCnt
                nxtforLColCnt1:=forLColCnt1
                nxtforLRowCnt:=forLRowCnt
                nxtforLRowCnt1:=forLRowCnt1
            }
        }
        .elsewhen(runningState===6.U){    
            io.predicate_in0.ready:=0.U
            io.predicate_in1.ready:=0.U
            io.data_out.bits:=io.load_data.bits
            io.data_out.valid:=1.U
            io.predicate_out.bits:=0.U
            io.predicate_out.valid:=0.U
            //io.data_in0.ready:=0.U
            //io.data_in1.ready:=0.U
            nxtforColCnt:=forColCnt
            nxtforRowCnt:=forRowCnt
            
            nxtforLColCnt:=forLColCnt
            nxtforLColCnt1:=forLColCnt1
            nxtforLRowCnt:=forLRowCnt
            nxtforLRowCnt1:=forLRowCnt1
            when(io.data_out.valid.asBool&&io.data_out.ready.asBool){
                nextrunningState:=3.U
                io.load_data.ready:=1.U
                nxtforRowCnt:=forRowCnt+1.U
            }
            .otherwise{
                nextrunningState:=6.U
            }
            
        }
        .elsewhen(runningState===7.U){    
            io.predicate_in0.ready:=0.U
            io.predicate_in1.ready:=0.U
            io.data_out.bits:=io.load_data.bits
            io.data_out.valid:=0.U
            io.predicate_out.bits:=Mux(forColCnt===subCol.asUInt-1.U&&forRowCnt===subRow.asUInt-1.U,1.U,0.U)
            io.predicate_out.valid:=instruction_mem(2)
            //io.data_in0.ready:=0.U
            //io.data_in1.ready:=0.U
            nxtforColCnt:=forColCnt
            nxtforRowCnt:=forRowCnt
            
            nxtforLColCnt:=forLColCnt
            nxtforLColCnt1:=forLColCnt1
            nxtforLRowCnt:=forLRowCnt
            nxtforLRowCnt1:=forLRowCnt1
            when(io.predicate_out.valid.asBool&&io.predicate_out.ready.asBool){
                nextrunningState:=3.U
                nxtforRowCnt:=forRowCnt+1.U
                // io.load_data.ready:=1.U
            }
            .otherwise{
                nextrunningState:=7.U
            }
            
        }
        .otherwise{
            io.rerun:=1.U
            io.predicate_in0.ready:=0.U
            io.predicate_in1.ready:=0.U
            io.data_out.bits:=0.U
            io.data_out.valid:=0.U
            io.predicate_out.bits:=0.U
            io.predicate_out.valid:=0.U
            //io.data_in0.ready:=0.U
            //io.data_in1.ready:=0.U
            nxtforColCnt:=0.U
            nxtforRowCnt:=0.U
            nxtforLColCnt:=0.U
            nxtforLColCnt1:=0.U
            nxtforLRowCnt:=0.U
            nxtforLRowCnt1:=0.U
            when(io.instruction_valid === 1.U){
                nextrunningState := 0.U
            }
            .otherwise{
                when(predicate_restart===1.U){
                    nextrunningState := 0.U
                }
                .otherwise{
                    nextrunningState := 2.U
                }
            }
        }
    }

    class resultStore(dataWidth:Int=32) extends Module{
        val io=IO(new Bundle{
            val data_in0 = Flipped(Decoupled(UInt(dataWidth.W)))
            // val predicate_in0 = Input(UInt(1.W))
            val storeBase = Input(UInt(dataWidth.W))
            // val storeStep = Input(UInt(dataWidth.W))
            val clear=Input(UInt(1.W))
            val rerun = Output(UInt(1.W))
            val store_port = Decoupled(UInt(dataWidth.W))
            val store_data = Output(UInt(dataWidth.W))
            val store_offset=Input(UInt(dataWidth.W))
            // val load_data = Flipped(Decoupled(UInt(dataWidth.W)))
        })
        // val storeCnt=RegInit(0.U(dataWidth.W))
        // val nxtStore=Wire(UInt(dataWidth.W))
        // storeCnt:=nxtStore
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        val nextValid0=Wire(UInt(1.W))
        io.data_in0.ready:=stage0Ready
        io.store_port.valid:=stage0Valid
        stage0Ready:=Mux(stage0Valid===0.U(1.W),1.U(1.W),io.store_port.ready&io.store_port.valid)
        stage0Valid:=nextValid0
        nextValid0:=Mux(io.data_in0.ready.asBool&&io.data_in0.valid.asBool,1.U(1.W),Mux(io.store_port.ready.asBool&&io.store_port.valid.asBool,0.U(1.W),stage0Valid))
        val store_data=Reg(UInt(dataWidth.W))
        val store_offset=Reg(UInt(dataWidth.W))
        store_offset:=Mux(io.data_in0.ready.asBool&&io.data_in0.valid.asBool,io.store_offset,Mux(io.store_port.ready.asBool&&io.store_port.valid.asBool,0.U,store_offset))
        store_data:=Mux(io.data_in0.ready.asBool&&io.data_in0.valid.asBool,io.data_in0.bits,Mux(io.store_port.ready.asBool&&io.store_port.valid.asBool,0.U,store_data))
        
        io.store_data:=store_data
        io.store_port.bits:=io.storeBase+store_offset
        // io.store_port.valid:=io.data_in0.valid
        // io.data_in0.ready:=io.store_port.ready
        // nxtStore:=Mux(io.clear.asBool,0.U,storeCnt+io.store_port.ready)
        io.rerun:= ~io.data_in0.valid & ~stage0Valid
    }

    object myMatrixLoad extends App {
        //(new chisel3.stage.ChiselStage).emitVerilog(new HEOOO(16, 64, 64, 64, 16, 8, 17, 7, 7, 32, 27, 64, 4, 4, 4, 1, 12, 12, 12, 2, 5, 8));
        //(new chisel3.stage.ChiselStage).emitVerilog(new IssueUnit(64, 16, 16, 7, 12));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new matrixLoadStore(16,32))))
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new resultStore(32))))
        
    }
}