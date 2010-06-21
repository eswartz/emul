grammar LLVM;
options {
 ASTLabelType=CommonTree;
  output=AST;
  language=Java;
}

//tokens {
  
//}

@header {
package org.ejs.eulang.llvm.parser;
import org.ejs.eulang.symbols.*;
import org.ejs.eulang.llvm.*;
import org.ejs.eulang.llvm.directives.*;
import org.ejs.eulang.llvm.ops.*;
import org.ejs.eulang.llvm.instrs.*;
import org.ejs.eulang.types.*;
} 
@lexer::header{
package org.ejs.eulang.llvm.parser;
}

@lexer::members {
  class EOFException extends RecognitionException {
    String message;
    EOFException(IntStream input, String message) {
       super(input);
       this.message = message;
    }
    public String toString() { return message; } 
  }
  
}
@members {
    public String getTokenErrorDisplay(Token t) {
        return '\'' + t.getText() + '\'';
    }

    LLParserHelper helper;   
     
    public LLVMParser(TokenStream input, LLParserHelper helper) {
        this(input);
        this.helper = helper;
    }
  
}

prog:   toplevelstmts EOF!
    ;
                
toplevelstmts:  directive *
    ; 
    
directive  : targetDataLayoutDirective (NEWLINE | EOF) 
  | targetTripleDirective  (NEWLINE | EOF)
  | typeDefinition  (NEWLINE | EOF)
  | globalDataDirective  (NEWLINE | EOF)
  | constantDirective (NEWLINE | EOF)
  | defineDirective (NEWLINE | EOF)
  | NEWLINE
  ;
  
targetDataLayoutDirective : 'target' 'datalayout' EQUALS stringLiteral 
  { helper.addTargetDataLayoutDirective($stringLiteral.theText); }
  ;

targetTripleDirective : 'target' 'triple' EQUALS stringLiteral
  { helper.addTargetTripleDirective($stringLiteral.theText); }
  ;

typeDefinition : identifier EQUALS 'type'
  type 
  { 
  	helper.addNewType($identifier.theId, $type.theType); 
  }
  ;

type returns [LLType theType]  
	@init
    {
	  	// ensure we recognize temp symbols like percent 0 as pointing
	  	// to types rather than variables
		helper.inTypeContext++;
    }
    @after
  {
    // done 
      helper.inTypeContext--;
    }
    :
	(  t0=inttype  { $type.theType = $t0.theType; }
	|  t1=structtype { $type.theType = $t1.theType; }
	|  t2=arraytype { $type.theType = $t2.theType; }
	|  'void'        { $type.theType = helper.typeEngine.VOID; }
	|  'label'        { $type.theType = helper.typeEngine.LABEL; }
	|  t3=symboltype { $type.theType = $t3.theType; }
	)     
	
	( '*'  { $type.theType = helper.addPointerType($type.theType); } )*
	
	(paramstype  { $type.theType = helper.addCodeType($type.theType, $paramstype.theArgs); } ) ?
	;


inttype returns [LLType theType] : INT_TYPE 
	{ $inttype.theType = helper.addIntType($INT_TYPE.text); }
	;

structtype returns  [LLType theType] : '{' typeList '}' 
	{
		$structtype.theType = helper.addTupleType($typeList.theTypes); 
	} 
	;
 
arraytype returns [LLType theType] :  '[' number 'x' type ']' 
	{ $arraytype.theType = helper.addArrayType($number.value, $type.theType); }
	;

paramstype returns [LLType[\] theArgs] : '(' typeList ')'
	{ $paramstype.theArgs = $typeList.theTypes; }
	; 

typeList returns [LLType[\] theTypes] 
  @init
  {
    List<LLType> types = new ArrayList<LLType>();
  }
  @after
  {
  $typeList.theTypes = types.toArray(new LLType[types.size()]);
  }
  : (t=type        { types.add($t.theType); }
      (',' u=type   { types.add($u.theType); }
    
      )*
    ) ?
    ;
symboltype returns [LLType theType] : identifier 
	{ $symboltype.theType = helper.findOrForwardNameType($identifier.theId); }
	;

globalDataDirective : identifier EQUALS linkage? 'global' typedop
 	{ helper.addGlobalDataDirective($identifier.theId, $linkage.value, $typedop.op); }
	;

constantDirective : identifier EQUALS addrspace? 'constant' typedop  // section, alignment...
  { helper.addConstantDirective($identifier.theId, $addrspace.value, $typedop.op); }
  ;

addrspace returns [ int value ] : 'addrspace' '(' number ')' { $addrspace.value = $number.value; } 
  ;
   
linkage returns [ LLLinkage value ] : ('private' | 'linker_private' | 'internal' | 'available_externally'
  | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' 
  | 'externally_visible' | 'dllimport' | 'dllexport' ) 
  { $linkage.value = LLLinkage.getForToken($linkage.text); }
  ;
  
typedop returns [ LLOperand op ] : type op
    { $typedop.op = $op.op; $typedop.op.setType($type.theType); }
	;

op returns [ LLOperand op ] : 
  (  number { $op.op = new LLConstOp(null, $number.value); }
  | charconst { $op.op = new LLConstOp(null, (int)$charconst.value); }
  | stringconst { $op.op = new LLStringLitOp((LLArrayType)null, $stringconst.value); }
  | structconst { $op.op = new LLStructOp((LLAggregateType)null, $structconst.values); }
  | arrayconst  { $op.op = new LLArrayOp((LLArrayType)null, $arrayconst.values); }
  | symbolconst  { $op.op = helper.getSymbolOp($symbolconst.theId, $symbolconst.theSymbol); }
  | 'zeroinitializer'  { $op.op = new LLZeroInitOp(null); }
  | constcastexpr   { $op.op = $constcastexpr.op; }
  )
  ;

constcastexpr returns [ LLOperand op ] : casttype '(' typedop 'to' type ')' 
    {
    $constcastexpr.op = new LLCastOp($casttype.cast, $type.theType, $typedop.op);
    } 
    ;
    
casttype returns [ ECast cast ] :
  {
  ECast cast = null;
  } 
  ( 'trunc' { cast=ECast.TRUNC; }
  | 'zext' { cast=ECast.ZEXT; }
  | 'sext' { cast=ECast.SEXT; }
  | 'fptrunc' { cast=ECast.FPTRUNC; }
  | 'fpext' { cast=ECast.FPEXT; }
  | 'fptoui' { cast=ECast.FPTOUI; }
  | 'fptosi' { cast=ECast.FPTOSI; }
  | 'uitofp' { cast=ECast.UITOFP; }
  | 'sitofp' { cast=ECast.SITOFP; }
  | 'ptrtoint' { cast=ECast.PTRTOINT; } 
  | 'inttoptr' { cast=ECast.INTTOPTR; }
  | 'bitcast' { cast=ECast.BITCAST; }
  )
  {
  $casttype.cast = cast;
  }
  ;
  
symbolconst returns [ String theId, ISymbol theSymbol ] :
  identifier 
  { $symbolconst.theSymbol = helper.findSymbol($identifier.theId); $symbolconst.theId = $identifier.theId; }
  ;
  
charconst returns [ char value ] : 
	charLiteral { 
		$charconst.value = $charLiteral.theText.charAt(0);
	}
	;

stringconst returns [ String value  ] :
	cstringLiteral {
		$stringconst.value = $cstringLiteral.theText;
	}
	;	
	
structconst returns [ LLOperand[\] values ] 
  @init {
    List<LLOperand> ops = new ArrayList<LLOperand>();
  }
  @after {
    $structconst.values = ops.toArray(new LLOperand[ops.size()]);
  }
  :
  '{' (t0=typedop { ops.add($t0.op); } 
    (',' t1=typedop  { ops.add($t1.op); }
    )* 
  )? 
  '}'
  ;
   

arrayconst returns [ LLOperand[\] values ] 
  @init {
    List<LLOperand> ops = new ArrayList<LLOperand>();
  }
  @after {
    $arrayconst.values = ops.toArray(new LLOperand[ops.size()]);
  }
  :
  '[' (t0=typedop { ops.add($t0.op); } 
    (',' t1=typedop  { ops.add($t1.op); }
    )* 
  )? 
  ']'
  ;
   
identifier returns [String theId] : 
  (
	NAMED_ID    { $identifier.theId = $NAMED_ID.text; }
  | UNNAMED_ID	{ $identifier.theId = $UNNAMED_ID.text; }
  | QUOTED_ID 	{ $identifier.theId = $QUOTED_ID.text.substring(0,1) 
  						+ helper.unescape($QUOTED_ID.text.substring(1), '"'); }
  )
  ;

number returns [int value] : NUMBER { $number.value = Integer.parseInt($NUMBER.text); } 
	;
	
charLiteral returns [String theText] : CHAR_LITERAL
  { 
  $charLiteral.theText = LLParserHelper.unescape($CHAR_LITERAL.text, '\'');
  }
  ;
	
stringLiteral returns [String theText] : STRING_LITERAL
  {
  $stringLiteral.theText = LLParserHelper.unescape($STRING_LITERAL.text, '"');
  }
  ;
  
cstringLiteral returns [String theText] : CSTRING_LITERAL
  {
  $cstringLiteral.theText = LLParserHelper.unescape($CSTRING_LITERAL.text.substring(1), '"');
  }
  ;

defineDirective : DEFINE linkage? visibility? cconv? attrs type identifier arglist fn_attrs NEWLINE? // section align gc
//defineDirective : 'define' 'default' type identifier arglist  NEWLINE // section align gc
    {
    helper.openNewDefine(
      $identifier.theId,
        $linkage.value, $visibility.vis, $cconv.text, 
        new LLAttrType(new LLAttrs($attrs.attrs), $type.theType),
        $arglist.argAttrs, new LLFuncAttrs($fn_attrs.attrs),
        null, //section
        0, //align
        null //gc
        );
    }
    
    '{' NEWLINE
    defineStmts 
    '}'  
    
    {
    helper.closeDefine();
    }
  ;

visibility returns [LLVisibility vis] : ('default' | 'hidden' | 'protected') { $visibility.vis = LLVisibility.getForToken($visibility.text); } 
    ;
    
cconv : ('ccc' | 'fastcc' | 'coldcc' | 'cc 10' | 'cc' number) 
    ;

attrs returns [String[\] attrs] 
  @init {
    List<String> attrs = new ArrayList<String>();
  }
  @after {
    $attrs.attrs = attrs.toArray(new String[attrs.size()]);
  }

  : ( attr { attrs.add($attr.text); } ) *
  ;

attr : 'zeroext' | 'signext' | 'inreg' | 'byval' | 'sret' | 'noalias' | 'nocapture' | 'nest' ;


fn_attrs returns [String[\] attrs] 
  @init {
    List<String> attrs = new ArrayList<String>();
  }
  @after {
    $fn_attrs.attrs = attrs.toArray(new String[attrs.size()]);
  }

  : ( fn_attr { attrs.add($fn_attr.text); } ) *
  ;

fn_attr : ( 'alignstack' '(' number ')' ) | 'alwaysinline' | 'inlinehint' | 'noinline' | 'optsize' 
    | 'noreturn' | 'nounwind' | 'readnone' | 'readonly' | 'ssp' | 'sspreq' | 'noredzone' | 'noimplicitfloat' | 'naked' 
    ; 




arglist returns [ LLArgAttrType[\] argAttrs ] 
 @init {
    List<LLArgAttrType> attrs = new ArrayList<LLArgAttrType>();
  }
  @after {
    $arglist.argAttrs = attrs.toArray(new LLArgAttrType[attrs.size()]);
  }

  : '(' 
      ( f0=funcarg         { attrs.add($f0.argAttr); }
        ( ',' f1=funcarg   { attrs.add($f1.argAttr); }
        )* 
      ) ? 
     ')' 
  ;

funcarg returns [ LLArgAttrType argAttr ] :
  type attrs identifier  { $funcarg.argAttr = new LLArgAttrType($identifier.theId.substring(1), new LLAttrs($attrs.attrs), $type.theType); }
  ;

defineStmts returns [ List<LLBlock> blocks ] 
  @init {
    List<LLBlock> blocks = new ArrayList<LLBlock>();
  }
  @after {
    $defineStmts.blocks = blocks;
  }
  
  : ( block   { blocks.add($block.block); } ) +
  ;
  
block returns [ LLBlock block ] 
 @init {
    LLBlock block;
  }
  @after {
    $block.block = block;
  }
  : 
  blocklabel   { block = helper.currentTarget.addBlock($blocklabel.theSym); } 
  
  ( instr NEWLINE { block.instrs().add($instr.inst); System.out.println($instr.inst); }  ) + 
  
  ;

blocklabel returns [ ISymbol theSym ] : LABEL ':' NEWLINE
    { 
    $blocklabel.theSym = helper.addLabel($LABEL.text);
    }
    ;

instr returns [LLInstr inst ] :  
  ( allocaInstr          { $instr.inst = $allocaInstr.inst; }  ) 
  | ( storeInstr         { $instr.inst = $storeInstr.inst; }  )
  | ( branchInstr        { $instr.inst = $branchInstr.inst; }  )
  | ( uncondBranchInstr  { $instr.inst = $uncondBranchInstr.inst; }  )
  | ( retInstr           { $instr.inst = $retInstr.inst; }  )
  | ( assignInstr        { $instr.inst = $assignInstr.inst; }  )
  ;

ret returns [LLOperand op] : 
    UNNAMED_ID    
    {
      ISymbol tmpSym = helper.defineSymbol($UNNAMED_ID.text);
      String tmp = $UNNAMED_ID.text.substring(1);
      $ret.op = new LLTempOp(Integer.parseInt(tmp), null);
    }
    ;

local returns [LLOperand op] : 
    NAMED_ID    { $local.op = new LLSymbolOp(helper.defineSymbol($NAMED_ID.text)); }
    ;

allocaInstr returns [LLAllocaInstr inst] :
  local EQUALS 'alloca' type typedop? 
  { 
  
  LLType ptrType = helper.typeEngine.getPointerType($type.theType);
  $allocaInstr.inst = $typedop.op == null 
    ? new LLAllocaInstr($local.op, ptrType) 
    : new LLAllocaInstr($local.op, ptrType, $typedop.op); 
  
  // fixup types
  $local.op.setType(ptrType);
  $allocaInstr.inst.setType(ptrType);
  ((LLSymbolOp)$allocaInstr.inst.getResult()).getSymbol().setType(ptrType);
  $allocaInstr.inst.getResult().setType(ptrType);  
  }  
  ;

storeInstr returns [LLStoreInstr inst] :
  'store' o1=typedop ',' o2=typedop  
   { $storeInstr.inst = new LLStoreInstr($o2.op.getType().getSubType(), $o1.op, $o2.op); }  
  ;

retInstr returns [LLRetInstr inst] :
  'ret' ( ( 'void'          { $retInstr.inst = new LLRetInstr(helper.typeEngine.VOID); } )
          | ( o1=typedop  { $retInstr.inst = new LLRetInstr($o1.op.getType(), $o1.op); } )
          )
  ;

assignInstr returns [LLAssignInstr inst] :
    {
    LLOperand ret;
    }
    ret  
    { ret = $ret.op; }
    EQUALS
    (
    ( 'load' loadop=typedop { $assignInstr.inst = new LLLoadInstr(ret, $loadop.op.getType().getSubType(), $loadop.op); } )
    | ( binexpr binexprsuffix? bt=type bop1=op ',' bop2=op  
        {
          $bop1.op.setType($bt.theType); 
          $bop2.op.setType($bt.theType); 
          ret.setType($bt.theType);
          String op = $binexpr.text;
          if ($binexprsuffix.text != null)
            op += ' ' + $binexprsuffix.text;
          $assignInstr.inst = new LLBinaryInstr(op, ret, ret.getType(), $bop1.op, $bop2.op); 
        } 
      )
     | ( cmp=('icmp' | 'fcmp') cmptype ct=type cop1=op ',' cop2=op  
        {
          $cop1.op.setType($ct.theType); 
          $cop2.op.setType($ct.theType); 
          ret.setType(helper.typeEngine.LLBOOL);
          $assignInstr.inst = new LLCompareInstr($cmp.text, $cmptype.text, ret, $cop1.op, $cop2.op); 
        } 
      )
     | 'getelementptr' 
        {
        List<LLOperand> ops = new ArrayList<LLOperand>();
        }
        gep=typedop { ops.add($gep.op); } ( gepind { ops.add($gepind.op); } ) +
       {
          ret.setType(helper.getElementPtrType(ops));
          $assignInstr.inst = new LLGetElementPtrInstr(ret, ops.get(0).getType(), ops.toArray(new LLOperand[ops.size()])); 
       } 
    ) 
    
    {
    // fixup types, since we don't know 'ret''s until now
    if (ret.getType() == null) {
      ((LLTempOp)ret).setType($assignInstr.inst.getType());
    } 
    ISymbol local = helper.findSymbol(ret.toString());
    local.setType(ret.getType());
    }
    ;

gepind returns [ LLConstOp op ] : 
    ',' type number { $gepind.op = new LLConstOp($number.value); } 
    ;
     
binexpr : 'add' | 'fadd' 
  | 'sub' | 'fsub'
  | 'mul' |'fmul' 
  | 'udiv' | 'sdiv' | 'fdiv' | 'urem' | 'srem' | 'frem' 
  | 'shl' | 'lshr' | 'ashr' | 'and' | 'or' | 'xor';

binexprsuffix : ( 'nuw' | 'nsw' | 'exact' )+ ;

cmptype : 'eq' | 'ne' | 'ugt' | 'uge' | 'ult' | 'ule' | 'sgt' | 'sge' | 'slt' | 'sle' ;

branchInstr returns [LLInstr inst] :
  'br' typedop ',' 'label' t=identifier ',' 'label' f=identifier    
  { $branchInstr.inst = new LLBranchInstr($typedop.op.getType(), $typedop.op, helper.getSymbolOp($t.theId, null), helper.getSymbolOp($f.theId, null)); }
  ;

uncondBranchInstr returns [LLInstr inst] :
  'br' 'label' identifier  
  { $uncondBranchInstr.inst = new LLUncondBranchInstr(helper.getSymbolOp($identifier.theId, null)); }
  ;
  
EQUALS : '=' ;

INT_TYPE : 'i' ('0'..'9')+ ;

//
//  Numbers
//
NUMBER : '-'? '0'..'9' (NUMSUFFIX ( '.' NUMSUFFIX)?);

//
//  Identifiers
//
UNNAMED_ID returns [String theId] : SYM_PFX NUMBER_SUFFIX { $UNNAMED_ID.theId = $SYM_PFX.text + $NUMBER_SUFFIX.text; };
NAMED_ID returns [String theId] : SYM_PFX NAME_SUFFIX { $NAMED_ID.theId = $SYM_PFX.text + $NAME_SUFFIX.text; } ;
QUOTED_ID returns [String theId] : SYM_PFX STRING_LITERAL { $QUOTED_ID.theId = $SYM_PFX.text + LLParserHelper.unescape($STRING_LITERAL.text, '"'); } ;

fragment
SYM_PFX : '%' | '@';


DEFINE : 'define' ;

LABEL : NAME_SUFFIX ;

fragment NAME_SUFFIX : ('a'..'z' | 'A' .. 'Z' | '$' | '.' | '_') ('a'..'z' | 'A'..'Z' | '$' | '.' | '0'..'9' | '_')* ;
fragment NUMBER_SUFFIX : ('0'..'9')+  ;
fragment NUMSUFFIX : ('0'..'9' | 'A'..'Z' | 'a'..'z') *;

//
//  Strings
//  
//CHAR_LITERAL: '\'' (('\\' .) | ~('\'')) * '\'';

CHAR_LITERAL : '\'' {
  while (true) {
		 int ch = input.LA(1);
		 if (ch == '\\') {
		    input.consume();
		    input.consume();
		 } else if (ch == -1) {
        match('\'');
		 } else if (ch != '\'') {
		    input.consume();
		 } else {
		    match('\'');
		    break;
		 }
  }
};

STRING_LITERAL : '"' {
  while (true) {
		 int ch = input.LA(1);
		 if (ch == '\\') {
		    input.consume();	// backslash
		    input.consume();	// escaped
     } else if (ch == -1) {
        match('\"');
		 } else if (ch != '\"') {
		    input.consume();
		 } else {
		    match('\"');
		    break;
		 }
  }
};

CSTRING_LITERAL : 'c"' {
  while (true) {
     int ch = input.LA(1);
     if (ch == '\\') {
        input.consume();  // backslash
        input.consume();  // escaped
     } else if (ch == -1) {
        match('\"');
     } else if (ch != '\"') {
        input.consume();
     } else {
        match('\"');
        break;
     }
  }
};

//
//  Whitespace
//
NEWLINE: ('\r'? '\n')  ;
WS  :   (' '|'\t')+     { $channel = HIDDEN; };

// Single-line comments begin with //, are followed by any characters
// other than those in a newline, and are terminated by newline characters.
SINGLE_COMMENT: ';' ~('\r' | '\n')* { skip(); } ;

// Multi-line comments are delimited by /* and */
// and are optionally followed by newline characters.
MULTI_COMMENT options { greedy = false; }
  : '/*' .* '*/' NEWLINE? { skip(); };


      
