/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapItem min;
    public HeapNode start;
    public int size;
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.min = null;
        this.start = null;
        size = 0;
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapItem insert(int key, String info) 
    {    
        return null; // should be replaced by student code
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin()
    {
        return null; // should be replaced by student code
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {
        return; // should be replaced by student code
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapItem x, int diff) 
    {    
        return; // should be replaced by student code
    }

    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {    
        return; // should be replaced by student code
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2)
    {
        if (this.start == null){
            this.start = heap2.start;
            this.size = heap2.size;
            this.min = heap2.min;
            heap2 = null;
            return;
        } else if (heap2.start == null) {
            return;
        }
        this.start.prev.next = heap2.start;
        HeapNode temp = this.start.prev;
        this.start.prev = heap2.start.prev;
        heap2.start.prev.next = this.start;
        heap2.start.prev = temp;
        if (this.min.compareTo(heap2.min)>0){
            this.min = heap2.min;
        }
        if (!lazyMelds){
            //successive linking
        }
        return;
    }

    public HeapNode link(HeapNode x, HeapNode y)
    {
        if (x.item.compareTo(y.item)>0){
            change(x, y);
        }
        if (x.child == null){
            y.next = y;
        } else{
            y.next = x.child;
            y.prev = x.prev;
            x.prev.next = y;
            x.prev = y;
        }
        x.child = y;
        y.parent = x;
        return x;
    }

    public void change(HeapNode x, HeapNode y){
        HeapItem temp = new HeapItem(x.item.key, x.item.info, x.item.node);
        x.item = y.item;
        y.item = temp;
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return 46; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return 46; // should be replaced by student code
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return 46; // should be replaced by student code
    }
    
    
    /**
     * Class implementing a node in a Heap.
     *  
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;

        public HeapNode(HeapItem item){
            this.item = item;
            child = null;
            next = null;
            prev = null;
            parent = null;
        }
    }
    
    /**
     * Class implementing an item in a Heap.
     *  
     */
    public static class HeapItem implements Comparable<HeapItem>{
        public HeapNode node;
        public int key;
        public String info;

        public HeapItem(int key, String info, HeapNode node) {
            this.node = node;
            this.key = key;
            this.info = info;
        }

        @Override
        public int compareTo(HeapItem y) {
            return Integer.compare(this.key, y.key);
        }
    }
}
