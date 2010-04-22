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
  POSTINC;
  POSTDEC;
  PREINC;
  PREDEC;
  
  LIT;
  
  IDREF;
  IDLIST;
  
  LABEL;
  GOTO;
  BLOCK;
  
  TUPLE;
  
  LABELSTMT;
  BINDING;
  
  IDEXPR;
  FIELDREF;
  ARRAY;
  INDEX;
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
    | FORWARD ID (COMMA ID)* SEMI -> ^(FORWARD ID)+
    | rhsExpr                  SEMI  -> ^(EXPR rhsExpr)
    | (LBRACE ) => xscope 
    ;

toplevelvalue : (LBRACE ) => xscope
    | selector
    | rhsExpr
    | data
    | macro
     
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

listiterable : ( code | macro ) ;
    
list : LBRACKET listitems RBRACKET     -> ^(LIST listitems*)
    ;
    
listitems: (listitem ( COMMA listitem )* COMMA?)?    
  ;
        
listitem : toplevelvalue ;
    
  
// code block

code : CODE proto? LBRACE codestmtlist RBRACE -> ^(CODE proto? codestmtlist*)  
    ;

// macro code block
macro : MACRO proto ? LBRACE codestmtlist RBRACE -> ^(MACRO proto? codestmtlist*)  
    ;

// argument definitions:  allow a list of names separated with commas,
// or a list of declarations, types, and initializers (with multiple args per type allowed) separated with semicolons
argdefs options {  backtrack = true; } :
  | argdefsWithTypes 
  | argdefWithType? 
  |  argdefsWithNames 
  ;
    
argdefsWithTypes: (argdefWithType ( SEMI argdefWithType)+ SEMI?)        -> argdefWithType* 
    ;

// make use of antlr's node replication
argdefWithType:  ID (COMMA ID)* (COLON type)?   -> ^(ARGDEF ID type* )+
    | MACRO ID (COMMA ID)* (COLON type)? (EQUALS init=rhsExpr)?    -> ^(ARGDEF MACRO ID type* $init?)+
  ;

argdefsWithNames :  (argdefWithName ( COMMA argdefWithName)+ COMMA?)    -> argdefWithName* 
    ;
argdefWithName: ID   -> ^(ARGDEF ID )
  ;

// prototype, as for a type or code block (no defaults allowed)
proto : LPAREN argdefs xreturns? RPAREN                   -> ^(PROTO xreturns? argdefs*)
    ;

xreturns: ARROW type      -> type
  | ARROW argtuple           -> argtuple
  | ARROW NIL            -> ^(TYPE NIL)
  ;

argtuple : LPAREN tupleargdefs RPAREN    -> ^(TUPLE tupleargdefs)
  ;

tupleargdefs: (tupleargdef ( COMMA tupleargdef)+ )                        -> tupleargdef* 
    ;

tupleargdef: type    -> type
  | QUESTION        -> ^(TYPE NIL)
  |                 -> ^(TYPE NIL)
  ;
  
type : ( baseType LBRACKET ) => baseType LBRACKET rhsExpr? RBRACKET   -> ^(TYPE ^(ARRAY baseType rhsExpr? ) ) 
     | baseType -> baseType 
  ;

baseType : idOrScopeRef AMP -> ^(TYPE ^(REF idOrScopeRef) ) 
     | idOrScopeRef -> ^(TYPE idOrScopeRef)  
     | CODE proto? -> ^(TYPE ^(CODE proto?) )
  ;

codestmtlist:  codeStmt (SEMI codeStmt?)*  ->  ^(STMTLIST codeStmt*)
    | -> ^(STMTLIST) 
    ;
    
codeStmt : labelStmt codeStmtExpr  -> ^(LABELSTMT labelStmt codeStmtExpr)
      | codeStmtExpr -> codeStmtExpr
      ;

codeStmtExpr options { backtrack=true; } :
     varDecl    -> varDecl
      | assignStmt    -> assignStmt
      | rhsExpr       ->  ^(STMTEXPR rhsExpr)
      | ( LBRACE ) => blockStmt         -> blockStmt
      | gotoStmt      -> gotoStmt
      //| withStmt      -> withStmt
      | controlStmt      -> controlStmt
      ;

varDecl: ID COLON_EQUALS assignExpr         -> ^(ALLOC ID TYPE assignExpr)
    | idTuple COLON_EQUALS assignExpr         -> ^(ALLOC idTuple TYPE assignExpr)
    | ID COLON type (EQUALS assignExpr)?  -> ^(ALLOC ID type assignExpr*)
    | idTuple COLON type (EQUALS assignExpr)?  -> ^(ALLOC idTuple type assignExpr*)
    | ID (COMMA ID)+ COLON_EQUALS PLUS? assignExpr (COMMA assignExpr)* 
        -> ^(ALLOC ^(LIST ID+) TYPE PLUS? ^(LIST assignExpr+))
    | ID (COMMA ID)+ COLON type (EQUALS PLUS? assignExpr (COMMA assignExpr)*)?  
        -> ^(ALLOC ^(LIST ID+) type PLUS? ^(LIST assignExpr+)?)
    ;

assignStmt : (idExpr EQUALS) => idExpr EQUALS assignExpr        -> ^(ASSIGN idExpr assignExpr)
    | idTuple EQUALS assignExpr               -> ^(ASSIGN idTuple assignExpr)
    | idExpr (COMMA idExpr)+ EQUALS PLUS? assignExpr (COMMA assignExpr)*       
        -> ^(ASSIGN ^(LIST idExpr+) PLUS? ^(LIST assignExpr+))
    ;
      
assignExpr : (idExpr EQUALS) => idExpr EQUALS assignExpr        -> ^(ASSIGN idExpr assignExpr)
    | (idTuple EQUALS) => idTuple EQUALS assignExpr               -> ^(ASSIGN idTuple assignExpr)
    | rhsExpr                             -> rhsExpr
    ;


controlStmt : doWhile | whileDo | repeat | forIter ;

doWhile : DO codeStmtExpr WHILE rhsExpr   -> ^(DO codeStmtExpr rhsExpr)
  ;

whileDo : WHILE rhsExpr DO codeStmtExpr   -> ^(WHILE rhsExpr codeStmtExpr)
  ;
  
repeat : REPEAT rhsExpr DO codeStmt         -> ^(REPEAT rhsExpr codeStmt)
  ; 

forIter : FOR forIds atId? IN rhsExpr DO codeStmt       -> ^(FOR forIds atId? rhsExpr codeStmt)
  ; 

forIds : ID (AND ID)* -> ID+ ;

atId : AT ID    -> ^(AT ID) 
  ;
  
breakStmt : BREAK rhsExpr ->  ^(BREAK rhsExpr)
  ; 

//valueCond : rhsExpr WHEN rhsExpr ELSE rhsExpr  -> ^(WHEN rhsExpr+) 
//  | UNTIL rhsExpr THEN rhsExpr ELSE rhsExpr    -> ^(UNTIL rhsExpr+)
//  | WITH rhsExpr                               -> rhsExpr
//    ;  
   
labelStmt: ATSIGN ID COLON                    -> ^(LABEL ID)
  ;
gotoStmt: GOTO idOrScopeRef (IF rhsExpr)?           -> ^(GOTO idOrScopeRef rhsExpr?)
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
    
funcCall : LPAREN arglist RPAREN   ->     ^(CALL arglist) 
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

condStar: cond -> cond
   | IF ifExprs -> ifExprs
    ;

//
//  if TEST then VALUE [elif TEST then VALUE]+ else VALUE
//
ifExprs : thenClause elses -> ^(CONDLIST thenClause elses)
  ;
thenClause : t=condStmtExpr THEN v=condStmtExpr   -> ^(CONDTEST $t $v) 
    ; 
elses : elif* elseClause    -> elif* elseClause 
    ;
elif : ELIF t=condStmtExpr THEN v=condStmtExpr  -> ^(CONDTEST $t $v )
    ;
elseClause : ELSE condStmtExpr       -> ^(CONDTEST ^(LIT TRUE) condStmtExpr)
   | FI -> ^(CONDTEST ^(LIT TRUE) ^(LIT NIL))
  ;

condStmtExpr : arg | breakStmt ;
    
cond:    ( logor  -> logor )
      ( QUESTION t=logor COLON f=logor -> ^(COND $cond $t $f ) )*
;

logor : ( logand  -> logand )
      ( OR r=logand -> ^(OR $logor $r) )*
      ;
logand : ( not -> not )
      ( AND r=not -> ^(AND $logand $r) ) *
      ;
              
not :  comp     -> comp
    | NOT u=comp     -> ^(NOT $u )
    ;
    
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


unary:  MINUS u=unary -> ^(NEG $u )
      | TILDE u=unary     -> ^(INV $u )
      | ( atom PLUSPLUS) => a=atom PLUSPLUS  -> ^(POSTINC $a)
      | ( atom MINUSMINUS) => a=atom MINUSMINUS -> ^(POSTDEC $a)
      | ( atom        -> atom )        
      | PLUSPLUS a=atom   -> ^(PREINC $a)
      | MINUSMINUS a=atom -> ^(PREDEC $a)
;
atom :
      NUMBER                          -> ^(LIT NUMBER)
    |   FALSE                         -> ^(LIT FALSE)
    |   TRUE                          -> ^(LIT TRUE)
    |   CHAR_LITERAL                  -> ^(LIT CHAR_LITERAL)
    |   STRING_LITERAL                -> ^(LIT STRING_LITERAL)
    |   NIL                          -> ^(LIT NIL)
    |   ( STAR idOrScopeRef LPAREN) => STAR idOrScopeRef f=funcCall  -> ^(INLINE idOrScopeRef $f)
    |   idExpr                  -> idExpr
    |   ( tuple ) => tuple                          -> tuple
    |   LPAREN assignExpr RPAREN               -> assignExpr
    |    code                           -> code   
    ;

// an idOrScopeRef can have dotted parts that interpreted either as scope derefs or field refs,
// so appendIdModifiers skips field derefs the first time to avoid complaints about ambiguities 
idExpr : idOrScopeRef appendIdModifiers? -> ^(IDEXPR idOrScopeRef appendIdModifiers*) ;

appendIdModifiers : nonFieldIdModifier idModifier* ;

nonFieldIdModifier : funcCall | arrayIndex ;
idModifier : fieldRef | funcCall | arrayIndex ;

fieldRef : PERIOD ID  -> ^(FIELDREF ID) ;

arrayIndex :  LBRACKET assignExpr RBRACKET -> ^(INDEX assignExpr) ;

idOrScopeRef : ID ( PERIOD ID ) * -> ^(IDREF ID+ ) 
      | c=colons ID ( PERIOD ID ) * -> ^(IDREF {split($c.tree)} ID+) 
      ;

colons : (COLON | COLONS)+ ;

data : DATA LBRACE fieldDecl* RBRACE  -> ^(DATA fieldDecl*) ;

staticVarDecl : STATIC varDecl -> ^(STATIC varDecl) ;

fieldDecl : staticVarDecl SEMI -> staticVarDecl 
    | varDecl SEMI -> varDecl 
    | fieldIdRef SEMI -> fieldIdRef;

fieldIdRef : ID (COMMA ID)* -> ^(ALLOC ID)+ ;

//LBRACE_LPAREN : '{(';
//LBRACE_STAR : '{*';
//LBRACE_STAR_LPAREN : '{*(';
LBRACE_LESS : '{<';

FORWARD : 'forward';
STATIC : 'static';

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
ATSIGN : '@';
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
PLUSPLUS : '++';
MINUSMINUS : '--';

POINTS : '->';
BAR_BAR : '||';

SELECT : 'select';

IF: 'if';
THEN : 'then';
ELSE : 'else';
ELIF : 'elif';
FI : 'fi';
DO : 'do';
WHILE : 'while';
AT : 'at';
WHEN : 'when';
UNTIL : 'until';
BREAK : 'break';
REPEAT : 'repeat';

//RETURN : 'return';
CODE : 'code';
DATA : 'data';
MACRO : 'macro';
FOR : 'for';
IN : 'in';
GOTO: 'goto';
FALSE: 'false';
TRUE: 'true';
NIL: ' nil';

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

      