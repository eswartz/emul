/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.GenerateAST;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstAttributes;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstPointerType;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstRedefinition;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstStmtScope;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.test.BaseTest;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLInstanceField;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.LLStaticField;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstDataType extends AstStmtScope implements IAstDataType {

	private IAstNodeList<IAstTypedNode> statics;
	private IAstNodeList<IAstTypedNode> ifields;
	private List<IAstRedefinition> redefs;
	private ISymbol typeName;
	private IAstCodeExpr initCode;
	private ISymbol initName;

		
	public AstDataType( TypeEngine typeEngine, ISymbol typeName,
			IAstNodeList<IAstStmt> stmts,
			IAstNodeList<IAstTypedNode> fields,
			IAstNodeList<IAstTypedNode> statics, 
			List<IAstRedefinition> redefs, 
			IScope scope) {
		super(stmts, scope);
		this.typeName = typeName;
		setFields(fields);
		setStatics(statics);
		this.redefs = redefs;
		
		// TODO: some assumption about whether type will exist here from callers
		if (typeName != null)
			setType(createDataType(typeEngine));
	}
	protected AstDataType( LLType type, 
			ISymbol typeName,
			IAstNodeList<IAstStmt> stmts,
			IAstNodeList<IAstTypedNode> fields,
			IAstNodeList<IAstTypedNode> statics, 
			List<IAstRedefinition> redefs, 
			IScope scope) {
		super(stmts, scope);
		this.typeName = typeName;
		setFields(fields);
		setStatics(statics);
		this.redefs = redefs;
		setType(type);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstDataType copy() {
		return (IAstDataType) fixupStmtScope(new AstDataType(
				type,
				typeName,
				doCopy(stmtList),
				doCopy(ifields), doCopy(statics), redefs,
				getScope().newInstance(getCopyScope())));
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ifields == null) ? 0 : ifields.hashCode());
		result = prime * result + ((statics == null) ? 0 : statics.hashCode());
		result = prime * result + ((redefs == null) ? 0 : redefs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstDataType other = (AstDataType) obj;
		if (ifields == null) {
			if (other.ifields != null)
				return false;
		} else if (!ifields.equals(other.ifields))
			return false;
		if (statics == null) {
			if (other.statics != null)
				return false;
		} else if (!statics.equals(other.statics))
			return false;
		if (redefs == null) {
			if (other.redefs != null)
				return false;
		} else if (!redefs.equals(other.redefs))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("DATA");
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#getFields()
	 */
	@Override
	public IAstNodeList<IAstTypedNode> getFields() {
		return ifields;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#getStatics()
	 */
	@Override
	public IAstNodeList<IAstTypedNode> getStatics() {
		return statics;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#setFields(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setFields(IAstNodeList<IAstTypedNode> fields) {
		this.ifields = reparent(this.ifields, fields);
		setType(null);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#setStatics(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setStatics(IAstNodeList<IAstTypedNode> statics) {
		this.statics = reparent(this.statics, statics);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataType#redefs()
	 */
	@Override
	public List<IAstRedefinition> redefs() {
		return redefs;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { ifields, statics, stmtList };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (statics == existing) 
			setStatics((IAstNodeList<IAstTypedNode>) another);
		else if (ifields == existing)
			setFields((IAstNodeList<IAstTypedNode>) another);
		else if (stmtList == existing)
			setStmtList((IAstNodeList<IAstStmt>) another);
		else
			super.replaceChild(existing, another);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		
		boolean changed = false;
		
		if (canReplaceType(this) || !getType().isComplete()) {
			LLDataType data = createDataType(typeEngine); 
			// TODO: references to this type's name need to be updated
			changed |= updateType(this, data);
		}
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (!redefs.isEmpty())  {
			StringBuilder sb = new StringBuilder();
			for (IAstRedefinition redef : redefs)
				sb.append(sb.length() == 0 ? ' ' : ',').append(redef);
			throw new TypeException(redefs.get(0), "type declares redefinitions of names that don't exist in target scope:" + sb);
		}
		
		// XXX codeptr
		for (IAstTypedNode node : ifields.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					LLType type = symbolExpr.getType();
					if (type instanceof LLCodeType)
						throw new TypeException(node, "cannot directly embed code in field");
				}
			}
		}
		
		for (IAstTypedNode node : statics.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					LLType type = symbolExpr.getType();
					if (type instanceof LLCodeType)
						throw new TypeException(node, "cannot directly embed code in field");
				}
			}
		}
	}

	public LLDataType createDataType(TypeEngine typeEngine) {
		List<LLInstanceField> newIFields = new ArrayList<LLInstanceField>();
		
		LLType[] existing = type != null ? ((LLDataType) type).getTypes() : null;
		int idx = 0;
		for (IAstTypedNode node : ifields.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					IAstTypedExpr defaul = alloc.getDefaultFor(i);
					LLType type = symbolExpr.getType();
					if (type == null && existing != null)
						type = existing[idx]; 
					LLInstanceField field = new LLInstanceField(symbolExpr.getSymbol().getName(), type, 
							symbolExpr, defaul, new HashSet<String>(alloc.getAttrs()));
					newIFields.add(field);
					idx++;
				}
			}
		}
		
		List<LLStaticField> newSFields = new ArrayList<LLStaticField>();
		for (IAstTypedNode node : statics.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
					IAstTypedExpr defaul = alloc.getDefaultFor(i);
					LLType type = symbolExpr.getType();
					if (type == null && existing != null)
						type = existing[idx]; 
					String name = symbolExpr.getSymbol().getName();
					LLStaticField field = new LLStaticField(name, type, 
							symbolExpr.getSymbol(),
							symbolExpr, defaul,
							new HashSet<String>(alloc.getAttrs()));
					newSFields.add(field);
					idx++;
				}
			}
		}
		
		ISymbol sym = getTypeName();
		LLDataType data = typeEngine.getDataType(sym, newIFields, newSFields);
		
		/*data = (LLDataType) data.substitute(typeEngine, 
				new LLUpType(sym, 0, data), 
				data);
		*/
		return data;
	}

	public ISymbol getTypeName() {
		return typeName;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataType#setTypeName(org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void setTypeName(ISymbol typeName) {
		this.typeName = typeName;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		super.setType(type);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstStmtScope#merge(org.ejs.eulang.ast.IAstStmtScope)
	 */
	@Override
	public void merge(IAstStmtScope added, TypeEngine typeEngine) throws ASTException {
		
		if (added instanceof IAstDataType) {
			IAstDataType addedData = (IAstDataType) added;

			setTypeName(addedData.getTypeName());
			
			// merge any redefs
			for (IAstRedefinition redef : addedData.redefs()) {
				// update any pending redefs
				boolean matched = false;
				IAstTypedExpr body = (IAstTypedExpr) redef.getExpr().copy();
				
				for (IAstRedefinition exredef : redefs) {
					if (exredef.getSymbol().equals(redef.getSymbol())) {
						// update
						exredef.setExpr(body);
						matched = true;
						break;
					}
				}
				if (matched)
					continue;
				
				// else update actual match
				ISymbol sym = getScope().get(redef.getSymbol());
				if (sym == null)
					throw new ASTException(redef, "unknown symbol " + redef + " marked as overriding definition");
				
				if (sym.getDefinition() instanceof IAstDefineStmt) {
					((IAstDefineStmt) sym.getDefinition()).bodyList().clear();
					((IAstDefineStmt) sym.getDefinition()).bodyList().add(body);
				}
				else if (sym.getDefinition() instanceof IAstAllocStmt) {
					IAstAllocStmt alloc = (IAstAllocStmt) sym.getDefinition();
					IAstNodeList<IAstSymbolExpr> allocSyms = alloc.getSymbolExprs();
					int pos = 0;
					IAstSymbolExpr theSym = null;
					while (pos < allocSyms.nodeCount()) {
						theSym = allocSyms.list().get(pos);
						if (theSym.getSymbol().getName().equals(redef.getSymbol())) {
							break;
						}
						pos++;
					}
					assert pos < allocSyms.nodeCount();


					IAstNodeList<IAstTypedExpr> allocExprs = alloc.getExprs();
					
					IAstTypedExpr existing = null;
					if (allocExprs.nodeCount() > 0)
						existing = allocExprs.list().get(pos);

					if (body instanceof IAstCodeExpr) {
						/*
						if (alloc.getTypeExpr() instanceof IAstPointerType && ((IAstPointerType) alloc.getTypeExpr()).getBaseType() instanceof IAstPrototype) {
							IAstPrototype newProto = (IAstPrototype) ((IAstPointerType) alloc.getTypeExpr()).getBaseType().copy();
							newProto.uniquifyIds();
							((IAstCodeExpr) body).setPrototype(newProto);
						}
						*/
						/*
						else
						if (alloc.hasAttr(IAstAttributes.THIS)) {
							GenerateAST.adjustPrototypeForThisCall(typeEngine, 
									((IAstCodeExpr) body).getPrototype(), getScope(), ((IAstScope) body).getScope());
							if (existing != null && existing instanceof IAstCodeExpr) {
								GenerateAST.adjustPrototypeForThisCall(typeEngine, 
									((IAstCodeExpr) existing).getPrototype(), getScope(), ((IAstScope) existing).getScope());
								existing.setType(null);
							}
							
							// destroy the types so we re-discover them (TODO: cleanup)
							body.setType(null);
							theSym.setType(null);
							theSym.getSymbol().setType(null);
							sym.setType(null);
							
							if (alloc.getSymbolExprs().nodeCount() == 1) {
								alloc.setType(null);
								if (alloc.getTypeExpr() != null)
									alloc.getTypeExpr().setType(null);
							}
						}
						*/
					}
					
					
					// TODO: split allocs into distinct entries to avoid this
					if (allocExprs.nodeCount() != 0 && allocExprs.nodeCount() != allocSyms.nodeCount())
						throw new ASTException(redef, "cannot override the value of " + redef + " currently: split the multi-alloc into pieces");
					
					if (allocExprs.nodeCount() == 0)
						allocExprs.add(body);
					else {
						existing = allocExprs.list().get(pos);
						allocExprs.replaceChild(existing, body);
					}
				}
				else
					assert false;
			}
			
			doMerge(ifields, addedData.getFields());
			doMerge(statics, addedData.getStatics());
		} 
		setType(null);
		super.merge(added, typeEngine);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataType#needsExplicitInit()
	 */
	@Override
	public boolean needsExplicitInit() {
		for (IAstTypedNode node : ifields.list()) {
			if (node instanceof IAstAllocStmt) {
				IAstAllocStmt alloc = (IAstAllocStmt) node;
				for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
					IAstTypedExpr defaul = alloc.getDefaultFor(i);
					if (defaul == null)
						continue;
					
					if (defaul instanceof IAstLitExpr) {
						if (((IAstLitExpr) defaul).isZero())
							continue;
					}
					
					return true;
				}
			}
		}
		
		for (IAstStmt stmt : stmts().list()) {
			if (!(stmt instanceof IAstDefineStmt))
				return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataType#getInitCode()
	 */
	@Override
	public IAstCodeExpr getInitCode(TypeEngine typeEngine) {
		if (initCode == null) {
			ISymbol initSym = getInitName(typeEngine);
				
			IScope initScope = new LocalScope(getTypeName().getScope());
			IAstPrototype initProto = new AstPrototype((LLCodeType) initSym.getType(), initScope, new String[] { "this" });
			Set<String> attrs = new HashSet<String>();
			attrs.add(IAstAttributes.THIS);
			IAstNodeList<IAstStmt> stmts = new AstNodeList<IAstStmt>(IAstStmt.class);

			IAstTypedExpr thisExpr = new AstDerefExpr(new AstSymbolExpr(false, initScope.get("this")), true);
			
			// add field decls
			IAstNodeList<IAstTypedExpr> fields = new AstNodeList<IAstTypedExpr>(IAstTypedExpr.class);
			IAstNodeList<IAstTypedExpr> exprs = new AstNodeList<IAstTypedExpr>(IAstTypedExpr.class);
			
			for (IAstTypedNode node : ifields.list()) {
				if (node instanceof IAstAllocStmt) {
					IAstAllocStmt alloc = (IAstAllocStmt) node;
					for (int i = 0; i < alloc.getSymbolExprs().nodeCount(); i++) {
						IAstSymbolExpr symbolExpr = alloc.getSymbolExprs().list().get(i);
						IAstTypedExpr defaul = alloc.getDefaultFor(i);
						if (defaul == null) {
							defaul = new AstNilLitExpr("0", symbolExpr.getType());
						}
						fields.add(new AstFieldExpr((IAstTypedExpr) thisExpr.copy(), new AstName(symbolExpr.getSymbol().getName())));
						exprs.add((IAstTypedExpr) defaul.copy());
					}
				}
			}
			
			if (fields.nodeCount() > 0) {
				IAstAssignStmt assn = new AstAssignStmt(IOperation.MOV, fields, exprs, false);
				stmts.add(assn);
			}
			
			// run init code
			
			for (IAstStmt stmt : stmts().list()) {
				if (!(stmt instanceof IAstDefineStmt))
					stmts.add((IAstStmt) stmt.copy());
			}
			
			initCode = new AstCodeExpr(initProto, initScope, stmts, attrs);
			initCode.setSourceRefTree(getSourceRef());
			initCode.uniquifyIds();
		}
		return initCode;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataType#getInitName()
	 */
	@Override
	public ISymbol getInitName(TypeEngine typeEngine) {
		if (initName == null) {
			if (typeName != null) {
				initName = getTypeName().getScope().add(getTypeName().getUniqueName() + "$init", false);
				LLPointerType thisPtrType = typeEngine.getPointerType(getType());
				LLCodeType dataInitFuncType = typeEngine.getCodeType(typeEngine.VOID, new LLType[] { thisPtrType });
				initName.setType(dataInitFuncType);
			}
		}
		return initName;
	}
}
