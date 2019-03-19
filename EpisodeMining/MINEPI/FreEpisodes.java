package MINEPI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FreEpisodes {
    // Position i in "levels" contains the list of Episodes of size i */
    private final List<Level> levels = new ArrayList<Level>();
    /** the total number of episode **/
    private int episodeCount = 0;


    public FreEpisodes(){
        // we create an empty level 0 by default
        levels.add(new Level());

    }

    // to store 1-episodes, k = 1
    public void addFrequentEpisode(Episode episode, int K){
        while(levels.size() <= K){
            levels.add(new Level());
        }
        levels.get(K).addFreEpisode(episode);
        this.episodeCount++;
    }

    // to store k-episodes and its block_start
    public void addFrequentFpisodeAndBlockStart(Episode episode, int k, int block_start){
        while(levels.size() <= k){
            levels.add(new Level());
        }
        levels.get(k).addFreEpisodeAndBlockStart(episode,block_start);
        this.episodeCount++;
    }

    public void initFirstLevelBlockStart(){
        this.levels.get(1).init_firstLevel_block_start();
    }

    public void out2file(String output) throws IOException {
        // Create a string buffer
        BufferedWriter writer = new BufferedWriter(new FileWriter(output));
        StringBuilder buffer = new StringBuilder();
        int numLevel = 0;
        for(Level l : levels) {
            buffer.append("  L" + numLevel + " \r\n");
            for(Episode episode: l.getK_freEpisodes()){
                buffer.append(episode.toString());
                buffer.append("\r\n");
            }
            buffer.append("\r\n");
            numLevel++;
        }

        // write to file and create a new line
        writer.write(buffer.toString());
        writer.close();
    }

    public Candidates genCandidateByLevel(int K){
        if(levels.size()>K) {
            return this.levels.get(K).genCandidateEpisode(K);
        }
        return null;
    }

    public int getFrequentEpisodesCount(){
        return this.episodeCount;
    }

    public void printFrequentEpisodes(){
        int numLevel = 0;
        for(Level l : levels) {
            System.out.println("  L" + numLevel + " +\r\n");
            for(Episode episode: l.getK_freEpisodes()){
                System.out.println(episode.toString()+"\r\n");
            }
            System.out.println("\r\n");
            numLevel++;
        }
    }




}
