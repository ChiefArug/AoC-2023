package chiefarug.code.adventofcode;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;

import static chiefarug.code.adventofcode.Day11.EmptySpace.EMPTY;
import static chiefarug.code.adventofcode.Day11.EmptySpace.EMPTY_BUT_COUNTED;
import static chiefarug.code.adventofcode.Day11.EmptySpace.EMPTY_BUT_ONE_MILLION;
import static chiefarug.code.adventofcode.Day11.EmptySpace.EMPTY_BUT_ONE_MILLION_BUT_COUNTED;
import static java.util.stream.Collectors.joining;

public class Day11 implements Day {

    interface SpaceTile {
    }

    enum EmptySpace implements SpaceTile {
        EMPTY, EMPTY_BUT_COUNTED {
            @Override
            public String toString() {
                return "---|---";
            }
        }, EMPTY_BUT_ONE_MILLION {
            @Override
            public String toString() {
                return "EMPTIER";
            }
        }, EMPTY_BUT_ONE_MILLION_BUT_COUNTED {
            @Override
            public String toString() {
                return "-||-||-";
            }
        };

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

        public static void recalculate(SpaceMap spaceMap) {
            counter = 0;
            Pos[] oldStarMap = starMap;
            starMap = new Pos[500];
            for (Pos pos : spaceMap) {
                SpaceTile spaceTile = spaceMap.get(pos);
                if (spaceTile == EMPTY || spaceTile == EMPTY_BUT_ONE_MILLION) continue;
                starMap[counter] = pos;
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
            tiles = new SpaceTile[oldTiles.length][oldTiles[0].length];
            SpaceTile[] emptyRow = new SpaceTile[oldTiles[0].length];
            Arrays.fill(emptyRow, EMPTY_BUT_ONE_MILLION);


            for (int y = 0; y < oldTiles.length; y++)
                if (!rowHasGalaxy[y])
                    tiles[y] = emptyRow;
                else
                    for (int x = 0; x < oldTiles[y].length; x++)
                        if (!columnHasGalaxy[x]) tiles[y][x] = EMPTY_BUT_ONE_MILLION;
                        else tiles[y][x] = oldTiles[y][x];

            System.out.println(
                    Arrays.stream(oldTiles)
                            .map(s -> Arrays.stream(s)
                                    .map(Objects::toString)
                                    .collect(joining()))
                            .collect(joining("\n"))
            );
            System.out.println();
            System.out.println(
                    Arrays.stream(tiles)
                            .map(s -> Arrays.stream(s)
                                    .map(Objects::toString)
                                    .map(st -> st == "null" ? " null  " : st)
                                    .collect(joining()))
                            .collect(joining("\n"))
            );

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


        long counter = 0;

        for (int galaxy = 0; galaxy < Galaxy.counter; galaxy++) {
            Pos galaxyPos = Galaxy.starMap[galaxy];
            for (int otherGalaxy = galaxy + 1; otherGalaxy < Galaxy.counter; otherGalaxy++) {
                Pos otherGalaxyPos = Galaxy.starMap[otherGalaxy];
                int distance = 0;
                int y = Math.min(galaxyPos.y, otherGalaxyPos.y);
                int x = Math.min(galaxyPos.x, otherGalaxyPos.x);
                for (; y <= Math.max(galaxyPos.y, otherGalaxyPos.y); y++) {
                    SpaceTile tile = spaceMap.get(new Pos(x, y));
                    if (!(tile instanceof Galaxy)) spaceMap.tiles[x][y] = EMPTY_BUT_COUNTED;
                    if (tile == EMPTY_BUT_ONE_MILLION || tile == EMPTY_BUT_ONE_MILLION_BUT_COUNTED) {
                        spaceMap.tiles[x][y] = EMPTY_BUT_ONE_MILLION_BUT_COUNTED;
                        distance += 1_000_000;
                    } else {
                        distance++;
                    }
                }
                y = Math.min(galaxyPos.y, otherGalaxyPos.y); // reset y cause y not
                for (; x <= Math.max(galaxyPos.x, otherGalaxyPos.x); x++) {
                    SpaceTile tile = spaceMap.get(new Pos(x, y));
                    if (!(tile instanceof Galaxy)) spaceMap.tiles[x][y] = EMPTY_BUT_COUNTED;
                    if (tile == EMPTY_BUT_ONE_MILLION || tile == EMPTY_BUT_ONE_MILLION_BUT_COUNTED) {
                        spaceMap.tiles[x][y] = EMPTY_BUT_ONE_MILLION_BUT_COUNTED;
                        distance += 1_000_000;
                    } else {
                        distance++;
                    }
                }
                counter += distance;
//                System.out.printf("Distance between Star%03d and Star%03d: " + distance + "%n", galaxy, otherGalaxy);
            }
        }
        System.out.println();
        System.out.println(
                Arrays.stream(spaceMap.tiles)
                        .map(s -> Arrays.stream(s)
                                .map(Objects::toString)
                                .map(st -> st == "null" ? " null  " : st) // we can do this cause of interning
                                .collect(joining()))
                        .collect(joining("\n"))
        );

        System.out.println(counter);
    }

    /*
    attempted answers for p2:
    473011912 (too low)
    4000142 (not tried)
    406726138763 (too high)
    19182662716510 (not tried)
    299901032031 (too low)
     */
    @Override
    public int number() {
        return 11;
    }
}
