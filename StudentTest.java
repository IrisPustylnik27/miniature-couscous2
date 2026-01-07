import java.util.*;

public class StudentTest {

    // ---------------- mini framework ----------------
    private static int passed = 0, failed = 0;

    private static void check(String name, TestCase t) {
        try {
            if (t.run()) {
                passed++;
            } else {
                failed++;
                System.out.println("[FAIL] " + name);
            }
        } catch (Throwable e) {
            failed++;
            System.out.println("[EXCEPTION] " + name + " -> " + e);
            e.printStackTrace(System.out);
        }
    }

    @FunctionalInterface
    private interface TestCase { boolean run(); }

    private static boolean assertTrue(boolean cond) { return cond; }

    private static boolean assertEqualsInt(int a, int b) { return a == b; }

    private static boolean assertMinKey(Heap h, Integer expected) {
        Heap.HeapItem m = h.findMin();
        if (expected == null) return m == null;
        return m != null && m.key == expected;
    }

    private static List<Integer> drainKeysByDeleteMin(Heap h) {
        List<Integer> keys = new ArrayList<>();
        while (h.findMin() != null) {
            keys.add(h.findMin().key);
            h.deleteMin();
        }
        return keys;
    }

    private static Heap.HeapItem findAnyNonRoot(Heap.HeapItem[] items) {
        for (Heap.HeapItem it : items) {
            if (it != null && it.node != null && it.node.parent != null) return it;
        }
        return null;
    }

    // ---------------- tests ----------------

    // T1: empty heap behavior
    private static boolean tEmpty() {
        Heap h = new Heap(true, true);
        if (!assertEqualsInt(h.size(), 0)) return false;
        if (!assertEqualsInt(h.numTrees(), 0)) return false;
        if (!assertEqualsInt(h.numMarkedNodes(), 0)) return false;
        if (!assertMinKey(h, null)) return false;
        h.deleteMin(); // should not crash
        return assertMinKey(h, null) && assertEqualsInt(h.size(), 0);
    }

    // T2: single insert
    private static boolean tSingleInsert() {
        Heap h = new Heap(true, true);
        Heap.HeapItem a = h.insert(7, "A");
        return assertEqualsInt(h.size(), 1)
                && assertEqualsInt(h.numTrees(), 1)
                && assertMinKey(h, 7)
                && a != null && a.key == 7;
    }

    // T3: insert order, min
    private static boolean tInsertMin() {
        Heap h = new Heap(true, true);
        h.insert(10, "A");
        h.insert(5, "B");
        h.insert(20, "C");
        return assertEqualsInt(h.size(), 3)
                && assertMinKey(h, 5);
    }

    // T4: deleteMin from singleton
    private static boolean tDeleteMinSingleton() {
        Heap h = new Heap(true, true);
        h.insert(4, "A");
        h.deleteMin();
        return assertEqualsInt(h.size(), 0)
                && assertEqualsInt(h.numTrees(), 0)
                && assertMinKey(h, null);
    }

    // T5: deleteMin removes smallest
    private static boolean tDeleteMinBasic() {
        Heap h = new Heap(true, true);
        h.insert(10, "A");
        h.insert(5, "B");
        h.insert(20, "C");
        h.deleteMin();
        return assertEqualsInt(h.size(), 2)
                && assertMinKey(h, 10);
    }

    // T6: repeated deleteMin yields sorted order (small)
    private static boolean tDrainSortedSmall() {
        Heap h = new Heap(true, true);
        int[] keys = {7, 3, 9, 1, 8, 2};
        for (int k : keys) h.insert(k, "v");
        List<Integer> got = drainKeysByDeleteMin(h);
        int[] copy = Arrays.copyOf(keys, keys.length);
        Arrays.sort(copy);
        List<Integer> exp = new ArrayList<>();
        for (int x : copy) exp.add(x);
        return got.equals(exp) && assertEqualsInt(h.size(), 0) && assertMinKey(h, null);
    }

    // T7: delete arbitrary item reduces size and keeps order
    private static boolean tDeleteArbitrary() {
        Heap h = new Heap(true, true);
        Heap.HeapItem[] items = new Heap.HeapItem[21];
        for (int i = 1; i <= 20; i++) items[i] = h.insert(i, "v");
        h.delete(items[13]);
        if (!assertEqualsInt(h.size(), 19)) return false;

        List<Integer> got = drainKeysByDeleteMin(h);
        List<Integer> exp = new ArrayList<>();
        for (int i = 1; i <= 20; i++) if (i != 13) exp.add(i);
        return got.equals(exp);
    }

    // T8: decreaseKey makes new min (simple root case)
    private static boolean tDecreaseKeyRootBecomesMin() {
        Heap h = new Heap(true, true);
        Heap.HeapItem x = h.insert(50, "X");
        h.insert(60, "Y");
        h.decreaseKey(x, 49); // 50 -> 1
        return assertMinKey(h, 1) && assertEqualsInt(h.size(), 2);
    }

    // T9: decreaseKey keeps size, updates item.key
    private static boolean tDecreaseKeyUpdatesItem() {
        Heap h = new Heap(true, true);
        Heap.HeapItem x = h.insert(30, "X");
        int before = h.size();
        h.decreaseKey(x, 10); // 30 -> 20
        return assertEqualsInt(h.size(), before) && x.key == 20;
    }

    // T10: meld basics (lazyMelds=true): size and min, numTrees should add
    private static boolean tMeldLazy() {
        Heap h1 = new Heap(true, true);
        Heap h2 = new Heap(true, true);
        h1.insert(10, "a");
        h1.insert(7, "b");
        h2.insert(3, "c");
        h2.insert(20, "d");
        int t1 = h1.numTrees();
        int t2 = h2.numTrees();
        h1.meld(h2);

        return assertEqualsInt(h1.size(), 4)
                && assertMinKey(h1, 3)
                && assertEqualsInt(h1.numTrees(), t1 + t2);
    }

    // T11: meld then drain sorted
    private static boolean tMeldThenDrainSorted() {
        Heap h1 = new Heap(true, true);
        Heap h2 = new Heap(true, true);
        int[] a = {10, 4, 15, 2};
        int[] b = {11, 3, 8, 1};
        for (int x : a) h1.insert(x, "a");
        for (int x : b) h2.insert(x, "b");
        h1.meld(h2);

        List<Integer> got = drainKeysByDeleteMin(h1);
        int[] all = new int[a.length + b.length];
        System.arraycopy(a, 0, all, 0, a.length);
        System.arraycopy(b, 0, all, a.length, b.length);
        Arrays.sort(all);
        List<Integer> exp = new ArrayList<>();
        for (int x : all) exp.add(x);

        return got.equals(exp);
    }

    // T12: counters monotonic sanity (links/cuts never decrease)
    private static boolean tCountersMonotonic() {
        Heap h = new Heap(true, true);
        int links0 = h.totalLinks();
        int cuts0 = h.totalCuts();

        Heap.HeapItem[] items = new Heap.HeapItem[101];
        for (int i = 1; i <= 100; i++) items[i] = h.insert(i, "v");
        h.deleteMin();

        int links1 = h.totalLinks();
        if (links1 < links0) return false;

        Heap.HeapItem nonRoot = findAnyNonRoot(items);
        if (nonRoot == null) {
            // If no non-root exists, you didn't consolidate; this test can’t proceed.
            // Return false so you notice.
            return false;
        }

        int target = h.findMin().key - 1;
        int diff = nonRoot.key - target;
        if (diff <= 0) diff = 1;
        h.decreaseKey(nonRoot, diff);

        int cuts1 = h.totalCuts();
        return cuts1 >= cuts0;
    }

    // T13: numTrees is never negative; numMarkedNodes never negative
    private static boolean tCountersNonNegative() {
        Heap h = new Heap(true, true);
        for (int i = 50; i >= 1; i--) h.insert(i, "v");
        h.deleteMin();
        h.deleteMin();
        if (h.numTrees() < 0) return false;
        if (h.numMarkedNodes() < 0) return false;
        return true;
    }

    // T14: cascading cuts actually increases totalCuts (lazyDecreaseKeys=true)
    // This is the robust version: create structure then pick a real non-root.
    private static boolean tCascadingCutIncreasesCuts() {
        Heap h = new Heap(true, true);

        Heap.HeapItem[] items = new Heap.HeapItem[301];
        for (int i = 1; i <= 300; i++) items[i] = h.insert(i, "v");

        // must create non-roots (requires your deleteMin to consolidate)
        h.deleteMin();

        Heap.HeapItem x = findAnyNonRoot(items);
        if (x == null) return false;

        int cutsBefore = h.totalCuts();
        int sizeBefore = h.size();

        int target = h.findMin().key - 1;
        int diff = x.key - target;
        if (diff <= 0) diff = 1;
        h.decreaseKey(x, diff);

        return h.totalCuts() > cutsBefore && h.size() == sizeBefore && h.findMin().key == x.key;
    }

    // T15: non-lazy decrease mode should not do cuts (lazyDecreaseKeys=false)
    private static boolean tNonLazyDecreaseDoesNotCut() {
        Heap h = new Heap(true, false);
        Heap.HeapItem[] items = new Heap.HeapItem[151];
        for (int i = 1; i <= 150; i++) items[i] = h.insert(i, "v");

        h.deleteMin();

        Heap.HeapItem x = findAnyNonRoot(items);
        if (x == null) return false;

        int cutsBefore = h.totalCuts();
        int sizeBefore = h.size();

        int target = h.findMin().key - 1;
        int diff = x.key - target;
        if (diff <= 0) diff = 1;
        h.decreaseKey(x, diff);

        return h.totalCuts() == cutsBefore && h.size() == sizeBefore && h.findMin().key == x.key;
    }

    // T16: randomized insert-deleteMin matches sorting (medium)
    private static boolean tRandomInsertDeleteMin() {
        Heap h = new Heap(true, true);
        Random rnd = new Random(2026);

        int n = 2000;
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = 1 + rnd.nextInt(200000);
            h.insert(keys[i], "v");
        }
        Arrays.sort(keys);

        for (int i = 0; i < n; i++) {
            Heap.HeapItem m = h.findMin();
            if (m == null || m.key != keys[i]) return false;
            h.deleteMin();
        }
        return h.size() == 0 && h.findMin() == null;
    }

    // T17: deleteMin repeatedly shouldn't crash (even if called on empty at end)
    private static boolean tDeleteMinManyTimesNoCrash() {
        Heap h = new Heap(true, true);
        for (int i = 1; i <= 100; i++) h.insert(i, "v");
        for (int i = 0; i < 110; i++) h.deleteMin(); // 10 extra on empty
        return h.findMin() == null && h.size() == 0;
    }

    private static boolean isNonDecreasing(List<Integer> a) {
        for (int i = 1; i < a.size(); i++) if (a.get(i) < a.get(i-1)) return false;
        return true;
    }

    private static List<Integer> toSortedList(int[] arr) {
        int[] b = Arrays.copyOf(arr, arr.length);
        Arrays.sort(b);
        List<Integer> out = new ArrayList<>();
        for (int x : b) out.add(x);
        return out;
    }

    private static Heap.HeapItem pickNonRoot(Heap.HeapItem[] items) {
        for (Heap.HeapItem it : items) {
            if (it != null && it.node != null && it.node.parent != null) return it;
        }
        return null;
    }
    private static boolean tFindMinAfterManyInserts() {
        Heap h = new Heap(true, true);
        int n = 5000;
        int min = Integer.MAX_VALUE;
        Random rnd = new Random(1);
        for (int i = 0; i < n; i++) {
            int k = 1 + rnd.nextInt(1_000_000);
            min = Math.min(min, k);
            h.insert(k, "v");
            if (h.findMin() == null || h.findMin().key != min) return false;
        }
        return h.size() == n;
    }

    private static boolean tSortedInsertsThenDeleteMin() {
        Heap h = new Heap(true, true);
        for (int i = 1; i <= 2000; i++) h.insert(i, "v");
        for (int i = 1; i <= 2000; i++) {
            if (h.findMin() == null || h.findMin().key != i) return false;
            h.deleteMin();
        }
        return h.size() == 0 && h.findMin() == null;
    }

    private static boolean tReverseInsertsThenDrainSorted() {
        Heap h = new Heap(true, true);
        for (int i = 2000; i >= 1; i--) h.insert(i, "v");
        List<Integer> got = drainKeysByDeleteMin(h);
        if (got.size() != 2000) return false;
        return isNonDecreasing(got) && got.get(0) == 1 && got.get(got.size()-1) == 2000;
    }

    private static boolean tSizeAccounting() {
        Heap h = new Heap(true, true);
        Heap.HeapItem a = h.insert(10, "a");
        Heap.HeapItem b = h.insert(20, "b");
        if (h.size() != 2) return false;

        int before = h.size();
        h.decreaseKey(b, 5);          // should not change size
        if (h.size() != before) return false;

        h.deleteMin();                 // size--
        if (h.size() != 1) return false;

        h.delete(a);                   // size--
        return h.size() == 0 && h.findMin() == null;
    }

    private static boolean tNumTreesBounds() {
        Heap h = new Heap(true, true);
        for (int i = 1; i <= 300; i++) h.insert(i, "v");
        if (h.numTrees() < 0 || h.numTrees() > h.size()) return false;
        for (int i = 0; i < 50; i++) {
            h.deleteMin();
            if (h.numTrees() < 0 || h.numTrees() > h.size()) return false;
        }
        return true;
    }

    private static boolean tMarkedNeverNegative() {
        Heap h = new Heap(true, true);
        Heap.HeapItem[] items = new Heap.HeapItem[1001];
        for (int i = 1; i <= 1000; i++) items[i] = h.insert(i, "v");

        // force consolidation
        h.deleteMin();

        Random rnd = new Random(2);
        for (int step = 0; step < 300; step++) {
            int idx = 2 + rnd.nextInt(999);
            Heap.HeapItem x = items[idx];
            if (x == null) continue;

            // decrease moderately
            int diff = Math.min(1 + rnd.nextInt(10), x.key);
            h.decreaseKey(x, diff);

            if (h.numMarkedNodes() < 0) return false;
        }
        return true;
    }

    private static boolean tNonLazyManyDecreaseNoCuts() {
        Heap h = new Heap(true, false); // lazyDecreaseKeys=false
        Heap.HeapItem[] items = new Heap.HeapItem[2001];
        for (int i = 1; i <= 2000; i++) items[i] = h.insert(i, "v");

        h.deleteMin(); // make structure

        int cuts0 = h.totalCuts();
        Random rnd = new Random(3);
        for (int step = 0; step < 500; step++) {
            Heap.HeapItem x = items[2 + rnd.nextInt(1999)];
            if (x == null) continue;
            int diff = Math.min(1 + rnd.nextInt(20), x.key);
            h.decreaseKey(x, diff);
            if (h.totalCuts() != cuts0) return false;
        }
        return true;
    }

    private static boolean tNonLazyCanStillUpdateMin() {
        Heap h = new Heap(true, false);
        Heap.HeapItem[] items = new Heap.HeapItem[501];
        for (int i = 1; i <= 500; i++) items[i] = h.insert(1000 + i, "v");

        h.deleteMin(); // consolidate

        Heap.HeapItem x = items[500];
        int target = (h.findMin() == null) ? 0 : h.findMin().key - 1;
        int diff = x.key - target;
        if (diff <= 0) diff = 1;

        int cutsBefore = h.totalCuts();
        h.decreaseKey(x, diff);

        return h.totalCuts() == cutsBefore && h.findMin() != null && h.findMin().key == x.key;
    }

    private static boolean tRandomAgainstPriorityQueue() {
        Heap h = new Heap(true, true);
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        Random rnd = new Random(4);

        for (int step = 0; step < 5000; step++) {
            boolean doInsert = pq.isEmpty() || rnd.nextInt(100) < 70;
            if (doInsert) {
                int k = 1 + rnd.nextInt(200000);
                h.insert(k, "v");
                pq.add(k);
            } else {
                Integer pqMin = pq.poll();
                Heap.HeapItem hm = h.findMin();
                if (pqMin == null) {
                    if (hm != null) return false;
                    h.deleteMin(); // should be safe
                } else {
                    if (hm == null || hm.key != pqMin) return false;
                    h.deleteMin();
                }
            }

            Heap.HeapItem hm = h.findMin();
            Integer pqPeek = pq.peek();
            if (pqPeek == null) {
                if (hm != null) return false;
            } else {
                if (hm == null || hm.key != pqPeek) return false;
            }
            if (h.size() != pq.size()) return false;
        }
        return true;
    }



    // ---------------- main ----------------
    public static void main(String[] args) {
        check("T1 empty heap", StudentTest::tEmpty);
        check("T2 single insert", StudentTest::tSingleInsert);
        check("T3 insert min", StudentTest::tInsertMin);
        check("T4 deleteMin singleton", StudentTest::tDeleteMinSingleton);
        check("T5 deleteMin basic", StudentTest::tDeleteMinBasic);
        check("T6 drain sorted small", StudentTest::tDrainSortedSmall);
        check("T7 delete arbitrary", StudentTest::tDeleteArbitrary);
        check("T8 decreaseKey root becomes min", StudentTest::tDecreaseKeyRootBecomesMin);
        check("T9 decreaseKey updates item", StudentTest::tDecreaseKeyUpdatesItem);
        check("T10 meld lazy", StudentTest::tMeldLazy);
        check("T11 meld then drain sorted", StudentTest::tMeldThenDrainSorted);
        check("T12 counters monotonic", StudentTest::tCountersMonotonic);
        check("T13 counters non-negative", StudentTest::tCountersNonNegative);
        check("T14 cascading cut increases cuts", StudentTest::tCascadingCutIncreasesCuts);
        check("T15 non-lazy decrease does not cut", StudentTest::tNonLazyDecreaseDoesNotCut);
        check("T16 random insert/deleteMin", StudentTest::tRandomInsertDeleteMin);
        check("T17 deleteMin many times no crash", StudentTest::tDeleteMinManyTimesNoCrash);
        check("T18 findMin after many inserts", StudentTest::tFindMinAfterManyInserts);
        check("T19 sorted inserts then deleteMin", StudentTest::tSortedInsertsThenDeleteMin);
        check("T20 reverse inserts then drain sorted", StudentTest::tReverseInsertsThenDrainSorted);
        check("T21 size accounting", StudentTest::tSizeAccounting);
        check("T22 numTrees bounds", StudentTest::tNumTreesBounds);
        check("T23 marked never negative", StudentTest::tMarkedNeverNegative);
        check("T24 non-lazy many decrease no cuts", StudentTest::tNonLazyManyDecreaseNoCuts);
        check("T25 non-lazy can update min", StudentTest::tNonLazyCanStillUpdateMin);
        check("T26 random vs PriorityQueue", StudentTest::tRandomAgainstPriorityQueue);

        System.out.println();
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed == 0) System.out.println("All tests passed!");
    }
}
