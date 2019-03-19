package MINEPI;

import java.util.ArrayList;
import java.util.List;

public class Level {
    // the list of the start position (index) of the block of having same n-1 prefix in the a level
    List<Integer> block_start;

    List<Episode> k_freEpisodes;

    int episodeCount = 0;


    Level(){
        this.block_start = new ArrayList<>();
        this.k_freEpisodes = new ArrayList<>();
    }

    // to store 1-episode
    public void addFreEpisode(Episode episode){
        this.k_freEpisodes.add(episode);
        episodeCount++;
    }

    public void addFreEpisodeAndBlockStart(Episode episode, int index){
        this.k_freEpisodes.add(episode);
        this.block_start.add(index);
        episodeCount++;
    }

    // to init the block_start of first level ( for 1-episodes)
    public void init_firstLevel_block_start(){
        for(int i = 0; i<this.k_freEpisodes.size(); i++){
            this.block_start.add(0);
        }
    }

    public int getEpisodeCount(){
        return this.episodeCount;
    }

    public List<Episode> getK_freEpisodes(){
        return this.k_freEpisodes;
    }

    // generate next K+1 candidate episodes from this K-level
    //  The candidateLength = K
    public Candidates genCandidateEpisode(int numLevel){

        Candidates candidates = new Candidates(numLevel+1);
        // This k only for record candidates.
        int k = -1;
        for(int i = 0; i<k_freEpisodes.size(); i++){
            // this i th  will become the suffix(include the last n-1 event)

            for(int j = 0; j<k_freEpisodes.size(); j++){
                // to compare every episode's prefix
                if(k_freEpisodes.get(i).compare2prefix(k_freEpisodes.get(j))){
                    // If the match is successful
                    // use every episode in the this block with the i th episode to generate candidates
                    // i th episode provides : all events,    z th episode provides : the last events
                    int current_block_start = k+1;
                    int[] prefixEvents = new int[numLevel+1];
                    System.arraycopy(this.k_freEpisodes.get(i).events,0,prefixEvents,0,numLevel);
                    for(int z = this.block_start.get(j); z<this.block_start.size() && this.block_start.get(z) == this.block_start.get(j); z++){
                        k++;
                        int[] candidate = prefixEvents.clone();
                        candidate[numLevel] = this.k_freEpisodes.get(z).events[numLevel-1];
                        candidates.addCandidate(candidate,current_block_start);
                    }

                    // when we complete this block, then we need not to try others blocks
                    // but only for the first level, because others's suffix are same

                    break;

                }
            }
        }
        return candidates;
    }



}
