package MINEPI;

import java.util.HashSet;
import java.util.Set;

/**
 *  Implement simultaneous event set
 */
public class Event {
    Set<Integer> events = new HashSet<>();
    int time;

    Event(){
    }



    Event(String[] events, int time){
        for(String e : events){
            this.events.add(Integer.parseInt(e));
        }
    }

    Event(int event, int time){
        events.add(event);
        this.time = time;
    }

    public void setTime(int time){
        this.time = time;
    }

    public void addEvent(Integer event){
        this.events.add(event);
    }

    public boolean contains(Integer event){
        return this.events.contains(event);
    }

    public int getTime(){
        return this.time;
    }
}
