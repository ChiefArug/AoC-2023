package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Day2 implements Day {

    static class Mutable<T> implements Supplier<T> {

        Mutable(T value) {
            this.value = value;
        }
        private T value;

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    record Draw(int red, int green, int blue) {
        static Draw parse(String in) {
            Mutable<Integer> red = new Mutable<>(0);
            Mutable<Integer> green = new Mutable<>(0);
            Mutable<Integer> blue = new Mutable<>(0);
            for (String s : in.split(", ")) {

                int n;
                String colour;
                if (Character.isDigit(s.charAt(1))) {
                    n = Integer.parseInt(s.substring(0, 2));
                    colour = s.substring(3);
                } else {
                    n = Integer.parseInt(s.substring(0, 1));
                    colour = s.substring(2);
                }
                switch (colour) {
                    case "red" -> red.set(n);
                    case "blue" -> blue.set(n);
                    case "green" -> green.set(n);
                    default -> throw new RuntimeException("Unknown colour '" + "'!");
                }
            }
            return new Draw(red.get(), green.get(), blue.get());
        }

        boolean isWithinTarget(Draw target) {
            return target.red() >= red() && target.green() >= green() && target.blue() >= blue();
        }
    }

    record Game(int id, List<Draw> draws) {
        static Game parse(String in) {
            int colonIndex = in.indexOf(':');
            int id = Integer.parseInt(in.substring(5, colonIndex));

            String draws = in.substring(colonIndex + 1);
            List<Draw> drawsList = Arrays.stream(draws.split("; "))
                    .map(String::trim)
                    .map(Draw::parse)
                    .toList();

            return new Game(id, drawsList);
        }
    }

    @Override
    public void run(BufferedReader input) {
        Draw target = new Draw(12, 13, 14);

        AtomicInteger count = new AtomicInteger();
        try {
            input.lines()
                    .map(Game::parse)
                    .filter(game -> game.draws().stream()
                            .filter(d -> d.isWithinTarget(target))
                            .count() == game.draws().size()
                    )
                    .forEach(game -> count.addAndGet(game.id()));
        } catch (Exception e) {
            System.out.println("Count so far: " + count);
            throw e;
        }

        System.out.println(count);
    }

    @Override
    public int number() {
        return 2;
    }
}
