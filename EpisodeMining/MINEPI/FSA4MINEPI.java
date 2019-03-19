package MINEPI;

/**
 *  implement a simple Finite-State Automaton for MINEPI algorithm
 *   we use -1 as end state
 */
public class FSA4MINEPI {
    final int end_staet = -1;
    int[] FSA;
    int pos = 0;
    int startTime;


    FSA4MINEPI(){
    }


    FSA4MINEPI(int[] candidate){
        int length = candidate.length;
        this.FSA = new int[length+1];
        System.arraycopy(candidate,0,this.FSA,0,length);
        // add a end state
        this.FSA[length] = this.end_staet;
    }

    public int waiting4Event(){
        return this.FSA[pos];
    }


    public void transit(){
        this.pos++;
    }

    public boolean isEnd(){
        return this.FSA[pos] == this.end_staet;
    }

    public boolean isSame(FSA4MINEPI fsa){
        return this.pos == fsa.pos;
    }

    public void addStartTime(int startTime){
        this.startTime = startTime;
    }

    public int getWinLength(int endTime){
        return endTime - startTime + 1;
    }

    public String toString(){
        String returnString = "";
        for(int i=0; i<pos-1; i++){
            returnString += this.FSA[i]+" -> ";
        }
        returnString += this.FSA[pos-1]+"  ";
        return returnString;
    }

}
