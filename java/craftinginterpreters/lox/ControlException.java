package craftinginterpreters.lox;

public class ControlException extends RuntimeException {
    static class BreakException extends ControlException {}
    static class ContinueException extends ControlException {}
}
