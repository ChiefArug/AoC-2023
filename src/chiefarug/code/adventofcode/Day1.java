package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.concurrent.atomic.AtomicInteger;

public class Day1 implements Day {
    public int number() { return 1; }

    static class Pair<L, R> {
        private final L l;
        private final R r;
        Pair(L l, R r) {
            this.l = l;
            this.r = r;
        }
        private R right() { return r; }
        private L left() { return l; }
    }

    public void run(BufferedReader input) {
        AtomicInteger sum = new AtomicInteger();
        try (input) {
            input.lines()
                    .map(s -> new Pair<Character, Character>(getFirstDigit(s), getLastDigit(s)))
                    .map(p -> Integer.valueOf("" + p.left() + p.right()))
                    .forEach(sum::addAndGet);
            System.out.println(sum);

        } catch (Exception e) {
            System.out.println("Sum so far: " + sum);
            throw new RuntimeException(e);
        }
    }

    private static char getFirstDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) return s.charAt(i);
        }
        throw new RuntimeException("No digit found in input string " + s);
    }
    private static char getLastDigit(String s) {
        for (int i = s.length() - 1; i > -1; i--) {
            if (Character.isDigit(s.charAt(i))) return s.charAt(i);
        }
        throw new RuntimeException("No digit found in input string " + s);
    }
}
