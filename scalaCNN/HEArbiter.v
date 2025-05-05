module PriorArbiter(
  input        clock,
  input        reset,
  input  [3:0] io_selectIn,
  output [3:0] io_selectOut
);
  wire [4:0] _T = io_selectIn - 4'h1; // @[ChiselLib.scala 14:31]
  wire [3:0] makeup = io_selectIn - 4'h1; // @[ChiselLib.scala 14:31]
  wire [3:0] _T_1 = ~makeup; // @[ChiselLib.scala 15:36]
  wire [3:0] _T_2 = io_selectIn & _T_1; // @[ChiselLib.scala 15:34]
  assign io_selectOut = _T_2; // @[ChiselLib.scala 15:21]
endmodule
module Pos2Bin(
  input        clock,
  input        reset,
  input  [3:0] io_selectIn,
  output [1:0] io_selectOut
);
  wire  _T = io_selectIn[0]; // @[ChiselLib.scala 35:40]
  wire  _T_1 = io_selectIn[1]; // @[ChiselLib.scala 35:40]
  wire  _T_2 = io_selectIn[2]; // @[ChiselLib.scala 35:40]
  wire  _T_3 = io_selectIn[3]; // @[ChiselLib.scala 35:40]
  wire  Reducted_0_0 = io_selectIn[0]; // @[ChiselLib.scala 25:26 ChiselLib.scala 35:27]
  wire  Reducted_0_1 = io_selectIn[1]; // @[ChiselLib.scala 25:26 ChiselLib.scala 35:27]
  wire  _T_4 = Reducted_0_0 | Reducted_0_1; // @[ChiselLib.scala 42:51]
  wire  _T_5 = Reducted_0_1 == 1'h1; // @[ChiselLib.scala 43:42]
  wire [1:0] TmpContainer_0_1 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [2:0] _GEN_3 = {{1'd0}, TmpContainer_0_1}; // @[ChiselLib.scala 44:66]
  wire [2:0] _T_6 = _GEN_3 << 1'h1; // @[ChiselLib.scala 44:66]
  wire [2:0] _T_7 = _T_6 | 3'h1; // @[ChiselLib.scala 44:72]
  wire [1:0] TmpContainer_0_0 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [2:0] _GEN_4 = {{1'd0}, TmpContainer_0_0}; // @[ChiselLib.scala 47:64]
  wire [2:0] _T_8 = _GEN_4 << 1'h1; // @[ChiselLib.scala 47:64]
  wire [2:0] _GEN_0 = _T_5 ? _T_7 : _T_8; // @[ChiselLib.scala 43:54 ChiselLib.scala 44:39 ChiselLib.scala 47:39]
  wire  Reducted_0_2 = io_selectIn[2]; // @[ChiselLib.scala 25:26 ChiselLib.scala 35:27]
  wire  Reducted_0_3 = io_selectIn[3]; // @[ChiselLib.scala 25:26 ChiselLib.scala 35:27]
  wire  _T_9 = Reducted_0_2 | Reducted_0_3; // @[ChiselLib.scala 42:51]
  wire  _T_10 = Reducted_0_3 == 1'h1; // @[ChiselLib.scala 43:42]
  wire [1:0] TmpContainer_0_3 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [2:0] _GEN_5 = {{1'd0}, TmpContainer_0_3}; // @[ChiselLib.scala 44:66]
  wire [2:0] _T_11 = _GEN_5 << 1'h1; // @[ChiselLib.scala 44:66]
  wire [2:0] _T_12 = _T_11 | 3'h1; // @[ChiselLib.scala 44:72]
  wire [1:0] TmpContainer_0_2 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [2:0] _GEN_6 = {{1'd0}, TmpContainer_0_2}; // @[ChiselLib.scala 47:64]
  wire [2:0] _T_13 = _GEN_6 << 1'h1; // @[ChiselLib.scala 47:64]
  wire [2:0] _GEN_1 = _T_10 ? _T_12 : _T_13; // @[ChiselLib.scala 43:54 ChiselLib.scala 44:39 ChiselLib.scala 47:39]
  wire  Reducted_1_0 = _T_4; // @[ChiselLib.scala 25:26 ChiselLib.scala 42:31]
  wire  Reducted_1_1 = _T_9; // @[ChiselLib.scala 25:26 ChiselLib.scala 42:31]
  wire  _T_14 = Reducted_1_0 | Reducted_1_1; // @[ChiselLib.scala 42:51]
  wire  _T_15 = Reducted_1_1 == 1'h1; // @[ChiselLib.scala 43:42]
  wire [1:0] TmpContainer_1_1 = _GEN_1[1:0]; // @[ChiselLib.scala 24:30]
  wire [2:0] _GEN_7 = {{1'd0}, TmpContainer_1_1}; // @[ChiselLib.scala 44:66]
  wire [2:0] _T_16 = _GEN_7 << 1'h1; // @[ChiselLib.scala 44:66]
  wire [2:0] _T_17 = _T_16 | 3'h1; // @[ChiselLib.scala 44:72]
  wire [1:0] TmpContainer_1_0 = _GEN_0[1:0]; // @[ChiselLib.scala 24:30]
  wire [2:0] _GEN_8 = {{1'd0}, TmpContainer_1_0}; // @[ChiselLib.scala 47:64]
  wire [2:0] _T_18 = _GEN_8 << 1'h1; // @[ChiselLib.scala 47:64]
  wire [2:0] _GEN_2 = _T_15 ? _T_17 : _T_18; // @[ChiselLib.scala 43:54 ChiselLib.scala 44:39 ChiselLib.scala 47:39]
  wire [1:0] TmpContainer_2_0 = _GEN_2[1:0]; // @[ChiselLib.scala 24:30]
  wire  _T_19 = TmpContainer_2_0[1]; // @[ChiselLib.scala 53:65]
  wire  _T_20 = TmpContainer_2_0[0]; // @[ChiselLib.scala 53:65]
  wire  reversedOut_1 = TmpContainer_2_0[0]; // @[ChiselLib.scala 51:29 ChiselLib.scala 53:27]
  wire  reversedOut_0 = TmpContainer_2_0[1]; // @[ChiselLib.scala 51:29 ChiselLib.scala 53:27]
  wire [1:0] _T_21 = {reversedOut_1,reversedOut_0}; // @[ChiselLib.scala 55:41]
  wire [1:0] TmpContainer_1_2 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [1:0] TmpContainer_1_3 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [1:0] TmpContainer_2_1 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [1:0] TmpContainer_2_2 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire [1:0] TmpContainer_2_3 = 2'h0; // @[ChiselLib.scala 24:30 ChiselLib.scala 30:39]
  wire  Reducted_1_2 = 1'h0; // @[ChiselLib.scala 25:26 ChiselLib.scala 28:31]
  wire  Reducted_1_3 = 1'h0; // @[ChiselLib.scala 25:26 ChiselLib.scala 28:31]
  wire  Reducted_2_0 = _T_14; // @[ChiselLib.scala 25:26 ChiselLib.scala 42:31]
  wire  Reducted_2_1 = 1'h0; // @[ChiselLib.scala 25:26 ChiselLib.scala 28:31]
  wire  Reducted_2_2 = 1'h0; // @[ChiselLib.scala 25:26 ChiselLib.scala 28:31]
  wire  Reducted_2_3 = 1'h0; // @[ChiselLib.scala 25:26 ChiselLib.scala 28:31]
  assign io_selectOut = _T_21; // @[ChiselLib.scala 55:21]
endmodule
module PriorMux(
  input        clock,
  input        reset,
  input  [3:0] io_selectIn,
  output [1:0] io_selectOut,
  output [3:0] io_selectOutVec
);
  wire  priorityArbiter_clock; // @[ChiselLib.scala 64:37]
  wire  priorityArbiter_reset; // @[ChiselLib.scala 64:37]
  wire [3:0] priorityArbiter_io_selectIn; // @[ChiselLib.scala 64:37]
  wire [3:0] priorityArbiter_io_selectOut; // @[ChiselLib.scala 64:37]
  wire  position2binary_clock; // @[ChiselLib.scala 67:37]
  wire  position2binary_reset; // @[ChiselLib.scala 67:37]
  wire [3:0] position2binary_io_selectIn; // @[ChiselLib.scala 67:37]
  wire [1:0] position2binary_io_selectOut; // @[ChiselLib.scala 67:37]
  PriorArbiter priorityArbiter ( // @[ChiselLib.scala 64:37]
    .clock(priorityArbiter_clock),
    .reset(priorityArbiter_reset),
    .io_selectIn(priorityArbiter_io_selectIn),
    .io_selectOut(priorityArbiter_io_selectOut)
  );
  Pos2Bin position2binary ( // @[ChiselLib.scala 67:37]
    .clock(position2binary_clock),
    .reset(position2binary_reset),
    .io_selectIn(position2binary_io_selectIn),
    .io_selectOut(position2binary_io_selectOut)
  );
  assign io_selectOut = position2binary_io_selectOut; // @[ChiselLib.scala 69:21]
  assign io_selectOutVec = priorityArbiter_io_selectOut; // @[ChiselLib.scala 66:24]
  assign priorityArbiter_clock = clock;
  assign priorityArbiter_reset = reset;
  assign priorityArbiter_io_selectIn = io_selectIn; // @[ChiselLib.scala 65:36]
  assign position2binary_clock = clock;
  assign position2binary_reset = reset;
  assign position2binary_io_selectIn = priorityArbiter_io_selectOut; // @[ChiselLib.scala 68:36]
endmodule
module ArbiterPow2(
  input        clock,
  input        reset,
  output       io_in_0_ready,
  input        io_in_0_valid,
  input        io_in_0_bits,
  output       io_in_1_ready,
  input        io_in_1_valid,
  input        io_in_1_bits,
  output       io_in_2_ready,
  input        io_in_2_valid,
  input        io_in_2_bits,
  output       io_in_3_ready,
  input        io_in_3_valid,
  input        io_in_3_bits,
  input        io_out_ready,
  output       io_out_valid,
  output       io_out_bits,
  output [1:0] io_chosen
);
  wire  arb_clock; // @[Arbit.scala 12:23]
  wire  arb_reset; // @[Arbit.scala 12:23]
  wire [3:0] arb_io_selectIn; // @[Arbit.scala 12:23]
  wire [1:0] arb_io_selectOut; // @[Arbit.scala 12:23]
  wire [3:0] arb_io_selectOutVec; // @[Arbit.scala 12:23]
  wire [3:0] outGrant = arb_io_selectOutVec; // @[Arbit.scala 15:26 Arbit.scala 21:17]
  wire  _T = outGrant[0]; // @[Arbit.scala 19:52]
  wire  _T_1 = io_out_ready & outGrant[0]; // @[Arbit.scala 19:42]
  wire  _T_2 = outGrant[1]; // @[Arbit.scala 19:52]
  wire  _T_3 = io_out_ready & outGrant[1]; // @[Arbit.scala 19:42]
  wire  _T_4 = outGrant[2]; // @[Arbit.scala 19:52]
  wire  _T_5 = io_out_ready & outGrant[2]; // @[Arbit.scala 19:42]
  wire  _T_6 = outGrant[3]; // @[Arbit.scala 19:52]
  wire  _T_7 = io_out_ready & outGrant[3]; // @[Arbit.scala 19:42]
  wire  inVal_1 = io_in_1_valid; // @[Arbit.scala 13:23 Arbit.scala 17:21]
  wire  inVal_0 = io_in_0_valid; // @[Arbit.scala 13:23 Arbit.scala 17:21]
  wire [1:0] lo = {inVal_1,inVal_0}; // @[Arbit.scala 22:32]
  wire  inVal_3 = io_in_3_valid; // @[Arbit.scala 13:23 Arbit.scala 17:21]
  wire  inVal_2 = io_in_2_valid; // @[Arbit.scala 13:23 Arbit.scala 17:21]
  wire [1:0] hi = {inVal_3,inVal_2}; // @[Arbit.scala 22:32]
  wire [3:0] _T_8 = {hi,lo}; // @[Arbit.scala 22:32]
  wire  inBits_0 = io_in_0_bits; // @[Arbit.scala 14:24 Arbit.scala 18:22]
  wire  _GEN_0 = inBits_0; // @[Arbit.scala 24:20 Arbit.scala 24:20]
  wire  inBits_1 = io_in_1_bits; // @[Arbit.scala 14:24 Arbit.scala 18:22]
  wire  _GEN_1 = 2'h1 == arb_io_selectOut ? inBits_1 : _GEN_0; // @[Arbit.scala 24:20 Arbit.scala 24:20]
  wire  inBits_2 = io_in_2_bits; // @[Arbit.scala 14:24 Arbit.scala 18:22]
  wire  _GEN_2 = 2'h2 == arb_io_selectOut ? inBits_2 : _GEN_1; // @[Arbit.scala 24:20 Arbit.scala 24:20]
  wire  inBits_3 = io_in_3_bits; // @[Arbit.scala 14:24 Arbit.scala 18:22]
  wire  _GEN_3 = 2'h3 == arb_io_selectOut ? inBits_3 : _GEN_2; // @[Arbit.scala 24:20 Arbit.scala 24:20]
  wire  _T_9 = inVal_0 | inVal_1; // @[Arbit.scala 25:42]
  wire  _T_10 = inVal_2 | inVal_3; // @[Arbit.scala 25:42]
  wire  _WIRE__0 = _T_9; // @[Arbit.scala 25:39 Arbit.scala 25:39]
  wire  _WIRE__1 = _T_10; // @[Arbit.scala 25:39 Arbit.scala 25:39]
  wire  _T_11 = _WIRE__0 | _WIRE__1; // @[Arbit.scala 25:42]
  wire  _inBits_arb_io_selectOut = _GEN_3; // @[Arbit.scala 24:20]
  wire  _WIRE_1_0 = _T_11; // @[Arbit.scala 25:39 Arbit.scala 25:39]
  PriorMux arb ( // @[Arbit.scala 12:23]
    .clock(arb_clock),
    .reset(arb_reset),
    .io_selectIn(arb_io_selectIn),
    .io_selectOut(arb_io_selectOut),
    .io_selectOutVec(arb_io_selectOutVec)
  );
  assign io_in_0_ready = _T_1; // @[Arbit.scala 19:27]
  assign io_in_1_ready = _T_3; // @[Arbit.scala 19:27]
  assign io_in_2_ready = _T_5; // @[Arbit.scala 19:27]
  assign io_in_3_ready = _T_7; // @[Arbit.scala 19:27]
  assign io_out_valid = _WIRE_1_0; // @[Arbit.scala 25:21]
  assign io_out_bits = _inBits_arb_io_selectOut; // @[Arbit.scala 24:20]
  assign io_chosen = arb_io_selectOut; // @[Arbit.scala 23:18]
  assign arb_clock = clock;
  assign arb_reset = reset;
  assign arb_io_selectIn = _T_8; // @[Arbit.scala 22:24]
endmodule
module HEArbiter(
  input        clock,
  input        reset,
  output       io_in_0_ready,
  input        io_in_0_valid,
  input        io_in_0_bits,
  output       io_in_1_ready,
  input        io_in_1_valid,
  input        io_in_1_bits,
  output       io_in_2_ready,
  input        io_in_2_valid,
  input        io_in_2_bits,
  output       io_in_3_ready,
  input        io_in_3_valid,
  input        io_in_3_bits,
  input        io_out_ready,
  output       io_out_valid,
  output       io_out_bits,
  output [1:0] io_chosen
);
  wire  arb_clock; // @[Arbit.scala 38:23]
  wire  arb_reset; // @[Arbit.scala 38:23]
  wire  arb_io_in_0_ready; // @[Arbit.scala 38:23]
  wire  arb_io_in_0_valid; // @[Arbit.scala 38:23]
  wire  arb_io_in_0_bits; // @[Arbit.scala 38:23]
  wire  arb_io_in_1_ready; // @[Arbit.scala 38:23]
  wire  arb_io_in_1_valid; // @[Arbit.scala 38:23]
  wire  arb_io_in_1_bits; // @[Arbit.scala 38:23]
  wire  arb_io_in_2_ready; // @[Arbit.scala 38:23]
  wire  arb_io_in_2_valid; // @[Arbit.scala 38:23]
  wire  arb_io_in_2_bits; // @[Arbit.scala 38:23]
  wire  arb_io_in_3_ready; // @[Arbit.scala 38:23]
  wire  arb_io_in_3_valid; // @[Arbit.scala 38:23]
  wire  arb_io_in_3_bits; // @[Arbit.scala 38:23]
  wire  arb_io_out_ready; // @[Arbit.scala 38:23]
  wire  arb_io_out_valid; // @[Arbit.scala 38:23]
  wire  arb_io_out_bits; // @[Arbit.scala 38:23]
  wire [1:0] arb_io_chosen; // @[Arbit.scala 38:23]
  wire  _T = arb_io_chosen[0]; // @[Arbit.scala 46:37]
  wire  _T_1 = arb_io_chosen[1]; // @[Arbit.scala 46:37]
  wire  choose_1 = arb_io_chosen[1]; // @[Arbit.scala 39:24 Arbit.scala 46:22]
  wire  choose_0 = arb_io_chosen[0]; // @[Arbit.scala 39:24 Arbit.scala 46:22]
  wire [1:0] _T_2 = {choose_1,choose_0}; // @[Arbit.scala 48:27]
  ArbiterPow2 arb ( // @[Arbit.scala 38:23]
    .clock(arb_clock),
    .reset(arb_reset),
    .io_in_0_ready(arb_io_in_0_ready),
    .io_in_0_valid(arb_io_in_0_valid),
    .io_in_0_bits(arb_io_in_0_bits),
    .io_in_1_ready(arb_io_in_1_ready),
    .io_in_1_valid(arb_io_in_1_valid),
    .io_in_1_bits(arb_io_in_1_bits),
    .io_in_2_ready(arb_io_in_2_ready),
    .io_in_2_valid(arb_io_in_2_valid),
    .io_in_2_bits(arb_io_in_2_bits),
    .io_in_3_ready(arb_io_in_3_ready),
    .io_in_3_valid(arb_io_in_3_valid),
    .io_in_3_bits(arb_io_in_3_bits),
    .io_out_ready(arb_io_out_ready),
    .io_out_valid(arb_io_out_valid),
    .io_out_bits(arb_io_out_bits),
    .io_chosen(arb_io_chosen)
  );
  assign io_in_0_ready = arb_io_in_0_ready; // @[Arbit.scala 43:27]
  assign io_in_1_ready = arb_io_in_1_ready; // @[Arbit.scala 43:27]
  assign io_in_2_ready = arb_io_in_2_ready; // @[Arbit.scala 43:27]
  assign io_in_3_ready = arb_io_in_3_ready; // @[Arbit.scala 43:27]
  assign io_out_valid = arb_io_out_valid; // @[Arbit.scala 50:21]
  assign io_out_bits = arb_io_out_bits; // @[Arbit.scala 49:20]
  assign io_chosen = _T_2; // @[Arbit.scala 48:18]
  assign arb_clock = clock;
  assign arb_reset = reset;
  assign arb_io_in_0_valid = io_in_0_valid; // @[Arbit.scala 41:31]
  assign arb_io_in_0_bits = io_in_0_bits; // @[Arbit.scala 42:30]
  assign arb_io_in_1_valid = io_in_1_valid; // @[Arbit.scala 41:31]
  assign arb_io_in_1_bits = io_in_1_bits; // @[Arbit.scala 42:30]
  assign arb_io_in_2_valid = io_in_2_valid; // @[Arbit.scala 41:31]
  assign arb_io_in_2_bits = io_in_2_bits; // @[Arbit.scala 42:30]
  assign arb_io_in_3_valid = io_in_3_valid; // @[Arbit.scala 41:31]
  assign arb_io_in_3_bits = io_in_3_bits; // @[Arbit.scala 42:30]
  assign arb_io_out_ready = io_out_ready; // @[Arbit.scala 51:25]
endmodule
