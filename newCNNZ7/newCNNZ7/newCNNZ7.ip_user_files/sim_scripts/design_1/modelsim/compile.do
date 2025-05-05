vlib modelsim_lib/work
vlib modelsim_lib/msim

vlib modelsim_lib/msim/xilinx_vip
vlib modelsim_lib/msim/xpm
vlib modelsim_lib/msim/microblaze_v11_0_6
vlib modelsim_lib/msim/xil_defaultlib
vlib modelsim_lib/msim/lmb_v10_v3_0_11
vlib modelsim_lib/msim/lmb_bram_if_cntlr_v4_0_19
vlib modelsim_lib/msim/blk_mem_gen_v8_4_4
vlib modelsim_lib/msim/axi_lite_ipif_v3_0_4
vlib modelsim_lib/msim/mdm_v3_2_21
vlib modelsim_lib/msim/lib_cdc_v1_0_2
vlib modelsim_lib/msim/proc_sys_reset_v5_0_13
vlib modelsim_lib/msim/generic_baseblocks_v2_1_0
vlib modelsim_lib/msim/axi_infrastructure_v1_1_0
vlib modelsim_lib/msim/axi_register_slice_v2_1_24
vlib modelsim_lib/msim/fifo_generator_v13_2_5
vlib modelsim_lib/msim/axi_data_fifo_v2_1_23
vlib modelsim_lib/msim/axi_crossbar_v2_1_25
vlib modelsim_lib/msim/axi_vip_v1_1_10
vlib modelsim_lib/msim/processing_system7_vip_v1_0_12

vmap xilinx_vip modelsim_lib/msim/xilinx_vip
vmap xpm modelsim_lib/msim/xpm
vmap microblaze_v11_0_6 modelsim_lib/msim/microblaze_v11_0_6
vmap xil_defaultlib modelsim_lib/msim/xil_defaultlib
vmap lmb_v10_v3_0_11 modelsim_lib/msim/lmb_v10_v3_0_11
vmap lmb_bram_if_cntlr_v4_0_19 modelsim_lib/msim/lmb_bram_if_cntlr_v4_0_19
vmap blk_mem_gen_v8_4_4 modelsim_lib/msim/blk_mem_gen_v8_4_4
vmap axi_lite_ipif_v3_0_4 modelsim_lib/msim/axi_lite_ipif_v3_0_4
vmap mdm_v3_2_21 modelsim_lib/msim/mdm_v3_2_21
vmap lib_cdc_v1_0_2 modelsim_lib/msim/lib_cdc_v1_0_2
vmap proc_sys_reset_v5_0_13 modelsim_lib/msim/proc_sys_reset_v5_0_13
vmap generic_baseblocks_v2_1_0 modelsim_lib/msim/generic_baseblocks_v2_1_0
vmap axi_infrastructure_v1_1_0 modelsim_lib/msim/axi_infrastructure_v1_1_0
vmap axi_register_slice_v2_1_24 modelsim_lib/msim/axi_register_slice_v2_1_24
vmap fifo_generator_v13_2_5 modelsim_lib/msim/fifo_generator_v13_2_5
vmap axi_data_fifo_v2_1_23 modelsim_lib/msim/axi_data_fifo_v2_1_23
vmap axi_crossbar_v2_1_25 modelsim_lib/msim/axi_crossbar_v2_1_25
vmap axi_vip_v1_1_10 modelsim_lib/msim/axi_vip_v1_1_10
vmap processing_system7_vip_v1_0_12 modelsim_lib/msim/processing_system7_vip_v1_0_12

vlog -work xilinx_vip  -incr -mfcu -sv -L axi_vip_v1_1_10 -L processing_system7_vip_v1_0_12 -L xilinx_vip "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/axi4stream_vip_axi4streampc.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/axi_vip_axi4pc.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/xil_common_vip_pkg.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/axi4stream_vip_pkg.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/axi_vip_pkg.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/axi4stream_vip_if.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/axi_vip_if.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/clk_vip_if.sv" \
"E:/Xilinx/Vivado/2021.1/data/xilinx_vip/hdl/rst_vip_if.sv" \

vlog -work xpm  -incr -mfcu -sv -L axi_vip_v1_1_10 -L processing_system7_vip_v1_0_12 -L xilinx_vip "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"E:/Xilinx/Vivado/2021.1/data/ip/xpm/xpm_cdc/hdl/xpm_cdc.sv" \
"E:/Xilinx/Vivado/2021.1/data/ip/xpm/xpm_memory/hdl/xpm_memory.sv" \

vcom -work xpm  -93 \
"E:/Xilinx/Vivado/2021.1/data/ip/xpm/xpm_VCOMP.vhd" \

vcom -work microblaze_v11_0_6  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/774d/hdl/microblaze_v11_0_vh_rfs.vhd" \

vcom -work xil_defaultlib  -93 \
"../../../bd/design_1/ip/design_1_microblaze_0_0/sim/design_1_microblaze_0_0.vhd" \

vcom -work lmb_v10_v3_0_11  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/c2ed/hdl/lmb_v10_v3_0_vh_rfs.vhd" \

vcom -work xil_defaultlib  -93 \
"../../../bd/design_1/ip/design_1_dlmb_v10_0/sim/design_1_dlmb_v10_0.vhd" \
"../../../bd/design_1/ip/design_1_ilmb_v10_0/sim/design_1_ilmb_v10_0.vhd" \

vcom -work lmb_bram_if_cntlr_v4_0_19  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/0b98/hdl/lmb_bram_if_cntlr_v4_0_vh_rfs.vhd" \

vcom -work xil_defaultlib  -93 \
"../../../bd/design_1/ip/design_1_dlmb_bram_if_cntlr_0/sim/design_1_dlmb_bram_if_cntlr_0.vhd" \
"../../../bd/design_1/ip/design_1_ilmb_bram_if_cntlr_0/sim/design_1_ilmb_bram_if_cntlr_0.vhd" \

vlog -work blk_mem_gen_v8_4_4  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/2985/simulation/blk_mem_gen_v8_4.v" \

vlog -work xil_defaultlib  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../bd/design_1/ip/design_1_lmb_bram_0/sim/design_1_lmb_bram_0.v" \

vcom -work axi_lite_ipif_v3_0_4  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/66ea/hdl/axi_lite_ipif_v3_0_vh_rfs.vhd" \

vcom -work mdm_v3_2_21  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/2932/hdl/mdm_v3_2_vh_rfs.vhd" \

vcom -work xil_defaultlib  -93 \
"../../../bd/design_1/ip/design_1_mdm_1_0/sim/design_1_mdm_1_0.vhd" \

vlog -work xil_defaultlib  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../bd/design_1/ip/design_1_clk_wiz_1_0/design_1_clk_wiz_1_0_clk_wiz.v" \
"../../../bd/design_1/ip/design_1_clk_wiz_1_0/design_1_clk_wiz_1_0.v" \

vcom -work lib_cdc_v1_0_2  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ef1e/hdl/lib_cdc_v1_0_rfs.vhd" \

vcom -work proc_sys_reset_v5_0_13  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/8842/hdl/proc_sys_reset_v5_0_vh_rfs.vhd" \

vcom -work xil_defaultlib  -93 \
"../../../bd/design_1/ip/design_1_rst_clk_wiz_1_100M_0/sim/design_1_rst_clk_wiz_1_100M_0.vhd" \

vlog -work generic_baseblocks_v2_1_0  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/b752/hdl/generic_baseblocks_v2_1_vl_rfs.v" \

vlog -work axi_infrastructure_v1_1_0  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl/axi_infrastructure_v1_1_vl_rfs.v" \

vlog -work axi_register_slice_v2_1_24  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/8f68/hdl/axi_register_slice_v2_1_vl_rfs.v" \

vlog -work fifo_generator_v13_2_5  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/276e/simulation/fifo_generator_vlog_beh.v" \

vcom -work fifo_generator_v13_2_5  -93 \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/276e/hdl/fifo_generator_v13_2_rfs.vhd" \

vlog -work fifo_generator_v13_2_5  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/276e/hdl/fifo_generator_v13_2_rfs.v" \

vlog -work axi_data_fifo_v2_1_23  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/94ec/hdl/axi_data_fifo_v2_1_vl_rfs.v" \

vlog -work axi_crossbar_v2_1_25  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/3917/hdl/axi_crossbar_v2_1_vl_rfs.v" \

vlog -work xil_defaultlib  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../bd/design_1/ip/design_1_xbar_0/sim/design_1_xbar_0.v" \

vlog -work axi_vip_v1_1_10  -incr -mfcu -sv -L axi_vip_v1_1_10 -L processing_system7_vip_v1_0_12 -L xilinx_vip "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/0980/hdl/axi_vip_v1_1_vl_rfs.sv" \

vlog -work processing_system7_vip_v1_0_12  -incr -mfcu -sv -L axi_vip_v1_1_10 -L processing_system7_vip_v1_0_12 -L xilinx_vip "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl/processing_system7_vip_v1_0_vl_rfs.sv" \

vlog -work xil_defaultlib  -incr -mfcu "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/6dcf" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/ec67/hdl" "+incdir+../../../../newCNNZ7.gen/sources_1/bd/design_1/ipshared/f42d/hdl" "+incdir+E:/Xilinx/Vivado/2021.1/data/xilinx_vip/include" \
"../../../bd/design_1/ip/design_1_processing_system7_0_0/sim/design_1_processing_system7_0_0.v" \
"../../../bd/design_1/ipshared/07a9/src/CNNAccelerator.v" \
"../../../bd/design_1/ipshared/07a9/src/CNNAccelerator_v1_0_S00_AXI.v" \
"../../../bd/design_1/ipshared/07a9/src/CNNAccelerator_v1_0.v" \
"../../../bd/design_1/ip/design_1_cnnRandomMem_0_0/sim/design_1_cnnRandomMem_0_0.v" \
"../../../bd/design_1/sim/design_1.v" \

vlog -work xil_defaultlib \
"glbl.v"

