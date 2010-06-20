/*
 * 
 */
package org.ejs.eulang.llvm.parser;

import java.util.HashMap;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.llvm.*;
import org.ejs.eulang.llvm.directives.*;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
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
	public ISymbol defineSymbol(String nameWithPrefix) {
		boolean isLocal = nameWithPrefix.startsWith("%");
		String name = nameWithPrefix.substring(1);
		
		ISymbol sym = isLocal && currentTarget != null ? currentTarget.getScope().add(name, false)
				: module.getModuleScope().add(name, false);
		LLSymbolOp fwd = fwdSymOps.get(nameWithPrefix);
		if (fwd != null) {
			fwd.setSymbol(sym);
		}
		return sym;
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
		return typeEngine.getIntType(intType, bits);
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
		ISymbol sym = defineSymbol(string);
		sym.setType(llOperand.getType());
		module.add(new LLGlobalDirective(sym, 
				linkage,
				llOperand));
	}
}
