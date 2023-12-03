package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class Day3 implements Day {
    @Override
    public void run(BufferedReader input) {
        char[][] schematic = input.lines().map(String::toCharArray).toArray(char[][]::new);
        int count = 0;
        List<Integer> validNumber = new ArrayList<>();

        for (int y = 0; y < schematic.length; y++) {
            for (int x = 0; x < schematic[y].length; x++) {
                char c = schematic[y][x];
                if (Character.isDigit(c)) {
                    StringBuilder number = new StringBuilder();
                    boolean isValid = safeCheckColumnForSymbols(x - 1, y, schematic);

                    do {
                        isValid |= safeCheckColumnForSymbols(x, y, schematic);
                        number.append(c);
                        if (x + 1 < schematic[y].length)
                            c = schematic[y][++x];
                        else break;
                    } while (Character.isDigit(c));

                    isValid |= safeCheckColumnForSymbols(x, y, schematic);

                    if (isValid) {
                        count += Integer.parseInt(number.toString());
                        validNumber.add(Integer.valueOf(number.toString()));
                    }
                }
            }
        }
        System.out.println(validNumber);
        System.out.println(count);
    }

    private boolean safeCheckColumnForSymbols(int x, int y, char[][] schematic) {
        if (x < 0 || x >= schematic[y].length) return false;

        int maxHeight = schematic.length;
        int minHeight = -1;

        return isSymbol(schematic[y][x]) ||
                (maxHeight > y + 1 && isSymbol(schematic[y + 1][x])) ||
                (minHeight < y - 1 && isSymbol(schematic[y - 1][x]));
    }

    private boolean isSymbol(char c) {
        return c != '.' && !Character.isDigit(c) && !Character.isAlphabetic(c);
    }

    @Override
    public int number() {
        return 3;
    }
}
