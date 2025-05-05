import chisel3._
import chisel3.tester._
//import chiseltest._
import org.scalatest._
import scala.util._
import scala.util.Random 
import scala.math._
import ChiselLib._
import cnnPE._
class AccumulateMACTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "AccumulateMACTest should " in {
            var lastNumber:Long=0
            var lastCal:Float=0.0F
            for (i <- 0 until 10) {  
                var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                var b = Random.nextFloat()*100 
                val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                val uk=binToInteger(str)
                val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                val uk2=binToInteger(str2)
                val str3=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCal))

                val uk3=binToInteger(str3)
                test(new cnnPE.MACCNN()) { c =>  
                    c.io.inputA.poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.inputB.poke(uk2.asUInt())
                   
                    c.io.inputC.poke(uk3.asUInt())
                    
                    c.io.inputValid.poke(1.U)
                    // c.io.round_cfg.poke(1.U)
                    c.clock.step(3) // 时钟前进一步以执行乘法  
                    c.io.outputReady.poke(1.U)
                    // var expectedProduct = (a.toFloat * b.toFloat).toFloat
                    var expectedProduct = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat+lastCal).toFloat))).trim(),2)
                    
                    // lastNumber=
                    val expectMul=java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat).toFloat))).trim(),2)
                    val actualProduct = c.io.outputD.peek().litValue.toLong // 获取实际乘积  
                    val actualMul=c.io.AccumulateSelf.peek().litValue.toLong // 获取实际乘积  
                    // lastNumber=0
                    // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration AccumulateMACTest: $i, A: $a, B: $b, C:$lastCal, Expected Product: $expectedProduct, Actual Product: $actualProduct, Expected Mul: $expectMul, Actual Mul: $actualMul")  
                    lastCal=a.toFloat * b.toFloat+lastCal
                    var diff: Short=(actualProduct.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    assert((actualProduct==expectedProduct||actualProduct < expectedProduct+diff && actualProduct > expectedProduct-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
                }  
            }  
    }    
}

class MaxMACTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "MaxMACTest should " in {
            var lastNumber:Long=0
            var lastCal:Float=0.0F
            for (i <- 0 until 10) {  
                var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                var b = Random.nextFloat()*100 
                val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                val uk=binToInteger(str)
                val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                val uk2=binToInteger(str2)
                val str3=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCal))

                val uk3=binToInteger(str3)
                test(new cnnPE.FloatMax()) { c =>  
                    c.io.inputA.poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.inputB.poke(uk3.asUInt())
                   
                    // c.io.inputC.poke(uk3.asUInt())
                    
                    c.io.inputValid.poke(1.U)
                    // c.io.round_cfg.poke(1.U)
                    c.clock.step(1) // 时钟前进一步以执行乘法  
                    c.io.outputReady.poke(1.U)
                    // var expectedProduct = (a.toFloat * b.toFloat).toFloat
                    var maxD: Float=0
                    if(a.toFloat>lastCal){
                        maxD=a
                    }
                    else{
                        maxD=lastCal
                    }
                    var expectedProduct = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(maxD.toFloat))).trim(),2)
                    
                    // lastNumber=
                    // val expectMul=java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat).toFloat))).trim(),2)
                    val actualProduct = c.io.outputD.peek().litValue.toLong // 获取实际乘积  
                    // val actualMul=c.io.AccumulateSelf.peek().litValue.toLong // 获取实际乘积  
                    // lastNumber=0
                    // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration MaxMACTest: $i, A: $a, B: $b, C:$lastCal, Expected Product: $expectedProduct, Actual Product: $actualProduct")  
                    lastCal=maxD
                    var diff: Short=(actualProduct.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    assert((actualProduct==expectedProduct||actualProduct < expectedProduct+diff && actualProduct > expectedProduct-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
                }  
            }  
    }    
}

class VectorAccumulateTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "VectorAccumulateTest should " in {
            var lastNumber:Long=0
            var lastCal:Float=0.0F
            var lastCalx:Float=0.0F
            var zh:Int=0
            for (i <- 0 until 10) {  
                
                test(new cnnPE.VectorMAC(2,32)) { c =>  
                    for(j<-0 to 5){
                    // c.io.round_cfg.poke(1.U)
                        var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                        var b = Random.nextFloat()*100 
                        val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                        val uk=binToInteger(str)
                        val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                        val uk2=binToInteger(str2)
                        var a1 = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                        var b1 = Random.nextFloat()*100 
                        val strx=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1))

                        val ukx=binToInteger(strx)
                        val str2x=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b1))

                        val uk2x=binToInteger(str2x)
                        c.io.inputA(0).poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                        c.io.inputB(0).poke(uk2.asUInt())
                        c.io.inputC(0).poke(0.asUInt)
                        c.io.inputA(1).poke(ukx.asUInt()) // 将随机数a作为有符号数输入  
                        c.io.inputB(1).poke(uk2x.asUInt())
                        c.io.inputC(1).poke(0.asUInt)
                        // c.io.inputC.poke(uk3.asUInt())
                        
                        c.io.inputValid(0).poke(1.U)
                        c.io.inputValid(1).poke(1.U)
                        zh=0
                        // if(j==0)
                        //     assert(c.io.accumulate.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.accumulate.peek().litValue.toLong==0, s"Product is incorrect at iteration $j")
                        
                        while(c.io.inputReady(0).peek().litValue.toLong==0L||c.io.inputReady(1).peek().litValue.toLong==0L){
                            c.clock.step(2) // 时钟前进一步以执行乘法
                            zh=1  
                            // c.io.outputReady.poke(1.U)
                        }
                        
                        // if(j==0)
                        //     assert(c.io.cccIn.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.cccIn.peek().litValue.toLong==1)
                        // if(j==0)
                        //     assert(c.io.cccInR.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.cccInR.peek().litValue.toLong==1)
                        // if(j==0)
                        //     assert(c.io.cccInV.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.cccInV.peek().litValue.toLong==1)
                        // if(j==0)
                        //     assert(c.io.peekAcc.peek().litValue.toLong==0)
                        // else
                        //     assert(c.io.peekAcc.peek().litValue.toLong==0)
                        // if(j==0)
                        //     assert(c.io.accumulate1.peek().litValue.toLong==0)
                        // else
                        //     assert(c.io.accumulate1.peek().litValue.toLong==0)
                        
                        // {
                        //     var eP = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCal.toFloat))).trim(),2)
                        //     var data=c.io.puC.peek().litValue.toLong
                        //     println(s"Iteration VectorAccumulateTest: $i,$j, puC: $data, eP:$eP")
                        // }
                        lastCal=lastCal+a.toFloat*b.toFloat
                        lastCalx=lastCalx+a1.toFloat*b1.toFloat
                        
                        
                            while(c.io.outReady.peek.litValue==0){
                            
                                c.clock.step(2) // 时钟前进一步以执行乘法
                                c.io.inputValid(0).poke(0.U)
                                c.io.inputValid(1).poke(0.U)
                                zh=1  
                            }
                        
                        if(zh==0){
                            c.clock.step(2) // 时钟前进一步以执行乘法
                            c.io.inputValid(0).poke(0.U)
                            c.io.inputValid(1).poke(0.U)
                        }
                    }
                    var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                    var b = Random.nextFloat()*100 
                    val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                    val uk=binToInteger(str)
                    val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                    val uk2=binToInteger(str2)
                    lastCal=lastCal+a.toFloat*b.toFloat
                    c.io.inputA(0).poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.inputB(0).poke(uk2.asUInt())
                    c.io.inputC(0).poke(1.asUInt)
                    var a1 = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                    var b1 = Random.nextFloat()*100 
                    val strx=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1))

                    val ukx=binToInteger(strx)
                    val str2x=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b1))

                    val uk2x=binToInteger(str2x)
                    lastCalx=lastCalx+a1.toFloat*b1.toFloat
                    c.io.inputA(1).poke(ukx.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.inputB(1).poke(uk2x.asUInt())
                    c.io.inputC(1).poke(1.asUInt)
                    // c.io.inputC.poke(uk3.asUInt())
                    
                    c.io.inputValid(0).poke(1.U)
                    c.io.inputValid(1).poke(1.U)
                    while(c.io.inputReady(0).peek().litValue.toLong==0L||c.io.inputReady(1).peek().litValue.toLong==0L){
                        c.clock.step(2) // 时钟前进一步以执行乘法  
                        // c.io.outputReady.poke(1.U)
                    }
                    while(c.io.outputValid.peek().litValue.toLong==0L){
                        c.clock.step(2) // 时钟前进一步以执行乘法  
                            // c.io.outputReady.poke(1.U)
                    }
                    
                    // var expectedProduct = (a.toFloat * b.toFloat).toFloat
                    var expectedProduct0 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCal.toFloat))).trim(),2)
                    var expectedProduct1 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCalx.toFloat))).trim(),2)
                    
                    // lastNumber=
                    // val expectMul=java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat).toFloat))).trim(),2)
                    val actualProduct0 = c.io.outputD(0).peek().litValue.toLong // 获取实际乘积  
                    val actualProduct1 = c.io.outputD(1).peek().litValue.toLong // 获取实际乘积  
                    c.io.outputReady.poke(1.asUInt)
                    
                    println(s"Iteration VectorAccumulateTest: $i, A: $a, B: $b, Expected Product: $expectedProduct0, Actual Product: $actualProduct0, Expected Product: $expectedProduct1, Actual Product: $actualProduct1")  
                    lastCal=0
                    lastCalx=0
                    var diff: Short=(actualProduct0.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    assert((actualProduct0==expectedProduct0||actualProduct0 < expectedProduct0+diff && actualProduct0 > expectedProduct0-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct0, Actual: $actualProduct0")  
                    assert((actualProduct1==expectedProduct1||actualProduct1 < expectedProduct1+diff && actualProduct1 > expectedProduct1-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct1, Actual: $actualProduct1")  
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
                }  
            }  
    }    
}


class VectorMaxPoolTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "VectorMaxPoolTest should " in {
            var lastNumber:Long=0
            var lastCal:Float=0.0F
            var lastCalx:Float=0.0F
            var zh:Int=0
            for (i <- 0 until 10) {  
                
                test(new cnnPE.VectorCompare(2,32)) { c =>  
                    for(j<-0 to 5){
                    // c.io.round_cfg.poke(1.U)
                        var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                        var b = Random.nextFloat()*100 
                        val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                        val uk=binToInteger(str)
                        val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                        val uk2=binToInteger(str2)
                        var a1 = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                        var b1 = Random.nextFloat()*100 
                        val strx=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1))

                        val ukx=binToInteger(strx)
                        val str2x=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b1))

                        val uk2x=binToInteger(str2x)
                        c.io.inputA(0).poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                        // c.io.inputB(0).poke(uk2.asUInt())
                        c.io.inputC(0).poke(0.asUInt)
                        c.io.inputA(1).poke(ukx.asUInt()) // 将随机数a作为有符号数输入  
                        // c.io.inputB(1).poke(uk2x.asUInt())
                        c.io.inputC(1).poke(0.asUInt)
                        // c.io.inputC.poke(uk3.asUInt())
                        
                        c.io.inputValid(0).poke(1.U)
                        c.io.inputValid(1).poke(1.U)
                        zh=0
                        
                        while(c.io.inputReady(0).peek().litValue.toLong==0L||c.io.inputReady(1).peek().litValue.toLong==0L){
                            c.clock.step(2) // 时钟前进一步以执行乘法
                            zh=1  
                            // c.io.outputReady.poke(1.U)
                        }
                        
                        if(a.toFloat>lastCal){
                            lastCal=a.toFloat
                        }
                        if(a1.toFloat>lastCalx){
                            lastCalx=a1.toFloat
                        }
                        // lastCalx=lastCalx+a1.toFloat*b1.toFloat
                        
                        
                            while(c.io.outReady.peek.litValue==0){
                            
                                c.clock.step(2) // 时钟前进一步以执行乘法
                                c.io.inputValid(0).poke(0.U)
                                c.io.inputValid(1).poke(0.U)
                                zh=1  
                            }
                        
                        if(zh==0){
                            c.clock.step(2) // 时钟前进一步以执行乘法
                            c.io.inputValid(0).poke(0.U)
                            c.io.inputValid(1).poke(0.U)
                        }
                    }
                    var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                    var b = Random.nextFloat()*100 
                    val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                    val uk=binToInteger(str)
                    val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                    val uk2=binToInteger(str2)
                    // lastCal=lastCal+a.toFloat*b.toFloat
                    c.io.inputA(0).poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    // c.io.inputB(0).poke(uk2.asUInt())
                    c.io.inputC(0).poke(1.asUInt)
                    var a1 = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                    var b1 = Random.nextFloat()*100 
                    val strx=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1))

                    val ukx=binToInteger(strx)
                    val str2x=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b1))

                    val uk2x=binToInteger(str2x)
                    if(a.toFloat>lastCal){
                            lastCal=a.toFloat
                        }
                        if(a1.toFloat>lastCalx){
                            lastCalx=a1.toFloat
                        }
                    c.io.inputA(1).poke(ukx.asUInt()) // 将随机数a作为有符号数输入  
                    // c.io.inputB(1).poke(uk2x.asUInt())
                    c.io.inputC(1).poke(1.asUInt)
                    // c.io.inputC.poke(uk3.asUInt())
                    
                    c.io.inputValid(0).poke(1.U)
                    c.io.inputValid(1).poke(1.U)
                    while(c.io.inputReady(0).peek().litValue.toLong==0L||c.io.inputReady(1).peek().litValue.toLong==0L){
                        c.clock.step(2) // 时钟前进一步以执行乘法  
                        // c.io.outputReady.poke(1.U)
                    }
                    while(c.io.outputValid.peek().litValue.toLong==0L){
                        c.clock.step(2) // 时钟前进一步以执行乘法  
                            // c.io.outputReady.poke(1.U)
                    }
                    
                    // var expectedProduct = (a.toFloat * b.toFloat).toFloat
                    var expectedProduct0 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCal.toFloat))).trim(),2)
                    var expectedProduct1 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCalx.toFloat))).trim(),2)
                    
                    // lastNumber=
                    // val expectMul=java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat).toFloat))).trim(),2)
                    val actualProduct0 = c.io.outputD(0).peek().litValue.toLong // 获取实际乘积  
                    val actualProduct1 = c.io.outputD(1).peek().litValue.toLong // 获取实际乘积  
                    c.io.outputReady.poke(1.asUInt)
                    
                    println(s"Iteration VectorMaxPoolTest: $i, A: $a, B: $b, Expected Product: $expectedProduct0, Actual Product: $actualProduct0, Expected Product: $expectedProduct1, Actual Product: $actualProduct1")  
                    lastCal=0
                    lastCalx=0
                    var diff: Short=(actualProduct0.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    assert((actualProduct0==expectedProduct0||actualProduct0 < expectedProduct0+diff && actualProduct0 > expectedProduct0-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct0, Actual: $actualProduct0")  
                    assert((actualProduct1==expectedProduct1||actualProduct1 < expectedProduct1+diff && actualProduct1 > expectedProduct1-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct1, Actual: $actualProduct1")  
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
                }  
            }  
    }    
}

class VectorADDTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "VectorADDTest should " in {
            var lastNumber:Long=0
            var lastCal:Float=0.0F
            var lastCalx:Float=0.0F
            var zh:Int=0
            for (i <- 0 until 10) {  
                
                test(new cnnPE.VectorADD(2,32)) { c =>  
                    for(j<-0 to 5){
                    // c.io.round_cfg.poke(1.U)
                        var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                        var b = Random.nextFloat()*100 
                        val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                        val uk=binToInteger(str)
                        val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                        val uk2=binToInteger(str2)
                        var a1 = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                        var b1 = Random.nextFloat()*100 
                        val strx=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1))

                        val ukx=binToInteger(strx)
                        val str2x=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b1))

                        val uk2x=binToInteger(str2x)
                        c.io.inputA(0).poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                        c.io.inputB(0).poke(uk2.asUInt())
                        c.io.inputC(0).poke(1.asUInt)
                        c.io.inputA(1).poke(ukx.asUInt()) // 将随机数a作为有符号数输入  
                        c.io.inputB(1).poke(uk2x.asUInt())
                        c.io.inputC(1).poke(1.asUInt)
                        // c.io.inputC.poke(uk3.asUInt())
                        
                        c.io.inputValid(0).poke(1.U)
                        c.io.inputValid(1).poke(1.U)
                        zh=0
                        // if(j==0)
                        //     assert(c.io.accumulate.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.accumulate.peek().litValue.toLong==0, s"Product is incorrect at iteration $j")
                        
                        while(c.io.inputReady(0).peek().litValue.toLong==0L||c.io.inputReady(1).peek().litValue.toLong==0L){
                            c.clock.step(2) // 时钟前进一步以执行乘法
                            zh=1  
                            // c.io.outputReady.poke(1.U)
                        }
                        c.io.outputReady.poke(1.U)
                        while(c.io.outputValid.peek().litValue.toLong==0L){
                            
                            c.clock.step(2) // 时钟前进一步以执行乘法  
                            zh=1 
                            c.io.inputValid(0).poke(0.U)
                            c.io.inputValid(1).poke(0.U)
                            
                                // c.io.outputReady.poke(1.U)
                        }
                        c.clock.step(2)
                        c.io.outputReady.poke(0.U)
                        // if(j==0)
                        //     assert(c.io.cccIn.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.cccIn.peek().litValue.toLong==1)
                        // if(j==0)
                        //     assert(c.io.cccInR.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.cccInR.peek().litValue.toLong==1)
                        // if(j==0)
                        //     assert(c.io.cccInV.peek().litValue.toLong==1)
                        // else
                        //     assert(c.io.cccInV.peek().litValue.toLong==1)
                        // if(j==0)
                        //     assert(c.io.peekAcc.peek().litValue.toLong==0)
                        // else
                        //     assert(c.io.peekAcc.peek().litValue.toLong==0)
                        // if(j==0)
                        //     assert(c.io.accumulate1.peek().litValue.toLong==0)
                        // else
                        //     assert(c.io.accumulate1.peek().litValue.toLong==0)
                        
                        // {
                        //     var eP = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(lastCal.toFloat))).trim(),2)
                        //     var data=c.io.puC.peek().litValue.toLong
                        //     println(s"Iteration VectorAccumulateTest: $i,$j, puC: $data, eP:$eP")
                        // }
                        lastCal=lastCal+a.toFloat*b.toFloat
                        lastCalx=lastCalx+a1.toFloat*b1.toFloat
                        
                        
                            // while(c.io.outReady.peek.litValue==0){
                            
                            //     c.clock.step(2) // 时钟前进一步以执行乘法
                            //     c.io.inputValid(0).poke(0.U)
                            //     c.io.inputValid(1).poke(0.U)
                            //     zh=1  
                            // }
                        var expectedProduct0 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a.toFloat+b.toFloat))).trim(),2)
                        var expectedProduct1 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1.toFloat+b1.toFloat))).trim(),2)
                        
                        val actualProduct0 = c.io.outputD(0).peek().litValue.toLong // 获取实际乘积  
                        val actualProduct1 = c.io.outputD(1).peek().litValue.toLong // 获取实际乘积  

                        assert((actualProduct0==expectedProduct0||actualProduct0 < expectedProduct0+6 && actualProduct0 > expectedProduct0-6), s"Product is incorrect at iteration $i! Expected: $expectedProduct0, Actual: $actualProduct0")  
                        assert((actualProduct1==expectedProduct1||actualProduct1 < expectedProduct1+6 && actualProduct1 > expectedProduct1-6), s"Product is incorrect at iteration $i! Expected: $expectedProduct1, Actual: $actualProduct1")  
                    
                        if(zh==0){
                            c.clock.step(2) // 时钟前进一步以执行乘法
                            c.io.inputValid(0).poke(0.U)
                            c.io.inputValid(1).poke(0.U)
                        }
                    }
                    var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                    var b = Random.nextFloat()*100 
                    val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                    val uk=binToInteger(str)
                    val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                    val uk2=binToInteger(str2)
                    lastCal=lastCal+a.toFloat*b.toFloat
                    c.io.inputA(0).poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.inputB(0).poke(uk2.asUInt())
                    c.io.inputC(0).poke(1.asUInt)
                    var a1 = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                    var b1 = Random.nextFloat()*100 
                    val strx=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1))

                    val ukx=binToInteger(strx)
                    val str2x=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b1))

                    val uk2x=binToInteger(str2x)
                    lastCalx=lastCalx+a1.toFloat*b1.toFloat
                    c.io.inputA(1).poke(ukx.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.inputB(1).poke(uk2x.asUInt())
                    c.io.inputC(1).poke(1.asUInt)
                    // c.io.inputC.poke(uk3.asUInt())
                    
                    c.io.inputValid(0).poke(1.U)
                    c.io.inputValid(1).poke(1.U)
                    while(c.io.inputReady(0).peek().litValue.toLong==0L||c.io.inputReady(1).peek().litValue.toLong==0L){
                        c.clock.step(2) // 时钟前进一步以执行乘法  
                        // c.io.outputReady.poke(1.U)
                    }
                    while(c.io.outputValid.peek().litValue.toLong==0L){
                        c.clock.step(2) // 时钟前进一步以执行乘法  
                            // c.io.outputReady.poke(1.U)
                    }
                    
                    // var expectedProduct = (a.toFloat * b.toFloat).toFloat
                    var expectedProduct0 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a.toFloat+b.toFloat))).trim(),2)
                    var expectedProduct1 = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a1.toFloat+b1.toFloat))).trim(),2)
                        
                    // lastNumber=
                    // val expectMul=java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat).toFloat))).trim(),2)
                    val actualProduct0 = c.io.outputD(0).peek().litValue.toLong // 获取实际乘积  
                    val actualProduct1 = c.io.outputD(1).peek().litValue.toLong // 获取实际乘积  
                    c.io.outputReady.poke(1.asUInt)
                    
                    println(s"Iteration VectorAccumulateTest: $i, A: $a, B: $b, Expected Product: $expectedProduct0, Actual Product: $actualProduct0, Expected Product: $expectedProduct1, Actual Product: $actualProduct1")  
                    lastCal=0
                    lastCalx=0
                    var diff: Short=(actualProduct0.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    assert((actualProduct0==expectedProduct0||actualProduct0 < expectedProduct0+diff && actualProduct0 > expectedProduct0-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct0, Actual: $actualProduct0")  
                    assert((actualProduct1==expectedProduct1||actualProduct1 < expectedProduct1+diff && actualProduct1 > expectedProduct1-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct1, Actual: $actualProduct1")  
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
                }  
            }  
    }    
}
