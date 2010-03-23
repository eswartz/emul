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
  MACRO;
  STMTLIST;
  PROTO;
  ARGLIST;
  ARGDEF;
  
  REF;
  
  EXPR;
  DEFINE_ASSIGN;
  ASSIGN;
  DEFINE;
  LIST;
  TYPE;
  
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
package org.ejs.eulang;
import java.util.HashMap;
} 

@members {
    public String getTokenErrorDisplay(Token t) {
        return '\'' + t.getText() + '\'';
    }


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

// inline code block
macro :   LBRACE_STAR_LPAREN argdefs xreturns? RPAREN codestmtlist RBRACE -> ^(MACRO ^(PROTO xreturns? argdefs*) codestmtlist*)  
    ;

argdefs: (argdef ( COMMA argdef)* COMMA?)?                        -> argdef* 
    ;

argdef:  protoargdef
    | ID EQUALS assignExpr    -> ^(ARGDEF ID TYPE assignExpr)
  ;

xreturns: RETURNS type      -> type
  ;
  
type :  ( ID -> ^(TYPE ID) )
      ( AMP -> ^(TYPE ^(REF ID) ) )? 
      
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
    ;

idOrScopeRef : ID -> ^(IDREF ID ) 
      | SCOPEREF -> ^(IDREF SCOPEREF)
      | COLONS ( ( SCOPEREF -> ^(IDREF COLONS SCOPEREF) ) | ( ID -> ^(IDREF COLONS ID) ) )
      | COLON+ ( ( SCOPEREF -> ^(IDREF COLON+ SCOPEREF) ) | ( ID -> ^(IDREF COLON+ ID) ) )
      ;
      