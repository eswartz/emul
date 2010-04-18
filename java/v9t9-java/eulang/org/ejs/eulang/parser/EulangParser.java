// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-04-17 19:58:52

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCOPE", "LIST_COMPREHENSION", "CODE", "MACRO", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "ALLOC", "ASSIGN", "DEFINE", "EXPR", "LIST", "TYPE", "STMTEXPR", "CONDLIST", "CONDTEST", "CALL", "INLINE", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NEG", "INV", "LIT", "IDREF", "IDLIST", "LABEL", "GOTO", "BLOCK", "TUPLE", "LABELSTMT", "BINDING", "ID", "EQUALS", "SEMI", "COLON", "COLON_EQUALS", "LBRACKET", "RBRACKET", "COMMA", "LBRACE", "RBRACE", "FOR", "IN", "LPAREN", "RPAREN", "ARROW", "NIL", "QUESTION", "AMP", "PLUS", "DO", "WHILE", "REPEAT", "AND", "AT", "BREAK", "WHEN", "ELSE", "UNTIL", "THEN", "WITH", "ATSIGN", "IF", "AS", "ELIF", "FI", "OR", "NOT", "COMPEQ", "COMPNE", "COMPLE", "COMPGE", "LESS", "GREATER", "BAR", "CARET", "LSHIFT", "RSHIFT", "URSHIFT", "MINUS", "STAR", "SLASH", "BACKSLASH", "PERCENT", "UMOD", "TILDE", "NUMBER", "FALSE", "TRUE", "CHAR_LITERAL", "STRING_LITERAL", "PERIOD", "COLONS", "LBRACE_LPAREN", "LBRACE_STAR", "LBRACE_STAR_LPAREN", "COLON_COLON_EQUALS", "HASH", "POINTS", "BAR_BAR", "SELECT", "DATA", "END", "IDSUFFIX", "LETTERLIKE", "DIGIT", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT"
    };
    public static final int CONDTEST=21;
    public static final int STAR=94;
    public static final int WHILE=65;
    public static final int MOD=33;
    public static final int DO=64;
    public static final int ARGLIST=10;
    public static final int EQUALS=46;
    public static final int NOT=81;
    public static final int EOF=-1;
    public static final int BREAK=69;
    public static final int TYPE=18;
    public static final int CODE=6;
    public static final int LBRACKET=50;
    public static final int TUPLE=42;
    public static final int RPAREN=58;
    public static final int STRING_LITERAL=104;
    public static final int GREATER=87;
    public static final int COMPLE=84;
    public static final int CARET=89;
    public static final int LESS=86;
    public static final int LBRACE_STAR_LPAREN=109;
    public static final int ATSIGN=75;
    public static final int GOTO=40;
    public static final int SELECT=114;
    public static final int LABELSTMT=43;
    public static final int RBRACE=54;
    public static final int STMTEXPR=19;
    public static final int PERIOD=105;
    public static final int UMOD=98;
    public static final int INV=35;
    public static final int LSHIFT=90;
    public static final int ELSE=71;
    public static final int LBRACE_LPAREN=107;
    public static final int NUMBER=100;
    public static final int LIT=36;
    public static final int UDIV=32;
    public static final int LIST=17;
    public static final int MUL=30;
    public static final int ARGDEF=11;
    public static final int FI=79;
    public static final int ELIF=78;
    public static final int WS=121;
    public static final int BITOR=26;
    public static final int NIL=60;
    public static final int UNTIL=72;
    public static final int STMTLIST=8;
    public static final int OR=80;
    public static final int ALLOC=13;
    public static final int IDLIST=38;
    public static final int REPEAT=66;
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
    public static final int AND=67;
    public static final int DEFINE=15;
    public static final int BITAND=25;
    public static final int LPAREN=57;
    public static final int IF=76;
    public static final int COLONS=106;
    public static final int COLON_COLON_EQUALS=110;
    public static final int AT=68;
    public static final int AS=77;
    public static final int CONDLIST=20;
    public static final int IDSUFFIX=117;
    public static final int SLASH=95;
    public static final int EXPR=16;
    public static final int IN=56;
    public static final int THEN=73;
    public static final int SCOPE=4;
    public static final int COMMA=52;
    public static final int BITXOR=27;
    public static final int TILDE=99;
    public static final int PLUS=63;
    public static final int SINGLE_COMMENT=122;
    public static final int DIGIT=119;
    public static final int RBRACKET=51;
    public static final int RSHIFT=91;
    public static final int WITH=74;
    public static final int ADD=28;
    public static final int COMPGE=85;
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
    public static final int WHEN=70;
    public static final int BLOCK=41;
    public static final int NEG=34;
    public static final int ASSIGN=14;
    public static final int URSHIFT=92;
    public static final int ARROW=59;
    public static final int COMPEQ=82;
    public static final int IDREF=37;
    public static final int DIV=31;
    public static final int COND=24;
    public static final int MACRO=7;
    public static final int PROTO=9;
    public static final int COMPNE=83;
    public static final int DATA=115;
    public static final int BAR=88;

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
                else if ( (LA3_1==SEMI||LA3_1==LPAREN||(LA3_1>=QUESTION && LA3_1<=PLUS)||LA3_1==AND||LA3_1==OR||(LA3_1>=COMPEQ && LA3_1<=UMOD)||LA3_1==PERIOD) ) {
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
                    // elements: toplevelvalue, ID
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
                    // elements: toplevelvalue, ID, type
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
            // elements: listiterable, forIn
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
            // elements: idlist, FOR, list
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:1: code : CODE ( LPAREN argdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE59=null;
        Token LPAREN60=null;
        Token RPAREN63=null;
        Token LBRACE64=null;
        Token RBRACE66=null;
        EulangParser.argdefs_return argdefs61 = null;

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
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:6: ( CODE ( LPAREN argdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:8: CODE ( LPAREN argdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE
            {
            CODE59=(Token)match(input,CODE,FOLLOW_CODE_in_code932); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CODE.add(CODE59);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:13: ( LPAREN argdefs ( xreturns )? RPAREN )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==LPAREN) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:15: LPAREN argdefs ( xreturns )? RPAREN
                    {
                    LPAREN60=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_code936); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN60);

                    pushFollow(FOLLOW_argdefs_in_code938);
                    argdefs61=argdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_argdefs.add(argdefs61.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:30: ( xreturns )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==ARROW) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:30: xreturns
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
            // elements: CODE, xreturns, codestmtlist, argdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 146:78: -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:81: ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:88: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:96: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:106: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:116: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:1: macro : MACRO ( LPAREN argdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.macro_return macro() throws RecognitionException {
        EulangParser.macro_return retval = new EulangParser.macro_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MACRO67=null;
        Token LPAREN68=null;
        Token RPAREN71=null;
        Token LBRACE72=null;
        Token RBRACE74=null;
        EulangParser.argdefs_return argdefs69 = null;

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
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:7: ( MACRO ( LPAREN argdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:9: MACRO ( LPAREN argdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE
            {
            MACRO67=(Token)match(input,MACRO,FOLLOW_MACRO_in_macro988); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_MACRO.add(MACRO67);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:15: ( LPAREN argdefs ( xreturns )? RPAREN )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==LPAREN) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:17: LPAREN argdefs ( xreturns )? RPAREN
                    {
                    LPAREN68=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_macro992); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN68);

                    pushFollow(FOLLOW_argdefs_in_macro994);
                    argdefs69=argdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_argdefs.add(argdefs69.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:32: ( xreturns )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==ARROW) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:32: xreturns
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
            // elements: MACRO, xreturns, codestmtlist, argdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 150:80: -> ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:83: ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_MACRO.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:91: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:99: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:109: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:119: ( codestmtlist )*
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

    public static class argdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:1: argdefs options {backtrack=true; } : ( | argdefsWithTypes | ( argdefWithType )? | argdefsWithNames );
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.argdefsWithTypes_return argdefsWithTypes75 = null;

        EulangParser.argdefWithType_return argdefWithType76 = null;

        EulangParser.argdefsWithNames_return argdefsWithNames77 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:40: ( | argdefsWithTypes | ( argdefWithType )? | argdefsWithNames )
            int alt20=4;
            switch ( input.LA(1) ) {
            case ARROW:
                {
                int LA20_1 = input.LA(2);

                if ( (synpred5_Eulang()) ) {
                    alt20=1;
                }
                else if ( (synpred7_Eulang()) ) {
                    alt20=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;
                }
                }
                break;
            case RPAREN:
                {
                int LA20_2 = input.LA(2);

                if ( (synpred5_Eulang()) ) {
                    alt20=1;
                }
                else if ( (synpred7_Eulang()) ) {
                    alt20=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 2, input);

                    throw nvae;
                }
                }
                break;
            case ID:
                {
                int LA20_3 = input.LA(2);

                if ( (synpred6_Eulang()) ) {
                    alt20=2;
                }
                else if ( (synpred7_Eulang()) ) {
                    alt20=3;
                }
                else if ( (true) ) {
                    alt20=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 3, input);

                    throw nvae;
                }
                }
                break;
            case MACRO:
                {
                int LA20_4 = input.LA(2);

                if ( (synpred6_Eulang()) ) {
                    alt20=2;
                }
                else if ( (synpred7_Eulang()) ) {
                    alt20=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 4, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }

            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:3: 
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:5: argdefsWithTypes
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithTypes_in_argdefs1059);
                    argdefsWithTypes75=argdefsWithTypes();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithTypes75.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:5: ( argdefWithType )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:5: ( argdefWithType )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==MACRO||LA19_0==ID) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:5: argdefWithType
                            {
                            pushFollow(FOLLOW_argdefWithType_in_argdefs1066);
                            argdefWithType76=argdefWithType();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefWithType76.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:6: argdefsWithNames
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithNames_in_argdefs1075);
                    argdefsWithNames77=argdefsWithNames();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithNames77.getTree());

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
    // $ANTLR end "argdefs"

    public static class argdefsWithTypes_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefsWithTypes"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:1: argdefsWithTypes : ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* ;
    public final EulangParser.argdefsWithTypes_return argdefsWithTypes() throws RecognitionException {
        EulangParser.argdefsWithTypes_return retval = new EulangParser.argdefsWithTypes_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI79=null;
        Token SEMI81=null;
        EulangParser.argdefWithType_return argdefWithType78 = null;

        EulangParser.argdefWithType_return argdefWithType80 = null;


        CommonTree SEMI79_tree=null;
        CommonTree SEMI81_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_argdefWithType=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithType");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:17: ( ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:20: argdefWithType ( SEMI argdefWithType )+ ( SEMI )?
            {
            pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1091);
            argdefWithType78=argdefWithType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType78.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:35: ( SEMI argdefWithType )+
            int cnt21=0;
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==SEMI) ) {
                    int LA21_1 = input.LA(2);

                    if ( (LA21_1==MACRO||LA21_1==ID) ) {
                        alt21=1;
                    }


                }


                switch (alt21) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:37: SEMI argdefWithType
            	    {
            	    SEMI79=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1095); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI79);

            	    pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1097);
            	    argdefWithType80=argdefWithType();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType80.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt21 >= 1 ) break loop21;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(21, input);
                        throw eee;
                }
                cnt21++;
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:59: ( SEMI )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==SEMI) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:59: SEMI
                    {
                    SEMI81=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1101); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI81);


                    }
                    break;

            }


            }



            // AST REWRITE
            // elements: argdefWithType
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 159:73: -> ( argdefWithType )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:76: ( argdefWithType )*
                while ( stream_argdefWithType.hasNext() ) {
                    adaptor.addChild(root_0, stream_argdefWithType.nextTree());

                }
                stream_argdefWithType.reset();

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
    // $ANTLR end "argdefsWithTypes"

    public static class argdefWithType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefWithType"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:1: argdefWithType : ( ID ( COMMA ID )* ( COLON type )? -> ( ^( ARGDEF ID ( type )* ) )+ | MACRO ID ( COMMA ID )* ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF MACRO ID ( type )* ( $init)? ) )+ );
    public final EulangParser.argdefWithType_return argdefWithType() throws RecognitionException {
        EulangParser.argdefWithType_return retval = new EulangParser.argdefWithType_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID82=null;
        Token COMMA83=null;
        Token ID84=null;
        Token COLON85=null;
        Token MACRO87=null;
        Token ID88=null;
        Token COMMA89=null;
        Token ID90=null;
        Token COLON91=null;
        Token EQUALS93=null;
        EulangParser.rhsExpr_return init = null;

        EulangParser.type_return type86 = null;

        EulangParser.type_return type92 = null;


        CommonTree ID82_tree=null;
        CommonTree COMMA83_tree=null;
        CommonTree ID84_tree=null;
        CommonTree COLON85_tree=null;
        CommonTree MACRO87_tree=null;
        CommonTree ID88_tree=null;
        CommonTree COMMA89_tree=null;
        CommonTree ID90_tree=null;
        CommonTree COLON91_tree=null;
        CommonTree EQUALS93_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_MACRO=new RewriteRuleTokenStream(adaptor,"token MACRO");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:15: ( ID ( COMMA ID )* ( COLON type )? -> ( ^( ARGDEF ID ( type )* ) )+ | MACRO ID ( COMMA ID )* ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF MACRO ID ( type )* ( $init)? ) )+ )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==ID) ) {
                alt28=1;
            }
            else if ( (LA28_0==MACRO) ) {
                alt28=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;
            }
            switch (alt28) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:18: ID ( COMMA ID )* ( COLON type )?
                    {
                    ID82=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1130); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID82);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:21: ( COMMA ID )*
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( (LA23_0==COMMA) ) {
                            alt23=1;
                        }


                        switch (alt23) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:22: COMMA ID
                    	    {
                    	    COMMA83=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefWithType1133); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA83);

                    	    ID84=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1135); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID84);


                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:33: ( COLON type )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==COLON) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:34: COLON type
                            {
                            COLON85=(Token)match(input,COLON,FOLLOW_COLON_in_argdefWithType1140); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON85);

                            pushFollow(FOLLOW_type_in_argdefWithType1142);
                            type86=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type86.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 163:49: -> ( ^( ARGDEF ID ( type )* ) )+
                    {
                        if ( !(stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:52: ^( ARGDEF ID ( type )* )
                            {
                            CommonTree root_1 = (CommonTree)adaptor.nil();
                            root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                            adaptor.addChild(root_1, stream_ID.nextNode());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:64: ( type )*
                            while ( stream_type.hasNext() ) {
                                adaptor.addChild(root_1, stream_type.nextTree());

                            }
                            stream_type.reset();

                            adaptor.addChild(root_0, root_1);
                            }

                        }
                        stream_ID.reset();

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:7: MACRO ID ( COMMA ID )* ( COLON type )? ( EQUALS init= rhsExpr )?
                    {
                    MACRO87=(Token)match(input,MACRO,FOLLOW_MACRO_in_argdefWithType1167); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MACRO.add(MACRO87);

                    ID88=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1169); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID88);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:16: ( COMMA ID )*
                    loop25:
                    do {
                        int alt25=2;
                        int LA25_0 = input.LA(1);

                        if ( (LA25_0==COMMA) ) {
                            alt25=1;
                        }


                        switch (alt25) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:17: COMMA ID
                    	    {
                    	    COMMA89=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefWithType1172); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA89);

                    	    ID90=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1174); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID90);


                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:28: ( COLON type )?
                    int alt26=2;
                    int LA26_0 = input.LA(1);

                    if ( (LA26_0==COLON) ) {
                        alt26=1;
                    }
                    switch (alt26) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:29: COLON type
                            {
                            COLON91=(Token)match(input,COLON,FOLLOW_COLON_in_argdefWithType1179); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON91);

                            pushFollow(FOLLOW_type_in_argdefWithType1181);
                            type92=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type92.getTree());

                            }
                            break;

                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:42: ( EQUALS init= rhsExpr )?
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==EQUALS) ) {
                        alt27=1;
                    }
                    switch (alt27) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:43: EQUALS init= rhsExpr
                            {
                            EQUALS93=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdefWithType1186); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS93);

                            pushFollow(FOLLOW_rhsExpr_in_argdefWithType1190);
                            init=rhsExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExpr.add(init.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: init, type, MACRO, ID
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
                    // 164:68: -> ( ^( ARGDEF MACRO ID ( type )* ( $init)? ) )+
                    {
                        if ( !(stream_MACRO.hasNext()||stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_MACRO.hasNext()||stream_ID.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:71: ^( ARGDEF MACRO ID ( type )* ( $init)? )
                            {
                            CommonTree root_1 = (CommonTree)adaptor.nil();
                            root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                            adaptor.addChild(root_1, stream_MACRO.nextNode());
                            adaptor.addChild(root_1, stream_ID.nextNode());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:89: ( type )*
                            while ( stream_type.hasNext() ) {
                                adaptor.addChild(root_1, stream_type.nextTree());

                            }
                            stream_type.reset();
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:95: ( $init)?
                            if ( stream_init.hasNext() ) {
                                adaptor.addChild(root_1, stream_init.nextTree());

                            }
                            stream_init.reset();

                            adaptor.addChild(root_0, root_1);
                            }

                        }
                        stream_MACRO.reset();
                        stream_ID.reset();

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
    // $ANTLR end "argdefWithType"

    public static class argdefsWithNames_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefsWithNames"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:1: argdefsWithNames : ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* ;
    public final EulangParser.argdefsWithNames_return argdefsWithNames() throws RecognitionException {
        EulangParser.argdefsWithNames_return retval = new EulangParser.argdefsWithNames_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA95=null;
        Token COMMA97=null;
        EulangParser.argdefWithName_return argdefWithName94 = null;

        EulangParser.argdefWithName_return argdefWithName96 = null;


        CommonTree COMMA95_tree=null;
        CommonTree COMMA97_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdefWithName=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithName");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:18: ( ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:22: argdefWithName ( COMMA argdefWithName )+ ( COMMA )?
            {
            pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1226);
            argdefWithName94=argdefWithName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName94.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:37: ( COMMA argdefWithName )+
            int cnt29=0;
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==COMMA) ) {
                    int LA29_1 = input.LA(2);

                    if ( (LA29_1==ID) ) {
                        alt29=1;
                    }


                }


                switch (alt29) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:39: COMMA argdefWithName
            	    {
            	    COMMA95=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1230); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA95);

            	    pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1232);
            	    argdefWithName96=argdefWithName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName96.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt29 >= 1 ) break loop29;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(29, input);
                        throw eee;
                }
                cnt29++;
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:62: ( COMMA )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==COMMA) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:62: COMMA
                    {
                    COMMA97=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1236); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA97);


                    }
                    break;

            }


            }



            // AST REWRITE
            // elements: argdefWithName
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 167:73: -> ( argdefWithName )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:76: ( argdefWithName )*
                while ( stream_argdefWithName.hasNext() ) {
                    adaptor.addChild(root_0, stream_argdefWithName.nextTree());

                }
                stream_argdefWithName.reset();

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
    // $ANTLR end "argdefsWithNames"

    public static class argdefWithName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefWithName"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:1: argdefWithName : ID -> ^( ARGDEF ID ) ;
    public final EulangParser.argdefWithName_return argdefWithName() throws RecognitionException {
        EulangParser.argdefWithName_return retval = new EulangParser.argdefWithName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID98=null;

        CommonTree ID98_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:15: ( ID -> ^( ARGDEF ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:17: ID
            {
            ID98=(Token)match(input,ID,FOLLOW_ID_in_argdefWithName1258); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID98);



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
            // 169:22: -> ^( ARGDEF ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:25: ^( ARGDEF ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

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
    // $ANTLR end "argdefWithName"

    public static class proto_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "proto"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:1: proto : LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN99=null;
        Token RPAREN102=null;
        EulangParser.argdefs_return argdefs100 = null;

        EulangParser.xreturns_return xreturns101 = null;


        CommonTree LPAREN99_tree=null;
        CommonTree RPAREN102_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:7: ( LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:9: LPAREN argdefs ( xreturns )? RPAREN
            {
            LPAREN99=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto1281); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN99);

            pushFollow(FOLLOW_argdefs_in_proto1283);
            argdefs100=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs100.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:24: ( xreturns )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==ARROW) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:24: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto1285);
                    xreturns101=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns101.getTree());

                    }
                    break;

            }

            RPAREN102=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto1288); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN102);



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
            // 173:59: -> ^( PROTO ( xreturns )? ( argdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:62: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:70: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:80: ( argdefs )*
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

    public static class xreturns_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xreturns"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:1: xreturns : ( ARROW type -> type | ARROW argtuple -> argtuple | ARROW NIL -> ^( TYPE NIL ) );
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ARROW103=null;
        Token ARROW105=null;
        Token ARROW107=null;
        Token NIL108=null;
        EulangParser.type_return type104 = null;

        EulangParser.argtuple_return argtuple106 = null;


        CommonTree ARROW103_tree=null;
        CommonTree ARROW105_tree=null;
        CommonTree ARROW107_tree=null;
        CommonTree NIL108_tree=null;
        RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleSubtreeStream stream_argtuple=new RewriteRuleSubtreeStream(adaptor,"rule argtuple");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:9: ( ARROW type -> type | ARROW argtuple -> argtuple | ARROW NIL -> ^( TYPE NIL ) )
            int alt32=3;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==ARROW) ) {
                switch ( input.LA(2) ) {
                case NIL:
                    {
                    alt32=3;
                    }
                    break;
                case LPAREN:
                    {
                    alt32=2;
                    }
                    break;
                case CODE:
                case ID:
                case COLON:
                case COLONS:
                    {
                    alt32=1;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 32, 1, input);

                    throw nvae;
                }

            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:11: ARROW type
                    {
                    ARROW103=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns1330); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW103);

                    pushFollow(FOLLOW_type_in_xreturns1332);
                    type104=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type104.getTree());


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
                    // 176:27: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:177:5: ARROW argtuple
                    {
                    ARROW105=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns1347); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW105);

                    pushFollow(FOLLOW_argtuple_in_xreturns1349);
                    argtuple106=argtuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_argtuple.add(argtuple106.getTree());


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
                    // 177:30: -> argtuple
                    {
                        adaptor.addChild(root_0, stream_argtuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:178:5: ARROW NIL
                    {
                    ARROW107=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns1369); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW107);

                    NIL108=(Token)match(input,NIL,FOLLOW_NIL_in_xreturns1371); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL108);



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
                    // 178:26: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:178:29: ^( TYPE NIL )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:1: argtuple : LPAREN tupleargdefs RPAREN -> ^( TUPLE tupleargdefs ) ;
    public final EulangParser.argtuple_return argtuple() throws RecognitionException {
        EulangParser.argtuple_return retval = new EulangParser.argtuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN109=null;
        Token RPAREN111=null;
        EulangParser.tupleargdefs_return tupleargdefs110 = null;


        CommonTree LPAREN109_tree=null;
        CommonTree RPAREN111_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleargdefs=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:10: ( LPAREN tupleargdefs RPAREN -> ^( TUPLE tupleargdefs ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:12: LPAREN tupleargdefs RPAREN
            {
            LPAREN109=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_argtuple1401); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN109);

            pushFollow(FOLLOW_tupleargdefs_in_argtuple1403);
            tupleargdefs110=tupleargdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdefs.add(tupleargdefs110.getTree());
            RPAREN111=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_argtuple1405); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN111);



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
            // 181:42: -> ^( TUPLE tupleargdefs )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:45: ^( TUPLE tupleargdefs )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:1: tupleargdefs : ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* ;
    public final EulangParser.tupleargdefs_return tupleargdefs() throws RecognitionException {
        EulangParser.tupleargdefs_return retval = new EulangParser.tupleargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA113=null;
        EulangParser.tupleargdef_return tupleargdef112 = null;

        EulangParser.tupleargdef_return tupleargdef114 = null;


        CommonTree COMMA113_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_tupleargdef=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:13: ( ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:15: ( tupleargdef ( COMMA tupleargdef )+ )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:15: ( tupleargdef ( COMMA tupleargdef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:16: tupleargdef ( COMMA tupleargdef )+
            {
            pushFollow(FOLLOW_tupleargdef_in_tupleargdefs1427);
            tupleargdef112=tupleargdef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef112.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:28: ( COMMA tupleargdef )+
            int cnt33=0;
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==COMMA) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:30: COMMA tupleargdef
            	    {
            	    COMMA113=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleargdefs1431); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA113);

            	    pushFollow(FOLLOW_tupleargdef_in_tupleargdefs1433);
            	    tupleargdef114=tupleargdef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef114.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt33 >= 1 ) break loop33;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(33, input);
                        throw eee;
                }
                cnt33++;
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
            // 184:75: -> ( tupleargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:78: ( tupleargdef )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:1: tupleargdef : ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) );
    public final EulangParser.tupleargdef_return tupleargdef() throws RecognitionException {
        EulangParser.tupleargdef_return retval = new EulangParser.tupleargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION116=null;
        EulangParser.type_return type115 = null;


        CommonTree QUESTION116_tree=null;
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:12: ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) )
            int alt34=3;
            switch ( input.LA(1) ) {
            case CODE:
            case ID:
            case COLON:
            case COLONS:
                {
                alt34=1;
                }
                break;
            case QUESTION:
                {
                alt34=2;
                }
                break;
            case COMMA:
            case RPAREN:
                {
                alt34=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }

            switch (alt34) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:14: type
                    {
                    pushFollow(FOLLOW_type_in_tupleargdef1478);
                    type115=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type115.getTree());


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
                    // 187:22: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:5: QUESTION
                    {
                    QUESTION116=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_tupleargdef1491); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION116);



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
                    // 188:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:24: ^( TYPE NIL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:21: 
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
                    // 189:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:24: ^( TYPE NIL )
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

    public static class type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:1: type : ( ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )? | CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) );
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP118=null;
        Token CODE119=null;
        EulangParser.idOrScopeRef_return idOrScopeRef117 = null;

        EulangParser.proto_return proto120 = null;


        CommonTree AMP118_tree=null;
        CommonTree CODE119_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:6: ( ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )? | CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:9: ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )?
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:9: ( idOrScopeRef -> ^( TYPE idOrScopeRef ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:11: idOrScopeRef
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_type1550);
                    idOrScopeRef117=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef117.getTree());


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
                    // 192:24: -> ^( TYPE idOrScopeRef )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:27: ^( TYPE idOrScopeRef )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:51: ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==AMP) ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:53: AMP
                            {
                            AMP118=(Token)match(input,AMP,FOLLOW_AMP_in_type1565); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_AMP.add(AMP118);



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
                            // 192:57: -> ^( TYPE ^( REF idOrScopeRef ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:60: ^( TYPE ^( REF idOrScopeRef ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:67: ^( REF idOrScopeRef )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:193:8: CODE ( proto )?
                    {
                    CODE119=(Token)match(input,CODE,FOLLOW_CODE_in_type1591); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CODE.add(CODE119);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:193:13: ( proto )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==LPAREN) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:193:13: proto
                            {
                            pushFollow(FOLLOW_proto_in_type1593);
                            proto120=proto();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_proto.add(proto120.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: CODE, proto
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 193:20: -> ^( TYPE ^( CODE ( proto )? ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:193:23: ^( TYPE ^( CODE ( proto )? ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:193:30: ^( CODE ( proto )? )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_2);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:193:37: ( proto )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:1: codestmtlist : ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) );
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI122=null;
        EulangParser.codeStmt_return codeStmt121 = null;

        EulangParser.codeStmt_return codeStmt123 = null;


        CommonTree SEMI122_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:13: ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:35: codeStmt ( SEMI ( codeStmt )? )*
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist1621);
                    codeStmt121=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt121.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:44: ( SEMI ( codeStmt )? )*
                    loop39:
                    do {
                        int alt39=2;
                        int LA39_0 = input.LA(1);

                        if ( (LA39_0==SEMI) ) {
                            alt39=1;
                        }


                        switch (alt39) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:45: SEMI ( codeStmt )?
                    	    {
                    	    SEMI122=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist1624); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI122);

                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:50: ( codeStmt )?
                    	    int alt38=2;
                    	    int LA38_0 = input.LA(1);

                    	    if ( ((LA38_0>=CODE && LA38_0<=MACRO)||LA38_0==GOTO||LA38_0==ID||LA38_0==COLON||LA38_0==LBRACE||LA38_0==FOR||LA38_0==LPAREN||LA38_0==NIL||(LA38_0>=DO && LA38_0<=REPEAT)||(LA38_0>=ATSIGN && LA38_0<=IF)||LA38_0==NOT||(LA38_0>=MINUS && LA38_0<=STAR)||(LA38_0>=TILDE && LA38_0<=STRING_LITERAL)||LA38_0==COLONS) ) {
                    	        alt38=1;
                    	    }
                    	    switch (alt38) {
                    	        case 1 :
                    	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:50: codeStmt
                    	            {
                    	            pushFollow(FOLLOW_codeStmt_in_codestmtlist1626);
                    	            codeStmt123=codeStmt();

                    	            state._fsp--;
                    	            if (state.failed) return retval;
                    	            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt123.getTree());

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
                    // 196:63: -> ^( STMTLIST ( codeStmt )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:67: ^( STMTLIST ( codeStmt )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:78: ( codeStmt )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:7: 
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
                    // 198:7: -> ^( STMTLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:10: ^( STMTLIST )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:1: codeStmt : ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.labelStmt_return labelStmt124 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr125 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr126 = null;


        RewriteRuleSubtreeStream stream_labelStmt=new RewriteRuleSubtreeStream(adaptor,"rule labelStmt");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:10: ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:12: labelStmt codeStmtExpr
                    {
                    pushFollow(FOLLOW_labelStmt_in_codeStmt1675);
                    labelStmt124=labelStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labelStmt.add(labelStmt124.getTree());
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt1677);
                    codeStmtExpr125=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr125.getTree());


                    // AST REWRITE
                    // elements: labelStmt, codeStmtExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 201:36: -> ^( LABELSTMT labelStmt codeStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:39: ^( LABELSTMT labelStmt codeStmtExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:202:9: codeStmtExpr
                    {
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt1698);
                    codeStmtExpr126=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr126.getTree());


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
                    // 202:22: -> codeStmtExpr
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:1: codeStmtExpr : ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );
    public final EulangParser.codeStmtExpr_return codeStmtExpr() throws RecognitionException {
        EulangParser.codeStmtExpr_return retval = new EulangParser.codeStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl127 = null;

        EulangParser.assignStmt_return assignStmt128 = null;

        EulangParser.rhsExpr_return rhsExpr129 = null;

        EulangParser.blockStmt_return blockStmt130 = null;

        EulangParser.gotoStmt_return gotoStmt131 = null;

        EulangParser.controlStmt_return controlStmt132 = null;


        RewriteRuleSubtreeStream stream_assignStmt=new RewriteRuleSubtreeStream(adaptor,"rule assignStmt");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_blockStmt=new RewriteRuleSubtreeStream(adaptor,"rule blockStmt");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_controlStmt=new RewriteRuleSubtreeStream(adaptor,"rule controlStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:14: ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt )
            int alt42=6;
            alt42 = dfa42.predict(input);
            switch (alt42) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:16: varDecl
                    {
                    pushFollow(FOLLOW_varDecl_in_codeStmtExpr1717);
                    varDecl127=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl127.getTree());


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
                    // 205:27: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:206:9: assignStmt
                    {
                    pushFollow(FOLLOW_assignStmt_in_codeStmtExpr1734);
                    assignStmt128=assignStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignStmt.add(assignStmt128.getTree());


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
                    // 206:23: -> assignStmt
                    {
                        adaptor.addChild(root_0, stream_assignStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:9: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_codeStmtExpr1758);
                    rhsExpr129=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr129.getTree());


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
                    // 208:23: -> ^( STMTEXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:27: ^( STMTEXPR rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:209:9: blockStmt
                    {
                    pushFollow(FOLLOW_blockStmt_in_codeStmtExpr1783);
                    blockStmt130=blockStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_blockStmt.add(blockStmt130.getTree());


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
                    // 209:27: -> blockStmt
                    {
                        adaptor.addChild(root_0, stream_blockStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:9: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_codeStmtExpr1805);
                    gotoStmt131=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt131.getTree());


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
                    // 210:23: -> gotoStmt
                    {
                        adaptor.addChild(root_0, stream_gotoStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:9: controlStmt
                    {
                    pushFollow(FOLLOW_controlStmt_in_codeStmtExpr1831);
                    controlStmt132=controlStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_controlStmt.add(controlStmt132.getTree());


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
                    // 212:26: -> controlStmt
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:215:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) | ID ( COMMA ID )+ COLON_EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( assignExpr )+ ) ) | ID ( COMMA ID )+ COLON type ( EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* )? -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( assignExpr )+ ) )? ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID133=null;
        Token COLON_EQUALS134=null;
        Token COLON_EQUALS137=null;
        Token ID139=null;
        Token COLON140=null;
        Token EQUALS142=null;
        Token COLON145=null;
        Token EQUALS147=null;
        Token ID149=null;
        Token COMMA150=null;
        Token ID151=null;
        Token COLON_EQUALS152=null;
        Token PLUS153=null;
        Token COMMA155=null;
        Token ID157=null;
        Token COMMA158=null;
        Token ID159=null;
        Token COLON160=null;
        Token EQUALS162=null;
        Token PLUS163=null;
        Token COMMA165=null;
        EulangParser.assignExpr_return assignExpr135 = null;

        EulangParser.idTuple_return idTuple136 = null;

        EulangParser.assignExpr_return assignExpr138 = null;

        EulangParser.type_return type141 = null;

        EulangParser.assignExpr_return assignExpr143 = null;

        EulangParser.idTuple_return idTuple144 = null;

        EulangParser.type_return type146 = null;

        EulangParser.assignExpr_return assignExpr148 = null;

        EulangParser.assignExpr_return assignExpr154 = null;

        EulangParser.assignExpr_return assignExpr156 = null;

        EulangParser.type_return type161 = null;

        EulangParser.assignExpr_return assignExpr164 = null;

        EulangParser.assignExpr_return assignExpr166 = null;


        CommonTree ID133_tree=null;
        CommonTree COLON_EQUALS134_tree=null;
        CommonTree COLON_EQUALS137_tree=null;
        CommonTree ID139_tree=null;
        CommonTree COLON140_tree=null;
        CommonTree EQUALS142_tree=null;
        CommonTree COLON145_tree=null;
        CommonTree EQUALS147_tree=null;
        CommonTree ID149_tree=null;
        CommonTree COMMA150_tree=null;
        CommonTree ID151_tree=null;
        CommonTree COLON_EQUALS152_tree=null;
        CommonTree PLUS153_tree=null;
        CommonTree COMMA155_tree=null;
        CommonTree ID157_tree=null;
        CommonTree COMMA158_tree=null;
        CommonTree ID159_tree=null;
        CommonTree COLON160_tree=null;
        CommonTree EQUALS162_tree=null;
        CommonTree PLUS163_tree=null;
        CommonTree COMMA165_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:215:8: ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) | ID ( COMMA ID )+ COLON_EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( assignExpr )+ ) ) | ID ( COMMA ID )+ COLON type ( EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* )? -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( assignExpr )+ ) )? ) )
            int alt52=6;
            alt52 = dfa52.predict(input);
            switch (alt52) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:215:10: ID COLON_EQUALS assignExpr
                    {
                    ID133=(Token)match(input,ID,FOLLOW_ID_in_varDecl1854); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID133);

                    COLON_EQUALS134=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1856); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS134);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1858);
                    assignExpr135=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr135.getTree());


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
                    // 215:45: -> ^( ALLOC ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:215:48: ^( ALLOC ID TYPE assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:7: idTuple COLON_EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_varDecl1886);
                    idTuple136=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple136.getTree());
                    COLON_EQUALS137=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1888); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS137);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1890);
                    assignExpr138=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr138.getTree());


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
                    // 216:47: -> ^( ALLOC idTuple TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:50: ^( ALLOC idTuple TYPE assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID139=(Token)match(input,ID,FOLLOW_ID_in_varDecl1918); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID139);

                    COLON140=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1920); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON140);

                    pushFollow(FOLLOW_type_in_varDecl1922);
                    type141=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type141.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:21: ( EQUALS assignExpr )?
                    int alt43=2;
                    int LA43_0 = input.LA(1);

                    if ( (LA43_0==EQUALS) ) {
                        alt43=1;
                    }
                    switch (alt43) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:22: EQUALS assignExpr
                            {
                            EQUALS142=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1925); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS142);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1927);
                            assignExpr143=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr143.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, ID, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 217:43: -> ^( ALLOC ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:46: ^( ALLOC ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:62: ( assignExpr )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:7: idTuple COLON type ( EQUALS assignExpr )?
                    {
                    pushFollow(FOLLOW_idTuple_in_varDecl1951);
                    idTuple144=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple144.getTree());
                    COLON145=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1953); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON145);

                    pushFollow(FOLLOW_type_in_varDecl1955);
                    type146=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type146.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:26: ( EQUALS assignExpr )?
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==EQUALS) ) {
                        alt44=1;
                    }
                    switch (alt44) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:27: EQUALS assignExpr
                            {
                            EQUALS147=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1958); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS147);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1960);
                            assignExpr148=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr148.getTree());

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
                    // 218:48: -> ^( ALLOC idTuple type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:51: ^( ALLOC idTuple type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:72: ( assignExpr )*
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
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:7: ID ( COMMA ID )+ COLON_EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )*
                    {
                    ID149=(Token)match(input,ID,FOLLOW_ID_in_varDecl1984); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID149);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:10: ( COMMA ID )+
                    int cnt45=0;
                    loop45:
                    do {
                        int alt45=2;
                        int LA45_0 = input.LA(1);

                        if ( (LA45_0==COMMA) ) {
                            alt45=1;
                        }


                        switch (alt45) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:11: COMMA ID
                    	    {
                    	    COMMA150=(Token)match(input,COMMA,FOLLOW_COMMA_in_varDecl1987); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA150);

                    	    ID151=(Token)match(input,ID,FOLLOW_ID_in_varDecl1989); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID151);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt45 >= 1 ) break loop45;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(45, input);
                                throw eee;
                        }
                        cnt45++;
                    } while (true);

                    COLON_EQUALS152=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1993); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS152);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:35: ( PLUS )?
                    int alt46=2;
                    int LA46_0 = input.LA(1);

                    if ( (LA46_0==PLUS) ) {
                        alt46=1;
                    }
                    switch (alt46) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:35: PLUS
                            {
                            PLUS153=(Token)match(input,PLUS,FOLLOW_PLUS_in_varDecl1995); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS153);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_assignExpr_in_varDecl1998);
                    assignExpr154=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr154.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:52: ( COMMA assignExpr )*
                    loop47:
                    do {
                        int alt47=2;
                        int LA47_0 = input.LA(1);

                        if ( (LA47_0==COMMA) ) {
                            alt47=1;
                        }


                        switch (alt47) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:53: COMMA assignExpr
                    	    {
                    	    COMMA155=(Token)match(input,COMMA,FOLLOW_COMMA_in_varDecl2001); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA155);

                    	    pushFollow(FOLLOW_assignExpr_in_varDecl2003);
                    	    assignExpr156=assignExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr156.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop47;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: PLUS, ID, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 220:9: -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( assignExpr )+ ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:12: ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( assignExpr )+ ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:20: ^( LIST ( ID )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext() ) {
                            adaptor.addChild(root_2, stream_ID.nextNode());

                        }
                        stream_ID.reset();

                        adaptor.addChild(root_1, root_2);
                        }
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:37: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:43: ^( LIST ( assignExpr )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_assignExpr.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_assignExpr.hasNext() ) {
                            adaptor.addChild(root_2, stream_assignExpr.nextTree());

                        }
                        stream_assignExpr.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:7: ID ( COMMA ID )+ COLON type ( EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* )?
                    {
                    ID157=(Token)match(input,ID,FOLLOW_ID_in_varDecl2047); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID157);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:10: ( COMMA ID )+
                    int cnt48=0;
                    loop48:
                    do {
                        int alt48=2;
                        int LA48_0 = input.LA(1);

                        if ( (LA48_0==COMMA) ) {
                            alt48=1;
                        }


                        switch (alt48) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:11: COMMA ID
                    	    {
                    	    COMMA158=(Token)match(input,COMMA,FOLLOW_COMMA_in_varDecl2050); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA158);

                    	    ID159=(Token)match(input,ID,FOLLOW_ID_in_varDecl2052); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID159);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt48 >= 1 ) break loop48;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(48, input);
                                throw eee;
                        }
                        cnt48++;
                    } while (true);

                    COLON160=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl2056); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON160);

                    pushFollow(FOLLOW_type_in_varDecl2058);
                    type161=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type161.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:33: ( EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* )?
                    int alt51=2;
                    int LA51_0 = input.LA(1);

                    if ( (LA51_0==EQUALS) ) {
                        alt51=1;
                    }
                    switch (alt51) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:34: EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )*
                            {
                            EQUALS162=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl2061); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS162);

                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:41: ( PLUS )?
                            int alt49=2;
                            int LA49_0 = input.LA(1);

                            if ( (LA49_0==PLUS) ) {
                                alt49=1;
                            }
                            switch (alt49) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:41: PLUS
                                    {
                                    PLUS163=(Token)match(input,PLUS,FOLLOW_PLUS_in_varDecl2063); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS163);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_assignExpr_in_varDecl2066);
                            assignExpr164=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr164.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:58: ( COMMA assignExpr )*
                            loop50:
                            do {
                                int alt50=2;
                                int LA50_0 = input.LA(1);

                                if ( (LA50_0==COMMA) ) {
                                    alt50=1;
                                }


                                switch (alt50) {
                            	case 1 :
                            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:221:59: COMMA assignExpr
                            	    {
                            	    COMMA165=(Token)match(input,COMMA,FOLLOW_COMMA_in_varDecl2069); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA165);

                            	    pushFollow(FOLLOW_assignExpr_in_varDecl2071);
                            	    assignExpr166=assignExpr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr166.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop50;
                                }
                            } while (true);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, ID, PLUS, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 222:9: -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( assignExpr )+ ) )? )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:12: ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( assignExpr )+ ) )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:20: ^( LIST ( ID )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext() ) {
                            adaptor.addChild(root_2, stream_ID.nextNode());

                        }
                        stream_ID.reset();

                        adaptor.addChild(root_1, root_2);
                        }
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:37: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:43: ( ^( LIST ( assignExpr )+ ) )?
                        if ( stream_assignExpr.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:43: ^( LIST ( assignExpr )+ )
                            {
                            CommonTree root_2 = (CommonTree)adaptor.nil();
                            root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                            if ( !(stream_assignExpr.hasNext()) ) {
                                throw new RewriteEarlyExitException();
                            }
                            while ( stream_assignExpr.hasNext() ) {
                                adaptor.addChild(root_2, stream_assignExpr.nextTree());

                            }
                            stream_assignExpr.reset();

                            adaptor.addChild(root_1, root_2);
                            }

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:1: assignStmt : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | idOrScopeRef ( COMMA idOrScopeRef )+ EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* -> ^( ASSIGN ^( LIST ( idOrScopeRef )+ ) ( PLUS )? ^( LIST ( assignExpr )+ ) ) );
    public final EulangParser.assignStmt_return assignStmt() throws RecognitionException {
        EulangParser.assignStmt_return retval = new EulangParser.assignStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS168=null;
        Token EQUALS171=null;
        Token COMMA174=null;
        Token EQUALS176=null;
        Token PLUS177=null;
        Token COMMA179=null;
        EulangParser.idOrScopeRef_return idOrScopeRef167 = null;

        EulangParser.assignExpr_return assignExpr169 = null;

        EulangParser.idTuple_return idTuple170 = null;

        EulangParser.assignExpr_return assignExpr172 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef173 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef175 = null;

        EulangParser.assignExpr_return assignExpr178 = null;

        EulangParser.assignExpr_return assignExpr180 = null;


        CommonTree EQUALS168_tree=null;
        CommonTree EQUALS171_tree=null;
        CommonTree COMMA174_tree=null;
        CommonTree EQUALS176_tree=null;
        CommonTree PLUS177_tree=null;
        CommonTree COMMA179_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | idOrScopeRef ( COMMA idOrScopeRef )+ EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* -> ^( ASSIGN ^( LIST ( idOrScopeRef )+ ) ( PLUS )? ^( LIST ( assignExpr )+ ) ) )
            int alt56=3;
            alt56 = dfa56.predict(input);
            switch (alt56) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignStmt2133);
                    idOrScopeRef167=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef167.getTree());
                    EQUALS168=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt2135); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS168);

                    pushFollow(FOLLOW_assignExpr_in_assignStmt2137);
                    assignExpr169=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr169.getTree());


                    // AST REWRITE
                    // elements: idOrScopeRef, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 228:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:55: ^( ASSIGN idOrScopeRef assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:229:7: idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignStmt2162);
                    idTuple170=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple170.getTree());
                    EQUALS171=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt2164); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS171);

                    pushFollow(FOLLOW_assignExpr_in_assignStmt2166);
                    assignExpr172=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr172.getTree());


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
                    // 229:47: -> ^( ASSIGN idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:229:50: ^( ASSIGN idTuple assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:7: idOrScopeRef ( COMMA idOrScopeRef )+ EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )*
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignStmt2198);
                    idOrScopeRef173=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef173.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:20: ( COMMA idOrScopeRef )+
                    int cnt53=0;
                    loop53:
                    do {
                        int alt53=2;
                        int LA53_0 = input.LA(1);

                        if ( (LA53_0==COMMA) ) {
                            alt53=1;
                        }


                        switch (alt53) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:21: COMMA idOrScopeRef
                    	    {
                    	    COMMA174=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt2201); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA174);

                    	    pushFollow(FOLLOW_idOrScopeRef_in_assignStmt2203);
                    	    idOrScopeRef175=idOrScopeRef();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef175.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt53 >= 1 ) break loop53;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(53, input);
                                throw eee;
                        }
                        cnt53++;
                    } while (true);

                    EQUALS176=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt2207); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS176);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:49: ( PLUS )?
                    int alt54=2;
                    int LA54_0 = input.LA(1);

                    if ( (LA54_0==PLUS) ) {
                        alt54=1;
                    }
                    switch (alt54) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:49: PLUS
                            {
                            PLUS177=(Token)match(input,PLUS,FOLLOW_PLUS_in_assignStmt2209); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS177);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_assignExpr_in_assignStmt2212);
                    assignExpr178=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr178.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:66: ( COMMA assignExpr )*
                    loop55:
                    do {
                        int alt55=2;
                        int LA55_0 = input.LA(1);

                        if ( (LA55_0==COMMA) ) {
                            alt55=1;
                        }


                        switch (alt55) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:67: COMMA assignExpr
                    	    {
                    	    COMMA179=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt2215); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA179);

                    	    pushFollow(FOLLOW_assignExpr_in_assignStmt2217);
                    	    assignExpr180=assignExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr180.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop55;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: assignExpr, PLUS, idOrScopeRef
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 231:9: -> ^( ASSIGN ^( LIST ( idOrScopeRef )+ ) ( PLUS )? ^( LIST ( assignExpr )+ ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:12: ^( ASSIGN ^( LIST ( idOrScopeRef )+ ) ( PLUS )? ^( LIST ( assignExpr )+ ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:21: ^( LIST ( idOrScopeRef )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_idOrScopeRef.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_idOrScopeRef.hasNext() ) {
                            adaptor.addChild(root_2, stream_idOrScopeRef.nextTree());

                        }
                        stream_idOrScopeRef.reset();

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:43: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:49: ^( LIST ( assignExpr )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_assignExpr.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_assignExpr.hasNext() ) {
                            adaptor.addChild(root_2, stream_assignExpr.nextTree());

                        }
                        stream_assignExpr.reset();

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
    // $ANTLR end "assignStmt"

    public static class assignExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:234:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS182=null;
        Token EQUALS185=null;
        EulangParser.idOrScopeRef_return idOrScopeRef181 = null;

        EulangParser.assignExpr_return assignExpr183 = null;

        EulangParser.idTuple_return idTuple184 = null;

        EulangParser.assignExpr_return assignExpr186 = null;

        EulangParser.rhsExpr_return rhsExpr187 = null;


        CommonTree EQUALS182_tree=null;
        CommonTree EQUALS185_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:234:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr )
            int alt57=3;
            alt57 = dfa57.predict(input);
            switch (alt57) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:234:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignExpr2276);
                    idOrScopeRef181=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef181.getTree());
                    EQUALS182=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr2278); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS182);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr2280);
                    assignExpr183=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr183.getTree());


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
                    // 234:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:234:55: ^( ASSIGN idOrScopeRef assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:7: idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignExpr2305);
                    idTuple184=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple184.getTree());
                    EQUALS185=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr2307); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS185);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr2309);
                    assignExpr186=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr186.getTree());


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
                    // 235:47: -> ^( ASSIGN idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:50: ^( ASSIGN idTuple assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr2341);
                    rhsExpr187=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr187.getTree());


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
                    // 236:43: -> rhsExpr
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:1: controlStmt : ( doWhile | whileDo | repeat | forIter );
    public final EulangParser.controlStmt_return controlStmt() throws RecognitionException {
        EulangParser.controlStmt_return retval = new EulangParser.controlStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.doWhile_return doWhile188 = null;

        EulangParser.whileDo_return whileDo189 = null;

        EulangParser.repeat_return repeat190 = null;

        EulangParser.forIter_return forIter191 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:13: ( doWhile | whileDo | repeat | forIter )
            int alt58=4;
            switch ( input.LA(1) ) {
            case DO:
                {
                alt58=1;
                }
                break;
            case WHILE:
                {
                alt58=2;
                }
                break;
            case REPEAT:
                {
                alt58=3;
                }
                break;
            case FOR:
                {
                alt58=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;
            }

            switch (alt58) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:15: doWhile
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_doWhile_in_controlStmt2387);
                    doWhile188=doWhile();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, doWhile188.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:25: whileDo
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_whileDo_in_controlStmt2391);
                    whileDo189=whileDo();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, whileDo189.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:35: repeat
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_repeat_in_controlStmt2395);
                    repeat190=repeat();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, repeat190.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:44: forIter
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_forIter_in_controlStmt2399);
                    forIter191=forIter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, forIter191.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:1: doWhile : DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) ;
    public final EulangParser.doWhile_return doWhile() throws RecognitionException {
        EulangParser.doWhile_return retval = new EulangParser.doWhile_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DO192=null;
        Token WHILE194=null;
        EulangParser.codeStmtExpr_return codeStmtExpr193 = null;

        EulangParser.rhsExpr_return rhsExpr195 = null;


        CommonTree DO192_tree=null;
        CommonTree WHILE194_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:9: ( DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:11: DO codeStmtExpr WHILE rhsExpr
            {
            DO192=(Token)match(input,DO,FOLLOW_DO_in_doWhile2408); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO192);

            pushFollow(FOLLOW_codeStmtExpr_in_doWhile2410);
            codeStmtExpr193=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr193.getTree());
            WHILE194=(Token)match(input,WHILE,FOLLOW_WHILE_in_doWhile2412); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE194);

            pushFollow(FOLLOW_rhsExpr_in_doWhile2414);
            rhsExpr195=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr195.getTree());


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
            // 242:43: -> ^( DO codeStmtExpr rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:46: ^( DO codeStmtExpr rhsExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:1: whileDo : WHILE rhsExpr DO codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) ;
    public final EulangParser.whileDo_return whileDo() throws RecognitionException {
        EulangParser.whileDo_return retval = new EulangParser.whileDo_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHILE196=null;
        Token DO198=null;
        EulangParser.rhsExpr_return rhsExpr197 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr199 = null;


        CommonTree WHILE196_tree=null;
        CommonTree DO198_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:9: ( WHILE rhsExpr DO codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:11: WHILE rhsExpr DO codeStmtExpr
            {
            WHILE196=(Token)match(input,WHILE,FOLLOW_WHILE_in_whileDo2437); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE196);

            pushFollow(FOLLOW_rhsExpr_in_whileDo2439);
            rhsExpr197=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr197.getTree());
            DO198=(Token)match(input,DO,FOLLOW_DO_in_whileDo2441); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO198);

            pushFollow(FOLLOW_codeStmtExpr_in_whileDo2443);
            codeStmtExpr199=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr199.getTree());


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
            // 245:43: -> ^( WHILE rhsExpr codeStmtExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:46: ^( WHILE rhsExpr codeStmtExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:1: repeat : REPEAT rhsExpr DO codeOrValueExpr -> ^( REPEAT rhsExpr codeOrValueExpr ) ;
    public final EulangParser.repeat_return repeat() throws RecognitionException {
        EulangParser.repeat_return retval = new EulangParser.repeat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token REPEAT200=null;
        Token DO202=null;
        EulangParser.rhsExpr_return rhsExpr201 = null;

        EulangParser.codeOrValueExpr_return codeOrValueExpr203 = null;


        CommonTree REPEAT200_tree=null;
        CommonTree DO202_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_REPEAT=new RewriteRuleTokenStream(adaptor,"token REPEAT");
        RewriteRuleSubtreeStream stream_codeOrValueExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeOrValueExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:8: ( REPEAT rhsExpr DO codeOrValueExpr -> ^( REPEAT rhsExpr codeOrValueExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:10: REPEAT rhsExpr DO codeOrValueExpr
            {
            REPEAT200=(Token)match(input,REPEAT,FOLLOW_REPEAT_in_repeat2468); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_REPEAT.add(REPEAT200);

            pushFollow(FOLLOW_rhsExpr_in_repeat2470);
            rhsExpr201=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr201.getTree());
            DO202=(Token)match(input,DO,FOLLOW_DO_in_repeat2472); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO202);

            pushFollow(FOLLOW_codeOrValueExpr_in_repeat2474);
            codeOrValueExpr203=codeOrValueExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeOrValueExpr.add(codeOrValueExpr203.getTree());


            // AST REWRITE
            // elements: REPEAT, codeOrValueExpr, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 248:52: -> ^( REPEAT rhsExpr codeOrValueExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:55: ^( REPEAT rhsExpr codeOrValueExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:1: forIter : FOR forIds ( atId )? IN rhsExpr DO codeOrValueExpr -> ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr ) ;
    public final EulangParser.forIter_return forIter() throws RecognitionException {
        EulangParser.forIter_return retval = new EulangParser.forIter_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR204=null;
        Token IN207=null;
        Token DO209=null;
        EulangParser.forIds_return forIds205 = null;

        EulangParser.atId_return atId206 = null;

        EulangParser.rhsExpr_return rhsExpr208 = null;

        EulangParser.codeOrValueExpr_return codeOrValueExpr210 = null;


        CommonTree FOR204_tree=null;
        CommonTree IN207_tree=null;
        CommonTree DO209_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_codeOrValueExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeOrValueExpr");
        RewriteRuleSubtreeStream stream_atId=new RewriteRuleSubtreeStream(adaptor,"rule atId");
        RewriteRuleSubtreeStream stream_forIds=new RewriteRuleSubtreeStream(adaptor,"rule forIds");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:9: ( FOR forIds ( atId )? IN rhsExpr DO codeOrValueExpr -> ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:11: FOR forIds ( atId )? IN rhsExpr DO codeOrValueExpr
            {
            FOR204=(Token)match(input,FOR,FOLLOW_FOR_in_forIter2504); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR204);

            pushFollow(FOLLOW_forIds_in_forIter2506);
            forIds205=forIds();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_forIds.add(forIds205.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:22: ( atId )?
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0==AT) ) {
                alt59=1;
            }
            switch (alt59) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:22: atId
                    {
                    pushFollow(FOLLOW_atId_in_forIter2508);
                    atId206=atId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atId.add(atId206.getTree());

                    }
                    break;

            }

            IN207=(Token)match(input,IN,FOLLOW_IN_in_forIter2511); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN207);

            pushFollow(FOLLOW_rhsExpr_in_forIter2513);
            rhsExpr208=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr208.getTree());
            DO209=(Token)match(input,DO,FOLLOW_DO_in_forIter2515); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO209);

            pushFollow(FOLLOW_codeOrValueExpr_in_forIter2517);
            codeOrValueExpr210=codeOrValueExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeOrValueExpr.add(codeOrValueExpr210.getTree());


            // AST REWRITE
            // elements: FOR, codeOrValueExpr, forIds, rhsExpr, atId
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 251:64: -> ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:67: ^( FOR forIds ( atId )? rhsExpr codeOrValueExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                adaptor.addChild(root_1, stream_forIds.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:80: ( atId )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:1: forIds : ID ( AND ID )* -> ( ID )+ ;
    public final EulangParser.forIds_return forIds() throws RecognitionException {
        EulangParser.forIds_return retval = new EulangParser.forIds_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID211=null;
        Token AND212=null;
        Token ID213=null;

        CommonTree ID211_tree=null;
        CommonTree AND212_tree=null;
        CommonTree ID213_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:8: ( ID ( AND ID )* -> ( ID )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:10: ID ( AND ID )*
            {
            ID211=(Token)match(input,ID,FOLLOW_ID_in_forIds2550); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID211);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:13: ( AND ID )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( (LA60_0==AND) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:14: AND ID
            	    {
            	    AND212=(Token)match(input,AND,FOLLOW_AND_in_forIds2553); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND212);

            	    ID213=(Token)match(input,ID,FOLLOW_ID_in_forIds2555); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID213);


            	    }
            	    break;

            	default :
            	    break loop60;
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
            // 254:23: -> ( ID )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:1: atId : AT ID -> ^( AT ID ) ;
    public final EulangParser.atId_return atId() throws RecognitionException {
        EulangParser.atId_return retval = new EulangParser.atId_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT214=null;
        Token ID215=null;

        CommonTree AT214_tree=null;
        CommonTree ID215_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:6: ( AT ID -> ^( AT ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:8: AT ID
            {
            AT214=(Token)match(input,AT,FOLLOW_AT_in_atId2571); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT214);

            ID215=(Token)match(input,ID,FOLLOW_ID_in_atId2573); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID215);



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
            // 256:17: -> ^( AT ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:20: ^( AT ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:1: codeOrValueExpr : ( codeStmt | breakStmt );
    public final EulangParser.codeOrValueExpr_return codeOrValueExpr() throws RecognitionException {
        EulangParser.codeOrValueExpr_return retval = new EulangParser.codeOrValueExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.codeStmt_return codeStmt216 = null;

        EulangParser.breakStmt_return breakStmt217 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:17: ( codeStmt | breakStmt )
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( ((LA61_0>=CODE && LA61_0<=MACRO)||LA61_0==GOTO||LA61_0==ID||LA61_0==COLON||LA61_0==LBRACE||LA61_0==FOR||LA61_0==LPAREN||LA61_0==NIL||(LA61_0>=DO && LA61_0<=REPEAT)||(LA61_0>=ATSIGN && LA61_0<=IF)||LA61_0==NOT||(LA61_0>=MINUS && LA61_0<=STAR)||(LA61_0>=TILDE && LA61_0<=STRING_LITERAL)||LA61_0==COLONS) ) {
                alt61=1;
            }
            else if ( (LA61_0==BREAK) ) {
                alt61=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                throw nvae;
            }
            switch (alt61) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:19: codeStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_codeStmt_in_codeOrValueExpr2598);
                    codeStmt216=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, codeStmt216.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:30: breakStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_breakStmt_in_codeOrValueExpr2602);
                    breakStmt217=breakStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, breakStmt217.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:1: breakStmt : BREAK valueCond -> ^( BREAK valueCond ) ;
    public final EulangParser.breakStmt_return breakStmt() throws RecognitionException {
        EulangParser.breakStmt_return retval = new EulangParser.breakStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BREAK218=null;
        EulangParser.valueCond_return valueCond219 = null;


        CommonTree BREAK218_tree=null;
        RewriteRuleTokenStream stream_BREAK=new RewriteRuleTokenStream(adaptor,"token BREAK");
        RewriteRuleSubtreeStream stream_valueCond=new RewriteRuleSubtreeStream(adaptor,"rule valueCond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:11: ( BREAK valueCond -> ^( BREAK valueCond ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:13: BREAK valueCond
            {
            BREAK218=(Token)match(input,BREAK,FOLLOW_BREAK_in_breakStmt2611); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BREAK.add(BREAK218);

            pushFollow(FOLLOW_valueCond_in_breakStmt2613);
            valueCond219=valueCond();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_valueCond.add(valueCond219.getTree());


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
            // 261:29: -> ^( BREAK valueCond )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:33: ^( BREAK valueCond )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:264:1: valueCond : ( rhsExpr WHEN rhsExpr ELSE rhsExpr -> ^( WHEN ( rhsExpr )+ ) | UNTIL rhsExpr THEN rhsExpr ELSE rhsExpr -> ^( UNTIL ( rhsExpr )+ ) | WITH rhsExpr -> rhsExpr );
    public final EulangParser.valueCond_return valueCond() throws RecognitionException {
        EulangParser.valueCond_return retval = new EulangParser.valueCond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHEN221=null;
        Token ELSE223=null;
        Token UNTIL225=null;
        Token THEN227=null;
        Token ELSE229=null;
        Token WITH231=null;
        EulangParser.rhsExpr_return rhsExpr220 = null;

        EulangParser.rhsExpr_return rhsExpr222 = null;

        EulangParser.rhsExpr_return rhsExpr224 = null;

        EulangParser.rhsExpr_return rhsExpr226 = null;

        EulangParser.rhsExpr_return rhsExpr228 = null;

        EulangParser.rhsExpr_return rhsExpr230 = null;

        EulangParser.rhsExpr_return rhsExpr232 = null;


        CommonTree WHEN221_tree=null;
        CommonTree ELSE223_tree=null;
        CommonTree UNTIL225_tree=null;
        CommonTree THEN227_tree=null;
        CommonTree ELSE229_tree=null;
        CommonTree WITH231_tree=null;
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleTokenStream stream_UNTIL=new RewriteRuleTokenStream(adaptor,"token UNTIL");
        RewriteRuleTokenStream stream_WHEN=new RewriteRuleTokenStream(adaptor,"token WHEN");
        RewriteRuleTokenStream stream_WITH=new RewriteRuleTokenStream(adaptor,"token WITH");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:264:11: ( rhsExpr WHEN rhsExpr ELSE rhsExpr -> ^( WHEN ( rhsExpr )+ ) | UNTIL rhsExpr THEN rhsExpr ELSE rhsExpr -> ^( UNTIL ( rhsExpr )+ ) | WITH rhsExpr -> rhsExpr )
            int alt62=3;
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
                alt62=1;
                }
                break;
            case UNTIL:
                {
                alt62=2;
                }
                break;
            case WITH:
                {
                alt62=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 62, 0, input);

                throw nvae;
            }

            switch (alt62) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:264:13: rhsExpr WHEN rhsExpr ELSE rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_valueCond2634);
                    rhsExpr220=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr220.getTree());
                    WHEN221=(Token)match(input,WHEN,FOLLOW_WHEN_in_valueCond2636); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_WHEN.add(WHEN221);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2638);
                    rhsExpr222=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr222.getTree());
                    ELSE223=(Token)match(input,ELSE,FOLLOW_ELSE_in_valueCond2640); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE223);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2642);
                    rhsExpr224=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr224.getTree());


                    // AST REWRITE
                    // elements: WHEN, rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 264:48: -> ^( WHEN ( rhsExpr )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:264:51: ^( WHEN ( rhsExpr )+ )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:5: UNTIL rhsExpr THEN rhsExpr ELSE rhsExpr
                    {
                    UNTIL225=(Token)match(input,UNTIL,FOLLOW_UNTIL_in_valueCond2659); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_UNTIL.add(UNTIL225);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2661);
                    rhsExpr226=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr226.getTree());
                    THEN227=(Token)match(input,THEN,FOLLOW_THEN_in_valueCond2663); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN227);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2665);
                    rhsExpr228=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr228.getTree());
                    ELSE229=(Token)match(input,ELSE,FOLLOW_ELSE_in_valueCond2667); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE229);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2669);
                    rhsExpr230=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr230.getTree());


                    // AST REWRITE
                    // elements: UNTIL, rhsExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 265:48: -> ^( UNTIL ( rhsExpr )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:51: ^( UNTIL ( rhsExpr )+ )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:5: WITH rhsExpr
                    {
                    WITH231=(Token)match(input,WITH,FOLLOW_WITH_in_valueCond2687); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_WITH.add(WITH231);

                    pushFollow(FOLLOW_rhsExpr_in_valueCond2689);
                    rhsExpr232=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr232.getTree());


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
                    // 266:48: -> rhsExpr
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:1: labelStmt : ATSIGN ID COLON -> ^( LABEL ID ) ;
    public final EulangParser.labelStmt_return labelStmt() throws RecognitionException {
        EulangParser.labelStmt_return retval = new EulangParser.labelStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATSIGN233=null;
        Token ID234=null;
        Token COLON235=null;

        CommonTree ATSIGN233_tree=null;
        CommonTree ID234_tree=null;
        CommonTree COLON235_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ATSIGN=new RewriteRuleTokenStream(adaptor,"token ATSIGN");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:10: ( ATSIGN ID COLON -> ^( LABEL ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:12: ATSIGN ID COLON
            {
            ATSIGN233=(Token)match(input,ATSIGN,FOLLOW_ATSIGN_in_labelStmt2740); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ATSIGN.add(ATSIGN233);

            ID234=(Token)match(input,ID,FOLLOW_ID_in_labelStmt2742); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID234);

            COLON235=(Token)match(input,COLON,FOLLOW_COLON_in_labelStmt2744); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON235);



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
            // 269:47: -> ^( LABEL ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:50: ^( LABEL ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:1: gotoStmt : GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) ;
    public final EulangParser.gotoStmt_return gotoStmt() throws RecognitionException {
        EulangParser.gotoStmt_return retval = new EulangParser.gotoStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token GOTO236=null;
        Token IF238=null;
        EulangParser.idOrScopeRef_return idOrScopeRef237 = null;

        EulangParser.rhsExpr_return rhsExpr239 = null;


        CommonTree GOTO236_tree=null;
        CommonTree IF238_tree=null;
        RewriteRuleTokenStream stream_GOTO=new RewriteRuleTokenStream(adaptor,"token GOTO");
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:9: ( GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:11: GOTO idOrScopeRef ( IF rhsExpr )?
            {
            GOTO236=(Token)match(input,GOTO,FOLLOW_GOTO_in_gotoStmt2780); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GOTO.add(GOTO236);

            pushFollow(FOLLOW_idOrScopeRef_in_gotoStmt2782);
            idOrScopeRef237=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef237.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:29: ( IF rhsExpr )?
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==IF) ) {
                alt63=1;
            }
            switch (alt63) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:30: IF rhsExpr
                    {
                    IF238=(Token)match(input,IF,FOLLOW_IF_in_gotoStmt2785); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF238);

                    pushFollow(FOLLOW_rhsExpr_in_gotoStmt2787);
                    rhsExpr239=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr239.getTree());

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
            // 271:53: -> ^( GOTO idOrScopeRef ( rhsExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:56: ^( GOTO idOrScopeRef ( rhsExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_GOTO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:76: ( rhsExpr )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:1: blockStmt : LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) ;
    public final EulangParser.blockStmt_return blockStmt() throws RecognitionException {
        EulangParser.blockStmt_return retval = new EulangParser.blockStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE240=null;
        Token RBRACE242=null;
        EulangParser.codestmtlist_return codestmtlist241 = null;


        CommonTree LBRACE240_tree=null;
        CommonTree RBRACE242_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:10: ( LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:12: LBRACE codestmtlist RBRACE
            {
            LBRACE240=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_blockStmt2824); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE240);

            pushFollow(FOLLOW_codestmtlist_in_blockStmt2826);
            codestmtlist241=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist241.getTree());
            RBRACE242=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_blockStmt2828); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE242);



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
            // 276:43: -> ^( BLOCK codestmtlist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:46: ^( BLOCK codestmtlist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:1: tuple : LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) ;
    public final EulangParser.tuple_return tuple() throws RecognitionException {
        EulangParser.tuple_return retval = new EulangParser.tuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN243=null;
        Token RPAREN245=null;
        EulangParser.tupleEntries_return tupleEntries244 = null;


        CommonTree LPAREN243_tree=null;
        CommonTree RPAREN245_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule tupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:7: ( LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:9: LPAREN tupleEntries RPAREN
            {
            LPAREN243=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_tuple2851); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN243);

            pushFollow(FOLLOW_tupleEntries_in_tuple2853);
            tupleEntries244=tupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleEntries.add(tupleEntries244.getTree());
            RPAREN245=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_tuple2855); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN245);



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
            // 279:41: -> ^( TUPLE ( tupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:44: ^( TUPLE ( tupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:1: tupleEntries : assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ ;
    public final EulangParser.tupleEntries_return tupleEntries() throws RecognitionException {
        EulangParser.tupleEntries_return retval = new EulangParser.tupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA247=null;
        EulangParser.assignExpr_return assignExpr246 = null;

        EulangParser.assignExpr_return assignExpr248 = null;


        CommonTree COMMA247_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:14: ( assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:16: assignExpr ( COMMA assignExpr )+
            {
            pushFollow(FOLLOW_assignExpr_in_tupleEntries2883);
            assignExpr246=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr246.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:27: ( COMMA assignExpr )+
            int cnt64=0;
            loop64:
            do {
                int alt64=2;
                int LA64_0 = input.LA(1);

                if ( (LA64_0==COMMA) ) {
                    alt64=1;
                }


                switch (alt64) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:28: COMMA assignExpr
            	    {
            	    COMMA247=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleEntries2886); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA247);

            	    pushFollow(FOLLOW_assignExpr_in_tupleEntries2888);
            	    assignExpr248=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr248.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt64 >= 1 ) break loop64;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(64, input);
                        throw eee;
                }
                cnt64++;
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
            // 282:48: -> ( assignExpr )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:1: idTuple : LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) ;
    public final EulangParser.idTuple_return idTuple() throws RecognitionException {
        EulangParser.idTuple_return retval = new EulangParser.idTuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN249=null;
        Token RPAREN251=null;
        EulangParser.idTupleEntries_return idTupleEntries250 = null;


        CommonTree LPAREN249_tree=null;
        CommonTree RPAREN251_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_idTupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule idTupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:9: ( LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:11: LPAREN idTupleEntries RPAREN
            {
            LPAREN249=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_idTuple2907); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN249);

            pushFollow(FOLLOW_idTupleEntries_in_idTuple2909);
            idTupleEntries250=idTupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTupleEntries.add(idTupleEntries250.getTree());
            RPAREN251=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_idTuple2911); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN251);



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
            // 285:45: -> ^( TUPLE ( idTupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:48: ^( TUPLE ( idTupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:1: idTupleEntries : idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ ;
    public final EulangParser.idTupleEntries_return idTupleEntries() throws RecognitionException {
        EulangParser.idTupleEntries_return retval = new EulangParser.idTupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA253=null;
        EulangParser.idOrScopeRef_return idOrScopeRef252 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef254 = null;


        CommonTree COMMA253_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:16: ( idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:18: idOrScopeRef ( COMMA idOrScopeRef )+
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries2939);
            idOrScopeRef252=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef252.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:31: ( COMMA idOrScopeRef )+
            int cnt65=0;
            loop65:
            do {
                int alt65=2;
                int LA65_0 = input.LA(1);

                if ( (LA65_0==COMMA) ) {
                    alt65=1;
                }


                switch (alt65) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:32: COMMA idOrScopeRef
            	    {
            	    COMMA253=(Token)match(input,COMMA,FOLLOW_COMMA_in_idTupleEntries2942); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA253);

            	    pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries2944);
            	    idOrScopeRef254=idOrScopeRef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef254.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt65 >= 1 ) break loop65;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(65, input);
                        throw eee;
                }
                cnt65++;
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
            // 288:54: -> ( idOrScopeRef )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:1: rhsExpr : condStar -> condStar ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.condStar_return condStar255 = null;


        RewriteRuleSubtreeStream stream_condStar=new RewriteRuleSubtreeStream(adaptor,"rule condStar");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:9: ( condStar -> condStar )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:13: condStar
            {
            pushFollow(FOLLOW_condStar_in_rhsExpr2965);
            condStar255=condStar();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStar.add(condStar255.getTree());


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
            // 291:22: -> condStar
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:1: funcCall : idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN257=null;
        Token RPAREN259=null;
        EulangParser.idOrScopeRef_return idOrScopeRef256 = null;

        EulangParser.arglist_return arglist258 = null;


        CommonTree LPAREN257_tree=null;
        CommonTree RPAREN259_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:10: ( idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:12: idOrScopeRef LPAREN arglist RPAREN
            {
            pushFollow(FOLLOW_idOrScopeRef_in_funcCall2986);
            idOrScopeRef256=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef256.getTree());
            LPAREN257=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall2988); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN257);

            pushFollow(FOLLOW_arglist_in_funcCall2990);
            arglist258=arglist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arglist.add(arglist258.getTree());
            RPAREN259=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall2992); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN259);



            // AST REWRITE
            // elements: arglist, idOrScopeRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 294:49: -> ^( CALL idOrScopeRef arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:56: ^( CALL idOrScopeRef arglist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA261=null;
        Token COMMA263=null;
        EulangParser.arg_return arg260 = null;

        EulangParser.arg_return arg262 = null;


        CommonTree COMMA261_tree=null;
        CommonTree COMMA263_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt68=2;
            int LA68_0 = input.LA(1);

            if ( ((LA68_0>=CODE && LA68_0<=MACRO)||LA68_0==GOTO||LA68_0==ID||LA68_0==COLON||LA68_0==LBRACE||LA68_0==LPAREN||LA68_0==NIL||LA68_0==IF||LA68_0==NOT||(LA68_0>=MINUS && LA68_0<=STAR)||(LA68_0>=TILDE && LA68_0<=STRING_LITERAL)||LA68_0==COLONS) ) {
                alt68=1;
            }
            switch (alt68) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist3023);
                    arg260=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg260.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:15: ( COMMA arg )*
                    loop66:
                    do {
                        int alt66=2;
                        int LA66_0 = input.LA(1);

                        if ( (LA66_0==COMMA) ) {
                            int LA66_1 = input.LA(2);

                            if ( ((LA66_1>=CODE && LA66_1<=MACRO)||LA66_1==GOTO||LA66_1==ID||LA66_1==COLON||LA66_1==LBRACE||LA66_1==LPAREN||LA66_1==NIL||LA66_1==IF||LA66_1==NOT||(LA66_1>=MINUS && LA66_1<=STAR)||(LA66_1>=TILDE && LA66_1<=STRING_LITERAL)||LA66_1==COLONS) ) {
                                alt66=1;
                            }


                        }


                        switch (alt66) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:17: COMMA arg
                    	    {
                    	    COMMA261=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist3027); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA261);

                    	    pushFollow(FOLLOW_arg_in_arglist3029);
                    	    arg262=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg262.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop66;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:29: ( COMMA )?
                    int alt67=2;
                    int LA67_0 = input.LA(1);

                    if ( (LA67_0==COMMA) ) {
                        alt67=1;
                    }
                    switch (alt67) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:29: COMMA
                            {
                            COMMA263=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist3033); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA263);


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
            // 298:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:298:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:1: arg : ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) );
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE265=null;
        Token RBRACE267=null;
        EulangParser.assignExpr_return assignExpr264 = null;

        EulangParser.codestmtlist_return codestmtlist266 = null;

        EulangParser.gotoStmt_return gotoStmt268 = null;


        CommonTree LBRACE265_tree=null;
        CommonTree RBRACE267_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:4: ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) )
            int alt69=3;
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
                alt69=1;
                }
                break;
            case LBRACE:
                {
                alt69=2;
                }
                break;
            case GOTO:
                {
                alt69=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 69, 0, input);

                throw nvae;
            }

            switch (alt69) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:7: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_arg3082);
                    assignExpr264=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr264.getTree());


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
                    // 301:37: -> ^( EXPR assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:40: ^( EXPR assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:5: LBRACE codestmtlist RBRACE
                    {
                    LBRACE265=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_arg3115); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE265);

                    pushFollow(FOLLOW_codestmtlist_in_arg3117);
                    codestmtlist266=codestmtlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist266.getTree());
                    RBRACE267=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_arg3119); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE267);



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
                    // 302:37: -> ^( EXPR ^( CODE codestmtlist ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:40: ^( EXPR ^( CODE codestmtlist ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:47: ^( CODE codestmtlist )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:5: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_arg3143);
                    gotoStmt268=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt268.getTree());


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
                    // 303:37: -> ^( EXPR gotoStmt )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:40: ^( EXPR gotoStmt )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:1: withStmt : WITH bindings ARROW b= rhsExpr ( ELSE e= codeStmtExpr )? -> ^( WITH bindings $b ( $e)? ) ;
    public final EulangParser.withStmt_return withStmt() throws RecognitionException {
        EulangParser.withStmt_return retval = new EulangParser.withStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WITH269=null;
        Token ARROW271=null;
        Token ELSE272=null;
        EulangParser.rhsExpr_return b = null;

        EulangParser.codeStmtExpr_return e = null;

        EulangParser.bindings_return bindings270 = null;


        CommonTree WITH269_tree=null;
        CommonTree ARROW271_tree=null;
        CommonTree ELSE272_tree=null;
        RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
        RewriteRuleTokenStream stream_WITH=new RewriteRuleTokenStream(adaptor,"token WITH");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_bindings=new RewriteRuleSubtreeStream(adaptor,"rule bindings");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:10: ( WITH bindings ARROW b= rhsExpr ( ELSE e= codeStmtExpr )? -> ^( WITH bindings $b ( $e)? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:12: WITH bindings ARROW b= rhsExpr ( ELSE e= codeStmtExpr )?
            {
            WITH269=(Token)match(input,WITH,FOLLOW_WITH_in_withStmt3196); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WITH.add(WITH269);

            pushFollow(FOLLOW_bindings_in_withStmt3198);
            bindings270=bindings();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bindings.add(bindings270.getTree());
            ARROW271=(Token)match(input,ARROW,FOLLOW_ARROW_in_withStmt3200); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ARROW.add(ARROW271);

            pushFollow(FOLLOW_rhsExpr_in_withStmt3204);
            b=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(b.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:42: ( ELSE e= codeStmtExpr )?
            int alt70=2;
            int LA70_0 = input.LA(1);

            if ( (LA70_0==ELSE) ) {
                alt70=1;
            }
            switch (alt70) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:43: ELSE e= codeStmtExpr
                    {
                    ELSE272=(Token)match(input,ELSE,FOLLOW_ELSE_in_withStmt3207); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE272);

                    pushFollow(FOLLOW_codeStmtExpr_in_withStmt3211);
                    e=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(e.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: bindings, b, WITH, e
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
            // 316:65: -> ^( WITH bindings $b ( $e)? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:68: ^( WITH bindings $b ( $e)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_WITH.nextNode(), root_1);

                adaptor.addChild(root_1, stream_bindings.nextTree());
                adaptor.addChild(root_1, stream_b.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:87: ( $e)?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:1: bindings : binding ( AND binding )* -> ( binding )+ ;
    public final EulangParser.bindings_return bindings() throws RecognitionException {
        EulangParser.bindings_return retval = new EulangParser.bindings_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND274=null;
        EulangParser.binding_return binding273 = null;

        EulangParser.binding_return binding275 = null;


        CommonTree AND274_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_binding=new RewriteRuleSubtreeStream(adaptor,"rule binding");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:9: ( binding ( AND binding )* -> ( binding )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:11: binding ( AND binding )*
            {
            pushFollow(FOLLOW_binding_in_bindings3239);
            binding273=binding();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_binding.add(binding273.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:19: ( AND binding )*
            loop71:
            do {
                int alt71=2;
                int LA71_0 = input.LA(1);

                if ( (LA71_0==AND) ) {
                    alt71=1;
                }


                switch (alt71) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:20: AND binding
            	    {
            	    AND274=(Token)match(input,AND,FOLLOW_AND_in_bindings3242); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND274);

            	    pushFollow(FOLLOW_binding_in_bindings3244);
            	    binding275=binding();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_binding.add(binding275.getTree());

            	    }
            	    break;

            	default :
            	    break loop71;
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
            // 319:34: -> ( binding )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:321:1: binding : rhsExpr AS type -> ^( BINDING type rhsExpr ) ;
    public final EulangParser.binding_return binding() throws RecognitionException {
        EulangParser.binding_return retval = new EulangParser.binding_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AS277=null;
        EulangParser.rhsExpr_return rhsExpr276 = null;

        EulangParser.type_return type278 = null;


        CommonTree AS277_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:321:8: ( rhsExpr AS type -> ^( BINDING type rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:321:10: rhsExpr AS type
            {
            pushFollow(FOLLOW_rhsExpr_in_binding3262);
            rhsExpr276=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr276.getTree());
            AS277=(Token)match(input,AS,FOLLOW_AS_in_binding3264); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AS.add(AS277);

            pushFollow(FOLLOW_type_in_binding3266);
            type278=type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_type.add(type278.getTree());


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
            // 321:28: -> ^( BINDING type rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:321:31: ^( BINDING type rhsExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:1: condStar : ( cond -> cond | IF ifExprs -> ifExprs );
    public final EulangParser.condStar_return condStar() throws RecognitionException {
        EulangParser.condStar_return retval = new EulangParser.condStar_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IF280=null;
        EulangParser.cond_return cond279 = null;

        EulangParser.ifExprs_return ifExprs281 = null;


        CommonTree IF280_tree=null;
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_ifExprs=new RewriteRuleSubtreeStream(adaptor,"rule ifExprs");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:9: ( cond -> cond | IF ifExprs -> ifExprs )
            int alt72=2;
            int LA72_0 = input.LA(1);

            if ( ((LA72_0>=CODE && LA72_0<=MACRO)||LA72_0==ID||LA72_0==COLON||LA72_0==LPAREN||LA72_0==NIL||LA72_0==NOT||(LA72_0>=MINUS && LA72_0<=STAR)||(LA72_0>=TILDE && LA72_0<=STRING_LITERAL)||LA72_0==COLONS) ) {
                alt72=1;
            }
            else if ( (LA72_0==IF) ) {
                alt72=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 72, 0, input);

                throw nvae;
            }
            switch (alt72) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:11: cond
                    {
                    pushFollow(FOLLOW_cond_in_condStar3296);
                    cond279=cond();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cond.add(cond279.getTree());


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
                    // 329:16: -> cond
                    {
                        adaptor.addChild(root_0, stream_cond.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:6: IF ifExprs
                    {
                    IF280=(Token)match(input,IF,FOLLOW_IF_in_condStar3311); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF280);

                    pushFollow(FOLLOW_ifExprs_in_condStar3313);
                    ifExprs281=ifExprs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ifExprs.add(ifExprs281.getTree());


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
                    // 331:17: -> ifExprs
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:1: ifExprs : thenClause elses -> ^( CONDLIST thenClause elses ) ;
    public final EulangParser.ifExprs_return ifExprs() throws RecognitionException {
        EulangParser.ifExprs_return retval = new EulangParser.ifExprs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.thenClause_return thenClause282 = null;

        EulangParser.elses_return elses283 = null;


        RewriteRuleSubtreeStream stream_thenClause=new RewriteRuleSubtreeStream(adaptor,"rule thenClause");
        RewriteRuleSubtreeStream stream_elses=new RewriteRuleSubtreeStream(adaptor,"rule elses");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:9: ( thenClause elses -> ^( CONDLIST thenClause elses ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:11: thenClause elses
            {
            pushFollow(FOLLOW_thenClause_in_ifExprs3340);
            thenClause282=thenClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_thenClause.add(thenClause282.getTree());
            pushFollow(FOLLOW_elses_in_ifExprs3342);
            elses283=elses();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elses.add(elses283.getTree());


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
            // 352:28: -> ^( CONDLIST thenClause elses )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:31: ^( CONDLIST thenClause elses )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:1: thenClause : t= arg THEN v= arg -> ^( CONDTEST $t $v) ;
    public final EulangParser.thenClause_return thenClause() throws RecognitionException {
        EulangParser.thenClause_return retval = new EulangParser.thenClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token THEN284=null;
        EulangParser.arg_return t = null;

        EulangParser.arg_return v = null;


        CommonTree THEN284_tree=null;
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:12: (t= arg THEN v= arg -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:14: t= arg THEN v= arg
            {
            pushFollow(FOLLOW_arg_in_thenClause3364);
            t=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(t.getTree());
            THEN284=(Token)match(input,THEN,FOLLOW_THEN_in_thenClause3366); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_THEN.add(THEN284);

            pushFollow(FOLLOW_arg_in_thenClause3370);
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
            // 354:33: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:36: ^( CONDTEST $t $v)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:1: elses : ( elif )* elseClause -> ( elif )* elseClause ;
    public final EulangParser.elses_return elses() throws RecognitionException {
        EulangParser.elses_return retval = new EulangParser.elses_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.elif_return elif285 = null;

        EulangParser.elseClause_return elseClause286 = null;


        RewriteRuleSubtreeStream stream_elseClause=new RewriteRuleSubtreeStream(adaptor,"rule elseClause");
        RewriteRuleSubtreeStream stream_elif=new RewriteRuleSubtreeStream(adaptor,"rule elif");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:7: ( ( elif )* elseClause -> ( elif )* elseClause )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:9: ( elif )* elseClause
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:9: ( elif )*
            loop73:
            do {
                int alt73=2;
                int LA73_0 = input.LA(1);

                if ( (LA73_0==ELIF) ) {
                    alt73=1;
                }


                switch (alt73) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:9: elif
            	    {
            	    pushFollow(FOLLOW_elif_in_elses3398);
            	    elif285=elif();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_elif.add(elif285.getTree());

            	    }
            	    break;

            	default :
            	    break loop73;
                }
            } while (true);

            pushFollow(FOLLOW_elseClause_in_elses3401);
            elseClause286=elseClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elseClause.add(elseClause286.getTree());


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
            // 356:29: -> ( elif )* elseClause
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:32: ( elif )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:1: elif : ELIF t= arg THEN v= arg -> ^( CONDTEST $t $v) ;
    public final EulangParser.elif_return elif() throws RecognitionException {
        EulangParser.elif_return retval = new EulangParser.elif_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELIF287=null;
        Token THEN288=null;
        EulangParser.arg_return t = null;

        EulangParser.arg_return v = null;


        CommonTree ELIF287_tree=null;
        CommonTree THEN288_tree=null;
        RewriteRuleTokenStream stream_ELIF=new RewriteRuleTokenStream(adaptor,"token ELIF");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:6: ( ELIF t= arg THEN v= arg -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:8: ELIF t= arg THEN v= arg
            {
            ELIF287=(Token)match(input,ELIF,FOLLOW_ELIF_in_elif3424); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ELIF.add(ELIF287);

            pushFollow(FOLLOW_arg_in_elif3428);
            t=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(t.getTree());
            THEN288=(Token)match(input,THEN,FOLLOW_THEN_in_elif3430); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_THEN.add(THEN288);

            pushFollow(FOLLOW_arg_in_elif3434);
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
            // 358:31: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:34: ^( CONDTEST $t $v)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:1: elseClause : ( ELSE arg -> ^( CONDTEST ^( LIT TRUE ) arg ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) );
    public final EulangParser.elseClause_return elseClause() throws RecognitionException {
        EulangParser.elseClause_return retval = new EulangParser.elseClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELSE289=null;
        Token FI291=null;
        EulangParser.arg_return arg290 = null;


        CommonTree ELSE289_tree=null;
        CommonTree FI291_tree=null;
        RewriteRuleTokenStream stream_FI=new RewriteRuleTokenStream(adaptor,"token FI");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:12: ( ELSE arg -> ^( CONDTEST ^( LIT TRUE ) arg ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) )
            int alt74=2;
            int LA74_0 = input.LA(1);

            if ( (LA74_0==ELSE) ) {
                alt74=1;
            }
            else if ( (LA74_0==FI) ) {
                alt74=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 74, 0, input);

                throw nvae;
            }
            switch (alt74) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:14: ELSE arg
                    {
                    ELSE289=(Token)match(input,ELSE,FOLLOW_ELSE_in_elseClause3460); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE289);

                    pushFollow(FOLLOW_arg_in_elseClause3462);
                    arg290=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg290.getTree());


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
                    // 360:29: -> ^( CONDTEST ^( LIT TRUE ) arg )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:32: ^( CONDTEST ^( LIT TRUE ) arg )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:43: ^( LIT TRUE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:6: FI
                    {
                    FI291=(Token)match(input,FI,FOLLOW_FI_in_elseClause3489); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FI.add(FI291);



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
                    // 361:9: -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:12: ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:23: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:35: ^( LIT NIL )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:1: cond : ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION293=null;
        Token COLON294=null;
        EulangParser.logor_return t = null;

        EulangParser.logor_return f = null;

        EulangParser.logor_return logor292 = null;


        CommonTree QUESTION293_tree=null;
        CommonTree COLON294_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logor=new RewriteRuleSubtreeStream(adaptor,"rule logor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:5: ( ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:10: ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:10: ( logor -> logor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:12: logor
            {
            pushFollow(FOLLOW_logor_in_cond3529);
            logor292=logor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logor.add(logor292.getTree());


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
            // 367:19: -> logor
            {
                adaptor.addChild(root_0, stream_logor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:7: ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            loop75:
            do {
                int alt75=2;
                int LA75_0 = input.LA(1);

                if ( (LA75_0==QUESTION) ) {
                    alt75=1;
                }


                switch (alt75) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:9: QUESTION t= logor COLON f= logor
            	    {
            	    QUESTION293=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond3546); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION293);

            	    pushFollow(FOLLOW_logor_in_cond3550);
            	    t=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(t.getTree());
            	    COLON294=(Token)match(input,COLON,FOLLOW_COLON_in_cond3552); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON294);

            	    pushFollow(FOLLOW_logor_in_cond3556);
            	    f=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(f.getTree());


            	    // AST REWRITE
            	    // elements: cond, f, t
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
            	    // 368:40: -> ^( COND $cond $t $f)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:43: ^( COND $cond $t $f)
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
    // $ANTLR end "cond"

    public static class logor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:1: logor : ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* ;
    public final EulangParser.logor_return logor() throws RecognitionException {
        EulangParser.logor_return retval = new EulangParser.logor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token OR296=null;
        EulangParser.logand_return r = null;

        EulangParser.logand_return logand295 = null;


        CommonTree OR296_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_logand=new RewriteRuleSubtreeStream(adaptor,"rule logand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:7: ( ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:9: ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:9: ( logand -> logand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:11: logand
            {
            pushFollow(FOLLOW_logand_in_logor3586);
            logand295=logand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logand.add(logand295.getTree());


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
            // 371:19: -> logand
            {
                adaptor.addChild(root_0, stream_logand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:7: ( OR r= logand -> ^( OR $logor $r) )*
            loop76:
            do {
                int alt76=2;
                int LA76_0 = input.LA(1);

                if ( (LA76_0==OR) ) {
                    alt76=1;
                }


                switch (alt76) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:9: OR r= logand
            	    {
            	    OR296=(Token)match(input,OR,FOLLOW_OR_in_logor3603); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR296);

            	    pushFollow(FOLLOW_logand_in_logor3607);
            	    r=logand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, OR, logor
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
            	    // 372:21: -> ^( OR $logor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:24: ^( OR $logor $r)
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
            	    break loop76;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:374:1: logand : ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* ;
    public final EulangParser.logand_return logand() throws RecognitionException {
        EulangParser.logand_return retval = new EulangParser.logand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND298=null;
        EulangParser.not_return r = null;

        EulangParser.not_return not297 = null;


        CommonTree AND298_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:374:8: ( ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:374:10: ( not -> not ) ( AND r= not -> ^( AND $logand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:374:10: ( not -> not )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:374:12: not
            {
            pushFollow(FOLLOW_not_in_logand3638);
            not297=not();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_not.add(not297.getTree());


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
            // 374:16: -> not
            {
                adaptor.addChild(root_0, stream_not.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:7: ( AND r= not -> ^( AND $logand $r) )*
            loop77:
            do {
                int alt77=2;
                int LA77_0 = input.LA(1);

                if ( (LA77_0==AND) ) {
                    alt77=1;
                }


                switch (alt77) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:9: AND r= not
            	    {
            	    AND298=(Token)match(input,AND,FOLLOW_AND_in_logand3654); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND298);

            	    pushFollow(FOLLOW_not_in_logand3658);
            	    r=not();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_not.add(r.getTree());


            	    // AST REWRITE
            	    // elements: logand, AND, r
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
            	    // 375:19: -> ^( AND $logand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:22: ^( AND $logand $r)
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
            	    break loop77;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:378:1: not : ( comp -> comp | NOT u= comp -> ^( NOT $u) );
    public final EulangParser.not_return not() throws RecognitionException {
        EulangParser.not_return retval = new EulangParser.not_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NOT300=null;
        EulangParser.comp_return u = null;

        EulangParser.comp_return comp299 = null;


        CommonTree NOT300_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:378:5: ( comp -> comp | NOT u= comp -> ^( NOT $u) )
            int alt78=2;
            int LA78_0 = input.LA(1);

            if ( ((LA78_0>=CODE && LA78_0<=MACRO)||LA78_0==ID||LA78_0==COLON||LA78_0==LPAREN||LA78_0==NIL||(LA78_0>=MINUS && LA78_0<=STAR)||(LA78_0>=TILDE && LA78_0<=STRING_LITERAL)||LA78_0==COLONS) ) {
                alt78=1;
            }
            else if ( (LA78_0==NOT) ) {
                alt78=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 78, 0, input);

                throw nvae;
            }
            switch (alt78) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:378:8: comp
                    {
                    pushFollow(FOLLOW_comp_in_not3704);
                    comp299=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(comp299.getTree());


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
                    // 378:17: -> comp
                    {
                        adaptor.addChild(root_0, stream_comp.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:7: NOT u= comp
                    {
                    NOT300=(Token)match(input,NOT,FOLLOW_NOT_in_not3720); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT300);

                    pushFollow(FOLLOW_comp_in_not3724);
                    u=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(u.getTree());


                    // AST REWRITE
                    // elements: u, NOT
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
                    // 379:22: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:25: ^( NOT $u)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:1: comp : ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ302=null;
        Token COMPNE303=null;
        Token COMPLE304=null;
        Token COMPGE305=null;
        Token LESS306=null;
        Token GREATER307=null;
        EulangParser.bitor_return r = null;

        EulangParser.bitor_return bitor301 = null;


        CommonTree COMPEQ302_tree=null;
        CommonTree COMPNE303_tree=null;
        CommonTree COMPLE304_tree=null;
        CommonTree COMPGE305_tree=null;
        CommonTree LESS306_tree=null;
        CommonTree GREATER307_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_bitor=new RewriteRuleSubtreeStream(adaptor,"rule bitor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:5: ( ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:9: ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:9: ( bitor -> bitor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:11: bitor
            {
            pushFollow(FOLLOW_bitor_in_comp3758);
            bitor301=bitor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitor.add(bitor301.getTree());


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
            // 382:24: -> bitor
            {
                adaptor.addChild(root_0, stream_bitor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:7: ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            loop79:
            do {
                int alt79=7;
                switch ( input.LA(1) ) {
                case COMPEQ:
                    {
                    alt79=1;
                    }
                    break;
                case COMPNE:
                    {
                    alt79=2;
                    }
                    break;
                case COMPLE:
                    {
                    alt79=3;
                    }
                    break;
                case COMPGE:
                    {
                    alt79=4;
                    }
                    break;
                case LESS:
                    {
                    alt79=5;
                    }
                    break;
                case GREATER:
                    {
                    alt79=6;
                    }
                    break;

                }

                switch (alt79) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:9: COMPEQ r= bitor
            	    {
            	    COMPEQ302=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_comp3791); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ302);

            	    pushFollow(FOLLOW_bitor_in_comp3795);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, r, COMPEQ
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
            	    // 383:24: -> ^( COMPEQ $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:383:27: ^( COMPEQ $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:384:9: COMPNE r= bitor
            	    {
            	    COMPNE303=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_comp3817); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE303);

            	    pushFollow(FOLLOW_bitor_in_comp3821);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, COMPNE, comp
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
            	    // 384:24: -> ^( COMPNE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:384:27: ^( COMPNE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:9: COMPLE r= bitor
            	    {
            	    COMPLE304=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp3843); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE304);

            	    pushFollow(FOLLOW_bitor_in_comp3847);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, COMPLE, comp
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
            	    // 385:27: -> ^( COMPLE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:30: ^( COMPLE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:386:9: COMPGE r= bitor
            	    {
            	    COMPGE305=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp3872); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE305);

            	    pushFollow(FOLLOW_bitor_in_comp3876);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPGE, r, comp
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
            	    // 386:27: -> ^( COMPGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:386:30: ^( COMPGE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:9: LESS r= bitor
            	    {
            	    LESS306=(Token)match(input,LESS,FOLLOW_LESS_in_comp3901); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS306);

            	    pushFollow(FOLLOW_bitor_in_comp3905);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, comp, LESS
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
            	    // 387:26: -> ^( LESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:29: ^( LESS $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:9: GREATER r= bitor
            	    {
            	    GREATER307=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp3931); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER307);

            	    pushFollow(FOLLOW_bitor_in_comp3935);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, r, GREATER
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
            	    // 388:28: -> ^( GREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:31: ^( GREATER $comp $r)
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
            	    break loop79;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:1: bitor : ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* ;
    public final EulangParser.bitor_return bitor() throws RecognitionException {
        EulangParser.bitor_return retval = new EulangParser.bitor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR309=null;
        EulangParser.bitxor_return r = null;

        EulangParser.bitxor_return bitxor308 = null;


        CommonTree BAR309_tree=null;
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_bitxor=new RewriteRuleSubtreeStream(adaptor,"rule bitxor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:6: ( ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:8: ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:8: ( bitxor -> bitxor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:10: bitxor
            {
            pushFollow(FOLLOW_bitxor_in_bitor3985);
            bitxor308=bitxor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitxor.add(bitxor308.getTree());


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
            // 393:22: -> bitxor
            {
                adaptor.addChild(root_0, stream_bitxor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:7: ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            loop80:
            do {
                int alt80=2;
                int LA80_0 = input.LA(1);

                if ( (LA80_0==BAR) ) {
                    alt80=1;
                }


                switch (alt80) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:9: BAR r= bitxor
            	    {
            	    BAR309=(Token)match(input,BAR,FOLLOW_BAR_in_bitor4013); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR309);

            	    pushFollow(FOLLOW_bitxor_in_bitor4017);
            	    r=bitxor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitxor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, bitor
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
            	    // 394:23: -> ^( BITOR $bitor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:394:26: ^( BITOR $bitor $r)
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
            	    break loop80;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:1: bitxor : ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* ;
    public final EulangParser.bitxor_return bitxor() throws RecognitionException {
        EulangParser.bitxor_return retval = new EulangParser.bitxor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CARET311=null;
        EulangParser.bitand_return r = null;

        EulangParser.bitand_return bitand310 = null;


        CommonTree CARET311_tree=null;
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_bitand=new RewriteRuleSubtreeStream(adaptor,"rule bitand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:7: ( ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:9: ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:9: ( bitand -> bitand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:396:11: bitand
            {
            pushFollow(FOLLOW_bitand_in_bitxor4043);
            bitand310=bitand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitand.add(bitand310.getTree());


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
            // 396:23: -> bitand
            {
                adaptor.addChild(root_0, stream_bitand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:397:7: ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            loop81:
            do {
                int alt81=2;
                int LA81_0 = input.LA(1);

                if ( (LA81_0==CARET) ) {
                    alt81=1;
                }


                switch (alt81) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:397:9: CARET r= bitand
            	    {
            	    CARET311=(Token)match(input,CARET,FOLLOW_CARET_in_bitxor4071); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET311);

            	    pushFollow(FOLLOW_bitand_in_bitxor4075);
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
            	    // 397:25: -> ^( BITXOR $bitxor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:397:28: ^( BITXOR $bitxor $r)
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
            	    break loop81;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:399:1: bitand : ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* ;
    public final EulangParser.bitand_return bitand() throws RecognitionException {
        EulangParser.bitand_return retval = new EulangParser.bitand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP313=null;
        EulangParser.shift_return r = null;

        EulangParser.shift_return shift312 = null;


        CommonTree AMP313_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:399:7: ( ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:399:9: ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:399:9: ( shift -> shift )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:399:11: shift
            {
            pushFollow(FOLLOW_shift_in_bitand4100);
            shift312=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(shift312.getTree());


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
            // 399:22: -> shift
            {
                adaptor.addChild(root_0, stream_shift.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:400:7: ( AMP r= shift -> ^( BITAND $bitand $r) )*
            loop82:
            do {
                int alt82=2;
                int LA82_0 = input.LA(1);

                if ( (LA82_0==AMP) ) {
                    alt82=1;
                }


                switch (alt82) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:400:9: AMP r= shift
            	    {
            	    AMP313=(Token)match(input,AMP,FOLLOW_AMP_in_bitand4128); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP313);

            	    pushFollow(FOLLOW_shift_in_bitand4132);
            	    r=shift();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: bitand, r
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
            	    // 400:22: -> ^( BITAND $bitand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:400:25: ^( BITAND $bitand $r)
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
            	    break loop82;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:1: shift : ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT315=null;
        Token RSHIFT316=null;
        Token URSHIFT317=null;
        EulangParser.factor_return r = null;

        EulangParser.factor_return factor314 = null;


        CommonTree LSHIFT315_tree=null;
        CommonTree RSHIFT316_tree=null;
        CommonTree URSHIFT317_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:6: ( ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:9: ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:9: ( factor -> factor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:11: factor
            {
            pushFollow(FOLLOW_factor_in_shift4159);
            factor314=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(factor314.getTree());


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
            // 403:25: -> factor
            {
                adaptor.addChild(root_0, stream_factor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:404:7: ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            loop83:
            do {
                int alt83=4;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt83=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt83=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt83=3;
                    }
                    break;

                }

                switch (alt83) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:404:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:404:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:404:11: LSHIFT r= factor
            	    {
            	    LSHIFT315=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift4193); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT315);

            	    pushFollow(FOLLOW_factor_in_shift4197);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, LSHIFT, shift
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
            	    // 404:29: -> ^( LSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:404:32: ^( LSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:11: RSHIFT r= factor
            	    {
            	    RSHIFT316=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift4226); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT316);

            	    pushFollow(FOLLOW_factor_in_shift4230);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: shift, r, RSHIFT
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
            	    // 405:29: -> ^( RSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:32: ^( RSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:11: URSHIFT r= factor
            	    {
            	    URSHIFT317=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift4258); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT317);

            	    pushFollow(FOLLOW_factor_in_shift4262);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, shift, URSHIFT
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
            	    // 406:30: -> ^( URSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:406:33: ^( URSHIFT $shift $r)
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
            	    break loop83;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:409:1: factor : ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS319=null;
        Token MINUS320=null;
        EulangParser.term_return r = null;

        EulangParser.term_return term318 = null;


        CommonTree PLUS319_tree=null;
        CommonTree MINUS320_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:410:5: ( ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:410:7: ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:410:7: ( term -> term )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:410:9: term
            {
            pushFollow(FOLLOW_term_in_factor4304);
            term318=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(term318.getTree());


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
            // 410:27: -> term
            {
                adaptor.addChild(root_0, stream_term.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:9: ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            loop84:
            do {
                int alt84=3;
                int LA84_0 = input.LA(1);

                if ( (LA84_0==PLUS) ) {
                    alt84=1;
                }
                else if ( (LA84_0==MINUS) && (synpred8_Eulang())) {
                    alt84=2;
                }


                switch (alt84) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:13: PLUS r= term
            	    {
            	    PLUS319=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor4337); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS319);

            	    pushFollow(FOLLOW_term_in_factor4341);
            	    r=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_term.add(r.getTree());


            	    // AST REWRITE
            	    // elements: factor, r
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
            	    // 411:33: -> ^( ADD $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:36: ^( ADD $factor $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:412:12: ( MINUS term )=> MINUS r= term
            	    {
            	    MINUS320=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor4383); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS320);

            	    pushFollow(FOLLOW_term_in_factor4387);
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
            	    // 412:49: -> ^( SUB $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:412:52: ^( SUB $factor $r)
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
            	    break loop84;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:1: term : ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR322=null;
        Token SLASH323=null;
        Token BACKSLASH324=null;
        Token PERCENT325=null;
        Token UMOD326=null;
        EulangParser.unary_return r = null;

        EulangParser.unary_return unary321 = null;


        CommonTree STAR322_tree=null;
        CommonTree SLASH323_tree=null;
        CommonTree BACKSLASH324_tree=null;
        CommonTree PERCENT325_tree=null;
        CommonTree UMOD326_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:6: ( ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:8: ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:8: ( unary -> unary )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:416:10: unary
            {
            pushFollow(FOLLOW_unary_in_term4432);
            unary321=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(unary321.getTree());


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
            // 416:33: -> unary
            {
                adaptor.addChild(root_0, stream_unary.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:9: ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            loop85:
            do {
                int alt85=6;
                int LA85_0 = input.LA(1);

                if ( (LA85_0==STAR) && (synpred9_Eulang())) {
                    alt85=1;
                }
                else if ( (LA85_0==SLASH) ) {
                    alt85=2;
                }
                else if ( (LA85_0==BACKSLASH) ) {
                    alt85=3;
                }
                else if ( (LA85_0==PERCENT) ) {
                    alt85=4;
                }
                else if ( (LA85_0==UMOD) ) {
                    alt85=5;
                }


                switch (alt85) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:11: ( STAR unary )=> STAR r= unary
            	    {
            	    STAR322=(Token)match(input,STAR,FOLLOW_STAR_in_term4476); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR322);

            	    pushFollow(FOLLOW_unary_in_term4480);
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
            	    // 417:52: -> ^( MUL $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:55: ^( MUL $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:418:11: SLASH r= unary
            	    {
            	    SLASH323=(Token)match(input,SLASH,FOLLOW_SLASH_in_term4516); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH323);

            	    pushFollow(FOLLOW_unary_in_term4520);
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
            	    // 418:36: -> ^( DIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:418:39: ^( DIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:419:11: BACKSLASH r= unary
            	    {
            	    BACKSLASH324=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_term4555); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BACKSLASH.add(BACKSLASH324);

            	    pushFollow(FOLLOW_unary_in_term4559);
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
            	    // 419:40: -> ^( UDIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:419:43: ^( UDIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:11: PERCENT r= unary
            	    {
            	    PERCENT325=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_term4594); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT325);

            	    pushFollow(FOLLOW_unary_in_term4598);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, term
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
            	    // 420:38: -> ^( MOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:41: ^( MOD $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:421:11: UMOD r= unary
            	    {
            	    UMOD326=(Token)match(input,UMOD,FOLLOW_UMOD_in_term4633); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UMOD.add(UMOD326);

            	    pushFollow(FOLLOW_unary_in_term4637);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, UMOD, r
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
            	    // 421:35: -> ^( UMOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:421:38: ^( UMOD $term $r)
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
            	    break loop85;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:1: unary : ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS328=null;
        Token TILDE329=null;
        EulangParser.unary_return u = null;

        EulangParser.atom_return atom327 = null;


        CommonTree MINUS328_tree=null;
        CommonTree TILDE329_tree=null;
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:6: ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) )
            int alt86=3;
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
                alt86=1;
                }
                break;
            case MINUS:
                {
                alt86=2;
                }
                break;
            case TILDE:
                {
                alt86=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 86, 0, input);

                throw nvae;
            }

            switch (alt86) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:11: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:11: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:13: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary4714);
                    atom327=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom327.getTree());


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
                    // 426:25: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:427:9: MINUS u= unary
                    {
                    MINUS328=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary4745); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS328);

                    pushFollow(FOLLOW_unary_in_unary4749);
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
                    // 427:23: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:427:26: ^( NEG $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:428:9: TILDE u= unary
                    {
                    TILDE329=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary4769); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE329);

                    pushFollow(FOLLOW_unary_in_unary4773);
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
                    // 428:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:428:30: ^( INV $u)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER330=null;
        Token FALSE331=null;
        Token TRUE332=null;
        Token CHAR_LITERAL333=null;
        Token STRING_LITERAL334=null;
        Token NIL335=null;
        Token STAR336=null;
        Token LPAREN340=null;
        Token RPAREN342=null;
        EulangParser.funcCall_return f = null;

        EulangParser.funcCall_return funcCall337 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef338 = null;

        EulangParser.tuple_return tuple339 = null;

        EulangParser.assignExpr_return assignExpr341 = null;

        EulangParser.code_return code343 = null;

        EulangParser.macro_return macro344 = null;


        CommonTree NUMBER330_tree=null;
        CommonTree FALSE331_tree=null;
        CommonTree TRUE332_tree=null;
        CommonTree CHAR_LITERAL333_tree=null;
        CommonTree STRING_LITERAL334_tree=null;
        CommonTree NIL335_tree=null;
        CommonTree STAR336_tree=null;
        CommonTree LPAREN340_tree=null;
        CommonTree RPAREN342_tree=null;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:6: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro )
            int alt87=13;
            alt87 = dfa87.predict(input);
            switch (alt87) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:431:7: NUMBER
                    {
                    NUMBER330=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom4801); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER330);



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
                    // 431:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:431:42: ^( LIT NUMBER )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:9: FALSE
                    {
                    FALSE331=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom4844); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE331);



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
                    // 432:39: -> ^( LIT FALSE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:42: ^( LIT FALSE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:433:9: TRUE
                    {
                    TRUE332=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom4886); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE332);



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
                    // 433:39: -> ^( LIT TRUE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:433:42: ^( LIT TRUE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:434:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL333=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom4929); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL333);



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
                    // 434:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:434:42: ^( LIT CHAR_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:435:9: STRING_LITERAL
                    {
                    STRING_LITERAL334=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom4964); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL334);



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
                    // 435:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:435:42: ^( LIT STRING_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:436:9: NIL
                    {
                    NIL335=(Token)match(input,NIL,FOLLOW_NIL_in_atom4997); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL335);



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
                    // 436:38: -> ^( LIT NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:436:41: ^( LIT NIL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:437:9: ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall
                    {
                    STAR336=(Token)match(input,STAR,FOLLOW_STAR_in_atom5051); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STAR.add(STAR336);

                    pushFollow(FOLLOW_funcCall_in_atom5055);
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
                    // 437:57: -> ^( INLINE $f)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:437:60: ^( INLINE $f)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:9: ( idOrScopeRef LPAREN )=> funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom5084);
                    funcCall337=funcCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_funcCall.add(funcCall337.getTree());


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
                    // 438:46: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:441:9: idOrScopeRef
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_atom5110);
                    idOrScopeRef338=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef338.getTree());


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
                    // 441:39: -> idOrScopeRef
                    {
                        adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:442:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_atom5149);
                    tuple339=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple339.getTree());


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
                    // 442:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:443:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN340=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom5188); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN340);

                    pushFollow(FOLLOW_assignExpr_in_atom5190);
                    assignExpr341=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr341.getTree());
                    RPAREN342=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom5192); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN342);



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
                    // 443:48: -> assignExpr
                    {
                        adaptor.addChild(root_0, stream_assignExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 12 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:444:9: code
                    {
                    pushFollow(FOLLOW_code_in_atom5220);
                    code343=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_code.add(code343.getTree());


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
                    // 444:40: -> code
                    {
                        adaptor.addChild(root_0, stream_code.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 13 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:445:9: macro
                    {
                    pushFollow(FOLLOW_macro_in_atom5263);
                    macro344=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_macro.add(macro344.getTree());


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
                    // 445:41: -> macro
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:448:1: idOrScopeRef : ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID345=null;
        Token PERIOD346=null;
        Token ID347=null;
        Token ID348=null;
        Token PERIOD349=null;
        Token ID350=null;
        EulangParser.colons_return c = null;


        CommonTree ID345_tree=null;
        CommonTree PERIOD346_tree=null;
        CommonTree ID347_tree=null;
        CommonTree ID348_tree=null;
        CommonTree PERIOD349_tree=null;
        CommonTree ID350_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:448:14: ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) )
            int alt90=2;
            int LA90_0 = input.LA(1);

            if ( (LA90_0==ID) ) {
                alt90=1;
            }
            else if ( (LA90_0==COLON||LA90_0==COLONS) ) {
                alt90=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 90, 0, input);

                throw nvae;
            }
            switch (alt90) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:448:16: ID ( PERIOD ID )*
                    {
                    ID345=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5309); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID345);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:448:19: ( PERIOD ID )*
                    loop88:
                    do {
                        int alt88=2;
                        int LA88_0 = input.LA(1);

                        if ( (LA88_0==PERIOD) ) {
                            alt88=1;
                        }


                        switch (alt88) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:448:21: PERIOD ID
                    	    {
                    	    PERIOD346=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef5313); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD346);

                    	    ID347=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5315); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID347);


                    	    }
                    	    break;

                    	default :
                    	    break loop88;
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
                    // 448:35: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:448:38: ^( IDREF ( ID )+ )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:453:9: c= colons ID ( PERIOD ID )*
                    {
                    pushFollow(FOLLOW_colons_in_idOrScopeRef5370);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID348=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5372); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID348);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:453:21: ( PERIOD ID )*
                    loop89:
                    do {
                        int alt89=2;
                        int LA89_0 = input.LA(1);

                        if ( (LA89_0==PERIOD) ) {
                            alt89=1;
                        }


                        switch (alt89) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:453:23: PERIOD ID
                    	    {
                    	    PERIOD349=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef5376); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD349);

                    	    ID350=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef5378); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID350);


                    	    }
                    	    break;

                    	default :
                    	    break loop89;
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
                    // 453:37: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:453:40: ^( IDREF ( ID )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:456:1: colons : ( COLON | COLONS )+ ;
    public final EulangParser.colons_return colons() throws RecognitionException {
        EulangParser.colons_return retval = new EulangParser.colons_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set351=null;

        CommonTree set351_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:456:8: ( ( COLON | COLONS )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:456:10: ( COLON | COLONS )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:456:10: ( COLON | COLONS )+
            int cnt91=0;
            loop91:
            do {
                int alt91=2;
                int LA91_0 = input.LA(1);

                if ( (LA91_0==COLON||LA91_0==COLONS) ) {
                    alt91=1;
                }


                switch (alt91) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            	    {
            	    set351=(Token)input.LT(1);
            	    if ( input.LA(1)==COLON||input.LA(1)==COLONS ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set351));
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
            	    if ( cnt91 >= 1 ) break loop91;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(91, input);
                        throw eee;
                }
                cnt91++;
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
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:3: ()
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:3: 
        {
        }
    }
    // $ANTLR end synpred5_Eulang

    // $ANTLR start synpred6_Eulang
    public final void synpred6_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:5: ( argdefsWithTypes )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:5: argdefsWithTypes
        {
        pushFollow(FOLLOW_argdefsWithTypes_in_synpred6_Eulang1059);
        argdefsWithTypes();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Eulang

    // $ANTLR start synpred7_Eulang
    public final void synpred7_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:5: ( ( argdefWithType )? )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:5: ( argdefWithType )?
        {
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:5: ( argdefWithType )?
        int alt92=2;
        int LA92_0 = input.LA(1);

        if ( (LA92_0==MACRO||LA92_0==ID) ) {
            alt92=1;
        }
        switch (alt92) {
            case 1 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:5: argdefWithType
                {
                pushFollow(FOLLOW_argdefWithType_in_synpred7_Eulang1066);
                argdefWithType();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred7_Eulang

    // $ANTLR start synpred8_Eulang
    public final void synpred8_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:412:12: ( MINUS term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:412:14: MINUS term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred8_Eulang4376); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred8_Eulang4378);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Eulang

    // $ANTLR start synpred9_Eulang
    public final void synpred9_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:11: ( STAR unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:13: STAR unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred9_Eulang4469); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred9_Eulang4471);
        unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Eulang

    // $ANTLR start synpred10_Eulang
    public final void synpred10_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:437:9: ( STAR idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:437:11: STAR idOrScopeRef LPAREN
        {
        match(input,STAR,FOLLOW_STAR_in_synpred10_Eulang5042); if (state.failed) return ;
        pushFollow(FOLLOW_idOrScopeRef_in_synpred10_Eulang5044);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred10_Eulang5046); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Eulang

    // $ANTLR start synpred11_Eulang
    public final void synpred11_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:9: ( idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:438:10: idOrScopeRef LPAREN
        {
        pushFollow(FOLLOW_idOrScopeRef_in_synpred11_Eulang5076);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred11_Eulang5078); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Eulang

    // $ANTLR start synpred12_Eulang
    public final void synpred12_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:442:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:442:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred12_Eulang5143);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Eulang

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
    public final boolean synpred11_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred11_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred10_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred10_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred12_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred12_Eulang_fragment(); // can never throw exception
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
    protected DFA52 dfa52 = new DFA52(this);
    protected DFA56 dfa56 = new DFA56(this);
    protected DFA57 dfa57 = new DFA57(this);
    protected DFA87 dfa87 = new DFA87(this);
    static final String DFA4_eotS =
        "\21\uffff";
    static final String DFA4_eofS =
        "\21\uffff";
    static final String DFA4_minS =
        "\1\6\1\uffff\1\6\2\uffff\1\56\1\55\2\uffff\1\6\1\0\3\uffff\1\56"+
        "\1\6\1\0";
    static final String DFA4_maxS =
        "\1\152\1\uffff\1\152\2\uffff\1\151\1\71\2\uffff\1\152\1\0\3\uffff"+
        "\1\151\1\152\1\0";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\1\4\2\uffff\2\2\2\uffff\3\2\3\uffff";
    static final String DFA4_specialS =
        "\2\uffff\1\0\2\uffff\1\6\1\3\3\uffff\1\4\3\uffff\1\2\1\1\1\5}>";
    static final String[] DFA4_transitionS = {
            "\2\4\45\uffff\1\4\2\uffff\1\4\1\uffff\1\3\2\uffff\1\1\3\uffff"+
            "\1\2\2\uffff\1\4\17\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff"+
            "\6\4\1\uffff\1\4",
            "",
            "\1\4\1\6\45\uffff\1\5\2\uffff\1\4\10\uffff\1\4\1\10\1\7\1\4"+
            "\17\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1"+
            "\4",
            "",
            "",
            "\1\4\1\14\1\13\3\uffff\1\11\4\uffff\1\4\1\12\1\7\1\uffff\3"+
            "\4\3\uffff\1\4\14\uffff\1\4\1\uffff\21\4\6\uffff\1\4",
            "\1\15\7\uffff\1\4\3\uffff\1\4",
            "",
            "",
            "\2\4\45\uffff\1\16\2\uffff\1\4\10\uffff\1\4\2\uffff\1\4\17"+
            "\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\4",
            "\1\uffff",
            "",
            "",
            "",
            "\1\4\1\14\1\13\3\uffff\1\17\4\uffff\1\4\1\20\1\7\1\uffff\3"+
            "\4\3\uffff\1\4\14\uffff\1\4\1\uffff\21\4\6\uffff\1\4",
            "\2\4\45\uffff\1\16\2\uffff\1\4\10\uffff\1\4\1\10\1\7\1\4\17"+
            "\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\4",
            "\1\uffff"
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
                    case 1 : 
                        int LA4_15 = input.LA(1);

                         
                        int index4_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_15==ID) ) {s = 14;}

                        else if ( (LA4_15==ARROW) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA4_15==RPAREN) && (synpred4_Eulang())) {s = 8;}

                        else if ( ((LA4_15>=CODE && LA4_15<=MACRO)||LA4_15==COLON||LA4_15==LPAREN||LA4_15==NIL||LA4_15==IF||LA4_15==NOT||(LA4_15>=MINUS && LA4_15<=STAR)||(LA4_15>=TILDE && LA4_15<=STRING_LITERAL)||LA4_15==COLONS) ) {s = 4;}

                         
                        input.seek(index4_15);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA4_14 = input.LA(1);

                         
                        int index4_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_14==COMMA) ) {s = 15;}

                        else if ( (LA4_14==ARROW) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA4_14==RPAREN) ) {s = 16;}

                        else if ( (LA4_14==EQUALS||LA4_14==LPAREN||(LA4_14>=QUESTION && LA4_14<=PLUS)||LA4_14==AND||LA4_14==OR||(LA4_14>=COMPEQ && LA4_14<=UMOD)||LA4_14==PERIOD) ) {s = 4;}

                        else if ( (LA4_14==COLON) && (synpred4_Eulang())) {s = 11;}

                        else if ( (LA4_14==SEMI) && (synpred4_Eulang())) {s = 12;}

                         
                        input.seek(index4_14);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA4_6 = input.LA(1);

                         
                        int index4_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_6==ID) && (synpred4_Eulang())) {s = 13;}

                        else if ( (LA4_6==LBRACE||LA4_6==LPAREN) ) {s = 4;}

                         
                        input.seek(index4_6);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA4_10 = input.LA(1);

                         
                        int index4_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Eulang()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index4_10);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA4_16 = input.LA(1);

                         
                        int index4_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Eulang()) ) {s = 13;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index4_16);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA4_5 = input.LA(1);

                         
                        int index4_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_5==EQUALS||LA4_5==LPAREN||(LA4_5>=QUESTION && LA4_5<=PLUS)||LA4_5==AND||LA4_5==OR||(LA4_5>=COMPEQ && LA4_5<=UMOD)||LA4_5==PERIOD) ) {s = 4;}

                        else if ( (LA4_5==COMMA) ) {s = 9;}

                        else if ( (LA4_5==RPAREN) ) {s = 10;}

                        else if ( (LA4_5==COLON) && (synpred4_Eulang())) {s = 11;}

                        else if ( (LA4_5==ARROW) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA4_5==SEMI) && (synpred4_Eulang())) {s = 12;}

                         
                        input.seek(index4_5);
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
        "\41\uffff";
    static final String DFA42_eofS =
        "\1\uffff\1\4\14\uffff\2\4\11\uffff\1\4\1\uffff\1\4\5\uffff";
    static final String DFA42_minS =
        "\1\6\1\56\1\6\1\55\5\uffff\2\55\1\uffff\1\56\1\55\3\56\1\55\1\6"+
        "\1\56\1\55\2\56\2\55\1\56\1\55\4\56\1\55\1\56";
    static final String DFA42_maxS =
        "\1\152\1\151\2\152\5\uffff\1\55\1\152\1\uffff\1\151\1\152\3\151"+
        "\1\55\1\152\1\151\1\55\2\151\1\152\1\55\1\151\1\55\1\142\3\151\1"+
        "\55\1\151";
    static final String DFA42_acceptS =
        "\4\uffff\1\3\1\4\1\5\1\6\1\1\2\uffff\1\2\25\uffff";
    static final String DFA42_specialS =
        "\41\uffff}>";
    static final String[] DFA42_transitionS = {
            "\2\4\40\uffff\1\6\4\uffff\1\1\2\uffff\1\3\4\uffff\1\5\1\uffff"+
            "\1\7\1\uffff\1\2\2\uffff\1\4\3\uffff\3\7\11\uffff\1\4\4\uffff"+
            "\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\3",
            "\1\13\1\4\2\10\2\uffff\1\12\1\uffff\1\4\2\uffff\1\4\3\uffff"+
            "\3\4\1\uffff\1\4\1\uffff\1\4\14\uffff\1\4\1\uffff\21\4\6\uffff"+
            "\1\11",
            "\2\4\45\uffff\1\14\2\uffff\1\15\10\uffff\1\4\2\uffff\1\4\17"+
            "\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\15",
            "\1\16\2\uffff\1\3\71\uffff\1\3",
            "",
            "",
            "",
            "",
            "",
            "\1\17",
            "\1\20\2\uffff\1\13\71\uffff\1\13",
            "",
            "\1\4\5\uffff\1\22\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\21",
            "\1\23\2\uffff\1\15\71\uffff\1\15",
            "\1\13\1\4\4\uffff\1\13\1\uffff\1\4\2\uffff\1\4\3\uffff\3\4"+
            "\1\uffff\1\4\1\uffff\1\4\14\uffff\1\4\1\uffff\21\4\6\uffff\1"+
            "\24",
            "\1\13\1\4\4\uffff\1\13\1\uffff\1\4\2\uffff\1\4\3\uffff\3\4"+
            "\1\uffff\1\4\1\uffff\1\4\14\uffff\1\4\1\uffff\21\4\6\uffff\1"+
            "\11",
            "\1\13\1\uffff\2\10\2\uffff\1\12\64\uffff\1\13",
            "\1\25",
            "\2\4\45\uffff\1\26\2\uffff\1\27\10\uffff\1\4\2\uffff\1\4\17"+
            "\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\27",
            "\1\4\5\uffff\1\22\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\30",
            "\1\31",
            "\1\4\5\uffff\1\22\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\21",
            "\1\4\5\uffff\1\22\4\uffff\1\4\1\33\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\32",
            "\1\34\2\uffff\1\27\71\uffff\1\27",
            "\1\35",
            "\1\13\1\4\4\uffff\1\13\1\uffff\1\4\2\uffff\1\4\3\uffff\3\4"+
            "\1\uffff\1\4\1\uffff\1\4\14\uffff\1\4\1\uffff\21\4\6\uffff\1"+
            "\24",
            "\1\36",
            "\1\13\1\4\2\10\4\uffff\1\4\6\uffff\3\4\1\uffff\1\4\1\uffff"+
            "\1\4\14\uffff\1\4\1\uffff\21\4",
            "\1\4\5\uffff\1\22\4\uffff\1\4\1\33\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\37",
            "\1\4\5\uffff\1\22\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\30",
            "\1\4\5\uffff\1\22\4\uffff\1\4\1\33\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\32",
            "\1\40",
            "\1\4\5\uffff\1\22\4\uffff\1\4\1\33\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\37"
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
            return "205:1: codeStmtExpr : ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );";
        }
    }
    static final String DFA52_eotS =
        "\33\uffff";
    static final String DFA52_eofS =
        "\33\uffff";
    static final String DFA52_minS =
        "\1\55\1\60\1\55\2\uffff\1\55\1\64\1\55\1\60\2\55\1\64\2\uffff\2"+
        "\64\3\55\1\60\3\64\2\uffff\1\55\1\64";
    static final String DFA52_maxS =
        "\1\71\1\64\1\152\2\uffff\1\55\1\151\1\152\1\64\1\55\1\152\1\151"+
        "\2\uffff\2\151\1\152\2\55\1\61\3\151\2\uffff\1\55\1\151";
    static final String DFA52_acceptS =
        "\3\uffff\1\1\1\3\7\uffff\1\6\1\5\11\uffff\1\4\1\2\2\uffff";
    static final String DFA52_specialS =
        "\33\uffff}>";
    static final String[] DFA52_transitionS = {
            "\1\1\13\uffff\1\2",
            "\1\4\1\3\2\uffff\1\5",
            "\1\6\2\uffff\1\7\71\uffff\1\7",
            "",
            "",
            "\1\10",
            "\1\12\64\uffff\1\11",
            "\1\13\2\uffff\1\7\71\uffff\1\7",
            "\1\14\1\15\2\uffff\1\5",
            "\1\16",
            "\1\17\2\uffff\1\20\71\uffff\1\20",
            "\1\12\64\uffff\1\21",
            "",
            "",
            "\1\12\64\uffff\1\11",
            "\1\12\5\uffff\1\23\56\uffff\1\22",
            "\1\24\2\uffff\1\20\71\uffff\1\20",
            "\1\25",
            "\1\26",
            "\1\27\1\30",
            "\1\12\5\uffff\1\23\56\uffff\1\31",
            "\1\12\64\uffff\1\21",
            "\1\12\5\uffff\1\23\56\uffff\1\22",
            "",
            "",
            "\1\32",
            "\1\12\5\uffff\1\23\56\uffff\1\31"
    };

    static final short[] DFA52_eot = DFA.unpackEncodedString(DFA52_eotS);
    static final short[] DFA52_eof = DFA.unpackEncodedString(DFA52_eofS);
    static final char[] DFA52_min = DFA.unpackEncodedStringToUnsignedChars(DFA52_minS);
    static final char[] DFA52_max = DFA.unpackEncodedStringToUnsignedChars(DFA52_maxS);
    static final short[] DFA52_accept = DFA.unpackEncodedString(DFA52_acceptS);
    static final short[] DFA52_special = DFA.unpackEncodedString(DFA52_specialS);
    static final short[][] DFA52_transition;

    static {
        int numStates = DFA52_transitionS.length;
        DFA52_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA52_transition[i] = DFA.unpackEncodedString(DFA52_transitionS[i]);
        }
    }

    class DFA52 extends DFA {

        public DFA52(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 52;
            this.eot = DFA52_eot;
            this.eof = DFA52_eof;
            this.min = DFA52_min;
            this.max = DFA52_max;
            this.accept = DFA52_accept;
            this.special = DFA52_special;
            this.transition = DFA52_transition;
        }
        public String getDescription() {
            return "215:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) | ID ( COMMA ID )+ COLON_EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( assignExpr )+ ) ) | ID ( COMMA ID )+ COLON type ( EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* )? -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( assignExpr )+ ) )? ) );";
        }
    }
    static final String DFA56_eotS =
        "\13\uffff";
    static final String DFA56_eofS =
        "\13\uffff";
    static final String DFA56_minS =
        "\1\55\1\56\1\55\1\uffff\1\55\2\uffff\2\56\1\55\1\56";
    static final String DFA56_maxS =
        "\1\152\1\151\1\152\1\uffff\1\55\2\uffff\2\151\1\55\1\151";
    static final String DFA56_acceptS =
        "\3\uffff\1\2\1\uffff\1\1\1\3\4\uffff";
    static final String DFA56_specialS =
        "\13\uffff}>";
    static final String[] DFA56_transitionS = {
            "\1\1\2\uffff\1\2\10\uffff\1\3\60\uffff\1\2",
            "\1\5\5\uffff\1\6\64\uffff\1\4",
            "\1\7\2\uffff\1\2\71\uffff\1\2",
            "",
            "\1\10",
            "",
            "",
            "\1\5\5\uffff\1\6\64\uffff\1\11",
            "\1\5\5\uffff\1\6\64\uffff\1\4",
            "\1\12",
            "\1\5\5\uffff\1\6\64\uffff\1\11"
    };

    static final short[] DFA56_eot = DFA.unpackEncodedString(DFA56_eotS);
    static final short[] DFA56_eof = DFA.unpackEncodedString(DFA56_eofS);
    static final char[] DFA56_min = DFA.unpackEncodedStringToUnsignedChars(DFA56_minS);
    static final char[] DFA56_max = DFA.unpackEncodedStringToUnsignedChars(DFA56_maxS);
    static final short[] DFA56_accept = DFA.unpackEncodedString(DFA56_acceptS);
    static final short[] DFA56_special = DFA.unpackEncodedString(DFA56_specialS);
    static final short[][] DFA56_transition;

    static {
        int numStates = DFA56_transitionS.length;
        DFA56_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA56_transition[i] = DFA.unpackEncodedString(DFA56_transitionS[i]);
        }
    }

    class DFA56 extends DFA {

        public DFA56(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 56;
            this.eot = DFA56_eot;
            this.eof = DFA56_eof;
            this.min = DFA56_min;
            this.max = DFA56_max;
            this.accept = DFA56_accept;
            this.special = DFA56_special;
            this.transition = DFA56_transition;
        }
        public String getDescription() {
            return "228:1: assignStmt : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | idOrScopeRef ( COMMA idOrScopeRef )+ EQUALS ( PLUS )? assignExpr ( COMMA assignExpr )* -> ^( ASSIGN ^( LIST ( idOrScopeRef )+ ) ( PLUS )? ^( LIST ( assignExpr )+ ) ) );";
        }
    }
    static final String DFA57_eotS =
        "\34\uffff";
    static final String DFA57_eofS =
        "\1\uffff\1\4\5\uffff\1\4\2\uffff\1\4\4\uffff\1\4\5\uffff\1\4\6\uffff";
    static final String DFA57_minS =
        "\1\6\1\56\1\55\1\6\1\uffff\1\55\1\uffff\2\56\1\55\1\56\2\55\1\6"+
        "\4\56\3\55\4\56\1\uffff\1\55\1\56";
    static final String DFA57_maxS =
        "\1\152\1\151\2\152\1\uffff\1\55\1\uffff\2\151\1\152\1\151\2\55\1"+
        "\152\4\151\1\152\2\55\1\142\3\151\1\uffff\1\55\1\151";
    static final String DFA57_acceptS =
        "\4\uffff\1\3\1\uffff\1\1\22\uffff\1\2\2\uffff";
    static final String DFA57_specialS =
        "\34\uffff}>";
    static final String[] DFA57_transitionS = {
            "\2\4\45\uffff\1\1\2\uffff\1\2\10\uffff\1\3\2\uffff\1\4\17\uffff"+
            "\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\2",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\5\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\21\4"+
            "\6\uffff\1\5",
            "\1\7\2\uffff\1\2\71\uffff\1\2",
            "\2\4\45\uffff\1\10\2\uffff\1\11\10\uffff\1\4\2\uffff\1\4\17"+
            "\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\11",
            "",
            "\1\12",
            "",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\5\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\21\4"+
            "\6\uffff\1\13",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\14",
            "\1\16\2\uffff\1\11\71\uffff\1\11",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\5\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\21\4"+
            "\6\uffff\1\5",
            "\1\17",
            "\1\20",
            "\2\4\45\uffff\1\21\2\uffff\1\22\10\uffff\1\4\2\uffff\1\4\17"+
            "\uffff\1\4\4\uffff\1\4\13\uffff\2\4\4\uffff\6\4\1\uffff\1\22",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\23",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\5\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\21\4"+
            "\6\uffff\1\13",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\14",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\24",
            "\1\26\2\uffff\1\22\71\uffff\1\22",
            "\1\27",
            "\1\30",
            "\1\31\1\4\3\uffff\2\4\1\uffff\1\4\3\uffff\2\4\1\uffff\5\4\1"+
            "\uffff\1\4\2\uffff\2\4\1\uffff\1\4\3\uffff\4\4\1\uffff\21\4",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\32",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\3\4\3\uffff\1\4\14\uffff"+
            "\1\4\1\uffff\21\4\6\uffff\1\23",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\24",
            "",
            "\1\33",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\3\4\3\uffff\1\4"+
            "\14\uffff\1\4\1\uffff\21\4\6\uffff\1\32"
    };

    static final short[] DFA57_eot = DFA.unpackEncodedString(DFA57_eotS);
    static final short[] DFA57_eof = DFA.unpackEncodedString(DFA57_eofS);
    static final char[] DFA57_min = DFA.unpackEncodedStringToUnsignedChars(DFA57_minS);
    static final char[] DFA57_max = DFA.unpackEncodedStringToUnsignedChars(DFA57_maxS);
    static final short[] DFA57_accept = DFA.unpackEncodedString(DFA57_acceptS);
    static final short[] DFA57_special = DFA.unpackEncodedString(DFA57_specialS);
    static final short[][] DFA57_transition;

    static {
        int numStates = DFA57_transitionS.length;
        DFA57_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA57_transition[i] = DFA.unpackEncodedString(DFA57_transitionS[i]);
        }
    }

    class DFA57 extends DFA {

        public DFA57(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 57;
            this.eot = DFA57_eot;
            this.eof = DFA57_eof;
            this.min = DFA57_min;
            this.max = DFA57_max;
            this.accept = DFA57_accept;
            this.special = DFA57_special;
            this.transition = DFA57_transition;
        }
        public String getDescription() {
            return "234:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr );";
        }
    }
    static final String DFA87_eotS =
        "\21\uffff";
    static final String DFA87_eofS =
        "\21\uffff";
    static final String DFA87_minS =
        "\1\6\7\uffff\3\0\6\uffff";
    static final String DFA87_maxS =
        "\1\152\7\uffff\3\0\6\uffff";
    static final String DFA87_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\3\uffff\1\14\1\15\1\10\1\11"+
        "\1\12\1\13";
    static final String DFA87_specialS =
        "\1\0\7\uffff\1\1\1\2\1\3\6\uffff}>";
    static final String[] DFA87_transitionS = {
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

    static final short[] DFA87_eot = DFA.unpackEncodedString(DFA87_eotS);
    static final short[] DFA87_eof = DFA.unpackEncodedString(DFA87_eofS);
    static final char[] DFA87_min = DFA.unpackEncodedStringToUnsignedChars(DFA87_minS);
    static final char[] DFA87_max = DFA.unpackEncodedStringToUnsignedChars(DFA87_maxS);
    static final short[] DFA87_accept = DFA.unpackEncodedString(DFA87_acceptS);
    static final short[] DFA87_special = DFA.unpackEncodedString(DFA87_specialS);
    static final short[][] DFA87_transition;

    static {
        int numStates = DFA87_transitionS.length;
        DFA87_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA87_transition[i] = DFA.unpackEncodedString(DFA87_transitionS[i]);
        }
    }

    class DFA87 extends DFA {

        public DFA87(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 87;
            this.eot = DFA87_eot;
            this.eof = DFA87_eof;
            this.min = DFA87_min;
            this.max = DFA87_max;
            this.accept = DFA87_accept;
            this.special = DFA87_special;
            this.transition = DFA87_transition;
        }
        public String getDescription() {
            return "430:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA87_0 = input.LA(1);

                         
                        int index87_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA87_0==NUMBER) ) {s = 1;}

                        else if ( (LA87_0==FALSE) ) {s = 2;}

                        else if ( (LA87_0==TRUE) ) {s = 3;}

                        else if ( (LA87_0==CHAR_LITERAL) ) {s = 4;}

                        else if ( (LA87_0==STRING_LITERAL) ) {s = 5;}

                        else if ( (LA87_0==NIL) ) {s = 6;}

                        else if ( (LA87_0==STAR) && (synpred10_Eulang())) {s = 7;}

                        else if ( (LA87_0==ID) ) {s = 8;}

                        else if ( (LA87_0==COLON||LA87_0==COLONS) ) {s = 9;}

                        else if ( (LA87_0==LPAREN) ) {s = 10;}

                        else if ( (LA87_0==CODE) ) {s = 11;}

                        else if ( (LA87_0==MACRO) ) {s = 12;}

                         
                        input.seek(index87_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA87_8 = input.LA(1);

                         
                        int index87_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index87_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA87_9 = input.LA(1);

                         
                        int index87_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 13;}

                        else if ( (true) ) {s = 14;}

                         
                        input.seek(index87_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA87_10 = input.LA(1);

                         
                        int index87_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred12_Eulang()) ) {s = 15;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index87_10);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 87, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog304 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts335 = new BitSet(new long[]{0x12212000000000C2L,0x000005F860021000L});
    public static final BitSet FOLLOW_ID_in_toplevelstat376 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat378 = new BitSet(new long[]{0x12252000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat380 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat414 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_toplevelstat416 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_toplevelstat418 = new BitSet(new long[]{0x0000C00000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat421 = new BitSet(new long[]{0x12252000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat423 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat431 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat462 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelstat464 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
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
    public static final BitSet FOLLOW_LBRACE_in_xscope687 = new BitSet(new long[]{0x12212000000000C0L,0x000005F860021000L});
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
    public static final BitSet FOLLOW_LBRACKET_in_list843 = new BitSet(new long[]{0x122D2000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_listitems_in_list845 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_list847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems877 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems881 = new BitSet(new long[]{0x12252000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_listitem_in_listitems883 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem914 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_code932 = new BitSet(new long[]{0x0220000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_code936 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_argdefs_in_code938 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_code940 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_code943 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_code949 = new BitSet(new long[]{0x12E12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codestmtlist_in_code951 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_code953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_macro988 = new BitSet(new long[]{0x0220000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_macro992 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_argdefs_in_macro994 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_macro996 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_macro999 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_macro1005 = new BitSet(new long[]{0x12E12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codestmtlist_in_macro1007 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_macro1009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_argdefs1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefs1066 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithNames_in_argdefs1075 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1091 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1095 = new BitSet(new long[]{0x0000200000000080L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1097 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1101 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1130 = new BitSet(new long[]{0x0011000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefWithType1133 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1135 = new BitSet(new long[]{0x0011000000000002L});
    public static final BitSet FOLLOW_COLON_in_argdefWithType1140 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_argdefWithType1142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_argdefWithType1167 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1169 = new BitSet(new long[]{0x0011400000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefWithType1172 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1174 = new BitSet(new long[]{0x0011400000000002L});
    public static final BitSet FOLLOW_COLON_in_argdefWithType1179 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_argdefWithType1181 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_argdefWithType1186 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_argdefWithType1190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1226 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1230 = new BitSet(new long[]{0x0010200000000080L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1232 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithName1258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto1281 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_argdefs_in_proto1283 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_proto1285 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_proto1288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns1330 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_xreturns1332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns1347 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_argtuple_in_xreturns1349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns1369 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_NIL_in_xreturns1371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_argtuple1401 = new BitSet(new long[]{0x2011200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_tupleargdefs_in_argtuple1403 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_argtuple1405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs1427 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_tupleargdefs1431 = new BitSet(new long[]{0x2011200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs1433 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_type_in_tupleargdef1478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_tupleargdef1491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_type1550 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_AMP_in_type1565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_type1591 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_proto_in_type1593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1621 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist1624 = new BitSet(new long[]{0x12A1A100000000C2L,0x000005F860021807L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1626 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_labelStmt_in_codeStmt1675 = new BitSet(new long[]{0x12A12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt1677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt1698 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmtExpr1717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_codeStmtExpr1734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_codeStmtExpr1758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blockStmt_in_codeStmtExpr1783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_codeStmtExpr1805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_controlStmt_in_codeStmtExpr1831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1854 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1856 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_varDecl1886 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1888 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1918 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl1920 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_varDecl1922 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1925 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1927 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_varDecl1951 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl1953 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_varDecl1955 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1958 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1960 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1984 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_varDecl1987 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_varDecl1989 = new BitSet(new long[]{0x0012000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1993 = new BitSet(new long[]{0x92012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_PLUS_in_varDecl1995 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1998 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_varDecl2001 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl2003 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl2047 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_varDecl2050 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_varDecl2052 = new BitSet(new long[]{0x0011000000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl2056 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_varDecl2058 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl2061 = new BitSet(new long[]{0x92012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_PLUS_in_varDecl2063 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl2066 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_varDecl2069 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl2071 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignStmt2133 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt2135 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt2137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignStmt2162 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt2164 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt2166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignStmt2198 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt2201 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignStmt2203 = new BitSet(new long[]{0x0010400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt2207 = new BitSet(new long[]{0x92012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_PLUS_in_assignStmt2209 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt2212 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt2215 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt2217 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignExpr2276 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr2278 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr2280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignExpr2305 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr2307 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr2309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr2341 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_doWhile_in_controlStmt2387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whileDo_in_controlStmt2391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_repeat_in_controlStmt2395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIter_in_controlStmt2399 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_doWhile2408 = new BitSet(new long[]{0x12A12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codeStmtExpr_in_doWhile2410 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_doWhile2412 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_doWhile2414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_whileDo2437 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_whileDo2439 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_DO_in_whileDo2441 = new BitSet(new long[]{0x12A12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codeStmtExpr_in_whileDo2443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REPEAT_in_repeat2468 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_repeat2470 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_DO_in_repeat2472 = new BitSet(new long[]{0x12A12100000000C0L,0x000005F860021827L});
    public static final BitSet FOLLOW_codeOrValueExpr_in_repeat2474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIter2504 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_forIds_in_forIter2506 = new BitSet(new long[]{0x0100000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_atId_in_forIter2508 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_IN_in_forIter2511 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_forIter2513 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_DO_in_forIter2515 = new BitSet(new long[]{0x12A12100000000C0L,0x000005F860021827L});
    public static final BitSet FOLLOW_codeOrValueExpr_in_forIter2517 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_forIds2550 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_AND_in_forIds2553 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_forIds2555 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_AT_in_atId2571 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_atId2573 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codeOrValueExpr2598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_breakStmt_in_codeOrValueExpr2602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_breakStmt2611 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021500L});
    public static final BitSet FOLLOW_valueCond_in_breakStmt2613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2634 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_WHEN_in_valueCond2636 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2638 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_ELSE_in_valueCond2640 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNTIL_in_valueCond2659 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2661 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_THEN_in_valueCond2663 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2665 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_ELSE_in_valueCond2667 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WITH_in_valueCond2687 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_valueCond2689 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATSIGN_in_labelStmt2740 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_labelStmt2742 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_labelStmt2744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GOTO_in_gotoStmt2780 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_gotoStmt2782 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_IF_in_gotoStmt2785 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_gotoStmt2787 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_blockStmt2824 = new BitSet(new long[]{0x12E12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codestmtlist_in_blockStmt2826 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_blockStmt2828 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tuple2851 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_tupleEntries_in_tuple2853 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_tuple2855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries2883 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_tupleEntries2886 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries2888 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_idTuple2907 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idTupleEntries_in_idTuple2909 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_idTuple2911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries2939 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_idTupleEntries2942 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries2944 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_condStar_in_rhsExpr2965 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_funcCall2986 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall2988 = new BitSet(new long[]{0x16212100000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_arglist_in_funcCall2990 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall2992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist3023 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist3027 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_arg_in_arglist3029 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist3033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg3082 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_arg3115 = new BitSet(new long[]{0x12E12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codestmtlist_in_arg3117 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_arg3119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_arg3143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WITH_in_withStmt3196 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_bindings_in_withStmt3198 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_ARROW_in_withStmt3200 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_rhsExpr_in_withStmt3204 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_ELSE_in_withStmt3207 = new BitSet(new long[]{0x12A12100000000C0L,0x000005F860021807L});
    public static final BitSet FOLLOW_codeStmtExpr_in_withStmt3211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_binding_in_bindings3239 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_AND_in_bindings3242 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_binding_in_bindings3244 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_rhsExpr_in_binding3262 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_AS_in_binding3264 = new BitSet(new long[]{0x0001200000000040L,0x0000040000000000L});
    public static final BitSet FOLLOW_type_in_binding3266 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_condStar3296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_condStar3311 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_ifExprs_in_condStar3313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_thenClause_in_ifExprs3340 = new BitSet(new long[]{0x0000000000000000L,0x000000000000C080L});
    public static final BitSet FOLLOW_elses_in_ifExprs3342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_thenClause3364 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_THEN_in_thenClause3366 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_arg_in_thenClause3370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elif_in_elses3398 = new BitSet(new long[]{0x0000000000000000L,0x000000000000C080L});
    public static final BitSet FOLLOW_elseClause_in_elses3401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELIF_in_elif3424 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_arg_in_elif3428 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_THEN_in_elif3430 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_arg_in_elif3434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELSE_in_elseClause3460 = new BitSet(new long[]{0x12212100000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_arg_in_elseClause3462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FI_in_elseClause3489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logor_in_cond3529 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond3546 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860020000L});
    public static final BitSet FOLLOW_logor_in_cond3550 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_cond3552 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860020000L});
    public static final BitSet FOLLOW_logor_in_cond3556 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_logand_in_logor3586 = new BitSet(new long[]{0x0000000000000002L,0x0000000000010000L});
    public static final BitSet FOLLOW_OR_in_logor3603 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860020000L});
    public static final BitSet FOLLOW_logand_in_logor3607 = new BitSet(new long[]{0x0000000000000002L,0x0000000000010000L});
    public static final BitSet FOLLOW_not_in_logand3638 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_AND_in_logand3654 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860020000L});
    public static final BitSet FOLLOW_not_in_logand3658 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_comp_in_not3704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_not3720 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_comp_in_not3724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bitor_in_comp3758 = new BitSet(new long[]{0x0000000000000002L,0x0000000000FC0000L});
    public static final BitSet FOLLOW_COMPEQ_in_comp3791 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3795 = new BitSet(new long[]{0x0000000000000002L,0x0000000000FC0000L});
    public static final BitSet FOLLOW_COMPNE_in_comp3817 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3821 = new BitSet(new long[]{0x0000000000000002L,0x0000000000FC0000L});
    public static final BitSet FOLLOW_COMPLE_in_comp3843 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3847 = new BitSet(new long[]{0x0000000000000002L,0x0000000000FC0000L});
    public static final BitSet FOLLOW_COMPGE_in_comp3872 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3876 = new BitSet(new long[]{0x0000000000000002L,0x0000000000FC0000L});
    public static final BitSet FOLLOW_LESS_in_comp3901 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3905 = new BitSet(new long[]{0x0000000000000002L,0x0000000000FC0000L});
    public static final BitSet FOLLOW_GREATER_in_comp3931 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitor_in_comp3935 = new BitSet(new long[]{0x0000000000000002L,0x0000000000FC0000L});
    public static final BitSet FOLLOW_bitxor_in_bitor3985 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_BAR_in_bitor4013 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitxor_in_bitor4017 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_bitand_in_bitxor4043 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_CARET_in_bitxor4071 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_bitand_in_bitxor4075 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_shift_in_bitand4100 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_AMP_in_bitand4128 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_shift_in_bitand4132 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_factor_in_shift4159 = new BitSet(new long[]{0x0000000000000002L,0x000000001C000000L});
    public static final BitSet FOLLOW_LSHIFT_in_shift4193 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_factor_in_shift4197 = new BitSet(new long[]{0x0000000000000002L,0x000000001C000000L});
    public static final BitSet FOLLOW_RSHIFT_in_shift4226 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_factor_in_shift4230 = new BitSet(new long[]{0x0000000000000002L,0x000000001C000000L});
    public static final BitSet FOLLOW_URSHIFT_in_shift4258 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_factor_in_shift4262 = new BitSet(new long[]{0x0000000000000002L,0x000000001C000000L});
    public static final BitSet FOLLOW_term_in_factor4304 = new BitSet(new long[]{0x8000000000000002L,0x0000000020000000L});
    public static final BitSet FOLLOW_PLUS_in_factor4337 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_term_in_factor4341 = new BitSet(new long[]{0x8000000000000002L,0x0000000020000000L});
    public static final BitSet FOLLOW_MINUS_in_factor4383 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_term_in_factor4387 = new BitSet(new long[]{0x8000000000000002L,0x0000000020000000L});
    public static final BitSet FOLLOW_unary_in_term4432 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_STAR_in_term4476 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4480 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_SLASH_in_term4516 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4520 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_BACKSLASH_in_term4555 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4559 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_PERCENT_in_term4594 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4598 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_UMOD_in_term4633 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_term4637 = new BitSet(new long[]{0x0000000000000002L,0x00000007C0000000L});
    public static final BitSet FOLLOW_atom_in_unary4714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_unary4745 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_unary4749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary4769 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_unary4773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom4801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_atom4844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_atom4886 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom4929 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom4964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NIL_in_atom4997 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_atom5051 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_funcCall_in_atom5055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom5084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_atom5110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_atom5149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom5188 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860021000L});
    public static final BitSet FOLLOW_assignExpr_in_atom5190 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_atom5192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_atom5220 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_atom5263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5309 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef5313 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5315 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_colons_in_idOrScopeRef5370 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5372 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef5376 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef5378 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_set_in_colons5409 = new BitSet(new long[]{0x0001000000000002L,0x0000040000000000L});
    public static final BitSet FOLLOW_ID_in_synpred1_Eulang369 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_synpred1_Eulang371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred2_Eulang407 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_synpred2_Eulang409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred3_Eulang455 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_synpred3_Eulang457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred4_Eulang550 = new BitSet(new long[]{0x0400200000000000L});
    public static final BitSet FOLLOW_set_in_synpred4_Eulang552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_synpred6_Eulang1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_synpred7_Eulang1066 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred8_Eulang4376 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_term_in_synpred8_Eulang4378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred9_Eulang4469 = new BitSet(new long[]{0x12012000000000C0L,0x000005F860000000L});
    public static final BitSet FOLLOW_unary_in_synpred9_Eulang4471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred10_Eulang5042 = new BitSet(new long[]{0x0001200000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred10_Eulang5044 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred10_Eulang5046 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred11_Eulang5076 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred11_Eulang5078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred12_Eulang5143 = new BitSet(new long[]{0x0000000000000002L});

}