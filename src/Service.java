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

    public int calculateAverageWaitTime(){
        Iterator<Request> it = requests.iterator();
        int sum = 0;
        while (it.hasNext()){
            Request request = it.next();
            sum += request.waitTime;
        }
        return sum/requests.size();
    }

    private void interruptRequest_tool(Request request){
        Service service = DagorSystem.getServiceFromRequest(request);
        service.requests.remove(request);
        if(request.isIndependent()){
            return;
        }
        Iterator<Request> it = request.followRequests.iterator();
        while (it.hasNext()){
            Request temp = it.next();
            interruptRequest_tool(temp);
        }
    }

    private void interruptRequest(Request request){
        Request priorRequest = request.priorRequest;
        while (priorRequest!=null){
            request = priorRequest;
            priorRequest = priorRequest.priorRequest;
        }
        interruptRequest_tool(request);
    }

    public void run(int time){
        int maxRequestNum = Config.MAX_REQUEST_NUM;
        for (int i = 0; i < requests.size(); i++) {
            Request request = requests.get(i);
            if(request.status==RequestStatus.BLOCKING){
                if(i<maxRequestNum){
                    request.status = RequestStatus.READY;
                }else{
                    request.waitTime++;
                }
            }else if(request.status==RequestStatus.READY){
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
                }
                request.readyTime = time;
            }else if(request.status==RequestStatus.WAITING){
                if(request.isIndependent()){
                    request.status = RequestStatus.RUNNING;
                    request.startTime = time;
                }
            }else if(request.status==RequestStatus.RUNNING){
                int runTime = time - request.startTime;
                if(runTime>Config.DEFAULT_TIMEOUT){//超时
                    interruptRequest(request);
                }else{
                    if(runTime>=request.getWorkload()){// 任务完成
                        Request priorRequest = request.priorRequest;
                        request.status = RequestStatus.FINISHED;
                        request.runTime = time - request.readyTime;
                        System.out.println(request.getRequestName() +" ---> "+ request.runTime);
                        if(priorRequest!=null){
                            priorRequest.deleteDependency(request);
                        }
                        requests.remove(i);
                        i--;
                    }
                }
            }
        }
    }

    public boolean isFree(){
        return requests.size()==0;
    }

}
