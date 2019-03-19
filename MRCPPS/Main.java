package MRCPPS;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        String root = "E:\\data\\MRCPPS\\";

//        String file = "example.txt";
        // example   FIFA   Bible  T5I1KD100K T10I1KD100K Leviathan
        String file = "example.txt";

        //  the length of transaction in Bible and FIFA is aways 1, so it cannot find the larger itemset,
        // and the minBond measure doesnot work.
        // hence for FIFA and Bible, we group the transaction( group 5 transaction for FIFA)

        Map<String,Boolean> file2needGroup = new HashMap<>();
        file2needGroup.put("FIFA.txt",true);
        file2needGroup.put("Bible.txt",true);
        file2needGroup.put("T10I1KD100K.txt",false);
        file2needGroup.put("Leviathan.txt",true);
        file2needGroup.put("example.txt",false);

        Map<String,Integer> file2groupNum = new HashMap<>();
        file2groupNum.put("FIFA.txt",3);
        file2groupNum.put("Bible.txt",3);
        file2groupNum.put("T10I1KD100K.txt",5);
        file2groupNum.put("Leviathan.txt",10);
        file2groupNum.put("example.txt",1);

        double minBond = 0.6;
        double minRa = 0.6;
        double maxStd = 1;

        int maxSup =2;
        boolean useLemma2 = true;
        boolean showDetail = true;
        boolean needGroup = file2needGroup.get(file);
        int groupNum = file2groupNum.get(file);

        Date day=new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(day));
        System.out.println(file+" --> "+ "maxSup: "+maxSup+"  minRa: "+minRa+"  maxStd: "+maxStd+"  minBond: "+minBond);


        AlgoMRCPPS MRCPPS = new AlgoMRCPPS(maxSup,maxStd,minBond,minRa,useLemma2,showDetail,needGroup,groupNum);
        MRCPPS.runAlgorithm(root+file,  root+"out\\"+file);
        MRCPPS.printStats();

    }

}
