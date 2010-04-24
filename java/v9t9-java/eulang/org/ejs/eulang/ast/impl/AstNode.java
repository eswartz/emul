/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.HashMap;
import java.util.Map;

import org.ejs.eulang.ISourceRef;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.AstVisitor;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author eswartz
 *
 */
abstract public class AstNode implements IAstNode {
	private static int gId;
	private int id = ++gId;
	
    private IAstNode parent;

    protected boolean dirty;

	private ISourceRef sourceRef;
    
    public AstNode() {
    }

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#getId()
     */
    @Override
    public int getId() {
    	return id;
    }
    public abstract boolean equals(Object obj);
    public abstract int hashCode();
    
    @Override
	public String toString() {
        String name = getClass().getName();
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
			name = name.substring(idx+1);
		}
        return "{ " + name + ":" +hashCode() + " }"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#getDumpChildren()
     */
    @Override
    public IAstNode[] getDumpChildren() {
    	return getChildren();
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#getReferencedNodes()
     */
    @Override
    public IAstNode[] getReferencedNodes() {
    	return getChildren();
    }
    
	protected String catenate(IAstNode[] nodes) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (IAstNode k : nodes) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(k);
		}
		return sb.toString();
	}

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#isDirty()
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#isDirtyTree()
     */
    public boolean isDirtyTree() {
        if (dirty) {
			return true;
		}
        IAstNode children[] = getChildren();
        for (IAstNode element : children) {
            if (element.isDirtyTree()) {
				return true;
			}
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#setDirty(boolean)
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        
    }
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#getParent()
     */
    public IAstNode getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#setParent(v9t9.tools.decomp.expr.IAstNode)
     */
    public void setParent(IAstNode node) {
        if (node != null && node != parent) {
			org.ejs.coffee.core.utils.Check.checkArg((parent == null));
		}
        parent = node;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#accept(v9t9.tools.decomp.expr.AstVisitor)
     */
    public int accept(AstVisitor visitor) {
        int ret = visitor.visit(this);
        if (ret == AstVisitor.PROCESS_ABORT)
        	return ret;
        if (ret == AstVisitor.PROCESS_CONTINUE) {
        	visitor.visitChildren(this);
        	try {
	        	ret = visitor.traverseChildren(this);
	        	if (ret == AstVisitor.PROCESS_ABORT)
	            	return ret;
        	} finally {
        		visitor.visitEnd(this);
        	}
        }
        return ret;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstNode#acceptReference(v9t9.tools.decomp.expr.AstVisitor)
     */
    public int acceptReference(AstVisitor visitor) {
    	return AstVisitor.PROCESS_CONTINUE;
    }


    /* (non-Javadoc)
     * @see v9t9.tools.ast.expr.IAstNode#getSourceRef()
     */
    @Override
    public ISourceRef getSourceRef() {
    	return sourceRef;
    }
    /* (non-Javadoc)
     * @see v9t9.tools.ast.expr.IAstNode#setSourceRef(v9t9.tools.ast.expr.ISourceRef)
     */
    @Override
    public void setSourceRef(ISourceRef sourceRef) {
    	this.sourceRef = sourceRef;
    }
    
    protected <T extends IAstNode> T reparent(T existing, T newkid) {
    	if (existing != null && existing.getParent() == this)
			existing.setParent(null);
		if (newkid != null)
			newkid.setParent(this);
		return newkid;
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#getDepth()
     */
    @Override
    public int getDepth() {
    	IAstNode[] kids = getChildren();
    	if (kids.length > 0) {
    		int maxKid = 0;
    		for (IAstNode kid : kids) {
    			maxKid = Math.max(kid.getDepth(), maxKid);
    		}
    		return 1 + maxKid;
    	}
    	return 1;
    }

    @SuppressWarnings("unchecked")
	protected <T extends IAstNode> T doCopy(T node, IAstNode copyParent) {
    	if (node == null)
			return null;
    	T copy = (T) node.copy(copyParent);
    	return copy;
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#findMatch(org.ejs.eulang.ast.IAstNode)
     */
    @Override
    public IAstNode findMatch(IAstNode target) {
    	if (target == null)
    		return null;
    	if (target.getId() == id)
    		return this;
    	for (IAstNode kid : getChildren()) {
    		IAstNode match = kid.findMatch(target);
    		if (match != null)
    			return match;
    	}
    	return null;
    }

    protected IScope getCopyScope(IAstNode copyParent) {
    	while (copyParent != null) {
    		if (copyParent instanceof IAstScope) {
    			return ((IAstScope) copyParent).getScope();
    		}
    		copyParent = copyParent.getParent();
    	}
    	return null;
    }
    protected IScope remapScope(IScope scope, IScope copy, IAstNode copyRoot) {
    	if (scope == null) return null;
    	Map<Integer, IAstNode> copyMap = new HashMap<Integer, IAstNode>();
    	getNodeMap(this, copyRoot, copyMap);
    	
    	// remove dead symbols
    	/*
    	for (Iterator<ISymbol> iter = scope.iterator(); iter.hasNext(); ) {
    		ISymbol sym = iter.next();
    		// dangling symbol?
    		if (sym.getDefinition() != null && !copyMap.containsKey(sym.getDefinition().getId())) {
    			iter.remove();
    		}
    	}*/
    	
    	Map<Integer, ISymbol> symbolMap = new HashMap<Integer, ISymbol>();
    	for (ISymbol symbol : scope) {
    		ISymbol copySymbol = copy.copySymbol(symbol);
    		/*
    		ISymbol copySymbol = symbol.newInstance();
    		copy.add(copySymbol);
    		//copySymbol.setScope(copy);
*/
    		assert !symbolMap.containsKey(symbol.getNumber());
    		symbolMap.put(symbol.getNumber(), copySymbol);
    		if (symbol.getDefinition() != null) {
    			IAstNode copyDef = copyMap.get(symbol.getDefinition().getId());
    			
    			//assert (copyDef != null); // may be null for dead symbols
    			copySymbol.setDefinition(copyDef);
    			// the type gets blitzed
    			copySymbol.setType(symbol.getType());
    		}
    	}
    	replaceSymbols(this, copyRoot, scope, symbolMap);
    	return copy;
    }
	
    /**
	 * @param copyRoot
     * @param origScope 
	 * @param symbolMap
	 */
	private static void replaceSymbols(IAstNode origRoot, IAstNode copyRoot,
			IScope origScope, Map<Integer, ISymbol> symbolMap) {
		if (origRoot instanceof IAstSymbolExpr) {
			ISymbol symbol = ((IAstSymbolExpr)origRoot).getSymbol();
			if (symbol.getScope() == origScope) {
				ISymbol replaced = symbolMap.get(symbol.getNumber());
				assert (replaced != null);
				((IAstSymbolExpr) copyRoot).setSymbol(replaced);
			}
		}
		IAstNode[] kids = origRoot.getChildren(); 
		IAstNode[] copyKids = copyRoot.getChildren();
		for (int i = 0; i < kids.length; i++) {
			replaceSymbols(kids[i], copyKids[i], origScope, symbolMap);
		}
	
	}

	/**
	 * @param copyRoot
	 * @param copyMap
	 */
	private void getNodeMap(IAstNode orig, IAstNode copy, Map<Integer, IAstNode> copyMap) {
		assert !copyMap.containsKey(orig.getId()) || copyMap.get(orig.getId()) == copy;
		copyMap.put(orig.getId(), copy);
		IAstNode[] kids = orig.getChildren();
		IAstNode[] copyKids = copy.getChildren();
		if (kids.length  != copyKids.length)
			throw new IllegalStateException();
		for (int i = 0; i < kids.length; i++) {
			getNodeMap(kids[i], copyKids[i], copyMap);
		}
	}

	protected <T extends IAstNode> T fixup(T orig, T copy) {
    	((AstNode)copy).id = ((AstNode)orig).id;
    	copy.setSourceRef(orig.getSourceRef());
    	if (orig instanceof IAstTypedNode)
    		((IAstTypedNode) copy).setType(((IAstTypedNode) orig).getType());
    	return copy;
    }
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getOwnerScope()
	 */
	@Override
	public IScope getOwnerScope() {
		IAstNode node = this;
		while (node != null) {
			if (node instanceof IAstScope)
				return ((IAstScope) node).getScope();
			node = node.getParent();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#validateTypes()
	 */
	@Override
	public void validateType(TypeEngine typeEngine) throws TypeException {
		if (this instanceof IAstTypedNode) {
			LLType thisType = ((IAstTypedNode) this).getType();
			if (thisType == null || !thisType.isComplete() || thisType.isGeneric())
				throw new TypeException(this, "type inference cannot resolve type");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#validateTypes()
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (this instanceof IAstTypedNode) {
			LLType thisType = ((IAstTypedNode) this).getType();
			if (thisType == null || !thisType.isComplete())
				return;
			
			for (IAstNode kid : getChildren()) {
				if (kid instanceof IAstTypedNode) {
					LLType kidType = ((IAstTypedNode) kid).getType();
					if (kidType != null && kidType.isComplete()) {
						if (!typeEngine.getBaseType(thisType).equals(typeEngine.getBaseType(kidType))) {
							throw new TypeException(kid, "expression's type does not match parent");
						}
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#uniquifyIds()
	 */
	@Override
	public void uniquifyIds() {
		this.id = gId++;
		for (IAstNode kid : getChildren())
			kid.uniquifyIds();
	}
}
