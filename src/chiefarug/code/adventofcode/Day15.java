package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Day15 implements Day {

    @Override
    public void run(BufferedReader input) {
        try {
            String oneLongLine = input.readLine();
            int result = Arrays.stream(oneLongLine.split(","))
                    .mapToInt(string -> {
                        // this is an unsigned byte. + * and - all work as normal on it. / does not, but we dont need that.
                        byte currentValue = 0;

                        byte[] asciiValues = string.getBytes(StandardCharsets.US_ASCII);

                        for (byte asciiValue : asciiValues) {
                            // addition, subtraction and multiplication all work
                            currentValue += asciiValue;
                            currentValue *= 17;
                            // we don't need to %= because this is an unsigned byte so that is done automagically
                        }
                        return Byte.toUnsignedInt(currentValue);
                    })
                    .sum();
            System.out.println(result);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int number() {
        return 15;
    }
}
