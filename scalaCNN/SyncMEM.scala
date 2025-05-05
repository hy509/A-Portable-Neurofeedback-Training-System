import chisel3._
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import chisel3.experimental._
import chisel3.experimental.BundleLiterals._
import chisel3.tester._
import chisel3.tester.RawTester.test
import pepe._
import Arbit._
package SyncMEM {
    class distinctReadWrite(portNum: Int,readPort:Int, dataWidth: Int,bankNum: Int) extends Module{
        val io=IO(new Bundle{
            val Address = Input(Vec(portNum,UInt(dataWidth.W)))
            val Valid = Input(Vec(portNum,UInt(1.W)))
            val rdFIFOReady=Input(Vec(readPort,UInt(1.W)))
            // val AddressOut = Output(Vec(portNum,UInt(dataWidth.W)))
            val ValidOut = Output(Vec(portNum,UInt(1.W)))
        })
        val AddressBank= Wire(Vec(portNum,Vec(log2Ceil(bankNum),UInt((1).W))))
        val AddressChoose = Wire(Vec(portNum,Vec(portNum,UInt(1.W))))
        val ValidW = Wire(Vec(portNum,UInt(1.W)))
        for(i<- 0 to portNum-1){
            if(i>=portNum-readPort){
                ValidW(i):=io.Valid(i)&io.rdFIFOReady(i-(portNum-readPort))
            }
            else{
                ValidW(i):=io.Valid(i)
            }
            for(j<- 0 to log2Ceil(bankNum) - 1){
                AddressBank(i)(j):=io.Address(i)(j)
            }
            for(j <- 0 to portNum-1){
                AddressChoose(i)(j):=Mux(i.U=/=j.U,i.U>j.U&&AddressBank(i).asUInt===AddressBank(j).asUInt&&ValidW(i).asBool&&ValidW(j).asBool(),0.U)
                
            }
            when(AddressChoose(i).asUInt.orR.asBool){
                io.ValidOut(i) := 0.U
            }
            .otherwise{
                io.ValidOut(i) := ValidW(i)
            }
        }
    }

    class Umem(dataWidth: Int, depth: Int) extends Module{
        val io=IO(new Bundle{
            val rdAddress = Input(UInt(dataWidth.W))
            val wrAddress = Input(UInt(dataWidth.W))
            val data_in = Input(UInt(dataWidth.W))
            val wr_en = Input(UInt(1.W))
            val data_out = Output(UInt(dataWidth.W))}
        )
        val MemBank = Mem(depth, UInt(dataWidth.W));
        val Dreg=Reg(UInt(dataWidth.W))
        when(io.wr_en.asBool()) {
            MemBank.write(io.wrAddress, io.data_in);
        }
        val rdData = MemBank.read(io.rdAddress);
        Dreg := rdData;
        io.data_out:=Dreg
    }


    class memBank(dataWidth: Int, depth: Int) extends Module{
        val io=IO(new Bundle{
            val rdAddress = Input(UInt(dataWidth.W))
            val wrAddress = Input(UInt(dataWidth.W))
            val data_in = Input(UInt(dataWidth.W))
            val wr_en = Input(UInt(1.W))
            val data_out = Output(UInt(dataWidth.W))

            val rdAddress1 = Input(UInt(dataWidth.W))
            val wrAddress1 = Input(UInt(dataWidth.W))
            val data_in1 = Input(UInt(dataWidth.W))
            val wr_en1 = Input(UInt(1.W))
            val data_out1 = Output(UInt(dataWidth.W))
            val start=Input(UInt(1.W))
        })
        // val MemBank = Mem(depth, UInt(dataWidth.W));
        // val Dreg=Reg(UInt(dataWidth.W))
        // val Dreg1=RegInit(UInt(dataWidth.W),0.U)
        // loadMemoryFromFile(MemBank, "testBin.txt", MemoryLoadFileType.Binary);
        val rdAddr=Mux(io.start.asBool(),io.rdAddress,io.rdAddress1)
        val wrAddr=Mux(io.start.asBool(),io.wrAddress,io.wrAddress1)
        val Din=Mux(io.start.asBool(),io.data_in,io.data_in1)
        val WE=Mux(io.start.asBool(),io.wr_en,io.wr_en1)
        // val rdData = MemBank.read(rdAddr);

        val memSysInst = Module(new Umem(dataWidth, depth))//n is the fifo depth,depth is the SRAM depth
        memSysInst.io.data_in:=Din
        memSysInst.io.rdAddress:=rdAddr
        memSysInst.io.wrAddress:=wrAddr
        memSysInst.io.wr_en:=WE
        io.data_out:=memSysInst.io.data_out
        // val toWriteData = RegNext(Din);
        // val doForward = RegNext((WE) & ((rdAddr) === (wrAddr)));
        // val address = io.wrAddress|io.rdAddress
        // when(WE.asBool()) {
        //     MemBank.write(wrAddr, Din);
        // }
        // Dreg := rdData;
        // io.data_out:=Dreg;
        // val rdData1 = MemBank.read(io.rdAddress1);
        // val toWriteData1 = RegNext(io.data_in1);
        // val doForward1 = RegNext(io.wr_en1 & (io.rdAddress1 === io.wrAddress1));
        // // val address = io.wrAddress|io.rdAddress
        // when(io.wr_en.asBool()) {
        //     MemBank.write(io.wrAddress1, io.data_in1);
        // }
        // Dreg1:=Mux(doForward===1.U, toWriteData, rdData);
        io.data_out1 := io.data_out// Mux(doForward1===1.U, toWriteData1, rdData1);
    }

    class memSys(n:Int,readPort: Int,writePort: Int,bankNum: Int,bankRead: Int,bankWrite: Int,dataWidth: Int, depth: Int) extends Module{
        val io=IO(new Bundle{
            val rdAddress = Input(Vec(readPort,UInt(dataWidth.W)))
            val rdValid = Input(Vec(readPort,UInt(1.W)))
            val rdReady = Output(Vec(readPort,UInt(1.W)))
            val wrAddress = Input(Vec(writePort,UInt(dataWidth.W)))
            val wrValid = Input(Vec(writePort,UInt(1.W)))
            val wrReady = Output(Vec(writePort,UInt(1.W)))
            val data_in = Input(Vec(writePort,UInt(dataWidth.W)))
            // val wr_en = Input(Vec(writePort,UInt(1.W)))
            val data_out = Output(Vec(readPort,UInt(dataWidth.W)))
            val outReady = Input(Vec(readPort,UInt(1.W)))
            val outValid = Output(Vec(readPort,UInt(1.W))) 
            val rdFIFOReady=Output(Vec(readPort,UInt(1.W)))
            
            val rdAddress1 = Input(UInt(dataWidth.W))
            val wrAddress1 = Input(UInt(dataWidth.W))
            val data_in1 = Input(UInt(dataWidth.W))
            val wr_en1 = Input(UInt(1.W))
            val data_out1 = Output(UInt(dataWidth.W))
            val start=Input(UInt(1.W))
        })
        assert(bankNum>=bankRead&&bankNum>=bankWrite)
        val fifoRequestBank = VecInit(Seq.fill(readPort)(Module(new pepe.fifo_in(n, 1)).io))
        val fifoDataBank = VecInit(Seq.fill(readPort)(Module(new pepe.fifo_in(n, dataWidth)).io))
        val bankU = VecInit(Seq.fill(bankNum)(Module(new memBank(dataWidth, depth)).io))
        val arbitReadU = VecInit(Seq.fill(bankRead)(Module(new Arbit.HEArbiter(readPort, dataWidth)).io))
        val rdValidVec=Wire(Vec(bankRead,Vec(readPort,UInt(1.W))))
        val rdReadyVec=Wire(Vec(bankRead,Vec(readPort,UInt(1.W))))
        val rdReadyVecTransport=Wire(Vec(readPort,Vec(bankRead,UInt(1.W))))
        val rdReadyVecTransportReduct=Wire(Vec(readPort,UInt(1.W)))
        val chosenVec=Wire(Vec(bankRead,UInt()))
        val chosenVecReg=Reg(Vec(bankRead,UInt()))
        val rdReadyVecReg=Reg(Vec(bankRead,Vec(readPort,UInt(1.W))))
        chosenVecReg:=chosenVec
        rdReadyVecReg:=rdReadyVec
        val outrdAddress=Wire(Vec(bankRead,UInt(dataWidth.W)))
        io.rdReady:=rdReadyVecTransportReduct
        val dataOutReady=Reg(Vec(readPort,UInt(1.W)))
        val rdPortDataLastUInt=Wire(Vec(readPort,Vec(dataWidth,UInt(1.W))))
        for(i <- 0 to readPort - 1){
            io.rdFIFOReady(i):=fifoRequestBank(i).enq.ready
            rdValidVec(0)(i):=io.rdValid(i)&fifoRequestBank(i).enq.ready
            fifoRequestBank(i).enq.valid:=rdReadyVecTransportReduct(i)&io.rdValid(i)
            fifoRequestBank(i).enq.bits:=1.U
            fifoRequestBank(i).deq.ready:=fifoDataBank(i).deq.valid&fifoDataBank(i).deq.ready
            fifoDataBank(i).enq.valid:=dataOutReady(i)
            fifoDataBank(i).enq.bits:=rdPortDataLastUInt(i).asUInt
            fifoDataBank(i).deq.ready:=io.outReady(i)&fifoDataBank(i).deq.valid
            io.outValid(i):=fifoDataBank(i).deq.valid&fifoRequestBank(i).deq.valid
            io.data_out(i):=fifoDataBank(i).deq.bits
            dataOutReady(i):=rdReadyVecTransportReduct(i)&io.rdValid(i)
        }
        
        
        val rdBank=Wire(Vec(bankRead,Vec(log2Ceil(bankNum),UInt(1.W))))
        val rdBankReg=Reg(Vec(bankRead,Vec(log2Ceil(bankNum),UInt(1.W))))
        rdBankReg:=rdBank
        val rdResidue=Wire(Vec(bankRead,Vec(dataWidth-log2Ceil(bankNum),UInt(1.W))))
        val rdPortData=Wire(Vec(readPort,Vec(bankRead,UInt(dataWidth.W))))
        val readData=Wire(Vec(bankRead,Vec(bankNum,UInt((dataWidth).W))))
        val readDataMatrix=Wire(Vec(bankRead,Vec(dataWidth,Vec(bankNum,UInt(1.W)))))
        val rdM=Wire(Vec(bankRead,Vec(dataWidth,UInt(1.W))))
        val rdPortDataLast=Wire(Vec(readPort,Vec(dataWidth,Vec(bankRead,UInt(1.W)))))
        

        val data_outReg = Reg(Vec(readPort,UInt(dataWidth.W)))
        val outRegValid=Wire(Vec(readPort,UInt(1.W)))
        // val outRegReady=Wire(Vec(readPort,UInt(1.W)))
        val outRegValidMatrix=Wire(Vec(bankRead,Vec(readPort,UInt(1.W))))
        val outRegReadyMatrix=Wire(Vec(bankRead,Vec(readPort,UInt(1.W))))

        val wrRegValidMatrix=Wire(Vec(readPort,Vec(bankRead,UInt(1.W))))
        // val wrRegReadyMatrix=Wire(Vec(readPort,Vec(bankRead,UInt(1.W))))
        val outState=RegInit(0.U(readPort.W)) 
        val nextState=Wire(Vec(readPort,UInt(1.W)))
        outState:=nextState.asUInt()
        
        
        
        for(i <- 0 to bankRead - 1){
            for(m<-0 to readPort-1){
                arbitReadU(i).in(m).valid:=rdValidVec(i)(m)
                arbitReadU(i).in(m).bits:=io.rdAddress(m)
                rdReadyVec(i)(m):=arbitReadU(i).in(m).ready&arbitReadU(i).in(m).valid
            }
            
            outrdAddress(i):=arbitReadU(i).out.bits
            arbitReadU(i).out.ready:=1.U// outRegValidMatrix(i).asUInt.orR&outRegReadyMatrix(i).asUInt.orR&outState(i)||outState(i)===0.U
            
            chosenVec(i):=arbitReadU(i).chosen
            for(m <- 0 to log2Ceil(bankNum) - 1){
                rdBank(i)(m):=outrdAddress(i)(m)
            }
            for(n <- 0 to dataWidth-log2Ceil(bankNum)-1){
                rdResidue(i)(n):=outrdAddress(i)(n+log2Ceil(bankNum))
            }
            if(i>0){
                for(j <- 0 to readPort-1){
                    rdValidVec(i)(j):=rdValidVec(i-1)(j)^rdReadyVec(i-1)(j)
                }
            }
            for(k <- 0 to readPort-1){
                rdReadyVecTransport(k)(i):=(rdReadyVec(i)(k))//&(outRegValidMatrix(i).asUInt.orR&outRegReadyMatrix(i).asUInt.orR&outState(i)||outState(i)===0.U)
                outRegValidMatrix(i)(k):=Mux(chosenVec(i)===k.U&&rdReadyVec(i).asUInt.orR,outRegValid(k),0.U)
                outRegReadyMatrix(i)(k):=Mux(chosenVec(i)===k.U&&rdReadyVec(i).asUInt.orR,io.outReady(k),0.U)
                wrRegValidMatrix(k)(i):=Mux(chosenVec(i)===k.U&&rdReadyVec(i).asUInt.orR,rdReadyVec(i).asUInt.orR,0.U)
            }
            for(l<-0 to dataWidth-1){
                rdM(i)(l):=readDataMatrix(i)(l).asUInt.orR
            }
        }
        
        for(i <- 0 to readPort-1){
            rdReadyVecTransportReduct(i):=rdReadyVecTransport(i).asUInt.orR
            for(j <- 0 to bankRead-1){
                rdPortData(i)(j):=Mux(chosenVecReg(j)===i.U&&rdReadyVecReg(j).asUInt.orR,rdM(j).asUInt,0.U)
            }
            for(j<-0 to dataWidth-1){
                for(k<-0 to bankRead-1){
                    rdPortDataLast(i)(j)(k):=rdPortData(i)(k)(j)
                }
                rdPortDataLastUInt(i)(j):=rdPortDataLast(i)(j).asUInt.orR
            }
            nextState(i):=Mux(outState(i)===0.U,wrRegValidMatrix(i).asUInt.orR(),Mux(wrRegValidMatrix(i).asUInt.orR(),1.U,~(outRegValid(i)&io.outReady(i))))
            outRegValid(i):=outState(i)
            data_outReg(i):=Mux(wrRegValidMatrix(i).asUInt.orR(),rdPortDataLastUInt(i).asUInt,data_outReg(i))
        }
        // val readValid=Wire(Vec(bankRead,UInt(1.W)))
        // for(j <- 0 to bankRead - 1){
        //     readValid(j):=rdReadyVec(j).asUInt.orR
        // }
        val readAddressBank=Wire(Vec(bankNum,Vec(bankRead,UInt((dataWidth-log2Ceil(bankNum)).W))))
        val readAddMatrix=Wire(Vec(bankNum,Vec(dataWidth-log2Ceil(bankNum),Vec(bankRead,UInt(1.W)))))
        val bankrdAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        
        for(i <- 0 to bankNum-1){
            bankU(i).start:=io.start
            bankU(i).rdAddress:=bankrdAdd(i).asUInt
            for(j <- 0 to bankRead - 1){
                readAddressBank(i)(j):=Mux(rdBank(j).asUInt===i.U&&rdReadyVec(j).asUInt>0.U,rdResidue(j).asUInt,0.U)
                readData(j)(i):=Mux(rdBankReg(j).asUInt===i.U&&rdReadyVecReg(j).asUInt>0.U,bankU(i).data_out,0.U)
                for(z <- 0 to dataWidth-1){
                    readDataMatrix(j)(z)(i):=readData(j)(i)(z)
                }
                
            }
            for(m <- 0 to dataWidth-log2Ceil(bankNum)-1){
                for(n <- 0 to bankRead - 1){
                    readAddMatrix(i)(m)(n):=readAddressBank(i)(n)(m)
                }
            }
            for(k <- 0 to dataWidth-log2Ceil(bankNum)-1){
                bankrdAdd(i)(k):=readAddMatrix(i)(k).asUInt.orR
            }
            for(l <- 0 to log2Ceil(bankNum)-1){
                bankrdAdd(i)(l+dataWidth-log2Ceil(bankNum)):=0.U
            }
        }
        
        val arbitWriteU = VecInit(Seq.fill(bankWrite)(Module(new Arbit.HEArbiter(writePort, dataWidth)).io))
        val arbitWriteDataU = VecInit(Seq.fill(bankWrite)(Module(new Arbit.HEArbiter(writePort, dataWidth)).io))
        val wrValidVec=Wire(Vec(bankWrite,Vec(writePort,UInt(1.W))))
        val wrReadyVec=Wire(Vec(bankWrite,Vec(writePort,UInt(1.W))))
        val wrReadyVecTransport=Wire(Vec(writePort,Vec(bankWrite,UInt(1.W))))
        val wrReadyVecTransportReduct=Wire(Vec(writePort,UInt(1.W)))
        val chosenWrVec=Wire(Vec(bankWrite,UInt()))
        val outwrAddress=Wire(Vec(bankWrite,UInt(dataWidth.W)))
        io.wrReady:=wrReadyVecTransportReduct
        wrValidVec(0):=io.wrValid

        val wrBank=Wire(Vec(bankWrite,Vec(log2Ceil(bankNum),UInt(1.W))))
        val wrResidue=Wire(Vec(bankWrite,Vec(dataWidth-log2Ceil(bankNum),UInt(1.W))))
        val wrData=Wire(Vec(bankWrite,UInt(dataWidth.W)))

        for(i <- 0 to bankWrite - 1){
            for(m<- 0 to writePort-1){
                arbitWriteU(i).in(m).valid:=wrValidVec(i)(m)
                arbitWriteU(i).in(m).bits:=io.wrAddress(m)
                arbitWriteDataU(i).in(m).valid:=wrValidVec(i)(m)
                arbitWriteDataU(i).in(m).bits:=io.data_in(m)
                wrReadyVec(i)(m):=arbitWriteU(i).in(m).ready&arbitWriteU(i).in(m).valid
            }
            for(k <- 0 to writePort-1){
                wrReadyVecTransport(k)(i):=wrReadyVec(i)(k)
            }
            outwrAddress(i):=arbitWriteU(i).out.bits
            wrData(i):=arbitWriteDataU(i).out.bits
            arbitWriteDataU(i).out.ready:=1.U
            arbitWriteU(i).out.ready:=1.U
            chosenWrVec(i):=arbitWriteU(i).chosen
            for(m <- 0 to log2Ceil(bankNum) - 1){
                wrBank(i)(m):=outwrAddress(i)(m)
            }
            for(n <- 0 to dataWidth-log2Ceil(bankNum)-1){
                wrResidue(i)(n):=outwrAddress(i)(n+log2Ceil(bankNum))
            }
            if(i>0){
                for(j <- 0 to writePort-1){
                    wrValidVec(i)(j):=wrValidVec(i-1)(j)^wrReadyVec(i-1)(j)
                }
            }
            
        }

        val writeAddressBank=Wire(Vec(bankNum,Vec(bankWrite,UInt((dataWidth-log2Ceil(bankNum)).W))))
        val writeAddMatrix=Wire(Vec(bankNum,Vec(dataWidth-log2Ceil(bankNum),Vec(bankWrite,UInt(1.W)))))
        val bankwrAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val writeDataBank=Wire(Vec(bankNum,Vec(bankWrite,UInt((dataWidth).W))))
        val writeDataMatrix=Wire(Vec(bankNum,Vec(dataWidth,Vec(bankWrite,UInt(1.W)))))
        val bankwrData=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val wr_en=Wire(Vec(bankNum,Vec(bankWrite,UInt(1.W))))
        for(i <- 0 to writePort-1){
            wrReadyVecTransportReduct(i):=wrReadyVecTransport(i).asUInt.orR
        }
        for(i <- 0 to bankNum-1){
            bankU(i).wrAddress:=bankwrAdd(i).asUInt
            bankU(i).data_in:=bankwrData(i).asUInt
            bankU(i).wr_en:=wr_en(i).asUInt.orR
            for(j <- 0 to bankWrite - 1){
                writeAddressBank(i)(j):=Mux(wrBank(j).asUInt===i.U&&wrReadyVec(j).asUInt>0.U,wrResidue(j).asUInt,0.U)
                writeDataBank(i)(j):=Mux(wrBank(j).asUInt===i.U&&wrReadyVec(j).asUInt>0.U,wrData(j).asUInt,0.U)
                wr_en(i)(j):=Mux(wrBank(j).asUInt===i.U&&wrReadyVec(j).asUInt>0.U,1.U,0.U)
            }
            for(m <- 0 to dataWidth-log2Ceil(bankNum)-1){
                for(n <- 0 to bankWrite - 1){
                    writeAddMatrix(i)(m)(n):=writeAddressBank(i)(n)(m)
                    // writeDataMatrix(i)(m)(n):=writeDataBank(i)(n)(m)
                }
            }
            for(m <- 0 to dataWidth-1){
                for(n <- 0 to bankWrite - 1){
                    // writeAddMatrix(i)(m)(n):=writeAddressBank(i)(n)(m)
                    writeDataMatrix(i)(m)(n):=writeDataBank(i)(n)(m)
                }
            }
            for(k <- 0 to dataWidth-log2Ceil(bankNum)-1){
                bankwrAdd(i)(k):=writeAddMatrix(i)(k).asUInt.orR
            }
            for(l <- 0 to log2Ceil(bankNum)-1){
                bankwrAdd(i)(l+dataWidth-log2Ceil(bankNum)):=0.U
            }
            for(k <- 0 to dataWidth-1){
                bankwrData(i)(k):=writeDataMatrix(i)(k).asUInt.orR
            }
        }

        val rd1Bank=Wire(Vec(log2Ceil(bankNum),UInt(1.W)))
        val rd1BankReg=Reg(Vec(log2Ceil(bankNum),UInt(1.W)))
        rd1BankReg:=rd1Bank
        val wr1Bank=Wire(Vec(log2Ceil(bankNum),UInt(1.W)))
        val rd1Address=Wire(Vec(dataWidth-log2Ceil(bankNum),UInt(1.W)))
        val wr1Address=Wire(Vec(dataWidth-log2Ceil(bankNum),UInt(1.W)))
        val rd1BankAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val wr1BankAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val wr1En=Wire(Vec(bankNum,UInt(1.W)))
        val wr1Din=Wire(Vec(bankNum,UInt(dataWidth.W)))
        val rd1Dout=Wire(Vec(bankNum,UInt(dataWidth.W)))
        for(i<-0 to log2Ceil(bankNum)-1){
            rd1Bank(i):=io.rdAddress1(i)
            wr1Bank(i):=io.wrAddress1(i)
        }
        for(i<-0 to dataWidth-log2Ceil(bankNum)-1){
            rd1Address(i):=io.rdAddress1(i+log2Ceil(bankNum))
            wr1Address(i):=io.wrAddress1(i+log2Ceil(bankNum))
        }
        for(i<- 0 to bankNum-1){
            wr1En(i):=Mux(wr1Bank.asUInt()===i.U,io.wr_en1,0.U)
            wr1Din(i):=Mux(wr1Bank.asUInt()===i.U,io.data_in1,0.U)
            for(k <- 0 to dataWidth-log2Ceil(bankNum)-1){
                rd1BankAdd(i)(k):=Mux(rd1Bank.asUInt()===i.U,rd1Address(k),0.U)
                wr1BankAdd(i)(k):=Mux(wr1Bank.asUInt()===i.U,wr1Address(k),0.U)
            }
            for(k <- 0 to log2Ceil(bankNum)-1){
                rd1BankAdd(i)(k+dataWidth-log2Ceil(bankNum)):=0.U
                wr1BankAdd(i)(k+dataWidth-log2Ceil(bankNum)):=0.U
            }
            bankU(i).data_in1:=wr1Din(i)
            bankU(i).wr_en1:=wr1En(i)
            bankU(i).rdAddress1:=rd1BankAdd(i).asUInt()
            bankU(i).wrAddress1:=wr1BankAdd(i).asUInt()
            rd1Dout(i):=bankU(i).data_out1
        }
        io.data_out1:=rd1Dout(rd1BankReg.asUInt())
    }

    class memSysDirect(n:Int,readPort: Int,writePort: Int,bankNum: Int,bankRead: Int,bankWrite: Int,dataWidth: Int, depth: Int) extends Module{
        val io=IO(new Bundle{
            val rdAddress = Input(Vec(readPort,UInt(dataWidth.W)))
            val rdValid = Input(Vec(readPort,UInt(1.W)))
            val rdReady = Output(Vec(readPort,UInt(1.W)))
            val wrAddress = Input(Vec(writePort,UInt(dataWidth.W)))
            val wrValid = Input(Vec(writePort,UInt(1.W)))
            val wrReady = Output(Vec(writePort,UInt(1.W)))
            val data_in = Input(Vec(writePort,UInt(dataWidth.W)))
            // val wr_en = Input(Vec(writePort,UInt(1.W)))
            val data_out = Output(Vec(readPort,UInt(dataWidth.W)))
            val outReady = Input(Vec(readPort,UInt(1.W)))
            val outValid = Output(Vec(readPort,UInt(1.W))) 
            
            val rdAddress1 = Input(UInt(dataWidth.W))
            val wrAddress1 = Input(UInt(dataWidth.W))
            val data_in1 = Input(UInt(dataWidth.W))
            val wr_en1 = Input(UInt(1.W))
            val data_out1 = Output(UInt(dataWidth.W))
            val start=Input(UInt(1.W))
        })
        assert(bankNum>=bankRead&&bankNum>=bankWrite)
        val fifoRequestBank = VecInit(Seq.fill(readPort)(Module(new pepe.fifo_in(n, 1)).io))
        val fifoDataBank = VecInit(Seq.fill(readPort)(Module(new pepe.fifo_in(n, dataWidth)).io))
        val bankU = VecInit(Seq.fill(bankNum)(Module(new memBank(dataWidth, depth)).io))
        // val rdAddress = Input(UInt(dataWidth.W))
        //     val wrAddress = Input(UInt(dataWidth.W))
        //     val data_in = Input(UInt(dataWidth.W))
        //     val wr_en = Input(UInt(1.W))
        //     val data_out = Output(UInt(dataWidth.W))
        // val rd0Address=Wire(Vec(dataWidth-log2Ceil(bankNum),UInt(1.W)))
        // val wr0Address=Wire(Vec(dataWidth-log2Ceil(bankNum),UInt(1.W)))
        val wrEn=Wire(Vec(bankNum,UInt(1.W)))
        val wrDin=Wire(Vec(bankNum,UInt(dataWidth.W)))
        val rdDout=Wire(Vec(bankNum,UInt(dataWidth.W)))
        val rdBankAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val wrBankAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val outVReg=Reg(Vec(bankNum,UInt(1.W)))
        for(i <- 0 to readPort - 1){
            // rdValidVec(0)(i):=io.rdValid(i)&fifoRequestBank(i).enq.ready
            fifoRequestBank(i).enq.valid:=io.rdValid(i)
            io.rdReady(i):=fifoRequestBank(i).enq.ready
            fifoRequestBank(i).enq.bits:=1.U
            fifoRequestBank(i).deq.ready:=fifoDataBank(i).deq.valid&fifoDataBank(i).deq.ready
            fifoDataBank(i).enq.valid:=outVReg(i)
            fifoDataBank(i).enq.bits:=rdDout(i)
            fifoDataBank(i).deq.ready:=io.outReady(i)&fifoDataBank(i).deq.valid
            io.outValid(i):=fifoDataBank(i).deq.valid&fifoRequestBank(i).deq.valid
            io.data_out(i):=fifoDataBank(i).deq.bits
            // dataOutReady(i):=rdReadyVecTransportReduct(i)&io.rdValid(i)
        }
        // for(i<-0 to dataWidth-log2Ceil(bankNum)-1){
        //     rd0Address(i):=io.rdAddress(i+log2Ceil(bankNum))
        //     wr0Address(i):=io.wrAddress(i+log2Ceil(bankNum))
        // }
        for(i<- 0 to bankNum-1){
            // io.outValid(i):=outVReg(i)
            outVReg(i):=io.rdValid(i)
            bankU(i).start:=io.start
            io.wrReady(i):=io.wrValid(i)
            wrEn(i):=io.wrValid(i)
            wrDin(i):=io.data_in(i)
            for(k <- 0 to dataWidth-log2Ceil(bankNum)-1){
                rdBankAdd(i)(k):=io.rdAddress(i)(k+log2Ceil(bankNum))
                wrBankAdd(i)(k):=io.wrAddress(i)(k+log2Ceil(bankNum))
                // rd0Address(i)(k):=io.rdAddress(i)(k+log2Ceil(bankNum))
                // wr0Address(i)(k):=io.wrAddress(i)(k+log2Ceil(bankNum))
            }
            for(k <- 0 to log2Ceil(bankNum)-1){
                rdBankAdd(i)(k+dataWidth-log2Ceil(bankNum)):=0.U
                wrBankAdd(i)(k+dataWidth-log2Ceil(bankNum)):=0.U
            }
            bankU(i).data_in:=wrDin(i)
            bankU(i).wr_en:=wrEn(i)
            bankU(i).rdAddress:=rdBankAdd(i).asUInt()
            bankU(i).wrAddress:=wrBankAdd(i).asUInt()
            rdDout(i):=bankU(i).data_out
        }
        val rd1Bank=Wire(Vec(log2Ceil(bankNum),UInt(1.W)))
        val rd1BankReg=Reg(Vec(log2Ceil(bankNum),UInt(1.W)))
        rd1BankReg:=rd1Bank
        val wr1Bank=Wire(Vec(log2Ceil(bankNum),UInt(1.W)))
        val rd1Address=Wire(Vec(dataWidth-log2Ceil(bankNum),UInt(1.W)))
        val wr1Address=Wire(Vec(dataWidth-log2Ceil(bankNum),UInt(1.W)))
        val rd1BankAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val wr1BankAdd=Wire(Vec(bankNum,Vec(dataWidth,UInt(1.W))))
        val wr1En=Wire(Vec(bankNum,UInt(1.W)))
        val wr1Din=Wire(Vec(bankNum,UInt(dataWidth.W)))
        val rd1Dout=Wire(Vec(bankNum,UInt(dataWidth.W)))
        for(i<-0 to log2Ceil(bankNum)-1){
            rd1Bank(i):=io.rdAddress1(i)
            wr1Bank(i):=io.wrAddress1(i)
        }
        for(i<-0 to dataWidth-log2Ceil(bankNum)-1){
            rd1Address(i):=io.rdAddress1(i+log2Ceil(bankNum))
            wr1Address(i):=io.wrAddress1(i+log2Ceil(bankNum))
        }
        for(i<- 0 to bankNum-1){
            wr1En(i):=Mux(wr1Bank.asUInt()===i.U,io.wr_en1,0.U)
            wr1Din(i):=Mux(wr1Bank.asUInt()===i.U,io.data_in1,0.U)
            for(k <- 0 to dataWidth-log2Ceil(bankNum)-1){
                rd1BankAdd(i)(k):=Mux(rd1Bank.asUInt()===i.U,rd1Address(k),0.U)
                wr1BankAdd(i)(k):=Mux(wr1Bank.asUInt()===i.U,wr1Address(k),0.U)
            }
            for(k <- 0 to log2Ceil(bankNum)-1){
                rd1BankAdd(i)(k+dataWidth-log2Ceil(bankNum)):=0.U
                wr1BankAdd(i)(k+dataWidth-log2Ceil(bankNum)):=0.U
            }
            bankU(i).data_in1:=wr1Din(i)
            bankU(i).wr_en1:=wr1En(i)
            bankU(i).rdAddress1:=rd1BankAdd(i).asUInt()
            bankU(i).wrAddress1:=wr1BankAdd(i).asUInt()
            rd1Dout(i):=bankU(i).data_out1
        }
        io.data_out1:=rd1Dout(rd1BankReg.asUInt())
    }

    object myMemSys extends App {
        //(new chisel3.stage.ChiselStage).emitVerilog(new HEOOO(16, 64, 64, 64, 16, 8, 17, 7, 7, 32, 27, 64, 4, 4, 4, 1, 12, 12, 12, 2, 5, 8));
        //(new chisel3.stage.ChiselStage).emitVerilog(new IssueUnit(64, 16, 16, 7, 12));
        (new chisel3.stage.ChiselStage).execute(Array("-X", "mverilog"), Seq(chisel3.stage.ChiselGeneratorAnnotation(()=>new memSys(4,4,2,9,2,1,32,512))));
    }
}