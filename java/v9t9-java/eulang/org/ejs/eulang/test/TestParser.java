/**
 * 
 */
package org.ejs.eulang.test;

import org.junit.Test;
/**
 * @author ejs
 *
 */
public class TestParser extends BaseTest  {
    @Test
    public void testEmpty() throws Exception {
    	parse("  \n\n");
    }
    @Test
    public void testCommentsEmpty() throws Exception {
    	parse("//a test\n");
    }
    @Test
    public void testNumber1() throws Exception {
    	parseAt("atom", "3192");
    }
    @Test
    public void testNumber2() throws Exception {
    	parseAt("atom", "0x100FFFp100");
    }
    @Test
    public void testNumber3() throws Exception {
    	parseAt("atom", "0x1.00FFFp100");
    }
    @Test
    public void testNumber4() throws Exception {
    	parseAt("atom", "129.39281e203");
    }
    @Test
    public void testChar1() throws Exception {
    	parseAt("atom", "'9'");
    }
    @Test
    public void testChar2() throws Exception {
    	parseAt("atom", "'\\u0004'");
    }
    @Test
    public void testString1() throws Exception {
    	parseAt("atom", "\"\"");
    }
    @Test
    public void testString2() throws Exception {
    	parseAt("atom", "\"There is stuff in here\"");
    }
    @Test
    public void testId1() throws Exception {
    	parseAt("atom", "myName");
    }
    @Test
    public void testId2() throws Exception {
    	parseAt("atom", "m");
    }
    @Test
    public void testExpr() throws Exception {
    	parse("({ 3+6; })");
    }
    @Test
    public void testTopLevelAssign() throws Exception  {
    	parse("i = 3 + 6 ;");
    	parse("//a test\ni = 3 + 6 ;");
    }
    @Test
    public void testTopLevelStmtList() throws Exception  {
    	parse("i = 3 + 6 ; j = 8 + 9;");
    }
    @Test
    public void testEmptyCodeBlock() throws Exception  {
    	parse("myCode = code {}; /* squadoosh */");
    }
    @Test
    public void testExprCodeBlock() throws Exception  {
    	parse("myCode = code { 3+6; };");
    }
    @Test
    public void testStmtCodeBlock() throws Exception  {
    	parse("myCode = code { i=3+6; j=i*i; };");
    }
    @Test
    public void testCodeBlockVar1() throws Exception  {
    	parse("testCodeBlockVar = code { block : code(x,y) = code(x,y) { x*y; }; };");
    }
    @Test
    public void testCodeBlockVar2() throws Exception  {
    	parse("testCodeBlockVar = code { block := code(x,y) { x*y }; };");
    }
    @Test
    public void testCodeBlockVar3() throws Exception  {
    	parse("testCodeBlockVar = code { block : code(x,y) = code { x*y }; };");
    }
    @Test
    public void testSelector1() throws Exception  {
    	parse("myCode = [ code { i=3+6; j=i*i; } ];");
    }
    @Test
    public void testSelector2() throws Exception  {
    	parse("myCode = [ " +
    			"code { i=3+6; j=i*333; }, " +
    			"code { }, " +
    			"code (a:Int;b) { }, " +	// (1) not a scope ref, (2) allow trailing comma
    			"]" +
    			";");
    }
    @Test
    public void testSelector2b() throws Exception  {
    	parse("myCode = [  ]" +
    			";");
    }
    @Test
    public void testSelector2c() throws Exception  {
    	parseFail("myCode = [ , ]" +
    			";");
    }
    @Test
    public void testCodeBlockArgs1() throws Exception  {
    	parse("sqrAdd = code (x, y) { x*x+y };");
    }
    @Test
    public void testCodeBlockArgs1b() throws Exception  {
    	parse("sqrAdd = code ( x; y) { x*x+y };");
    }
    @Test
    public void testCodeBlockArgs1c() throws Exception  {
    	parseFail("sqrAdd = code ( ,)  { x*x+y; };");
    }
    @Test
    public void testCodeBlockArgs2() throws Exception  {
    	parse("myCode = code (a : Int; b ; c: Float){  } ;");
    }
    @Test
    public void testCodeBlockReturns1() throws Exception  {
    	parse("sqrAdd = code( => Object ) { };");
    }
    @Test
    public void testCodeBlockReturns2() throws Exception  {
    	parse("sqrAdd = code( x , y => Object ) { };");
    }
    @Test
    public void testCodeBlockFuncCall1a() throws Exception  {
    	parse("sqrAdd = code ( a,b ) { a*a+b } ;"
    			+"myCode = code ( a ) { sqrAdd(a*10, a-10) ; } ;");
    }
    @Test
    public void testCodeBlockFuncCall1b() throws Exception  {
    	parseAt("codestmtlist", "sqrAdd(a*10, a-10);");
    }
    @Test
    public void testCodeBlockFuncCall1c() throws Exception  {
    	parseAt("codestmtlist", "sqrAdd(10, -10);");
    }
    @Test
    public void testCodeBlockFuncCall1d() throws Exception  {
    	parseAt("codestmtlist", "a = 1; sqrAdd(a, a=2);");
    }
    @Test
    public void testCodeBlockFuncCall2a() throws Exception  {
    	parseAt("codestmtlist", "sqrAdd() + sqrAdd();");
    }
    @Test
    public void testCodeBlockFuncCall2b() throws Exception  {
    	parseAt("codestmtlist", "sqrAdd(a*10, a-10) + sqrAdd(10,20);");
    }
    @Test
    public void testCodeBlockFuncCall2c() throws Exception  {
    	parseAt("codestmtlist", "a = 1 / sqrAdd(-1, -2);");
    }
    @Test
    public void testExpr1() throws Exception  {
    	parseAt("rhsExpr", "-1 - -1");
    }
    @Test
    public void testExpr2() throws Exception  {
    	parseAt("rhsExpr", "(y & 0xff) << 8 + 5");
    }
    @Test
    public void testExpr2b() throws Exception  {
    	parseAt("rhsExpr", "y * z + a / c + d & 11");
    }
    @Test
    public void testExpr2c() throws Exception  {
    	parseAt("rhsExpr", "y & z ~ d");
    }
    @Test
    public void testExpr2d() throws Exception  {
    	parseAt("rhsExpr", "y | z +>> a +/ c ~ d & 11");
    }
    @Test
    public void testCondExpr2b() throws Exception  {
    	parseAt("rhsExpr", "(y*2 > 0 and x < 10) ? true : false");
    }
    @Test
    public void testCondExpr2c() throws Exception  {
    	parseAt("rhsExpr", "a ? (b ? 1 : 2) : 3");
    }
    @Test
    public void testCondExpr2d() throws Exception  {
    	parseAt("rhsExpr", "a==4 ? ((y*2 > 0 and x < 10) ? a<<9!=0 : a!=4) : rout(a)");
    }
    @Test
    public void testProto() throws Exception  {
    	parse("run = (x,y);");
    }
    @Test
    public void testNoTopLevelStmts() throws Exception  {
    	parseFail(" @label:");
    }
    
    //@Test
    public void testListCompr0() throws Exception  {
    	parseAt("listCompr", "for T in [ Int, Float ] : code ( x : T; y : T => T ) { x*x+y }");
    }
    //@Test
    public void testListCompr1() throws Exception  {
    	parse("sqrAdd = [ for T in [ Int, Float ] : code( x : T; y : T => T ) { x*x+y } ] ; ");
    }
    //@Test
    public void testListCompr2() throws Exception  {
    	parse("sqrAdd = [ for T, U in [ Int, Float ] : code ( x : T; y : U )  { x*x+y } ] ; ") ;
    }
    //@Test
    public void testListCompr3() throws Exception  {
    	parse("sqrAdd = [ for T in [ Int, Float ] for U in [ Byte, Double ] : code ( x : T; y : U ) {  x*x+y } ];") ;
    }
    //@Test
    public void testListCompr4() throws Exception  {
    	// TODO: be sure the outer list has three items
    	parse("sqrAdd = [ code ( x, y, z ) {} , for T in [ Byte, Double ] : code ( x : T; y : U ) {  x*x+y }, code ( a, b, c ) {}];") ;
    }
    
    @Test
    public void testForward1() throws Exception  {
    	parse("forward sqrAdd;");
    }
    @Test
    public void testScope1() throws Exception  {
    	parse("{ forward sqrAdd; }");
    }
    @Test
    public void testScope2() throws Exception  {
    	parse("{ forward sqrAdd; inner = { foo = code() { sqrAdd() }; }; }");
    }
    /*
    @Test
    public void testType1() throws Exception  {
    	parseAt("type", "Int&");
    }
    @Test
    public void testType1b() throws Exception  {
    	parseAt("varDecl", "x : Int&");
    }
    @Test
    public void testType1c() throws Exception  {
    	parseAt("codestmtlist", "x : Int&;");
    }
    @Test
    public void testType1d() throws Exception  {
    	parse("foo = code { x : Int& = 0; };");
    }*/
    @Test
    public void testCodeExpr1() throws Exception  {
    	parse("codeExpr1 = code { x; } ; ");
    }
    @Test
    public void testCodeExpr2a() throws Exception  {
    	parse("codeExpr2 = code { x.y.z; } ; ");
    }
    @Test
    public void testCodeExpr2b() throws Exception  {
    	parse("codeExpr2 = code { x.y.z(); } ; ");
    }
    
    @Test
    public void testScopeRef1() throws Exception  {
    	parse("foo = code { Ref.adr; Ref.adr(0x8370); };");
    }
    /*
    @Test
    public void testRef1() throws Exception  {
    	parse("foo = code { x : Int& = Ref.adr(0x8370); };");
    }*/
    
    @Test
    public void testEmptyMacroCodeBlock() throws Exception  {
    	parse("myICode = code #macro {};");
    }
    //@Test
    public void testDefineMacroCodeBlock() throws Exception  {
    	parse("myICode = code #macro (x,y) {}; foo = code { a=*myICode(1,2); };");
    }
    //@Test
    public void testCallAsMacroCodeBlock() throws Exception  {
    	parse("myICode = code (x,y) {}; foo = code { a=*myICode(1,2); };");
    }
    @Test
    public void testAmbiguousProtoOrExpr() throws Exception  {
    	// TODO: test
    	parse("myProto = (x,y); myExpr = (10);  myProtoNotExpr = (x);");
    }
    @Test
    public void testScopeOrFloat() throws Exception  {
    	parse("foo = code { x.y.z0 + 0x.e0; };");
    }
    @Test
    public void testScopeRefs() throws Exception  {
    	parse("scopeRef = code { :x = x; } ; ");
    }
    @Test
    public void testScopeRefs2() throws Exception  {
    	parse("scopeRef = code { a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs3a() throws Exception  {
    	parse("scopeRef = code { :a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs3b() throws Exception  {
    	parse("scopeRef = code { ::a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs3c() throws Exception  {
    	parse("scopeRef = code { :::::a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs3d() throws Exception  {
    	parse("scopeRef = code { : : :: : a.b.c = x; } ; ");
    }
    @Test
    public void testScopeRefs3e() throws Exception  {
    	parse("scopeRef = code { : :: : a.\nb\t.\tc = x; } ; ");
    }
    @Test
    public void testScopeRefs4() throws Exception  {
    	parse("scopeRef = code { :x = x; ::a.b.c = r; } ; ");
    }
    
    @Test 
    public void testOpPrec1a() throws Exception {
    	parse("opPrec1 = code { x=1*2+3>>4&5 ~ 6|7==8 and 9 or 10; };");
    }
    @Test 
    public void testOpPrec2a() throws Exception {
    	parse("opPrec1 = code { x=1 or 2 and 3==4|5 ~ 6&7>>8+9*10; };");
    }
    @Test 
    public void testOpPrec1b() throws Exception {
    	// TODO: make sure all tokens used
    	parse("opPrec1 = code { x=1*2/3\\4+\\4.5+5-6>>7<<8+>>8.5&9 ~ 10|11<12>13<=14>=15==16!=17 and 18 or 19; };");
    }
    @Test 
    public void testOpPrec2b() throws Exception {
    	// TODO: make sure all tokens used
    	parse("opPrec1 = code { x=1 or 2 and 3!=4==5>=6<=7>8<9|10 ~ 11&12<<13>>14-15+16%17/18*19; };");
    }
    @Test 
    public void testOpPrec3() throws Exception {
    	parse("opPrec1 = code { x=1000>>2>>5; };");
    }
    @Test 
    public void testOpPrec3b() throws Exception {
    	parse("opPrec1 = code { x=1*2/3%4+/5; };");
    }
    
    @Test
    public void testTuples1() throws Exception {
    	parse("tuples1 = code (x,y) { (y,x); };");
    }
    @Test
    public void testTuples2() throws Exception {
    	parse("tuples2 = (7, code (x,y) { (y,x); });");
    }
    @Test
    public void testTuples3() throws Exception {
    	parse("tuples3 = code (x,y => (Int, Int)) { (y,x); };");
    }
    @Test
    public void testIncsDecs() throws Exception  {
    	parse("maker1 = code(u:Int;v:Int) { u++  };\n" +
    			"maker1b = code(u:Int;v:Int) {  ++v };\n" +
    			"maker2 = code(u:Int;v:Int) { u-- };\n"+
    			"maker2b = code(u:Int;v:Int) {  --v };\n"+
    			""
    			);
    }
}


