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
  
  CONDLIST;
  CONDTEST;
  
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
  
  NEG;
  INV;
  
  LIT;
  
  IDREF;
  IDLIST;
  
  LABEL;
  GOTO;
  BLOCK;
  
  TUPLE;
  
  LABELSTMT;
  BINDING;
}

@header {
package org.ejs.eulang.parser;
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
    
toplevelstat:  (ID EQUALS) => ID EQUALS toplevelvalue     SEMI  -> ^(DEFINE ID toplevelvalue)
    |  (ID COLON) => ID COLON type (EQUALS toplevelvalue)?     SEMI  -> ^(ALLOC ID type toplevelvalue?)
    |  (ID COLON_EQUALS) => ID COLON_EQUALS rhsExpr  SEMI  -> ^(ALLOC ID TYPE rhsExpr)
    | rhsExpr                  SEMI  -> ^(EXPR rhsExpr)
    | xscope
    ;

toplevelvalue : xscope
    | (  LPAREN (RPAREN | ID) ) => proto     
    | selector
    | rhsExpr
    ;

// one or more selectors
selector: LBRACKET selectors RBRACKET    -> ^(LIST selectors*) 
    ;

selectors: (selectoritem ( COMMA selectoritem )* COMMA?)?    -> selectoritem*
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

// macro code block
macro : MACRO ( LPAREN optargdefs xreturns? RPAREN ) ? LBRACE codestmtlist RBRACE -> ^(MACRO ^(PROTO xreturns? optargdefs*) codestmtlist*)  
    ;

// prototype, as for a type or code block (no defaults allowed)
proto : LPAREN argdefs xreturns? RPAREN                   -> ^(PROTO xreturns? argdefs*)
    ;
argdefs: (argdef ( COMMA argdef)* COMMA?)?                        -> argdef* 
    ;

argdef: MACRO? ID (COLON type)?    -> ^(ARGDEF MACRO? ID type* )
  ;

xreturns: ARROW type      -> type
  | ARROW argtuple           -> argtuple
  | ARROW NULL            -> ^(TYPE NULL)
  ;

argtuple : LPAREN tupleargdefs RPAREN    -> ^(TUPLE tupleargdefs)
  ;

tupleargdefs: (tupleargdef ( COMMA tupleargdef)+ )                        -> tupleargdef* 
    ;

tupleargdef: type    -> type
  | QUESTION        -> ^(TYPE NULL)
  |                 -> ^(TYPE NULL)
  ;
  
// args inside a prototype, which have optional initializers
optargdefs: (optargdef ( COMMA optargdef)* COMMA?)?                        -> optargdef* 
    ;

optargdef: ID (COLON type)?   -> ^(ARGDEF ID type* )
    | MACRO ID (COLON type)? (EQUALS init=rhsExpr)?    -> ^(ARGDEF MACRO ID type* $init?)
  ;
  
type :  ( idOrScopeRef -> ^(TYPE idOrScopeRef) )  ( AMP -> ^(TYPE ^(REF idOrScopeRef) ) )? 
     | CODE proto? -> ^(TYPE ^(CODE proto?) )
  ;

codestmtlist:  /*(codeStmt ) =>*/ codeStmt (SEMI codeStmt?)*  ->  ^(STMTLIST codeStmt*)
    //| rhsExpr          -> ^(STMTLIST ^(RETURN rhsExpr))
    | -> ^(STMTLIST) 
    ;
    
codeStmt : labelStmt codeStmtExpr  -> ^(LABELSTMT labelStmt codeStmtExpr)
      | codeStmtExpr -> codeStmtExpr
      ;

codeStmtExpr : varDecl    -> varDecl
      | assignStmt    -> assignStmt
      //| returnStmt SEMI   -> returnStmt
      | rhsExpr       ->  ^(STMTEXPR rhsExpr)
      | blockStmt         -> blockStmt
      //| gotoStmt SEMI     -> gotoStmt
      | gotoStmt      -> gotoStmt
      //| labelStmt     -> labelStmt
      //| withStmt      -> withStmt
      ;

varDecl: ID COLON_EQUALS assignExpr         -> ^(ALLOC ID TYPE assignExpr)
    | idTuple COLON_EQUALS assignExpr         -> ^(ALLOC idTuple TYPE assignExpr)
    | ID COLON type (EQUALS assignExpr)?  -> ^(ALLOC ID type assignExpr*)
    | idTuple COLON type (EQUALS assignExpr)?  -> ^(ALLOC idTuple type assignExpr*)
    ;

//returnStmt : RETURN assignExpr?           -> ^(RETURN assignExpr?)
      //;

assignStmt : idOrScopeRef EQUALS assignExpr        -> ^(ASSIGN idOrScopeRef assignExpr)
    | idTuple EQUALS assignExpr               -> ^(ASSIGN idTuple assignExpr)
    ;
      
assignExpr : idOrScopeRef EQUALS assignExpr        -> ^(ASSIGN idOrScopeRef assignExpr)
    | idTuple EQUALS assignExpr               -> ^(ASSIGN idTuple assignExpr)
    | rhsExpr                             -> rhsExpr
    ;

labelStmt: AT ID COLON                    -> ^(LABEL ID)
  ;
//gotoStmt: GOTO idOrScopeRef ( COMMA rhsExpr )?           -> ^(GOTO idOrScopeRef rhsExpr?)
gotoStmt: AT idOrScopeRef                -> ^(GOTO idOrScopeRef)
    | AT idOrScopeRef LPAREN assignExpr RPAREN   -> ^(GOTO idOrScopeRef assignExpr)
  ;
  
blockStmt: LBRACE codestmtlist RBRACE     -> ^(BLOCK codestmtlist)
  ;

tuple : LPAREN tupleEntries RPAREN      -> ^(TUPLE tupleEntries+) 
  ;
  
tupleEntries : assignExpr (COMMA assignExpr)+  -> assignExpr+ 
; 

idTuple : LPAREN idTupleEntries RPAREN      -> ^(TUPLE idTupleEntries+) 
  ;
  
idTupleEntries : idOrScopeRef (COMMA idOrScopeRef)+  -> idOrScopeRef+ 
; 

rhsExpr :   condStar -> condStar
    ;
    
funcCall : idOrScopeRef LPAREN arglist RPAREN   ->     ^(CALL idOrScopeRef arglist) 
    ;


arglist: (arg ( COMMA arg)* COMMA?)?                        -> ^(ARGLIST arg*) 
    ;

arg:  assignExpr                    -> ^(EXPR assignExpr)
  | LBRACE codestmtlist RBRACE      -> ^(EXPR ^(CODE codestmtlist) )
  | gotoStmt                        -> ^(EXPR gotoStmt)
   ;

//
//  Expressions
//

//
//  With
//
//  with <expr> as <type> [and <expr2> as <type2>] => <expr> [else <stmt>]
//

withStmt : WITH bindings ARROW b=rhsExpr (ELSE e=codeStmtExpr)? -> ^(WITH bindings $b $e?) 
  ;

bindings: binding (AND binding)* -> binding+
  ;  
binding: rhsExpr AS type   -> ^(BINDING type rhsExpr) 
  ;  

// multi-argument cond  

condStar: cond -> cond
   | SELECT LBRACKET condTests RBRACKET -> condTests
   | SELECT condTestExprs -> condTestExprs
    ;
condTests : condTest (BAR_BAR condTest)* BAR_BAR? condFinalOrEmpty -> ^(CONDLIST condTest* condFinalOrEmpty)
  ;    
condTest : (cond THEN) => cond THEN arg -> ^(CONDTEST cond arg)
  ;
condFinal : ELSE arg -> ^(CONDTEST ^(LIT TRUE) arg)
    ;
condFinalOrEmpty : condFinal -> condFinal
    | -> ^(CONDTEST ^(LIT TRUE) ^(LIT NULL))
    ;

condTestExprs : condTest (BAR_BAR condTest)* condFinal -> ^(CONDLIST condTest* condFinal)
  ;    

cond:    ( logor  -> logor )
      ( QUESTION t=logor COLON f=logor -> ^(COND $cond $t $f ) )*
;

logor : ( logand  -> logand )
      ( OR r=logand -> ^(OR $logor $r) )*
      ;
logand : ( comp -> comp )
      ( AND r=comp -> ^(AND $logand $r) ) *
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
      | NOT u=unary     -> ^(NOT $u )
      | TILDE u=unary     -> ^(INV $u )
;
atom :
      NUMBER                          -> ^(LIT NUMBER)
    |   FALSE                         -> ^(LIT FALSE)
    |   TRUE                          -> ^(LIT TRUE)
    |   CHAR_LITERAL                  -> ^(LIT CHAR_LITERAL)
    |   STRING_LITERAL                -> ^(LIT STRING_LITERAL)
    |   NULL                          -> ^(LIT NULL)
    |   ( STAR idOrScopeRef LPAREN) => STAR f=funcCall  -> ^(INLINE $f)
    |   (idOrScopeRef LPAREN ) => funcCall   -> funcCall
    //|   INVOKE                        -> ^(INVOKE)
    //|   RECURSE LPAREN arglist RPAREN   -> ^(RECURSE arglist) 
    |   idOrScopeRef                  -> idOrScopeRef
    |   ( tuple ) => tuple                          -> tuple
    |   LPAREN assignExpr RPAREN               -> assignExpr
    |   code                           -> code   
    |   macro                           -> macro   
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
//EXCL : '!';
NOT : 'not';
TILDE : '~';
AT : '@';
AMP : '&'; 
BAR : '|';
CARET : '^';
SEMI : ';';
QUESTION : '?';
AND : 'and';
OR : 'or';
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
ARROW : '=>' ;
PERIOD : '.';

POINTS : '->';
BAR_BAR : '||';

SELECT : 'select';
THEN : 'then';
ELSE : 'else';
//RETURN : 'return';
CODE : 'code';
DATA : 'data';
MACRO : 'macro';
FOR : 'for';
IN : 'in';
//GOTO: 'goto';
FALSE: 'false';
TRUE: 'true';
NULL: ' null';

WITH: 'with';
AS: 'as';
END: 'end';

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

      