package com.dohko.id;

/**
 * @description:
 * @author: luxiaohua
 * @date: 2019-10-23 10:56
 */
public class IdGenerator {

    /**
     * 开始时间截 (2019-01-01)
     */
    private final long twepoch = 1546272000000L;

    /**
     * 机器id所占的位数
     */
    private final long workerIdBits = 8L;

    /**
     * 数据中心id所占的位数
     */
    private final long dataCenterIdBits = 2L;

    /**
     * 支持的最大机器id，结果是255 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /**
     * 支持的最大数据标识id，结果是3
     */
    private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 12L;

    /**
     * 机器ID向左移12位
     */
    private final long workerIdShift = sequenceBits;

    /** 数据标识id向左移17位(12+5) */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间截向左移22位(5+5+12) */
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /** 工作机器ID(0~255) */
    private long workerId;

    /** 数据中心ID(0~3) */
    private long dataCenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;



    private IdGenerator() {
        // 固定一个数据中心
        long dataCenterId = 1L;

        // 获取workerId
        long workerId = IpUtils.getLastIp();

        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDataCenterId));
        }

        this.workerId = workerId;
        this.dataCenterId = dataCenterId;



    }


    private static IdGenerator idGenerator = new IdGenerator();

    public static IdGenerator getInstance() {
        return idGenerator;
    }


    /**
     * 生成id
     * @return
     */
    public static long generateId() {
        return IdGenerator.getInstance().nextId();
    }


    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = now();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 同一毫秒内序列达到最大值
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = untilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift)
                | (dataCenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long untilNextMillis(long lastTimestamp) {
        long timestamp = now();
        while (timestamp <= lastTimestamp) {
            timestamp = now();
        }
        return timestamp;
    }


    /**
     * 获取当前时间戳（毫秒）
     * @return
     */
    protected long now() {
        return System.currentTimeMillis();
    }



}
