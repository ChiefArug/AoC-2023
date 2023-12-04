package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdventOfCode {

    public static final Day day = new Day4();

    public static void main(String[] args) {
        BufferedReader reader;

        if (args.length > 0 && args[0].equals("debug")) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        } else {
            try {
                reader = new BufferedReader(new FileReader("day" + day.number() + ".txt"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        day.run(reader);

        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static void log(String s) {
        System.out.println(s);
    }

}