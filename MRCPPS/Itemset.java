package MRCPPS;

import java.util.Arrays;
import java.util.List;

public class Itemset {
    private int[] name;
    private double ra;

    Itemset(int[] name, double ra){
        this.name = name;
        this.ra = ra;
    }

    Itemset(int name, double ra){
        this.name = new int[]{name};
        this.ra =ra;
    }

    public int[] getName() {
        return name;
    }

    public void setName(int[] name) {
        this.name = name;
    }

    public double getRa() {
        return ra;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public int size(){
        return name.length;
    }

    @Override
    public String toString() {
        String s = "";
        for(int n:name){
            s = s + n+ " ,";
        }
        s = s.substring(0,s.length()-1);
        s+="   #ra ："+this.ra;
        return s;
    }
}
