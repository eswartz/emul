grammar Eulang;
options {
 ASTLabelType=CommonTree;
  output=AST;
  language=Java;
}

tokens {
  SCOPE;
  ADDSCOPE;
  
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

  CAST;  
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
  POINTER;
  
  DEREF;
  ADDRREF;
  ADDROF;
  
  INITEXPR;
  INITLIST;
  
  INSTANCE;
  GENERIC;
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
    
toplevelstat:  defineStmt
    |  (ID COLON) => ID COLON type (EQUALS rhsExprOrInitList)?     SEMI  -> ^(ALLOC ID type rhsExprOrInitList?)
    |  (ID COLON_EQUALS) => ID COLON_EQUALS rhsExprOrInitList  SEMI  -> ^(ALLOC ID TYPE rhsExprOrInitList)
    | FORWARD ID (COMMA ID)* SEMI -> ^(FORWARD ID)+
    | rhsExpr                  SEMI  -> ^(EXPR rhsExpr)
    | (LBRACE ) => xscope 
    ;

rhsExprOrInitList : rhsExpr | initList ;

defineStmt : (ID EQUALS LBRACKET) => ID EQUALS LBRACKET idlistOrEmpty RBRACKET  toplevelvalue     SEMI  -> ^(DEFINE ID idlistOrEmpty toplevelvalue) 
    | (ID EQUALS) => ID EQUALS toplevelvalue     SEMI  -> ^(DEFINE ID toplevelvalue)
  ;

toplevelvalue : (LBRACE ) => xscope
    | ID PLUS data -> ^(ADDSCOPE ID data)
    | ID PLUS xscope -> ^(ADDSCOPE ID xscope)
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
        
selectoritem :  macro | rhsExpr | listCompr;

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
idlistOrEmpty : idlist -> idlist | -> ^(IDLIST) ;

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
  | argdefWithType
  |  argdefsWithNames 
  ;
    
argdefsWithTypes: (argdefWithType ( SEMI argdefWithType)+ SEMI?)        -> argdefWithType* 
    ;

// make use of antlr's node replication
argdefWithType:  ATSIGN? ID (COMMA ID)* (COLON type)?   -> ^(ARGDEF ATSIGN? ID type* )+
    | MACRO ID (COMMA ID)* (COLON type)? (EQUALS init=rhsExpr)?    -> ^(ARGDEF MACRO ID type* $init?)+
  ;

argdefsWithNames :  (argdefWithName ( COMMA argdefWithName)+ COMMA?)    -> argdefWithName* 
    ;
argdefWithName: ATSIGN? ID   -> ^(ARGDEF ATSIGN? ID )
  ;

// prototype, as for a type or code block (no defaults allowed)
proto : LPAREN argdefs xreturns? RPAREN                   -> ^(PROTO xreturns? argdefs*)
    ;

xreturns : ARROW type      -> type
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
  
// note: the warning can be ignored: we want to consume as many arraySuffs
// as possible, otherwise multi-dimensional arrays are not constructed as
// desired.
type : 
    (nonArrayType -> nonArrayType)
     ( 
	     (
	       (arraySuff+) => arraySuff+ -> ^(TYPE ^(ARRAY $type arraySuff+))
	      )
	      |
	      ( 
	       LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^(TYPE ^(ARRAY $type rhsExpr+ ) ) 
	      ) 
	      | 
	      (
	        CARET -> ^(TYPE ^(POINTER $type))
	      )
      )*
  ; 
  
nonArrayType :  
    (idOrScopeRef instantiation) => idOrScopeRef instantiation -> ^(INSTANCE idOrScopeRef instantiation )
    |  ( idOrScopeRef -> ^(TYPE idOrScopeRef) )
     | ( CODE proto? -> ^(TYPE ^(CODE proto?) ) )
     
  ; 
arraySuff : LBRACKET rhsExpr RBRACKET -> rhsExpr
    | LBRACKET RBRACKET -> FALSE
    ;
codestmtlist:  codeStmt (SEMI codeStmt?)*  ->  ^(STMTLIST codeStmt*)
    | -> ^(STMTLIST) 
    ;
    
codeStmt : labelStmt codeStmtExpr  -> ^(LABELSTMT labelStmt codeStmtExpr)
      | codeStmtExpr -> codeStmtExpr
      ;

codeStmtExpr :
      ( varDecl) => varDecl    -> varDecl
      | (assignStmt) => assignStmt    -> assignStmt
      | rhsExpr       ->  ^(STMTEXPR rhsExpr)
      | ( LBRACE ) => blockStmt         -> blockStmt
      | gotoStmt      -> gotoStmt
      //| withStmt      -> withStmt
      | controlStmt      -> controlStmt
      ;

varDecl: ID COLON_EQUALS assignOrInitExpr         -> ^(ALLOC ID TYPE assignOrInitExpr)
    | idTuple COLON_EQUALS assignOrInitExpr         -> ^(ALLOC idTuple TYPE assignOrInitExpr)
    | ID COLON type (EQUALS assignOrInitExpr)?  -> ^(ALLOC ID type assignOrInitExpr*)
    | idTuple COLON type (EQUALS assignOrInitExpr)?  -> ^(ALLOC idTuple type assignOrInitExpr*)
    | ID (COMMA ID)+ COLON_EQUALS PLUS? assignOrInitExpr (COMMA assignOrInitExpr)* 
        -> ^(ALLOC ^(LIST ID+) TYPE PLUS? ^(LIST assignOrInitExpr+))
    | ID (COMMA ID)+ COLON type (EQUALS PLUS? assignOrInitExpr (COMMA assignOrInitExpr)*)?  
        -> ^(ALLOC ^(LIST ID+) type PLUS? ^(LIST assignOrInitExpr+)?)
    ;

// assignment statement (statement level) 
assignStmt : (atom assignEqOp) => atom assignEqOp assignOrInitExpr        -> ^(ASSIGN assignEqOp atom assignOrInitExpr)
    | idTuple EQUALS assignOrInitExpr               -> ^(ASSIGN EQUALS idTuple assignOrInitExpr)
    // possible multi-assign statement
    | (atom (COMMA atom)+ assignEqOp ) => atom (COMMA atom)+ assignEqOp PLUS? assignOrInitExpr (COMMA assignOrInitExpr)*       
        -> ^(ASSIGN assignEqOp ^(LIST atom+) PLUS? ^(LIST assignOrInitExpr+))
    ;
      
assignOrInitExpr : assignExpr | initList ;

// assign expr
assignExpr : (atom assignEqOp) => atom assignEqOp assignExpr        -> ^(ASSIGN assignEqOp atom assignExpr)
    | (idTuple EQUALS) => idTuple EQUALS assignExpr               -> ^(ASSIGN EQUALS idTuple assignExpr)
    | rhsExpr                             -> rhsExpr
    ;

assignOp : PLUS_EQ | MINUS_EQ | STAR_EQ | SLASH_EQ | REM_EQ | UDIV_EQ | UREM_EQ | MOD_EQ
  | AND_EQ | OR_EQ | XOR_EQ | LSHIFT_EQ | RSHIFT_EQ | URSHIFT_EQ | CLSHIFT_EQ | CRSHIFT_EQ ;
assignEqOp : EQUALS | assignOp ;

initList : LBRACKET (initExpr (COMMA initExpr)*)? RBRACKET     -> ^(INITLIST initExpr* ) ;
initExpr 
    : (rhsExpr) => e=rhsExpr                                              -> ^(INITEXPR $e) 
    | PERIOD ID EQUALS ei=initElement                                  -> ^(INITEXPR $ei ID) 
    | (LBRACKET i=rhsExpr RBRACKET) => LBRACKET i=rhsExpr RBRACKET EQUALS ei=initElement                  -> ^(INITEXPR $ei $i)
    | initList 
    ;

initElement : rhsExpr | initList;    

controlStmt : doWhile | whileDo | repeat | forIter ;

doWhile : DO codeStmtExpr WHILE rhsExpr   -> ^(DO codeStmtExpr rhsExpr)
  ;

whileDo : WHILE rhsExpr DO codeStmtExpr   -> ^(WHILE rhsExpr codeStmtExpr)
  ;
  
repeat : REPEAT rhsExpr DO codeStmt         -> ^(REPEAT rhsExpr codeStmt)
  ; 

forIter : FOR forIds forMovement? IN rhsExpr DO codeStmt       -> ^(FOR ^(LIST forIds) forMovement? rhsExpr codeStmt)
  ; 

forIds : ID (AND ID)* -> ID+ ;

forMovement : atId | stepping ;

stepping : BY rhsExpr -> ^(BY rhsExpr); 

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

//withStmt : WITH bindings ARROW b=rhsExpr (ELSE e=codeStmtExpr)? -> ^(WITH bindings $b $e?) 
//  ;

//bindings: binding (AND binding)* -> binding+
//  ;  
//binding: condStar AS type   -> ^(BINDING type condStar) 
 // ;  

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
      | COMPULE r=bitor    -> ^(COMPULE $comp $r)
      | COMPUGE r=bitor    -> ^(COMPUGE $comp $r)
      | LESS r=bitor     -> ^(LESS $comp $r)
      | ULESS r=bitor     -> ^(ULESS $comp $r)
      | GREATER r=bitor    -> ^(GREATER $comp $r)
      | UGREATER r=bitor    -> ^(UGREATER $comp $r)
      )*
;               


bitor: ( bitxor      -> bitxor )       
      ( BAR r=bitxor  -> ^(BITOR $bitor $r) ) *
;
bitxor: ( bitand      -> bitand )       
      ( TILDE r=bitand  -> ^(BITXOR $bitxor $r) )*
;
bitand: ( shift      -> shift )       
      ( AMP r=shift  -> ^(BITAND $bitand $r) )*
;

shift:  ( factor        -> factor )         
      ( ( LSHIFT r=factor   -> ^(LSHIFT $shift $r) ) 
      | ( RSHIFT r=factor   -> ^(RSHIFT $shift $r) )
      | ( URSHIFT r=factor   -> ^(URSHIFT $shift $r) )
      | ( CRSHIFT r=factor   -> ^(CRSHIFT $shift $r) )
      | ( CLSHIFT r=factor   -> ^(CLSHIFT $shift $r) )
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
        | REM r=unary            -> ^(REM $term $r)
        | UDIV r=unary            -> ^(UDIV $term $r)
        | UREM r=unary            -> ^(UREM $term $r)
        | MOD r=unary            -> ^(MOD $term $r)
        )*                        
    ; 


unary:  MINUS u=unary -> ^(NEG $u )
      | TILDE u=unary     -> ^(INV $u )
      //| (noIdAtom idModifier) => noIdAtom idModifier+ -> ^(IDEXPR noIdAtom idModifier+)
      //| idExpr      -> idExpr 
      | ( atom PLUSPLUS) => a=atom PLUSPLUS  -> ^(POSTINC $a)
      | ( atom MINUSMINUS) => a=atom MINUSMINUS -> ^(POSTDEC $a)
      | ( atom        -> atom )        
      | PLUSPLUS a=atom   -> ^(PREINC $a)
      | MINUSMINUS a=atom -> ^(PREDEC $a)
      | AMP atom        -> ^(ADDROF atom)
;

atom :
  ( 
      NUMBER                          -> ^(LIT NUMBER)
    |   FALSE                         -> ^(LIT FALSE)
    |   TRUE                          -> ^(LIT TRUE)
    |   CHAR_LITERAL                  -> ^(LIT CHAR_LITERAL)
    |   STRING_LITERAL                -> ^(LIT STRING_LITERAL)
    |   NIL                          -> ^(LIT NIL)
    |   idExpr                          -> idExpr
    |   ( tuple ) => tuple                          -> tuple
    |   LPAREN a1=assignExpr RPAREN               -> $a1
    |    code                           -> code
    |   ( STAR idOrScopeRef LPAREN) => STAR idOrScopeRef  LPAREN arglist RPAREN  -> ^(INLINE idOrScopeRef arglist)
   ) 

    ( 
      ( PERIOD ID  -> ^(FIELDREF $atom ID) )
    | (  LPAREN arglist RPAREN   -> ^(CALL $atom arglist) )
    | arrayAccess   -> ^(INDEX $atom arrayAccess)
    //| ( LBRACKET assignExpr RBRACKET  -> ^(INDEX $atom assignExpr) )
    | ( CARET -> ^(DEREF $atom) )
    | ( ( LBRACE type RBRACE) -> ^(CAST type $atom ) ) 
    )*

    ( 
      AS type -> ^(CAST type $atom) 
    )?  
    ;

arrayAccess : LBRACKET assignExpr (COMMA assignExpr)* RBRACKET  -> assignExpr+
  ;
idExpr :
    ( idOrScopeRef -> idOrScopeRef) 
    ( (instantiation ) => instantiation -> ^(INSTANCE $idExpr instantiation) ) ?
    ;

instantiation : LESS (instanceExpr (COMMA instanceExpr)*)? GREATER   -> ^(LIST instanceExpr*) 
  ; 

instanceExpr options { backtrack=true;} : type | atom ;
idOrScopeRef : ID ( PERIOD ID ) * -> ^(IDREF ID+ ) 
      | c=colons ID ( PERIOD ID ) * -> ^(IDREF {split($c.tree)} ID+) 
      ;

colons : (COLON | COLONS )+ ;

data : DATA LBRACE fieldDecl* RBRACE  -> ^(DATA fieldDecl*) ;

staticVarDecl : STATIC varDecl -> ^(STATIC varDecl) ;

fieldDecl : staticVarDecl SEMI -> staticVarDecl 
    | varDecl SEMI -> varDecl 
    | defineStmt
    //| fieldIdRef SEMI -> fieldIdRef
    ;

fieldIdRef : ID (COMMA ID)* -> ^(ALLOC ID)+ ;

//LBRACE_LPAREN : '{(';
//LBRACE_STAR : '{*';
//LBRACE_STAR_LPAREN : '{*(';
//LBRACE_LESS : '{<';

FORWARD : 'forward';
STATIC : 'static';

COLON : ':';
//COLON_COLON : '::';
COMMA : ',';
EQUALS : '=';
COLON_EQUALS : ':=';
COLON_COLON_EQUALS : '::=';
PLUS : '+';
PLUS_EQ : '+=';
MINUS : '-';
MINUS_EQ : '-=';
STAR : '*';
STAR_EQ : '*=';
SLASH : '/';
SLASH_EQ : '/=';
UDIV : '+/';
UDIV_EQ : '+/=';
REM : '\\';
REM_EQ : '\\=';
UREM : '+\\';
UREM_EQ : '+\\=';
MOD : '%';
MOD_EQ : '%=';
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
AND_EQ : '&=';
OR : 'or';
OR_EQ : '|=';
//XOR : 'xor';
XOR_EQ : '~=';
COMPEQ : '==';
COMPNE : '!=';
COMPGE : '>=';
COMPUGE : '+>=';
COMPLE : '<=';
COMPULE : '+<=';
GREATER : '>';
UGREATER : '+>';
LESS : '<';
ULESS : '+<';
LSHIFT : '<<';
LSHIFT_EQ : '<<=';
RSHIFT : '>>';
RSHIFT_EQ : '>>=';
URSHIFT : '+>>';
URSHIFT_EQ : '+>>=';
CRSHIFT : '>>|';
CRSHIFT_EQ : '>>|=';
CLSHIFT : '<<|';
CLSHIFT_EQ : '<<|=';
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
BY : 'by';

//RETURN : 'return';
CODE : 'code';
DATA : 'data';
MACRO : 'macro';
FOR : 'for';
IN : 'in';
GOTO: 'goto';
FALSE: 'false';
TRUE: 'true';
NIL: 'nil';

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

      