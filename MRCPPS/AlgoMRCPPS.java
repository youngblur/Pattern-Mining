package MRCPPS;

import java.io.*;
import java.util.*;

public class AlgoMRCPPS {


    /**  start time of the last execution */
    protected long startTimestamp;
    /** end  time of the last execution */
    protected long endTime;

    /**
     The  patterns that are found
     (if the user want to keep them into memory) */
    protected Itemsets patterns;

    /** object to write the output file */
    BufferedWriter writer = null;

    /** the number of patterns found */
    protected int itemsetCount;

    /**  buffer for storing the current itemset that is mined when performing mining
     the idea is to always reuse the same buffer to reduce memory usage. */
    final int BUFFERS_SIZE = 40000;

    /** size of the buffer*/
    private int[] itemsetBuffer = null;

    // This buffer is used to store an itemset that will be written to file
    // so that the algorithm can sort the itemset before it is output to file
    // (when the user choose to output result to file).
    private int[] itemsetOutputBuffer = null;

    /** Special parameter to set the maximum size of itemsets to be discovered */
    int maxItemsetSize = Integer.MAX_VALUE;

    // record the length of each sequence
    private List<Integer> lenOfseqList;

    // recod the size of sequence database
    private int sizeOfseq;

    // thresholds:
    private int maxSup;

    private double maxStd;

    private double minBond;

    private double minRa;

    // using Lemma2?;
    private boolean useLemma2;

    // save file to show detail
    private boolean showDetails;

    // how many transactions will be grouped to one transaction
    private int groupNum;

    private boolean needGroup;

    AlgoMRCPPS(int maxSup, double maxStd, double minBond, double minRa,boolean useLemma2, boolean showDetails, boolean needGroup, int groupNum){
        this.maxSup = maxSup;
        this.maxStd = maxStd;
        this.minBond = minBond;
        this.minRa = minRa;

        this.needGroup = needGroup;
        this.groupNum = groupNum;


        this.useLemma2 = useLemma2;
        this.showDetails = showDetails;

        this.lenOfseqList= new ArrayList<Integer>();
        this.sizeOfseq = 0;
    }

    public Itemsets runAlgorithm(String input, String output) throws FileNotFoundException, IOException {
        // record start time
        startTimestamp = System.currentTimeMillis();

        // number of itemsets found
        itemsetCount = 0;

        //initialize tool to record memory usage
        MemoryLogger.getInstance().reset();

        // initialize the buffer for storing the current itemset
        itemsetBuffer = new int[BUFFERS_SIZE];

        // if the user want to keep the result into memory
        if (output == null) {
            writer = null;
            patterns = new Itemsets("Correlated Rare Periodic Pattern in multiple Sequences");
        } else { // if the user want to save the result to a file
            patterns = null;
            writer = new BufferedWriter(new FileWriter(output));
            itemsetOutputBuffer = new int[BUFFERS_SIZE];
        }

        // (1) PREPROCESSING: Initial database scan to determine the RCPPSlist of each item
        // The TID is stored in a map:
        //    key: item   value: RCPPSlist
        final Map<Integer, RCPPSlist> mapRCPPSlist = scanDatabaseToDeterminRCPPSlistOfSingleItems(input);

        // (2) obtain the name of the candidates having length is 1
        List<Integer> candidates = new ArrayList<>(mapRCPPSlist.keySet());

        // (3) Sort the list of items by the total order of increasing numCand.
        Collections.sort(candidates, new Comparator<Integer>() {
            @Override
            public int compare(Integer arg0, Integer arg1) {
                return mapRCPPSlist.get(arg0).getNumCand() - mapRCPPSlist.get(arg1).getNumCand();
            }
        });

        // Now we will combine each pairs of single items to generate equivalence classes
        // of 2-itemsets

        if(maxItemsetSize >=2) {
            // For each frequent item I according to the total order
            for (int i = 0; i < candidates.size(); i++) {
                int itemI = candidates.get(i);
                // we obtain the RCPPSlist and support of that item
                RCPPSlist RCPPSlistI = mapRCPPSlist.get(itemI);

                // We create empty equivalence class for storing all 2-itemsets starting with
                // the item "i".
                // This equivalence class is represented by two structures.
                // The first structure stores the suffix of all 2-itemsets starting with the prefix "i".
                // For example, if itemI = "1" and the equivalence class contains 12, 13, 14, then
                // the structure "equivalenceC  lassIitems" will only contain  2, 3 and 4 instead of
                // 12, 13 and 14.  The reason for this implementation choice is that it is more
                // memory efficient.
                List<Integer> equivalenceClassIitems = new ArrayList<Integer>();
                // The second structure stores the RCPPSlist of each 2-itemset in the equivalence class
                // of the prefix "i".
                List<RCPPSlist> equivalenceClassIRCPPSlist = new ArrayList<RCPPSlist>();

                // For each item itemJ that is larger than i according to the total order of
                // increasing total of numCand.
                for (int j = i + 1; j < candidates.size(); j++) {
                    int itemJ = candidates.get(j);

                    // Obtain the RCPPSlist of item J and its support.
                    RCPPSlist RCPPSlistJ = mapRCPPSlist.get(itemJ);

                    RCPPSlist RCPPSlistIJ = RCPPSlistI.genRCPPSlistOfCandidate(RCPPSlistJ, this.minBond);

                    double boundRa = (double) RCPPSlistIJ.getNumCand() / (double) this.sizeOfseq;
                    if (boundRa!=0 && boundRa>= minRa) {
                        // save the candidate
                        equivalenceClassIitems.add(itemJ);
                        equivalenceClassIRCPPSlist.add(RCPPSlistIJ);
                    }
                }
                // Process all itemsets from the equivalence class of 2-itemsets starting with prefix I
                // to find larger itemsets if that class has more than 0 itemsets.
                if(equivalenceClassIitems.size() > 0) {
                    // This is done by a recursive call. Note that we pass
                    // item I to that method as the prefix of that equivalence class.
                    itemsetBuffer[0] = itemI;
                    processEquivalenceClass(itemsetBuffer, 1, equivalenceClassIitems, equivalenceClassIRCPPSlist);
                }
            }
        }

        // we check the memory usage
        MemoryLogger.getInstance().checkMemory();

        // We have finish the search.
        // Therefore, we close the output file writer if the result was saved to a file
        if(writer != null){
            writer.close();
        }

        // record the end time for statistics
        endTime = System.currentTimeMillis();

        // Return all frequent itemsets found or null if the result was saved to a file.
        return patterns;
    }

    private void processEquivalenceClass(int[] prefix, int prefixLength, List<Integer> equivalenceClassItems, List<RCPPSlist> equivalenceClassIRCPPSlist) throws IOException {
        // If there is only one itemset in equivalence class
        if(equivalenceClassItems.size() == 1) {
            int itemI = equivalenceClassItems.get(0);
            RCPPSlist RCPPSlistI = equivalenceClassIRCPPSlist.get(0);

            // distinguish the ra of the 'itemI' with prefix
            double ra = (double)RCPPSlistI.getNumSeq(this.maxSup,this.maxStd,this.lenOfseqList,this.useLemma2) / (double) this.sizeOfseq;

            if(ra!=0 && ra>= minRa) {
                // Then, we just save that itemset to file and stop.
                // To save the itemset we call the method save with the prefix "prefix" and the suffix
                // "itemI".
                save(prefix, prefixLength, itemI, ra,RCPPSlistI);
            }
            return;
        }

        // If there is only two itemsets in the equivalence class
        if(equivalenceClassItems.size() == 2) {
            int itemI = equivalenceClassItems.get(0);
            RCPPSlist RCPPSlistI = equivalenceClassIRCPPSlist.get(0);
            // distinguish the ra of the 'itemI' with prefix
            double raI = (double)RCPPSlistI.getNumSeq(this.maxSup,this.maxStd,this.lenOfseqList,this.useLemma2) / (double) this.sizeOfseq;

            int itemJ = equivalenceClassItems.get(1);
            RCPPSlist RCPPSlistJ = equivalenceClassIRCPPSlist.get(1);
            // distinguish the ra of the 'itemI' with prefix
            double raJ = (double)RCPPSlistJ.getNumSeq(this.maxSup,this.maxStd,this.lenOfseqList,this.useLemma2) / (double) this.sizeOfseq;

            if(raI!=0 && raI>= minRa){
                save(prefix, prefixLength, itemI, raI,RCPPSlistI);
            }

            if(raJ!=0 && raJ>= minRa){
                save(prefix, prefixLength, itemJ, raJ,RCPPSlistJ);
            }


            if(prefixLength + 2 <= maxItemsetSize){
                RCPPSlist RCPPSlistIJ = RCPPSlistI.genRCPPSlistOfCandidate(RCPPSlistJ,this.minBond);

                double raIJ = (double)RCPPSlistIJ.getNumSeq(this.maxSup,this.maxStd,this.lenOfseqList,this.useLemma2) / (double) this.sizeOfseq;
                if(raIJ!=0 && raIJ>= minRa){
                    int newPrefixLength = prefixLength + 1;
                    prefix[prefixLength] = itemI;

                    save(prefix,newPrefixLength,itemJ,raIJ,RCPPSlistIJ);
                }
            }
            // we check the memory usage
            MemoryLogger.getInstance().checkMemory();

            return;
        }
        // The next loop combines each pairs of itemsets of the equivalence class
        // to form larger itemsets
        // For each itemset "prefix" + "i"
        for(int i=0; i< equivalenceClassItems.size(); i++) {
            int suffixI = equivalenceClassItems.get(i);
            // get the RCPPSlist and ra of that itemset
            RCPPSlist RCPPSlistI = equivalenceClassIRCPPSlist.get(i);
            double raI = (double) RCPPSlistI.getNumSeq(this.maxSup, this.maxStd, this.lenOfseqList, this.useLemma2) / (double) this.sizeOfseq;
            if(raI!=0 && raI >= minRa){
                save(prefix, prefixLength, suffixI, raI,RCPPSlistI);
            }
            if(prefixLength+2 <= maxItemsetSize) {
                // create the empty equivalence class for storing all itemsets of the
                // equivalence class starting with prefix + i
                List<Integer> equivalenceClassISuffixItems = new ArrayList<Integer>();
                List<RCPPSlist> equivalenceClassISuffixRCPPSlist = new ArrayList<RCPPSlist>();

                // For each itemset "prefix" + j"
                for (int j = i + 1; j < equivalenceClassItems.size(); j++) {
                    int suffixJ = equivalenceClassItems.get(j);
                    RCPPSlist RCPPSlistJ = equivalenceClassIRCPPSlist.get(j);

                    RCPPSlist RCPPSlistIJ = RCPPSlistI.genRCPPSlistOfCandidate(RCPPSlistJ,this.minBond);

                    double boundRa = (double)RCPPSlistIJ.getNumCand() / (double) this.sizeOfseq;

                    if(boundRa!=0 && boundRa>=minRa){
                        equivalenceClassISuffixItems.add(suffixJ);
                        equivalenceClassISuffixRCPPSlist.add(RCPPSlistIJ);
                    }
                }
                // If there is more than an itemset in the equivalence class
                // then we recursively process that equivalence class to find larger itemsets
                if(equivalenceClassISuffixItems.size() >0) {
                    // We create the itemset prefix + i
                    prefix[prefixLength] = suffixI;
                    int newPrefixLength = prefixLength+1;

                    // Recursive call
                    processEquivalenceClass(prefix, newPrefixLength, equivalenceClassISuffixItems, equivalenceClassISuffixRCPPSlist);
                }
            }
        }

    }

    private Map<Integer,RCPPSlist> scanDatabaseToDeterminRCPPSlistOfSingleItems(String input) throws IOException{
            //  for the sigle RCPPSs, their bond in each sequence is the maximal value = 1,  we donot do pruning in the process of scanning the database
            // after we record all information, we need drop the items that its boundRa < minRa

        Map<Integer,RCPPSlist> mapRCPPSlist = new HashMap<>();

        // read file
        BufferedReader reader = new BufferedReader(new FileReader(input));
        String line;
        // key : item,  value : temp tid-list
        int SID = 0;
        while((line = reader.readLine()) != null){
            // if the line is  a comment, is  empty or is a
            // kind of metadata
            if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%'
                    || line.charAt(0) == '@') {
                continue;
            }
            String[] lineSplited = line.split(" ");

            int TID = 1;
            int groupCount = 0;
            // for each token in this sequence (item, separator between itemsets (-1) or end of sequence (-2)
            for(String token : lineSplited){
                Integer item = Integer.parseInt(token);
                // if it is an item
                if(item > 0){
                    RCPPSlist  itemRCPPSlist = mapRCPPSlist.get(item);
                    if(itemRCPPSlist == null){
                        itemRCPPSlist = new RCPPSlist();
                        mapRCPPSlist.put(item,itemRCPPSlist);
                    }

                    itemRCPPSlist.addSID(SID);  // 'addSID' will determine whether the SID repeats, we skip it if repeating
                    itemRCPPSlist.addTID(TID);
                }else if(item == -1){
                    if(this.needGroup){
                        groupCount++;
                        if(groupCount == this.groupNum){
                            // group groupNum transactions to be a transaction (a TID)
                            groupCount = 0;
                            TID++;
                        }
                    }else{
                        // for each transaction is a TID
                        TID++;
                    }

                }

            }
            if(groupCount > 0){
                this.lenOfseqList.add(TID);
            }else{
                this.lenOfseqList.add( TID-1 );
            }
            SID++;
        }

        this.sizeOfseq = SID;

        // close the input file
        reader.close();

        // check the boundRa, in the single item :   numCand == numSeq
        Iterator<Map.Entry<Integer, RCPPSlist>> it = mapRCPPSlist.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, RCPPSlist> entry = it.next();
            Integer item = entry.getKey();
            RCPPSlist itemRCPPSlist = entry.getValue();

            double boundRa = (double)itemRCPPSlist.getNumCand() / (double) this.sizeOfseq;
            if(boundRa < minRa){
                // remove it
                it.remove();
            }else{
                // to obtain the ra
                double ra = (double)itemRCPPSlist.getNumSeq(this.maxSup,this.maxStd,this.lenOfseqList,this.useLemma2) / (double) this.sizeOfseq;
                // ##############  for checking
//                System.out.println(item+" : "+ra);
                if(ra!=0 && ra>= minRa){

                    saveSingleItem(item,ra,itemRCPPSlist);
                }
            }
        }
        return mapRCPPSlist;
    }

    void saveSingleItem(int item, double ra,RCPPSlist itemRCPPSlist) throws IOException {
        // increase the itemset count
        itemsetCount++;
        // if the result should be saved to memory
        if(writer == null){
            // add it to the set of frequent itemsets
            Itemset itemset = new Itemset(item,ra);
            patterns.addItemset(itemset,itemset.size());
        }else {
            // if the result should be saved to a file
            // write it to the output file
            StringBuilder buffer = new StringBuilder();
            buffer.append(item);
            buffer.append("  #ra: ");
            buffer.append(ra);
            if (showDetails) {
                buffer.append(itemRCPPSlist.getDetails(lenOfseqList));
            }
            writer.write(buffer.toString());
            writer.newLine();
        }
    }
    void save(int[] prefix, int prefixLength, int suffixItem, double ra, RCPPSlist itemRCPPSlist) throws IOException {
        // increase the itemset count
        itemsetCount++;
        // if the result should be saved to memory
        if(writer == null) {
            // append the prefix with the suffix
            int[] itemsetArray = new int[prefixLength + 1];
            System.arraycopy(prefix, 0, itemsetArray, 0, prefixLength);
            itemsetArray[prefixLength] = suffixItem;
            // Create an object "Itemset" and add it to the set of frequent itemsets
            Itemset itemset = new Itemset(itemsetArray, ra);
            patterns.addItemset(itemset, itemset.size());
        }else{
            // if the result should be saved to a file
            // write it to the output file
            StringBuilder buffer = new StringBuilder();
            for(int i=0; i < prefixLength; i++) {
                int item = prefix[i];
                buffer.append(item);
                buffer.append(" ");
            }
            buffer.append(suffixItem);
            buffer.append("  #ra: ");
            buffer.append(ra);
            if (showDetails) {
                buffer.append(itemRCPPSlist.getDetails(lenOfseqList));
            }
            writer.write(buffer.toString());
            writer.newLine();
        }
    }

    /**
     * Print statistics about the algorithm execution to System.out.
     */
    public void printStats() {
        System.out.println("=============  MRCPPS - STATS =============");
        long temps = endTime - startTimestamp;
        System.out.println(" Sequence count from SDB : "
                + this.sizeOfseq);
        System.out.println(" Total time ~ " + temps + " ms");
        System.out.println(" RCPPS count : "
                + itemsetCount);
        System.out.println(" Maximum memory usage : "
                + MemoryLogger.getInstance().getMaxMemory() + " mb");
        int maxLen = 0, minLen = Integer.MAX_VALUE;
        double avgLen = 0.0;
        for(int len : lenOfseqList){
            if(len > maxLen)    maxLen = len;
            if(len < minLen)    minLen = len;
            avgLen += len;
        }
        avgLen = avgLen / (double) lenOfseqList.size();
        System.out.println(" maximum sequence length : "+ maxLen
                        +"   minimum sequence length : "+ minLen
                        +"   average sequence length : "+ avgLen
        );
        System.out.println("===================================================");
    }



}
