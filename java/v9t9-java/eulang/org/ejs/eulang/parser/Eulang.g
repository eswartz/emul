grammar Eulang;
options {
 ASTLabelType=CommonTree;
  output=AST;
  language=Java;
}

tokens {
  SCOPE;
  LIST_COMPREHENSION;
  CODE;
  MACRO;
  STMTLIST;
  PROTO;
  ARGLIST;
  ARGDEF;
  
  REF;
  
  ALLOC;
  ASSIGN;
  DEFINE;
  EXPR;
  LIST;
  TYPE;
  STMTEXPR;
  
  CALL;
  INLINE;   // modifier on CALL
  
  COND;
  BITAND;
  BITOR;
  BITXOR;
  
  ADD;
  SUB;
  MUL;
  DIV;
  UDIV;
  MOD;
  
  NOT;
  NEG;
  INV;

  LIT;
  
  IDREF;
  IDLIST;
}

@header {
package org.ejs.eulang.parser;
import java.util.HashMap;
} 
@lexer::header{
package org.ejs.eulang.parser;
}

@members {
    public String getTokenErrorDisplay(Token t) {
        return '\'' + t.getText() + '\'';
    }

  protected CommonTree split(CommonTree items) {
        if (items == null) return null;
        StringBuilder sb = new StringBuilder();
        if (items.getText()!=null) sb.append(items.getText());
        for (int  i = 0; i < items.getChildCount(); i++)
          sb.append(items.getChild(i).getText());
        //return new CommonTree(new CommonToken(COLONS, sb.toString()));
        CommonTree out = new CommonTree();
        for (int i = 0; i < sb.length(); i++) 
          out.addChild(new CommonTree(new CommonToken(COLON, ":")));
          return out;
      }
}

prog:   toplevelstmts EOF!
    ;
                
toplevelstmts: toplevelstat*      -> ^(STMTLIST toplevelstat*)
    ; 
    
toplevelstat:   ID EQUALS toplevelvalue     SEMI  -> ^(DEFINE ID toplevelvalue)
    | ID COLON type (EQUALS toplevelvalue)?     SEMI  -> ^(ALLOC ID type toplevelvalue?)
    | ID COLON_EQUALS rhsExpr  SEMI  -> ^(ALLOC ID TYPE rhsExpr)
    | rhsExpr                  SEMI  -> ^(EXPR rhsExpr)
    | xscope
    ;

toplevelvalue : xscope
    //| code
    | macro
    | (  LPAREN (RPAREN | ID) ) => proto     
    | selector
    | rhsExpr
    ;

// one or more selectors
selector: LBRACKET selectors RBRACKET    -> ^(LIST selectors*) 
    ;

selectors: (selectoritem ( COMMA selectoritem )* COMMA?)?    
  ;
        
selectoritem: listCompr | code | macro ;

//  scope
//
xscope : LBRACE toplevelstmts RBRACE    -> ^(SCOPE toplevelstmts* )
    ;

//  list comprehension
//
listCompr: forIn+ COLON listiterable     -> ^(LIST_COMPREHENSION forIn+ listiterable ) 
    ;
  
forIn : FOR idlist IN list      -> ^(FOR idlist list ) ;

idlist : ID (COMMA ID)*    -> ^(IDLIST ID+)
    ;

listiterable : ( code | macro | proto ) ;
    
list : LBRACKET listitems RBRACKET     -> ^(LIST listitems*)
    ;
    
listitems: (listitem ( COMMA listitem )* COMMA?)?    
  ;
        
listitem : toplevelvalue ;
    
  
// code block

code : CODE ( LPAREN optargdefs xreturns? RPAREN ) ? LBRACE codestmtlist RBRACE -> ^(CODE ^(PROTO xreturns? optargdefs*) codestmtlist*)  
    ;

// inline code block
macro : MACRO ( LPAREN optargdefs xreturns? RPAREN ) ? LBRACE codestmtlist RBRACE -> ^(MACRO ^(PROTO xreturns? optargdefs*) codestmtlist*)  
    ;

proto : LPAREN argdefs xreturns? RPAREN                   -> ^(PROTO xreturns? argdefs*)
    ;
argdefs: (argdef ( COMMA argdef)* COMMA?)?                        -> argdef* 
    ;

argdef: ID (COLON type)?    -> ^(ARGDEF ID type* )
  ;

xreturns: RETURNS type      -> type
  ;

optargdefs: (optargdef ( COMMA optargdef)* COMMA?)?                        -> optargdef* 
    ;

optargdef: ID (COLON type)? (EQUALS init=rhsExpr)?    -> ^(ARGDEF ID type* $init?)

  ;
  
type :  ( idOrScopeRef -> ^(TYPE idOrScopeRef) )  ( AMP -> ^(TYPE ^(REF idOrScopeRef) ) )? 
     | CODE proto ? -> ^(TYPE proto )
  ;

codestmtlist: (codeStmt SEMI) => (codeStmt SEMI (codeStmt  SEMI)*) ? ->  ^(STMTLIST codeStmt*)
    | rhsExpr          -> ^(STMTLIST ^(RETURN rhsExpr))
    | -> ^(STMTLIST) 
    ;
    
codeStmt : varDecl    -> varDecl
      | assignStmt    -> assignStmt
      | returnStmt    -> returnStmt
      | rhsExpr       -> ^(STMTEXPR rhsExpr)
      ;

varDecl: ID COLON_EQUALS assignExpr         -> ^(ALLOC ID TYPE assignExpr)
    | ID COLON type (EQUALS assignExpr)?  -> ^(ALLOC ID type assignExpr*)
    ;

returnStmt : RETURN assignExpr?           -> ^(RETURN assignExpr?)
      ;

assignStmt : idOrScopeRef EQUALS assignExpr        -> ^(ASSIGN idOrScopeRef assignExpr)
    ;
      
assignExpr : idOrScopeRef EQUALS assignExpr        -> ^(ASSIGN idOrScopeRef assignExpr)
    | rhsExpr                             -> rhsExpr
    ;

rhsExpr :   cond                          -> cond
    ;
    
funcCall : idOrScopeRef LPAREN arglist RPAREN   ->     ^(CALL idOrScopeRef arglist) 
    ;


arglist: (arg ( COMMA arg)* COMMA?)?                        -> ^(ARGLIST arg*) 
    ;

arg:  assignExpr                    -> ^(EXPR assignExpr) 
   ;

//
//  Expressions
//

cond:    ( logor  -> logor )
      ( QUESTION t=logor COLON f=logor -> ^(COND $cond $t $f ) )*
;

logor : ( logand  -> logand )
      ( COMPOR r=logand -> ^(COMPOR $logor $r) )*
      ;
logand : ( comp -> comp )
      ( COMPAND r=comp -> ^(COMPAND $logand $r) ) *
      ;
              
// in Python, "not expr" is here

comp:   ( bitor        -> bitor )          
      ( COMPEQ r=bitor -> ^(COMPEQ $comp $r)
      | COMPNE r=bitor -> ^(COMPNE $comp $r)
      | COMPLE r=bitor    -> ^(COMPLE $comp $r)
      | COMPGE r=bitor    -> ^(COMPGE $comp $r)
      | LESS r=bitor     -> ^(LESS $comp $r)
      | GREATER r=bitor    -> ^(GREATER $comp $r)
      )*
;               


bitor: ( bitxor      -> bitxor )       
      ( BAR r=bitxor  -> ^(BITOR $bitor $r) ) *
;
bitxor: ( bitand      -> bitand )       
      ( CARET r=bitand  -> ^(BITXOR $bitxor $r) )*
;
bitand: ( shift      -> shift )       
      ( AMP r=shift  -> ^(BITAND $bitand $r) )*
;

shift:  ( factor        -> factor )         
      ( ( LSHIFT r=factor   -> ^(LSHIFT $shift $r) ) 
      | ( RSHIFT r=factor   -> ^(RSHIFT $shift $r) )
      | ( URSHIFT r=factor   -> ^(URSHIFT $shift $r) )
      )*
  ;
factor 
    : ( term              -> term )
        (   PLUS r=term         -> ^(ADD $factor $r)
        |  ( MINUS term) => MINUS r=term        -> ^(SUB $factor $r)
        )*
    ;

term : ( unary                  -> unary )
        ( ( STAR unary) => STAR r=unary            -> ^(MUL  $term $r)
        | SLASH r=unary            -> ^(DIV $term $r)
        | BACKSLASH r=unary            -> ^(UDIV $term $r)
        | PERCENT r=unary            -> ^(MOD $term $r)
        | UMOD r=unary            -> ^(UMOD $term $r)
        )*                        
    ; 


unary:    ( atom        -> atom )        
      | MINUS u=unary -> ^(NEG $u )
      | EXCL u=unary     -> ^(NOT $u )
      | TILDE u=unary     -> ^(INV $u )
;
atom options { k=2; } :
      NUMBER                          -> ^(LIT NUMBER)
    |   CHAR_LITERAL                  -> ^(LIT CHAR_LITERAL)
    |   STRING_LITERAL                -> ^(LIT STRING_LITERAL)
    |   ( STAR idOrScopeRef LPAREN) => STAR f=funcCall  -> ^(INLINE $f)
    |   (idOrScopeRef LPAREN ) => funcCall   -> funcCall
    |   idOrScopeRef
    |   LPAREN assignExpr RPAREN               -> assignExpr
    |   code                           -> code   
    ;

idOrScopeRef : ID ( PERIOD ID ) * -> ^(IDREF ID+ ) 
      //| SCOPEREF -> ^(IDREF SCOPEREF)
      //| COLONS ( ( SCOPEREF -> ^(IDREF COLONS SCOPEREF) ) | ( ID -> ^(IDREF COLONS ID) ) )
      //| COLON+ ( ( SCOPEREF -> ^(IDREF COLON+ SCOPEREF) ) | ( ID -> ^(IDREF COLON+ ID) ) )
      //| COLONS ( ( ID ( PERIOD ID ) * -> ^(IDREF COLONS ID+) ) )
      | c=colons ID ( PERIOD ID ) * -> ^(IDREF {split($c.tree)} ID+) 
      ;

colons : (COLON | COLONS)+ ;

LBRACE_LPAREN : '{(';
LBRACE_STAR : '{*';
LBRACE_STAR_LPAREN : '{*(';
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
PERIOD : '.';

RETURN : 'return';
CODE : 'code';
DATA : 'data';
MACRO : 'macro';
FOR : 'for';
IN : 'in';

//
//  Numbers
//
NUMBER: '0'..'9' (IDSUFFIX ( '.' IDSUFFIX)?);

//
//  Identifiers
//
//SCOPEREF : ID ('.' ID) + ;

//  Handle multiple colons which aren't ':' or '::='.  (We ignore spaces so we have to account for this)
COLONS : COLON COLON+ ;

ID : LETTERLIKE IDSUFFIX ;
fragment IDSUFFIX : ( LETTERLIKE | DIGIT )*;
fragment LETTERLIKE:  'a'..'z' | 'A'..'Z' | '_';
fragment DIGIT: '0'..'9';
 
//
//  Strings
//  
CHAR_LITERAL: '\'' ~('\'') * '\'';
STRING_LITERAL: '"' ~('"') * '"';

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

      