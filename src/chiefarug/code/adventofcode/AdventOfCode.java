package chiefarug.code.adventofcode;

import java.io.*;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class AdventOfCode {

    public static final Day day;

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

    public static BufferedReader loadInput(String inputUrl) {
        try {
            URL url = new URL(inputUrl);
            return new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void log(String s) {
        System.out.println(s);
    }

}