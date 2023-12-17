package chiefarug.code.adventofcode;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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

        /**
         * @return It was super effective!
         */
        abstract List<Direction> lightUsesSlam(Direction hitDir);

        @Override
        public String toString() {
            return String.valueOf(gridRep);
        }
    }

    record Pos(int y, int x) {
    }

    static class Beam {
        private static final Object ACTIVE = new Object();
        private final Contraption contraption;
        private Pos currentPos;
        private final Direction movementDir;

        Beam(Contraption contraption, Pos startPos, Direction startDirection) {
            this.contraption = contraption;
            this.currentPos = startPos;
            this.movementDir = startDirection;
        }

        Stream<Beam> move() {
            if (isInMap() && contraption.whatsHitHere[currentPos.y][currentPos.x] == null)
                contraption.whatsHitHere[currentPos.y][currentPos.x] = EnumSet.of(movementDir);//contraption.shineUpon(currentPos);

            currentPos = movementDir.move(currentPos);
            if (!isInMap()) return Stream.of();
            return contraption.get(currentPos).lightUsesSlam(movementDir).stream()
                    .map(this::fromHitDirection)
                    .filter(Beam::isInMap)
                    .filter(Beam::isSpotVisited)
                    .peek(b -> contraption.activeBeams.put(b, ACTIVE));
        }

        private boolean isInMap() {
            return currentPos.x >= 0 && currentPos.x < contraption.map[0].length && currentPos.y >= 0 && currentPos.y < contraption.map.length;
        }

        Beam fromHitDirection(Direction dir) {
            return new Beam(contraption, currentPos, dir);
        }

        private boolean isSpotVisited() {
            Set<Direction> whatsHitHereBefore = contraption.whatsHitHere[currentPos.y][currentPos.x];
            if (whatsHitHereBefore == null) {
                contraption.whatsHitHere[currentPos.y][currentPos.x] = EnumSet.of(movementDir);
                return true;
            }
            return whatsHitHereBefore.add(movementDir);
        }
    }

    record Contraption(Tile[][] map, Set<Direction>[][] whatsHitHere,
                       WeakHashMap<Beam, Object> activeBeams) implements Comparable<Contraption> {
        Tile get(Pos pos) {
            return map[pos.y][pos.x];
        }

        int countShinies() {
            int counter = 0;
            for (Set<Direction>[] booleans : whatsHitHere) {
                for (Set<Direction> setHere : booleans) {
                    if (setHere != null && !setHere.isEmpty()) counter++;
                }
            }
            return counter;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(map.length * (map[0].length + 1));
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map[y].length; x++) {
                    if (whatsHitHere[y][x] != null && !whatsHitHere[y][x].isEmpty()) stringBuilder.append('#');
                    else stringBuilder.append(map[y][x].gridRep);
                }
                stringBuilder.append('\n');
            }
            return stringBuilder.toString();
        }

        @Override
        public int compareTo(@NotNull Day16.Contraption o) {
            return Integer.compare(countShinies(), o.countShinies());
        }
    }


    @Override
    public void run(BufferedReader input) {
        Tile[][] map = input.lines()
                .map(String::chars)
                .map(is -> is.mapToObj(i -> Tile.byGridChar.get((char) i)).toArray(Tile[]::new))
                .toArray(Tile[][]::new);

        int permutations = map.length * 2 + map[0].length * 2;

        Contraption[] contraptions = new Contraption[permutations];
        Arrays.setAll(contraptions, _i -> new Contraption(map, new Set[map.length][map[0].length], new WeakHashMap<>()));


        List<Beam> beams = new ArrayList<>(permutations);
        int i = 0;
        for (int y = 0; y < map.length; y++) {
            beams.add(new Beam(contraptions[i++], new Pos(y, -1), Direction.EAST));
            beams.add(new Beam(contraptions[i++], new Pos(y, map[0].length), Direction.WEST));
        }
        for (int x = 0; x < map[0].length; x++) {
            beams.add(new Beam(contraptions[i++], new Pos(-1, x), Direction.SOUTH));
            beams.add(new Beam(contraptions[i++], new Pos(map.length, x), Direction.NORTH));
        }


        AtomicInteger watched = new AtomicInteger(new Random().nextInt(contraptions.length));

        JFrame frame = new JFrame("Contraption " + watched.get());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JTextArea textArea = new JTextArea("hi", 110, 110);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.YELLOW);
        textArea.setFont(new Font("Monocraft", Font.PLAIN, 4));
        frame.add(textArea);
        frame.pack();
        frame.setVisible(true);
        textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    frame.setTitle("Component " + watched.addAndGet(1));
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    frame.setTitle("Component " + watched.addAndGet(-1));
                }
                textArea.setText(contraptions[watched.get()].toString());
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        while (!beams.isEmpty()) {
            // i wish streams could be non lazy so this re-allocation mess didn't have to happen. oh well, the gc will deal with it
            beams = beams.stream().flatMap(Beam::move).toList();

            textArea.setText(contraptions[watched.get()].toString());

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        Arrays.stream(contraptions).max(Comparator.naturalOrder()).ifPresent(s -> {
            System.out.println(s);
            System.out.println(s.countShinies());
        });
    }

    @Override
    public int number() {
        return 16;
    }
}
