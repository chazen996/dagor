public class Config {
    public static final double RATE_PRIORITY = 0.6;
    public static final int MAX_REQUEST_NUM = 2000;
    public static final int MAX_PRIORITY = 128;
    public static final int DEFAULT_TIMEOUT = 500;
    public static int calculateUserPriority(int userId){
        return userId%MAX_PRIORITY;
    }
}
