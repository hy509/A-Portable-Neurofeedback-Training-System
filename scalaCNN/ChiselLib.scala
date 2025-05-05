

import chisel3._
import chisel3.util._


package ChiselLib{

    class PriorArbiter(dataWidth: Int) extends Module{
        val io=IO(new Bundle{
            val selectIn=Input(UInt((dataWidth).W))
            val selectOut=Output(UInt((dataWidth).W))
        })
        val makeup=io.selectIn-1.U(dataWidth.W)
        io.selectOut:=io.selectIn&(~makeup)
    }

    class Pos2Bin(dataWidth: Int) extends Module{
        // support pow of 2 only
        val io=IO(new Bundle{
            val selectIn=Input(UInt((dataWidth).W))
            val selectOut=Output(UInt((log2Ceil(dataWidth)).W))
        })
        val TmpContainer=Wire(Vec(log2Ceil(dataWidth)+1,Vec(dataWidth,UInt(log2Ceil(dataWidth).W))))
        val Reducted=Wire(Vec(log2Ceil(dataWidth)+1,Vec(dataWidth,UInt(1.W))))
        for(j<- 0 to log2Ceil(dataWidth)){
            for(i<-0 to dataWidth-1){
                Reducted(j)(i):=0.U(1.W)
                // if(j<log2Ceil(dataWidth)){
                    TmpContainer(j)(i):=0.U(log2Ceil(dataWidth).W)
                // }
            }
        }
        for(i<-0 to dataWidth-1){
            Reducted(0)(i):=io.selectIn(i)
        }
        var count: Int = dataWidth
        for(i <- 1 to log2Ceil(dataWidth)){
            
            count=count/2
            for(j<-0 to count-1){
                Reducted(i)(j):=Reducted(i-1)(2*j)|Reducted(i-1)(2*j+1)
                when(Reducted(i-1)(2*j+1)===1.U(1.W)){
                    TmpContainer(i)(j):=(TmpContainer(i-1)(2*j+1)<<1.U)|1.U(log2Ceil(dataWidth).W)
                }
                .otherwise{
                    TmpContainer(i)(j):=(TmpContainer(i-1)(2*j)<<1.U)
                }
            }
        }
        val reversedOut=Wire(Vec(log2Ceil(dataWidth),UInt(1.W)))
        for(i<- 0 to log2Ceil(dataWidth)-1){
            reversedOut(i):=TmpContainer(log2Ceil(dataWidth))(0)(log2Ceil(dataWidth)-1-i)
        }
        io.selectOut:=reversedOut.asUInt()
    }

    class PriorMux(dataWidth: Int) extends Module{
        val io=IO(new Bundle{
            val selectIn=Input(UInt((dataWidth).W))
            val selectOut=Output(UInt((log2Ceil(dataWidth)).W))
            val selectOutVec=Output(UInt((dataWidth).W))
        })
        val priorityArbiter = Module(new PriorArbiter(dataWidth))
        priorityArbiter.io.selectIn:=io.selectIn
        io.selectOutVec:=priorityArbiter.io.selectOut
        val position2binary = Module(new Pos2Bin(dataWidth))
        position2binary.io.selectIn:=priorityArbiter.io.selectOut
        io.selectOut:=position2binary.io.selectOut
    }

    class Bin2Pos(dataWidth: Int) extends Module{
        val io=IO(new Bundle{
            val selectIn=Input(UInt((log2Ceil(dataWidth)).W))
            val selectOut=Output(UInt(dataWidth.W))
        })
        val TmpContainer=Wire(Vec(dataWidth,UInt(1.W)))
        for(i<- 0 to dataWidth-1){
            TmpContainer(i):=0.U(1.W)

        }
        TmpContainer(io.selectIn):=1.U(1.W)
        io.selectOut:=TmpContainer.asUInt()
    }

    class BasicFU(dataWidth: Int, insType: Int) extends Module{
        //add, sub, compare, the unsigned one
        val io=IO(new Bundle{
            val ins=Input(UInt(log2Ceil(insType).W))
            val inA=Input(UInt(((dataWidth)).W))
            val inB=Input(UInt(((dataWidth)).W))
            val outC=Output(UInt(dataWidth.W))
            // val R=Output(UInt((dataWidth+1).W))
        })
        val TmpContainer=Wire(UInt((dataWidth+1).W))
        val FinalResults=Wire(Vec(dataWidth,UInt(1.W)))
        val theB=Wire(UInt(dataWidth.W))
        val theBTmp=Wire(Vec(dataWidth+1,UInt(1.W)))
        val theBE=Wire(UInt((dataWidth+1).W))
        val theAE=Wire(Vec(dataWidth+1,UInt(1.W)))
        // io.R:=TmpContainer
        theB:=io.inB
        for(i<-0 to dataWidth-1){
            theBTmp(i):=io.inB(i)
        }
        theBTmp(dataWidth):=0.U(1.W)
        when(io.ins===0.U(log2Ceil(insType).W)){//add
            theBE:=theBTmp.asUInt()
        }
        .elsewhen(io.ins===1.U(log2Ceil(insType).W))//sub
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .elsewhen(io.ins===2.U(log2Ceil(insType).W))//equal
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .elsewhen(io.ins===3.U(log2Ceil(insType).W))//less than
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .elsewhen(io.ins===4.U(log2Ceil(insType).W))//greater than
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .otherwise{
            theBE:=theBTmp.asUInt()
        }
        val FinalCompare=Wire(Vec(dataWidth,UInt(1.W)))
        for(i<-0 to dataWidth-1){
            // theBE(i):=theB(i)
            theAE(i):=io.inA(i)
            FinalResults(i):=TmpContainer(i)
            FinalCompare(i):=0.U(1.W)
        }
        // theBE(dataWidth):=0.U(1.W)
        theAE(dataWidth):=0.U(1.W)
        TmpContainer:=theAE.asUInt()+theBE
        val TmpContainerVec=Wire(Vec(dataWidth+1,UInt(1.W)))
        for(i<-0 to dataWidth){
            TmpContainerVec(i):=TmpContainer(i)
        }
        // val equalOrNot=Wire(UInt(1.W))
        val lessOrgreat=TmpContainer(dataWidth)
        
        when(io.ins===0.U(log2Ceil(insType).W)){//add
            io.outC:=FinalResults.asUInt()
        }
        .elsewhen(io.ins===1.U(log2Ceil(insType).W))//sub
        {
            io.outC:=FinalResults.asUInt()
        }
        .elsewhen(io.ins===2.U(log2Ceil(insType).W))//equal
        {
            FinalCompare(0):= ~(TmpContainerVec.reduceTree(_ | _))
            io.outC:=FinalCompare.asUInt()
        }
        .elsewhen(io.ins===3.U(log2Ceil(insType).W))//less than
        {
            FinalCompare(0):=lessOrgreat
            io.outC:=FinalCompare.asUInt()
        }
        .elsewhen(io.ins===4.U(log2Ceil(insType).W))//greater than
        {
            FinalCompare(0):= ~lessOrgreat
            io.outC:=FinalCompare.asUInt()
        }
        .otherwise{
            io.outC:=FinalResults.asUInt()
        }
    }

    class BasicFUSign(dataWidth: Int, insType: Int) extends Module{
        //add, sub, compare, Both the unsigned one and the signed one
        val io=IO(new Bundle{
            val signedOrNot=Input(UInt(1.W))
            val ins=Input(UInt(log2Ceil(insType).W))
            val inA=Input(SInt(((dataWidth)).W))
            val inB=Input(SInt(((dataWidth)).W))
            val outC=Output(SInt(dataWidth.W))
            val overflow=Output(UInt(1.W))
        })
        val TmpContainer=Wire(UInt((dataWidth+1).W))
        val FinalResults=Wire(Vec(dataWidth,UInt(1.W)))
        val theB=Wire(UInt(dataWidth.W))
        val theBTmp=Wire(Vec(dataWidth+1,UInt(1.W)))
        val theBE=Wire(UInt((dataWidth+1).W))
        val theAE=Wire(Vec(dataWidth+1,UInt(1.W)))
        theB:=io.inB.asUInt()
        for(i<-0 to dataWidth-1){
            theBTmp(i):=io.inB(i)
        }
        theBTmp(dataWidth):=Mux(io.signedOrNot.asBool(),io.inB(dataWidth-1),0.U(1.W))
        when(io.ins===0.U(log2Ceil(insType).W)){//add
            theBE:=theBTmp.asUInt()
        }
        .elsewhen(io.ins===1.U(log2Ceil(insType).W))//sub
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .elsewhen(io.ins===2.U(log2Ceil(insType).W))//equal
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .elsewhen(io.ins===3.U(log2Ceil(insType).W))//less than
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .elsewhen(io.ins===4.U(log2Ceil(insType).W))//greater than
        {
            theBE:= ~(theBTmp.asUInt()-1.U((dataWidth+1).W))
        }
        .otherwise{
            theBE:=theBTmp.asUInt()
        }
        val FinalCompare=Wire(Vec(dataWidth,UInt(1.W)))
        for(i<-0 to dataWidth-1){
            // theBE(i):=theB(i)
            theAE(i):=io.inA(i)
            FinalResults(i):=TmpContainer(i)
            FinalCompare(i):=0.U(1.W)
        }
        // theBE(dataWidth):=Mux(io.signedOrNot,theB(dataWidth-1),0.U(1.W))
        theAE(dataWidth):=Mux(io.signedOrNot.asBool(),io.inA(dataWidth-1),0.U(1.W))
        TmpContainer:=theAE.asUInt()+theBE.asUInt()
        // val equalOrNot=Wire(UInt(1.W))
        val lessOrgreat=TmpContainer(dataWidth)
        val TmpContainerVec=Wire(Vec(dataWidth+1,UInt(1.W)))
        for(i<-0 to dataWidth){
            TmpContainerVec(i):=TmpContainer(i)
        }
        
        when(io.ins===0.U(log2Ceil(insType).W)){//add
            io.outC:=FinalResults.asUInt().asSInt()
        }
        .elsewhen(io.ins===1.U(log2Ceil(insType).W))//sub
        {
            io.outC:=FinalResults.asUInt().asSInt()
        }
        .elsewhen(io.ins===2.U(log2Ceil(insType).W))//equal
        {
            FinalCompare(0):= ~(TmpContainerVec.reduceTree(_ | _))
            io.outC:=FinalCompare.asUInt().asSInt()
        }
        .elsewhen(io.ins===3.U(log2Ceil(insType).W))//less than
        {
            FinalCompare(0):=lessOrgreat
            io.outC:=FinalCompare.asUInt().asSInt()
        }
        .elsewhen(io.ins===4.U(log2Ceil(insType).W))//greater than
        {
            FinalCompare(0):= ~lessOrgreat
            io.outC:=FinalCompare.asUInt().asSInt()
        }
        .otherwise{
            io.outC:=FinalResults.asUInt().asSInt()
        }
        io.overflow:=(theAE(dataWidth)===theBE(dataWidth)&TmpContainer(dataWidth)=/=theAE(dataWidth))
    }

    class BasicLogic(dataWidth: Int, insType: Int, shiftWidth: Int) extends Module{
        //and, or, not, and the shift
        val io=IO(new Bundle{
            val ins=Input(UInt(log2Ceil(insType).W))
            val inA=Input(UInt(((dataWidth)).W))
            val inB=Input(UInt(((dataWidth)).W))
            val shiftB=Input(UInt(((shiftWidth)).W))
            val outC=Output(UInt(dataWidth.W))
        })
        when(io.ins===0.U(log2Ceil(insType).W)){//and
            io.outC:=io.inA&io.inB
        }
        .elsewhen(io.ins===1.U(log2Ceil(insType).W))//or
        {
            io.outC:=io.inA|io.inB
        }
        .elsewhen(io.ins===2.U(log2Ceil(insType).W))//not
        {
            io.outC:= ~io.inA
        }
        .elsewhen(io.ins===3.U(log2Ceil(insType).W))//left shift
        {
            io.outC:=io.inA<<io.shiftB
        }
        .elsewhen(io.ins===4.U(log2Ceil(insType).W))//unsigned right shift
        {
            io.outC:=io.inA>>io.shiftB
        }
        .elsewhen(io.ins===5.U(log2Ceil(insType).W))//signed right shift
        {
            io.outC:=(io.inA.asSInt()>>io.shiftB).asUInt()
        }
        
        .otherwise{
            io.outC:=0.U(dataWidth.W)
        }
    }
 
  
    class BoothMultiplierBase4(DATA_WIDTH: Int) extends Module {  
        val io = IO(new Bundle {  
            val a = Input(SInt(DATA_WIDTH.W))  // Signed input a  
            val b = Input(SInt(DATA_WIDTH.W))  // Signed input b  
            val product = Output(SInt((2 * DATA_WIDTH).W)) // Signed output product  
        })  
        
        val booth_bits = Wire(Vec((DATA_WIDTH / 2), UInt(3.W)))  
        val partial_products = RegInit(VecInit(Seq.fill(DATA_WIDTH / 2)(0.S((2 * DATA_WIDTH).W))))  
        
        // On every positive edge of the clock  
        
        val b_extended = io.b << 1.U // Sign-extend b with an extra 0 
        // val b_extended = Wire(SInt((DATA_WIDTH+1).W))
        // val b_extendedVec = Wire(Vec(DATA_WIDTH+1,UInt(1.W)))
        // for(i<- 1 to DATA_WIDTH){
        //     b_extendedVec(i):=io.b(i-1)
        // }
        // b_extendedVec(0):=0.U(1.W)
        // b_extended := b_extendedVec.asSInt()
        val a_neg = -io.a                    // Negation of a  
        val a_pos = io.a                     // Positive of a
        val regProduct = RegInit(0.S((2 * DATA_WIDTH).W))
        val partial_products_vec=Wire(Vec(DATA_WIDTH / 2,SInt((2 * DATA_WIDTH).W)))
        // Calculate Booth bits  
        for (i <- 0 until DATA_WIDTH / 2) {  
            booth_bits(i) := Cat(b_extended(2*i+2), b_extended(2*i+1), b_extended(2*i)) 

            // Calculate partial products based on Booth encoding  
            partial_products_vec(i):=partial_products(i) << ((2*i).U)
            partial_products(i) := MuxCase(0.S, Array(  
            (booth_bits(i) === 0.U || booth_bits(i) === 7.U) -> 0.S,  
            (booth_bits(i) === 1.U || booth_bits(i) === 2.U)  -> a_pos,  
            (booth_bits(i) === 3.U) -> (a_pos << 1.U),  
            (booth_bits(i) === 4.U) -> (a_neg << 1.U),                 // 此处自动进行符号位的扩展，下同
            (booth_bits(i) === 5.U || booth_bits(i) === 6.U) -> a_neg  
            ))  
        }  

        // Accumulate partial products to form the final product  
        
        io.product := partial_products_vec.reduceTree(_+_)
        // io.product := partial_products.zipWithIndex.map{
        //     case (pp, i) => pp << ((2*i).U)
        // }.reduce(_+_)

    

    }  

    class DIVU(regWidth: Int) extends Module {
        val io = IO(
            new Bundle{
                val dividend = Input(UInt(regWidth.W));
                val divisor = Input(UInt(regWidth.W));
                val start = Input(UInt(1.W));
                val q = Output(UInt(regWidth.W));
                val r = Output(UInt(regWidth.W));
                val busy = Output(UInt(1.W));
            }
        )
        val ready = Wire(UInt(1.W));
        val count = RegInit(UInt(log2Ceil(regWidth + 1).W), 0.U);
        val regQ = Reg(UInt(regWidth.W));
        val regR = Reg(UInt(regWidth.W));
        val regB = Reg(UInt(regWidth.W));
        val busy = RegInit(UInt(1.W), 0.U);
        val busy2 = RegInit(UInt(1.W), 0.U);
        val rSign = RegInit(UInt(1.W), 0.U);
        ready := !busy & busy2;
        val subAdd = Wire(UInt((regWidth + 1).W));
        val tmp0 = Wire(Vec((regWidth + 1), UInt(1.W)));
        val tmp1 = Wire(Vec((regWidth + 1), UInt(1.W)));
        val subAddHi = Wire(Vec((regWidth), UInt(1.W)));
        val nextRegQ = Wire(Vec((regWidth), UInt(1.W)));
        io.busy := busy;
        for(i <- 0 to regWidth - 1) {
            tmp0(i + 1) := regR(i);
            tmp1(i) := regB(i);
            subAddHi(i) := subAdd(i);
        }
        for(i <- 0 to regWidth - 2) {
            nextRegQ(i + 1) := regQ(i);
        }
        nextRegQ(0) := !subAdd(regWidth);
        tmp0(0) := io.q(regWidth - 1);
        tmp1(regWidth) := 0.U;
        //subAddHi(0) := !subAdd(regWidth);
        subAdd := Mux(rSign.asBool(), tmp0.asUInt + tmp1.asUInt(), tmp0.asUInt - tmp1.asUInt());
        io.r := Mux(rSign.asBool(), regR + regB, regR);
        io.q := regQ;
        busy2 := busy;
        regR := Mux(io.start.asBool(), 0.U, Mux(busy.asBool(), subAddHi.asUInt(), regR));
        rSign := Mux(io.start.asBool(), 0.U, Mux(busy.asBool(), subAdd(regWidth), rSign));
        regQ := Mux(io.start.asBool(), io.dividend, Mux(busy.asBool(), nextRegQ.asUInt(), regQ));
        regB := Mux(io.start.asBool(), io.divisor, regB);
        count := Mux(io.start.asBool(), 0.U, Mux(busy.asBool(), count + 1.U, count));
        busy := Mux(io.start.asBool(), 1.U, Mux(busy.asBool(), Mux(count === (regWidth - 1).U, 0.U, busy), busy));
    }

    class DIVSU(regWidth: Int) extends Module {
        val io = IO(
            new Bundle{
                val dividend = Input(UInt(regWidth.W));
                val divisor = Input(UInt(regWidth.W));
                val start = Input(UInt(1.W));
                val q = Output(UInt(regWidth.W));
                val r = Output(UInt(regWidth.W));
                val busy = Output(UInt(1.W));
            }
        )
        val ready = Wire(UInt(1.W));
        val count = RegInit(UInt(log2Ceil(regWidth + 1).W), 0.U);
        val regQ = Reg(UInt(regWidth.W));
        val regR = Reg(UInt(regWidth.W));
        val regB = Reg(UInt(regWidth.W));
        val busy = RegInit(UInt(1.W), 0.U);
        val busy2 = RegInit(UInt(1.W), 0.U);
        val rSign = RegInit(UInt(1.W), 0.U);
        ready := !busy & busy2;
        val subAdd = Wire(UInt((regWidth + 1).W));
        val tmp0 = Wire(Vec((regWidth + 1), UInt(1.W)));
        val tmp1 = Wire(Vec((regWidth + 1), UInt(1.W)));
        val subAddHi = Wire(Vec((regWidth), UInt(1.W)));
        val nextRegQ = Wire(Vec((regWidth), UInt(1.W)));
        val reg_r2 = Wire(UInt(regWidth.W));
        reg_r2:=Mux(rSign.asBool,regR+regB,regR)
        io.busy := busy;
        for(i <- 0 to regWidth - 1) {
            tmp0(i + 1) := regR(i);
            tmp1(i) := regB(i);
            subAddHi(i) := subAdd(i);
        }
        for(i <- 0 to regWidth - 2) {
            nextRegQ(i + 1) := regQ(i);
        }
        nextRegQ(0) := !subAdd(regWidth);
        tmp0(0) := regQ(regWidth - 1);
        tmp1(regWidth) := 0.U;
        //subAddHi(0) := !subAdd(regWidth);
        subAdd := Mux(rSign.asBool(), tmp0.asUInt + tmp1.asUInt(), tmp0.asUInt - tmp1.asUInt());
        io.r := Mux(io.dividend(regWidth-1).asBool(), (~reg_r2) + 1.U, reg_r2);
        io.q := Mux((io.divisor(regWidth-1)^io.dividend(regWidth-1)),(~regQ)+1.U,regQ);
        busy2 := busy;
        regR := Mux(io.start.asBool(), 0.U, Mux(busy.asBool(), subAddHi.asUInt(), regR));
        rSign := Mux(io.start.asBool(), 0.U, Mux(busy.asBool(), subAdd(regWidth), rSign));
        regQ := Mux(io.start.asBool(), Mux(io.dividend(regWidth-1).asBool(),(~io.dividend)+1.U,io.dividend), Mux(busy.asBool(), nextRegQ.asUInt(), regQ));
        regB := Mux(io.start.asBool(), Mux(io.divisor(regWidth-1).asBool(),(~io.divisor)+1.U,io.divisor), regB);
        count := Mux(io.start.asBool(), 0.U, Mux(busy.asBool(), count + 1.U, count));
        busy := Mux(io.start.asBool(), 1.U, Mux(busy.asBool(), Mux(count === (regWidth - 1).U, 0.U, busy), busy));
    }

    class ExponentialUnit(DATA_WIDTH: Int) extends Module {  
        val io = IO(new Bundle {  
            val a = Input(SInt(DATA_WIDTH.W))  // Signed input a  
            // val b = Input(SInt(DATA_WIDTH.W))  // Signed input b  
            val product = Output(SInt((2 * DATA_WIDTH).W)) // Signed output product  
        })  
    
    }

    

    /* An object extending App to generate the Verilog code*/
    object BoothMultiplierBase4 extends App {
    (new chisel3.stage.ChiselStage).emitVerilog(new BoothMultiplierBase4(32), Array("--target-dir", "./verilog/BoothMultiplier"))
    }


    object myLib extends App {
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new PriorMux(32))));
        
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new Bin2Pos(32))));
        
    }
}