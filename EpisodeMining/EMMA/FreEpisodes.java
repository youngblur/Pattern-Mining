package EMMA;


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

//        System.out.println(episode.toString());
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

    public int getTotalLevelNum(){
        return this.levels.size();
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
