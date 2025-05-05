import chisel3._
import chisel3.util._
import ChiselLib._

package fpu{
    class pipeFPUMul32(val dataWidth: Int =32) extends Module{
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
        val s1=Wire(UInt(1.W))
        val s2=Wire(UInt(1.W))
        val exp1=Wire(Vec(8,UInt(1.W)))
        val exp2=Wire(Vec(8,UInt(1.W)))
        val man1=Wire(Vec(24,UInt(1.W)))
        val man2=Wire(Vec(24,UInt(1.W)))
        val man1da=Wire(UInt(24.W))
        val man2da=Wire(UInt(24.W))
        val man1A=Wire(Vec(23,UInt(1.W)))
        val man2B=Wire(Vec(23,UInt(1.W)))
        val n=Wire(UInt(1.W))
        val temp1=Wire(UInt(10.W))
        val temp2=Wire(UInt(10.W))
        val temp3=Wire(UInt(10.W))
        val mul_out_p=Wire(UInt(48.W))
        //-------'s' the sign，'e' exponential，'m' tail------------//
        //first pipe logic
        val one_s_out=Wire(UInt(1.W))
        val one_e_out=Wire(UInt(10.W))
        val one_m_out=Wire(UInt(48.W))
        
        //first pipe reg
        val one_s_reg=Reg(UInt(1.W))
        val one_e_reg=Reg(UInt(10.W))
        val one_m_reg=Reg(UInt(48.W))

        //second pipe logic
        val two_f_out=Wire(UInt(2.W))
        val two_e_out=Wire(UInt(8.W))
        val two_m_out=Wire(UInt(23.W))

        //second pipe reg
        val two_s_reg=Reg(UInt(1.W))
        val two_f_reg=Reg(UInt(2.W))
        val two_e_reg=Reg(UInt(8.W))
        val two_m_reg=Reg(UInt(23.W))
        // stage Ready and Valid
        val stateReg=RegInit(VecInit(Seq.fill(2)(0.U((1).W))))  
        val stage0Ready=Wire(UInt(1.W))
        val stage0Valid=RegInit(0.U(1.W))
        val stage1Valid=RegInit(0.U(1.W))
        val isNAN=RegInit(0.U(1.W))
        val isINF=RegInit(0.U(1.W))
        val isNANs=RegInit(0.U(1.W))
        val isINFs=RegInit(0.U(1.W))
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
        
        /*---------------extract flout_a and flout_b's s,m,e---------------------*/
        s1:=io.fpuA(dataWidth-1)
        for(i<- 0 to 7){
            exp1(i):=io.fpuA(23+i)
        }
        for(i<- 0 to 22){
            man1(i):=io.fpuA(i)
            man1A(i):=io.fpuA(i)
        }
        man1(23):=1.U(1.W)
        s2:=io.fpuB(dataWidth-1)
        for(i<- 0 to 7){
            exp2(i):=io.fpuB(23+i)
        }
        for(i<- 0 to 22){
            man2(i):=io.fpuB(i)
            man2B(i):=io.fpuB(i)
        }
        man2(23):=1.U(1.W)
        man1da:=man1.asUInt()
        man2da:=man2.asUInt()
        /*--------------------first logic---------------------------------*/
        //sign
        one_s_out := s1 ^ s2;   //xor input sign
        
        //mul the man
        // one_m_out:=Mux(man1.asUInt === 8388608.U(24.W) || man2.asUInt === 8388608.U(24.W), 0.U(48.W), man1.asUInt * man2.asUInt)
        one_m_out:=man1da * man2da

        //double sign the exponential
        val TmpExp1=Wire(Vec(7,UInt(1.W)))
        for(i<- 0 to 6){
            TmpExp1(i):=exp1(i)
        }
        temp1:=Mux(exp1(7) === 1.U(1.W), Cat(0.U(3.W),TmpExp1.asUInt()), Cat(7.U(3.W),TmpExp1.asUInt()))
        val TmpExp2=Wire(Vec(7,UInt(1.W)))
        for(i<- 0 to 6){
            TmpExp2(i):=exp2(i)
        }
        temp2:=Mux(exp2(7) === 1.U(1.W), Cat(0.U(3.W),TmpExp2.asUInt()), Cat(7.U(3.W),TmpExp2.asUInt()))

        //add the exponential
        one_e_out := temp1+ temp2
        /*--------------------first reg---------------------------------*/
        one_s_reg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,one_s_out,one_s_reg)
        val signSec=Reg(UInt(1.W))
        one_e_reg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,one_e_out,one_e_reg)
        one_m_reg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,one_m_out,one_m_reg) 
        val a_eW=Wire(Vec(10,UInt(1.W)))
        val b_eW=Wire(Vec(10,UInt(1.W)))
        val a_e=Wire((UInt(10.W)))
        val b_e=Wire((UInt(10.W)))
        a_e:=a_eW.asUInt-127.U
        b_e:=b_eW.asUInt-127.U
        a_eW(8):=0.U(1.W)
        a_eW(9):=0.U(1.W)
        b_eW(8):=0.U(1.W)
        b_eW(9):=0.U(1.W)
        for(i<- 0 to 7){
            b_eW(i):=io.fpuB(23+i)
            a_eW(i):=io.fpuA(23+i)
        }
        val isZero=RegInit(0.U(1.W))
        val isZeros=RegInit(0.U(1.W))
        isZero:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,Mux(((a_e.asSInt) === -127.S(10.W)) && (man1A.asUInt === 0.U) || ((b_e.asSInt) === -127.S(10.W)) && (man2B.asUInt === 0.U),1.U,0.U),isZero)
        isNAN:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,Mux(a_e===128.U(10.W)&&man1A.asUInt=/=0.U(24.U)||b_e===128.U(10.W)&&man2B.asUInt=/=0.U(24.U),1.U,Mux(a_e===128.U(10.W)&&b_e.asUInt.asSInt=== -127.S(10.W)&&man2B.asUInt===0.U||b_e===128.U(10.W)&&a_e.asUInt.asSInt=== -127.S(10.W)&&man1A.asUInt===0.U,1.U,0.U)),isNAN) 
        isINF:=Mux(io.fpuReady.asBool&&io.fpuValid.asBool,Mux(a_e===128.U||b_e===128.U,1.U,0.U),isINF)
        val roundReg=Reg(UInt(1.W))
        roundReg := Mux(io.fpuReady.asBool&&io.fpuValid.asBool,io.round_cfg,roundReg) 
        /*--------------------second logic---------------------------------*/
        //regulate the tail
        val mul_out_p_cut=Wire(Vec(23,UInt(1.W)))
        for(i<- 0 to 22){
            mul_out_p_cut(i):=mul_out_p(23+i)
        }
        two_m_out:=Mux(one_m_reg === 0.U(48.W),0.U(23.W),Mux(roundReg===1.U(1.W),Mux(mul_out_p(22)===1.U(1.W),mul_out_p_cut.asUInt+1.U(23.W),mul_out_p_cut.asUInt),mul_out_p_cut.asUInt))
        n:=Mux(one_m_reg === 0.U(48.W),0.U(1.W),Mux(one_m_reg(47)===1.U(1.W),1.U(1.W),0.U(1.W)))
        mul_out_p:=Mux(one_m_reg === 0.U(48.W),0.U(23.W),Mux(one_m_reg(47)===1.U(1.W),one_m_reg >> 1.U,one_m_reg))
        val cround=Wire(UInt(1.W))
        cround:=Mux(one_m_reg === 0.U(48.W),0.U(1.W),Mux(roundReg===1.U(1.W),Mux(mul_out_p(22)===1.U(1.W),Mux(mul_out_p_cut.asUInt+1.U(23.W)===0.U(23.W),1.U,0.U),0.asUInt),0.asUInt))
       
        //double signed, 01 is upper overflow, 10 is downside overflow
        val one_e_cut=Wire(Vec(10,UInt(1.W)))
        for(i<- 0 to 9){
            one_e_cut(i):=one_e_reg(i)
        }
        temp3:=one_e_cut.asUInt+n+1.U(1.W)+cround
        val doubleSign=Wire(Vec(2,UInt(1.W)))
        val tmp3Cut=Wire(Vec(7,UInt(1.W)))
        for(i<- 0 to 6){
            tmp3Cut(i):=temp3(i)
        }
        doubleSign(1):=temp3(9)
        doubleSign(0):=temp3(8)
        two_f_out:=Mux(doubleSign.asUInt===1.U(2.W),1.U(2.W),Mux(doubleSign.asUInt===2.U(2.W),2.U(2.W),0.U(2.W)))
        two_e_out:=Mux(temp3(7)===1.U(1.W),Cat(0.U(1.W),tmp3Cut.asUInt()),Cat(1.U(1.W),tmp3Cut.asUInt()))
        /*-------------------second reg------------------------------------*/
        signSec:=Mux(stage0Ready.asBool&&stage0Valid.asBool,one_s_reg,signSec)
        two_s_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(1.W),one_s_reg),two_s_reg)
        two_f_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(2.W),two_f_out),two_f_reg)
        two_m_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(23.W),two_m_out),two_m_reg)
        two_e_reg:=Mux(stage0Ready.asBool&&stage0Valid.asBool,Mux((two_m_out === 0.U(23.W)) && (two_e_out === 0.U(23.W)),0.U(8.W),two_e_out),two_e_reg)
        isINFs:=Mux(stage0Ready.asBool&&stage0Valid.asBool,isINF,isINFs)
        isNANs:=Mux(stage0Ready.asBool&&stage0Valid.asBool,isNAN,isNANs)
        isZeros:=Mux(stage0Ready.asBool&&stage0Valid.asBool,isZero,isZeros)
        //output Results
        io.overflow:=two_f_reg
        Results:=Mux(isNANs===1.U, Cat(1.U(1.W),255.U(8.W),1.U(1.W),0.U(22.W)),Mux(isINFs===1.U,Cat(signSec,255.U(8.W),0.U(23.W)),Mux(isZeros===1.U,Cat(signSec,0.U(8.W),0.U(23.W)),Cat(two_s_reg,two_e_reg,two_m_reg))))

    }

    class combFPUSub32(val dataWidth: Int =32) extends Module{
        val io=IO(new Bundle{
            val minA=Input(UInt(dataWidth.W))
            val minB=Input(UInt(dataWidth.W))
            val minC=Output(UInt(dataWidth.W))
            // val round=Input(UInt(1.W))
        })
        // val minS=Wire(UInt(32.W))
        val signA=Wire(UInt(1.W))
        val signB=Wire(UInt(1.W))
        val signS=Wire(UInt(1.W))
        val expA=Wire(Vec(8,UInt(1.W)))
        val expB=Wire(Vec(8,UInt(1.W)))
        val expS=Wire(Vec(8,UInt(1.W)))
        val expS_tmp=Wire((UInt(8.W)))
        val manA=Wire(Vec(27,UInt(1.W)))
        val manA_tmp=Wire(Vec(28,UInt(1.W)))
        val add_tmp=Wire(UInt(28.W))
        val manSVec=Wire(Vec(25,UInt(1.W)))
        val manB=Wire(Vec(27,UInt(1.W)))
        val manB_tmp=Wire(Vec(28,UInt(1.W)))
        val manS=Wire((UInt(25.W)))
        val manS_tmp=Wire(UInt(25.W))
        val count=Wire(UInt(8.W))
        val manBTmp=Wire(UInt(27.W))
        val finalResults=Wire(Vec(dataWidth,UInt(1.W)))
        val isNAN=Wire(UInt(1.W))
        val isINF=Wire(UInt(1.W))
        val isZero0=Wire(UInt(1.W))
        val isZero1=Wire(UInt(1.W))
        val isZero2=Wire(UInt(1.W))
        val a_eW=Wire(Vec(10,UInt(1.W)))
        val b_eW=Wire(Vec(10,UInt(1.W)))
        val a_m=Wire(Vec(27,UInt(1.W)))
        val b_m=Wire(Vec(27,UInt(1.W)))
        val a_e=Wire((UInt(10.W)))
        val b_e=Wire((UInt(10.W)))
        a_e:=a_eW.asUInt-127.U
        b_e:=b_eW.asUInt-127.U
        a_eW(8):=0.U(1.W)
        a_eW(9):=0.U(1.W)
        b_eW(8):=0.U(1.W)
        b_eW(9):=0.U(1.W)
        for(i<-0 to 24){
            manSVec(i):=add_tmp(i+3)
        }
        for(i<- 0 to 7){
            b_eW(i):=io.minB(23+i)
            a_eW(i):=io.minA(23+i)
        }
        isNAN:=(a_e === 128.U & a_m.asUInt =/= 0.U) | (b_e === 128.U & b_m.asUInt =/= 0.U) | (a_e === 128.U)&(b_e === 128.U) & (signA =/= signB)
        isINF:=(a_e === 128.U) & ~isNAN | (b_e === 128.U)
        isZero0:=(((a_e).asSInt === -127.S(10.W)) & (a_m.asUInt === 0.U)) & (((b_e).asSInt === -127.S(10.W)) & (b_m.asUInt === 0.U)) 
        isZero1:=(((a_e).asSInt === -127.S(10.W)) & (a_m.asUInt === 0.U))
        isZero2:=(((b_e).asSInt === -127.S(10.W)) & (b_m.asUInt === 0.U)) 
        val reverseB=Wire(Vec(dataWidth,UInt(1.W)))
        for(i<- 0 to dataWidth-2){
            reverseB(i):=io.minB(i)
        }
        reverseB(dataWidth-1):= ~io.minB(dataWidth-1)
        io.minC:=Mux(isNAN.asBool, Cat(1.U(1.W),255.U(8.W),1.U(1.W),0.U(22.W)), Mux(isINF.asBool,Cat(Mux(a_e === 128.U, signA,signB),255.U(8),0.U(23.W)),Mux(isZero1.asBool,reverseB.asUInt,Mux(isZero2.asBool,io.minA,finalResults.asUInt))))
        signA:=io.minA(31);
        signB:=io.minB(31);
        for(i<-0 to 7){
            expA(i):=io.minA(23+i)
            expB(i):=io.minB(23+i)
        }
        for(i<-0 to 22){
            manA(i+3):=io.minA(i)
            manB(i+3):=io.minB(i)
            a_m(i+3):=io.minA(i)
            b_m(i+3):=io.minB(i)
        }
        manA(0):=0.U
        manA(1):=0.U
        manA(2):=0.U
        manB(0):=0.U
        manB(1):=0.U
        manB(2):=0.U
        a_m(0):=0.U
        a_m(1):=0.U
        a_m(2):=0.U
        b_m(0):=0.U
        b_m(1):=0.U
        b_m(2):=0.U
        a_m(26):=0.U
        b_m(26):=0.U
        manA(26):=1.U(1.W)
        manB(26):=1.U(1.W)
        for(i<-0 to 22){
            manA_tmp(i+3):=io.minA(i)
            manB_tmp(i+3):=io.minB(i)
        }
        manA_tmp(0):=0.U
        manA_tmp(1):=0.U
        manA_tmp(2):=0.U
        manB_tmp(0):=0.U
        manB_tmp(1):=0.U
        manB_tmp(2):=0.U
        manA_tmp(26):=1.U(1.W)
        manB_tmp(26):=1.U(1.W)
        manA_tmp(27):=0.U(1.W)
        manB_tmp(27):=0.U(1.W)
        when(expA.asUInt===expB.asUInt){
            count:=0.U(8.W)
            for(i<-0 to 22){
                manA_tmp(i+3):=io.minA(i)
                manB_tmp(i+3):=io.minB(i)
            }
            // manA_tmp(23):=1.U(1.W)
            // manB_tmp(23):=1.U(1.W)
            expS:=expA
            manBTmp:=0.U(24.W)
        }
        .elsewhen(expA.asUInt>expB.asUInt){
            count:=expA.asUInt-expB.asUInt
            // for(i<-0 to 22){
            //     // manA_tmp(i):=io.minA(i)
            //     manB_tmp(i):=manBTmp(i)
            // }
            for(i<-0 to 26){
                // manA_tmp(i):=io.minA(i)
                manB_tmp(i):=manBTmp(i)
            }
            // manA_tmp(23):=1.U(1.W)
            manB_tmp(27):=0.U
            manBTmp:=manB.asUInt>>count
            expS:=expA
        }
        .otherwise{
            count:=expB.asUInt-expA.asUInt
            for(i<-0 to 26){
                // manB_tmp(i):=io.minB(i)
                manA_tmp(i):=manBTmp(i)
            }
            // manB_tmp(23):=1.U(1.W)
            manA_tmp(27):=0.U
            manBTmp:=manA.asUInt>>count
            expS:=expB
        }
        when((signA^signB).asBool){
            add_tmp:=manA_tmp.asUInt+manB_tmp.asUInt
            
            signS:=signA
        }
        .otherwise{
            when(manA_tmp.asUInt>=manB_tmp.asUInt){
                add_tmp:=manA_tmp.asUInt-manB_tmp.asUInt
                signS:=signA
            }
            .otherwise{
                add_tmp:=manB_tmp.asUInt-manA_tmp.asUInt
                signS:= ~signA
            }
        }
        manS:=Mux(add_tmp(2)===1.U(1.W), manSVec.asUInt,manSVec.asUInt)
        when(manS(24)===1.U){
            manS_tmp:=manS
            expS_tmp:=expS.asUInt+1.U
            finalResults(31):=signS
            for(i<-0 to 7){
                finalResults(23+i):=expS_tmp(i)
            }
            for(i<-0 to 22){
                finalResults(i):=manS_tmp(i+1)
            }
            
        }	
        .otherwise{
            when(manS(23)===1.U){
                manS_tmp:=0.U(25.W)
                expS_tmp:=expS.asUInt
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS(i)
                }
            }
            .elsewhen(manS(22)===1.U){
                expS_tmp:=expS.asUInt-1.U
                manS_tmp:=manS<<1.U | Cat(0.U(24.W),add_tmp(2))
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                // minS={signS,expS_tmp[7:0],manS_tmp[22:0]};
            }
            .elsewhen(manS(21)===1.U){
                expS_tmp:=expS.asUInt-2.U
                manS_tmp:=manS<<2.U | Cat(0.U(23.W),add_tmp(2),add_tmp(1))
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(20)===1.U){
                expS_tmp:=expS.asUInt-3.U
                manS_tmp:=manS<<3.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(19)===1.U){
                expS_tmp:=expS.asUInt-4.U
                manS_tmp:=manS<<4.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<1.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(18)===1.U){
                expS_tmp:=expS.asUInt-5.U
                manS_tmp:=manS<<5.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<2.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(17)===1.U){
                expS_tmp:=expS.asUInt-6.U
                manS_tmp:=manS<<6.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<3.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(16)===1.U){
                expS_tmp:=expS.asUInt-7.U
                manS_tmp:=manS<<7.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<4.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(15)===1.U){
                expS_tmp:=expS.asUInt-8.U
                manS_tmp:=manS<<8.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<5.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(14)===1.U){
                expS_tmp:=expS.asUInt-9.U
                manS_tmp:=manS<<9.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<6.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(13)===1.U){
                expS_tmp:=expS.asUInt-10.U
                manS_tmp:=manS<<10.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<7.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(12)===1.U){
                expS_tmp:=expS.asUInt-11.U
                manS_tmp:=manS<<11.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<8.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(11)===1.U){
                expS_tmp:=expS.asUInt-12.U
                manS_tmp:=manS<<12.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<9.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(10)===1.U){
                expS_tmp:=expS.asUInt-13.U
                manS_tmp:=manS<<13.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<10.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(9)===1.U){
                expS_tmp:=expS.asUInt-14.U
                manS_tmp:=manS<<14.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<11.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(8)===1.U){
                expS_tmp:=expS.asUInt-15.U
                manS_tmp:=manS<<15.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<12.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(7)===1.U){
                expS_tmp:=expS.asUInt-16.U
                manS_tmp:=manS<<16.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<13.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(6)===1.U){
                expS_tmp:=expS.asUInt-17.U
                manS_tmp:=manS<<17.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<14.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(5)===1.U){
                expS_tmp:=expS.asUInt-18.U
                manS_tmp:=manS<<18.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<15.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(4)===1.U){
                expS_tmp:=expS.asUInt-19.U
                manS_tmp:=manS<<19.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<16.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(3)===1.U){
                expS_tmp:=expS.asUInt-20.U
                manS_tmp:=manS<<20.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<17.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(2)===1.U){
                expS_tmp:=expS.asUInt-21.U
                manS_tmp:=manS<<21.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<18.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .elsewhen(manS(1)===1.U){
                expS_tmp:=expS.asUInt-22.U
                manS_tmp:=manS<<22.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<19.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
            .otherwise{
                expS_tmp:=expS.asUInt-23.U
                manS_tmp:=manS<<23.U | Cat(0.U(22.W),add_tmp(2),add_tmp(1),add_tmp(0))<<20.U
                finalResults(31):=signS
                for(i<-0 to 7){
                    finalResults(23+i):=expS_tmp(i)
                }
                for(i<-0 to 22){
                    finalResults(i):=manS_tmp(i)
                }
                
            }
        }		
	
    }

    class longLatencyFPUDiv(val dataWidth: Int=32) extends Module{
        val io=IO(new Bundle{
            val fpuA=Input(UInt(dataWidth.W))
            val fpuB=Input(UInt(dataWidth.W))
            val fpuReady=Output(UInt(1.W))
            val fpuValid=Input(UInt(1.W))
            val fpuC=Output(UInt(dataWidth.W))
            val fpuCReady=Input(UInt(1.W))
            val fpuCValid=Output(UInt(1.W))
        })
        val stateMachine=RegInit(0.U(4.W))
        val nextMachine=Wire(UInt(4.W))
        // val Areg=Reg(UInt(dataWidth.W))
        // val Breg=Reg(UInt(dataWidth.W))
        val a_m=Reg(Vec(24,UInt(1.W)))
        val b_m=Reg(Vec(24,UInt(1.W)))
        val z_m=Reg(Vec(24,UInt(1.W)))
        val a_eVec=Wire(Vec(10,UInt(1.W)))
        val b_eVec=Wire(Vec(10,UInt(1.W)))
        val a_e=Reg(UInt(10.W))
        val b_e=Reg(UInt(10.W))
        val z_e=Reg(UInt(10.W))
        val a_s=Reg(UInt(1.W))
        val b_s=Reg(UInt(1.W))
        val z_s=Reg(UInt(1.W))
        val zVec=Reg(Vec(32,UInt(1.W)))
        val quotient=Reg(UInt(51.W))
        val divisor=Reg(UInt(51.W))
        val dividend=Reg(UInt(51.W))
        val remainder=Reg(UInt(51.W))
        val count=Reg(UInt(6.W))
        val guard=Reg(UInt(1.W))
        val round_bit=Reg(UInt(1.W))
        val sticky=Reg(UInt(1.W))
        val z_mV=Wire(UInt(24.W))
        val z_eCut=Wire(Vec(10,UInt(1.W)))
        val z_eAdd=Wire(UInt(10.W))
        z_eAdd:=z_eCut.asUInt+127.U
        z_mV:=z_m.asUInt+1.U
        for(i<-0 to 7){
            a_eVec(i):=io.fpuA(i+23)
            b_eVec(i):=io.fpuB(i+23)
            z_eCut(i):=z_e(i)
        }
        z_eCut(8):=0.U
        z_eCut(9):=0.U
        a_eVec(8):=0.U
        a_eVec(9):=0.U
        b_eVec(8):=0.U
        b_eVec(9):=0.U
        
        stateMachine:=nextMachine
        io.fpuReady:=stateMachine===0.U(4.W)
        io.fpuCValid:=stateMachine===13.U(4.W)
        when(stateMachine===0.U){
            when(io.fpuValid===1.U){
                nextMachine:=1.U
                // Areg:=io.fpuA.asUInt
                // Breg:=io.fpuB.asUInt
                for(i<-0 to 22){
                    a_m(i):=io.fpuA(i)
                    b_m(i):=io.fpuB(i)
                }
                a_m(23):=0.U
                b_m(23):=0.U
                a_e:=a_eVec.asUInt-127.U
                b_e:=b_eVec.asUInt-127.U
                a_s:=io.fpuA(31)
                b_s:=io.fpuB(31)
            }
            .otherwise{
                nextMachine:=0.U
            }
        }
        .elsewhen(stateMachine===1.U){
            when((a_e === 128.U && a_m.asUInt =/= 0.U) || (b_e === 128.U && b_m.asUInt =/= 0.U)){
                zVec(31):=1.U
                zVec(22):=1.U
                for(i<-0 to 7){
                    zVec(23+i):=1.U(1.W)
                }
                for(i<-0 to 21){
                    zVec(i):=0.U(1.W)
                }
                nextMachine:=13.U
            }
            .elsewhen((a_e === 128.U) && (b_e === 128.U)){
                zVec(31):=1.U
                zVec(22):=1.U
                for(i<-0 to 7){
                    zVec(23+i):=1.U(1.W)
                }
                for(i<-0 to 21){
                    zVec(i):=0.U(1.W)
                }
                nextMachine:=13.U
            }
            .elsewhen(a_e === 128.U){
                zVec(31):=Mux((b_e.asSInt === -127.S(10.W)) && (b_m.asUInt === 0.U), 1.U, a_s^b_s)
                zVec(22):=Mux((b_e.asSInt === -127.S(10.W)) && (b_m.asUInt === 0.U), 1.U, 0.U)
                for(i<-0 to 7){
                    zVec(23+i):=1.U(1.W)
                }
                for(i<-0 to 21){
                    zVec(i):=0.U(1.W)
                }
                nextMachine:=13.U
            }
            .elsewhen(b_e === 128.U){
                zVec(31):=a_s^b_s
                zVec(22):=0.U
                for(i<-0 to 7){
                    zVec(23+i):=0.U(1.W)
                }
                for(i<-0 to 21){
                    zVec(i):=0.U(1.W)
                }
                nextMachine:=13.U
            }
            .elsewhen((a_e.asSInt === -127.S(10.W)) && (a_m.asUInt === 0.U)){
                zVec(31):=Mux((b_e.asSInt === -127.S(10.W)) && (b_m.asUInt === 0.U), 1.U, a_s^b_s)
                zVec(22):=Mux((b_e.asSInt === -127.S(10.W)) && (b_m.asUInt === 0.U), 1.U, 0.U)
                for(i<-0 to 7){
                    zVec(23+i):=Mux((b_e.asSInt === -127.S(10.W)) && (b_m.asUInt === 0.U), 1.U, 0.U)
                }
                for(i<-0 to 21){
                    zVec(i):=0.U(1.W)
                }
                nextMachine:=13.U
            }
            .elsewhen((b_e.asSInt === -127.S(10.W)) && (b_m.asUInt === 0.U)){
                zVec(31):=a_s^b_s
                zVec(22):= 0.U
                for(i<-0 to 7){
                    zVec(23+i):=1.U
                }
                for(i<-0 to 21){
                    zVec(i):=0.U(1.W)
                }
                nextMachine:=13.U
            }
            .otherwise{
                when(a_e.asSInt === -127.S(10.W)){
                    a_e := (-126.S(10.W)).asUInt
                    
                }
                .otherwise{
                    a_m(23):=1.U
                }
                when(b_e.asSInt === -127.S(10.W)){
                    b_e := (-126.S(10.W)).asUInt
                }
                .otherwise{
                    b_m(23):=1.U
                }
                nextMachine:=2.U
            }
        }
        .elsewhen(stateMachine===2.U){
            when(a_m(23)===1.U&&b_m(23)===1.U){
                nextMachine:=3.U
                quotient:=0.U(51.W)
                z_s:=a_s ^ b_s
                z_e:=a_e - b_e
                remainder:=0.U(51.W)
                count:=0.U
                dividend:=Cat(a_m.asUInt,0.U(27.W))
                divisor:=Cat(0.U(27.W),b_m.asUInt)
            }
            .otherwise{
                when(a_m(23)=/=1.U){
                    a_e:=a_e-1.U
                    for(i<- 1 to 23){
                        a_m(i):=a_m(i-1)
                    }
                    a_m(0):=0.U
                }
                .otherwise{
                    a_m:=a_m
                    a_e:=a_e
                }
                when(b_m(23)=/=1.U){
                    b_e:=b_e-1.U
                    for(i<- 1 to 23){
                        b_m(i):=b_m(i-1)
                    }
                    b_m(0):=0.U
                }
                .otherwise{
                    b_m:=b_m
                    b_e:=b_e
                }
                nextMachine:=2.U
            }
        }
        .elsewhen(stateMachine===3.U){
            quotient:=quotient << 1
            remainder := remainder << 1 | Cat(0.U(50.W),dividend(50))
            dividend := dividend << 1
            
            nextMachine:=4.U
        }
        .elsewhen(stateMachine===4.U){
            when(remainder >= divisor){
                remainder := remainder - divisor
                quotient := quotient | Cat(0.U(50.W),1.U(1.W))
                // nextMachine:=4.U
            }
            .otherwise{
                remainder:=remainder
                quotient:=quotient
                // nextMachine:=4.U
            }
            when(count===49.U){
                nextMachine:=5.U
            }
            .otherwise{
                count:=count+1.U
                nextMachine:=3.U
            }
        
        }
        .elsewhen(stateMachine===5.U){
            for(i<-0 to 23){
                z_m(i):=quotient(3+i)
            }
            guard:=quotient(2)
            round_bit:=quotient(1)
            sticky:=quotient(0)|(remainder=/=0.U(51.W))
            nextMachine:=6.U
        }
        .elsewhen(stateMachine===6.U){
            when(z_m(23)===0.U&&z_e.asSInt> -126.S(10.W)){
                z_e:=z_e-1.U
                for(i<- 1 to 23){
                    z_m(i):=z_m(i-1)
                }
                z_m(0):=guard
                guard:=round_bit
                round_bit:=0.U
                nextMachine:=6.U
            }
            .otherwise{
                nextMachine:=7.U
            }
            
        }
        .elsewhen(stateMachine===7.U){
            when(z_e.asSInt< -126.S(10.W)){
                z_e:=z_e+1.U
                for(i<- 0 to 22){
                    z_m(i):=z_m(i+1)
                }
                z_m(23):=0.U
                guard:=z_m(0)
                round_bit:=guard
                nextMachine:=7.U
                sticky := sticky | round_bit
            }
            .otherwise{
                nextMachine:=8.U
            }
        }
        .elsewhen(stateMachine===8.U){
            when(guard.asBool && (round_bit.asBool || sticky.asBool || z_m(0).asBool)){
                // z_m:=z_mV
                for(i<-0 to 23){
                    z_m(i):=z_mV(i)
                }
            }
            when(z_m.asUInt===16777215.U(24.W)){
                z_e :=z_e + 1.U
            }
            nextMachine:=9.U
        }
        .elsewhen(stateMachine===9.U){
            nextMachine:=13.U
            for(i<-0 to 22){
                zVec(i):=Mux(z_e.asSInt>127.S(10.W),0.U,z_m(i))
            }
            for(i<- 0 to 7){
                zVec(23+i):=Mux(z_e.asSInt=== -126.S(10.W)&&z_m(23)===0.U,0.U,Mux(z_e.asSInt>127.S(10.W),1.U,z_eAdd(i)))
            }
            zVec(31):=z_s
            // z[22 : 0] <= z_m[22:0];
            // z[30 : 23] <= z_e[7:0] + 127;
            // z[31] <= z_s;
            // if ($signed(z_e) == -126 && z_m[23] == 0) begin
            // z[30 : 23] <= 0;
            // end
            // //if overflow occurs, return inf
            // if ($signed(z_e) > 127) begin
            // z[22 : 0] <= 0;
            // z[30 : 23] <= 255;
            // z[31] <= z_s;
            // end
        }
        .otherwise{
            when(io.fpuCReady===1.U){
                nextMachine:=0.U
            }
            .otherwise{
                nextMachine:=13.U
            }
        }
        io.fpuC:=zVec.asUInt
    }

    class float2intComb(val dataWidth:Int=32) extends Module{
        val io=IO(new Bundle{
            val fpuA=Input(UInt(dataWidth.W))
            val fpuC=Output(UInt(dataWidth.W))
        })
        val a_e=Wire(Vec(9,UInt(1.W)))
        val exp=Wire(UInt(9.W))
        val transPort0=Wire(UInt(32.W))
        
        val tail=Wire(Vec(32,UInt(1.W)))
        val a_s=io.fpuA(31)
        for(i<- 0 to 7){
            a_e(i):=io.fpuA(23+i)
        }
        for(i<- 0 to 22){
            tail(i):=io.fpuA(i)
        }
        tail(23):=1.U
        for(i<- 24 to 31){
            tail(i):=0.U
        }
        a_e(8):=0.U
        // a_e(9):=0.U
        exp:=a_e.asUInt-127.U
        when(exp.asSInt === -127.S(9.W)){
            io.fpuC:=0.U(32.W)
            transPort0:=0.U
        }
        .elsewhen(exp.asSInt >= 31.S(9.W)){
            io.fpuC:=Cat(1.U(1.W),0.U(31.W))
            transPort0:=0.U
        }
        .otherwise{
            when(exp.asSInt > 23.S(9.W)){
                transPort0:=(tail.asUInt)<<(exp.asUInt-23.U)
                io.fpuC:=Mux(a_s.asBool,~(transPort0-1.U),transPort0)
            }
            .elsewhen(exp.asSInt < 0.S(9.W)){
                transPort0:=0.U
                io.fpuC:=0.U
            }
            .otherwise{
                transPort0:=(tail.asUInt)>>(23.U-exp.asUInt)
                io.fpuC:=Mux(a_s.asBool,~(transPort0-1.U),transPort0)
            }
            
        }
        
    }

    class int2floatComb(val dataWidth:Int=32) extends Module{
        val io=IO(new Bundle{
            val fpuA=Input(UInt(dataWidth.W))
            val fpuC=Output(UInt(dataWidth.W))
        })
        val complementary=Wire(UInt(dataWidth.W))
        complementary:= ~(io.fpuA-1.U)
        val muxIn=Wire(Vec(dataWidth,UInt(1.W)))
        val pMux = Module(new ChiselLib.PriorMux(dataWidth))
        // val pMux2 = Module(new ChiselLib.PriorMux(dataWidth))
        val chosen=Wire(UInt((log2Ceil(dataWidth)).W))
        // val chosen2=Wire(UInt((log2Ceil(dataWidth)).W))
        val maskVec=Wire(UInt(dataWidth.W))
        // val muxIn2=Wire(UInt(dataWidth.W))
        val tail=Wire(Vec(23,UInt(1.W)))
        val expBits=Wire(UInt(8.W))
        // maskVec:=pMux.io.selectOutVec
        pMux.io.selectIn:=muxIn.asUInt
        chosen:=pMux.io.selectOut
        // pMux2.io.selectIn:=muxIn2
        // muxIn2:=muxIn.asUInt^maskVec
        // chosen2:=pMux2.io.selectOut
        when(io.fpuA===0.U){
            io.fpuC:=Cat(0.U(1.W),0.U(8.W),0.U(23.W))
            maskVec:=0.U
            for(i<- 0 to 31){
                muxIn(i):=complementary(31-i)
            }
            for(i<- 0 to 22){
                tail(i):=0.U
            }
            expBits:=0.U
        }
        .otherwise{
            when(io.fpuA.asSInt<0.S){
                for(i<- 0 to 31){
                    muxIn(i):=complementary(31-i)
                }
                maskVec:=(complementary)<<(chosen+1.U)
                for(i<- 0 to 22){
                    tail(i):=maskVec(9+i)
                }
                expBits:=127.U(8.W)+31.U(8.W)-chosen
                io.fpuC:=Cat(1.U(1.W),expBits,tail.asUInt)
            }
            .otherwise{
                for(i<- 0 to 31){
                    muxIn(i):=io.fpuA(31-i)
                }
                maskVec:=(io.fpuA)<<(chosen+1.U)
                for(i<- 0 to 22){
                    tail(i):=maskVec(9+i)
                }
                expBits:=127.U(8.W)+31.U(8.W)-chosen
                io.fpuC:=Cat(0.U(1.W),expBits,tail.asUInt)
            }
        }
    }

    object myFPmul extends App {
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new pipeFPUMul32())));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new combFPUSub32())));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new longLatencyFPUDiv())));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new float2intComb())));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new int2floatComb())));
    }
}