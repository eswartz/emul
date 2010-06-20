/**
 * 
 */
package org.ejs.eulang.llvm.parser;

import java.util.List;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.impl.AstType;
import org.ejs.eulang.llvm.*;
import org.ejs.eulang.llvm.directives.*;
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
	public LocalScope typeScope;
	
	/**
	 * 
	 */
	public LLParserHelper(LLModule module) {
		this.module = module;
		this.typeEngine = module.getTarget().getTypeEngine();
		this.typeScope = new LocalScope(module.getModuleScope());
	}
	
	public ISymbol findSymbol(String name) {
		boolean isLocal = name.startsWith("%");
		name = name.substring(1);
		
		if (inTypeContext > 0 && isLocal) {
			ISymbol typeSym = typeScope.search(name);
			if (typeSym != null)
				return typeSym;
		} 
		return isLocal && currentTarget != null ? currentTarget.getScope().search(name)
					: module.getModuleScope().search(name);
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
			symbol = module.getModuleScope().add(name, false);
			symbol.setType(type);
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
	
	public LLType[] getTypeList(List types) {
		LLType[] subTypes = new LLType[types.size()];
		int i = 0;
		for (Object o : types) {
			subTypes[i++] = ((LLVMParser.type_return) o).theType;
		}
		return subTypes;
	}
	public LLTupleType addTupleType(List types) {
		return typeEngine.getTupleType(getTypeList(types));
	}
	
	public LLCodeType addCodeType(LLType retType, List types) {
		return typeEngine.getCodeType(retType, getTypeList(types));
	}
	
	public LLArrayType addArrayType(int count, LLType base) {
		return typeEngine.getArrayType(base, count, null);
	}
	
	public LLType findOrForwardNameType(String id) {
		ISymbol sym = findSymbol(id);
		if (sym == null) {
			sym = typeScope.add(id, false);
			return typeEngine.getNamedType(sym);
		}
		return ((IAstType) sym.getDefinition()).getType();
	}
	
	public String unescape(String token, char term) {
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
}
