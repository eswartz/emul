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
import org.ejs.eulang.llvm.*;
import org.ejs.eulang.llvm.ops.*;
import org.ejs.eulang.llvm.instrs.*;
import org.ejs.eulang.llvm.types.*;
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
  
targetDataLayoutDirective : 'target' 'datalayout' EQUALS s=STRING_LITERAL 
  { helper.addTargetDataLayoutDirective($s.text); }
  ;

targetTripleDirective : 'target' 'triple' EQUALS s=STRING_LITERAL
  { helper.addTargetTripleDirective($s.text); }
  ;

typeDefinition : identifier EQUALS 'type'
  type 
  { 
  	helper.addNewType($identifier.text, $type); 
  }
  ;

type returns [LLType theType] : 
	@init
    {
	  	// ensure we recognize temp symbols like '%0' as pointing
	  	// to types rather than variables
		helper.inTypeContext++;
    }
    
	(  inttype 
	|  structtype
	|  arraytype
	|  symboltype
	)
	( '*'  { $theType = helper.addPointerType($type.theType); } )*
	
	(paramstype  { $theType = helper.addCodeType($type.theType, $paramstype.theArgs); } ) ?
	
	@after
	{
		// done 
  		helper.inTypeContext--;
  	}
	;


inttype returns [LLType theType] : INT_TYPE 
	{ $theType = helper.addIntType($INT_TYPE.text); }
	;

structtype returns  [LLType theType] : '{' type (',' type)+ '}' 
	{
		$theType = helper.addTupleType($type+); 
	} 
	;
 
arraytype returns [LLType theType] :  '[' number 'x' type ']' 
	{ $arraytype = helper.addArrayType($number.value, $type); }
	;

paramstype returns [LLType[] theArgs] : '(' (type (',' type)*)? ')'
	{ $paramstype.theArgs = helper.getTypeList($type+); }
	; 

symboltype returns [LLType theType] : identifier 
	{ $symboltype.theType = helper.findOrForwardNameType($identifier.theId); }
	;

globalDataDirective : 'global' typedconstant
 	{ helper.addGlobalDataDirective($type.theType, $constant.op); }
	;

typedconstant returns [ LLOperand op ] : type
	  number { $typedconstant.op = new LLConstOp($type.theType, $number.value); }
	| charconst { $typedconstant.op = new LLConstOp($type.theType, $charconst.value); }
	| stringconst { $typedconstant.op = new LLStringLitOp($type.theType, $stringconst.value); }
	;
	
charconst returns [ char value ] : 
	CHAR_LITERAL { 
		String v = helper.unescape($CHAR_LITERAL.text, '\'');
		$charconst.value = v.charAt(0);
	}
	;

stringconst returns [ String value  ] :
	STRING_LITERAL {
		String v = helper.unescape($STRING_LITERAL.text, '"');
		$stringconst.value = v;
	}
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
	
EQUALS : '=' ;

INT_TYPE : 'i' ('0'..'9')+ ;

//
//  Numbers
//
NUMBER : '0'..'9' (NUMSUFFIX ( '.' NUMSUFFIX)?);

//
//  Identifiers
//

NAMED_ID : ('%' | '@') NAME_SUFFIX ;
UNNAMED_ID : ('%' | '@') NUMBER_SUFFIX ;
QUOTED_ID : ('%' | '@') STRING_LITERAL_SUFFIX ;

fragment NAME_SUFFIX : ('a'..'z' | 'A' .. 'Z' | '$' | '.' | '_') ('a'..'z' | 'A'..'Z' | '$' | '.' | '0'..'9')* ;
fragment NUMBER_SUFFIX : ('0'..'9')+  ;
fragment NUMSUFFIX : ('0'..'9' | 'A'..'Z' | 'a'..'z') *;

//
//  Strings
//  
//CHAR_LITERAL: '\'' (('\\' .) | ~('\'')) * '\'';
CHAR_LITERAL returns String : '\'' {
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
	$CHAR_LITERAL = helper.unescape($CHAR_LITERAL_SUFFIX.text);
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
  $STRING_LITERAL = helper.unescape($STRING_LITERAL_SUFFIX.text);
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

      
