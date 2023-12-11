package chiefarug.code.adventofcode;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

import static chiefarug.code.adventofcode.Day11.EmptySpace.EMPTY;

public class Day11 implements Day {

    interface SpaceTile {
    }

    enum EmptySpace implements SpaceTile {
        EMPTY;

        @Override
        public String toString() {
            return " EMPTY ";
        }
    }

    static class Galaxy implements SpaceTile {
        static Pos[] starMap = new Pos[500]; // my input has 440 stars, be on the safe side
        static int counter = 0;
        final int id;

        public Galaxy(Pos pos) {
            id = counter++;
            starMap[id] = pos;
        }

        record Tmp() implements SpaceTile {}

        public static void recalculate(SpaceMap spaceMap) {
            counter = 0;
            Pos[] oldStarMap = starMap;
            starMap = new Pos[500];
            for (Pos pos : spaceMap) {
                if (spaceMap.get(pos) == EMPTY) continue;
                starMap[counter] = pos;
                spaceMap.tiles[pos.y][pos.x] = new Tmp();
//                System.out.println(counter + " Old: " + oldStarMap[counter] + " New: " + pos);
                counter++;
            }
        }

        @Override
        public String toString() {
            return "Star" + String.format("%03d", id);
        }
    }

    record Pos(int x, int y) {
    }

    static final class SpaceMap implements Iterable<Pos> {
        private SpaceTile[][] tiles;

        SpaceMap(SpaceTile[][] tiles) {
            this.tiles = tiles;
        }

        SpaceTile get(Pos pos) {
            return tiles[pos.y][pos.x];
        }

        void expand(boolean[] columnHasGalaxy, boolean[] rowHasGalaxy) {
            int extraColumnCount = antiCount(columnHasGalaxy);
            int extraRowCount = antiCount(rowHasGalaxy);

            SpaceTile[][] oldTiles = tiles;
            tiles = new SpaceTile[oldTiles.length + extraRowCount][oldTiles[0].length + extraColumnCount];
            SpaceTile[] emptyRow = new SpaceTile[oldTiles[0].length + extraColumnCount];
            Arrays.fill(emptyRow, EMPTY);


            for (int yOld = 0, y = 0; yOld < oldTiles.length; yOld++, y++) {
                if (!rowHasGalaxy[yOld]) {
                    tiles[y++] = emptyRow;
                    tiles[y] = emptyRow;
                    continue;
                }
                for (int xOld = 0, x = 0; xOld < oldTiles[yOld].length; xOld++, x++) {
                    if (!columnHasGalaxy[xOld]) {
                        tiles[y][x++] = EMPTY;
                        tiles[y][x] = EMPTY;
                        continue;
                    }
                    tiles[y][x] = oldTiles[yOld][xOld];
                }
            }

//            System.out.println(
//                    Arrays.stream(oldTiles)
//                            .map(s -> Arrays.stream(s)
//                                    .map(Objects::toString)
//                                    .collect(Collectors.joining()))
//                            .collect(Collectors.joining("\n"))
//            );
//            System.out.println();
//            System.out.println(
//                    Arrays.stream(tiles)
//                            .map(s -> Arrays.stream(s)
//                                    .map(Objects::toString)
//                                    .map(st -> st == "null" ? " null  " : st)
//                                    .collect(Collectors.joining()))
//                            .collect(Collectors.joining("\n"))
//            );

        }

        private static int antiCount(boolean[] bArray) {
            int count = 0;
            for (boolean b : bArray) if (!b) count++;
            return count;
        }

        /**
         * Iterate over the positions in this bit of space
         */
        @NotNull
        @Override
        public Iterator<Pos> iterator() {


            return new Iterator<>() {
                Iterator<Integer> x = IntStream.range(0, tiles[0].length).iterator();
                final Iterator<Integer> y = IntStream.range(0, tiles.length).iterator();
                int currentY = y.next();

                @Override
                public boolean hasNext() {
                    return x.hasNext() || y.hasNext();
                }

                @Override
                public Pos next() {
                    if (!x.hasNext()) {
                        x = IntStream.range(0, tiles[0].length).iterator();
                        currentY = y.next();
                    }
                    return new Pos(x.next(), currentY);
                }
            };
        }
    }

    @Override
    public void run(BufferedReader input) {
        String[] lines = input.lines().toArray(String[]::new);

        SpaceTile[][] rows = new SpaceTile[lines.length][lines[0].length()];

        boolean[] columnHasGalaxy = new boolean[lines[0].length()];
        boolean[] rowHasGalaxy = new boolean[lines.length];

        for (int y = 0, y1 = 0; y < lines.length; y++, y1++) {
            String line = lines[y];
            SpaceTile[] row = new SpaceTile[line.length()];

            char[] charArray = line.toCharArray();
            for (int x = 0; x < charArray.length; x++) {
                row[x] = switch (charArray[x]) {
                    case '.' -> EMPTY;
                    case '#' -> {
                        columnHasGalaxy[x] = true;
                        rowHasGalaxy[y] = true;
                        yield new Galaxy(new Pos(x, y));
                    }
                    default -> throw new WatException();
                };
            }
            rows[y] = row;
        }

        SpaceMap spaceMap = new SpaceMap(rows);
        spaceMap.expand(columnHasGalaxy, rowHasGalaxy);

        Galaxy.recalculate(spaceMap);
//        System.out.println(
//                Arrays.stream(spaceMap.tiles)
//                        .map(s -> Arrays.stream(s)
//                                .map(Objects::toString)
//                                .map(st -> st == "null" ? " null  " : st) // we can do this cause of interning
//                                .collect(Collectors.joining()))
//                        .collect(Collectors.joining("\n"))
//        );

        long counter = 0;

        for (int galaxy = 0; galaxy < Galaxy.counter; galaxy++) {
            Pos pos1 = Galaxy.starMap[galaxy];
            for (int otherGalaxy = galaxy + 1; otherGalaxy < Galaxy.counter; otherGalaxy++) {
                Pos pos2 = Galaxy.starMap[otherGalaxy];
                int distance = Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y);
                counter += distance;
//                System.out.printf("Distance between Star%03d and Star%03d: " + distance + "%n", galaxy, otherGalaxy);
            }
        }

        System.out.println(counter);
    }

    @Override
    public int number() {
        return 11;
    }
}
