package MRCPPS;

import java.util.ArrayList;
import java.util.List;

public class RCPPSlist {

    // sequence id list
    private List<Integer> SIDlist;

    // Conjunctive tid-list
    private List<List<Integer>> list_conTIDlist;

    // Disconjunctive tid-list
    private List<List<Integer>> list_disTIDlist;



    RCPPSlist(){
        this.list_conTIDlist = new ArrayList<>();
        this.SIDlist = new ArrayList<>();
        this.list_disTIDlist = new ArrayList<>();
    }


    public int getSizeOfSIDlist(){
        return this.SIDlist.size();
    }

    public void addSID(int sid){
        // if the sid has been used, we skip it
        if(SIDlist.size() >0 && SIDlist.get(SIDlist.size()-1) == sid){
            return;
        }
        SIDlist.add(sid);
        list_conTIDlist.add(new ArrayList<Integer>());
        list_disTIDlist.add(new ArrayList<Integer>());
    }

    public void addTID(int tid){
        // if the tid is same, we skip it.
        int lastIndex = this.list_conTIDlist.get(this.getSizeOfSIDlist()-1).size() - 1;
        if(lastIndex>0 && this.list_conTIDlist.get(this.getSizeOfSIDlist()-1).get(lastIndex) == tid){
            return;
        }
        list_conTIDlist.get(this.getSizeOfSIDlist()-1).add(tid);
        list_disTIDlist.get(this.getSizeOfSIDlist()-1).add(tid);
    }

    // Construct function
    public RCPPSlist genRCPPSlistOfCandidate(RCPPSlist alpha,double minBond){
        RCPPSlist candidate = new RCPPSlist();
        // i to index of this.SIDlist,   j to index of alpha of SIDlist
        int i = 0, j = 0;
        while(i<this.SIDlist.size() && j<alpha.SIDlist.size()){
            if(this.SIDlist.get(i) < alpha.SIDlist.get(j)){
                // if the sequence id of this  less than the sequence id of alpha
                i++;
            }else if(this.SIDlist.get(i) > alpha.SIDlist.get(j)){
                // if the sequence id of this  greater than the sequence id of alpha
                j++;
            }else{
                // equal
                int currentSID = this.SIDlist.get(i);

                List<Integer> i_thOfconTIDlist =  this.list_disTIDlist.get(i);
                List<Integer> j_thOfconTIDlist =  alpha.getList_conTIDlist().get(j);
                List<Integer> conTIDlistOfCandidate = getConjunctiveList(i_thOfconTIDlist,j_thOfconTIDlist);
                if(conTIDlistOfCandidate != null && conTIDlistOfCandidate.size() >0){
                    // if they have intersection
                    List<Integer> i_thOfdisTIDlist = this.list_disTIDlist.get(i);
                    List<Integer> j_thOfdisTIDlist = alpha.getList_disTIDlist().get(j);
                    List<Integer> disTIDlistOfCandidate = getDisconjunctiveList(i_thOfdisTIDlist,j_thOfdisTIDlist);

                    double bond = (double)conTIDlistOfCandidate.size() / (double)disTIDlistOfCandidate.size();

                    if(bond >= minBond){
                        //  if bond(X,SID) >=maxBond, then X is candidate in this SID
                        candidate.getSIDlist().add(currentSID);
                        candidate.getList_conTIDlist().add(conTIDlistOfCandidate);
                        candidate.getList_disTIDlist().add(disTIDlistOfCandidate);
                    }

                }

                i++;
                j++;
            }
        }

        return candidate;
    }



    public List<Integer> getDisconjunctiveList(List<Integer> a, List<Integer> b){
        List<Integer> res = new ArrayList<>();
        if( a== null && b ==null) return res;
        if( a == null){
            res.addAll(b);
            return res;
        }
        if( b == null){
            res.addAll(a);
            return res;
        }

        int ai = 0, bi = 0;
        while(ai<a.size() && bi<b.size()){
            if(a.get(ai) < b.get(bi)){
                res.add(a.get(ai));
                ai++;
            }else if(a.get(ai) > b.get(bi)){
                res.add(b.get(bi));
                bi++;
            }else{
                res.add(a.get(ai));
                ai++;
                bi++;
            }
        }
        if(ai < a.size()){
            for(; ai<a.size(); ai++){
                res.add(a.get(ai));
            }
        }
        if(bi < a.size()){
            for(; bi<b.size(); bi++){
                res.add(b.get(bi));
            }
        }
        return res;
    }

    public List<Integer> getConjunctiveList(List<Integer> a, List<Integer> b){
        List<Integer> res = new ArrayList<>();
        if(a == null || b == null || a.size() <=0 || b.size() <=0)  return res;

        int ai = 0, bi = 0;
        while(ai<a.size() && bi<b.size()){
            if(a.get(ai) < b.get(bi)){
                ai++;
            }else if(a.get(ai) > b.get(bi)){
                bi++;
            }else{
                res.add(a.get(ai));
                ai++;
                bi++;
            }
        }
        return res;
    }

    /**
     *
     * @param maxSup
     * @param maxStd
     * @param lenOfseqList  contains the length of each sequence   (index is the sequence id  from zero to )
     * @param useLemma2
     * @return
     */
    public int getNumSeq(double maxSup,double maxStd, List<Integer> lenOfseqList, boolean useLemma2){
        // to check whether the candidate is both rare and periodic
        // and from 'genRCPPSlistOfCandidate', we know that the candidate is correlated in each sequence
        int numSeq = 0;
        for(int i = 0; i<this.SIDlist.size(); i++){
            int SID = SIDlist.get(i);
            List<Integer> conTIDlist = this.list_conTIDlist.get(i);

            int lenOfcurSeq = lenOfseqList.get(SID);
            double stanDev = getStanDevFromTIDlist(conTIDlist,lenOfcurSeq,useLemma2);
            int sup = conTIDlist.size();
            if( sup <= maxSup && stanDev <= maxStd){
                numSeq++;
            }
        }
        return numSeq;
    }

    public int getNumCand(){
        // because we only save the correlated items in each sequence in the process of 'genRCPPSlistOfCandidate'.
        return this.SIDlist.size();
    }

    public double getStanDevFromTIDlist(List<Integer> conTIDlist,int lenOfs,boolean useLemma2){

        double stanDev = 0;
        int preTID = 0;  // note that the smallest of TID is 1
        if(useLemma2){
            for(int i = 0; i<conTIDlist.size(); i++){
                int per_i = conTIDlist.get(i) - preTID;
                stanDev = stanDev + Math.pow(per_i,2);
                preTID = conTIDlist.get(i);
            }
            // for per_(k+1)^2
            stanDev = stanDev + Math.pow(lenOfs - preTID,2);

            stanDev = stanDev / (double)(conTIDlist.size()+1);
            stanDev = stanDev - Math.pow((double)lenOfs / (double)(conTIDlist.size()+1), 2 );
            stanDev = Math.sqrt(stanDev);
        }else{
            double avgPer = 0;
            for(int i = 0; i<conTIDlist.size();i++){
                int per_i = conTIDlist.get(i) - preTID;
                avgPer = avgPer + per_i;
                preTID = conTIDlist.get(i);
            }
            // for per_{k+1}
            avgPer = avgPer + lenOfs - preTID;

            avgPer = avgPer / (double)(conTIDlist.size()+1);

            preTID = 0;
            for(int i = 0; i<conTIDlist.size(); i++){
                int per_i = conTIDlist.get(i) - preTID;
                stanDev = stanDev + Math.pow(per_i - avgPer, 2);
                preTID = conTIDlist.get(i);
            }
            // for per_{k+1}
            stanDev = stanDev + Math.pow(lenOfs - preTID - avgPer, 2);
            stanDev = stanDev / (double)(conTIDlist.size() + 1);
            stanDev = Math.sqrt(stanDev);
        }
        return stanDev;
    }


    public List<List<Integer>> getList_conTIDlist() {
        return list_conTIDlist;
    }

    public void setList_conTIDlist(List<List<Integer>> list_conTIDlist) {
        this.list_conTIDlist = list_conTIDlist;
    }

    public List<Integer> getSIDlist() {
        return SIDlist;
    }

    public void setSIDlist(List<Integer> SIDlist) {
        this.SIDlist = SIDlist;
    }

    public List<List<Integer>> getList_disTIDlist() {
        return list_disTIDlist;
    }

    public void setList_disTIDlist(List<List<Integer>> list_disTIDlist) {
        this.list_disTIDlist = list_disTIDlist;
    }

    public String getDetails( List<Integer> lenOfseqList){
        StringBuilder buffer = new StringBuilder();
        buffer.append(" #<SID,sup,bond,stanDev>:");
        for(int z = 0; z<this.SIDlist.size(); z++){
            int SID = SIDlist.get(z);
            int lenOfs = lenOfseqList.get(SID);
            List<Integer> conTIDlist = this.list_conTIDlist.get(z);
            List<Integer> disTIDlist = this.list_disTIDlist.get(z);

            int sup = conTIDlist.size();
            double bond = (double) sup / (double) disTIDlist.size();

            double stanDev = 0;
            int preTID = 0;
            for(int i = 0; i<conTIDlist.size(); i++){
                int per_i = conTIDlist.get(i) - preTID;
                stanDev = stanDev + Math.pow(per_i,2);
                preTID = conTIDlist.get(i);
            }
            // for per_(k+1)^2
            stanDev = stanDev + Math.pow(lenOfs - preTID,2);

            stanDev = stanDev / (double)(conTIDlist.size()+1);
            stanDev = stanDev - Math.pow((double)lenOfs / (double)(conTIDlist.size()+1), 2 );
            stanDev = Math.sqrt(stanDev);

            buffer.append(" < "+ SID + " , "+ sup + " , " + bond + " , "+stanDev+" > ");
        }

        return buffer.toString();

    }
}
