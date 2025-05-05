file:///D:/FPGA/scalaCNN/CNNAccelerator.scala
### java.lang.IndexOutOfBoundsException: 0

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.3
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.3\scala3-library_3-3.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar [exists ]
Options:



action parameters:
offset: 3022
uri: file:///D:/FPGA/scalaCNN/CNNAccelerator.scala
text:
```scala
import chisel3._
import chisel3.util._


import ChiselLib._
import cnnPE._
import matrixLoad._

package CNNAccelerator{
    class CNNAccelerator(dataWidth:Int=32,instructionWidth:Int=64,vecNum:Int=16,n:Int,depth: Int) extends Module{
        val io=IO(new Bundle{
            val instructions=Input(Vec(vecNum,UInt((2*instructionWidth).W)))
            val subMatrixSteps=Input(Vec(vecNum,UInt((4*dataWidth).W)))
            val instruction_valid = Input(UInt(1.W))
            val storeBase=Input(Vec(vecNum,UInt((dataWidth).W)))
            val storeBase_valid = Input(UInt(1.W))
            val rerun = Output(UInt(1.W))
            val start=Input(UInt(1.W))
            val memWrite=Input(UInt(1.W))
            val memAddr=Input(UInt(dataWidth.W))
            val memWriteData=Input(UInt(dataWidth.W))
            val memReadData=Output(UInt(dataWidth.W))
        })
        val instructionsMem=Reg(Vec(vecNum,UInt((2*instructionWidth).W)))
        val subMatrixStepsMem=Reg(Vec(vecNum,UInt((4*dataWidth).W)))
        val storeBaseMem=Reg(Vec(vecNum,UInt((4*dataWidth).W)))
        for(i<- 0 to vecNum-1){
            instructionsMem(i):=Mux(io.instruction_valid.asBool,io.instructions(i),instructionsMem(i))
            subMatrixStepsMem(i):=Mux(io.instruction_valid.asBool,io.subMatrixSteps(i),subMatrixStepsMem(i))
            storeBaseMem(i):=Mux(io.storeBase_valid.asBool,io.storeBase(i),storeBaseMem(i))
        }
        val loadU=VecInit(Seq.fill(vecNum)(Module(new matrixLoad.matrixLoadStore(dataWidth, instructionWidth)).io))
        val storeU=VecInit(Seq.fill(vecNum)(Module(new matrixLoad.resultStore(dataWidth)).io))
        val memSysInst = Module(new memSys(n,vecNum,vecNum,vecNum,vecNum,vecNum,dataWidth, depth))//n is the fifo depth,depth is the SRAM depth
        // val rdAddress = Input(Vec(readPort,UInt(dataWidth.W)))
        // val rdValid = Input(Vec(readPort,UInt(1.W)))
        // val rdReady = Output(Vec(readPort,UInt(1.W)))
        // val wrAddress = Input(Vec(writePort,UInt(dataWidth.W)))
        // val wrValid = Input(Vec(writePort,UInt(1.W)))
        // val wrReady = Output(Vec(writePort,UInt(1.W)))
        // val data_in = Input(Vec(writePort,UInt(dataWidth.W)))
        // val data_out = Output(Vec(readPort,UInt(dataWidth.W)))
        // val outReady = Input(Vec(readPort,UInt(1.W)))
        // val outValid = Output(Vec(readPort,UInt(1.W))) 
        
        // val rdAddress1 = Input(UInt(dataWidth.W))
        // val wrAddress1 = Input(UInt(dataWidth.W))
        // val data_in1 = Input(UInt(dataWidth.W))
        // val wr_en1 = Input(UInt(1.W))
        // val data_out1 = Output(UInt(dataWidth.W))
        // val start=Input(UInt(1.W))
        memSysInst.io.rdAddress1:=io.memAddr
        memSysInst.io.wrAddress1:=io.memAddr
        memSysInst.io.data_in1:=io.memWriteData
        memSysInst.io.wr_en1:=io.memWrite
        memSysInst.io.start:=io.start
        io.memReadData:=memSysInst.io.data_out1
        for(@@)
    }
}
```



#### Error stacktrace:

```
scala.collection.LinearSeqOps.apply(LinearSeq.scala:131)
	scala.collection.LinearSeqOps.apply$(LinearSeq.scala:128)
	scala.collection.immutable.List.apply(List.scala:79)
	dotty.tools.dotc.util.Signatures$.countParams(Signatures.scala:501)
	dotty.tools.dotc.util.Signatures$.applyCallInfo(Signatures.scala:186)
	dotty.tools.dotc.util.Signatures$.computeSignatureHelp(Signatures.scala:94)
	dotty.tools.dotc.util.Signatures$.signatureHelp(Signatures.scala:63)
	scala.meta.internal.pc.MetalsSignatures$.signatures(MetalsSignatures.scala:17)
	scala.meta.internal.pc.SignatureHelpProvider$.signatureHelp(SignatureHelpProvider.scala:51)
	scala.meta.internal.pc.ScalaPresentationCompiler.signatureHelp$$anonfun$1(ScalaPresentationCompiler.scala:435)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: 0