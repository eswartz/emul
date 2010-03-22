lexer grammar EulangLexer;

@header {
package org.ejs.eulang;
import java.util.HashMap;
} 


LBRACE_LPAREN : '{(';
LBRACE_STAR : '{*';
COLON : ':';
COMMA : ',';
EQUALS : '=';
COLON_EQUALS : ':=';
COLON_COLON_EQUALS : '::=';
PLUS : '+';
MINUS : '-';
STAR : '*';
SLASH : '/';
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACKET : '[';
RBRACKET : ']';
HASH : '#';
EXCL : '!';
TILDE : '~';
AT : '@';
AMP : '&'; 
BAR : '|';
CARET : '^';
SEMI : ';';
QUESTION : '?';
COMPAND : '&&';
COMPOR : '||';
COMPEQ : '==';
COMPNE : '!=';
COMPGE : '>=';
COMPLE : '<=';
GREATER : '>';
LESS : '<';
LSHIFT : '<<';
RSHIFT : '>>';
URSHIFT : '>>>';
BACKSLASH : '\\';
PERCENT : '%';
UMOD : '%%';
RETURNS : '=>' ;

RETURN : 'return';
FOR : 'for';
IN : 'in';

//
//  Numbers
//
//NEGNUMBER: '-' NUMBER ;

NUMBER: '0'..'9' (IDSUFFIX ( '.' IDSUFFIX)?);

//
//  Identifiers
//
ID : LETTERLIKE IDSUFFIX ;
fragment IDSUFFIX : ( LETTERLIKE | DIGIT )*;
fragment LETTERLIKE:  'a'..'z' | 'A'..'Z' | '_';
fragment DIGIT: '0'..'9';
 
//
//  Strings
//  
CHAR_LITERAL: '\'' ~('\'') * '\'';
STRING_LITERAL: '"' ~('"') * '"';
fragment SPACE: ' ' | '\t';

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
