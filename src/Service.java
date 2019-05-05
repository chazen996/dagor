import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Service {
    private int worload;
    private String serviceName;
    private List<Service> dependencies = new ArrayList<>();
    public List<Request> requests = new ArrayList<>();

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

    private void interruptRequest_tool(Request request, Service targetService){
        Service service = DagorSystem.getServiceFromRequest(request);
        System.out.println("运行超时，请求"+ request.getRequestName() +"已终止");
        if(service!=targetService){
            service.requests.remove(request);
        }
        if(request.isIndependent()){
            return;
        }
        Iterator<Request> it = request.followRequests.iterator();
        while (it.hasNext()){
            Request temp = it.next();
            interruptRequest_tool(temp,targetService);
        }
    }

    private void interruptRequest(Request request){
        Request priorRequest = request.priorRequest;
        Service targetService = DagorSystem.getServiceFromRequest(request);
        while (priorRequest!=null){
            request = priorRequest;
            priorRequest = priorRequest.priorRequest;
        }
        interruptRequest_tool(request,targetService);
    }

    private void printCompletedRequest(Request request, int currentTime){
        if(request.status==RequestStatus.FINISHED){
            System.out.println("时刻: "+currentTime);
            System.out.println("请求" + request.getRequestName() +"已完成, 执行时长："+
                    request.runTime + "s, 等待时长：" + request.waitTime + "s");
        }
    }

    private boolean overloadDetection(){
        int averageWaitTime = calculateAverageWaitTime();
        if(averageWaitTime>=Config.AVERAGE_WAITIE_TIME){
            System.out.println("*服务"+ serviceName + "已过载，队列平均等待时间：" +averageWaitTime +"*");
            return true;
        }
        return false;
    }

    public void run(int time){
        List<Request> toBeDeleted = new ArrayList<>();
        int maxRequestNum = Config.MAX_REQUEST_NUM;
        if(overloadDetection()){

        }
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
                    interruptRequest(request);
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
        }
        if(toBeDeleted.size()!=0){
            for (Request request:toBeDeleted) {
                requests.remove(request);
            }
        }
    }
}
