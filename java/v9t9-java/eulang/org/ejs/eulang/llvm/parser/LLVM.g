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
                
toplevelstmts:  directive*
    ; 

directive : targetDataLayoutDirective 
  | targetTripleDirective
  | typeDefinition
  | globalDataDirective
  //| defineDirective
  //| constantDirective
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

globalDataDirective : identifier EQUALS linkage? 'global' typedconstant
 	{ helper.addGlobalDataDirective($identifier.text, $linkage.value, $typedconstant.op); }
	;

linkage returns [ LLLinkage value ] : ('private' | 'linker_private' | 'internal' | 'available_externally'
  | 'linkonce' | 'weak' | 'common' | 'appending' | 'extern_weak' | 'linkonce_odr' | 'weak_odr' 
  | 'externally_visible' | 'dllimport' | 'dllexport' ) 
  { $linkage.value = LLLinkage.getForToken($linkage.text); }
  ;
  
typedconstant returns [ LLOperand op ] : type
	(  number { $typedconstant.op = new LLConstOp($type.theType, $number.value); }
	| charconst { $typedconstant.op = new LLConstOp($type.theType, (int)$charconst.value); }
	| stringconst { $typedconstant.op = new LLStringLitOp((LLArrayType)$type.theType, $stringconst.value); }
	| structconst { $typedconstant.op = new LLStructOp((LLAggregateType)$type.theType, $structconst.values); }
	| arrayconst  { $typedconstant.op = new LLArrayOp((LLArrayType)$type.theType, $arrayconst.values); }
	| symbolconst  { $typedconstant.op = helper.getSymbolOp($symbolconst.theId, $symbolconst.theSymbol); }
	| 'zeroinitializer'  { $typedconstant.op = new LLZeroInitOp($type.theType); }
	)
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
  '{' (t0=typedconstant { ops.add($t0.op); } 
    (',' t1=typedconstant  { ops.add($t1.op); }
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
  '[' (t0=typedconstant { ops.add($t0.op); } 
    (',' t1=typedconstant  { ops.add($t1.op); }
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
  
EQUALS : '=' ;

INT_TYPE : 'i' ('0'..'9')+ ;

//
//  Numbers
//
NUMBER : '0'..'9' (NUMSUFFIX ( '.' NUMSUFFIX)?);

//
//  Identifiers
//

NAMED_ID returns [String theId] : SYM_PFX NAME_SUFFIX { $NAMED_ID.theId = $SYM_PFX.text + $NAME_SUFFIX.text; } ;
UNNAMED_ID returns [String theId] : SYM_PFX NUMBER_SUFFIX { $UNNAMED_ID.theId = $SYM_PFX.text + $NUMBER_SUFFIX.text; };
QUOTED_ID returns [String theId] : SYM_PFX STRING_LITERAL { $QUOTED_ID.theId = $SYM_PFX.text + LLParserHelper.unescape($STRING_LITERAL.text, '"'); } ;

fragment
SYM_PFX : '%' | '@';

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
NEWLINE: ('\r'? '\n')+   { $channel = HIDDEN; };
WS  :   (' '|'\t')+     { $channel = HIDDEN; };

// Single-line comments begin with //, are followed by any characters
// other than those in a newline, and are terminated by newline characters.
SINGLE_COMMENT: '//' ~('\r' | '\n')* NEWLINE { skip(); };

// Multi-line comments are delimited by /* and */
// and are optionally followed by newline characters.
MULTI_COMMENT options { greedy = false; }
  : '/*' .* '*/' NEWLINE? { skip(); };

      
