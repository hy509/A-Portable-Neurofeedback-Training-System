;buildInfoPackage: chisel3, version: 3.4.3, scalaVersion: 2.12.12, sbtVersion: 1.3.10
circuit ArbiterArbitrary : 
  module Arbiter : 
    input clock : Clock
    input reset : Reset
    output io : {flip in : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<32>}[4], out : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<32>}, chosen : UInt<2>}
    
    io.chosen <= UInt<2>("h03") @[Arbiter.scala 123:13]
    io.out.bits <= io.in[3].bits @[Arbiter.scala 124:15]
    when io.in[2].valid : @[Arbiter.scala 126:27]
      io.chosen <= UInt<2>("h02") @[Arbiter.scala 127:17]
      io.out.bits <= io.in[2].bits @[Arbiter.scala 128:19]
      skip @[Arbiter.scala 126:27]
    when io.in[1].valid : @[Arbiter.scala 126:27]
      io.chosen <= UInt<1>("h01") @[Arbiter.scala 127:17]
      io.out.bits <= io.in[1].bits @[Arbiter.scala 128:19]
      skip @[Arbiter.scala 126:27]
    when io.in[0].valid : @[Arbiter.scala 126:27]
      io.chosen <= UInt<1>("h00") @[Arbiter.scala 127:17]
      io.out.bits <= io.in[0].bits @[Arbiter.scala 128:19]
      skip @[Arbiter.scala 126:27]
    node _grant_T = or(io.in[0].valid, io.in[1].valid) @[Arbiter.scala 31:68]
    node _grant_T_1 = or(_grant_T, io.in[2].valid) @[Arbiter.scala 31:68]
    node grant_1 = eq(io.in[0].valid, UInt<1>("h00")) @[Arbiter.scala 31:78]
    node grant_2 = eq(_grant_T, UInt<1>("h00")) @[Arbiter.scala 31:78]
    node grant_3 = eq(_grant_T_1, UInt<1>("h00")) @[Arbiter.scala 31:78]
    node _io_in_0_ready_T = and(UInt<1>("h01"), io.out.ready) @[Arbiter.scala 134:19]
    io.in[0].ready <= _io_in_0_ready_T @[Arbiter.scala 134:14]
    node _io_in_1_ready_T = and(grant_1, io.out.ready) @[Arbiter.scala 134:19]
    io.in[1].ready <= _io_in_1_ready_T @[Arbiter.scala 134:14]
    node _io_in_2_ready_T = and(grant_2, io.out.ready) @[Arbiter.scala 134:19]
    io.in[2].ready <= _io_in_2_ready_T @[Arbiter.scala 134:14]
    node _io_in_3_ready_T = and(grant_3, io.out.ready) @[Arbiter.scala 134:19]
    io.in[3].ready <= _io_in_3_ready_T @[Arbiter.scala 134:14]
    node _io_out_valid_T = eq(grant_3, UInt<1>("h00")) @[Arbiter.scala 135:19]
    node _io_out_valid_T_1 = or(_io_out_valid_T, io.in[3].valid) @[Arbiter.scala 135:31]
    io.out.valid <= _io_out_valid_T_1 @[Arbiter.scala 135:16]
    
  module ArbiterArbitrary : 
    input clock : Clock
    input reset : UInt<1>
    output io : {flip in : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<32>}[4], out : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<32>}, chosen : UInt}
    
    inst arbiter of Arbiter @[Arbit.scala 68:29]
    arbiter.clock <= clock
    arbiter.reset <= reset
    arbiter.io.in[0].bits <= io.in[0].bits @[Arbit.scala 69:23]
    arbiter.io.in[0].valid <= io.in[0].valid @[Arbit.scala 69:23]
    io.in[0].ready <= arbiter.io.in[0].ready @[Arbit.scala 69:23]
    arbiter.io.in[1].bits <= io.in[1].bits @[Arbit.scala 69:23]
    arbiter.io.in[1].valid <= io.in[1].valid @[Arbit.scala 69:23]
    io.in[1].ready <= arbiter.io.in[1].ready @[Arbit.scala 69:23]
    arbiter.io.in[2].bits <= io.in[2].bits @[Arbit.scala 69:23]
    arbiter.io.in[2].valid <= io.in[2].valid @[Arbit.scala 69:23]
    io.in[2].ready <= arbiter.io.in[2].ready @[Arbit.scala 69:23]
    arbiter.io.in[3].bits <= io.in[3].bits @[Arbit.scala 69:23]
    arbiter.io.in[3].valid <= io.in[3].valid @[Arbit.scala 69:23]
    io.in[3].ready <= arbiter.io.in[3].ready @[Arbit.scala 69:23]
    io.out.bits <= arbiter.io.out.bits @[Arbit.scala 70:16]
    io.out.valid <= arbiter.io.out.valid @[Arbit.scala 70:16]
    arbiter.io.out.ready <= io.out.ready @[Arbit.scala 70:16]
    io.chosen <= arbiter.io.chosen @[Arbit.scala 71:19]
    
