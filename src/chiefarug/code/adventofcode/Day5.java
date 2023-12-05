package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day5 implements Day {

    record LongRange(long min, long max) {
        boolean isInRange(long target) {
            return target >= min && target < max;
        }
    }

    // Map of the range to the difference between the min of range and the min of map to range
    static class SeedyMap extends HashMap<LongRange, Long> {
        final String name;
        SeedyMap(String name) {
            this.name = name;
        }

        void addMapping(long[] threeLengthArray) {
            if (threeLengthArray.length != 3)
                throw new RuntimeException("Erm, that doesn't work! Array length isn't 3. Array: " + Arrays.toString(threeLengthArray));
            addMapping(threeLengthArray[0], threeLengthArray[1], threeLengthArray[2]);
        }

        void addMapping(long to, long from, long range) {
            put(new LongRange(from, from + range), to - from);
        }

        long getTarget(long in) {
            for (LongRange longRange : keySet()) {
                if (longRange.isInRange(in)) {
                    return in + get(longRange);
                }
            }
            return in;
        }

        @Override
        public String toString() {
            return this.name + " " + super.toString();
        }
    }
    record LowestSeed(long seedId, long location) {}

    @Override
    public void run(BufferedReader input) {
        LowestSeed lowest = new LowestSeed(-1, Long.MAX_VALUE);
        try {
            // the first line, which is a list of seed numbers
            long[] seeds = Arrays.stream(input.readLine().substring(7).split(" ")).mapToLong(Long::parseLong).toArray();
            Arrays.sort(seeds);

            SeedyMap[] maps = Arrays.stream(
                            input.lines().filter(Predicate.not(String::isBlank)).collect(Collectors.joining(","))
                                    .split(",(?=[a-z]+-[a-z]+-[a-z]+ map:,)")
                    )
                    // split into name and numbers
                    .map(s -> s.split(":"))
                    .map(s -> {
                        SeedyMap map = new SeedyMap(s[0]);
                        System.out.println(Arrays.toString(s));
                        Arrays.stream(s[1].split(","))
                                .filter(Predicate.not(String::isBlank))
                                .forEach(numberString -> map.addMapping(Arrays.stream(numberString.split(" ")).mapToLong(Long::parseLong).toArray()));
                        return map;
                    })
                    .toArray(SeedyMap[]::new);


            for (long seedVaue : seeds) {
                long originalSeed = seedVaue;
                for (SeedyMap map : maps) {
                    seedVaue = map.getTarget(seedVaue);
                }
                if (seedVaue < lowest.location) lowest = new LowestSeed(originalSeed, seedVaue);
            }


            System.out.println(Arrays.toString(seeds));

        } catch (Exception e) {
            System.out.println("Current lowest: " + lowest);
            throw new RuntimeException(e);
        }
        System.out.println(lowest);
    }

    @Override
    public int number() {
        return 5;
    }
}
