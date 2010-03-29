/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.ast.impl.ArithmeticBinaryOperation;
import org.ejs.eulang.ast.impl.ArithmeticUnaryOperation;
import org.ejs.eulang.ast.impl.ComparisonOperation;
import org.ejs.eulang.ast.impl.ComparisonUnaryOperation;
import org.ejs.eulang.ast.impl.LogicalBinaryOperation;
import org.ejs.eulang.ast.impl.LogicalUnaryOperation;
import org.ejs.eulang.ast.impl.ShiftOperation;

/**
 * @author ejs
 *
 */
public interface IOperation {
	IUnaryOperation NEG = new ArithmeticUnaryOperation("-");
	IBinaryOperation ADD = new ArithmeticBinaryOperation("+", true);
	IBinaryOperation SUB = new ArithmeticBinaryOperation("-", false);
	IBinaryOperation MUL = new ArithmeticBinaryOperation("*", true);
	IBinaryOperation DIV = new ArithmeticBinaryOperation("/", false);
	IBinaryOperation MOD = new ArithmeticBinaryOperation("%", false);
	IBinaryOperation UDIV = new ArithmeticBinaryOperation("\\", false);
	IBinaryOperation UMOD = new ArithmeticBinaryOperation("%%", false);
	
	IBinaryOperation SHL = new ShiftOperation("<<");
	IBinaryOperation SHR = new ShiftOperation(">>>");
	IBinaryOperation SAR = new ShiftOperation(">>");
	
	IUnaryOperation INV = new LogicalUnaryOperation("~");
	IBinaryOperation BITOR = new LogicalBinaryOperation("|", true);
	IBinaryOperation BITAND = new LogicalBinaryOperation("&", true);
	IBinaryOperation BITXOR = new LogicalBinaryOperation("^", true);
	
	IUnaryOperation NOT = new ComparisonUnaryOperation("not");
	IBinaryOperation COMPAND = new ComparisonOperation("&&", true);
	IBinaryOperation COMPOR = new ComparisonOperation("||", true);
	IBinaryOperation COMPEQ = new ComparisonOperation("==", true);
	IBinaryOperation COMPNE = new ComparisonOperation("!=", true);
	IBinaryOperation COMPGT = new ComparisonOperation(">", false);
	IBinaryOperation COMPLT = new ComparisonOperation("<", false);
	IBinaryOperation COMPLE = new ComparisonOperation("<=", false);
	IBinaryOperation COMPGE = new ComparisonOperation(">=", false);
	
	IUnaryOperation CAST = new CastOperation("<cast>");
	
	String getName();
	boolean isCommutative();
	
}
