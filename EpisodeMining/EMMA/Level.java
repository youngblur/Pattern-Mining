package EMMA;


import java.util.ArrayList;
import java.util.List;

public class Level {

    List<Episode> k_freEpisodes;

    int episodeCount = 0;


    Level(){
        this.k_freEpisodes = new ArrayList<>();
    }

    // to store 1-episode
    public void addFreEpisode(Episode episode){
        this.k_freEpisodes.add(episode);
        episodeCount++;
    }


    public int getEpisodeCount(){
        return this.episodeCount;
    }

    public List<Episode> getK_freEpisodes(){
        return this.k_freEpisodes;
    }





}
