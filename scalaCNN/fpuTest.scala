import chisel3._
import chisel3.tester._
//import chiseltest._
import org.scalatest._
import scala.util._
import scala.util.Random 
import scala.math._
import ChiselLib._
import fpu._

class FPUMULTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "FPUMULTest should " in {
       
            for (i <- 0 until 10) {  
                var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                var b = Random.nextFloat()*100 
                val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                val uk=binToInteger(str)
                val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                val uk2=binToInteger(str2)
                test(new fpu.pipeFPUMul32()) { c =>  
                    c.io.fpuA.poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.fpuB.poke(uk2.asUInt())
                    c.io.fpuValid.poke(1.U)
                    c.io.round_cfg.poke(1.U)
                    c.clock.step(6) // 时钟前进一步以执行乘法  
            
                    // var expectedProduct = (a.toFloat * b.toFloat).toFloat
                    var expectedProduct = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat).toFloat))).trim(),2)
                    val actualProduct = c.io.fpuC.peek().litValue.toLong // 获取实际乘积  
                    // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration FPMUL: $i, A: $a, B: $b, Expected Product: $expectedProduct, Actual Product: $actualProduct")  
                    var diff: Short=(actualProduct.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    assert((actualProduct==expectedProduct||actualProduct < expectedProduct+diff && actualProduct > expectedProduct-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
                }  
            }  
            var a = 6.25f // 生成-128到127之间的随机数  
            var b = 5.12f
            val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

            val uk=binToInteger(str)
            val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

            val uk2=binToInteger(str2)
            test(new fpu.pipeFPUMul32()) { c =>  
                c.io.fpuA.poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                c.io.fpuB.poke(uk2.asUInt())
                c.io.fpuValid.poke(1.U)
                c.io.round_cfg.poke(1.U)
                c.clock.step(6) // 时钟前进一步以执行乘法  
        
                // var expectedProduct = (a.toFloat * b.toFloat).toFloat
                var expectedProduct = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat * b.toFloat).toFloat))).trim(),2)
                val actualProduct = c.io.fpuC.peek().litValue.toLong // 获取实际乘积  
                // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积
                /* 
                    c: 这是测试环境中BoothMultiplierBase4模块的实例。
                    c.io.product: 这是指向模块输出端口product的引用。
                    peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                    litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                */
                
                println(s"Iteration FPMUL: 11, A: $a, B: $b, Expected Product: $expectedProduct, Actual Product: $actualProduct")  
                var diff: Short=(actualProduct.toShort*0.03).toShort
                if(diff<=6){
                    diff=6
                }
                // assert((actualProduct==expectedProduct||actualProduct < expectedProduct+diff && actualProduct > expectedProduct-diff), s"Product is incorrect at iteration 11! Expected: $expectedProduct, Actual: $actualProduct")  
            }
    }    
}


class FPUSubTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "FPUSubTest should " in {
       
            for (i <- 0 until 10) {  
                var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                var b = Random.nextFloat()*100 
                val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                val uk=binToInteger(str)
                val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                val uk2=binToInteger(str2)
                test(new fpu.combFPUSub32()) { c =>  
                    c.io.minA.poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.minB.poke(uk2.asUInt())
                    // c.io.round.poke(1.U)
                    // c.io.fpuValid.poke(1.U)
                    // c.io.round_cfg.poke(0.U)
                    c.clock.step(6) // 时钟前进一步以执行乘法  
                    // var finishV=c.io.fpuCValid.peek().litValue.toInt
                    // var expectedProduct = (a.toFloat / b.toFloat).toFloat
                    
                    var expectedProduct = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat - b.toFloat).toFloat))).trim(),2)
                    // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积  
                    val actualProduct = c.io.minC.peek().litValue.toLong // 获取实际乘积
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    var diff: Short=(actualProduct.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    println(s"Iteration FPSub: $i,  A: $a, B: $b, Expected Product: $expectedProduct, Actual Product: $actualProduct, diff: $diff")  
                    assert((actualProduct==expectedProduct||actualProduct < expectedProduct+diff && actualProduct > expectedProduct-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
                }  
            }  
            // val m=64.2973f
            // val n=63.08878f
            // val str3=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(m))

            // val uk3=binToInteger(str3)
            // val str4=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(n))

            // val uk4=binToInteger(str4)
            // println(s"////////////////////////////////////")
            // println(str3)
            // println(str4)
            // println(uk3)
            // println(uk4)
            // test(new fpu.combFPUSub32()) { c =>  
            //         c.io.minA.poke(uk3.asUInt()) // 将随机数a作为有符号数输入  
            //         c.io.minB.poke(uk4.asUInt())
            //         // c.io.fpuValid.poke(1.U)
            //         // c.io.round.poke(0.U)
            //         c.clock.step(6) // 时钟前进一步以执行乘法  
            //         // var finishV=c.io.fpuCValid.peek().litValue.toInt
            //         // var expectedProduct = (a.toFloat / b.toFloat).toFloat
                    
            //         var expectedProduct = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((m.toFloat - n.toFloat).toFloat))).trim(),2)
            //         // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积  
            //         val actualProduct = c.io.minC.peek().litValue.toLong // 获取实际乘积
            //         /* 
            //             c: 这是测试环境中BoothMultiplierBase4模块的实例。
            //             c.io.product: 这是指向模块输出端口product的引用。
            //             peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
            //             litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
            //         */
            //         var diff: Short=(actualProduct.toShort*0.03).toShort
            //         if(diff<=6){
            //             diff=6
            //         }
            //         println(s"Iteration extra FPSub:,  A: $m, B: $n, Expected Product: $expectedProduct, Actual Product: $actualProduct")  
            //         assert((actualProduct==expectedProduct||actualProduct < expectedProduct+diff && actualProduct > expectedProduct-diff), s"Product is incorrect at iteration extra! Expected: $expectedProduct, Actual: $actualProduct")  
            
            //         // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
            //     }  

    }    
}

class FPUDivTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "FPUDivTest should " in {
       
            for (i <- 0 until 10) {  
                var a = Random.nextFloat()*100 // 生成-128到127之间的随机数  
                var b = Random.nextFloat()*100 
                val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                val uk=binToInteger(str)
                val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                val uk2=binToInteger(str2)
                test(new fpu.longLatencyFPUDiv()) { c =>  
                    c.io.fpuA.poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    c.io.fpuB.poke(uk2.asUInt())
                    c.io.fpuValid.poke(1.U)
                    // c.io.round_cfg.poke(0.U)
                    c.clock.step(600) // 时钟前进一步以执行乘法  
                    var finishV=c.io.fpuCValid.peek().litValue.toInt
                    // var expectedProduct = (a.toFloat / b.toFloat).toFloat
                    
                    var expectedProduct = java.lang.Long.parseLong((java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits((a.toFloat / b.toFloat).toFloat))).trim(),2)
                    // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积  
                    val actualProduct = c.io.fpuC.peek().litValue.toLong // 获取实际乘积
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    var diff: Short=(actualProduct.toShort*0.03).toShort
                    if(diff<=6){
                        diff=6
                    }
                    println(s"Iteration FPDiv: $i, isFinish: $finishV, A: $a, B: $b, Expected Product: $expectedProduct, Actual Product: $actualProduct")  
                    assert((actualProduct==expectedProduct||actualProduct < expectedProduct+diff && actualProduct > expectedProduct-diff), s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
                }  
            }  
    }    
}

class FPUF2ITest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "FPUF2ITest should " in {
       
            for (i <- 0 until 10) {  
                var a = Random.nextFloat()*10 // 生成-128到127之间的随机数  
                // var b = Random.nextFloat()*100 
                val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

                val uk=binToInteger(str)
                // val str2=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(b))

                // val uk2=binToInteger(str2)
                test(new fpu.float2intComb()) { c =>  
                    c.io.fpuA.poke(uk.asUInt()) // 将随机数a作为有符号数输入  
                    // c.io.fpuB.poke(uk2.asUInt())
                    // c.io.fpuValid.poke(1.U)
                    // c.io.round_cfg.poke(0.U)
                    c.clock.step(6) // 时钟前进一步以执行乘法  
                    // var finishV=c.io.fpuCValid.peek().litValue.toInt
                    // var expectedProduct = (a.toFloat / b.toFloat).toFloat
                    
                    var expectedProduct = a.toLong
                    // val actualProduct = IEEE754ToDouble(c.io.fpuC.peek().litValue.toInt.toBinaryString,"F") // 获取实际乘积  
                    val actualProduct = c.io.fpuC.peek().litValue.toLong // 获取实际乘积
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration FPF2I: $i, A: $a, Expected Product: $expectedProduct, Actual Product: $actualProduct")  
                    assert((actualProduct==expectedProduct), s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
                }  
            }  
    }    
}

class FPUI2FTest extends FreeSpec with ChiselScalatestTester with Matchers{
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
    "FPUI2FTest should " in {
       
            for (i <- 0 until 10) {  
                var a = Random.nextInt(65536) // 生成-128到127之间的随机数  
                
                test(new fpu.int2floatComb()) { c =>  
                    c.io.fpuA.poke(a.asUInt()) // 将随机数a作为有符号数输入  
                    
                    c.clock.step(6) // 时钟前进一步以执行乘法  
                    var expectedProduct = binToInteger(java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a.toFloat)))
                      
                    val actualProduct = c.io.fpuC.peek().litValue.toInt // 获取实际乘积
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration FPI2F: $i, A: $a, Expected Product: $expectedProduct, Actual Product: $actualProduct")  
                    assert((actualProduct==expectedProduct), s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    // assert((actualProduct-expectedProduct)/actualProduct<0.05, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
                }  
            }  
    }    
}