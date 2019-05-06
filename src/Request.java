import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Request {
    private String serviceType;
    private String requestName;
    private User user;
    private int bussinessPriority;
    public int waitTime;
    public RequestStatus status = RequestStatus.BLOCKING;
    public List<Request> followRequests = new ArrayList<>();
    private int workload;
    public int readyTime = 0;
    public int runTime = -1;
    public int startTime = 0;
    public Request priorRequest = null;

    private static int baseRequestId = 1;
    public int requestId;

    private int priority;

    public int getPriority(){
        return priority;
    }


    public int getWorkload() {
        return workload;
    }

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

    private void calucatePriority(){
        double rate = Config.RATE_PRIORITY;
        int maxPriorityBaseNumber = Config.MAX_PRIORITY_BASE_NUMBER;
        int maxPriority = Config.MAX_PRIORITY;
        int userPriority = Config.calculateUserPriority(user.getUserId());
        int priority = (int)((rate*bussinessPriority + (1-rate)*userPriority)/maxPriorityBaseNumber*128);
        if(priority>=maxPriority) priority = maxPriority-1;
        if(priority<0) priority = 0;
        this.priority = priority+1;
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
