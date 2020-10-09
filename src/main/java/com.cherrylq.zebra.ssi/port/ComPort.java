package com.cherrylq.zebra.ssi.port;

import cn.hutool.core.util.HexUtil;
import com.cherrylq.zebra.ssi.config.DeviceConfig;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
public class ComPort extends BasePortAbstract implements SerialPortEventListener {

    /**
     * LED_OFF: 04 E8 04 00 FF 10
     */
    private static final String LED_OFF = "04 E8 04 00 FF 10";

    /**
     * 关闭扫描头
     * SCAN_DISABLE: 04 EA 04 00 FF 0E
     */
    private static final String SCAN_DISABLE = "04 EA 04 00 FF 0E";

    /**
     * 打开扫描头
     * SCAN_ENABLE: 04 E9 04 00 FF 0F
     */
    private static final String SCAN_ENABLE = "04 E9 04 00 FF 0F";

    /**
     * 开始识别扫描信息
     * START_DECODE: 04 E4 04 00 FF 14
     */
    private static final String START_DECODE = "04 E9 04 00 FF 0F";

    /**
     * 停止识别扫描信息
     * STOP_DECODE: 04 E5 04 00 FF 13
     */
    private static final String STOP_DECODE = "04 E9 04 00 FF 0F";

    /**
     * 初始化数据格式
     * Data As Is Beeps: 07 C6 04 00 02 EB 00 FE 42
     */
    private static final String DATA_AS_IS_BEEPS = "07 C6 04 00 02 EB 00 FE 42";

    /**
     * Data As Is:       07 C6 04 00 FF EB 00 FD 45
     */
    private static final String DATA_AS_IS = "07 C6 04 00 FF EB 00 FD 45";

    /**
     * <PREFIX> <DATA> <SUFFIX 1>: 07 C6 04 00 FF EB 05 FD 40
     */
    private static final String PREFIX_DATA_SUFFIX1 = "07 C6 04 00 FF EB 05 FD 40";

    /**
     * <PREFIX> <DATA> <SUFFIX 1> <SUFFIX 2>: 07 C6 04 00 FF EB 07 FD 3E
     */
    private static final String PREFIX_DATA_SUFFIX1_SUFFIX2 = "07 C6 04 00 FF EB 07 FD 3E";

    /**
     * Prefix 02      09 C6 04 00 FF 63 10 69 02 FD 50
     */
    private static final String PREFIX_02 = "09 C6 04 00 FF 63 10 69 02 FD 50";

    /**
     * Suffix 1 03 09 C6 04 00 FF 62 10 68 03 FD 51
     */
    private static final String SUFFIX1_03 = "09 C6 04 00 FF 62 10 68 03 FD 51";

    /**
     * Suffix 1 enter 09 C6 04 00 FF 62 10 68 0D FD 47
     */
    private static final String SUFFIX1_ENTER = "09 C6 04 00 FF 62 10 68 0D FD 47";

    /**
     * Suffix 2 03 09 C6 04 00 FF 64 10 6A 03 FD 4D
     */
    private static final String SUFFIX2_03 = "09 C6 04 00 FF 64 10 6A 03 FD 4D";

    /**
     * Presentation (Blink) 07 C6 04 00 FF 8A 07 FD 9F
     */
    private static final String PRESENTATION_BLINK = "07 C6 04 00 FF 8A 07 FD 9F";

    /**
     * Enable Interleaved 2 of 5          07 C6 04 00 FF 06 01 FE 29
     */
    private static final String ENABLE_INTERLEAVED_2_OF_5 = "07 C6 04 00 FF 06 01 FE 29";

    /**
     * I 2 of 5 - Any Length              09 C6 04 00 FF 16 00 17 00 FE 01
     */
    private static final String I_2_OF_5_ANY_LENGTH = "09 C6 04 00 FF 16 00 17 00 FE 01";

    /**
     * Enable Code 93                     07 C6 04 00 FF 09 01 FE 26
     */
    private static final String ENABLE_CODE_93 = "07 C6 04 00 FF 09 01 FE 26";

    /**
     * Enable Codabar                     07 C6 04 00 FF 07 01 FE 28
     */
    private static final String ENABLE_CODABAR = "07 C6 04 00 FF 07 01 FE 28";

    /**
     * Enable MSI						  07 C6 04 00 FF 0B 01 FE 24
     */
    private static final String ENABLE_MSI = "07 C6 04 00 FF 0B 01 FE 24";

    /**
     * Enable PDF417					  07 C6 04 00 FF 0F 01 FE 20
     */
    private static final String ENABLE_PDF417 = "07 C6 04 00 FF 0F 01 FE 20";

    /**
     * Enable MicroPDF417                 07 C6 04 00 FF E3 01 FD 4C
     */
    private static final String ENABLE_MICROPDF417 = "07 C6 04 00 FF E3 01 FD 4C";

    /**
     * Enable Data Matrix                 07 C6 04 00 FF F0 01 FD 3F
     */
    private static final String ENABLE_DATA_MATRIX = "07 C6 04 00 FF F0 01 FD 3F";

    /**
     * Inverse Autodetect                 07 C6 04 00 FF F1 02 FD 3D
     */
    private static final String INVERSE_AUTODETECT = "07 C6 04 00 FF F1 02 FD 3D";

    /**
     * Enable Maxicode                    08 C6 04 00 FF F0 26 01 FD 18
     */
    private static final String ENABLE_MAXICODE = "08 C6 04 00 FF F0 26 01 FD 18";

    @Setter
    private DeviceConfig config;
    /**
     * 拼接指令
     */
    private String command = "";

    public ComPort() {
    }

    @Getter
    private SerialPort serialPort;

    /**
     * 定时任务
     */
    private ScheduledThreadPoolExecutor schedulePool = new ScheduledThreadPoolExecutor(1);

    @Override
    public boolean initPort() {
        //打开COM口
        boolean ret;
        log.info("init port begin-------");
        try {
            ret = open();
            log.info("{} is opened.");
        } catch (SerialPortException e) {
            log.error("open the port[" + config.getPort() + "] fail. The message is:" + e.getMessage(), e);
            ret = false;
            try {
                serialPort.closePort();
            } catch (SerialPortException ex) {
                log.error(e.getMessage(), e);
            }
        }

        try {
            if (ret) {
                ret = addComListener();
            }
        } catch (SerialPortException e) {
            log.error("The port[" + config.getPort() + "] add Com Listener fail. The message is:" + e.getMessage());
            try {
                serialPort.closePort();
            } catch (SerialPortException ex) {
                log.error(e.getMessage(), e);
            }
            ret = false;
        }
        return ret == true ? isOpen.get() : ret;
    }

    protected boolean open() throws SerialPortException {
        boolean ret = false;
        if (config == null) {
            ret = false;
        } else {
//            portId = CommPortIdentifier.getPortIdentifier(config.getPort().toUpperCase());
//
//            // 端口已激活
//            if (portId.isCurrentlyOwned()) {
//                return true;
//            }

            //开启端口//./COM3
            serialPort = new SerialPort("//./" + config.getPort().toUpperCase());
            //(SerialPort) portId.open(config.getName() + "_port", config.getTimeOut());

            serialPort.openPort();

            // 设置端口参数
            serialPort.setParams(config.getBoundRate(), config.getDataBits(), config.getStopBits(), config.getParity(), false, false);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            if (serialPort.isOpened()) {
                log.info("打开串口：" + serialPort.getPortName());
                isOpen.set(true);
                return ret = true;
            }
        }
        return ret;
    }

    protected boolean addComListener() throws SerialPortException {
        boolean result = false;
        //注册COM事件回调
        serialPort.addEventListener(this);
        //激活接受时间
//        serialPort.enableReceiveTimeout(100);

        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_RXFLAG + SerialPort.MASK_CTS + SerialPort.MASK_DSR + SerialPort.MASK_BREAK + SerialPort.MASK_ERR
                + SerialPort.MASK_RING + SerialPort.MASK_RLSD + SerialPort.MASK_TXEMPTY;//Prepare mask
        serialPort.setEventsMask(mask);

        serialPort.setDTR(true);
        serialPort.setRTS(true);

        return result = true;
    }

    @Override
    public int sendCommand(byte[] command) {
        int ret = -9;
        if (command != null) {
            // 是否要求字节码增加 STX SEX. true(默认) = 添加 0203. false = 无 0203
        }

        InputStream is = new ByteArrayInputStream(command);
        try {
            serialPort.writeBytes(command);
        } catch (SerialPortException e) {
            ret = -9;
            log.error(e.getMessage(), e);
        }
        return ret;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        //单词读取的数据

        //处理事件
        if (event.isRXCHAR()) {//If data is available
            if (event.getEventValue() == 0) {
                return;
            }
            //Data available at the serial port. 数据在串口可用。
            //读取的内容存储在list中
            log.debug("DATA_AVAILABLE");

            byte[] data;
            try {
                data = serialPort.readBytes();
                // 异步执行 指令
                log.info("call back: {}", new String(data));
            } catch (SerialPortException e) {
                log.error("{} port read error.", e.getPortName());
                log.error(e.getMessage(), e);
                return;
            }

            log.debug("data received end");
        } else if (event.isCTS()) {//If CTS line has changed state
            //Clear to send.    清楚发送
            if (event.getEventValue() == 1) {//If line is ON
            } else {
            }
        } else if (event.isDSR()) {///If DSR line has changed state
            //Data set ready.   数据设备准备就绪
            if (event.getEventValue() == 1) {//If line is ON
            }
        } else if (event.isBREAK()) {
        } else if (event.isERR()) {
        } else if (event.isRING()) {
            if (event.getEventValue() == 1) {//If line is ON
            } else {
            }
        } else if (event.isRLSD()) {
            if (event.getEventValue() == 1) {//If line is ON
            } else {
            }
        } else if (event.isTXEMPTY()) {
            if (event.getEventValue() == 1) {//If line is ON
            } else {
            }
        } else {
            try {
                throw new IllegalStateException("Unexpected value: " + Arrays.toString(serialPort.getLinesStatus()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean closePort() {
        if (!isOpen.get()) {
            return true;
        }

        if (serialPort == null) {
            log.warn("serialPort is null");
            return false;
        }
//        try {
//            output.close();
//            input.close();
//        } catch (IOException e) {
//            log.error("The device [" + config.getName() + "] close port IOExeption.The error message is:" + e.getMessage());
//            return false;
//        }
        try {
            serialPort.closePort();
        } catch (SerialPortException e) {
            log.error(e.getMessage(), e);
        }
        isOpen.compareAndSet(true, false);
        return true;
    }

    @Override
    public boolean restartDevice() {
        // 关闭端口
        closePort();
        // 打开端口
        return initPort();
    }

    @Override
    public void run() {
        super.run();
    }

    /**
     * 销毁串口
     */
    @Override
    @PreDestroy
    public void shutdown() {
        closePort();
    }

    public static void main(String[] args) {
        DeviceConfig device = new DeviceConfig();
        device.setPort("COM65");
        device.setBoundRate(9600);
        device.setParity(0);
        device.setTimeOut(2000);
        device.setStopBits(1);
        device.setDataBits(8);

        final ComPort port = new ComPort();
        port.setConfig(device);
        port.initPort();

        port.sendCommand(HexUtil.decodeHex(DATA_AS_IS_BEEPS.replace(" ", "")));

    }
}
