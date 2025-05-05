import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import chisel3.experimental._
import chisel3.experimental.BundleLiterals._
import chisel3.tester._
import chisel3.tester.RawTester.test
package pepe {
    
    class fifo_in(n: Int, dataWidth: Int) extends Module {
        val io = IO(new Bundle{
            val enq = Flipped(Decoupled(UInt(dataWidth.W))) 
            val deq = Decoupled(UInt(dataWidth.W))
        })
        
        val enqPtr = RegInit(0.U((1 + log2Ceil(n)).W)) 
        val deqPtr = RegInit(0.U((1 + log2Ceil(n)).W)) 
        val isFull = RegInit(false.B)  
        
        val doEnq = io.enq.valid && io.enq.ready 
        val doDeq = io.deq.valid && io.deq.ready 
        
        val isEmpty = !isFull && (enqPtr === deqPtr) 
        val deqPtrInc = Wire(UInt((1 + log2Ceil(n)).W));
        val enqPtrInc = Wire(UInt((1 + log2Ceil(n)).W));
        when(deqPtr + 1.U((1 + log2Ceil(n)).W) === n.U((1 + log2Ceil(n)).W)) {
            deqPtrInc := 0.U;
        }
        .otherwise {
            deqPtrInc := deqPtr + 1.U;
        }
        when(enqPtr + 1.U((1 + log2Ceil(n)).W) === n.U((1 + log2Ceil(n)).W)) {
            enqPtrInc := 0.U;
        }
        .otherwise {
            enqPtrInc := enqPtr + 1.U;
        }
        //val deqPtrInc = deqPtr + 1.U
        //val enqPtrInc = enqPtr + 1.U
        
        
        val isFullNext = Mux(doEnq && !doDeq && (enqPtrInc === deqPtr),   
                                                true.B , Mux(doDeq && isFull, 
                                                            false.B, isFull))
        enqPtr := Mux(doEnq, enqPtrInc, enqPtr) 
        deqPtr := Mux(doDeq, deqPtrInc, deqPtr) 
        
        isFull := isFullNext
        val ram = Mem(n, UInt(dataWidth.W))
        when(doEnq){
            ram(enqPtr) := io.enq.bits
        }
        io.enq.ready := !isFull
        io.deq.valid := !isEmpty
        
        ram(deqPtr) <> io.deq.bits
    }

    //instmem(1)=1,no input predicate;instmem(2)=1, has output predicate;instmem(0) select predicate
    class process_element(hasMul: Int, hasAdd: Int, hasDiv: Int, hasSub: Int, dataWidth: Int, instructionWidth: Int, hasLS: Int, hasAnd: Int, hasOr: Int, hasReverse: Int,hasLeft: Int, hasRight: Int,hasEq: Int,hasLeq: Int,hasGeq:Int,hasFor: Int) extends Module {
        val io = IO(new Bundle {
            val data_in0 = Flipped(Decoupled(UInt(dataWidth.W)))
            val data_in1 = Flipped(Decoupled(UInt(dataWidth.W)))
            val predicate_in0 = Flipped(Decoupled(UInt(1.W)))
            val predicate_in1 = Flipped(Decoupled(UInt(1.W)))
            val instruction_in = Flipped(Decoupled(UInt((2*instructionWidth).W)))
            val data_out = Decoupled(UInt(dataWidth.W))
            val predicate_out = Decoupled(UInt())
            val rerun = Output(UInt())
            val store_port = Decoupled(UInt(dataWidth.W))
            val store_data = Output(UInt(dataWidth.W))
            val load_port = Decoupled(UInt(dataWidth.W))
            val load_data = Flipped(Decoupled(UInt(dataWidth.W)))
            val start=Input(UInt(1.W))
            // val start=Input(UInt(1.W))
        })
        val instruction_mem_last = RegInit(0.U((instructionWidth).W))
        val instruction_mem_next = RegInit(0.U((instructionWidth).W))
        val inst_sel=RegInit(0.U((1).W))
        val inst_valid=RegInit(0.U((1).W))
        val sel=RegInit(0.U(1.W))
        val sel_next=Wire(UInt(1.W))
        val instlow=Wire(Vec(instructionWidth,UInt()))
        val insthigh=Wire(Vec(instructionWidth,UInt()))
        for(i<-0 to instructionWidth-1){
            insthigh(i):=io.instruction_in.bits(i+instructionWidth)
            instlow(i):=io.instruction_in.bits(i)
        }
        instruction_mem_last := Mux(io.instruction_in.valid, instlow.asUInt, instruction_mem_last)
        instruction_mem_next := Mux(io.instruction_in.valid, insthigh.asUInt, instruction_mem_next)
        io.instruction_in.ready := io.instruction_in.valid
        val mulResult = Wire(UInt(dataWidth.W))
        inst_valid:=Mux(io.instruction_in.valid,io.instruction_in.valid,inst_valid)
        inst_sel:=Mux(io.start===1.U,inst_valid,0.U)
        // val data0 = RegInit(0.U((dataWidth).W))
        // data0 := Mux(io.data_in0.valid, io.data_in0.bits, data0)
        // val data1 = RegInit(0.U((dataWidth).W))
        // data1 := Mux(io.data_in1.valid, io.data_in1.bits, data1)
        val Imm = Wire(Vec(dataWidth,UInt()))
        for(i <- 0 to dataWidth-1){
            Imm(i) := Mux(sel.asBool,instruction_mem_last(i+instructionWidth-dataWidth),instruction_mem_next(i+instructionWidth-dataWidth))
        }
        val imple=instruction_mem_last(instructionWidth-dataWidth-1)
        val forout=Mux(sel.asBool,instruction_mem_last(instructionWidth-dataWidth-2),instruction_mem_next(instructionWidth-dataWidth-2))
        val typeFunc = Wire(Vec(instructionWidth-7-dataWidth,UInt()))
        val function=typeFunc.asUInt
        val hasDin=Wire(Vec(2,UInt(1.W)))
        val instruction_mem_tmp=Wire(Vec(3,UInt(1.W)))
        for(i<-0 to 2){
            instruction_mem_tmp(i):=Mux(sel.asBool,instruction_mem_last(i),instruction_mem_next(i))
            if(i<2){
                hasDin(i):=Mux(sel.asBool,instruction_mem_last(instructionWidth-dataWidth-4+i),instruction_mem_next(instructionWidth-dataWidth-4+i))
            }
        }
        val instruction_mem=instruction_mem_tmp.asUInt
        for(i <- 0 to instructionWidth - 8-dataWidth) {
            typeFunc(i) := Mux(sel.asBool,instruction_mem_last(i+3),instruction_mem_next(i+3))
        }
        if(hasMul == 1) {
            mulResult := Mux(typeFunc.asUInt()===0.U,io.data_in0.bits * io.data_in1.bits,Mux(typeFunc.asUInt()===47.U,Imm.asUInt(),io.data_in0.bits*Imm.asUInt()) )
        }
        else {
            mulResult := 0.U
        }
        val addResult = Wire(UInt(dataWidth.W))
        if(hasAdd == 1) {
            addResult := Mux(typeFunc.asUInt()===1.U,io.data_in0.bits + io.data_in1.bits, io.data_in0.bits+Imm.asUInt())
        }
        else {
            addResult := 0.U
        }
        val subResult = Wire(UInt(dataWidth.W))
        if(hasSub == 1) {
            subResult := Mux(typeFunc.asUInt()===2.U,io.data_in0.bits - io.data_in1.bits,io.data_in0.bits-Imm.asUInt()) 
        }
        else {
            subResult := 0.U
        }
        val divResult = Wire(UInt(dataWidth.W))
        val remResult = Wire(UInt(dataWidth.W))
        if(hasDiv == 1) {
            divResult := Mux(typeFunc.asUInt()===3.U,io.data_in0.bits / io.data_in1.bits, io.data_in0.bits/Imm.asUInt()) 
            remResult := Mux(typeFunc.asUInt()===4.U,io.data_in0.bits % io.data_in1.bits,io.data_in0.bits%Imm.asUInt())
        }
        else {
            divResult := 0.U
            remResult := 0.U
        }
        val andResult = Wire(UInt(dataWidth.W))
        if(hasAnd == 1) {
            andResult := Mux(typeFunc.asUInt()===10.U||typeFunc.asUInt()===26.U,io.data_in0.bits & io.data_in1.bits,io.data_in0.bits&Imm.asUInt()) 
        }
        else {
            andResult := 0.U
        }
        val orResult = Wire(UInt(dataWidth.W))
        if(hasOr == 1) {
            orResult := Mux(typeFunc.asUInt()===11.U||typeFunc.asUInt()===28.U,io.data_in0.bits | io.data_in1.bits,io.data_in0.bits|Imm.asUInt()) 
        }
        else {
            orResult := 0.U
        }
        val reverseResult = Wire(UInt(dataWidth.W))
        if(hasReverse == 1) {
            reverseResult := ~io.data_in0.bits
        }
        else {
            reverseResult := 0.U
        }
        val leftResult = Wire(UInt(dataWidth.W))
        if(hasLeft == 1) {
            leftResult := 0.U//Mux(typeFunc.asUInt()===15.U,io.data_in0.bits << io.data_in1.bits,io.data_in0.bits<<Imm.asUInt()) 
        }
        else {
            leftResult := 0.U
        }
        val rightResult = Wire(UInt(dataWidth.W))
        if(hasRight == 1) {
            rightResult := 0.U//Mux(typeFunc.asUInt()===16.U,io.data_in0.bits >> io.data_in1.bits,io.data_in0.bits>>Imm.asUInt()) 
        }
        else {
            rightResult := 0.U
        }
        val eqResult = Wire(UInt(dataWidth.W))
        if(hasEq == 1) {
            eqResult := Mux(typeFunc.asUInt()===17.U||typeFunc.asUInt()===32.U,io.data_in0.bits === io.data_in1.bits,io.data_in0.bits===Imm.asUInt()) 
        }
        else {
            eqResult := 0.U
        }
        val leqResult = Wire(UInt(dataWidth.W))
        if(hasLeq == 1) {
            leqResult := Mux(typeFunc.asUInt()===18.U||typeFunc.asUInt()===34.U,io.data_in0.bits < io.data_in1.bits,io.data_in0.bits<Imm.asUInt()) 
        }
        else {
            leqResult := 0.U
        }
        val geqResult = Wire(UInt(dataWidth.W))
        if(hasGeq == 1) {
            geqResult := Mux(typeFunc.asUInt()===19.U||typeFunc.asUInt()===36.U,io.data_in0.bits > io.data_in1.bits,io.data_in0.bits>Imm.asUInt()) 
        }
        else {
            geqResult := 0.U
        }
        val toLoad = Wire(UInt(dataWidth.W))
        val storeResult = Wire(UInt(dataWidth.W))
        val toStore = Wire(UInt(dataWidth.W))
        if(hasLS == 1) {
            storeResult := Mux(typeFunc.asUInt()===38.U||typeFunc.asUInt()===41.U,io.data_in0.bits,0.U)
            toStore := Mux(typeFunc.asUInt()===38.U,io.data_in1.bits+Imm.asUInt(),Imm.asUInt())
            toLoad := Mux(typeFunc.asUInt()===39.U,io.data_in0.bits+Imm.asUInt(),Imm.asUInt())
        }
        else {
            storeResult := 0.U
            toStore := 0.U
            toLoad := 0.U
        }
        val predicate_mux0 = io.predicate_in0.bits&io.predicate_in1.bits&io.predicate_in0.valid&io.predicate_in1.valid
        val predicate_mux1 = io.predicate_in0.bits&io.predicate_in0.valid
        val predicate_mux0bit = io.predicate_in0.valid&io.predicate_in1.valid
        val predicate_mux1bit = io.predicate_in0.valid
        val useImm=typeFunc.asUInt()===5.U||typeFunc.asUInt()===6.U||typeFunc.asUInt()===7.U||typeFunc.asUInt()===8.U||typeFunc.asUInt()===9.U||typeFunc.asUInt()===13.U|| typeFunc.asUInt() === 14.U||typeFunc.asUInt()===20.U||typeFunc.asUInt()===21.U||typeFunc.asUInt()===22.U||typeFunc.asUInt()===23.U||typeFunc.asUInt()===24.U||typeFunc.asUInt()===27.U||typeFunc.asUInt()===29.U||typeFunc.asUInt()===31.U||typeFunc.asUInt()===33.U||typeFunc.asUInt()===35.U||typeFunc.asUInt()===37.U||typeFunc.asUInt()===39.U||typeFunc.asUInt()===41.U||typeFunc.asUInt()===42.U||typeFunc.asUInt()===43.U||typeFunc.asUInt()===47.U
        // io.predicate_in0.ready := io.predicate_in0.valid
        // io.predicate_in1.ready := io.predicate_in1.valid
        val predicate_restart=(~instruction_mem(1))&Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0bit, predicate_mux1bit)
        val data_restart=Mux(~useImm,io.data_in0.valid&io.data_in1.valid,io.data_in0.valid)
        val predicate = instruction_mem(1) | Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0, predicate_mux1) 
        val runningState = RegInit(UInt(8.W), 0.U)
        val nextrunningState = Wire(UInt(8.W))
        
        val predicate_gen=typeFunc.asUInt() === 26.U || typeFunc.asUInt() === 27.U || typeFunc.asUInt() === 28.U || typeFunc.asUInt() === 29.U ||typeFunc.asUInt() === 30.U ||typeFunc.asUInt() === 31.U ||typeFunc.asUInt() === 32.U ||typeFunc.asUInt() === 33.U || typeFunc.asUInt() === 34.U ||typeFunc.asUInt() === 35.U ||typeFunc.asUInt() === 36.U ||typeFunc.asUInt() === 37.U
        val fori=RegInit(0.U((dataWidth).W))
        
        val nextI=Wire(UInt(dataWidth.W))
        fori:=nextI
       
        sel:=sel_next
        val typefor=typeFunc.asUInt===43.U||typeFunc.asUInt()===44.U||typeFunc.asUInt()===45.U||typeFunc.asUInt===46.U
        val forup=typeFunc.asUInt()===45.U||typeFunc.asUInt===46.U
        val forin=typeFunc.asUInt===44.U||typeFunc.asUInt===45.U
        val forall=typeFunc.asUInt===45.U
        val Upside=Wire(UInt(dataWidth.W))
        val Immdata=Imm.asUInt
        Upside:=Mux(forup,Mux(forall,io.data_in1.bits,io.data_in0.bits),Imm.asUInt)
        val Step=Mux(forin,io.data_in0.bits,1.U)
        val phiInst=typeFunc.asUInt()===48.U
        when (runningState === 0.U) {
            when(phiInst===1.U){
                nextrunningState := 0.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                sel_next:=0.U
                io.data_in0.ready := 0.U
                io.data_in1.ready := 0.U
                io.predicate_out.bits := 0.U
                io.predicate_out.valid :=0.U
                // io.rerun := 0.U
            }
            .elsewhen(predicate === 1.U&&inst_sel===1.U){
                when(typeFunc.asUInt()===45.U&&io.data_in1.valid||typeFunc.asUInt()===46.U&&io.data_in0.valid){
                    nextrunningState := 1.U
                }
                .elsewhen(typeFunc.asUInt()===45.U&&(io.data_in1.valid===0.U)||typeFunc.asUInt()===46.U&&(io.data_in0.valid===0.U)){
                    nextrunningState := 0.U
                }
                .otherwise{
                    nextrunningState := 1.U
                }
                
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                io.data_in0.ready := 0.U
                io.data_in1.ready := 0.U
                sel_next:=0.U
                io.predicate_out.bits := 0.U
                io.predicate_out.valid :=0.U
            }
            .elsewhen(inst_sel===1.U&(~imple)&(~instruction_mem(1)) & Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0bit, predicate_mux1bit)===1.U&Mux((~instruction_mem(1))&instruction_mem(0), io.predicate_in0.bits&io.predicate_in1.bits,  io.predicate_in0.bits)===0.U){
                when(typeFunc.asUInt()===25.U){
                    io.data_in0.ready := 0.U
                    io.data_in1.ready := 0.U
                    io.predicate_out.bits := 0.U
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    when(io.predicate_out.ready&&io.predicate_out.valid){
                        nextrunningState := 2.U
                        io.predicate_in0.ready := (~instruction_mem(1))
                        io.predicate_in1.ready := (~instruction_mem(1))&(~instruction_mem(0))
                    }
                    .otherwise{
                        nextrunningState := 0.U
                        io.predicate_in0.ready := 0.U
                        io.predicate_in1.ready := 0.U
                    }
                }
                .otherwise{
                    io.predicate_out.bits := 0.U
                    io.predicate_out.valid :=0.U
                    when(hasDin.asUInt()===0.U&&io.data_in0.valid&&io.data_in1.valid||hasDin.asUInt()===1.U&&io.data_in0.valid){
                        nextrunningState := 2.U
                        io.predicate_in0.ready := (~instruction_mem(1))
                        io.predicate_in1.ready := (~instruction_mem(1))&(~instruction_mem(0))
                        io.data_in0.ready := io.data_in0.valid
                        io.data_in1.ready := hasDin.asUInt()===0.U&&io.data_in1.valid
                    }
                    .elsewhen(hasDin.asUInt()===0.U||hasDin.asUInt()===1.U){
                        nextrunningState := 0.U
                        io.predicate_in0.ready := 0.U
                        io.predicate_in1.ready := 0.U
                        io.data_in0.ready := 0.U
                        io.data_in1.ready := 0.U
                    }
                    .otherwise{
                        nextrunningState := 2.U
                        io.predicate_in0.ready := (~instruction_mem(1))
                        io.predicate_in1.ready := (~instruction_mem(1))&(~instruction_mem(0))
                        io.data_in0.ready := 0.U
                        io.data_in1.ready := 0.U
                    }
                }
                sel_next:=0.U
            }
            .elsewhen(inst_sel===1.U&(imple)&(~instruction_mem(1)) & Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0bit, predicate_mux1bit)&Mux((~instruction_mem(1))&instruction_mem(0), io.predicate_in0.bits&io.predicate_in1.bits,  io.predicate_in0.bits)===0.U){
                when(typeFunc.asUInt()===45.U&&io.data_in1.valid||typeFunc.asUInt()===46.U&&io.data_in0.valid){
                    nextrunningState := 1.U
                }
                .elsewhen(typeFunc.asUInt()===45.U&&(~io.data_in1.valid)||typeFunc.asUInt()===46.U&&(~io.data_in0.valid)){
                    nextrunningState := 0.U
                }
                .otherwise{
                    nextrunningState := 1.U
                }
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                sel_next:=1.U
                io.data_in0.ready := 0.U
                io.data_in1.ready := 0.U
                io.predicate_out.bits := 0.U
                io.predicate_out.valid :=0.U
            }
            .otherwise{
                nextrunningState := 0.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                sel_next:=0.U
                io.data_in0.ready := 0.U
                io.data_in1.ready := 0.U
                io.predicate_out.bits := 0.U
                io.predicate_out.valid :=0.U
            }

            io.rerun := 0.U
            io.data_out.bits := 0.U
            io.data_out.valid := 0.U
            // io.predicate_out.bits := 0.U
            // io.predicate_out.valid := 0.U
            
            
            // io.predicate_in0.ready := 0.U
            // io.predicate_in1.ready := 0.U
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.load_data.ready:=0.U
            nextI:=0.U
        }
        .elsewhen(runningState === 1.U){
            sel_next:=sel
            when(typefor){
                io.data_out.bits := fori
                io.data_out.valid := Mux(forout,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),1.U),0.U)
                io.predicate_out.bits := 1.U
                io.store_port.valid := 0.U
                io.store_port.bits := toStore
                io.store_data := storeResult
                io.load_port.valid := 0.U
                io.load_port.bits := toLoad
                io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                //nextI:=0.U
            }
            .otherwise{
                //nextI:=0.U
                when(typeFunc.asUInt() === 0.U || typeFunc.asUInt() === 5.U ||typeFunc.asUInt()===47.U){
                    io.data_out.bits := mulResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)||typeFunc.asUInt()===47.U
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 1.U || typeFunc.asUInt() === 6.U ){
                    io.data_out.bits := addResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 2.U || typeFunc.asUInt() === 7.U ){
                    io.data_out.bits := subResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 3.U || typeFunc.asUInt() === 8.U ){
                    io.data_out.bits := divResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 4.U || typeFunc.asUInt() === 9.U ){
                    io.data_out.bits := remResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 13.U ){
                    io.data_out.bits := andResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 11.U || typeFunc.asUInt() === 14.U ){
                    io.data_out.bits := orResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 12.U){
                    io.data_out.bits := reverseResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 20.U){
                    io.data_out.bits := leftResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 15.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 21.U){
                    io.data_out.bits := rightResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 16.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 22.U){
                    io.data_out.bits := eqResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 17.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 23.U){
                    io.data_out.bits := leqResult
                    io.data_out.valid := Mux(typeFunc.asUInt() === 18.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.predicate_out.valid :=instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 24.U){
                    io.data_out.bits := geqResult
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.data_out.valid := Mux(typeFunc.asUInt() === 19.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.predicate_out.bits := 1.U
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                }
                .elsewhen(typeFunc.asUInt() === 25.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := 1.U
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
                .elsewhen(typeFunc.asUInt() === 26.U||typeFunc.asUInt() === 27.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := andResult
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
                .elsewhen(typeFunc.asUInt() === 28.U||typeFunc.asUInt() === 29.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := orResult
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
                .elsewhen(typeFunc.asUInt() === 30.U||typeFunc.asUInt() === 31.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := reverseResult
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
                .elsewhen(typeFunc.asUInt() === 32.U||typeFunc.asUInt() === 33.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := eqResult
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
                .elsewhen(typeFunc.asUInt() === 34.U||typeFunc.asUInt() === 35.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := leqResult
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
                .elsewhen(typeFunc.asUInt() === 36.U||typeFunc.asUInt() === 37.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := geqResult
                    io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
                .elsewhen(typeFunc.asUInt()===38.U||typeFunc.asUInt()===41.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := 1.U
                    io.predicate_out.valid := io.store_port.ready && (instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid))
                    io.store_port.valid := (io.data_in1.valid||typeFunc.asUInt()===41.U)&&io.data_in0.valid
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    
                }
                .elsewhen(typeFunc.asUInt()===39.U||typeFunc.asUInt()===42.U){
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := 1.U
                    io.predicate_out.valid := 0.U//io.load_port.ready && (instruction_mem(1)||Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid))
                    io.load_port.valid := io.data_in0.valid||typeFunc.asUInt()===42.U
                    io.load_port.bits := toLoad
                    io.store_port.valid := 0.U
                    io.store_port.bits := 0.U
                    io.store_data := 0.U
                    // io.store_data := storeResult
                }
                .otherwise{
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    io.predicate_out.bits := 0.U
                    io.predicate_out.valid :=0.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                }
            }
            when(typefor&&(io.predicate_out.valid===1.U&&io.predicate_out.ready===1.U&&io.data_out.valid&&io.data_out.ready||io.predicate_out.ready&&io.predicate_out.valid&&forout===0.U||io.data_out.valid&&io.data_out.ready&&(~instruction_mem(2)))){
                when(fori+Step<Upside){
                    nextrunningState := Mux(forin,Mux(io.data_in0.valid,1.U,1.U),1.U)
                    nextI:=fori+Mux(forin,Mux(io.data_in0.valid,Step,0.U),Step)
                    io.data_in0.ready := Mux(typeFunc.asUInt()===46.U,0.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U))
                    io.data_in1.ready:=0.U
                    io.predicate_in0.ready:=0.U
                    io.predicate_in1.ready:=0.U
                }
                .otherwise{
                    nextrunningState := Mux(forin,Mux(io.data_in0.valid,2.U,1.U),2.U)
                    nextI:=fori+Mux(forin,Mux(io.data_in0.valid,Step,0.U),Step)
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                    io.data_in0.ready:= Mux(typeFunc.asUInt()===46.U,1.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U))   
                    io.data_in1.ready:=Mux(typeFunc.asUInt()===45.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U),0.U)
                }
                
                
                
            }
            .elsewhen(typefor&&io.predicate_out.valid===1.U&&io.predicate_out.ready===1.U&&(io.data_out.ready===0.U||io.data_out.valid===0.U)&&forout){
                
                nextrunningState := 8.U
                nextI:=fori
                
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                io.data_in0.ready := 0.U
                io.data_in1.ready:=0.U
            }
            .elsewhen(typefor&&(io.predicate_out.ready===0.U||io.predicate_out.valid===0.U)&&io.data_out.ready===1.U&&io.data_out.valid===1.U&&instruction_mem(2)){
                
                nextrunningState := 9.U
                nextI:=fori+Mux(forin,Mux(io.data_in0.valid,Step,0.U),Step)
                
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                io.data_in0.ready := Mux(typeFunc.asUInt()===46.U,1.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U))
                io.data_in1.ready:=Mux(typeFunc.asUInt()===45.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U),0.U)
            }
            .elsewhen(typefor&&(io.predicate_out.ready===0.U||io.predicate_out.valid===0.U)&&(io.data_out.ready===0.U||io.data_out.valid===0.U)){
                
                nextrunningState := 1.U
                nextI:=fori
                
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                io.data_in0.ready := 0.U
                io.data_in1.ready:=0.U
            }
            .otherwise{
                nextI:=fori
                when((typeFunc.asUInt()===38.U||typeFunc.asUInt()===41.U)&& io.store_port.valid === 1.U && io.store_port.ready === 1.U&& io.predicate_out.valid === 1.U && io.predicate_out.ready === 1.U || (typeFunc.asUInt()===38.U||typeFunc.asUInt()===41.U)&& io.store_port.valid === 1.U && io.store_port.ready === 1.U && instruction_mem(2)===0.U){
                    nextrunningState := 2.U
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                    io.data_in0.ready := typeFunc.asUInt()===38.U|1.U
                    io.data_in1.ready:=typeFunc.asUInt()===38.U
                }
                
                .elsewhen((typeFunc.asUInt()===38.U||typeFunc.asUInt()===41.U)&& io.store_port.valid === 1.U && io.store_port.ready === 1.U &&(io.predicate_out.ready===0.U||io.predicate_out.valid===0.U)&&instruction_mem(2)){
                    nextrunningState := 5.U
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                    io.data_in0.ready := typeFunc.asUInt()===38.U|1.U
                    io.data_in1.ready:=typeFunc.asUInt()===38.U
                }
                
                .elsewhen((typeFunc.asUInt()===38.U||typeFunc.asUInt()===41.U) && (io.store_port.ready === 0.U||io.store_port.valid===0.U)){
                    nextrunningState := 1.U
                    io.predicate_in0.ready := 0.U//Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := 0.U//Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.data_in0.ready := 0.U
                    io.data_in1.ready:=0.U
                }
                .elsewhen((typeFunc.asUInt()===39.U||typeFunc.asUInt()===42.U)&& io.load_port.valid === 1.U && io.load_port.ready === 1.U){
                    nextrunningState := 6.U
                    io.predicate_in0.ready := 0.U//Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := 0.U//Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.data_in0.ready := typeFunc.asUInt()===39.U
                    io.data_in1.ready:=0.U
                }
                .elsewhen((typeFunc.asUInt()===39.U||typeFunc.asUInt()===42.U) && (io.load_port.ready === 0.U||io.load_port.valid===0.U)){
                    nextrunningState := 1.U
                    io.predicate_in0.ready := 0.U//Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := 0.U//Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.data_in0.ready := 0.U
                    io.data_in1.ready:=0.U
                }
                .elsewhen(io.data_out.ready === 0.U&&io.predicate_out.valid === 1.U&&io.predicate_out.ready === 1.U&&typeFunc.asUInt() =/= 25.U&&predicate_gen=/=1.U&&instruction_mem(2)===1.U){
                    nextrunningState := 4.U
                    io.data_in0.ready:=0.U
                    io.data_in1.ready:=0.U
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                }
                .elsewhen(io.predicate_out.valid === 1.U&&io.predicate_out.ready === 1.U&&(typeFunc.asUInt() === 25.U||predicate_gen===1.U&&typeFunc.asUInt()=/=25.U && Mux( typeFunc.asUInt()%2.U === 0.U, io.data_in0.valid&io.data_in1.valid,io.data_in0.valid))){
                    nextrunningState := 2.U
                    io.data_in0.ready:=typeFunc.asUInt()=/=25.U && Mux( typeFunc.asUInt()%2.U === 0.U, io.data_in0.valid&io.data_in1.valid,io.data_in0.valid)
                    io.data_in1.ready:=typeFunc.asUInt()=/=25.U && Mux(typeFunc.asUInt()%2.U === 0.U, io.data_in0.valid&io.data_in1.valid,false.B)
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                }
                .elsewhen((io.predicate_out.ready===0.U||io.predicate_out.valid===0.U)&&(typeFunc.asUInt() === 25.U||predicate_gen===1.U)){
                    nextrunningState := 1.U
                    io.data_in0.ready:=0.U
                    io.data_in1.ready:=0.U
                    io.predicate_in0.ready :=0.U
                    io.predicate_in1.ready :=0.U
                }
                
                .elsewhen(io.data_out.ready === 1.U&&io.data_out.valid === 1.U&&io.predicate_out.valid === 1.U&&io.predicate_out.ready === 1.U&&instruction_mem(2)===1.U){
                    nextrunningState := 2.U
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                    io.data_in0.ready := typeFunc.asUInt=/=47.U&&Mux(~useImm.asBool(),io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.data_in1.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U|| typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,false.B)
                }
                .elsewhen(io.data_out.ready === 1.U&&io.data_out.valid === 1.U&&(io.predicate_out.ready===0.U||io.predicate_out.valid===0.U)&&(instruction_mem(2))&&(instruction_mem(1))){
                    nextrunningState := 3.U
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                    io.data_in0.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.data_in1.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U|| typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,false.B)
                }
                .elsewhen(io.data_out.ready === 1.U&&io.data_out.valid === 1.U&&(io.predicate_out.ready===0.U||io.predicate_out.valid===0.U)&&(instruction_mem(2))&&(~instruction_mem(1))){
                    nextrunningState := 3.U
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                    io.data_in0.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.data_in1.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U|| typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,false.B)
                }
                .elsewhen(io.data_out.ready === 1.U&&io.data_out.valid === 1.U&&(~instruction_mem(2))){
                    nextrunningState := 2.U
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                    io.data_in0.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                    io.data_in1.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U|| typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,false.B)
                }
                // .elsewhen(io.data_out.ready === 1.U&&io.predicate_out.ready === 0.U&&(instruction_mem(2))&&(instruction_mem(1))){
                //     nextrunningState := 3.U
                //     io.predicate_in0.ready := 0.U
                //     io.predicate_in1.ready := 0.U
                //     io.data_in0.ready := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                //     io.data_in1.ready := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U|| typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in1.valid)
                // }
                
                .otherwise{
                    nextrunningState := 1.U
                    io.data_in0.ready:=0.U
                    io.data_in1.ready:=0.U
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                }
            }
            io.rerun := 0.U
            io.load_data.ready:=0.U
        }
        .elsewhen(runningState === 3.U){
            io.rerun := 0.U
            io.data_out.bits := 0.U
            io.data_out.valid := 0.U
            io.data_in0.ready := 0.U
            io.data_in1.ready := 0.U
            io.predicate_out.bits := 1.U
            io.predicate_out.valid :=instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.load_data.ready:=0.U    
            nextI:=0.U
            sel_next:=sel
            when(io.predicate_out.ready === 1.U&&io.predicate_out.valid === 1.U){
                nextrunningState := 2.U
                io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
            }
            .otherwise{
                nextrunningState := 3.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
            }
        }
        .elsewhen(runningState===4.U){
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.load_data.ready:=0.U
            io.rerun:=0.U
            sel_next:=sel
            when(typeFunc.asUInt() === 0.U || typeFunc.asUInt() === 5.U ||typeFunc.asUInt()===47.U){
                io.data_out.bits := mulResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)||typeFunc.asUInt()===47.U
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 1.U || typeFunc.asUInt() === 6.U ){
                io.data_out.bits := addResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 2.U || typeFunc.asUInt() === 7.U ){
                io.data_out.bits := subResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 3.U || typeFunc.asUInt() === 8.U ){
                io.data_out.bits := divResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 4.U || typeFunc.asUInt() === 9.U ){
                io.data_out.bits := remResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 13.U ){
                io.data_out.bits := andResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 11.U || typeFunc.asUInt() === 14.U ){
                io.data_out.bits := orResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 12.U){
                io.data_out.bits := reverseResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 20.U){
                io.data_out.bits := leftResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 15.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 21.U){
                io.data_out.bits := rightResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 16.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 22.U){
                io.data_out.bits := eqResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 17.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 23.U){
                io.data_out.bits := leqResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 18.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .elsewhen(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 24.U){
                io.data_out.bits := geqResult
                io.data_out.valid := Mux(typeFunc.asUInt() === 19.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.predicate_out.bits := 0.U
                io.predicate_out.valid := 0.U
            }
            .otherwise{
                io.data_out.bits := 0.U
                io.data_out.valid := 0.U
                io.predicate_out.bits := 0.U
                io.predicate_out.valid :=0.U
            }
            when(io.data_out.ready === 1.U&&io.data_out.valid === 1.U){
                nextrunningState := 2.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                io.data_in0.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U || typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,io.data_in0.valid)
                io.data_in1.ready := typeFunc.asUInt=/=47.U&&Mux(typeFunc.asUInt() === 19.U||typeFunc.asUInt() === 18.U||typeFunc.asUInt() === 17.U||typeFunc.asUInt() === 16.U||typeFunc.asUInt() === 15.U||typeFunc.asUInt() === 1.U|| typeFunc.asUInt() === 11.U|| typeFunc.asUInt() === 10.U|| typeFunc.asUInt() === 0.U|| typeFunc.asUInt() === 2.U|| typeFunc.asUInt() === 3.U|| typeFunc.asUInt() === 4.U,io.data_in0.valid & io.data_in1.valid,false.B)
            }
            .otherwise{
                nextrunningState := 4.U
                // io.data_out.bits := 0.U
                // io.data_out.valid := 0.U
                // io.predicate_out.bits := 0.U
                // io.predicate_out.valid :=0.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
                io.data_in0.ready := 0.U
                io.data_in1.ready := 0.U
            }
            nextI:=0.U
        }
        .elsewhen(runningState===5.U){
            io.data_out.bits := 0.U
            io.data_out.valid := 0.U
            io.predicate_out.bits := 1.U
            io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            sel_next:=sel
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.load_data.ready:=0.U
            io.data_in0.ready := 0.U
            io.data_in1.ready := 0.U
            io.rerun:=0.U
            nextI:=0.U
            when(io.predicate_out.ready&&io.predicate_out.valid){
                nextrunningState:=2.U
                io.predicate_in1.ready:=(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                io.predicate_in0.ready:=(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
            }
            .otherwise{
                nextrunningState:=5.U
                io.predicate_in1.ready:=0.U
                io.predicate_in0.ready:=0.U
            
            }
        }
        
        .elsewhen(runningState===6.U){
            sel_next:=sel
            when(io.load_data.valid===1.U){
                io.data_out.bits := io.load_data.bits
                io.data_out.valid := io.load_data.valid
                io.predicate_out.bits := 1.U
                io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                when(io.data_out.ready ===1.U&&io.data_out.valid === 1.U &&io.predicate_out.valid === 1.U&& io.predicate_out.ready === 1.U||io.data_out.ready ===1.U&&io.data_out.valid === 1.U &&instruction_mem(2)===0.U){
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                    nextrunningState:=2.U
                    io.load_data.ready:=1.U
                }
                .elsewhen((io.data_out.ready ===0.U||io.data_out.valid===0.U) && io.predicate_out.ready === 1.U&&io.predicate_out.valid === 1.U){
                    nextrunningState:=7.U
                    io.predicate_in0.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                    io.predicate_in1.ready := (~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                    io.load_data.ready:=0.U
                }
                .elsewhen(io.data_out.ready ===1.U&&io.data_out.valid === 1.U&&(io.predicate_out.ready===0.U||io.predicate_out.valid===0.U)&&instruction_mem(2)){
                    nextrunningState:=10.U
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                    io.load_data.ready:=0.U
                }
                .otherwise{
                    nextrunningState:=6.U
                    io.load_data.ready:=0.U
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                }
            }
            .otherwise{
                io.data_out.bits := io.load_data.bits
                io.data_out.valid := 0.U
                io.predicate_out.bits := 1.U
                io.predicate_out.valid := 0.U
                nextrunningState:=6.U
                io.load_data.ready:=0.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
            }
            io.data_in0.ready := 0.U
            io.data_in1.ready := 0.U
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.rerun:=0.U
            nextI:=0.U
        }
        .elsewhen(runningState===7.U){
            sel_next:=sel
            when(io.load_data.valid===1.U){
                io.data_out.bits := io.load_data.bits
                io.data_out.valid := io.load_data.valid
                io.predicate_out.bits := 1.U
                io.predicate_out.valid := 0.U
                when(io.data_out.ready ===1.U&&io.data_out.valid === 1.U ){
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                    nextrunningState:=2.U
                    io.load_data.ready:=1.U
                }   
                
                .otherwise{
                    io.predicate_in0.ready := 0.U
                    io.predicate_in1.ready := 0.U
                    nextrunningState:=7.U
                    io.load_data.ready:=0.U
                }
            }
            .otherwise{
                io.data_out.bits := io.load_data.bits
                io.data_out.valid := 0.U
                io.predicate_out.bits := 1.U
                io.predicate_out.valid := 0.U
                nextrunningState:=7.U
                io.load_data.ready:=0.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
            }
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.rerun:=0.U
            nextI:=0.U
            io.data_in0.ready := 0.U
            io.data_in1.ready := 0.U
        }
        .elsewhen(runningState===8.U){
            sel_next:=sel
            io.data_out.bits := fori
            io.data_out.valid := Mux(forout,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),1.U),0.U)
            io.predicate_out.bits := 1.U
            io.predicate_out.valid := 0.U
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.load_data.ready:=0.U
            io.rerun:=0.U
            io.predicate_in1.ready:=0.U
            io.predicate_in0.ready:=0.U
            
            
            when(io.data_out.ready&&io.data_out.valid){
                when(fori+Mux(forin,Mux(io.data_in0.valid,Step,0.U),Step)<Upside){
                    nextrunningState := 1.U
                    nextI:=fori+Mux(forin,Mux(io.data_in0.valid,Step,0.U),Step)
                    io.data_in0.ready := Mux(typeFunc.asUInt()===46.U,0.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U))
                    io.data_in1.ready := 0.U
                }
                .otherwise{
                    nextrunningState := 2.U
                    nextI:=fori
                    io.data_in0.ready := Mux(typeFunc.asUInt()===46.U,0.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U))
                    io.data_in1.ready:=Mux(typeFunc.asUInt()===45.U,Mux(forin,Mux(io.data_in0.valid,1.U,0.U),0.U),0.U)
                }
            }
            .otherwise{
                nextrunningState:= 8.U
                io.data_in1.ready := 0.U
                nextI:=fori
                io.data_in0.ready := 0.U
            }
        }
        .elsewhen(runningState===9.U){
            sel_next:=sel
            io.data_out.bits := 0.U
            io.data_out.valid := 0.U
            io.predicate_out.bits := 1.U
            io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            nextI:=fori
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.load_data.ready:=0.U
            io.rerun:=0.U
            io.data_in0.ready := 0.U
            io.data_in1.ready := 0.U
            when(io.predicate_out.ready&&io.predicate_out.valid){
                when(fori<Upside){
                    nextrunningState:=1.U
                    io.predicate_in1.ready:=0.U
                    io.predicate_in0.ready:=0.U
            
                }
                .otherwise{
                    nextrunningState:=2.U
                    io.predicate_in1.ready:=(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                    io.predicate_in0.ready:=(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
            
                }
            }
            .otherwise{
                nextrunningState:=9.U
                io.predicate_in1.ready:=0.U
                io.predicate_in0.ready:=0.U
            
            }
        }
        .elsewhen(runningState===10.U){
            sel_next:=sel
            io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
            when(io.predicate_out.ready===1.U&&io.predicate_out.ready){
                io.data_out.bits := io.load_data.bits
                io.data_out.valid := 0.U
                io.predicate_out.bits := 1.U
                io.predicate_in1.ready:=(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,false.B)
                io.predicate_in0.ready:=(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                io.load_data.ready:=0.U
                nextrunningState:=2.U
                
            }
            .otherwise{
                io.data_out.bits := io.load_data.bits
                io.data_out.valid := 0.U
                io.predicate_out.bits := 1.U
                //io.predicate_out.valid := 0.U
                nextrunningState:=10.U
                io.load_data.ready:=0.U
                io.predicate_in0.ready := 0.U
                io.predicate_in1.ready := 0.U
            }
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.rerun:=0.U
            nextI:=0.U
            io.data_in0.ready := 0.U
            io.data_in1.ready := 0.U
        }
        .otherwise{
            sel_next:=0.U
            io.rerun := 1.U
            nextI:=0.U
            io.load_data.ready:=0.U
            io.data_out.bits := 0.U
            io.data_out.valid := 0.U
            io.data_in0.ready := 0.U
            io.data_in1.ready := 0.U
            io.predicate_in0.ready := 0.U
            io.predicate_in1.ready := 0.U
            io.store_port.valid := 0.U
            io.store_port.bits := toStore
            io.store_data := storeResult
            io.load_port.valid := 0.U
            io.load_port.bits := toLoad
            io.predicate_out.bits := 0.U
            io.predicate_out.valid := 0.U
            when(io.instruction_in.valid === 1.U){
                nextrunningState := 0.U
                

                // io.rerun := 0.U
                io.data_out.bits := 0.U
                io.data_out.valid := 0.U
                // io.predicate_out.bits := 0.U
                // io.predicate_out.valid := 0.U
                
                
                // io.predicate_in0.ready := 0.U
                // io.predicate_in1.ready := 0.U
                io.store_port.valid := 0.U
                io.store_port.bits := toStore
                io.store_data := storeResult
                io.load_port.valid := 0.U
                io.load_port.bits := toLoad
                io.load_data.ready:=0.U
                nextI:=0.U
            }
            .otherwise{
                when(predicate_restart===1.U||data_restart===1.U){
                    // nextrunningState := 0.U
                    when(phiInst===1.U){
                        nextrunningState := 0.U
                        io.predicate_in0.ready := 0.U
                        io.predicate_in1.ready := 0.U
                        sel_next:=0.U
                        io.data_in0.ready := 0.U
                        io.data_in1.ready := 0.U
                        io.predicate_out.bits := 0.U
                        io.predicate_out.valid :=0.U
                        // io.rerun := 0.U
                    }
                    .elsewhen(predicate === 1.U&&inst_sel===1.U){
                        when(typeFunc.asUInt()===45.U&&io.data_in1.valid||typeFunc.asUInt()===46.U&&io.data_in0.valid){
                            nextrunningState := 1.U
                        }
                        .elsewhen(typeFunc.asUInt()===45.U&&(io.data_in1.valid===0.U)||typeFunc.asUInt()===46.U&&(io.data_in0.valid===0.U)){
                            nextrunningState := 0.U
                        }
                        .otherwise{
                            nextrunningState := 1.U
                        }
                        
                        io.predicate_in0.ready := 0.U
                        io.predicate_in1.ready := 0.U
                        io.data_in0.ready := 0.U
                        io.data_in1.ready := 0.U
                        sel_next:=0.U
                        io.predicate_out.bits := 0.U
                        io.predicate_out.valid :=0.U
                    }
                    .elsewhen(inst_sel===1.U&(~imple)&(~instruction_mem(1)) & Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0bit, predicate_mux1bit)===1.U&Mux((~instruction_mem(1))&instruction_mem(0), io.predicate_in0.bits&io.predicate_in1.bits,  io.predicate_in0.bits)===0.U){
                        when(typeFunc.asUInt()===25.U){
                            io.data_in0.ready := 0.U
                            io.data_in1.ready := 0.U
                            io.predicate_out.bits := 0.U
                            io.predicate_out.valid := instruction_mem(2)&&instruction_mem(1)||instruction_mem(2)&&(~instruction_mem(1))&&Mux((~instruction_mem(1))&instruction_mem(0),io.predicate_in0.valid & io.predicate_in1.valid,io.predicate_in0.valid)
                            when(io.predicate_out.ready&&io.predicate_out.valid){
                                nextrunningState := 2.U
                                io.predicate_in0.ready := (~instruction_mem(1))
                                io.predicate_in1.ready := (~instruction_mem(1))&(~instruction_mem(0))
                            }
                            .otherwise{
                                nextrunningState := 0.U
                                io.predicate_in0.ready := 0.U
                                io.predicate_in1.ready := 0.U
                            }
                        }
                        .otherwise{
                            io.predicate_out.bits := 0.U
                            io.predicate_out.valid :=0.U
                            when(hasDin.asUInt()===0.U&&io.data_in0.valid&&io.data_in1.valid||hasDin.asUInt()===1.U&&io.data_in0.valid){
                                nextrunningState := 2.U
                                io.predicate_in0.ready := (~instruction_mem(1))
                                io.predicate_in1.ready := (~instruction_mem(1))&(~instruction_mem(0))
                                io.data_in0.ready := io.data_in0.valid
                                io.data_in1.ready := hasDin.asUInt()===0.U&&io.data_in1.valid
                            }
                            .elsewhen(hasDin.asUInt()===0.U||hasDin.asUInt()===1.U){
                                nextrunningState := 0.U
                                io.predicate_in0.ready := 0.U
                                io.predicate_in1.ready := 0.U
                                io.data_in0.ready := 0.U
                                io.data_in1.ready := 0.U
                            }
                            .otherwise{
                                nextrunningState := 2.U
                                io.predicate_in0.ready := (~instruction_mem(1))
                                io.predicate_in1.ready := (~instruction_mem(1))&(~instruction_mem(0))
                                io.data_in0.ready := 0.U
                                io.data_in1.ready := 0.U
                            }
                        }
                        sel_next:=0.U
                    }
                    .elsewhen(inst_sel===1.U&(imple)&(~instruction_mem(1)) & Mux((~instruction_mem(1))&instruction_mem(0), predicate_mux0bit, predicate_mux1bit)&Mux((~instruction_mem(1))&instruction_mem(0), io.predicate_in0.bits&io.predicate_in1.bits,  io.predicate_in0.bits)===0.U){
                        when(typeFunc.asUInt()===45.U&&io.data_in1.valid||typeFunc.asUInt()===46.U&&io.data_in0.valid){
                            nextrunningState := 1.U
                        }
                        .elsewhen(typeFunc.asUInt()===45.U&&(~io.data_in1.valid)||typeFunc.asUInt()===46.U&&(~io.data_in0.valid)){
                            nextrunningState := 0.U
                        }
                        .otherwise{
                            nextrunningState := 1.U
                        }
                        io.predicate_in0.ready := 0.U
                        io.predicate_in1.ready := 0.U
                        sel_next:=1.U
                        io.data_in0.ready := 0.U
                        io.data_in1.ready := 0.U
                        io.predicate_out.bits := 0.U
                        io.predicate_out.valid :=0.U
                    }
                    .otherwise{
                        nextrunningState := 2.U
                        io.predicate_in0.ready := 0.U
                        io.predicate_in1.ready := 0.U
                        sel_next:=0.U
                        io.data_in0.ready := 0.U
                        io.data_in1.ready := 0.U
                        io.predicate_out.bits := 0.U
                        io.predicate_out.valid :=0.U
                    }

                    // io.rerun := 0.U
                    io.data_out.bits := 0.U
                    io.data_out.valid := 0.U
                    // io.predicate_out.bits := 0.U
                    // io.predicate_out.valid := 0.U
                    
                    
                    // io.predicate_in0.ready := 0.U
                    // io.predicate_in1.ready := 0.U
                    io.store_port.valid := 0.U
                    io.store_port.bits := toStore
                    io.store_data := storeResult
                    io.load_port.valid := 0.U
                    io.load_port.bits := toLoad
                    io.load_data.ready:=0.U
                    nextI:=0.U
                }
                .otherwise{
                    nextrunningState := 2.U
                }
            }
            
        }
        runningState := nextrunningState
    }

    object myPE extends App {
        //(new chisel3.stage.ChiselStage).emitVerilog(new HEOOO(16, 64, 64, 64, 16, 8, 17, 7, 7, 32, 27, 64, 4, 4, 4, 1, 12, 12, 12, 2, 5, 8));
        //(new chisel3.stage.ChiselStage).emitVerilog(new IssueUnit(64, 16, 16, 7, 12));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new process_element(1,1,1,1,32,64,1,1,1,1,1,1,1,1,1,1))));
    }
}