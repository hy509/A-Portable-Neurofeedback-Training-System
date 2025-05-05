import chisel3._
import chisel3.util._
import ChiselLib._

package Arbit {
    class ArbiterPow2(userNum: Int, dataWidth: Int) extends Module {
        val io = IO(new Bundle {
            val in = Flipped(Vec(userNum, Decoupled(UInt(dataWidth.W))));
            val out = Decoupled(UInt(dataWidth.W));
            val chosen = Output(UInt((log2Ceil(userNum).W)));
        })
        val arb=Module(new ChiselLib.PriorMux(userNum))
        val inVal=Wire(Vec(userNum,UInt(1.W)))
        val inBits=Wire(Vec(userNum,UInt(dataWidth.W)))
        val outGrant=Wire(UInt(userNum.W))
        for(i<- 0 to userNum-1){
            inVal(i):=io.in(i).valid
            inBits(i):=io.in(i).bits
            io.in(i).ready:=io.out.ready & outGrant(i)
        }
        outGrant:=arb.io.selectOutVec
        arb.io.selectIn:=inVal.asUInt
        io.chosen:=arb.io.selectOut
        io.out.bits:=inBits(arb.io.selectOut)
        io.out.valid:=inVal.reduceTree(_ | _)
        // val arbiter = Module(new Arbiter(UInt(dataWidth.W), userNum));
        // arbiter.io.in <> io.in;
        // io.out <> arbiter.io.out;
        // io.chosen := arbiter.io.chosen;
    }

    class HEArbiter(userNum: Int, dataWidth: Int) extends Module {
        val io = IO(new Bundle {
            val in = Flipped(Vec(userNum, Decoupled(UInt(dataWidth.W))));
            val out = Decoupled(UInt(dataWidth.W));
            val chosen = Output(UInt((log2Ceil(userNum).W)));
        })
        val arb=Module(new ArbiterPow2(scala.math.pow(2,log2Ceil(userNum)).toInt,dataWidth))
        val choose=Wire(Vec(log2Ceil(userNum),UInt(1.W)))
        for(i<- 0 to userNum-1){
            arb.io.in(i).valid:=io.in(i).valid
            arb.io.in(i).bits:=io.in(i).bits
            io.in(i).ready:=arb.io.in(i).ready
        }
        for(i<- 0 to log2Ceil(userNum)-1){
            choose(i):=arb.io.chosen(i)
        }
        io.chosen:=choose.asUInt
        io.out.bits:=arb.io.out.bits
        io.out.valid:=arb.io.out.valid
        arb.io.out.ready:=io.out.ready
        if(scala.math.pow(2,log2Ceil(userNum)).toInt>userNum){
            for(i<- userNum to scala.math.pow(2,log2Ceil(userNum)).toInt-1){
                arb.io.in(i).valid:=0.U
                arb.io.in(i).bits:=0.U(dataWidth.W)
                // io.in(i).ready:=0.U
            }
        }
    }

    class ArbiterArbitrary(userNum: Int, dataWidth: Int) extends Module {
        val io = IO(new Bundle {
            val in = Flipped(Vec(userNum, Decoupled(UInt(dataWidth.W))));
            val out = Decoupled(UInt(dataWidth.W));
            val chosen = Output(UInt());
        })
        
        val arbiter = Module(new Arbiter(UInt(dataWidth.W), userNum));
        arbiter.io.in <> io.in;
        io.out <> arbiter.io.out;
        io.chosen := arbiter.io.chosen;
    }

    object myArbit extends App {
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new HEArbiter(4,1))));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new ArbiterArbitrary(4,32))));
    }
}