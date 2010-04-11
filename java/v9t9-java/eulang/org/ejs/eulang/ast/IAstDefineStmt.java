/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;


/**
 * This node defines a name to a node.  This means a variant of the node may be 
 * substituted for the name.
 * @author ejs
 *
 */
public interface IAstDefineStmt extends IAstStmt, IAstSymbolDefiner {
	IAstDefineStmt copy(IAstNode copyParent);
	
	IAstSymbolExpr getSymbolExpr();
	void setSymbolExpr(IAstSymbolExpr id);
	
	ISymbol getSymbol();
	
	/** @deprecated */
	IAstTypedExpr getExpr();
	/** @deprecated */
	void setExpr(IAstTypedExpr expr);
	
	/**
	 * Get the abstract variants of the definition body.  There is one for each instance
	 * defined in source code (e.g. explicit definitions, list comprehensions, etc).
	 * The types of each may be incomplete or generic until type inference, after which
	 * they will be generic or complete.
	 */
	List<IAstTypedExpr> bodyList();
	
	/**
	 * Get the complete or generic type to definition body map.  This is known only
	 * after type inference.
	 */
	//Map<LLType, IAstTypedExpr> typedBodyMap();
	
	
	/**
	 * Get the body of the definition matching the given type of a symbol
	 * referencing this definition.  
	 * <p>
	 * This returns a possibly generic or untyped AST from the choices available in the definition
	 * ({@link #bodyList()}) until inference (at which point all types are complete or generic).
	 * <p>
	 * In general, type matching tries to match exact types first, then compatible types,
	 * then generic types, then unknown types.
	 * 
	 * @param type the expr type to match, or <code>null</code> for the first one
	 * @return body of definition or <code>null</code>.  If not null, the type of the
	 * body is a key for {@link #bodyToInstanceMap()}, {@link #getMatchingInstance(LLType)} 
	 * and {@link #registerInstance(LLType, IAstTypedExpr)}.
	 */
	IAstTypedExpr getMatchingBodyExpr(LLType type);
	
	/**
	 * Get the instances of the expressions from {@link #bodyList()} for each concrete
	 * type.  This mapping only makes sense with generic or complete types.
	 * @return read-only map of body types to the instances recorded for them.  If the body type
	 * is not generic, there is only one entry in the list, and it cannot be modified.
	 */
	Map<LLType, List<IAstTypedExpr>> bodyToInstanceMap();
	
	/**
	 * Get a generated matching resolved version of the definition used for this type.
	 * The type is a key to locate the unique bodyList element.
	 * <p>
	 * For a non-generic body type, the returns the body expression; otherwise,
	 * this returns the concrete instance registered via {@link #registerInstance(LLType, IAstTypedExpr)}.
	 * @param bodyType the type of an expr for which the instance is searched
	 * @param instanceType the type of a target expr for which the instance is searched
	 * @return expanded and type-specific body of the define, or <code>null</code>
	 */
	IAstTypedExpr getMatchingInstance(LLType bodyType, LLType instanceType);

	
	/**
	 * Add an instance for the given generic body type.  
	 * <p>
	 * @param bodyType the type of the matching expr for which the instance was made,
	 * not generic
	 * @param expr expanded and type-specific body of the define
	 * @throws IllegalArgumentException if the bodyType is not generic
	 */
	void registerInstance(LLType bodyType, IAstTypedExpr expr);
	
	/**
	 * Get all concrete instances.
	 */
	Collection<IAstTypedExpr> getConcreteInstances();
}
