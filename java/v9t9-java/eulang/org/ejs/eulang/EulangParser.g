parser grammar EulangParser;
options {
 ASTLabelType=CommonTree;
  output=AST;
  language=Java;
  tokenVocab=EulangLexer;
}
tokens {
  SCOPE;
  LIST_COMPREHENSION;
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
  
  IDLIST;
}
@header {
package org.ejs.eulang;
import java.util.HashMap;
} 


@members {
}


prog:   toplevelstmts EOF!
    ;
                
toplevelstmts: toplevelstat*      -> ^(STMTLIST toplevelstat*)
    ; 
    
toplevelstat:   ID EQUALS toplevelvalue     SEMI  -> ^(DEFINE_ASSIGN ID toplevelvalue)
    | ID COLON_EQUALS rhsExpr  SEMI  -> ^(DEFINE ID rhsExpr)
    | rhsExpr                  SEMI?  -> ^(EXPR rhsExpr)
    | xscope
    ;

toplevelvalue : xscope
    | code
    | proto       
    | selector
    | rhsExpr
    ;

// one or more selectors
selector: LBRACKET selectors RBRACKET    -> ^(LIST selectors*) 
    ;

selectors: (selectoritem ( COMMA selectoritem )* COMMA?)?    
  ;
        
selectoritem: listCompr | code  ;

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

listiterable : ( code | proto ) ;
    
list : LBRACKET listitems RBRACKET     -> ^(LIST listitems*)
    ;
    
listitems: (listitem ( COMMA listitem )* COMMA?)?    
  ;
        
listitem : toplevelvalue ;
    
//
// prototype:  like foo = (x,y);
//
// such a prototype cannot use any initializers, or else this looks like an assignment as in "x = 40; foo= (x=40);"
//
proto :   LPAREN protoargdefs xreturns? RPAREN                   -> ^(PROTO xreturns? protoargdefs*)
  ;
  
protoargdefs: (protoargdef ( COMMA protoargdef)* COMMA?)?                        -> protoargdef* 
    ;

protoargdef:  ID (COLON type (EQUALS rhsExpr)?)?    -> ^(ARGDEF ID type* rhsExpr*)
  ;
  
// code block
code :   LBRACE_LPAREN argdefs xreturns? RPAREN codestmtlist RBRACE -> ^(CODE ^(PROTO xreturns? argdefs*) codestmtlist*)  
    ;

argdefs: (argdef ( COMMA argdef)* COMMA?)?                        -> argdef* 
    ;

argdef:  protoargdef
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
      | returnExpr
      ;

varDecl: ID COLON_EQUALS assignExpr         -> ^(DEFINE ID TYPE assignExpr)
    | ID COLON type (EQUALS assignExpr)?  -> ^(DEFINE ID type assignExpr*)
    ;

returnExpr : RETURN assignExpr?           -> ^(RETURN assignExpr?)
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

