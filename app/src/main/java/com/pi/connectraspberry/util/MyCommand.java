package com.pi.connectraspberry.util;

public interface MyCommand {


    // Android --> raspberry

    String CMD_START = "cmd_start";
    String PIC_START = "pic_start";


    String COMMAND_CONVERT = "CONVERT";
    String COMMAND_AUTO = "AUTO";
    String COMMAND_NEXT = "NEXT";
    String COMMAND_PRE = "PRE";
    String COMMAND_CLEAR_PIC = "COMMAND_CLEAR_PIC";
    String COMMAND_PHONE = "PHONE";




    // raspberry --> Android
    String CMD_IMG = "IMG_START";
    String CMD_CMD = "CMD_START";



}
