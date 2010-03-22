// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g 2010-03-21 19:02:19

package org.ejs.eulang;
import java.util.HashMap;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class EulangParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "LBRACE_LPAREN", "LBRACE_STAR", "COLON", "COMMA", "EQUALS", "COLON_EQUALS", "COLON_COLON_EQUALS", "PLUS", "MINUS", "STAR", "SLASH", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "HASH", "EXCL", "TILDE", "AT", "AMP", "BAR", "CARET", "SEMI", "QUESTION", "COMPAND", "COMPOR", "COMPEQ", "COMPNE", "COMPGE", "COMPLE", "GREATER", "LESS", "LSHIFT", "RSHIFT", "URSHIFT", "BACKSLASH", "PERCENT", "UMOD", "IDSUFFIX", "NUMBER", "LETTERLIKE", "ID", "DIGIT", "CHAR_LITERAL", "STRING_LITERAL", "SPACE", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT", "CODE", "STMTLIST", "ARGLIST", "ARGDEF", "EXPR", "DEFINE_ASSIGN", "ASSIGN", "DEFINE", "LIST", "TYPE", "CALL", "COND", "BITAND", "BITOR", "BITXOR", "MUL", "DIV", "UDIV", "MOD", "COMPXOR"
    };
    public static final int STAR=13;
    public static final int MOD=74;
    public static final int LBRACE_STAR=5;
    public static final int AMP=25;
    public static final int LBRACE=17;
    public static final int MULTI_COMMENT=55;
    public static final int ARGLIST=58;
    public static final int EXCL=22;
    public static final int EQUALS=8;
    public static final int ID=47;
    public static final int DEFINE=63;
    public static final int SPACE=51;
    public static final int EOF=-1;
    public static final int BITAND=68;
    public static final int LPAREN=15;
    public static final int TYPE=65;
    public static final int CODE=56;
    public static final int AT=24;
    public static final int COLON_COLON_EQUALS=10;
    public static final int LBRACKET=19;
    public static final int RPAREN=16;
    public static final int IDSUFFIX=44;
    public static final int EXPR=60;
    public static final int STRING_LITERAL=50;
    public static final int SLASH=14;
    public static final int GREATER=36;
    public static final int COMMA=7;
    public static final int COMPLE=35;
    public static final int CARET=27;
    public static final int BITXOR=70;
    public static final int TILDE=23;
    public static final int LESS=37;
    public static final int PLUS=11;
    public static final int SINGLE_COMMENT=54;
    public static final int COMPAND=30;
    public static final int DIGIT=48;
    public static final int RBRACKET=20;
    public static final int RSHIFT=39;
    public static final int COMPGE=34;
    public static final int RBRACE=18;
    public static final int PERCENT=42;
    public static final int LETTERLIKE=46;
    public static final int UMOD=43;
    public static final int LSHIFT=38;
    public static final int COMPOR=31;
    public static final int UDIV=73;
    public static final int NUMBER=45;
    public static final int LBRACE_LPAREN=4;
    public static final int HASH=21;
    public static final int MINUS=12;
    public static final int LIST=64;
    public static final int MUL=71;
    public static final int SEMI=28;
    public static final int DEFINE_ASSIGN=61;
    public static final int ARGDEF=59;
    public static final int COLON=6;
    public static final int WS=53;
    public static final int BITOR=69;
    public static final int COLON_EQUALS=9;
    public static final int QUESTION=29;
    public static final int NEWLINE=52;
    public static final int CHAR_LITERAL=49;
    public static final int COMPXOR=75;
    public static final int STMTLIST=57;
    public static final int ASSIGN=62;
    public static final int URSHIFT=40;
    public static final int COMPEQ=32;
    public static final int CALL=66;
    public static final int DIV=72;
    public static final int COND=67;
    public static final int COMPNE=33;
    public static final int BAR=26;
    public static final int BACKSLASH=41;

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




    public static class prog_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prog"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:40:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:40:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:40:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog160);
            toplevelstmts1=toplevelstmts();

            state._fsp--;

            adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog162); 

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:1: toplevelstmts : ( stat )* -> ^( STMTLIST ( stat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.stat_return stat3 = null;


        RewriteRuleSubtreeStream stream_stat=new RewriteRuleSubtreeStream(adaptor,"rule stat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:14: ( ( stat )* -> ^( STMTLIST ( stat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:16: ( stat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:16: ( stat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==MINUS||LA1_0==LPAREN||LA1_0==NUMBER||LA1_0==ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:16: stat
            	    {
            	    pushFollow(FOLLOW_stat_in_toplevelstmts191);
            	    stat3=stat();

            	    state._fsp--;

            	    stream_stat.add(stat3.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);



            // AST REWRITE
            // elements: stat
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 43:27: -> ^( STMTLIST ( stat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:30: ^( STMTLIST ( stat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:41: ( stat )*
                while ( stream_stat.hasNext() ) {
                    adaptor.addChild(root_1, stream_stat.nextTree());

                }
                stream_stat.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class stat_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stat"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:1: stat : ( ID EQUALS value SEMI -> ^( DEFINE_ASSIGN ID value ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) );
    public final EulangParser.stat_return stat() throws RecognitionException {
        EulangParser.stat_return retval = new EulangParser.stat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID4=null;
        Token EQUALS5=null;
        Token SEMI7=null;
        Token ID8=null;
        Token COLON_EQUALS9=null;
        Token SEMI11=null;
        Token SEMI13=null;
        EulangParser.value_return value6 = null;

        EulangParser.rhsExpr_return rhsExpr10 = null;

        EulangParser.rhsExpr_return rhsExpr12 = null;


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
        RewriteRuleSubtreeStream stream_value=new RewriteRuleSubtreeStream(adaptor,"rule value");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:5: ( ID EQUALS value SEMI -> ^( DEFINE_ASSIGN ID value ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) )
            int alt3=3;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ID) ) {
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
                case PLUS:
                case MINUS:
                case STAR:
                case SLASH:
                case LPAREN:
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
                case COMPXOR:
                    {
                    alt3=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }

            }
            else if ( (LA3_0==MINUS||LA3_0==LPAREN||LA3_0==NUMBER) ) {
                alt3=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:9: ID EQUALS value SEMI
                    {
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_stat225);  
                    stream_ID.add(ID4);

                    EQUALS5=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_stat227);  
                    stream_EQUALS.add(EQUALS5);

                    pushFollow(FOLLOW_value_in_stat229);
                    value6=value();

                    state._fsp--;

                    stream_value.add(value6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_stat235);  
                    stream_SEMI.add(SEMI7);



                    // AST REWRITE
                    // elements: ID, value
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 46:35: -> ^( DEFINE_ASSIGN ID value )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:38: ^( DEFINE_ASSIGN ID value )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE_ASSIGN, "DEFINE_ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_value.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:47:7: ID COLON_EQUALS rhsExpr SEMI
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_stat254);  
                    stream_ID.add(ID8);

                    COLON_EQUALS9=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_stat256);  
                    stream_COLON_EQUALS.add(COLON_EQUALS9);

                    pushFollow(FOLLOW_rhsExpr_in_stat258);
                    rhsExpr10=rhsExpr();

                    state._fsp--;

                    stream_rhsExpr.add(rhsExpr10.getTree());
                    SEMI11=(Token)match(input,SEMI,FOLLOW_SEMI_in_stat261);  
                    stream_SEMI.add(SEMI11);



                    // AST REWRITE
                    // elements: rhsExpr, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 47:38: -> ^( DEFINE ID rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:47:41: ^( DEFINE ID rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:48:7: rhsExpr ( SEMI )?
                    {
                    pushFollow(FOLLOW_rhsExpr_in_stat280);
                    rhsExpr12=rhsExpr();

                    state._fsp--;

                    stream_rhsExpr.add(rhsExpr12.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:48:32: ( SEMI )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==SEMI) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:48:32: SEMI
                            {
                            SEMI13=(Token)match(input,SEMI,FOLLOW_SEMI_in_stat299);  
                            stream_SEMI.add(SEMI13);


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
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 48:39: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:48:42: ^( EXPR rhsExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "stat"

    public static class value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:1: value : ( code | selector | rhsExpr );
    public final EulangParser.value_return value() throws RecognitionException {
        EulangParser.value_return retval = new EulangParser.value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code14 = null;

        EulangParser.selector_return selector15 = null;

        EulangParser.rhsExpr_return rhsExpr16 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:6: ( code | selector | rhsExpr )
            int alt4=3;
            switch ( input.LA(1) ) {
            case LBRACE_LPAREN:
                {
                alt4=1;
                }
                break;
            case LBRACKET:
                {
                alt4=2;
                }
                break;
            case MINUS:
            case LPAREN:
            case NUMBER:
            case ID:
                {
                alt4=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:8: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_value321);
                    code14=code();

                    state._fsp--;

                    adaptor.addChild(root_0, code14.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:52:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_value336);
                    selector15=selector();

                    state._fsp--;

                    adaptor.addChild(root_0, selector15.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:53:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_value344);
                    rhsExpr16=rhsExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, rhsExpr16.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "value"

    public static class selector_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selector"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:1: selector : LBRACKET selectorlist RBRACKET -> ^( LIST ( selectorlist )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET17=null;
        Token RBRACKET19=null;
        EulangParser.selectorlist_return selectorlist18 = null;


        CommonTree LBRACKET17_tree=null;
        CommonTree RBRACKET19_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectorlist=new RewriteRuleSubtreeStream(adaptor,"rule selectorlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:9: ( LBRACKET selectorlist RBRACKET -> ^( LIST ( selectorlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:11: LBRACKET selectorlist RBRACKET
            {
            LBRACKET17=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector357);  
            stream_LBRACKET.add(LBRACKET17);

            pushFollow(FOLLOW_selectorlist_in_selector359);
            selectorlist18=selectorlist();

            state._fsp--;

            stream_selectorlist.add(selectorlist18.getTree());
            RBRACKET19=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector361);  
            stream_RBRACKET.add(RBRACKET19);



            // AST REWRITE
            // elements: selectorlist
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 57:45: -> ^( LIST ( selectorlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:48: ^( LIST ( selectorlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:55: ( selectorlist )*
                while ( stream_selectorlist.hasNext() ) {
                    adaptor.addChild(root_1, stream_selectorlist.nextTree());

                }
                stream_selectorlist.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class selectorlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectorlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:1: selectorlist : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? ;
    public final EulangParser.selectorlist_return selectorlist() throws RecognitionException {
        EulangParser.selectorlist_return retval = new EulangParser.selectorlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA21=null;
        Token COMMA23=null;
        EulangParser.selectoritem_return selectoritem20 = null;

        EulangParser.selectoritem_return selectoritem22 = null;


        CommonTree COMMA21_tree=null;
        CommonTree COMMA23_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:13: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:15: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:15: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==LBRACE_LPAREN) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:16: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectorlist387);
                    selectoritem20=selectoritem();

                    state._fsp--;

                    adaptor.addChild(root_0, selectoritem20.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:29: ( COMMA selectoritem )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            int LA5_1 = input.LA(2);

                            if ( (LA5_1==LBRACE_LPAREN) ) {
                                alt5=1;
                            }


                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:31: COMMA selectoritem
                    	    {
                    	    COMMA21=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectorlist391); 
                    	    COMMA21_tree = (CommonTree)adaptor.create(COMMA21);
                    	    adaptor.addChild(root_0, COMMA21_tree);

                    	    pushFollow(FOLLOW_selectoritem_in_selectorlist393);
                    	    selectoritem22=selectoritem();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, selectoritem22.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:53: ( COMMA )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==COMMA) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:53: COMMA
                            {
                            COMMA23=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectorlist398); 
                            COMMA23_tree = (CommonTree)adaptor.create(COMMA23);
                            adaptor.addChild(root_0, COMMA23_tree);


                            }
                            break;

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "selectorlist"

    public static class selectoritem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectoritem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:1: selectoritem : code ;
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code24 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:13: ( code )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:15: code
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_code_in_selectoritem423);
            code24=code();

            state._fsp--;

            adaptor.addChild(root_0, code24.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class code_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "code"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:1: code : LBRACE_LPAREN argdefs RPAREN codestmtlist RBRACE -> ^( CODE ( argdefs )* ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE_LPAREN25=null;
        Token RPAREN27=null;
        Token RBRACE29=null;
        EulangParser.argdefs_return argdefs26 = null;

        EulangParser.codestmtlist_return codestmtlist28 = null;


        CommonTree LBRACE_LPAREN25_tree=null;
        CommonTree RPAREN27_tree=null;
        CommonTree RBRACE29_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE_LPAREN=new RewriteRuleTokenStream(adaptor,"token LBRACE_LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:67:5: ( LBRACE_LPAREN argdefs RPAREN codestmtlist RBRACE -> ^( CODE ( argdefs )* ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:67:9: LBRACE_LPAREN argdefs RPAREN codestmtlist RBRACE
            {
            LBRACE_LPAREN25=(Token)match(input,LBRACE_LPAREN,FOLLOW_LBRACE_LPAREN_in_code441);  
            stream_LBRACE_LPAREN.add(LBRACE_LPAREN25);

            pushFollow(FOLLOW_argdefs_in_code443);
            argdefs26=argdefs();

            state._fsp--;

            stream_argdefs.add(argdefs26.getTree());
            RPAREN27=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_code445);  
            stream_RPAREN.add(RPAREN27);

            pushFollow(FOLLOW_codestmtlist_in_code447);
            codestmtlist28=codestmtlist();

            state._fsp--;

            stream_codestmtlist.add(codestmtlist28.getTree());
            RBRACE29=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code449);  
            stream_RBRACE.add(RBRACE29);



            // AST REWRITE
            // elements: codestmtlist, argdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 67:58: -> ^( CODE ( argdefs )* ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:67:61: ^( CODE ( argdefs )* ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CODE, "CODE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:67:68: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_1, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:67:77: ( codestmtlist )*
                while ( stream_codestmtlist.hasNext() ) {
                    adaptor.addChild(root_1, stream_codestmtlist.nextTree());

                }
                stream_codestmtlist.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class argdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:1: argdefs : ( argdef ( COMMA argdef )* ( COMMA )? )? -> ^( ARGLIST ( argdef )* ) ;
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA31=null;
        Token COMMA33=null;
        EulangParser.argdef_return argdef30 = null;

        EulangParser.argdef_return argdef32 = null;


        CommonTree COMMA31_tree=null;
        CommonTree COMMA33_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdef=new RewriteRuleSubtreeStream(adaptor,"rule argdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:8: ( ( argdef ( COMMA argdef )* ( COMMA )? )? -> ^( ARGLIST ( argdef )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==ID) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:11: argdef ( COMMA argdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_argdef_in_argdefs476);
                    argdef30=argdef();

                    state._fsp--;

                    stream_argdef.add(argdef30.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:18: ( COMMA argdef )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( (LA8_0==COMMA) ) {
                            int LA8_1 = input.LA(2);

                            if ( (LA8_1==ID) ) {
                                alt8=1;
                            }


                        }


                        switch (alt8) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:20: COMMA argdef
                    	    {
                    	    COMMA31=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs480);  
                    	    stream_COMMA.add(COMMA31);

                    	    pushFollow(FOLLOW_argdef_in_argdefs482);
                    	    argdef32=argdef();

                    	    state._fsp--;

                    	    stream_argdef.add(argdef32.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:35: ( COMMA )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==COMMA) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:35: COMMA
                            {
                            COMMA33=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs486);  
                            stream_COMMA.add(COMMA33);


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
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 70:67: -> ^( ARGLIST ( argdef )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:70: ^( ARGLIST ( argdef )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:80: ( argdef )*
                while ( stream_argdef.hasNext() ) {
                    adaptor.addChild(root_1, stream_argdef.nextTree());

                }
                stream_argdef.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:1: argdef : ( ID ( COLON type ( EQUALS assignExpr )? )? -> ^( ARGDEF ID ( type )* ( assignExpr )* ) | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) );
    public final EulangParser.argdef_return argdef() throws RecognitionException {
        EulangParser.argdef_return retval = new EulangParser.argdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID34=null;
        Token COLON35=null;
        Token EQUALS37=null;
        Token ID39=null;
        Token EQUALS40=null;
        EulangParser.type_return type36 = null;

        EulangParser.assignExpr_return assignExpr38 = null;

        EulangParser.assignExpr_return assignExpr41 = null;


        CommonTree ID34_tree=null;
        CommonTree COLON35_tree=null;
        CommonTree EQUALS37_tree=null;
        CommonTree ID39_tree=null;
        CommonTree EQUALS40_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:7: ( ID ( COLON type ( EQUALS assignExpr )? )? -> ^( ARGDEF ID ( type )* ( assignExpr )* ) | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==ID) ) {
                int LA13_1 = input.LA(2);

                if ( (LA13_1==EQUALS) ) {
                    alt13=2;
                }
                else if ( ((LA13_1>=COLON && LA13_1<=COMMA)||LA13_1==RPAREN) ) {
                    alt13=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 13, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:10: ID ( COLON type ( EQUALS assignExpr )? )?
                    {
                    ID34=(Token)match(input,ID,FOLLOW_ID_in_argdef535);  
                    stream_ID.add(ID34);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:13: ( COLON type ( EQUALS assignExpr )? )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==COLON) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:14: COLON type ( EQUALS assignExpr )?
                            {
                            COLON35=(Token)match(input,COLON,FOLLOW_COLON_in_argdef538);  
                            stream_COLON.add(COLON35);

                            pushFollow(FOLLOW_type_in_argdef540);
                            type36=type();

                            state._fsp--;

                            stream_type.add(type36.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:25: ( EQUALS assignExpr )?
                            int alt11=2;
                            int LA11_0 = input.LA(1);

                            if ( (LA11_0==EQUALS) ) {
                                alt11=1;
                            }
                            switch (alt11) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:26: EQUALS assignExpr
                                    {
                                    EQUALS37=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdef543);  
                                    stream_EQUALS.add(EQUALS37);

                                    pushFollow(FOLLOW_assignExpr_in_argdef545);
                                    assignExpr38=assignExpr();

                                    state._fsp--;

                                    stream_assignExpr.add(assignExpr38.getTree());

                                    }
                                    break;

                            }


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ID, assignExpr, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 73:51: -> ^( ARGDEF ID ( type )* ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:54: ^( ARGDEF ID ( type )* ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:66: ( type )*
                        while ( stream_type.hasNext() ) {
                            adaptor.addChild(root_1, stream_type.nextTree());

                        }
                        stream_type.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:72: ( assignExpr )*
                        while ( stream_assignExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        }
                        stream_assignExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:7: ID EQUALS assignExpr
                    {
                    ID39=(Token)match(input,ID,FOLLOW_ID_in_argdef574);  
                    stream_ID.add(ID39);

                    EQUALS40=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdef576);  
                    stream_EQUALS.add(EQUALS40);

                    pushFollow(FOLLOW_assignExpr_in_argdef578);
                    assignExpr41=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr41.getTree());


                    // AST REWRITE
                    // elements: ID, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 74:31: -> ^( ARGDEF ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:34: ^( ARGDEF ID TYPE assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:1: type : ID -> ^( TYPE ID ) ;
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID42=null;

        CommonTree ID42_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:5: ( ID -> ^( TYPE ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:8: ID
            {
            ID42=(Token)match(input,ID,FOLLOW_ID_in_type604);  
            stream_ID.add(ID42);



            // AST REWRITE
            // elements: ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 77:17: -> ^( TYPE ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:20: ^( TYPE ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:1: codestmtlist : ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) ;
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI44=null;
        Token SEMI46=null;
        EulangParser.codeStmt_return codeStmt43 = null;

        EulangParser.codeStmt_return codeStmt45 = null;


        CommonTree SEMI44_tree=null;
        CommonTree SEMI46_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:13: ( ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==MINUS||LA16_0==LPAREN||LA16_0==NUMBER||LA16_0==ID) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:16: codeStmt ( SEMI codeStmt )* ( SEMI )?
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist629);
                    codeStmt43=codeStmt();

                    state._fsp--;

                    stream_codeStmt.add(codeStmt43.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:25: ( SEMI codeStmt )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( (LA14_0==SEMI) ) {
                            int LA14_1 = input.LA(2);

                            if ( (LA14_1==MINUS||LA14_1==LPAREN||LA14_1==NUMBER||LA14_1==ID) ) {
                                alt14=1;
                            }


                        }


                        switch (alt14) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:27: SEMI codeStmt
                    	    {
                    	    SEMI44=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist633);  
                    	    stream_SEMI.add(SEMI44);

                    	    pushFollow(FOLLOW_codeStmt_in_codestmtlist635);
                    	    codeStmt45=codeStmt();

                    	    state._fsp--;

                    	    stream_codeStmt.add(codeStmt45.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:44: ( SEMI )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==SEMI) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:44: SEMI
                            {
                            SEMI46=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist640);  
                            stream_SEMI.add(SEMI46);


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
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 80:53: -> ^( STMTLIST ( codeStmt )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:57: ^( STMTLIST ( codeStmt )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:68: ( codeStmt )*
                while ( stream_codeStmt.hasNext() ) {
                    adaptor.addChild(root_1, stream_codeStmt.nextTree());

                }
                stream_codeStmt.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:83:1: codeStmt : ( varDecl | assignExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl47 = null;

        EulangParser.assignExpr_return assignExpr48 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:83:10: ( varDecl | assignExpr )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ID) ) {
                int LA17_1 = input.LA(2);

                if ( (LA17_1==COLON||LA17_1==COLON_EQUALS) ) {
                    alt17=1;
                }
                else if ( (LA17_1==EQUALS||(LA17_1>=PLUS && LA17_1<=LPAREN)||LA17_1==RBRACE||(LA17_1>=AMP && LA17_1<=UMOD)||LA17_1==COMPXOR) ) {
                    alt17=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 17, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA17_0==MINUS||LA17_0==LPAREN||LA17_0==NUMBER) ) {
                alt17=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:83:12: varDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_varDecl_in_codeStmt671);
                    varDecl47=varDecl();

                    state._fsp--;

                    adaptor.addChild(root_0, varDecl47.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:84:9: assignExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignExpr_in_codeStmt681);
                    assignExpr48=assignExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, assignExpr48.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID49=null;
        Token COLON_EQUALS50=null;
        Token ID52=null;
        Token COLON53=null;
        Token EQUALS55=null;
        EulangParser.assignExpr_return assignExpr51 = null;

        EulangParser.type_return type54 = null;

        EulangParser.assignExpr_return assignExpr56 = null;


        CommonTree ID49_tree=null;
        CommonTree COLON_EQUALS50_tree=null;
        CommonTree ID52_tree=null;
        CommonTree COLON53_tree=null;
        CommonTree EQUALS55_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:8: ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==ID) ) {
                int LA19_1 = input.LA(2);

                if ( (LA19_1==COLON_EQUALS) ) {
                    alt19=1;
                }
                else if ( (LA19_1==COLON) ) {
                    alt19=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 19, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:10: ID COLON_EQUALS assignExpr
                    {
                    ID49=(Token)match(input,ID,FOLLOW_ID_in_varDecl695);  
                    stream_ID.add(ID49);

                    COLON_EQUALS50=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl697);  
                    stream_COLON_EQUALS.add(COLON_EQUALS50);

                    pushFollow(FOLLOW_assignExpr_in_varDecl699);
                    assignExpr51=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr51.getTree());


                    // AST REWRITE
                    // elements: assignExpr, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 87:45: -> ^( DEFINE ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:87:48: ^( DEFINE ID TYPE assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID52=(Token)match(input,ID,FOLLOW_ID_in_varDecl727);  
                    stream_ID.add(ID52);

                    COLON53=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl729);  
                    stream_COLON.add(COLON53);

                    pushFollow(FOLLOW_type_in_varDecl731);
                    type54=type();

                    state._fsp--;

                    stream_type.add(type54.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:21: ( EQUALS assignExpr )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==EQUALS) ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:22: EQUALS assignExpr
                            {
                            EQUALS55=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl734);  
                            stream_EQUALS.add(EQUALS55);

                            pushFollow(FOLLOW_assignExpr_in_varDecl736);
                            assignExpr56=assignExpr();

                            state._fsp--;

                            stream_assignExpr.add(assignExpr56.getTree());

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
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 88:43: -> ^( DEFINE ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:46: ^( DEFINE ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:63: ( assignExpr )*
                        while ( stream_assignExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        }
                        stream_assignExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class assignExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:1: assignExpr : ( ID EQUALS assignExpr -> ^( ASSIGN ID assignExpr ) | rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID57=null;
        Token EQUALS58=null;
        EulangParser.assignExpr_return assignExpr59 = null;

        EulangParser.rhsExpr_return rhsExpr60 = null;


        CommonTree ID57_tree=null;
        CommonTree EQUALS58_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:12: ( ID EQUALS assignExpr -> ^( ASSIGN ID assignExpr ) | rhsExpr )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==ID) ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==EQUALS) ) {
                    alt20=1;
                }
                else if ( (LA20_1==COMMA||(LA20_1>=PLUS && LA20_1<=RPAREN)||LA20_1==RBRACE||(LA20_1>=AMP && LA20_1<=UMOD)||LA20_1==COMPXOR) ) {
                    alt20=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA20_0==MINUS||LA20_0==LPAREN||LA20_0==NUMBER) ) {
                alt20=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:14: ID EQUALS assignExpr
                    {
                    ID57=(Token)match(input,ID,FOLLOW_ID_in_assignExpr765);  
                    stream_ID.add(ID57);

                    EQUALS58=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr767);  
                    stream_EQUALS.add(EQUALS58);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr769);
                    assignExpr59=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr59.getTree());


                    // AST REWRITE
                    // elements: assignExpr, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 91:42: -> ^( ASSIGN ID assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:45: ^( ASSIGN ID assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:92:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_assignExpr794);
                    rhsExpr60=rhsExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, rhsExpr60.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:95:1: rhsExpr : cond ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.cond_return cond61 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:95:9: ( cond )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:95:13: cond
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_cond_in_rhsExpr809);
            cond61=cond();

            state._fsp--;

            adaptor.addChild(root_0, cond61.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:98:1: funcCall : ID LPAREN arglist RPAREN -> ^( CALL ID arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID62=null;
        Token LPAREN63=null;
        Token RPAREN65=null;
        EulangParser.arglist_return arglist64 = null;


        CommonTree ID62_tree=null;
        CommonTree LPAREN63_tree=null;
        CommonTree RPAREN65_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:98:10: ( ID LPAREN arglist RPAREN -> ^( CALL ID arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:98:12: ID LPAREN arglist RPAREN
            {
            ID62=(Token)match(input,ID,FOLLOW_ID_in_funcCall827);  
            stream_ID.add(ID62);

            LPAREN63=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall829);  
            stream_LPAREN.add(LPAREN63);

            pushFollow(FOLLOW_arglist_in_funcCall831);
            arglist64=arglist();

            state._fsp--;

            stream_arglist.add(arglist64.getTree());
            RPAREN65=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall833);  
            stream_RPAREN.add(RPAREN65);



            // AST REWRITE
            // elements: arglist, ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 98:39: -> ^( CALL ID arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:98:46: ^( CALL ID arglist )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CALL, "CALL"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                adaptor.addChild(root_1, stream_arglist.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA67=null;
        Token COMMA69=null;
        EulangParser.arg_return arg66 = null;

        EulangParser.arg_return arg68 = null;


        CommonTree COMMA67_tree=null;
        CommonTree COMMA69_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==MINUS||LA23_0==LPAREN||LA23_0==NUMBER||LA23_0==ID) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist864);
                    arg66=arg();

                    state._fsp--;

                    stream_arg.add(arg66.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:15: ( COMMA arg )*
                    loop21:
                    do {
                        int alt21=2;
                        int LA21_0 = input.LA(1);

                        if ( (LA21_0==COMMA) ) {
                            int LA21_1 = input.LA(2);

                            if ( (LA21_1==MINUS||LA21_1==LPAREN||LA21_1==NUMBER||LA21_1==ID) ) {
                                alt21=1;
                            }


                        }


                        switch (alt21) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:17: COMMA arg
                    	    {
                    	    COMMA67=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist868);  
                    	    stream_COMMA.add(COMMA67);

                    	    pushFollow(FOLLOW_arg_in_arglist870);
                    	    arg68=arg();

                    	    state._fsp--;

                    	    stream_arg.add(arg68.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop21;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:29: ( COMMA )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==COMMA) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:29: COMMA
                            {
                            COMMA69=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist874);  
                            stream_COMMA.add(COMMA69);


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
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 102:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:74: ( arg )*
                while ( stream_arg.hasNext() ) {
                    adaptor.addChild(root_1, stream_arg.nextTree());

                }
                stream_arg.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:1: arg : assignExpr -> ^( EXPR assignExpr ) ;
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr70 = null;


        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:4: ( assignExpr -> ^( EXPR assignExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:7: assignExpr
            {
            pushFollow(FOLLOW_assignExpr_in_arg923);
            assignExpr70=assignExpr();

            state._fsp--;

            stream_assignExpr.add(assignExpr70.getTree());


            // AST REWRITE
            // elements: assignExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 105:37: -> ^( EXPR assignExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:105:40: ^( EXPR assignExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                adaptor.addChild(root_1, stream_assignExpr.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:1: cond : (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )? ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION71=null;
        Token COLON72=null;
        EulangParser.logcond_return l = null;

        EulangParser.cond_return t = null;

        EulangParser.cond_return f = null;


        CommonTree QUESTION71_tree=null;
        CommonTree COLON72_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logcond=new RewriteRuleSubtreeStream(adaptor,"rule logcond");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:5: ( (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:10: (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:10: (l= logcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:12: l= logcond
            {
            pushFollow(FOLLOW_logcond_in_cond970);
            l=logcond();

            state._fsp--;

            stream_logcond.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 109:23: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:7: ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==QUESTION) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:9: QUESTION t= cond COLON f= cond
                    {
                    QUESTION71=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond988);  
                    stream_QUESTION.add(QUESTION71);

                    pushFollow(FOLLOW_cond_in_cond992);
                    t=cond();

                    state._fsp--;

                    stream_cond.add(t.getTree());
                    COLON72=(Token)match(input,COLON,FOLLOW_COLON_in_cond994);  
                    stream_COLON.add(COLON72);

                    pushFollow(FOLLOW_cond_in_cond998);
                    f=cond();

                    state._fsp--;

                    stream_cond.add(f.getTree());


                    // AST REWRITE
                    // elements: l, t, f
                    // token labels: 
                    // rule labels: f, retval, t, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_f=new RewriteRuleSubtreeStream(adaptor,"rule f",f!=null?f.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_t=new RewriteRuleSubtreeStream(adaptor,"rule t",t!=null?t.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 110:38: -> ^( COND $l $t $f)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:41: ^( COND $l $t $f)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(COND, "COND"), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_t.nextTree());
                        adaptor.addChild(root_1, stream_f.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:114:1: logcond : (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )? ;
    public final EulangParser.logcond_return logcond() throws RecognitionException {
        EulangParser.logcond_return retval = new EulangParser.logcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPAND73=null;
        Token COMPOR74=null;
        Token COMPXOR75=null;
        EulangParser.binlogcond_return l = null;

        EulangParser.binlogcond_return r = null;


        CommonTree COMPAND73_tree=null;
        CommonTree COMPOR74_tree=null;
        CommonTree COMPXOR75_tree=null;
        RewriteRuleTokenStream stream_COMPAND=new RewriteRuleTokenStream(adaptor,"token COMPAND");
        RewriteRuleTokenStream stream_COMPXOR=new RewriteRuleTokenStream(adaptor,"token COMPXOR");
        RewriteRuleTokenStream stream_COMPOR=new RewriteRuleTokenStream(adaptor,"token COMPOR");
        RewriteRuleSubtreeStream stream_binlogcond=new RewriteRuleSubtreeStream(adaptor,"rule binlogcond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:114:8: ( (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:114:10: (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:114:10: (l= binlogcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:114:12: l= binlogcond
            {
            pushFollow(FOLLOW_binlogcond_in_logcond1036);
            l=binlogcond();

            state._fsp--;

            stream_binlogcond.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 114:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:7: ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )?
            int alt25=4;
            switch ( input.LA(1) ) {
                case COMPAND:
                    {
                    alt25=1;
                    }
                    break;
                case COMPOR:
                    {
                    alt25=2;
                    }
                    break;
                case COMPXOR:
                    {
                    alt25=3;
                    }
                    break;
            }

            switch (alt25) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:9: COMPAND r= binlogcond
                    {
                    COMPAND73=(Token)match(input,COMPAND,FOLLOW_COMPAND_in_logcond1063);  
                    stream_COMPAND.add(COMPAND73);

                    pushFollow(FOLLOW_binlogcond_in_logcond1067);
                    r=binlogcond();

                    state._fsp--;

                    stream_binlogcond.add(r.getTree());


                    // AST REWRITE
                    // elements: COMPAND, l, r
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 115:30: -> ^( COMPAND $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:33: ^( COMPAND $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPAND.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:116:9: COMPOR r= binlogcond
                    {
                    COMPOR74=(Token)match(input,COMPOR,FOLLOW_COMPOR_in_logcond1089);  
                    stream_COMPOR.add(COMPOR74);

                    pushFollow(FOLLOW_binlogcond_in_logcond1093);
                    r=binlogcond();

                    state._fsp--;

                    stream_binlogcond.add(r.getTree());


                    // AST REWRITE
                    // elements: COMPOR, r, l
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 116:29: -> ^( COMPOR $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:116:32: ^( COMPOR $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPOR.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:117:9: COMPXOR r= binlogcond
                    {
                    COMPXOR75=(Token)match(input,COMPXOR,FOLLOW_COMPXOR_in_logcond1115);  
                    stream_COMPXOR.add(COMPXOR75);

                    pushFollow(FOLLOW_binlogcond_in_logcond1119);
                    r=binlogcond();

                    state._fsp--;

                    stream_binlogcond.add(r.getTree());


                    // AST REWRITE
                    // elements: r, l, COMPXOR
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 117:31: -> ^( COMPXOR $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:117:34: ^( COMPXOR $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPXOR.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:121:1: binlogcond : (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )? ;
    public final EulangParser.binlogcond_return binlogcond() throws RecognitionException {
        EulangParser.binlogcond_return retval = new EulangParser.binlogcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP76=null;
        Token BAR77=null;
        Token CARET78=null;
        EulangParser.compeq_return l = null;

        EulangParser.compeq_return r = null;


        CommonTree AMP76_tree=null;
        CommonTree BAR77_tree=null;
        CommonTree CARET78_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_compeq=new RewriteRuleSubtreeStream(adaptor,"rule compeq");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:121:11: ( (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:121:13: (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:121:13: (l= compeq -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:121:15: l= compeq
            {
            pushFollow(FOLLOW_compeq_in_binlogcond1153);
            l=compeq();

            state._fsp--;

            stream_compeq.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 121:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:7: ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )?
            int alt26=4;
            switch ( input.LA(1) ) {
                case AMP:
                    {
                    alt26=1;
                    }
                    break;
                case BAR:
                    {
                    alt26=2;
                    }
                    break;
                case CARET:
                    {
                    alt26=3;
                    }
                    break;
            }

            switch (alt26) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:9: AMP r= compeq
                    {
                    AMP76=(Token)match(input,AMP,FOLLOW_AMP_in_binlogcond1182);  
                    stream_AMP.add(AMP76);

                    pushFollow(FOLLOW_compeq_in_binlogcond1186);
                    r=compeq();

                    state._fsp--;

                    stream_compeq.add(r.getTree());


                    // AST REWRITE
                    // elements: r, l
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 122:23: -> ^( BITAND $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:26: ^( BITAND $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITAND, "BITAND"), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:123:9: BAR r= compeq
                    {
                    BAR77=(Token)match(input,BAR,FOLLOW_BAR_in_binlogcond1209);  
                    stream_BAR.add(BAR77);

                    pushFollow(FOLLOW_compeq_in_binlogcond1213);
                    r=compeq();

                    state._fsp--;

                    stream_compeq.add(r.getTree());


                    // AST REWRITE
                    // elements: l, r
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 123:23: -> ^( BITOR $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:123:26: ^( BITOR $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITOR, "BITOR"), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:124:9: CARET r= compeq
                    {
                    CARET78=(Token)match(input,CARET,FOLLOW_CARET_in_binlogcond1236);  
                    stream_CARET.add(CARET78);

                    pushFollow(FOLLOW_compeq_in_binlogcond1240);
                    r=compeq();

                    state._fsp--;

                    stream_compeq.add(r.getTree());


                    // AST REWRITE
                    // elements: l, r
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 124:25: -> ^( BITXOR $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:124:28: ^( BITXOR $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BITXOR, "BITXOR"), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:1: compeq : (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )? ;
    public final EulangParser.compeq_return compeq() throws RecognitionException {
        EulangParser.compeq_return retval = new EulangParser.compeq_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ79=null;
        Token COMPNE80=null;
        EulangParser.comp_return l = null;

        EulangParser.comp_return r = null;


        CommonTree COMPEQ79_tree=null;
        CommonTree COMPNE80_tree=null;
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:7: ( (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:11: (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:11: (l= comp -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:128:13: l= comp
            {
            pushFollow(FOLLOW_comp_in_compeq1276);
            l=comp();

            state._fsp--;

            stream_comp.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 128:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:7: ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )?
            int alt27=3;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==COMPEQ) ) {
                alt27=1;
            }
            else if ( (LA27_0==COMPNE) ) {
                alt27=2;
            }
            switch (alt27) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:9: COMPEQ r= comp
                    {
                    COMPEQ79=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_compeq1310);  
                    stream_COMPEQ.add(COMPEQ79);

                    pushFollow(FOLLOW_comp_in_compeq1314);
                    r=comp();

                    state._fsp--;

                    stream_comp.add(r.getTree());


                    // AST REWRITE
                    // elements: l, COMPEQ, r
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 129:23: -> ^( COMPEQ $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:26: ^( COMPEQ $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPEQ.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:130:9: COMPNE r= comp
                    {
                    COMPNE80=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_compeq1336);  
                    stream_COMPNE.add(COMPNE80);

                    pushFollow(FOLLOW_comp_in_compeq1340);
                    r=comp();

                    state._fsp--;

                    stream_comp.add(r.getTree());


                    // AST REWRITE
                    // elements: l, r, COMPNE
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 130:23: -> ^( COMPNE $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:130:26: ^( COMPNE $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPNE.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:134:1: comp : (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )? ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPLE81=null;
        Token COMPGE82=null;
        Token LESS83=null;
        Token GREATER84=null;
        EulangParser.shift_return l = null;

        EulangParser.shift_return r = null;


        CommonTree COMPLE81_tree=null;
        CommonTree COMPGE82_tree=null;
        CommonTree LESS83_tree=null;
        CommonTree GREATER84_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:134:5: ( (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:134:8: (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:134:8: (l= shift -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:134:10: l= shift
            {
            pushFollow(FOLLOW_shift_in_comp1374);
            l=shift();

            state._fsp--;

            stream_shift.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 134:28: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:7: ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )?
            int alt28=5;
            switch ( input.LA(1) ) {
                case COMPLE:
                    {
                    alt28=1;
                    }
                    break;
                case COMPGE:
                    {
                    alt28=2;
                    }
                    break;
                case LESS:
                    {
                    alt28=3;
                    }
                    break;
                case GREATER:
                    {
                    alt28=4;
                    }
                    break;
            }

            switch (alt28) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:9: COMPLE r= shift
                    {
                    COMPLE81=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp1401);  
                    stream_COMPLE.add(COMPLE81);

                    pushFollow(FOLLOW_shift_in_comp1405);
                    r=shift();

                    state._fsp--;

                    stream_shift.add(r.getTree());


                    // AST REWRITE
                    // elements: COMPLE, l, r
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 135:27: -> ^( COMPLE $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:135:30: ^( COMPLE $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPLE.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:136:9: COMPGE r= shift
                    {
                    COMPGE82=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp1430);  
                    stream_COMPGE.add(COMPGE82);

                    pushFollow(FOLLOW_shift_in_comp1434);
                    r=shift();

                    state._fsp--;

                    stream_shift.add(r.getTree());


                    // AST REWRITE
                    // elements: COMPGE, r, l
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 136:27: -> ^( COMPGE $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:136:30: ^( COMPGE $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPGE.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:9: LESS r= shift
                    {
                    LESS83=(Token)match(input,LESS,FOLLOW_LESS_in_comp1459);  
                    stream_LESS.add(LESS83);

                    pushFollow(FOLLOW_shift_in_comp1463);
                    r=shift();

                    state._fsp--;

                    stream_shift.add(r.getTree());


                    // AST REWRITE
                    // elements: l, LESS, r
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 137:26: -> ^( LESS $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:29: ^( LESS $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_LESS.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:138:9: GREATER r= shift
                    {
                    GREATER84=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp1489);  
                    stream_GREATER.add(GREATER84);

                    pushFollow(FOLLOW_shift_in_comp1493);
                    r=shift();

                    state._fsp--;

                    stream_shift.add(r.getTree());


                    // AST REWRITE
                    // elements: GREATER, r, l
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 138:28: -> ^( GREATER $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:138:31: ^( GREATER $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_GREATER.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:1: shift : (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )? ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT85=null;
        Token RSHIFT86=null;
        Token URSHIFT87=null;
        EulangParser.factor_return l = null;

        EulangParser.factor_return r = null;


        CommonTree LSHIFT85_tree=null;
        CommonTree RSHIFT86_tree=null;
        CommonTree URSHIFT87_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:6: ( (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:9: (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:9: (l= factor -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:11: l= factor
            {
            pushFollow(FOLLOW_factor_in_shift1545);
            l=factor();

            state._fsp--;

            stream_factor.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 142:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:143:7: ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )?
            int alt29=4;
            switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt29=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt29=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt29=3;
                    }
                    break;
            }

            switch (alt29) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:143:9: LSHIFT r= factor
                    {
                    LSHIFT85=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift1578);  
                    stream_LSHIFT.add(LSHIFT85);

                    pushFollow(FOLLOW_factor_in_shift1582);
                    r=factor();

                    state._fsp--;

                    stream_factor.add(r.getTree());


                    // AST REWRITE
                    // elements: r, l, LSHIFT
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 143:27: -> ^( LSHIFT $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:143:30: ^( LSHIFT $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_LSHIFT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:144:9: RSHIFT r= factor
                    {
                    RSHIFT86=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift1607);  
                    stream_RSHIFT.add(RSHIFT86);

                    pushFollow(FOLLOW_factor_in_shift1611);
                    r=factor();

                    state._fsp--;

                    stream_factor.add(r.getTree());


                    // AST REWRITE
                    // elements: r, RSHIFT, l
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 144:27: -> ^( RSHIFT $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:144:30: ^( RSHIFT $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_RSHIFT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:9: URSHIFT r= factor
                    {
                    URSHIFT87=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift1635);  
                    stream_URSHIFT.add(URSHIFT87);

                    pushFollow(FOLLOW_factor_in_shift1639);
                    r=factor();

                    state._fsp--;

                    stream_factor.add(r.getTree());


                    // AST REWRITE
                    // elements: r, l, URSHIFT
                    // token labels: 
                    // rule labels: retval, r, l
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
                    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 145:28: -> ^( URSHIFT $l $r)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:31: ^( URSHIFT $l $r)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_URSHIFT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_l.nextTree());
                        adaptor.addChild(root_1, stream_r.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:148:1: factor : (l= multExpr -> $l) ( PLUS r= multExpr -> ^( PLUS $l $r) | MINUS r= multExpr -> ^( MINUS $l $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS88=null;
        Token MINUS89=null;
        EulangParser.multExpr_return l = null;

        EulangParser.multExpr_return r = null;


        CommonTree PLUS88_tree=null;
        CommonTree MINUS89_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_multExpr=new RewriteRuleSubtreeStream(adaptor,"rule multExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:148:8: ( (l= multExpr -> $l) ( PLUS r= multExpr -> ^( PLUS $l $r) | MINUS r= multExpr -> ^( MINUS $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:148:10: (l= multExpr -> $l) ( PLUS r= multExpr -> ^( PLUS $l $r) | MINUS r= multExpr -> ^( MINUS $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:148:10: (l= multExpr -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:148:12: l= multExpr
            {
            pushFollow(FOLLOW_multExpr_in_factor1676);
            l=multExpr();

            state._fsp--;

            stream_multExpr.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 148:36: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:9: ( PLUS r= multExpr -> ^( PLUS $l $r) | MINUS r= multExpr -> ^( MINUS $l $r) )*
            loop30:
            do {
                int alt30=3;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==MINUS) ) {
                    int LA30_2 = input.LA(2);

                    if ( (LA30_2==MINUS||LA30_2==LPAREN||LA30_2==NUMBER||LA30_2==ID) ) {
                        alt30=2;
                    }


                }
                else if ( (LA30_0==PLUS) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:13: PLUS r= multExpr
            	    {
            	    PLUS88=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor1710);  
            	    stream_PLUS.add(PLUS88);

            	    pushFollow(FOLLOW_multExpr_in_factor1714);
            	    r=multExpr();

            	    state._fsp--;

            	    stream_multExpr.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, PLUS, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 149:37: -> ^( PLUS $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:40: ^( PLUS $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_PLUS.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:150:13: MINUS r= multExpr
            	    {
            	    MINUS89=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor1748);  
            	    stream_MINUS.add(MINUS89);

            	    pushFollow(FOLLOW_multExpr_in_factor1752);
            	    r=multExpr();

            	    state._fsp--;

            	    stream_multExpr.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, MINUS, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 150:37: -> ^( MINUS $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:150:40: ^( MINUS $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_MINUS.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    public static class multExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:154:1: multExpr : (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )* ;
    public final EulangParser.multExpr_return multExpr() throws RecognitionException {
        EulangParser.multExpr_return retval = new EulangParser.multExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR90=null;
        Token SLASH91=null;
        Token BACKSLASH92=null;
        Token PERCENT93=null;
        Token UMOD94=null;
        EulangParser.atom_return l = null;

        EulangParser.atom_return r = null;


        CommonTree STAR90_tree=null;
        CommonTree SLASH91_tree=null;
        CommonTree BACKSLASH92_tree=null;
        CommonTree PERCENT93_tree=null;
        CommonTree UMOD94_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:154:10: ( (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:154:12: (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:154:12: (l= atom -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:154:14: l= atom
            {
            pushFollow(FOLLOW_atom_in_multExpr1799);
            l=atom();

            state._fsp--;

            stream_atom.add(l.getTree());


            // AST REWRITE
            // elements: l
            // token labels: 
            // rule labels: retval, l
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 154:38: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:155:9: ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )*
            loop31:
            do {
                int alt31=6;
                switch ( input.LA(1) ) {
                case STAR:
                    {
                    alt31=1;
                    }
                    break;
                case SLASH:
                    {
                    alt31=2;
                    }
                    break;
                case BACKSLASH:
                    {
                    alt31=3;
                    }
                    break;
                case PERCENT:
                    {
                    alt31=4;
                    }
                    break;
                case UMOD:
                    {
                    alt31=5;
                    }
                    break;

                }

                switch (alt31) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:155:11: STAR r= atom
            	    {
            	    STAR90=(Token)match(input,STAR,FOLLOW_STAR_in_multExpr1835);  
            	    stream_STAR.add(STAR90);

            	    pushFollow(FOLLOW_atom_in_multExpr1839);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 155:35: -> ^( MUL $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:155:38: ^( MUL $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MUL, "MUL"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:156:11: SLASH r= atom
            	    {
            	    SLASH91=(Token)match(input,SLASH,FOLLOW_SLASH_in_multExpr1876);  
            	    stream_SLASH.add(SLASH91);

            	    pushFollow(FOLLOW_atom_in_multExpr1880);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 156:35: -> ^( DIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:156:38: ^( DIV $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DIV, "DIV"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:11: BACKSLASH r= atom
            	    {
            	    BACKSLASH92=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_multExpr1915);  
            	    stream_BACKSLASH.add(BACKSLASH92);

            	    pushFollow(FOLLOW_atom_in_multExpr1919);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 157:39: -> ^( UDIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:42: ^( UDIV $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UDIV, "UDIV"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:158:11: PERCENT r= atom
            	    {
            	    PERCENT93=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_multExpr1954);  
            	    stream_PERCENT.add(PERCENT93);

            	    pushFollow(FOLLOW_atom_in_multExpr1958);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 158:37: -> ^( MOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:158:40: ^( MOD $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(MOD, "MOD"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:11: UMOD r= atom
            	    {
            	    UMOD94=(Token)match(input,UMOD,FOLLOW_UMOD_in_multExpr1993);  
            	    stream_UMOD.add(UMOD94);

            	    pushFollow(FOLLOW_atom_in_multExpr1997);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


            	    // AST REWRITE
            	    // elements: UMOD, l, r
            	    // token labels: 
            	    // rule labels: retval, r, l
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_r=new RewriteRuleSubtreeStream(adaptor,"rule r",r!=null?r.tree:null);
            	    RewriteRuleSubtreeStream stream_l=new RewriteRuleSubtreeStream(adaptor,"rule l",l!=null?l.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 159:34: -> ^( UMOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:37: ^( UMOD $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UMOD.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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
    // $ANTLR end "multExpr"

    public static class atom_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atom"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:1: atom options {k=2; } : ( MINUS NUMBER -> | NUMBER -> NUMBER | funcCall -> funcCall | ID -> ID | LPAREN assignExpr RPAREN -> assignExpr );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS95=null;
        Token NUMBER96=null;
        Token NUMBER97=null;
        Token ID99=null;
        Token LPAREN100=null;
        Token RPAREN102=null;
        EulangParser.funcCall_return funcCall98 = null;

        EulangParser.assignExpr_return assignExpr101 = null;


        CommonTree MINUS95_tree=null;
        CommonTree NUMBER96_tree=null;
        CommonTree NUMBER97_tree=null;
        CommonTree ID99_tree=null;
        CommonTree LPAREN100_tree=null;
        CommonTree RPAREN102_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_funcCall=new RewriteRuleSubtreeStream(adaptor,"rule funcCall");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:23: ( MINUS NUMBER -> | NUMBER -> NUMBER | funcCall -> funcCall | ID -> ID | LPAREN assignExpr RPAREN -> assignExpr )
            int alt32=5;
            alt32 = dfa32.predict(input);
            switch (alt32) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:164:5: MINUS NUMBER
                    {
                    MINUS95=(Token)match(input,MINUS,FOLLOW_MINUS_in_atom2082);  
                    stream_MINUS.add(MINUS95);

                    NUMBER96=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom2084);  
                    stream_NUMBER.add(NUMBER96);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 164:43: ->
                    {
                        adaptor.addChild(root_0, new CommonTree(new CommonToken(NUMBER, "-" + (NUMBER96!=null?NUMBER96.getText():null))));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:165:7: NUMBER
                    {
                    NUMBER97=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom2121);  
                    stream_NUMBER.add(NUMBER97);



                    // AST REWRITE
                    // elements: NUMBER
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 165:39: -> NUMBER
                    {
                        adaptor.addChild(root_0, stream_NUMBER.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:9: funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom2160);
                    funcCall98=funcCall();

                    state._fsp--;

                    stream_funcCall.add(funcCall98.getTree());


                    // AST REWRITE
                    // elements: funcCall
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 166:53: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:167:9: ID
                    {
                    ID99=(Token)match(input,ID,FOLLOW_ID_in_atom2209);  
                    stream_ID.add(ID99);



                    // AST REWRITE
                    // elements: ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 167:48: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:168:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN100=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom2259);  
                    stream_LPAREN.add(LPAREN100);

                    pushFollow(FOLLOW_assignExpr_in_atom2261);
                    assignExpr101=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr101.getTree());
                    RPAREN102=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom2263);  
                    stream_RPAREN.add(RPAREN102);



                    // AST REWRITE
                    // elements: assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 168:48: -> assignExpr
                    {
                        adaptor.addChild(root_0, stream_assignExpr.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

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

    // Delegated rules


    protected DFA32 dfa32 = new DFA32(this);
    static final String DFA32_eotS =
        "\45\uffff";
    static final String DFA32_eofS =
        "\3\uffff\1\6\41\uffff";
    static final String DFA32_minS =
        "\1\14\2\uffff\1\6\41\uffff";
    static final String DFA32_maxS =
        "\1\57\2\uffff\1\113\41\uffff";
    static final String DFA32_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\5\1\3\1\4\36\uffff";
    static final String DFA32_specialS =
        "\45\uffff}>";
    static final String[] DFA32_transitionS = {
            "\1\1\2\uffff\1\4\35\uffff\1\2\1\uffff\1\3",
            "",
            "",
            "\2\6\3\uffff\4\6\1\5\1\6\1\uffff\1\6\6\uffff\23\6\1\uffff\1"+
            "\6\1\uffff\1\6\33\uffff\1\6",
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
            ""
    };

    static final short[] DFA32_eot = DFA.unpackEncodedString(DFA32_eotS);
    static final short[] DFA32_eof = DFA.unpackEncodedString(DFA32_eofS);
    static final char[] DFA32_min = DFA.unpackEncodedStringToUnsignedChars(DFA32_minS);
    static final char[] DFA32_max = DFA.unpackEncodedStringToUnsignedChars(DFA32_maxS);
    static final short[] DFA32_accept = DFA.unpackEncodedString(DFA32_acceptS);
    static final short[] DFA32_special = DFA.unpackEncodedString(DFA32_specialS);
    static final short[][] DFA32_transition;

    static {
        int numStates = DFA32_transitionS.length;
        DFA32_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA32_transition[i] = DFA.unpackEncodedString(DFA32_transitionS[i]);
        }
    }

    class DFA32 extends DFA {

        public DFA32(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 32;
            this.eot = DFA32_eot;
            this.eof = DFA32_eof;
            this.min = DFA32_min;
            this.max = DFA32_max;
            this.accept = DFA32_accept;
            this.special = DFA32_special;
            this.transition = DFA32_transition;
        }
        public String getDescription() {
            return "163:1: atom options {k=2; } : ( MINUS NUMBER -> | NUMBER -> NUMBER | funcCall -> funcCall | ID -> ID | LPAREN assignExpr RPAREN -> assignExpr );";
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog160 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stat_in_toplevelstmts191 = new BitSet(new long[]{0x0000A00000009002L});
    public static final BitSet FOLLOW_ID_in_stat225 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_stat227 = new BitSet(new long[]{0x0000A00000089010L});
    public static final BitSet FOLLOW_value_in_stat229 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_SEMI_in_stat235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_stat254 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_stat256 = new BitSet(new long[]{0x0000A00010009000L});
    public static final BitSet FOLLOW_rhsExpr_in_stat258 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_SEMI_in_stat261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_stat280 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_stat299 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_value321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_value336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_value344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector357 = new BitSet(new long[]{0x0000000000100010L});
    public static final BitSet FOLLOW_selectorlist_in_selector359 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectorlist387 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_selectorlist391 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_selectoritem_in_selectorlist393 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_selectorlist398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_selectoritem423 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_LPAREN_in_code441 = new BitSet(new long[]{0x0000800000010000L});
    public static final BitSet FOLLOW_argdefs_in_code443 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_code445 = new BitSet(new long[]{0x0000A00000049000L});
    public static final BitSet FOLLOW_codestmtlist_in_code447 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACE_in_code449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdef_in_argdefs476 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_argdefs480 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_argdef_in_argdefs482 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_argdefs486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdef535 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_COLON_in_argdef538 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_type_in_argdef540 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_EQUALS_in_argdef543 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_assignExpr_in_argdef545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdef574 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_argdef576 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_assignExpr_in_argdef578 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_type604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist629 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist633 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist635 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmt671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_codeStmt681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl695 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl697 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl727 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_varDecl729 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_type_in_varDecl731 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl734 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignExpr765 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr767 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_rhsExpr809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_funcCall827 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall829 = new BitSet(new long[]{0x0000A00000019000L});
    public static final BitSet FOLLOW_arglist_in_funcCall831 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall833 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist864 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_arglist868 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_arg_in_arglist870 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_arglist874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logcond_in_cond970 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond988 = new BitSet(new long[]{0x0000A00000009040L});
    public static final BitSet FOLLOW_cond_in_cond992 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_cond994 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_cond_in_cond998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1036 = new BitSet(new long[]{0x00000000C0000002L,0x0000000000000800L});
    public static final BitSet FOLLOW_COMPAND_in_logcond1063 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMPOR_in_logcond1089 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMPXOR_in_logcond1115 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1153 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_AMP_in_binlogcond1182 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BAR_in_binlogcond1209 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CARET_in_binlogcond1236 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comp_in_compeq1276 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_COMPEQ_in_compeq1310 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_comp_in_compeq1314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMPNE_in_compeq1336 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_comp_in_compeq1340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_shift_in_comp1374 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_COMPLE_in_comp1401 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_shift_in_comp1405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMPGE_in_comp1430 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_shift_in_comp1434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_comp1459 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_shift_in_comp1463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_comp1489 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_shift_in_comp1493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_factor_in_shift1545 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_LSHIFT_in_shift1578 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_factor_in_shift1582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RSHIFT_in_shift1607 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_factor_in_shift1611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_URSHIFT_in_shift1635 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_factor_in_shift1639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multExpr_in_factor1676 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_PLUS_in_factor1710 = new BitSet(new long[]{0x0000A00000009800L});
    public static final BitSet FOLLOW_multExpr_in_factor1714 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_MINUS_in_factor1748 = new BitSet(new long[]{0x0000A00000009800L});
    public static final BitSet FOLLOW_multExpr_in_factor1752 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_atom_in_multExpr1799 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_STAR_in_multExpr1835 = new BitSet(new long[]{0x0000AE000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1839 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_SLASH_in_multExpr1876 = new BitSet(new long[]{0x0000AE000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1880 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_BACKSLASH_in_multExpr1915 = new BitSet(new long[]{0x0000AE000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1919 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_PERCENT_in_multExpr1954 = new BitSet(new long[]{0x0000AE000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1958 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_UMOD_in_multExpr1993 = new BitSet(new long[]{0x0000AE000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1997 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_MINUS_in_atom2082 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_NUMBER_in_atom2084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom2121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom2160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_atom2209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom2259 = new BitSet(new long[]{0x0000A00000009000L});
    public static final BitSet FOLLOW_assignExpr_in_atom2261 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_atom2263 = new BitSet(new long[]{0x0000000000000002L});

}