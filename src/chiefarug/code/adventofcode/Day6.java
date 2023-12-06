package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day6 implements Day {

    record QuadraticSolution(double min, double max) {
        @SuppressWarnings({"SpellCheckingInspection", "SameParameterValue"})
        static QuadraticSolution solve(long a, long b, long c) {
            /*
            x = b +- sqrt(b^2 - 4ac) / 2a
             */
            long bSquared = b * b;
            long fourac = 4 * a * c;
            long twoa = 2 * a;
            double sqrted = Math.sqrt(bSquared - fourac);
            double topHalfPositive = -b + sqrted;
            double topHalfNegative = -b - sqrted;

            double x1 = topHalfPositive / twoa;
            double x2 = topHalfNegative / twoa;
            return new QuadraticSolution(Math.min(x1, x2), Math.max(x1, x2));
        }
    }

    record Race(long time, long recordDistance) {
    }

    @Override
    public void run(BufferedReader input) {
        long count;
        try {
            String firstLine = Arrays.stream(input.readLine().substring(9).split(" ")).filter(Predicate.not(String::isEmpty)).collect(Collectors.joining());
            String secondLine = Arrays.stream(input.readLine().substring(9).split(" ")).filter(Predicate.not(String::isEmpty)).collect(Collectors.joining());
            Race race = new Race(Long.parseLong(firstLine), Long.parseLong(secondLine));


            QuadraticSolution timesForRecord = QuadraticSolution.solve(-1, race.time, -race.recordDistance);
            long minTime = ((long) Math.floor(timesForRecord.min)) + 1;
            long maxTime = ((long) Math.ceil(timesForRecord.max)) - 1;
            count = maxTime - minTime + 1;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        System.out.println(count);
    }

    @Override
    public int number() {
        return 6;
    }
}
