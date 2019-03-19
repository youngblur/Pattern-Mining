import java.util.List;

public class Itemset {
    /** the array of items **/
    public int[] itemset;

    /**  the support of this itemset */
    public int support = 0;

    /** the maxla of this itemset **/
    public int maxla = 0;


    /**
     * Get the items as array
     * @return the items
     */
    public int[] getItems() {
        return itemset;
    }

    /**
     * Constructor
     */
    public Itemset(){
        itemset = new int[]{};
    }

    /**
     * Constructor
     * @param item an item that should be added to the new itemset
     */
    public Itemset(int item){
        itemset = new int[]{item};
    }

    /**
     * Constructor
     * @param items an array of items that should be added to the new itemset
     */
    public Itemset(int [] items){
        this.itemset = items;
    }

    /**
     * Constructor
     * @param itemset a list of Integer representing items in the itemset
     * @param support the support of the itemset
     */
    public Itemset(List<Integer> itemset, int support, int maxla){
        this.itemset = new int[itemset.size()];
        int i = 0;
        for (Integer item : itemset) {
            this.itemset[i++] = item.intValue();
        }
        this.support = support;
        this.maxla = maxla;
    }

    public Itemset(int[] itemset, int support, int maxla){
        this.itemset = itemset;
        this.support = support;
        this.maxla = maxla;
    }

    /**
     * Get the support of this itemset
     */
    public int getAbsoluteSupport(){
        return support;
    }

    /**
     * Increase the support of this itemset by 1
     */
    public void increaseTransactionCount() {
        this.support++;
    }
}
