;buildInfoPackage: chisel3, version: 3.4.3, scalaVersion: 2.12.12, sbtVersion: 1.3.10
circuit HEArbiter : 
  module PriorArbiter : 
    input clock : Clock
    input reset : Reset
    output io : {flip selectIn : UInt<4>, selectOut : UInt<4>}
    
    node _T = sub(io.selectIn, UInt<4>("h01")) @[ChiselLib.scala 14:31]
    node makeup = tail(_T, 1) @[ChiselLib.scala 14:31]
    node _T_1 = not(makeup) @[ChiselLib.scala 15:36]
    node _T_2 = and(io.selectIn, _T_1) @[ChiselLib.scala 15:34]
    io.selectOut <= _T_2 @[ChiselLib.scala 15:21]
    
  module Pos2Bin : 
    input clock : Clock
    input reset : Reset
    output io : {flip selectIn : UInt<4>, selectOut : UInt<2>}
    
    wire TmpContainer : UInt<2>[4][3] @[ChiselLib.scala 24:30]
    wire Reducted : UInt<1>[4][3] @[ChiselLib.scala 25:26]
    Reducted[0][0] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[0][0] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[0][1] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[0][1] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[0][2] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[0][2] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[0][3] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[0][3] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[1][0] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[1][0] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[1][1] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[1][1] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[1][2] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[1][2] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[1][3] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[1][3] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[2][0] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[2][0] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[2][1] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[2][1] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[2][2] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[2][2] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    Reducted[2][3] <= UInt<1>("h00") @[ChiselLib.scala 28:31]
    TmpContainer[2][3] <= UInt<2>("h00") @[ChiselLib.scala 30:39]
    node _T = bits(io.selectIn, 0, 0) @[ChiselLib.scala 35:40]
    Reducted[0][0] <= _T @[ChiselLib.scala 35:27]
    node _T_1 = bits(io.selectIn, 1, 1) @[ChiselLib.scala 35:40]
    Reducted[0][1] <= _T_1 @[ChiselLib.scala 35:27]
    node _T_2 = bits(io.selectIn, 2, 2) @[ChiselLib.scala 35:40]
    Reducted[0][2] <= _T_2 @[ChiselLib.scala 35:27]
    node _T_3 = bits(io.selectIn, 3, 3) @[ChiselLib.scala 35:40]
    Reducted[0][3] <= _T_3 @[ChiselLib.scala 35:27]
    node _T_4 = or(Reducted[0][0], Reducted[0][1]) @[ChiselLib.scala 42:51]
    Reducted[1][0] <= _T_4 @[ChiselLib.scala 42:31]
    node _T_5 = eq(Reducted[0][1], UInt<1>("h01")) @[ChiselLib.scala 43:42]
    when _T_5 : @[ChiselLib.scala 43:54]
      node _T_6 = dshl(TmpContainer[0][1], UInt<1>("h01")) @[ChiselLib.scala 44:66]
      node _T_7 = or(_T_6, UInt<2>("h01")) @[ChiselLib.scala 44:72]
      TmpContainer[1][0] <= _T_7 @[ChiselLib.scala 44:39]
      skip @[ChiselLib.scala 43:54]
    else : @[ChiselLib.scala 46:27]
      node _T_8 = dshl(TmpContainer[0][0], UInt<1>("h01")) @[ChiselLib.scala 47:64]
      TmpContainer[1][0] <= _T_8 @[ChiselLib.scala 47:39]
      skip @[ChiselLib.scala 46:27]
    node _T_9 = or(Reducted[0][2], Reducted[0][3]) @[ChiselLib.scala 42:51]
    Reducted[1][1] <= _T_9 @[ChiselLib.scala 42:31]
    node _T_10 = eq(Reducted[0][3], UInt<1>("h01")) @[ChiselLib.scala 43:42]
    when _T_10 : @[ChiselLib.scala 43:54]
      node _T_11 = dshl(TmpContainer[0][3], UInt<1>("h01")) @[ChiselLib.scala 44:66]
      node _T_12 = or(_T_11, UInt<2>("h01")) @[ChiselLib.scala 44:72]
      TmpContainer[1][1] <= _T_12 @[ChiselLib.scala 44:39]
      skip @[ChiselLib.scala 43:54]
    else : @[ChiselLib.scala 46:27]
      node _T_13 = dshl(TmpContainer[0][2], UInt<1>("h01")) @[ChiselLib.scala 47:64]
      TmpContainer[1][1] <= _T_13 @[ChiselLib.scala 47:39]
      skip @[ChiselLib.scala 46:27]
    node _T_14 = or(Reducted[1][0], Reducted[1][1]) @[ChiselLib.scala 42:51]
    Reducted[2][0] <= _T_14 @[ChiselLib.scala 42:31]
    node _T_15 = eq(Reducted[1][1], UInt<1>("h01")) @[ChiselLib.scala 43:42]
    when _T_15 : @[ChiselLib.scala 43:54]
      node _T_16 = dshl(TmpContainer[1][1], UInt<1>("h01")) @[ChiselLib.scala 44:66]
      node _T_17 = or(_T_16, UInt<2>("h01")) @[ChiselLib.scala 44:72]
      TmpContainer[2][0] <= _T_17 @[ChiselLib.scala 44:39]
      skip @[ChiselLib.scala 43:54]
    else : @[ChiselLib.scala 46:27]
      node _T_18 = dshl(TmpContainer[1][0], UInt<1>("h01")) @[ChiselLib.scala 47:64]
      TmpContainer[2][0] <= _T_18 @[ChiselLib.scala 47:39]
      skip @[ChiselLib.scala 46:27]
    wire reversedOut : UInt<1>[2] @[ChiselLib.scala 51:29]
    node _T_19 = bits(TmpContainer[2][0], 1, 1) @[ChiselLib.scala 53:65]
    reversedOut[0] <= _T_19 @[ChiselLib.scala 53:27]
    node _T_20 = bits(TmpContainer[2][0], 0, 0) @[ChiselLib.scala 53:65]
    reversedOut[1] <= _T_20 @[ChiselLib.scala 53:27]
    node _T_21 = cat(reversedOut[1], reversedOut[0]) @[ChiselLib.scala 55:41]
    io.selectOut <= _T_21 @[ChiselLib.scala 55:21]
    
  module PriorMux : 
    input clock : Clock
    input reset : Reset
    output io : {flip selectIn : UInt<4>, selectOut : UInt<2>, selectOutVec : UInt<4>}
    
    inst priorityArbiter of PriorArbiter @[ChiselLib.scala 64:37]
    priorityArbiter.clock <= clock
    priorityArbiter.reset <= reset
    priorityArbiter.io.selectIn <= io.selectIn @[ChiselLib.scala 65:36]
    io.selectOutVec <= priorityArbiter.io.selectOut @[ChiselLib.scala 66:24]
    inst position2binary of Pos2Bin @[ChiselLib.scala 67:37]
    position2binary.clock <= clock
    position2binary.reset <= reset
    position2binary.io.selectIn <= priorityArbiter.io.selectOut @[ChiselLib.scala 68:36]
    io.selectOut <= position2binary.io.selectOut @[ChiselLib.scala 69:21]
    
  module ArbiterPow2 : 
    input clock : Clock
    input reset : Reset
    output io : {flip in : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<1>}[4], out : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<1>}, chosen : UInt<2>}
    
    inst arb of PriorMux @[Arbit.scala 12:23]
    arb.clock <= clock
    arb.reset <= reset
    wire inVal : UInt<1>[4] @[Arbit.scala 13:23]
    wire inBits : UInt<1>[4] @[Arbit.scala 14:24]
    wire outGrant : UInt<4> @[Arbit.scala 15:26]
    inVal[0] <= io.in[0].valid @[Arbit.scala 17:21]
    inBits[0] <= io.in[0].bits @[Arbit.scala 18:22]
    node _T = bits(outGrant, 0, 0) @[Arbit.scala 19:52]
    node _T_1 = and(io.out.ready, _T) @[Arbit.scala 19:42]
    io.in[0].ready <= _T_1 @[Arbit.scala 19:27]
    inVal[1] <= io.in[1].valid @[Arbit.scala 17:21]
    inBits[1] <= io.in[1].bits @[Arbit.scala 18:22]
    node _T_2 = bits(outGrant, 1, 1) @[Arbit.scala 19:52]
    node _T_3 = and(io.out.ready, _T_2) @[Arbit.scala 19:42]
    io.in[1].ready <= _T_3 @[Arbit.scala 19:27]
    inVal[2] <= io.in[2].valid @[Arbit.scala 17:21]
    inBits[2] <= io.in[2].bits @[Arbit.scala 18:22]
    node _T_4 = bits(outGrant, 2, 2) @[Arbit.scala 19:52]
    node _T_5 = and(io.out.ready, _T_4) @[Arbit.scala 19:42]
    io.in[2].ready <= _T_5 @[Arbit.scala 19:27]
    inVal[3] <= io.in[3].valid @[Arbit.scala 17:21]
    inBits[3] <= io.in[3].bits @[Arbit.scala 18:22]
    node _T_6 = bits(outGrant, 3, 3) @[Arbit.scala 19:52]
    node _T_7 = and(io.out.ready, _T_6) @[Arbit.scala 19:42]
    io.in[3].ready <= _T_7 @[Arbit.scala 19:27]
    outGrant <= arb.io.selectOutVec @[Arbit.scala 21:17]
    node lo = cat(inVal[1], inVal[0]) @[Arbit.scala 22:32]
    node hi = cat(inVal[3], inVal[2]) @[Arbit.scala 22:32]
    node _T_8 = cat(hi, lo) @[Arbit.scala 22:32]
    arb.io.selectIn <= _T_8 @[Arbit.scala 22:24]
    io.chosen <= arb.io.selectOut @[Arbit.scala 23:18]
    io.out.bits <= inBits[arb.io.selectOut] @[Arbit.scala 24:20]
    node _T_9 = or(inVal[0], inVal[1]) @[Arbit.scala 25:42]
    node _T_10 = or(inVal[2], inVal[3]) @[Arbit.scala 25:42]
    wire _WIRE : UInt<1>[2] @[Arbit.scala 25:39]
    _WIRE[0] <= _T_9 @[Arbit.scala 25:39]
    _WIRE[1] <= _T_10 @[Arbit.scala 25:39]
    node _T_11 = or(_WIRE[0], _WIRE[1]) @[Arbit.scala 25:42]
    wire _WIRE_1 : UInt<1>[1] @[Arbit.scala 25:39]
    _WIRE_1[0] <= _T_11 @[Arbit.scala 25:39]
    io.out.valid <= _WIRE_1[0] @[Arbit.scala 25:21]
    
  module HEArbiter : 
    input clock : Clock
    input reset : UInt<1>
    output io : {flip in : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<1>}[4], out : {flip ready : UInt<1>, valid : UInt<1>, bits : UInt<1>}, chosen : UInt<2>}
    
    inst arb of ArbiterPow2 @[Arbit.scala 38:23]
    arb.clock <= clock
    arb.reset <= reset
    wire choose : UInt<1>[2] @[Arbit.scala 39:24]
    arb.io.in[0].valid <= io.in[0].valid @[Arbit.scala 41:31]
    arb.io.in[0].bits <= io.in[0].bits @[Arbit.scala 42:30]
    io.in[0].ready <= arb.io.in[0].ready @[Arbit.scala 43:27]
    arb.io.in[1].valid <= io.in[1].valid @[Arbit.scala 41:31]
    arb.io.in[1].bits <= io.in[1].bits @[Arbit.scala 42:30]
    io.in[1].ready <= arb.io.in[1].ready @[Arbit.scala 43:27]
    arb.io.in[2].valid <= io.in[2].valid @[Arbit.scala 41:31]
    arb.io.in[2].bits <= io.in[2].bits @[Arbit.scala 42:30]
    io.in[2].ready <= arb.io.in[2].ready @[Arbit.scala 43:27]
    arb.io.in[3].valid <= io.in[3].valid @[Arbit.scala 41:31]
    arb.io.in[3].bits <= io.in[3].bits @[Arbit.scala 42:30]
    io.in[3].ready <= arb.io.in[3].ready @[Arbit.scala 43:27]
    node _T = bits(arb.io.chosen, 0, 0) @[Arbit.scala 46:37]
    choose[0] <= _T @[Arbit.scala 46:22]
    node _T_1 = bits(arb.io.chosen, 1, 1) @[Arbit.scala 46:37]
    choose[1] <= _T_1 @[Arbit.scala 46:22]
    node _T_2 = cat(choose[1], choose[0]) @[Arbit.scala 48:27]
    io.chosen <= _T_2 @[Arbit.scala 48:18]
    io.out.bits <= arb.io.out.bits @[Arbit.scala 49:20]
    io.out.valid <= arb.io.out.valid @[Arbit.scala 50:21]
    arb.io.out.ready <= io.out.ready @[Arbit.scala 51:25]
    
