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
  //| targetTripleDirective
  //| typeDefinition
  //| defineDirective
  //| globalDataDirective
  //| constantDirective
  ;
  
targetDataLayoutDirective : 'target' 'datalayout' EQUALS s=STRING_LITERAL 
  { helper.addTargetDataLayoutDirective($s.text); }
  ;

identifier : NAMED_ID   
  | UNNAMED_ID
  | QUOTED_ID  
  ;

EQUALS : '=' ;

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

STRING_LITERAL : STRING_LITERAL_SUFFIX ;

fragment STRING_LITERAL_SUFFIX: '"' {
  while (true) {
		 int ch = input.LA(1);
		 if (ch == '\\') {
		    input.consume();
		    input.consume();
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

      