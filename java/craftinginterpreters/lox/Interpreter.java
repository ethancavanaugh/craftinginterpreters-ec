package craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {return "<native fn>";}
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        }
        catch (RuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new ControlException.Break();
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        throw new ControlException.Continue();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        }
        else if (stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);

        throw new ControlException.Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) value = evaluate(stmt.initializer);

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while(isTruthy(evaluate(stmt.condition))) {
            try {
                executeInLoop(stmt.body);
            }
            catch (ControlException e) {
                if (e instanceof ControlException.Break) break;
                if (e instanceof ControlException.Continue) {
                    //Still carries out increment expression if in for loop
                    if (stmt.increment != null) evaluate(stmt.increment);
                }
            }
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

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
                //Number + String converts number to a string and concatenates
                else if ((left instanceof String && right instanceof Double) || (left instanceof Double && right instanceof String)){
                    return stringify(left)+ stringify(right);
                }
                else {
                    throw new RuntimeError(expr.operator, "Operands must both be numbers or strings.");
                }
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;

            case GREATER:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }
                else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) > 0;
                }
                else {
                    throw new RuntimeError(expr.operator, "Operands must both be numbers or strings.");
                }
            case GREATER_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }
                else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) >= 0;
                }
                else {
                    throw new RuntimeError(expr.operator, "Operands must both be numbers or strings.");
                }
            case LESS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }
                else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) < 0;
                }
                else {
                    throw new RuntimeError(expr.operator, "Operands must both be numbers or strings.");
                }
            case LESS_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }
                else if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) <= 0;
                }
                else {
                    throw new RuntimeError(expr.operator, "Operands must both be numbers or strings.");
                }

            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
        "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    //Logical operators do not return true/false,
    //but one of the operands with appropriate truthiness
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        }
        else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        if (isTruthy(evaluate(expr.condition))) {
            return evaluate(expr.trueExpr);
        }
        return evaluate(expr.falseExpr);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private void checkNumberOperands(Token operator, Object... operands) {
        for (Object operand : operands) {
            if (!(operand instanceof Double)) {
                throw new RuntimeError(operator, "Operand must be a number.");
            }
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private void executeInLoop(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> stmts, Environment environment) {
        Environment previousEnv = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        }
        finally {
            this.environment = previousEnv;
        }
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

    private String stringify(Object o) {
        if (o == null) return "nil";

        if (o instanceof Double) {
            String text = o.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return o.toString();
    }
}
