package EMMA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncodingTable {
    private List<Episode> F1;

    private List<List<int[]>> F1boundlist;

    private int tableLength;

    EncodingTable(){
        this.F1 = new ArrayList<>();
        this.F1boundlist = new ArrayList<>();
        this.tableLength = 0;
    }

    public void addEpisodeAndBoundlist(Episode episode, List<int[]> boundlist){
        F1.add(episode);
        F1boundlist.add(boundlist);
        tableLength++;
    }

    public Episode getEpisodebyID(int ID){
        return this.F1.get(ID);
    }

    public List<int[]> getBoundlistByID(int ID){
        return this.F1boundlist.get(ID);
    }

    public int[] getEpisodeNameByID(int ID){
        return this.F1.get(ID).events.get(0);
    }

    public List<Episode> getF1() {
        return F1;
    }

    public List<List<int[]>> getF1boundlist() {
        return F1boundlist;
    }


    public int getTableLength(){
        return this.tableLength;
    }
}
