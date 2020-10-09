package com.cherrylq.zebra.ssi.config;

import lombok.Data;

@Data
public class DeviceConfig {

    /******************  COM口通讯参数   begin*******************/
    private String port;
    private int boundRate;
    private int parity;
    private int timeOut;
    private int stopBits;
    private int dataBits;
    private int flowControlMode;
    /******************  COM口通讯参数   begin*******************/


}