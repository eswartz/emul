/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDataDecl;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstDataDecl extends AstTypedNode implements IAstDataDecl {

	private IAstNodeList<IAstTypedNode> statics;
	private IAstNodeList<IAstTypedNode> fields;

	/**
	 * @param fields
	 * @param statics
	 */
	public AstDataDecl(IAstNodeList<IAstTypedNode> fields,
			IAstNodeList<IAstTypedNode> statics) {
		setFields(fields);
		setStatics(statics);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#getFields()
	 */
	@Override
	public IAstNodeList<IAstTypedNode> getFields() {
		return fields;
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
		this.fields = reparent(this.fields, fields);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDataDecl#setStatics(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setStatics(IAstNodeList<IAstTypedNode> statics) {
		this.statics = reparent(this.statics, statics);

	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstDataDecl copy(IAstNode copyParent) {
		return fixup(this, new AstDataDecl(doCopy(fields, copyParent), doCopy(statics, copyParent)));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { fields, statics };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (statics == existing) 
			setStatics((IAstNodeList<IAstTypedNode>) another);
		else if (statics == fields)
			setFields((IAstNodeList<IAstTypedNode>) another);
		else
			throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		
		boolean changed = false;
		
		if (canReplaceType(this)) {
			// 
		}
		return changed;
	}

}
