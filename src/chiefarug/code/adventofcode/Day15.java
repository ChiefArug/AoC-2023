package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Day15 implements Day {

    static class Box extends ArrayList<Lens> {
        void replaceOrAdd(Lens lens) {
            if (lens.focalLength == -1) throw new WatException();
            int index = indexOf(lens);
            if (index == -1) {
                add(lens);
                return;
            }
            set(index, lens);
        }

    }

    record Lens(String id, byte focalLength) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Lens lens = (Lens) o;
            return id.equals(lens.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "[" + id + " " + focalLength + "]";
        }
    }

    Box[] boxes = new Box[256];

    {
        Arrays.setAll(boxes, _i -> new Box());
    }


    interface Action {
        Action REMOVE = Box::remove;
        Action ADD = Box::replaceOrAdd;

        void apply(Box box, Lens lens);
    }

    @Override
    public void run(BufferedReader input) {
        try {
            String oneLongLine = input.readLine();
            Arrays.stream(oneLongLine.split(",")).forEach(string -> {
                Action action;
                byte lensSize = -1;
                switch (string.charAt(string.length() - 1)) {
                    case '-' -> {
                        action = Action.REMOVE;
                        string = string.substring(0, string.length() - 1);
                    }
                    // add/replace
                    default -> {
                        action = Action.ADD;
                        lensSize = Byte.parseByte(string.substring(string.length() - 1));
                        string = string.substring(0, string.length() - 2);
                    }
                }

                // this is an unsigned byte. + * and - all work as normal on it. / does not, but we don't need that.
                byte currentValue = 0;

                byte[] asciiValues = string.getBytes(StandardCharsets.US_ASCII);

                for (byte asciiValue : asciiValues) {
                    // addition, subtraction and multiplication all work
                    currentValue += asciiValue;
                    currentValue *= 17;
                    // we don't need to %= because this is an unsigned byte so that is done automagically
                }

                int actualValue = Byte.toUnsignedInt(currentValue);
                action.apply(boxes[actualValue], new Lens(string, lensSize));
            });
            long focusingPower = 0;

            for (int b = 0; b < boxes.length; b++) {
                Box box = boxes[b];
                for (int l = 0; l < box.size(); l++) {
                    Lens lens = box.get(l);
                    focusingPower += (long) (1 + b) * (1 + l) * lens.focalLength;
                }

            }


            System.out.println(focusingPower);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int number() {
        return 15;
    }
}
