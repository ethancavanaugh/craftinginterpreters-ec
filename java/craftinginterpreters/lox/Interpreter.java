package craftinginterpreters.lox;

public class Interpreter implements Expr.Visitor<Object> {
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                else if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
            case MINUS: return (double) left - (double) right;
            case STAR: return (double) left * (double) right;
            case SLASH: return (double) left / (double) right;

            case GREATER: return (double)left > (double)right;
            case GREATER_EQUAL: return (double)left >= (double)right;
            case LESS: return (double)left < (double)right;
            case LESS_EQUAL: return (double)left <= (double)right;

            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    //Null and false booleans are falsy, all other values are truthy
    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (boolean) o;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        else if (left == null) return false;

        return left.equals(right);
    }
}
