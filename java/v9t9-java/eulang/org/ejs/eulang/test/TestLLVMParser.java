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
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLConstantDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLGlobalDirective;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.instrs.LLTypedInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.ops.LLStringLitOp;
import org.ejs.eulang.llvm.ops.LLStructOp;
import org.ejs.eulang.llvm.ops.LLZeroInitOp;
import org.ejs.eulang.llvm.parser.LLParserHelper;
import org.ejs.eulang.llvm.parser.LLVMLexer;
import org.ejs.eulang.llvm.parser.LLVMParser;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLSymbolType;
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
		
		assertTrue(helper.getForwardTypes().isEmpty());
		StringBuilder ssb = new StringBuilder();
		for (String name : helper.getForwardSymbols().keySet()) {
			ssb.append(name).append(' ');
		}
		if (ssb.length() > 0)
			fail("Undefined symbols: " + ssb.toString());
		
		mod.accept(new LLCodeVisitor() {
			/* (non-Javadoc)
			 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterInstr(org.ejs.eulang.llvm.LLBlock, org.ejs.eulang.llvm.instrs.LLInstr)
			 */
			@Override
			public boolean enterInstr(LLBlock block, LLInstr instr) {
				if (instr instanceof LLTypedInstr) {
					LLType typ = ((LLTypedInstr) instr).getType();
					assertTrue(instr+":"+typ, typ != null && typ.isComplete());
				}
				return true;
			}
			/* (non-Javadoc)
			 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterOperand(org.ejs.eulang.llvm.instrs.LLInstr, int, org.ejs.eulang.llvm.ops.LLOperand)
			 */
			@Override
			public boolean enterOperand(LLInstr instr, int num,
					LLOperand operand) {
				assertTrue(instr+":"+num+":"+operand, operand != null && operand.getType()!= null && operand.getType().isComplete());
				return false;
			}
		});
		
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
		
		// make sure types are sensible
		assertEquals("%Int", typeEngine.getIntType(16).getLLVMName());
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
		

		assertEquals("%Charx5", charx5.getLLVMName());
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
			"@\"a._.Str$16\" = global %Str$16 { %Int 16, %Charx16 c\"SUCKA!!! \\0d\\0a\\09\\ff\\7f\\00\\02\" }\n"+
			"";
		
		LLModule mod = doLLVMParse(text);
		assertEquals(1, mod.getDirectives().size());
		LLGlobalDirective gd = (LLGlobalDirective) mod.getDirectives().get(0);
		
		assertEquals("a._.Str$16", gd.getSymbol().getName());
		
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
	
	@Test
	public void testConstants1() throws Exception {
		String text = "%Charx0 = type [0 x i8]\n" + 
				"%Charx3 = type [3 x i8]\n" + 
				"%Int._.Int_ = type i16 (i16)\n" + 
				"%\"Int._.Node$p.Int_\" = type i16 (%\"Node$p\", i16)\n" + 
				"%Int._._ = type i16 ()\n" + 
				"%Node = type { i16 }\n" + 
				"%\"Node$p\" = type %Node*\n" + 
				"%Node2 = type { i16, %Str }\n" + 
				"%\"Node2$p\" = type %Node2*\n" + 
				"%Str = type { i16, %Charx0 }\n" + 
				"%\"Str$3\" = type { i16, %Charx3 }\n" + 
				"%\"Str$3$p\" = type %\"Str$3\"*\n" + 
				"%\"Str$p\" = type %Str*\n" + 
				"%\"_Int.Str_._.Node2$p.Int.Str$p_\" = type %Node2 (%\"Node2$p\", i16, %\"Str$p\")\n" + 
				"%\"void._.Node$p_\" = type void (%\"Node$p\")\n" + 
				"%\"void._.Node2$p_\" = type void (%\"Node2$p\")\n" + 
				"\n" + 
				"@brk._.Int = global i16 -24576                    ; <i16*> [#uses=6]\n" + 
				"@\".const._.Str$3\" = constant %\"Str$3\" { i16 3, %Charx3 c\"foo\" } ; <%\"Str$3$p\"> [#uses=1]\n" + 
				"" +
			"";
		
		LLModule mod = doLLVMParse(text);
		
		LLGlobalDirective gd = (LLGlobalDirective) mod.getDirectives().get(0);
		assertTrue(gd.getInit() instanceof LLConstOp);
		assertEquals(typeEngine.INT, gd.getInit().getType());
		assertEquals(-24576, ((LLConstOp)gd.getInit()).getValue());
		
		LLConstantDirective cd = (LLConstantDirective) mod.getDirectives().get(1);
		assertNotNull(cd.getConstant().getType());
		assertEquals(".const._.Str$3", cd.getSymbol().getName());
		assertEquals(cd.getSymbol().getType(), cd.getConstant().getType());
		
		System.out.println(cd);

		// make sure the output matches the input
		LLDataType str3 = (LLDataType) cd.getConstant().getType();
		assertEquals(typeEngine.INT, str3.getType(0));
		LLOperand el0 = ((LLStructOp)cd.getConstant()).getElements()[0];
		assertEquals("%Int", el0.getType().getLLVMName());
	}
	
	@Test
	public void testDefines1() throws Exception {
		String text =
				"%i16$p._.Int_ = type %i16$p (%Int)\n" + 
				"%i16$p = type i16*\n" + 
				"%Int = type i16\n" + 
				"%i16$p._.Int_$p = type %i16$p._.Int_*\n" + 
				"\n" + 
				"define default %i16$p @defaultNew._.i16$p._.Int_(%Int %x)  optsize \n" + 
				"{\n" + 
				"entry.16:\n" + 
				"%_.x.17 =       alloca %Int \n" + 
				"        store %Int %x, %Int* %_.x.17\n" +
				"		br label %next\n"+
				
				"next:\n"+
				"		br i1 %x, label %entry.16, label %last\n"+
				"last:\n"+
				"        ret %i16$p inttoptr (i16 0 to %i16$p)\n" + 
				//"        ret i16 100\n" + 
				"}\n" + 
				//"\n" + 
				//"@new._.i16$p._.Int_$p = global %i16$p._.Int_$p @defaultNew._.i16$p._.Int_\n" + 
				"" +
			"";
		
		LLModule mod = doLLVMParse(text);
		System.out.println(mod);
		
		ISymbol sym = mod.getModuleScope().get("defaultNew._.i16$p._.Int_");
		LLDefineDirective def = mod.getDefineDirective(sym);
		assertNotNull(def);
		ISymbol x;
		x = def.getScope().get("x");
		assertEquals(typeEngine.INT, x.getType());
		x = def.getScope().get("_.x.17");
		assertEquals(typeEngine.getPointerType(typeEngine.INT), x.getType());
		
		String[] lines = mod.toString().trim().split("\n");
		assertEquals("br i1 %x, label %entry.16, label %last", lines[lines.length - 4].trim()); 
	}

	@Test
	public void testDefines2() throws Exception {
		String text =
				"%Int._.Int_ = type %Int (%Int)\n" + 
				"%Int = type i16\n" + 
				"%Bool = type i1\n" + 
				"%i16$p = type i16*\n" + 
				"%__label = type label\n" + 
				"\n" + 
				"define default %Int @testWhile._.Int._.Int_(%Int %x)  optsize \n" + 
				"{\n" + 
				"entry.16:\n" + 
				"%_.x.17 =       alloca %Int \n" + 
				"        store %Int %x, %Int* %_.x.17\n" + 
				"%_.s.19 =       alloca %Int \n" + 
				"        store %Int 0, %Int* %_.s.19\n" + 
				"%_.b.20 =       alloca %Int \n" + 
				"        store %Int 1, %Int* %_.b.20\n" + 
				"%_.loopValue.22 =       alloca %Int \n" + 
				"        store %Int 0, %Int* %_.loopValue.22\n" + 
				"        br label %loopEnter.23\n" + 
				"loopEnter.23:\n" + 
				"%0 =    load %Int* %_.b.20\n" + 
				"%1 =    load %Int* %_.x.17\n" + 
				"%2 =    icmp slt %Int %0, %1\n" + 
				"        br %Bool %2, label %loopBody.24, label %loopExit.25\n" + 
				"loopBody.24:\n" + 
				"%3 =    load %Int* %_.s.19\n" + 
				"%4 =    load %Int* %_.b.20\n" + 
				"%5 =    add nsw nuw %Int %3, %4\n" + 
				"%6 =    load %Int* %_.b.20\n" + 
				"%7 =    add %Int %6, 1\n" + 
				"        store %Int %5, %Int* %_.s.19\n" + 
				"        store %Int %7, %Int* %_.b.20\n" + 
				"%8 =    load %Int* %_.s.19\n" + 
				"        store %Int %8, %Int* %_.loopValue.22\n" + 
				"        br label %loopEnter.23\n" + 
				"loopExit.25:\n" + 
				"%9 =    load %Int* %_.loopValue.22\n" + 
				"        ret %Int %9\n" + 
				"}\n" + 
				"\n" + 
				""+
			"";
		
		LLModule mod = doLLVMParse(text);
		System.out.println(mod);
	}
	

	@Test
	public void testDefines3() throws Exception {
		String text =
			//1
				"; ModuleID = '/tmp/testDataVirtualMethod3b.opt.bc'\n" + 
				"target datalayout = \"E-p:16:16-s0:8:16-a0:8:16\"\n" + 
				"target triple = \"9900-unknown-v9t9\"\n" + 
				"\n" + 
				"%Class = type { i16, i16, %\"Int._.Class$p_$p\", %\"Int._.Class$p.Int_$p\" }\n" + 
				"%\"Class$p\" = type %Class*\n" + 
				"%\"Int._.Class$p.Int_\" = type i16 (%\"Class$p\", i16)\n" + 
				"%\"Int._.Class$p.Int_$p\" = type %\"Int._.Class$p.Int_\"*\n" + 
				"%\"Int._.Class$p.Int_$p$p\" = type %\"Int._.Class$p.Int_$p\"*\n" +
				//10
				"%\"Int._.Class$p_\" = type i16 (%\"Class$p\")\n" + 
				"%\"Int._.Class$p_$p\" = type %\"Int._.Class$p_\"*\n" + 
				"%\"Int._.Class$p_$p$p\" = type %\"Int._.Class$p_$p\"*\n" + 
				"%Int._._ = type i16 ()\n" + 
				"%\"Int._._$p\" = type %Int._._*\n" + 
				"%\"void._.Class$p_\" = type void (%\"Class$p\")\n" + 
				"\n" +
				//17
				"define void @\"Class.$__init__$._.void._.Class$p_\"(%\"Class$p\" nocapture %this) nounwind optsize {\n" + 
				"entry.36:\n" + 
				//19
				"  %0 = getelementptr %\"Class$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
				"  store i16 3, i16* %0\n" + 
				"  %1 = getelementptr %\"Class$p\" %this, i16 0, i32 1 ; <i16*> [#uses=1]\n" + 
				"  store i16 5, i16* %1\n" + 
				"  %2 = getelementptr %\"Class$p\" %this, i16 0, i32 2 ; <%\"Int._.Class$p_$p$p\"> [#uses=1]\n" + 
				"  store %\"Int._.Class$p_$p\" @\".Class.$__init__$._.void._.Class$p_$inner._.Int._.Class$p_\", %\"Int._.Class$p_$p$p\" %2\n" + 
				"  %3 = getelementptr %\"Class$p\" %this, i16 0, i32 3 ; <%\"Int._.Class$p.Int_$p$p\"> [#uses=1]\n" + 
				"  store %\"Int._.Class$p.Int_$p\" @\".Class.$__init__$._.void._.Class$p_$inner._.Int._.Class$p.Int_\", %\"Int._.Class$p.Int_$p$p\" %3\n" + 
				"  ret void\n" + 
				"}\n" + 
				"\n" + 
				"define i16 @\".Class.$__init__$._.void._.Class$p_$inner._.Int._.Class$p_\"(%\"Class$p\" nocapture %this) nounwind readonly optsize {\n" + 
				"entry.41:\n" + 
				"  %0 = getelementptr %\"Class$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
				"  %1 = load i16* %0                               ; <i16> [#uses=1]\n" + 
				"  %2 = getelementptr %\"Class$p\" %this, i16 0, i32 1 ; <i16*> [#uses=1]\n" + 
				"  %3 = load i16* %2                               ; <i16> [#uses=1]\n" + 
				"  %4 = add i16 %3, %1                             ; <i16> [#uses=1]\n" + 
				"  ret i16 %4\n" + 
				"}\n" + 
				"\n" + 
				"define i16 @\".Class.$__init__$._.void._.Class$p_$inner._.Int._.Class$p.Int_\"(%\"Class$p\" nocapture %this, i16 %x) nounwind readonly optsize {\n" + 
				"entry.46:\n" + 
				"  %0 = getelementptr %\"Class$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
				"  %1 = load i16* %0                               ; <i16> [#uses=1]\n" + 
				"  %2 = sub i16 %1, %x                             ; <i16> [#uses=1]\n" + 
				"  ret i16 %2\n" + 
				"}\n" + 
				"\n" + 
				"define void @\"Derived.$__init__$._.void._.Derived$p_\"(%\"Class$p\" nocapture %this) nounwind optsize {\n" + 
				"entry.59:\n" + 
				"  %0 = getelementptr %\"Class$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
				"  store i16 3, i16* %0\n" + 
				"  %1 = getelementptr %\"Class$p\" %this, i16 0, i32 1 ; <i16*> [#uses=1]\n" + 
				"  store i16 100, i16* %1\n" + 
				"  %2 = getelementptr %\"Class$p\" %this, i16 0, i32 2 ; <%\"Int._.Class$p_$p$p\"> [#uses=1]\n" + 
				"  store %\"Int._.Class$p_$p\" @\".Derived.$__init__$._.void._.Derived$p_$inner._.Int._.Derived$p_\", %\"Int._.Class$p_$p$p\" %2\n" + 
				"  %3 = getelementptr %\"Class$p\" %this, i16 0, i32 3 ; <%\"Int._.Class$p.Int_$p$p\"> [#uses=1]\n" + 
				"  store %\"Int._.Class$p.Int_$p\" @\".Derived.$__init__$._.void._.Derived$p_$inner._.Int._.Class$p.Int_\", %\"Int._.Class$p.Int_$p$p\" %3\n" + 
				"  ret void\n" + 
				"}\n" + 
				"\n" + 
				"define i16 @\".Derived.$__init__$._.void._.Derived$p_$inner._.Int._.Derived$p_\"(%\"Class$p\" nocapture %this) nounwind readonly optsize {\n" + 
				"entry.64:\n" + 
				"  %0 = getelementptr %\"Class$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
				"  %1 = load i16* %0                               ; <i16> [#uses=1]\n" + 
				"  %2 = getelementptr %\"Class$p\" %this, i16 0, i32 1 ; <i16*> [#uses=1]\n" + 
				"  %3 = load i16* %2                               ; <i16> [#uses=1]\n" + 
				"  %4 = sub i16 %1, %3                             ; <i16> [#uses=1]\n" + 
				"  ret i16 %4\n" + 
				"}\n" + 
				"\n" + 
				"define i16 @\".Derived.$__init__$._.void._.Derived$p_$inner._.Int._.Class$p.Int_\"(%\"Class$p\" nocapture %this, i16 %x) nounwind readonly optsize {\n" + 
				"entry.68:\n" + 
				"  %0 = getelementptr %\"Class$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
				"  %1 = load i16* %0                               ; <i16> [#uses=1]\n" + 
				"  %2 = sub i16 %1, %x                             ; <i16> [#uses=1]\n" + 
				"  ret i16 %2\n" + 
				"}\n" + 
				"\n" + 
				"define i16 @foo._.Int._._() nounwind readnone optsize {\n" + 
				"entry.73:\n" + 
				"  ret i16 -9700\n" + 
				"}\n" + 
			"";
		
		LLModule mod = doLLVMParse(text);
		String redis = mod.toString();
		System.out.println(mod);
		
		assertMatchText("store %Int._.Class\\$p.Int_\\$p @.Derived.\\$__init__\\$._.void._.Derived\\$p_\\$inner._.Int._.Class\\$p.Int_, %Int._.Class\\$p.Int_\\$p\\* %3", redis);
		assertMatchText("getelementptr %Class\\$p %this, i32 0, i32 0", redis);
		assertMatchText("load i16\\* %0", redis);
		
	}
	
	@Test
	public void testDefines4() throws Exception {
		String text =
			//1
			"; ModuleID = '/tmp/test.opt.bc'\n" + 
			"target datalayout = \"E-p:16:16-s0:8:16-a0:8:16\"\n" + 
			"target triple = \"9900-unknown-v9t9\"\n" + 
			"\n" + 
			"%Charx0 = type [0 x i8]\n" + 
			"%Charx3 = type [3 x i8]\n" + 
			"%Int._.Int_ = type i16 (i16)\n" + 
			"%\"Int._.Node$p.Int_\" = type i16 (%\"Node$p\", i16)\n" + 
			"%Int._._ = type i16 ()\n" + 
			"%Node = type { i16 }\n" + 
			"%\"Node$p\" = type %Node*\n" + 
			"%Node2 = type { i16, %Str }\n" + 
			"%\"Node2$p\" = type %Node2*\n" + 
			"%Str = type { i16, %Charx0 }\n" + 
			"%\"Str$3\" = type { i16, %Charx3 }\n" + 
			"%\"Str$3$p\" = type %\"Str$3\"*\n" + 
			"%\"Str$p\" = type %Str*\n" + 
			"%\"_Int.Str_._.Node2$p.Int.Str$p_\" = type %Node2 (%\"Node2$p\", i16, %\"Str$p\")\n" + 
			"%\"void._.Node$p_\" = type void (%\"Node$p\")\n" + 
			"%\"void._.Node2$p_\" = type void (%\"Node2$p\")\n" + 
			"\n" + 
			"@brk._.Int = global i16 -24576                    ; <i16*> [#uses=6]\n" + 
			"@\".const._.Str$3\" = constant %\"Str$3\" { i16 3, %Charx3 c\"foo\" } ; <%\"Str$3$p\"> [#uses=1]\n" + 
			"\n" + 
			"define i16 @_new._.Int._.Int_(i16 %s) nounwind optsize {\n" + 
			"entry.36:\n" + 
			"  %0 = load i16* @brk._.Int                       ; <i16> [#uses=2]\n" + 
			"  %1 = add i16 %s, 1                              ; <i16> [#uses=1]\n" + 
			"  %2 = and i16 %1, -2                             ; <i16> [#uses=1]\n" + 
			"  %3 = add i16 %0, %2                             ; <i16> [#uses=1]\n" + 
			"  store i16 %3, i16* @brk._.Int\n" + 
			"  ret i16 %0\n" + 
			"}\n" + 
			"\n" + 
			"define i16 @\"Node.__init__._.Int._.Node$p.Int_\"(%\"Node$p\" nocapture %this, i16 %x) nounwind optsize {\n" + 
			"entry.44:\n" + 
			"  %0 = getelementptr %\"Node$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
			"  store i16 %x, i16* %0\n" + 
			"  ret i16 %x\n" + 
			"}\n" + 
			"\n" + 
			"define void @\"Node.$__init__$._.void._.Node$p_\"(%\"Node$p\" nocapture %this) nounwind optsize {\n" + 
			"entry.52:\n" + 
			"  %0 = getelementptr %\"Node$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
			"  store i16 0, i16* %0\n" + 
			"  ret void\n" + 
			"}\n" + 
			"\n" + 
			"define %Node2 @\"Node2.__init__._._Int.Str_._.Node2$p.Int.Str$p_\"(%\"Node2$p\" nocapture %this, i16 %x, %\"Str$p\" nocapture %y) nounwind optsize {\n" + 
			"entry.62:\n" + 
			"  %0 = load %\"Str$p\" %y                           ; <%Str> [#uses=2]\n" + 
			"  %1 = insertvalue %Node2 undef, i16 %x, 0        ; <%Node2> [#uses=1]\n" + 
			"  %2 = insertvalue %Node2 %1, %Str %0, 1          ; <%Node2> [#uses=1]\n" + 
			"  %3 = getelementptr %\"Node2$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
			"  store i16 %x, i16* %3\n" + 
			"  %4 = getelementptr %\"Node2$p\" %this, i16 0, i32 1 ; <%\"Str$p\"> [#uses=1]\n" + 
			"  store %Str %0, %\"Str$p\" %4\n" + 
			"  ret %Node2 %2\n" + 
			"}\n" + 
			"\n" + 
			"define void @\"Node2.$__init__$._.void._.Node2$p_\"(%\"Node2$p\" nocapture %this) nounwind optsize {\n" + 
			"entry.72:\n" + 
			"  %0 = getelementptr %\"Node2$p\" %this, i16 0, i32 0 ; <i16*> [#uses=1]\n" + 
			"  store i16 0, i16* %0\n" + 
			"  %1 = getelementptr %\"Node2$p\" %this, i16 0, i32 1 ; <%\"Str$p\"> [#uses=1]\n" + 
			"  store %Str zeroinitializer, %\"Str$p\" %1\n" + 
			"  ret void\n" + 
			"}\n" + 
			"\n" + 
			"define i16 @main._.Int._._() nounwind optsize {\n" + 
			"entry.79:\n" + 
			"  %0 = load i16* @brk._.Int                       ; <i16> [#uses=2]\n" + 
			"  %1 = add i16 %0, 2                              ; <i16> [#uses=1]\n" + 
			"  store i16 %1, i16* @brk._.Int\n" + 
			"  %2 = inttoptr i16 %0 to %\"Node$p\"               ; <%\"Node$p\"> [#uses=1]\n" + 
			"  %3 = getelementptr %\"Node$p\" %2, i16 0, i32 0   ; <i16*> [#uses=2]\n" + 
			"  store i16 10, i16* %3\n" + 
			"  %4 = load i16* @brk._.Int                       ; <i16> [#uses=2]\n" + 
			"  %5 = add i16 %4, 4                              ; <i16> [#uses=1]\n" + 
			"  store i16 %5, i16* @brk._.Int\n" + 
			"  %6 = inttoptr i16 %4 to %\"Node2$p\"              ; <%\"Node2$p\"> [#uses=3]\n" + 
			"  %7 = load %\"Str$p\" bitcast (%\"Str$3$p\" @\".const._.Str$3\" to %\"Str$p\") ; <%Str> [#uses=1]\n" + 
			"  %8 = getelementptr %\"Node2$p\" %6, i16 0, i32 0  ; <i16*> [#uses=1]\n" + 
			"  store i16 40, i16* %8\n" + 
			"  %9 = getelementptr %\"Node2$p\" %6, i16 0, i32 1  ; <%\"Str$p\"> [#uses=1]\n" + 
			"  store %Str %7, %\"Str$p\" %9\n" + 
			"  %10 = getelementptr %\"Node2$p\" %6, i16 0, i32 1, i32 0 ; <i16*> [#uses=1]\n" + 
			"  %11 = load i16* %10                             ; <i16> [#uses=1]\n" + 
			"  %12 = load i16* %3                              ; <i16> [#uses=1]\n" + 
			"  %13 = add i16 %11, 40                           ; <i16> [#uses=1]\n" + 
			"  %14 = add i16 %13, %12                          ; <i16> [#uses=1]\n" + 
			"  ret i16 %14\n" + 
			"}\n" + 
			"";
		
		LLModule mod = doLLVMParse(text);
		String redis = mod.toString();
		System.out.println(mod);
		
		assertMatchText("insertvalue %Node2 undef, %Int %x, 0", redis);
		assertMatchText("inttoptr %Int %0 to %Node\\$p", redis);
		assertMatchText("inttoptr %Int %4 to %Node2\\$p", redis);
	}	
	@Test
	public void testDefines5() throws Exception {
		String text =
			//1
			"target datalayout = \"E-p:16:16-s0:8:16-a0:8:16\"\n" + 
			"\n" + 
			"target triple = \"9900-unknown-v9t9\"\n" + 
			"\n" + 
			"%_Int.Int.Int_._.Int.Int.Int_ = type {%Int,%Int,%Int} (%Int,%Int,%Int)\n" + 
			"%Int = type i16\n" + 
			"%i16$p = type i16*\n" + 
			"%_Int.Int_._.Int.Int_ = type {%Int,%Int} (%Int,%Int)\n" + 
			"\n" + 
			"define default {%Int,%Int,%Int} @swap._._Int.Int.Int_._.Int.Int.Int_(%Int %x, %Int %y, %Int %z)  optsize \n" + 
			"{\n" + 
			"entry.21:\n" + 
			"%_.x.22 = 	alloca %Int \n" + 
			"	store %Int %x, %Int* %_.x.22\n" + 
			"%_.y.24 = 	alloca %Int \n" + 
			"	store %Int %y, %Int* %_.y.24\n" + 
			"%_.z.25 = 	alloca %Int \n" + 
			"	store %Int %z, %Int* %_.z.25\n" + 
			"%0 = 	load %Int* %_.y.24\n" + 
			"%1 = 	load %Int* %_.z.25\n" + 
			"%2 = 	load %Int* %_.x.22\n" + 
			"%3 = 	insertvalue {%Int,%Int,%Int} undef, %Int %0, 0\n" + 
			"%4 = 	insertvalue {%Int,%Int,%Int} %3, %Int %1, 1\n" + 
			"%5 = 	insertvalue {%Int,%Int,%Int} %4, %Int %2, 2\n" + 
			"	ret {%Int,%Int,%Int} %5\n" + 
			"}\n" + 
			"\n" + 
			"define default {%Int,%Int} @testTuples4b._._Int.Int_._.Int.Int_(%Int %a, %Int %b)  optsize \n" + 
			"{\n" + 
			"entry.28:\n" + 
			"%_.a.29 = 	alloca %Int \n" + 
			"	store %Int %a, %Int* %_.a.29\n" + 
			"%_.b.30 = 	alloca %Int \n" + 
			"	store %Int %b, %Int* %_.b.30\n" + 
			"%0 = 	load %Int* %_.a.29\n" + 
			"%1 = 	load %Int* %_.b.30\n" + 
			"%2 = 	add %Int %0, %1\n" + 
			"%3 = 	load %Int* %_.a.29\n" + 
			"%4 = 	load %Int* %_.b.30\n" + 
			"%5 = 	sub %Int %3, %4\n" + 
			"%6 = 	load %Int* %_.b.30\n" + 
			"%7 = 	call {%Int,%Int,%Int} @swap._._Int.Int.Int_._.Int.Int.Int_ (%Int %2, %Int %5, %Int %6)\n" + 
			"%8 = 	extractvalue {%Int,%Int,%Int} %7, 0\n" + 
			"%_.x.31 = 	alloca %Int \n" + 
			"	store %Int %8, %Int* %_.x.31\n" + 
			"%9 = 	extractvalue {%Int,%Int,%Int} %7, 1\n" + 
			"%_.o.32 = 	alloca %Int \n" + 
			"	store %Int %9, %Int* %_.o.32\n" + 
			"%10 = 	extractvalue {%Int,%Int,%Int} %7, 2\n" + 
			"%_.y.33 = 	alloca %Int \n" + 
			"	store %Int %10, %Int* %_.y.33\n" + 
			"%11 = 	load %Int* %_.a.29\n" + 
			"%12 = 	load %Int* %_.x.31\n" + 
			"%13 = 	mul %Int %11, %12\n" + 
			"%14 = 	load %Int* %_.y.33\n" + 
			"%15 = 	load %Int* %_.b.30\n" + 
			"%16 = 	mul %Int %14, %15\n" + 
			"%17 = 	insertvalue {%Int,%Int} undef, %Int %13, 0\n" + 
			"%18 = 	insertvalue {%Int,%Int} %17, %Int %16, 1\n" + 
			"	ret {%Int,%Int} %18\n" + 
			"}\n" + 
			"\n" + 
			"";
		
		LLModule mod = doLLVMParse(text);
		String redis = mod.toString();
		System.out.println(mod);
		
		assertMatchText("call \\{%Int,%Int,%Int\\} @swap._._Int.Int.Int_._.Int.Int.Int_ \\(%Int %2, %Int %5, %Int %6\\)", redis);
		assertMatchText("extractvalue \\{%Int,%Int,%Int\\} %7, 2", redis);
	}
}
