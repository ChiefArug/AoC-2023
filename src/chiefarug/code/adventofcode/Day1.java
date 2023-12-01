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
            Character number = getNumberAt(s, i);
            if (number != null) return number;
        }
        throw new RuntimeException("No digit found in input string " + s);
    }
    private static char getLastDigit(String s) {
        for (int i = s.length() - 1; i > -1; i--) {
            if (Character.isDigit(s.charAt(i))) return s.charAt(i);
            Character number = getNumberAt(s, i);
            if (number != null) return number;
        }
        throw new RuntimeException("No digit found in input string '" + s + "'");
    }

    private static final String[] numbers = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    public static Character getNumberAt(String s, int startIndex) {
        for (int i = 0; i < numbers.length; i++) {
            if (s.substring(startIndex).startsWith(numbers[i])) {
                Character c = Character.forDigit(i, 10);
                if (c.equals('\u0000')) throw new RuntimeException("How did that happen? " + s.substring(startIndex));
                return c;
            }
        }
        return null;
    }
}
