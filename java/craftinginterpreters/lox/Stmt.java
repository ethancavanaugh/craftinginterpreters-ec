package craftinginterpreters.lox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitBreakStmt(Break stmt);
		R visitContinueStmt(Continue stmt);
		R visitBlockStmt(Block stmt);
		R visitExpressionStmt(Expression stmt);
		R visitFunctionStmt(Function stmt);
		R visitIfStmt(If stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
		R visitWhileStmt(While stmt);
	}

	abstract <R> R accept(Visitor<R> visitor);

	static class Break extends Stmt {

		public Break() {
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}
	}
	static class Continue extends Stmt {

		public Continue() {
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitContinueStmt(this);
		}
	}
	static class Block extends Stmt {
		final List<Stmt> statements;

		public Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}
	}
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
	static class Function extends Stmt {
		final Token name;
		final List<Token> params;
		final List<Stmt> body;

		public Function(Token name, List<Token> params, List<Stmt> body) {
			this.name = name;
			this.params = params;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStmt(this);
		}
	}
	static class If extends Stmt {
		final Expr condition;
		final Stmt thenBranch;
		final Stmt elseBranch;

		public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
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
	static class While extends Stmt {
		final Expr condition;
		final Stmt body;
		final Expr increment;

		public While(Expr condition, Stmt body, Expr increment) {
			this.condition = condition;
			this.body = body;
			this.increment = increment;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}
	}
}
