package MINEPI;


/**
 *   implement Class of Episode ( serial episode) in simple sequence
 *   it means that the episode only contains the single symbol for one time point
 */
public class Episode {


    // the events in the serial episode
    // each event is a single event(symbol)
    int[] events;


    // The support of episode
    int support = 0;





    Episode(){

    }

    Episode(int[] events,int support){
        this.events = events;
        this.support = support;
    }


    public void increaseSupport(){
        this.support++;
    }

    //  this episode will use last n-1 event as suffix to compare with the first n-1 event of the episode of having the same size
    public boolean compare2prefix(Episode prefix){
        // we only compare with others in the condition that the size is large 1
        for(int i=0; i<this.events.length-1;i++){
            if(this.events[i+1] != prefix.events[i]) return false;
        }
        return true;
    }

    public String toString(){
        String returnString = "";
        int episodeLength= events.length;
        for(int i = 0; i< episodeLength-1; i++){
            returnString= returnString + String.valueOf(events[i]) +  " -> ";
        }
        returnString = returnString + String.valueOf(events[episodeLength-1])+"  #SUP : "+ String.valueOf(this.support);
        return returnString;
    }

}
