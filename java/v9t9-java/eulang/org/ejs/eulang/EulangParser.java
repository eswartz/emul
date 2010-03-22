// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g 2010-03-21 20:30:54

package org.ejs.eulang;
import java.util.HashMap;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class EulangParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "LBRACE_LPAREN", "LBRACE_STAR", "COLON", "COMMA", "EQUALS", "COLON_EQUALS", "COLON_COLON_EQUALS", "PLUS", "MINUS", "STAR", "SLASH", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "HASH", "EXCL", "TILDE", "AT", "AMP", "BAR", "CARET", "SEMI", "QUESTION", "COMPAND", "COMPOR", "COMPEQ", "COMPNE", "COMPGE", "COMPLE", "GREATER", "LESS", "LSHIFT", "RSHIFT", "URSHIFT", "BACKSLASH", "PERCENT", "UMOD", "RETURNS", "IDSUFFIX", "NUMBER", "LETTERLIKE", "ID", "DIGIT", "CHAR_LITERAL", "STRING_LITERAL", "SPACE", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT", "CODE", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "EXPR", "DEFINE_ASSIGN", "ASSIGN", "DEFINE", "LIST", "TYPE", "CALL", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "COMPXOR"
    };
    public static final int STAR=13;
    public static final int MOD=78;
    public static final int LBRACE_STAR=5;
    public static final int AMP=25;
    public static final int LBRACE=17;
    public static final int MULTI_COMMENT=56;
    public static final int SUB=74;
    public static final int ARGLIST=60;
    public static final int EXCL=22;
    public static final int EQUALS=8;
    public static final int ID=48;
    public static final int DEFINE=65;
    public static final int SPACE=52;
    public static final int EOF=-1;
    public static final int BITAND=70;
    public static final int LPAREN=15;
    public static final int TYPE=67;
    public static final int CODE=57;
    public static final int AT=24;
    public static final int COLON_COLON_EQUALS=10;
    public static final int LBRACKET=19;
    public static final int RPAREN=16;
    public static final int IDSUFFIX=45;
    public static final int EXPR=62;
    public static final int STRING_LITERAL=51;
    public static final int SLASH=14;
    public static final int GREATER=36;
    public static final int COMMA=7;
    public static final int COMPLE=35;
    public static final int CARET=27;
    public static final int BITXOR=72;
    public static final int TILDE=23;
    public static final int LESS=37;
    public static final int PLUS=11;
    public static final int SINGLE_COMMENT=55;
    public static final int COMPAND=30;
    public static final int DIGIT=49;
    public static final int RBRACKET=20;
    public static final int RSHIFT=39;
    public static final int RETURNS=44;
    public static final int ADD=73;
    public static final int COMPGE=34;
    public static final int RBRACE=18;
    public static final int PERCENT=42;
    public static final int LETTERLIKE=47;
    public static final int UMOD=43;
    public static final int LSHIFT=38;
    public static final int COMPOR=31;
    public static final int UDIV=77;
    public static final int NUMBER=46;
    public static final int LBRACE_LPAREN=4;
    public static final int HASH=21;
    public static final int MINUS=12;
    public static final int LIST=66;
    public static final int MUL=75;
    public static final int SEMI=28;
    public static final int DEFINE_ASSIGN=63;
    public static final int ARGDEF=61;
    public static final int COLON=6;
    public static final int WS=54;
    public static final int BITOR=71;
    public static final int COLON_EQUALS=9;
    public static final int QUESTION=29;
    public static final int NEWLINE=53;
    public static final int CHAR_LITERAL=50;
    public static final int COMPXOR=79;
    public static final int STMTLIST=58;
    public static final int ASSIGN=64;
    public static final int URSHIFT=40;
    public static final int COMPEQ=32;
    public static final int CALL=68;
    public static final int DIV=76;
    public static final int COND=69;
    public static final int PROTO=59;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:43:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog175);
            toplevelstmts1=toplevelstmts();

            state._fsp--;

            adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog177); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:1: toplevelstmts : ( stat )* -> ^( STMTLIST ( stat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.stat_return stat3 = null;


        RewriteRuleSubtreeStream stream_stat=new RewriteRuleSubtreeStream(adaptor,"rule stat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:14: ( ( stat )* -> ^( STMTLIST ( stat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:16: ( stat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:16: ( stat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==MINUS||LA1_0==LPAREN||LA1_0==NUMBER||LA1_0==ID||(LA1_0>=CHAR_LITERAL && LA1_0<=STRING_LITERAL)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:16: stat
            	    {
            	    pushFollow(FOLLOW_stat_in_toplevelstmts206);
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
            // 46:27: -> ^( STMTLIST ( stat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:30: ^( STMTLIST ( stat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:46:41: ( stat )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:49:1: stat : ( ID EQUALS value SEMI -> ^( DEFINE_ASSIGN ID value ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) );
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:49:5: ( ID EQUALS value SEMI -> ^( DEFINE_ASSIGN ID value ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) )
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
                case CHAR_LITERAL:
                case STRING_LITERAL:
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
            else if ( (LA3_0==MINUS||LA3_0==LPAREN||LA3_0==NUMBER||(LA3_0>=CHAR_LITERAL && LA3_0<=STRING_LITERAL)) ) {
                alt3=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:49:9: ID EQUALS value SEMI
                    {
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_stat240);  
                    stream_ID.add(ID4);

                    EQUALS5=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_stat242);  
                    stream_EQUALS.add(EQUALS5);

                    pushFollow(FOLLOW_value_in_stat244);
                    value6=value();

                    state._fsp--;

                    stream_value.add(value6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_stat250);  
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
                    // 49:35: -> ^( DEFINE_ASSIGN ID value )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:49:38: ^( DEFINE_ASSIGN ID value )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:50:7: ID COLON_EQUALS rhsExpr SEMI
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_stat269);  
                    stream_ID.add(ID8);

                    COLON_EQUALS9=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_stat271);  
                    stream_COLON_EQUALS.add(COLON_EQUALS9);

                    pushFollow(FOLLOW_rhsExpr_in_stat273);
                    rhsExpr10=rhsExpr();

                    state._fsp--;

                    stream_rhsExpr.add(rhsExpr10.getTree());
                    SEMI11=(Token)match(input,SEMI,FOLLOW_SEMI_in_stat276);  
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
                    // 50:38: -> ^( DEFINE ID rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:50:41: ^( DEFINE ID rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:7: rhsExpr ( SEMI )?
                    {
                    pushFollow(FOLLOW_rhsExpr_in_stat295);
                    rhsExpr12=rhsExpr();

                    state._fsp--;

                    stream_rhsExpr.add(rhsExpr12.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:32: ( SEMI )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==SEMI) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:32: SEMI
                            {
                            SEMI13=(Token)match(input,SEMI,FOLLOW_SEMI_in_stat314);  
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
                    // 51:39: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:42: ^( EXPR rhsExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:1: value : ( code | selector | rhsExpr );
    public final EulangParser.value_return value() throws RecognitionException {
        EulangParser.value_return retval = new EulangParser.value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code14 = null;

        EulangParser.selector_return selector15 = null;

        EulangParser.rhsExpr_return rhsExpr16 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:6: ( code | selector | rhsExpr )
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
            case CHAR_LITERAL:
            case STRING_LITERAL:
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:8: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_value336);
                    code14=code();

                    state._fsp--;

                    adaptor.addChild(root_0, code14.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:55:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_value351);
                    selector15=selector();

                    state._fsp--;

                    adaptor.addChild(root_0, selector15.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:56:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_value359);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:1: selector : LBRACKET selectorlist RBRACKET -> ^( LIST ( selectorlist )* ) ;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:9: ( LBRACKET selectorlist RBRACKET -> ^( LIST ( selectorlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:11: LBRACKET selectorlist RBRACKET
            {
            LBRACKET17=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector372);  
            stream_LBRACKET.add(LBRACKET17);

            pushFollow(FOLLOW_selectorlist_in_selector374);
            selectorlist18=selectorlist();

            state._fsp--;

            stream_selectorlist.add(selectorlist18.getTree());
            RBRACKET19=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector376);  
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
            // 60:45: -> ^( LIST ( selectorlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:48: ^( LIST ( selectorlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:55: ( selectorlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:1: selectorlist : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? ;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:13: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:15: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:15: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==LBRACE_LPAREN) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:16: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectorlist402);
                    selectoritem20=selectoritem();

                    state._fsp--;

                    adaptor.addChild(root_0, selectoritem20.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:29: ( COMMA selectoritem )*
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
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:31: COMMA selectoritem
                    	    {
                    	    COMMA21=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectorlist406); 
                    	    COMMA21_tree = (CommonTree)adaptor.create(COMMA21);
                    	    adaptor.addChild(root_0, COMMA21_tree);

                    	    pushFollow(FOLLOW_selectoritem_in_selectorlist408);
                    	    selectoritem22=selectoritem();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, selectoritem22.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:53: ( COMMA )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==COMMA) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:53: COMMA
                            {
                            COMMA23=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectorlist413); 
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:1: selectoritem : code ;
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code24 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:13: ( code )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:15: code
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_code_in_selectoritem438);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:69:1: code : LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE_LPAREN25=null;
        Token RPAREN28=null;
        Token RBRACE30=null;
        EulangParser.argdefs_return argdefs26 = null;

        EulangParser.xreturns_return xreturns27 = null;

        EulangParser.codestmtlist_return codestmtlist29 = null;


        CommonTree LBRACE_LPAREN25_tree=null;
        CommonTree RPAREN28_tree=null;
        CommonTree RBRACE30_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE_LPAREN=new RewriteRuleTokenStream(adaptor,"token LBRACE_LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:5: ( LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:9: LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE
            {
            LBRACE_LPAREN25=(Token)match(input,LBRACE_LPAREN,FOLLOW_LBRACE_LPAREN_in_code455);  
            stream_LBRACE_LPAREN.add(LBRACE_LPAREN25);

            pushFollow(FOLLOW_argdefs_in_code457);
            argdefs26=argdefs();

            state._fsp--;

            stream_argdefs.add(argdefs26.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:31: ( xreturns )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==RETURNS) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:31: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_code459);
                    xreturns27=xreturns();

                    state._fsp--;

                    stream_xreturns.add(xreturns27.getTree());

                    }
                    break;

            }

            RPAREN28=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_code462);  
            stream_RPAREN.add(RPAREN28);

            pushFollow(FOLLOW_codestmtlist_in_code464);
            codestmtlist29=codestmtlist();

            state._fsp--;

            stream_codestmtlist.add(codestmtlist29.getTree());
            RBRACE30=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code466);  
            stream_RBRACE.add(RBRACE30);



            // AST REWRITE
            // elements: xreturns, argdefs, codestmtlist
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 70:68: -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:71: ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CODE, "CODE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:78: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:86: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:96: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:70:106: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:1: argdefs : ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* ;
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA32=null;
        Token COMMA34=null;
        EulangParser.argdef_return argdef31 = null;

        EulangParser.argdef_return argdef33 = null;


        CommonTree COMMA32_tree=null;
        CommonTree COMMA34_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdef=new RewriteRuleSubtreeStream(adaptor,"rule argdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:8: ( ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==ID) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:11: argdef ( COMMA argdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_argdef_in_argdefs500);
                    argdef31=argdef();

                    state._fsp--;

                    stream_argdef.add(argdef31.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:18: ( COMMA argdef )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0==COMMA) ) {
                            int LA9_1 = input.LA(2);

                            if ( (LA9_1==ID) ) {
                                alt9=1;
                            }


                        }


                        switch (alt9) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:20: COMMA argdef
                    	    {
                    	    COMMA32=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs504);  
                    	    stream_COMMA.add(COMMA32);

                    	    pushFollow(FOLLOW_argdef_in_argdefs506);
                    	    argdef33=argdef();

                    	    state._fsp--;

                    	    stream_argdef.add(argdef33.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:35: ( COMMA )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==COMMA) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:35: COMMA
                            {
                            COMMA34=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs510);  
                            stream_COMMA.add(COMMA34);


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
            // 73:67: -> ( argdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:73:70: ( argdef )*
                while ( stream_argdef.hasNext() ) {
                    adaptor.addChild(root_0, stream_argdef.nextTree());

                }
                stream_argdef.reset();

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:1: argdef : ( ID ( COLON type ( EQUALS assignExpr )? )? -> ^( ARGDEF ID ( type )* ( assignExpr )* ) | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) );
    public final EulangParser.argdef_return argdef() throws RecognitionException {
        EulangParser.argdef_return retval = new EulangParser.argdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID35=null;
        Token COLON36=null;
        Token EQUALS38=null;
        Token ID40=null;
        Token EQUALS41=null;
        EulangParser.type_return type37 = null;

        EulangParser.assignExpr_return assignExpr39 = null;

        EulangParser.assignExpr_return assignExpr42 = null;


        CommonTree ID35_tree=null;
        CommonTree COLON36_tree=null;
        CommonTree EQUALS38_tree=null;
        CommonTree ID40_tree=null;
        CommonTree EQUALS41_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:7: ( ID ( COLON type ( EQUALS assignExpr )? )? -> ^( ARGDEF ID ( type )* ( assignExpr )* ) | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==ID) ) {
                int LA14_1 = input.LA(2);

                if ( (LA14_1==EQUALS) ) {
                    alt14=2;
                }
                else if ( ((LA14_1>=COLON && LA14_1<=COMMA)||LA14_1==RPAREN||LA14_1==RETURNS) ) {
                    alt14=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 14, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:10: ID ( COLON type ( EQUALS assignExpr )? )?
                    {
                    ID35=(Token)match(input,ID,FOLLOW_ID_in_argdef555);  
                    stream_ID.add(ID35);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:13: ( COLON type ( EQUALS assignExpr )? )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==COLON) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:14: COLON type ( EQUALS assignExpr )?
                            {
                            COLON36=(Token)match(input,COLON,FOLLOW_COLON_in_argdef558);  
                            stream_COLON.add(COLON36);

                            pushFollow(FOLLOW_type_in_argdef560);
                            type37=type();

                            state._fsp--;

                            stream_type.add(type37.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:25: ( EQUALS assignExpr )?
                            int alt12=2;
                            int LA12_0 = input.LA(1);

                            if ( (LA12_0==EQUALS) ) {
                                alt12=1;
                            }
                            switch (alt12) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:26: EQUALS assignExpr
                                    {
                                    EQUALS38=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdef563);  
                                    stream_EQUALS.add(EQUALS38);

                                    pushFollow(FOLLOW_assignExpr_in_argdef565);
                                    assignExpr39=assignExpr();

                                    state._fsp--;

                                    stream_assignExpr.add(assignExpr39.getTree());

                                    }
                                    break;

                            }


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: assignExpr, ID, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 76:51: -> ^( ARGDEF ID ( type )* ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:54: ^( ARGDEF ID ( type )* ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:66: ( type )*
                        while ( stream_type.hasNext() ) {
                            adaptor.addChild(root_1, stream_type.nextTree());

                        }
                        stream_type.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:76:72: ( assignExpr )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:7: ID EQUALS assignExpr
                    {
                    ID40=(Token)match(input,ID,FOLLOW_ID_in_argdef594);  
                    stream_ID.add(ID40);

                    EQUALS41=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdef596);  
                    stream_EQUALS.add(EQUALS41);

                    pushFollow(FOLLOW_assignExpr_in_argdef598);
                    assignExpr42=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr42.getTree());


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
                    // 77:31: -> ^( ARGDEF ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:34: ^( ARGDEF ID TYPE assignExpr )
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

    public static class xreturns_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xreturns"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:1: xreturns : RETURNS type -> type ;
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURNS43=null;
        EulangParser.type_return type44 = null;


        CommonTree RETURNS43_tree=null;
        RewriteRuleTokenStream stream_RETURNS=new RewriteRuleTokenStream(adaptor,"token RETURNS");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:9: ( RETURNS type -> type )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:80:11: RETURNS type
            {
            RETURNS43=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_xreturns623);  
            stream_RETURNS.add(RETURNS43);

            pushFollow(FOLLOW_type_in_xreturns625);
            type44=type();

            state._fsp--;

            stream_type.add(type44.getTree());


            // AST REWRITE
            // elements: type
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 80:29: -> type
            {
                adaptor.addChild(root_0, stream_type.nextTree());

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
    // $ANTLR end "xreturns"

    public static class type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:82:1: type : ID -> ^( TYPE ID ) ;
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID45=null;

        CommonTree ID45_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:82:5: ( ID -> ^( TYPE ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:82:8: ID
            {
            ID45=(Token)match(input,ID,FOLLOW_ID_in_type644);  
            stream_ID.add(ID45);



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
            // 82:17: -> ^( TYPE ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:82:20: ^( TYPE ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:1: codestmtlist : ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) ;
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI47=null;
        Token SEMI49=null;
        EulangParser.codeStmt_return codeStmt46 = null;

        EulangParser.codeStmt_return codeStmt48 = null;


        CommonTree SEMI47_tree=null;
        CommonTree SEMI49_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:13: ( ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==MINUS||LA17_0==LPAREN||LA17_0==NUMBER||LA17_0==ID||(LA17_0>=CHAR_LITERAL && LA17_0<=STRING_LITERAL)) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:16: codeStmt ( SEMI codeStmt )* ( SEMI )?
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist669);
                    codeStmt46=codeStmt();

                    state._fsp--;

                    stream_codeStmt.add(codeStmt46.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:25: ( SEMI codeStmt )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0==SEMI) ) {
                            int LA15_1 = input.LA(2);

                            if ( (LA15_1==MINUS||LA15_1==LPAREN||LA15_1==NUMBER||LA15_1==ID||(LA15_1>=CHAR_LITERAL && LA15_1<=STRING_LITERAL)) ) {
                                alt15=1;
                            }


                        }


                        switch (alt15) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:27: SEMI codeStmt
                    	    {
                    	    SEMI47=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist673);  
                    	    stream_SEMI.add(SEMI47);

                    	    pushFollow(FOLLOW_codeStmt_in_codestmtlist675);
                    	    codeStmt48=codeStmt();

                    	    state._fsp--;

                    	    stream_codeStmt.add(codeStmt48.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:44: ( SEMI )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==SEMI) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:44: SEMI
                            {
                            SEMI49=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist680);  
                            stream_SEMI.add(SEMI49);


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
            // 85:53: -> ^( STMTLIST ( codeStmt )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:57: ^( STMTLIST ( codeStmt )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:85:68: ( codeStmt )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:1: codeStmt : ( varDecl | assignExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl50 = null;

        EulangParser.assignExpr_return assignExpr51 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:10: ( varDecl | assignExpr )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==ID) ) {
                int LA18_1 = input.LA(2);

                if ( (LA18_1==COLON||LA18_1==COLON_EQUALS) ) {
                    alt18=1;
                }
                else if ( (LA18_1==EQUALS||(LA18_1>=PLUS && LA18_1<=LPAREN)||LA18_1==RBRACE||(LA18_1>=AMP && LA18_1<=UMOD)||LA18_1==COMPXOR) ) {
                    alt18=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA18_0==MINUS||LA18_0==LPAREN||LA18_0==NUMBER||(LA18_0>=CHAR_LITERAL && LA18_0<=STRING_LITERAL)) ) {
                alt18=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:88:12: varDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_varDecl_in_codeStmt711);
                    varDecl50=varDecl();

                    state._fsp--;

                    adaptor.addChild(root_0, varDecl50.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:89:9: assignExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignExpr_in_codeStmt721);
                    assignExpr51=assignExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, assignExpr51.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:92:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID52=null;
        Token COLON_EQUALS53=null;
        Token ID55=null;
        Token COLON56=null;
        Token EQUALS58=null;
        EulangParser.assignExpr_return assignExpr54 = null;

        EulangParser.type_return type57 = null;

        EulangParser.assignExpr_return assignExpr59 = null;


        CommonTree ID52_tree=null;
        CommonTree COLON_EQUALS53_tree=null;
        CommonTree ID55_tree=null;
        CommonTree COLON56_tree=null;
        CommonTree EQUALS58_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:92:8: ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==ID) ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==COLON_EQUALS) ) {
                    alt20=1;
                }
                else if ( (LA20_1==COLON) ) {
                    alt20=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:92:10: ID COLON_EQUALS assignExpr
                    {
                    ID52=(Token)match(input,ID,FOLLOW_ID_in_varDecl735);  
                    stream_ID.add(ID52);

                    COLON_EQUALS53=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl737);  
                    stream_COLON_EQUALS.add(COLON_EQUALS53);

                    pushFollow(FOLLOW_assignExpr_in_varDecl739);
                    assignExpr54=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr54.getTree());


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
                    // 92:45: -> ^( DEFINE ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:92:48: ^( DEFINE ID TYPE assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID55=(Token)match(input,ID,FOLLOW_ID_in_varDecl767);  
                    stream_ID.add(ID55);

                    COLON56=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl769);  
                    stream_COLON.add(COLON56);

                    pushFollow(FOLLOW_type_in_varDecl771);
                    type57=type();

                    state._fsp--;

                    stream_type.add(type57.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:21: ( EQUALS assignExpr )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==EQUALS) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:22: EQUALS assignExpr
                            {
                            EQUALS58=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl774);  
                            stream_EQUALS.add(EQUALS58);

                            pushFollow(FOLLOW_assignExpr_in_varDecl776);
                            assignExpr59=assignExpr();

                            state._fsp--;

                            stream_assignExpr.add(assignExpr59.getTree());

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
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 93:43: -> ^( DEFINE ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:46: ^( DEFINE ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:93:63: ( assignExpr )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:1: assignExpr : ( ID EQUALS assignExpr -> ^( ASSIGN ID assignExpr ) | rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID60=null;
        Token EQUALS61=null;
        EulangParser.assignExpr_return assignExpr62 = null;

        EulangParser.rhsExpr_return rhsExpr63 = null;


        CommonTree ID60_tree=null;
        CommonTree EQUALS61_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:12: ( ID EQUALS assignExpr -> ^( ASSIGN ID assignExpr ) | rhsExpr )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==ID) ) {
                int LA21_1 = input.LA(2);

                if ( (LA21_1==EQUALS) ) {
                    alt21=1;
                }
                else if ( (LA21_1==COMMA||(LA21_1>=PLUS && LA21_1<=RPAREN)||LA21_1==RBRACE||(LA21_1>=AMP && LA21_1<=RETURNS)||LA21_1==COMPXOR) ) {
                    alt21=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 21, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA21_0==MINUS||LA21_0==LPAREN||LA21_0==NUMBER||(LA21_0>=CHAR_LITERAL && LA21_0<=STRING_LITERAL)) ) {
                alt21=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:14: ID EQUALS assignExpr
                    {
                    ID60=(Token)match(input,ID,FOLLOW_ID_in_assignExpr805);  
                    stream_ID.add(ID60);

                    EQUALS61=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr807);  
                    stream_EQUALS.add(EQUALS61);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr809);
                    assignExpr62=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr62.getTree());


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
                    // 96:42: -> ^( ASSIGN ID assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:45: ^( ASSIGN ID assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:97:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_assignExpr834);
                    rhsExpr63=rhsExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, rhsExpr63.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:100:1: rhsExpr : cond ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.cond_return cond64 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:100:9: ( cond )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:100:13: cond
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_cond_in_rhsExpr849);
            cond64=cond();

            state._fsp--;

            adaptor.addChild(root_0, cond64.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:103:1: funcCall : ID LPAREN arglist RPAREN -> ^( CALL ID arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID65=null;
        Token LPAREN66=null;
        Token RPAREN68=null;
        EulangParser.arglist_return arglist67 = null;


        CommonTree ID65_tree=null;
        CommonTree LPAREN66_tree=null;
        CommonTree RPAREN68_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:103:10: ( ID LPAREN arglist RPAREN -> ^( CALL ID arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:103:12: ID LPAREN arglist RPAREN
            {
            ID65=(Token)match(input,ID,FOLLOW_ID_in_funcCall867);  
            stream_ID.add(ID65);

            LPAREN66=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall869);  
            stream_LPAREN.add(LPAREN66);

            pushFollow(FOLLOW_arglist_in_funcCall871);
            arglist67=arglist();

            state._fsp--;

            stream_arglist.add(arglist67.getTree());
            RPAREN68=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall873);  
            stream_RPAREN.add(RPAREN68);



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
            // 103:39: -> ^( CALL ID arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:103:46: ^( CALL ID arglist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA70=null;
        Token COMMA72=null;
        EulangParser.arg_return arg69 = null;

        EulangParser.arg_return arg71 = null;


        CommonTree COMMA70_tree=null;
        CommonTree COMMA72_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==MINUS||LA24_0==LPAREN||LA24_0==NUMBER||LA24_0==ID||(LA24_0>=CHAR_LITERAL && LA24_0<=STRING_LITERAL)) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist904);
                    arg69=arg();

                    state._fsp--;

                    stream_arg.add(arg69.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:15: ( COMMA arg )*
                    loop22:
                    do {
                        int alt22=2;
                        int LA22_0 = input.LA(1);

                        if ( (LA22_0==COMMA) ) {
                            int LA22_1 = input.LA(2);

                            if ( (LA22_1==MINUS||LA22_1==LPAREN||LA22_1==NUMBER||LA22_1==ID||(LA22_1>=CHAR_LITERAL && LA22_1<=STRING_LITERAL)) ) {
                                alt22=1;
                            }


                        }


                        switch (alt22) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:17: COMMA arg
                    	    {
                    	    COMMA70=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist908);  
                    	    stream_COMMA.add(COMMA70);

                    	    pushFollow(FOLLOW_arg_in_arglist910);
                    	    arg71=arg();

                    	    state._fsp--;

                    	    stream_arg.add(arg71.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop22;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:29: ( COMMA )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0==COMMA) ) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:29: COMMA
                            {
                            COMMA72=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist914);  
                            stream_COMMA.add(COMMA72);


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
            // 107:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:107:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:1: arg : assignExpr -> ^( EXPR assignExpr ) ;
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr73 = null;


        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:4: ( assignExpr -> ^( EXPR assignExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:7: assignExpr
            {
            pushFollow(FOLLOW_assignExpr_in_arg963);
            assignExpr73=assignExpr();

            state._fsp--;

            stream_assignExpr.add(assignExpr73.getTree());


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
            // 110:37: -> ^( EXPR assignExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:110:40: ^( EXPR assignExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:117:1: cond : (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION74=null;
        Token COLON75=null;
        EulangParser.logcond_return l = null;

        EulangParser.cond_return t = null;

        EulangParser.cond_return f = null;


        CommonTree QUESTION74_tree=null;
        CommonTree COLON75_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logcond=new RewriteRuleSubtreeStream(adaptor,"rule logcond");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:117:5: ( (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:117:10: (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:117:10: (l= logcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:117:12: l= logcond
            {
            pushFollow(FOLLOW_logcond_in_cond1013);
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
            // 117:23: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:118:7: ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==QUESTION) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:118:9: QUESTION t= cond COLON f= cond
            	    {
            	    QUESTION74=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond1031);  
            	    stream_QUESTION.add(QUESTION74);

            	    pushFollow(FOLLOW_cond_in_cond1035);
            	    t=cond();

            	    state._fsp--;

            	    stream_cond.add(t.getTree());
            	    COLON75=(Token)match(input,COLON,FOLLOW_COLON_in_cond1037);  
            	    stream_COLON.add(COLON75);

            	    pushFollow(FOLLOW_cond_in_cond1041);
            	    f=cond();

            	    state._fsp--;

            	    stream_cond.add(f.getTree());


            	    // AST REWRITE
            	    // elements: t, f, l
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
            	    // 118:38: -> ^( COND $l $t $f)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:118:41: ^( COND $l $t $f)
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

            	default :
            	    break loop25;
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
    // $ANTLR end "cond"

    public static class logcond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logcond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:1: logcond : (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )* ;
    public final EulangParser.logcond_return logcond() throws RecognitionException {
        EulangParser.logcond_return retval = new EulangParser.logcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPAND76=null;
        Token COMPOR77=null;
        Token COMPXOR78=null;
        EulangParser.binlogcond_return l = null;

        EulangParser.binlogcond_return r = null;


        CommonTree COMPAND76_tree=null;
        CommonTree COMPOR77_tree=null;
        CommonTree COMPXOR78_tree=null;
        RewriteRuleTokenStream stream_COMPAND=new RewriteRuleTokenStream(adaptor,"token COMPAND");
        RewriteRuleTokenStream stream_COMPXOR=new RewriteRuleTokenStream(adaptor,"token COMPXOR");
        RewriteRuleTokenStream stream_COMPOR=new RewriteRuleTokenStream(adaptor,"token COMPOR");
        RewriteRuleSubtreeStream stream_binlogcond=new RewriteRuleSubtreeStream(adaptor,"rule binlogcond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:8: ( (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:10: (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:10: (l= binlogcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:12: l= binlogcond
            {
            pushFollow(FOLLOW_binlogcond_in_logcond1079);
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
            // 122:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:123:7: ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )*
            loop26:
            do {
                int alt26=4;
                switch ( input.LA(1) ) {
                case COMPAND:
                    {
                    alt26=1;
                    }
                    break;
                case COMPOR:
                    {
                    alt26=2;
                    }
                    break;
                case COMPXOR:
                    {
                    alt26=3;
                    }
                    break;

                }

                switch (alt26) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:123:9: COMPAND r= binlogcond
            	    {
            	    COMPAND76=(Token)match(input,COMPAND,FOLLOW_COMPAND_in_logcond1106);  
            	    stream_COMPAND.add(COMPAND76);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1110);
            	    r=binlogcond();

            	    state._fsp--;

            	    stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, COMPAND
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
            	    // 123:30: -> ^( COMPAND $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:123:33: ^( COMPAND $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:124:9: COMPOR r= binlogcond
            	    {
            	    COMPOR77=(Token)match(input,COMPOR,FOLLOW_COMPOR_in_logcond1132);  
            	    stream_COMPOR.add(COMPOR77);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1136);
            	    r=binlogcond();

            	    state._fsp--;

            	    stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r, COMPOR
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
            	    // 124:29: -> ^( COMPOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:124:32: ^( COMPOR $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:9: COMPXOR r= binlogcond
            	    {
            	    COMPXOR78=(Token)match(input,COMPXOR,FOLLOW_COMPXOR_in_logcond1158);  
            	    stream_COMPXOR.add(COMPXOR78);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1162);
            	    r=binlogcond();

            	    state._fsp--;

            	    stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPXOR, r, l
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
            	    // 125:31: -> ^( COMPXOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:34: ^( COMPXOR $l $r)
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

            	default :
            	    break loop26;
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
    // $ANTLR end "logcond"

    public static class binlogcond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "binlogcond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:1: binlogcond : (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )* ;
    public final EulangParser.binlogcond_return binlogcond() throws RecognitionException {
        EulangParser.binlogcond_return retval = new EulangParser.binlogcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP79=null;
        Token BAR80=null;
        Token CARET81=null;
        EulangParser.compeq_return l = null;

        EulangParser.compeq_return r = null;


        CommonTree AMP79_tree=null;
        CommonTree BAR80_tree=null;
        CommonTree CARET81_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_compeq=new RewriteRuleSubtreeStream(adaptor,"rule compeq");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:11: ( (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:13: (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:13: (l= compeq -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:15: l= compeq
            {
            pushFollow(FOLLOW_compeq_in_binlogcond1196);
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
            // 129:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:130:7: ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )*
            loop27:
            do {
                int alt27=4;
                switch ( input.LA(1) ) {
                case AMP:
                    {
                    alt27=1;
                    }
                    break;
                case BAR:
                    {
                    alt27=2;
                    }
                    break;
                case CARET:
                    {
                    alt27=3;
                    }
                    break;

                }

                switch (alt27) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:130:9: AMP r= compeq
            	    {
            	    AMP79=(Token)match(input,AMP,FOLLOW_AMP_in_binlogcond1225);  
            	    stream_AMP.add(AMP79);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1229);
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
            	    // 130:23: -> ^( BITAND $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:130:26: ^( BITAND $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:9: BAR r= compeq
            	    {
            	    BAR80=(Token)match(input,BAR,FOLLOW_BAR_in_binlogcond1252);  
            	    stream_BAR.add(BAR80);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1256);
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
            	    // 131:23: -> ^( BITOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:131:26: ^( BITOR $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:9: CARET r= compeq
            	    {
            	    CARET81=(Token)match(input,CARET,FOLLOW_CARET_in_binlogcond1279);  
            	    stream_CARET.add(CARET81);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1283);
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
            	    // 132:25: -> ^( BITXOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:28: ^( BITXOR $l $r)
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

            	default :
            	    break loop27;
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
    // $ANTLR end "binlogcond"

    public static class compeq_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compeq"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:136:1: compeq : (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )* ;
    public final EulangParser.compeq_return compeq() throws RecognitionException {
        EulangParser.compeq_return retval = new EulangParser.compeq_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ82=null;
        Token COMPNE83=null;
        EulangParser.comp_return l = null;

        EulangParser.comp_return r = null;


        CommonTree COMPEQ82_tree=null;
        CommonTree COMPNE83_tree=null;
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:136:7: ( (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:136:11: (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:136:11: (l= comp -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:136:13: l= comp
            {
            pushFollow(FOLLOW_comp_in_compeq1319);
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
            // 136:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:7: ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )*
            loop28:
            do {
                int alt28=3;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==COMPEQ) ) {
                    alt28=1;
                }
                else if ( (LA28_0==COMPNE) ) {
                    alt28=2;
                }


                switch (alt28) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:9: COMPEQ r= comp
            	    {
            	    COMPEQ82=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_compeq1353);  
            	    stream_COMPEQ.add(COMPEQ82);

            	    pushFollow(FOLLOW_comp_in_compeq1357);
            	    r=comp();

            	    state._fsp--;

            	    stream_comp.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPEQ, l, r
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
            	    // 137:23: -> ^( COMPEQ $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:26: ^( COMPEQ $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:138:9: COMPNE r= comp
            	    {
            	    COMPNE83=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_compeq1379);  
            	    stream_COMPNE.add(COMPNE83);

            	    pushFollow(FOLLOW_comp_in_compeq1383);
            	    r=comp();

            	    state._fsp--;

            	    stream_comp.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPNE, l, r
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
            	    // 138:23: -> ^( COMPNE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:138:26: ^( COMPNE $l $r)
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

            	default :
            	    break loop28;
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
    // $ANTLR end "compeq"

    public static class comp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:1: comp : (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPLE84=null;
        Token COMPGE85=null;
        Token LESS86=null;
        Token GREATER87=null;
        EulangParser.shift_return l = null;

        EulangParser.shift_return r = null;


        CommonTree COMPLE84_tree=null;
        CommonTree COMPGE85_tree=null;
        CommonTree LESS86_tree=null;
        CommonTree GREATER87_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:5: ( (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:8: (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:8: (l= shift -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:10: l= shift
            {
            pushFollow(FOLLOW_shift_in_comp1417);
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
            // 142:28: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:143:7: ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )*
            loop29:
            do {
                int alt29=5;
                switch ( input.LA(1) ) {
                case COMPLE:
                    {
                    alt29=1;
                    }
                    break;
                case COMPGE:
                    {
                    alt29=2;
                    }
                    break;
                case LESS:
                    {
                    alt29=3;
                    }
                    break;
                case GREATER:
                    {
                    alt29=4;
                    }
                    break;

                }

                switch (alt29) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:143:9: COMPLE r= shift
            	    {
            	    COMPLE84=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp1444);  
            	    stream_COMPLE.add(COMPLE84);

            	    pushFollow(FOLLOW_shift_in_comp1448);
            	    r=shift();

            	    state._fsp--;

            	    stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, COMPLE, r
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
            	    // 143:27: -> ^( COMPLE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:143:30: ^( COMPLE $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:144:9: COMPGE r= shift
            	    {
            	    COMPGE85=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp1473);  
            	    stream_COMPGE.add(COMPGE85);

            	    pushFollow(FOLLOW_shift_in_comp1477);
            	    r=shift();

            	    state._fsp--;

            	    stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, COMPGE, l
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
            	    // 144:27: -> ^( COMPGE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:144:30: ^( COMPGE $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:9: LESS r= shift
            	    {
            	    LESS86=(Token)match(input,LESS,FOLLOW_LESS_in_comp1502);  
            	    stream_LESS.add(LESS86);

            	    pushFollow(FOLLOW_shift_in_comp1506);
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
            	    // 145:26: -> ^( LESS $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:29: ^( LESS $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:9: GREATER r= shift
            	    {
            	    GREATER87=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp1532);  
            	    stream_GREATER.add(GREATER87);

            	    pushFollow(FOLLOW_shift_in_comp1536);
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
            	    // 146:28: -> ^( GREATER $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:31: ^( GREATER $l $r)
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

            	default :
            	    break loop29;
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
    // $ANTLR end "comp"

    public static class shift_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:150:1: shift : (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT88=null;
        Token RSHIFT89=null;
        Token URSHIFT90=null;
        EulangParser.factor_return l = null;

        EulangParser.factor_return r = null;


        CommonTree LSHIFT88_tree=null;
        CommonTree RSHIFT89_tree=null;
        CommonTree URSHIFT90_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:150:6: ( (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:150:9: (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:150:9: (l= factor -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:150:11: l= factor
            {
            pushFollow(FOLLOW_factor_in_shift1588);
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
            // 150:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:151:7: ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )*
            loop30:
            do {
                int alt30=4;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt30=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt30=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt30=3;
                    }
                    break;

                }

                switch (alt30) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:151:9: LSHIFT r= factor
            	    {
            	    LSHIFT88=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift1621);  
            	    stream_LSHIFT.add(LSHIFT88);

            	    pushFollow(FOLLOW_factor_in_shift1625);
            	    r=factor();

            	    state._fsp--;

            	    stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r, LSHIFT
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
            	    // 151:27: -> ^( LSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:151:30: ^( LSHIFT $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:9: RSHIFT r= factor
            	    {
            	    RSHIFT89=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift1650);  
            	    stream_RSHIFT.add(RSHIFT89);

            	    pushFollow(FOLLOW_factor_in_shift1654);
            	    r=factor();

            	    state._fsp--;

            	    stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, RSHIFT
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
            	    // 152:27: -> ^( RSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:30: ^( RSHIFT $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:153:9: URSHIFT r= factor
            	    {
            	    URSHIFT90=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift1678);  
            	    stream_URSHIFT.add(URSHIFT90);

            	    pushFollow(FOLLOW_factor_in_shift1682);
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
            	    // 153:28: -> ^( URSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:153:31: ^( URSHIFT $l $r)
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
    // $ANTLR end "shift"

    public static class factor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "factor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:156:1: factor : (l= multExpr -> $l) ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS91=null;
        Token MINUS92=null;
        EulangParser.multExpr_return l = null;

        EulangParser.multExpr_return r = null;


        CommonTree PLUS91_tree=null;
        CommonTree MINUS92_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_multExpr=new RewriteRuleSubtreeStream(adaptor,"rule multExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:5: ( (l= multExpr -> $l) ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:7: (l= multExpr -> $l) ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:7: (l= multExpr -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:157:9: l= multExpr
            {
            pushFollow(FOLLOW_multExpr_in_factor1725);
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
            // 157:33: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:158:9: ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )*
            loop31:
            do {
                int alt31=3;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==MINUS) ) {
                    int LA31_2 = input.LA(2);

                    if ( (LA31_2==MINUS||LA31_2==LPAREN||LA31_2==NUMBER||LA31_2==ID||(LA31_2>=CHAR_LITERAL && LA31_2<=STRING_LITERAL)) ) {
                        alt31=2;
                    }


                }
                else if ( (LA31_0==PLUS) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:158:13: PLUS r= multExpr
            	    {
            	    PLUS91=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor1759);  
            	    stream_PLUS.add(PLUS91);

            	    pushFollow(FOLLOW_multExpr_in_factor1763);
            	    r=multExpr();

            	    state._fsp--;

            	    stream_multExpr.add(r.getTree());


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
            	    // 158:37: -> ^( ADD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:158:40: ^( ADD $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADD, "ADD"), root_1);

            	        adaptor.addChild(root_1, stream_l.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:13: MINUS r= multExpr
            	    {
            	    MINUS92=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor1797);  
            	    stream_MINUS.add(MINUS92);

            	    pushFollow(FOLLOW_multExpr_in_factor1801);
            	    r=multExpr();

            	    state._fsp--;

            	    stream_multExpr.add(r.getTree());


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
            	    // 159:37: -> ^( SUB $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:40: ^( SUB $l $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SUB, "SUB"), root_1);

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
    // $ANTLR end "factor"

    public static class multExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "multExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:1: multExpr : (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )* ;
    public final EulangParser.multExpr_return multExpr() throws RecognitionException {
        EulangParser.multExpr_return retval = new EulangParser.multExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR93=null;
        Token SLASH94=null;
        Token BACKSLASH95=null;
        Token PERCENT96=null;
        Token UMOD97=null;
        EulangParser.atom_return l = null;

        EulangParser.atom_return r = null;


        CommonTree STAR93_tree=null;
        CommonTree SLASH94_tree=null;
        CommonTree BACKSLASH95_tree=null;
        CommonTree PERCENT96_tree=null;
        CommonTree UMOD97_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:10: ( (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:12: (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:12: (l= atom -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:14: l= atom
            {
            pushFollow(FOLLOW_atom_in_multExpr1848);
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
            // 163:38: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:164:9: ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )*
            loop32:
            do {
                int alt32=6;
                switch ( input.LA(1) ) {
                case STAR:
                    {
                    alt32=1;
                    }
                    break;
                case SLASH:
                    {
                    alt32=2;
                    }
                    break;
                case BACKSLASH:
                    {
                    alt32=3;
                    }
                    break;
                case PERCENT:
                    {
                    alt32=4;
                    }
                    break;
                case UMOD:
                    {
                    alt32=5;
                    }
                    break;

                }

                switch (alt32) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:164:11: STAR r= atom
            	    {
            	    STAR93=(Token)match(input,STAR,FOLLOW_STAR_in_multExpr1884);  
            	    stream_STAR.add(STAR93);

            	    pushFollow(FOLLOW_atom_in_multExpr1888);
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
            	    // 164:35: -> ^( MUL $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:164:38: ^( MUL $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:165:11: SLASH r= atom
            	    {
            	    SLASH94=(Token)match(input,SLASH,FOLLOW_SLASH_in_multExpr1925);  
            	    stream_SLASH.add(SLASH94);

            	    pushFollow(FOLLOW_atom_in_multExpr1929);
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
            	    // 165:35: -> ^( DIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:165:38: ^( DIV $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:11: BACKSLASH r= atom
            	    {
            	    BACKSLASH95=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_multExpr1964);  
            	    stream_BACKSLASH.add(BACKSLASH95);

            	    pushFollow(FOLLOW_atom_in_multExpr1968);
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
            	    // 166:39: -> ^( UDIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:42: ^( UDIV $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:167:11: PERCENT r= atom
            	    {
            	    PERCENT96=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_multExpr2003);  
            	    stream_PERCENT.add(PERCENT96);

            	    pushFollow(FOLLOW_atom_in_multExpr2007);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


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
            	    // 167:37: -> ^( MOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:167:40: ^( MOD $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:168:11: UMOD r= atom
            	    {
            	    UMOD97=(Token)match(input,UMOD,FOLLOW_UMOD_in_multExpr2042);  
            	    stream_UMOD.add(UMOD97);

            	    pushFollow(FOLLOW_atom_in_multExpr2046);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, r, UMOD
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
            	    // 168:34: -> ^( UMOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:168:37: ^( UMOD $l $r)
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
            	    break loop32;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:172:1: atom options {k=2; } : ( MINUS NUMBER -> | NUMBER -> NUMBER | CHAR_LITERAL | STRING_LITERAL | funcCall -> funcCall | ID -> ID | LPAREN assignExpr RPAREN -> assignExpr );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS98=null;
        Token NUMBER99=null;
        Token NUMBER100=null;
        Token CHAR_LITERAL101=null;
        Token STRING_LITERAL102=null;
        Token ID104=null;
        Token LPAREN105=null;
        Token RPAREN107=null;
        EulangParser.funcCall_return funcCall103 = null;

        EulangParser.assignExpr_return assignExpr106 = null;


        CommonTree MINUS98_tree=null;
        CommonTree NUMBER99_tree=null;
        CommonTree NUMBER100_tree=null;
        CommonTree CHAR_LITERAL101_tree=null;
        CommonTree STRING_LITERAL102_tree=null;
        CommonTree ID104_tree=null;
        CommonTree LPAREN105_tree=null;
        CommonTree RPAREN107_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_funcCall=new RewriteRuleSubtreeStream(adaptor,"rule funcCall");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:172:23: ( MINUS NUMBER -> | NUMBER -> NUMBER | CHAR_LITERAL | STRING_LITERAL | funcCall -> funcCall | ID -> ID | LPAREN assignExpr RPAREN -> assignExpr )
            int alt33=7;
            alt33 = dfa33.predict(input);
            switch (alt33) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:173:5: MINUS NUMBER
                    {
                    MINUS98=(Token)match(input,MINUS,FOLLOW_MINUS_in_atom2131);  
                    stream_MINUS.add(MINUS98);

                    NUMBER99=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom2133);  
                    stream_NUMBER.add(NUMBER99);



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
                    // 173:43: ->
                    {
                        adaptor.addChild(root_0, new CommonTree(new CommonToken(NUMBER, "-" + (NUMBER99!=null?NUMBER99.getText():null))));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:174:9: NUMBER
                    {
                    NUMBER100=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom2172);  
                    stream_NUMBER.add(NUMBER100);



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
                    // 174:41: -> NUMBER
                    {
                        adaptor.addChild(root_0, stream_NUMBER.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:175:9: CHAR_LITERAL
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CHAR_LITERAL101=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom2211); 
                    CHAR_LITERAL101_tree = (CommonTree)adaptor.create(CHAR_LITERAL101);
                    adaptor.addChild(root_0, CHAR_LITERAL101_tree);


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:176:9: STRING_LITERAL
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    STRING_LITERAL102=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom2221); 
                    STRING_LITERAL102_tree = (CommonTree)adaptor.create(STRING_LITERAL102);
                    adaptor.addChild(root_0, STRING_LITERAL102_tree);


                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:177:9: funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom2231);
                    funcCall103=funcCall();

                    state._fsp--;

                    stream_funcCall.add(funcCall103.getTree());


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
                    // 177:53: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:178:9: ID
                    {
                    ID104=(Token)match(input,ID,FOLLOW_ID_in_atom2280);  
                    stream_ID.add(ID104);



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
                    // 178:48: -> ID
                    {
                        adaptor.addChild(root_0, stream_ID.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN105=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom2330);  
                    stream_LPAREN.add(LPAREN105);

                    pushFollow(FOLLOW_assignExpr_in_atom2332);
                    assignExpr106=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr106.getTree());
                    RPAREN107=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom2334);  
                    stream_RPAREN.add(RPAREN107);



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
                    // 179:48: -> assignExpr
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


    protected DFA33 dfa33 = new DFA33(this);
    static final String DFA33_eotS =
        "\52\uffff";
    static final String DFA33_eofS =
        "\5\uffff\1\10\44\uffff";
    static final String DFA33_minS =
        "\1\14\4\uffff\1\6\44\uffff";
    static final String DFA33_maxS =
        "\1\63\4\uffff\1\117\44\uffff";
    static final String DFA33_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\uffff\1\7\1\5\1\6\41\uffff";
    static final String DFA33_specialS =
        "\52\uffff}>";
    static final String[] DFA33_transitionS = {
            "\1\1\2\uffff\1\6\36\uffff\1\2\1\uffff\1\5\1\uffff\1\3\1\4",
            "",
            "",
            "",
            "",
            "\2\10\3\uffff\4\10\1\7\1\10\1\uffff\1\10\6\uffff\24\10\1\uffff"+
            "\1\10\1\uffff\1\10\1\uffff\2\10\33\uffff\1\10",
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
            ""
    };

    static final short[] DFA33_eot = DFA.unpackEncodedString(DFA33_eotS);
    static final short[] DFA33_eof = DFA.unpackEncodedString(DFA33_eofS);
    static final char[] DFA33_min = DFA.unpackEncodedStringToUnsignedChars(DFA33_minS);
    static final char[] DFA33_max = DFA.unpackEncodedStringToUnsignedChars(DFA33_maxS);
    static final short[] DFA33_accept = DFA.unpackEncodedString(DFA33_acceptS);
    static final short[] DFA33_special = DFA.unpackEncodedString(DFA33_specialS);
    static final short[][] DFA33_transition;

    static {
        int numStates = DFA33_transitionS.length;
        DFA33_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA33_transition[i] = DFA.unpackEncodedString(DFA33_transitionS[i]);
        }
    }

    class DFA33 extends DFA {

        public DFA33(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 33;
            this.eot = DFA33_eot;
            this.eof = DFA33_eof;
            this.min = DFA33_min;
            this.max = DFA33_max;
            this.accept = DFA33_accept;
            this.special = DFA33_special;
            this.transition = DFA33_transition;
        }
        public String getDescription() {
            return "172:1: atom options {k=2; } : ( MINUS NUMBER -> | NUMBER -> NUMBER | CHAR_LITERAL | STRING_LITERAL | funcCall -> funcCall | ID -> ID | LPAREN assignExpr RPAREN -> assignExpr );";
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog175 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stat_in_toplevelstmts206 = new BitSet(new long[]{0x000D400000009002L});
    public static final BitSet FOLLOW_ID_in_stat240 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_stat242 = new BitSet(new long[]{0x000D400000089010L});
    public static final BitSet FOLLOW_value_in_stat244 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_SEMI_in_stat250 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_stat269 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_stat271 = new BitSet(new long[]{0x000D400010009000L});
    public static final BitSet FOLLOW_rhsExpr_in_stat273 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_SEMI_in_stat276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_stat295 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_stat314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_value336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_value351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_value359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector372 = new BitSet(new long[]{0x0000000000100010L});
    public static final BitSet FOLLOW_selectorlist_in_selector374 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectorlist402 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_selectorlist406 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_selectoritem_in_selectorlist408 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_selectorlist413 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_selectoritem438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_LPAREN_in_code455 = new BitSet(new long[]{0x0001100000010000L});
    public static final BitSet FOLLOW_argdefs_in_code457 = new BitSet(new long[]{0x0000100000010000L});
    public static final BitSet FOLLOW_xreturns_in_code459 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_code462 = new BitSet(new long[]{0x000D400000049000L});
    public static final BitSet FOLLOW_codestmtlist_in_code464 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACE_in_code466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdef_in_argdefs500 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_argdefs504 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_argdef_in_argdefs506 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_argdefs510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdef555 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_COLON_in_argdef558 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_type_in_argdef560 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_EQUALS_in_argdef563 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_assignExpr_in_argdef565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdef594 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_argdef596 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_assignExpr_in_argdef598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_xreturns623 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_type_in_xreturns625 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_type644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist669 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist673 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist675 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmt711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_codeStmt721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl735 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl737 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl767 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_varDecl769 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_type_in_varDecl771 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl774 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignExpr805 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr807 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_rhsExpr849 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_funcCall867 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall869 = new BitSet(new long[]{0x000D400000019000L});
    public static final BitSet FOLLOW_arglist_in_funcCall871 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall873 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist904 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_arglist908 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_arg_in_arglist910 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_arglist914 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg963 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logcond_in_cond1013 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond1031 = new BitSet(new long[]{0x000D400000009040L});
    public static final BitSet FOLLOW_cond_in_cond1035 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_cond1037 = new BitSet(new long[]{0x000D400020009000L});
    public static final BitSet FOLLOW_cond_in_cond1041 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1079 = new BitSet(new long[]{0x00000000C0000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_COMPAND_in_logcond1106 = new BitSet(new long[]{0x000D4000C0009000L,0x0000000000008000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1110 = new BitSet(new long[]{0x00000000C0000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_COMPOR_in_logcond1132 = new BitSet(new long[]{0x000D4000C0009000L,0x0000000000008000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1136 = new BitSet(new long[]{0x00000000C0000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_COMPXOR_in_logcond1158 = new BitSet(new long[]{0x000D4000C0009000L,0x0000000000008000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1162 = new BitSet(new long[]{0x00000000C0000002L,0x0000000000008000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1196 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_AMP_in_binlogcond1225 = new BitSet(new long[]{0x000D40000E009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1229 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_BAR_in_binlogcond1252 = new BitSet(new long[]{0x000D40000E009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1256 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_CARET_in_binlogcond1279 = new BitSet(new long[]{0x000D40000E009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1283 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_comp_in_compeq1319 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_COMPEQ_in_compeq1353 = new BitSet(new long[]{0x000D400300009000L});
    public static final BitSet FOLLOW_comp_in_compeq1357 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_COMPNE_in_compeq1379 = new BitSet(new long[]{0x000D400300009000L});
    public static final BitSet FOLLOW_comp_in_compeq1383 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_shift_in_comp1417 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_COMPLE_in_comp1444 = new BitSet(new long[]{0x000D403C00009000L});
    public static final BitSet FOLLOW_shift_in_comp1448 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_COMPGE_in_comp1473 = new BitSet(new long[]{0x000D403C00009000L});
    public static final BitSet FOLLOW_shift_in_comp1477 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_LESS_in_comp1502 = new BitSet(new long[]{0x000D403C00009000L});
    public static final BitSet FOLLOW_shift_in_comp1506 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_GREATER_in_comp1532 = new BitSet(new long[]{0x000D403C00009000L});
    public static final BitSet FOLLOW_shift_in_comp1536 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_factor_in_shift1588 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_LSHIFT_in_shift1621 = new BitSet(new long[]{0x000D41C000009000L});
    public static final BitSet FOLLOW_factor_in_shift1625 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_RSHIFT_in_shift1650 = new BitSet(new long[]{0x000D41C000009000L});
    public static final BitSet FOLLOW_factor_in_shift1654 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_URSHIFT_in_shift1678 = new BitSet(new long[]{0x000D41C000009000L});
    public static final BitSet FOLLOW_factor_in_shift1682 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_multExpr_in_factor1725 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_PLUS_in_factor1759 = new BitSet(new long[]{0x000D400000009800L});
    public static final BitSet FOLLOW_multExpr_in_factor1763 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_MINUS_in_factor1797 = new BitSet(new long[]{0x000D400000009800L});
    public static final BitSet FOLLOW_multExpr_in_factor1801 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_atom_in_multExpr1848 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_STAR_in_multExpr1884 = new BitSet(new long[]{0x000D4E000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1888 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_SLASH_in_multExpr1925 = new BitSet(new long[]{0x000D4E000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1929 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_BACKSLASH_in_multExpr1964 = new BitSet(new long[]{0x000D4E000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr1968 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_PERCENT_in_multExpr2003 = new BitSet(new long[]{0x000D4E000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr2007 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_UMOD_in_multExpr2042 = new BitSet(new long[]{0x000D4E000000F000L});
    public static final BitSet FOLLOW_atom_in_multExpr2046 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_MINUS_in_atom2131 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_NUMBER_in_atom2133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom2172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom2211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom2221 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom2231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_atom2280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom2330 = new BitSet(new long[]{0x000D400000009000L});
    public static final BitSet FOLLOW_assignExpr_in_atom2332 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_atom2334 = new BitSet(new long[]{0x0000000000000002L});

}