/**
 * 
 */
package v9t9.forthcomp;

import v9t9.forthcomp.ISemantics;

/**
 * This represents a word and its semantics, whether on the host or target
 * side.
 * 
 * A word is a block of code and/or data with a name and with interpret,
 * compilation, and runtime semantics.
 *
 * Forth "immediate" words are those for which the compilation semantics are
 * the interpretation semantics.
 *  
 * A word can have both host and target side behaviors, which may be different.
 * Some words may have Java code associated with them, or may be proxies
 * for a word interpreter which iterates references to other words and invokes
 * their semantics in a certain context.
 * 
 * A word may be defined either from the host or target (e.g. parsed file)
 * and may have either normal semantics -- e.g. a target-side definition
 * and a host-side proxy -- or may only live in the host at compile time.
 * 
 * A word may be defined to be a target-only definition, which does not
 * affect the host-side semantics.  This allows for a self-booting 
 * environment without the need to resort to assembly or duplicated,
 * less-powerful variants of words as bootstrap code.
 * 
 * A word may have a dictionary entry, used on the target side.  This entry
 * tells whether the word will be accessible from the target in the
 * target variant of the environment.  
 * 
 * <p>
 * 
 * When a word, originally defined with host semantics, is defined on the
 * target side, the host semantics may either become the target semantics
 * (with the behavior emulated) or the host semantics may be replaced 
 * with semantics that augment that of the target, or the host semantics
 * may continue to apply.
 * 
 * For example, "," will compile a cell into the dictionary.  When only the
 * host version exists, then the DP in the TargetContext is modified.
 * Once the target gets a definition of ",", the host version is still used
 * since the behavior is the same. 
 * 
 * ":" will create a colon word.  The host semantics require creating a
 * new IWord, allocating a DictEntry, and parsing the host-side input
 * stream.  When the target version is defined, it will not be executed. 
 * 
 * <p>
 * 
 * Classes:
 * 
 * <li>Primitive word:  this contains target-side opcodes, intended for
 * literal inlining into the target.  The host must supply corresponding
 * Java code if the word can execute at compile time.
 * 
 * <li>Compiler support word: this contains host-side Java code only and
 * has no target-side definition (yet).  This must be an immediate word
 * for use in compiling code blocks or an interpret-only word.
 * 
 * <li>Colon word:  this is a typical code definition, which has
 * references to constituent words.  Such words may be invoked from the
 * host as long as there is a host-side variant.  This is discovered
 * dynamically at the time of such an invocation.
 * 
 * <li> Literal word:  this is pure data (char, cell, double, string)
 * which has a host and target side representation (potentially different).
 * There is not usually a dictionary entry for such words -- unless they
 * are defined to be constants at runtime.
 * 
 * <p>
 *
 * Words have "tokens" which are compiled into code blocks, pushed onto
 * stacks, and used to navigate the dictionary.  Thus on the target side
 * they are usually cell-sized integers.  On the host side, these are
 * Java object references.
 *  
 * <p>
 * 
 * 
 * 
 * @author ejs
 *
 */
public interface IWord {

	String getName();

	/**
	 * If defined, these semantics are used when encountering the word in
	 * interpret mode.  For typical words, the word is executed or the literal value is
	 * pushed on the stack, but parsing words may consume input and modify the
	 * dictionary, for example.
	 * 
	 * @return ISemantics or null
	 */
	ISemantics getInterpretationSemantics();
	void setInterpretationSemantics(ISemantics semantics);
	/** 
	 * If defined, these semantics are used when encountering the word
	 * in compile mode.  Typically the xt of a word is compiled into a code
	 * block, or one or more words (for literals) are compiled into the
	 * block.  But some words may execute to form control structures
	 * (e.g. IF, DO, ...) -- the Forth "immediate".
	 *  
	 * @return ISemantics or null
	 */
	ISemantics getCompilationSemantics();
	void setCompilationSemantics(ISemantics semantics);
	/** 
	 * If defined, these semantics are used when encountering the word
	 * at runtime.  This is typically used only by the host side
	 * when executing words while compiling.
	 * @return ISemantics or null
	 */
	ISemantics getExecutionSemantics();
	void setExecutionSemantics(ISemantics semantics);

}
