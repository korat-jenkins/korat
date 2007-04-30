package korat.utils.cv;

public class C {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Pair p;
        koratInit(args[1]);
        p = oldAlgorithm(n);
        print("old: ", p.candidates, p.index);
        koratInit(args[1]);
        p = newAlgorithm(n);
        print("new: ", p.candidates, p.index);
    }

    static class Pair {
        Candidate[] candidates;

        int index;

        Pair(Candidate[] c, int i) {
            candidates = c;
            index = i;
        }
    }

    static class Candidate {
        long l;

        Candidate(long l) {
            this.l = l;
        }

        public String toString() {
            return l + "";
        }
    }

    // this "new" algorithm keeps the starting index for each range
    // it turns it computes the same as the old algorithm (but has
    // one 0 more at the beginning of the array)
    static Pair newAlgorithm(int n) {
        Candidate[] candidates = new Candidate[2 * n];
        int distance = 1;
        int index = 1;
        while (Korat_hasNext()) {
            int i = 0;
            if (index == candidates.length) {
                // print("> ", candidates, index);
                for (int j = 1; j < candidates.length / 2; j++) {
                    candidates[j] = candidates[2 * j];
                }
                i = distance;
                distance = distance * 2;
                index = n;
            }
            // print("", candidates, index);
            for (; i < distance; i++) {
                candidates[index] = Korat_next();
                if (!Korat_hasNext())
                    break;
            }
            index++;
        }
        return new Pair(candidates, index);
        // this should actually return one Candidate[n] array by shifting
        // some elements to the left, but I did this just for comparison of
        // algorithms
    }

    // this old algorithm keeps the ending index for each range
    // there was a bug in the paper with missing "-1"
    // now it seems the work the way it should; i also tried to change the code
    // accordingly, but please check that
    static Pair oldAlgorithm(int n) {
        Candidate[] candidates = new Candidate[2 * n];
        int distance = 1;
        int index = 0;
        while (Korat_hasNext()) {
            int i = 0;
            while (i < distance) {
                candidates[index] = Korat_next();
                i++;
                if (!Korat_hasNext())
                    break;
            }
            if (index < candidates.length - 1)
                index++; // ////////////// paper missed this -1
            else { // index == candidates.length - 1 //////////////// paper
                    // missed this -1
                for (int j = 0; j < candidates.length / 2; j++) {
                    candidates[j] = candidates[2 * j + 1];
                }
                distance = distance * 2;
                index = n;
            }
        }
        return new Pair(candidates, index);
        // this should actually return one Candidate[n] array by shifting
        // some elements to the left, but I did this just for comparison of
        // algorithms
    }

    static void print(String prefix, Candidate[] candidates, int index) {
        System.out.print(prefix);
        for (int k = 0; k < index; k++)
            System.out.print(nullToZero(candidates[k]) + ",");
        System.out.println();
    }

    static String nullToZero(Candidate c) {
        if (c == null)
            return "0";
        return c.toString();
    }

    static long length;

    static long l;

    static void koratInit(String s) {
        length = Long.parseLong(s);
        l = 0;
    }

    static boolean Korat_hasNext() {
        return l < length;
    }

    static Candidate Korat_next() {
        return new Candidate(++l);
    }
}
