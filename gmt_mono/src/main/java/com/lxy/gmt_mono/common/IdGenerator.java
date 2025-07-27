package com.lxy.gmt_mono.common;

import cn.hutool.core.lang.Snowflake;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;

/**
 * 雪花算法ID生成器
 * 使用Java原生API获取MAC地址，兼容性更强
 */
@Component
public class IdGenerator {

    private long workerId;
    private long datacenterId;
    private Snowflake snowflake;

    @PostConstruct
    public void init() {
        try {
            // 1. 获取本机的MAC地址
            byte[] mac = getLocalMacAddress();

            // 2. 使用MAC地址生成workerId和datacenterId
            // & 0x1F (十六进制的31) 等价于 & 31，用于取低5位 (0-31)
            datacenterId = (long) mac[mac.length - 2] & 0x1F;
            workerId = (long) mac[mac.length - 1] & 0x1F;

        } catch (Exception e) {
            // 如果获取MAC地址失败，打印警告并使用随机值
            System.err.println("获取机器 workerId/datacenterId 失败，将使用随机值: " + e.getMessage());
            Random random = new Random();
            workerId = random.nextInt(32);
            datacenterId = random.nextInt(32);
        }

        // 3. 【核心】使用Hutool的Snowflake构造函数，现在我们传入的是绝对合法的ID
        this.snowflake = new Snowflake(workerId, datacenterId);

    }

    /**
     * 【核心方法】使用Java原生API获取本机MAC地址
     * @return MAC地址的字节数组
     * @throws SocketException 获取网络接口失败
     * @throws UnknownHostException 获取本机IP地址失败
     * @throws IllegalStateException 找不到合适的网络接口
     */
    private byte[] getLocalMacAddress() throws SocketException, UnknownHostException, IllegalStateException {
        // a. 获取本机IP地址
        InetAddress localhost = InetAddress.getLocalHost();

        // b. 根据IP地址获取对应的网络接口
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);

        // c. 如果通过IP找不到（比如在某些Docker或虚拟机环境中），则遍历所有网络接口
        if (networkInterface == null) {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nic = interfaces.nextElement();
                // 我们寻找一个“正在运行”且“不是回环接口”的物理网卡
                if (nic.isUp() && !nic.isLoopback()) {
                    networkInterface = nic;
                    break;
                }
            }
        }

        // 如果还是找不到，就抛出异常
        if (networkInterface == null) {
            throw new IllegalStateException("找不到合适的网络接口");
        }

        // d. 从找到的网络接口中获取MAC地址
        byte[] mac = networkInterface.getHardwareAddress();
        if (mac == null || mac.length < 2) {
            throw new IllegalStateException("不能从选择的网络接口中获得硬件地址");
        }

        return mac;
    }
    /**
     * 获取下一个ID
     *
     * @return long形式的ID
     */
    public long nextId() {
        return snowflake.nextId();
    }

    /**
     * 获取下一个ID
     *
     * @return String形式的ID
     */
    public String nextIdStr() {
        return snowflake.nextIdStr();
    }
}
