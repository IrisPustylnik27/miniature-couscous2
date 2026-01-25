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
     * O(1) if lazyMeld is true and O(logn) if false.
     *
     */
    public HeapItem insert(int key, String info)
    {
        HeapNode node = NewNode(key, info);
        Heap heap2 = NewHeap(node);
        meld(heap2);
        return node.item;
    }

    /**
     *
     * Build new node.
     *
     * O(1).
     *
     */

    public HeapNode NewNode(int key, String info){
        HeapItem item = new HeapItem(key, info);
        HeapNode node = new HeapNode(item);
        item.node = node;
        return node;
    }

    /**
     *
     * Build new heap from node.
     *
     * O(1).
     *
     */

    public Heap NewHeap(HeapNode node){
        Heap heap2 = new Heap(lazyMelds, lazyDecreaseKeys);
        heap2.start = node;
        node.next = node;
        node.prev = node;
        heap2.size++;
        heap2.numTrees++;
        heap2.min = node.item;
        return heap2;
    }

    /**
     *
     * Return the minimal HeapItem, null if empty.
     *
     * O(1).
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
     * O(logn).
     *
     */
    public void deleteMin() {
        if (this.min == null) {
            return;
        }

        boolean fl = min.node.child == null;

        Heap minHeap = null;
        if (!fl) {
            minHeap = new Heap(lazyMelds, lazyDecreaseKeys);
            minHeap.start = min.node.child;
            minHeap.updatingParent(minHeap.start, null);

        }

        this.removeFromTheRootList(min.node);
        this.size--;
        this.numTrees--;

        if (fl) {
            successiveLinking();
            return;
        }

        minHeap.successiveLinking();


        meld(minHeap);
        if (lazyMelds) {
            successiveLinking();
        }

        this.updatingMarking(this.start);
    }


    /**
     *
     * Update marking in root list.
     *
     * O(k) when k is number of items in the list.
     *
     */

    public void updatingMarking(HeapNode node){
        if (node == null) {
            return;
        }
        HeapNode cnt = node;
        do{
            if (cnt.marked) {
                this.numMarkedNodes--;
                cnt.marked = false;
            }
            cnt = cnt.next;
        }while(cnt != node);
    }

    /**
     *
     * Update parent in list.
     *
     * O(k) when k is number of items in the list.
     *
     */

    public void updatingParent(HeapNode node, HeapNode newParent){
        if (node == null) {
            return;
        }
        HeapNode cnt = node;
        do{
            cnt.parent = newParent;
            cnt = cnt.next;
        }while(cnt != node);
    }

    /**
     *
     * pre: 0<=diff<=x.key
     *
     * Decrease the key of x by diff and fix the heap.
     *
     * O(logn), if lazyMelds = false and lazyDecreaseKeys = true then O((logn)^2).
     *
     */
    public void decreaseKey(HeapItem x, int diff)
    {
        x.key = x.key - diff;
        if (x.node.parent == null) {
            if (x.compareTo(this.min) < 0) {
                this.min = x;
            }
            return;
        }
        if (x.key >= x.node.parent.item.key){
            return;
        }
        if (!this.lazyDecreaseKeys) {
            heapifyUp(x);
        } else {
            cascadingCuts(x);
        }

        if (this.min == null || x.compareTo(this.min) < 0) {
            this.min = x;
        }

    }

    /**
     *
     * Cuts till we find not marked item.
     *
     * O(logn), if lazyMeld = false then O((logn)^2).
     *
     */


    public void cascadingCuts(HeapItem currItem)
    {
        if (currItem.node.parent == null) {
            return;
        }
        HeapItem parentItem = currItem.node.parent.item;
        cut(currItem);
        Heap heap2 = NewHeap(currItem.node);
        heap2.size = 0;
        meld(heap2);
        if(parentItem.node.parent != null){
            if (!parentItem.node.marked){
                parentItem.node.marked = true;
                numMarkedNodes++;
            }
            else{
                cascadingCuts(parentItem);
            }
        }
    }

    /**
     *
     * Cutting subtree from the parent.
     *
     * O(1).
     *
     */


    public void cut(HeapItem currItem)
    {
        if (currItem.node.parent == null) {
            return;
        }

        HeapItem parentItem = currItem.node.parent.item;

        if (currItem.node.marked){
            numMarkedNodes--;
        }
        currItem.node.marked = false;
        this.removeChild(parentItem.node, currItem.node);
        numCuts++;
    }

    /**
     *
     * Remove one of the children from given parent.
     *
     * O(1).
     *
     */

    public void removeChild(HeapNode parent, HeapNode node) {
        if (node.next == node) {
            parent.child = null;
        } else {
            if (parent.child == node) parent.child = node.next;
            removeFromTheCircularList(node);
        }
        node.parent = null;
        parent.rank--;
    }

    /**
     *
     * Changing items of parent and child till tree is right.
     *
     * O(logn).
     *
     */


    public void heapifyUp(HeapItem x)
    {
        while((x.node.parent != null) && (x.key < x.node.parent.item.key)){
            change(x.node, x.node.parent);
            numHeapify++;
        }
    }


    /**
     *
     * Delete the x from the heap.
     *
     * O(logn).
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
     * O(1) if lazyMeld is true, O(logn) if false.
     *
     */
    public void meld(Heap heap2)
    {
        if (this.start == null){
            this.start = heap2.start;
            this.size += heap2.size;
            this.min = heap2.min;
            this.numTrees = heap2.numTrees;
            this.numMarkedNodes += heap2.numMarkedNodes;
            //do we need to upgrade cuts, links and heapyfy?
            heap2 = null;
            return;
        } else if (heap2.start == null){
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
        this.numMarkedNodes += heap2.numMarkedNodes();

        if (!lazyMelds){
            this.successiveLinking();
        }
        heap2 = null;
    }

    /**
     *
     * Linking trees with the same rank.
     *
     * O(logn).
     *
     */

    public void successiveLinking(){
        if (this.start == null) return;
        ArrayList<HeapNode> nodes = new ArrayList<>(this.numTrees);
        HeapNode stopper = this.start;
        HeapNode cnt = this.start;
        do{
            nodes.add(cnt);
            cnt = cnt.next;
        }while(cnt != stopper);

        for(HeapNode node: nodes){
            node.next = node;
            node.prev = node;
            node.parent = null;
        }

        int n = this.size();
        int len = (n <= 1) ? 10:(int) Math.ceil(Math.log(n)/Math.log(2))+10;
        HeapNode[] degreeTable = new HeapNode[len];

        for(HeapNode node: nodes){
            int cr = node.rank;
            while (degreeTable[cr] != null){
                node = link(node, degreeTable[cr]);
                degreeTable[cr] = null;
                cr = node.rank;
            }
            degreeTable[cr] = node;
        }

        this.start = null;
        this.min = null;
        this.numTrees = 0;

        for (HeapNode x: degreeTable) {
            if (x == null) {continue;}
            if (this.min == null) {
                min = x.item;
                this.start = x;
            } else {
                x.prev = this.start.prev;
                x.next = this.start;
                this.start.prev.next = x;
                this.start.prev = x;
                if (x.item.compareTo(this.min) < 0) {
                    this.min = x.item;
                }
            }
            this.numTrees++;
        }

    }

    /**
     *
     * Removing item from the root list.
     *
     * O(1).
     *
     */

    public void removeFromTheRootList(HeapNode node){
        if (node == null) return;
        if (node.next == node) {
            this.start = null;
        }else{
            if (node == this.start) this.start = node.next;
            removeFromTheCircularList(node);
        }
    }

    /**
     *
     * Removing item from the list.
     *
     * O(1).
     *
     */

    public void removeFromTheCircularList(HeapNode node){
        if (node == null) {
            return;
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = node;
        node.next = node;
    }

    /**
     *
     * Linking two trees together.
     *
     * O(1).
     *
     */


    public HeapNode link(HeapNode x, HeapNode y)
    {
        if (x.item.compareTo(y.item)>0){
            HeapNode temp = x;
            x = y;
            y = temp;
        }
        if (x.child == null){
            y.next = y;
            y.prev = y;
        } else{
            y.next = x.child;
            y.prev = x.child.prev;
            x.child.prev.next = y;
            x.child.prev = y;
        }
        x.child = y;
        y.parent = x;
        this.numLinks++;
        x.rank++;
        if (y.marked){
            numMarkedNodes--;
        }
        y.marked = false;
        return x;
    }

    /**
     *
     * Changing items of two given nodes.
     *
     * O(1).
     *
     */

    public void change(HeapNode x, HeapNode y){
        HeapItem temp = x.item;
        x.item = y.item;
        y.item = temp;

        y.item.node = y;
        x.item.node = x;

    }


    /**
     *
     * Return the number of elements in the heap.
     *
     * O(1).
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
     * O(1).
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
     * O(1).
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
     * O(1).
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
     * O(1).
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
     * O(1).
     *
     */
    public int totalHeapifyCosts()
    {
        return this.numHeapify; // should be replaced by student code
    }


    /**
     * Class implementing a node in a Heap.
     *
     *
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public boolean marked;

        public HeapNode(HeapItem item){
            this.item = item;
            child = null;
            next = null;
            prev = null;
            parent = null;
            marked = false;
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
