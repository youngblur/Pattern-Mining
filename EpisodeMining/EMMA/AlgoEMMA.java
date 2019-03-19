package EMMA;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AlgoEMMA {
    // current level
    int K = 0;
    // for statistics

    private long startTimestamp; // start time of the latest execution

    private long endTimestamp; // end time of the latest execution

    private int candidateCount = 0;


    // whether the timestamps need self increment as step of 1 for each transcation
    private boolean self_increment;


    // The  patterns that are found
    // (if the user want to keep them into memory)
    protected FreEpisodes freEpisodes = null;

    // a sequence database to store all events
    //   int[] :  0-> item, 1-> tid
    private List<int[]> indexDB;

    private List<Itemset> freItemsets = null;

    // Encoding table
    private EncodingTable ET = null;


    private int minSupport;

    private int maxWindow;

    int minimumLength = 0;

    int maximumLength = Integer.MAX_VALUE;


    /** the total execution time **/
    public double totalExecutionTime = 0;

    /** the maximumMemoryUsage **/
    public double maximumMemoryUsage = 0;


    AlgoEMMA(int minSupport,int maxWindow, boolean self_increment){
        this.minSupport = minSupport;
        this.maxWindow = maxWindow;
        this.self_increment = self_increment;
    }


    public FreEpisodes runAlgorithm(String input, String output) throws IOException {
        // reset maximum
        MemoryLogger.getInstance().reset();


        startTimestamp = System.currentTimeMillis();

        this.indexDB = new ArrayList<>();

        // init the freItemset to contain the frequent itemset
        freItemsets = new ArrayList<>();

        // scan the file to the menory (sequence) and determine the frequent 1-items in the level1
        Set<Integer> frequentItemsName = scanDatabaseToDetermineFrequentItems(input);

        int frequentItemsCount = frequentItemsName.size();

        // transfrom the TDB into the indexDB and maintain the locations of all frequent 1-items in the index database
        scanDatabaseAgainToDetermineIndexDB(input,frequentItemsName);

        frequentItemsName = null;

        // obatin all frequent itemsets without candidates
        for(int i = 0; i< frequentItemsCount; i++){

            fimajoin(freItemsets.get(i), 1);
        }

        // Encode the database construction

        this.ET = new EncodingTable();
        freEpisodes = new FreEpisodes();


        for(Itemset itemset:freItemsets){
            List<int[]> events = new ArrayList<>();
            events.add(itemset.getName());
            Episode episode = new Episode(events,itemset.getSupport());
            freEpisodes.addFrequentEpisode(episode,1);
            candidateCount++;

            List<int[]> boundlist = new ArrayList<>();
            for(int location: itemset.getLocationList()){
                int[] bound = new int[]{indexDB.get(location)[1],indexDB.get(location)[1]};
                boundlist.add(bound);
            }
            ET.addEpisodeAndBoundlist(episode,boundlist);

        }
        this.indexDB = null;
        this.freItemsets = null;

        for(int i = 0; i<ET.getTableLength(); i++){
            // only do s-Extension
            SerialJoins(ET.getEpisodebyID(i),ET.getBoundlistByID(i),1);
        }

        this.ET = null;

        // record end time
        endTimestamp = System.currentTimeMillis();
        // check the memory usage
        MemoryLogger.getInstance().checkMemory();

        if(output != null) {
            this.freEpisodes.out2file(output);
        }

        return freEpisodes;
    }

    private void SerialJoins(Episode alpha,List<int[]> alphaBoundlist,int levelNum){
        for(int j = 0; j< ET.getTableLength(); j++){
            List<int[]> tempBoundlist = temporalJoin(alphaBoundlist,ET.getBoundlistByID(j));
            if(tempBoundlist.size() >= minSupport){
                Episode beta = alpha.sExtension(ET.getEpisodeNameByID(j),tempBoundlist.size());
                this.freEpisodes.addFrequentEpisode(beta,levelNum+1);

                SerialJoins(beta,tempBoundlist,levelNum+1);
            }
        }
    }

    private List<int[]> temporalJoin(List<int[]> alphaBoundlist, List<int[]> fjBoundlist){
        this.candidateCount++;

        List<int[]> tempBoundlist = new ArrayList<>();

        for(int i = 0, j = 0; i<alphaBoundlist.size() && j<fjBoundlist.size(); ){


//            [ts_i,te_i] and te_j  -> [ts_i,te_j] where te_j - ts_i < maxWindow and te_j > te_i
            if(fjBoundlist.get(j)[1] <= alphaBoundlist.get(i)[1]){
                // the te_j are small than current te_i
                j++;
            }else if(fjBoundlist.get(j)[1] - alphaBoundlist.get(i)[0]>=maxWindow){
                // the te_j are large than current te_i, but te_j - ts_i >= maxWindow
                i++;
            }else{
                // the te_j are large than current ts_i and te_j -ts_i < maxWindow
                tempBoundlist.add(new int[]{alphaBoundlist.get(i)[0], fjBoundlist.get(j)[1]});
                // Each start point of alpha bound only can statisfy one within maxWindow
                // why not j++? because the j may combine with the next head of bound if they statisfy the conditions
                i++;
            }
        }
        return tempBoundlist;
    }

    private void fimajoin(Itemset itemset ,int itemsetLength){
        Map<Integer,List<Integer>> mapCurrentItemsLocationList = new HashMap<>();
        List<Integer> LFI = genPListAndObtainFrequentItems(itemset.getLocationList(),mapCurrentItemsLocationList);
        for(int lf_j : LFI){
            int[] newFreItemset = new int[itemsetLength+1];
            System.arraycopy(itemset.getName(),0,newFreItemset,0,itemsetLength);
            newFreItemset[itemsetLength] = lf_j;

            // save it to the freItemsets
            Itemset newItemset = new Itemset(newFreItemset, mapCurrentItemsLocationList.get(lf_j));
            freItemsets.add(newItemset);

            fimajoin(newItemset,itemsetLength+1);
        }

    }

    private List<Integer> genPListAndObtainFrequentItems(List<Integer> locationList, Map<Integer, List<Integer>> mapCurrentItemsLocationList){
        List<Integer> frequentItems = new ArrayList<>();

        Map<Integer,Integer> mapItemCount = new HashMap<>();

        for(int i = 0; i< locationList.size(); i++){
            int index = locationList.get(i);
            int currentTid = indexDB.get(index)[1];

            // find following items that having same TID with currentTID
            index++;
            while(index<indexDB.size() && indexDB.get(index)[1] == currentTid){
                int item_name = indexDB.get(index)[0];
                Integer support = mapItemCount.get(item_name);
                List<Integer> currentItemLocationList = mapCurrentItemsLocationList.get(item_name);
                if(support == null){
                    mapItemCount.put(item_name,1);

                    currentItemLocationList = new ArrayList<>();
                    currentItemLocationList.add(index);
                    mapCurrentItemsLocationList.put(item_name,currentItemLocationList);

                }else{
                    mapItemCount.put(item_name,support+1);

                    currentItemLocationList.add(index);
                    mapCurrentItemsLocationList.put(item_name,currentItemLocationList);
                }
                index++;
            }
        }

        for(Map.Entry<Integer, Integer> entry : mapItemCount.entrySet()){
            if(entry.getValue() >= minSupport){
                frequentItems.add(entry.getKey());
            }else{
                // if the item is not frequent ,then delete its locationlist in the map
                mapCurrentItemsLocationList.remove(entry.getKey());
            }
        }

        return frequentItems;
    }

    private void scanDatabaseAgainToDetermineIndexDB(String input, Set<Integer> frequentItemsName) throws IOException {
        // read file
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;

        Map<Integer,List<Integer>> mapItemLocationList = new HashMap<>();

        int index = 0;
        if (self_increment) {
            int current_TID = 0;
            while (((line = reader.readLine()) != null)) {

                current_TID++;
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%'
                        || line.charAt(0) == '@') {
                    continue;
                }

                String[] lineSplited = line.split(" ");

                // 按照 ascll 码排序
                Arrays.sort(lineSplited);


                for (String itemString : lineSplited) {
                    Integer item_name = Integer.parseInt(itemString);

                    if(!frequentItemsName.contains(item_name)){
                        // if the item_name is not frequent item, skip it
                        continue;
                    }

                    List<Integer> locationList = mapItemLocationList.get(item_name);
                    if(locationList == null){
                        locationList = new ArrayList<>();
                        locationList.add(index);
                        mapItemLocationList.put(item_name,locationList);

                        indexDB.add(new int[]{item_name,current_TID});
                        index++;

                    }else if(locationList.get(locationList.size()-1) != index){
                        // maybe exist the same item in the one transaction
                        locationList.add(index);
                        mapItemLocationList.put(item_name,locationList);

                        indexDB.add(new int[]{item_name,current_TID});
                        index++;
                    }
                }
            }
        } else {
            // the timestamp exist in file
            int current_TID = 1;

            while (((line = reader.readLine()) != null)) {
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }

                String[] lineSplited = line.split("\\|");

                String[] lineItems = lineSplited[0].split(" ");
                current_TID = Integer.parseInt(lineSplited[1]);

                for (String itemString : lineItems) {
                    Integer item_name = Integer.parseInt(itemString);

                    if(!frequentItemsName.contains(item_name)){
                        // if the item_name is not frequent item, skip it
                        continue;
                    }

                    List<Integer> locationList = mapItemLocationList.get(item_name);
                    if(locationList == null){
                        locationList = new ArrayList<>();
                        locationList.add(index);
                        mapItemLocationList.put(item_name,locationList);

                        indexDB.add(new int[]{item_name,current_TID});
                        index++;

                    }else if(locationList.get(locationList.size()-1) != index){
                        // maybe exist the same item in the one transaction
                        locationList.add(index);
                        mapItemLocationList.put(item_name,locationList);

                        indexDB.add(new int[]{item_name,current_TID});
                        index++;
                    }
                }
            }
        }

        // to add the locationList to corresponding frequent item
        for(int i =0;i < freItemsets.size(); i++){
            int item_name = freItemsets.get(i).getName()[0];
            freItemsets.get(i).setLocationList(mapItemLocationList.get(item_name));
        }

        mapItemLocationList = null;
    }

    private Set<Integer> scanDatabaseToDetermineFrequentItems(String input) throws IOException {
        // read file
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;

        // key: item,  value: support
        Map<Integer,Integer> mapItemCount = new HashMap<>();

        if(self_increment){
//            int current_TID = 0;
            while (((line = reader.readLine()) != null)) {

//                current_TID++;
                // if the line is  a comment, is  empty or is a
                // kind of metadata
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%'
                        || line.charAt(0) == '@') {
                    continue;
                }

                String[] lineSplited = line.split(" ");

                for (String itemString : lineSplited) {
                    Integer item_name = Integer.parseInt(itemString);
                    Integer item_spport = mapItemCount.get(item_name);
                    if(item_spport == null){
                        mapItemCount.put(item_name,1);
                    }else{
                        mapItemCount.put(item_name,item_spport+1);
                    }
                }
            }
        }else {
            //// the timestamp exist in file
//            int current_TID = 1;

            while (((line = reader.readLine()) != null)) {
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }

                String[] lineSplited = line.split("\\|");

                String[] lineItems = lineSplited[0].split(" ");
//                current_TID = Integer.parseInt(lineSplited[1]);

                for (String itemString : lineItems) {
                    Integer item_name = Integer.parseInt(itemString);
                    Integer item_spport = mapItemCount.get(item_name);
                    if(item_spport == null){
                        mapItemCount.put(item_name,1);
                    }else{
                        mapItemCount.put(item_name,item_spport+1);
                    }
                }
            }
        }


        // record the frequent items' name , to filter non frequent items later.
        Set<Integer> frequentItemsName = new HashSet<>();

        // We obatin all frequent items;
        for(Map.Entry<Integer, Integer> entry : mapItemCount.entrySet()){
            if(entry.getValue() >= minSupport){
                Itemset item = new Itemset(new int[]{entry.getKey()});
                freItemsets.add(item);

                frequentItemsName.add(entry.getKey());
            }
        }

        mapItemCount = null;
        return frequentItemsName;
    }

    /**
     * Print statistics about the algorithm execution to System.out.
     */
    public void printStats() {
        System.out.println("=============  EMMA(head episode) - STATS =============");
        System.out.println(" Candidates count : " + candidateCount);
        System.out.println(" The algorithm stopped at size : " + freEpisodes.getTotalLevelNum());
        System.out.println(" Frequent itemsets count : " + this.freEpisodes.getFrequentEpisodesCount());
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println(" Total time ~ : " + (endTimestamp - startTimestamp) + " ms");
        System.out.println("===================================================");
    }
}

