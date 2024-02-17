package craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
	}

	abstract <R> R accept(Visitor<R> visitor);

	static class Expression extends Stmt {
		final Expr expression;

		public Expression(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}
	}
	static class Print extends Stmt {
		final Expr expression;

		public Print(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}
	}
	static class Var extends Stmt {
		final Token name;
		final Expr initializer;

		public Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}
	}
}
