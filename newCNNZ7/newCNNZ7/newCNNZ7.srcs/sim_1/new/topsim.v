`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2024/09/20 16:42:28
// Design Name: 
// Module Name: topsim
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module topsim(

    );
reg clock;
reg resetn;
wire rxd=0;
wire txd;
initial begin
    clock=0;
    resetn=0;
    #40
    resetn=1;
end
always #10 clock=~clock;
design_1_wrapper dw
   (.clk_100MHz(clock),
    .reset_rtl_0(resetn));
endmodule
