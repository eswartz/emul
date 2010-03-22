// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g 2010-03-21 22:33:02

package org.ejs.eulang;
import java.util.HashMap;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class EulangParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "LBRACE_LPAREN", "LBRACE_STAR", "COLON", "COMMA", "EQUALS", "COLON_EQUALS", "COLON_COLON_EQUALS", "PLUS", "MINUS", "STAR", "SLASH", "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LBRACKET", "RBRACKET", "HASH", "EXCL", "TILDE", "AT", "AMP", "BAR", "CARET", "SEMI", "QUESTION", "COMPAND", "COMPOR", "COMPEQ", "COMPNE", "COMPGE", "COMPLE", "GREATER", "LESS", "LSHIFT", "RSHIFT", "URSHIFT", "BACKSLASH", "PERCENT", "UMOD", "RETURNS", "PERIOD", "RETURN", "FOR", "IN", "IDSUFFIX", "NUMBER", "ID", "SCOPEREF", "LETTERLIKE", "DIGIT", "CHAR_LITERAL", "STRING_LITERAL", "SPACE", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT", "SCOPE", "LIST_COMPREHENSION", "CODE", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "EXPR", "DEFINE_ASSIGN", "ASSIGN", "DEFINE", "LIST", "TYPE", "CALL", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "IDREF", "IDLIST", "COMPXOR"
    };
    public static final int STAR=13;
    public static final int MOD=86;
    public static final int LBRACE_STAR=5;
    public static final int AMP=25;
    public static final int LBRACE=17;
    public static final int MULTI_COMMENT=61;
    public static final int FOR=47;
    public static final int SUB=82;
    public static final int ARGLIST=67;
    public static final int EXCL=22;
    public static final int EQUALS=8;
    public static final int ID=51;
    public static final int DEFINE=73;
    public static final int SPACE=57;
    public static final int EOF=-1;
    public static final int BITAND=78;
    public static final int LPAREN=15;
    public static final int TYPE=75;
    public static final int CODE=64;
    public static final int AT=24;
    public static final int COLON_COLON_EQUALS=10;
    public static final int LBRACKET=19;
    public static final int RPAREN=16;
    public static final int IDSUFFIX=49;
    public static final int EXPR=70;
    public static final int STRING_LITERAL=56;
    public static final int SLASH=14;
    public static final int GREATER=36;
    public static final int IN=48;
    public static final int SCOPE=62;
    public static final int COMMA=7;
    public static final int SCOPEREF=52;
    public static final int COMPLE=35;
    public static final int CARET=27;
    public static final int BITXOR=80;
    public static final int RETURN=46;
    public static final int TILDE=23;
    public static final int LESS=37;
    public static final int PLUS=11;
    public static final int SINGLE_COMMENT=60;
    public static final int COMPAND=30;
    public static final int DIGIT=54;
    public static final int RBRACKET=20;
    public static final int RSHIFT=39;
    public static final int RETURNS=44;
    public static final int ADD=81;
    public static final int COMPGE=34;
    public static final int RBRACE=18;
    public static final int PERCENT=42;
    public static final int LIST_COMPREHENSION=63;
    public static final int LETTERLIKE=53;
    public static final int PERIOD=45;
    public static final int UMOD=43;
    public static final int LSHIFT=38;
    public static final int COMPOR=31;
    public static final int UDIV=85;
    public static final int LBRACE_LPAREN=4;
    public static final int NUMBER=50;
    public static final int HASH=21;
    public static final int MINUS=12;
    public static final int LIST=74;
    public static final int MUL=83;
    public static final int SEMI=28;
    public static final int DEFINE_ASSIGN=71;
    public static final int ARGDEF=68;
    public static final int REF=69;
    public static final int COLON=6;
    public static final int WS=59;
    public static final int BITOR=79;
    public static final int COLON_EQUALS=9;
    public static final int QUESTION=29;
    public static final int NEWLINE=58;
    public static final int CHAR_LITERAL=55;
    public static final int COMPXOR=89;
    public static final int STMTLIST=65;
    public static final int ASSIGN=72;
    public static final int URSHIFT=40;
    public static final int IDLIST=88;
    public static final int COMPEQ=32;
    public static final int CALL=76;
    public static final int IDREF=87;
    public static final int DIV=84;
    public static final int COND=77;
    public static final int PROTO=66;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:51:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog207);
            toplevelstmts1=toplevelstmts();

            state._fsp--;

            adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog209); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:1: toplevelstmts : ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstat_return toplevelstat3 = null;


        RewriteRuleSubtreeStream stream_toplevelstat=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:14: ( ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:16: ( toplevelstat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:16: ( toplevelstat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==MINUS||LA1_0==LPAREN||LA1_0==LBRACE||(LA1_0>=NUMBER && LA1_0<=SCOPEREF)||(LA1_0>=CHAR_LITERAL && LA1_0<=STRING_LITERAL)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:16: toplevelstat
            	    {
            	    pushFollow(FOLLOW_toplevelstat_in_toplevelstmts238);
            	    toplevelstat3=toplevelstat();

            	    state._fsp--;

            	    stream_toplevelstat.add(toplevelstat3.getTree());

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
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 54:35: -> ^( STMTLIST ( toplevelstat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:38: ^( STMTLIST ( toplevelstat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:54:49: ( toplevelstat )*
                while ( stream_toplevelstat.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstat.nextTree());

                }
                stream_toplevelstat.reset();

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

    public static class toplevelstat_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstat"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:1: toplevelstat : ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE_ASSIGN ID toplevelvalue ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) | xscope );
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:13: ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE_ASSIGN ID toplevelvalue ) | ID COLON_EQUALS rhsExpr SEMI -> ^( DEFINE ID rhsExpr ) | rhsExpr ( SEMI )? -> ^( EXPR rhsExpr ) | xscope )
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
                case PLUS:
                case MINUS:
                case STAR:
                case SLASH:
                case LPAREN:
                case LBRACE:
                case RBRACE:
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
                break;
            case MINUS:
            case LPAREN:
            case NUMBER:
            case SCOPEREF:
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
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:17: ID EQUALS toplevelvalue SEMI
                    {
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat272);  
                    stream_ID.add(ID4);

                    EQUALS5=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat274);  
                    stream_EQUALS.add(EQUALS5);

                    pushFollow(FOLLOW_toplevelvalue_in_toplevelstat276);
                    toplevelvalue6=toplevelvalue();

                    state._fsp--;

                    stream_toplevelvalue.add(toplevelvalue6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat282);  
                    stream_SEMI.add(SEMI7);



                    // AST REWRITE
                    // elements: ID, toplevelvalue
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 57:51: -> ^( DEFINE_ASSIGN ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:57:54: ^( DEFINE_ASSIGN ID toplevelvalue )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE_ASSIGN, "DEFINE_ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:58:7: ID COLON_EQUALS rhsExpr SEMI
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat301);  
                    stream_ID.add(ID8);

                    COLON_EQUALS9=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelstat303);  
                    stream_COLON_EQUALS.add(COLON_EQUALS9);

                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat305);
                    rhsExpr10=rhsExpr();

                    state._fsp--;

                    stream_rhsExpr.add(rhsExpr10.getTree());
                    SEMI11=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat308);  
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
                    // 58:38: -> ^( DEFINE ID rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:58:41: ^( DEFINE ID rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:59:7: rhsExpr ( SEMI )?
                    {
                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat327);
                    rhsExpr12=rhsExpr();

                    state._fsp--;

                    stream_rhsExpr.add(rhsExpr12.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:59:32: ( SEMI )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==SEMI) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:59:32: SEMI
                            {
                            SEMI13=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat346);  
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
                    // 59:39: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:59:42: ^( EXPR rhsExpr )
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
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:60:7: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelstat364);
                    xscope14=xscope();

                    state._fsp--;

                    adaptor.addChild(root_0, xscope14.getTree());

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
    // $ANTLR end "toplevelstat"

    public static class toplevelvalue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelvalue"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:1: toplevelvalue : ( xscope | code | proto | selector | rhsExpr );
    public final EulangParser.toplevelvalue_return toplevelvalue() throws RecognitionException {
        EulangParser.toplevelvalue_return retval = new EulangParser.toplevelvalue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.xscope_return xscope15 = null;

        EulangParser.code_return code16 = null;

        EulangParser.proto_return proto17 = null;

        EulangParser.selector_return selector18 = null;

        EulangParser.rhsExpr_return rhsExpr19 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:15: ( xscope | code | proto | selector | rhsExpr )
            int alt4=5;
            switch ( input.LA(1) ) {
            case LBRACE:
                {
                alt4=1;
                }
                break;
            case LBRACE_LPAREN:
                {
                alt4=2;
                }
                break;
            case LPAREN:
                {
                switch ( input.LA(2) ) {
                case ID:
                    {
                    switch ( input.LA(3) ) {
                    case EQUALS:
                    case PLUS:
                    case MINUS:
                    case STAR:
                    case SLASH:
                    case LPAREN:
                    case AMP:
                    case BAR:
                    case CARET:
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
                    case COMPXOR:
                        {
                        alt4=5;
                        }
                        break;
                    case COLON:
                    case COMMA:
                    case RETURNS:
                        {
                        alt4=3;
                        }
                        break;
                    case RPAREN:
                        {
                        alt4=3;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 4, 6, input);

                        throw nvae;
                    }

                    }
                    break;
                case RPAREN:
                case RETURNS:
                    {
                    alt4=3;
                    }
                    break;
                case MINUS:
                case LPAREN:
                case NUMBER:
                case SCOPEREF:
                case CHAR_LITERAL:
                case STRING_LITERAL:
                    {
                    alt4=5;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 3, input);

                    throw nvae;
                }

                }
                break;
            case LBRACKET:
                {
                alt4=4;
                }
                break;
            case MINUS:
            case NUMBER:
            case ID:
            case SCOPEREF:
            case CHAR_LITERAL:
            case STRING_LITERAL:
                {
                alt4=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:63:17: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelvalue377);
                    xscope15=xscope();

                    state._fsp--;

                    adaptor.addChild(root_0, xscope15.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:64:7: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_toplevelvalue385);
                    code16=code();

                    state._fsp--;

                    adaptor.addChild(root_0, code16.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:65:7: proto
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_proto_in_toplevelvalue393);
                    proto17=proto();

                    state._fsp--;

                    adaptor.addChild(root_0, proto17.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:66:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_toplevelvalue408);
                    selector18=selector();

                    state._fsp--;

                    adaptor.addChild(root_0, selector18.getTree());

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:67:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue416);
                    rhsExpr19=rhsExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, rhsExpr19.getTree());

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
    // $ANTLR end "toplevelvalue"

    public static class selector_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selector"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:71:1: selector : LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET20=null;
        Token RBRACKET22=null;
        EulangParser.selectors_return selectors21 = null;


        CommonTree LBRACKET20_tree=null;
        CommonTree RBRACKET22_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectors=new RewriteRuleSubtreeStream(adaptor,"rule selectors");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:71:9: ( LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:71:11: LBRACKET selectors RBRACKET
            {
            LBRACKET20=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector429);  
            stream_LBRACKET.add(LBRACKET20);

            pushFollow(FOLLOW_selectors_in_selector431);
            selectors21=selectors();

            state._fsp--;

            stream_selectors.add(selectors21.getTree());
            RBRACKET22=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector433);  
            stream_RBRACKET.add(RBRACKET22);



            // AST REWRITE
            // elements: selectors
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 71:42: -> ^( LIST ( selectors )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:71:45: ^( LIST ( selectors )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:71:52: ( selectors )*
                while ( stream_selectors.hasNext() ) {
                    adaptor.addChild(root_1, stream_selectors.nextTree());

                }
                stream_selectors.reset();

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

    public static class selectors_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectors"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:1: selectors : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? ;
    public final EulangParser.selectors_return selectors() throws RecognitionException {
        EulangParser.selectors_return retval = new EulangParser.selectors_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA24=null;
        Token COMMA26=null;
        EulangParser.selectoritem_return selectoritem23 = null;

        EulangParser.selectoritem_return selectoritem25 = null;


        CommonTree COMMA24_tree=null;
        CommonTree COMMA26_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:10: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==LBRACE_LPAREN||LA7_0==FOR) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:13: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectors459);
                    selectoritem23=selectoritem();

                    state._fsp--;

                    adaptor.addChild(root_0, selectoritem23.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:26: ( COMMA selectoritem )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            int LA5_1 = input.LA(2);

                            if ( (LA5_1==LBRACE_LPAREN||LA5_1==FOR) ) {
                                alt5=1;
                            }


                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:28: COMMA selectoritem
                    	    {
                    	    COMMA24=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors463); 
                    	    COMMA24_tree = (CommonTree)adaptor.create(COMMA24);
                    	    adaptor.addChild(root_0, COMMA24_tree);

                    	    pushFollow(FOLLOW_selectoritem_in_selectors465);
                    	    selectoritem25=selectoritem();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, selectoritem25.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:50: ( COMMA )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==COMMA) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:74:50: COMMA
                            {
                            COMMA26=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors470); 
                            COMMA26_tree = (CommonTree)adaptor.create(COMMA26);
                            adaptor.addChild(root_0, COMMA26_tree);


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
    // $ANTLR end "selectors"

    public static class selectoritem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "selectoritem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:1: selectoritem : ( listCompr | code );
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.listCompr_return listCompr27 = null;

        EulangParser.code_return code28 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:13: ( listCompr | code )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==FOR) ) {
                alt8=1;
            }
            else if ( (LA8_0==LBRACE_LPAREN) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:15: listCompr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_listCompr_in_selectoritem495);
                    listCompr27=listCompr();

                    state._fsp--;

                    adaptor.addChild(root_0, listCompr27.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:77:27: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_selectoritem499);
                    code28=code();

                    state._fsp--;

                    adaptor.addChild(root_0, code28.getTree());

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
    // $ANTLR end "selectoritem"

    public static class xscope_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xscope"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:1: xscope : LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) ;
    public final EulangParser.xscope_return xscope() throws RecognitionException {
        EulangParser.xscope_return retval = new EulangParser.xscope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE29=null;
        Token RBRACE31=null;
        EulangParser.toplevelstmts_return toplevelstmts30 = null;


        CommonTree LBRACE29_tree=null;
        CommonTree RBRACE31_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmts=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmts");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:8: ( LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:10: LBRACE toplevelstmts RBRACE
            {
            LBRACE29=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope511);  
            stream_LBRACE.add(LBRACE29);

            pushFollow(FOLLOW_toplevelstmts_in_xscope513);
            toplevelstmts30=toplevelstmts();

            state._fsp--;

            stream_toplevelstmts.add(toplevelstmts30.getTree());
            RBRACE31=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope515);  
            stream_RBRACE.add(RBRACE31);



            // AST REWRITE
            // elements: toplevelstmts
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 81:41: -> ^( SCOPE ( toplevelstmts )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:44: ^( SCOPE ( toplevelstmts )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:81:52: ( toplevelstmts )*
                while ( stream_toplevelstmts.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstmts.nextTree());

                }
                stream_toplevelstmts.reset();

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
    // $ANTLR end "xscope"

    public static class listCompr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listCompr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:86:1: listCompr : ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) ;
    public final EulangParser.listCompr_return listCompr() throws RecognitionException {
        EulangParser.listCompr_return retval = new EulangParser.listCompr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON33=null;
        EulangParser.forIn_return forIn32 = null;

        EulangParser.listiterable_return listiterable34 = null;


        CommonTree COLON33_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_forIn=new RewriteRuleSubtreeStream(adaptor,"rule forIn");
        RewriteRuleSubtreeStream stream_listiterable=new RewriteRuleSubtreeStream(adaptor,"rule listiterable");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:86:10: ( ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:86:12: ( forIn )+ COLON listiterable
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:86:12: ( forIn )+
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:86:12: forIn
            	    {
            	    pushFollow(FOLLOW_forIn_in_listCompr542);
            	    forIn32=forIn();

            	    state._fsp--;

            	    stream_forIn.add(forIn32.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);

            COLON33=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr545);  
            stream_COLON.add(COLON33);

            pushFollow(FOLLOW_listiterable_in_listCompr547);
            listiterable34=listiterable();

            state._fsp--;

            stream_listiterable.add(listiterable34.getTree());


            // AST REWRITE
            // elements: forIn, listiterable
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 86:42: -> ^( LIST_COMPREHENSION ( forIn )+ listiterable )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:86:45: ^( LIST_COMPREHENSION ( forIn )+ listiterable )
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
    // $ANTLR end "listCompr"

    public static class forIn_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forIn"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:89:1: forIn : FOR idlist IN list -> ^( FOR idlist list ) ;
    public final EulangParser.forIn_return forIn() throws RecognitionException {
        EulangParser.forIn_return retval = new EulangParser.forIn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR35=null;
        Token IN37=null;
        EulangParser.idlist_return idlist36 = null;

        EulangParser.list_return list38 = null;


        CommonTree FOR35_tree=null;
        CommonTree IN37_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_list=new RewriteRuleSubtreeStream(adaptor,"rule list");
        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:89:7: ( FOR idlist IN list -> ^( FOR idlist list ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:89:9: FOR idlist IN list
            {
            FOR35=(Token)match(input,FOR,FOLLOW_FOR_in_forIn579);  
            stream_FOR.add(FOR35);

            pushFollow(FOLLOW_idlist_in_forIn581);
            idlist36=idlist();

            state._fsp--;

            stream_idlist.add(idlist36.getTree());
            IN37=(Token)match(input,IN,FOLLOW_IN_in_forIn583);  
            stream_IN.add(IN37);

            pushFollow(FOLLOW_list_in_forIn585);
            list38=list();

            state._fsp--;

            stream_list.add(list38.getTree());


            // AST REWRITE
            // elements: FOR, list, idlist
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 89:33: -> ^( FOR idlist list )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:89:36: ^( FOR idlist list )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idlist.nextTree());
                adaptor.addChild(root_1, stream_list.nextTree());

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
    // $ANTLR end "forIn"

    public static class idlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:1: idlist : ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) ;
    public final EulangParser.idlist_return idlist() throws RecognitionException {
        EulangParser.idlist_return retval = new EulangParser.idlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID39=null;
        Token COMMA40=null;
        Token ID41=null;

        CommonTree ID39_tree=null;
        CommonTree COMMA40_tree=null;
        CommonTree ID41_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:8: ( ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:10: ID ( COMMA ID )*
            {
            ID39=(Token)match(input,ID,FOLLOW_ID_in_idlist610);  
            stream_ID.add(ID39);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:13: ( COMMA ID )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==COMMA) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:14: COMMA ID
            	    {
            	    COMMA40=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist613);  
            	    stream_COMMA.add(COMMA40);

            	    ID41=(Token)match(input,ID,FOLLOW_ID_in_idlist615);  
            	    stream_ID.add(ID41);


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
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 91:28: -> ^( IDLIST ( ID )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:91:31: ^( IDLIST ( ID )+ )
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
    // $ANTLR end "idlist"

    public static class listiterable_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listiterable"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:94:1: listiterable : ( code | proto ) ;
    public final EulangParser.listiterable_return listiterable() throws RecognitionException {
        EulangParser.listiterable_return retval = new EulangParser.listiterable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code42 = null;

        EulangParser.proto_return proto43 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:94:14: ( ( code | proto ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:94:16: ( code | proto )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:94:16: ( code | proto )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==LBRACE_LPAREN) ) {
                alt11=1;
            }
            else if ( (LA11_0==LPAREN) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:94:18: code
                    {
                    pushFollow(FOLLOW_code_in_listiterable644);
                    code42=code();

                    state._fsp--;

                    adaptor.addChild(root_0, code42.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:94:25: proto
                    {
                    pushFollow(FOLLOW_proto_in_listiterable648);
                    proto43=proto();

                    state._fsp--;

                    adaptor.addChild(root_0, proto43.getTree());

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
    // $ANTLR end "listiterable"

    public static class list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "list"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:1: list : LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) ;
    public final EulangParser.list_return list() throws RecognitionException {
        EulangParser.list_return retval = new EulangParser.list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET44=null;
        Token RBRACKET46=null;
        EulangParser.listitems_return listitems45 = null;


        CommonTree LBRACKET44_tree=null;
        CommonTree RBRACKET46_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_listitems=new RewriteRuleSubtreeStream(adaptor,"rule listitems");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:6: ( LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:8: LBRACKET listitems RBRACKET
            {
            LBRACKET44=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list663);  
            stream_LBRACKET.add(LBRACKET44);

            pushFollow(FOLLOW_listitems_in_list665);
            listitems45=listitems();

            state._fsp--;

            stream_listitems.add(listitems45.getTree());
            RBRACKET46=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list667);  
            stream_RBRACKET.add(RBRACKET46);



            // AST REWRITE
            // elements: listitems
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 96:40: -> ^( LIST ( listitems )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:43: ^( LIST ( listitems )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:96:50: ( listitems )*
                while ( stream_listitems.hasNext() ) {
                    adaptor.addChild(root_1, stream_listitems.nextTree());

                }
                stream_listitems.reset();

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
    // $ANTLR end "list"

    public static class listitems_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listitems"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:1: listitems : ( listitem ( COMMA listitem )* ( COMMA )? )? ;
    public final EulangParser.listitems_return listitems() throws RecognitionException {
        EulangParser.listitems_return retval = new EulangParser.listitems_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA48=null;
        Token COMMA50=null;
        EulangParser.listitem_return listitem47 = null;

        EulangParser.listitem_return listitem49 = null;


        CommonTree COMMA48_tree=null;
        CommonTree COMMA50_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:10: ( ( listitem ( COMMA listitem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==LBRACE_LPAREN||LA14_0==MINUS||LA14_0==LPAREN||LA14_0==LBRACE||LA14_0==LBRACKET||(LA14_0>=NUMBER && LA14_0<=SCOPEREF)||(LA14_0>=CHAR_LITERAL && LA14_0<=STRING_LITERAL)) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems697);
                    listitem47=listitem();

                    state._fsp--;

                    adaptor.addChild(root_0, listitem47.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:22: ( COMMA listitem )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==COMMA) ) {
                            int LA12_1 = input.LA(2);

                            if ( (LA12_1==LBRACE_LPAREN||LA12_1==MINUS||LA12_1==LPAREN||LA12_1==LBRACE||LA12_1==LBRACKET||(LA12_1>=NUMBER && LA12_1<=SCOPEREF)||(LA12_1>=CHAR_LITERAL && LA12_1<=STRING_LITERAL)) ) {
                                alt12=1;
                            }


                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:24: COMMA listitem
                    	    {
                    	    COMMA48=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems701); 
                    	    COMMA48_tree = (CommonTree)adaptor.create(COMMA48);
                    	    adaptor.addChild(root_0, COMMA48_tree);

                    	    pushFollow(FOLLOW_listitem_in_listitems703);
                    	    listitem49=listitem();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, listitem49.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:42: ( COMMA )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==COMMA) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:99:42: COMMA
                            {
                            COMMA50=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems708); 
                            COMMA50_tree = (CommonTree)adaptor.create(COMMA50);
                            adaptor.addChild(root_0, COMMA50_tree);


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
    // $ANTLR end "listitems"

    public static class listitem_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listitem"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:1: listitem : toplevelvalue ;
    public final EulangParser.listitem_return listitem() throws RecognitionException {
        EulangParser.listitem_return retval = new EulangParser.listitem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelvalue_return toplevelvalue51 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:10: ( toplevelvalue )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:102:12: toplevelvalue
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelvalue_in_listitem734);
            toplevelvalue51=toplevelvalue();

            state._fsp--;

            adaptor.addChild(root_0, toplevelvalue51.getTree());

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
    // $ANTLR end "listitem"

    public static class proto_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "proto"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:1: proto : LPAREN protoargdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( protoargdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN52=null;
        Token RPAREN55=null;
        EulangParser.protoargdefs_return protoargdefs53 = null;

        EulangParser.xreturns_return xreturns54 = null;


        CommonTree LPAREN52_tree=null;
        CommonTree RPAREN55_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_protoargdefs=new RewriteRuleSubtreeStream(adaptor,"rule protoargdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:7: ( LPAREN protoargdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( protoargdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:11: LPAREN protoargdefs ( xreturns )? RPAREN
            {
            LPAREN52=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto754);  
            stream_LPAREN.add(LPAREN52);

            pushFollow(FOLLOW_protoargdefs_in_proto756);
            protoargdefs53=protoargdefs();

            state._fsp--;

            stream_protoargdefs.add(protoargdefs53.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:31: ( xreturns )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==RETURNS) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:31: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto758);
                    xreturns54=xreturns();

                    state._fsp--;

                    stream_xreturns.add(xreturns54.getTree());

                    }
                    break;

            }

            RPAREN55=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto761);  
            stream_RPAREN.add(RPAREN55);



            // AST REWRITE
            // elements: xreturns, protoargdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 109:66: -> ^( PROTO ( xreturns )? ( protoargdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:69: ^( PROTO ( xreturns )? ( protoargdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:77: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:109:87: ( protoargdefs )*
                while ( stream_protoargdefs.hasNext() ) {
                    adaptor.addChild(root_1, stream_protoargdefs.nextTree());

                }
                stream_protoargdefs.reset();

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
    // $ANTLR end "proto"

    public static class protoargdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "protoargdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:1: protoargdefs : ( protoargdef ( COMMA protoargdef )* ( COMMA )? )? -> ( protoargdef )* ;
    public final EulangParser.protoargdefs_return protoargdefs() throws RecognitionException {
        EulangParser.protoargdefs_return retval = new EulangParser.protoargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA57=null;
        Token COMMA59=null;
        EulangParser.protoargdef_return protoargdef56 = null;

        EulangParser.protoargdef_return protoargdef58 = null;


        CommonTree COMMA57_tree=null;
        CommonTree COMMA59_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_protoargdef=new RewriteRuleSubtreeStream(adaptor,"rule protoargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:13: ( ( protoargdef ( COMMA protoargdef )* ( COMMA )? )? -> ( protoargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:15: ( protoargdef ( COMMA protoargdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:15: ( protoargdef ( COMMA protoargdef )* ( COMMA )? )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==ID) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:16: protoargdef ( COMMA protoargdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_protoargdef_in_protoargdefs804);
                    protoargdef56=protoargdef();

                    state._fsp--;

                    stream_protoargdef.add(protoargdef56.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:28: ( COMMA protoargdef )*
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
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:30: COMMA protoargdef
                    	    {
                    	    COMMA57=(Token)match(input,COMMA,FOLLOW_COMMA_in_protoargdefs808);  
                    	    stream_COMMA.add(COMMA57);

                    	    pushFollow(FOLLOW_protoargdef_in_protoargdefs810);
                    	    protoargdef58=protoargdef();

                    	    state._fsp--;

                    	    stream_protoargdef.add(protoargdef58.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop16;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:50: ( COMMA )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==COMMA) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:50: COMMA
                            {
                            COMMA59=(Token)match(input,COMMA,FOLLOW_COMMA_in_protoargdefs814);  
                            stream_COMMA.add(COMMA59);


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
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 112:82: -> ( protoargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:112:85: ( protoargdef )*
                while ( stream_protoargdef.hasNext() ) {
                    adaptor.addChild(root_0, stream_protoargdef.nextTree());

                }
                stream_protoargdef.reset();

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
    // $ANTLR end "protoargdefs"

    public static class protoargdef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "protoargdef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:1: protoargdef : ID ( COLON type ( EQUALS rhsExpr )? )? -> ^( ARGDEF ID ( type )* ( rhsExpr )* ) ;
    public final EulangParser.protoargdef_return protoargdef() throws RecognitionException {
        EulangParser.protoargdef_return retval = new EulangParser.protoargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID60=null;
        Token COLON61=null;
        Token EQUALS63=null;
        EulangParser.type_return type62 = null;

        EulangParser.rhsExpr_return rhsExpr64 = null;


        CommonTree ID60_tree=null;
        CommonTree COLON61_tree=null;
        CommonTree EQUALS63_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:12: ( ID ( COLON type ( EQUALS rhsExpr )? )? -> ^( ARGDEF ID ( type )* ( rhsExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:15: ID ( COLON type ( EQUALS rhsExpr )? )?
            {
            ID60=(Token)match(input,ID,FOLLOW_ID_in_protoargdef859);  
            stream_ID.add(ID60);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:18: ( COLON type ( EQUALS rhsExpr )? )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==COLON) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:19: COLON type ( EQUALS rhsExpr )?
                    {
                    COLON61=(Token)match(input,COLON,FOLLOW_COLON_in_protoargdef862);  
                    stream_COLON.add(COLON61);

                    pushFollow(FOLLOW_type_in_protoargdef864);
                    type62=type();

                    state._fsp--;

                    stream_type.add(type62.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:30: ( EQUALS rhsExpr )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==EQUALS) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:31: EQUALS rhsExpr
                            {
                            EQUALS63=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_protoargdef867);  
                            stream_EQUALS.add(EQUALS63);

                            pushFollow(FOLLOW_rhsExpr_in_protoargdef869);
                            rhsExpr64=rhsExpr();

                            state._fsp--;

                            stream_rhsExpr.add(rhsExpr64.getTree());

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
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 115:53: -> ^( ARGDEF ID ( type )* ( rhsExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:56: ^( ARGDEF ID ( type )* ( rhsExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:68: ( type )*
                while ( stream_type.hasNext() ) {
                    adaptor.addChild(root_1, stream_type.nextTree());

                }
                stream_type.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:115:74: ( rhsExpr )*
                while ( stream_rhsExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_rhsExpr.nextTree());

                }
                stream_rhsExpr.reset();

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
    // $ANTLR end "protoargdef"

    public static class code_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "code"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:1: code : LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE_LPAREN65=null;
        Token RPAREN68=null;
        Token RBRACE70=null;
        EulangParser.argdefs_return argdefs66 = null;

        EulangParser.xreturns_return xreturns67 = null;

        EulangParser.codestmtlist_return codestmtlist69 = null;


        CommonTree LBRACE_LPAREN65_tree=null;
        CommonTree RPAREN68_tree=null;
        CommonTree RBRACE70_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE_LPAREN=new RewriteRuleTokenStream(adaptor,"token LBRACE_LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:6: ( LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:10: LBRACE_LPAREN argdefs ( xreturns )? RPAREN codestmtlist RBRACE
            {
            LBRACE_LPAREN65=(Token)match(input,LBRACE_LPAREN,FOLLOW_LBRACE_LPAREN_in_code906);  
            stream_LBRACE_LPAREN.add(LBRACE_LPAREN65);

            pushFollow(FOLLOW_argdefs_in_code908);
            argdefs66=argdefs();

            state._fsp--;

            stream_argdefs.add(argdefs66.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:32: ( xreturns )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==RETURNS) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:32: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_code910);
                    xreturns67=xreturns();

                    state._fsp--;

                    stream_xreturns.add(xreturns67.getTree());

                    }
                    break;

            }

            RPAREN68=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_code913);  
            stream_RPAREN.add(RPAREN68);

            pushFollow(FOLLOW_codestmtlist_in_code915);
            codestmtlist69=codestmtlist();

            state._fsp--;

            stream_codestmtlist.add(codestmtlist69.getTree());
            RBRACE70=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code917);  
            stream_RBRACE.add(RBRACE70);



            // AST REWRITE
            // elements: codestmtlist, argdefs, xreturns
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 119:69: -> ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:72: ^( CODE ^( PROTO ( xreturns )? ( argdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CODE, "CODE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:79: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:87: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:97: ( argdefs )*
                while ( stream_argdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_argdefs.nextTree());

                }
                stream_argdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:119:107: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:1: argdefs : ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* ;
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA72=null;
        Token COMMA74=null;
        EulangParser.argdef_return argdef71 = null;

        EulangParser.argdef_return argdef73 = null;


        CommonTree COMMA72_tree=null;
        CommonTree COMMA74_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdef=new RewriteRuleSubtreeStream(adaptor,"rule argdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:8: ( ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==ID) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:11: argdef ( COMMA argdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_argdef_in_argdefs951);
                    argdef71=argdef();

                    state._fsp--;

                    stream_argdef.add(argdef71.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:18: ( COMMA argdef )*
                    loop22:
                    do {
                        int alt22=2;
                        int LA22_0 = input.LA(1);

                        if ( (LA22_0==COMMA) ) {
                            int LA22_1 = input.LA(2);

                            if ( (LA22_1==ID) ) {
                                alt22=1;
                            }


                        }


                        switch (alt22) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:20: COMMA argdef
                    	    {
                    	    COMMA72=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs955);  
                    	    stream_COMMA.add(COMMA72);

                    	    pushFollow(FOLLOW_argdef_in_argdefs957);
                    	    argdef73=argdef();

                    	    state._fsp--;

                    	    stream_argdef.add(argdef73.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop22;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:35: ( COMMA )?
                    int alt23=2;
                    int LA23_0 = input.LA(1);

                    if ( (LA23_0==COMMA) ) {
                        alt23=1;
                    }
                    switch (alt23) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:35: COMMA
                            {
                            COMMA74=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs961);  
                            stream_COMMA.add(COMMA74);


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
            // 122:67: -> ( argdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:122:70: ( argdef )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:1: argdef : ( protoargdef | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) );
    public final EulangParser.argdef_return argdef() throws RecognitionException {
        EulangParser.argdef_return retval = new EulangParser.argdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID76=null;
        Token EQUALS77=null;
        EulangParser.protoargdef_return protoargdef75 = null;

        EulangParser.assignExpr_return assignExpr78 = null;


        CommonTree ID76_tree=null;
        CommonTree EQUALS77_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:7: ( protoargdef | ID EQUALS assignExpr -> ^( ARGDEF ID TYPE assignExpr ) )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==ID) ) {
                int LA25_1 = input.LA(2);

                if ( (LA25_1==EQUALS) ) {
                    alt25=2;
                }
                else if ( ((LA25_1>=COLON && LA25_1<=COMMA)||LA25_1==RPAREN||LA25_1==RETURNS) ) {
                    alt25=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:125:10: protoargdef
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_protoargdef_in_argdef1006);
                    protoargdef75=protoargdef();

                    state._fsp--;

                    adaptor.addChild(root_0, protoargdef75.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:126:7: ID EQUALS assignExpr
                    {
                    ID76=(Token)match(input,ID,FOLLOW_ID_in_argdef1014);  
                    stream_ID.add(ID76);

                    EQUALS77=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdef1016);  
                    stream_EQUALS.add(EQUALS77);

                    pushFollow(FOLLOW_assignExpr_in_argdef1018);
                    assignExpr78=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr78.getTree());


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
                    // 126:31: -> ^( ARGDEF ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:126:34: ^( ARGDEF ID TYPE assignExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:1: xreturns : RETURNS type -> type ;
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURNS79=null;
        EulangParser.type_return type80 = null;


        CommonTree RETURNS79_tree=null;
        RewriteRuleTokenStream stream_RETURNS=new RewriteRuleTokenStream(adaptor,"token RETURNS");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:9: ( RETURNS type -> type )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:129:11: RETURNS type
            {
            RETURNS79=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_xreturns1043);  
            stream_RETURNS.add(RETURNS79);

            pushFollow(FOLLOW_type_in_xreturns1045);
            type80=type();

            state._fsp--;

            stream_type.add(type80.getTree());


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
            // 129:29: -> type
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:1: type : ( ID -> ^( TYPE ID ) ) ( AMP -> ^( TYPE ^( REF ID ) ) )? ;
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID81=null;
        Token AMP82=null;

        CommonTree ID81_tree=null;
        CommonTree AMP82_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:6: ( ( ID -> ^( TYPE ID ) ) ( AMP -> ^( TYPE ^( REF ID ) ) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:9: ( ID -> ^( TYPE ID ) ) ( AMP -> ^( TYPE ^( REF ID ) ) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:9: ( ID -> ^( TYPE ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:11: ID
            {
            ID81=(Token)match(input,ID,FOLLOW_ID_in_type1070);  
            stream_ID.add(ID81);



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
            // 132:14: -> ^( TYPE ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:132:17: ^( TYPE ID )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:133:7: ( AMP -> ^( TYPE ^( REF ID ) ) )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==AMP) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:133:9: AMP
                    {
                    AMP82=(Token)match(input,AMP,FOLLOW_AMP_in_type1090);  
                    stream_AMP.add(AMP82);



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
                    // 133:13: -> ^( TYPE ^( REF ID ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:133:16: ^( TYPE ^( REF ID ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:133:23: ^( REF ID )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(REF, "REF"), root_2);

                        adaptor.addChild(root_2, stream_ID.nextNode());

                        adaptor.addChild(root_1, root_2);
                        }

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
    // $ANTLR end "type"

    public static class codestmtlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codestmtlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:1: codestmtlist : ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) ;
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI84=null;
        Token SEMI86=null;
        EulangParser.codeStmt_return codeStmt83 = null;

        EulangParser.codeStmt_return codeStmt85 = null;


        CommonTree SEMI84_tree=null;
        CommonTree SEMI86_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:13: ( ( codeStmt ( SEMI codeStmt )* ( SEMI )? )? -> ^( STMTLIST ( codeStmt )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:15: ( codeStmt ( SEMI codeStmt )* ( SEMI )? )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==MINUS||LA29_0==LPAREN||LA29_0==RETURN||(LA29_0>=NUMBER && LA29_0<=SCOPEREF)||(LA29_0>=CHAR_LITERAL && LA29_0<=STRING_LITERAL)) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:16: codeStmt ( SEMI codeStmt )* ( SEMI )?
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist1125);
                    codeStmt83=codeStmt();

                    state._fsp--;

                    stream_codeStmt.add(codeStmt83.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:25: ( SEMI codeStmt )*
                    loop27:
                    do {
                        int alt27=2;
                        int LA27_0 = input.LA(1);

                        if ( (LA27_0==SEMI) ) {
                            int LA27_1 = input.LA(2);

                            if ( (LA27_1==MINUS||LA27_1==LPAREN||LA27_1==RETURN||(LA27_1>=NUMBER && LA27_1<=SCOPEREF)||(LA27_1>=CHAR_LITERAL && LA27_1<=STRING_LITERAL)) ) {
                                alt27=1;
                            }


                        }


                        switch (alt27) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:27: SEMI codeStmt
                    	    {
                    	    SEMI84=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist1129);  
                    	    stream_SEMI.add(SEMI84);

                    	    pushFollow(FOLLOW_codeStmt_in_codestmtlist1131);
                    	    codeStmt85=codeStmt();

                    	    state._fsp--;

                    	    stream_codeStmt.add(codeStmt85.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop27;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:44: ( SEMI )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==SEMI) ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:44: SEMI
                            {
                            SEMI86=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist1136);  
                            stream_SEMI.add(SEMI86);


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
            // 137:53: -> ^( STMTLIST ( codeStmt )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:57: ^( STMTLIST ( codeStmt )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:137:68: ( codeStmt )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:140:1: codeStmt : ( varDecl | assignExpr | returnExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl87 = null;

        EulangParser.assignExpr_return assignExpr88 = null;

        EulangParser.returnExpr_return returnExpr89 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:140:10: ( varDecl | assignExpr | returnExpr )
            int alt30=3;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA30_1 = input.LA(2);

                if ( (LA30_1==COLON||LA30_1==COLON_EQUALS) ) {
                    alt30=1;
                }
                else if ( (LA30_1==EQUALS||(LA30_1>=PLUS && LA30_1<=LPAREN)||LA30_1==RBRACE||(LA30_1>=AMP && LA30_1<=UMOD)||LA30_1==COMPXOR) ) {
                    alt30=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 1, input);

                    throw nvae;
                }
                }
                break;
            case MINUS:
            case LPAREN:
            case NUMBER:
            case SCOPEREF:
            case CHAR_LITERAL:
            case STRING_LITERAL:
                {
                alt30=2;
                }
                break;
            case RETURN:
                {
                alt30=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:140:12: varDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_varDecl_in_codeStmt1167);
                    varDecl87=varDecl();

                    state._fsp--;

                    adaptor.addChild(root_0, varDecl87.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:141:9: assignExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignExpr_in_codeStmt1177);
                    assignExpr88=assignExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, assignExpr88.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:142:9: returnExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_returnExpr_in_codeStmt1187);
                    returnExpr89=returnExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, returnExpr89.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID90=null;
        Token COLON_EQUALS91=null;
        Token ID93=null;
        Token COLON94=null;
        Token EQUALS96=null;
        EulangParser.assignExpr_return assignExpr92 = null;

        EulangParser.type_return type95 = null;

        EulangParser.assignExpr_return assignExpr97 = null;


        CommonTree ID90_tree=null;
        CommonTree COLON_EQUALS91_tree=null;
        CommonTree ID93_tree=null;
        CommonTree COLON94_tree=null;
        CommonTree EQUALS96_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:8: ( ID COLON_EQUALS assignExpr -> ^( DEFINE ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( DEFINE ID type ( assignExpr )* ) )
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==ID) ) {
                int LA32_1 = input.LA(2);

                if ( (LA32_1==COLON_EQUALS) ) {
                    alt32=1;
                }
                else if ( (LA32_1==COLON) ) {
                    alt32=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 32, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:10: ID COLON_EQUALS assignExpr
                    {
                    ID90=(Token)match(input,ID,FOLLOW_ID_in_varDecl1201);  
                    stream_ID.add(ID90);

                    COLON_EQUALS91=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1203);  
                    stream_COLON_EQUALS.add(COLON_EQUALS91);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1205);
                    assignExpr92=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr92.getTree());


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
                    // 145:45: -> ^( DEFINE ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:145:48: ^( DEFINE ID TYPE assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID93=(Token)match(input,ID,FOLLOW_ID_in_varDecl1233);  
                    stream_ID.add(ID93);

                    COLON94=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1235);  
                    stream_COLON.add(COLON94);

                    pushFollow(FOLLOW_type_in_varDecl1237);
                    type95=type();

                    state._fsp--;

                    stream_type.add(type95.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:21: ( EQUALS assignExpr )?
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==EQUALS) ) {
                        alt31=1;
                    }
                    switch (alt31) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:22: EQUALS assignExpr
                            {
                            EQUALS96=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1240);  
                            stream_EQUALS.add(EQUALS96);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1242);
                            assignExpr97=assignExpr();

                            state._fsp--;

                            stream_assignExpr.add(assignExpr97.getTree());

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
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 146:43: -> ^( DEFINE ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:46: ^( DEFINE ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:146:63: ( assignExpr )*
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

    public static class returnExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "returnExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:1: returnExpr : RETURN ( assignExpr )? -> ^( RETURN ( assignExpr )? ) ;
    public final EulangParser.returnExpr_return returnExpr() throws RecognitionException {
        EulangParser.returnExpr_return retval = new EulangParser.returnExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURN98=null;
        EulangParser.assignExpr_return assignExpr99 = null;


        CommonTree RETURN98_tree=null;
        RewriteRuleTokenStream stream_RETURN=new RewriteRuleTokenStream(adaptor,"token RETURN");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:12: ( RETURN ( assignExpr )? -> ^( RETURN ( assignExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:14: RETURN ( assignExpr )?
            {
            RETURN98=(Token)match(input,RETURN,FOLLOW_RETURN_in_returnExpr1271);  
            stream_RETURN.add(RETURN98);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:21: ( assignExpr )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==MINUS||LA33_0==LPAREN||(LA33_0>=NUMBER && LA33_0<=SCOPEREF)||(LA33_0>=CHAR_LITERAL && LA33_0<=STRING_LITERAL)) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:21: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_returnExpr1273);
                    assignExpr99=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr99.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: assignExpr, RETURN
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 149:43: -> ^( RETURN ( assignExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:46: ^( RETURN ( assignExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_RETURN.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:149:55: ( assignExpr )?
                if ( stream_assignExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_assignExpr.nextTree());

                }
                stream_assignExpr.reset();

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
    // $ANTLR end "returnExpr"

    public static class assignExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:1: assignExpr : ( ID EQUALS assignExpr -> ^( ASSIGN ID assignExpr ) | rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID100=null;
        Token EQUALS101=null;
        EulangParser.assignExpr_return assignExpr102 = null;

        EulangParser.rhsExpr_return rhsExpr103 = null;


        CommonTree ID100_tree=null;
        CommonTree EQUALS101_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:12: ( ID EQUALS assignExpr -> ^( ASSIGN ID assignExpr ) | rhsExpr )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==ID) ) {
                int LA34_1 = input.LA(2);

                if ( (LA34_1==EQUALS) ) {
                    alt34=1;
                }
                else if ( (LA34_1==COMMA||(LA34_1>=PLUS && LA34_1<=RPAREN)||LA34_1==RBRACE||(LA34_1>=AMP && LA34_1<=RETURNS)||LA34_1==COMPXOR) ) {
                    alt34=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 34, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA34_0==MINUS||LA34_0==LPAREN||LA34_0==NUMBER||LA34_0==SCOPEREF||(LA34_0>=CHAR_LITERAL && LA34_0<=STRING_LITERAL)) ) {
                alt34=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:14: ID EQUALS assignExpr
                    {
                    ID100=(Token)match(input,ID,FOLLOW_ID_in_assignExpr1314);  
                    stream_ID.add(ID100);

                    EQUALS101=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr1316);  
                    stream_EQUALS.add(EQUALS101);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr1318);
                    assignExpr102=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr102.getTree());


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
                    // 152:42: -> ^( ASSIGN ID assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:152:45: ^( ASSIGN ID assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:153:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_assignExpr1343);
                    rhsExpr103=rhsExpr();

                    state._fsp--;

                    adaptor.addChild(root_0, rhsExpr103.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:156:1: rhsExpr : cond ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.cond_return cond104 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:156:9: ( cond )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:156:13: cond
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_cond_in_rhsExpr1358);
            cond104=cond();

            state._fsp--;

            adaptor.addChild(root_0, cond104.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:1: funcCall : idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN106=null;
        Token RPAREN108=null;
        EulangParser.idOrScopeRef_return idOrScopeRef105 = null;

        EulangParser.arglist_return arglist107 = null;


        CommonTree LPAREN106_tree=null;
        CommonTree RPAREN108_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:10: ( idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:12: idOrScopeRef LPAREN arglist RPAREN
            {
            pushFollow(FOLLOW_idOrScopeRef_in_funcCall1376);
            idOrScopeRef105=idOrScopeRef();

            state._fsp--;

            stream_idOrScopeRef.add(idOrScopeRef105.getTree());
            LPAREN106=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall1378);  
            stream_LPAREN.add(LPAREN106);

            pushFollow(FOLLOW_arglist_in_funcCall1380);
            arglist107=arglist();

            state._fsp--;

            stream_arglist.add(arglist107.getTree());
            RPAREN108=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall1382);  
            stream_RPAREN.add(RPAREN108);



            // AST REWRITE
            // elements: arglist, idOrScopeRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 159:49: -> ^( CALL idOrScopeRef arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:159:56: ^( CALL idOrScopeRef arglist )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CALL, "CALL"), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA110=null;
        Token COMMA112=null;
        EulangParser.arg_return arg109 = null;

        EulangParser.arg_return arg111 = null;


        CommonTree COMMA110_tree=null;
        CommonTree COMMA112_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==MINUS||LA37_0==LPAREN||(LA37_0>=NUMBER && LA37_0<=SCOPEREF)||(LA37_0>=CHAR_LITERAL && LA37_0<=STRING_LITERAL)) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist1413);
                    arg109=arg();

                    state._fsp--;

                    stream_arg.add(arg109.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:15: ( COMMA arg )*
                    loop35:
                    do {
                        int alt35=2;
                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==COMMA) ) {
                            int LA35_1 = input.LA(2);

                            if ( (LA35_1==MINUS||LA35_1==LPAREN||(LA35_1>=NUMBER && LA35_1<=SCOPEREF)||(LA35_1>=CHAR_LITERAL && LA35_1<=STRING_LITERAL)) ) {
                                alt35=1;
                            }


                        }


                        switch (alt35) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:17: COMMA arg
                    	    {
                    	    COMMA110=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist1417);  
                    	    stream_COMMA.add(COMMA110);

                    	    pushFollow(FOLLOW_arg_in_arglist1419);
                    	    arg111=arg();

                    	    state._fsp--;

                    	    stream_arg.add(arg111.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:29: ( COMMA )?
                    int alt36=2;
                    int LA36_0 = input.LA(1);

                    if ( (LA36_0==COMMA) ) {
                        alt36=1;
                    }
                    switch (alt36) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:29: COMMA
                            {
                            COMMA112=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist1423);  
                            stream_COMMA.add(COMMA112);


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
            // 163:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:163:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:1: arg : assignExpr -> ^( EXPR assignExpr ) ;
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr113 = null;


        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:4: ( assignExpr -> ^( EXPR assignExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:7: assignExpr
            {
            pushFollow(FOLLOW_assignExpr_in_arg1472);
            assignExpr113=assignExpr();

            state._fsp--;

            stream_assignExpr.add(assignExpr113.getTree());


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
            // 166:37: -> ^( EXPR assignExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:166:40: ^( EXPR assignExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:173:1: cond : (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION114=null;
        Token COLON115=null;
        EulangParser.logcond_return l = null;

        EulangParser.cond_return t = null;

        EulangParser.cond_return f = null;


        CommonTree QUESTION114_tree=null;
        CommonTree COLON115_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logcond=new RewriteRuleSubtreeStream(adaptor,"rule logcond");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:173:5: ( (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:173:10: (l= logcond -> $l) ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:173:10: (l= logcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:173:12: l= logcond
            {
            pushFollow(FOLLOW_logcond_in_cond1522);
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
            // 173:23: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:174:7: ( QUESTION t= cond COLON f= cond -> ^( COND $l $t $f) )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==QUESTION) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:174:9: QUESTION t= cond COLON f= cond
            	    {
            	    QUESTION114=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond1540);  
            	    stream_QUESTION.add(QUESTION114);

            	    pushFollow(FOLLOW_cond_in_cond1544);
            	    t=cond();

            	    state._fsp--;

            	    stream_cond.add(t.getTree());
            	    COLON115=(Token)match(input,COLON,FOLLOW_COLON_in_cond1546);  
            	    stream_COLON.add(COLON115);

            	    pushFollow(FOLLOW_cond_in_cond1550);
            	    f=cond();

            	    state._fsp--;

            	    stream_cond.add(f.getTree());


            	    // AST REWRITE
            	    // elements: l, f, t
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
            	    // 174:38: -> ^( COND $l $t $f)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:174:41: ^( COND $l $t $f)
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
            	    break loop38;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:178:1: logcond : (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )* ;
    public final EulangParser.logcond_return logcond() throws RecognitionException {
        EulangParser.logcond_return retval = new EulangParser.logcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPAND116=null;
        Token COMPOR117=null;
        Token COMPXOR118=null;
        EulangParser.binlogcond_return l = null;

        EulangParser.binlogcond_return r = null;


        CommonTree COMPAND116_tree=null;
        CommonTree COMPOR117_tree=null;
        CommonTree COMPXOR118_tree=null;
        RewriteRuleTokenStream stream_COMPAND=new RewriteRuleTokenStream(adaptor,"token COMPAND");
        RewriteRuleTokenStream stream_COMPXOR=new RewriteRuleTokenStream(adaptor,"token COMPXOR");
        RewriteRuleTokenStream stream_COMPOR=new RewriteRuleTokenStream(adaptor,"token COMPOR");
        RewriteRuleSubtreeStream stream_binlogcond=new RewriteRuleSubtreeStream(adaptor,"rule binlogcond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:178:8: ( (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:178:10: (l= binlogcond -> $l) ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:178:10: (l= binlogcond -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:178:12: l= binlogcond
            {
            pushFollow(FOLLOW_binlogcond_in_logcond1588);
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
            // 178:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:7: ( COMPAND r= binlogcond -> ^( COMPAND $l $r) | COMPOR r= binlogcond -> ^( COMPOR $l $r) | COMPXOR r= binlogcond -> ^( COMPXOR $l $r) )*
            loop39:
            do {
                int alt39=4;
                switch ( input.LA(1) ) {
                case COMPAND:
                    {
                    alt39=1;
                    }
                    break;
                case COMPOR:
                    {
                    alt39=2;
                    }
                    break;
                case COMPXOR:
                    {
                    alt39=3;
                    }
                    break;

                }

                switch (alt39) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:9: COMPAND r= binlogcond
            	    {
            	    COMPAND116=(Token)match(input,COMPAND,FOLLOW_COMPAND_in_logcond1615);  
            	    stream_COMPAND.add(COMPAND116);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1619);
            	    r=binlogcond();

            	    state._fsp--;

            	    stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, COMPAND, r
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
            	    // 179:30: -> ^( COMPAND $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:179:33: ^( COMPAND $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:180:9: COMPOR r= binlogcond
            	    {
            	    COMPOR117=(Token)match(input,COMPOR,FOLLOW_COMPOR_in_logcond1641);  
            	    stream_COMPOR.add(COMPOR117);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1645);
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
            	    // 180:29: -> ^( COMPOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:180:32: ^( COMPOR $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:181:9: COMPXOR r= binlogcond
            	    {
            	    COMPXOR118=(Token)match(input,COMPXOR,FOLLOW_COMPXOR_in_logcond1667);  
            	    stream_COMPXOR.add(COMPXOR118);

            	    pushFollow(FOLLOW_binlogcond_in_logcond1671);
            	    r=binlogcond();

            	    state._fsp--;

            	    stream_binlogcond.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, COMPXOR, r
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
            	    // 181:31: -> ^( COMPXOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:181:34: ^( COMPXOR $l $r)
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
            	    break loop39;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:185:1: binlogcond : (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )* ;
    public final EulangParser.binlogcond_return binlogcond() throws RecognitionException {
        EulangParser.binlogcond_return retval = new EulangParser.binlogcond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP119=null;
        Token BAR120=null;
        Token CARET121=null;
        EulangParser.compeq_return l = null;

        EulangParser.compeq_return r = null;


        CommonTree AMP119_tree=null;
        CommonTree BAR120_tree=null;
        CommonTree CARET121_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_compeq=new RewriteRuleSubtreeStream(adaptor,"rule compeq");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:185:11: ( (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:185:13: (l= compeq -> $l) ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:185:13: (l= compeq -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:185:15: l= compeq
            {
            pushFollow(FOLLOW_compeq_in_binlogcond1705);
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
            // 185:29: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:186:7: ( AMP r= compeq -> ^( BITAND $l $r) | BAR r= compeq -> ^( BITOR $l $r) | CARET r= compeq -> ^( BITXOR $l $r) )*
            loop40:
            do {
                int alt40=4;
                switch ( input.LA(1) ) {
                case AMP:
                    {
                    alt40=1;
                    }
                    break;
                case BAR:
                    {
                    alt40=2;
                    }
                    break;
                case CARET:
                    {
                    alt40=3;
                    }
                    break;

                }

                switch (alt40) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:186:9: AMP r= compeq
            	    {
            	    AMP119=(Token)match(input,AMP,FOLLOW_AMP_in_binlogcond1734);  
            	    stream_AMP.add(AMP119);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1738);
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
            	    // 186:23: -> ^( BITAND $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:186:26: ^( BITAND $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:187:9: BAR r= compeq
            	    {
            	    BAR120=(Token)match(input,BAR,FOLLOW_BAR_in_binlogcond1761);  
            	    stream_BAR.add(BAR120);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1765);
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
            	    // 187:23: -> ^( BITOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:187:26: ^( BITOR $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:188:9: CARET r= compeq
            	    {
            	    CARET121=(Token)match(input,CARET,FOLLOW_CARET_in_binlogcond1788);  
            	    stream_CARET.add(CARET121);

            	    pushFollow(FOLLOW_compeq_in_binlogcond1792);
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
            	    // 188:25: -> ^( BITXOR $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:188:28: ^( BITXOR $l $r)
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
            	    break loop40;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:192:1: compeq : (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )* ;
    public final EulangParser.compeq_return compeq() throws RecognitionException {
        EulangParser.compeq_return retval = new EulangParser.compeq_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ122=null;
        Token COMPNE123=null;
        EulangParser.comp_return l = null;

        EulangParser.comp_return r = null;


        CommonTree COMPEQ122_tree=null;
        CommonTree COMPNE123_tree=null;
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:192:7: ( (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:192:11: (l= comp -> $l) ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:192:11: (l= comp -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:192:13: l= comp
            {
            pushFollow(FOLLOW_comp_in_compeq1828);
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
            // 192:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:7: ( COMPEQ r= comp -> ^( COMPEQ $l $r) | COMPNE r= comp -> ^( COMPNE $l $r) )*
            loop41:
            do {
                int alt41=3;
                int LA41_0 = input.LA(1);

                if ( (LA41_0==COMPEQ) ) {
                    alt41=1;
                }
                else if ( (LA41_0==COMPNE) ) {
                    alt41=2;
                }


                switch (alt41) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:9: COMPEQ r= comp
            	    {
            	    COMPEQ122=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_compeq1862);  
            	    stream_COMPEQ.add(COMPEQ122);

            	    pushFollow(FOLLOW_comp_in_compeq1866);
            	    r=comp();

            	    state._fsp--;

            	    stream_comp.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, COMPEQ
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
            	    // 193:23: -> ^( COMPEQ $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:193:26: ^( COMPEQ $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:194:9: COMPNE r= comp
            	    {
            	    COMPNE123=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_compeq1888);  
            	    stream_COMPNE.add(COMPNE123);

            	    pushFollow(FOLLOW_comp_in_compeq1892);
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
            	    // 194:23: -> ^( COMPNE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:194:26: ^( COMPNE $l $r)
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
            	    break loop41;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:1: comp : (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPLE124=null;
        Token COMPGE125=null;
        Token LESS126=null;
        Token GREATER127=null;
        EulangParser.shift_return l = null;

        EulangParser.shift_return r = null;


        CommonTree COMPLE124_tree=null;
        CommonTree COMPGE125_tree=null;
        CommonTree LESS126_tree=null;
        CommonTree GREATER127_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:5: ( (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:8: (l= shift -> $l) ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:8: (l= shift -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:198:10: l= shift
            {
            pushFollow(FOLLOW_shift_in_comp1926);
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
            // 198:28: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:199:7: ( COMPLE r= shift -> ^( COMPLE $l $r) | COMPGE r= shift -> ^( COMPGE $l $r) | LESS r= shift -> ^( LESS $l $r) | GREATER r= shift -> ^( GREATER $l $r) )*
            loop42:
            do {
                int alt42=5;
                switch ( input.LA(1) ) {
                case COMPLE:
                    {
                    alt42=1;
                    }
                    break;
                case COMPGE:
                    {
                    alt42=2;
                    }
                    break;
                case LESS:
                    {
                    alt42=3;
                    }
                    break;
                case GREATER:
                    {
                    alt42=4;
                    }
                    break;

                }

                switch (alt42) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:199:9: COMPLE r= shift
            	    {
            	    COMPLE124=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp1953);  
            	    stream_COMPLE.add(COMPLE124);

            	    pushFollow(FOLLOW_shift_in_comp1957);
            	    r=shift();

            	    state._fsp--;

            	    stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, COMPLE
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
            	    // 199:27: -> ^( COMPLE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:199:30: ^( COMPLE $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:200:9: COMPGE r= shift
            	    {
            	    COMPGE125=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp1982);  
            	    stream_COMPGE.add(COMPGE125);

            	    pushFollow(FOLLOW_shift_in_comp1986);
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
            	    // 200:27: -> ^( COMPGE $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:200:30: ^( COMPGE $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:201:9: LESS r= shift
            	    {
            	    LESS126=(Token)match(input,LESS,FOLLOW_LESS_in_comp2011);  
            	    stream_LESS.add(LESS126);

            	    pushFollow(FOLLOW_shift_in_comp2015);
            	    r=shift();

            	    state._fsp--;

            	    stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, LESS, l
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
            	    // 201:26: -> ^( LESS $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:201:29: ^( LESS $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:202:9: GREATER r= shift
            	    {
            	    GREATER127=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp2041);  
            	    stream_GREATER.add(GREATER127);

            	    pushFollow(FOLLOW_shift_in_comp2045);
            	    r=shift();

            	    state._fsp--;

            	    stream_shift.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, l, GREATER
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
            	    // 202:28: -> ^( GREATER $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:202:31: ^( GREATER $l $r)
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
            	    break loop42;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:1: shift : (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT128=null;
        Token RSHIFT129=null;
        Token URSHIFT130=null;
        EulangParser.factor_return l = null;

        EulangParser.factor_return r = null;


        CommonTree LSHIFT128_tree=null;
        CommonTree RSHIFT129_tree=null;
        CommonTree URSHIFT130_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:6: ( (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:9: (l= factor -> $l) ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:9: (l= factor -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:206:11: l= factor
            {
            pushFollow(FOLLOW_factor_in_shift2097);
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
            // 206:27: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:207:7: ( LSHIFT r= factor -> ^( LSHIFT $l $r) | RSHIFT r= factor -> ^( RSHIFT $l $r) | URSHIFT r= factor -> ^( URSHIFT $l $r) )*
            loop43:
            do {
                int alt43=4;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt43=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt43=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt43=3;
                    }
                    break;

                }

                switch (alt43) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:207:9: LSHIFT r= factor
            	    {
            	    LSHIFT128=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift2130);  
            	    stream_LSHIFT.add(LSHIFT128);

            	    pushFollow(FOLLOW_factor_in_shift2134);
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
            	    // 207:27: -> ^( LSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:207:30: ^( LSHIFT $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:208:9: RSHIFT r= factor
            	    {
            	    RSHIFT129=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift2159);  
            	    stream_RSHIFT.add(RSHIFT129);

            	    pushFollow(FOLLOW_factor_in_shift2163);
            	    r=factor();

            	    state._fsp--;

            	    stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: l, RSHIFT, r
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
            	    // 208:27: -> ^( RSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:208:30: ^( RSHIFT $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:209:9: URSHIFT r= factor
            	    {
            	    URSHIFT130=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift2187);  
            	    stream_URSHIFT.add(URSHIFT130);

            	    pushFollow(FOLLOW_factor_in_shift2191);
            	    r=factor();

            	    state._fsp--;

            	    stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, URSHIFT, l
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
            	    // 209:28: -> ^( URSHIFT $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:209:31: ^( URSHIFT $l $r)
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
            	    break loop43;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:212:1: factor : (l= multExpr -> $l) ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS131=null;
        Token MINUS132=null;
        EulangParser.multExpr_return l = null;

        EulangParser.multExpr_return r = null;


        CommonTree PLUS131_tree=null;
        CommonTree MINUS132_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_multExpr=new RewriteRuleSubtreeStream(adaptor,"rule multExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:213:5: ( (l= multExpr -> $l) ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:213:7: (l= multExpr -> $l) ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:213:7: (l= multExpr -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:213:9: l= multExpr
            {
            pushFollow(FOLLOW_multExpr_in_factor2234);
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
            // 213:33: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:214:9: ( PLUS r= multExpr -> ^( ADD $l $r) | MINUS r= multExpr -> ^( SUB $l $r) )*
            loop44:
            do {
                int alt44=3;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==MINUS) ) {
                    int LA44_2 = input.LA(2);

                    if ( (LA44_2==MINUS||LA44_2==LPAREN||(LA44_2>=NUMBER && LA44_2<=SCOPEREF)||(LA44_2>=CHAR_LITERAL && LA44_2<=STRING_LITERAL)) ) {
                        alt44=2;
                    }


                }
                else if ( (LA44_0==PLUS) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:214:13: PLUS r= multExpr
            	    {
            	    PLUS131=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor2268);  
            	    stream_PLUS.add(PLUS131);

            	    pushFollow(FOLLOW_multExpr_in_factor2272);
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
            	    // 214:37: -> ^( ADD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:214:40: ^( ADD $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:215:13: MINUS r= multExpr
            	    {
            	    MINUS132=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor2306);  
            	    stream_MINUS.add(MINUS132);

            	    pushFollow(FOLLOW_multExpr_in_factor2310);
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
            	    // 215:37: -> ^( SUB $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:215:40: ^( SUB $l $r)
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
            	    break loop44;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:1: multExpr : (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )* ;
    public final EulangParser.multExpr_return multExpr() throws RecognitionException {
        EulangParser.multExpr_return retval = new EulangParser.multExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR133=null;
        Token SLASH134=null;
        Token BACKSLASH135=null;
        Token PERCENT136=null;
        Token UMOD137=null;
        EulangParser.atom_return l = null;

        EulangParser.atom_return r = null;


        CommonTree STAR133_tree=null;
        CommonTree SLASH134_tree=null;
        CommonTree BACKSLASH135_tree=null;
        CommonTree PERCENT136_tree=null;
        CommonTree UMOD137_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:10: ( (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:12: (l= atom -> $l) ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:12: (l= atom -> $l)
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:219:14: l= atom
            {
            pushFollow(FOLLOW_atom_in_multExpr2357);
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
            // 219:38: -> $l
            {
                adaptor.addChild(root_0, stream_l.nextTree());

            }

            retval.tree = root_0;
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:220:9: ( STAR r= atom -> ^( MUL $l $r) | SLASH r= atom -> ^( DIV $l $r) | BACKSLASH r= atom -> ^( UDIV $l $r) | PERCENT r= atom -> ^( MOD $l $r) | UMOD r= atom -> ^( UMOD $l $r) )*
            loop45:
            do {
                int alt45=6;
                switch ( input.LA(1) ) {
                case STAR:
                    {
                    alt45=1;
                    }
                    break;
                case SLASH:
                    {
                    alt45=2;
                    }
                    break;
                case BACKSLASH:
                    {
                    alt45=3;
                    }
                    break;
                case PERCENT:
                    {
                    alt45=4;
                    }
                    break;
                case UMOD:
                    {
                    alt45=5;
                    }
                    break;

                }

                switch (alt45) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:220:11: STAR r= atom
            	    {
            	    STAR133=(Token)match(input,STAR,FOLLOW_STAR_in_multExpr2393);  
            	    stream_STAR.add(STAR133);

            	    pushFollow(FOLLOW_atom_in_multExpr2397);
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
            	    // 220:35: -> ^( MUL $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:220:38: ^( MUL $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:221:11: SLASH r= atom
            	    {
            	    SLASH134=(Token)match(input,SLASH,FOLLOW_SLASH_in_multExpr2434);  
            	    stream_SLASH.add(SLASH134);

            	    pushFollow(FOLLOW_atom_in_multExpr2438);
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
            	    // 221:35: -> ^( DIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:221:38: ^( DIV $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:222:11: BACKSLASH r= atom
            	    {
            	    BACKSLASH135=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_multExpr2473);  
            	    stream_BACKSLASH.add(BACKSLASH135);

            	    pushFollow(FOLLOW_atom_in_multExpr2477);
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
            	    // 222:39: -> ^( UDIV $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:222:42: ^( UDIV $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:223:11: PERCENT r= atom
            	    {
            	    PERCENT136=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_multExpr2512);  
            	    stream_PERCENT.add(PERCENT136);

            	    pushFollow(FOLLOW_atom_in_multExpr2516);
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
            	    // 223:37: -> ^( MOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:223:40: ^( MOD $l $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:224:11: UMOD r= atom
            	    {
            	    UMOD137=(Token)match(input,UMOD,FOLLOW_UMOD_in_multExpr2551);  
            	    stream_UMOD.add(UMOD137);

            	    pushFollow(FOLLOW_atom_in_multExpr2555);
            	    r=atom();

            	    state._fsp--;

            	    stream_atom.add(r.getTree());


            	    // AST REWRITE
            	    // elements: UMOD, r, l
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
            	    // 224:34: -> ^( UMOD $l $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:224:37: ^( UMOD $l $r)
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
            	    break loop45;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:228:1: atom options {k=2; } : ( MINUS NUMBER -> | NUMBER -> NUMBER | CHAR_LITERAL | STRING_LITERAL | funcCall -> funcCall | ID -> ^( IDREF ID ) | SCOPEREF -> ^( IDREF SCOPEREF ) | LPAREN assignExpr RPAREN -> assignExpr );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS138=null;
        Token NUMBER139=null;
        Token NUMBER140=null;
        Token CHAR_LITERAL141=null;
        Token STRING_LITERAL142=null;
        Token ID144=null;
        Token SCOPEREF145=null;
        Token LPAREN146=null;
        Token RPAREN148=null;
        EulangParser.funcCall_return funcCall143 = null;

        EulangParser.assignExpr_return assignExpr147 = null;


        CommonTree MINUS138_tree=null;
        CommonTree NUMBER139_tree=null;
        CommonTree NUMBER140_tree=null;
        CommonTree CHAR_LITERAL141_tree=null;
        CommonTree STRING_LITERAL142_tree=null;
        CommonTree ID144_tree=null;
        CommonTree SCOPEREF145_tree=null;
        CommonTree LPAREN146_tree=null;
        CommonTree RPAREN148_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_SCOPEREF=new RewriteRuleTokenStream(adaptor,"token SCOPEREF");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_funcCall=new RewriteRuleSubtreeStream(adaptor,"rule funcCall");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:228:23: ( MINUS NUMBER -> | NUMBER -> NUMBER | CHAR_LITERAL | STRING_LITERAL | funcCall -> funcCall | ID -> ^( IDREF ID ) | SCOPEREF -> ^( IDREF SCOPEREF ) | LPAREN assignExpr RPAREN -> assignExpr )
            int alt46=8;
            alt46 = dfa46.predict(input);
            switch (alt46) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:229:5: MINUS NUMBER
                    {
                    MINUS138=(Token)match(input,MINUS,FOLLOW_MINUS_in_atom2640);  
                    stream_MINUS.add(MINUS138);

                    NUMBER139=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom2642);  
                    stream_NUMBER.add(NUMBER139);



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
                    // 229:43: ->
                    {
                        adaptor.addChild(root_0, new CommonTree(new CommonToken(NUMBER, "-" + (NUMBER139!=null?NUMBER139.getText():null))));

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:230:9: NUMBER
                    {
                    NUMBER140=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom2681);  
                    stream_NUMBER.add(NUMBER140);



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
                    // 230:41: -> NUMBER
                    {
                        adaptor.addChild(root_0, stream_NUMBER.nextNode());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:231:9: CHAR_LITERAL
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    CHAR_LITERAL141=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom2720); 
                    CHAR_LITERAL141_tree = (CommonTree)adaptor.create(CHAR_LITERAL141);
                    adaptor.addChild(root_0, CHAR_LITERAL141_tree);


                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:232:9: STRING_LITERAL
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    STRING_LITERAL142=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom2730); 
                    STRING_LITERAL142_tree = (CommonTree)adaptor.create(STRING_LITERAL142);
                    adaptor.addChild(root_0, STRING_LITERAL142_tree);


                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:233:9: funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom2740);
                    funcCall143=funcCall();

                    state._fsp--;

                    stream_funcCall.add(funcCall143.getTree());


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
                    // 233:53: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:234:9: ID
                    {
                    ID144=(Token)match(input,ID,FOLLOW_ID_in_atom2789);  
                    stream_ID.add(ID144);



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
                    // 234:43: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:234:46: ^( IDREF ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:235:9: SCOPEREF
                    {
                    SCOPEREF145=(Token)match(input,SCOPEREF,FOLLOW_SCOPEREF_in_atom2838);  
                    stream_SCOPEREF.add(SCOPEREF145);



                    // AST REWRITE
                    // elements: SCOPEREF
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 235:49: -> ^( IDREF SCOPEREF )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:235:52: ^( IDREF SCOPEREF )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, stream_SCOPEREF.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:236:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN146=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom2887);  
                    stream_LPAREN.add(LPAREN146);

                    pushFollow(FOLLOW_assignExpr_in_atom2889);
                    assignExpr147=assignExpr();

                    state._fsp--;

                    stream_assignExpr.add(assignExpr147.getTree());
                    RPAREN148=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom2891);  
                    stream_RPAREN.add(RPAREN148);



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
                    // 236:48: -> assignExpr
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

    public static class idOrScopeRef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idOrScopeRef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:1: idOrScopeRef : ( ID -> ^( IDREF ID ) | SCOPEREF -> ^( IDREF SCOPEREF ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID149=null;
        Token SCOPEREF150=null;

        CommonTree ID149_tree=null;
        CommonTree SCOPEREF150_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_SCOPEREF=new RewriteRuleTokenStream(adaptor,"token SCOPEREF");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:14: ( ID -> ^( IDREF ID ) | SCOPEREF -> ^( IDREF SCOPEREF ) )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==ID) ) {
                alt47=1;
            }
            else if ( (LA47_0==SCOPEREF) ) {
                alt47=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:16: ID
                    {
                    ID149=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef2923);  
                    stream_ID.add(ID149);



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
                    // 239:19: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:239:22: ^( IDREF ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:240:9: SCOPEREF
                    {
                    SCOPEREF150=(Token)match(input,SCOPEREF,FOLLOW_SCOPEREF_in_idOrScopeRef2943);  
                    stream_SCOPEREF.add(SCOPEREF150);



                    // AST REWRITE
                    // elements: SCOPEREF
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 240:18: -> ^( IDREF SCOPEREF )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/EulangParser.g:240:21: ^( IDREF SCOPEREF )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, stream_SCOPEREF.nextNode());

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
    // $ANTLR end "idOrScopeRef"

    // Delegated rules


    protected DFA46 dfa46 = new DFA46(this);
    static final String DFA46_eotS =
        "\124\uffff";
    static final String DFA46_eofS =
        "\5\uffff\1\10\1\56\115\uffff";
    static final String DFA46_minS =
        "\1\14\4\uffff\2\6\115\uffff";
    static final String DFA46_maxS =
        "\1\70\4\uffff\2\131\115\uffff";
    static final String DFA46_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\2\uffff\1\10\1\6\36\uffff\1\5\6\uffff\1"+
        "\7\36\uffff\1\5\6\uffff";
    static final String DFA46_specialS =
        "\124\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\1\2\uffff\1\7\42\uffff\1\2\1\5\1\6\2\uffff\1\3\1\4",
            "",
            "",
            "",
            "",
            "\2\10\3\uffff\4\10\1\47\3\10\1\uffff\1\10\4\uffff\24\10\5\uffff"+
            "\3\10\2\uffff\2\10\40\uffff\1\10",
            "\2\56\3\uffff\4\56\1\115\3\56\1\uffff\1\56\4\uffff\24\56\5"+
            "\uffff\3\56\2\uffff\2\56\40\uffff\1\56",
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
            return "228:1: atom options {k=2; } : ( MINUS NUMBER -> | NUMBER -> NUMBER | CHAR_LITERAL | STRING_LITERAL | funcCall -> funcCall | ID -> ^( IDREF ID ) | SCOPEREF -> ^( IDREF SCOPEREF ) | LPAREN assignExpr RPAREN -> assignExpr );";
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog207 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts238 = new BitSet(new long[]{0x019C000000029002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat272 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat274 = new BitSet(new long[]{0x019C0000000A9010L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat276 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat301 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelstat303 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat305 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat327 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstat364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_toplevelvalue385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_toplevelvalue393 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue416 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector429 = new BitSet(new long[]{0x0000800000100010L});
    public static final BitSet FOLLOW_selectors_in_selector431 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors459 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_selectors463 = new BitSet(new long[]{0x0000800000000010L});
    public static final BitSet FOLLOW_selectoritem_in_selectors465 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_selectors470 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listCompr_in_selectoritem495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_selectoritem499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope511 = new BitSet(new long[]{0x019C000000029000L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope513 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr542 = new BitSet(new long[]{0x0000800000000040L});
    public static final BitSet FOLLOW_COLON_in_listCompr545 = new BitSet(new long[]{0x0000000000008010L});
    public static final BitSet FOLLOW_listiterable_in_listCompr547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn579 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_idlist_in_forIn581 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_IN_in_forIn583 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_list_in_forIn585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist610 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_idlist613 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_ID_in_idlist615 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_code_in_listiterable644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_listiterable648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list663 = new BitSet(new long[]{0x019C0000001A9010L});
    public static final BitSet FOLLOW_listitems_in_list665 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_RBRACKET_in_list667 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems697 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_listitems701 = new BitSet(new long[]{0x019C0000000A9010L});
    public static final BitSet FOLLOW_listitem_in_listitems703 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_listitems708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto754 = new BitSet(new long[]{0x0008100000010000L});
    public static final BitSet FOLLOW_protoargdefs_in_proto756 = new BitSet(new long[]{0x0000100000010000L});
    public static final BitSet FOLLOW_xreturns_in_proto758 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_proto761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protoargdef_in_protoargdefs804 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_protoargdefs808 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_protoargdef_in_protoargdefs810 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_protoargdefs814 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_protoargdef859 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_COLON_in_protoargdef862 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_type_in_protoargdef864 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_EQUALS_in_protoargdef867 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_rhsExpr_in_protoargdef869 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_LPAREN_in_code906 = new BitSet(new long[]{0x0008100000010000L});
    public static final BitSet FOLLOW_argdefs_in_code908 = new BitSet(new long[]{0x0000100000010000L});
    public static final BitSet FOLLOW_xreturns_in_code910 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_code913 = new BitSet(new long[]{0x019C400000049000L});
    public static final BitSet FOLLOW_codestmtlist_in_code915 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACE_in_code917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdef_in_argdefs951 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_argdefs955 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_argdef_in_argdefs957 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_argdefs961 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_protoargdef_in_argdef1006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdef1014 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_argdef1016 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_assignExpr_in_argdef1018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_xreturns1043 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_type_in_xreturns1045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_type1070 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_AMP_in_type1090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1125 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist1129 = new BitSet(new long[]{0x019C400000009000L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1131 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist1136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmt1167 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_codeStmt1177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_returnExpr_in_codeStmt1187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1201 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1203 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1233 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_varDecl1235 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_type_in_varDecl1237 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1240 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_returnExpr1271 = new BitSet(new long[]{0x019C000000009002L});
    public static final BitSet FOLLOW_assignExpr_in_returnExpr1273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignExpr1314 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr1316 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr1318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr1343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_rhsExpr1358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_funcCall1376 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall1378 = new BitSet(new long[]{0x019C000000019000L});
    public static final BitSet FOLLOW_arglist_in_funcCall1380 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall1382 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist1413 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_arglist1417 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_arg_in_arglist1419 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_COMMA_in_arglist1423 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg1472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logcond_in_cond1522 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond1540 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_cond_in_cond1544 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_cond1546 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_cond_in_cond1550 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1588 = new BitSet(new long[]{0x00000000C0000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_COMPAND_in_logcond1615 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1619 = new BitSet(new long[]{0x00000000C0000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_COMPOR_in_logcond1641 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1645 = new BitSet(new long[]{0x00000000C0000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_COMPXOR_in_logcond1667 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_binlogcond_in_logcond1671 = new BitSet(new long[]{0x00000000C0000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1705 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_AMP_in_binlogcond1734 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1738 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_BAR_in_binlogcond1761 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1765 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_CARET_in_binlogcond1788 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_compeq_in_binlogcond1792 = new BitSet(new long[]{0x000000000E000002L});
    public static final BitSet FOLLOW_comp_in_compeq1828 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_COMPEQ_in_compeq1862 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_comp_in_compeq1866 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_COMPNE_in_compeq1888 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_comp_in_compeq1892 = new BitSet(new long[]{0x0000000300000002L});
    public static final BitSet FOLLOW_shift_in_comp1926 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_COMPLE_in_comp1953 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_shift_in_comp1957 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_COMPGE_in_comp1982 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_shift_in_comp1986 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_LESS_in_comp2011 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_shift_in_comp2015 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_GREATER_in_comp2041 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_shift_in_comp2045 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_factor_in_shift2097 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_LSHIFT_in_shift2130 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_factor_in_shift2134 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_RSHIFT_in_shift2159 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_factor_in_shift2163 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_URSHIFT_in_shift2187 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_factor_in_shift2191 = new BitSet(new long[]{0x000001C000000002L});
    public static final BitSet FOLLOW_multExpr_in_factor2234 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_PLUS_in_factor2268 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_multExpr_in_factor2272 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_MINUS_in_factor2306 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_multExpr_in_factor2310 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_atom_in_multExpr2357 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_STAR_in_multExpr2393 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_atom_in_multExpr2397 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_SLASH_in_multExpr2434 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_atom_in_multExpr2438 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_BACKSLASH_in_multExpr2473 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_atom_in_multExpr2477 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_PERCENT_in_multExpr2512 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_atom_in_multExpr2516 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_UMOD_in_multExpr2551 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_atom_in_multExpr2555 = new BitSet(new long[]{0x00000E0000006002L});
    public static final BitSet FOLLOW_MINUS_in_atom2640 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_NUMBER_in_atom2642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom2681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom2720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom2730 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom2740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_atom2789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCOPEREF_in_atom2838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom2887 = new BitSet(new long[]{0x019C000000009000L});
    public static final BitSet FOLLOW_assignExpr_in_atom2889 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RPAREN_in_atom2891 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef2923 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCOPEREF_in_idOrScopeRef2943 = new BitSet(new long[]{0x0000000000000002L});

}