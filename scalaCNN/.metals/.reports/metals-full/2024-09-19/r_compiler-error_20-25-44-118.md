file:///D:/FPGA/scalaCNN/CNNAccelerator.scala
### java.lang.IndexOutOfBoundsException: 0

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 3.3.3
Classpath:
<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala3-library_3\3.3.3\scala3-library_3-3.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar [exists ]
Options:



action parameters:
offset: 1101
uri: file:///D:/FPGA/scalaCNN/CNNAccelerator.scala
text:
```scala
import chisel3._
import chisel3.util._


import ChiselLib._
import cnnPE._
import matrixLoad._

package CNNAccelerator{
    class CNNAccelerator(dataWidth:Int=32,instructionWidth:Int=64,vecNum:Int=16) extends Module{
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