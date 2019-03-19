package MINEPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main {

    public static void main(String[] args) throws IOException {
        AlgoMINEPI MINEPI = new AlgoMINEPI(2,10,true);
        FreEpisodes freEpisodes = MINEPI.runAlgorithm("data/example.txt","data/out_example.txt");
        MINEPI.printStats();
        getFrequentKepisodes(MINEPI.sequence,2,11,new int[]{1,2,3,4});
    }


    public static void getFrequentKepisodes(List<Event> sequence, int minSupport,int maxWindow,int [] episode){


        List<FSA4MINEPI> FSAList = new ArrayList<>();
        FSAList.add(new FSA4MINEPI(episode));

        List<List<Integer>> timeList = new ArrayList<>();
        timeList.add(new ArrayList<>());

        int support = 0;

        for(Event eventSet : sequence){
            int currentFSAsize = FSAList.size();
            for(int j = currentFSAsize-1; j>=0; j--){
                if(eventSet.contains(FSAList.get(j).waiting4Event())){
                    FSAList.get(j).transit();
                    timeList.get(j).add(eventSet.time);
                    if(j == currentFSAsize-1){
                        FSAList.add(new FSA4MINEPI(episode));
                        timeList.add(new ArrayList<>());
                    }
                    if(FSAList.get(j).isEnd()){
                        // if current FSA reach end state, then support ++ and remove it.
                        if(timeList.get(j).get(3)-timeList.get(j).get(0)+1<maxWindow) {
                            support++;
                            System.out.println("accept");
                        }else{
                            System.out.println("unaccept");
                        }
                        FSAList.remove(j);
                        System.out.println(timeList.get(j).toString());
                        timeList.remove(j);

                    }
                    if(j>=1 && FSAList.get(j).isSame(FSAList.get(j-1)) && !eventSet.contains(FSAList.get(j-1).waiting4Event()) ){
                        // if current FSA's wait equals to previous FSA's wait, then we delete previous FSA in the FSAList
                        FSAList.remove(j-1);
                        timeList.remove(j-1);
                        j--;  // to index the current FSA in the FSAList
                    }
                }
            }
        }
        if(support >= minSupport){
            System.out.println(support);
        }

    }
}
