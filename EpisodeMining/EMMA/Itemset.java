package EMMA;

import java.util.ArrayList;
import java.util.List;

public class Itemset {
    private int[] name;
    private List<Integer> locationList = null;

    Itemset(){

    }

    Itemset(int[] name){
        this.name = name;
    }

    Itemset(int[] name, List<Integer> locationList){
        this.name = name;
        this.locationList = locationList;
    }

    public int[] getName(){
        return this.name;
    }

    public List<Integer> getLocationList(){
        return this.locationList;
    }

    public void setLocationList(List<Integer> locationList){
        this.locationList =  locationList;
    }

    public int getSupport(){
        return this.locationList.size();
    }
}
