import java.util.*;

public class ExperimentRunner {

    // הגדרת גודל הניסוי לפי דרישות הפרויקט
    private static final int N = 464646;
    private static final int REPETITIONS = 20;

    // הגדרת סוגי הערימות כפי שנדרש
    public enum HeapType {
        BINOMIAL,           // ערימה בינומית (רגילה)
        LAZY_BINOMIAL,      // ערימה בינומית עצלה
        FIBONACCI,          // ערימת פיבונאצ'י
        BINOMIAL_WITH_CUTS  // ערימה בינומית עם ניתוקים
    }

    // מפעל ליצירת ערימות לפי סוג
    public static Heap createHeap(HeapType type) {
        switch (type) {
            case BINOMIAL:
                // lazyMelds=false, lazyDecreaseKeys=false
                return new Heap(false, false);
            case LAZY_BINOMIAL:
                // lazyMelds=true, lazyDecreaseKeys=false
                return new Heap(true, false);
            case FIBONACCI:
                // lazyMelds=true, lazyDecreaseKeys=true
                return new Heap(true, true);
            case BINOMIAL_WITH_CUTS:
                // lazyMelds=false, lazyDecreaseKeys=true
                return new Heap(false, true);
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting experiments with N=" + N + " and " + REPETITIONS + " repetitions.");
        System.out.println("Please wait, this might take some time...\n");

        // נבצע את שלושת הניסויים
        runExperiment(1);
        runExperiment(2);
        runExperiment(3);
    }

    /**
     * פונקציה שמנהלת את הריצה של ניסוי ספציפי (1, 2 או 3)
     * עבור כל סוגי הערימות, ומדפיסה טבלה בסוף.
     */
    private static void runExperiment(int experimentNum) {
        System.out.println("=== Experiment " + experimentNum + " Results ===");
        
        // כותרות הטבלה (CSV format)
        System.out.println("Heap Type,Run Time (ms),Final Size,Num Trees,Total Links,Total Cuts,Total Heapify,Max Cost Per Op");

        // ריצה על כל סוג ערימה
        for (HeapType type : HeapType.values()) {
            ExperimentStats stats = new ExperimentStats();

            // ביצוע חזרות
            for (int i = 0; i < REPETITIONS; i++) {
                // הגרלת פרמוטציה חדשה לכל חזרה
                int[] data = generatePermutation(N);
                
                // יצירת ערימה חדשה
                Heap heap = createHeap(type);
                
                // ביצוע הניסוי הבודד ומדידת זמנים
                long startTime = System.currentTimeMillis();
                long maxCost = performSingleRun(heap, data, experimentNum);
                long endTime = System.currentTimeMillis();

                // צבירת נתונים לממוצע
                stats.addRun(
                    endTime - startTime,
                    heap.size(),
                    heap.numTrees(),
                    heap.totalLinks(),
                    heap.totalCuts(),
                    heap.totalHeapifyCosts(),
                    maxCost
                );
            }

            // הדפסת שורה בטבלה עבור סוג הערימה הנוכחי
            System.out.println(String.format("%s,%s", type, stats.getAveragesCSV()));
        }
        System.out.println("\n");
    }

    /**
     * מבצעת ריצה בודדת של ניסוי (1, 2 או 3) על ערימה נתונה עם דאטה נתון.
     * מחזירה את העלות המקסימלית לפעולה שנרשמה במהלך הריצה.
     */
    private static long performSingleRun(Heap heap, int[] data, int experimentNum) {
        // מערך עזר לשמירת המצביעים לאיברים (מפתח 1 נמצא באינדקס 1 וכו')
        Heap.HeapItem[] pointers = new Heap.HeapItem[N + 1];
        long maxOpCost = 0;

        // שלב 1 (זהה לכולם): הכנסת איברים 1..n
        for (int key : data) {
            String info = "val_" + key;
            // ביצוע הכנסה ומדידת עלות
            long cost = measureOpCost(heap, () -> {
                Heap.HeapItem item = heap.insert(key, info);
                pointers[key] = item; // שמירת המצביע
            });
            maxOpCost = Math.max(maxOpCost, cost);
        }

        // שלב 2 (זהה לכולם): מחיקת המינימום
        long costDelMin = measureOpCost(heap, heap::deleteMin);
        maxOpCost = Math.max(maxOpCost, costDelMin);

        // המשך הניסויים הספציפיים
        if (experimentNum == 1) {
            // ניסוי 1 מסתיים כאן
            return maxOpCost;
        }

        if (experimentNum == 2) {
            // ניסוי 2: מחיקת המקסימום עד שנשארים עם 46 איברים
            // המקסימום תמיד יהיה האיבר בעל המפתח הגדול ביותר שטרם נמחק.
            // מכיוון שהכנסנו 1..N, נרוץ מ-N למטה.
            int currentMax = N;
            int targetSize = 46;
            
            while (heap.size() > targetSize) {
                final int keyToDelete = currentMax;
                // ייתכן שהמקסימום כבר נמחק (ב-deleteMin הראשון)? 
                // אם מחקנו את 1, והלולאה מתחילה מ-N, אז לא. (N > 1 בהנחת N גדול)
                
                Heap.HeapItem item = pointers[keyToDelete];
                if (item != null) { // בדיקת שפיות
                    long cost = measureOpCost(heap, () -> heap.delete(item));
                    maxOpCost = Math.max(maxOpCost, cost);
                }
                currentMax--;
            }
        } 
        else if (experimentNum == 3) {
            // ניסוי 3: 0.1n פעמים הקטנת מפתח של המקסימום ל-0
            int limit = (int) (0.1 * N);
            int currentMax = N;
            
            for (int i = 0; i < limit; i++) {
                final int keyToDecrease = currentMax;
                Heap.HeapItem item = pointers[keyToDecrease];
                
                // הקטנה ל-0. ה-diff הוא הערך של המפתח עצמו (כי המפתח חיובי)
                long cost = measureOpCost(heap, () -> heap.decreaseKey(item, item.key));
                maxOpCost = Math.max(maxOpCost, cost);
                
                currentMax--;
            }
            
            // מחיקת מינימום שוב
            long costLastDel = measureOpCost(heap, heap::deleteMin);
            maxOpCost = Math.max(maxOpCost, costLastDel);
        }

        return maxOpCost;
    }

    /**
     * פונקציית עזר למדידת עלות פעולה בודדת.
     * העלות מוגדרת כסכום השינויים ב: Links + Cuts + Heapify
     */
    private static long measureOpCost(Heap heap, Runnable action) {
        long linksBefore = heap.totalLinks();
        long cutsBefore = heap.totalCuts();
        long heapifyBefore = heap.totalHeapifyCosts();

        action.run();

        long linksAfter = heap.totalLinks();
        long cutsAfter = heap.totalCuts();
        long heapifyAfter = heap.totalHeapifyCosts();

        return (linksAfter - linksBefore) + 
               (cutsAfter - cutsBefore) + 
               (heapifyAfter - heapifyBefore);
    }

    /**
     * יצירת פרמוטציה אקראית של המספרים 1..n
     */
    private static int[] generatePermutation(int n) {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    /**
     * מחלקה פנימית לצבירת סטטיסטיקות וחישוב ממוצעים
     */
    static class ExperimentStats {
        long totalRunTime = 0;
        long totalSize = 0;
        long totalTrees = 0;
        long totalLinks = 0;
        long totalCuts = 0;
        long totalHeapify = 0;
        long maxMaxCost = 0; // ה-Max cost המקסימלי שנצפה בכל הריצות (או ממוצע? הטבלה מבקשת ממוצע על פני 20 ניסויים, אבל עלות מקסימלית היא usually absolute max or average of maxes. נלך על ממוצע של המקסימום כפי שנהוג בניסויים כאלה)
        
        // עדכון: ההוראה היא "יש למלא בטבלה את הממוצע על פני 20 ניסויים".
        // לכן נסכום את ה-MaxCost של כל ריצה ונחלק ב-20.
        
        void addRun(long time, int size, int trees, int links, int cuts, int heapify, long maxCost) {
            totalRunTime += time;
            totalSize += size;
            totalTrees += trees;
            totalLinks += links;
            totalCuts += cuts;
            totalHeapify += heapify;
            maxMaxCost += maxCost;
        }

        String getAveragesCSV() {
            double avgTime = (double) totalRunTime / REPETITIONS;
            double avgSize = (double) totalSize / REPETITIONS;
            double avgTrees = (double) totalTrees / REPETITIONS;
            double avgLinks = (double) totalLinks / REPETITIONS;
            double avgCuts = (double) totalCuts / REPETITIONS;
            double avgHeapify = (double) totalHeapify / REPETITIONS;
            double avgMaxCost = (double) maxMaxCost / REPETITIONS;

            // עיצוב למספרים ללא נקודה עשרונית ארוכה מדי
            return String.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                    avgTime, avgSize, avgTrees, avgLinks, avgCuts, avgHeapify, avgMaxCost);
        }
    }
}