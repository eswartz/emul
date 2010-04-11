/**
 * 
 */
package org.ejs.eulang.llvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.ISourceRef;
import org.ejs.eulang.ITarget;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.Message;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstAllocTupleStmt;
import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstAssignTupleStmt;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstBlockStmt;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstLabelStmt;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTupleExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.impl.ArithmeticBinaryOperation;
import org.ejs.eulang.ast.impl.ComparisonBinaryOperation;
import org.ejs.eulang.llvm.directives.LLConstantDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLGlobalDirective;
import org.ejs.eulang.llvm.directives.LLTargetDataTypeDirective;
import org.ejs.eulang.llvm.directives.LLTargetTripleDirective;
import org.ejs.eulang.llvm.instrs.LLAllocaInstr;
import org.ejs.eulang.llvm.instrs.LLBinaryInstr;
import org.ejs.eulang.llvm.instrs.LLBranchInstr;
import org.ejs.eulang.llvm.instrs.LLCallInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr;
import org.ejs.eulang.llvm.instrs.LLExtractValueInstr;
import org.ejs.eulang.llvm.instrs.LLInsertValueInstr;
import org.ejs.eulang.llvm.instrs.LLLoadInstr;
import org.ejs.eulang.llvm.instrs.LLRetInst;
import org.ejs.eulang.llvm.instrs.LLStoreInstr;
import org.ejs.eulang.llvm.instrs.LLUnaryInstr;
import org.ejs.eulang.llvm.instrs.LLUncondBranchInstr;
import org.ejs.eulang.llvm.instrs.LLCastInstr.ECast;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.ops.LLUndefOp;
import org.ejs.eulang.llvm.ops.LLVariableOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
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
	private ILLCodeTarget currentTarget;
	private LLVariableStorage varStorage;

	public LLVMGenerator(ITarget target) {
		this.typeEngine = target.getTypeEngine();
		this.target = target;
		
		messages = new ArrayList<Message>();
		varStorage = new LLVariableStorage();
	}
	
	private Map<String, String[]> fileText = new HashMap<String, String[]>();
	
	protected String getSource(ISourceRef ref) {
		if (ref == null || ref.getFile() == null) return "";
		
		String[] fileC = fileText.get(ref.getFile());
		if (fileC == null) {
			try {
				File file = new File(ref.getFile());
				FileInputStream fis = new FileInputStream(file);
				byte[] text = new byte[(int) file.length()];
				fis.read(text);
				String fileT = new String(text);
				fileText.put(ref.getFile(), fileT.split("\n"));
			} catch (IOException e) {
				return "";
			}
		}
		
		int offset = Math.max(1, ref.getLine());
		return fileC[offset-1].substring(ref.getColumn());
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
		if (node instanceof IAstDefineStmt) {
			//ensureTypes(((IAstDefineStmt) node).getExpr());
			return;
		} else if (node instanceof IAstSymbolExpr) {
			// don't get stuck on definition and don't recurse up
			IAstSymbolExpr symExpr = (IAstSymbolExpr) node;
			if (symExpr.getDefinition() != null) {
				IAstTypedExpr instance = symExpr.getInstance();
				if (instance == null)
					throw new ASTException(node, "could not find an instance for " + symExpr.getSymbol().getName() +"; add some type specifications");
				ensureTypes(instance);
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
		for (Map.Entry<String, String> nfEnt : module.getNonFileText().entrySet())
			fileText.put(nfEnt.getKey(), nfEnt.getValue().split("\n"));
		
		this.ll = new LLModule(module.getOwnerScope());

		currentTarget = null;
		
		ll.addModuleDirective(new LLTargetDataTypeDirective(typeEngine));
		ll.addModuleDirective(new LLTargetTripleDirective(target));
		
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
		

		for (LLType type : typeEngine.getTypes()) {
			if (type.getLLVMName() != null)
				ll.addExternType(type);
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
	
		// only generate concrete instances
		for (IAstTypedExpr expr : stmt.bodyList()) {
			if (expr.getType() == null || expr.getType().isGeneric())
				continue;
		
			generateGlobalExpr(stmt, expr);
		}
		for (List<IAstTypedExpr> instanceList : stmt.bodyToInstanceMap().values()) {
			for (IAstTypedExpr instance : instanceList)
				generateGlobalExpr(stmt, instance);
		}
	}

	private void generateGlobalExpr(IAstDefineStmt stmt, IAstTypedExpr expr)
			throws ASTException {
		ensureTypes(expr);
		
		if (expr instanceof IAstCodeExpr) {
			generateGlobalCode(stmt.getSymbol(), (IAstCodeExpr) expr);
		} else if (expr instanceof IAstLitExpr) {
			generateGlobalConstant(stmt.getSymbol(), (IAstLitExpr) expr);
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
		
		ISymbol modSymbol = ll.getModuleSymbol(symbol, expr);
		ll.add(new LLConstantDirective(modSymbol, true, expr.getType(), new LLConstant(expr.getLiteral())));
		
	}


	private LLFuncAttrs getFuncAttrType(IAstCodeExpr expr) {
		return new LLFuncAttrs();
	}

	private LLArgAttrType[] getArgAttrTypes(IAstArgDef[] argumentTypes) {
		LLArgAttrType[] attrTypes = new LLArgAttrType[argumentTypes.length];
		for (int i = 0; i < attrTypes.length; i++) {
			LLType argType = argumentTypes[i].getType();
			if (argumentTypes[i].isVar())
				argType = typeEngine.getPointerType(argType);
			LLAttrs attrs = null; //new LLAttrs("noalias");
			// /*ISymbol typeSymbol =*/ ll.addExternType(argumentTypes[i].getType());
			attrTypes[i] = new LLArgAttrType(argumentTypes[i].getName(),  attrs, argType);
		}
		return attrTypes;
	}

	private LLAttrType getRetAttrType(IAstType returnType) {
		// /*ISymbol typeSymbol =*/ ll.addExternType(returnType.getType());
		return new LLAttrType(null, returnType.getType());
	}
	/**
	 * @param symbol
	 * @param expr
	 * @throws ASTException 
	 */
	private void generateGlobalCode(ISymbol symbol, IAstCodeExpr expr) throws ASTException {
		ensureTypes(expr);
		
		ISymbol modSymbol = ll.getModuleSymbol(symbol, expr);
		
		LLDefineDirective define = new LLDefineDirective(target, ll, 
				expr.getScope(),
				modSymbol,
				null /*linkage*/, 
				LLVisibility.DEFAULT,
				null,
				getRetAttrType(expr.getPrototype().returnType()), //target.getLLCallingConvention(),
				getArgAttrTypes(expr.getPrototype().argumentTypes()),
				getFuncAttrType(expr),
				null /*section*/,
				0 /*align*/,
				null /*gc*/);
		ll.add(define);
		
		generateCode(define, symbol, expr);
	}

	/**
	 * @param argVal 
	 * @param ret
	 */
	private LLVariableOp makeLocalStorage(ISymbol symbol, boolean isVar, LLOperand argVal) {
		ILLVariable var;
		if (isVar) {
			var = new LLVarArgument(symbol, typeEngine);
		} else {
			if (symbol.getType().getBasicType() == BasicType.REF)
				var = new LLRefLocalVariable(symbol, typeEngine);
			else
				var = new LLLocalVariable(symbol, typeEngine);
		}
		varStorage.registerVariable(symbol, var);
		
		var.allocate(currentTarget, argVal);
		return new LLVariableOp(var);
	}

	/**
	 * @param blocks
	 * @param stmts
	 * @throws ASTException 
	 */
	private void generateCode(ILLCodeTarget define, ISymbol symbol, IAstCodeExpr code) throws ASTException {
		ILLCodeTarget oldDefine = currentTarget;
		
		//IAstCodeExpr code = codeOrig.copy(null);
		
		try {
			currentTarget = define;
			
			IScope scope = code.getScope();
			define.addBlock(scope.addTemporary("entry"));
			
			// get return value
			LLType returnType = code.getPrototype().returnType().getType();
			
			// get address of each incoming argument, assuming it 
			// will be accessed only on the frame in the best case
			for (IAstArgDef argDef : code.getPrototype().argumentTypes()) {
				
				ISymbol argSymbol = argDef.getSymbolExpr().getSymbol();
				LLOperand argVal = generateSymbolExpr(argDef.getSymbolExpr());
				/*LLVariableOp argAddrOp =*/ makeLocalStorage(argSymbol, argDef.isVar(), argVal);
			}
			
			LLOperand ret = generateStmtList(code.stmts());
			
			// deallocate variables
			for (ILLVariable var : varStorage.getVariablesForScope(scope)) {
				var.deallocate(currentTarget);
			}
			
			if (returnType.getBasicType() != BasicType.VOID) {
				LLOperand retVal = currentTarget.load(returnType, ret);
				currentTarget.emit(new LLRetInst(returnType, retVal));
			} else {
				currentTarget.emit(new LLRetInst(returnType));
			}
		} finally {
			currentTarget = oldDefine;
		}
		
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
				currentTarget.addBlock(((IAstLabelStmt) stmt).getLabel().getSymbol());
				continue;
			} else if (currentTarget.getCurrentBlock() == null) {
				currentTarget.addBlock(stmts.getOwnerScope().addTemporary("block"));
			}
			
			result = generateStmt(stmt);
			
			
			// end of block instr
			if (stmt instanceof IAstGotoStmt) {
				currentTarget.setCurrentBlock(null);
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
		} else if (stmt instanceof IAstAllocTupleStmt) {
			result = generateLocalAllocTupleStmt((IAstAllocTupleStmt) stmt);
		} else if (stmt instanceof IAstAssignStmt) {
			result = generateAssignStmt((IAstAssignStmt) stmt);
		} else if (stmt instanceof IAstAssignTupleStmt) {
			result = generateAssignTupleStmt((IAstAssignTupleStmt) stmt);
		} else if (stmt instanceof IAstBlockStmt) {
			result = generateStmtList(((IAstBlockStmt) stmt).stmts());
		} else if (stmt instanceof IAstDefineStmt) {
			// ignore
		} else {
			unhandled(stmt);
		}
		return result;
	}

	private LLOperand generateAssignStmt(
			IAstAssignStmt stmt) throws ASTException {
		return generateAssign(stmt.getType(), stmt.getSymbol(), stmt.getExpr());
	}
	

	private LLOperand generateLocalAllocStmt(
			IAstAllocStmt stmt) throws ASTException {
		
		//LLSymbolOp sym = new LLSymbolOp(stmt.getSymbol());

		
		LLVariableOp ret = makeLocalStorage(stmt.getSymbol(), false, null);
		
		if (stmt.getExpr() != null) {
			LLOperand value = generateTypedExpr(stmt.getExpr());
			currentTarget.store(stmt.getExpr().getType(), value, ret);
			
			//if (ret != sym)
			//	currentTarget.emit(new LLStoreInstr(stmt.getExpr().getType(), value, ret));
		}
		
		return ret;
	}

	private LLOperand generateLocalAllocTupleStmt(
			IAstAllocTupleStmt stmt) throws ASTException {

		LLOperand value = generateTypedExpr(stmt.getExpr());
		
		IAstNodeList<IAstSymbolExpr> syms = stmt.getSymbols().elements();
		
		for (int idx = 0; idx < syms.nodeCount(); idx++) {
			IAstSymbolExpr sym = syms.list().get(idx);
			
			LLOperand val = currentTarget.newTemp(sym.getType());
			currentTarget.emit(new LLExtractValueInstr(val, stmt.getType(), value, new LLConstOp(idx)));
			
			makeLocalStorage(sym.getSymbol(), false, val);
		}
		return value;
	}
	
	
	private LLOperand generateAssign( LLType type,
			IAstSymbolExpr symbolExpr, IAstTypedExpr expr) throws ASTException {
		LLOperand value = generateTypedExpr(expr);
		LLOperand var = generateSymbolExpr(symbolExpr);
		
		currentTarget.store(type, value, var);
		
		//emit(new LLStoreInstr(type, value, var));
		return var;
	}


	private LLOperand generateSymbolExpr(
			IAstSymbolExpr symbolExpr) {
		// TODO: out-of-scope variables
		ISymbol symbol = symbolExpr.getSymbol();
		if (!(symbol.getScope() instanceof LocalScope))
			symbol = ll.getModuleSymbol(symbol, symbolExpr);
		ILLVariable var = varStorage.lookupVariable(symbol);
		if (var != null)
			return new LLVariableOp(var);
		return new LLSymbolOp(symbol);
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
		//currentTarget.emit(new LLCommentInstr(getSource(expr.getSourceRef())));
		LLOperand temp;
		if (expr instanceof IAstExprStmt) 
			temp = generateExprStmt((IAstExprStmt) expr);
		else if (expr instanceof IAstStmtListExpr)
			temp = generateStmtListExpr((IAstStmtListExpr) expr);
		else if (expr instanceof IAstLitExpr)
			temp = generateLitExpr((IAstLitExpr) expr);
		else if (expr instanceof IAstFuncCallExpr)
			temp = generateFuncCallExpr((IAstFuncCallExpr) expr);
		else if (expr instanceof IAstSymbolExpr)
			temp = generateSymbolExpr((IAstSymbolExpr) expr);
		else if (expr instanceof IAstUnaryExpr)
			temp = generateUnaryExpr((IAstUnaryExpr) expr);
		else if (expr instanceof IAstBinExpr)
			temp = generateBinExpr((IAstBinExpr) expr);
		else if (expr instanceof IAstCondList)
			temp = generateCondList((IAstCondList) expr);
		else if (expr instanceof IAstTupleExpr)
			temp = generateTupleExpr((IAstTupleExpr) expr);
		else {
			unhandled(expr);
			return null;
		}
		
		temp = currentTarget.load(expr.getType(), temp);
		return temp;
	}

	/**
	 * A tuple is an unnamed type.  We just fill in all the pieces.
	 * @param tuple
	 * @return
	 * @throws ASTException
	 */
	private LLOperand generateTupleExpr(IAstTupleExpr tuple) throws ASTException {
		
		//ISymbol tupleSym = currentTarget.newTempSymbol();
		//tupleSym.setType(tuple.getType());
		//LLVariableOp ret = makeLocalStorage(tupleSym, false, null);
		
		LLOperand ret = new LLUndefOp();
		for (int idx = 0; idx < tuple.elements().nodeCount(); idx++) {
			IAstTypedExpr expr = tuple.elements().list().get(idx);
			LLOperand el = generateTypedExpr(expr);
			
			LLOperand tmp = currentTarget.newTemp(tuple.getType()); 
			currentTarget.emit(new LLInsertValueInstr(tmp, tuple.getType(), ret,
					expr.getType(), el, idx));
			ret = tmp;
		}
		
		return ret;
	}

	private LLOperand generateAssignTupleStmt(
			IAstAssignTupleStmt stmt) throws ASTException {

		LLOperand value = generateTypedExpr(stmt.getExpr());
		
		IAstNodeList<IAstSymbolExpr> syms = stmt.getSymbols().elements();
		
		for (int idx = 0; idx < syms.nodeCount(); idx++) {
			IAstSymbolExpr sym = syms.list().get(idx);
			
			LLOperand val = currentTarget.newTemp(sym.getType());
			currentTarget.emit(new LLExtractValueInstr(val, stmt.getType(), value, new LLConstOp(idx)));
			
			LLOperand var = generateSymbolExpr(sym);
			//makeLocalStorage(sym.getSymbol(), false, val);
			currentTarget.store(sym.getType(), val, var);
		}
		return value;
	}
	
	private LLOperand generateCondList(IAstCondList condList) throws ASTException {
		IScope scope = condList.getOwnerScope();
		ISymbol retvalSym = scope.addTemporary("cond");
		LLOperand retval = new LLSymbolOp(retvalSym);
		currentTarget.emit(new LLAllocaInstr(retval, condList.getType()));
		
		// generate a series of tests
		LLBlock[] conds = new LLBlock[condList.getCondExprs().nodeCount()];

		ISymbol nextTest = null;
		int idx = 0;
		for (IAstCondExpr expr : condList.getCondExprs().list()) {
			ISymbol resultLabel;
			
			if (nextTest != null) {
				currentTarget.addBlock(nextTest);
			}
			if (idx + 1 < condList.getCondExprs().nodeCount()) {
				LLOperand test = generateTypedExpr(expr.getTest());
				resultLabel = scope.addTemporary("cb");
				nextTest = scope.addTemporary("ct");
				currentTarget.emit(new LLBranchInstr(
						//expr.getTest().getType(),
						typeEngine.LLBOOL,
						test, new LLSymbolOp(resultLabel), new LLSymbolOp(nextTest)));
			} else {
				// last test is always true
				resultLabel = nextTest;
			}
			
			currentTarget.addBlock(resultLabel);
			
			LLOperand result = generateTypedExpr(expr.getExpr());
			
			currentTarget.emit(new LLStoreInstr(condList.getType(), result, retval));
			conds[idx++] = currentTarget.getCurrentBlock();
		}
		
		ISymbol condSetSym = scope.addTemporary("cs");
		currentTarget.addBlock(condSetSym);
		
		for (LLBlock cond : conds)
			cond.instrs().add(new LLUncondBranchInstr(new LLSymbolOp(condSetSym)));
		
		LLOperand retTemp = currentTarget.newTemp(condList.getType());
		currentTarget.emit(new LLLoadInstr(retTemp, condList.getType(), retval));
		
		return retTemp;
	}
	
	private LLOperand generateUnaryExpr(IAstUnaryExpr expr) throws ASTException {
		LLOperand ret;
		LLOperand op = generateTypedExpr(expr.getExpr());
		if (expr.getOp().getLLVMName() != null) {
			ret = currentTarget.newTemp(expr.getType());
			currentTarget.emit(new LLUnaryInstr(expr.getOp(), ret, expr.getType(), op));
		} else {
			if (expr.getOp() == IOperation.NEG) {
				// result = sub 0, val
				ret = currentTarget.newTemp(expr.getType());
				currentTarget.emit(new LLBinaryInstr("sub", ret, expr.getType(), new LLConstOp(0), op));
			} else if (expr.getOp() == IOperation.CAST) {
				ret = generateCast(expr, op);
			} else {
				unhandled(expr);
				ret = null;
			}
		}
		return ret;
	}

	/**
	 * Cast one value (value, w/origType) to another (type).
	 * Loads and stores automatically handle memory dereferencing, so we can ignore
	 * those casts, unless they are illegal:
	 * <p>
	 * <li>casting from value to reference (should be explicit new)
	 * <li>casting from pointer (var) to reference (another explicit new)
	 * <li>casting from reference to reference (types change, should be explicit new)
	 * <li>casting from value to pointer (should not happen)
	 * <li>casting from pointer to pointer (types chane, should not happen)
	 * <p>
	 * We handle casting from reference, pointer, etc. to value by dereferencing implicitly.  
	 * @param type
	 * @param expr
	 */
	private LLOperand generateCast(IAstUnaryExpr expr, LLOperand value) throws ASTException {
		LLType type = expr.getType();
		LLType origType = expr.getExpr().getType();
		
		//if (type.getBasicType() == BasicType.REF)
		//	throw new ASTException(expr, "cannot cast to a reference; must use .New()");
		//if (type.getBasicType() == BasicType.POINTER)
		//	throw new ASTException(expr, "cannot cast to a pointer");
		
		// first, automagically skip all memory operations
		while (origType.getBasicType() == BasicType.REF || origType.getBasicType() == BasicType.POINTER) {
			// dereference the value...
			value = currentTarget.load(origType.getSubType(), value);
			origType = origType.getSubType();
		}
		
		// strip target type to basic
		while (type.getBasicType() == BasicType.REF || type.getBasicType() == BasicType.POINTER) {
			type = type.getSubType();
		}
		
		// now, do value conversion to basic type
		if (origType.equals(type)) {
			// good
		} else {
			ECast cast = null;
			if ((origType.getBasicType() == BasicType.INTEGRAL || origType.getBasicType() == BasicType.BOOL) 
					&& type.getBasicType() == BasicType.INTEGRAL || type.getBasicType() == BasicType.BOOL) {
				if (origType.getBits() > type.getBits()) {
					cast = ECast.TRUNC;
				}
				else if (origType.getBits() < type.getBits()) {
					// TODO: signedness
					cast = ECast.SEXT;
				} 
				else {
					cast = ECast.BITCAST;
				}
			} 
			else if (origType.getBasicType() == BasicType.FLOATING && type.getBasicType() == BasicType.FLOATING) {
				if (origType.getBits() > type.getBits()) {
					cast = ECast.FPTRUNC;
				}
				else if (origType.getBits() < type.getBits()) {
					cast = ECast.FPEXT;
				} 
				else {
					cast = ECast.BITCAST;
				}
			}
			else if ((origType.getBasicType() == BasicType.INTEGRAL || origType.getBasicType() == BasicType.BOOL)
					&& type.getBasicType() == BasicType.FLOATING) {
				// TODO: signedness
				cast = ECast.SITOFP;
			}
			else if (origType.getBasicType() == BasicType.FLOATING
					&& (type.getBasicType() == BasicType.INTEGRAL || type.getBasicType() == BasicType.BOOL)) {
				// TODO: signedness
				cast = ECast.FPTOSI;
			}
			else
				unhandled(expr);
			
			LLOperand temp = currentTarget.newTemp(type);
			currentTarget.emit(new LLCastInstr(temp, cast, origType, value, type));
			
			value = temp;
		}
		
		return value;
	}

	private LLOperand generateBinExpr(IAstBinExpr expr) throws ASTException {
		IBinaryOperation op = expr.getOp();

		if (op == IOperation.COMPAND) {
			return generateShortCircuitAnd(expr);
		} else if (op == IOperation.COMPOR) {
			return generateShortCircuitOr(expr);
		}
		
		LLOperand left = generateTypedExpr(expr.getLeft());
		LLOperand right = generateTypedExpr(expr.getRight());
		
		LLOperand ret = currentTarget.newTemp(expr.getType());
		String instr = op.getLLVMName();
		if (instr != null) {
			if (op instanceof ComparisonBinaryOperation) {
				if (expr.getLeft().getType().getBasicType() == BasicType.FLOATING)
					instr = "fcmp " + ((ComparisonBinaryOperation) op).getLLFloatPrefix()  + instr;
				else
					instr = "icmp " + ((ComparisonBinaryOperation) op).getLLIntPrefix() + instr;
			}
			else if (op instanceof ArithmeticBinaryOperation) {
				String prefix = (expr.getLeft().getType().getBasicType() == BasicType.FLOATING) ? 
						((ArithmeticBinaryOperation) op).getFloatPrefix() : ((ArithmeticBinaryOperation) op).getIntPrefix();
				if (prefix != null) 
					instr = prefix + instr;
			}
			currentTarget.emit(new LLBinaryInstr(instr, ret, expr.getLeft().getType(), left, right));
		} else {
			unhandled(expr);
		}
		return ret;
	}

	private LLOperand generateShortCircuitAnd(IAstBinExpr expr) throws ASTException {
		IBinaryOperation op = expr.getOp();
		assert op == IOperation.COMPAND;
		
		IScope scope = expr.getOwnerScope();
		
		// get a var for the outcome
		ISymbol boolResultSym = scope.addTemporary("and");
		LLOperand retval = new LLSymbolOp(boolResultSym);
		currentTarget.emit(new LLAllocaInstr(retval, expr.getType()));

		ISymbol rhsLabel, outLabel;

		///
		
		// calculate the left side and save that
		LLOperand left = generateTypedExpr(expr.getLeft());
		currentTarget.store(expr.getType(), left, retval);
		
		rhsLabel = scope.addTemporary("rhsOut");
		outLabel = scope.addTemporary("andOut");
		
		// if it was false, done
		currentTarget.emit(new LLBranchInstr(
				expr.getLeft().getType(),
				//typeEngine.LLBOOL,
				left, new LLSymbolOp(rhsLabel), new LLSymbolOp(outLabel)));
		
		//
		
		// else, calculate rhs and overwrite the result with that
		currentTarget.addBlock(rhsLabel);
		
		LLOperand right = generateTypedExpr(expr.getRight());
		
		currentTarget.store(expr.getRight().getType(), right, retval);
		currentTarget.emit(new LLUncondBranchInstr(new LLSymbolOp(outLabel)));
		
		currentTarget.addBlock(outLabel);
			
		LLOperand retTemp = currentTarget.newTemp(expr.getType());
		currentTarget.emit(new LLLoadInstr(retTemp, expr.getType(), retval));
		
		return retTemp;
	}
	
	private LLOperand generateShortCircuitOr(IAstBinExpr expr) throws ASTException {
		IBinaryOperation op = expr.getOp();
		assert op == IOperation.COMPOR;
		
		IScope scope = expr.getOwnerScope();
		
		// get result holder
		ISymbol boolResultSym = scope.addTemporary("or");
		LLOperand retval = new LLSymbolOp(boolResultSym);
		currentTarget.emit(new LLAllocaInstr(retval, expr.getType()));

		ISymbol rhsLabel, outLabel;

		///
		
		// calculate lhs
		LLOperand left = generateTypedExpr(expr.getLeft());
		currentTarget.store(expr.getType(), left, retval);
		
		// if it was true, done
		rhsLabel = scope.addTemporary("rhsOut");
		outLabel = scope.addTemporary("andOut");
		currentTarget.emit(new LLBranchInstr(
				expr.getLeft().getType(),
				//typeEngine.LLBOOL,
				left, new LLSymbolOp(outLabel), new LLSymbolOp(rhsLabel)));
		
		//
		
		// else, see if the rhs is true
		currentTarget.addBlock(rhsLabel);
		
		LLOperand right = generateTypedExpr(expr.getRight());
		
		currentTarget.store(expr.getRight().getType(), right, retval);
		currentTarget.emit(new LLUncondBranchInstr(new LLSymbolOp(outLabel)));
		
		currentTarget.addBlock(outLabel);
			
		LLOperand retTemp = currentTarget.newTemp(expr.getType());
		currentTarget.emit(new LLLoadInstr(retTemp, expr.getType(), retval));
		
		return retTemp;
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
		
		LLCodeType funcType = (LLCodeType) expr.getFunction().getType();
		LLOperand[] ops = new LLOperand[funcType.getArgTypes().length];
		
		int idx = 0;
		for (IAstTypedExpr arg : expr.arguments().list()) {
			ops[idx++] = generateTypedExpr(arg);
		}

		LLOperand func = generateTypedExpr(expr.getFunction());

		if (!(funcType.getRetType() instanceof LLVoidType)) {
			ret = currentTarget.newTemp(funcType.getRetType());
		}

		currentTarget.emit(new LLCallInstr(ret, expr.getType(), func, funcType, ops));
		return ret;
	}

	private LLOperand generateLitExpr( IAstLitExpr expr) throws ASTException {
		Object object = expr.getObject();
		if (object instanceof Boolean)
			return new LLConstOp(Boolean.TRUE.equals(object) ? 1 : 0);
		else
			return new LLConstOp((Number) object);
			
	}

	/**
	 * @return
	 */
	public LLModule getModule() {
		return ll;
	}

}
