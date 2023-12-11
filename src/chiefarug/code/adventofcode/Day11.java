package chiefarug.code.adventofcode;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class Day11 implements Day {

//    interface SpaceTile {
//    }
//
//    enum EmptySpace implements SpaceTile {
//        EMPTY;
//
//        @Override
//        public String toString() {
//            return " EMPTY ";
//        }
//    }

    //    static class Galaxy implements SpaceTile {
    static Pos[] starMap = new Pos[500]; // my input has 440 stars, be on the safe side
    static short counter = 1;
//        final int id;

    short Galaxy(Pos pos) {
        short id = counter++;
        starMap[id] = pos;
        return id;
    }

//        record Tmp() implements SpaceTile {}

    static void recalculate(SpaceMap spaceMap) {
        counter = 1;
        Pos[] oldStarMap = starMap;
        starMap = new Pos[500];

        Pos[] starPosA = StreamSupport.stream(spaceMap.spliterator(), false) // non parrallel is faster
                .filter(pos -> spaceMap.get(pos) != 0)
//                .peek(System.out::println)
                .toArray(Pos[]::new);
        for (Pos starPos : starPosA) {
            starMap[counter] = starPos;
//          spaceMap.tiles[pos.y][pos.x] = new Tmp();
//          System.out.println(counter + " Old: " + oldStarMap[counter] + " New: " + pos);
            counter++;
        }


//        }

//        @Override
//        public String toString() {
//            return "Star" + String.format("%03d", id);
//        }
    }

    record Pos(int x, int y) {
    }

    static final class SpaceMap {
        private short[][] tiles;
        private short[] emptyRow;

        SpaceMap(short[][] tiles) {
            this.tiles = tiles;
        }

        short get(Pos pos) {
            return tiles[pos.y][pos.x];
        }


        void expand(boolean[] columnHasGalaxy, boolean[] rowHasGalaxy) {
            int extraColumnCount = antiCount(columnHasGalaxy);
            int extraRowCount = antiCount(rowHasGalaxy);

            short[][] oldTiles = tiles;
            tiles = new short[oldTiles.length + extraRowCount][];
            emptyRow = new short[oldTiles[0].length + extraColumnCount];


            for (int yOld = 0, y = 0; yOld < oldTiles.length; yOld++, y++) {
                if (!rowHasGalaxy[yOld]) {
                    for (int i = 0; i <= 999999; i++)
                        tiles[y++] = emptyRow;
                    tiles[y] = emptyRow;
                    continue;
                }
                tiles[y] = new short[oldTiles[0].length + extraColumnCount];
                for (int xOld = 0, x = 0; xOld < oldTiles[yOld].length; xOld++, x++) {
                    if (!columnHasGalaxy[xOld]) {
                        for (int i = 0; i <= 999999; i++)
                            tiles[y][x++] = 0;
                        tiles[y][x] = 0;
                        continue;
                    }
                    tiles[y][x] = oldTiles[yOld][xOld];
                    // we do have all stars here.
//                    if (tiles[y][x] != 0)
//                        System.out.println(tiles[y][x]);
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
            return count * 1_000_000;
        }

        /**
         * Spliterate over the positions in this bit of space
         */
        @NotNull
        public Spliterator<Pos> spliterator() {
            return new SpaceSpliterator(0, tiles.length);
        }

        class SpaceSpliterator implements Spliterator<Pos> {

            private final int maxX = tiles[0].length;

            @Override
            public boolean tryAdvance(Consumer<? super Pos> action) {
                if (!x.hasNext()) {
                    do {
                        AtomicInteger yPos = new AtomicInteger(-1);
                        boolean couldY = y.tryAdvance(yPos::set);
                        if (!couldY) return false;
                        x = IntStream.range(0, tiles[0].length).iterator();
                            currentY = yPos.get();
                    } while (tiles[currentY] == emptyRow); // stop iterating empty rows.
                }
                action.accept(new Pos(x.next(), currentY));
                return true;
            }

            @Override
            public Spliterator<Pos> trySplit() {
                long estStartSize = estimateSize();
                Spliterator<Integer> yTriedSplit = y.trySplit();
                if (yTriedSplit == null) return null;
                long estEndSize = estimateSize();
                Spliterator<Pos> other = new SpaceSpliterator(yTriedSplit);
                long estOtherSize = other.estimateSize();
                System.out.println("est size difference: " + (estStartSize - (estEndSize + estOtherSize)));
                return other;
            }

            @Override
            public long estimateSize() {
                return y.estimateSize() * maxX;
            }

            @Override
            public int characteristics() {
                return ORDERED;
            }

            SpaceSpliterator(int yMin, int yMax) {
                this(IntStream.range(yMin, yMax).spliterator());
            }

            private SpaceSpliterator(Spliterator<Integer> ySplitter) {
                this.y = ySplitter;
                y.tryAdvance(i -> currentY = i);
                this.x = IntStream.range(0, maxX).iterator();
            }

            Iterator<Integer> x;
            final Spliterator<Integer> y;
            int currentY = -1;
        }
    }

    @Override
    public void run(BufferedReader input) {
        String[] lines = input.lines().toArray(String[]::new);

        short[][] rows = new short[lines.length][lines[0].length()];

        boolean[] columnHasGalaxy = new boolean[lines[0].length()];
        boolean[] rowHasGalaxy = new boolean[lines.length];

        for (int y = 0, y1 = 0; y < lines.length; y++, y1++) {
            String line = lines[y];
            short[] row = new short[line.length()];

            char[] charArray = line.toCharArray();
            for (int x = 0; x < charArray.length; x++) {
                row[x] = switch (charArray[x]) {
                    case '.' -> 0;
                    case '#' -> {
                        columnHasGalaxy[x] = true;
                        rowHasGalaxy[y] = true;
                        yield Galaxy(new Pos(x, y));
                    }
                    default -> throw new WatException();
                };
            }
            rows[y] = row;
        }
        System.out.println("before resize: " + Arrays.toString(starMap));

        SpaceMap spaceMap = new SpaceMap(rows);
        spaceMap.expand(columnHasGalaxy, rowHasGalaxy);

        recalculate(spaceMap);
        System.out.println("after resize: " + Arrays.toString(starMap));

        long distanceCounter = countDistance();

        System.out.println(distanceCounter);
    }

    private static long countDistance() {
        long distanceCounter = 0;

        for (int galaxy = 1; galaxy < counter; galaxy++) {
            Pos pos1 = starMap[galaxy];
            for (int otherGalaxy = galaxy + 1; otherGalaxy < counter; otherGalaxy++) {
                Pos pos2 = starMap[otherGalaxy];
                int distance = Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y);
                distanceCounter += distance;
//                System.out.printf("Distance between Star%03d and Star%03d: " + distance + "%n", galaxy, otherGalaxy);
            }
        }
        return distanceCounter;
    }

    @Override
    public int number() {
        return 11;
    }
    /*
    attempted answers for p2:
    473011912 (too low)
    4000142 (not tried)
    406726138763 (too high)
     */
}
