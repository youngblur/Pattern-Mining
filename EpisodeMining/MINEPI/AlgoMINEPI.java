package MINEPI;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *    This algorithm  only for mining the serial episode based on minimal occurrences(windows)
 *    because it do not satisfy the anti-monotony,
 *    but the continuous subepisode satisfy the anti-monotony,
 *    so,
 */
public class AlgoMINEPI {

    // current level
    int K = 0;
    // for statistics

    private long startTimestamp; // start time of the latest execution

    private long endTimestamp; // end time of the latest execution

    private int candidateCount = 0;

    // a sequence database to store all events
    public List<Event> sequence;

    // whether the timestamps need self increment as step of 1 for each transcation
    private boolean self_increment;


    // The  patterns that are found
    // (if the user want to keep them into memory)
    protected FreEpisodes freEpisodes = null;

    private int minSupport;

    private int maxWindow;

    int minimumLength = 0;

    int maximumLength = Integer.MAX_VALUE;


    /** the total execution time **/
    public double totalExecutionTime = 0;

    /** the maximumMemoryUsage **/
    public double maximumMemoryUsage = 0;


    AlgoMINEPI(int minSupport,int maxWindow, boolean self_increment){
        this.minSupport = minSupport;
        this.maxWindow = maxWindow;
        this.self_increment = self_increment;
    }


    public FreEpisodes runAlgorithm(String input, String output) throws IOException {
        // reset maximum
        MemoryLogger.getInstance().reset();


        startTimestamp = System.currentTimeMillis();

        sequence = new ArrayList<>();

        freEpisodes = new FreEpisodes();

        // scan the file to the menory (sequence) and determine the frequent 1-episodes in the level1

        scanDatabaseToDetermineFrequentSingleEpisode(input);

        // using level 1 to generate candidates of size 2
        this.K++;

        // generate candidates of having size 2
        Candidates candidates = freEpisodes.genCandidateByLevel(K);


        while(candidates != null && !candidates.isEmpty()){
            candidateCount+=candidates.getCandidateCount();
            K++;
            candidates.getFrequentKepisodes(this.sequence,this.minSupport,this.maxWindow,this.freEpisodes);
            candidates = freEpisodes.genCandidateByLevel(K);
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

    public void scanDatabaseToDetermineFrequentSingleEpisode(String input) throws IOException {
        // read file
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;

        // key: 1-Episode,  value: support
        Map<Integer,Integer> mapSingleEventCount = new HashMap<>();

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
                Event simulEvent = new Event();
                String[] lineSplited = line.split(" ");

                for (String itemString : lineSplited) {
                    Integer item_name = Integer.parseInt(itemString);
                    simulEvent.addEvent(item_name);

                    Integer count = mapSingleEventCount.get(item_name);
                    if(count == null){
                        mapSingleEventCount.put(item_name,1);
                        candidateCount++;
                    }else{
                        mapSingleEventCount.put(item_name,++count);
                    }

                }

                simulEvent.setTime(current_TID);

                this.sequence.add(simulEvent);
            }
        }else {
            //// the timestamp exist in file
            int current_TID = 1;

            while (((line = reader.readLine()) != null)) {
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@') {
                    continue;
                }
                Event simulEvent = new Event();

                String[] lineSplited = line.split("\\|");

                String[] lineItems = lineSplited[0].split(" ");
                for (String itemString : lineItems) {
                    Integer item_name = Integer.parseInt(itemString);
                    simulEvent.addEvent(item_name);

                    Integer count = mapSingleEventCount.get(item_name);
                    if(count == null){
                        mapSingleEventCount.put(item_name,1);
                        candidateCount++;
                    }else{
                        mapSingleEventCount.put(item_name,++count);
                    }
                }
                current_TID = Integer.parseInt(lineSplited[1]);
                simulEvent.setTime(current_TID);

                sequence.add(simulEvent);
            }
        }


        for(Map.Entry<Integer, Integer> entry : mapSingleEventCount.entrySet()){
            if(entry.getValue() >= minSupport){
                // save frequent 1-episodes
                Episode episode = new Episode(new int[]{entry.getKey()},entry.getValue());
                this.freEpisodes.addFrequentEpisode(episode,1);
            }
        }

        freEpisodes.initFirstLevelBlockStart();
    }

    /**
     * Print statistics about the algorithm execution to System.out.
     */
    public void printStats() {
        System.out.println("=============  MINEPI - STATS =============");
        System.out.println(" Candidates count : " + candidateCount);
        System.out.println(" The algorithm stopped at size : " + K);
        System.out.println(" Frequent itemsets count : " + this.freEpisodes.getFrequentEpisodesCount());
        System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
        System.out.println(" Total time ~ : " + (endTimestamp - startTimestamp) + " ms");
        System.out.println("===================================================");
    }

}
