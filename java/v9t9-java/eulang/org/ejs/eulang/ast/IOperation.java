/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IOperation {
	IOperation ADD = new ArithmeticOperation("+", true);
	IOperation SUB = new ArithmeticOperation("-", false);
	IOperation MUL = new ArithmeticOperation("*", true);
	IOperation DIV = new ArithmeticOperation("/", false);
	IOperation MOD = new ArithmeticOperation("%", false);
	IOperation UDIV = new ArithmeticOperation("\\", false);
	IOperation UMOD = new ArithmeticOperation("%%", false);
	IOperation SHL = new ArithmeticOperation("<<", false);
	IOperation SHR = new ArithmeticOperation(">>>", false);
	IOperation SAR = new ArithmeticOperation(">>", false);
	IOperation BITOR = new ArithmeticOperation("|", true);
	IOperation BITAND = new ArithmeticOperation("&", true);
	IOperation BITXOR = new ArithmeticOperation("^", true);
	IOperation COMPAND = new ArithmeticOperation("&&", true);
	IOperation COMPOR = new ArithmeticOperation("||", true);
	IOperation COMPEQ = new ArithmeticOperation("==", true);
	IOperation COMPNE = new ArithmeticOperation("!=", true);
	IOperation COMPGT = new ArithmeticOperation(">", false);
	IOperation COMPLT = new ArithmeticOperation(">", false);
	IOperation COMPLE = new ArithmeticOperation(">=", false);
	IOperation COMPGE = new ArithmeticOperation("<=", false);
	
	String getName();
	boolean isCommutative();
}
