public enum RequestStatus {
    BLOCKING("阻塞中","任务阻塞中"),
    READY("已就绪","任务已就绪"),
    WAITING("等待中","任务正在等待中"),
    RUNNING("运行中","任务运行中"),
    FINISHED("已完成","任务已完成");
    final String STATUS_NAME;
    final String STATUS_DESC;

    RequestStatus(String STATUS_NAME, String STATUS_DESC) {
        this.STATUS_NAME = STATUS_NAME;
        this.STATUS_DESC = STATUS_DESC;
    }
}
