// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-04-17 09:43:08

package org.ejs.eulang.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class EulangParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCOPE", "LIST_COMPREHENSION", "CODE", "MACRO", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "ALLOC", "ASSIGN", "DEFINE", "EXPR", "LIST", "TYPE", "STMTEXPR", "CONDLIST", "CONDTEST", "CALL", "INLINE", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NEG", "INV", "LIT", "IDREF", "IDLIST", "LABEL", "GOTO", "BLOCK", "TUPLE", "LABELSTMT", "BINDING", "ID", "EQUALS", "SEMI", "COLON", "COLON_EQUALS", "LBRACKET", "RBRACKET", "COMMA", "LBRACE", "RBRACE", "FOR", "IN", "LPAREN", "RPAREN", "ARROW", "NIL", "QUESTION", "AMP", "DO", "WHILE", "REPEAT", "AND", "AT", "BREAK", "WHEN", "ELSE", "UNTIL", "THEN", "WITH", "ATSIGN", "IF", "AS", "ELIF", "FI", "OR", "NOT", "COMPEQ", "COMPNE", "COMPLE", "COMPGE", "LESS", "GREATER", "BAR", "CARET", "LSHIFT", "RSHIFT", "URSHIFT", "PLUS", "MINUS", "STAR", "SLASH", "BACKSLASH", "PERCENT", "UMOD", "TILDE", "NUMBER", "FALSE", "TRUE", "CHAR_LITERAL", "STRING_LITERAL", "PERIOD", "COLONS", "LBRACE_LPAREN", "LBRACE_STAR", "LBRACE_STAR_LPAREN", "COLON_COLON_EQUALS", "HASH", "POINTS", "BAR_BAR", "SELECT", "DATA", "END", "IDSUFFIX", "LETTERLIKE", "DIGIT", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT"
    };
    public static final int CONDTEST=21;
    public static final int STAR=94;
    public static final int WHILE=64;
    public static final int MOD=33;
    public static final int DO=63;
    public static final int ARGLIST=10;
    public static final int EQUALS=46;
    public static final int NOT=80;
    public static final int EOF=-1;
    public static final int BREAK=68;
    public static final int TYPE=18;
    public static final int CODE=6;
    public static final int LBRACKET=50;
    public static final int TUPLE=42;
    public static final int RPAREN=58;
    public static final int STRING_LITERAL=104;
    public static final int GREATER=86;
    public static final int COMPLE=83;
    public static final int CARET=88;
    public static final int LESS=85;
    public static final int LBRACE_STAR_LPAREN=109;
    public static final int ATSIGN=74;
    public static final int GOTO=40;
    public static final int SELECT=114;
    public static final int LABELSTMT=43;
    public static final int RBRACE=54;
    public static final int STMTEXPR=19;
    public static final int PERIOD=105;
    public static final int UMOD=98;
    public static final int INV=35;
    public static final int LSHIFT=89;
    public static final int ELSE=70;
    public static final int LBRACE_LPAREN=107;
    public static final int NUMBER=100;
    public static final int LIT=36;
    public static final int UDIV=32;
    public static final int LIST=17;
    public static final int MUL=30;
    public static final int ARGDEF=11;
    public static final int FI=78;
    public static final int ELIF=77;
    public static final int WS=121;
    public static final int BITOR=26;
    public static final int NIL=60;
    public static final int UNTIL=71;
    public static final int STMTLIST=8;
    public static final int OR=79;
    public static final int ALLOC=13;
    public static final int IDLIST=38;
    public static final int REPEAT=65;
    public static final int INLINE=23;
    public static final int CALL=22;
    public static final int END=116;
    public static final int FALSE=101;
    public static final int BACKSLASH=96;
    public static final int BINDING=44;
    public static final int BAR_BAR=113;
    public static final int LBRACE_STAR=108;
    public static final int AMP=62;
    public static final int POINTS=112;
    public static final int LBRACE=53;
    public static final int MULTI_COMMENT=123;
    public static final int FOR=55;
    public static final int SUB=29;
    public static final int ID=45;
    public static final int AND=66;
    public static final int DEFINE=15;
    public static final int BITAND=25;
    public static final int LPAREN=57;
    public static final int IF=75;
    public static final int COLONS=106;
    public static final int COLON_COLON_EQUALS=110;
    public static final int AT=67;
    public static final int AS=76;
    public static final int CONDLIST=20;
    public static final int IDSUFFIX=117;
    public static final int SLASH=95;
    public static final int EXPR=16;
    public static final int IN=56;
    public static final int THEN=72;
    public static final int SCOPE=4;
    public static final int COMMA=52;
    public static final int BITXOR=27;
    public static final int TILDE=99;
    public static final int PLUS=92;
    public static final int SINGLE_COMMENT=122;
    public static final int DIGIT=119;
    public static final int RBRACKET=51;
    public static final int RSHIFT=90;
    public static final int WITH=73;
    public static final int ADD=28;
    public static final int COMPGE=84;
    public static final int PERCENT=97;
    public static final int LETTERLIKE=118;
    public static final int LIST_COMPREHENSION=5;
    public static final int HASH=111;
    public static final int MINUS=93;
    public static final int TRUE=102;
    public static final int SEMI=47;
    public static final int REF=12;
    public static final int COLON=48;
    public static final int COLON_EQUALS=49;
    public static final int NEWLINE=120;
    public static final int QUESTION=61;
    public static final int CHAR_LITERAL=103;
    public static final int LABEL=39;
    public static final int WHEN=69;
    public static final int BLOCK=41;
    public static final int NEG=34;
    public static final int ASSIGN=14;
    public static final int URSHIFT=91;
    public static final int ARROW=59;
    public static final int COMPEQ=81;
    public static final int IDREF=37;
    public static final int DIV=31;
    public static final int COND=24;
    public static final int MACRO=7;
    public static final int PROTO=9;
    public static final int COMPNE=82;
    public static final int DATA=115;
    public static final int BAR=87;

    // delegates
    // delegators


        public EulangParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public EulangParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return EulangParser.tokenNames; }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g"; }


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


    public static class prog_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prog"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:90:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:90:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:90:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog304);
            toplevelstmts1=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog306); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "prog"

    public static class toplevelstmts_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstmts"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:1: toplevelstmts : ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstat_return toplevelstat3 = null;


        RewriteRuleSubtreeStream stream_toplevelstat=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:14: ( ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:16: ( toplevelstat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:16: ( toplevelstat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=CODE && LA1_0<=MACRO)||LA1_0==ID||LA1_0==COLON||LA1_0==LBRACE||LA1_0==LPAREN||LA1_0==NIL||LA1_0==IF||LA1_0==NOT||(LA1_0>=MINUS && LA1_0<=STAR)||(LA1_0>=TILDE && LA1_0<=STRING_LITERAL)||LA1_0==COLONS) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:16: toplevelstat
            	    {
            	    pushFollow(FOLLOW_toplevelstat_in_toplevelstmts335);
            	    toplevelstat3=toplevelstat();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_toplevelstat.add(toplevelstat3.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);



            // AST REWRITE
            // elements: toplevelstat
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 93:35: -> ^( STMTLIST ( toplevelstat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:38: ^( STMTLIST ( toplevelstat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:49: ( toplevelstat )*
                while ( stream_toplevelstat.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstat.nextTree());

                }
                stream_toplevelstat.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelstmts"

    public static class toplevelstat_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstat"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:1: toplevelstat : ( ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) | ( ID COLON )=> ID COLON type ( EQUALS toplevelvalue )? SEMI -> ^( ALLOC ID type ( toplevelvalue )? ) | ( ID COLON_EQUALS )=> ID COLON_EQUALS rhsExpr SEMI -> ^( ALLOC ID TYPE rhsExpr ) | rhsExpr SEMI -> ^( EXPR rhsExpr ) | xscope );
    public final EulangParser.toplevelstat_return toplevelstat() throws RecognitionException {
        EulangParser.toplevelstat_return retval = new EulangParser.toplevelstat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID4=null;
        Token EQUALS5=null;
        Token SEMI7=null;
        Token ID8=null;
        Token COLON9=null;
        Token EQUALS11=null;
        Token SEMI13=null;
        Token ID14=null;
        Token COLON_EQUALS15=null;
        Token SEMI17=null;
        Token SEMI19=null;
        EulangParser.toplevelvalue_return toplevelvalue6 = null;

        EulangParser.type_return type10 = null;

        EulangParser.toplevelvalue_return toplevelvalue12 = null;

        EulangParser.rhsExpr_return rhsExpr16 = null;

        EulangParser.rhsExpr_return rhsExpr18 = null;

        EulangParser.xscope_return xscope20 = null;


        CommonTree ID4_tree=null;
        CommonTree EQUALS5_tree=null;
        CommonTree SEMI7_tree=null;
        CommonTree ID8_tree=null;
        CommonTree COLON9_tree=null;
        CommonTree EQUALS11_tree=null;
        CommonTree SEMI13_tree=null;
        CommonTree ID14_tree=null;
        CommonTree COLON_EQUALS15_tree=null;
        CommonTree SEMI17_tree=null;
        CommonTree SEMI19_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_toplevelvalue=new RewriteRuleSubtreeStream(adaptor,"rule toplevelvalue");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:13: ( ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) | ( ID COLON )=> ID COLON type ( EQUALS toplevelvalue )? SEMI -> ^( ALLOC ID type ( toplevelvalue )? ) | ( ID COLON_EQUALS )=> ID COLON_EQUALS rhsExpr SEMI -> ^( ALLOC ID TYPE rhsExpr ) | rhsExpr SEMI -> ^( EXPR rhsExpr ) | xscope )
            int alt3=5;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==EQUALS) && (synpred1_Eulang())) {
                    alt3=1;
                }
                else if ( (LA3_1==COLON) && (synpred2_Eulang())) {
                    alt3=2;
                }
                else if ( (LA3_1==COLON_EQUALS) && (synpred3_Eulang())) {
                    alt3=3;
                }
                else if ( (LA3_1==SEMI||LA3_1==LPAREN||(LA3_1>=QUESTION && LA3_1<=AMP)||LA3_1==AND||LA3_1==OR||(LA3_1>=COMPEQ && LA3_1<=UMOD)||LA3_1==PERIOD) ) {
                    alt3=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
                }
                break;
            case CODE:
            case MACRO:
            case COLON:
            case LPAREN:
            case NIL:
            case IF:
            case NOT:
            case MINUS:
            case STAR:
            case TILDE:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case COLONS:
                {
                alt3=4;
                }
                break;
            case LBRACE:
                {
                alt3=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:16: ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI
                    {
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat376); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID4);

                    EQUALS5=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat378); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS5);

                    pushFollow(FOLLOW_toplevelvalue_in_toplevelstat380);
                    toplevelvalue6=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat386); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI7);



                    // AST REWRITE
                    // elements: ID, toplevelvalue
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 96:65: -> ^( DEFINE ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:68: ^( DEFINE ID toplevelvalue )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:8: ( ID COLON )=> ID COLON type ( EQUALS toplevelvalue )? SEMI
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat414); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID8);

                    COLON9=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelstat416); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON9);

                    pushFollow(FOLLOW_type_in_toplevelstat418);
                    type10=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type10.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:36: ( EQUALS toplevelvalue )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==EQUALS) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:37: EQUALS toplevelvalue
                            {
                            EQUALS11=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat421); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS11);

                            pushFollow(FOLLOW_toplevelvalue_in_toplevelstat423);
                            toplevelvalue12=toplevelvalue();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue12.getTree());

                            }
                            break;

                    }

                    SEMI13=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat431); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI13);



                    // AST REWRITE
                    // elements: type, toplevelvalue, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 97:70: -> ^( ALLOC ID type ( toplevelvalue )? )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:73: ^( ALLOC ID type ( toplevelvalue )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:89: ( toplevelvalue )?
                        if ( stream_toplevelvalue.hasNext() ) {
                            adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

                        }
                        stream_toplevelvalue.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:98:8: ( ID COLON_EQUALS )=> ID COLON_EQUALS rhsExpr SEMI
                    {
                    ID14=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat462); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID14);

                    COLON_EQUALS15=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelstat464); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS15);

                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat466);
                    rhsExpr16=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr16.getTree());
                    SEMI17=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat469); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI17);



                    // AST REWRITE
                    // elements: ID, rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 98:60: -> ^( ALLOC ID TYPE rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:98:63: ^( ALLOC ID TYPE rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:99:7: rhsExpr SEMI
                    {
                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat490);
                    rhsExpr18=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr18.getTree());
                    SEMI19=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat509); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI19);



                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 99:38: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:99:41: ^( EXPR rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:100:7: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelstat526);
                    xscope20=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope20.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelstat"

    public static class toplevelvalue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelvalue"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:103:1: toplevelvalue : ( xscope | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr );
    public final EulangParser.toplevelvalue_return toplevelvalue() throws RecognitionException {
        EulangParser.toplevelvalue_return retval = new EulangParser.toplevelvalue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.xscope_return xscope21 = null;

        EulangParser.proto_return proto22 = null;

        EulangParser.selector_return selector23 = null;

        EulangParser.rhsExpr_return rhsExpr24 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:103:15: ( xscope | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr )
            int alt4=4;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:103:17: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelvalue539);
                    xscope21=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope21.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:104:7: ( LPAREN ( RPAREN | ID ) )=> proto
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_proto_in_toplevelvalue564);
                    proto22=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, proto22.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:105:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_toplevelvalue577);
                    selector23=selector();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selector23.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:106:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue585);
                    rhsExpr24=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr24.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "toplevelvalue"

    public static class selector_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selector"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:110:1: selector : LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET25=null;
        Token RBRACKET27=null;
        EulangParser.selectors_return selectors26 = null;


        CommonTree LBRACKET25_tree=null;
        CommonTree RBRACKET27_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectors=new RewriteRuleSubtreeStream(adaptor,"rule selectors");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:110:9: ( LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:110:11: LBRACKET selectors RBRACKET
            {
            LBRACKET25=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector598); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET25);

            pushFollow(FOLLOW_selectors_in_selector600);
            selectors26=selectors();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectors.add(selectors26.getTree());
            RBRACKET27=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector602); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET27);



            // AST REWRITE
            // elements: selectors
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 110:42: -> ^( LIST ( selectors )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:110:45: ^( LIST ( selectors )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:110:52: ( selectors )*
                while ( stream_selectors.hasNext() ) {
                    adaptor.addChild(root_1, stream_selectors.nextTree());

                }
                stream_selectors.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selector"

    public static class selectors_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectors"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:1: selectors : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* ;
    public final EulangParser.selectors_return selectors() throws RecognitionException {
        EulangParser.selectors_return retval = new EulangParser.selectors_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA29=null;
        Token COMMA31=null;
        EulangParser.selectoritem_return selectoritem28 = null;

        EulangParser.selectoritem_return selectoritem30 = null;


        CommonTree COMMA29_tree=null;
        CommonTree COMMA31_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_selectoritem=new RewriteRuleSubtreeStream(adaptor,"rule selectoritem");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:10: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=CODE && LA7_0<=MACRO)||LA7_0==FOR) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:13: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectors628);
                    selectoritem28=selectoritem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem28.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:26: ( COMMA selectoritem )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            int LA5_1 = input.LA(2);

                            if ( ((LA5_1>=CODE && LA5_1<=MACRO)||LA5_1==FOR) ) {
                                alt5=1;
                            }


                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:28: COMMA selectoritem
                    	    {
                    	    COMMA29=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors632); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA29);

                    	    pushFollow(FOLLOW_selectoritem_in_selectors634);
                    	    selectoritem30=selectoritem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem30.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:50: ( COMMA )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==COMMA) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:50: COMMA
                            {
                            COMMA31=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors639); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA31);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: selectoritem
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 113:62: -> ( selectoritem )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:113:65: ( selectoritem )*
                while ( stream_selectoritem.hasNext() ) {
                    adaptor.addChild(root_0, stream_selectoritem.nextTree());

                }
                stream_selectoritem.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectors"

    public static class selectoritem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectoritem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:1: selectoritem : ( listCompr | code | macro );
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.listCompr_return listCompr32 = null;

        EulangParser.code_return code33 = null;

        EulangParser.macro_return macro34 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:13: ( listCompr | code | macro )
            int alt8=3;
            switch ( input.LA(1) ) {
            case FOR:
                {
                alt8=1;
                }
                break;
            case CODE:
                {
                alt8=2;
                }
                break;
            case MACRO:
                {
                alt8=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:15: listCompr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_listCompr_in_selectoritem668);
                    listCompr32=listCompr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listCompr32.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:27: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_selectoritem672);
                    code33=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code33.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:34: macro
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_macro_in_selectoritem676);
                    macro34=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro34.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "selectoritem"

    public static class xscope_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xscope"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:120:1: xscope : LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) ;
    public final EulangParser.xscope_return xscope() throws RecognitionException {
        EulangParser.xscope_return retval = new EulangParser.xscope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE35=null;
        Token RBRACE37=null;
        EulangParser.toplevelstmts_return toplevelstmts36 = null;


        CommonTree LBRACE35_tree=null;
        CommonTree RBRACE37_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmts=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmts");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:120:8: ( LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:120:10: LBRACE toplevelstmts RBRACE
            {
            LBRACE35=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope687); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE35);

            pushFollow(FOLLOW_toplevelstmts_in_xscope689);
            toplevelstmts36=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmts.add(toplevelstmts36.getTree());
            RBRACE37=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope691); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE37);



            // AST REWRITE
            // elements: toplevelstmts
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 120:41: -> ^( SCOPE ( toplevelstmts )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:120:44: ^( SCOPE ( toplevelstmts )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:120:52: ( toplevelstmts )*
                while ( stream_toplevelstmts.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstmts.nextTree());

                }
                stream_toplevelstmts.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "xscope"

    public static class listCompr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listCompr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:125:1: listCompr : ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) ;
    public final EulangParser.listCompr_return listCompr() throws RecognitionException {
        EulangParser.listCompr_return retval = new EulangParser.listCompr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON39=null;
        EulangParser.forIn_return forIn38 = null;

        EulangParser.listiterable_return listiterable40 = null;


        CommonTree COLON39_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_forIn=new RewriteRuleSubtreeStream(adaptor,"rule forIn");
        RewriteRuleSubtreeStream stream_listiterable=new RewriteRuleSubtreeStream(adaptor,"rule listiterable");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:125:10: ( ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:125:12: ( forIn )+ COLON listiterable
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:125:12: ( forIn )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==FOR) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:125:12: forIn
            	    {
            	    pushFollow(FOLLOW_forIn_in_listCompr718);
            	    forIn38=forIn();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_forIn.add(forIn38.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);

            COLON39=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr721); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON39);

            pushFollow(FOLLOW_listiterable_in_listCompr723);
            listiterable40=listiterable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listiterable.add(listiterable40.getTree());


            // AST REWRITE
            // elements: forIn, listiterable
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 125:42: -> ^( LIST_COMPREHENSION ( forIn )+ listiterable )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:125:45: ^( LIST_COMPREHENSION ( forIn )+ listiterable )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST_COMPREHENSION, "LIST_COMPREHENSION"), root_1);

                if ( !(stream_forIn.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_forIn.hasNext() ) {
                    adaptor.addChild(root_1, stream_forIn.nextTree());

                }
                stream_forIn.reset();
                adaptor.addChild(root_1, stream_listiterable.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listCompr"

    public static class forIn_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forIn"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:128:1: forIn : FOR idlist IN list -> ^( FOR idlist list ) ;
    public final EulangParser.forIn_return forIn() throws RecognitionException {
        EulangParser.forIn_return retval = new EulangParser.forIn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR41=null;
        Token IN43=null;
        EulangParser.idlist_return idlist42 = null;

        EulangParser.list_return list44 = null;


        CommonTree FOR41_tree=null;
        CommonTree IN43_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_list=new RewriteRuleSubtreeStream(adaptor,"rule list");
        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:128:7: ( FOR idlist IN list -> ^( FOR idlist list ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:128:9: FOR idlist IN list
            {
            FOR41=(Token)match(input,FOR,FOLLOW_FOR_in_forIn755); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR41);

            pushFollow(FOLLOW_idlist_in_forIn757);
            idlist42=idlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idlist.add(idlist42.getTree());
            IN43=(Token)match(input,IN,FOLLOW_IN_in_forIn759); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN43);

            pushFollow(FOLLOW_list_in_forIn761);
            list44=list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_list.add(list44.getTree());


            // AST REWRITE
            // elements: FOR, list, idlist
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 128:33: -> ^( FOR idlist list )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:128:36: ^( FOR idlist list )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idlist.nextTree());
                adaptor.addChild(root_1, stream_list.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "forIn"

    public static class idlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:1: idlist : ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) ;
    public final EulangParser.idlist_return idlist() throws RecognitionException {
        EulangParser.idlist_return retval = new EulangParser.idlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID45=null;
        Token COMMA46=null;
        Token ID47=null;

        CommonTree ID45_tree=null;
        CommonTree COMMA46_tree=null;
        CommonTree ID47_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:8: ( ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:10: ID ( COMMA ID )*
            {
            ID45=(Token)match(input,ID,FOLLOW_ID_in_idlist786); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID45);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:13: ( COMMA ID )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==COMMA) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:14: COMMA ID
            	    {
            	    COMMA46=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist789); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA46);

            	    ID47=(Token)match(input,ID,FOLLOW_ID_in_idlist791); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID47);


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);



            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 130:28: -> ^( IDLIST ( ID )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:31: ^( IDLIST ( ID )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDLIST, "IDLIST"), root_1);

                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    adaptor.addChild(root_1, stream_ID.nextNode());

                }
                stream_ID.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idlist"

    public static class listiterable_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listiterable"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:1: listiterable : ( code | macro | proto ) ;
    public final EulangParser.listiterable_return listiterable() throws RecognitionException {
        EulangParser.listiterable_return retval = new EulangParser.listiterable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code48 = null;

        EulangParser.macro_return macro49 = null;

        EulangParser.proto_return proto50 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:14: ( ( code | macro | proto ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:16: ( code | macro | proto )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:16: ( code | macro | proto )
            int alt11=3;
            switch ( input.LA(1) ) {
            case CODE:
                {
                alt11=1;
                }
                break;
            case MACRO:
                {
                alt11=2;
                }
                break;
            case LPAREN:
                {
                alt11=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:18: code
                    {
                    pushFollow(FOLLOW_code_in_listiterable820);
                    code48=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code48.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:25: macro
                    {
                    pushFollow(FOLLOW_macro_in_listiterable824);
                    macro49=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro49.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:33: proto
                    {
                    pushFollow(FOLLOW_proto_in_listiterable828);
                    proto50=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, proto50.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listiterable"

    public static class list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "list"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:1: list : LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) ;
    public final EulangParser.list_return list() throws RecognitionException {
        EulangParser.list_return retval = new EulangParser.list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET51=null;
        Token RBRACKET53=null;
        EulangParser.listitems_return listitems52 = null;


        CommonTree LBRACKET51_tree=null;
        CommonTree RBRACKET53_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_listitems=new RewriteRuleSubtreeStream(adaptor,"rule listitems");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:6: ( LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:8: LBRACKET listitems RBRACKET
            {
            LBRACKET51=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list843); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET51);

            pushFollow(FOLLOW_listitems_in_list845);
            listitems52=listitems();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listitems.add(listitems52.getTree());
            RBRACKET53=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list847); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET53);



            // AST REWRITE
            // elements: listitems
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 135:40: -> ^( LIST ( listitems )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:43: ^( LIST ( listitems )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:50: ( listitems )*
                while ( stream_listitems.hasNext() ) {
                    adaptor.addChild(root_1, stream_listitems.nextTree());

                }
                stream_listitems.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "list"

    public static class listitems_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listitems"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:1: listitems : ( listitem ( COMMA listitem )* ( COMMA )? )? ;
    public final EulangParser.listitems_return listitems() throws RecognitionException {
        EulangParser.listitems_return retval = new EulangParser.listitems_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA55=null;
        Token COMMA57=null;
        EulangParser.listitem_return listitem54 = null;

        EulangParser.listitem_return listitem56 = null;


        CommonTree COMMA55_tree=null;
        CommonTree COMMA57_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:10: ( ( listitem ( COMMA listitem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( ((LA14_0>=CODE && LA14_0<=MACRO)||LA14_0==ID||LA14_0==COLON||LA14_0==LBRACKET||LA14_0==LBRACE||LA14_0==LPAREN||LA14_0==NIL||LA14_0==IF||LA14_0==NOT||(LA14_0>=MINUS && LA14_0<=STAR)||(LA14_0>=TILDE && LA14_0<=STRING_LITERAL)||LA14_0==COLONS) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems877);
                    listitem54=listitem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem54.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:22: ( COMMA listitem )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==COMMA) ) {
                            int LA12_1 = input.LA(2);

                            if ( ((LA12_1>=CODE && LA12_1<=MACRO)||LA12_1==ID||LA12_1==COLON||LA12_1==LBRACKET||LA12_1==LBRACE||LA12_1==LPAREN||LA12_1==NIL||LA12_1==IF||LA12_1==NOT||(LA12_1>=MINUS && LA12_1<=STAR)||(LA12_1>=TILDE && LA12_1<=STRING_LITERAL)||LA12_1==COLONS) ) {
                                alt12=1;
                            }


                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:24: COMMA listitem
                    	    {
                    	    COMMA55=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems881); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA55_tree = (CommonTree)adaptor.create(COMMA55);
                    	    adaptor.addChild(root_0, COMMA55_tree);
                    	    }
                    	    pushFollow(FOLLOW_listitem_in_listitems883);
                    	    listitem56=listitem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem56.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:42: ( COMMA )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==COMMA) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:42: COMMA
                            {
                            COMMA57=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems888); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA57_tree = (CommonTree)adaptor.create(COMMA57);
                            adaptor.addChild(root_0, COMMA57_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listitems"

    public static class listitem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listitem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:1: listitem : toplevelvalue ;
    public final EulangParser.listitem_return listitem() throws RecognitionException {
        EulangParser.listitem_return retval = new EulangParser.listitem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelvalue_return toplevelvalue58 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:10: ( toplevelvalue )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:12: toplevelvalue
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelvalue_in_listitem914);
            toplevelvalue58=toplevelvalue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelvalue58.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "listitem"

    public static class code_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "code"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:1: code : CODE ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE59=null;
        Token LPAREN60=null;
        Token RPAREN63=null;
        Token LBRACE64=null;
        Token RBRACE66=null;
        EulangParser.optargdefs_return optargdefs61 = null;

        EulangParser.xreturns_return xreturns62 = null;

        EulangParser.codestmtlist_return codestmtlist65 = null;


        CommonTree CODE59_tree=null;
        CommonTree LPAREN60_tree=null;
        CommonTree RPAREN63_tree=null;
        CommonTree LBRACE64_tree=null;
        CommonTree RBRACE66_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        RewriteRuleSubtreeStream stream_optargdefs=new RewriteRuleSubtreeStream(adaptor,"rule optargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:6: ( CODE ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:8: CODE ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE
            {
            CODE59=(Token)match(input,CODE,FOLLOW_CODE_in_code932); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CODE.add(CODE59);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:13: ( LPAREN optargdefs ( xreturns )? RPAREN )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==LPAREN) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:15: LPAREN optargdefs ( xreturns )? RPAREN
                    {
                    LPAREN60=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_code936); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN60);

                    pushFollow(FOLLOW_optargdefs_in_code938);
                    optargdefs61=optargdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdefs.add(optargdefs61.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:33: ( xreturns )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==ARROW) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:33: xreturns
                            {
                            pushFollow(FOLLOW_xreturns_in_code940);
                            xreturns62=xreturns();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_xreturns.add(xreturns62.getTree());

                            }
                            break;

                    }

                    RPAREN63=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_code943); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN63);


                    }
                    break;

            }

            LBRACE64=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_code949); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE64);

            pushFollow(FOLLOW_codestmtlist_in_code951);
            codestmtlist65=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist65.getTree());
            RBRACE66=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code953); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE66);



            // AST REWRITE
            // elements: codestmtlist, CODE, optargdefs, xreturns
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 146:81: -> ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:84: ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:91: ^( PROTO ( xreturns )? ( optargdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:99: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:109: ( optargdefs )*
                while ( stream_optargdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_optargdefs.nextTree());

                }
                stream_optargdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:122: ( codestmtlist )*
                while ( stream_codestmtlist.hasNext() ) {
                    adaptor.addChild(root_1, stream_codestmtlist.nextTree());

                }
                stream_codestmtlist.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "code"

    public static class macro_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "macro"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:1: macro : MACRO ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.macro_return macro() throws RecognitionException {
        EulangParser.macro_return retval = new EulangParser.macro_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MACRO67=null;
        Token LPAREN68=null;
        Token RPAREN71=null;
        Token LBRACE72=null;
        Token RBRACE74=null;
        EulangParser.optargdefs_return optargdefs69 = null;

        EulangParser.xreturns_return xreturns70 = null;

        EulangParser.codestmtlist_return codestmtlist73 = null;


        CommonTree MACRO67_tree=null;
        CommonTree LPAREN68_tree=null;
        CommonTree RPAREN71_tree=null;
        CommonTree LBRACE72_tree=null;
        CommonTree RBRACE74_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_MACRO=new RewriteRuleTokenStream(adaptor,"token MACRO");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        RewriteRuleSubtreeStream stream_optargdefs=new RewriteRuleSubtreeStream(adaptor,"rule optargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:7: ( MACRO ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:9: MACRO ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE
            {
            MACRO67=(Token)match(input,MACRO,FOLLOW_MACRO_in_macro988); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_MACRO.add(MACRO67);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:15: ( LPAREN optargdefs ( xreturns )? RPAREN )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==LPAREN) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:17: LPAREN optargdefs ( xreturns )? RPAREN
                    {
                    LPAREN68=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_macro992); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN68);

                    pushFollow(FOLLOW_optargdefs_in_macro994);
                    optargdefs69=optargdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdefs.add(optargdefs69.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:35: ( xreturns )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==ARROW) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:35: xreturns
                            {
                            pushFollow(FOLLOW_xreturns_in_macro996);
                            xreturns70=xreturns();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_xreturns.add(xreturns70.getTree());

                            }
                            break;

                    }

                    RPAREN71=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_macro999); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN71);


                    }
                    break;

            }

            LBRACE72=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_macro1005); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE72);

            pushFollow(FOLLOW_codestmtlist_in_macro1007);
            codestmtlist73=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist73.getTree());
            RBRACE74=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_macro1009); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE74);



            // AST REWRITE
            // elements: codestmtlist, MACRO, optargdefs, xreturns
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 150:83: -> ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:86: ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_MACRO.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:94: ^( PROTO ( xreturns )? ( optargdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:102: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:112: ( optargdefs )*
                while ( stream_optargdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_optargdefs.nextTree());

                }
                stream_optargdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:125: ( codestmtlist )*
                while ( stream_codestmtlist.hasNext() ) {
                    adaptor.addChild(root_1, stream_codestmtlist.nextTree());

                }
                stream_codestmtlist.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "macro"

    public static class proto_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "proto"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:1: proto : LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN75=null;
        Token RPAREN78=null;
        EulangParser.argdefs_return argdefs76 = null;

        EulangParser.xreturns_return xreturns77 = null;


        CommonTree LPAREN75_tree=null;
        CommonTree RPAREN78_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:7: ( LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:9: LPAREN argdefs ( xreturns )? RPAREN
            {
            LPAREN75=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto1044); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN75);

            pushFollow(FOLLOW_argdefs_in_proto1046);
            argdefs76=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs76.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:24: ( xreturns )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==ARROW) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:24: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto1048);
                    xreturns77=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns77.getTree());

                    }
                    break;

            }

            RPAREN78=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto1051); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN78);



            // AST REWRITE
            // elements: argdefs, xreturns
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 154:59: -> ^( PROTO ( xreturns )? ( argdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:62: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:70: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:80: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_1, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "proto"

    public static class argdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:1: argdefs : ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* ;
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA80=null;
        Token COMMA82=null;
        EulangParser.argdef_return argdef79 = null;

        EulangParser.argdef_return argdef81 = null;


        CommonTree COMMA80_tree=null;
        CommonTree COMMA82_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdef=new RewriteRuleSubtreeStream(adaptor,"rule argdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:8: ( ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==MACRO||LA22_0==ID) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:11: argdef ( COMMA argdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_argdef_in_argdefs1093);
                    argdef79=argdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_argdef.add(argdef79.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:18: ( COMMA argdef )*
                    loop20:
                    do {
                        int alt20=2;
                        int LA20_0 = input.LA(1);

                        if ( (LA20_0==COMMA) ) {
                            int LA20_1 = input.LA(2);

                            if ( (LA20_1==MACRO||LA20_1==ID) ) {
                                alt20=1;
                            }


                        }


                        switch (alt20) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:20: COMMA argdef
                    	    {
                    	    COMMA80=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1097); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA80);

                    	    pushFollow(FOLLOW_argdef_in_argdefs1099);
                    	    argdef81=argdef();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_argdef.add(argdef81.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop20;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:35: ( COMMA )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==COMMA) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:35: COMMA
                            {
                            COMMA82=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1103); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA82);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: argdef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 156:67: -> ( argdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:70: ( argdef )*
                while ( stream_argdef.hasNext() ) {
                    adaptor.addChild(root_0, stream_argdef.nextTree());

                }
                stream_argdef.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argdefs"

    public static class argdef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:1: argdef : ( MACRO )? ID ( COLON type )? -> ^( ARGDEF ( MACRO )? ID ( type )* ) ;
    public final EulangParser.argdef_return argdef() throws RecognitionException {
        EulangParser.argdef_return retval = new EulangParser.argdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MACRO83=null;
        Token ID84=null;
        Token COLON85=null;
        EulangParser.type_return type86 = null;


        CommonTree MACRO83_tree=null;
        CommonTree ID84_tree=null;
        CommonTree COLON85_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_MACRO=new RewriteRuleTokenStream(adaptor,"token MACRO");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:7: ( ( MACRO )? ID ( COLON type )? -> ^( ARGDEF ( MACRO )? ID ( type )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:9: ( MACRO )? ID ( COLON type )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:9: ( MACRO )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==MACRO) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:9: MACRO
                    {
                    MACRO83=(Token)match(input,MACRO,FOLLOW_MACRO_in_argdef1147); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MACRO.add(MACRO83);


                    }
                    break;

            }

            ID84=(Token)match(input,ID,FOLLOW_ID_in_argdef1150); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID84);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:19: ( COLON type )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==COLON) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:20: COLON type
                    {
                    COLON85=(Token)match(input,COLON,FOLLOW_COLON_in_argdef1153); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON85);

                    pushFollow(FOLLOW_type_in_argdef1155);
                    type86=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type86.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: MACRO, type, ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 159:36: -> ^( ARGDEF ( MACRO )? ID ( type )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:39: ^( ARGDEF ( MACRO )? ID ( type )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:48: ( MACRO )?
                if ( stream_MACRO.hasNext() ) {
                    adaptor.addChild(root_1, stream_MACRO.nextNode());

                }
                stream_MACRO.reset();
                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:58: ( type )*
                while ( stream_type.hasNext() ) {
                    adaptor.addChild(root_1, stream_type.nextTree());

                }
                stream_type.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argdef"

    public static class xreturns_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xreturns"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:1: xreturns : ( ARROW type -> type | ARROW argtuple -> argtuple | ARROW NIL -> ^( TYPE NIL ) );
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ARROW87=null;
        Token ARROW89=null;
        Token ARROW91=null;
        Token NIL92=null;
        EulangParser.type_return type88 = null;

        EulangParser.argtuple_return argtuple90 = null;


        CommonTree ARROW87_tree=null;
        CommonTree ARROW89_tree=null;
        CommonTree ARROW91_tree=null;
        CommonTree NIL92_tree=null;
        RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleSubtreeStream stream_argtuple=new RewriteRuleSubtreeStream(adaptor,"rule argtuple");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:9: ( ARROW type -> type | ARROW argtuple -> argtuple | ARROW NIL -> ^( TYPE NIL ) )
            int alt25=3;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==ARROW) ) {
                switch ( input.LA(2) ) {
                case NIL:
                    {
                    alt25=3;
                    }
                    break;
                case CODE:
                case ID:
                case COLON:
                case COLONS:
                    {
                    alt25=1;
                    }
                    break;
                case LPAREN:
                    {
                    alt25=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 1, input);

                    throw nvae;
                }

            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:11: ARROW type
                    {
                    ARROW87=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns1185); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW87);

                    pushFollow(FOLLOW_type_in_xreturns1187);
                    type88=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type88.getTree());


                    // AST REWRITE
                    // elements: type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 162:27: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:5: ARROW argtuple
                    {
                    ARROW89=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns1202); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW89);

                    pushFollow(FOLLOW_argtuple_in_xreturns1204);
                    argtuple90=argtuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_argtuple.add(argtuple90.getTree());


                    // AST REWRITE
                    // elements: argtuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 163:30: -> argtuple
                    {
                        adaptor.addChild(root_0, stream_argtuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:5: ARROW NIL
                    {
                    ARROW91=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns1224); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW91);

                    NIL92=(Token)match(input,NIL,FOLLOW_NIL_in_xreturns1226); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL92);



                    // AST REWRITE
                    // elements: NIL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 164:26: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:29: ^( TYPE NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_NIL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "xreturns"

    public static class argtuple_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argtuple"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:1: argtuple : LPAREN tupleargdefs RPAREN -> ^( TUPLE tupleargdefs ) ;
    public final EulangParser.argtuple_return argtuple() throws RecognitionException {
        EulangParser.argtuple_return retval = new EulangParser.argtuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN93=null;
        Token RPAREN95=null;
        EulangParser.tupleargdefs_return tupleargdefs94 = null;


        CommonTree LPAREN93_tree=null;
        CommonTree RPAREN95_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleargdefs=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:10: ( LPAREN tupleargdefs RPAREN -> ^( TUPLE tupleargdefs ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:12: LPAREN tupleargdefs RPAREN
            {
            LPAREN93=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_argtuple1256); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN93);

            pushFollow(FOLLOW_tupleargdefs_in_argtuple1258);
            tupleargdefs94=tupleargdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdefs.add(tupleargdefs94.getTree());
            RPAREN95=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_argtuple1260); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN95);



            // AST REWRITE
            // elements: tupleargdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 167:42: -> ^( TUPLE tupleargdefs )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:45: ^( TUPLE tupleargdefs )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TUPLE, "TUPLE"), root_1);

                adaptor.addChild(root_1, stream_tupleargdefs.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "argtuple"

    public static class tupleargdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleargdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:1: tupleargdefs : ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* ;
    public final EulangParser.tupleargdefs_return tupleargdefs() throws RecognitionException {
        EulangParser.tupleargdefs_return retval = new EulangParser.tupleargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA97=null;
        EulangParser.tupleargdef_return tupleargdef96 = null;

        EulangParser.tupleargdef_return tupleargdef98 = null;


        CommonTree COMMA97_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_tupleargdef=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:13: ( ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:15: ( tupleargdef ( COMMA tupleargdef )+ )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:15: ( tupleargdef ( COMMA tupleargdef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:16: tupleargdef ( COMMA tupleargdef )+
            {
            pushFollow(FOLLOW_tupleargdef_in_tupleargdefs1282);
            tupleargdef96=tupleargdef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef96.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:28: ( COMMA tupleargdef )+
            int cnt26=0;
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==COMMA) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:30: COMMA tupleargdef
            	    {
            	    COMMA97=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleargdefs1286); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA97);

            	    pushFollow(FOLLOW_tupleargdef_in_tupleargdefs1288);
            	    tupleargdef98=tupleargdef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef98.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt26 >= 1 ) break loop26;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(26, input);
                        throw eee;
                }
                cnt26++;
            } while (true);


            }



            // AST REWRITE
            // elements: tupleargdef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 170:75: -> ( tupleargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:78: ( tupleargdef )*
                while ( stream_tupleargdef.hasNext() ) {
                    adaptor.addChild(root_0, stream_tupleargdef.nextTree());

                }
                stream_tupleargdef.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tupleargdefs"

    public static class tupleargdef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleargdef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:1: tupleargdef : ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) );
    public final EulangParser.tupleargdef_return tupleargdef() throws RecognitionException {
        EulangParser.tupleargdef_return retval = new EulangParser.tupleargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION100=null;
        EulangParser.type_return type99 = null;


        CommonTree QUESTION100_tree=null;
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:12: ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) )
            int alt27=3;
            switch ( input.LA(1) ) {
            case CODE:
            case ID:
            case COLON:
            case COLONS:
                {
                alt27=1;
                }
                break;
            case QUESTION:
                {
                alt27=2;
                }
                break;
            case COMMA:
            case RPAREN:
                {
                alt27=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }

            switch (alt27) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:14: type
                    {
                    pushFollow(FOLLOW_type_in_tupleargdef1333);
                    type99=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type99.getTree());


                    // AST REWRITE
                    // elements: type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 173:22: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:5: QUESTION
                    {
                    QUESTION100=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_tupleargdef1346); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION100);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 174:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:24: ^( TYPE NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NIL, "NIL"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:21: 
                    {

                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 175:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:24: ^( TYPE NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NIL, "NIL"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tupleargdef"

    public static class optargdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "optargdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:1: optargdefs : ( optargdef ( COMMA optargdef )* ( COMMA )? )? -> ( optargdef )* ;
    public final EulangParser.optargdefs_return optargdefs() throws RecognitionException {
        EulangParser.optargdefs_return retval = new EulangParser.optargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA102=null;
        Token COMMA104=null;
        EulangParser.optargdef_return optargdef101 = null;

        EulangParser.optargdef_return optargdef103 = null;


        CommonTree COMMA102_tree=null;
        CommonTree COMMA104_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_optargdef=new RewriteRuleSubtreeStream(adaptor,"rule optargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:11: ( ( optargdef ( COMMA optargdef )* ( COMMA )? )? -> ( optargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:13: ( optargdef ( COMMA optargdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:13: ( optargdef ( COMMA optargdef )* ( COMMA )? )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==MACRO||LA30_0==ID) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:14: optargdef ( COMMA optargdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_optargdef_in_optargdefs1403);
                    optargdef101=optargdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdef.add(optargdef101.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:24: ( COMMA optargdef )*
                    loop28:
                    do {
                        int alt28=2;
                        int LA28_0 = input.LA(1);

                        if ( (LA28_0==COMMA) ) {
                            int LA28_1 = input.LA(2);

                            if ( (LA28_1==MACRO||LA28_1==ID) ) {
                                alt28=1;
                            }


                        }


                        switch (alt28) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:26: COMMA optargdef
                    	    {
                    	    COMMA102=(Token)match(input,COMMA,FOLLOW_COMMA_in_optargdefs1407); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA102);

                    	    pushFollow(FOLLOW_optargdef_in_optargdefs1409);
                    	    optargdef103=optargdef();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_optargdef.add(optargdef103.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:44: ( COMMA )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==COMMA) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:44: COMMA
                            {
                            COMMA104=(Token)match(input,COMMA,FOLLOW_COMMA_in_optargdefs1413); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA104);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: optargdef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 179:76: -> ( optargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:79: ( optargdef )*
                while ( stream_optargdef.hasNext() ) {
                    adaptor.addChild(root_0, stream_optargdef.nextTree());

                }
                stream_optargdef.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "optargdefs"

    public static class optargdef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "optargdef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:1: optargdef : ( ID ( COLON type )? -> ^( ARGDEF ID ( type )* ) | MACRO ID ( COLON type )? ( EQUALS init= rhsExpr )? -> ^( ARGDEF MACRO ID ( type )* ( $init)? ) );
    public final EulangParser.optargdef_return optargdef() throws RecognitionException {
        EulangParser.optargdef_return retval = new EulangParser.optargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID105=null;
        Token COLON106=null;
        Token MACRO108=null;
        Token ID109=null;
        Token COLON110=null;
        Token EQUALS112=null;
        EulangParser.rhsExpr_return init = null;

        EulangParser.type_return type107 = null;

        EulangParser.type_return type111 = null;


        CommonTree ID105_tree=null;
        CommonTree COLON106_tree=null;
        CommonTree MACRO108_tree=null;
        CommonTree ID109_tree=null;
        CommonTree COLON110_tree=null;
        CommonTree EQUALS112_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_MACRO=new RewriteRuleTokenStream(adaptor,"token MACRO");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:10: ( ID ( COLON type )? -> ^( ARGDEF ID ( type )* ) | MACRO ID ( COLON type )? ( EQUALS init= rhsExpr )? -> ^( ARGDEF MACRO ID ( type )* ( $init)? ) )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==ID) ) {
                alt34=1;
            }
            else if ( (LA34_0==MACRO) ) {
                alt34=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:12: ID ( COLON type )?
                    {
                    ID105=(Token)match(input,ID,FOLLOW_ID_in_optargdef1457); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID105);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:15: ( COLON type )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==COLON) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:16: COLON type
                            {
                            COLON106=(Token)match(input,COLON,FOLLOW_COLON_in_optargdef1460); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON106);

                            pushFollow(FOLLOW_type_in_optargdef1462);
                            type107=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type107.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ID, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 182:31: -> ^( ARGDEF ID ( type )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:34: ^( ARGDEF ID ( type )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:46: ( type )*
                        while ( stream_type.hasNext() ) {
                            adaptor.addChild(root_1, stream_type.nextTree());

                        }
                        stream_type.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:7: MACRO ID ( COLON type )? ( EQUALS init= rhsExpr )?
                    {
                    MACRO108=(Token)match(input,MACRO,FOLLOW_MACRO_in_optargdef1486); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MACRO.add(MACRO108);

                    ID109=(Token)match(input,ID,FOLLOW_ID_in_optargdef1488); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID109);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:16: ( COLON type )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==COLON) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:17: COLON type
                            {
                            COLON110=(Token)match(input,COLON,FOLLOW_COLON_in_optargdef1491); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON110);

                            pushFollow(FOLLOW_type_in_optargdef1493);
                            type111=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type111.getTree());

                            }
                            break;

                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:30: ( EQUALS init= rhsExpr )?
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==EQUALS) ) {
                        alt33=1;
                    }
                    switch (alt33) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:31: EQUALS init= rhsExpr
                            {
                            EQUALS112=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_optargdef1498); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS112);

                            pushFollow(FOLLOW_rhsExpr_in_optargdef1502);
                            init=rhsExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExpr.add(init.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: init, ID, MACRO, type
                    // token labels: 
                    // rule labels: retval, init
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_init=new RewriteRuleSubtreeStream(adaptor,"rule init",init!=null?init.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 183:56: -> ^( ARGDEF MACRO ID ( type )* ( $init)? )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:59: ^( ARGDEF MACRO ID ( type )* ( $init)? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                        adaptor.addChild(root_1, stream_MACRO.nextNode());
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:77: ( type )*
                        while ( stream_type.hasNext() ) {
                            adaptor.addChild(root_1, stream_type.nextTree());

                        }
                        stream_type.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:83: ( $init)?
                        if ( stream_init.hasNext() ) {
                            adaptor.addChild(root_1, stream_init.nextTree());

                        }
                        stream_init.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "optargdef"

    public static class type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:1: type : ( ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )? | CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) );
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP114=null;
        Token CODE115=null;
        EulangParser.idOrScopeRef_return idOrScopeRef113 = null;

        EulangParser.proto_return proto116 = null;


        CommonTree AMP114_tree=null;
        CommonTree CODE115_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:6: ( ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )? | CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==ID||LA37_0==COLON||LA37_0==COLONS) ) {
                alt37=1;
            }
            else if ( (LA37_0==CODE) ) {
                alt37=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:9: ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )?
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:9: ( idOrScopeRef -> ^( TYPE idOrScopeRef ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:11: idOrScopeRef
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_type1540);
                    idOrScopeRef113=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef113.getTree());


                    // AST REWRITE
                    // elements: idOrScopeRef
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 186:24: -> ^( TYPE idOrScopeRef )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:27: ^( TYPE idOrScopeRef )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:51: ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==AMP) ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:53: AMP
                            {
                            AMP114=(Token)match(input,AMP,FOLLOW_AMP_in_type1555); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_AMP.add(AMP114);



                            // AST REWRITE
                            // elements: idOrScopeRef
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 186:57: -> ^( TYPE ^( REF idOrScopeRef ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:60: ^( TYPE ^( REF idOrScopeRef ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:67: ^( REF idOrScopeRef )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(REF, "REF"), root_2);

                                adaptor.addChild(root_2, stream_idOrScopeRef.nextTree());

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:8: CODE ( proto )?
                    {
                    CODE115=(Token)match(input,CODE,FOLLOW_CODE_in_type1581); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CODE.add(CODE115);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:13: ( proto )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==LPAREN) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:13: proto
                            {
                            pushFollow(FOLLOW_proto_in_type1583);
                            proto116=proto();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_proto.add(proto116.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: proto, CODE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 187:20: -> ^( TYPE ^( CODE ( proto )? ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:23: ^( TYPE ^( CODE ( proto )? ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:30: ^( CODE ( proto )? )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_2);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:37: ( proto )?
                        if ( stream_proto.hasNext() ) {
                            adaptor.addChild(root_2, stream_proto.nextTree());

                        }
                        stream_proto.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type"

    public static class codestmtlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codestmtlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:1: codestmtlist : ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) );
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI118=null;
        EulangParser.codeStmt_return codeStmt117 = null;

        EulangParser.codeStmt_return codeStmt119 = null;


        CommonTree SEMI118_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:13: ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) )
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( ((LA40_0>=CODE && LA40_0<=MACRO)||LA40_0==GOTO||LA40_0==ID||LA40_0==COLON||LA40_0==LBRACE||LA40_0==FOR||LA40_0==LPAREN||LA40_0==NIL||(LA40_0>=DO && LA40_0<=REPEAT)||(LA40_0>=ATSIGN && LA40_0<=IF)||LA40_0==NOT||(LA40_0>=MINUS && LA40_0<=STAR)||(LA40_0>=TILDE && LA40_0<=STRING_LITERAL)||LA40_0==COLONS) ) {
                alt40=1;
            }
            else if ( (LA40_0==RBRACE) ) {
                alt40=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }
            switch (alt40) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:35: codeStmt ( SEMI ( codeStmt )? )*
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist1611);
                    codeStmt117=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt117.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:44: ( SEMI ( codeStmt )? )*
                    loop39:
                    do {
                        int alt39=2;
                        int LA39_0 = input.LA(1);

                        if ( (LA39_0==SEMI) ) {
                            alt39=1;
                        }


                        switch (alt39) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:45: SEMI ( codeStmt )?
                    	    {
                    	    SEMI118=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist1614); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI118);

                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:50: ( codeStmt )?
                    	    int alt38=2;
                    	    int LA38_0 = input.LA(1);

                    	    if ( ((LA38_0>=CODE && LA38_0<=MACRO)||LA38_0==GOTO||LA38_0==ID||LA38_0==COLON||LA38_0==LBRACE||LA38_0==FOR||LA38_0==LPAREN||LA38_0==NIL||(LA38_0>=DO && LA38_0<=REPEAT)||(LA38_0>=ATSIGN && LA38_0<=IF)||LA38_0==NOT||(LA38_0>=MINUS && LA38_0<=STAR)||(LA38_0>=TILDE && LA38_0<=STRING_LITERAL)||LA38_0==COLONS) ) {
                    	        alt38=1;
                    	    }
                    	    switch (alt38) {
                    	        case 1 :
                    	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:50: codeStmt
                    	            {
                    	            pushFollow(FOLLOW_codeStmt_in_codestmtlist1616);
                    	            codeStmt119=codeStmt();

                    	            state._fsp--;
                    	            if (state.failed) return retval;
                    	            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt119.getTree());

                    	            }
                    	            break;

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop39;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: codeStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 190:63: -> ^( STMTLIST ( codeStmt )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:67: ^( STMTLIST ( codeStmt )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:78: ( codeStmt )*
                        while ( stream_codeStmt.hasNext() ) {
                            adaptor.addChild(root_1, stream_codeStmt.nextTree());

                        }
                        stream_codeStmt.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:7: 
                    {

                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 192:7: -> ^( STMTLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:10: ^( STMTLIST )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "codestmtlist"

    public static class codeStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codeStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:1: codeStmt : ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.labelStmt_return labelStmt120 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr121 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr122 = null;


        RewriteRuleSubtreeStream stream_labelStmt=new RewriteRuleSubtreeStream(adaptor,"rule labelStmt");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:10: ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==ATSIGN) ) {
                alt41=1;
            }
            else if ( ((LA41_0>=CODE && LA41_0<=MACRO)||LA41_0==GOTO||LA41_0==ID||LA41_0==COLON||LA41_0==LBRACE||LA41_0==FOR||LA41_0==LPAREN||LA41_0==NIL||(LA41_0>=DO && LA41_0<=REPEAT)||LA41_0==IF||LA41_0==NOT||(LA41_0>=MINUS && LA41_0<=STAR)||(LA41_0>=TILDE && LA41_0<=STRING_LITERAL)||LA41_0==COLONS) ) {
                alt41=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:12: labelStmt codeStmtExpr
                    {
                    pushFollow(FOLLOW_labelStmt_in_codeStmt1665);
                    labelStmt120=labelStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labelStmt.add(labelStmt120.getTree());
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt1667);
                    codeStmtExpr121=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr121.getTree());


                    // AST REWRITE
                    // elements: codeStmtExpr, labelStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 195:36: -> ^( LABELSTMT labelStmt codeStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:39: ^( LABELSTMT labelStmt codeStmtExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LABELSTMT, "LABELSTMT"), root_1);

                        adaptor.addChild(root_1, stream_labelStmt.nextTree());
                        adaptor.addChild(root_1, stream_codeStmtExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:9: codeStmtExpr
                    {
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt1688);
                    codeStmtExpr122=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr122.getTree());


                    // AST REWRITE
                    // elements: codeStmtExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 196:22: -> codeStmtExpr
                    {
                        adaptor.addChild(root_0, stream_codeStmtExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "codeStmt"

    public static class codeStmtExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codeStmtExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:1: codeStmtExpr : ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );
    public final EulangParser.codeStmtExpr_return codeStmtExpr() throws RecognitionException {
        EulangParser.codeStmtExpr_return retval = new EulangParser.codeStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl123 = null;

        EulangParser.assignStmt_return assignStmt124 = null;

        EulangParser.rhsExpr_return rhsExpr125 = null;

        EulangParser.blockStmt_return blockStmt126 = null;

        EulangParser.gotoStmt_return gotoStmt127 = null;

        EulangParser.controlStmt_return controlStmt128 = null;


        RewriteRuleSubtreeStream stream_assignStmt=new RewriteRuleSubtreeStream(adaptor,"rule assignStmt");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_blockStmt=new RewriteRuleSubtreeStream(adaptor,"rule blockStmt");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_controlStmt=new RewriteRuleSubtreeStream(adaptor,"rule controlStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:14: ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt )
            int alt42=6;
            alt42 = dfa42.predict(input);
            switch (alt42) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:16: varDecl
                    {
                    pushFollow(FOLLOW_varDecl_in_codeStmtExpr1707);
                    varDecl123=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl123.getTree());


                    // AST REWRITE
                    // elements: varDecl
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 199:27: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:9: assignStmt
                    {
                    pushFollow(FOLLOW_assignStmt_in_codeStmtExpr1724);
                    assignStmt124=assignStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignStmt.add(assignStmt124.getTree());


                    // AST REWRITE
                    // elements: assignStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 200:23: -> assignStmt
                    {
                        adaptor.addChild(root_0, stream_assignStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:202:9: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_codeStmtExpr1748);
                    rhsExpr125=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr125.getTree());


                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 202:23: -> ^( STMTEXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:202:27: ^( STMTEXPR rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTEXPR, "STMTEXPR"), root_1);

                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:203:9: blockStmt
                    {
                    pushFollow(FOLLOW_blockStmt_in_codeStmtExpr1773);
                    blockStmt126=blockStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_blockStmt.add(blockStmt126.getTree());


                    // AST REWRITE
                    // elements: blockStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 203:27: -> blockStmt
                    {
                        adaptor.addChild(root_0, stream_blockStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:9: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_codeStmtExpr1802);
                    gotoStmt127=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt127.getTree());


                    // AST REWRITE
                    // elements: gotoStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 205:23: -> gotoStmt
                    {
                        adaptor.addChild(root_0, stream_gotoStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:9: controlStmt
                    {
                    pushFollow(FOLLOW_controlStmt_in_codeStmtExpr1835);
                    controlStmt128=controlStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_controlStmt.add(controlStmt128.getTree());


                    // AST REWRITE
                    // elements: controlStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 208:26: -> controlStmt
                    {
                        adaptor.addChild(root_0, stream_controlStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "codeStmtExpr"

    public static class varDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "varDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID129=null;
        Token COLON_EQUALS130=null;
        Token COLON_EQUALS133=null;
        Token ID135=null;
        Token COLON136=null;
        Token EQUALS138=null;
        Token COLON141=null;
        Token EQUALS143=null;
        EulangParser.assignExpr_return assignExpr131 = null;

        EulangParser.idTuple_return idTuple132 = null;

        EulangParser.assignExpr_return assignExpr134 = null;

        EulangParser.type_return type137 = null;

        EulangParser.assignExpr_return assignExpr139 = null;

        EulangParser.idTuple_return idTuple140 = null;

        EulangParser.type_return type142 = null;

        EulangParser.assignExpr_return assignExpr144 = null;


        CommonTree ID129_tree=null;
        CommonTree COLON_EQUALS130_tree=null;
        CommonTree COLON_EQUALS133_tree=null;
        CommonTree ID135_tree=null;
        CommonTree COLON136_tree=null;
        CommonTree EQUALS138_tree=null;
        CommonTree COLON141_tree=null;
        CommonTree EQUALS143_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:8: ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) )
            int alt45=4;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:10: ID COLON_EQUALS assignExpr
                    {
                    ID129=(Token)match(input,ID,FOLLOW_ID_in_varDecl1858); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID129);

                    COLON_EQUALS130=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1860); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS130);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1862);
                    assignExpr131=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr131.getTree());


                    // AST REWRITE
                    // elements: assignExpr, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 211:45: -> ^( ALLOC ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:48: ^( ALLOC ID TYPE assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:7: idTuple COLON_EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_varDecl1890);
                    idTuple132=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple132.getTree());
                    COLON_EQUALS133=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1892); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS133);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1894);
                    assignExpr134=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr134.getTree());


                    // AST REWRITE
                    // elements: idTuple, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 212:47: -> ^( ALLOC idTuple TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:50: ^( ALLOC idTuple TYPE assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:213:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID135=(Token)match(input,ID,FOLLOW_ID_in_varDecl1922); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID135);

                    COLON136=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1924); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON136);

                    pushFollow(FOLLOW_type_in_varDecl1926);
                    type137=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type137.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:213:21: ( EQUALS assignExpr )?
                    int alt43=2;
                    int LA43_0 = input.LA(1);

                    if ( (LA43_0==EQUALS) ) {
                        alt43=1;
                    }
                    switch (alt43) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:213:22: EQUALS assignExpr
                            {
                            EQUALS138=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1929); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS138);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1931);
                            assignExpr139=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr139.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: assignExpr, type, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 213:43: -> ^( ALLOC ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:213:46: ^( ALLOC ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:213:62: ( assignExpr )*
                        while ( stream_assignExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        }
                        stream_assignExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:7: idTuple COLON type ( EQUALS assignExpr )?
                    {
                    pushFollow(FOLLOW_idTuple_in_varDecl1955);
                    idTuple140=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple140.getTree());
                    COLON141=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1957); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON141);

                    pushFollow(FOLLOW_type_in_varDecl1959);
                    type142=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type142.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:26: ( EQUALS assignExpr )?
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==EQUALS) ) {
                        alt44=1;
                    }
                    switch (alt44) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:27: EQUALS assignExpr
                            {
                            EQUALS143=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1962); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS143);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1964);
                            assignExpr144=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr144.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: idTuple, type, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 214:48: -> ^( ALLOC idTuple type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:51: ^( ALLOC idTuple type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:72: ( assignExpr )*
                        while ( stream_assignExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        }
                        stream_assignExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "varDecl"

    public static class assignStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:1: assignStmt : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) );
    public final EulangParser.assignStmt_return assignStmt() throws RecognitionException {
        EulangParser.assignStmt_return retval = new EulangParser.assignStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS146=null;
        Token EQUALS149=null;
        EulangParser.idOrScopeRef_return idOrScopeRef145 = null;

        EulangParser.assignExpr_return assignExpr147 = null;

        EulangParser.idTuple_return idTuple148 = null;

        EulangParser.assignExpr_return assignExpr150 = null;


        CommonTree EQUALS146_tree=null;
        CommonTree EQUALS149_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==ID||LA46_0==COLON||LA46_0==COLONS) ) {
                alt46=1;
            }
            else if ( (LA46_0==LPAREN) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignStmt2002);
                    idOrScopeRef145=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef145.getTree());
                    EQUALS146=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt2004); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS146);

                    pushFollow(FOLLOW_assignExpr_in_assignStmt2006);
                    assignExpr147=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr147.getTree());


                    // AST REWRITE
                    // elements: assignExpr, idOrScopeRef
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 220:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:55: ^( ASSIGN idOrScopeRef assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:7: idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignStmt2031);
                    idTuple148=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple148.getTree());
                    EQUALS149=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt2033); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS149);

                    pushFollow(FOLLOW_assignExpr_in_assignStmt2035);
                    assignExpr150=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr150.getTree());


                    // AST REWRITE
                    // elements: assignExpr, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 221:47: -> ^( ASSIGN idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:50: ^( ASSIGN idTuple assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignStmt"

    public static class assignExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS152=null;
        Token EQUALS155=null;
        EulangParser.idOrScopeRef_return idOrScopeRef151 = null;

        EulangParser.assignExpr_return assignExpr153 = null;

        EulangParser.idTuple_return idTuple154 = null;

        EulangParser.assignExpr_return assignExpr156 = null;

        EulangParser.rhsExpr_return rhsExpr157 = null;


        CommonTree EQUALS152_tree=null;
        CommonTree EQUALS155_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr )
            int alt47=3;
            alt47 = dfa47.predict(input);
            switch (alt47) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignExpr2078);
                    idOrScopeRef151=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef151.getTree());
                    EQUALS152=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr2080); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS152);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr2082);
                    assignExpr153=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr153.getTree());


                    // AST REWRITE
                    // elements: assignExpr, idOrScopeRef
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 224:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:55: ^( ASSIGN idOrScopeRef assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:7: idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignExpr2107);
                    idTuple154=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple154.getTree());
                    EQUALS155=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr2109); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS155);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr2111);
                    assignExpr156=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr156.getTree());


                    // AST REWRITE
                    // elements: assignExpr, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 225:47: -> ^( ASSIGN idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:50: ^( ASSIGN idTuple assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:226:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr2143);
                    rhsExpr157=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr157.getTree());


                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 226:43: -> rhsExpr
                    {
                        adaptor.addChild(root_0, stream_rhsExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "assignExpr"

    public static class controlStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "controlStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:1: controlStmt : ( doWhile | whileDo | repeat | forIter );
    public final EulangParser.controlStmt_return controlStmt() throws RecognitionException {
        EulangParser.controlStmt_return retval = new EulangParser.controlStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.doWhile_return doWhile158 = null;

        EulangParser.whileDo_return whileDo159 = null;

        EulangParser.repeat_return repeat160 = null;

        EulangParser.forIter_return forIter161 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:13: ( doWhile | whileDo | repeat | forIter )
            int alt48=4;
            switch ( input.LA(1) ) {
            case DO:
                {
                alt48=1;
                }
                break;
            case WHILE:
                {
                alt48=2;
                }
                break;
            case REPEAT:
                {
                alt48=3;
                }
                break;
            case FOR:
                {
                alt48=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }

            switch (alt48) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:15: doWhile
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_doWhile_in_controlStmt2189);
                    doWhile158=doWhile();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, doWhile158.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:25: whileDo
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_whileDo_in_controlStmt2193);
                    whileDo159=whileDo();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, whileDo159.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:35: repeat
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_repeat_in_controlStmt2197);
                    repeat160=repeat();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, repeat160.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:44: forIter
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_forIter_in_controlStmt2201);
                    forIter161=forIter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, forIter161.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "controlStmt"

    public static class doWhile_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "doWhile"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:232:1: doWhile : DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) ;
    public final EulangParser.doWhile_return doWhile() throws RecognitionException {
        EulangParser.doWhile_return retval = new EulangParser.doWhile_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DO162=null;
        Token WHILE164=null;
        EulangParser.codeStmtExpr_return codeStmtExpr163 = null;

        EulangParser.rhsExpr_return rhsExpr165 = null;


        CommonTree DO162_tree=null;
        CommonTree WHILE164_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:232:9: ( DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:232:11: DO codeStmtExpr WHILE rhsExpr
            {
            DO162=(Token)match(input,DO,FOLLOW_DO_in_doWhile2210); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO162);

            pushFollow(FOLLOW_codeStmtExpr_in_doWhile2212);
            codeStmtExpr163=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr163.getTree());
            WHILE164=(Token)match(input,WHILE,FOLLOW_WHILE_in_doWhile2214); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE164);

            pushFollow(FOLLOW_rhsExpr_in_doWhile2216);
            rhsExpr165=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr165.getTree());


            // AST REWRITE
            // elements: rhsExpr, codeStmtExpr, DO
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 232:43: -> ^( DO codeStmtExpr rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:232:46: ^( DO codeStmtExpr rhsExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_DO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_codeStmtExpr.nextTree());
                adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "doWhile"

    public static class whileDo_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "whileDo"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:1: whileDo : WHILE rhsExpr DO codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) ;
    public final EulangParser.whileDo_return whileDo() throws RecognitionException {
        EulangParser.whileDo_return retval = new EulangParser.whileDo_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHILE166=null;
        Token DO168=null;
        EulangParser.rhsExpr_return rhsExpr167 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr169 = null;


        CommonTree WHILE166_tree=null;
        CommonTree DO168_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:9: ( WHILE rhsExpr DO codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:11: WHILE rhsExpr DO codeStmtExpr
            {
            WHILE166=(Token)match(input,WHILE,FOLLOW_WHILE_in_whileDo2239); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE166);

            pushFollow(FOLLOW_rhsExpr_in_whileDo2241);
            rhsExpr167=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr167.getTree());
            DO168=(Token)match(input,DO,FOLLOW_DO_in_whileDo2243); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO168);

            pushFollow(FOLLOW_codeStmtExpr_in_whileDo2245);
            codeStmtExpr169=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr169.getTree());


            // AST REWRITE
            // elements: rhsExpr, WHILE, codeStmtExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 235:43: -> ^( WHILE rhsExpr codeStmtExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:46: ^( WHILE rhsExpr codeStmtExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_WHILE.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeStmtExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "whileDo"

    public static class repeat_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "repeat"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:1: repeat : REPEAT rhsExpr DO codeOrValueExpr -> ^( REPEAT rhsExpr codeOrValueExpr ) ;
    public final EulangParser.repeat_return repeat() throws RecognitionException {
        EulangParser.repeat_return retval = new EulangParser.repeat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token REPEAT170=null;
        Token DO172=null;
        EulangParser.rhsExpr_return rhsExpr171 = null;

        EulangParser.codeOrValueExpr_return codeOrValueExpr173 = null;


        CommonTree REPEAT170_tree=null;
        CommonTree DO172_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_REPEAT=new RewriteRuleTokenStream(adaptor,"token REPEAT");
        RewriteRuleSubtreeStream stream_codeOrValueExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeOrValueExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:8: ( REPEAT rhsExpr DO codeOrValueExpr -> ^( REPEAT rhsExpr codeOrValueExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:10: REPEAT rhsExpr DO codeOrValueExpr
            {
            REPEAT170=(Token)match(input,REPEAT,FOLLOW_REPEAT_in_repeat2270); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_REPEAT.add(REPEAT170);

            pushFollow(FOLLOW_rhsExpr_in_repeat2272);
            rhsExpr171=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr171.getTree());
            DO172=(Token)match(input,DO,FOLLOW_DO_in_repeat2274); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO172);

            pushFollow(FOLLOW_codeOrValueExpr_in_repeat2276);
            codeOrValueExpr173=codeOrValueExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeOrValueExpr.add(codeOrValueExpr173.getTree());


            // AST REWRITE
            // elements: codeOrValueExpr, rhsExpr, REPEAT
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 238:52: -> ^( REPEAT rhsExpr codeOrValueExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:55: ^( REPEAT rhsExpr codeOrValueExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_REPEAT.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeOrValueExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "repeat"

    public static class forIter_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forIter"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:1: forIter : FOR forIds ( atId )? IN rhsExpr DO codeOrValueExpr -> ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr ) ;
    public final EulangParser.forIter_return forIter() throws RecognitionException {
        EulangParser.forIter_return retval = new EulangParser.forIter_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR174=null;
        Token IN177=null;
        Token DO179=null;
        EulangParser.forIds_return forIds175 = null;

        EulangParser.atId_return atId176 = null;

        EulangParser.rhsExpr_return rhsExpr178 = null;

        EulangParser.codeOrValueExpr_return codeOrValueExpr180 = null;


        CommonTree FOR174_tree=null;
        CommonTree IN177_tree=null;
        CommonTree DO179_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_codeOrValueExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeOrValueExpr");
        RewriteRuleSubtreeStream stream_atId=new RewriteRuleSubtreeStream(adaptor,"rule atId");
        RewriteRuleSubtreeStream stream_forIds=new RewriteRuleSubtreeStream(adaptor,"rule forIds");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:9: ( FOR forIds ( atId )? IN rhsExpr DO codeOrValueExpr -> ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:11: FOR forIds ( atId )? IN rhsExpr DO codeOrValueExpr
            {
            FOR174=(Token)match(input,FOR,FOLLOW_FOR_in_forIter2306); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR174);

            pushFollow(FOLLOW_forIds_in_forIter2308);
            forIds175=forIds();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_forIds.add(forIds175.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:22: ( atId )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==AT) ) {
                alt49=1;
            }
            switch (alt49) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:22: atId
                    {
                    pushFollow(FOLLOW_atId_in_forIter2310);
                    atId176=atId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atId.add(atId176.getTree());

                    }
                    break;

            }

            IN177=(Token)match(input,IN,FOLLOW_IN_in_forIter2313); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN177);

            pushFollow(FOLLOW_rhsExpr_in_forIter2315);
            rhsExpr178=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr178.getTree());
            DO179=(Token)match(input,DO,FOLLOW_DO_in_forIter2317); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO179);

            pushFollow(FOLLOW_codeOrValueExpr_in_forIter2319);
            codeOrValueExpr180=codeOrValueExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeOrValueExpr.add(codeOrValueExpr180.getTree());


            // AST REWRITE
            // elements: FOR, atId, codeOrValueExpr, rhsExpr, forIds
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 241:64: -> ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:67: ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                adaptor.addChild(root_1, stream_forIds.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:80: ( atId )?
                if ( stream_atId.hasNext() ) {
                    adaptor.addChild(root_1, stream_atId.nextTree());

                }
                stream_atId.reset();
                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeOrValueExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "forIter"

    public static class forIds_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forIds"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:244:1: forIds : ID ( AND ID )* -> ( ID )+ ;
    public final EulangParser.forIds_return forIds() throws RecognitionException {
        EulangParser.forIds_return retval = new EulangParser.forIds_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID181=null;
        Token AND182=null;
        Token ID183=null;

        CommonTree ID181_tree=null;
        CommonTree AND182_tree=null;
        CommonTree ID183_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:244:8: ( ID ( AND ID )* -> ( ID )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:244:10: ID ( AND ID )*
            {
            ID181=(Token)match(input,ID,FOLLOW_ID_in_forIds2352); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID181);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:244:13: ( AND ID )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==AND) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:244:14: AND ID
            	    {
            	    AND182=(Token)match(input,AND,FOLLOW_AND_in_forIds2355); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND182);

            	    ID183=(Token)match(input,ID,FOLLOW_ID_in_forIds2357); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID183);


            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);



            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 244:23: -> ( ID )+
            {
                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    adaptor.addChild(root_0, stream_ID.nextNode());

                }
                stream_ID.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "forIds"

    public static class atId_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atId"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:246:1: atId : AT ID -> ^( AT ID ) ;
    public final EulangParser.atId_return atId() throws RecognitionException {
        EulangParser.atId_return retval = new EulangParser.atId_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT184=null;
        Token ID185=null;

        CommonTree AT184_tree=null;
        CommonTree ID185_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:246:6: ( AT ID -> ^( AT ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:246:8: AT ID
            {
            AT184=(Token)match(input,AT,FOLLOW_AT_in_atId2373); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT184);

            ID185=(Token)match(input,ID,FOLLOW_ID_in_atId2375); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID185);



            // AST REWRITE
            // elements: AT, ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 246:17: -> ^( AT ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:246:20: ^( AT ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_AT.nextNode(), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "atId"

    public static class codeOrValueExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codeOrValueExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:249:1: codeOrValueExpr : ( codeStmt | breakStmt );
    public final EulangParser.codeOrValueExpr_return codeOrValueExpr() throws RecognitionException {
        EulangParser.codeOrValueExpr_return retval = new EulangParser.codeOrValueExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.codeStmt_return codeStmt186 = null;

        EulangParser.breakStmt_return breakStmt187 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:249:17: ( codeStmt | breakStmt )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( ((LA51_0>=CODE && LA51_0<=MACRO)||LA51_0==GOTO||LA51_0==ID||LA51_0==COLON||LA51_0==LBRACE||LA51_0==FOR||LA51_0==LPAREN||LA51_0==NIL||(LA51_0>=DO && LA51_0<=REPEAT)||(LA51_0>=ATSIGN && LA51_0<=IF)||LA51_0==NOT||(LA51_0>=MINUS && LA51_0<=STAR)||(LA51_0>=TILDE && LA51_0<=STRING_LITERAL)||LA51_0==COLONS) ) {
                alt51=1;
            }
            else if ( (LA51_0==BREAK) ) {
                alt51=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }
            switch (alt51) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:249:19: codeStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_codeStmt_in_codeOrValueExpr2400);
                    codeStmt186=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, codeStmt186.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:249:30: breakStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_breakStmt_in_codeOrValueExpr2404);
                    breakStmt187=breakStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, breakStmt187.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "codeOrValueExpr"

    public static class breakStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "breakStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:1: breakStmt : BREAK valueCond -> ^( BREAK valueCond ) ;
    public final EulangParser.breakStmt_return breakStmt() throws RecognitionException {
        EulangParser.breakStmt_return retval = new EulangParser.breakStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BREAK188=null;
        EulangParser.valueCond_return valueCond189 = null;


        CommonTree BREAK188_tree=null;
        RewriteRuleTokenStream stream_BREAK=new RewriteRuleTokenStream(adaptor,"token BREAK");
        RewriteRuleSubtreeStream stream_valueCond=new RewriteRuleSubtreeStream(adaptor,"rule valueCond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:11: ( BREAK valueCond -> ^( BREAK valueCond ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:13: BREAK valueCond
            {
            BREAK188=(Token)match(input,BREAK,FOLLOW_BREAK_in_breakStmt2413); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BREAK.add(BREAK188);

            pushFollow(FOLLOW_valueCond_in_breakStmt2415);
            valueCond189=valueCond();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_valueCond.add(valueCond189.getTree());


            // AST REWRITE
            // elements: valueCond, BREAK
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 251:29: -> ^( BREAK valueCond )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:33: ^( BREAK valueCond )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_BREAK.nextNode(), root_1);

                adaptor.addChild(root_1, stream_valueCond.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "breakStmt"

    public static class valueCond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "valueCond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:1: valueCond : ( rhsExpr WHEN rhsExpr ELSE rhsExpr -> ^( WHEN ( rhsExpr )+ ) | UNTIL rhsExpr THEN rhsExpr ELSE rhsExpr -> ^( UNTIL ( rhsExpr )+ ) | WITH rhsExpr -> rhsExpr );
    public final EulangParser.valueCond_return valueCond() throws RecognitionException {
        EulangParser.valueCond_return retval = new EulangParser.valueCond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHEN191=null;
        Token ELSE193=null;
        Token UNTIL195=null;
        Token THEN197=null;
        Token ELSE199=null;
        Token WITH201=null;
        EulangParser.rhsExpr_return rhsExpr190 = null;

        EulangParser.rhsExpr_return rhsExpr192 = null;

        EulangParser.rhsExpr_return rhsExpr194 = null;

        EulangParser.rhsExpr_return rhsExpr196 = null;

        EulangParser.rhsExpr_return rhsExpr198 = null;

        EulangParser.rhsExpr_return rhsExpr200 = null;

        EulangParser.rhsExpr_return rhsExpr202 = null;


        CommonTree WHEN191_tree=null;
        CommonTree ELSE193_tree=null;
        CommonTree UNTIL195_tree=null;
        CommonTree THEN197_tree=null;
        CommonTree ELSE199_tree=null;
        CommonTree WITH201_tree=null;
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleTokenStream stream_UNTIL=new RewriteRuleTokenStream(adaptor,"token UNTIL");
        RewriteRuleTokenStream stream_WHEN=new RewriteRuleTokenStream(adaptor,"token WHEN");
        RewriteRuleTokenStream stream_WITH=new RewriteRuleTokenStream(adaptor,"token WITH");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:11: ( rhsExpr WHEN rhsExpr ELSE rhsExpr -> ^( WHEN ( rhsExpr )+ ) | UNTIL rhsExpr THEN rhsExpr ELSE rhsExpr -> ^( UNTIL ( rhsExpr )+ ) | WITH rhsExpr -> rhsExpr )
            int alt52=3;
            switch ( input.LA(1) ) {
            case CODE:
            case MACRO:
            case ID:
            case COLON:
            case LPAREN:
            case NIL:
            case IF:
            case NOT:
            case MINUS:
            case STAR:
            case TILDE:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case COLONS:
                {
                alt52=1;
                }
                break;
            case UNTIL:
                {
                alt52=2;
                }
                break;
            case WITH:
                {
                alt52=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;
            }

            switch (alt52) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:13: rhsExpr WHEN rhsExpr ELSE rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_valueCond2436);
                    rhsExpr190=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr190.getTree());
                    WHEN191=(Token)match(input,WHEN,FOLLOW_WHEN_in_valueCond2438); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_WHEN.add(WHEN191);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2440);
                    rhsExpr192=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr192.getTree());
                    ELSE193=(Token)match(input,ELSE,FOLLOW_ELSE_in_valueCond2442); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE193);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2444);
                    rhsExpr194=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr194.getTree());


                    // AST REWRITE
                    // elements: rhsExpr, WHEN
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 254:48: -> ^( WHEN ( rhsExpr )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:51: ^( WHEN ( rhsExpr )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_WHEN.nextNode(), root_1);

                        if ( !(stream_rhsExpr.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_rhsExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        }
                        stream_rhsExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:5: UNTIL rhsExpr THEN rhsExpr ELSE rhsExpr
                    {
                    UNTIL195=(Token)match(input,UNTIL,FOLLOW_UNTIL_in_valueCond2461); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_UNTIL.add(UNTIL195);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2463);
                    rhsExpr196=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr196.getTree());
                    THEN197=(Token)match(input,THEN,FOLLOW_THEN_in_valueCond2465); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN197);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2467);
                    rhsExpr198=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr198.getTree());
                    ELSE199=(Token)match(input,ELSE,FOLLOW_ELSE_in_valueCond2469); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE199);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2471);
                    rhsExpr200=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr200.getTree());


                    // AST REWRITE
                    // elements: rhsExpr, UNTIL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 255:48: -> ^( UNTIL ( rhsExpr )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:51: ^( UNTIL ( rhsExpr )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_UNTIL.nextNode(), root_1);

                        if ( !(stream_rhsExpr.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_rhsExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        }
                        stream_rhsExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:5: WITH rhsExpr
                    {
                    WITH201=(Token)match(input,WITH,FOLLOW_WITH_in_valueCond2489); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_WITH.add(WITH201);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2491);
                    rhsExpr202=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr202.getTree());


                    // AST REWRITE
                    // elements: rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 256:48: -> rhsExpr
                    {
                        adaptor.addChild(root_0, stream_rhsExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "valueCond"

    public static class labelStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "labelStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:1: labelStmt : ATSIGN ID COLON -> ^( LABEL ID ) ;
    public final EulangParser.labelStmt_return labelStmt() throws RecognitionException {
        EulangParser.labelStmt_return retval = new EulangParser.labelStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATSIGN203=null;
        Token ID204=null;
        Token COLON205=null;

        CommonTree ATSIGN203_tree=null;
        CommonTree ID204_tree=null;
        CommonTree COLON205_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ATSIGN=new RewriteRuleTokenStream(adaptor,"token ATSIGN");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:10: ( ATSIGN ID COLON -> ^( LABEL ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:12: ATSIGN ID COLON
            {
            ATSIGN203=(Token)match(input,ATSIGN,FOLLOW_ATSIGN_in_labelStmt2542); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ATSIGN.add(ATSIGN203);

            ID204=(Token)match(input,ID,FOLLOW_ID_in_labelStmt2544); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID204);

            COLON205=(Token)match(input,COLON,FOLLOW_COLON_in_labelStmt2546); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON205);



            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 259:47: -> ^( LABEL ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:50: ^( LABEL ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LABEL, "LABEL"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "labelStmt"

    public static class gotoStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "gotoStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:1: gotoStmt : GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) ;
    public final EulangParser.gotoStmt_return gotoStmt() throws RecognitionException {
        EulangParser.gotoStmt_return retval = new EulangParser.gotoStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token GOTO206=null;
        Token IF208=null;
        EulangParser.idOrScopeRef_return idOrScopeRef207 = null;

        EulangParser.rhsExpr_return rhsExpr209 = null;


        CommonTree GOTO206_tree=null;
        CommonTree IF208_tree=null;
        RewriteRuleTokenStream stream_GOTO=new RewriteRuleTokenStream(adaptor,"token GOTO");
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:9: ( GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:11: GOTO idOrScopeRef ( IF rhsExpr )?
            {
            GOTO206=(Token)match(input,GOTO,FOLLOW_GOTO_in_gotoStmt2582); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GOTO.add(GOTO206);

            pushFollow(FOLLOW_idOrScopeRef_in_gotoStmt2584);
            idOrScopeRef207=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef207.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:29: ( IF rhsExpr )?
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0==IF) ) {
                alt53=1;
            }
            switch (alt53) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:30: IF rhsExpr
                    {
                    IF208=(Token)match(input,IF,FOLLOW_IF_in_gotoStmt2587); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF208);

                    pushFollow(FOLLOW_rhsExpr_in_gotoStmt2589);
                    rhsExpr209=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr209.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: GOTO, rhsExpr, idOrScopeRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 261:53: -> ^( GOTO idOrScopeRef ( rhsExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:56: ^( GOTO idOrScopeRef ( rhsExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_GOTO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:76: ( rhsExpr )?
                if ( stream_rhsExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                }
                stream_rhsExpr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "gotoStmt"

    public static class blockStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "blockStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:1: blockStmt : LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) ;
    public final EulangParser.blockStmt_return blockStmt() throws RecognitionException {
        EulangParser.blockStmt_return retval = new EulangParser.blockStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE210=null;
        Token RBRACE212=null;
        EulangParser.codestmtlist_return codestmtlist211 = null;


        CommonTree LBRACE210_tree=null;
        CommonTree RBRACE212_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:10: ( LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:12: LBRACE codestmtlist RBRACE
            {
            LBRACE210=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_blockStmt2626); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE210);

            pushFollow(FOLLOW_codestmtlist_in_blockStmt2628);
            codestmtlist211=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist211.getTree());
            RBRACE212=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_blockStmt2630); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE212);



            // AST REWRITE
            // elements: codestmtlist
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 266:43: -> ^( BLOCK codestmtlist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:46: ^( BLOCK codestmtlist )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BLOCK, "BLOCK"), root_1);

                adaptor.addChild(root_1, stream_codestmtlist.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "blockStmt"

    public static class tuple_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tuple"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:1: tuple : LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) ;
    public final EulangParser.tuple_return tuple() throws RecognitionException {
        EulangParser.tuple_return retval = new EulangParser.tuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN213=null;
        Token RPAREN215=null;
        EulangParser.tupleEntries_return tupleEntries214 = null;


        CommonTree LPAREN213_tree=null;
        CommonTree RPAREN215_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule tupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:7: ( LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:9: LPAREN tupleEntries RPAREN
            {
            LPAREN213=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_tuple2653); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN213);

            pushFollow(FOLLOW_tupleEntries_in_tuple2655);
            tupleEntries214=tupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleEntries.add(tupleEntries214.getTree());
            RPAREN215=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_tuple2657); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN215);



            // AST REWRITE
            // elements: tupleEntries
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 269:41: -> ^( TUPLE ( tupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:44: ^( TUPLE ( tupleEntries )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TUPLE, "TUPLE"), root_1);

                if ( !(stream_tupleEntries.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_tupleEntries.hasNext() ) {
                    adaptor.addChild(root_1, stream_tupleEntries.nextTree());

                }
                stream_tupleEntries.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tuple"

    public static class tupleEntries_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleEntries"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:1: tupleEntries : assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ ;
    public final EulangParser.tupleEntries_return tupleEntries() throws RecognitionException {
        EulangParser.tupleEntries_return retval = new EulangParser.tupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA217=null;
        EulangParser.assignExpr_return assignExpr216 = null;

        EulangParser.assignExpr_return assignExpr218 = null;


        CommonTree COMMA217_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:14: ( assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:16: assignExpr ( COMMA assignExpr )+
            {
            pushFollow(FOLLOW_assignExpr_in_tupleEntries2685);
            assignExpr216=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr216.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:27: ( COMMA assignExpr )+
            int cnt54=0;
            loop54:
            do {
                int alt54=2;
                int LA54_0 = input.LA(1);

                if ( (LA54_0==COMMA) ) {
                    alt54=1;
                }


                switch (alt54) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:28: COMMA assignExpr
            	    {
            	    COMMA217=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleEntries2688); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA217);

            	    pushFollow(FOLLOW_assignExpr_in_tupleEntries2690);
            	    assignExpr218=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr218.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt54 >= 1 ) break loop54;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(54, input);
                        throw eee;
                }
                cnt54++;
            } while (true);



            // AST REWRITE
            // elements: assignExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 272:48: -> ( assignExpr )+
            {
                if ( !(stream_assignExpr.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_assignExpr.hasNext() ) {
                    adaptor.addChild(root_0, stream_assignExpr.nextTree());

                }
                stream_assignExpr.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tupleEntries"

    public static class idTuple_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idTuple"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:1: idTuple : LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) ;
    public final EulangParser.idTuple_return idTuple() throws RecognitionException {
        EulangParser.idTuple_return retval = new EulangParser.idTuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN219=null;
        Token RPAREN221=null;
        EulangParser.idTupleEntries_return idTupleEntries220 = null;


        CommonTree LPAREN219_tree=null;
        CommonTree RPAREN221_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_idTupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule idTupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:9: ( LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:11: LPAREN idTupleEntries RPAREN
            {
            LPAREN219=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_idTuple2709); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN219);

            pushFollow(FOLLOW_idTupleEntries_in_idTuple2711);
            idTupleEntries220=idTupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTupleEntries.add(idTupleEntries220.getTree());
            RPAREN221=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_idTuple2713); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN221);



            // AST REWRITE
            // elements: idTupleEntries
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 275:45: -> ^( TUPLE ( idTupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:48: ^( TUPLE ( idTupleEntries )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TUPLE, "TUPLE"), root_1);

                if ( !(stream_idTupleEntries.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_idTupleEntries.hasNext() ) {
                    adaptor.addChild(root_1, stream_idTupleEntries.nextTree());

                }
                stream_idTupleEntries.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idTuple"

    public static class idTupleEntries_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idTupleEntries"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:278:1: idTupleEntries : idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ ;
    public final EulangParser.idTupleEntries_return idTupleEntries() throws RecognitionException {
        EulangParser.idTupleEntries_return retval = new EulangParser.idTupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA223=null;
        EulangParser.idOrScopeRef_return idOrScopeRef222 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef224 = null;


        CommonTree COMMA223_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:278:16: ( idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:278:18: idOrScopeRef ( COMMA idOrScopeRef )+
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries2741);
            idOrScopeRef222=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef222.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:278:31: ( COMMA idOrScopeRef )+
            int cnt55=0;
            loop55:
            do {
                int alt55=2;
                int LA55_0 = input.LA(1);

                if ( (LA55_0==COMMA) ) {
                    alt55=1;
                }


                switch (alt55) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:278:32: COMMA idOrScopeRef
            	    {
            	    COMMA223=(Token)match(input,COMMA,FOLLOW_COMMA_in_idTupleEntries2744); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA223);

            	    pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries2746);
            	    idOrScopeRef224=idOrScopeRef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef224.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt55 >= 1 ) break loop55;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(55, input);
                        throw eee;
                }
                cnt55++;
            } while (true);



            // AST REWRITE
            // elements: idOrScopeRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 278:54: -> ( idOrScopeRef )+
            {
                if ( !(stream_idOrScopeRef.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_idOrScopeRef.hasNext() ) {
                    adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

                }
                stream_idOrScopeRef.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idTupleEntries"

    public static class rhsExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rhsExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:1: rhsExpr : condStar -> condStar ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.condStar_return condStar225 = null;


        RewriteRuleSubtreeStream stream_condStar=new RewriteRuleSubtreeStream(adaptor,"rule condStar");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:9: ( condStar -> condStar )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:13: condStar
            {
            pushFollow(FOLLOW_condStar_in_rhsExpr2767);
            condStar225=condStar();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStar.add(condStar225.getTree());


            // AST REWRITE
            // elements: condStar
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 281:22: -> condStar
            {
                adaptor.addChild(root_0, stream_condStar.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rhsExpr"

    public static class funcCall_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "funcCall"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:1: funcCall : idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN227=null;
        Token RPAREN229=null;
        EulangParser.idOrScopeRef_return idOrScopeRef226 = null;

        EulangParser.arglist_return arglist228 = null;


        CommonTree LPAREN227_tree=null;
        CommonTree RPAREN229_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:10: ( idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:12: idOrScopeRef LPAREN arglist RPAREN
            {
            pushFollow(FOLLOW_idOrScopeRef_in_funcCall2788);
            idOrScopeRef226=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef226.getTree());
            LPAREN227=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall2790); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN227);

            pushFollow(FOLLOW_arglist_in_funcCall2792);
            arglist228=arglist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arglist.add(arglist228.getTree());
            RPAREN229=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall2794); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN229);



            // AST REWRITE
            // elements: idOrScopeRef, arglist
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 284:49: -> ^( CALL idOrScopeRef arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:56: ^( CALL idOrScopeRef arglist )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CALL, "CALL"), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                adaptor.addChild(root_1, stream_arglist.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "funcCall"

    public static class arglist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arglist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA231=null;
        Token COMMA233=null;
        EulangParser.arg_return arg230 = null;

        EulangParser.arg_return arg232 = null;


        CommonTree COMMA231_tree=null;
        CommonTree COMMA233_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( ((LA58_0>=CODE && LA58_0<=MACRO)||LA58_0==GOTO||LA58_0==ID||LA58_0==COLON||LA58_0==LBRACE||LA58_0==LPAREN||LA58_0==NIL||LA58_0==IF||LA58_0==NOT||(LA58_0>=MINUS && LA58_0<=STAR)||(LA58_0>=TILDE && LA58_0<=STRING_LITERAL)||LA58_0==COLONS) ) {
                alt58=1;
            }
            switch (alt58) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist2825);
                    arg230=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg230.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:15: ( COMMA arg )*
                    loop56:
                    do {
                        int alt56=2;
                        int LA56_0 = input.LA(1);

                        if ( (LA56_0==COMMA) ) {
                            int LA56_1 = input.LA(2);

                            if ( ((LA56_1>=CODE && LA56_1<=MACRO)||LA56_1==GOTO||LA56_1==ID||LA56_1==COLON||LA56_1==LBRACE||LA56_1==LPAREN||LA56_1==NIL||LA56_1==IF||LA56_1==NOT||(LA56_1>=MINUS && LA56_1<=STAR)||(LA56_1>=TILDE && LA56_1<=STRING_LITERAL)||LA56_1==COLONS) ) {
                                alt56=1;
                            }


                        }


                        switch (alt56) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:17: COMMA arg
                    	    {
                    	    COMMA231=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist2829); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA231);

                    	    pushFollow(FOLLOW_arg_in_arglist2831);
                    	    arg232=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg232.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop56;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:29: ( COMMA )?
                    int alt57=2;
                    int LA57_0 = input.LA(1);

                    if ( (LA57_0==COMMA) ) {
                        alt57=1;
                    }
                    switch (alt57) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:29: COMMA
                            {
                            COMMA233=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist2835); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA233);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: arg
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 288:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:74: ( arg )*
                while ( stream_arg.hasNext() ) {
                    adaptor.addChild(root_1, stream_arg.nextTree());

                }
                stream_arg.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arglist"

    public static class arg_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arg"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:1: arg : ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) );
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE235=null;
        Token RBRACE237=null;
        EulangParser.assignExpr_return assignExpr234 = null;

        EulangParser.codestmtlist_return codestmtlist236 = null;

        EulangParser.gotoStmt_return gotoStmt238 = null;


        CommonTree LBRACE235_tree=null;
        CommonTree RBRACE237_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:4: ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) )
            int alt59=3;
            switch ( input.LA(1) ) {
            case CODE:
            case MACRO:
            case ID:
            case COLON:
            case LPAREN:
            case NIL:
            case IF:
            case NOT:
            case MINUS:
            case STAR:
            case TILDE:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case COLONS:
                {
                alt59=1;
                }
                break;
            case LBRACE:
                {
                alt59=2;
                }
                break;
            case GOTO:
                {
                alt59=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;
            }

            switch (alt59) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:7: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_arg2884);
                    assignExpr234=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr234.getTree());


                    // AST REWRITE
                    // elements: assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 291:37: -> ^( EXPR assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:40: ^( EXPR assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:5: LBRACE codestmtlist RBRACE
                    {
                    LBRACE235=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_arg2917); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE235);

                    pushFollow(FOLLOW_codestmtlist_in_arg2919);
                    codestmtlist236=codestmtlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist236.getTree());
                    RBRACE237=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_arg2921); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE237);



                    // AST REWRITE
                    // elements: codestmtlist
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 292:37: -> ^( EXPR ^( CODE codestmtlist ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:40: ^( EXPR ^( CODE codestmtlist ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:47: ^( CODE codestmtlist )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CODE, "CODE"), root_2);

                        adaptor.addChild(root_2, stream_codestmtlist.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:5: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_arg2945);
                    gotoStmt238=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt238.getTree());


                    // AST REWRITE
                    // elements: gotoStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 293:37: -> ^( EXPR gotoStmt )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:40: ^( EXPR gotoStmt )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_gotoStmt.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "arg"

    public static class withStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "withStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:1: withStmt : WITH bindings ARROW b= rhsExpr ( ELSE e= codeStmtExpr )? -> ^( WITH bindings $b ( $e)? ) ;
    public final EulangParser.withStmt_return withStmt() throws RecognitionException {
        EulangParser.withStmt_return retval = new EulangParser.withStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WITH239=null;
        Token ARROW241=null;
        Token ELSE242=null;
        EulangParser.rhsExpr_return b = null;

        EulangParser.codeStmtExpr_return e = null;

        EulangParser.bindings_return bindings240 = null;


        CommonTree WITH239_tree=null;
        CommonTree ARROW241_tree=null;
        CommonTree ELSE242_tree=null;
        RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
        RewriteRuleTokenStream stream_WITH=new RewriteRuleTokenStream(adaptor,"token WITH");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_bindings=new RewriteRuleSubtreeStream(adaptor,"rule bindings");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:10: ( WITH bindings ARROW b= rhsExpr ( ELSE e= codeStmtExpr )? -> ^( WITH bindings $b ( $e)? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:12: WITH bindings ARROW b= rhsExpr ( ELSE e= codeStmtExpr )?
            {
            WITH239=(Token)match(input,WITH,FOLLOW_WITH_in_withStmt2998); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WITH.add(WITH239);

            pushFollow(FOLLOW_bindings_in_withStmt3000);
            bindings240=bindings();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bindings.add(bindings240.getTree());
            ARROW241=(Token)match(input,ARROW,FOLLOW_ARROW_in_withStmt3002); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ARROW.add(ARROW241);

            pushFollow(FOLLOW_rhsExpr_in_withStmt3006);
            b=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(b.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:42: ( ELSE e= codeStmtExpr )?
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==ELSE) ) {
                alt60=1;
            }
            switch (alt60) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:43: ELSE e= codeStmtExpr
                    {
                    ELSE242=(Token)match(input,ELSE,FOLLOW_ELSE_in_withStmt3009); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE242);

                    pushFollow(FOLLOW_codeStmtExpr_in_withStmt3013);
                    e=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(e.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: bindings, WITH, b, e
            // token labels: 
            // rule labels: retval, e, b
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_e=new RewriteRuleSubtreeStream(adaptor,"rule e",e!=null?e.tree:null);
            RewriteRuleSubtreeStream stream_b=new RewriteRuleSubtreeStream(adaptor,"rule b",b!=null?b.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 306:65: -> ^( WITH bindings $b ( $e)? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:68: ^( WITH bindings $b ( $e)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_WITH.nextNode(), root_1);

                adaptor.addChild(root_1, stream_bindings.nextTree());
                adaptor.addChild(root_1, stream_b.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:87: ( $e)?
                if ( stream_e.hasNext() ) {
                    adaptor.addChild(root_1, stream_e.nextTree());

                }
                stream_e.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "withStmt"

    public static class bindings_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bindings"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:309:1: bindings : binding ( AND binding )* -> ( binding )+ ;
    public final EulangParser.bindings_return bindings() throws RecognitionException {
        EulangParser.bindings_return retval = new EulangParser.bindings_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND244=null;
        EulangParser.binding_return binding243 = null;

        EulangParser.binding_return binding245 = null;


        CommonTree AND244_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_binding=new RewriteRuleSubtreeStream(adaptor,"rule binding");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:309:9: ( binding ( AND binding )* -> ( binding )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:309:11: binding ( AND binding )*
            {
            pushFollow(FOLLOW_binding_in_bindings3041);
            binding243=binding();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_binding.add(binding243.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:309:19: ( AND binding )*
            loop61:
            do {
                int alt61=2;
                int LA61_0 = input.LA(1);

                if ( (LA61_0==AND) ) {
                    alt61=1;
                }


                switch (alt61) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:309:20: AND binding
            	    {
            	    AND244=(Token)match(input,AND,FOLLOW_AND_in_bindings3044); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND244);

            	    pushFollow(FOLLOW_binding_in_bindings3046);
            	    binding245=binding();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_binding.add(binding245.getTree());

            	    }
            	    break;

            	default :
            	    break loop61;
                }
            } while (true);



            // AST REWRITE
            // elements: binding
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 309:34: -> ( binding )+
            {
                if ( !(stream_binding.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_binding.hasNext() ) {
                    adaptor.addChild(root_0, stream_binding.nextTree());

                }
                stream_binding.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bindings"

    public static class binding_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "binding"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:1: binding : rhsExpr AS type -> ^( BINDING type rhsExpr ) ;
    public final EulangParser.binding_return binding() throws RecognitionException {
        EulangParser.binding_return retval = new EulangParser.binding_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AS247=null;
        EulangParser.rhsExpr_return rhsExpr246 = null;

        EulangParser.type_return type248 = null;


        CommonTree AS247_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:8: ( rhsExpr AS type -> ^( BINDING type rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:10: rhsExpr AS type
            {
            pushFollow(FOLLOW_rhsExpr_in_binding3064);
            rhsExpr246=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr246.getTree());
            AS247=(Token)match(input,AS,FOLLOW_AS_in_binding3066); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AS.add(AS247);

            pushFollow(FOLLOW_type_in_binding3068);
            type248=type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_type.add(type248.getTree());


            // AST REWRITE
            // elements: type, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 311:28: -> ^( BINDING type rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:31: ^( BINDING type rhsExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BINDING, "BINDING"), root_1);

                adaptor.addChild(root_1, stream_type.nextTree());
                adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "binding"

    public static class condStar_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condStar"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:1: condStar : ( cond -> cond | IF ifExprs -> ifExprs );
    public final EulangParser.condStar_return condStar() throws RecognitionException {
        EulangParser.condStar_return retval = new EulangParser.condStar_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IF250=null;
        EulangParser.cond_return cond249 = null;

        EulangParser.ifExprs_return ifExprs251 = null;


        CommonTree IF250_tree=null;
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_ifExprs=new RewriteRuleSubtreeStream(adaptor,"rule ifExprs");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:9: ( cond -> cond | IF ifExprs -> ifExprs )
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( ((LA62_0>=CODE && LA62_0<=MACRO)||LA62_0==ID||LA62_0==COLON||LA62_0==LPAREN||LA62_0==NIL||LA62_0==NOT||(LA62_0>=MINUS && LA62_0<=STAR)||(LA62_0>=TILDE && LA62_0<=STRING_LITERAL)||LA62_0==COLONS) ) {
                alt62=1;
            }
            else if ( (LA62_0==IF) ) {
                alt62=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                throw nvae;
            }
            switch (alt62) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:11: cond
                    {
                    pushFollow(FOLLOW_cond_in_condStar3098);
                    cond249=cond();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cond.add(cond249.getTree());


                    // AST REWRITE
                    // elements: cond
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 319:16: -> cond
                    {
                        adaptor.addChild(root_0, stream_cond.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:321:6: IF ifExprs
                    {
                    IF250=(Token)match(input,IF,FOLLOW_IF_in_condStar3113); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF250);

                    pushFollow(FOLLOW_ifExprs_in_condStar3115);
                    ifExprs251=ifExprs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ifExprs.add(ifExprs251.getTree());


                    // AST REWRITE
                    // elements: ifExprs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 321:17: -> ifExprs
                    {
                        adaptor.addChild(root_0, stream_ifExprs.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "condStar"

    public static class ifExprs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ifExprs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:1: ifExprs : thenClause elses -> ^( CONDLIST thenClause elses ) ;
    public final EulangParser.ifExprs_return ifExprs() throws RecognitionException {
        EulangParser.ifExprs_return retval = new EulangParser.ifExprs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.thenClause_return thenClause252 = null;

        EulangParser.elses_return elses253 = null;


        RewriteRuleSubtreeStream stream_thenClause=new RewriteRuleSubtreeStream(adaptor,"rule thenClause");
        RewriteRuleSubtreeStream stream_elses=new RewriteRuleSubtreeStream(adaptor,"rule elses");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:9: ( thenClause elses -> ^( CONDLIST thenClause elses ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:11: thenClause elses
            {
            pushFollow(FOLLOW_thenClause_in_ifExprs3142);
            thenClause252=thenClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_thenClause.add(thenClause252.getTree());
            pushFollow(FOLLOW_elses_in_ifExprs3144);
            elses253=elses();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elses.add(elses253.getTree());


            // AST REWRITE
            // elements: thenClause, elses
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 342:28: -> ^( CONDLIST thenClause elses )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:31: ^( CONDLIST thenClause elses )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDLIST, "CONDLIST"), root_1);

                adaptor.addChild(root_1, stream_thenClause.nextTree());
                adaptor.addChild(root_1, stream_elses.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ifExprs"

    public static class thenClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "thenClause"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:344:1: thenClause : t= arg THEN v= arg -> ^( CONDTEST $t $v) ;
    public final EulangParser.thenClause_return thenClause() throws RecognitionException {
        EulangParser.thenClause_return retval = new EulangParser.thenClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token THEN254=null;
        EulangParser.arg_return t = null;

        EulangParser.arg_return v = null;


        CommonTree THEN254_tree=null;
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:344:12: (t= arg THEN v= arg -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:344:14: t= arg THEN v= arg
            {
            pushFollow(FOLLOW_arg_in_thenClause3166);
            t=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(t.getTree());
            THEN254=(Token)match(input,THEN,FOLLOW_THEN_in_thenClause3168); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_THEN.add(THEN254);

            pushFollow(FOLLOW_arg_in_thenClause3172);
            v=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(v.getTree());


            // AST REWRITE
            // elements: v, t
            // token labels: 
            // rule labels: v, retval, t
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_v=new RewriteRuleSubtreeStream(adaptor,"rule v",v!=null?v.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 344:33: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:344:36: ^( CONDTEST $t $v)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                adaptor.addChild(root_1, stream_t.nextTree());
                adaptor.addChild(root_1, stream_v.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "thenClause"

    public static class elses_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elses"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:1: elses : ( elif )* elseClause -> ( elif )* elseClause ;
    public final EulangParser.elses_return elses() throws RecognitionException {
        EulangParser.elses_return retval = new EulangParser.elses_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.elif_return elif255 = null;

        EulangParser.elseClause_return elseClause256 = null;


        RewriteRuleSubtreeStream stream_elseClause=new RewriteRuleSubtreeStream(adaptor,"rule elseClause");
        RewriteRuleSubtreeStream stream_elif=new RewriteRuleSubtreeStream(adaptor,"rule elif");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:7: ( ( elif )* elseClause -> ( elif )* elseClause )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:9: ( elif )* elseClause
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:9: ( elif )*
            loop63:
            do {
                int alt63=2;
                int LA63_0 = input.LA(1);

                if ( (LA63_0==ELIF) ) {
                    alt63=1;
                }


                switch (alt63) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:9: elif
            	    {
            	    pushFollow(FOLLOW_elif_in_elses3200);
            	    elif255=elif();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_elif.add(elif255.getTree());

            	    }
            	    break;

            	default :
            	    break loop63;
                }
            } while (true);

            pushFollow(FOLLOW_elseClause_in_elses3203);
            elseClause256=elseClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elseClause.add(elseClause256.getTree());


            // AST REWRITE
            // elements: elseClause, elif
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 346:29: -> ( elif )* elseClause
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:32: ( elif )*
                while ( stream_elif.hasNext() ) {
                    adaptor.addChild(root_0, stream_elif.nextTree());

                }
                stream_elif.reset();
                adaptor.addChild(root_0, stream_elseClause.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elses"

    public static class elif_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elif"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:1: elif : ELIF t= arg THEN v= arg -> ^( CONDTEST $t $v) ;
    public final EulangParser.elif_return elif() throws RecognitionException {
        EulangParser.elif_return retval = new EulangParser.elif_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELIF257=null;
        Token THEN258=null;
        EulangParser.arg_return t = null;

        EulangParser.arg_return v = null;


        CommonTree ELIF257_tree=null;
        CommonTree THEN258_tree=null;
        RewriteRuleTokenStream stream_ELIF=new RewriteRuleTokenStream(adaptor,"token ELIF");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:6: ( ELIF t= arg THEN v= arg -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:8: ELIF t= arg THEN v= arg
            {
            ELIF257=(Token)match(input,ELIF,FOLLOW_ELIF_in_elif3226); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ELIF.add(ELIF257);

            pushFollow(FOLLOW_arg_in_elif3230);
            t=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(t.getTree());
            THEN258=(Token)match(input,THEN,FOLLOW_THEN_in_elif3232); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_THEN.add(THEN258);

            pushFollow(FOLLOW_arg_in_elif3236);
            v=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(v.getTree());


            // AST REWRITE
            // elements: t, v
            // token labels: 
            // rule labels: v, retval, t
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_v=new RewriteRuleSubtreeStream(adaptor,"rule v",v!=null?v.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 348:31: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:34: ^( CONDTEST $t $v)
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                adaptor.addChild(root_1, stream_t.nextTree());
                adaptor.addChild(root_1, stream_v.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elif"

    public static class elseClause_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elseClause"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:350:1: elseClause : ( ELSE arg -> ^( CONDTEST ^( LIT TRUE ) arg ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) );
    public final EulangParser.elseClause_return elseClause() throws RecognitionException {
        EulangParser.elseClause_return retval = new EulangParser.elseClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELSE259=null;
        Token FI261=null;
        EulangParser.arg_return arg260 = null;


        CommonTree ELSE259_tree=null;
        CommonTree FI261_tree=null;
        RewriteRuleTokenStream stream_FI=new RewriteRuleTokenStream(adaptor,"token FI");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:350:12: ( ELSE arg -> ^( CONDTEST ^( LIT TRUE ) arg ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) )
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==ELSE) ) {
                alt64=1;
            }
            else if ( (LA64_0==FI) ) {
                alt64=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 64, 0, input);

                throw nvae;
            }
            switch (alt64) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:350:14: ELSE arg
                    {
                    ELSE259=(Token)match(input,ELSE,FOLLOW_ELSE_in_elseClause3262); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE259);

                    pushFollow(FOLLOW_arg_in_elseClause3264);
                    arg260=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg260.getTree());


                    // AST REWRITE
                    // elements: arg
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 350:29: -> ^( CONDTEST ^( LIT TRUE ) arg )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:350:32: ^( CONDTEST ^( LIT TRUE ) arg )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:350:43: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        adaptor.addChild(root_1, stream_arg.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:6: FI
                    {
                    FI261=(Token)match(input,FI,FOLLOW_FI_in_elseClause3291); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FI.add(FI261);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 351:9: -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:12: ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:23: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:35: ^( LIT NIL )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(NIL, "NIL"));

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elseClause"

    public static class cond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:1: cond : ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION263=null;
        Token COLON264=null;
        EulangParser.logor_return t = null;

        EulangParser.logor_return f = null;

        EulangParser.logor_return logor262 = null;


        CommonTree QUESTION263_tree=null;
        CommonTree COLON264_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logor=new RewriteRuleSubtreeStream(adaptor,"rule logor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:5: ( ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:10: ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:10: ( logor -> logor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:12: logor
            {
            pushFollow(FOLLOW_logor_in_cond3331);
            logor262=logor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logor.add(logor262.getTree());


            // AST REWRITE
            // elements: logor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 357:19: -> logor
            {
                adaptor.addChild(root_0, stream_logor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:7: ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            loop65:
            do {
                int alt65=2;
                int LA65_0 = input.LA(1);

                if ( (LA65_0==QUESTION) ) {
                    alt65=1;
                }


                switch (alt65) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:9: QUESTION t= logor COLON f= logor
            	    {
            	    QUESTION263=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond3348); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION263);

            	    pushFollow(FOLLOW_logor_in_cond3352);
            	    t=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(t.getTree());
            	    COLON264=(Token)match(input,COLON,FOLLOW_COLON_in_cond3354); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON264);

            	    pushFollow(FOLLOW_logor_in_cond3358);
            	    f=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(f.getTree());


            	    // AST REWRITE
            	    // elements: t, cond, f
            	    // token labels: 
            	    // rule labels: f, retval, t
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_f=new RewriteRuleSubtreeStream(adaptor,"rule f",f!=null?f.tree:null);
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 358:40: -> ^( COND $cond $t $f)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:43: ^( COND $cond $t $f)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(COND, "COND"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_t.nextTree());
            	        adaptor.addChild(root_1, stream_f.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "cond"

    public static class logor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:1: logor : ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* ;
    public final EulangParser.logor_return logor() throws RecognitionException {
        EulangParser.logor_return retval = new EulangParser.logor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token OR266=null;
        EulangParser.logand_return r = null;

        EulangParser.logand_return logand265 = null;


        CommonTree OR266_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_logand=new RewriteRuleSubtreeStream(adaptor,"rule logand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:7: ( ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:9: ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:9: ( logand -> logand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:11: logand
            {
            pushFollow(FOLLOW_logand_in_logor3388);
            logand265=logand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logand.add(logand265.getTree());


            // AST REWRITE
            // elements: logand
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 361:19: -> logand
            {
                adaptor.addChild(root_0, stream_logand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:7: ( OR r= logand -> ^( OR $logor $r) )*
            loop66:
            do {
                int alt66=2;
                int LA66_0 = input.LA(1);

                if ( (LA66_0==OR) ) {
                    alt66=1;
                }


                switch (alt66) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:9: OR r= logand
            	    {
            	    OR266=(Token)match(input,OR,FOLLOW_OR_in_logor3405); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR266);

            	    pushFollow(FOLLOW_logand_in_logor3409);
            	    r=logand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: logor, OR, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 362:21: -> ^( OR $logor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:24: ^( OR $logor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_OR.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logor"

    public static class logand_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logand"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:1: logand : ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* ;
    public final EulangParser.logand_return logand() throws RecognitionException {
        EulangParser.logand_return retval = new EulangParser.logand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND268=null;
        EulangParser.not_return r = null;

        EulangParser.not_return not267 = null;


        CommonTree AND268_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:8: ( ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:10: ( not -> not ) ( AND r= not -> ^( AND $logand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:10: ( not -> not )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:12: not
            {
            pushFollow(FOLLOW_not_in_logand3440);
            not267=not();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_not.add(not267.getTree());


            // AST REWRITE
            // elements: not
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 364:16: -> not
            {
                adaptor.addChild(root_0, stream_not.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:7: ( AND r= not -> ^( AND $logand $r) )*
            loop67:
            do {
                int alt67=2;
                int LA67_0 = input.LA(1);

                if ( (LA67_0==AND) ) {
                    alt67=1;
                }


                switch (alt67) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:9: AND r= not
            	    {
            	    AND268=(Token)match(input,AND,FOLLOW_AND_in_logand3456); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND268);

            	    pushFollow(FOLLOW_not_in_logand3460);
            	    r=not();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_not.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, AND, logand
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 365:19: -> ^( AND $logand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:22: ^( AND $logand $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_AND.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logand"

    public static class not_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "not"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:1: not : ( comp -> comp | NOT u= comp -> ^( NOT $u) );
    public final EulangParser.not_return not() throws RecognitionException {
        EulangParser.not_return retval = new EulangParser.not_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NOT270=null;
        EulangParser.comp_return u = null;

        EulangParser.comp_return comp269 = null;


        CommonTree NOT270_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:5: ( comp -> comp | NOT u= comp -> ^( NOT $u) )
            int alt68=2;
            int LA68_0 = input.LA(1);

            if ( ((LA68_0>=CODE && LA68_0<=MACRO)||LA68_0==ID||LA68_0==COLON||LA68_0==LPAREN||LA68_0==NIL||(LA68_0>=MINUS && LA68_0<=STAR)||(LA68_0>=TILDE && LA68_0<=STRING_LITERAL)||LA68_0==COLONS) ) {
                alt68=1;
            }
            else if ( (LA68_0==NOT) ) {
                alt68=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 68, 0, input);

                throw nvae;
            }
            switch (alt68) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:8: comp
                    {
                    pushFollow(FOLLOW_comp_in_not3506);
                    comp269=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(comp269.getTree());


                    // AST REWRITE
                    // elements: comp
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 368:17: -> comp
                    {
                        adaptor.addChild(root_0, stream_comp.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:7: NOT u= comp
                    {
                    NOT270=(Token)match(input,NOT,FOLLOW_NOT_in_not3522); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT270);

                    pushFollow(FOLLOW_comp_in_not3526);
                    u=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(u.getTree());


                    // AST REWRITE
                    // elements: NOT, u
                    // token labels: 
                    // rule labels: retval, u
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_u=new RewriteRuleSubtreeStream(adaptor,"rule u",u!=null?u.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 369:22: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:25: ^( NOT $u)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_NOT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "not"

    public static class comp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:1: comp : ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ272=null;
        Token COMPNE273=null;
        Token COMPLE274=null;
        Token COMPGE275=null;
        Token LESS276=null;
        Token GREATER277=null;
        EulangParser.bitor_return r = null;

        EulangParser.bitor_return bitor271 = null;


        CommonTree COMPEQ272_tree=null;
        CommonTree COMPNE273_tree=null;
        CommonTree COMPLE274_tree=null;
        CommonTree COMPGE275_tree=null;
        CommonTree LESS276_tree=null;
        CommonTree GREATER277_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_bitor=new RewriteRuleSubtreeStream(adaptor,"rule bitor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:5: ( ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:9: ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:9: ( bitor -> bitor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:11: bitor
            {
            pushFollow(FOLLOW_bitor_in_comp3560);
            bitor271=bitor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitor.add(bitor271.getTree());


            // AST REWRITE
            // elements: bitor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 372:24: -> bitor
            {
                adaptor.addChild(root_0, stream_bitor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:7: ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            loop69:
            do {
                int alt69=7;
                switch ( input.LA(1) ) {
                case COMPEQ:
                    {
                    alt69=1;
                    }
                    break;
                case COMPNE:
                    {
                    alt69=2;
                    }
                    break;
                case COMPLE:
                    {
                    alt69=3;
                    }
                    break;
                case COMPGE:
                    {
                    alt69=4;
                    }
                    break;
                case LESS:
                    {
                    alt69=5;
                    }
                    break;
                case GREATER:
                    {
                    alt69=6;
                    }
                    break;

                }

                switch (alt69) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:9: COMPEQ r= bitor
            	    {
            	    COMPEQ272=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_comp3593); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ272);

            	    pushFollow(FOLLOW_bitor_in_comp3597);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, COMPEQ, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 373:24: -> ^( COMPEQ $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:27: ^( COMPEQ $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPEQ.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:374:9: COMPNE r= bitor
            	    {
            	    COMPNE273=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_comp3619); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE273);

            	    pushFollow(FOLLOW_bitor_in_comp3623);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPNE, r, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 374:24: -> ^( COMPNE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:374:27: ^( COMPNE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPNE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:9: COMPLE r= bitor
            	    {
            	    COMPLE274=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp3645); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE274);

            	    pushFollow(FOLLOW_bitor_in_comp3649);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPLE, r, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 375:27: -> ^( COMPLE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:30: ^( COMPLE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPLE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:9: COMPGE r= bitor
            	    {
            	    COMPGE275=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp3674); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE275);

            	    pushFollow(FOLLOW_bitor_in_comp3678);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPGE, comp, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 376:27: -> ^( COMPGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:30: ^( COMPGE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPGE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:9: LESS r= bitor
            	    {
            	    LESS276=(Token)match(input,LESS,FOLLOW_LESS_in_comp3703); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS276);

            	    pushFollow(FOLLOW_bitor_in_comp3707);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, LESS, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 377:26: -> ^( LESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:29: ^( LESS $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_LESS.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 6 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:378:9: GREATER r= bitor
            	    {
            	    GREATER277=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp3733); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER277);

            	    pushFollow(FOLLOW_bitor_in_comp3737);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, GREATER, comp
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 378:28: -> ^( GREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:378:31: ^( GREATER $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_GREATER.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop69;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comp"

    public static class bitor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:1: bitor : ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* ;
    public final EulangParser.bitor_return bitor() throws RecognitionException {
        EulangParser.bitor_return retval = new EulangParser.bitor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR279=null;
        EulangParser.bitxor_return r = null;

        EulangParser.bitxor_return bitxor278 = null;


        CommonTree BAR279_tree=null;
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_bitxor=new RewriteRuleSubtreeStream(adaptor,"rule bitxor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:6: ( ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:8: ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:8: ( bitxor -> bitxor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:10: bitxor
            {
            pushFollow(FOLLOW_bitxor_in_bitor3787);
            bitxor278=bitxor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitxor.add(bitxor278.getTree());


            // AST REWRITE
            // elements: bitxor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 383:22: -> bitxor
            {
                adaptor.addChild(root_0, stream_bitxor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:384:7: ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            loop70:
            do {
                int alt70=2;
                int LA70_0 = input.LA(1);

                if ( (LA70_0==BAR) ) {
                    alt70=1;
                }


                switch (alt70) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:384:9: BAR r= bitxor
            	    {
            	    BAR279=(Token)match(input,BAR,FOLLOW_BAR_in_bitor3815); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR279);

            	    pushFollow(FOLLOW_bitxor_in_bitor3819);
            	    r=bitxor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitxor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: bitor, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 384:23: -> ^( BITOR $bitor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:384:26: ^( BITOR $bitor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITOR, "BITOR"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop70;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitor"

    public static class bitxor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitxor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:386:1: bitxor : ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* ;
    public final EulangParser.bitxor_return bitxor() throws RecognitionException {
        EulangParser.bitxor_return retval = new EulangParser.bitxor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CARET281=null;
        EulangParser.bitand_return r = null;

        EulangParser.bitand_return bitand280 = null;


        CommonTree CARET281_tree=null;
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_bitand=new RewriteRuleSubtreeStream(adaptor,"rule bitand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:386:7: ( ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:386:9: ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:386:9: ( bitand -> bitand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:386:11: bitand
            {
            pushFollow(FOLLOW_bitand_in_bitxor3845);
            bitand280=bitand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitand.add(bitand280.getTree());


            // AST REWRITE
            // elements: bitand
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 386:23: -> bitand
            {
                adaptor.addChild(root_0, stream_bitand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:7: ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            loop71:
            do {
                int alt71=2;
                int LA71_0 = input.LA(1);

                if ( (LA71_0==CARET) ) {
                    alt71=1;
                }


                switch (alt71) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:9: CARET r= bitand
            	    {
            	    CARET281=(Token)match(input,CARET,FOLLOW_CARET_in_bitxor3873); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET281);

            	    pushFollow(FOLLOW_bitand_in_bitxor3877);
            	    r=bitand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: bitxor, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 387:25: -> ^( BITXOR $bitxor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:28: ^( BITXOR $bitxor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITXOR, "BITXOR"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop71;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitxor"

    public static class bitand_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitand"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:1: bitand : ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* ;
    public final EulangParser.bitand_return bitand() throws RecognitionException {
        EulangParser.bitand_return retval = new EulangParser.bitand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP283=null;
        EulangParser.shift_return r = null;

        EulangParser.shift_return shift282 = null;


        CommonTree AMP283_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:7: ( ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:9: ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:9: ( shift -> shift )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:11: shift
            {
            pushFollow(FOLLOW_shift_in_bitand3902);
            shift282=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(shift282.getTree());


            // AST REWRITE
            // elements: shift
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 389:22: -> shift
            {
                adaptor.addChild(root_0, stream_shift.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:390:7: ( AMP r= shift -> ^( BITAND $bitand $r) )*
            loop72:
            do {
                int alt72=2;
                int LA72_0 = input.LA(1);

                if ( (LA72_0==AMP) ) {
                    alt72=1;
                }


                switch (alt72) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:390:9: AMP r= shift
            	    {
            	    AMP283=(Token)match(input,AMP,FOLLOW_AMP_in_bitand3930); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP283);

            	    pushFollow(FOLLOW_shift_in_bitand3934);
            	    r=shift();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, bitand
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 390:22: -> ^( BITAND $bitand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:390:25: ^( BITAND $bitand $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITAND, "BITAND"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop72;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitand"

    public static class shift_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:1: shift : ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT285=null;
        Token RSHIFT286=null;
        Token URSHIFT287=null;
        EulangParser.factor_return r = null;

        EulangParser.factor_return factor284 = null;


        CommonTree LSHIFT285_tree=null;
        CommonTree RSHIFT286_tree=null;
        CommonTree URSHIFT287_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:6: ( ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:9: ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:9: ( factor -> factor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:11: factor
            {
            pushFollow(FOLLOW_factor_in_shift3961);
            factor284=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(factor284.getTree());


            // AST REWRITE
            // elements: factor
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 393:25: -> factor
            {
                adaptor.addChild(root_0, stream_factor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:7: ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            loop73:
            do {
                int alt73=4;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt73=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt73=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt73=3;
                    }
                    break;

                }

                switch (alt73) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:11: LSHIFT r= factor
            	    {
            	    LSHIFT285=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift3995); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT285);

            	    pushFollow(FOLLOW_factor_in_shift3999);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, shift, LSHIFT
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 394:29: -> ^( LSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:32: ^( LSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_LSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:11: RSHIFT r= factor
            	    {
            	    RSHIFT286=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift4028); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT286);

            	    pushFollow(FOLLOW_factor_in_shift4032);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: shift, RSHIFT, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 395:29: -> ^( RSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:32: ^( RSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_RSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:11: URSHIFT r= factor
            	    {
            	    URSHIFT287=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift4060); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT287);

            	    pushFollow(FOLLOW_factor_in_shift4064);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, URSHIFT, shift
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 396:30: -> ^( URSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:33: ^( URSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_URSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop73;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "shift"

    public static class factor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "factor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:399:1: factor : ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS289=null;
        Token MINUS290=null;
        EulangParser.term_return r = null;

        EulangParser.term_return term288 = null;


        CommonTree PLUS289_tree=null;
        CommonTree MINUS290_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:400:5: ( ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:400:7: ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:400:7: ( term -> term )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:400:9: term
            {
            pushFollow(FOLLOW_term_in_factor4106);
            term288=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(term288.getTree());


            // AST REWRITE
            // elements: term
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 400:27: -> term
            {
                adaptor.addChild(root_0, stream_term.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:401:9: ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            loop74:
            do {
                int alt74=3;
                int LA74_0 = input.LA(1);

                if ( (LA74_0==PLUS) ) {
                    alt74=1;
                }
                else if ( (LA74_0==MINUS) && (synpred5_Eulang())) {
                    alt74=2;
                }


                switch (alt74) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:401:13: PLUS r= term
            	    {
            	    PLUS289=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor4139); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS289);

            	    pushFollow(FOLLOW_term_in_factor4143);
            	    r=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_term.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, factor
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 401:33: -> ^( ADD $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:401:36: ^( ADD $factor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADD, "ADD"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:402:12: ( MINUS term )=> MINUS r= term
            	    {
            	    MINUS290=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor4185); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS290);

            	    pushFollow(FOLLOW_term_in_factor4189);
            	    r=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_term.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, factor
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 402:49: -> ^( SUB $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:402:52: ^( SUB $factor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SUB, "SUB"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "factor"

    public static class term_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:1: term : ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR292=null;
        Token SLASH293=null;
        Token BACKSLASH294=null;
        Token PERCENT295=null;
        Token UMOD296=null;
        EulangParser.unary_return r = null;

        EulangParser.unary_return unary291 = null;


        CommonTree STAR292_tree=null;
        CommonTree SLASH293_tree=null;
        CommonTree BACKSLASH294_tree=null;
        CommonTree PERCENT295_tree=null;
        CommonTree UMOD296_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:6: ( ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:8: ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:8: ( unary -> unary )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:10: unary
            {
            pushFollow(FOLLOW_unary_in_term4234);
            unary291=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(unary291.getTree());


            // AST REWRITE
            // elements: unary
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 406:33: -> unary
            {
                adaptor.addChild(root_0, stream_unary.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:407:9: ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            loop75:
            do {
                int alt75=6;
                int LA75_0 = input.LA(1);

                if ( (LA75_0==STAR) && (synpred6_Eulang())) {
                    alt75=1;
                }
                else if ( (LA75_0==SLASH) ) {
                    alt75=2;
                }
                else if ( (LA75_0==BACKSLASH) ) {
                    alt75=3;
                }
                else if ( (LA75_0==PERCENT) ) {
                    alt75=4;
                }
                else if ( (LA75_0==UMOD) ) {
                    alt75=5;
                }


                switch (alt75) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:407:11: ( STAR unary )=> STAR r= unary
            	    {
            	    STAR292=(Token)match(input,STAR,FOLLOW_STAR_in_term4278); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR292);

            	    pushFollow(FOLLOW_unary_in_term4282);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 407:52: -> ^( MUL $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:407:55: ^( MUL $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MUL, "MUL"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:11: SLASH r= unary
            	    {
            	    SLASH293=(Token)match(input,SLASH,FOLLOW_SLASH_in_term4318); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH293);

            	    pushFollow(FOLLOW_unary_in_term4322);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 408:36: -> ^( DIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:39: ^( DIV $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DIV, "DIV"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:409:11: BACKSLASH r= unary
            	    {
            	    BACKSLASH294=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_term4357); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BACKSLASH.add(BACKSLASH294);

            	    pushFollow(FOLLOW_unary_in_term4361);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 409:40: -> ^( UDIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:409:43: ^( UDIV $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UDIV, "UDIV"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:410:11: PERCENT r= unary
            	    {
            	    PERCENT295=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_term4396); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT295);

            	    pushFollow(FOLLOW_unary_in_term4400);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 410:38: -> ^( MOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:410:41: ^( MOD $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MOD, "MOD"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:11: UMOD r= unary
            	    {
            	    UMOD296=(Token)match(input,UMOD,FOLLOW_UMOD_in_term4435); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UMOD.add(UMOD296);

            	    pushFollow(FOLLOW_unary_in_term4439);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: UMOD, term, r
            	    // token labels: 
            	    // rule labels: retval, r
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 411:35: -> ^( UMOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:38: ^( UMOD $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UMOD.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "term"

    public static class unary_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unary"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:1: unary : ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS298=null;
        Token TILDE299=null;
        EulangParser.unary_return u = null;

        EulangParser.atom_return atom297 = null;


        CommonTree MINUS298_tree=null;
        CommonTree TILDE299_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:6: ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) )
            int alt76=3;
            switch ( input.LA(1) ) {
            case CODE:
            case MACRO:
            case ID:
            case COLON:
            case LPAREN:
            case NIL:
            case STAR:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case COLONS:
                {
                alt76=1;
                }
                break;
            case MINUS:
                {
                alt76=2;
                }
                break;
            case TILDE:
                {
                alt76=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 76, 0, input);

                throw nvae;
            }

            switch (alt76) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:11: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:11: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:13: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary4516);
                    atom297=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom297.getTree());


                    // AST REWRITE
                    // elements: atom
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 416:25: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:9: MINUS u= unary
                    {
                    MINUS298=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary4547); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS298);

                    pushFollow(FOLLOW_unary_in_unary4551);
                    u=unary();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unary.add(u.getTree());


                    // AST REWRITE
                    // elements: u
                    // token labels: 
                    // rule labels: retval, u
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_u=new RewriteRuleSubtreeStream(adaptor,"rule u",u!=null?u.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 417:23: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:26: ^( NEG $u)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NEG, "NEG"), root_1);

                        adaptor.addChild(root_1, stream_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:418:9: TILDE u= unary
                    {
                    TILDE299=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary4571); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE299);

                    pushFollow(FOLLOW_unary_in_unary4575);
                    u=unary();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_unary.add(u.getTree());


                    // AST REWRITE
                    // elements: u
                    // token labels: 
                    // rule labels: retval, u
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_u=new RewriteRuleSubtreeStream(adaptor,"rule u",u!=null?u.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 418:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:418:30: ^( INV $u)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INV, "INV"), root_1);

                        adaptor.addChild(root_1, stream_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unary"

    public static class atom_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atom"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER300=null;
        Token FALSE301=null;
        Token TRUE302=null;
        Token CHAR_LITERAL303=null;
        Token STRING_LITERAL304=null;
        Token NIL305=null;
        Token STAR306=null;
        Token LPAREN310=null;
        Token RPAREN312=null;
        EulangParser.funcCall_return f = null;

        EulangParser.funcCall_return funcCall307 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef308 = null;

        EulangParser.tuple_return tuple309 = null;

        EulangParser.assignExpr_return assignExpr311 = null;

        EulangParser.code_return code313 = null;

        EulangParser.macro_return macro314 = null;


        CommonTree NUMBER300_tree=null;
        CommonTree FALSE301_tree=null;
        CommonTree TRUE302_tree=null;
        CommonTree CHAR_LITERAL303_tree=null;
        CommonTree STRING_LITERAL304_tree=null;
        CommonTree NIL305_tree=null;
        CommonTree STAR306_tree=null;
        CommonTree LPAREN310_tree=null;
        CommonTree RPAREN312_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_macro=new RewriteRuleSubtreeStream(adaptor,"rule macro");
        RewriteRuleSubtreeStream stream_tuple=new RewriteRuleSubtreeStream(adaptor,"rule tuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_code=new RewriteRuleSubtreeStream(adaptor,"rule code");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_funcCall=new RewriteRuleSubtreeStream(adaptor,"rule funcCall");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:6: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro )
            int alt77=13;
            alt77 = dfa77.predict(input);
            switch (alt77) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:421:7: NUMBER
                    {
                    NUMBER300=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom4603); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER300);



                    // AST REWRITE
                    // elements: NUMBER
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 421:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:421:42: ^( LIT NUMBER )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_NUMBER.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:422:9: FALSE
                    {
                    FALSE301=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom4646); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE301);



                    // AST REWRITE
                    // elements: FALSE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 422:39: -> ^( LIT FALSE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:422:42: ^( LIT FALSE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_FALSE.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:9: TRUE
                    {
                    TRUE302=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom4688); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE302);



                    // AST REWRITE
                    // elements: TRUE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 423:39: -> ^( LIT TRUE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:42: ^( LIT TRUE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_TRUE.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:424:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL303=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom4731); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL303);



                    // AST REWRITE
                    // elements: CHAR_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 424:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:424:42: ^( LIT CHAR_LITERAL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_CHAR_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:425:9: STRING_LITERAL
                    {
                    STRING_LITERAL304=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom4766); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL304);



                    // AST REWRITE
                    // elements: STRING_LITERAL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 425:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:425:42: ^( LIT STRING_LITERAL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:9: NIL
                    {
                    NIL305=(Token)match(input,NIL,FOLLOW_NIL_in_atom4799); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL305);



                    // AST REWRITE
                    // elements: NIL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 426:38: -> ^( LIT NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:41: ^( LIT NIL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_NIL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:427:9: ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall
                    {
                    STAR306=(Token)match(input,STAR,FOLLOW_STAR_in_atom4853); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STAR.add(STAR306);

                    pushFollow(FOLLOW_funcCall_in_atom4857);
                    f=funcCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_funcCall.add(f.getTree());


                    // AST REWRITE
                    // elements: f
                    // token labels: 
                    // rule labels: f, retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_f=new RewriteRuleSubtreeStream(adaptor,"rule f",f!=null?f.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 427:57: -> ^( INLINE $f)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:427:60: ^( INLINE $f)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INLINE, "INLINE"), root_1);

                        adaptor.addChild(root_1, stream_f.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:428:9: ( idOrScopeRef LPAREN )=> funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom4886);
                    funcCall307=funcCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_funcCall.add(funcCall307.getTree());


                    // AST REWRITE
                    // elements: funcCall
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 428:46: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:431:9: idOrScopeRef
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_atom4912);
                    idOrScopeRef308=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef308.getTree());


                    // AST REWRITE
                    // elements: idOrScopeRef
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 431:39: -> idOrScopeRef
                    {
                        adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_atom4951);
                    tuple309=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple309.getTree());


                    // AST REWRITE
                    // elements: tuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 432:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:433:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN310=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom4990); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN310);

                    pushFollow(FOLLOW_assignExpr_in_atom4992);
                    assignExpr311=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr311.getTree());
                    RPAREN312=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom4994); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN312);



                    // AST REWRITE
                    // elements: assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 433:48: -> assignExpr
                    {
                        adaptor.addChild(root_0, stream_assignExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 12 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:434:9: code
                    {
                    pushFollow(FOLLOW_code_in_atom5022);
                    code313=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_code.add(code313.getTree());


                    // AST REWRITE
                    // elements: code
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 434:40: -> code
                    {
                        adaptor.addChild(root_0, stream_code.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 13 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:435:9: macro
                    {
                    pushFollow(FOLLOW_macro_in_atom5065);
                    macro314=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_macro.add(macro314.getTree());


                    // AST REWRITE
                    // elements: macro
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 435:41: -> macro
                    {
                        adaptor.addChild(root_0, stream_macro.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "atom"

    public static class idOrScopeRef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idOrScopeRef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:1: idOrScopeRef : ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID315=null;
        Token PERIOD316=null;
        Token ID317=null;
        Token ID318=null;
        Token PERIOD319=null;
        Token ID320=null;
        EulangParser.colons_return c = null;


        CommonTree ID315_tree=null;
        CommonTree PERIOD316_tree=null;
        CommonTree ID317_tree=null;
        CommonTree ID318_tree=null;
        CommonTree PERIOD319_tree=null;
        CommonTree ID320_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:14: ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) )
            int alt80=2;
            int LA80_0 = input.LA(1);

            if ( (LA80_0==ID) ) {
                alt80=1;
            }
            else if ( (LA80_0==COLON||LA80_0==COLONS) ) {
                alt80=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                throw nvae;
            }
            switch (alt80) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:16: ID ( PERIOD ID )*
                    {
                    ID315=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5111); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID315);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:19: ( PERIOD ID )*
                    loop78:
                    do {
                        int alt78=2;
                        int LA78_0 = input.LA(1);

                        if ( (LA78_0==PERIOD) ) {
                            alt78=1;
                        }


                        switch (alt78) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:21: PERIOD ID
                    	    {
                    	    PERIOD316=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef5115); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD316);

                    	    ID317=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5117); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID317);


                    	    }
                    	    break;

                    	default :
                    	    break loop78;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 438:35: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:38: ^( IDREF ( ID )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        if ( !(stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext() ) {
                            adaptor.addChild(root_1, stream_ID.nextNode());

                        }
                        stream_ID.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:443:9: c= colons ID ( PERIOD ID )*
                    {
                    pushFollow(FOLLOW_colons_in_idOrScopeRef5172);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID318=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5174); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID318);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:443:21: ( PERIOD ID )*
                    loop79:
                    do {
                        int alt79=2;
                        int LA79_0 = input.LA(1);

                        if ( (LA79_0==PERIOD) ) {
                            alt79=1;
                        }


                        switch (alt79) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:443:23: PERIOD ID
                    	    {
                    	    PERIOD319=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef5178); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD319);

                    	    ID320=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5180); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID320);


                    	    }
                    	    break;

                    	default :
                    	    break loop79;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 443:37: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:443:40: ^( IDREF ( ID )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, split((c!=null?((CommonTree)c.tree):null)));
                        if ( !(stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext() ) {
                            adaptor.addChild(root_1, stream_ID.nextNode());

                        }
                        stream_ID.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "idOrScopeRef"

    public static class colons_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "colons"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:446:1: colons : ( COLON | COLONS )+ ;
    public final EulangParser.colons_return colons() throws RecognitionException {
        EulangParser.colons_return retval = new EulangParser.colons_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set321=null;

        CommonTree set321_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:446:8: ( ( COLON | COLONS )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:446:10: ( COLON | COLONS )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:446:10: ( COLON | COLONS )+
            int cnt81=0;
            loop81:
            do {
                int alt81=2;
                int LA81_0 = input.LA(1);

                if ( (LA81_0==COLON||LA81_0==COLONS) ) {
                    alt81=1;
                }


                switch (alt81) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            	    {
            	    set321=(Token)input.LT(1);
            	    if ( input.LA(1)==COLON||input.LA(1)==COLONS ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set321));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt81 >= 1 ) break loop81;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(81, input);
                        throw eee;
                }
                cnt81++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "colons"

    // $ANTLR start synpred1_Eulang
    public final void synpred1_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:16: ( ID EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:17: ID EQUALS
        {
        match(input,ID,FOLLOW_ID_in_synpred1_Eulang369); if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred1_Eulang371); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Eulang

    // $ANTLR start synpred2_Eulang
    public final void synpred2_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:8: ( ID COLON )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:9: ID COLON
        {
        match(input,ID,FOLLOW_ID_in_synpred2_Eulang407); if (state.failed) return ;
        match(input,COLON,FOLLOW_COLON_in_synpred2_Eulang409); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Eulang

    // $ANTLR start synpred3_Eulang
    public final void synpred3_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:98:8: ( ID COLON_EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:98:9: ID COLON_EQUALS
        {
        match(input,ID,FOLLOW_ID_in_synpred3_Eulang455); if (state.failed) return ;
        match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_synpred3_Eulang457); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Eulang

    // $ANTLR start synpred4_Eulang
    public final void synpred4_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:104:7: ( LPAREN ( RPAREN | ID ) )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:104:10: LPAREN ( RPAREN | ID )
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred4_Eulang550); if (state.failed) return ;
        if ( input.LA(1)==ID||input.LA(1)==RPAREN ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }


        }
    }
    // $ANTLR end synpred4_Eulang

    // $ANTLR start synpred5_Eulang
    public final void synpred5_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:402:12: ( MINUS term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:402:14: MINUS term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred5_Eulang4178); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred5_Eulang4180);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Eulang

    // $ANTLR start synpred6_Eulang
    public final void synpred6_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:407:11: ( STAR unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:407:13: STAR unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred6_Eulang4271); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred6_Eulang4273);
        unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Eulang

    // $ANTLR start synpred7_Eulang
    public final void synpred7_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:427:9: ( STAR idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:427:11: STAR idOrScopeRef LPAREN
        {
        match(input,STAR,FOLLOW_STAR_in_synpred7_Eulang4844); if (state.failed) return ;
        pushFollow(FOLLOW_idOrScopeRef_in_synpred7_Eulang4846);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred7_Eulang4848); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Eulang

    // $ANTLR start synpred8_Eulang
    public final void synpred8_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:428:9: ( idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:428:10: idOrScopeRef LPAREN
        {
        pushFollow(FOLLOW_idOrScopeRef_in_synpred8_Eulang4878);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred8_Eulang4880); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Eulang

    // $ANTLR start synpred9_Eulang
    public final void synpred9_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred9_Eulang4945);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Eulang

    // Delegated rules

    public final boolean synpred6_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred9_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA42 dfa42 = new DFA42(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA47 dfa47 = new DFA47(this);
    protected DFA77 dfa77 = new DFA77(this);
    static final String DFA4_eotS =
        "\22\uffff";
    static final String DFA4_eofS =
        "\22\uffff";
    static final String DFA4_minS =
        "\1\6\1\uffff\1\6\2\uffff\1\56\1\55\3\uffff\1\6\1\0\1\uffff\1\56"+
        "\1\55\1\0\2\uffff";
    static final String DFA4_maxS =
        "\1\152\1\uffff\1\152\2\uffff\1\151\1\71\3\uffff\1\152\1\0\1\uffff"+
        "\1\151\1\71\1\0\2\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\1\4\2\uffff\3\2\2\uffff\1\2\3\uffff\2\2";
    static final String DFA4_specialS =
        "\2\uffff\1\1\2\uffff\1\3\1\5\3\uffff\1\4\1\6\1\uffff\1\0\1\2\1\7"+
        "\2\uffff}>";
    static final String[] DFA4_transitionS = {
            "\2\4\45\uffff\1\4\2\uffff\1\4\1\uffff\1\3\2\uffff\1\1\3\uffff"+
            "\1\2\2\uffff\1\4\16\uffff\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff"+
            "\6\4\1\uffff\1\4",
            "",
            "\1\4\1\6\45\uffff\1\5\2\uffff\1\4\10\uffff\1\4\1\10\1\7\1\4"+
            "\16\uffff\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff\1"+
            "\4",
            "",
            "",
            "\1\4\1\uffff\1\11\3\uffff\1\12\4\uffff\1\4\1\13\1\7\1\uffff"+
            "\2\4\3\uffff\1\4\14\uffff\1\4\1\uffff\22\4\6\uffff\1\4",
            "\1\14\7\uffff\1\4\3\uffff\1\4",
            "",
            "",
            "",
            "\1\4\1\16\45\uffff\1\15\2\uffff\1\4\10\uffff\1\4\1\10\1\7\1"+
            "\4\16\uffff\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff"+
            "\1\4",
            "\1\uffff",
            "",
            "\1\4\1\uffff\1\20\3\uffff\1\12\4\uffff\1\4\1\17\1\7\1\uffff"+
            "\2\4\3\uffff\1\4\14\uffff\1\4\1\uffff\22\4\6\uffff\1\4",
            "\1\21\7\uffff\1\4\3\uffff\1\4",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "103:1: toplevelvalue : ( xscope | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_13 = input.LA(1);

                         
                        int index4_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_13==EQUALS||LA4_13==LPAREN||(LA4_13>=QUESTION && LA4_13<=AMP)||LA4_13==AND||LA4_13==OR||(LA4_13>=COMPEQ && LA4_13<=UMOD)||LA4_13==PERIOD) ) {s = 4;}

                        else if ( (LA4_13==RPAREN) ) {s = 15;}

                        else if ( (LA4_13==COMMA) ) {s = 10;}

                        else if ( (LA4_13==COLON) && (synpred4_Eulang())) {s = 16;}

                        else if ( (LA4_13==ARROW) && (synpred4_Eulang())) {s = 7;}

                         
                        input.seek(index4_13);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_2 = input.LA(1);

                         
                        int index4_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_2==ID) ) {s = 5;}

                        else if ( (LA4_2==CODE||LA4_2==COLON||LA4_2==LPAREN||LA4_2==NIL||LA4_2==IF||LA4_2==NOT||(LA4_2>=MINUS && LA4_2<=STAR)||(LA4_2>=TILDE && LA4_2<=STRING_LITERAL)||LA4_2==COLONS) ) {s = 4;}

                        else if ( (LA4_2==MACRO) ) {s = 6;}

                        else if ( (LA4_2==ARROW) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA4_2==RPAREN) && (synpred4_Eulang())) {s = 8;}

                         
                        input.seek(index4_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA4_14 = input.LA(1);

                         
                        int index4_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_14==LBRACE||LA4_14==LPAREN) ) {s = 4;}

                        else if ( (LA4_14==ID) && (synpred4_Eulang())) {s = 17;}

                         
                        input.seek(index4_14);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA4_5 = input.LA(1);

                         
                        int index4_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_5==COLON) && (synpred4_Eulang())) {s = 9;}

                        else if ( (LA4_5==COMMA) ) {s = 10;}

                        else if ( (LA4_5==ARROW) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA4_5==RPAREN) ) {s = 11;}

                        else if ( (LA4_5==EQUALS||LA4_5==LPAREN||(LA4_5>=QUESTION && LA4_5<=AMP)||LA4_5==AND||LA4_5==OR||(LA4_5>=COMPEQ && LA4_5<=UMOD)||LA4_5==PERIOD) ) {s = 4;}

                         
                        input.seek(index4_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA4_10 = input.LA(1);

                         
                        int index4_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_10==ARROW) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA4_10==RPAREN) && (synpred4_Eulang())) {s = 8;}

                        else if ( (LA4_10==ID) ) {s = 13;}

                        else if ( (LA4_10==CODE||LA4_10==COLON||LA4_10==LPAREN||LA4_10==NIL||LA4_10==IF||LA4_10==NOT||(LA4_10>=MINUS && LA4_10<=STAR)||(LA4_10>=TILDE && LA4_10<=STRING_LITERAL)||LA4_10==COLONS) ) {s = 4;}

                        else if ( (LA4_10==MACRO) ) {s = 14;}

                         
                        input.seek(index4_10);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA4_6 = input.LA(1);

                         
                        int index4_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_6==LBRACE||LA4_6==LPAREN) ) {s = 4;}

                        else if ( (LA4_6==ID) && (synpred4_Eulang())) {s = 12;}

                         
                        input.seek(index4_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA4_11 = input.LA(1);

                         
                        int index4_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index4_11);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA4_15 = input.LA(1);

                         
                        int index4_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index4_15);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 4, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA42_eotS =
        "\37\uffff";
    static final String DFA42_eofS =
        "\1\uffff\1\4\13\uffff\2\4\10\uffff\1\4\1\uffff\1\4\5\uffff";
    static final String DFA42_minS =
        "\1\6\1\56\1\6\1\55\5\uffff\1\55\1\uffff\1\56\1\55\2\56\1\55\1\6"+
        "\1\56\1\55\2\56\2\55\1\56\1\55\4\56\1\55\1\56";
    static final String DFA42_maxS =
        "\1\152\1\151\2\152\5\uffff\1\55\1\uffff\1\151\1\152\2\151\1\55\1"+
        "\152\1\151\1\55\2\151\1\152\1\55\1\151\1\55\1\142\3\151\1\55\1\151";
    static final String DFA42_acceptS =
        "\4\uffff\1\3\1\4\1\5\1\6\1\1\1\uffff\1\2\24\uffff";
    static final String DFA42_specialS =
        "\37\uffff}>";
    static final String[] DFA42_transitionS = {
            "\2\4\40\uffff\1\6\4\uffff\1\1\2\uffff\1\3\4\uffff\1\5\1\uffff"+
            "\1\7\1\uffff\1\2\2\uffff\1\4\2\uffff\3\7\11\uffff\1\4\4\uffff"+
            "\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff\1\3",
            "\1\12\1\4\2\10\4\uffff\1\4\2\uffff\1\4\3\uffff\2\4\1\uffff"+
            "\1\4\1\uffff\1\4\14\uffff\1\4\1\uffff\22\4\6\uffff\1\11",
            "\2\4\45\uffff\1\13\2\uffff\1\14\10\uffff\1\4\2\uffff\1\4\16"+
            "\uffff\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff\1\14",
            "\1\15\2\uffff\1\3\71\uffff\1\3",
            "",
            "",
            "",
            "",
            "",
            "\1\16",
            "",
            "\1\4\5\uffff\1\20\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\17",
            "\1\21\2\uffff\1\14\71\uffff\1\14",
            "\1\12\1\4\6\uffff\1\4\2\uffff\1\4\3\uffff\2\4\1\uffff\1\4\1"+
            "\uffff\1\4\14\uffff\1\4\1\uffff\22\4\6\uffff\1\22",
            "\1\12\1\4\6\uffff\1\4\2\uffff\1\4\3\uffff\2\4\1\uffff\1\4\1"+
            "\uffff\1\4\14\uffff\1\4\1\uffff\22\4\6\uffff\1\11",
            "\1\23",
            "\2\4\45\uffff\1\24\2\uffff\1\25\10\uffff\1\4\2\uffff\1\4\16"+
            "\uffff\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff\1\25",
            "\1\4\5\uffff\1\20\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\26",
            "\1\27",
            "\1\4\5\uffff\1\20\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\17",
            "\1\4\5\uffff\1\20\4\uffff\1\4\1\31\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\30",
            "\1\32\2\uffff\1\25\71\uffff\1\25",
            "\1\33",
            "\1\12\1\4\6\uffff\1\4\2\uffff\1\4\3\uffff\2\4\1\uffff\1\4\1"+
            "\uffff\1\4\14\uffff\1\4\1\uffff\22\4\6\uffff\1\22",
            "\1\34",
            "\1\12\1\4\2\10\4\uffff\1\4\6\uffff\2\4\1\uffff\1\4\1\uffff"+
            "\1\4\14\uffff\1\4\1\uffff\22\4",
            "\1\4\5\uffff\1\20\4\uffff\1\4\1\31\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\35",
            "\1\4\5\uffff\1\20\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\26",
            "\1\4\5\uffff\1\20\4\uffff\1\4\1\31\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\30",
            "\1\36",
            "\1\4\5\uffff\1\20\4\uffff\1\4\1\31\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\35"
    };

    static final short[] DFA42_eot = DFA.unpackEncodedString(DFA42_eotS);
    static final short[] DFA42_eof = DFA.unpackEncodedString(DFA42_eofS);
    static final char[] DFA42_min = DFA.unpackEncodedStringToUnsignedChars(DFA42_minS);
    static final char[] DFA42_max = DFA.unpackEncodedStringToUnsignedChars(DFA42_maxS);
    static final short[] DFA42_accept = DFA.unpackEncodedString(DFA42_acceptS);
    static final short[] DFA42_special = DFA.unpackEncodedString(DFA42_specialS);
    static final short[][] DFA42_transition;

    static {
        int numStates = DFA42_transitionS.length;
        DFA42_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA42_transition[i] = DFA.unpackEncodedString(DFA42_transitionS[i]);
        }
    }

    class DFA42 extends DFA {

        public DFA42(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 42;
            this.eot = DFA42_eot;
            this.eof = DFA42_eof;
            this.min = DFA42_min;
            this.max = DFA42_max;
            this.accept = DFA42_accept;
            this.special = DFA42_special;
            this.transition = DFA42_transition;
        }
        public String getDescription() {
            return "199:1: codeStmtExpr : ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );";
        }
    }
    static final String DFA45_eotS =
        "\27\uffff";
    static final String DFA45_eofS =
        "\27\uffff";
    static final String DFA45_minS =
        "\1\55\1\60\1\55\2\uffff\1\64\3\55\3\64\3\55\1\60\3\64\2\uffff\1"+
        "\55\1\64";
    static final String DFA45_maxS =
        "\1\71\1\61\1\152\2\uffff\1\151\1\152\1\55\1\152\3\151\1\152\2\55"+
        "\1\61\3\151\2\uffff\1\55\1\151";
    static final String DFA45_acceptS =
        "\3\uffff\1\1\1\3\16\uffff\1\2\1\4\2\uffff";
    static final String DFA45_specialS =
        "\27\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\1\13\uffff\1\2",
            "\1\4\1\3",
            "\1\5\2\uffff\1\6\71\uffff\1\6",
            "",
            "",
            "\1\10\64\uffff\1\7",
            "\1\11\2\uffff\1\6\71\uffff\1\6",
            "\1\12",
            "\1\13\2\uffff\1\14\71\uffff\1\14",
            "\1\10\64\uffff\1\15",
            "\1\10\64\uffff\1\7",
            "\1\10\5\uffff\1\17\56\uffff\1\16",
            "\1\20\2\uffff\1\14\71\uffff\1\14",
            "\1\21",
            "\1\22",
            "\1\24\1\23",
            "\1\10\5\uffff\1\17\56\uffff\1\25",
            "\1\10\64\uffff\1\15",
            "\1\10\5\uffff\1\17\56\uffff\1\16",
            "",
            "",
            "\1\26",
            "\1\10\5\uffff\1\17\56\uffff\1\25"
    };

    static final short[] DFA45_eot = DFA.unpackEncodedString(DFA45_eotS);
    static final short[] DFA45_eof = DFA.unpackEncodedString(DFA45_eofS);
    static final char[] DFA45_min = DFA.unpackEncodedStringToUnsignedChars(DFA45_minS);
    static final char[] DFA45_max = DFA.unpackEncodedStringToUnsignedChars(DFA45_maxS);
    static final short[] DFA45_accept = DFA.unpackEncodedString(DFA45_acceptS);
    static final short[] DFA45_special = DFA.unpackEncodedString(DFA45_specialS);
    static final short[][] DFA45_transition;

    static {
        int numStates = DFA45_transitionS.length;
        DFA45_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA45_transition[i] = DFA.unpackEncodedString(DFA45_transitionS[i]);
        }
    }

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = DFA45_eot;
            this.eof = DFA45_eof;
            this.min = DFA45_min;
            this.max = DFA45_max;
            this.accept = DFA45_accept;
            this.special = DFA45_special;
            this.transition = DFA45_transition;
        }
        public String getDescription() {
            return "211:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) );";
        }
    }
    static final String DFA47_eotS =
        "\34\uffff";
    static final String DFA47_eofS =
        "\1\uffff\1\4\5\uffff\1\4\2\uffff\1\4\4\uffff\1\4\5\uffff\1\4\6\uffff";
    static final String DFA47_minS =
        "\1\6\1\56\1\55\1\6\1\uffff\1\55\1\uffff\2\56\1\55\1\56\2\55\1\6"+
        "\4\56\3\55\4\56\1\uffff\1\55\1\56";
    static final String DFA47_maxS =
        "\1\152\1\151\2\152\1\uffff\1\55\1\uffff\2\151\1\152\1\151\2\55\1"+
        "\152\4\151\1\152\2\55\1\142\3\151\1\uffff\1\55\1\151";
    static final String DFA47_acceptS =
        "\4\uffff\1\3\1\uffff\1\1\22\uffff\1\2\2\uffff";
    static final String DFA47_specialS =
        "\34\uffff}>";
    static final String[] DFA47_transitionS = {
            "\2\4\45\uffff\1\1\2\uffff\1\2\10\uffff\1\3\2\uffff\1\4\16\uffff"+
            "\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff\1\2",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\4\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\22\4"+
            "\6\uffff\1\5",
            "\1\7\2\uffff\1\2\71\uffff\1\2",
            "\2\4\45\uffff\1\10\2\uffff\1\11\10\uffff\1\4\2\uffff\1\4\16"+
            "\uffff\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff\1\11",
            "",
            "\1\12",
            "",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\4\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\22\4"+
            "\6\uffff\1\13",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\14",
            "\1\16\2\uffff\1\11\71\uffff\1\11",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\4\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\22\4"+
            "\6\uffff\1\5",
            "\1\17",
            "\1\20",
            "\2\4\45\uffff\1\21\2\uffff\1\22\10\uffff\1\4\2\uffff\1\4\16"+
            "\uffff\1\4\4\uffff\1\4\14\uffff\2\4\4\uffff\6\4\1\uffff\1\22",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\23",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\4\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\22\4"+
            "\6\uffff\1\13",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\14",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\24",
            "\1\26\2\uffff\1\22\71\uffff\1\22",
            "\1\27",
            "\1\30",
            "\1\31\1\4\3\uffff\2\4\1\uffff\1\4\3\uffff\2\4\1\uffff\4\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\22\4",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\32",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\22\4\6\uffff\1\23",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\24",
            "",
            "\1\33",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\22\4\6\uffff\1\32"
    };

    static final short[] DFA47_eot = DFA.unpackEncodedString(DFA47_eotS);
    static final short[] DFA47_eof = DFA.unpackEncodedString(DFA47_eofS);
    static final char[] DFA47_min = DFA.unpackEncodedStringToUnsignedChars(DFA47_minS);
    static final char[] DFA47_max = DFA.unpackEncodedStringToUnsignedChars(DFA47_maxS);
    static final short[] DFA47_accept = DFA.unpackEncodedString(DFA47_acceptS);
    static final short[] DFA47_special = DFA.unpackEncodedString(DFA47_specialS);
    static final short[][] DFA47_transition;

    static {
        int numStates = DFA47_transitionS.length;
        DFA47_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA47_transition[i] = DFA.unpackEncodedString(DFA47_transitionS[i]);
        }
    }

    class DFA47 extends DFA {

        public DFA47(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 47;
            this.eot = DFA47_eot;
            this.eof = DFA47_eof;
            this.min = DFA47_min;
            this.max = DFA47_max;
            this.accept = DFA47_accept;
            this.special = DFA47_special;
            this.transition = DFA47_transition;
        }
        public String getDescription() {
            return "224:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr );";
        }
    }
    static final String DFA77_eotS =
        "\21\uffff";
    static final String DFA77_eofS =
        "\21\uffff";
    static final String DFA77_minS =
        "\1\6\7\uffff\3\0\6\uffff";
    static final String DFA77_maxS =
        "\1\152\7\uffff\3\0\6\uffff";
    static final String DFA77_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\3\uffff\1\14\1\15\1\10\1\11"+
        "\1\12\1\13";
    static final String DFA77_specialS =
        "\1\0\7\uffff\1\1\1\2\1\3\6\uffff}>";
    static final String[] DFA77_transitionS = {
            "\1\13\1\14\45\uffff\1\10\2\uffff\1\11\10\uffff\1\12\2\uffff"+
            "\1\6\41\uffff\1\7\5\uffff\1\1\1\2\1\3\1\4\1\5\1\uffff\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA77_eot = DFA.unpackEncodedString(DFA77_eotS);
    static final short[] DFA77_eof = DFA.unpackEncodedString(DFA77_eofS);
    static final char[] DFA77_min = DFA.unpackEncodedStringToUnsignedChars(DFA77_minS);
    static final char[] DFA77_max = DFA.unpackEncodedStringToUnsignedChars(DFA77_maxS);
    static final short[] DFA77_accept = DFA.unpackEncodedString(DFA77_acceptS);
    static final short[] DFA77_special = DFA.unpackEncodedString(DFA77_specialS);
    static final short[][] DFA77_transition;

    static {
        int numStates = DFA77_transitionS.length;
        DFA77_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA77_transition[i] = DFA.unpackEncodedString(DFA77_transitionS[i]);
        }
    }

    class DFA77 extends DFA {

        public DFA77(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 77;
            this.eot = DFA77_eot;
            this.eof = DFA77_eof;
            this.min = DFA77_min;
            this.max = DFA77_max;
            this.accept = DFA77_accept;
            this.special = DFA77_special;
            this.transition = DFA77_transition;
        }
        public String getDescription() {
            return "420:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA77_0 = input.LA(1);

                         
                        int index77_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA77_0==NUMBER) ) {s = 1;}

                        else if ( (LA77_0==FALSE) ) {s = 2;}

                        else if ( (LA77_0==TRUE) ) {s = 3;}

                        else if ( (LA77_0==CHAR_LITERAL) ) {s = 4;}

                        else if ( (LA77_0==STRING_LITERAL) ) {s = 5;}

                        else if ( (LA77_0==NIL) ) {s = 6;}

                        else if ( (LA77_0==STAR) && (synpred7_Eulang())) {s = 7;}

                        else if ( (LA77_0==ID) ) {s = 8;}

                        else if ( (LA77_0==COLON||LA77_0==COLONS) ) {s = 9;}

                        else if ( (LA77_0==LPAREN) ) {s = 10;}

                        else if ( (LA77_0==CODE) ) {s = 11;}

                        else if ( (LA77_0==MACRO) ) {s = 12;}

                         
                        input.seek(index77_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA77_8 = input.LA(1);

                         
                        int index77_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Eulang()) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index77_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA77_9 = input.LA(1);

                         
                        int index77_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Eulang()) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index77_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA77_10 = input.LA(1);

                         
                        int index77_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Eulang()) ) {s = 15;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index77_10);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 77, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog304 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts335 = new BitSet(new long[]{0x12212000000000C2L,0x000005F860010800L});
    public static final BitSet FOLLOW_ID_in_toplevelstat376 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat378 = new BitSet(new long[]{0x12252000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat380 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat414 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_toplevelstat416 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_toplevelstat418 = new BitSet(new long[]{0x0000C00000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat421 = new BitSet(new long[]{0x12252000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat423 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat462 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelstat464 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat466 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat490 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstat526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_toplevelvalue564 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector598 = new BitSet(new long[]{0x12892000000000C0L,0x000005F040000000L});
    public static final BitSet FOLLOW_selectors_in_selector600 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors628 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_selectors632 = new BitSet(new long[]{0x12812000000000C0L,0x000005F040000000L});
    public static final BitSet FOLLOW_selectoritem_in_selectors634 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_selectors639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listCompr_in_selectoritem668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_selectoritem672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_selectoritem676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope687 = new BitSet(new long[]{0x12212000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope689 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr718 = new BitSet(new long[]{0x0081000000000000L});
    public static final BitSet FOLLOW_COLON_in_listCompr721 = new BitSet(new long[]{0x12012000000000C0L,0x000005F040000000L});
    public static final BitSet FOLLOW_listiterable_in_listCompr723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn755 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_idlist_in_forIn757 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_IN_in_forIn759 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_list_in_forIn761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist786 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_idlist789 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idlist791 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_code_in_listiterable820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_listiterable824 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_listiterable828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list843 = new BitSet(new long[]{0x122D2000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_listitems_in_list845 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_list847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems877 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems881 = new BitSet(new long[]{0x12252000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_listitem_in_listitems883 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem914 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_code932 = new BitSet(new long[]{0x0220000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_code936 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_optargdefs_in_code938 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_code940 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_code943 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_code949 = new BitSet(new long[]{0x92E12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codestmtlist_in_code951 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_code953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_macro988 = new BitSet(new long[]{0x0220000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_macro992 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_optargdefs_in_macro994 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_macro996 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_macro999 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_macro1005 = new BitSet(new long[]{0x92E12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codestmtlist_in_macro1007 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_macro1009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto1044 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_argdefs_in_proto1046 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_proto1048 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_proto1051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdef_in_argdefs1093 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1097 = new BitSet(new long[]{0x0000200000000080L});
    public static final BitSet FOLLOW_argdef_in_argdefs1099 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_argdef1147 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_argdef1150 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COLON_in_argdef1153 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_argdef1155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns1185 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_xreturns1187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns1202 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_argtuple_in_xreturns1204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns1224 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_NIL_in_xreturns1226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_argtuple1256 = new BitSet(new long[]{0x2011200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_tupleargdefs_in_argtuple1258 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_argtuple1260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs1282 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_tupleargdefs1286 = new BitSet(new long[]{0x2011200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs1288 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_type_in_tupleargdef1333 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_tupleargdef1346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_optargdef_in_optargdefs1403 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_optargdefs1407 = new BitSet(new long[]{0x0000200000000080L});
    public static final BitSet FOLLOW_optargdef_in_optargdefs1409 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_optargdefs1413 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_optargdef1457 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COLON_in_optargdef1460 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_optargdef1462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_optargdef1486 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_optargdef1488 = new BitSet(new long[]{0x0001400000000002L});
    public static final BitSet FOLLOW_COLON_in_optargdef1491 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_optargdef1493 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_optargdef1498 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_optargdef1502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_type1540 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_AMP_in_type1555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_type1581 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_proto_in_type1583 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1611 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist1614 = new BitSet(new long[]{0x92A1A100000000C2L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1616 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_labelStmt_in_codeStmt1665 = new BitSet(new long[]{0x92A12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt1667 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt1688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmtExpr1707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_codeStmtExpr1724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_codeStmtExpr1748 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blockStmt_in_codeStmtExpr1773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_codeStmtExpr1802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_controlStmt_in_codeStmtExpr1835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1858 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1860 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_varDecl1890 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1892 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1894 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1922 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl1924 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_varDecl1926 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1929 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_varDecl1955 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl1957 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_varDecl1959 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1962 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignStmt2002 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt2004 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt2006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignStmt2031 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt2033 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt2035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignExpr2078 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr2080 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr2082 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignExpr2107 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr2109 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr2111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr2143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_doWhile_in_controlStmt2189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whileDo_in_controlStmt2193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_repeat_in_controlStmt2197 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIter_in_controlStmt2201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_doWhile2210 = new BitSet(new long[]{0x92A12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codeStmtExpr_in_doWhile2212 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_WHILE_in_doWhile2214 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_doWhile2216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_whileDo2239 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_whileDo2241 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_DO_in_whileDo2243 = new BitSet(new long[]{0x92A12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codeStmtExpr_in_whileDo2245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REPEAT_in_repeat2270 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_repeat2272 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_DO_in_repeat2274 = new BitSet(new long[]{0x92A12100000000C0L,0x000005F860010C13L});
    public static final BitSet FOLLOW_codeOrValueExpr_in_repeat2276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIter2306 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_forIds_in_forIter2308 = new BitSet(new long[]{0x0100000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_atId_in_forIter2310 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_IN_in_forIter2313 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_forIter2315 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_DO_in_forIter2317 = new BitSet(new long[]{0x92A12100000000C0L,0x000005F860010C13L});
    public static final BitSet FOLLOW_codeOrValueExpr_in_forIter2319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_forIds2352 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_AND_in_forIds2355 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_forIds2357 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_AT_in_atId2373 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_atId2375 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codeOrValueExpr2400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_breakStmt_in_codeOrValueExpr2404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_breakStmt2413 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010A80L});
    public static final BitSet FOLLOW_valueCond_in_breakStmt2415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2436 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_WHEN_in_valueCond2438 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2440 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ELSE_in_valueCond2442 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2444 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNTIL_in_valueCond2461 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2463 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_THEN_in_valueCond2465 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2467 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ELSE_in_valueCond2469 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WITH_in_valueCond2489 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATSIGN_in_labelStmt2542 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_labelStmt2544 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_labelStmt2546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GOTO_in_gotoStmt2582 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_gotoStmt2584 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L});
    public static final BitSet FOLLOW_IF_in_gotoStmt2587 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_gotoStmt2589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_blockStmt2626 = new BitSet(new long[]{0x92E12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codestmtlist_in_blockStmt2628 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_blockStmt2630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tuple2653 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_tupleEntries_in_tuple2655 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_tuple2657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries2685 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_tupleEntries2688 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries2690 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_idTuple2709 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idTupleEntries_in_idTuple2711 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_idTuple2713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries2741 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_idTupleEntries2744 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries2746 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_condStar_in_rhsExpr2767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_funcCall2788 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall2790 = new BitSet(new long[]{0x16212100000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_arglist_in_funcCall2792 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall2794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist2825 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist2829 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_arg_in_arglist2831 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist2835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg2884 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_arg2917 = new BitSet(new long[]{0x92E12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codestmtlist_in_arg2919 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_arg2921 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_arg2945 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WITH_in_withStmt2998 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_bindings_in_withStmt3000 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_ARROW_in_withStmt3002 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_rhsExpr_in_withStmt3006 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_ELSE_in_withStmt3009 = new BitSet(new long[]{0x92A12100000000C0L,0x000005F860010C03L});
    public static final BitSet FOLLOW_codeStmtExpr_in_withStmt3013 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_binding_in_bindings3041 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_AND_in_bindings3044 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_binding_in_bindings3046 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_rhsExpr_in_binding3064 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_AS_in_binding3066 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_binding3068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_condStar3098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_condStar3113 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_ifExprs_in_condStar3115 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_thenClause_in_ifExprs3142 = new BitSet(new long[]{0x0000000000000000L,0x0000000000006040L});
    public static final BitSet FOLLOW_elses_in_ifExprs3144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_thenClause3166 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_THEN_in_thenClause3168 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_arg_in_thenClause3172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elif_in_elses3200 = new BitSet(new long[]{0x0000000000000000L,0x0000000000006040L});
    public static final BitSet FOLLOW_elseClause_in_elses3203 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELIF_in_elif3226 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_arg_in_elif3230 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_THEN_in_elif3232 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_arg_in_elif3236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELSE_in_elseClause3262 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_arg_in_elseClause3264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FI_in_elseClause3291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logor_in_cond3331 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond3348 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010000L});
    public static final BitSet FOLLOW_logor_in_cond3352 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_cond3354 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010000L});
    public static final BitSet FOLLOW_logor_in_cond3358 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_logand_in_logor3388 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_OR_in_logor3405 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010000L});
    public static final BitSet FOLLOW_logand_in_logor3409 = new BitSet(new long[]{0x0000000000000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_not_in_logand3440 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_AND_in_logand3456 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010000L});
    public static final BitSet FOLLOW_not_in_logand3460 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_comp_in_not3506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_not3522 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_comp_in_not3526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bitor_in_comp3560 = new BitSet(new long[]{0x0000000000000002L,0x00000000007E0000L});
    public static final BitSet FOLLOW_COMPEQ_in_comp3593 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3597 = new BitSet(new long[]{0x0000000000000002L,0x00000000007E0000L});
    public static final BitSet FOLLOW_COMPNE_in_comp3619 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3623 = new BitSet(new long[]{0x0000000000000002L,0x00000000007E0000L});
    public static final BitSet FOLLOW_COMPLE_in_comp3645 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3649 = new BitSet(new long[]{0x0000000000000002L,0x00000000007E0000L});
    public static final BitSet FOLLOW_COMPGE_in_comp3674 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3678 = new BitSet(new long[]{0x0000000000000002L,0x00000000007E0000L});
    public static final BitSet FOLLOW_LESS_in_comp3703 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3707 = new BitSet(new long[]{0x0000000000000002L,0x00000000007E0000L});
    public static final BitSet FOLLOW_GREATER_in_comp3733 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3737 = new BitSet(new long[]{0x0000000000000002L,0x00000000007E0000L});
    public static final BitSet FOLLOW_bitxor_in_bitor3787 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_BAR_in_bitor3815 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitxor_in_bitor3819 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_bitand_in_bitxor3845 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_CARET_in_bitxor3873 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitand_in_bitxor3877 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_shift_in_bitand3902 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_AMP_in_bitand3930 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_shift_in_bitand3934 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_factor_in_shift3961 = new BitSet(new long[]{0x0000000000000002L,0x000000000E000000L});
    public static final BitSet FOLLOW_LSHIFT_in_shift3995 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_factor_in_shift3999 = new BitSet(new long[]{0x0000000000000002L,0x000000000E000000L});
    public static final BitSet FOLLOW_RSHIFT_in_shift4028 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_factor_in_shift4032 = new BitSet(new long[]{0x0000000000000002L,0x000000000E000000L});
    public static final BitSet FOLLOW_URSHIFT_in_shift4060 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_factor_in_shift4064 = new BitSet(new long[]{0x0000000000000002L,0x000000000E000000L});
    public static final BitSet FOLLOW_term_in_factor4106 = new BitSet(new long[]{0x0000000000000002L,0x0000000030000000L});
    public static final BitSet FOLLOW_PLUS_in_factor4139 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_term_in_factor4143 = new BitSet(new long[]{0x0000000000000002L,0x0000000030000000L});
    public static final BitSet FOLLOW_MINUS_in_factor4185 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_term_in_factor4189 = new BitSet(new long[]{0x0000000000000002L,0x0000000030000000L});
    public static final BitSet FOLLOW_unary_in_term4234 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_STAR_in_term4278 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4282 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_SLASH_in_term4318 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4322 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_BACKSLASH_in_term4357 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4361 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_PERCENT_in_term4396 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4400 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_UMOD_in_term4435 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4439 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_atom_in_unary4516 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_unary4547 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_unary4551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary4571 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_unary4575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom4603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_atom4646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_atom4688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom4731 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom4766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NIL_in_atom4799 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_atom4853 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_funcCall_in_atom4857 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom4886 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_atom4912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_atom4951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom4990 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860010800L});
    public static final BitSet FOLLOW_assignExpr_in_atom4992 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_atom4994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_atom5022 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_atom5065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5111 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef5115 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5117 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_colons_in_idOrScopeRef5172 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5174 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef5178 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5180 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_set_in_colons5211 = new BitSet(new long[]{0x0001000000000002L,0x0000040000000000L});
    public static final BitSet FOLLOW_ID_in_synpred1_Eulang369 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_synpred1_Eulang371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred2_Eulang407 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_synpred2_Eulang409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred3_Eulang455 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_synpred3_Eulang457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred4_Eulang550 = new BitSet(new long[]{0x0400200000000000L});
    public static final BitSet FOLLOW_set_in_synpred4_Eulang552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred5_Eulang4178 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_term_in_synpred5_Eulang4180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred6_Eulang4271 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_synpred6_Eulang4273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred7_Eulang4844 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred7_Eulang4846 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred7_Eulang4848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred8_Eulang4878 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred8_Eulang4880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred9_Eulang4945 = new BitSet(new long[]{0x0000000000000002L});

}