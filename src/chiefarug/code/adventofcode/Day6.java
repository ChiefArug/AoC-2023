package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

public class Day6 implements Day {

    record QuadraticSolution(double min, double max) {
        @SuppressWarnings({"SpellCheckingInspection", "SameParameterValue"})
        static QuadraticSolution solve(int a, int b, int c) {
            /*
            x = b +- sqrt(b^2 - 4ac) / 2a
             */
            int bSquared = b * b;
            int fourac = 4 * a * c;
            int twoa = 2 * a;
            double sqrted = Math.sqrt(bSquared - fourac);
            double topHalfPositive = -b + sqrted;
            double topHalfNegative = -b - sqrted;

            double x1 = topHalfPositive / twoa;
            double x2 = topHalfNegative / twoa;
            return new QuadraticSolution(Math.min(x1, x2), Math.max(x1, x2));
        }
    }

    record Race(int time, int recordDistance) {}

    @Override
    public void run(BufferedReader input) {
        long count = 1;
        try {
            String[] firstLine = Arrays.stream(input.readLine().substring(9).split(" ")).filter(Predicate.not(String::isEmpty)).toArray(String[]::new);
            String[] secondLine = Arrays.stream(input.readLine().substring(9).split(" ")).filter(Predicate.not(String::isEmpty)).toArray(String[]::new);
            Race[] races = new Race[firstLine.length];

            for (int i = 0; i < firstLine.length; i++) {
                races[i] = new Race(Integer.parseInt(firstLine[i].strip()), Integer.parseInt(secondLine[i].strip()));
            }



            for (Race race : races) {
                QuadraticSolution timesForRecord = QuadraticSolution.solve(-1, race.time, -race.recordDistance);
                int minTime = ((int) Math.floor(timesForRecord.min)) + 1;
                int maxTime = ((int) Math.ceil(timesForRecord.max)) - 1;
                int possibilities = maxTime - minTime + 1;
                count *= possibilities;
            }
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
