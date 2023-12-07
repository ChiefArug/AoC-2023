package chiefarug.code.adventofcode;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;

public class Day7 implements Day {

    @SuppressWarnings("unused")
    enum Card {
        A, K, Q, J, T, _9, _8, _7, _6, _5, _4, _3, _2;

        static Card fromChar(char c) {
            if (Character.isDigit(c)) return valueOf("_" + c);
            return valueOf(String.valueOf(c));
        }

        static CardList fromString(String s) {
            return CardList.fromArray(s.chars().mapToObj(i -> fromChar((char) i)).toArray(Card[]::new));
        }
    }

    enum WinType {
        FIVE_OF_A_KIND,
        FOUR_OF_A_KIND,
        FULL_HOUSE,
        THREE_OF_A_KIND,
        TWO_PAIRS,
        ONE_PAIR,
        HIGH_CARD;


        static WinType forCards(CardList cards) {
            WinType bestWin;
            int uniques = EnumSet.of(cards.c1(), cards.c2(), cards.c3(), cards.c4(), cards.c5()).size();

            bestWin = switch (uniques) {
                case 1 -> FIVE_OF_A_KIND;
                case 2 -> {
                    Card[] sortedCards = new Card[5];
                    System.arraycopy(cards.cards, 0, sortedCards, 0, 5);
                    Arrays.sort(sortedCards);
                    // if we can confirm two pairs at either sorted end, the middle card doesn't matter as it is one of the two types
                    if (sortedCards[0] == sortedCards[1] && sortedCards[3] == sortedCards[4])
                        yield FULL_HOUSE;
                    yield FOUR_OF_A_KIND;
                }
                case 3 -> {
                    Card[] sortedCards = new Card[5];
                    System.arraycopy(cards.cards, 0, sortedCards, 0, 5);
                    Arrays.sort(sortedCards);

                    int m = 1;
                    for (int i = 1; i < sortedCards.length; i++) {
                        if (sortedCards[i] == sortedCards[i - 1])
                            m++;
                        else {
                            if (m == 1) continue;
                            yield switch (m) {
                                case 2 -> TWO_PAIRS;
                                case 3 -> THREE_OF_A_KIND;
                                default -> throw new WatException();
                            };
                        }
                    }
                    yield switch (m) {
                        case 2 -> TWO_PAIRS;
                        case 3 -> THREE_OF_A_KIND;
                        default -> throw new WatException();
                    };
                }
                case 4 -> ONE_PAIR;
                case 5 -> HIGH_CARD;
                default -> throw new WatException();
            };
            return bestWin;
        }
    }

    record CardList(Card[] cards) {
        static CardList fromArray(Card[] ca) {
            if (ca.length != 5) throw new IllegalArgumentException("Card list length not five");
            return new CardList(ca);
        }

        Card c1() {
            return cards[0];
        }

        Card c2() {
            return cards[1];
        }

        Card c3() {
            return cards[2];
        }

        Card c4() {
            return cards[3];
        }

        Card c5() {
            return cards[4];
        }

        @Override
        public String toString() {
            return Arrays.toString(cards);
        }
    }

    record Hand(CardList cards, int bid) {
    }

    record ResultHand(WinType winType, Hand hand) implements Comparable<ResultHand> {
        static ResultHand fromHand(Hand hand) {
            return new ResultHand(WinType.forCards(hand.cards), hand);
        }

        int bid() {
            return hand.bid();
        }

        CardList cards() {
            return hand.cards();
        }

        @Override
        public int compareTo(@NotNull ResultHand rh) {
            if (rh == this) return 0;
            int defaultSort = winType.compareTo(rh.winType);
            if (defaultSort != 0) return defaultSort;
            for (int i = 0; i < 5; i++) {
                int backupSort = cards().cards[i].compareTo(rh.cards().cards[i]);
                if (backupSort != 0) return backupSort;
            }
            throw new IllegalArgumentException("Two sets of cards are identical!");
        }
    }

    @Override
    public void run(BufferedReader input) {
        long winnings = 0;

        ResultHand[] sortedResults = input.lines()
                .map(s -> new Hand(Card.fromString(s.substring(0, 5)), Integer.parseInt(s.substring(6))))
                .map(ResultHand::fromHand)
                .sorted(Comparator.reverseOrder())
                .toArray(ResultHand[]::new);

        for (int i = 0; i < sortedResults.length; i++) {
            winnings += (i + 1L) * sortedResults[i].bid();
        }

        System.out.println(winnings);
    }

    @Override
    public int number() {
        return 7;
    }
}
