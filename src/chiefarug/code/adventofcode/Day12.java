package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("StringConcatenationInLoop")
public class Day12 implements Day {

    final static Pattern[] regexes = new Pattern[17];
    final static String[] damaged = new String[17];
    final static Pattern anyGroup = Pattern.compile("(?<=^|.)#+(?=.|$)");

    static {
        String regex = "(?<=^|[.?])"; // lookbehind for start of string or .
        String dmg = "";
        for (int i = 1; i < regexes.length; i++) {
            // we want to do this so that each thing is its own instance
            regexes[i] = Pattern.compile((regex += "[#?]") // # or ?
                    + "(?=[.?]|$)"); // lookahead for end of string or .
            damaged[i] = dmg += "#";
        }
        for (Pattern pattern : regexes) {
            System.out.println(pattern);
        }
    }

    record CacheKey(String springSet, int damageLengthIndex, int startIndex) {
    }
    record CacheValue(int permutations, String sourceString) {}

    static final class SpringGroup {
        private final String springs;
        private final int[] damageLengths;

        SpringGroup(String springs, int[] damageLengths) {
            this.springs = springs;
            this.damageLengths = damageLengths;
        }

        private static final Map<CacheKey, CacheValue> matchToPermutationsCache = new HashMap<>();
        long cacheHits = 0;

        int check() {
            int subIndex = 0;
            int permutations = 0;

            System.out.println("\nLooking for: " + springs + " with damage lengths " + Arrays.toString(damageLengths));
            permutations += findMatches(springs, subIndex, 0);
            System.out.println("Cache hits: " + cacheHits);

            System.out.println("Combinations found: " + permutations);
            return permutations;
        }

        private int findMatches(final String original, int startIndex, int damageLengthsIndex) {
            int permutations = 0;
            final boolean isLast = damageLengthsIndex + 1 >= damageLengths.length;

            do {
                // AREA: setup
                StringBuilder current = new StringBuilder(original);
                int damageLength = damageLengths[damageLengthsIndex];

                Matcher match = regexes[damageLength].matcher(current);
                match.useTransparentBounds(true); // allow matching . outside of the bounds

                match.region(startIndex, match.regionEnd());


                // AREA: Find a match
                if (!match.find()) {
//                    System.out.println("Skipping combination for inserting damage length of " + damageLength + " into " + current + " (originally " + springs + " ) as no valid spot was found");
                    return permutations;
                }
                int start = match.start();
                final int matchStart = start;
                int end = match.end();

                // AREA: Get the string to insert
                StringBuilder toInsert = new StringBuilder(damaged[damageLength]);
                if (start != 0) {
                    toInsert.insert(0, '.');
                    start--; // move start to the left by one
                }
                if (end != current.length()) {
                    toInsert.append('.');
                    end++; // move end to the right by one
                }
                if (end - start != toInsert.length()) throw new WatException("oops");

                // AREA: Insert the string and set the new start searching from index. The replacement is for debug and printing purposes
                current.replace(start, end, toInsert.toString());
                startIndex = matchStart;
                do {startIndex++;} while (startIndex < original.length() && original.charAt(startIndex) == '.'); // skip blank sections

                // AREA: Process the next permutation(s)
                if (!isLast) {
                    int newPermutations;
                    int startSearchPoint = end - 1; // subtract one to undo the first ++ operation
                    do {
                        CacheKey mtc = new CacheKey(springs, damageLengthsIndex + 1, ++startSearchPoint);
                        if (!matchToPermutationsCache.containsKey(mtc)) {
                            // pass the current one in instead of the root one for debugging purposes. it should work perfectly fine using the root one though
                            newPermutations = findMatches(current.toString(), mtc.startIndex, mtc.damageLengthIndex);
                            matchToPermutationsCache.put(mtc, new CacheValue(newPermutations, current.toString()));
                        } else {
//                            System.out.println("cache hit");
                            cacheHits++;
                            CacheValue value = matchToPermutationsCache.get(mtc);
                            newPermutations = value.permutations;
//                            if (newPermutations != findMatches(current.toString(), mtc.startIndex, mtc.damageLengthIndex))
//                                System.out.println("Bad cache hit!");
                        }
                    } while (newPermutations < 1 && startSearchPoint + 1 < current.length());
                    if (newPermutations == 0)
                        continue; //if we really didn't find anything, we cant do much more with this current setup, so move to find the next match
                    // if we did find something add its permutations up
                    permutations += (newPermutations - 1);
                } else {
                    // this is the last one, check if there are more groups than there should be (caused by additional # after the last match we replaced)
                    if (anyGroup.matcher(current).results().count() != damageLengths.length) return permutations;
                }


                // AREA: Do cleanup and fancy printing
                if (springs.length() != current.length()) throw new WatException("oops");
                if (isLast)
//                    System.out.println("Found permutation for " + springs + " " + Arrays.toString(damageLengths) + "! " + current.toString().replace('?', '.'));
                    System.out.println("Permutation: " + current.toString().replace('?', '.'));
                permutations++;
            } while (true);
        }
    }

    @Override
    public void run(BufferedReader input) {
        AtomicLong sum = new AtomicLong();
        try {
            input.lines()
                    .map(s -> s.split(" "))
                    .peek(s -> byFive(s, 0, '?'))
                    .peek(s -> byFive(s, 1, ','))
                    .map(s -> new SpringGroup(s[0], Arrays.stream(s[1].split(",")).mapToInt(Integer::parseInt).toArray()))
                    .mapToInt(SpringGroup::check)
                    .forEach(sum::addAndGet);
        } catch (Exception e) {
            System.out.println("sum so far: " + sum);
            throw e;
        }

        System.out.println(sum);


    }

    private static void byFive(String[] s, int index, char separator) {
        s[index] = s[index] + separator + s[index] + separator + s[index] + separator + s[index] + separator + s[index];
    }

    @Override
    public int number() {
        return 12;
    }
    /*
    answers tried
    9241 (too high)
    5925 (too low)
    **** CORRECT!
     */
}
