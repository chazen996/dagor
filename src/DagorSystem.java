import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class DagorSystem {

    /* 使用Map模拟服务中心，所有的服务都在此Map中注册 */
    private static Map<String,Service> serviceMap = new LinkedHashMap<>();

    /* 通过Request获取与其对应的服务 */
    public static Service getServiceFromRequest(Request request){
        String serviceName = request.getServiceType();
        return serviceMap.get(serviceName);
    }

    /* 注册服务，通过读取services.txt文件动态注册服务，书写规则为'入口服务 工作量 [跳跃服务(所依赖的服务)]' */
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

    /* 提交请求, 通过读取requests.txt文件动态生成请求，书写规则为'用户名 服务名 业务优先级'  */
    private static void submitRequest() throws Exception{
        InputStream in = DagorSystem.class.getClassLoader().getResourceAsStream("requests.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String str = bufferedReader.readLine();
        while (str!=null){
            String[] args = str.split(" ");
            User user = new User(args[0]);
            new Request(args[1],user,Integer.valueOf(args[2]));
            str = bufferedReader.readLine();
        }
        bufferedReader.close();
        inputStreamReader.close();
        in.close();
    }

    /* 入口函数，模拟系统从0时刻开始运行直到所有的请求都被完成 */
    public static void main(String[] args) throws Exception {
        registService();
        submitRequest();
        int time = 0;
        boolean done;
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
