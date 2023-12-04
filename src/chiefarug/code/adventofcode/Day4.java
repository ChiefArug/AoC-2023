package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day4 implements Day {

    record RawCardData(String winning, String obtained) {
        CardData parse() {
            return new CardData(
                    Arrays.stream(winning.split(" "))
                            .map(String::strip)
                            .filter(Predicate.not(String::isEmpty))
                            .mapToInt(Integer::parseInt)
                            .toArray(),
                    Arrays.stream(obtained.split(" "))
                            .map(String::strip)
                            .filter(Predicate.not(String::isEmpty))
                            .map(Integer::valueOf)
                            .collect(Collectors.toUnmodifiableSet())
            );
        }
    }

    record CardData(int[] winning, Set<Integer> obtained) {
        int getScore() {
            return (int) Math.floor(Math.pow(2, Arrays.stream(winning)
                    .filter(obtained::contains)
                    .count() - 1));
        }
    }

    @Override
    public void run(BufferedReader input) {
        AtomicInteger count = new AtomicInteger();
        try {
            input.lines()
                    .map(s -> s.split(":")[1].split("\\|"))
                    .map(sa -> new RawCardData(sa[0], sa[1]))
                    .map(RawCardData::parse)
                    .mapToInt(CardData::getScore)
                    .forEach(count::addAndGet);
        } catch (Exception e) {
            System.out.println("current count: " + count);
            throw e;
        }
        System.out.println(count);
    }

    @Override
    public int number() {
        return 4;
    }
}
