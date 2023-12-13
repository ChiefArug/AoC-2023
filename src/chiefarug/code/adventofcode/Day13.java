package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day13 implements Day {
    record PatternMap(char[][] isRock) {

        int getScore() {
            // if vertical + left (index + 1)
            // if horizontal + 100 * above (index + 1)
            OptionalInt rowResult = IntStream.range(0, isRock.length - 1 /* subtract one from the exclusive end as we add one for secondRow*/)
                    .filter(i -> compareRows(i, i + 1))
                    .map(i -> ++i)
                    .map(i -> i * 100) // for horizontal lines multiply by 100
                    .findFirst();
            int result = rowResult.orElseGet(IntStream.range(0, isRock[0].length - 1 /* subtract one from the exclusive end as we add one for secondRow*/)
                    .filter(i -> compareColumns(i, i + 1))
                    .map(i -> ++i)
                    .findFirst()::getAsInt);
            System.out.println(result + " for array:\n" + this);
            return result;
        }


        boolean compareRows(int highRow, int lowRow) {
            if (highRow < 0 || lowRow >= isRock.length) return true; // it matches cause one of them could be anything as it is outside the bounds
            return Arrays.equals(isRock[highRow], isRock[lowRow]) && compareRows(highRow - 1, lowRow + 1);
        }

        boolean compareColumns(int leftRow, int rightRow) {
            if (leftRow < 0 || rightRow >= isRock[0].length) return true; // it matches cause one of them could be anything as it is outside the bounds
            return Arrays.stream(isRock).noneMatch(chars -> chars[leftRow] != chars[rightRow]) && compareColumns(leftRow - 1, rightRow + 1);
        }

        @Override
        public String toString() {
            return Arrays.stream(isRock).map(ca -> {
                StringBuilder sb = new StringBuilder(ca.length);
                for (int i = 0; i < ca.length; i++) {
                    sb.insert(i, ca[i]);
                }
                return sb.toString();
            }).collect(Collectors.joining("\n"));
        }
    }

    @Override
    public void run(BufferedReader input) {
        String[] sets = input.lines().collect(Collectors.joining(",")).split(",,");
        var result = Arrays.stream(sets)
                .map(s -> Arrays.stream(s.split(",")).map(String::toCharArray).toArray(char[][]::new))
                .map(PatternMap::new)
                .mapToInt(PatternMap::getScore)
                .sum();
        System.out.println(result);
    }

    @Override
    public int number() {
        return 13;
    }
}
