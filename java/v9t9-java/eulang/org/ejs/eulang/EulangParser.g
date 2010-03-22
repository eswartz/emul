parser grammar EulangParser;
options {
 ASTLabelType=CommonTree;
  output=AST;
  language=Java;
  tokenVocab=EulangLexer;
}
tokens {
  CODE;
  STMTLIST;
  PROTO;
  ARGLIST;
  ARGDEF;
  EXPR;
  DEFINE_ASSIGN;
  ASSIGN;
  DEFINE;
  LIST;
  TYPE;
  CALL;
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
}
@header {
package org.ejs.eulang;
import java.util.HashMap;
} 


@members {
}


prog:   toplevelstmts EOF!
    ;
                
toplevelstmts: stat*      -> ^(STMTLIST stat*)
    ; 
    
stat:   ID EQUALS value     SEMI  -> ^(DEFINE_ASSIGN ID value)
    | ID COLON_EQUALS rhsExpr  SEMI  -> ^(DEFINE ID rhsExpr)
    | rhsExpr                  SEMI?  -> ^(EXPR rhsExpr)
    ;

value: code       
    | selector
    | rhsExpr
    ;

// one or more selectors
selector: LBRACKET selectorlist RBRACKET    -> ^(LIST selectorlist*) 
    ;

selectorlist: (selectoritem ( COMMA selectoritem )* COMMA?)?    
  ;
        
selectoritem: code  ;

// code block
code
    :   LBRACE_LPAREN argdefs xreturns? RPAREN codestmtlist RBRACE -> ^(CODE ^(PROTO xreturns? argdefs*) codestmtlist*)  
    ;

argdefs: (argdef ( COMMA argdef)* COMMA?)?                        -> argdef* 
    ;

argdef:  ID (COLON type (EQUALS assignExpr)?)?    -> ^(ARGDEF ID type* assignExpr*)
    | ID EQUALS assignExpr    -> ^(ARGDEF ID TYPE assignExpr)
  ;

xreturns: RETURNS type      -> type
  ;
type:  ID       -> ^(TYPE ID)
  ;

codestmtlist: (codeStmt ( SEMI codeStmt )* SEMI?) ? ->  ^(STMTLIST codeStmt*)
    ;
    
codeStmt : varDecl
      | assignExpr
      ;

varDecl: ID COLON_EQUALS assignExpr         -> ^(DEFINE ID TYPE assignExpr)
    | ID COLON type (EQUALS assignExpr)?  -> ^(DEFINE ID type assignExpr*)
    ;

assignExpr : ID EQUALS assignExpr        -> ^(ASSIGN ID assignExpr)
    | rhsExpr
    ;

rhsExpr :   cond 
    ;
    
funcCall : ID LPAREN arglist RPAREN   ->     ^(CALL ID arglist) 
    ;


arglist: (arg ( COMMA arg)* COMMA?)?                        -> ^(ARGLIST arg*) 
    ;

arg:  assignExpr                    -> ^(EXPR assignExpr) 
   ;

//
//  Expressions
//

cond:    ( l=logcond  -> $l )
      ( QUESTION t=cond COLON f=cond -> ^(COND $l $t $f ) 
      )*
;

logcond: ( l=binlogcond     -> $l )      
      ( COMPAND r=binlogcond -> ^(COMPAND $l $r)
      | COMPOR r=binlogcond -> ^(COMPOR $l $r)
      | COMPXOR r=binlogcond  -> ^(COMPXOR $l $r)
      )*
;

binlogcond: ( l=compeq      -> $l )       
      ( AMP r=compeq  -> ^(BITAND $l $r)
      | BAR r=compeq  -> ^(BITOR $l $r)
      | CARET r=compeq  -> ^(BITXOR $l $r)
      )*
;

compeq:   ( l=comp        -> $l )          
      ( COMPEQ r=comp -> ^(COMPEQ $l $r)
      | COMPNE r=comp -> ^(COMPNE $l $r)
      )*
;

comp:  ( l=shift           -> $l )
      ( COMPLE r=shift    -> ^(COMPLE $l $r)
      | COMPGE r=shift    -> ^(COMPGE $l $r)
      | LESS r=shift     -> ^(LESS $l $r)
      | GREATER r=shift    -> ^(GREATER $l $r)
      )*
;               

shift:  ( l=factor        -> $l )         
      ( LSHIFT r=factor   -> ^(LSHIFT $l $r) 
      | RSHIFT r=factor   -> ^(RSHIFT $l $r)
      | URSHIFT r=factor   -> ^(URSHIFT $l $r)
      )*
  ;
factor  
    : ( l=multExpr              -> $l )
        (   PLUS r=multExpr         -> ^(ADD $l $r)
        |   MINUS r=multExpr        -> ^(SUB $l $r)
        )*
    ;

multExpr : ( l=atom                  -> $l )
        ( STAR r=atom             -> ^(MUL  $l $r)
        | SLASH r=atom            -> ^(DIV $l $r)
        | BACKSLASH r=atom            -> ^(UDIV $l $r)
        | PERCENT r=atom            -> ^(MOD $l $r)
        | UMOD r=atom            -> ^(UMOD $l $r)
        )*                        
    ; 

atom options { k=2; } :
    MINUS NUMBER                          -> {new CommonTree(new CommonToken(NUMBER, "-" + $NUMBER.text))}
    |   NUMBER                          -> NUMBER
    |   CHAR_LITERAL
    |   STRING_LITERAL
    |   funcCall                                    -> funcCall
    |   ID                                     -> ID
    |   LPAREN assignExpr RPAREN               -> assignExpr 
    ;

