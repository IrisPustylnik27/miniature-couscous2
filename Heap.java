import java.lang.reflect.Array;
import java.util.ArrayList;

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

    private int numTrees;
    private int numLinks;
    private int numCuts;
    private int numMarkedNodes;
    private int numHeapify;
    
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
        numTrees = 0;
        numLinks = 0;
        numCuts = 0;
        numMarkedNodes = 0;
        numHeapify = 0;
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
        HeapNode node = NewNode(key, info);
        Heap heap2 = NewHeap(node);
        meld(heap2);
        return node.item;
    }

    public HeapNode NewNode(int key, String info){
        HeapItem item = new HeapItem(key, info);
        HeapNode node = new HeapNode(item);
        item.node = node;
        return node;
    }

    public Heap NewHeap(HeapNode node){
        Heap heap2 = new Heap(lazyMelds, lazyDecreaseKeys);
        heap2.start = node;
        node.next = node;
        node.prev = node;
        heap2.size += 1;
        heap2.min = node.item;
        return heap2;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin()
    {
        return this.min; // should be replaced by student code
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin()
    {
        Heap minHeap = new Heap(lazyMelds, lazyDecreaseKeys);
        minHeap.start = min.node.child;
        minHeap.start.parent = null;
        this.deleteFromTheList(new HeapNode(min));
        HeapNode cnt = minHeap.start;
        HeapNode newMin = minHeap.start;
        while(cnt != null){
            if(newMin.item.compareTo(cnt.item)>0){
                newMin = cnt;
            }
            cnt = cnt.next; 
        }
        minHeap.min = newMin.item;
        cnt = this.start;
        newMin = this.start;
        while(cnt != null){
            if(newMin.item.compareTo(cnt.item)>0){
                newMin = cnt;
            }
            cnt = cnt.next; 
        }
        this.min = newMin.item;
        meld(minHeap);
        if(lazyMelds){
            successiveLinking();
        }
        return; 
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
        decreaseKey(x, x.key+1);
        deleteMin();
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

        if (this.min.compareTo(heap2.min) > 0){
            this.min = heap2.min;
        }

        this.size += heap2.size();
        this.numTrees += heap2.numTrees();

        if (!lazyMelds){
            this.successiveLinking();
        }
        heap2 = null;
    }

    public void successiveLinking(){
        HeapNode[] degreeTable = new HeapNode[this.numTrees()];
        HeapNode curr = this.start;

        do{
            int cr = curr.rank;
            while (degreeTable[cr] != null){
                degreeTable[cr] = link(curr, degreeTable[cr]);
                degreeTable[cr] = null;
                cr = curr.rank;
            }
            degreeTable[cr] = curr;
            HeapNode temp = curr.next;
            this.deleteFromTheList(curr);
            numTrees -= 1;
            curr = temp;
        }while (curr != null);

        for (HeapNode x: degreeTable) {
            if (x == null) {continue;}
            if (this.min == null) {
                min = x.item;
                this.start = x;
                numTrees += 1;
            } else {
                x.prev = this.start.prev;
                x.next = this.start;
                this.start.prev.next = x;
                this.start.prev = x;
                if (x.item.compareTo(this.min) < 0) {
                    this.min = x.item;
                }
            }
        }

    }

    private void deleteFromTheList (HeapNode node){
        if (node.next == node){
            this.start = null;
        } else{
            node.prev.next = node.next;
            node.next.prev = node.prev;
            node.prev = node;
            node.next = node;
        }
        this.min = null;
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
        this.numLinks += 1;
        x.rank = Math.max(x.rank, y.rank) + 1;
        return x;
    }

    public void change(HeapNode x, HeapNode y){
        HeapItem temp = new HeapItem(x.item.key, x.item.info);
        temp.node = x.item.node;
        x.item = y.item;
        y.item = temp;

        //changing ranks
        y.rank += x.rank;
        x.rank = y.rank - x.rank;
        y.rank -= x.rank;
    }
    
    
    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return this.size; // should be replaced by student code
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return this.numTrees; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return this.numMarkedNodes; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return this.numLinks; // should be replaced by student code
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return this.numCuts; // should be replaced by student code
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return this.numHeapify; // should be replaced by student code
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

        public HeapItem(int key, String info) {
            this.node = null;
            this.key = key;
            this.info = info;
        }

        @Override
        public int compareTo(HeapItem y) {
            return Integer.compare(this.key, y.key);
        }
    }
}
