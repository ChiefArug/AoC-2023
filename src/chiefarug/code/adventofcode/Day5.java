package chiefarug.code.adventofcode;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day5 implements Day {

    record LongRange(long min, long max) implements Comparable<LongRange> {
        static LongRange of(long min, long max) {
            if (max < min)
                throw new IllegalArgumentException("min greater than max");
            return new LongRange (min, max);
        }

        static LongRange ofShifted(long min, long max, long shift) {
            return new LongRange(min + shift, max + shift);
        }

        LongRange shift(long shiftAmount) {
            return new LongRange(min + shiftAmount, max + shiftAmount);
        }
        
        boolean isInRange(long target) {
            return target >= min && target < max;
        }

        // info about comment notation:
        // / is used to represent one end of the other longrange
        // \ is used to represent one end of this longrange
        // - is used to represent this range occupying a space
        // _ is used to represent the other range occupying the space
        // = is used to represent two ranges overlapping in that space
        // > is used to represent a range continuing, usually for purposes
        // If the returned array is null, then the ranges do not match.
        // Otherwise, the first element in the array has been shifted as needed, and the rest may need further processing.
        @Nullable
        LongRange[] intersectWithAndShift(LongRange other, long shift) {
            if (this.equals(other)) return new LongRange[]{this.shift(shift)};
            // Their min is less than our min
            //        shift>
            // /______\=====>
            if (other.min <= min) {
                // Their max is greater than or equal to our max ( we are completely enclosed within it)
                //      shift
                // /___\=====\___/
                if (other.max >= max) {
                    return new LongRange[]{this.shift(shift)};
                // Their max is greater than our min
                //        shift
                // /_____\=====/-----\
                } else if (other.max > min){ // we are split in two by the other range. the first half is outside, the second half is inside
                    return new LongRange[]{LongRange.ofShifted(min, other.max, shift), LongRange.of(other.max, max)};
                }
                // We are completely after this range, so we cannot intersect it range
                // /_____/ \---\
                return null;

            // Their min is less than our max, but greater than our min
            //       shift>
            // \----/=====>
            } else if (other.min <= max) {
                var start = LongRange.of(min, other.min - 1); // minus one to prevent infinite recursion
                // Their max is less than our max, so it is completely encased in us
                //        shift
                // \-----/=====/-----\
                if (other.max < max) {
                    var middle = other.shift(shift);
                    var end = LongRange.of(other.max, max);
                    return new LongRange[]{middle, start, end};
                // Their max is greater than our max, so it is completely encased in us
                //        shift
                // \-----/=====\_____/
                } else {
                    var end = LongRange.ofShifted(other.min, max, shift);
                    return new LongRange[]{end, start};
                }
            }
            // we are completely before the other range, so we cannot intersect with this.
            // \-----\ /_____/
            return null;
        }

        @Override
        public int compareTo(LongRange lr) {
            return Long.compare(min, lr.min);
        }
    }

    // Map of the range to the difference between the min of range and the min of map to range
    static class SeedyMap extends TreeMap<LongRange, Long> {
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
            put(LongRange.of(from, from + range), to - from);
        }

        LongRange[] getTarget(LongRange in) {
            for (LongRange longRange : keySet()) {
                LongRange[] ranges = in.intersectWithAndShift(longRange, get(longRange));
                if (ranges != null) {
                    if (ranges.length == 1)
                        return ranges;
                    LongRange shifted = ranges[0];
                    if (ranges.length == 2)
                        return Stream.concat(Stream.of(shifted), Arrays.stream(getTarget(ranges[1]))).toArray(LongRange[]::new);
                    if (ranges.length == 3)
                        return Stream.concat(Stream.of(shifted), Stream.concat(Arrays.stream(getTarget(ranges[1])), Arrays.stream(getTarget(ranges[2])))).toArray(LongRange[]::new);
                    throw new RuntimeException("How did that happen? Got array of length " + ranges.length + " out of intersectWithAndShift!");
                }

            }
            return new LongRange[]{in};
        }

        @Override
        public String toString() {
            return this.name + " " + super.toString();
        }
    }

    @Override
    public void run(BufferedReader input) {
//        LowestSeed lowest = new LowestSeed(-1, Long.MAX_VALUE);
        try {
            // the first line, which is a list of seed numbers
            long[] seedValues = Arrays.stream(input.readLine().substring(7).split(" ")).mapToLong(Long::parseLong).toArray();
            LongRange[] seeds = new LongRange[seedValues.length / 2];
            for (int i = 0; i < seedValues.length; i += 2) {
                seeds[(i / 2)] = LongRange.of(seedValues[i], seedValues[i] + seedValues[i + 1]);
            }
            Arrays.sort(seeds);

            SeedyMap[] maps = Arrays.stream(
                            input.lines().filter(Predicate.not(String::isBlank)).collect(Collectors.joining(","))
                                    .split(",(?=[a-z]+-[a-z]+-[a-z]+ map:,)")
                    )
                    // split into name and numbers
                    .map(s -> s.split(":"))
                    .map(s -> {
                        SeedyMap map = new SeedyMap(s[0]);
                        Arrays.stream(s[1].split(","))
                                .filter(Predicate.not(String::isBlank))
                                .forEach(numberString -> map.addMapping(Arrays.stream(numberString.split(" ")).mapToLong(Long::parseLong).toArray()));
                        return map;
                    })
                    .toArray(SeedyMap[]::new);



            Stream<LongRange> seedStream = Arrays.stream(seeds);
            for (SeedyMap map : maps) {
                // because the seed map is ordered, we can iterate it like this.
                System.out.println("adding flatmap op for " + map.name);
                seedStream = seedStream.flatMap(seed -> Arrays.stream(map.getTarget(seed)));
            }
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            LongRange smallest = seedStream.sorted().findFirst().get();
            System.out.println(smallest);

//            System.out.println(Arrays.toString(seedValues));

        } catch (Exception e) {
//            System.out.println("Current lowest: " + lowest);
            throw new RuntimeException(e);
        }
//        System.out.println(lowest);
    }

    @Override
    public int number() {
        return 5;
    }
}
