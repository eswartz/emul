/*
 * 
 */
package org.ejs.eulang.llvm.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.llvm.*;
import org.ejs.eulang.llvm.directives.*;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalScope;
import org.ejs.eulang.types.*;

/**
 * @author ejs
 *
 */
public class LLParserHelper {

	private static final String HEXDIGITS = "0123456789ABCDEF";
	public final LLModule module;
	public ILLCodeTarget currentTarget;
	public TypeEngine typeEngine;
	public int inTypeContext;
	public IScope typeScope;
	
	private Map<ISymbol, LLSymbolType> fwdTypes = new HashMap<ISymbol, LLSymbolType>();
	private Map<String, LLSymbolOp> fwdSymOps = new HashMap<String, LLSymbolOp>();
	
	/**
	 * 
	 */
	public LLParserHelper(LLModule module) {
		this.module = module;
		this.typeEngine = module.getTarget().getTypeEngine();
		this.typeScope = module.getTypeScope();
	}
	
	public LLSymbolOp getSymbolOp(String nameWithPrefix, ISymbol sym) {
		LLSymbolOp symOp;
		if (sym == null) {
			sym = findSymbol(nameWithPrefix);
		}
		if (sym == null) {
			symOp = fwdSymOps.get(nameWithPrefix);
			if (symOp == null) {
				symOp = new LLSymbolOp(null);
				System.out.println("Forward symbol op for: " + nameWithPrefix);
				fwdSymOps.put(nameWithPrefix, symOp);
			}
		} else {
			symOp = new LLSymbolOp(sym);
		}
		return symOp;
	}
	public ISymbol findSymbol(String nameWithPrefix) {
		boolean isLocal = nameWithPrefix.startsWith("%");
		String name = nameWithPrefix.substring(1);
		
		if (inTypeContext > 0 && isLocal) {
			ISymbol typeSym = typeScope.search(name);
			if (typeSym != null)
				return typeSym;
			typeSym = module.getGlobalScope().get(name);
			if (typeSym != null && typeEngine.getNamedType(typeSym) != null)
				return typeSym;
		} 
		ISymbol theSym = isLocal && currentTarget != null ? currentTarget.getScope().search(name)
					: module.getModuleScope().search(name);
		return theSym;
	}
	public ISymbol defineSymbol(String nameWithPrefix, LLType type) {
		boolean isLocal = nameWithPrefix.startsWith("%");
		String name = nameWithPrefix.substring(1);
		
		ISymbol sym = isLocal && currentTarget != null ? currentTarget.getScope().add(name, false)
				: module.getModuleScope().add(name, false);
		sym.setType(type);
		
		LLSymbolOp fwd = fwdSymOps.get(nameWithPrefix);
		if (fwd != null) {
			System.out.println("Defined forward symbol " + nameWithPrefix);
			fwdSymOps.remove(nameWithPrefix);
			fwd.setSymbol(sym);
			fwd.setType(sym.getType());
		}
		return sym;
	}
	public ISymbol defineSymbol(String nameWithPrefix) {
		return defineSymbol(nameWithPrefix, null);
	}
	public void addNewType(String name, LLType type) {
		assert name.startsWith("%");
		name = name.substring(1);
		ISymbol symbol;
		try {
			Integer.parseInt(name);
			// it's temporary
			symbol = typeScope.add(name, false);
			symbol.setType(type);
		} catch (NumberFormatException e) {
			symbol = module.getTypeScope().search(name);
			if (symbol != null) {
				assert symbol.getType() == null;
			} else {
				symbol = module.getTypeScope().add(name, false);
				if (type instanceof LLTupleType) {
					type = typeEngine.getDataType(symbol, Arrays.asList(((LLTupleType)type).getTypes()));
				}
			}
			symbol.setType(type);
		}
		
		LLSymbolType fwd = fwdTypes.get(symbol);
		if (fwd != null) {
			System.out.println("Defined forward type " + fwd.getSymbol());
			fwd.getSymbol().setType(type);
			fwdTypes.remove(symbol);
		}
		
		symbol.setDefinition(new AstType(type));
		module.addExternType(type);
		typeEngine.register(type);
	}
	
	public void addTargetDataLayoutDirective(String desc) {
		module.addModuleDirective(new LLTargetDataTypeDirective(typeEngine, desc));
	}
	
	public void addTargetTripleDirective(String desc) {
		assert module.getTarget().getTriple().equals(desc);
		module.addModuleDirective(new LLTargetTripleDirective(module.getTarget()));
	}
	
	public LLIntType addIntType(String intType) {
		int bits = Integer.parseInt(intType.substring(1));
		return typeEngine.getIntType(bits);
	}
	
	public LLPointerType addPointerType(LLType type) {
		return typeEngine.getPointerType(type);
	}
	
	public LLTupleType addTupleType(LLType[] types) {
		return typeEngine.getTupleType(types);
	}
	
	public LLCodeType addCodeType(LLType retType, LLType[] types) {
		return typeEngine.getCodeType(retType, types);
	}
	
	public LLArrayType addArrayType(int count, LLType base) {
		return typeEngine.getArrayType(base, count, null);
	}
	
	public LLType findOrForwardNameType(String idWithPrefix) {
		boolean isLocal = idWithPrefix.startsWith("%");
		
		ISymbol sym = findSymbol(idWithPrefix);
		String id = idWithPrefix.substring(1);
		if (sym == null) {
			sym = typeScope.add(id, false);
		}
		
		LLType realType = typeEngine.getRealType(sym.getType());
		if (realType == null) {
			LLSymbolType symType = fwdTypes.get(sym);
			if (symType == null) {
				System.out.println("Forward type for: " + sym);
				symType = new LLSymbolType(sym);
				fwdTypes.put(sym, symType);
			}
			realType = symType;
		}
		return realType;
	}
	
	public static String unescape(String token, char term) {
		StringBuilder sb = new StringBuilder();
		// ignore surrounding quotes
		for (int i = 1; i < token.length() - 1; ) {
			char ch = token.charAt(i++);
			if (ch == '\\') {
				ch = token.charAt(i++);
				if (ch == term) {
					/* same */
				} else {
					switch (ch) {
					case '\\':	/* same */ break;
					case 'n': ch = '\n'; break;
					case 'r': ch = '\r'; break;
					case 't': ch = '\t'; break;
					default: {
						int v = ((HEXDIGITS.indexOf(Character.toUpperCase(ch)) << 4)
							| (HEXDIGITS.indexOf(Character.toUpperCase(token.charAt(i++)))));
						assert (v & 0xff) == v;		// i.e., no '-1'
						ch = (char) v;
					}
					}
				}
			} 
			sb.append(ch);
		}
		return sb.toString();
	}

	/**
	 * @param string
	 * @param llOperand
	 */
	public void addGlobalDataDirective(String string, LLLinkage linkage, LLOperand llOperand) {
		ISymbol sym = defineSymbol(string, llOperand.getType());
		module.add(new LLGlobalDirective(sym, 
				linkage,
				llOperand));
	}
	public void addConstantDirective(String string, int addrSpace, LLOperand llOperand) {
		ISymbol sym = defineSymbol(string, llOperand.getType());
		LLConstantDirective directive = new LLConstantDirective(sym, true, llOperand);
		directive.setAddrSpace(addrSpace);
		module.add(directive);
	}
	
	public ISymbol addLabel(String id) {
		
		ISymbol label = defineSymbol("%" + id, typeEngine.LABEL);
		label.setType(typeEngine.LABEL);
		return label;
	}

	/**
	 * @param string
	 * @param llLinkage
	 * @param llVisibility
	 * @param string2
	 * @param llAttrType
	 * @param llArgAttrTypes
	 * @param llFuncAttrs
	 * @param object
	 * @param i
	 * @param object2
	 */
	public void openNewDefine(String theId, LLLinkage llLinkage,
			LLVisibility llVisibility, String cconv, LLAttrType llAttrType,
			LLArgAttrType[] llArgAttrTypes, LLFuncAttrs llFuncAttrs,
			String section, int align, String gc) {
		LLType[] argTypes = new LLType[llArgAttrTypes.length];
		for (int i = 0; i <argTypes.length; i++)
			argTypes[i] = llArgAttrTypes[i].getType();
		LLCodeType codeType = typeEngine.getCodeType(llAttrType.getType(), argTypes);
		ISymbol defSym = defineSymbol(theId, codeType);
		
	    LLDefineDirective def = new LLDefineDirective(null, module.getTarget(),
	        module, new LocalScope(module.getModuleScope()),
	        defSym, llLinkage, llVisibility, cconv, 
	        llAttrType,
	        llArgAttrTypes, llFuncAttrs,
	        section, align, gc);
	    currentTarget = def;
	    
	    for (LLArgAttrType argAttr : llArgAttrTypes) {
	    	ISymbol arg = def.getScope().add(argAttr.getName(), false);
	    	arg.setType(argAttr.getType());
	    }
	}

	/**
	 * 
	 */
	public void closeDefine() {
		module.add((LLDefineDirective) currentTarget);
		currentTarget = null;
	}

	/**
	 * @return the fwdTypes
	 */
	public Map<ISymbol, LLSymbolType> getForwardTypes() {
		return fwdTypes;
	}
	/**
	 * @return the fwdSymOps
	 */
	public Map<String, LLSymbolOp> getForwardSymbols() {
		return fwdSymOps;
	}

	/**
	 * @param ops
	 * @return
	 */
	public LLType getElementPtrType(List<LLOperand> ops) {
		int idx = 0;
		LLType type = ops.get(idx++).getType();
		LLType retType = type;
		while (idx < ops.size()) {
			if (idx > 1) {
				int val = ((LLConstOp) ops.get(idx)).getValue().intValue();
				if (type instanceof LLAggregateType)
					type = ((LLAggregateType) type).getType(val);
				else
					type = type.getSubType();
			} else {
				type = type.getSubType();
			}
			
			retType = typeEngine.getPointerType(type);
			idx++;
			
		}
		return retType;
	}
}
