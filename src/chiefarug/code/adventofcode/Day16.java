package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Day16 implements Day {

    enum Direction {
        NORTH, EAST, SOUTH, WEST;

        private final List<Direction> selfAsList = List.of(this);
        private final Supplier<List<Direction>> neighbours = new Supplier<>() {
            private List<Direction> value;
            @Override
            public List<Direction> get() {
                return value == null ? value = List.of(Direction.this.clockwise(), Direction.this.counterClockwise()) : value;
            }
        };
        List<Direction> asList() {
            return selfAsList;
        }

        List<Direction> neighbours() {
            return neighbours.get();
        }

        Direction counterClockwise() {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
            };

        }

        Direction clockwise() {
            return switch (this) {
                case NORTH -> WEST;
                case EAST -> NORTH;
                case SOUTH -> EAST;
                case WEST -> SOUTH;
            };
        }

        Pos move(Pos pos) {
            return switch (this) {
                case NORTH -> new Pos(pos.y - 1, pos.x);
                case EAST -> new Pos(pos.y, pos.x + 1);
                case SOUTH -> new Pos(pos.y + 1, pos.x);
                case WEST -> new Pos(pos.y, pos.x - 1);
            };
        }
    }

    enum Tile {
        EMPTY('.') {
            @Override
            List<Direction> lightUsesSlam(Direction hitDir) {
                return hitDir.asList();
            }
        }, VERTICAL_SPLITTER('-') {
            @Override
            List<Direction> lightUsesSlam(Direction hitDir) {
                return switch (hitDir) {
                    case NORTH, SOUTH -> hitDir.neighbours();
                    case EAST, WEST -> hitDir.asList();
                };
            }

        }, HORIZONTAL_SPLITTER('|') {
            @Override
            List<Direction> lightUsesSlam(Direction hitDir) {
                return switch (hitDir) {
                    case EAST, WEST -> hitDir.neighbours();
                    case NORTH, SOUTH -> hitDir.asList();
                };
            }
        }, LEFT_TO_RIGHT_MIRROR('\\') {
            @Override
            List<Direction> lightUsesSlam(Direction hitDir) {
                return switch (hitDir) {
                    case NORTH, SOUTH -> hitDir.clockwise().asList();
                    case EAST, WEST -> hitDir.counterClockwise().asList();
                };
            }
        }, RIGHT_TO_LEFT_MIRROR('/') {
            @Override
            List<Direction> lightUsesSlam(Direction hitDir) {
                return switch (hitDir) {
                    case EAST, WEST -> hitDir.clockwise().asList();
                    case NORTH, SOUTH -> hitDir.counterClockwise().asList();
                };
            }
        };


        public static final Map<Character, Tile> byGridChar = new HashMap<>(5);

        static {
            for (Tile tile : Tile.values()) {
                byGridChar.put(tile.gridRep, tile);
            }
        }

        private final char gridRep;

        Tile(char gridRepresentation) {
            gridRep = gridRepresentation;
        }

        /** @return It was super effective! */
        abstract List<Direction> lightUsesSlam(Direction hitDir);

        @Override
        public String toString() {
            return String.valueOf(gridRep);
        }
    }

    record Pos(int y, int x) {}

    static class Beam {
        private final Contraption contraption;
        private Pos currentPos;
        private final Direction movementDir;
        public Beam(Contraption contraption) {
            this(contraption, new Pos(0, -1), Direction.EAST);
        }

        private Beam(Contraption contraption, Pos startPos, Direction startDirection) {
            this.contraption = contraption;
            this.currentPos = startPos;
            this.movementDir = startDirection;
        }

        Stream<Beam> move() {
            if (isInMap()) contraption.shineUpon(currentPos);
            currentPos = movementDir.move(currentPos);
            if (!isInMap()) return Stream.of();
            return contraption.get(currentPos).lightUsesSlam(movementDir).stream()
                    .map(this::fromHitDirection)
                    .filter(Beam::isInMap);
        }

        private boolean isInMap() {
            return currentPos.x >= 0 && currentPos.x < contraption.map[0].length && currentPos.y >= 0 && currentPos.y < contraption.map.length;
        }

        Beam fromHitDirection(Direction dir) {
            return new Beam(contraption, currentPos, dir);
        }

    }

    record Contraption(Tile[][] map, boolean[][] lightMap) {
        Tile get(Pos pos) {
            return map[pos.y][pos.x];
        }

        void shineUpon(Pos pos) {
            lightMap[pos.y][pos.x] = true;
        }

        int countShinies() {
            int counter = 0;
            for (boolean[] booleans : lightMap) {
                for (boolean aBoolean : booleans) {
                    if (aBoolean) counter++;
                }
            }
            return counter;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(map.length * (map[0].length + 1));
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map[y].length; x++) {
                    if (lightMap[y][x]) stringBuilder.append('#');
                    else stringBuilder.append(map[y][x].gridRep);
                }
                stringBuilder.append('\n');
            }
            return stringBuilder.toString();
        }
    }
    record LightMap(boolean[][] map) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof LightMap lm)
                return Arrays.deepEquals(map, lm.map);
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(map);
        }
    }


    @Override
    public void run(BufferedReader input) {
        Tile[][] map = input.lines()
                .map(String::chars)
                .map(is -> is.mapToObj(i -> Tile.byGridChar.get((char) i)).toArray(Tile[]::new))
                .toArray(Tile[][]::new);

        Contraption contraption = new Contraption(map, new boolean[map.length][map[0].length]);

        Beam beam = new Beam(contraption);

        List<Beam> beams = List.of(beam);
        Set<LightMap> previousLights = new HashSet<>();
        int sameCounter = 0;
        while (!beams.isEmpty()) {
            // i wish streams could be non lazy so this re-allocation mess didn't have to happen. oh well, the gc will deal with it
            beams = beams.stream().flatMap(Beam::move).toList();
            System.out.println(contraption);

            if (!previousLights.add(new LightMap(deepCopy(contraption.lightMap))) && ++sameCounter > 100) { //TODO: better loop detection. if we hit a mirror or split that is already lit we can stop that beam from calculating further.
                break; // were probably in a loop
            }

            try {
                Thread.sleep(50); // looks pretty in the console
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        System.out.println(contraption.countShinies());
    }

    private boolean[][] deepCopy(boolean[][] in) {
        boolean[][] out = new boolean[in.length][];
        for (int i = 0; i < in.length; i++) {
            out[i] = Arrays.copyOf(in[i], in[i].length);
        }
        return out;
    }

    @Override
    public int number() {
        return 16;
    }
}
