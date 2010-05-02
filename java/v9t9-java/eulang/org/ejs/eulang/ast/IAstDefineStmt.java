/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLInstanceType;
import org.ejs.eulang.types.LLType;


/**
 * This node defines a name to a node.  This means a variant of the node may be 
 * substituted for the name.
 * @author ejs
 *
 */
public interface IAstDefineStmt extends IAstScope, IAstStmt, IAstSymbolDefiner {
	IAstDefineStmt copy(IAstNode copyParent);
	
	IAstSymbolExpr getSymbolExpr();
	void setSymbolExpr(IAstSymbolExpr id);
	
	boolean isGeneric();
	void setGeneric(boolean generic);
	
	ISymbol getSymbol();
	
	/**
	 * Get the abstract variants of the definition body.  There is one for each instance
	 * defined in source code (e.g. explicit definitions, list comprehensions, etc).
	 * The types of each may be incomplete or generic until type inference, after which
	 * they will be generic or complete.
	 */
	List<IAstTypedExpr> bodyList();

	/**
	 * Get the generic arguments for this define, or <code>null</code> if not generic
	 */
	ISymbol[] getGenericVariables();
	
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
	 * Find or create an expansion for a given body type and supplied instance parameters,
	 * used for expanding an IAstInstanceExpr. 
	 * @param bodyType the generic type of a body from #bodyList or #getMatchingBodyExpr
	 * @param instanceParams parameters supplied in IAstInstanceExpr
	 * @return a symbol referencing the expansion produced
	 * @throws ASTException if expansion is illegal (bad instance params)
	 * @throws IllegalArgumentException for non-generic or non-matching bodyType
	 */
	ISymbol getInstanceForParameters(TypeEngine typeEngine, LLType bodyType, List<IAstTypedExpr> instanceParams) throws ASTException;

	/**
	 * Get the complete or generic type to definition body map.  This is known only
	 * after type inference.
	 */
	//Map<LLType, IAstTypedExpr> typedBodyMap();
	
	/**
	 * Get the instances of the expressions from {@link #bodyList()} for each concrete
	 * type.  This mapping only makes sense with generic or complete types.
	 * @return read-only map of body types to the instances recorded for them.  If the body type
	 * is not generic, there is only one entry in the list, and it cannot be modified.
	 */
	Map<LLType, List<ISymbol>> bodyToInstanceMap();
	
	/**
	 * Get the symbol for a instantiated version of the definition used for this type.
	 * The type is a key to locate the unique bodyList element.
	 * <p>
	 * This returns the concrete instance registered via {@link #getInstanceForParameters(TypeEngine, LLType, List)} or
	 * {@link #registerInstance(IAstTypedExpr, IAstTypedExpr)}.
	 * @param bodyType the type of an expr for which the instance is searched
	 * @param instanceType the type of a target expr for which the instance is searched
	 * @return the symbol for the expanded and type-specific body of the define, whose definition is the body, or <code>null</code>
	 * @throws IllegalArgumentException if the bodyType is not generic
	 */
	ISymbol getMatchingInstance(LLType bodyType, LLType instanceType);

	
	/**
	 * Add a generated instance for the given generic body type.  
	 * <p>
	 * @param bodyType the type of the generic expr for which the instance was made
	 * @param expansion expanded and type-specific body of the define
	 * @throws IllegalArgumentException if the bodyType is not generic or the symbol does not have a body
	 */
	ISymbol registerInstance(IAstTypedExpr body, IAstTypedExpr expansion);
	
	/**
	 * Get all concrete instances.  These are the non-generic body expressions
	 * and the generated instances.
	 */
	Collection<IAstTypedExpr> getConcreteInstances();

	Map<LLInstanceType, ISymbol> getInstanceMap(TypeEngine typeEngine, LLType bodyType);

	
}
