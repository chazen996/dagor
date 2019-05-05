import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class DagorSystem {

    private static Map<String,Service> serviceMap = new LinkedHashMap<>();

    public static Service getServiceFromRequest(Request request){
        String serviceName = request.getServiceType();
        return serviceMap.get(serviceName);
    }

    @Test
    private static void registService() throws Exception{
        InputStream in = DagorSystem.class.getClassLoader().getResourceAsStream("services.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String str = bufferedReader.readLine();
        while (str!=null){
            String[] args = str.split(" ");
            if(!serviceMap.containsKey(args[0])){
                Service service = new Service(Integer.valueOf(args[1]),args[0]);
                if(args.length==3){
                    String[] priorServiceNameList = args[2].split(",");
                    for (String priorServiceName:priorServiceNameList) {
                        Service priorService = serviceMap.get(priorServiceName);
                        priorService.addDependency(service);
                    }
                }
                serviceMap.put(args[0],service);
            }
            str = bufferedReader.readLine();
        }
        bufferedReader.close();
        inputStreamReader.close();
        in.close();
    }

    private static void submitRequest() throws Exception{
        InputStream in = DagorSystem.class.getClassLoader().getResourceAsStream("requests.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String str = bufferedReader.readLine();
        while (str!=null){
            String[] args = str.split(" ");
            User user = new User(args[0]);
            Request request = new Request(args[1],user,Integer.valueOf(args[2]));
            str = bufferedReader.readLine();
        }
        bufferedReader.close();
        inputStreamReader.close();
        in.close();
    }

    public static void main(String[] args) throws Exception {
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
