import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class DagorSystem {

    private static Map<String,Service> serviceMap = new LinkedHashMap<>();

    public static Service getServiceFromRequest(Request request){
        String serviceName = request.getServiceType();
        return serviceMap.get(serviceName);
    }

    private static void registService(){
        Service logInService = new Service(10,"LogIn");
        Service messageService = new Service(15,"Message");
        Service accountService = new Service(12,"Account");
        logInService.addDependency(accountService);
        serviceMap.put("LogIn",logInService);
        serviceMap.put("Message",messageService);
        serviceMap.put("Account",accountService);
    }

    private static void submitRequest(){
        Random random = new Random();
        Request logInRequest = new Request("LogIn",
                new User("chazen1",10000+random.nextInt(1000)),500);
        Request messageRequest = new Request("Message",
                new User("chazen2",10000+random.nextInt(1000)),600);
    }

    public static void main(String[] args) {
        registService();
        submitRequest();
        int time = 0;
        boolean done = true;
        while (true){
            done = true;
            for (String key:serviceMap.keySet()) {
                Service service = serviceMap.get(key);
                service.run(time);
                if(!service.isFree()){
                    done = false;
                }
            }
            time++;
            if(done){
                break;
            }
        }
    }
}
