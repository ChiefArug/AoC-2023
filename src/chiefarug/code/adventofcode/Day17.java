package chiefarug.code.adventofcode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static chiefarug.code.adventofcode.Day17.Direction.*;
import static chiefarug.code.adventofcode.Day17.Node.ORIGIN;

public class Day17 implements Day {

    private static Pos TARGET;
    private static int[][] map;

    record Pos(int x, int y) {
        Pos up() {
            return new Pos(x, y - 1);
        }

        Pos down() {
            return new Pos(x, y + 1);
        }

        Pos left() {
            return new Pos(x - 1, y);
        }

        Pos right() {
            return new Pos(x + 1, y);
        }

        Pos shift(Direction dir) {
            return switch (dir) {
                case UP -> up();
                case DOWN -> down();
                case LEFT -> left();
                case RIGHT -> right();
            };
        }
    }

    enum Direction {
        UP, DOWN, LEFT, RIGHT;

        Direction opposite() {
            return switch (this) {
                case UP -> DOWN;
                case DOWN -> UP;
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
            };
        }
    }

    static class Node implements Comparable<Node> {
        public static Node ORIGIN;
        static Node[][] nodeAtPos = new Node[map.length][map[0].length];
        private final Pos pos;
        private final Node previous;
        private EnumMap<Direction, Node> next;
        private final int straightInARow;
        private final Direction movementDirection;
        private boolean stillValid = true;
        private final int distanceWorth;

        Node(Pos pos, Node previous, int straightInARow, Direction movementDirecton, int distanceWorth) {
            this.pos = pos;
            this.previous = previous;
            this.straightInARow = straightInARow;
            this.movementDirection = movementDirecton;
            this.distanceWorth = distanceWorth;
        }

        @Override
        public int compareTo(@NotNull Day17.Node o) {
            return Integer.compare(getScore(), o.getScore());
        }

        protected int getScore() {
            int distance = Math.abs(pos.x - TARGET.x) + Math.abs(pos.y - TARGET.y);
            int distanceWorth;
            // this is literally the end!
            if (distance == 0)
                distanceWorth = Integer.MAX_VALUE; // because of integer wraparound this puts it on the bottom of the queue!
                // the closer it is, the more the distance is worth in score
            else
                distanceWorth = (map.length + map[0].length) / distance;
            int heatWorth = map[pos.y][pos.x];
            return distanceWorth + heatWorth;
        }

        protected int getHeatLoss() {
            return map[pos.y][pos.x] + previous.getHeatLoss();
        }

        @Nullable
        private Node inDirection(Direction dir) {
            if (movementDirection != null && dir == movementDirection.opposite()) return null;
            return new Node(pos.shift(dir), this, dir == movementDirection ? straightInARow + 1 : 1, dir, distanceWorth + 1);
        }

        protected boolean isValid() {
            if (!stillValid) return false;
            if (!(pos.y >= 0 && pos.x >= 0 && pos.y < map.length && pos.x < map[0].length && straightInARow <= 3)) {
                this.stillValid = false;
                return false;
            }

            Node prev = this;
            do {
                prev = prev.previous;
            } while (prev.previous != null && prev.isValid());
            if (prev == ORIGIN) return true;
            // otherwise this and all the other ones are no longer valid
//            unlink();
            return false;
        }

//        private void unlink() {
//            next.forEach((d, n) -> {
//                if (n != null)
//                    n.next.put(d.opposite(), null);
//            });
//            if (!isValid())
//                previous.unlink();
//        }

        public Collection<Node> getNeighbours() {
            if (next != null) return next.values();

            next = new EnumMap<>(Direction.class);
            Stream.of(inDirection(UP), inDirection(RIGHT), inDirection(DOWN), inDirection(LEFT))
                    .filter(Objects::nonNull)
                    .filter(Node::isValid)
                    .filter(n -> {
                        Node previousHere = nodeAtPos[n.pos.y][n.pos.x];
                        if (previousHere == null || n.compareTo(previousHere) > 0) {
                            nodeAtPos[n.pos.y][n.pos.x] = n;
                            return true;
                        }
                        return false;
                    })
                    .forEach(neighbour -> this.next.put(neighbour.movementDirection, neighbour));

            return next.values();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Node) obj;
            return Objects.equals(this.pos, that.pos) &&
                    Objects.equals(this.previous, that.previous) &&
                    this.straightInARow == that.straightInARow &&
                    Objects.equals(this.movementDirection, that.movementDirection);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, previous, straightInARow, movementDirection);
        }

        @Override
        public String toString() {
            return "Node[" + pos.x + ", " + pos.y + " | " + "score: " + getScore() + "|" + getHeatLoss() + "]";
        }
    }

    @Override
    public void run(BufferedReader input) {
        map = input.lines()
                .map(s -> s.chars().map(c -> Integer.parseInt(String.valueOf((char) c))).toArray())
                .toArray(int[][]::new);

        Pos startPos = new Pos(0, 0);
        ORIGIN = new Node(startPos, null, 1, null, 0) {
            @Override
            protected int getHeatLoss() {
                return 0;
            }

            @Override
            protected int getScore() {
                return 0;
            }

            @Override
            protected boolean isValid() {
                return true;
            }
        };
        TARGET = new Pos(map[0].length - 1, map.length - 1);

        List<Node> ends = new ArrayList<>();
        Node current = ORIGIN;
        Queue<Node> queue = new ArrayDeque<>();
        do {
            if (current.pos.equals(TARGET)) ends.add(current);
            // get the neighbours of the pos, which will be ranked
            queue.addAll(current.getNeighbours()); // add them to the queue
            // choose the highest ranking pos, and repeat.
            queue = queue.stream().sorted().collect(Collectors.toCollection(ArrayDeque::new));
            current = queue.poll();

        } while (queue.peek() != null);
        // if we reach the end
        System.out.println(current);
        ends.forEach(System.out::println);
    }

    @Override
    public int number() {
        return 17;
    }
}
