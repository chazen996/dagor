import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Service {
    private int worload;    //工作量，该属性将影响提交到当前服务请求的执行时间，在提交某个请求时同步为其赋予实际的工作量
    private String serviceName; // 当前服务名
    private List<Service> dependencies = new ArrayList<>(); // 当前服务所依赖的服务
    public List<Request> requests = new ArrayList<>();  // 当前服务下提交的请求

    private int priorityLevel = Config.MAX_PRIORITY;    // 当前服务的截止准入优先级

    public Service(int worload, String serviceName) {
        this.worload = worload;
        this.serviceName = serviceName;
    }

    public void addDependency(Service dependency){
        dependencies.add(dependency);
    }

    private Request generateNewRequest(Request request, String serviceType){
        String preRequestName = request.getRequestName();
        Service service = DagorSystem.getServiceFromRequest(request);
        return request.copy(serviceType, preRequestName, service.worload);
    }

    public void handleRequest(Request request){
        request.setWorkload(worload);
        requests.add(request);
    }

    public boolean isFree(){
        return requests.size()==0;
    }

    public int calculateAverageWaitTime(){
        if(requests.size()==0){
            return 0;
        }
        Iterator<Request> it = requests.iterator();
        int sum = 0;
        while (it.hasNext()){
            Request request = it.next();
            sum += request.waitTime;
        }
        return sum/requests.size();
    }

    private void printTime(int currentTime){
        System.out.println("时刻: "+ currentTime +
                " ----------------------------------------------------------------------");
    }

    private void interruptRequest_tool(Request request, Service targetService, boolean isAccessControl){
        Service service = DagorSystem.getServiceFromRequest(request);
        if(isAccessControl){
            System.out.println("【准入控制】请求"+request.getRequestName()+"的优先级为："+
                    request.getPriority()+", 满足截止条件,已终止");
        }else{
            System.out.println("【运行超时】请求"+ request.getRequestName() +"已终止");
        }
        if(service!=targetService){
            service.requests.remove(request);
        }
        if(request.isIndependent()){
            return;
        }
        Iterator<Request> it = request.followRequests.iterator();
        while (it.hasNext()){
            Request temp = it.next();
            interruptRequest_tool(temp,targetService,isAccessControl);
        }
    }

    /* 当请求出现超时或违反准入控制原则时，递归终止整条服务调用链上的所有请求 */
    private void interruptRequest(Request request, boolean isAccessControl){
        Request priorRequest = request.priorRequest;
        Service targetService = DagorSystem.getServiceFromRequest(request);
        while (priorRequest!=null){
            request = priorRequest;
            priorRequest = priorRequest.priorRequest;
        }
        interruptRequest_tool(request,targetService, isAccessControl);
    }

    private void printCompletedRequest(Request request, int currentTime){
        if(request.status==RequestStatus.FINISHED){
            printTime(currentTime);
            System.out.println("【已完成】请求" + request.getRequestName() +"已完成, 执行时长："+
                    request.runTime + "s, 等待时长：" + request.waitTime + "s");
        }
    }

    /* 过载检测：判断当前服务是否过载，并根据情况返回true或false */
    private boolean overloadDetection(int currentTime){
        int averageWaitTime = calculateAverageWaitTime();
        if(averageWaitTime>=Config.AVERAGE_WAITIE_TIME){
            printTime(currentTime);
            System.out.println("*服务"+ serviceName + "已过载，队列平均等待时间：" +averageWaitTime +"s*");
            System.out.println("*开始执行准入控制，当前队列截止准入优先级："+priorityLevel+"*");
            return true;
        }
        return false;
    }

    /* 准入控制：当前服务出现过载时，实施准入控制机制，缓解服务的压力 */
    private void accessControl() {
        List<Request> toBeDeleted = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            Request request = requests.get(i);
            if(priorityLevel<request.getPriority()){
                toBeDeleted.add(request);
            }
        }
        for (Request request:toBeDeleted) {
            interruptRequest(request,true);
            requests.remove(request);
        }
    }

    public void run(int time){
        List<Request> toBeDeleted = new ArrayList<>();
        int maxRequestNum = Config.MAX_REQUEST_NUM;
        int maxPriority = Integer.MIN_VALUE;
        for (int i = 0; i < requests.size(); i++) {
            Request request = requests.get(i);
            if(request.status==RequestStatus.BLOCKING){
                if(i<maxRequestNum){
                    request.status = RequestStatus.READY;
                }else{
                    request.waitTime++;
                }
            }
            if(request.status==RequestStatus.READY){
                if(dependencies.size()!=0){
                    Iterator<Service> it = dependencies.iterator();
                    while (it.hasNext()){
                        Service service = it.next();
                        Request requestReplica = generateNewRequest(request, service.serviceName);//刚创建的任务status为BLOCKING
                        requestReplica.priorRequest = request;
                        request.addDependency(requestReplica);
                    }
                    request.status = RequestStatus.WAITING;
                }else{
                    request.status = RequestStatus.RUNNING;
                    request.startTime = time;
                }
                request.readyTime = time;
            }
            if(request.status==RequestStatus.WAITING){
                if(request.isIndependent()){
                    request.status = RequestStatus.RUNNING;
                    request.startTime = time;
                }
            }
            if(request.status==RequestStatus.RUNNING){
                int runTime = time - request.startTime;
                int executTime = time - request.readyTime;
                if(executTime>=Config.DEFAULT_TIMEOUT){//超时
                    toBeDeleted.add(request);
                    printTime(time);
                    interruptRequest(request,false);
                }else{
                    if(runTime>=request.getWorkload()){// 任务完成
                        Request priorRequest = request.priorRequest;
                        request.status = RequestStatus.FINISHED;
                        request.runTime = time - request.readyTime;
                        printCompletedRequest(request,time);
                        if(priorRequest!=null){
                            priorRequest.deleteDependency(request);
                        }
                        toBeDeleted.add(request);
                    }
                }
            }
            if(request.getPriority()>maxPriority){
                maxPriority = request.getPriority();
            }
        }
        if(toBeDeleted.size()!=0){
            for (Request request:toBeDeleted) {
                requests.remove(request);
            }
        }
        priorityLevel = maxPriority-1;
        if(overloadDetection(time)){
            accessControl();
        }
    }
}
