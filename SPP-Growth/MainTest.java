import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainTest {


    public static void main(String[] args) throws IOException {

        Map<String,Integer> largestTs = new HashMap<>();
        largestTs.put("T10I4D100K.dat.txt",100000);
        largestTs.put("chess.dat.txt",3196);
        largestTs.put("mushroom.dat.txt",8124);
        largestTs.put("kosarak_0.2.txt",198001);
        largestTs.put("kosarak_0.4.txt",396001);
        largestTs.put("kosarak_0.6.txt",594002);
        largestTs.put("kosarak_0.8.txt",792002);
        largestTs.put("kosarak.dat.txt",990002);
        largestTs.put("OnlineRetail.txt",541909);


        String root = "E:\\data\\paper\\PFTI\\";
        String file = "OnlineRetail_hour.txt";

//        int maxPer = (int)(largestTs.get(file)*0.002);
//        int minSup = (int)(largestTs.get(file)*0.005);
//        int maxLa = (int)(largestTs.get(file)*0.001);

//        these are thresholds of online_minute.txt

        // OnlineRetail_day.txt  total:374 day , actually 305 day
        // OnlineRetail_day.txt  total:8957hour, actually 2975hour
        int maxPer = 7;
        int minSup = 2000;
        int maxLa = 0;


        // self-growth = flase only for online_minute.txt , others are true
        AlgoSPPgrowth algo = new AlgoSPPgrowth(maxPer,minSup,maxLa,true);
        algo.runAlgorithm(root+file,".\\data\\out\\no_"+file);
        algo.printStats();
    }
}
