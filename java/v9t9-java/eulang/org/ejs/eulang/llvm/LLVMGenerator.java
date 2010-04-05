/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstBlockStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFloatLitExpr;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLabelStmt;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.impl.AstSymbolExpr;
import org.ejs.eulang.ast.impl.ComparisonOperation;
import org.ejs.eulang.llvm.directives.LLConstantDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLGlobalDirective;
import org.ejs.eulang.llvm.directives.LLTargetDataTypeDirective;
import org.ejs.eulang.llvm.directives.LLTargetTripleDirective;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLBaseInstr;
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.instrs.LLBranchInstr;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLRetInst;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.instrs.LLUnaryInstr;
import org.ejs.eulang.llvm.instrs.LLUncondBranchInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLTempOp;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.symbols.ModuleScope;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLVoidType;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * Generate LLVM instructions
 * @author ejs
 *
 */
public class LLVMGenerator {

	private List<Message> messages;
	private final TypeEngine typeEngine;
	private final ITarget target;
	private LLModule ll;
	private LLDefineDirective currentDefine;

	public LLVMGenerator(ITarget target) {
		this.typeEngine = target.getTypeEngine();
		this.target = target;
		
		this.ll = new LLModule();
		messages = new ArrayList<Message>();
	}
	
	/**
	 * @return the messages
	 */
	public List<Message> getMessages() {
		return messages;
	}
	
	/**
	 * @return the sb
	 */
	public String getText() {
		return ll.toString();
	}
	
	/**
	 * @param stmt
	 */
	private void ensureTypes(IAstNode node) throws ASTException {
		// TODO: multiple expansions
		if (node instanceof IAstDefineStmt) {
			ensureTypes(((IAstDefineStmt) node).getExpr());
			return;
		} else if (node instanceof IAstSymbolExpr) {
			// don't get stuck on definition and don't recurse up
			if (((IAstSymbolExpr) node).getSymbol().getDefinition() != node.getParent()) {
				ensureTypes(((IAstSymbolExpr) node).getSymbol().getDefinition());
				return;
			}
		} 
		if (node instanceof IAstTypedNode) {
			IAstTypedNode typed = (IAstTypedNode) node;
			if (typed.getType() == null || !typed.getType().isComplete())
				throw new ASTException(node, "incomplete type information; add some specifications");
		}
		for (IAstNode kid : node.getChildren()) {
			ensureTypes(kid);
		}
	}

	public void generate(IAstModule module) {
		currentDefine = null;
		
		ll.add(new LLTargetDataTypeDirective(typeEngine));
		ll.add(new LLTargetTripleDirective(target));
		
		for (IAstStmt stmt : module.getStmtList().list()) {
			// TODO: rules for module scopes, exports, visibility... right now we export everything
			try {
				if (stmt instanceof IAstAllocStmt)
					generateGlobalAlloc((IAstAllocStmt)stmt);
				else if (stmt instanceof IAstDefineStmt)
					generateGlobalDefine((IAstDefineStmt)stmt);
					/* ignore */
				else
					unhandled(stmt);
			} catch (ASTException e) {
				messages.add(new Message(e.getNode().getSourceRef(), e.getMessage())); 
			}
		}
	}

	private void unhandled(IAstNode node) throws ASTException {
		throw new ASTException(node, "unhandled generating: " + node.toString());
	}
	
	/**
	 * {@link http://www.llvm.org/docs/LangRef.html#globalvars}
	 * @param stmt
	 * @throws ASTException
	 */
	private void generateGlobalAlloc(IAstAllocStmt stmt) throws ASTException {
		ensureTypes(stmt);
		
		if (stmt.getType() instanceof LLCodeType)
			generateGlobalCode(stmt.getSymbol(), (IAstCodeExpr) stmt.getExpr());
		else
			ll.add(new LLGlobalDirective(stmt.getSymbol(), LLVisibility.DEFAULT, LLLinkage.INTERNAL, stmt.getType()));
		
	}


	/**
	 * {@link http://www.llvm.org/docs/LangRef.html#functionstructure}
	 * @param stmt
	 * @throws ASTException
	 */
	private void generateGlobalDefine(IAstDefineStmt stmt) throws ASTException {
	
		// TODO: get appropriate entry(-ies)
		ensureTypes(stmt.getExpr());
		
		if (stmt.getExpr() instanceof IAstCodeExpr) {
			generateGlobalCode(stmt.getSymbol(), (IAstCodeExpr) stmt.getExpr());
		} else if (stmt.getExpr() instanceof IAstLitExpr) {
			generateGlobalConstant(stmt.getSymbol(), (IAstLitExpr) stmt.getExpr());
		} else {
			unhandled(stmt);
		}
	}

	/**
	 * @param symbol
	 * @param expr
	 * @throws ASTException 
	 */
	private void generateGlobalConstant(ISymbol symbol, IAstLitExpr expr) throws ASTException {
		ensureTypes(expr);
		
		ll.add(new LLConstantDirective(symbol, true, expr.getType(), new LLConstant(expr.getLiteral())));
		
	}


	private LLFuncAttrs getFuncAttrType(IAstCodeExpr expr) {
		return new LLFuncAttrs();
	}

	private LLArgAttrType[] getArgAttrTypes(IAstArgDef[] argumentTypes) {
		LLArgAttrType[] attrTypes = new LLArgAttrType[argumentTypes.length];
		for (int i = 0; i < attrTypes.length; i++) {
			attrTypes[i] = new LLArgAttrType(argumentTypes[i].getName(),  null, argumentTypes[i].getType());
		}
		return attrTypes;
	}

	private LLAttrType getRetAttrType(IAstType returnType) {
		return new LLAttrType(null, returnType.getType());
	}
	/**
	 * @param symbol
	 * @param expr
	 * @throws ASTException 
	 */
	private void generateGlobalCode(ISymbol symbol, IAstCodeExpr expr) throws ASTException {
		ensureTypes(expr);
		
		LLDefineDirective define = new LLDefineDirective(symbol, 
				null /*linkage*/,
				LLVisibility.DEFAULT,
				null, //target.getLLCallingConvention(),
				getRetAttrType(expr.getPrototype().returnType()),
				getArgAttrTypes(expr.getPrototype().argumentTypes()),
				getFuncAttrType(expr),
				null /*section*/,
				0 /*align*/,
				null /*gc*/);
		ll.add(define);
		
		generateCode(define, symbol, expr);
	}

	private void emit( LLBaseInstr instr) {
		currentDefine.getCurrentBlock().instrs().add(instr);
	}
	/**
	 * @param blocks
	 * @param stmts
	 * @throws ASTException 
	 */
	private void generateCode(LLDefineDirective define, ISymbol symbol, IAstCodeExpr codeOrig) throws ASTException {
		LLDefineDirective oldDefine = currentDefine;
		
		IAstCodeExpr code = codeOrig.copy(null);
		
		try {
			currentDefine = define;
			
			IScope scope = code.getScope();
			define.addBlock(scope.addTemporary("entry"));
			
			// get return value
			ISymbol retvalSym = scope.addTemporary("retval");
			LLOperand retval = new LLSymbolOp(retvalSym);
			LLType returnType = code.getPrototype().returnType().getType();
			emit(new LLAllocaInstr(retval, returnType));
			
			// get address of each incoming argument, assuming it 
			// will be accessed only on the frame in the best case
			for (IAstArgDef argDef : code.getPrototype().argumentTypes()) {
				LLOperand argVal = generateSymbolExpr(argDef.getSymbolExpr());
				//emit(new LLAllocaInstr(argVal, argDef.getType()));
				
				ISymbol argAddrSym = scope.addTemporary(argDef.getName() + "_addr");
				//argAddrSym.setType(typeEngine.getPointerType(argDef.getSymbolExpr().getSymbol().getType()));
				argAddrSym.setType(argDef.getSymbolExpr().getSymbol().getType());
				argAddrSym.setDefinition(argDef.getSymbolExpr().getSymbol().getDefinition());
				
				LLOperand argAddr = new LLSymbolOp(argAddrSym);
				emit(new LLAllocaInstr(argAddr, typeEngine.INTPTR));
				
				emit(new LLStoreInstr(argDef.getType(), argVal, argAddr));
				
				// now change all code
				replaceSymbols(code, argDef.getSymbolExpr().getSymbol(), argAddrSym);
			}
			
			LLOperand ret = generateStmtList(code.stmts());
			emit(new LLStoreInstr(returnType, ret, retval));
			
			LLOperand retvalTemp = temp();
			emit(new LLLoadInstr(retvalTemp, returnType, retval));
			emit(new LLRetInst(returnType, retvalTemp));
		} finally {
			currentDefine = oldDefine;
		}
		
	}

	/**
	 * @param code
	 * @param symbolExpr
	 * @param argAddrSym
	 */
	private void replaceSymbols(IAstNode node, ISymbol from, ISymbol to) {
		if (node instanceof IAstSymbolExpr) {
			IAstSymbolExpr symExpr = (IAstSymbolExpr) node;
			if (symExpr.getSymbol().equals(from))
				symExpr.setSymbol(to);
		}
		for (IAstNode kid : node.getChildren())
			replaceSymbols(kid, from, to);
	}

	/**
	 * @param stmts
	 * @throws ASTException 
	 */
	private LLOperand generateStmtList(IAstNodeList<IAstStmt> stmts) throws ASTException {
		LLOperand result = null;
		for (IAstStmt stmt : stmts.list()) {
			// ensure we have a block
			if (stmt instanceof IAstLabelStmt) {
				currentDefine.addBlock(((IAstLabelStmt) stmt).getLabel().getSymbol());
				continue;
			} else if (currentDefine.getCurrentBlock() == null) {
				currentDefine.addBlock(stmts.getOwnerScope().addTemporary("block"));
			}
			
			result = generateStmt(stmt);
			
			
			// end of block instr
			if (stmt instanceof IAstGotoStmt) {
				currentDefine.setCurrentBlock(null);
			}
		}	
		return result;
	}

	private LLOperand generateStmt( IAstStmt stmt) throws ASTException {
		LLOperand result = null;
		if (stmt instanceof IAstExprStmt) {
			result = generateExprStmt((IAstExprStmt) stmt);
		} else if (stmt instanceof IAstAllocStmt) {
			result = generateLocalAllocStmt((IAstAllocStmt) stmt);
		} else if (stmt instanceof IAstAssignStmt) {
			result = generateAssignStmt((IAstAssignStmt) stmt);
		} else if (stmt instanceof IAstBlockStmt) {
			result = generateStmtList(((IAstBlockStmt) stmt).stmts());
		} else if (stmt instanceof IAstDefineStmt) {
			// ignore
		}
		return result;
	}

	private LLOperand generateAssignStmt(
			IAstAssignStmt stmt) throws ASTException {
		return generateAssign(stmt.getType(), stmt.getSymbol(), stmt.getExpr());
	}

	private LLOperand generateLocalAllocStmt(
			IAstAllocStmt stmt) throws ASTException {
		
		LLOperand ret = generateSymbolExpr(stmt.getSymbolExpr());
		
		emit(new LLAllocaInstr(ret, stmt.getType()));
		
		if (stmt.getExpr() != null) {
			return generateAssign(stmt.getType(), new AstSymbolExpr(stmt.getSymbol()), stmt.getExpr());
		}
		return null;
	}

	private LLOperand generateAssign( LLType type,
			IAstSymbolExpr symbolExpr, IAstTypedExpr expr) throws ASTException {
		LLOperand value = generateTypedExpr(expr);
		LLOperand var = generateSymbolExpr(symbolExpr);
		emit(new LLStoreInstr(type, value, var));
		return var;
	}


	private LLOperand generateSymbolExpr(
			IAstSymbolExpr symbolExpr) {
		// TODO: out-of-scope variables
		return new LLSymbolOp(symbolExpr.getSymbol());
	}

	private LLOperand generateStmtListExpr(
			IAstStmtListExpr expr) throws ASTException {
		LLOperand result = null;
		for (IAstStmt stmt : expr.getStmtList().list()) {
			result = generateStmt(stmt);
		}
		return result;
	}
	
	private LLOperand generateExprStmt( IAstExprStmt expr) throws ASTException {
		return generateTypedExpr(expr.getExpr());
	}

	private LLOperand generateTypedExpr( IAstTypedExpr expr) throws ASTException {
		if (expr instanceof IAstExprStmt) 
			return generateExprStmt((IAstExprStmt) expr);
		else if (expr instanceof IAstStmtListExpr)
			return generateStmtListExpr((IAstStmtListExpr) expr);
		else if (expr instanceof IAstLitExpr)
			return generateLitExpr((IAstLitExpr) expr);
		else if (expr instanceof IAstFuncCallExpr)
			return generateFuncCallExpr((IAstFuncCallExpr) expr);
		else if (expr instanceof IAstSymbolExpr) {
			// TODO: hacky
			LLOperand symOp = generateSymbolExpr((IAstSymbolExpr) expr);
			if (symOp instanceof LLTempOp || (((IAstSymbolExpr) expr).isAddress()))
				return symOp;
			LLTempOp temp = temp();
			emit(new LLLoadInstr(temp, expr.getType(), symOp));
			return temp;
		}
		else if (expr instanceof IAstUnaryExpr)
			return generateUnaryExpr((IAstUnaryExpr) expr);
		else if (expr instanceof IAstBinExpr)
			return generateBinExpr((IAstBinExpr) expr);
		else if (expr instanceof IAstCondList)
			return generateCondList((IAstCondList) expr);
		else {
			unhandled(expr);
			return null;
		}
	}

	/**
	 * @param expr
	 * @return
	 * @throws ASTException 
	 */
	private LLOperand generateCondList(IAstCondList condList) throws ASTException {
		IScope scope = condList.getOwnerScope();
		ISymbol retvalSym = scope.addTemporary("cond");
		LLOperand retval = new LLSymbolOp(retvalSym);
		emit(new LLAllocaInstr(retval, condList.getType()));
		
		// generate a series of tests
		LLBlock[] conds = new LLBlock[condList.getCondExprs().nodeCount()];

		ISymbol nextTest = null;
		int idx = 0;
		for (IAstCondExpr expr : condList.getCondExprs().list()) {
			ISymbol resultLabel;
			
			if (nextTest != null) {
				currentDefine.addBlock(nextTest);
			}
			if (idx + 1 < condList.getCondExprs().nodeCount()) {
				LLOperand test = generateTypedExpr(expr.getTest());
				resultLabel = scope.addTemporary("condBlock");
				nextTest = scope.addTemporary("condTest");
				emit(new LLBranchInstr(expr.getTest().getType(), test, new LLSymbolOp(resultLabel), new LLSymbolOp(nextTest)));
			} else {
				// last test is always true
				resultLabel = nextTest;
			}
			
			currentDefine.addBlock(resultLabel);
			
			LLOperand result = generateTypedExpr(expr.getExpr());
			
			emit(new LLStoreInstr(condList.getType(), result, retval));
			conds[idx++] = currentDefine.getCurrentBlock();
		}
		
		ISymbol condSetSym = scope.addTemporary("condset");
		currentDefine.addBlock(condSetSym);
		
		for (LLBlock cond : conds)
			cond.instrs().add(new LLUncondBranchInstr(new LLSymbolOp(condSetSym)));
		
		LLOperand retTemp = temp();
		emit(new LLLoadInstr(retTemp, condList.getType(), retval));
		
		return retTemp;
	}

	private LLOperand generateUnaryExpr(IAstUnaryExpr expr) throws ASTException {
		LLOperand op = generateTypedExpr(expr.getExpr());
		LLTempOp ret = temp();
		if (expr.getOp().getLLVMName() != null) {
			emit(new LLUnaryInstr(expr.getOp(), ret, expr.getType(), op));
		} else {
			if (expr.getOp() == IOperation.NEG) {
				// result = sub 0, val
				emit(new LLBinaryInstr("sub", IOperation.SUB, ret, expr.getType(), new LLConstOp(0), op));
			} else {
				unhandled(expr);
			}
		}
		return ret;
	}

	private LLOperand generateBinExpr(IAstBinExpr expr) throws ASTException {
		LLOperand left = generateTypedExpr(expr.getLeft());
		LLOperand right = generateTypedExpr(expr.getRight());
		
		LLTempOp ret = temp();
		String instr = expr.getOp().getLLVMName();
		if (instr != null) {
			if (expr.getOp() instanceof ComparisonOperation) {
				if (expr.getLeft().getType().getBasicType() == BasicType.FLOATING)
					instr = "fcmp " + ((ComparisonOperation) expr.getOp()).getLLFloatPrefix()  + instr;
				else
					instr = "icmp " + ((ComparisonOperation) expr.getOp()).getLLIntPrefix() + instr;
			}
			emit(new LLBinaryInstr(instr, expr.getOp(), ret, expr.getLeft().getType(), left, right));
		} else {
			unhandled(expr);
		}
		return ret;
	}

	/**
	 * @param define
	 * @param expr
	 * @return
	 */
	private LLOperand generateFuncCallExpr(
			IAstFuncCallExpr expr) throws ASTException {
		LLOperand ret = null;
		
		//LLCodeType funcType = (LLCodeType) expr.getFunction().getType();
		
		LLCodeType funcType = (LLCodeType) getSymbolType(expr.getFunction());
		LLOperand[] ops = new LLOperand[funcType.getArgTypes().length];
		if (!(funcType.getRetType() instanceof LLVoidType)) {
			ret = temp();
		}
		
		int idx = 0;
		for (IAstTypedExpr arg : expr.arguments().list()) {
			ops[idx++] = generateTypedExpr(arg);
		}

		LLOperand func = generateTypedExpr(expr.getFunction());
		
		emit(new LLCallInstr(ret, expr.getType(), func, funcType, ops));
		return ret;
	}

	/**
	 * @param node
	 * @return
	 */
	private LLType getSymbolType(IAstTypedExpr node) {
		if (node.getType() != null)
			return node.getType();
		
		if (node instanceof IAstSymbolExpr) {
			IAstNode def = ((IAstSymbolExpr) node).getSymbol().getDefinition();
			if (def instanceof IAstDefineStmt) {
				// TODO: instances
				return ((IAstDefineStmt) def).getExpr().getType();
			}
			if (!(def instanceof ITyped))
				return null;
			return ((ITyped)node).getType();
		} 
		return null;
	}

	/**
	 * @return
	 */
	private LLTempOp temp() {
		return new LLTempOp(currentDefine.nextId());
	}

	private LLOperand generateLitExpr( IAstLitExpr expr) throws ASTException {
		return new LLConstOp((Number) expr.getObject());
			
	}

}
