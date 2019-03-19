package MINEPI_plus;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 *    This algorithm  is implemented for mining episodes in a conplex sequence based
 *    The calculation of frequency base on the head frequency
 *    the head frequency statisfy anti-monotone
 */
public class AlgoMINEPI_plus {


    // for statistics

    private long startTimestamp; // start time of the latest execution

    private long endTimestamp; // end time of the latest execution

    private int candidateCount = 0;


    // whether the timestamps need self increment as step of 1 for each transcation
    private boolean self_increment;


    // The  patterns that are found
    // (if the user want to keep them into memory)
    protected FreEpisodes freEpisodes = null;

    private List<Episode> F1;

    private int minSupport;

    private int maxWindow;

    int minimumLength = 0;

    int maximumLength = Integer.MAX_VALUE;


    /** the total execution time **/
    public double totalExecutionTime = 0;

    /** the maximumMemoryUsage **/
    public double maximumMemoryUsage = 0;



    AlgoMINEPI_plus(int minSupport, int maxWindow, boolean self_increment){
        this.minSupport = minSupport;
        this.maxWindow = maxWindow;
        this.self_increment = self_increment;
    }


    public FreEpisodes runAlgorithm(String input, String output) throws IOException {
        // reset maximum
        MemoryLogger.getInstance().reset();

        startTimestamp = System.currentTimeMillis();

        this.freEpisodes = new FreEpisodes();

        // scan the file to the menory (sequence) and determine the frequent 1-episodes in the level1
        scanDatabaseToDetermineFrequentSingleEpisode(input);



        for(int i = 0; i< F1.size(); i++){
            // the episode contains the boundlist.
            SerialJoins(F1.get(i),F1.get(i).getLastItem(),1);
        }




        // record end time
        endTimestamp = System.currentTimeMillis();
        // check the memory usage
        MemoryLogger.getInstance().checkMemory();

        if(output != null) {
            this.freEpisodes.out2file(output);
        }

        return freEpisodes;

    }

    public void SerialJoins(Episode alpha, int lastItem, int levelNum){

        for(int j = 0; j<F1.size(); j++){
            Episode fj = F1.get(j);

            if(fj.getLastItem() > lastItem){
                List<int[]> tempBoundlist = equalJoin(alpha,fj);
                if(tempBoundlist.size()>= minSupport){
                    Episode beta = alpha.iExtension(fj.getLastItem(),tempBoundlist);
                    this.freEpisodes.addFrequentEpisode(beta,levelNum);
                    SerialJoins(beta,fj.getLastItem(),levelNum);
                }
            }
            List<int[]> tempBoundlist = temporalJoin(alpha,fj);
            if(tempBoundlist.size() >= minSupport){
                Episode beta = alpha.sExtension(fj.getLastItem(),tempBoundlist);
                this.freEpisodes.addFrequentEpisode(beta,levelNum+1);
                SerialJoins(beta,fj.getLastItem(),levelNum+1);
            }
        }
    }

    public List<int[]> temporalJoin(Episode alpha, Episode singleEpisode){
        this.candidateCount++;

        List<int[]> tempBoundlist = new ArrayList<>();
        List<int[]> alphaBoundlistdlist = alpha.getBoundlist();
        List<int[]> singleBoundlist = singleEpisode.getBoundlist();
        for(int i = 0, j = 0; i<alphaBoundlistdlist.size() && j<singleBoundlist.size(); ){


//            [ts_i,te_i] and te_j  -> [ts_i,te_j] where te_j - ts_i < maxWindow and te_j > te_i
            if(singleBoundlist.get(j)[1] <= alphaBoundlistdlist.get(i)[1]){
                // the te_j are small than current te_i
                j++;
            }else if(singleBoundlist.get(j)[1] - alphaBoundlistdlist.get(i)[0]>=maxWindow){
                // the te_j are large than current te_i, but te_j - ts_i >= maxWindow
                i++;
            }else{
                // the te_j are large than current ts_i and te_j -ts_i < maxWindow
                tempBoundlist.add(new int[]{alphaBoundlistdlist.get(i)[0], singleBoundlist.get(j)[1]});
                // Each start point of alpha bound only can statisfy one within maxWindow
                // why not j++? because the j may combine with the next head of bound if they statisfy the conditions
                i++;
            }
        }
        return tempBoundlist;
    }


    public List<int[]> equalJoin(Episode alpha, Episode singleEpisode){
        this.candidateCount++;

        List<int[]> tempBoundlist = new ArrayList<>();
        List<int[]> alphaBoundlistdlist = alpha.getBoundlist();
        List<int[]> singleBoundlist = singleEpisode.getBoundlist();
        for(int i = 0, j = 0; i<alphaBoundlistdlist.size() && j<singleBoundlist.size(); ){

            if(alphaBoundlistdlist.get(i)[1] < singleBoundlist.get(j)[1]){
                // if current alphaBound less than current singleBound, then i++
                // for singleBouldlist  [1] and [0] are  equal
                i++;
            }else if(alphaBoundlistdlist.get(i)[1] == singleBoundlist.get(j)[1]){
                // if current alphaBound equal to the current singleBound, we add this bound

                tempBoundlist.add(alphaBoundlistdlist.get(i));
                i++;
                j++;
            }else{
                // if cuurent alphaBould large than the current singleBound , j++
                j++;
            }
        }
        return tempBoundlist;
    }

    public void scanDatabaseToDetermineFrequentSingleEpisode(String input) throws IOException {
        // read file
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;

        // key: 1-Episode,  value: bould list
        Map<Integer,List<int[]>> mapSingleEventCount = new HashMap<>();

        if(self_increment){
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

                for (String itemString : lineSplited) {
                    Integer item_name = Integer.parseInt(itemString);

                    List<int[]> bouldList = mapSingleEventCount.get(item_name);
                    if(bouldList == null){
                        bouldList = new ArrayList<>();
                        bouldList.add(new int[]{current_TID,current_TID});
                        mapSingleEventCount.put(item_name,bouldList);
                        candidateCount++;

                    }else{
                        bouldList.add(new int[]{current_TID,current_TID});
                        mapSingleEventCount.put(item_name,bouldList);
                    }

                }

            }
        }else {
            //// the timestamp exist in file
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

                    List<int[]> bouldList = mapSingleEventCount.get(item_name);
                    if(bouldList == null){
                        bouldList = new ArrayList<>();
                        bouldList.add(new int[]{current_TID,current_TID});
                        mapSingleEventCount.put(item_name,bouldList);
                        candidateCount++;
                    }else{
                        bouldList.add(new int[]{current_TID,current_TID});
                        mapSingleEventCount.put(item_name,bouldList);
                    }
                }
            }
        }


        freEpisodes = new FreEpisodes();
        F1 = new ArrayList<>();

        for(Map.Entry<Integer, List<int[]>> entry : mapSingleEventCount.entrySet()){
            List<int[]> bouldList = entry.getValue();
            if(bouldList.size()>= minSupport){
                // save frequent 1-episodes
                int[] symbol = new int[]{entry.getKey()};
                List<int[]> event = new ArrayList<int[]>(){{add(symbol);}};
                Episode episode = new Episode(event,bouldList);
                this.freEpisodes.addFrequentEpisode(episode,1);
                F1.add(episode);
            }
        }
    }


    /**
     * Print statistics about the algorithm execution to System.out.
     */
    public void printStats() {
        System.out.println("=============  MINEPI+ (head episode) - STATS =============");
        System.out.println(" Candidates count : " + candidateCount);
        System.out.println(" The algorithm stopped at size : " + freEpisodes.getTotalLevelNum());
        System.out.println(" Frequent itemsets count : " + this.freEpisodes.getFrequentEpisodesCount());
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println(" Total time ~ : " + (endTimestamp - startTimestamp) + " ms");
        System.out.println("===================================================");
    }

}
