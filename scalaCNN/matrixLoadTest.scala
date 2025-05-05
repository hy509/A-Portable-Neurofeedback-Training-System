import chisel3._
import chisel3.tester._

import chisel3.util._

//import chiseltest._
import org.scalatest._
import scala.util._
import scala.util.Random 
import scala.math._
import ChiselLib._
import cnnPE._
import matrixLoad._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.WriteVcdAnnotation

class GenInst(val dataWidth: Int =128) extends Module{
    val io=IO(new Bundle{
        val Row=Output(UInt(32.W))
        val subRow=Output(UInt(32.W))
        val steps=Output(UInt(32.W))
        val outputD=Output(Vec(128,UInt(1.W)))
        val outputValid=Output(Vec(128,UInt(1.W)))
        
    })
    val inputInst=Wire(Vec(128,UInt(1.W)))
    val subMatrix=Wire(Vec(128,UInt(1.W)))
    var Row=Random.nextInt(32)*1
    while(Row==0){
        Row=Random.nextInt(32)*1
    }
    var subRow=Random.nextInt(Row)
    while(subRow==0){
        subRow=Random.nextInt(Row)
    }
    var minsizer:Int=5
    if(Row<5){
        minsizer=Row
    }
    var steps=Random.nextInt(minsizer)
    while(steps==0){
        steps=Random.nextInt(minsizer)
    }
    for(k<-0 to 31){
        inputInst(32+k):=0.U//startPoint
        subMatrix(96+k):=0.U
        subMatrix(k):=(steps.asUInt())(k)
        subMatrix(32+k):=(subRow.asUInt())(k)
        subMatrix(64+k):=(subRow.asUInt())(k)
        inputInst(64+k):=(Row.asUInt())(k)
        inputInst(96+k):=(Row.asUInt())(k)
    }
    inputInst(31):=0.U
    inputInst(30):=0.U
    inputInst(29):=0.U
    inputInst(28):=0.U
    inputInst(27):=0.U
    val typeLoad:Int = 86
    for(k<-0 to 23){
        inputInst(23-k+3):=(typeLoad.asUInt())(23-k)
    }
    inputInst(2):=1.U
    inputInst(1):=1.U
    inputInst(0):=0.U
    io.outputD:=inputInst
    io.outputValid:=subMatrix
    io.Row:=Row.asUInt
    io.subRow:=subRow.asUInt
    io.steps:=steps.asUInt
}

class GenLoadTest(dataWidth:Int,instructionWidth:Int) extends Module{
    val io=IO(new Bundle{
        val predicate_in0 = Flipped(Decoupled(UInt(1.W)))
        val predicate_in1 = Flipped(Decoupled(UInt(1.W)))
        val instruction_valid = Input(UInt(1.W))
        val data_out = Decoupled(UInt(dataWidth.W))
        val predicate_out = Decoupled(UInt())
        // val rerun = Output(UInt())
        val start=Input(UInt(1.W))
        // val noSelf=Output(UInt(1.W))
        val load_port = Decoupled(UInt(dataWidth.W))
        val load_data = Flipped(Decoupled(UInt(dataWidth.W)))
        val Row=Output(UInt(32.W))
        val subRow=Output(UInt(32.W))
        val steps=Output(UInt(32.W))
        val instructions=Output(UInt(32.W))
    })
    val priorityArbiter = Module(new GenInst(128))
    val p=Module(new matrixLoad.matrixLoadStore(32,64))
    val sInst=Wire(Vec(32,UInt(1.W)))
    p.io.predicate_in0<>io.predicate_in0
    p.io.predicate_in1<>io.predicate_in1
    p.io.instruction_valid<>io.instruction_valid
    io.predicate_out<>p.io.predicate_out
    io.data_out<>p.io.data_out
    p.io.start:=io.start
    io.load_port<>p.io.load_port
    p.io.load_data.bits:=io.load_data.bits
    p.io.load_data.valid:=io.load_data.valid
    io.load_data.ready:=p.io.load_data.ready
    p.io.instructions:=priorityArbiter.io.outputD.asUInt
    p.io.subMatrixSteps:=priorityArbiter.io.outputValid.asUInt
    io.Row:=priorityArbiter.io.Row
    io.subRow:=priorityArbiter.io.subRow
    io.steps:=priorityArbiter.io.steps
    for(i<-0 to 31){
        sInst(i):=priorityArbiter.io.outputD(i)
    }
    io.instructions:=sInst.asUInt
}

class matrixLoadTest extends FreeSpec with ChiselScalatestTester with Matchers{
    def binToInteger(binaryString: String): Int = {
        val integerString = binaryString.split("\\.")(0)
        var num = 0D
        integerString.reverse.toArray.zipWithIndex.foreach(info => {
        val factor = info._1
        if (factor.equals('1')) {
            val index = info._2.toDouble
            num += math.pow(2, index)
        }
        })
        num.toInt
    }
    "matrixLoadTest should " in {
            var lastNumber:Long=0
            var lastCal:Float=0.0F
            var capturedLoad:Long=0
            var expectedLoad:Long=0
            var capturedPred:Long=0
            var expectedPred:Long=0
            for (i <- 0 until 10) {  

                // val priorityArbiter = Module(new GenInst(128))
                test(new GenLoadTest(32,64)).withAnnotations(Seq(WriteVcdAnnotation)) { c =>  
                    
                    c.clock.setTimeout(1000000)
                    var Row=c.io.Row.peek().litValue.toInt
                    var subRow=c.io.subRow.peek().litValue.toInt
                    var steps=c.io.steps.peek().litValue.toInt
                    var inst:Int=c.io.instructions.peek().litValue.toInt
                    println(s"Iteration matrixLoadTest: $i, Row: $Row, subRow: $subRow, Step:$steps,Inst:$inst")  
                    
                    
                    c.io.predicate_in0.valid.poke(0.B)  
                    c.io.predicate_in1.valid.poke(0.B)
                    c.io.predicate_in0.bits.poke(0.U)   
                    c.io.predicate_in1.bits.poke(0.asUInt)
                    // c.io.subMatrixSteps.poke(priorityArbiter.io.outputValid.asUInt)
                    // c.io.instructions.poke(priorityArbiter.io.outputD.asUInt)

                    c.io.instruction_valid.poke(1.asUInt)
                    c.io.load_port.ready.poke(1.B)
                    c.io.load_data.valid.poke(1.B)
                    c.io.data_out.ready.poke(1.B)
                    c.io.predicate_out.ready.poke(1.B)
                    c.clock.step(3) 
                    c.io.instruction_valid.poke(1.B)
                    c.clock.step(2) 
                    c.io.start.poke(1.U)
                    var LC:Int=0
                    var LR:Int=0
                    var SC:Int=0
                    var SR:Int=0
                    var lV:Long=0
                    while(LC+subRow<=Row){
                        LR=0
                        while(LR+subRow<=Row){
                            SC=0
                            while(SC<subRow){
                                SR=0
                                while(SR<subRow){
                                    expectedLoad=(LC+SC)*Row+LR+SR
                                    // c.clock.step(2) 
                                    lV=0
                                    while(lV==0){
                                        c.clock.step(1) 
                                        lV=c.io.load_port.valid.peek().litValue.toLong
                                    }
                                    capturedLoad=c.io.load_port.bits.peek().litValue.toLong
                                    // println(s"Iteration matrixLoadTest: $i, capturedLoad: $capturedLoad, expectedLoad: $expectedLoad") 
                                    
                                    assert(expectedLoad==capturedLoad, s"Product is incorrect at iteration $i! Expected: $expectedLoad, Actual: $capturedLoad")
                                    c.io.load_port.ready.poke(1.B)
                                    
                                    // c.clock.step(1) 
                                    // c.io.load_port.ready.poke(0.B)
                                    c.io.load_data.valid.poke(1.B)
                                    c.io.predicate_out.ready.poke(1.B)
                                    c.io.data_out.ready.poke(1.B)
                                    c.io.load_data.bits.poke(1.U)
                                    c.io.load_data.valid.poke(1.B)
                                    // c.clock.step(2) 
                                    lV=0
                                    while(lV==0){
                                        c.clock.step(1) 
                                        lV=c.io.load_data.ready.peek().litValue.toLong
                                    }
                                    capturedPred=c.io.predicate_out.bits.peek().litValue.toLong
                                    if(SR==subRow-1){
                                        if(  SC==subRow-1)
                                            expectedPred=1
                                        else
                                            expectedPred=0
                                    }
                                    else{
                                        expectedPred=0
                                    }
                                    // println(s"Iteration matrixLoadTest: $i, capturedLoad: $capturedLoad, expectedLoad: $expectedLoad, expectedPred:$expectedPred,capturedPred:$capturedPred") 
                                    
                                    assert(expectedPred==capturedPred, s"Pred is incorrect at iteration $i! Expected: $expectedPred, Actual: $capturedPred")
                                    // c.io.load_data.valid.poke(0.B)
                                    // c.io.predicate_out.ready.poke(0.B)
                                    // c.io.data_out.ready.poke(0.B)
                                    SR=SR+1
                                }
                                SC=SC+1
                            }
                            LR=LR+steps
                        }
                        LC=LC+steps
                    }
                    
                }  
            }  
    }    
}