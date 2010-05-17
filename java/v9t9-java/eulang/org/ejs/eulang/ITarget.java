/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.llvm.FunctionConvention;
import org.ejs.eulang.llvm.ILLCodeTarget;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * The compilation target
 * @author ejs
 *
 */
public interface ITarget {
	enum Intrinsic {
		/** signed division */
		SIGNED_DIVISION,
		/** signed remainder */
		SIGNED_REMAINDER,
		/** modulo */
		MODULO,
		/** shift right circular */
		SHIFT_RIGHT_CIRCULAR,
		/** shift left circular */
		SHIFT_LEFT_CIRCULAR,
		/** increment reference */
		INCREF,
		/** decrement reference */
		DECREF,
	};
	TypeEngine getTypeEngine();
	
	/** e.g. "ccc", "fastcc", "cc &lt;n&gt;" */
	String getLLCallingConvention();

	/**
	 * Get the GNU-style target triple
	 * @return e.g. "foo-bar-baz"
	 */
	String getTriple();
	
	/**
	 * Increment a reference to a ref-counted object with the given id (may be 0)
	 * @param target 
	 * @param valueType 
	 * @param value
	 */
	void incRef(ILLCodeTarget target, LLType valueType, LLOperand value);

	/**
	 * Decrement a reference to a ref-counted object with the given id (may be  0)
	 * @param target 
	 * @param valueType 
	 * @param value
	 */
	void decRef(ILLCodeTarget target, LLType valueType, LLOperand value);

	/** Get all the supported register classes, in preference order */
	IRegClass[] getRegisterClasses();
	
	/** Get the calling convention that applies to this code */
	ICallingConvention getCallingConvention(FunctionConvention convention);

	/**
	 * Get an intrinsic function
	 * @param type 
	 * @param string
	 * @return symbol
	 */
	ISymbol getIntrinsic(ILLCodeTarget target, Intrinsic intrinsic, LLType type);
	
	int getSP();
}
