package chiefarug.code.adventofcode;

public class WatException extends RuntimeException {
    public WatException() {
        this("");
    }
    public WatException(String message) {
        super(message.isEmpty() ? "wat" : "wat: " + message);
    }
}
