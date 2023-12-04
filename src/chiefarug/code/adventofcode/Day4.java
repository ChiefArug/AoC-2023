package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day4 implements Day {

    record RawCardData(int cardId, String winning, String obtained) {
        CardData parse() {
            return new CardData(
                    cardId,
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

    record CardData(int cardId, int[] winning, Set<Integer> obtained) {
        int getWinners() {
            return (int) Arrays.stream(winning)
                    .filter(obtained::contains)
                    .count();
        }
    }

    record CardCopier(CardData card, AtomicInteger copies) {
        void copy(int times) {
            copies().addAndGet(times);
        }
    }

    @Override
    public void run(BufferedReader input) {
        AtomicInteger count = new AtomicInteger();

        Map<Integer, CardCopier> cards = input.lines()
                .map(s -> Arrays.stream(s.split("Card *|[:|]")).filter(Predicate.not(String::isBlank)).toArray(String[]::new))
                .map(sa -> new RawCardData(Integer.parseInt(sa[0]), sa[1], sa[2]))
                .map(RawCardData::parse)
                .collect(Collectors.toMap(c -> c.cardId, c -> new CardCopier(c,new AtomicInteger(1))));

        cards.forEach((id, card) -> {
            int winners = card.card.getWinners();
            while (winners > 0)
                cards.get(id + winners--).copy(card.copies.get());
        });

        cards.forEach((_i, card) -> count.addAndGet(card.copies.get()));

        System.out.println(count);
    }

    @Override
    public int number() {
        return 4;
    }
}
