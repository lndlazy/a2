package com.pi.connectraspberry.util;

public interface MyCommand {


    // Android --> raspberry

    String CMD_START = "cmd_start";
    String PIC_START = "pic_start";
    String MD5_START = "md5_start";



    String mobileInfo = "mobile:";
    String configInfo = "config:";
    String createFolder = "createFolder:";
    String deleteFolder = "deleteFolder:";
    String getFolderImgs = "getFolderImgs:";
    String clearFolder = "clearFolder:";
    String COMMAND_CHECK_CONNECT = "checkConnect:";

    //String imageMd5 = "imageMd5:";


    String COMMAND_CONVERT = "CONVERT:";
    String COMMAND_AUTO = "AUTO";
    String COMMAND_NEXT = "NEXT";
    String COMMAND_PRE = "PRE";
    String COMMAND_CLEAR_PIC = "CLEAR_PIC";
    String CLEAR_DATA = "CLEAR_DATA";
    String Extraction_log = "Extraction_log";//提取日志
    String COMMAND_PHONE = "PHONE";
    String COMMAND_SET_CONFIG = "SET_CONFIG";


    // raspberry --> Android
    String CMD_IMG = "IMG_START";
    String CMD_CMD = "CMD_START";
    String CMD_TOA = "CMD_TOAST";
    String CMD_FOL = "CMD_F0LDE";
    String LOG_START = "LOG_START";
    //心跳
    String HEART = "CMD_HEART";
}
