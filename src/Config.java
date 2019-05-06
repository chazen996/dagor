/*
*
    正常运行
    public static final double RATE_PRIORITY = 0.5;
    public static final int MAX_REQUEST_NUM = 2000;
    public static final int MAX_PRIORITY = 128;
    public static final int DEFAULT_TIMEOUT = 500;
    public static final int AVERAGE_WAITIE_TIME = 20;
    public static final int MAX_PRIORITY_BASE_NUMBER = 1000;

    过载检测及准入控制
    public static final double RATE_PRIORITY = 0.5;
    public static final int MAX_REQUEST_NUM = 2;
    public static final int MAX_PRIORITY = 128;
    public static final int DEFAULT_TIMEOUT = 500;
    public static final int AVERAGE_WAITIE_TIME = 20;
    public static final int MAX_PRIORITY_BASE_NUMBER = 1000;

    运行超时
    public static final double RATE_PRIORITY = 0.5;
    public static final int MAX_REQUEST_NUM = 2000;
    public static final int MAX_PRIORITY = 128;
    public static final int DEFAULT_TIMEOUT = 25;
    public static final int AVERAGE_WAITIE_TIME = 20;
    public static final int MAX_PRIORITY_BASE_NUMBER = 1000;
* */

import java.util.Random;

public class Config {
    public static final double RATE_PRIORITY = 0.5;
    public static final int MAX_REQUEST_NUM = 2000;
    public static final int MAX_PRIORITY = 128;
    public static final int DEFAULT_TIMEOUT = 500;
    public static final int AVERAGE_WAITIE_TIME = 20;
    public static final int MAX_PRIORITY_BASE_NUMBER = 1000;
    private static final Random random = new Random();

    /* 基于用户 ID 的第二层准入控制，在原文中通过以用户 ID 作为参数的哈希函数来动态生成用户优先级，
    并在入口函数中每小时更改一次哈希函数，这里加以简化了，就算是同一个用户也有很大的可能得到不同的用户优先级 */
    public static int calculateUserPriority(int userId){
        return (userId + 10*(random.nextInt(5)+1))%MAX_PRIORITY_BASE_NUMBER;
    }
}