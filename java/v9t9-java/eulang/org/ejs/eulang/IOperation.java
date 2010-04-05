/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.ast.impl.ArithmeticBinaryOperation;
import org.ejs.eulang.ast.impl.ArithmeticUnaryOperation;
import org.ejs.eulang.ast.impl.ComparisonOperation;
import org.ejs.eulang.ast.impl.ComparisonUnaryOperation;
import org.ejs.eulang.ast.impl.IndirectOperation;
import org.ejs.eulang.ast.impl.LogicalBinaryOperation;
import org.ejs.eulang.ast.impl.LogicalUnaryOperation;
import org.ejs.eulang.ast.impl.ShiftOperation;

/**
 * @author ejs
 *
 */
public interface IOperation {
	//IUnaryOperation INDIRECT = new IndirectOperation("INDIRECT");
	
	IUnaryOperation NEG = new ArithmeticUnaryOperation("-");
	IBinaryOperation ADD = new ArithmeticBinaryOperation("+", "add", true);
	IBinaryOperation SUB = new ArithmeticBinaryOperation("-", "sub", false);
	IBinaryOperation MUL = new ArithmeticBinaryOperation("*", "mul", true);
	IBinaryOperation DIV = new ArithmeticBinaryOperation("/", "sdiv", false);
	IBinaryOperation MOD = new ArithmeticBinaryOperation("%", "srem", false);
	IBinaryOperation UDIV = new ArithmeticBinaryOperation("\\", "udiv", false);
	IBinaryOperation UMOD = new ArithmeticBinaryOperation("%%", "urem", false);
	
	IBinaryOperation SHL = new ShiftOperation("<<", "shl");
	IBinaryOperation SHR = new ShiftOperation(">>>", "lshr");
	IBinaryOperation SAR = new ShiftOperation(">>", "ashr");
	
	IUnaryOperation INV = new LogicalUnaryOperation("~");
	IBinaryOperation BITOR = new LogicalBinaryOperation("|", "or", true);
	IBinaryOperation BITAND = new LogicalBinaryOperation("&", "and", true);
	IBinaryOperation BITXOR = new LogicalBinaryOperation("^", "xor", true);
	
	IUnaryOperation NOT = new ComparisonUnaryOperation("not");
	IBinaryOperation COMPAND = new ComparisonOperation("and", null, true, "", "");
	IBinaryOperation COMPOR = new ComparisonOperation("or", null, true, "", "");
	IBinaryOperation COMPEQ = new ComparisonOperation("==", "eq", true, "", "o");
	IBinaryOperation COMPNE = new ComparisonOperation("!=", "ne", true, "", "o");
	IBinaryOperation COMPGT = new ComparisonOperation(">", "gt", false, "s", "o");
	IBinaryOperation COMPLT = new ComparisonOperation("<", "lt", false, "s", "o");
	IBinaryOperation COMPLE = new ComparisonOperation("<=", "le", false, "s", "o");
	IBinaryOperation COMPGE = new ComparisonOperation(">=", "ge", false, "s", "o");
	
	IUnaryOperation CAST = new CastOperation("<cast>");
	
	String getName();
	String getLLVMName();
	boolean isCommutative();
	
}
