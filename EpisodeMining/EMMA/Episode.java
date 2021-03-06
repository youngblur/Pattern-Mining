package EMMA;


import java.util.ArrayList;
import java.util.List;

/**
 *   implement Class of Episode ( serial episode) in complex sequence
 *   it means that the episode can contains multiple symbols for one time point
 */
public class Episode {


    // the events in the serial episode
    // each event is a non-empty eventset
    List<int[]> events;


    // The support of episode
    int support = 0;




    Episode(){
        this.events = new ArrayList<>();
        this.support = 0;
    }

    Episode(List<int[]> events, int support){
        this.events = events;

        this.support = support;
    }



    public Episode iExtension(int item, int support){
        int[] finalEventSet = this.events.get(events.size()-1);
        int len = finalEventSet.length;
        int[] newEventSet = new int[len+1];
        System.arraycopy(finalEventSet,0,newEventSet,0,len);
        newEventSet[len] = item;
        List<int[]> newEvents = new ArrayList<int[]>(events);
        // set the last eventSet to the new eventSet.
        newEvents.set(events.size()-1,newEventSet);
        return new Episode(newEvents,support);
    }

    public Episode sExtension(int item, int support){
        List<int[]> newEvents = new ArrayList<int[]>(events);
        newEvents.add(new int[]{item});
        return new Episode(newEvents,support);
    }

    public Episode sExtension(int[] fllowingEpisodeName, int support){
        List<int[]> newEvents = new ArrayList<int[]>(events);
        newEvents.add(fllowingEpisodeName);
        return new Episode(newEvents,support);
    }

    /**
     *  only for 1-episode to call
     * @return
     */
    public int getLastItem(){
        return events.get(0)[0];
    }


    public void increaseSupport(){
        this.support++;
    }



    public String toString(){
        String returnString = "";
        int episodeLength= events.size();
        for(int i = 0; i< episodeLength-1; i++){
            returnString =returnString+ "< ";
            for(int j = 0; j<events.get(i).length-1; j++){
                returnString = returnString + String.valueOf(events.get(i)[j])+", ";
            }
            returnString = returnString + String.valueOf(events.get(i)[events.get(i).length-1])+" >";
            returnString= returnString +  " ==》 ";
        }
        returnString = returnString + "< ";
        for(int j = 0; j<events.get(episodeLength-1).length-1 ; j++){
            returnString = returnString + String.valueOf(events.get(episodeLength-1)[j])+", ";
        }
        returnString = returnString + String.valueOf(events.get(episodeLength-1)[events.get(episodeLength-1).length-1])+ " >";
        returnString = returnString + "  #SUP : "+ String.valueOf(this.support);
        return returnString;
    }

}
