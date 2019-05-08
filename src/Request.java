import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Request {
    private String serviceType; // 所属服务类型
    private String requestName; // 请求名称，这个名称是自动生成的，规则为'所属服务@请求ID[-子服务@子请求ID...]'
    private User user;  // 提交请求的用户
    private int bussinessPriority;  // 业务优先级
    public int waitTime;    // 该请求在队列中的等待时间
    public RequestStatus status = RequestStatus.BLOCKING;   // 当前请求的状态，具体参看枚举类RequestStatus
    public List<Request> followRequests = new ArrayList<>();    // 后续的子请求
    private int workload;   // 实际请求的工作量，本质上由其所属的服务类型决定，并在一定范围内浮动
    public int readyTime = 0;   // 就绪时刻（当前请求变为READY状态的时刻，用于计算runTime）
    public int runTime = -1;    // 运行时间（从readyTime时刻开始到该请求结束总耗时）
    public int startTime = 0;   // 开始时刻（当前请求进入RUNNING状态的时刻，用于结合workload判断该请求是否已结束）
    public Request priorRequest = null; // 指向父级请求的引用

    private static int baseRequestId = 1;   // 用来递增生成所需请求的id，以保证各个请求不相同
    public int requestId;

    private int priority;

    public int getPriority(){
        return priority;
    }


    public int getWorkload() {
        return workload;
    }

    /* 根据所属的服务类型，声明当前请求的实际工作量 */
    public void setWorkload(int workload) {
        Random random = new Random();
        this.workload = workload + random.nextInt(5);
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public Request(String serviceType, User user, int bussinessPriority) {
        this.serviceType = serviceType;
        this.user = user;
        requestId = baseRequestId++;
        this.requestName = serviceType+"@"+requestId;

        if(bussinessPriority>=Config.MAX_PRIORITY_BASE_NUMBER) this.bussinessPriority = Config.MAX_PRIORITY_BASE_NUMBER-1;
        if(bussinessPriority<0) this.bussinessPriority = 0;
        this.bussinessPriority = bussinessPriority;

        calucatePriority();
        Service service = DagorSystem.getServiceFromRequest(this);
        service.handleRequest(this);
    }

    /* 根据业务优先级和用户优先级计算当前请求的优先级，计算结果为1-128的整数，数值越低则优先级越高 */
    private void calucatePriority(){
        double rate = Config.RATE_PRIORITY;
        int maxPriorityBaseNumber = Config.MAX_PRIORITY_BASE_NUMBER;
        int maxPriority = Config.MAX_PRIORITY;
        int userPriority = Config.calculateUserPriority(user.getUserId());
        int priority = (int)((rate*bussinessPriority + (1-rate)*userPriority)/maxPriorityBaseNumber*maxPriority);
        if(priority>=maxPriority) priority = maxPriority-1;
        if(priority<0) priority = 0;
        this.priority = maxPriority - priority;
    }

    public String getServiceType() {
        return serviceType;
    }

    public boolean isIndependent(){
        return followRequests.size()==0;
    }

    public void addDependency(Request request){
        followRequests.add(request);
    }

    public void deleteDependency(Request request){
        followRequests.remove(request);
    }

    public Request copy(String serviceType, String preRequestName, int workload){
        Request newRequest = new Request(serviceType, user, bussinessPriority);
        newRequest.setRequestName(preRequestName+"-"+serviceType+"@"+newRequest.requestId);
        newRequest.setWorkload(workload);
        return newRequest;
    }
}
