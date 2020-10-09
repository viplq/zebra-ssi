package com.cherrylq.zebra.ssi.port;


import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO
 *
 * @author Liuqian
 * @version V1.0
 * @className BasePortAbstract
 * @date 2020/10/9 11:27
 */
@Slf4j
public abstract class BasePortAbstract extends Thread implements IPort {

    protected AtomicBoolean isOpen = new AtomicBoolean(false);

    /**
     * 端口输入流
     */
    protected InputStream input;

    private long lockTime;

    protected volatile AtomicBoolean restOpen = new AtomicBoolean(true);

    protected Lock lock = new ReentrantLock();

    private final ExecutorService executor = new ThreadPoolExecutor(0, 10,
            30L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public BasePortAbstract() {

    }

    /**
     * 实现重新开设备的业务
     *
     * @return
     */
    public abstract boolean restartDevice();

    /**
     * 重启设备， 已经控制重复启动. 在设备断开位置，调用 restart()
     */
    @Override
    public void restart() {
        boolean tryLock = false;
        // 剔除等待任务量
        if (!restOpen.get()) {
            return;
        }

        try {
            // 锁等待
            if (!(tryLock = lock.tryLock(1, TimeUnit.MILLISECONDS))) {
                return;
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            return;
        }

        // 变更状态，忽略并发的请求
        restOpen.compareAndSet(true, false);

        isOpen.compareAndSet(true, false);

        try {

            // 控制并发漏掉任务，5秒之内不进行重启
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lockTime) <= 5000) {
                return;
            }

            // 重新启动 串口
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        while (!isOpen.get()) {
                            // 设备启动后，停止轮询
                            if (restartDevice()) {
                                // 等待设备启动完成
                                TimeUnit.MILLISECONDS.sleep(RESTART_SECONDS);
                                break;
                            }

                            TimeUnit.MILLISECONDS.sleep(1000L);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (isOpen.get()) {
                            // 变更状态，忽略并发的请求
                            restOpen.compareAndSet(false, true);
                        }
                    }
                }
            });
        } finally {
            lockTime = System.currentTimeMillis();
            if (tryLock) {
                lock.unlock();
            }
        }
    }

}
