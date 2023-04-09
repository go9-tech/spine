/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.go9.spine.web.internal.usecase;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;

import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.ConnectByRootOperator;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.RowGetExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.TryCastExpression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.ValueListExpression;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.XMLSerializeExpr;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SubSelect;
import tech.go9.spine.web.api.function.PredicateFunction;
import tech.go9.spine.web.api.function.PredicateVisitor;
import tech.go9.spine.web.internal.utils.QuerydslUtils;
import tech.go9.spine.core.api.exception.NotImplementedException;
import tech.go9.spine.core.api.exception.UnexpectedException;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DefaultPredicateVisitor implements PredicateVisitor {

	private final Map<String, PredicateFunction> predicateFunctionMap;

	private SimplePath<?> rootPath;

	private Stack<Object> stack;

	public DefaultPredicateVisitor(Map<String, PredicateFunction> predicateFunctionMap) {
		this.predicateFunctionMap = predicateFunctionMap;
	}

	public void init(SimplePath<?> rootPath) {
		this.rootPath = rootPath;
		this.stack = new Stack<Object>();
	}

	@Override
	public void visit(BitwiseRightShift aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(BitwiseLeftShift aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(NullValue nullValue) {
		this.stack.push(Expressions.nullExpression());
	}

	@Override
	public void visit(Function function) {
		String functionName = String.format("%sPredicateFunction", function.getName());
		PredicateFunction predicateFunction = this.predicateFunctionMap.get(functionName);
		if (predicateFunction == null) {
			throw new UnexpectedException("Unknown function %s", functionName);
		}
		this.stack.push(predicateFunction.visit(rootPath, function.getParameters()));
	}

	@Override
	public void visit(SignedExpression signedExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		this.stack.push(doubleValue.getValue());
	}

	@Override
	public void visit(LongValue longValue) {
		this.stack.push(longValue.getValue());
	}

	@Override
	public void visit(HexValue hexValue) {
		this.stack.push(hexValue.getValue());
	}

	@Override
	public void visit(DateValue dateValue) {
		this.stack.push(dateValue.getValue());
	}

	@Override
	public void visit(TimeValue timeValue) {
		this.stack.push(timeValue.getValue());
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		this.stack.push(timestampValue.getValue());
	}

	@Override
	public void visit(Parenthesis parenthesis) {
		parenthesis.getExpression().accept(this);
	}

	@Override
	public void visit(StringValue stringValue) {
		this.stack.push(stringValue.getValue());
	}

	@Override
	public void visit(Addition addition) {
		// this.stack.push(this.stack.pop() + this.stack.pop());
		throw new NotImplementedException();
	}

	@Override
	public void visit(Division division) {
		// this.stack.push(this.stack.pop() / this.stack.pop());
		throw new NotImplementedException();
	}

	@Override
	public void visit(Multiplication multiplication) {
		// this.stack.push(this.stack.pop() * this.stack.pop());
		throw new NotImplementedException();
	}

	@Override
	public void visit(Subtraction subtraction) {
		// this.stack.push(this.stack.pop() - this.stack.pop());
		throw new NotImplementedException();
	}

	@Override
	public void visit(AndExpression andExpression) {
		binaryExpression(andExpression, Ops.AND);
	}

	@Override
	public void visit(OrExpression orExpression) {
		binaryExpression(orExpression, Ops.OR);
	}

	@Override
	public void visit(Between between) {
		// binaryExpression(between, Ops.BETWEEN);
		throw new NotImplementedException();
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		binaryExpression(equalsTo, Ops.EQ);
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		binaryExpression(greaterThan, Ops.GT);
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		binaryExpression(greaterThanEquals, Ops.GOE);
	}

	@Override
	public void visit(InExpression inExpression) {
		inExpression(inExpression);
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {
		// comparisonExpression(isNullExpression, Ops.IS_NULL);
		throw new NotImplementedException();
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		binaryExpression(likeExpression, Ops.LIKE);
	}

	@Override
	public void visit(MinorThan minorThan) {
		binaryExpression(minorThan, Ops.LT);
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		binaryExpression(minorThanEquals, Ops.LOE);
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		binaryExpression(notEqualsTo, Ops.NE);
	}

	@Override
	public void visit(Column tableColumn) {
		this.stack.push(tableColumn);
	}

	@Override
	public void visit(SubSelect subSelect) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(CaseExpression caseExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(WhenClause whenClause) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		throw new NotImplementedException();
	}

	/*
	 * @Override public void visit(AllComparisonExpression allComparisonExpression) {
	 * throw new NotImplementedException(); }
	 */

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(Concat concat) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(Matches matches) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(CastExpression cast) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(Modulo modulo) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(AnalyticExpression aexpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(ExtractExpression eexpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(IntervalExpression iexpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(OracleHierarchicalExpression oexpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(RegExpMatchOperator rexpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(JsonExpression jsonExpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(JsonOperator jsonExpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(RegExpMySQLOperator regExpMySQLOperator) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(UserVariable var) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(NumericBind bind) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(KeepExpression aexpr) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(MySQLGroupConcat groupConcat) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(ValueListExpression valueList) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(RowConstructor rowConstructor) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(OracleHint hint) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(TimeKeyExpression timeKeyExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(DateTimeLiteralExpression literal) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(NotExpression aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(ExpressionList expressionList) {
		List<String> expressions = new ArrayList<>();
		expressionList.getExpressions().forEach(expression -> expressions.add(((StringValue) expression).getValue()));
		this.stack.push(expressions);
	}

	@Override
	public void visit(MultiExpressionList multiExprList) {
		throw new NotImplementedException();
	}

	public Predicate getPredicate() {
		BooleanExpression booleanExpression = (BooleanExpression) this.stack.pop();
		this.stack = new Stack<>();
		return booleanExpression;
	}

	private void binaryExpression(BinaryExpression binaryExpression, Ops ops) {
		binaryExpression.getLeftExpression().accept(this);
		binaryExpression.getRightExpression().accept(this);
		// this.stack.push(Expressions.enumOperation(null, ops, getExpressions()));
		this.stack.push(Expressions.predicate(ops, this.getExpressions()));
	}

	private void inExpression(InExpression inExpression) {
		inExpression.getLeftExpression().accept(this);
		inExpression.getRightItemsList().accept(this);
		this.stack.push(Expressions.predicate(Ops.IN, this.getExpressions()));
	}

	private Expression<?>[] getExpressions() {
		Object right = this.stack.pop();
		Object left = this.stack.pop();
		Expression<?>[] expressions = new Expression<?>[2];
		expressions[0] = this.getExpression(left);
		if (expressions[0] instanceof EnumPath) {
			expressions[1] = this.getExpression(expressions[0], right);
		}
		else if (expressions[0].getType().getName().equals("java.net.URI")) {
			expressions[1] = this.getURIExpression(right);
		}
		else {
			expressions[1] = this.getExpression(right);
		}
		return expressions;
	}

	private Expression<?> getURIExpression(Object right) {
		URI uri = URI.create((String) right);
		return Expressions.asComparable(uri);
	}

	private Expression<?> getExpression(Expression<?> left, Object right) {
		String name = ((Column) right).getColumnName();
		Enum num = null;
		Class type = left.getType();
		Object[] enumConstants = type.getEnumConstants();
		for (Object enumConstant : enumConstants) {
			Enum currentEnum = (Enum) enumConstant;
			if (currentEnum.name().equals(name)) {
				num = currentEnum;
				break;
			}
		}
		return Expressions.asEnum(num);
	}

	private Expression<?> getExpression(Object param) {
		if (param instanceof Expression<?>) {
			return (Expression<?>) param;
		}
		else if (param instanceof Column) {
			return QuerydslUtils.createPath(this.rootPath, ((Column) param).getFullyQualifiedName());
		}
		else {
			return Expressions.constant(param);
		}
	}

	@Override
	public void visit(IntegerDivision division) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(FullTextSearch fullTextSearch) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(IsBooleanExpression isBooleanExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(NextValExpression aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(CollateExpression aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(SimilarToExpression aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(ArrayExpression aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(NamedExpressionList namedExpressionList) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(VariableAssignment aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(XMLSerializeExpr aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(XorExpression orExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(TryCastExpression cast) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(RowGetExpression rowGetExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(ArrayConstructor aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(TimezoneExpression aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(JsonAggregateFunction aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(JsonFunction aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(ConnectByRootOperator aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(OracleNamedFunctionParameter aThis) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(AllColumns allColumns) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(AllValue allValue) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(IsDistinctExpression isDistinctExpression) {
		throw new NotImplementedException();
	}

	@Override
	public void visit(GeometryDistance geometryDistance) {
		throw new NotImplementedException();
	}

}
