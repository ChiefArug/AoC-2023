package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Day3 implements Day {

    record Pos(int y, int x) {
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof Pos pos) return y == pos.y && x == pos.x;
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(y, x);
        }
    }

    static class GearData {
        int ratio;
        Boolean invalid;
        GearData(int firstPower) {
            ratio = firstPower;
        }
        void addGear(int power) {
            if (invalid == null) {
                ratio *= power;
                invalid = false;
            } else if (!invalid) {
                invalid = true;
            }
        }

        boolean isValid() {
            return invalid == Boolean.FALSE;
        }

        @Override
        public String toString() {
            return isValid() ? "INVALID" : "Ratio(" + ratio + ")";
        }
    }

    @Override
    public void run(BufferedReader input) {
        char[][] schematic = input.lines().map(String::toCharArray).toArray(char[][]::new);
        int count = 0;
        Map<Pos, GearData> gears = new HashMap<>();

        for (int y = 0; y < schematic.length; y++) {
            for (int x = 0; x < schematic[y].length; x++) {
                char c = schematic[y][x];
                if (Character.isDigit(c)) {
                    StringBuilder number = new StringBuilder();

                    List<Pos> foundGears = new ArrayList<>(safeCheckColumnForGears(x - 1, y, schematic));

                    do {
                        foundGears.addAll(safeCheckColumnForGears(x, y, schematic));
                        number.append(c);
                        if (x + 1 < schematic[y].length)
                            c = schematic[y][++x];
                        else break;
                    } while (Character.isDigit(c));

                    foundGears.addAll(safeCheckColumnForGears(x, y, schematic));

                    for (Pos foundGear : foundGears) {
                        GearData gearData = gears.get(foundGear);
                        if (gearData == null) gears.put(foundGear, new GearData(Integer.parseInt(number.toString())));
                        else gearData.addGear(Integer.parseInt(number.toString()));
                    }
                }
            }
        }

        for (GearData gear : gears.values()) {
            if (gear.isValid())
                count += gear.ratio;
        }
        System.out.println(gears);
        System.out.println(count);
    }

    private List<Pos> safeCheckColumnForGears(int x, int y, char[][] schematic) {
        if (x < 0 || x >= schematic[y].length) return List.of();
        List<Pos> found = new ArrayList<>();

        int maxHeight = schematic.length;
        int minHeight = -1;

        if (isGear(schematic[y][x])) found.add(new Pos(y, x));
        if (maxHeight > y + 1 && isGear(schematic[y + 1][x])) found.add(new Pos(y + 1, x));
        if (minHeight < y - 1 && isGear(schematic[y - 1][x])) found.add(new Pos(y - 1, x));
        return found.isEmpty() ? List.of() : found;
    }

    private boolean isGear(char c) {
        return c == '*';
    }


    @Override
    public int number() {
        return 3;
    }
}
