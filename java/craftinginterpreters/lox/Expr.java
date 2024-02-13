package craftinginterpreters.lox;

import java.util.List;

abstract class Expr {
	static class Binary extends Expr {
		final Expr left;
		final Token operator;
		final Expr right;

		public Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}
	}
	static class Grouping extends Expr {
		final Expr expression;

		public Grouping(Expr expression) {
			this.expression = expression;
		}
	}
	static class Literal extends Expr {
		final Object value;

		public Literal(Object value) {
			this.value = value;
		}
	}
	static class Unary extends Expr {
		final Token operator;
		final Expr right;

		public Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}
	}
}
