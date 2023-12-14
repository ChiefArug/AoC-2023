package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;

public class Day14 implements Day {


    static final class KindaHeavyMap {
        private char[][] array;

        KindaHeavyMap(char[][] array) {
            this.array = array;
        }

        // Basic 'physics' simulation. We just move it until nothing moves.
        void settleTowardsNorth() {
            char[][] original;
            do {
                original = array;
                tickTilt(original);
                try {
                    //noinspection BusyWait
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (!Arrays.deepEquals(original, array));
        }


        void tickTilt(char[][] oldMap) {
            this.array = new char[oldMap.length][oldMap[0].length];

            // because we don't iterate this row it won't be filled, so we need to do that here.
            // we are able to reuse the instance because if this does change, somewhere else in the map will also change
            // so the deep equals check still fails despite this particular array mutating in both the old and the new
            this.array[0] = oldMap[0];

            // iterate in reverse, so we start at the bottom.
            y:
            for (int y = array.length - 1; y >= 1; y--) { // also stop before we reach the last row, because that doesn't need changing
                x:
                for (int x = 0; x < array[y].length; x++) {
                    char c = oldMap[y][x];
                    if (c == 'O') {
                        array[y][x] = 'W'; //DEBUG: set the current position to a W to show we are working here

                        char cAbove;
                        int ySearch = y;
                        do_op:
                        do {
                            cAbove = oldMap[--ySearch][x];
                            switch (cAbove) {
                                case '#' -> {break do_op;} // we can't move cause of this annoying rock
                                case '.' -> {// we did find an empty spot, so we can swap our position with said spot if it is also clear on the new array. otherwise continue the search
                                    if (array[ySearch][x] == '\u0000' || array[ySearch][x] == '.') {
                                        array[ySearch][x] = 'O';
                                        array[y][x] = '.';
                                        continue x;
                                    }
                                }
                            }
                        } while (ySearch > 0);
                    }
                    if (array[y][x] == '\u0000' || array[y][x] == 'W')
                        array[y][x] = c; // we were not able to move, so set our current pos to this only if it hasn't already been filled (or was only filled with a debug W)

                }
            }
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println(this);
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

    @Override
    public void run(BufferedReader input) {
        KindaHeavyMap map = new KindaHeavyMap(input.lines()
                .map(String::toCharArray)
                .toArray(char[][]::new));
        System.out.println(map);
        map.settleTowardsNorth();
        System.out.println(map.countWeight());
    }

    @Override
    public int number() {
        return 14;
    }
}
