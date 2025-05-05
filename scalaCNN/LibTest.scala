

import chisel3._
import chisel3.tester._
//import chiseltest._
import org.scalatest._
import scala.util._
import scala.util.Random 
import scala.math._
import ChiselLib._



class BoothMultiplierBase4Test extends FreeSpec with ChiselScalatestTester with Matchers{
    "MUL should " in {
       
            for (i <- 0 until 10) {  
                val a = Random.nextInt(65536) - 65536/2 // 生成-128到127之间的随机数  
                val b = Random.nextInt(65536) - 65536/2  
        
                test(new ChiselLib.BoothMultiplierBase4(32)) { c =>  
                    c.io.a.poke(a.S) // 将随机数a作为有符号数输入  
                    c.io.b.poke(b.S) // 将随机数b作为有符号数输入  
                    c.clock.step(2) // 时钟前进一步以执行乘法  
            
                    val expectedProduct = a.toLong * b.toLong // 计算预期乘积  
                    val actualProduct = c.io.product.peek().litValue.toLong // 获取实际乘积  
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration Mul: $i, A: $a, B: $b, Expected Product: $expectedProduct, Actual Product: $actualProduct")  

                    assert(actualProduct === expectedProduct, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
                }  
            }  
    }  
        

}


    
class BasicFUTest extends FreeSpec with ChiselScalatestTester with Matchers{
    "BasicFUTest should " in {
       
            for (i <- 0 until 10) {  
                var a = Random.nextInt(65536) // 生成0到255之间的随机数  
                var b = Random.nextInt(65536)  
                var tmp: Int=0
                val ins=Random.nextInt(5)  
                val k=Random.nextInt(3)
                if(b>a & ins==1){
                    tmp=a
                    a=b
                    b=tmp
                }
                else if(ins==2&k==1){
                    b=a
                } 
                test(new ChiselLib.BasicFU(32,8)) { c =>  
                    c.io.ins.poke(ins.asUInt) //   
                    c.io.inA.poke(a.asUInt) //  
                    c.io.inB.poke(b.asUInt) //  
                    c.clock.step(2) // 
            
                    var expectedProduct: Float = a.toLong * b.toLong // 计算预期乘积  
                    if(ins==0){
                        expectedProduct=a.toLong + b.toLong
                    }
                    else if(ins==1){
                        expectedProduct=a.toLong - b.toLong
                    }
                    else if(ins==2){
                        if(a==b)
                            expectedProduct= 1.toLong
                        else
                            expectedProduct= 0.toLong
                    }
                    else if(ins==3){
                        if(a<b)
                            expectedProduct= 1.toLong
                        else
                            expectedProduct= 0.toLong
                    }
                    else if(ins==4){
                        if(a>b)
                            expectedProduct= 1.toLong
                        else
                            expectedProduct= 0.toLong
                    }
                    val actualProduct = c.io.outC.peek().litValue.toLong // 获取实际乘积 
                    // val actualR = c.io.R.peek().litValue.toLong // 获取实际乘积 
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration BFU: $i, A: $a, B: $b, ins: $ins, Expected Product: $expectedProduct, Actual Product: $actualProduct")  

                    assert(actualProduct === expectedProduct, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
                }  
            }  
    }  
        

}


class BasicFUSignTest extends FreeSpec with ChiselScalatestTester with Matchers{
    "BasicFUSignTest should " in {
       
            for (i <- 0 until 10) {  
                var a = Random.nextInt(65536) - 65536/2 // 生成-128到127之间的随机数  
                var b = Random.nextInt(65536) - 65536/2  
                val ins=Random.nextInt(5)  
                val k=Random.nextInt(3)
                // var aIn:UInt=a
                // var bIn:UInt=b
                // val TmpWireA=a.asSInt
                // val TmpWireB=b.asSInt
                // for(i<-0 to 31){
                //     TmpWireA(i):=a.asSInt()(i)
                //     TmpWireB(i):=b.asSInt()(i)
                // }
                if(ins==2&k==1){
                    b=a
                } 
                test(new ChiselLib.BasicFUSign(32,8)) { c =>  
                    c.io.inA.poke(a.asSInt) // 将随机数a作为有符号数输入  
                    c.io.inB.poke(b.asSInt) // 将随机数b作为有符号数输入  
                    c.io.ins.poke(ins.asUInt)
                    c.io.signedOrNot.poke(1.U)
                    c.clock.step(2) // 时钟前进一步以执行乘法  
            
                    var expectedProduct = a.toLong * b.toLong // 计算预期乘积  
                    if(ins==0){
                        expectedProduct=a.toLong + b.toLong
                    }
                    else if(ins==1){
                        expectedProduct=a.toLong - b.toLong
                    }
                    else if(ins==2){
                        if(a==b)
                            expectedProduct= 1.toLong
                        else
                            expectedProduct= 0.toLong
                    }
                    else if(ins==3){
                        if(a<b)
                            expectedProduct= 1.toLong
                        else
                            expectedProduct= 0.toLong
                    }
                    else if(ins==4){
                        if(a>b)
                            expectedProduct= 1.toLong
                        else
                            expectedProduct= 0.toLong
                    }
                    val actualProduct = c.io.outC.peek().litValue.toLong // 获取实际乘积  
                    /* 
                        c: 这是测试环境中BoothMultiplierBase4模块的实例。
                        c.io.product: 这是指向模块输出端口product的引用。
                        peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
                        litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
                    */
                    
                    println(s"Iteration Signed BFU: $i, A: $a, B: $b, ins: $ins, Expected Product: $expectedProduct, Actual Product: $actualProduct")  

                    assert(actualProduct === expectedProduct, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
                }  
            }  
    }  
}

      

//     class FastEXP extends FreeSpec with ChiselScalatestTester with Matchers{
//         def binToInteger(binaryString: String): Int = {
//             val integerString = binaryString.split("\\.")(0)
//             var num = 0D
//             integerString.reverse.toArray.zipWithIndex.foreach(info => {
//             val factor = info._1
//             if (factor.equals('1')) {
//                 val index = info._2.toDouble
//                 num += math.pow(2, index)
//             }
//             })
//             num.toInt
//         }
        
//     "FastEXP should " in {
       
//             for (i <- 0 until 10) {  
//                 var a = Random.nextFloat() // 生成-128到127之间的随机数  
//                 // var b = Random.nextFloat(65536) - 65536/2  
//                 val str=java.lang.Integer.toBinaryString(java.lang.Float.floatToRawIntBits(a))

//                 val uk=binToInteger(str)
//                 test(new ChiselLib.ExponentialUnit(32)) { c =>  
//                     c.io.a.poke(uk.asSInt()) // 将随机数a作为有符号数输入  
                    
//                     c.clock.step(6) // 时钟前进一步以执行乘法  
            
//                     var expectedProduct = scala.math.pow(2.718f,a).toFloat
                    
//                     val actualProduct = c.io.product.peek().litValue.toFloat // 获取实际乘积  
//                     /* 
//                         c: 这是测试环境中BoothMultiplierBase4模块的实例。
//                         c.io.product: 这是指向模块输出端口product的引用。
//                         peek(): 这是一个Chisel测试方法，用于在不推进时钟的情况下读取端口的当前值。
//                         litValue: 这是一个方法，用于从Chisel的Data类型中提取实际的Scala值（在这个例子中是BigInt） 
//                     */
                    
//                     println(s"Iteration EXP: $i, A: $a, uk: $uk, Expected Product: $expectedProduct, Actual Product: $actualProduct")  

//                     assert((actualProduct-expectedProduct)/actualProduct < 0.1, s"Product is incorrect at iteration $i! Expected: $expectedProduct, Actual: $actualProduct")  
            
                    
//                 }  
//             }  
//     }      

// }