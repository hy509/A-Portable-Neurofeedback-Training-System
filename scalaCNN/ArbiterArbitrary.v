module Arbiter(
  input         clock,
  input         reset,
  output        io_in_0_ready,
  input         io_in_0_valid,
  input  [31:0] io_in_0_bits,
  output        io_in_1_ready,
  input         io_in_1_valid,
  input  [31:0] io_in_1_bits,
  output        io_in_2_ready,
  input         io_in_2_valid,
  input  [31:0] io_in_2_bits,
  output        io_in_3_ready,
  input         io_in_3_valid,
  input  [31:0] io_in_3_bits,
  input         io_out_ready,
  output        io_out_valid,
  output [31:0] io_out_bits,
  output [1:0]  io_chosen
);
  wire [1:0] _GEN_0 = io_in_2_valid ? 2'h2 : 2'h3; // @[Arbiter.scala 126:27 Arbiter.scala 127:17 Arbiter.scala 123:13]
  wire [31:0] _GEN_1 = io_in_2_valid ? io_in_2_bits : io_in_3_bits; // @[Arbiter.scala 126:27 Arbiter.scala 128:19 Arbiter.scala 124:15]
  wire [1:0] _GEN_2 = io_in_1_valid ? 2'h1 : _GEN_0; // @[Arbiter.scala 126:27 Arbiter.scala 127:17]
  wire [31:0] _GEN_3 = io_in_1_valid ? io_in_1_bits : _GEN_1; // @[Arbiter.scala 126:27 Arbiter.scala 128:19]
  wire [1:0] _GEN_4 = io_in_0_valid ? 2'h0 : _GEN_2; // @[Arbiter.scala 126:27 Arbiter.scala 127:17]
  wire [31:0] _GEN_5 = io_in_0_valid ? io_in_0_bits : _GEN_3; // @[Arbiter.scala 126:27 Arbiter.scala 128:19]
  wire  _grant_T = io_in_0_valid | io_in_1_valid; // @[Arbiter.scala 31:68]
  wire  _grant_T_1 = _grant_T | io_in_2_valid; // @[Arbiter.scala 31:68]
  wire  grant_1 = io_in_0_valid == 1'h0; // @[Arbiter.scala 31:78]
  wire  grant_2 = _grant_T == 1'h0; // @[Arbiter.scala 31:78]
  wire  grant_3 = _grant_T_1 == 1'h0; // @[Arbiter.scala 31:78]
  wire  _io_in_0_ready_T = 1'h1 & io_out_ready; // @[Arbiter.scala 134:19]
  wire  _io_in_1_ready_T = grant_1 & io_out_ready; // @[Arbiter.scala 134:19]
  wire  _io_in_2_ready_T = grant_2 & io_out_ready; // @[Arbiter.scala 134:19]
  wire  _io_in_3_ready_T = grant_3 & io_out_ready; // @[Arbiter.scala 134:19]
  wire  _io_out_valid_T = grant_3 == 1'h0; // @[Arbiter.scala 135:19]
  wire  _io_out_valid_T_1 = _io_out_valid_T | io_in_3_valid; // @[Arbiter.scala 135:31]
  assign io_in_0_ready = _io_in_0_ready_T; // @[Arbiter.scala 134:14]
  assign io_in_1_ready = _io_in_1_ready_T; // @[Arbiter.scala 134:14]
  assign io_in_2_ready = _io_in_2_ready_T; // @[Arbiter.scala 134:14]
  assign io_in_3_ready = _io_in_3_ready_T; // @[Arbiter.scala 134:14]
  assign io_out_valid = _io_out_valid_T_1; // @[Arbiter.scala 135:16]
  assign io_out_bits = _GEN_5;
  assign io_chosen = _GEN_4;
endmodule
module ArbiterArbitrary(
  input         clock,
  input         reset,
  output        io_in_0_ready,
  input         io_in_0_valid,
  input  [31:0] io_in_0_bits,
  output        io_in_1_ready,
  input         io_in_1_valid,
  input  [31:0] io_in_1_bits,
  output        io_in_2_ready,
  input         io_in_2_valid,
  input  [31:0] io_in_2_bits,
  output        io_in_3_ready,
  input         io_in_3_valid,
  input  [31:0] io_in_3_bits,
  input         io_out_ready,
  output        io_out_valid,
  output [31:0] io_out_bits,
  output [1:0]  io_chosen
);
  wire  arbiter_clock; // @[Arbit.scala 68:29]
  wire  arbiter_reset; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_0_ready; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_0_valid; // @[Arbit.scala 68:29]
  wire [31:0] arbiter_io_in_0_bits; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_1_ready; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_1_valid; // @[Arbit.scala 68:29]
  wire [31:0] arbiter_io_in_1_bits; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_2_ready; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_2_valid; // @[Arbit.scala 68:29]
  wire [31:0] arbiter_io_in_2_bits; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_3_ready; // @[Arbit.scala 68:29]
  wire  arbiter_io_in_3_valid; // @[Arbit.scala 68:29]
  wire [31:0] arbiter_io_in_3_bits; // @[Arbit.scala 68:29]
  wire  arbiter_io_out_ready; // @[Arbit.scala 68:29]
  wire  arbiter_io_out_valid; // @[Arbit.scala 68:29]
  wire [31:0] arbiter_io_out_bits; // @[Arbit.scala 68:29]
  wire [1:0] arbiter_io_chosen; // @[Arbit.scala 68:29]
  Arbiter arbiter ( // @[Arbit.scala 68:29]
    .clock(arbiter_clock),
    .reset(arbiter_reset),
    .io_in_0_ready(arbiter_io_in_0_ready),
    .io_in_0_valid(arbiter_io_in_0_valid),
    .io_in_0_bits(arbiter_io_in_0_bits),
    .io_in_1_ready(arbiter_io_in_1_ready),
    .io_in_1_valid(arbiter_io_in_1_valid),
    .io_in_1_bits(arbiter_io_in_1_bits),
    .io_in_2_ready(arbiter_io_in_2_ready),
    .io_in_2_valid(arbiter_io_in_2_valid),
    .io_in_2_bits(arbiter_io_in_2_bits),
    .io_in_3_ready(arbiter_io_in_3_ready),
    .io_in_3_valid(arbiter_io_in_3_valid),
    .io_in_3_bits(arbiter_io_in_3_bits),
    .io_out_ready(arbiter_io_out_ready),
    .io_out_valid(arbiter_io_out_valid),
    .io_out_bits(arbiter_io_out_bits),
    .io_chosen(arbiter_io_chosen)
  );
  assign io_in_0_ready = arbiter_io_in_0_ready; // @[Arbit.scala 69:23]
  assign io_in_1_ready = arbiter_io_in_1_ready; // @[Arbit.scala 69:23]
  assign io_in_2_ready = arbiter_io_in_2_ready; // @[Arbit.scala 69:23]
  assign io_in_3_ready = arbiter_io_in_3_ready; // @[Arbit.scala 69:23]
  assign io_out_valid = arbiter_io_out_valid; // @[Arbit.scala 70:16]
  assign io_out_bits = arbiter_io_out_bits; // @[Arbit.scala 70:16]
  assign io_chosen = arbiter_io_chosen; // @[Arbit.scala 71:19]
  assign arbiter_clock = clock;
  assign arbiter_reset = reset;
  assign arbiter_io_in_0_valid = io_in_0_valid; // @[Arbit.scala 69:23]
  assign arbiter_io_in_0_bits = io_in_0_bits; // @[Arbit.scala 69:23]
  assign arbiter_io_in_1_valid = io_in_1_valid; // @[Arbit.scala 69:23]
  assign arbiter_io_in_1_bits = io_in_1_bits; // @[Arbit.scala 69:23]
  assign arbiter_io_in_2_valid = io_in_2_valid; // @[Arbit.scala 69:23]
  assign arbiter_io_in_2_bits = io_in_2_bits; // @[Arbit.scala 69:23]
  assign arbiter_io_in_3_valid = io_in_3_valid; // @[Arbit.scala 69:23]
  assign arbiter_io_in_3_bits = io_in_3_bits; // @[Arbit.scala 69:23]
  assign arbiter_io_out_ready = io_out_ready; // @[Arbit.scala 70:16]
endmodule
