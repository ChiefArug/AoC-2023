package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;

public class Day8 implements Day {

    record PathChoice(String left, String right) {}

    record Node(String name, PathChoice next) {

    }
    @Override
    public void run(BufferedReader input) {
        try {
            String directionInstructions = input.readLine();
            boolean[] leftArray = new boolean[directionInstructions.length()];
            for (int i = 0;i < directionInstructions.length(); i++) {
                leftArray[i] = directionInstructions.charAt(i) == 'L';
            }

            input.readLine();
            Map<String, PathChoice> map = input.lines()
                    .map(s -> new Node(s.substring(0, 3), new PathChoice(s.substring(7, 10), s.substring(12, 15))))
                    .collect(Collectors.toMap(Node::name, Node::next));

            String[] startNodes = map.keySet().stream().filter(s -> s.endsWith("A")).toArray(String[]::new);
            BigInteger lcm = BigInteger.valueOf(getSteps(leftArray, map, startNodes[0]));
            for (int i = 1; i < startNodes.length; i++) {
                String startNode = startNodes[i];
                lcm = lcm(lcm, BigInteger.valueOf(getSteps(leftArray, map, startNode)));
            }

            System.out.println(lcm);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b).abs().divide(a.gcd(b));
    }

    private static int getSteps(boolean[] leftArray, Map<String, PathChoice> map, String startNode) {
        int steps = 0;
        do {
            boolean shouldGoLeft = leftArray[steps % leftArray.length];
            PathChoice pathChoice = map.get(startNode);
            startNode = shouldGoLeft ? pathChoice.left() : pathChoice.right();
            steps++;
        } while (!startNode.endsWith("Z"));
        return steps;
    }

    @Override
    public int number() {
        return 8;
    }
}
