package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static chiefarug.code.adventofcode.Day10.PipeSegment.*;

public class Day10 implements Day {

    static class Mutable<T> implements Supplier<T> {

        private T value;

        public void set(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        public boolean isNull() {
            return value == null;
        }

        public Mutable() {
            this(null);
        }

        public Mutable(T initialValue) {
            value = initialValue;
        }

        @Override
        public String toString() {
            return "Mutable[" + value + "]";
        }
    }

    record Map(char[][] lines) {
        PipeSegment get(Pos pos) {
            if (pos.y < 0 || pos.y >= ySize() || pos.x < 0 || pos.x >= xSize()) return GROUND;
            return PipeSegment.forCharacter(lines[pos.y()][pos.x]);
        }

        int ySize() {
            return lines.length;
        }

        int xSize() {
            return lines[0].length;
        }

        char[] getLine(int y) {
            return lines[y];
        }

        void set(Pos pos, PipeSegment pipe) {
            lines[pos.y][pos.x] = pipe.represent;
        }

        @Override
        public String toString() {
            return Arrays.stream(lines)
                    .map(String::new)
                    .map(s -> {
                        for (PipeSegment value : PipeSegment.values())
                            s = s.replace(String.valueOf(value.represent), value.boxChar);
                        return s;
                    })
                    .collect(Collectors.joining("\n"));
        }
    }

    @FunctionalInterface
    interface Offset {
        Pos offset(Pos pos);
    }

    record Pos(int y, int x) {
        Pos shift(int y, int x) {
            return new Pos(y() + y, x() + x);
        }

        Pos north() {
            return shift(-1, 0);
        }

        Pos south() {
            return shift(1, 0);
        }

        Pos east() {
            return shift(0, 1);
        }

        Pos west() {
            return shift(0, -1);
        }

        void scanAround(Consumer<Pos> forEach) {
            forEach.accept(north());
            forEach.accept(south());
            forEach.accept(east());
            forEach.accept(west());
        }

        Pos getNextInLine(Map map, Pos source, PipeSegment shape) {
            Pos next = shape.offset1(this);
            if (!map.get(next).isPipe() || next.equals(source)) next = shape.offset2(this);
            return next;
        }
    }

    enum PipeSegment {
        NORTH_SOUTH('|', "│", Pos::north, Pos::south),
        EAST_WEST('-', "─", Pos::east, Pos::west),
        NORTH_EAST('L', "└", Pos::north, Pos::east),
        SOUTH_WEST('7', "┐", Pos::south, Pos::west),
        SOUTH_EAST('F', "┌", Pos::south, Pos::east),
        NORTH_WEST('J', "┘", Pos::north, Pos::west),
        GROUND('.', "░", null, null) {
            @Override
            boolean isPipe() {
                return false;
            }
        },
        ANIMAL('S', "▚", null, null),
        POSSIBLE_NEST('N', "█", null, null);
        private final char represent;
        private final String boxChar;
        private final Offset offset1;
        private final Offset offset2;

        static final java.util.Map<Character, PipeSegment> charToPipe = new HashMap<>();

        static {
            for (PipeSegment value : values()) {
                charToPipe.put(value.represent, value);
            }
        }

        PipeSegment(char represent, String boxChar, Offset offset1, Offset offset2) {
            this.represent = represent;
            this.boxChar = boxChar;
            this.offset1 = offset1;
            this.offset2 = offset2;
        }

        static PipeSegment forCharacter(char c) {
            return charToPipe.get(c);
        }

        static PipeSegment fromConnections(Pos connection1, Pos connection2) {
            int xOffset = connection1.x - connection2.x;
            int yOffset = connection1.y - connection2.y;
            return switch (xOffset) {
                case -1 -> switch (yOffset) {
                    case -1 -> NORTH_WEST;
                    case 1 -> NORTH_EAST;
                    default -> throw new WatException();
                };
                case 2, -2 -> NORTH_SOUTH;
                case 0 -> EAST_WEST;
                case 1 -> switch (yOffset) {
                    case -1 -> SOUTH_WEST;
                    case 1 -> SOUTH_EAST;
                    default -> throw new WatException();
                };
                default -> throw new WatException();
            };
        }


        boolean isPipe() {
            return true;
        }

        Pos offset1(Pos p) {
            return offset1.offset(p);
        }

        Pos offset2(Pos p) {
            return offset2.offset(p);
        }

    }

    @Override
    public void run(BufferedReader input) {
        Map map = new Map(input.lines().map(String::toCharArray).toArray(char[][]::new));
        Pos tmpStartingPos = null;
        for (int i = 0; i < map.ySize(); i++) {
            String line = new String(map.getLine(i));
            int sIndex = line.indexOf("S");
            if (sIndex != -1) {
                tmpStartingPos = new Pos(i, sIndex);
                break;
            }
        }
        if (tmpStartingPos == null) throw new WatException("no starting pos found");
        final Pos startingPos = tmpStartingPos;

        Mutable<Pos> nextPipe1 = new Mutable<>();
        Mutable<Pos> nextPipe2 = new Mutable<>(); // we will visit here last
        tmpStartingPos.scanAround(p -> {
            PipeSegment pipe = map.get(p);
            if (pipe.isPipe() && (pipe.offset1(p).equals(startingPos) || pipe.offset2(p).equals(startingPos))) { // the pipe connects to us
                (nextPipe1.isNull() ? nextPipe1 : nextPipe2).set(p);
            }
        });

        PipeSegment startingPipeSegment = PipeSegment.fromConnections(nextPipe1.get(), nextPipe2.get());

        Set<Pos> pipe = new HashSet<>();
        pipe.add(startingPos);

        int moves = 0;
        Pos previousPos = startingPos;
        Pos currentPos = nextPipe1.get();
        while (!currentPos.equals(startingPos)) {
            pipe.add(currentPos);
            Pos oldCurrentPos = currentPos;
            currentPos = currentPos.getNextInLine(map, previousPos, map.get(currentPos));
            previousPos = oldCurrentPos;
            moves++;
        }

        Map pipeOnly = new Map(new char[map.ySize()][map.xSize()]);
        pipeOnly.set(startingPos, ANIMAL);
        int count = 0;
        boolean insidePipe = false;
        String previousDirection = "";

        for (int y = 0; y < map.ySize(); y++) {
            for (int x = 0; x < map.xSize(); x++) {
                Pos pos = new Pos(y, x);
                if (pipe.contains(pos)) {
                    PipeSegment pipeAt = map.get(pos);
                    if (pipeAt != EAST_WEST) {
                        switch (pipeAt) {
                            case NORTH_EAST -> {
                                previousDirection = "north";
                                insidePipe = !insidePipe;
                            }
                            case SOUTH_EAST -> {
                                previousDirection = "south";
                                insidePipe = !insidePipe;
                            }
                            case SOUTH_WEST -> {
                                if (previousDirection.equals("south")) insidePipe = !insidePipe;
                            }
                            case NORTH_WEST ->  {
                                if (previousDirection.equals("north")) insidePipe = !insidePipe;
                            }
                            case NORTH_SOUTH, ANIMAL -> insidePipe = !insidePipe; // we can do this cause the animal in my input is a |
                        }
                    }
                    pipeOnly.set(pos, map.get(pos));
                } else if (insidePipe) {
                    previousDirection = "";
                    count++;
                    pipeOnly.set(pos, POSSIBLE_NEST);
                } else {
                    previousDirection = "";
                    pipeOnly.set(pos, GROUND);
                }
            }
        }

        System.out.println(map);
        System.out.println();
        System.out.println(pipeOnly);

        System.out.println(count);
    }

    @Override
    public int number() {
        return 10;
    }
}
