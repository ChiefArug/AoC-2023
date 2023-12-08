package chiefarug.code.adventofcode;

import java.io.BufferedReader;
import java.io.IOException;
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

            int steps = 0;
            String currentNode = "AAA";
            do {
                boolean shouldGoLeft = leftArray[steps % leftArray.length];
                PathChoice pathChoice = map.get(currentNode);
                currentNode = shouldGoLeft ? pathChoice.left() : pathChoice.right();
                steps++;
            } while (!currentNode.equals("ZZZ"));

            System.out.println(steps);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int number() {
        return 8;
    }
}
