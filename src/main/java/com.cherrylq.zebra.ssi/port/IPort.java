package com.cherrylq.zebra.ssi.port;


public interface IPort {

    static int RESTART_SECONDS = 5;

    /**
     * 根据配置信息开启端口
     *
     * @return boolean
     */
    boolean initPort();

    /**
     * 以数组形式向端口发送信息
     *
     * @param commond
     * @return code
     */
    int sendCommand(byte[] commond);

    /**
     * 关闭端口
     *
     * @return boolean
     */
    boolean closePort();

    /**
     * 重启设备， 已经控制重复启动
     */
    void restart();

    /**
     * 销毁串口
     */
    void shutdown();
}
