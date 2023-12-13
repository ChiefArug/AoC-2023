package chiefarug.code.adventofcode;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day13 implements Day {
    record PatternMap(char[][] rocksAndAsh, boolean isOriginal) {

        int getScore(int originalScore) {
            // if vertical + left (index + 1)
            // if horizontal + 100 * above (index + 1)
            int[] rowResult = IntStream.range(0, rocksAndAsh.length - 1 /* subtract one from the exclusive end as we add one for secondRow*/)
                    .filter(i -> compareRows(i, i + 1))
                    .map(i -> ++i) // increase by one because the mirror line is one below the index
                    .map(i -> i * 100) // for horizontal lines multiply by 100
                    .filter(i -> i != originalScore)
                    .toArray();
            int[] columnResult = IntStream.range(0, rocksAndAsh[0].length - 1 /* subtract one from the exclusive end as we add one for secondRow*/)
                    .filter(i -> compareColumns(i, i + 1))
                    .map(i -> ++i) // increase by one because the mirror line is one to the right of index
                    .filter(i -> i != originalScore)
                    .toArray();

            int result = -1;
            for (int i : rowResult) {
                if (result != -1) throw new WatException("Multiple results found for:\n" + this);
                result = i;
            }
            for (int i : columnResult) {
                if (result != -1) throw new WatException("Multiple results found for:\n" + this);
                result = i;
            }


//            System.out.println((isOriginal ? "[Old] " : "[New] ") + result + " for array:\n" + this + "\n");
            return result;
        }


        boolean compareRows(int highRow, int lowRow) {
            if (highRow < 0 || lowRow >= rocksAndAsh.length)
                return true; // it matches cause one of them could be anything as it is outside the bounds
            return Arrays.equals(rocksAndAsh[highRow], rocksAndAsh[lowRow]) && compareRows(highRow - 1, lowRow + 1);
        }

        boolean compareColumns(int leftRow, int rightRow) {
            if (leftRow < 0 || rightRow >= rocksAndAsh[0].length)
                return true; // it matches cause one of them could be anything as it is outside the bounds
            return Arrays.stream(rocksAndAsh).noneMatch(chars -> chars[leftRow] != chars[rightRow]) && compareColumns(leftRow - 1, rightRow + 1);
        }

        @Override
        public String toString() {
            return Arrays.stream(rocksAndAsh).map(ca -> {
                StringBuilder sb = new StringBuilder(ca.length);
                for (int i = 0; i < ca.length; i++) {
                    sb.insert(i, ca[i]);
                }
                return sb.toString();
            }).collect(Collectors.joining("\n"));
        }
    }

    record Pair<L, R>(L left, R right) {}

    @Override
    public void run(BufferedReader input) {
        String[] sets = input.lines().collect(Collectors.joining(",")).split(",,");
        var result = Arrays.stream(sets)
                .map(s -> Arrays.stream(s.split(",")).map(String::toCharArray).toArray(char[][]::new))
                .map(caa -> new Pair<>(new PatternMap(caa, true),  // pair the original with a stream of new alternatives
                        // The below four lines loop over ever x and y position in the grid and create a new pattern for each position, putting each of those patterns into a stream
                        IntStream.range(0, caa.length).mapToObj(y ->
                                IntStream.range(0, caa[y].length).mapToObj(x ->
                                        newPatternMapWithSingleInverted(caa, y, x)
                                )).flatMap(Function.identity())
                ))
                // each inner stream is for combinations for an input grid
                .mapToInt(p -> {
                    int originalScore = p.left().getScore(-1); // if anything has the same score it is INVALID and should be YEETED
                    return p.right()
                            .mapToInt(pattern -> pattern.getScore(originalScore))
                            .filter(i -> i != originalScore)
                            .filter(i -> i > 0)
                            .findFirst().orElseThrow(WatException::new);
                })
                .sum();
        System.out.println(result);
    }

    @NotNull
    private static PatternMap newPatternMapWithSingleInverted(char[][] caa, int yOfInversion, int xOfInversion) {
        char[] oldRow = caa[yOfInversion];
        char[] newRow = new char[oldRow.length];
        System.arraycopy(oldRow, 0, newRow, 0, oldRow.length);
        newRow[xOfInversion] = oldRow[xOfInversion] == '.' ? '#' : '.';
        char[][] newRows = new char[caa.length][];
        System.arraycopy(caa, 0, newRows, 0, caa.length);
        newRows[yOfInversion] = newRow;
        return new PatternMap(newRows, false);
    }

    @Override
    public int number() {
        return 13;
    }
}
