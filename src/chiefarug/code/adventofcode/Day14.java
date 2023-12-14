package chiefarug.code.adventofcode;

import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

import static chiefarug.code.adventofcode.Day14.Axis.X;
import static chiefarug.code.adventofcode.Day14.Axis.Y;
import static chiefarug.code.adventofcode.Day14.ShiftDirection.NEGATIVE;
import static chiefarug.code.adventofcode.Day14.ShiftDirection.POSITIVE;

public class Day14 implements Day {

    // this code will (probably) brute force it given enough time (i recommend re-adding the cache for that), but i couldnt be bothered waiting so see the day14.xlsx spreadsheet for me working it out
    @Override
    public void run(BufferedReader input) {
        KindaHeavyMap map = new KindaHeavyMap(input.lines()
                .map(String::toCharArray)
                .toArray(char[][]::new));
        System.out.println(map);
        IntStream.range(0, 1_000_000_000).forEach(i -> map.spin(i));
        System.out.println(map.countWeight());
    }
    static final class KindaHeavyMap {

        private char[][] array;

        KindaHeavyMap(char[][] array) {
            this.array = array;
        }
        record Settler(Direction dir, char[][] caa) {

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj instanceof Settler c2d) {
                    return dir == c2d.dir && Arrays.deepEquals(caa, c2d.caa);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(dir.hashCode(), Arrays.deepHashCode(caa));
            }
            @Override
            public String toString() {
                return dir.name() + ":\n" + Arrays.deepToString(caa);
            }

        }



        void spin(int number) {
            for (Direction dir : Direction.values()) {
                settleTowards(dir);
            }
            cache.add(number, array);
            if (number >= 501) {
                cache.stream().mapToInt(c -> new KindaHeavyMap(c).countWeight()).filter(i -> i > 100301 && i < 100317).forEach(System.out::println);
                throw new RuntimeException("early exit");
            }
        }

        List<char[][]>/*, char[][]>*/ cache = new ArrayList<>(500);//new LinkedHashMap<>(1000, 0.5f);
        int cacheCounter;

        // Basic 'physics' simulation. We just move it until nothing moves.

        void settleTowards(Direction dir) {
            char[][] original = array;
//            Settler settler = new Settler(dir, original);
//            if (cache.contains(settler) && cacheCounter++ > 1000) { // we already know what the result of this will be, which means we have encountered a loop!
//                List<Settler> cacheButList = cache.stream().toList();
//                // first find where the loop began.
//                int counter = cacheButList.indexOf(settler);
//
//                System.out.println("Loop began at the " + counter + "stndth iteration");
//                int ONE_BILLION = 1_000_000_000; // it's a big number, so we are allowed to shout
//                int loopSize = cache.size() - counter;
//                int amountToEnterLoop = counter - loopSize;
//                int amountLeftOf_ONE_BILLION_ForLoops = ONE_BILLION - amountToEnterLoop;
//                int remainder = amountLeftOf_ONE_BILLION_ForLoops % loopSize;
//                int iterationToFetchFromCache = amountToEnterLoop + remainder + 2; // plus two to account for the two-off-by-one errors elsewhere (not sure where, but somewhere)
//
////                char[][] fetched = cache.get(cacheButList.get(iterationToFetchFromCache));
//
//                cacheButList.stream().filter(s -> s.dir == Direction.NORTH).map(s -> new KindaHeavyMap(s.caa).countWeight()).forEach(System.out::println);
//
//                throw new RuntimeException("I know the answer! It's: " /*+ new KindaHeavyMap(fetched).countWeight()*/); // convenient way to exit
                /* expected end for test case: (debug wiht this)
                .....#....
                ....#...O#
                .....##...
                ...#......
                .....OOO#.

                .O#...O#.#
                ....O#...O
                ......OOOO
                #....###.O
                #.OOO#..OO
                 */
//            }
            do {
                original = array;

                // the mutation of this.array is moved out here, and tickTilt changed to a static method to better communicate that it only mutates this.array
                array = tickTiltTowards(dir, original);

//                System.out.flush();
//                String out = this.toString();
//                System.out.print("\033[H\033[2J");
//                System.out.println(dir);
//                System.out.println(out);
//                try {
//                    //noinspection BusyWait
//                    Thread.sleep(speed);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            } while (!Arrays.deepEquals(original, array));
//            cache.put(settler, array);
//            cache.add(settler);
        }

        @Contract(pure = true)
        static char[][] tickTiltTowards(Direction dir, char[][] oldMap) {
            char[][] newMap = new char[oldMap.length][oldMap[0].length];

            // because we don't iterate the first row/column it won't be filled, so we need to do that here.
            // we now care about the instance from the old char[][] being modified as it is now part of the hash for a key for a HashMap
            // so modifying it would have spooky undefined results,
            // so we have to copy anything and everything.
            switch (dir) {
                case NORTH -> newMap[0] = Arrays.copyOf(oldMap[0], oldMap[0].length);
                case WEST -> {
                    for (int y = 0; y < oldMap.length; y++) {
                        Pos pos = new Pos(y, 0);
                        set(newMap, pos, get(oldMap, pos));
                    }
                }
                case SOUTH -> newMap[newMap.length - 1] = Arrays.copyOf(oldMap[oldMap.length - 1], oldMap[oldMap.length - 1].length);
                case EAST -> {
                    for (int y = 0; y < oldMap.length; y++) {
                        Pos pos = new Pos(y, oldMap[0].length - 1);
                        set(newMap, pos, get(oldMap, pos));
                    }
                }
            }


            // iterate in reverse, so we start at the bottom.
            Direction.FromSidePosIterator posIterator = dir.iterateFromThisSide(newMap[0].length, newMap.length);
            atNextPos: while (posIterator.hasNext()) {
                final Pos pos = posIterator.next();
                final char c = get(oldMap, pos);
                if (c == 'O') {
                    set(newMap, pos, 'W'); //DEBUG: set the current position to a W to show we are working here

                    char cAbove;
                    Pos search = pos;
                    do_op:
                    do {
                        cAbove = get(oldMap, search = search.shift(dir));
                        switch (cAbove) {
                            case '#' -> { break do_op; } // we can't move cause of this annoying rock
                            case '.' -> {// we did find an empty spot, so we can swap our position with said spot if it is also clear on the new array. otherwise continue the search
                                if (get(newMap, search) == '\u0000' || get(newMap, search) == '.') {
                                    set(newMap, search, 'O');
                                    set(newMap, pos, '.');
                                    continue atNextPos;
                                }
                            }
                        }
                    } while (posIterator.isNotNextToMainEdge(search));
                }
                if (get(newMap, pos) == '\u0000' || get(newMap, pos) == 'W')
                    set(newMap, pos, c); // we were not able to move, so set our current pos to this only if it hasn't already been filled (or was only filled with a debug W)
            }
            return newMap;
        }

        private static char get(char[][] caa, Pos pos) {
            return caa[pos.y][pos.x];
        }

        private static void set(char[][] array, Pos pos, char c) {
            array[pos.y][pos.x] = c;
        }

        int countWeight() {
            int counter = 0;
            for (int y = 0; y < array.length; y++) {
                int multiplier = array.length - y;
                for (char c : array[y])
                    if (c == 'O')
                        counter += multiplier;
            }
            return counter;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(array.length * (array[0].length + 1));
            for (char[] chars : array) {
                for (char c : chars) {
                    sb.append(c);
                }
                sb.append('\n');
            }
            return sb.toString();
        }

    }

    record Pos(int y, int x) {

        Pos shift(Direction dir) {
            return dir.shift(this);
        }
        int getComponent(Axis axis) {
            return axis == X ? x : y;
        }

    }
    enum Axis {
        X, Y

    }

    enum ShiftDirection {
        POSITIVE, NEGATIVE;
        int modify(int i) {
            return this == NEGATIVE ? -i : i;
        }

    }
    enum Direction {
        NORTH(Y, NEGATIVE), WEST(X, NEGATIVE), SOUTH(Y, POSITIVE), EAST(X, POSITIVE);
        private final Axis axis;

        private final ShiftDirection shiftDirection;

        Direction(Axis axis, ShiftDirection shiftDirection) {
            this.axis = axis;
            this.shiftDirection = shiftDirection;
        }

        Pos shift(Pos pos) {
            return shift(pos, 1);
        }

        Pos shift(Pos pos, int amount) {
            return axis == X ?
                    new Pos(pos.y, pos.x + shiftDirection.modify(amount)) :
                    new Pos(pos.y + shiftDirection.modify(amount), pos.x);
        }

        FromSidePosIterator iterateFromThisSide(int xMaxExclusive, int yMaxExclusive) {
            return new FromSidePosIterator(xMaxExclusive, yMaxExclusive);
        }
        class FromSidePosIterator implements Iterator<Pos> {

            private final int xMaxExclusive;

            private final int yMaxExclusive;

            FromSidePosIterator(int xMaxExclusive, int yMaxExclusive) {
                this.xMaxExclusive = xMaxExclusive;
                this.yMaxExclusive = yMaxExclusive;
                shiftDirectionIterator = new EitherDirectionIntIterator();
                otherAxisIterator = IntStream.range(0, otherAxisMax()).iterator();
                currentInShiftDirection = shiftDirectionIterator.nextInt();
            }

            private int mainAxisMin() {
                return shiftDirection == POSITIVE ? 0 : 1;
            }

            @Override
            public boolean hasNext() {
                return shiftDirectionIterator.hasNext() || otherAxisIterator.hasNext();
            }

            @Override
            public Pos next() {
                if (otherAxisIterator.hasNext()) {
                    return new Pos(nextY(), nextX());
                }
                currentInShiftDirection = shiftDirectionIterator.nextInt();
                otherAxisIterator = IntStream.range(0, otherAxisMax()).iterator();
                return new Pos(nextY(), nextX());
            }

            private int nextX() {
                return axis == X ? currentInShiftDirection : otherAxisIterator.next();
            }

            private int nextY() {
                return axis == Y ? currentInShiftDirection : otherAxisIterator.next();
            }

            private int mainAxisMax() {
                return axis == X ? xMaxExclusive : yMaxExclusive;
            }

            private int otherAxisMax() {
                return axis != X ? xMaxExclusive : yMaxExclusive;
            }



            public boolean isNotNextToMainEdge(Pos pos) {
                if (shiftDirection == POSITIVE)
                    return pos.getComponent(axis) < mainAxisMax() - 2;
                return pos.getComponent(axis) > 1;
            }

            private final EitherDirectionIntIterator shiftDirectionIterator;
            private PrimitiveIterator.OfInt otherAxisIterator;

            private int currentInShiftDirection;
            class EitherDirectionIntIterator implements PrimitiveIterator.OfInt {
                private final int startExclusive = shiftDirection == POSITIVE ? -1 : mainAxisMax(); // yes this is exclusive. no don't ask me why
                private final int end = shiftDirection == POSITIVE ? mainAxisMax() - 2 : 1; // shift the end by one on negative so that we do not iterate the last row/column, and by two on positive cause the max is exclusive

                private int current = startExclusive;

                @Override
                public boolean hasNext() {
                    return shiftDirection == POSITIVE ? current < end : current > end;
                }
                @Override
                public int nextInt() {
                    return current += shiftDirection.modify(1);
                }
            }
        }

    }

    /*
    attempted answers:
    100301 (too low)
    100195 (too low)
    100317 (too high)
    other found that may be submitted
    100308 (iterationToFetch - 112)
    100376 (-116)
    100433 (-120)
    100506 (-124)
    100598 (-128)
    100750 (-136)
     */

    @Override
    public int number() {
        return 14;
    }
}
