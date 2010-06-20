/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLGlobalDirective;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLStringLitOp;
import org.ejs.eulang.llvm.ops.LLStructOp;
import org.ejs.eulang.llvm.ops.LLZeroInitOp;
import org.ejs.eulang.llvm.parser.LLParserHelper;
import org.ejs.eulang.llvm.parser.LLVMLexer;
import org.ejs.eulang.llvm.parser.LLVMParser;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLTupleType;
import org.ejs.eulang.types.LLType;
import org.junit.Test;
/**
 * @author ejs
 *
 */
public class TestLLVMParser extends BaseTest {
	protected LLModule doLLVMParse(String text) throws Exception {
		return doLLVMParse(text, false);
	}

	protected LLModule doLLVMParse(String text, boolean expectError) throws Exception {
		GlobalScope globalScope = new GlobalScope();
		LLModule mod = new LLModule(typeEngine, v9t9Target, globalScope);
		LLParserHelper helper = new LLParserHelper(mod);
		doLLVMParse(helper, text, expectError);
		
		// finalize types
		boolean changed = false;
		do {
			for (LLType type : typeEngine.getTypes()) {
				if (type instanceof LLSymbolType) {
					LLType real = typeEngine.getRealType(type);
					if (real != null && real != type) {
						changed = true;
					}
				}
			}
		} while (changed);
		
		for (ISymbol sym : mod.getTypeScope()) {
			if (!(sym.getType() != null && sym.getType().isComplete()))
				fail(sym+": type");	
		}
		
		return mod;
	}
	
	/**
	 * @param text
	 * @return
	 */
	protected ParserRuleReturnScope doLLVMParse(LLParserHelper helper, String str, boolean expectError) throws RecognitionException {
		System.err.flush();
		System.out.flush();
		final StringBuilder errors = new StringBuilder();
		try {
	    	// create a CharStream that reads from standard input
	        LLVMLexer lexer = new LLVMLexer(new ANTLRStringStream(str)) {
	        	/* (non-Javadoc)
	        	 * @see org.antlr.runtime.BaseRecognizer#emitErrorMessage(java.lang.String)
	        	 */
	        	@Override
	        	public void emitErrorMessage(String msg) {
	        		errors.append( msg +"\n");
	        	}
	        };
	        
	        // create a buffer of tokens pulled from the lexer
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        // create a parser that feeds off the tokens buffer
	        LLVMParser parser = new LLVMParser(tokens, helper);
	        // begin parsing at rule
	        ParserRuleReturnScope prog = null;
	        prog = parser.prog();
	        if (dumpTreeize)
	        	System.out.println("\n"+str);
	        
	        if (!expectError) {
				if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
					System.err.println(errors);
					fail(errors.toString());
				}
			} else {
				assertTrue(parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0);
			}
	        
	        if (dumpTreeize && prog != null && prog.getTree() != null)
	        	System.out.println(((Tree) prog.getTree()).toStringTree());
	
	        if (!expectError)
	        	assertTrue("did not consume all input", tokens.index() >= tokens.size());
	
	        return prog;
		} finally {
			System.err.flush();
			System.out.flush();
			
		}
	}
	
	@Test
	public void testTargetDirectives() throws Exception {
		String text = "target datalayout = \"E-p:16:16-s0:8:16-a0:8:16\"\n" + 
				"\n" + 
				"target triple = \"9900-unknown-v9t9\"\n" + 
				"\n" + 
				"";
		
		doLLVMParse(text);
	}

	@Test
	public void testTypesInt() throws Exception {
		String text = 
			"%Int = type i16\n"+
			"%Byte = type i8\n"+
				"";
		
		LLModule mod = doLLVMParse(text);
		assertEquals(2, mod.getTypeScope().getSymbols().length);
		ISymbol intSym = mod.getTypeScope().search("Int");
		assertNotNull(intSym);
		assertEquals(typeEngine.getIntType(16), intSym.getType());
		ISymbol byteSym = mod.getTypeScope().search("Byte");
		assertNotNull(byteSym);
		assertEquals(typeEngine.getIntType(8), byteSym.getType());
		
		// making sure we're not seeing default types
		assertNull(mod.getModuleScope().get("Bool"));
	}
	@Test
	public void testTypesArray() throws Exception {
		String text = 
			"%Charx5 = type [ 5 x i8 ]\n"+
				"";
		
		LLModule mod = doLLVMParse(text);
		assertEquals(1, mod.getTypeScope().getSymbols().length);
		ISymbol charx5 = mod.getTypeScope().search("Charx5");
		assertNotNull(charx5);
		assertEquals(typeEngine.getArrayType(typeEngine.BYTE, 5, null), charx5.getType());
	}
	@Test
	public void testTypesPointer() throws Exception {
		String text = 
			"%Charx5 = type [ 5 x i8 ]\n"+
			"%Charx5$p = type %Charx5*\n"+
				"";
		
		LLModule mod = doLLVMParse(text);
		ISymbol charx5p = mod.getTypeScope().search("Charx5$p");
		assertNotNull(charx5p);
		assertEquals(typeEngine.getPointerType(typeEngine.getArrayType(typeEngine.getIntType(8), 5, null)),
				charx5p.getType());
	}
	@Test
	public void testTypesTuple() throws Exception {
		String text = 
			"%foo = type { i8, i8*, [ 5 x i8 ]}\n"+
				"";
		
		LLModule mod = doLLVMParse(text);
		ISymbol fooSym = mod.getTypeScope().search("foo");
		assertNotNull(fooSym);
		LLType fooType = fooSym.getType();
		assertTrue(fooType instanceof LLDataType);
		assertEquals(typeEngine.BYTE, fooType.getType(0));
		assertEquals(typeEngine.getPointerType(typeEngine.getIntType(8)), fooType.getType(1)); 
		assertEquals(typeEngine.getArrayType(typeEngine.getIntType(8), 5, null), fooType.getType(2));
	}
	@Test
	public void testTypesFwd() throws Exception {
		String text = 
			"%Int = type i16\n"+
			"%Byte = type i8\n"+
			"%foo = type { i8, %func*, [ 5 x i8 ]}\n"+
			"%func = type %Byte (%Int*, %Int)\n"+
				"";
		
		LLModule mod = doLLVMParse(text);
		ISymbol funcSym = mod.getTypeScope().search("func");
		assertNotNull(funcSym);
		assertEquals(typeEngine.getCodeType(typeEngine.getIntType(8),
				new LLType[] {
				typeEngine.getPointerType(typeEngine.getIntType(16)),
					typeEngine.getIntType(16)
				}),
				funcSym.getType());
	}
	@Test
	public void testTypesLLVMOpt() throws Exception {
		String text = 
			"%Class = type { i16, i16, %\"Int$Class$p_$p\", %\"Int$Class$p$Int_$p\" }\n" + 
			"%\"Class$p\" = type %Class*\n" + 
			"%\"Int$Class$p$Int_\" = type i16 (%\"Class$p\", i16)\n" + 
			"%\"Int$Class$p$Int_$p\" = type %\"Int$Class$p$Int_\"*\n" + 
			"%\"Int$Class$p$Int_$p$p\" = type %\"Int$Class$p$Int_$p\"*\n" + 
			"%\"Int$Class$p_\" = type i16 (%\"Class$p\")\n" + 
			"%\"Int$Class$p_$p\" = type %\"Int$Class$p_\"*\n" + 
			"%\"Int$Class$p_$p$p\" = type %\"Int$Class$p_$p\"*\n" + 
			"%\"Int$_\" = type i16 ()\n" + 
			"%\"Int$_$p\" = type %\"Int$_\"*\n" + 
			"%\"void$Class$p_\" = type void (%\"Class$p\")\n" + 
			"";
		
		LLModule mod = doLLVMParse(text);
		ISymbol sym = mod.getTypeScope().search("void$Class$p_");
		assertNotNull(sym);
		assertTrue(sym.getType() instanceof LLCodeType);
		
	}
	
	@Test
	public void testGlobalData1() throws Exception {
		String text = 
			"\n" + 
			"%Int = type i16\n" + 
			"%void._.Int.Int_ = type void (%Int,%Int)\n" + 
			"%i16$p = type i16*\n" + 
			"\n" + 
			"@bat._.Int = global %Int 10";
		
		LLModule mod = doLLVMParse(text);
		assertEquals(1, mod.getDirectives().size());
		LLGlobalDirective gd = (LLGlobalDirective) mod.getDirectives().get(0);
		assertTrue(gd.getInit() instanceof LLConstOp);
		assertEquals(typeEngine.INT, gd.getInit().getType());
		
		assertEquals("bat._.Int", gd.getSymbol().getName());
		assertEquals(mod.getModuleScope(), gd.getSymbol().getScope());
	}
	@Test
	public void testGlobalData2() throws Exception {
		String text = 
			"\n" + 
			"%Str$16 = type {i16,[ 16 x i8 ]}\n" + 
			"%Int = type i16\n" + 
			"%Charx16 = type [ 16 x i8 ]\n" + 
			"%Char = type i8\n" + 
			"%Str$5._._ = type %Str$5 ()\n" + 
			"%Str$5 = type {i16,[ 5 x i8 ]}\n" + 
			"%Charx5 = type [ 5 x i8 ]\n" + 
			"%Str$5$p = type %Str$5*\n" + 
			"\n" + 
			"@a._.Str$16 = global %Str$16 { %Int 16, %Charx16 c\"SUCKA!!! \\0d\\0a\\09\\ff\\7f\\00\\02\" }\n"+
			"";
		
		LLModule mod = doLLVMParse(text);
		assertEquals(1, mod.getDirectives().size());
		LLGlobalDirective gd = (LLGlobalDirective) mod.getDirectives().get(0);
		assertTrue(gd.getInit() instanceof LLStructOp);
		LLStructOp sop = (LLStructOp) gd.getInit();
		assertEquals(2, sop.getElements().length);
		LLDataType type = (LLDataType) sop.getType();
		assertEquals(typeEngine.INT, type.getType(0));
		assertEquals(typeEngine.getArrayType(typeEngine.getIntType(8), 16, null), type.getType(1));
		
		assertTrue(sop.getElements()[1] instanceof LLStringLitOp) ;
		assertEquals("SUCKA!!! \r\n\t\u00ff\u007f\u0000\u0002", ((LLStringLitOp)sop.getElements()[1]).getText());
	}
	
	@Test
	public void testGlobalData3() throws Exception {
		String text =
			"%Tuple = type {i8,i8,i16}\n" + 
			"%Byte = type i8\n" + 
			"%Int = type i16\n" + 
			"%Tuple$p = type %Tuple*\n" + 
			"%void._.Tuple$p_ = type void (%Tuple$p)\n" + 
			"%Tuple$p$p = type %Tuple$p*\n" + 
			"%Byte$p = type %Byte*\n" + 
			"%i16$p = type i16*\n" + 
			"%Tuple._.Int.Int.Int_ = type %Tuple (%Int,%Int,%Int)\n" + 
			"\n" + 
			"@t._.Tuple = global %Tuple zeroinitializer\n" + 
			"";
		
		LLModule mod = doLLVMParse(text);
		assertEquals(1, mod.getDirectives().size());
		LLGlobalDirective gd = (LLGlobalDirective) mod.getDirectives().get(0);
		assertTrue(gd.getInit() instanceof LLZeroInitOp);
		LLDataType type = (LLDataType) gd.getInit().getType();
		LLType i8 = typeEngine.getIntType(8);
		LLType i16 = typeEngine.getIntType(16);
		assertEquals(i8, type.getType(0));
		assertEquals(i8, type.getType(1));
		assertEquals(i16, type.getType(2));
		
	}
	
	@Test
	public void testGlobalData4() throws Exception {
		String text =
			"target datalayout = \"E-p:16:16-s0:8:16-a0:8:16\"\n" + 
			"\n" + 
			"target triple = \"9900-unknown-v9t9\"\n" + 
			"\n" + 
			"%Class = type {i16,i16,%Int._.Class$p_*}\n" + 
			"%Int = type i16\n" + 
			"%Int._.Class$p_$p = type %Int._.Class$p_*\n" + 
			"%Int._.Class$p_ = type %Int (%Class$p)\n" + 
			"%Class$p = type %Class*\n" + 
			"%void._.Class$p_ = type void (%Class$p)\n" + 
			"%Class$p$p = type %Class$p*\n" + 
			"%i16$p = type i16*\n" + 
			"%Int._.Class$p_$p$p = type %Int._.Class$p_$p*\n" + 
			"%void._._$p = type %void._._*\n" + 
			"%void._._ = type void ()\n" + 
			"%.global_ctor_entryx1 = type [ 1 x {%void._._*} ]\n" + 
			"%.global_ctor_entry = type {%void._._*}\n" + 
			"@x._.Class = global %Class zeroinitializer\n" + 
			"@llvm.global_ctors = appending global %.global_ctor_entryx1 [ %.global_ctor_entry { %void._._$p @.global_ctors._.void._._$p } ]\n" + 
			"@.global_ctors._.void._._$p = global %Int 10\n" +	// a LIE! 
			"@y._.Class = global %Class zeroinitializer\n" + 
			"" + 
			"";
		
		LLModule mod = doLLVMParse(text);
		assertEquals(4, mod.getDirectives().size());
		
		LLGlobalDirective globalDir = (LLGlobalDirective) mod.getDirectives().get(1);
		assertEquals(LLLinkage.APPENDING, globalDir.getLinkage());
		LLArrayType type = (LLArrayType) globalDir.getInit().getType();
		assertEquals(type, globalDir.getSymbol().getType());
		
		LLDataType klass = (LLDataType) typeEngine.getNamedType(mod.getTypeScope().search("Class"));
		assertEquals(48, klass.getBits());
	}
}
