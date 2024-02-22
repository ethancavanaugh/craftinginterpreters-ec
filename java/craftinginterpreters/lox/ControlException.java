package craftinginterpreters.lox;

public class ControlException extends RuntimeException {
    ControlException() {
        super(null, null, false, false);
    }

    static class Break extends ControlException {}
    static class Continue extends ControlException {}

    static class Return extends ControlException {
        final Object value;

        Return(Object value) {
            this.value = value;
        }
    }
}
