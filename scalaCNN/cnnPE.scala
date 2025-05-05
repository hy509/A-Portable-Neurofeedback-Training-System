import chisel3._
import chisel3.util._
import ChiselLib._
import fpu._
package cnnPE{
    class MACCNN(val dataWidth: Int =32) extends Module{
        val io=IO(new Bundle{
            val inputA=Input(UInt(dataWidth.W))
            val inputB=Input(UInt(dataWidth.W))
            val inputC=Input(UInt(dataWidth.W))
            val inputValid=Input(UInt(1.W))
            val inputReady=Output(UInt(1.W))
            val AccumulateSelf=Output(UInt(dataWidth.W))
            val outputD=Output(UInt(dataWidth.W))
            val outputValid=Output(UInt(1.W))
            val outputReady=Input(UInt(1.W))
        })
        // val runningState=RegInit(0.U(8.W))
        // val nextState=Wire(UInt(8.W))
        // runningState:=nextState
        val macC=Reg(UInt(dataWidth.W))
        val macC2=Reg(UInt(dataWidth.W))
        //pipe stage0
        val fpuMul=Module(new fpu.pipeFPUMul32(dataWidth))
        val fpuAdd=Module(new fpu.combFPUSub32(dataWidth))
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        val nextValid0=Wire(UInt(1.W))
        stage0Ready:=Mux(io.outputValid===0.U(1.W),1.U(1.W),io.outputReady.asBool&&io.outputValid.asBool)
        stage0Valid:=nextValid0
        nextValid0:=Mux(io.inputValid.asBool&&io.inputReady.asBool,1.U(1.W),Mux(stage0Valid.asBool&&stage0Ready.asBool,0.U(1.W),stage0Valid))
        
        fpuMul.io.fpuA:=io.inputA
        fpuMul.io.fpuB:=io.inputB
        io.inputReady:=fpuMul.io.fpuReady
        fpuMul.io.fpuValid:=io.inputValid
        fpuMul.io.round_cfg:=1.U(1.W)
        val reverseC=Wire(Vec(dataWidth,UInt(1.W)))
        for(i<-0 to dataWidth-2){
            reverseC(i):=io.inputC(i)
        }
        reverseC(dataWidth-1):= ~io.inputC(dataWidth-1)
        when(io.inputReady.asBool && io.inputValid.asBool){
            macC:= reverseC.asUInt
        }
        .otherwise{
            macC:=macC
        }
        when(stage0Ready.asBool && stage0Valid.asBool){
            macC2:= macC
        }
        .otherwise{
            macC2:=macC2
        }
        io.AccumulateSelf:=fpuMul.io.fpuC
        fpuAdd.io.minA:=fpuMul.io.fpuC
        fpuAdd.io.minB:=macC2
        io.outputD:=fpuAdd.io.minC
        io.outputValid:=fpuMul.io.fpuCValid
        fpuMul.io.fpuCReady:=io.outputReady
    }
    class VectorMAC(val vecNum:Int,val dataWidth: Int =32) extends Module{//serve CNN
        val io=IO(new Bundle{
            val inputA=Input(Vec(vecNum,UInt(dataWidth.W)))
            val inputB=Input(Vec(vecNum,UInt(dataWidth.W)))
            val inputC=Input(Vec(vecNum,UInt(1.W)))
            val inputValid=Input(Vec(vecNum,UInt(1.W)))
            val inputReady=Output(Vec(vecNum,UInt(1.W)))
            // val AccumulateSelf=Input(UInt(1.W))
            val outputD=Output(Vec(vecNum,UInt(dataWidth.W)))
            val outputValid=Output(UInt(1.W))
            val outputReady=Input(UInt(1.W))
            val store_offset=Input(Vec(vecNum,UInt(dataWidth.W)))
            val store_offsetOut=Output(Vec(vecNum,UInt(dataWidth.W)))
            /////////////////////debug//////////////////////
            // val accumulate=Output(UInt(1.W))
            // val accumulate1=Output(UInt(1.W))
            // val puC=Output(UInt(dataWidth.W))
            // val cccIn=Output(UInt(1.W))
            // val cccInV=Output(UInt(1.W))
            // val cccInR=Output(UInt(1.W))
            // val peekAcc=Output(UInt(1.W))
            val outReady=Output(UInt(1.W))
            // val accumulate2=Output(UInt(1.W))
        })
        val warpperU = VecInit(Seq.fill(vecNum)(Module(new MACCNN(dataWidth)).io))
        val AccumulateSelf=RegInit(1.U(1.W))
        val AccumulateOutSelf=RegInit(0.U(1.W))
        val AccumulateOutSelf1=RegInit(0.U(1.W))
        val AccumulateOutSelf2=RegInit(0.U(1.W))
        val store_offReg=Reg(Vec(vecNum,UInt(dataWidth.W)))
        val store_offReg1=Reg(Vec(vecNum,UInt(dataWidth.W)))
        val AccumulateOutSelf3=RegInit(0.U(1.W))
        val nxtAccumulateSelf=Wire(UInt(1.W))
        val nxtAccumulateOutSelf=Wire(UInt(1.W))
        val nxtAccumulateOutSelf1=Wire(UInt(1.W))
        // val nxtAccumulateOutSelf2=Wire(UInt(1.W))
        AccumulateSelf:=nxtAccumulateSelf
        AccumulateOutSelf:=nxtAccumulateOutSelf
        AccumulateOutSelf1:=nxtAccumulateOutSelf1
        val validIn=Wire(UInt(1.W))
        validIn:=io.inputValid.reduceTree(_&_)
        val readyInVec=Wire(Vec(vecNum,UInt(1.W)))
        val readyIn=Wire(UInt(1.W))
        readyIn:=readyInVec.reduceTree(_&_)
        val validOutVec=Wire(Vec(vecNum,UInt(1.W)))
        val validOut=Wire(UInt(1.W))
        validOut:=validOutVec.reduceTree(_&_)
        val fpuC=Wire(Vec(vecNum,UInt(dataWidth.W)))
        val Cin=Wire(UInt(1.W))
        val notCin=Wire(UInt(1.W))
        ////////////////debug////////////////////
        // io.accumulate:=AccumulateSelf
        // io.accumulate1:=AccumulateOutSelf
        // io.puC:=fpuC(0)
        // io.cccIn:=notCin
        // io.cccInR:=readyIn
        // io.cccInV:=validIn
        // io.peekAcc:=nxtAccumulateSelf
        io.outReady:=warpperU(0).outputValid
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        ////////////////debug////////////////////
        io.outputValid:=Mux(AccumulateOutSelf1.asBool,validOut,0.U)
        for(i<-0 to vecNum-1){
            io.inputReady(i):=readyIn&Mux(AccumulateSelf.asBool,1.U,validOut)&validIn
            readyInVec(i):=warpperU(i).inputReady
            validOutVec(i):=warpperU(i).outputValid
            io.outputD(i):=Mux(AccumulateOutSelf1.asBool,warpperU(i).outputD,0.U)
            warpperU(i).outputReady:=Mux(AccumulateOutSelf1.asBool,io.outputReady,Mux(AccumulateSelf.asBool,1.U,validOut)&validIn)
            store_offReg(i):=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, io.store_offset(i),Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,store_offReg(i),store_offReg(i)))
            store_offReg1(i):=Mux(stage0Ready.asBool&&stage0Valid.asBool,store_offReg(i),store_offReg1(i))
            io.store_offsetOut(i):=store_offReg1(i)
        }
        
        Cin:=io.inputC.reduceTree(_&_)
        
        notCin:= ~Cin
        
        val nextValid0=Wire(UInt(1.W))
        stage0Ready:=Mux(validOut===0.U(1.W),1.U(1.W),warpperU(0).outputReady.asBool&&validOut.asBool)
        stage0Valid:=nextValid0
        nextValid0:=Mux(validIn.asBool&&readyIn.asBool,1.U(1.W),Mux(stage0Valid.asBool&&stage0Ready.asBool,0.U(1.W),stage0Valid))
        
        
        nxtAccumulateSelf:=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, 1.U,Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,0.U,AccumulateSelf))
        nxtAccumulateOutSelf:=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, 1.U,Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,0.U,AccumulateOutSelf))
        nxtAccumulateOutSelf1:=Mux(stage0Ready.asBool&&stage0Valid.asBool,AccumulateOutSelf,AccumulateOutSelf1)
        // AccumulateOutSelf2:=AccumulateOutSelf1
        
        for(i<-0 to vecNum-1){
            fpuC(i):=Mux(AccumulateSelf.asBool,0.U,warpperU(i).outputD)
            warpperU(i).inputA:=io.inputA(i)
            warpperU(i).inputB:=io.inputB(i)
            warpperU(i).inputC:=fpuC(i)
            warpperU(i).inputValid:=validIn&Mux(AccumulateSelf.asBool,1.U,validOut)
        }
        
    }
    class FloatMax(val dataWidth: Int =32) extends Module{
        val io=IO(new Bundle{
            val inputA=Input(UInt(dataWidth.W))
            val inputB=Input(UInt(dataWidth.W))
            // val inputC=Input(UInt(dataWidth.W))
            val inputValid=Input(UInt(1.W))
            val inputReady=Output(UInt(1.W))
            // val AccumulateSelf=Input(UInt(1.W))
            val outputD=Output(UInt(dataWidth.W))
            val outputValid=Output(UInt(1.W))
            val outputReady=Input(UInt(1.W))
            
        })
        val Maximum=Reg(UInt(dataWidth.W))
        val RegState=RegInit(0.U(1.W))
        io.inputReady:=RegState===0.U | RegState===1.U & io.outputReady & io.outputValid & io.inputValid
        RegState:=Mux(RegState===1.U && io.outputReady.asBool && io.outputValid.asBool && io.inputValid.asBool || RegState===0.U && io.inputReady.asBool && io.inputValid.asBool, 1.U, Mux(RegState===1.U && io.outputReady.asBool && io.outputValid.asBool,0.U,RegState))
        io.outputValid:=RegState===1.U
        val MaxWire=Wire(UInt(dataWidth.W))
        val fpuAdd=Module(new fpu.combFPUSub32(dataWidth))
        fpuAdd.io.minA:=io.inputA
        fpuAdd.io.minB:=io.inputB
        val resultsC=fpuAdd.io.minC
        MaxWire:=Mux(resultsC(dataWidth-1)===0.U,io.inputA,io.inputB)
        Maximum:=Mux(io.inputReady.asBool && io.inputValid.asBool,MaxWire,Maximum)
        io.outputD:=Maximum
    }
    class VectorCompare(val vecNum:Int=32,val dataWidth: Int =32) extends Module{//serve maxpool
        val io=IO(new Bundle{
            val inputA=Input(Vec(vecNum,UInt(dataWidth.W)))
            // val inputB=Input(Vec(vecNum,UInt(dataWidth.W)))
            val inputC=Input(Vec(vecNum,UInt(1.W)))
            val inputValid=Input(Vec(vecNum,UInt(1.W)))
            val inputReady=Output(Vec(vecNum,UInt(1.W)))
            // val AccumulateSelf=Input(UInt(1.W))
            val outputD=Output(Vec(vecNum,UInt(dataWidth.W)))
            val outputValid=Output(UInt(1.W))
            val outputReady=Input(UInt(1.W))
            val outReady=Output(UInt(1.W))
            val store_offset=Input(Vec(vecNum,UInt(dataWidth.W)))
            val store_offsetOut=Output(Vec(vecNum,UInt(dataWidth.W)))
        })
        val store_offReg=Reg(Vec(vecNum,UInt(dataWidth.W)))
        val store_offReg1=Reg(Vec(vecNum,UInt(dataWidth.W)))
        val warpperU = VecInit(Seq.fill(vecNum)(Module(new FloatMax(dataWidth)).io))
        val AccumulateSelf=RegInit(1.U(1.W))
        val AccumulateOutSelf=RegInit(0.U(1.W))
        val AccumulateOutSelf1=RegInit(0.U(1.W))
        val AccumulateOutSelf2=RegInit(0.U(1.W))
        val AccumulateOutSelf3=RegInit(0.U(1.W))
        val nxtAccumulateSelf=Wire(UInt(1.W))
        val nxtAccumulateOutSelf=Wire(UInt(1.W))
        val nxtAccumulateOutSelf1=Wire(UInt(1.W))
        io.outReady:=warpperU(0).outputValid
        // val nxtAccumulateOutSelf2=Wire(UInt(1.W))
        AccumulateSelf:=nxtAccumulateSelf
        AccumulateOutSelf:=nxtAccumulateOutSelf
        AccumulateOutSelf1:=nxtAccumulateOutSelf1
        val validIn=Wire(UInt(1.W))
        validIn:=io.inputValid.reduceTree(_&_)
        val readyInVec=Wire(Vec(vecNum,UInt(1.W)))
        val readyIn=Wire(UInt(1.W))
        readyIn:=readyInVec.reduceTree(_&_)
        val validOutVec=Wire(Vec(vecNum,UInt(1.W)))
        val validOut=Wire(UInt(1.W))
        validOut:=validOutVec.reduceTree(_&_)
        io.outputValid:=Mux(AccumulateOutSelf.asBool,validOut,0.U)
        val Cin=io.inputC.reduceTree(_&_)
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        val notCin=Wire(UInt(1.W))
        for(i<-0 to vecNum-1){
            io.inputReady(i):=readyIn&validIn&Mux(AccumulateSelf.asBool,1.U,validOut)
            readyInVec(i):=warpperU(i).inputReady
            validOutVec(i):=warpperU(i).outputValid
            io.outputD(i):=Mux(AccumulateOutSelf.asBool,warpperU(i).outputD,0.U)
            warpperU(i).outputReady:=Mux(AccumulateOutSelf.asBool,io.outputReady,Mux(AccumulateSelf.asBool,1.U,validOut)&validIn)
            store_offReg(i):=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, io.store_offset(i),Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,store_offReg(i),store_offReg(i)))
            store_offReg1(i):=Mux(stage0Ready.asBool&&stage0Valid.asBool,store_offReg(i),store_offReg1(i))
            io.store_offsetOut(i):=store_offReg(i)
        }
        
        val nextValid0=Wire(UInt(1.W))
        stage0Ready:=Mux(validOut===0.U(1.W),1.U(1.W),warpperU(0).outputReady.asBool&&validOut.asBool)
        stage0Valid:=nextValid0
        nextValid0:=Mux(validIn.asBool&&readyIn.asBool,1.U(1.W),Mux(stage0Valid.asBool&&stage0Ready.asBool,0.U(1.W),stage0Valid))
        
        
        notCin:= ~Cin
        nxtAccumulateSelf:=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, 1.U,Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,0.U,AccumulateSelf))
        nxtAccumulateOutSelf:=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, 1.U,Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,0.U,AccumulateOutSelf))
        nxtAccumulateOutSelf1:=Mux(stage0Ready.asBool&&stage0Valid.asBool,AccumulateOutSelf,AccumulateOutSelf1)
        // AccumulateOutSelf2:=AccumulateOutSelf1
        val fpuC=Wire(Vec(vecNum,UInt(dataWidth.W)))
        for(i<-0 to vecNum-1){
            fpuC(i):=Mux(AccumulateSelf.asBool,0.U,warpperU(i).outputD)
            warpperU(i).inputA:=io.inputA(i)
            warpperU(i).inputB:=fpuC(i)
            // warpperU(i).inputC:=fpuC(i)
            warpperU(i).inputValid:=validIn&Mux(AccumulateSelf.asBool,1.U,validOut)
        }
        
    }



    class pipeFPUPass2(val dataWidth: Int =32) extends Module{
        val io=IO(new Bundle{
            val fpuA=Input(UInt(dataWidth.W))
            val fpuB=Input(UInt(dataWidth.W))
            val fpuReady=Output(UInt(1.W))
            val fpuValid=Input(UInt(1.W))
            val fpuC=Output(UInt(dataWidth.W))
            val fpuCReady=Input(UInt(1.W))
            val fpuCValid=Output(UInt(1.W))
            val round_cfg=Input(UInt(1.W))
            val overflow=Output(UInt(2.W))
        })
        val Results=Wire(UInt(dataWidth.W))
        io.fpuC:=Results.asUInt
        // val s1=Wire(UInt(1.W))
        // val s2=Wire(UInt(1.W))
        // val exp1=Wire(Vec(8,UInt(1.W)))
        // val exp2=Wire(Vec(8,UInt(1.W)))
        // val man1=Wire(Vec(24,UInt(1.W)))
        // val man2=Wire(Vec(24,UInt(1.W)))
        // val man1A=Wire(Vec(23,UInt(1.W)))
        // val man2B=Wire(Vec(23,UInt(1.W)))
        // val n=Wire(UInt(1.W))
        // val temp1=Wire(UInt(10.W))
        // val temp2=Wire(UInt(10.W))
        // val temp3=Wire(UInt(10.W))
        // val mul_out_p=Wire(UInt(48.W))
        //-------'s' the sign，'e' exponential，'m' tail------------//
        //first pipe logic
        // val one_s_out=Wire(UInt(1.W))
        // val one_e_out=Wire(UInt(10.W))
        // val one_m_out=Wire(UInt(48.W))
        
        // //first pipe reg
        // val one_s_reg=Reg(UInt(1.W))
        // val one_e_reg=Reg(UInt(10.W))
        // val one_m_reg=Reg(UInt(48.W))

        // //second pipe logic
        // val two_f_out=Wire(UInt(2.W))
        // val two_e_out=Wire(UInt(8.W))
        // val two_m_out=Wire(UInt(23.W))

        // //second pipe reg
        // val two_s_reg=Reg(UInt(1.W))
        // val two_f_reg=Reg(UInt(2.W))
        // val two_e_reg=Reg(UInt(8.W))
        // val two_m_reg=Reg(UInt(23.W))
        val Apass0=Reg(UInt(dataWidth.W))
        val Apass1=Reg(UInt(dataWidth.W))
        // stage Ready and Valid
        val stateReg=RegInit(VecInit(Seq.fill(2)(0.U((1).W))))  
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        val stage1Valid=RegInit(0.U(1.W))
        // val isNAN=RegInit(0.U(1.W))
        // val isINF=RegInit(0.U(1.W))
        // val isNANs=RegInit(0.U(1.W))
        // val isINFs=RegInit(0.U(1.W))
        val nextValid0=Wire(UInt(1.W))
        val nextValid1=Wire(UInt(1.W))
        io.fpuReady:= Mux(stage0Valid===0.U(1.W),1.U(1.W),stage0Valid.asBool&&stage0Ready.asBool)
        stage0Ready:=Mux(stage1Valid===0.U(1.W),1.U(1.W),io.fpuCReady.asBool&&io.fpuCValid.asBool)
        stateReg(0):=Mux(stateReg(0)===0.U(1.W),io.fpuValid,Mux(stage0Valid.asBool&&stage0Ready.asBool&&io.fpuValid.asBool,1.U(1.W),Mux(stage0Valid.asBool&&stage0Ready.asBool,0.U(1.W),stateReg(0))))
        stateReg(1):=Mux(stateReg(1)===0.U(1.W),stage0Valid,Mux(io.fpuCReady.asBool&&io.fpuCValid.asBool&&stage0Valid.asBool,1.U(1.W),Mux(io.fpuCReady.asBool&&io.fpuCValid.asBool,0.U(1.W),stateReg(1))))
        stage0Valid:=nextValid0
        stage1Valid:=nextValid1
        
        nextValid0:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,1.U(1.W),Mux(stage0Valid.asBool&&stage0Ready.asBool,0.U(1.W),stage0Valid))
        nextValid1:=Mux(stage0Valid.asBool&&stage0Ready.asBool,1.U(1.W),Mux(io.fpuCValid.asBool&&io.fpuCReady.asBool,0.U(1.W),stage1Valid))
        io.fpuCValid:=stage1Valid
        

        Apass0:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,io.fpuA,Apass0)
        Apass1:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Apass0,Apass1)
        /*---------------extract flout_a and flout_b's s,m,e---------------------*/
        // s1:=io.fpuA(dataWidth-1)
        // for(i<- 0 to 7){
        //     exp1(i):=io.fpuA(23+i)
        // }
        // for(i<- 0 to 22){
        //     man1(i):=io.fpuA(i)
        //     man1A(i):=io.fpuA(i)
        // }
        // man1(23):=1.U(1.W)
        // s2:=io.fpuB(dataWidth-1)
        // for(i<- 0 to 7){
        //     exp2(i):=io.fpuB(23+i)
        // }
        // for(i<- 0 to 22){
        //     man2(i):=io.fpuB(i)
        //     man2B(i):=io.fpuB(i)
        // }
        // man2(23):=1.U(1.W)
        // /*--------------------first logic---------------------------------*/
        // //sign
        // one_s_out := s1 ^ s2;   //xor input sign
        
        // //mul the man
        // // one_m_out:=Mux(man1.asUInt === 8388608.U(24.W) || man2.asUInt === 8388608.U(24.W), 0.U(48.W), man1.asUInt * man2.asUInt)
        // one_m_out:=man1.asUInt * man2.asUInt

        // //double sign the exponential
        // val TmpExp1=Wire(Vec(7,UInt(1.W)))
        // for(i<- 0 to 6){
        //     TmpExp1(i):=exp1(i)
        // }
        // temp1:=Mux(exp1(7) === 1.U(1.W), Cat(0.U(3.W),TmpExp1.asUInt()), Cat(7.U(3.W),TmpExp1.asUInt()))
        // val TmpExp2=Wire(Vec(7,UInt(1.W)))
        // for(i<- 0 to 6){
        //     TmpExp2(i):=exp2(i)
        // }
        // temp2:=Mux(exp2(7) === 1.U(1.W), Cat(0.U(3.W),TmpExp2.asUInt()), Cat(7.U(3.W),TmpExp2.asUInt()))

        // //add the exponential
        // one_e_out := temp1+ temp2
        // /*--------------------first reg---------------------------------*/
        // one_s_reg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,one_s_out,one_s_reg)
        // val signSec=Reg(UInt(1.W))
        // one_e_reg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,one_e_out,one_e_reg)
        // one_m_reg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,one_m_out,one_m_reg) 
        // val a_eW=Wire(Vec(10,UInt(1.W)))
        // val b_eW=Wire(Vec(10,UInt(1.W)))
        // val a_e=Wire((UInt(10.W)))
        // val b_e=Wire((UInt(10.W)))
        // a_e:=a_eW.asUInt-127.U
        // b_e:=b_eW.asUInt-127.U
        // a_eW(8):=0.U(1.W)
        // a_eW(9):=0.U(1.W)
        // b_eW(8):=0.U(1.W)
        // b_eW(9):=0.U(1.W)
        // for(i<- 0 to 7){
        //     b_eW(i):=io.fpuB(23+i)
        //     a_eW(i):=io.fpuA(23+i)
        // }
        // val isZero=RegInit(0.U(1.W))
        // val isZeros=RegInit(0.U(1.W))
        // isZero:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,Mux(((a_e.asSInt) === -127.S(10.W)) && (man1A.asUInt === 0.U) || ((b_e.asSInt) === -127.S(10.W)) && (man2B.asUInt === 0.U),1.U,0.U),isZero)
        // isNAN:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,Mux(a_e===128.U(10.W)&&man1A.asUInt=/=0.U(24.U)||b_e===128.U(10.W)&&man2B.asUInt=/=0.U(24.U),1.U,Mux(a_e===128.U(10.W)&&b_e.asUInt.asSInt=== -127.S(10.W)&&man2B.asUInt===0.U||b_e===128.U(10.W)&&a_e.asUInt.asSInt=== -127.S(10.W)&&man1A.asUInt===0.U,1.U,0.U)),isNAN) 
        // isINF:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,Mux(a_e===128.U||b_e===128.U,1.U,0.U),isINF)
        // val roundReg=Reg(UInt(1.W))
        // roundReg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,io.round_cfg,roundReg) 
        // /*--------------------second logic---------------------------------*/
        // //regulate the tail
        // val mul_out_p_cut=Wire(Vec(23,UInt(1.W)))
        // for(i<- 0 to 22){
        //     mul_out_p_cut(i):=mul_out_p(23+i)
        // }
        // two_m_out:=Mux(one_m_reg === 0.U(48.W),0.U(23.W),Mux(roundReg===1.U(1.W),Mux(mul_out_p(22)===1.U(1.W),mul_out_p_cut.asUInt+1.U(23.W),mul_out_p_cut.asUInt),mul_out_p_cut.asUInt))
        // n:=Mux(one_m_reg === 0.U(48.W),0.U(1.W),Mux(one_m_reg(47)===1.U(1.W),1.U(1.W),0.U(1.W)))
        // mul_out_p:=Mux(one_m_reg === 0.U(48.W),0.U(23.W),Mux(one_m_reg(47)===1.U(1.W),one_m_reg >> 1.U,one_m_reg))
        
        // //double signed, 01 is upper overflow, 10 is downside overflow
        // val one_e_cut=Wire(Vec(10,UInt(1.W)))
        // for(i<- 0 to 9){
        //     one_e_cut(i):=one_e_reg(i)
        // }
        // temp3:=one_e_cut.asUInt+n+1.U(1.W)
        // val doubleSign=Wire(Vec(2,UInt(1.W)))
        // val tmp3Cut=Wire(Vec(7,UInt(1.W)))
        // for(i<- 0 to 6){
        //     tmp3Cut(i):=temp3(i)
        // }
        // doubleSign(1):=temp3(9)
        // doubleSign(0):=temp3(8)
        // two_f_out:=Mux(doubleSign.asUInt===1.U(2.W),1.U(2.W),Mux(doubleSign.asUInt===2.U(2.W),2.U(2.W),0.U(2.W)))
        // two_e_out:=Mux(temp3(7)===1.U(1.W),Cat(0.U(1.W),tmp3Cut.asUInt()),Cat(1.U(1.W),tmp3Cut.asUInt()))
        // /*-------------------second reg------------------------------------*/
        // signSec:=Mux(stage0Ready.asBool&&stage0Valid.asBool,one_s_reg,signSec)
        // two_s_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(1.W),one_s_reg),two_s_reg)
        // two_f_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(2.W),two_f_out),two_f_reg)
        // two_m_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(23.W),two_m_out),two_m_reg)
        // two_e_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(8.W),two_e_out),two_e_reg)
        // isINFs:=Mux(stage0Ready.asBool&&stage0Valid.asBool,isINF,isINFs)
        // isNANs:=Mux(stage0Ready.asBool&&stage0Valid.asBool,isNAN,isNANs)
        // isZeros:=Mux(stage0Ready.asBool&&stage0Valid.asBool,isZero,isZeros)
        //output Results
        io.overflow:=0.U
        Results:=Apass1

    }

    class ADDCNN(val dataWidth: Int =32) extends Module{
        val io=IO(new Bundle{
            val inputA=Input(UInt(dataWidth.W))
            val inputB=Input(UInt(dataWidth.W))
            val inputC=Input(UInt(dataWidth.W))
            val inputValid=Input(UInt(1.W))
            val inputReady=Output(UInt(1.W))
            val AccumulateSelf=Output(UInt(dataWidth.W))
            val outputD=Output(UInt(dataWidth.W))
            val outputValid=Output(UInt(1.W))
            val outputReady=Input(UInt(1.W))
        })
        // val runningState=RegInit(0.U(8.W))
        // val nextState=Wire(UInt(8.W))
        // runningState:=nextState
        val macC=Reg(UInt(dataWidth.W))
        val macC2=Reg(UInt(dataWidth.W))
        //pipe stage0
        val fpuMul=Module(new pipeFPUPass2(dataWidth))
        val fpuAdd=Module(new fpu.combFPUSub32(dataWidth))
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        val nextValid0=Wire(UInt(1.W))
        stage0Ready:=Mux(io.outputValid===0.U(1.W),1.U(1.W),io.outputReady.asBool&&io.outputValid.asBool)
        stage0Valid:=nextValid0
        nextValid0:=Mux(io.inputValid.asBool&&io.inputReady.asBool,1.U(1.W),Mux(stage0Valid.asBool&&stage0Ready.asBool,0.U(1.W),stage0Valid))
        
        fpuMul.io.fpuA:=io.inputA
        fpuMul.io.fpuB:=0.U
        io.inputReady:=fpuMul.io.fpuReady
        fpuMul.io.fpuValid:=io.inputValid
        fpuMul.io.round_cfg:=1.U(1.W)
        val reverseC=Wire(Vec(dataWidth,UInt(1.W)))
        for(i<-0 to dataWidth-2){
            reverseC(i):=io.inputB(i)
        }
        reverseC(dataWidth-1):= ~io.inputB(dataWidth-1)
        when(io.inputReady.asBool && io.inputValid.asBool){
            macC:= reverseC.asUInt
        }
        .otherwise{
            macC:=macC
        }
        when(stage0Ready.asBool && stage0Valid.asBool){
            macC2:= macC
        }
        .otherwise{
            macC2:=macC2
        }
        io.AccumulateSelf:=fpuMul.io.fpuC
        fpuAdd.io.minA:=fpuMul.io.fpuC
        fpuAdd.io.minB:=macC2
        io.outputD:=fpuAdd.io.minC
        io.outputValid:=fpuMul.io.fpuCValid
        fpuMul.io.fpuCReady:=io.outputReady
    }

    class VectorADD(val vecNum:Int,val dataWidth: Int =32) extends Module{//serve CNN
        val io=IO(new Bundle{
            val inputA=Input(Vec(vecNum,UInt(dataWidth.W)))
            val inputB=Input(Vec(vecNum,UInt(dataWidth.W)))
            val inputC=Input(Vec(vecNum,UInt(1.W)))
            val inputValid=Input(Vec(vecNum,UInt(1.W)))
            val inputReady=Output(Vec(vecNum,UInt(1.W)))
            // val AccumulateSelf=Input(UInt(1.W))
            val outputD=Output(Vec(vecNum,UInt(dataWidth.W)))
            val outputValid=Output(UInt(1.W))
            val outputReady=Input(UInt(1.W))
            val store_offset=Input(Vec(vecNum,UInt(dataWidth.W)))
            val store_offsetOut=Output(Vec(vecNum,UInt(dataWidth.W)))
            /////////////////////debug//////////////////////
            // val accumulate=Output(UInt(1.W))
            // val accumulate1=Output(UInt(1.W))
            // val puC=Output(UInt(dataWidth.W))
            // val cccIn=Output(UInt(1.W))
            // val cccInV=Output(UInt(1.W))
            // val cccInR=Output(UInt(1.W))
            // val peekAcc=Output(UInt(1.W))
            val outReady=Output(UInt(1.W))
            // val accumulate2=Output(UInt(1.W))
        })
        val warpperU = VecInit(Seq.fill(vecNum)(Module(new ADDCNN(dataWidth)).io))
        val AccumulateSelf=RegInit(1.U(1.W))
        val AccumulateOutSelf=RegInit(0.U(1.W))
        val AccumulateOutSelf1=RegInit(0.U(1.W))
        val AccumulateOutSelf2=RegInit(0.U(1.W))
        val store_offReg=Reg(Vec(vecNum,UInt(dataWidth.W)))
        val store_offReg1=Reg(Vec(vecNum,UInt(dataWidth.W)))
        val AccumulateOutSelf3=RegInit(0.U(1.W))
        val nxtAccumulateSelf=Wire(UInt(1.W))
        val nxtAccumulateOutSelf=Wire(UInt(1.W))
        val nxtAccumulateOutSelf1=Wire(UInt(1.W))
        // val nxtAccumulateOutSelf2=Wire(UInt(1.W))
        AccumulateSelf:=nxtAccumulateSelf
        AccumulateOutSelf:=nxtAccumulateOutSelf
        AccumulateOutSelf1:=nxtAccumulateOutSelf1
        val validIn=Wire(UInt(1.W))
        validIn:=io.inputValid.reduceTree(_&_)
        val readyInVec=Wire(Vec(vecNum,UInt(1.W)))
        val readyIn=Wire(UInt(1.W))
        readyIn:=readyInVec.reduceTree(_&_)
        val validOutVec=Wire(Vec(vecNum,UInt(1.W)))
        val validOut=Wire(UInt(1.W))
        validOut:=validOutVec.reduceTree(_&_)
        val fpuC=Wire(Vec(vecNum,UInt(dataWidth.W)))
        val Cin=Wire(UInt(1.W))
        val notCin=Wire(UInt(1.W))
        ////////////////debug////////////////////
        // io.accumulate:=AccumulateSelf
        // io.accumulate1:=AccumulateOutSelf
        // io.puC:=fpuC(0)
        // io.cccIn:=notCin
        // io.cccInR:=readyIn
        // io.cccInV:=validIn
        // io.peekAcc:=nxtAccumulateSelf
        io.outReady:=warpperU(0).outputValid
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        ////////////////debug////////////////////
        io.outputValid:=Mux(AccumulateOutSelf1.asBool,validOut,0.U)
        for(i<-0 to vecNum-1){
            io.inputReady(i):=readyIn&Mux(AccumulateSelf.asBool,1.U,validOut)&validIn
            readyInVec(i):=warpperU(i).inputReady
            validOutVec(i):=warpperU(i).outputValid
            io.outputD(i):=Mux(AccumulateOutSelf1.asBool,warpperU(i).outputD,0.U)
            warpperU(i).outputReady:=Mux(AccumulateOutSelf1.asBool,io.outputReady,Mux(AccumulateSelf.asBool,1.U,validOut)&validIn)
            store_offReg(i):=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, io.store_offset(i),Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,store_offReg(i),store_offReg(i)))
            store_offReg1(i):=Mux(stage0Ready.asBool&&stage0Valid.asBool,store_offReg(i),store_offReg1(i))
            io.store_offsetOut(i):=store_offReg1(i)
        }
        
        Cin:=io.inputC.reduceTree(_&_)
        
        notCin:= ~Cin
        
        val nextValid0=Wire(UInt(1.W))
        stage0Ready:=Mux(validOut===0.U(1.W),1.U(1.W),warpperU(0).outputReady.asBool&&validOut.asBool)
        stage0Valid:=nextValid0
        nextValid0:=Mux(validIn.asBool&&readyIn.asBool,1.U(1.W),Mux(stage0Valid.asBool&&stage0Ready.asBool,0.U(1.W),stage0Valid))
        
        
        nxtAccumulateSelf:=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, 1.U,Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,0.U,AccumulateSelf))
        nxtAccumulateOutSelf:=Mux(Cin.asBool && validIn.asBool&&readyIn.asBool, 1.U,Mux(notCin.asBool && validIn.asBool&&readyIn.asBool,0.U,AccumulateOutSelf))
        nxtAccumulateOutSelf1:=Mux(stage0Ready.asBool&&stage0Valid.asBool,AccumulateOutSelf,AccumulateOutSelf1)
        // AccumulateOutSelf2:=AccumulateOutSelf1
        
        for(i<-0 to vecNum-1){
            fpuC(i):=Mux(AccumulateSelf.asBool,0.U,warpperU(i).outputD)
            warpperU(i).inputA:=io.inputA(i)
            warpperU(i).inputB:=io.inputB(i)
            warpperU(i).inputC:=fpuC(i)
            warpperU(i).inputValid:=validIn&Mux(AccumulateSelf.asBool,1.U,validOut)
        }
        
    }

    object myVecCompare extends App {
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new VectorCompare())));
        // (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new ArbiterArbitrary(4,32))));
    }
}