// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g 2010-03-22 19:41:27

package org.ejs.eulang;
import java.util.HashMap;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class EulangParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "LBRACE_LPAREN", "LBRACE_STAR", "LBRACE_STAR_LPAREN", "COLON", "COMMA", "EQUALS", "COLON_EQUALS", "COLON_COLON_EQUALS", "PLUS", "MINUS", "STAR", "SLASH", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "HASH", "EXCL", "TILDE", "AT", "AMP", "BAR", "CARET", "SEMI", "QUESTION", "COMPAND", "COMPOR", "COMPEQ", "COMPNE", "COMPGE", "COMPLE", "GREATER", "LESS", "LSHIFT", "RSHIFT", "URSHIFT", "BACKSLASH", "PERCENT", "UMOD", "RETURNS", "PERIOD", "RETURN", "FOR", "IN", "IDSUFFIX", "NUMBER", "ID", "SCOPEREF", "COLONS", "LETTERLIKE", "DIGIT", "CHAR_LITERAL", "STRING_LITERAL", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT", "SCOPE", "LIST_COMPREHENSION", "CODE", "MACRO", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "EXPR", "DEFINE_ASSIGN", "ASSIGN", "DEFINE", "LIST", "TYPE", "CALL", "INLINE", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NOT", "NEG", "INV", "LIT", "IDREF", "IDLIST", "COMPXOR"
    };
    public static final int STAR=14;
    public static final int MOD=89;
    public static final int ARGLIST=69;
    public static final int EQUALS=9;
    public static final int EXCL=23;
    public static final int NOT=90;
    public static final int EOF=-1;
    public static final int TYPE=77;
    public static final int CODE=65;
    public static final int LBRACKET=20;
    public static final int RPAREN=17;
    public static final int GREATER=37;
    public static final int STRING_LITERAL=58;
    public static final int COMPLE=36;
    public static final int CARET=28;
    public static final int LESS=38;
    public static final int RETURN=47;
    public static final int LBRACE_STAR_LPAREN=6;
    public static final int COMPAND=31;
    public static final int RBRACE=19;
    public static final int PERIOD=46;
    public static final int UMOD=44;
    public static final int INV=92;
    public static final int LSHIFT=39;
    public static final int LIT=93;
    public static final int NUMBER=51;
    public static final int LBRACE_LPAREN=4;
    public static final int UDIV=88;
    public static final int LIST=76;
    public static final int MUL=86;
    public static final int DEFINE_ASSIGN=73;
    public static final int ARGDEF=70;
    public static final int WS=60;
    public static final int BITOR=82;
    public static final int COMPXOR=96;
    public static final int STMTLIST=67;
    public static final int IDLIST=95;
    public static final int CALL=78;
    public static final int INLINE=79;
    public static final int BACKSLASH=42;
    public static final int AMP=26;
    public static final int LBRACE_STAR=5;
    public static final int LBRACE=18;
    public static final int MULTI_COMMENT=62;
    public static final int FOR=48;
    public static final int SUB=85;
    public static final int ID=52;
    public static final int DEFINE=75;
    public static final int BITAND=81;
    public static final int LPAREN=16;
    public static final int COLONS=54;
    public static final int COLON_COLON_EQUALS=11;
    public static final int AT=25;
    public static final int IDSUFFIX=50;
    public static final int SLASH=15;
    public static final int EXPR=72;
    public static final int IN=49;
    public static final int SCOPE=63;
    public static final int COMMA=8;
    public static final int SCOPEREF=53;
    public static final int BITXOR=83;
    public static final int TILDE=24;
    public static final int PLUS=12;
    public static final int SINGLE_COMMENT=61;
    public static final int DIGIT=56;
    public static final int RBRACKET=21;
    public static final int RSHIFT=40;
    public static final int RETURNS=45;
    public static final int ADD=84;
    public static final int COMPGE=35;
    public static final int PERCENT=43;
    public static final int LETTERLIKE=55;
    public static final int LIST_COMPREHENSION=64;
    public static final int COMPOR=32;
    public static final int HASH=22;
    public static final int MINUS=13;
    public static final int SEMI=29;
    public static final int REF=71;
    public static final int COLON=7;
    public static final int COLON_EQUALS=10;
    public static final int NEWLINE=59;
    public static final int QUESTION=30;
    public static final int CHAR_LITERAL=57;
    public static final int NEG=91;
    public static final int ASSIGN=74;
    public static final int URSHIFT=41;
    public static final int COMPEQ=33;
    public static final int IDREF=94;
    public static final int DIV=87;
    public static final int COND=80;
    public static final int MACRO=66;
    public static final int PROTO=68;
    public static final int COMPNE=34;
    public static final int BAR=27;

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
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g"; }


        public String getTokenErrorDisplay(Token t) {
            return '\'' + t.getText() + '\'';
        }




    public static class prog_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prog"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog252);
            toplevelstmts1=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog254); if (state.failed) return retval;

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:1: toplevelstmts : ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstat_return toplevelstat3 = null;


        RewriteRuleSubtreeStream stream_toplevelstat=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:14: ( ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:16: ( toplevelstat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:16: ( toplevelstat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==COLON||(LA1_0>=MINUS && LA1_0<=STAR)||LA1_0==LPAREN||LA1_0==LBRACE||(LA1_0>=EXCL && LA1_0<=TILDE)||(LA1_0>=NUMBER && LA1_0<=COLONS)||(LA1_0>=CHAR_LITERAL && LA1_0<=STRING_LITERAL)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:16: toplevelstat
            	    {
            	    pushFollow(FOLLOW_toplevelstat_in_toplevelstmts283);
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
            // 69:35: -> ^( STMTLIST ( toplevelstat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:38: ^( STMTLIST ( toplevelstat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:49: ( toplevelstat )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:72:1: toplevelstat : ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE_ASSIGN ID toplevelvalue ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) | xscope );
    public final EulangParser.toplevelstat_return toplevelstat() throws RecognitionException {
        EulangParser.toplevelstat_return retval = new EulangParser.toplevelstat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID4=null;
        Token EQUALS5=null;
        Token SEMI7=null;
        Token ID8=null;
        Token COLON_EQUALS9=null;
        Token SEMI11=null;
        Token SEMI13=null;
        EulangParser.toplevelvalue_return toplevelvalue6 = null;

        EulangParser.rhsExpr_return rhsExpr10 = null;

        EulangParser.rhsExpr_return rhsExpr12 = null;

        EulangParser.xscope_return xscope14 = null;


        CommonTree ID4_tree=null;
        CommonTree EQUALS5_tree=null;
        CommonTree SEMI7_tree=null;
        CommonTree ID8_tree=null;
        CommonTree COLON_EQUALS9_tree=null;
        CommonTree SEMI11_tree=null;
        CommonTree SEMI13_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_toplevelvalue=new RewriteRuleSubtreeStream(adaptor,"rule toplevelvalue");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:72:13: ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE_ASSIGN ID toplevelvalue ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) | xscope )
            int alt3=4;
            switch ( input.LA(1) ) {
            case ID:
                {
                switch ( input.LA(2) ) {
                case EQUALS:
                    {
                    alt3=1;
                    }
                    break;
                case COLON_EQUALS:
                    {
                    alt3=2;
                    }
                    break;
                case EOF:
                case COLON:
                case PLUS:
                case MINUS:
                case STAR:
                case SLASH:
                case LPAREN:
                case LBRACE:
                case RBRACE:
                case EXCL:
                case TILDE:
                case AMP:
                case BAR:
                case CARET:
                case SEMI:
                case QUESTION:
                case COMPAND:
                case COMPOR:
                case COMPEQ:
                case COMPNE:
                case COMPGE:
                case COMPLE:
                case GREATER:
                case LESS:
                case LSHIFT:
                case RSHIFT:
                case URSHIFT:
                case BACKSLASH:
                case PERCENT:
                case UMOD:
                case NUMBER:
                case ID:
                case SCOPEREF:
                case COLONS:
                case CHAR_LITERAL:
                case STRING_LITERAL:
                case COMPXOR:
                    {
                    alt3=3;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }

                }
                break;
            case COLON:
            case MINUS:
            case STAR:
            case LPAREN:
            case EXCL:
            case TILDE:
            case NUMBER:
            case SCOPEREF:
            case COLONS:
            case CHAR_LITERAL:
            case STRING_LITERAL:
                {
                alt3=3;
                }
                break;
            case LBRACE:
                {
                alt3=4;
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:72:17: ID EQUALS toplevelvalue SEMI
                    {
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat317); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID4);

                    EQUALS5=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat319); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS5);

                    pushFollow(FOLLOW_toplevelvalue_in_toplevelstat321);
                    toplevelvalue6=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat327); if (state.failed) return retval; 
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
                    // 72:51: -> ^( DEFINE_ASSIGN ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:72:54: ^( DEFINE_ASSIGN ID toplevelvalue )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE_ASSIGN, "DEFINE_ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:7: ID COLON_EQUALS rhsExpr SEMI
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat346); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID8);

                    COLON_EQUALS9=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelstat348); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS9);

                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat350);
                    rhsExpr10=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr10.getTree());
                    SEMI11=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat353); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI11);



                    // AST REWRITE
                    // elements: rhsExpr, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 73:38: -> ^( DEFINE ID rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:41: ^( DEFINE ID rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:7: rhsExpr ( SEMI )?
                    {
                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat372);
                    rhsExpr12=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr12.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:32: ( SEMI )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==SEMI) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:32: SEMI
                            {
                            SEMI13=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat391); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SEMI.add(SEMI13);


                            }
                            break;

                    }



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
                    // 74:39: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:42: ^( EXPR rhsExpr )
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
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:75:7: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelstat409);
                    xscope14=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope14.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:78:1: toplevelvalue : ( xscope | code | macro | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr );
    public final EulangParser.toplevelvalue_return toplevelvalue() throws RecognitionException {
        EulangParser.toplevelvalue_return retval = new EulangParser.toplevelvalue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.xscope_return xscope15 = null;

        EulangParser.code_return code16 = null;

        EulangParser.macro_return macro17 = null;

        EulangParser.proto_return proto18 = null;

        EulangParser.selector_return selector19 = null;

        EulangParser.rhsExpr_return rhsExpr20 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:78:15: ( xscope | code | macro | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr )
            int alt4=6;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:78:17: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelvalue422);
                    xscope15=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope15.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:79:7: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_toplevelvalue430);
                    code16=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code16.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:7: macro
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_macro_in_toplevelvalue438);
                    macro17=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro17.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:7: ( LPAREN ( RPAREN | ID ) )=> proto
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_proto_in_toplevelvalue463);
                    proto18=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, proto18.getTree());

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:82:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_toplevelvalue476);
                    selector19=selector();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selector19.getTree());

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:83:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue484);
                    rhsExpr20=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr20.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:1: selector : LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET21=null;
        Token RBRACKET23=null;
        EulangParser.selectors_return selectors22 = null;


        CommonTree LBRACKET21_tree=null;
        CommonTree RBRACKET23_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectors=new RewriteRuleSubtreeStream(adaptor,"rule selectors");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:9: ( LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:11: LBRACKET selectors RBRACKET
            {
            LBRACKET21=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector497); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET21);

            pushFollow(FOLLOW_selectors_in_selector499);
            selectors22=selectors();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectors.add(selectors22.getTree());
            RBRACKET23=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector501); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET23);



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
            // 87:42: -> ^( LIST ( selectors )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:45: ^( LIST ( selectors )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:52: ( selectors )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:1: selectors : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? ;
    public final EulangParser.selectors_return selectors() throws RecognitionException {
        EulangParser.selectors_return retval = new EulangParser.selectors_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA25=null;
        Token COMMA27=null;
        EulangParser.selectoritem_return selectoritem24 = null;

        EulangParser.selectoritem_return selectoritem26 = null;


        CommonTree COMMA25_tree=null;
        CommonTree COMMA27_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:10: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==LBRACE_LPAREN||LA7_0==LBRACE_STAR_LPAREN||LA7_0==FOR) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:13: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectors527);
                    selectoritem24=selectoritem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selectoritem24.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:26: ( COMMA selectoritem )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            int LA5_1 = input.LA(2);

                            if ( (LA5_1==LBRACE_LPAREN||LA5_1==LBRACE_STAR_LPAREN||LA5_1==FOR) ) {
                                alt5=1;
                            }


                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:28: COMMA selectoritem
                    	    {
                    	    COMMA25=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors531); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA25_tree = (CommonTree)adaptor.create(COMMA25);
                    	    adaptor.addChild(root_0, COMMA25_tree);
                    	    }
                    	    pushFollow(FOLLOW_selectoritem_in_selectors533);
                    	    selectoritem26=selectoritem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, selectoritem26.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:50: ( COMMA )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==COMMA) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:90:50: COMMA
                            {
                            COMMA27=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors538); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA27_tree = (CommonTree)adaptor.create(COMMA27);
                            adaptor.addChild(root_0, COMMA27_tree);
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
    // $ANTLR end "selectors"

    public static class selectoritem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectoritem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:1: selectoritem : ( listCompr | code | macro );
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.listCompr_return listCompr28 = null;

        EulangParser.code_return code29 = null;

        EulangParser.macro_return macro30 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:13: ( listCompr | code | macro )
            int alt8=3;
            switch ( input.LA(1) ) {
            case FOR:
                {
                alt8=1;
                }
                break;
            case LBRACE_LPAREN:
                {
                alt8=2;
                }
                break;
            case LBRACE_STAR_LPAREN:
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:15: listCompr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_listCompr_in_selectoritem563);
                    listCompr28=listCompr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listCompr28.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:27: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_selectoritem567);
                    code29=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code29.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:34: macro
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_macro_in_selectoritem571);
                    macro30=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro30.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:97:1: xscope : LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) ;
    public final EulangParser.xscope_return xscope() throws RecognitionException {
        EulangParser.xscope_return retval = new EulangParser.xscope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE31=null;
        Token RBRACE33=null;
        EulangParser.toplevelstmts_return toplevelstmts32 = null;


        CommonTree LBRACE31_tree=null;
        CommonTree RBRACE33_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmts=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmts");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:97:8: ( LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:97:10: LBRACE toplevelstmts RBRACE
            {
            LBRACE31=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope582); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE31);

            pushFollow(FOLLOW_toplevelstmts_in_xscope584);
            toplevelstmts32=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmts.add(toplevelstmts32.getTree());
            RBRACE33=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope586); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE33);



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
            // 97:41: -> ^( SCOPE ( toplevelstmts )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:97:44: ^( SCOPE ( toplevelstmts )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:97:52: ( toplevelstmts )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:1: listCompr : ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) ;
    public final EulangParser.listCompr_return listCompr() throws RecognitionException {
        EulangParser.listCompr_return retval = new EulangParser.listCompr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON35=null;
        EulangParser.forIn_return forIn34 = null;

        EulangParser.listiterable_return listiterable36 = null;


        CommonTree COLON35_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_forIn=new RewriteRuleSubtreeStream(adaptor,"rule forIn");
        RewriteRuleSubtreeStream stream_listiterable=new RewriteRuleSubtreeStream(adaptor,"rule listiterable");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:10: ( ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:12: ( forIn )+ COLON listiterable
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:12: ( forIn )+
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:12: forIn
            	    {
            	    pushFollow(FOLLOW_forIn_in_listCompr613);
            	    forIn34=forIn();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_forIn.add(forIn34.getTree());

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

            COLON35=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr616); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON35);

            pushFollow(FOLLOW_listiterable_in_listCompr618);
            listiterable36=listiterable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listiterable.add(listiterable36.getTree());


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
            // 102:42: -> ^( LIST_COMPREHENSION ( forIn )+ listiterable )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:45: ^( LIST_COMPREHENSION ( forIn )+ listiterable )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:1: forIn : FOR idlist IN list -> ^( FOR idlist list ) ;
    public final EulangParser.forIn_return forIn() throws RecognitionException {
        EulangParser.forIn_return retval = new EulangParser.forIn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR37=null;
        Token IN39=null;
        EulangParser.idlist_return idlist38 = null;

        EulangParser.list_return list40 = null;


        CommonTree FOR37_tree=null;
        CommonTree IN39_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_list=new RewriteRuleSubtreeStream(adaptor,"rule list");
        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:7: ( FOR idlist IN list -> ^( FOR idlist list ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:9: FOR idlist IN list
            {
            FOR37=(Token)match(input,FOR,FOLLOW_FOR_in_forIn650); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR37);

            pushFollow(FOLLOW_idlist_in_forIn652);
            idlist38=idlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idlist.add(idlist38.getTree());
            IN39=(Token)match(input,IN,FOLLOW_IN_in_forIn654); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN39);

            pushFollow(FOLLOW_list_in_forIn656);
            list40=list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_list.add(list40.getTree());


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
            // 105:33: -> ^( FOR idlist list )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:36: ^( FOR idlist list )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:1: idlist : ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) ;
    public final EulangParser.idlist_return idlist() throws RecognitionException {
        EulangParser.idlist_return retval = new EulangParser.idlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID41=null;
        Token COMMA42=null;
        Token ID43=null;

        CommonTree ID41_tree=null;
        CommonTree COMMA42_tree=null;
        CommonTree ID43_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:8: ( ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:10: ID ( COMMA ID )*
            {
            ID41=(Token)match(input,ID,FOLLOW_ID_in_idlist681); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID41);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:13: ( COMMA ID )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==COMMA) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:14: COMMA ID
            	    {
            	    COMMA42=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist684); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA42);

            	    ID43=(Token)match(input,ID,FOLLOW_ID_in_idlist686); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID43);


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
            // 107:28: -> ^( IDLIST ( ID )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:31: ^( IDLIST ( ID )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:1: listiterable : ( code | macro | proto ) ;
    public final EulangParser.listiterable_return listiterable() throws RecognitionException {
        EulangParser.listiterable_return retval = new EulangParser.listiterable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code44 = null;

        EulangParser.macro_return macro45 = null;

        EulangParser.proto_return proto46 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:14: ( ( code | macro | proto ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:16: ( code | macro | proto )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:16: ( code | macro | proto )
            int alt11=3;
            switch ( input.LA(1) ) {
            case LBRACE_LPAREN:
                {
                alt11=1;
                }
                break;
            case LBRACE_STAR_LPAREN:
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:18: code
                    {
                    pushFollow(FOLLOW_code_in_listiterable715);
                    code44=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code44.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:25: macro
                    {
                    pushFollow(FOLLOW_macro_in_listiterable719);
                    macro45=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro45.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:33: proto
                    {
                    pushFollow(FOLLOW_proto_in_listiterable723);
                    proto46=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, proto46.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:1: list : LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) ;
    public final EulangParser.list_return list() throws RecognitionException {
        EulangParser.list_return retval = new EulangParser.list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET47=null;
        Token RBRACKET49=null;
        EulangParser.listitems_return listitems48 = null;


        CommonTree LBRACKET47_tree=null;
        CommonTree RBRACKET49_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_listitems=new RewriteRuleSubtreeStream(adaptor,"rule listitems");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:6: ( LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:8: LBRACKET listitems RBRACKET
            {
            LBRACKET47=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list738); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET47);

            pushFollow(FOLLOW_listitems_in_list740);
            listitems48=listitems();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listitems.add(listitems48.getTree());
            RBRACKET49=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list742); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET49);



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
            // 112:40: -> ^( LIST ( listitems )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:43: ^( LIST ( listitems )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:50: ( listitems )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:1: listitems : ( listitem ( COMMA listitem )* ( COMMA )? )? ;
    public final EulangParser.listitems_return listitems() throws RecognitionException {
        EulangParser.listitems_return retval = new EulangParser.listitems_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA51=null;
        Token COMMA53=null;
        EulangParser.listitem_return listitem50 = null;

        EulangParser.listitem_return listitem52 = null;


        CommonTree COMMA51_tree=null;
        CommonTree COMMA53_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:10: ( ( listitem ( COMMA listitem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==LBRACE_LPAREN||(LA14_0>=LBRACE_STAR_LPAREN && LA14_0<=COLON)||(LA14_0>=MINUS && LA14_0<=STAR)||LA14_0==LPAREN||LA14_0==LBRACE||LA14_0==LBRACKET||(LA14_0>=EXCL && LA14_0<=TILDE)||(LA14_0>=NUMBER && LA14_0<=COLONS)||(LA14_0>=CHAR_LITERAL && LA14_0<=STRING_LITERAL)) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems772);
                    listitem50=listitem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem50.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:22: ( COMMA listitem )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==COMMA) ) {
                            int LA12_1 = input.LA(2);

                            if ( (LA12_1==LBRACE_LPAREN||(LA12_1>=LBRACE_STAR_LPAREN && LA12_1<=COLON)||(LA12_1>=MINUS && LA12_1<=STAR)||LA12_1==LPAREN||LA12_1==LBRACE||LA12_1==LBRACKET||(LA12_1>=EXCL && LA12_1<=TILDE)||(LA12_1>=NUMBER && LA12_1<=COLONS)||(LA12_1>=CHAR_LITERAL && LA12_1<=STRING_LITERAL)) ) {
                                alt12=1;
                            }


                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:24: COMMA listitem
                    	    {
                    	    COMMA51=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems776); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA51_tree = (CommonTree)adaptor.create(COMMA51);
                    	    adaptor.addChild(root_0, COMMA51_tree);
                    	    }
                    	    pushFollow(FOLLOW_listitem_in_listitems778);
                    	    listitem52=listitem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem52.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:42: ( COMMA )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==COMMA) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:42: COMMA
                            {
                            COMMA53=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems783); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA53_tree = (CommonTree)adaptor.create(COMMA53);
                            adaptor.addChild(root_0, COMMA53_tree);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:118:1: listitem : toplevelvalue ;
    public final EulangParser.listitem_return listitem() throws RecognitionException {
        EulangParser.listitem_return retval = new EulangParser.listitem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelvalue_return toplevelvalue54 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:118:10: ( toplevelvalue )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:118:12: toplevelvalue
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelvalue_in_listitem809);
            toplevelvalue54=toplevelvalue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelvalue54.getTree());

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

    public static class proto_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "proto"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:1: proto : LPAREN protoargdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( protoargdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN55=null;
        Token RPAREN58=null;
        EulangParser.protoargdefs_return protoargdefs56 = null;

        EulangParser.xreturns_return xreturns57 = null;


        CommonTree LPAREN55_tree=null;
        CommonTree RPAREN58_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_protoargdefs=new RewriteRuleSubtreeStream(adaptor,"rule protoargdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:7: ( LPAREN protoargdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( protoargdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:11: LPAREN protoargdefs ( xreturns )? RPAREN
            {
            LPAREN55=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto829); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN55);

            pushFollow(FOLLOW_protoargdefs_in_proto831);
            protoargdefs56=protoargdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_protoargdefs.add(protoargdefs56.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:31: ( xreturns )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==RETURNS) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:31: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto833);
                    xreturns57=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns57.getTree());

                    }
                    break;

            }

            RPAREN58=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto836); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN58);



            // AST REWRITE
            // elements: xreturns, protoargdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 125:66: -> ^( PROTO ( xreturns )? ( protoargdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:69: ^( PROTO ( xreturns )? ( protoargdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:77: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:87: ( protoargdefs )*
                while ( stream_protoargdefs.hasNext() ) {
                    adaptor.addChild(root_1, stream_protoargdefs.nextTree());

                }
                stream_protoargdefs.reset();

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

    public static class protoargdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "protoargdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:1: protoargdefs : ( protoargdef ( COMMA protoargdef )* ( COMMA )? )? -> ( protoargdef )* ;
    public final EulangParser.protoargdefs_return protoargdefs() throws RecognitionException {
        EulangParser.protoargdefs_return retval = new EulangParser.protoargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA60=null;
        Token COMMA62=null;
        EulangParser.protoargdef_return protoargdef59 = null;

        EulangParser.protoargdef_return protoargdef61 = null;


        CommonTree COMMA60_tree=null;
        CommonTree COMMA62_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_protoargdef=new RewriteRuleSubtreeStream(adaptor,"rule protoargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:13: ( ( protoargdef ( COMMA protoargdef )* ( COMMA )? )? -> ( protoargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:15: ( protoargdef ( COMMA protoargdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:15: ( protoargdef ( COMMA protoargdef )* ( COMMA )? )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==ID) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:16: protoargdef ( COMMA protoargdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_protoargdef_in_protoargdefs879);
                    protoargdef59=protoargdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_protoargdef.add(protoargdef59.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:28: ( COMMA protoargdef )*
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( (LA16_0==COMMA) ) {
                            int LA16_1 = input.LA(2);

                            if ( (LA16_1==ID) ) {
                                alt16=1;
                            }


                        }


                        switch (alt16) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:30: COMMA protoargdef
                    	    {
                    	    COMMA60=(Token)match(input,COMMA,FOLLOW_COMMA_in_protoargdefs883); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA60);

                    	    pushFollow(FOLLOW_protoargdef_in_protoargdefs885);
                    	    protoargdef61=protoargdef();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_protoargdef.add(protoargdef61.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop16;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:50: ( COMMA )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==COMMA) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:50: COMMA
                            {
                            COMMA62=(Token)match(input,COMMA,FOLLOW_COMMA_in_protoargdefs889); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA62);


                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: protoargdef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 128:82: -> ( protoargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:85: ( protoargdef )*
                while ( stream_protoargdef.hasNext() ) {
                    adaptor.addChild(root_0, stream_protoargdef.nextTree());

                }
                stream_protoargdef.reset();

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
    // $ANTLR end "protoargdefs"

    public static class protoargdef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "protoargdef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:1: protoargdef : ID ( COLON type ( EQUALS rhsExpr )? )? -> ^( ARGDEF ID ( type )* ( rhsExpr )* ) ;
    public final EulangParser.protoargdef_return protoargdef() throws RecognitionException {
        EulangParser.protoargdef_return retval = new EulangParser.protoargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID63=null;
        Token COLON64=null;
        Token EQUALS66=null;
        EulangParser.type_return type65 = null;

        EulangParser.rhsExpr_return rhsExpr67 = null;


        CommonTree ID63_tree=null;
        CommonTree COLON64_tree=null;
        CommonTree EQUALS66_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:12: ( ID ( COLON type ( EQUALS rhsExpr )? )? -> ^( ARGDEF ID ( type )* ( rhsExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:15: ID ( COLON type ( EQUALS rhsExpr )? )?
            {
            ID63=(Token)match(input,ID,FOLLOW_ID_in_protoargdef934); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID63);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:18: ( COLON type ( EQUALS rhsExpr )? )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==COLON) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:19: COLON type ( EQUALS rhsExpr )?
                    {
                    COLON64=(Token)match(input,COLON,FOLLOW_COLON_in_protoargdef937); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON64);

                    pushFollow(FOLLOW_type_in_protoargdef939);
                    type65=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type65.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:30: ( EQUALS rhsExpr )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==EQUALS) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:31: EQUALS rhsExpr
                            {
                            EQUALS66=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_protoargdef942); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS66);

                            pushFollow(FOLLOW_rhsExpr_in_protoargdef944);
                            rhsExpr67=rhsExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr67.getTree());

                            }
                            break;

                    }


                    }
                    break;

            }



            // AST REWRITE
            // elements: type, ID, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 131:53: -> ^( ARGDEF ID ( type )* ( rhsExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:56: ^( ARGDEF ID ( type )* ( rhsExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:68: ( type )*
                while ( stream_type.hasNext() ) {
                    adaptor.addChild(root_1, stream_type.nextTree());

                }
                stream_type.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:74: ( rhsExpr )*
                while ( stream_rhsExpr.hasNext() ) {
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
    // $ANTLR end "protoargdef"

    public static class code_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "code"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:1: code : LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE_LPAREN68=null;
        Token RPAREN71=null;
        Token RBRACE73=null;
        EulangParser.argdefs_return argdefs69 = null;

        EulangParser.xreturns_return xreturns70 = null;

        EulangParser.codestmtlist_return codestmtlist72 = null;


        CommonTree LBRACE_LPAREN68_tree=null;
        CommonTree RPAREN71_tree=null;
        CommonTree RBRACE73_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE_LPAREN=new RewriteRuleTokenStream(adaptor,"token LBRACE_LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:6: ( LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:10: LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE
            {
            LBRACE_LPAREN68=(Token)match(input,LBRACE_LPAREN,FOLLOW_LBRACE_LPAREN_in_code981); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE_LPAREN.add(LBRACE_LPAREN68);

            pushFollow(FOLLOW_argdefs_in_code983);
            argdefs69=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs69.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:32: ( xreturns )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==RETURNS) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:32: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_code985);
                    xreturns70=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns70.getTree());

                    }
                    break;

            }

            RPAREN71=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_code988); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN71);

            pushFollow(FOLLOW_codestmtlist_in_code990);
            codestmtlist72=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist72.getTree());
            RBRACE73=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code992); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE73);



            // AST REWRITE
            // elements: codestmtlist, argdefs, xreturns
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 135:69: -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:72: ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CODE, "CODE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:79: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:87: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:97: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:107: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:1: macro : LBRACE_STAR_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.macro_return macro() throws RecognitionException {
        EulangParser.macro_return retval = new EulangParser.macro_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE_STAR_LPAREN74=null;
        Token RPAREN77=null;
        Token RBRACE79=null;
        EulangParser.argdefs_return argdefs75 = null;

        EulangParser.xreturns_return xreturns76 = null;

        EulangParser.codestmtlist_return codestmtlist78 = null;


        CommonTree LBRACE_STAR_LPAREN74_tree=null;
        CommonTree RPAREN77_tree=null;
        CommonTree RBRACE79_tree=null;
        RewriteRuleTokenStream stream_LBRACE_STAR_LPAREN=new RewriteRuleTokenStream(adaptor,"token LBRACE_STAR_LPAREN");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:7: ( LBRACE_STAR_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:11: LBRACE_STAR_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE
            {
            LBRACE_STAR_LPAREN74=(Token)match(input,LBRACE_STAR_LPAREN,FOLLOW_LBRACE_STAR_LPAREN_in_macro1029); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE_STAR_LPAREN.add(LBRACE_STAR_LPAREN74);

            pushFollow(FOLLOW_argdefs_in_macro1031);
            argdefs75=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs75.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:38: ( xreturns )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==RETURNS) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:38: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_macro1033);
                    xreturns76=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns76.getTree());

                    }
                    break;

            }

            RPAREN77=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_macro1036); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN77);

            pushFollow(FOLLOW_codestmtlist_in_macro1038);
            codestmtlist78=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist78.getTree());
            RBRACE79=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_macro1040); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE79);



            // AST REWRITE
            // elements: codestmtlist, xreturns, argdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 139:75: -> ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:78: ^( MACRO ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MACRO, "MACRO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:86: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:94: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:104: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:139:114: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:1: argdefs : ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* ;
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA81=null;
        Token COMMA83=null;
        EulangParser.argdef_return argdef80 = null;

        EulangParser.argdef_return argdef82 = null;


        CommonTree COMMA81_tree=null;
        CommonTree COMMA83_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdef=new RewriteRuleSubtreeStream(adaptor,"rule argdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:8: ( ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==ID) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:11: argdef ( COMMA argdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_argdef_in_argdefs1074);
                    argdef80=argdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_argdef.add(argdef80.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:18: ( COMMA argdef )*
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( (LA23_0==COMMA) ) {
                            int LA23_1 = input.LA(2);

                            if ( (LA23_1==ID) ) {
                                alt23=1;
                            }


                        }


                        switch (alt23) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:20: COMMA argdef
                    	    {
                    	    COMMA81=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1078); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA81);

                    	    pushFollow(FOLLOW_argdef_in_argdefs1080);
                    	    argdef82=argdef();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_argdef.add(argdef82.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:35: ( COMMA )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==COMMA) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:35: COMMA
                            {
                            COMMA83=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1084); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA83);


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
            // 142:67: -> ( argdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:70: ( argdef )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:1: argdef : ( protoargdef | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) );
    public final EulangParser.argdef_return argdef() throws RecognitionException {
        EulangParser.argdef_return retval = new EulangParser.argdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID85=null;
        Token EQUALS86=null;
        EulangParser.protoargdef_return protoargdef84 = null;

        EulangParser.assignExpr_return assignExpr87 = null;


        CommonTree ID85_tree=null;
        CommonTree EQUALS86_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:7: ( protoargdef | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==ID) ) {
                int LA26_1 = input.LA(2);

                if ( (LA26_1==EQUALS) ) {
                    alt26=2;
                }
                else if ( ((LA26_1>=COLON && LA26_1<=COMMA)||LA26_1==RPAREN||LA26_1==RETURNS) ) {
                    alt26=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 26, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:10: protoargdef
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_protoargdef_in_argdef1129);
                    protoargdef84=protoargdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, protoargdef84.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:7: ID EQUALS assignExpr
                    {
                    ID85=(Token)match(input,ID,FOLLOW_ID_in_argdef1137); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID85);

                    EQUALS86=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdef1139); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS86);

                    pushFollow(FOLLOW_assignExpr_in_argdef1141);
                    assignExpr87=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr87.getTree());


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
                    // 146:31: -> ^( ARGDEF ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:34: ^( ARGDEF ID TYPE assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
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
    // $ANTLR end "argdef"

    public static class xreturns_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xreturns"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:1: xreturns : RETURNS type -> type ;
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURNS88=null;
        EulangParser.type_return type89 = null;


        CommonTree RETURNS88_tree=null;
        RewriteRuleTokenStream stream_RETURNS=new RewriteRuleTokenStream(adaptor,"token RETURNS");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:9: ( RETURNS type -> type )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:11: RETURNS type
            {
            RETURNS88=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_xreturns1166); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RETURNS.add(RETURNS88);

            pushFollow(FOLLOW_type_in_xreturns1168);
            type89=type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_type.add(type89.getTree());


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
            // 149:29: -> type
            {
                adaptor.addChild(root_0, stream_type.nextTree());

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
    // $ANTLR end "xreturns"

    public static class type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:1: type : ( ID -> ^( TYPE ID ) ) ( AMP -> ^( TYPE ^( REF ID ) ) )? ;
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID90=null;
        Token AMP91=null;

        CommonTree ID90_tree=null;
        CommonTree AMP91_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:6: ( ( ID -> ^( TYPE ID ) ) ( AMP -> ^( TYPE ^( REF ID ) ) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:9: ( ID -> ^( TYPE ID ) ) ( AMP -> ^( TYPE ^( REF ID ) ) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:9: ( ID -> ^( TYPE ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:11: ID
            {
            ID90=(Token)match(input,ID,FOLLOW_ID_in_type1193); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID90);



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
            // 152:14: -> ^( TYPE ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:17: ^( TYPE ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:153:7: ( AMP -> ^( TYPE ^( REF ID ) ) )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==AMP) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:153:9: AMP
                    {
                    AMP91=(Token)match(input,AMP,FOLLOW_AMP_in_type1213); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AMP.add(AMP91);



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
                    // 153:13: -> ^( TYPE ^( REF ID ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:153:16: ^( TYPE ^( REF ID ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:153:23: ^( REF ID )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(REF, "REF"), root_2);

                        adaptor.addChild(root_2, stream_ID.nextNode());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:1: codestmtlist : ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) ;
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI93=null;
        Token SEMI95=null;
        EulangParser.codeStmt_return codeStmt92 = null;

        EulangParser.codeStmt_return codeStmt94 = null;


        CommonTree SEMI93_tree=null;
        CommonTree SEMI95_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:13: ( ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==COLON||(LA30_0>=MINUS && LA30_0<=STAR)||LA30_0==LPAREN||(LA30_0>=EXCL && LA30_0<=TILDE)||LA30_0==RETURN||(LA30_0>=NUMBER && LA30_0<=COLONS)||(LA30_0>=CHAR_LITERAL && LA30_0<=STRING_LITERAL)) ) {
                alt30=1;
            }
            switch (alt30) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:16: codeStmt ( SEMI codeStmt )* ( SEMI )?
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist1248);
                    codeStmt92=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt92.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:25: ( SEMI codeStmt )*
                    loop28:
                    do {
                        int alt28=2;
                        int LA28_0 = input.LA(1);

                        if ( (LA28_0==SEMI) ) {
                            int LA28_1 = input.LA(2);

                            if ( (LA28_1==COLON||(LA28_1>=MINUS && LA28_1<=STAR)||LA28_1==LPAREN||(LA28_1>=EXCL && LA28_1<=TILDE)||LA28_1==RETURN||(LA28_1>=NUMBER && LA28_1<=COLONS)||(LA28_1>=CHAR_LITERAL && LA28_1<=STRING_LITERAL)) ) {
                                alt28=1;
                            }


                        }


                        switch (alt28) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:27: SEMI codeStmt
                    	    {
                    	    SEMI93=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist1252); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI93);

                    	    pushFollow(FOLLOW_codeStmt_in_codestmtlist1254);
                    	    codeStmt94=codeStmt();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt94.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:44: ( SEMI )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==SEMI) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:44: SEMI
                            {
                            SEMI95=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist1259); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SEMI.add(SEMI95);


                            }
                            break;

                    }


                    }
                    break;

            }



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
            // 157:53: -> ^( STMTLIST ( codeStmt )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:57: ^( STMTLIST ( codeStmt )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:68: ( codeStmt )*
                while ( stream_codeStmt.hasNext() ) {
                    adaptor.addChild(root_1, stream_codeStmt.nextTree());

                }
                stream_codeStmt.reset();

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
    // $ANTLR end "codestmtlist"

    public static class codeStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codeStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:160:1: codeStmt : ( varDecl | assignExpr | returnExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl96 = null;

        EulangParser.assignExpr_return assignExpr97 = null;

        EulangParser.returnExpr_return returnExpr98 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:160:10: ( varDecl | assignExpr | returnExpr )
            int alt31=3;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA31_1 = input.LA(2);

                if ( (LA31_1==COLON||LA31_1==COLON_EQUALS) ) {
                    alt31=1;
                }
                else if ( (LA31_1==EQUALS||(LA31_1>=PLUS && LA31_1<=LPAREN)||LA31_1==RBRACE||(LA31_1>=AMP && LA31_1<=UMOD)||LA31_1==COMPXOR) ) {
                    alt31=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 31, 1, input);

                    throw nvae;
                }
                }
                break;
            case COLON:
            case MINUS:
            case STAR:
            case LPAREN:
            case EXCL:
            case TILDE:
            case NUMBER:
            case SCOPEREF:
            case COLONS:
            case CHAR_LITERAL:
            case STRING_LITERAL:
                {
                alt31=2;
                }
                break;
            case RETURN:
                {
                alt31=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }

            switch (alt31) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:160:12: varDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_varDecl_in_codeStmt1290);
                    varDecl96=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, varDecl96.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:161:9: assignExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignExpr_in_codeStmt1300);
                    assignExpr97=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignExpr97.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:162:9: returnExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_returnExpr_in_codeStmt1310);
                    returnExpr98=returnExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, returnExpr98.getTree());

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

    public static class varDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "varDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:165:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID99=null;
        Token COLON_EQUALS100=null;
        Token ID102=null;
        Token COLON103=null;
        Token EQUALS105=null;
        EulangParser.assignExpr_return assignExpr101 = null;

        EulangParser.type_return type104 = null;

        EulangParser.assignExpr_return assignExpr106 = null;


        CommonTree ID99_tree=null;
        CommonTree COLON_EQUALS100_tree=null;
        CommonTree ID102_tree=null;
        CommonTree COLON103_tree=null;
        CommonTree EQUALS105_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:165:8: ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==ID) ) {
                int LA33_1 = input.LA(2);

                if ( (LA33_1==COLON_EQUALS) ) {
                    alt33=1;
                }
                else if ( (LA33_1==COLON) ) {
                    alt33=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 33, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:165:10: ID COLON_EQUALS assignExpr
                    {
                    ID99=(Token)match(input,ID,FOLLOW_ID_in_varDecl1324); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID99);

                    COLON_EQUALS100=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1326); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS100);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1328);
                    assignExpr101=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr101.getTree());


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
                    // 165:45: -> ^( DEFINE ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:165:48: ^( DEFINE ID TYPE assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID102=(Token)match(input,ID,FOLLOW_ID_in_varDecl1356); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID102);

                    COLON103=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1358); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON103);

                    pushFollow(FOLLOW_type_in_varDecl1360);
                    type104=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type104.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:21: ( EQUALS assignExpr )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==EQUALS) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:22: EQUALS assignExpr
                            {
                            EQUALS105=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1363); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS105);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1365);
                            assignExpr106=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr106.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ID, type, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 166:43: -> ^( DEFINE ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:46: ^( DEFINE ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:63: ( assignExpr )*
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

    public static class returnExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "returnExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:169:1: returnExpr : RETURN ( assignExpr )? -> ^( RETURN ( assignExpr )? ) ;
    public final EulangParser.returnExpr_return returnExpr() throws RecognitionException {
        EulangParser.returnExpr_return retval = new EulangParser.returnExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURN107=null;
        EulangParser.assignExpr_return assignExpr108 = null;


        CommonTree RETURN107_tree=null;
        RewriteRuleTokenStream stream_RETURN=new RewriteRuleTokenStream(adaptor,"token RETURN");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:169:12: ( RETURN ( assignExpr )? -> ^( RETURN ( assignExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:169:14: RETURN ( assignExpr )?
            {
            RETURN107=(Token)match(input,RETURN,FOLLOW_RETURN_in_returnExpr1394); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RETURN.add(RETURN107);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:169:21: ( assignExpr )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==COLON||(LA34_0>=MINUS && LA34_0<=STAR)||LA34_0==LPAREN||(LA34_0>=EXCL && LA34_0<=TILDE)||(LA34_0>=NUMBER && LA34_0<=COLONS)||(LA34_0>=CHAR_LITERAL && LA34_0<=STRING_LITERAL)) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:169:21: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_returnExpr1396);
                    assignExpr108=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr108.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: RETURN, assignExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 169:43: -> ^( RETURN ( assignExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:169:46: ^( RETURN ( assignExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_RETURN.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:169:55: ( assignExpr )?
                if ( stream_assignExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_assignExpr.nextTree());

                }
                stream_assignExpr.reset();

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
    // $ANTLR end "returnExpr"

    public static class assignExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:172:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS110=null;
        EulangParser.idOrScopeRef_return idOrScopeRef109 = null;

        EulangParser.assignExpr_return assignExpr111 = null;

        EulangParser.rhsExpr_return rhsExpr112 = null;


        CommonTree EQUALS110_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:172:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | rhsExpr -> rhsExpr )
            int alt35=2;
            alt35 = dfa35.predict(input);
            switch (alt35) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:172:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignExpr1437);
                    idOrScopeRef109=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef109.getTree());
                    EQUALS110=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr1439); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS110);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr1441);
                    assignExpr111=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr111.getTree());


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
                    // 172:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:172:55: ^( ASSIGN idOrScopeRef assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:173:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr1466);
                    rhsExpr112=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr112.getTree());


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
                    // 173:43: -> rhsExpr
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

    public static class rhsExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rhsExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:176:1: rhsExpr : cond -> cond ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.cond_return cond113 = null;


        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:176:9: ( cond -> cond )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:176:13: cond
            {
            pushFollow(FOLLOW_cond_in_rhsExpr1513);
            cond113=cond();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_cond.add(cond113.getTree());


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
            // 176:43: -> cond
            {
                adaptor.addChild(root_0, stream_cond.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:1: funcCall : idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN115=null;
        Token RPAREN117=null;
        EulangParser.idOrScopeRef_return idOrScopeRef114 = null;

        EulangParser.arglist_return arglist116 = null;


        CommonTree LPAREN115_tree=null;
        CommonTree RPAREN117_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:10: ( idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:12: idOrScopeRef LPAREN arglist RPAREN
            {
            pushFollow(FOLLOW_idOrScopeRef_in_funcCall1559);
            idOrScopeRef114=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef114.getTree());
            LPAREN115=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall1561); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN115);

            pushFollow(FOLLOW_arglist_in_funcCall1563);
            arglist116=arglist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arglist.add(arglist116.getTree());
            RPAREN117=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall1565); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN117);



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
            // 179:49: -> ^( CALL idOrScopeRef arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:56: ^( CALL idOrScopeRef arglist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA119=null;
        Token COMMA121=null;
        EulangParser.arg_return arg118 = null;

        EulangParser.arg_return arg120 = null;


        CommonTree COMMA119_tree=null;
        CommonTree COMMA121_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==COLON||(LA38_0>=MINUS && LA38_0<=STAR)||LA38_0==LPAREN||(LA38_0>=EXCL && LA38_0<=TILDE)||(LA38_0>=NUMBER && LA38_0<=COLONS)||(LA38_0>=CHAR_LITERAL && LA38_0<=STRING_LITERAL)) ) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist1596);
                    arg118=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg118.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:15: ( COMMA arg )*
                    loop36:
                    do {
                        int alt36=2;
                        int LA36_0 = input.LA(1);

                        if ( (LA36_0==COMMA) ) {
                            int LA36_1 = input.LA(2);

                            if ( (LA36_1==COLON||(LA36_1>=MINUS && LA36_1<=STAR)||LA36_1==LPAREN||(LA36_1>=EXCL && LA36_1<=TILDE)||(LA36_1>=NUMBER && LA36_1<=COLONS)||(LA36_1>=CHAR_LITERAL && LA36_1<=STRING_LITERAL)) ) {
                                alt36=1;
                            }


                        }


                        switch (alt36) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:17: COMMA arg
                    	    {
                    	    COMMA119=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist1600); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA119);

                    	    pushFollow(FOLLOW_arg_in_arglist1602);
                    	    arg120=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg120.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop36;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:29: ( COMMA )?
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==COMMA) ) {
                        alt37=1;
                    }
                    switch (alt37) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:29: COMMA
                            {
                            COMMA121=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist1606); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA121);


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
            // 183:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:183:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:186:1: arg : assignExpr -> ^( EXPR assignExpr ) ;
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr122 = null;


        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:186:4: ( assignExpr -> ^( EXPR assignExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:186:7: assignExpr
            {
            pushFollow(FOLLOW_assignExpr_in_arg1655);
            assignExpr122=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr122.getTree());


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
            // 186:37: -> ^( EXPR assignExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:186:40: ^( EXPR assignExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                adaptor.addChild(root_1, stream_assignExpr.nextTree());

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
    // $ANTLR end "arg"

    public static class cond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:1: cond : (l= logcond -> $l) ( ( QUESTION t= cond )=> QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION123=null;
        Token COLON124=null;
        EulangParser.logcond_return l = null;

        EulangParser.cond_return t = null;

        EulangParser.cond_return f = null;


        CommonTree QUESTION123_tree=null;
        CommonTree COLON124_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logcond=new RewriteRuleSubtreeStream(adaptor,"rule logcond");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:5: ( (l= logcond -> $l) ( ( QUESTION t= cond )=> QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:10: (l= logcond -> $l) ( ( QUESTION t= cond )=> QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:10: (l= logcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:12: l= logcond
            {
            pushFollow(FOLLOW_logcond_in_cond1705);
            l=logcond();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logcond.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 193:23: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:194:7: ( ( QUESTION t= cond )=> QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==QUESTION) ) {
                    int LA39_2 = input.LA(2);

                    if ( (synpred2_EulangParser()) ) {
                        alt39=1;
                    }


                }


                switch (alt39) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:194:9: ( QUESTION t= cond )=> QUESTION t= cond COLON f= cond
            	    {
            	    QUESTION123=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond1735); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION123);

            	    pushFollow(FOLLOW_cond_in_cond1739);
            	    t=cond();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_cond.add(t.getTree());
            	    COLON124=(Token)match(input,COLON,FOLLOW_COLON_in_cond1741); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON124);

            	    pushFollow(FOLLOW_cond_in_cond1745);
            	    f=cond();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_cond.add(f.getTree());


            	    // AST REWRITE
            	    // elements: f, l, t
            	    // token labels: 
            	    // rule labels: f, retval, t, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_f=new RewriteRuleSubtreeStream(adaptor,"rule f",f!=null?f.tree:null);
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 194:61: -> ^( COND $l $t $f)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:194:64: ^( COND $l $t $f)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(COND, "COND"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_t.nextTree());
            	        adaptor.addChild(root_1, stream_f.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop39;
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

    public static class logcond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logcond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:1: logcond : (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )* ;
    public final EulangParser.logcond_return logcond() throws RecognitionException {
        EulangParser.logcond_return retval = new EulangParser.logcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPAND125=null;
        Token COMPOR126=null;
        Token COMPXOR127=null;
        EulangParser.binlogcond_return l = null;

        EulangParser.binlogcond_return r = null;


        CommonTree COMPAND125_tree=null;
        CommonTree COMPOR126_tree=null;
        CommonTree COMPXOR127_tree=null;
        RewriteRuleTokenStream stream_COMPAND=new RewriteRuleTokenStream(adaptor,"token COMPAND");
        RewriteRuleTokenStream stream_COMPXOR=new RewriteRuleTokenStream(adaptor,"token COMPXOR");
        RewriteRuleTokenStream stream_COMPOR=new RewriteRuleTokenStream(adaptor,"token COMPOR");
        RewriteRuleSubtreeStream stream_binlogcond=new RewriteRuleSubtreeStream(adaptor,"rule binlogcond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:8: ( (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:10: (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:10: (l= binlogcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:12: l= binlogcond
            {
            pushFollow(FOLLOW_binlogcond_in_logcond1783);
            l=binlogcond();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_binlogcond.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 198:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:199:7: ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )*
            loop40:
            do {
                int alt40=4;
                switch ( input.LA(1) ) {
                case COMPAND:
                    {
                    alt40=1;
                    }
                    break;
                case COMPOR:
                    {
                    alt40=2;
                    }
                    break;
                case COMPXOR:
                    {
                    alt40=3;
                    }
                    break;

                }

                switch (alt40) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:199:9: COMPAND r= binlogcond
            	    {
            	    COMPAND125=(Token)match(input,COMPAND,FOLLOW_COMPAND_in_logcond1810); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPAND.add(COMPAND125);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1814);
            	    r=binlogcond();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPAND, r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 199:30: -> ^( COMPAND $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:199:33: ^( COMPAND $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPAND.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:200:9: COMPOR r= binlogcond
            	    {
            	    COMPOR126=(Token)match(input,COMPOR,FOLLOW_COMPOR_in_logcond1836); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPOR.add(COMPOR126);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1840);
            	    r=binlogcond();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, COMPOR
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 200:29: -> ^( COMPOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:200:32: ^( COMPOR $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPOR.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:201:9: COMPXOR r= binlogcond
            	    {
            	    COMPXOR127=(Token)match(input,COMPXOR,FOLLOW_COMPXOR_in_logcond1862); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPXOR.add(COMPXOR127);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1866);
            	    r=binlogcond();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, COMPXOR
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 201:31: -> ^( COMPXOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:201:34: ^( COMPXOR $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPXOR.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop40;
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
    // $ANTLR end "logcond"

    public static class binlogcond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "binlogcond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:205:1: binlogcond : (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )* ;
    public final EulangParser.binlogcond_return binlogcond() throws RecognitionException {
        EulangParser.binlogcond_return retval = new EulangParser.binlogcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP128=null;
        Token BAR129=null;
        Token CARET130=null;
        EulangParser.compeq_return l = null;

        EulangParser.compeq_return r = null;


        CommonTree AMP128_tree=null;
        CommonTree BAR129_tree=null;
        CommonTree CARET130_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_compeq=new RewriteRuleSubtreeStream(adaptor,"rule compeq");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:205:11: ( (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:205:13: (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:205:13: (l= compeq -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:205:15: l= compeq
            {
            pushFollow(FOLLOW_compeq_in_binlogcond1900);
            l=compeq();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_compeq.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 205:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:7: ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )*
            loop41:
            do {
                int alt41=4;
                switch ( input.LA(1) ) {
                case AMP:
                    {
                    alt41=1;
                    }
                    break;
                case BAR:
                    {
                    alt41=2;
                    }
                    break;
                case CARET:
                    {
                    alt41=3;
                    }
                    break;

                }

                switch (alt41) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:9: AMP r= compeq
            	    {
            	    AMP128=(Token)match(input,AMP,FOLLOW_AMP_in_binlogcond1929); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP128);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1933);
            	    r=compeq();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_compeq.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 206:23: -> ^( BITAND $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:26: ^( BITAND $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITAND, "BITAND"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:207:9: BAR r= compeq
            	    {
            	    BAR129=(Token)match(input,BAR,FOLLOW_BAR_in_binlogcond1956); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR129);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1960);
            	    r=compeq();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_compeq.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 207:23: -> ^( BITOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:207:26: ^( BITOR $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITOR, "BITOR"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:208:9: CARET r= compeq
            	    {
            	    CARET130=(Token)match(input,CARET,FOLLOW_CARET_in_binlogcond1983); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET130);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1987);
            	    r=compeq();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_compeq.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 208:25: -> ^( BITXOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:208:28: ^( BITXOR $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITXOR, "BITXOR"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop41;
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
    // $ANTLR end "binlogcond"

    public static class compeq_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compeq"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:212:1: compeq : (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )* ;
    public final EulangParser.compeq_return compeq() throws RecognitionException {
        EulangParser.compeq_return retval = new EulangParser.compeq_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ131=null;
        Token COMPNE132=null;
        EulangParser.comp_return l = null;

        EulangParser.comp_return r = null;


        CommonTree COMPEQ131_tree=null;
        CommonTree COMPNE132_tree=null;
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:212:7: ( (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:212:11: (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:212:11: (l= comp -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:212:13: l= comp
            {
            pushFollow(FOLLOW_comp_in_compeq2023);
            l=comp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_comp.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 212:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:213:7: ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )*
            loop42:
            do {
                int alt42=3;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==COMPEQ) ) {
                    alt42=1;
                }
                else if ( (LA42_0==COMPNE) ) {
                    alt42=2;
                }


                switch (alt42) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:213:9: COMPEQ r= comp
            	    {
            	    COMPEQ131=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_compeq2057); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ131);

            	    pushFollow(FOLLOW_comp_in_compeq2061);
            	    r=comp();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_comp.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPEQ, l, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 213:23: -> ^( COMPEQ $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:213:26: ^( COMPEQ $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPEQ.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:214:9: COMPNE r= comp
            	    {
            	    COMPNE132=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_compeq2083); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE132);

            	    pushFollow(FOLLOW_comp_in_compeq2087);
            	    r=comp();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_comp.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, COMPNE
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 214:23: -> ^( COMPNE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:214:26: ^( COMPNE $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPNE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop42;
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
    // $ANTLR end "compeq"

    public static class comp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:218:1: comp : (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPLE133=null;
        Token COMPGE134=null;
        Token LESS135=null;
        Token GREATER136=null;
        EulangParser.shift_return l = null;

        EulangParser.shift_return r = null;


        CommonTree COMPLE133_tree=null;
        CommonTree COMPGE134_tree=null;
        CommonTree LESS135_tree=null;
        CommonTree GREATER136_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:218:5: ( (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:218:8: (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:218:8: (l= shift -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:218:10: l= shift
            {
            pushFollow(FOLLOW_shift_in_comp2121);
            l=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 218:28: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:7: ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )*
            loop43:
            do {
                int alt43=5;
                switch ( input.LA(1) ) {
                case COMPLE:
                    {
                    alt43=1;
                    }
                    break;
                case COMPGE:
                    {
                    alt43=2;
                    }
                    break;
                case LESS:
                    {
                    alt43=3;
                    }
                    break;
                case GREATER:
                    {
                    alt43=4;
                    }
                    break;

                }

                switch (alt43) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:9: COMPLE r= shift
            	    {
            	    COMPLE133=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp2148); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE133);

            	    pushFollow(FOLLOW_shift_in_comp2152);
            	    r=shift();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPLE, r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 219:27: -> ^( COMPLE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:30: ^( COMPLE $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPLE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:220:9: COMPGE r= shift
            	    {
            	    COMPGE134=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp2177); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE134);

            	    pushFollow(FOLLOW_shift_in_comp2181);
            	    r=shift();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, COMPGE
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 220:27: -> ^( COMPGE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:220:30: ^( COMPGE $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPGE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:221:9: LESS r= shift
            	    {
            	    LESS135=(Token)match(input,LESS,FOLLOW_LESS_in_comp2206); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS135);

            	    pushFollow(FOLLOW_shift_in_comp2210);
            	    r=shift();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: LESS, l, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 221:26: -> ^( LESS $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:221:29: ^( LESS $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_LESS.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:222:9: GREATER r= shift
            	    {
            	    GREATER136=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp2236); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER136);

            	    pushFollow(FOLLOW_shift_in_comp2240);
            	    r=shift();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r, GREATER
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 222:28: -> ^( GREATER $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:222:31: ^( GREATER $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_GREATER.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop43;
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

    public static class shift_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:226:1: shift : (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT137=null;
        Token RSHIFT138=null;
        Token URSHIFT139=null;
        EulangParser.factor_return l = null;

        EulangParser.factor_return r = null;


        CommonTree LSHIFT137_tree=null;
        CommonTree RSHIFT138_tree=null;
        CommonTree URSHIFT139_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:226:6: ( (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:226:9: (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:226:9: (l= factor -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:226:11: l= factor
            {
            pushFollow(FOLLOW_factor_in_shift2292);
            l=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 226:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:227:7: ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )*
            loop44:
            do {
                int alt44=4;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt44=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt44=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt44=3;
                    }
                    break;

                }

                switch (alt44) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:227:9: LSHIFT r= factor
            	    {
            	    LSHIFT137=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift2325); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT137);

            	    pushFollow(FOLLOW_factor_in_shift2329);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, LSHIFT, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 227:27: -> ^( LSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:227:30: ^( LSHIFT $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_LSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:228:9: RSHIFT r= factor
            	    {
            	    RSHIFT138=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift2354); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT138);

            	    pushFollow(FOLLOW_factor_in_shift2358);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, RSHIFT, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 228:27: -> ^( RSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:228:30: ^( RSHIFT $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_RSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:229:9: URSHIFT r= factor
            	    {
            	    URSHIFT139=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift2382); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT139);

            	    pushFollow(FOLLOW_factor_in_shift2386);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r, URSHIFT
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 229:28: -> ^( URSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:229:31: ^( URSHIFT $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_URSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop44;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:232:1: factor : (l= term -> $l) ( PLUS r= term -> ^( ADD $l $r) | ( MINUS r= term )=> MINUS r= term -> ^( SUB $l $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS140=null;
        Token MINUS141=null;
        EulangParser.term_return l = null;

        EulangParser.term_return r = null;


        CommonTree PLUS140_tree=null;
        CommonTree MINUS141_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:233:5: ( (l= term -> $l) ( PLUS r= term -> ^( ADD $l $r) | ( MINUS r= term )=> MINUS r= term -> ^( SUB $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:233:7: (l= term -> $l) ( PLUS r= term -> ^( ADD $l $r) | ( MINUS r= term )=> MINUS r= term -> ^( SUB $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:233:7: (l= term -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:233:9: l= term
            {
            pushFollow(FOLLOW_term_in_factor2428);
            l=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 233:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:234:9: ( PLUS r= term -> ^( ADD $l $r) | ( MINUS r= term )=> MINUS r= term -> ^( SUB $l $r) )*
            loop45:
            do {
                int alt45=3;
                alt45 = dfa45.predict(input);
                switch (alt45) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:234:13: PLUS r= term
            	    {
            	    PLUS140=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor2462); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS140);

            	    pushFollow(FOLLOW_term_in_factor2466);
            	    r=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_term.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 234:33: -> ^( ADD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:234:36: ^( ADD $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADD, "ADD"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:235:13: ( MINUS r= term )=> MINUS r= term
            	    {
            	    MINUS141=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor2512); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS141);

            	    pushFollow(FOLLOW_term_in_factor2516);
            	    r=term();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_term.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 235:53: -> ^( SUB $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:235:56: ^( SUB $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SUB, "SUB"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop45;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:1: term : (l= unary -> $l) ( ( STAR r= unary )=> STAR r= unary -> ^( MUL $l $r) | SLASH r= unary -> ^( DIV $l $r) | BACKSLASH r= unary -> ^( UDIV $l $r) | PERCENT r= unary -> ^( MOD $l $r) | UMOD r= unary -> ^( UMOD $l $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR142=null;
        Token SLASH143=null;
        Token BACKSLASH144=null;
        Token PERCENT145=null;
        Token UMOD146=null;
        EulangParser.unary_return l = null;

        EulangParser.unary_return r = null;


        CommonTree STAR142_tree=null;
        CommonTree SLASH143_tree=null;
        CommonTree BACKSLASH144_tree=null;
        CommonTree PERCENT145_tree=null;
        CommonTree UMOD146_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:6: ( (l= unary -> $l) ( ( STAR r= unary )=> STAR r= unary -> ^( MUL $l $r) | SLASH r= unary -> ^( DIV $l $r) | BACKSLASH r= unary -> ^( UDIV $l $r) | PERCENT r= unary -> ^( MOD $l $r) | UMOD r= unary -> ^( UMOD $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:8: (l= unary -> $l) ( ( STAR r= unary )=> STAR r= unary -> ^( MUL $l $r) | SLASH r= unary -> ^( DIV $l $r) | BACKSLASH r= unary -> ^( UDIV $l $r) | PERCENT r= unary -> ^( MOD $l $r) | UMOD r= unary -> ^( UMOD $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:8: (l= unary -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:10: l= unary
            {
            pushFollow(FOLLOW_unary_in_term2563);
            l=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 239:35: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:240:9: ( ( STAR r= unary )=> STAR r= unary -> ^( MUL $l $r) | SLASH r= unary -> ^( DIV $l $r) | BACKSLASH r= unary -> ^( UDIV $l $r) | PERCENT r= unary -> ^( MOD $l $r) | UMOD r= unary -> ^( UMOD $l $r) )*
            loop46:
            do {
                int alt46=6;
                alt46 = dfa46.predict(input);
                switch (alt46) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:240:12: ( STAR r= unary )=> STAR r= unary
            	    {
            	    STAR142=(Token)match(input,STAR,FOLLOW_STAR_in_term2612); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR142);

            	    pushFollow(FOLLOW_unary_in_term2616);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 240:57: -> ^( MUL $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:240:60: ^( MUL $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MUL, "MUL"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:241:11: SLASH r= unary
            	    {
            	    SLASH143=(Token)match(input,SLASH,FOLLOW_SLASH_in_term2653); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH143);

            	    pushFollow(FOLLOW_unary_in_term2657);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 241:36: -> ^( DIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:241:39: ^( DIV $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DIV, "DIV"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:242:11: BACKSLASH r= unary
            	    {
            	    BACKSLASH144=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_term2692); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BACKSLASH.add(BACKSLASH144);

            	    pushFollow(FOLLOW_unary_in_term2696);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 242:40: -> ^( UDIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:242:43: ^( UDIV $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UDIV, "UDIV"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:243:11: PERCENT r= unary
            	    {
            	    PERCENT145=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_term2731); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT145);

            	    pushFollow(FOLLOW_unary_in_term2735);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 243:38: -> ^( MOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:243:41: ^( MOD $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MOD, "MOD"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:244:11: UMOD r= unary
            	    {
            	    UMOD146=(Token)match(input,UMOD,FOLLOW_UMOD_in_term2770); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UMOD.add(UMOD146);

            	    pushFollow(FOLLOW_unary_in_term2774);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: UMOD, r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 244:35: -> ^( UMOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:244:38: ^( UMOD $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UMOD.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop46;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:249:1: unary : ( ( atom -> atom ) | ( MINUS u= unary )=> MINUS u= unary -> ^( NEG $u) | EXCL u= unary -> ^( NOT $u) | TILDE u= unary -> ^( INV $u) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS148=null;
        Token EXCL149=null;
        Token TILDE150=null;
        EulangParser.unary_return u = null;

        EulangParser.atom_return atom147 = null;


        CommonTree MINUS148_tree=null;
        CommonTree EXCL149_tree=null;
        CommonTree TILDE150_tree=null;
        RewriteRuleTokenStream stream_EXCL=new RewriteRuleTokenStream(adaptor,"token EXCL");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:249:6: ( ( atom -> atom ) | ( MINUS u= unary )=> MINUS u= unary -> ^( NEG $u) | EXCL u= unary -> ^( NOT $u) | TILDE u= unary -> ^( INV $u) )
            int alt47=4;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==COLON||LA47_0==STAR||LA47_0==LPAREN||(LA47_0>=NUMBER && LA47_0<=COLONS)||(LA47_0>=CHAR_LITERAL && LA47_0<=STRING_LITERAL)) ) {
                alt47=1;
            }
            else if ( (LA47_0==MINUS) && (synpred5_EulangParser())) {
                alt47=2;
            }
            else if ( (LA47_0==EXCL) ) {
                alt47=3;
            }
            else if ( (LA47_0==TILDE) ) {
                alt47=4;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:249:11: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:249:11: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:249:13: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary2851);
                    atom147=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom147.getTree());


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
                    // 249:25: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:250:9: ( MINUS u= unary )=> MINUS u= unary
                    {
                    MINUS148=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary2897); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS148);

                    pushFollow(FOLLOW_unary_in_unary2901);
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
                    // 250:47: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:250:50: ^( NEG $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:251:9: EXCL u= unary
                    {
                    EXCL149=(Token)match(input,EXCL,FOLLOW_EXCL_in_unary2921); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EXCL.add(EXCL149);

                    pushFollow(FOLLOW_unary_in_unary2925);
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
                    // 251:26: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:251:29: ^( NOT $u)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NOT, "NOT"), root_1);

                        adaptor.addChild(root_1, stream_u.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:252:9: TILDE u= unary
                    {
                    TILDE150=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary2949); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE150);

                    pushFollow(FOLLOW_unary_in_unary2953);
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
                    // 252:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:252:30: ^( INV $u)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:254:1: atom options {k=2; } : ( NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef | LPAREN assignExpr RPAREN -> assignExpr );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER151=null;
        Token CHAR_LITERAL152=null;
        Token STRING_LITERAL153=null;
        Token STAR154=null;
        Token LPAREN157=null;
        Token RPAREN159=null;
        EulangParser.funcCall_return f = null;

        EulangParser.funcCall_return funcCall155 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef156 = null;

        EulangParser.assignExpr_return assignExpr158 = null;


        CommonTree NUMBER151_tree=null;
        CommonTree CHAR_LITERAL152_tree=null;
        CommonTree STRING_LITERAL153_tree=null;
        CommonTree STAR154_tree=null;
        CommonTree LPAREN157_tree=null;
        CommonTree RPAREN159_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_funcCall=new RewriteRuleSubtreeStream(adaptor,"rule funcCall");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:254:23: ( NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef | LPAREN assignExpr RPAREN -> assignExpr )
            int alt48=7;
            alt48 = dfa48.predict(input);
            switch (alt48) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:255:7: NUMBER
                    {
                    NUMBER151=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom2990); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER151);



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
                    // 255:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:255:42: ^( LIT NUMBER )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:256:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL152=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom3033); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL152);



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
                    // 256:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:256:42: ^( LIT CHAR_LITERAL )
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
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:257:9: STRING_LITERAL
                    {
                    STRING_LITERAL153=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom3068); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL153);



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
                    // 257:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:257:42: ^( LIT STRING_LITERAL )
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
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:258:9: ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall
                    {
                    STAR154=(Token)match(input,STAR,FOLLOW_STAR_in_atom3112); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STAR.add(STAR154);

                    pushFollow(FOLLOW_funcCall_in_atom3116);
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
                    // 258:57: -> ^( INLINE $f)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:258:60: ^( INLINE $f)
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
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:259:9: ( idOrScopeRef LPAREN )=> funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom3145);
                    funcCall155=funcCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_funcCall.add(funcCall155.getTree());


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
                    // 259:46: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:260:9: idOrScopeRef
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_idOrScopeRef_in_atom3161);
                    idOrScopeRef156=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, idOrScopeRef156.getTree());

                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:261:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN157=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom3171); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN157);

                    pushFollow(FOLLOW_assignExpr_in_atom3173);
                    assignExpr158=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr158.getTree());
                    RPAREN159=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom3175); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN159);



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
                    // 261:48: -> assignExpr
                    {
                        adaptor.addChild(root_0, stream_assignExpr.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:264:1: idOrScopeRef : ( ID -> ^( IDREF ID ) | SCOPEREF -> ^( IDREF SCOPEREF ) | COLONS ( ( SCOPEREF -> ^( IDREF COLONS SCOPEREF ) ) | ( ID -> ^( IDREF COLONS ID ) ) ) | ( COLON )+ ( ( SCOPEREF -> ^( IDREF ( COLON )+ SCOPEREF ) ) | ( ID -> ^( IDREF ( COLON )+ ID ) ) ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID160=null;
        Token SCOPEREF161=null;
        Token COLONS162=null;
        Token SCOPEREF163=null;
        Token ID164=null;
        Token COLON165=null;
        Token SCOPEREF166=null;
        Token ID167=null;

        CommonTree ID160_tree=null;
        CommonTree SCOPEREF161_tree=null;
        CommonTree COLONS162_tree=null;
        CommonTree SCOPEREF163_tree=null;
        CommonTree ID164_tree=null;
        CommonTree COLON165_tree=null;
        CommonTree SCOPEREF166_tree=null;
        CommonTree ID167_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_SCOPEREF=new RewriteRuleTokenStream(adaptor,"token SCOPEREF");
        RewriteRuleTokenStream stream_COLONS=new RewriteRuleTokenStream(adaptor,"token COLONS");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:264:14: ( ID -> ^( IDREF ID ) | SCOPEREF -> ^( IDREF SCOPEREF ) | COLONS ( ( SCOPEREF -> ^( IDREF COLONS SCOPEREF ) ) | ( ID -> ^( IDREF COLONS ID ) ) ) | ( COLON )+ ( ( SCOPEREF -> ^( IDREF ( COLON )+ SCOPEREF ) ) | ( ID -> ^( IDREF ( COLON )+ ID ) ) ) )
            int alt52=4;
            switch ( input.LA(1) ) {
            case ID:
                {
                alt52=1;
                }
                break;
            case SCOPEREF:
                {
                alt52=2;
                }
                break;
            case COLONS:
                {
                alt52=3;
                }
                break;
            case COLON:
                {
                alt52=4;
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:264:16: ID
                    {
                    ID160=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef3207); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID160);



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
                    // 264:19: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:264:22: ^( IDREF ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:265:9: SCOPEREF
                    {
                    SCOPEREF161=(Token)match(input,SCOPEREF,FOLLOW_SCOPEREF_in_idOrScopeRef3227); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SCOPEREF.add(SCOPEREF161);



                    // AST REWRITE
                    // elements: SCOPEREF
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 265:18: -> ^( IDREF SCOPEREF )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:265:21: ^( IDREF SCOPEREF )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, stream_SCOPEREF.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:9: COLONS ( ( SCOPEREF -> ^( IDREF COLONS SCOPEREF ) ) | ( ID -> ^( IDREF COLONS ID ) ) )
                    {
                    COLONS162=(Token)match(input,COLONS,FOLLOW_COLONS_in_idOrScopeRef3245); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLONS.add(COLONS162);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:16: ( ( SCOPEREF -> ^( IDREF COLONS SCOPEREF ) ) | ( ID -> ^( IDREF COLONS ID ) ) )
                    int alt49=2;
                    int LA49_0 = input.LA(1);

                    if ( (LA49_0==SCOPEREF) ) {
                        alt49=1;
                    }
                    else if ( (LA49_0==ID) ) {
                        alt49=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 49, 0, input);

                        throw nvae;
                    }
                    switch (alt49) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:18: ( SCOPEREF -> ^( IDREF COLONS SCOPEREF ) )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:18: ( SCOPEREF -> ^( IDREF COLONS SCOPEREF ) )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:20: SCOPEREF
                            {
                            SCOPEREF163=(Token)match(input,SCOPEREF,FOLLOW_SCOPEREF_in_idOrScopeRef3251); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SCOPEREF.add(SCOPEREF163);



                            // AST REWRITE
                            // elements: SCOPEREF, COLONS
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 266:29: -> ^( IDREF COLONS SCOPEREF )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:32: ^( IDREF COLONS SCOPEREF )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                                adaptor.addChild(root_1, stream_COLONS.nextNode());
                                adaptor.addChild(root_1, stream_SCOPEREF.nextNode());

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }


                            }
                            break;
                        case 2 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:61: ( ID -> ^( IDREF COLONS ID ) )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:61: ( ID -> ^( IDREF COLONS ID ) )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:63: ID
                            {
                            ID164=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef3269); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_ID.add(ID164);



                            // AST REWRITE
                            // elements: ID, COLONS
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 266:66: -> ^( IDREF COLONS ID )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:266:69: ^( IDREF COLONS ID )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                                adaptor.addChild(root_1, stream_COLONS.nextNode());
                                adaptor.addChild(root_1, stream_ID.nextNode());

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }


                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:9: ( COLON )+ ( ( SCOPEREF -> ^( IDREF ( COLON )+ SCOPEREF ) ) | ( ID -> ^( IDREF ( COLON )+ ID ) ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:9: ( COLON )+
                    int cnt50=0;
                    loop50:
                    do {
                        int alt50=2;
                        int LA50_0 = input.LA(1);

                        if ( (LA50_0==COLON) ) {
                            alt50=1;
                        }


                        switch (alt50) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:9: COLON
                    	    {
                    	    COLON165=(Token)match(input,COLON,FOLLOW_COLON_in_idOrScopeRef3293); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COLON.add(COLON165);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt50 >= 1 ) break loop50;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(50, input);
                                throw eee;
                        }
                        cnt50++;
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:16: ( ( SCOPEREF -> ^( IDREF ( COLON )+ SCOPEREF ) ) | ( ID -> ^( IDREF ( COLON )+ ID ) ) )
                    int alt51=2;
                    int LA51_0 = input.LA(1);

                    if ( (LA51_0==SCOPEREF) ) {
                        alt51=1;
                    }
                    else if ( (LA51_0==ID) ) {
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
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:18: ( SCOPEREF -> ^( IDREF ( COLON )+ SCOPEREF ) )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:18: ( SCOPEREF -> ^( IDREF ( COLON )+ SCOPEREF ) )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:20: SCOPEREF
                            {
                            SCOPEREF166=(Token)match(input,SCOPEREF,FOLLOW_SCOPEREF_in_idOrScopeRef3300); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SCOPEREF.add(SCOPEREF166);



                            // AST REWRITE
                            // elements: COLON, SCOPEREF
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 267:29: -> ^( IDREF ( COLON )+ SCOPEREF )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:32: ^( IDREF ( COLON )+ SCOPEREF )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                                if ( !(stream_COLON.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_COLON.hasNext() ) {
                                    adaptor.addChild(root_1, stream_COLON.nextNode());

                                }
                                stream_COLON.reset();
                                adaptor.addChild(root_1, stream_SCOPEREF.nextNode());

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }


                            }
                            break;
                        case 2 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:61: ( ID -> ^( IDREF ( COLON )+ ID ) )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:61: ( ID -> ^( IDREF ( COLON )+ ID ) )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:63: ID
                            {
                            ID167=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef3319); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_ID.add(ID167);



                            // AST REWRITE
                            // elements: COLON, ID
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 267:66: -> ^( IDREF ( COLON )+ ID )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:267:69: ^( IDREF ( COLON )+ ID )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                                if ( !(stream_COLON.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_COLON.hasNext() ) {
                                    adaptor.addChild(root_1, stream_COLON.nextNode());

                                }
                                stream_COLON.reset();
                                adaptor.addChild(root_1, stream_ID.nextNode());

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }


                            }
                            break;

                    }


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

    // $ANTLR start synpred1_EulangParser
    public final void synpred1_EulangParser_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:7: ( LPAREN ( RPAREN | ID ) )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:10: LPAREN ( RPAREN | ID )
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred1_EulangParser449); if (state.failed) return ;
        if ( input.LA(1)==RPAREN||input.LA(1)==ID ) {
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
    // $ANTLR end synpred1_EulangParser

    // $ANTLR start synpred2_EulangParser
    public final void synpred2_EulangParser_fragment() throws RecognitionException {   
        EulangParser.cond_return t = null;


        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:194:9: ( QUESTION t= cond )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:194:11: QUESTION t= cond
        {
        match(input,QUESTION,FOLLOW_QUESTION_in_synpred2_EulangParser1725); if (state.failed) return ;
        pushFollow(FOLLOW_cond_in_synpred2_EulangParser1729);
        t=cond();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_EulangParser

    // $ANTLR start synpred3_EulangParser
    public final void synpred3_EulangParser_fragment() throws RecognitionException {   
        EulangParser.term_return r = null;


        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:235:13: ( MINUS r= term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:235:15: MINUS r= term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred3_EulangParser2502); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred3_EulangParser2506);
        r=term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_EulangParser

    // $ANTLR start synpred4_EulangParser
    public final void synpred4_EulangParser_fragment() throws RecognitionException {   
        EulangParser.unary_return r = null;


        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:240:12: ( STAR r= unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:240:14: STAR r= unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred4_EulangParser2602); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred4_EulangParser2606);
        r=unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_EulangParser

    // $ANTLR start synpred5_EulangParser
    public final void synpred5_EulangParser_fragment() throws RecognitionException {   
        EulangParser.unary_return u = null;


        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:250:9: ( MINUS u= unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:250:11: MINUS u= unary
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred5_EulangParser2884); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred5_EulangParser2888);
        u=unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_EulangParser

    // $ANTLR start synpred6_EulangParser
    public final void synpred6_EulangParser_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:258:9: ( STAR idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:258:11: STAR idOrScopeRef LPAREN
        {
        match(input,STAR,FOLLOW_STAR_in_synpred6_EulangParser3103); if (state.failed) return ;
        pushFollow(FOLLOW_idOrScopeRef_in_synpred6_EulangParser3105);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred6_EulangParser3107); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_EulangParser

    // $ANTLR start synpred7_EulangParser
    public final void synpred7_EulangParser_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:259:9: ( idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:259:10: idOrScopeRef LPAREN
        {
        pushFollow(FOLLOW_idOrScopeRef_in_synpred7_EulangParser3137);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred7_EulangParser3139); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_EulangParser

    // Delegated rules

    public final boolean synpred6_EulangParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_EulangParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_EulangParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_EulangParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_EulangParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_EulangParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_EulangParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_EulangParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_EulangParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_EulangParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred4_EulangParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred4_EulangParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_EulangParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_EulangParser_fragment(); // can never throw exception
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
    protected DFA35 dfa35 = new DFA35(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA46 dfa46 = new DFA46(this);
    protected DFA48 dfa48 = new DFA48(this);
    static final String DFA4_eotS =
        "\15\uffff";
    static final String DFA4_eofS =
        "\15\uffff";
    static final String DFA4_minS =
        "\1\4\3\uffff\1\7\2\uffff\1\7\2\uffff\1\0\2\uffff";
    static final String DFA4_maxS =
        "\1\72\3\uffff\1\72\2\uffff\1\140\2\uffff\1\0\2\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\uffff\1\5\1\6\1\uffff\2\4\1\uffff\2\4";
    static final String DFA4_specialS =
        "\4\uffff\1\0\2\uffff\1\1\2\uffff\1\2\2\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\2\1\uffff\1\3\1\6\5\uffff\2\6\1\uffff\1\4\1\uffff\1\1\1\uffff"+
            "\1\5\2\uffff\2\6\32\uffff\4\6\2\uffff\2\6",
            "",
            "",
            "",
            "\1\6\5\uffff\2\6\1\uffff\1\6\1\11\5\uffff\2\6\24\uffff\1\10"+
            "\5\uffff\1\6\1\7\2\6\2\uffff\2\6",
            "",
            "",
            "\1\13\1\14\1\6\2\uffff\5\6\1\12\10\uffff\3\6\1\uffff\17\6\1"+
            "\10\62\uffff\1\6",
            "",
            "",
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
            return "78:1: toplevelvalue : ( xscope | code | macro | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_4 = input.LA(1);

                         
                        int index4_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_4==ID) ) {s = 7;}

                        else if ( (LA4_4==COLON||(LA4_4>=MINUS && LA4_4<=STAR)||LA4_4==LPAREN||(LA4_4>=EXCL && LA4_4<=TILDE)||LA4_4==NUMBER||(LA4_4>=SCOPEREF && LA4_4<=COLONS)||(LA4_4>=CHAR_LITERAL && LA4_4<=STRING_LITERAL)) ) {s = 6;}

                        else if ( (LA4_4==RETURNS) && (synpred1_EulangParser())) {s = 8;}

                        else if ( (LA4_4==RPAREN) && (synpred1_EulangParser())) {s = 9;}

                         
                        input.seek(index4_4);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_7 = input.LA(1);

                         
                        int index4_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_7==EQUALS||(LA4_7>=PLUS && LA4_7<=LPAREN)||(LA4_7>=AMP && LA4_7<=CARET)||(LA4_7>=QUESTION && LA4_7<=UMOD)||LA4_7==COMPXOR) ) {s = 6;}

                        else if ( (LA4_7==RPAREN) ) {s = 10;}

                        else if ( (LA4_7==COLON) && (synpred1_EulangParser())) {s = 11;}

                        else if ( (LA4_7==COMMA) && (synpred1_EulangParser())) {s = 12;}

                        else if ( (LA4_7==RETURNS) && (synpred1_EulangParser())) {s = 8;}

                         
                        input.seek(index4_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA4_10 = input.LA(1);

                         
                        int index4_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_EulangParser()) ) {s = 12;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index4_10);
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
    static final String DFA35_eotS =
        "\13\uffff";
    static final String DFA35_eofS =
        "\13\uffff";
    static final String DFA35_minS =
        "\1\7\2\10\1\64\1\7\2\uffff\4\10";
    static final String DFA35_maxS =
        "\1\72\2\140\2\65\2\uffff\4\140";
    static final String DFA35_acceptS =
        "\5\uffff\1\2\1\1\4\uffff";
    static final String DFA35_specialS =
        "\13\uffff}>";
    static final String[] DFA35_transitionS = {
            "\1\4\5\uffff\2\5\1\uffff\1\5\6\uffff\2\5\32\uffff\1\5\1\1\1"+
            "\2\1\3\2\uffff\2\5",
            "\1\5\1\6\2\uffff\6\5\1\uffff\1\5\6\uffff\24\5\62\uffff\1\5",
            "\1\5\1\6\2\uffff\6\5\1\uffff\1\5\6\uffff\24\5\62\uffff\1\5",
            "\1\10\1\7",
            "\1\4\54\uffff\1\12\1\11",
            "",
            "",
            "\1\5\1\6\2\uffff\6\5\1\uffff\1\5\6\uffff\24\5\62\uffff\1\5",
            "\1\5\1\6\2\uffff\6\5\1\uffff\1\5\6\uffff\24\5\62\uffff\1\5",
            "\1\5\1\6\2\uffff\6\5\1\uffff\1\5\6\uffff\24\5\62\uffff\1\5",
            "\1\5\1\6\2\uffff\6\5\1\uffff\1\5\6\uffff\24\5\62\uffff\1\5"
    };

    static final short[] DFA35_eot = DFA.unpackEncodedString(DFA35_eotS);
    static final short[] DFA35_eof = DFA.unpackEncodedString(DFA35_eofS);
    static final char[] DFA35_min = DFA.unpackEncodedStringToUnsignedChars(DFA35_minS);
    static final char[] DFA35_max = DFA.unpackEncodedStringToUnsignedChars(DFA35_maxS);
    static final short[] DFA35_accept = DFA.unpackEncodedString(DFA35_acceptS);
    static final short[] DFA35_special = DFA.unpackEncodedString(DFA35_specialS);
    static final short[][] DFA35_transition;

    static {
        int numStates = DFA35_transitionS.length;
        DFA35_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA35_transition[i] = DFA.unpackEncodedString(DFA35_transitionS[i]);
        }
    }

    class DFA35 extends DFA {

        public DFA35(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 35;
            this.eot = DFA35_eot;
            this.eof = DFA35_eof;
            this.min = DFA35_min;
            this.max = DFA35_max;
            this.accept = DFA35_accept;
            this.special = DFA35_special;
            this.transition = DFA35_transition;
        }
        public String getDescription() {
            return "172:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | rhsExpr -> rhsExpr );";
        }
    }
    static final String DFA45_eotS =
        "\47\uffff";
    static final String DFA45_eofS =
        "\1\1\46\uffff";
    static final String DFA45_minS =
        "\1\7\34\uffff\1\0\11\uffff";
    static final String DFA45_maxS =
        "\1\140\34\uffff\1\0\11\uffff";
    static final String DFA45_acceptS =
        "\1\uffff\1\3\43\uffff\1\1\1\2";
    static final String DFA45_specialS =
        "\35\uffff\1\0\11\uffff}>";
    static final String[] DFA45_transitionS = {
            "\2\1\3\uffff\1\45\1\35\1\1\1\uffff\4\1\1\uffff\1\1\1\uffff\2"+
            "\1\1\uffff\20\1\3\uffff\1\1\5\uffff\4\1\2\uffff\2\1\45\uffff"+
            "\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
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
            return "()* loopback of 234:9: ( PLUS r= term -> ^( ADD $l $r) | ( MINUS r= term )=> MINUS r= term -> ^( SUB $l $r) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA45_29 = input.LA(1);

                         
                        int index45_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_EulangParser()) ) {s = 38;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index45_29);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 45, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA46_eotS =
        "\53\uffff";
    static final String DFA46_eofS =
        "\1\1\52\uffff";
    static final String DFA46_minS =
        "\1\7\31\uffff\1\0\20\uffff";
    static final String DFA46_maxS =
        "\1\140\31\uffff\1\0\20\uffff";
    static final String DFA46_acceptS =
        "\1\uffff\1\6\44\uffff\1\2\1\3\1\4\1\5\1\1";
    static final String DFA46_specialS =
        "\32\uffff\1\0\20\uffff}>";
    static final String[] DFA46_transitionS = {
            "\2\1\3\uffff\2\1\1\32\1\46\4\1\1\uffff\1\1\1\uffff\2\1\1\uffff"+
            "\20\1\1\47\1\50\1\51\1\1\5\uffff\4\1\2\uffff\2\1\45\uffff\1"+
            "\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA46_eot = DFA.unpackEncodedString(DFA46_eotS);
    static final short[] DFA46_eof = DFA.unpackEncodedString(DFA46_eofS);
    static final char[] DFA46_min = DFA.unpackEncodedStringToUnsignedChars(DFA46_minS);
    static final char[] DFA46_max = DFA.unpackEncodedStringToUnsignedChars(DFA46_maxS);
    static final short[] DFA46_accept = DFA.unpackEncodedString(DFA46_acceptS);
    static final short[] DFA46_special = DFA.unpackEncodedString(DFA46_specialS);
    static final short[][] DFA46_transition;

    static {
        int numStates = DFA46_transitionS.length;
        DFA46_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA46_transition[i] = DFA.unpackEncodedString(DFA46_transitionS[i]);
        }
    }

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = DFA46_eot;
            this.eof = DFA46_eof;
            this.min = DFA46_min;
            this.max = DFA46_max;
            this.accept = DFA46_accept;
            this.special = DFA46_special;
            this.transition = DFA46_transition;
        }
        public String getDescription() {
            return "()* loopback of 240:9: ( ( STAR r= unary )=> STAR r= unary -> ^( MUL $l $r) | SLASH r= unary -> ^( DIV $l $r) | BACKSLASH r= unary -> ^( UDIV $l $r) | PERCENT r= unary -> ^( MOD $l $r) | UMOD r= unary -> ^( UMOD $l $r) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA46_26 = input.LA(1);

                         
                        int index46_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_EulangParser()) ) {s = 42;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index46_26);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 46, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA48_eotS =
        "\142\uffff";
    static final String DFA48_eofS =
        "\5\uffff\2\12\133\uffff";
    static final String DFA48_minS =
        "\1\7\4\uffff\2\7\1\64\1\7\42\uffff\1\0\50\uffff\1\0\7\uffff\5\0"+
        "\1\uffff";
    static final String DFA48_maxS =
        "\1\72\4\uffff\2\140\2\65\42\uffff\1\0\50\uffff\1\0\7\uffff\5\0\1"+
        "\uffff";
    static final String DFA48_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\4\uffff\1\7\1\6\126\uffff\1\5";
    static final String DFA48_specialS =
        "\1\0\52\uffff\1\1\50\uffff\1\2\7\uffff\1\3\1\4\1\5\1\6\1\7\1\uffff}>";
    static final String[] DFA48_transitionS = {
            "\1\10\6\uffff\1\4\1\uffff\1\11\42\uffff\1\1\1\5\1\6\1\7\2\uffff"+
            "\1\2\1\3",
            "",
            "",
            "",
            "",
            "\2\12\3\uffff\4\12\1\53\3\12\1\uffff\1\12\1\uffff\2\12\1\uffff"+
            "\24\12\5\uffff\4\12\2\uffff\2\12\45\uffff\1\12",
            "\2\12\3\uffff\4\12\1\124\3\12\1\uffff\1\12\1\uffff\2\12\1\uffff"+
            "\24\12\5\uffff\4\12\2\uffff\2\12\45\uffff\1\12",
            "\1\135\1\134",
            "\1\140\54\uffff\1\137\1\136",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "\1\uffff",
            "\1\uffff",
            ""
    };

    static final short[] DFA48_eot = DFA.unpackEncodedString(DFA48_eotS);
    static final short[] DFA48_eof = DFA.unpackEncodedString(DFA48_eofS);
    static final char[] DFA48_min = DFA.unpackEncodedStringToUnsignedChars(DFA48_minS);
    static final char[] DFA48_max = DFA.unpackEncodedStringToUnsignedChars(DFA48_maxS);
    static final short[] DFA48_accept = DFA.unpackEncodedString(DFA48_acceptS);
    static final short[] DFA48_special = DFA.unpackEncodedString(DFA48_specialS);
    static final short[][] DFA48_transition;

    static {
        int numStates = DFA48_transitionS.length;
        DFA48_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA48_transition[i] = DFA.unpackEncodedString(DFA48_transitionS[i]);
        }
    }

    class DFA48 extends DFA {

        public DFA48(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 48;
            this.eot = DFA48_eot;
            this.eof = DFA48_eof;
            this.min = DFA48_min;
            this.max = DFA48_max;
            this.accept = DFA48_accept;
            this.special = DFA48_special;
            this.transition = DFA48_transition;
        }
        public String getDescription() {
            return "254:1: atom options {k=2; } : ( NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef | LPAREN assignExpr RPAREN -> assignExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA48_0 = input.LA(1);

                         
                        int index48_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA48_0==NUMBER) ) {s = 1;}

                        else if ( (LA48_0==CHAR_LITERAL) ) {s = 2;}

                        else if ( (LA48_0==STRING_LITERAL) ) {s = 3;}

                        else if ( (LA48_0==STAR) && (synpred6_EulangParser())) {s = 4;}

                        else if ( (LA48_0==ID) ) {s = 5;}

                        else if ( (LA48_0==SCOPEREF) ) {s = 6;}

                        else if ( (LA48_0==COLONS) ) {s = 7;}

                        else if ( (LA48_0==COLON) ) {s = 8;}

                        else if ( (LA48_0==LPAREN) ) {s = 9;}

                         
                        input.seek(index48_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA48_43 = input.LA(1);

                         
                        int index48_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_EulangParser()) ) {s = 97;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index48_43);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA48_84 = input.LA(1);

                         
                        int index48_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_EulangParser()) ) {s = 97;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index48_84);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA48_92 = input.LA(1);

                         
                        int index48_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_EulangParser()) ) {s = 97;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index48_92);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA48_93 = input.LA(1);

                         
                        int index48_93 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_EulangParser()) ) {s = 97;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index48_93);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA48_94 = input.LA(1);

                         
                        int index48_94 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_EulangParser()) ) {s = 97;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index48_94);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA48_95 = input.LA(1);

                         
                        int index48_95 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_EulangParser()) ) {s = 97;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index48_95);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA48_96 = input.LA(1);

                         
                        int index48_96 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_EulangParser()) ) {s = 97;}

                        else if ( (true) ) {s = 10;}

                         
                        input.seek(index48_96);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 48, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog252 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts283 = new BitSet(new long[]{0x0678000001856082L});
    public static final BitSet FOLLOW_ID_in_toplevelstat317 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat319 = new BitSet(new long[]{0x06780000019560D0L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat321 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat346 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelstat348 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat350 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat353 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat372 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstat409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_toplevelvalue430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_toplevelvalue438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_toplevelvalue463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector497 = new BitSet(new long[]{0x0001000000200050L});
    public static final BitSet FOLLOW_selectors_in_selector499 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector501 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors527 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_selectors531 = new BitSet(new long[]{0x0001000000000050L});
    public static final BitSet FOLLOW_selectoritem_in_selectors533 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_selectors538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listCompr_in_selectoritem563 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_selectoritem567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_selectoritem571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope582 = new BitSet(new long[]{0x0678000001856080L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope584 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr613 = new BitSet(new long[]{0x0001000000000080L});
    public static final BitSet FOLLOW_COLON_in_listCompr616 = new BitSet(new long[]{0x0000000000010050L});
    public static final BitSet FOLLOW_listiterable_in_listCompr618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn650 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_idlist_in_forIn652 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_IN_in_forIn654 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_list_in_forIn656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist681 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_idlist684 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_ID_in_idlist686 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_code_in_listiterable715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_listiterable719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_listiterable723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list738 = new BitSet(new long[]{0x0678000001B560D0L});
    public static final BitSet FOLLOW_listitems_in_list740 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACKET_in_list742 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems772 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_listitems776 = new BitSet(new long[]{0x06780000019560D0L});
    public static final BitSet FOLLOW_listitem_in_listitems778 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_listitems783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto829 = new BitSet(new long[]{0x0010200000020000L});
    public static final BitSet FOLLOW_protoargdefs_in_proto831 = new BitSet(new long[]{0x0000200000020000L});
    public static final BitSet FOLLOW_xreturns_in_proto833 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_proto836 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protoargdef_in_protoargdefs879 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_protoargdefs883 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_protoargdef_in_protoargdefs885 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_protoargdefs889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_protoargdef934 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COLON_in_protoargdef937 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_type_in_protoargdef939 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_EQUALS_in_protoargdef942 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_rhsExpr_in_protoargdef944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_LPAREN_in_code981 = new BitSet(new long[]{0x0010200000020000L});
    public static final BitSet FOLLOW_argdefs_in_code983 = new BitSet(new long[]{0x0000200000020000L});
    public static final BitSet FOLLOW_xreturns_in_code985 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_code988 = new BitSet(new long[]{0x0678800001896080L});
    public static final BitSet FOLLOW_codestmtlist_in_code990 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_RBRACE_in_code992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_STAR_LPAREN_in_macro1029 = new BitSet(new long[]{0x0010200000020000L});
    public static final BitSet FOLLOW_argdefs_in_macro1031 = new BitSet(new long[]{0x0000200000020000L});
    public static final BitSet FOLLOW_xreturns_in_macro1033 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_macro1036 = new BitSet(new long[]{0x0678800001896080L});
    public static final BitSet FOLLOW_codestmtlist_in_macro1038 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_RBRACE_in_macro1040 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdef_in_argdefs1074 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1078 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_argdef_in_argdefs1080 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protoargdef_in_argdef1129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdef1137 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_argdef1139 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_assignExpr_in_argdef1141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_xreturns1166 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_type_in_xreturns1168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_type1193 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_AMP_in_type1213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1248 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist1252 = new BitSet(new long[]{0x0678800001816080L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1254 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist1259 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmt1290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_codeStmt1300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_returnExpr_in_codeStmt1310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1324 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1326 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1356 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_COLON_in_varDecl1358 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_type_in_varDecl1360 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1363 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1365 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_returnExpr1394 = new BitSet(new long[]{0x0678000001816082L});
    public static final BitSet FOLLOW_assignExpr_in_returnExpr1396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignExpr1437 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr1439 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr1441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr1466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_rhsExpr1513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_funcCall1559 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall1561 = new BitSet(new long[]{0x0678000001836080L});
    public static final BitSet FOLLOW_arglist_in_funcCall1563 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall1565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist1596 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_arglist1600 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_arg_in_arglist1602 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_arglist1606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg1655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logcond_in_cond1705 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond1735 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_cond_in_cond1739 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_COLON_in_cond1741 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_cond_in_cond1745 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1783 = new BitSet(new long[]{0x0000000180000002L,0x0000000100000000L});
    public static final BitSet FOLLOW_COMPAND_in_logcond1810 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1814 = new BitSet(new long[]{0x0000000180000002L,0x0000000100000000L});
    public static final BitSet FOLLOW_COMPOR_in_logcond1836 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1840 = new BitSet(new long[]{0x0000000180000002L,0x0000000100000000L});
    public static final BitSet FOLLOW_COMPXOR_in_logcond1862 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1866 = new BitSet(new long[]{0x0000000180000002L,0x0000000100000000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1900 = new BitSet(new long[]{0x000000001C000002L});
    public static final BitSet FOLLOW_AMP_in_binlogcond1929 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1933 = new BitSet(new long[]{0x000000001C000002L});
    public static final BitSet FOLLOW_BAR_in_binlogcond1956 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1960 = new BitSet(new long[]{0x000000001C000002L});
    public static final BitSet FOLLOW_CARET_in_binlogcond1983 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1987 = new BitSet(new long[]{0x000000001C000002L});
    public static final BitSet FOLLOW_comp_in_compeq2023 = new BitSet(new long[]{0x0000000600000002L});
    public static final BitSet FOLLOW_COMPEQ_in_compeq2057 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_comp_in_compeq2061 = new BitSet(new long[]{0x0000000600000002L});
    public static final BitSet FOLLOW_COMPNE_in_compeq2083 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_comp_in_compeq2087 = new BitSet(new long[]{0x0000000600000002L});
    public static final BitSet FOLLOW_shift_in_comp2121 = new BitSet(new long[]{0x0000007800000002L});
    public static final BitSet FOLLOW_COMPLE_in_comp2148 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_shift_in_comp2152 = new BitSet(new long[]{0x0000007800000002L});
    public static final BitSet FOLLOW_COMPGE_in_comp2177 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_shift_in_comp2181 = new BitSet(new long[]{0x0000007800000002L});
    public static final BitSet FOLLOW_LESS_in_comp2206 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_shift_in_comp2210 = new BitSet(new long[]{0x0000007800000002L});
    public static final BitSet FOLLOW_GREATER_in_comp2236 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_shift_in_comp2240 = new BitSet(new long[]{0x0000007800000002L});
    public static final BitSet FOLLOW_factor_in_shift2292 = new BitSet(new long[]{0x0000038000000002L});
    public static final BitSet FOLLOW_LSHIFT_in_shift2325 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_factor_in_shift2329 = new BitSet(new long[]{0x0000038000000002L});
    public static final BitSet FOLLOW_RSHIFT_in_shift2354 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_factor_in_shift2358 = new BitSet(new long[]{0x0000038000000002L});
    public static final BitSet FOLLOW_URSHIFT_in_shift2382 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_factor_in_shift2386 = new BitSet(new long[]{0x0000038000000002L});
    public static final BitSet FOLLOW_term_in_factor2428 = new BitSet(new long[]{0x0000000000003002L});
    public static final BitSet FOLLOW_PLUS_in_factor2462 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_term_in_factor2466 = new BitSet(new long[]{0x0000000000003002L});
    public static final BitSet FOLLOW_MINUS_in_factor2512 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_term_in_factor2516 = new BitSet(new long[]{0x0000000000003002L});
    public static final BitSet FOLLOW_unary_in_term2563 = new BitSet(new long[]{0x00001C000000C002L});
    public static final BitSet FOLLOW_STAR_in_term2612 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_term2616 = new BitSet(new long[]{0x00001C000000C002L});
    public static final BitSet FOLLOW_SLASH_in_term2653 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_term2657 = new BitSet(new long[]{0x00001C000000C002L});
    public static final BitSet FOLLOW_BACKSLASH_in_term2692 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_term2696 = new BitSet(new long[]{0x00001C000000C002L});
    public static final BitSet FOLLOW_PERCENT_in_term2731 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_term2735 = new BitSet(new long[]{0x00001C000000C002L});
    public static final BitSet FOLLOW_UMOD_in_term2770 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_term2774 = new BitSet(new long[]{0x00001C000000C002L});
    public static final BitSet FOLLOW_atom_in_unary2851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_unary2897 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_unary2901 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCL_in_unary2921 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_unary2925 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary2949 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_unary2953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom2990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom3033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom3068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_atom3112 = new BitSet(new long[]{0x0070000000000080L});
    public static final BitSet FOLLOW_funcCall_in_atom3116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom3145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_atom3161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom3171 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_assignExpr_in_atom3173 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_RPAREN_in_atom3175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef3207 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCOPEREF_in_idOrScopeRef3227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLONS_in_idOrScopeRef3245 = new BitSet(new long[]{0x0030000000000000L});
    public static final BitSet FOLLOW_SCOPEREF_in_idOrScopeRef3251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef3269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_idOrScopeRef3293 = new BitSet(new long[]{0x0030000000000080L});
    public static final BitSet FOLLOW_SCOPEREF_in_idOrScopeRef3300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef3319 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred1_EulangParser449 = new BitSet(new long[]{0x0010000000020000L});
    public static final BitSet FOLLOW_set_in_synpred1_EulangParser451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_synpred2_EulangParser1725 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_cond_in_synpred2_EulangParser1729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred3_EulangParser2502 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_term_in_synpred3_EulangParser2506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred4_EulangParser2602 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_synpred4_EulangParser2606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred5_EulangParser2884 = new BitSet(new long[]{0x0678000001816080L});
    public static final BitSet FOLLOW_unary_in_synpred5_EulangParser2888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred6_EulangParser3103 = new BitSet(new long[]{0x0070000000000080L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred6_EulangParser3105 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred6_EulangParser3107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred7_EulangParser3137 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred7_EulangParser3139 = new BitSet(new long[]{0x0000000000000002L});

}