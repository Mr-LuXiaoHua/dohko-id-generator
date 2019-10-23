package com.dohko.id;

import java.net.InetAddress;

/**
 * @description:
 * @author: luxiaohua
 * @date: 2019-10-23 10:52
 */
public class IpUtils {

    /**
     * 获取ip最后8位的值, 如：192.168.0.116 则取 116
     * @return
     */
    public static int getLastIp() {
        int ip = 0;
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String host = inetAddress.getHostAddress();
            String[] hostArr = host.split("\\.");
            ip= Integer.valueOf(hostArr[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }

}
