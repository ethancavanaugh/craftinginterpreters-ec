package craftinginterpreters.lox;

public class ControlException extends RuntimeException {
    ControlException() {
        super(null, null, false, false);
    }

    static class BreakException extends ControlException {}
    static class ContinueException extends ControlException {}

    static class ReturnException extends ControlException {
        final Object value;

        ReturnException(Object value) {
            this.value = value;
        }
    }
}
