package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Day9 implements Day {

    record ReportLine(int[] original, List<int[]> predicted) {
        int getNextPredicted() {
            int next = 0;
            for (int i = predicted.size() - 1; i >= 0; i--) {
                int[] currentLevel = predicted.get(i);
                int last = currentLevel[currentLevel.length - 1];
                next = last + next;
            }
            return original[original.length - 1] + next;
        }
    }

    private int[] getNextLevel(int[] above) {
        int[] next = new int[above.length - 1];
        for (int i = 0; i < next.length; i++) {
            next[i] = above[i + 1] - above[i];
        }
        return next;
    }

    private boolean allTheSame(int[] toCheck) { // we can save a level of array by going to where they are all the same
        int first = toCheck[0];
        for (int i = 1; i < toCheck.length; i++) {
            if (toCheck[i] != first) return false;
        }
        return true;
    }

    @Override
    public void run(BufferedReader input) {
        AtomicLong count = new AtomicLong();

        try {
            /*var ints = */input.lines()
                    .map(s -> Arrays.stream(s.split(" "))
                            .mapToInt(Integer::parseInt)
                            .toArray())
                    .map(ia -> {
                        List<int[]> levels = new ArrayList<>();
                        int[] currentArray = ia;
                        do {
                            currentArray = getNextLevel(currentArray);
                            levels.add(currentArray);
                        }
                        while (!allTheSame(currentArray));
                        return new ReportLine(ia, levels);
                    })
                    .mapToInt(ReportLine::getNextPredicted)
                    .forEach(count::addAndGet);
            System.out.println(count);
        } catch (Exception e) {
            System.out.println("So far: " + count);
        }

    }

    @Override
    public int number() {
        return 9;
    }
}
