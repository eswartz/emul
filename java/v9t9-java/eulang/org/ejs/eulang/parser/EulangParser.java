// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-04-13 19:36:23

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCOPE", "LIST_COMPREHENSION", "CODE", "MACRO", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "ALLOC", "ASSIGN", "DEFINE", "EXPR", "LIST", "TYPE", "STMTEXPR", "CONDLIST", "CONDTEST", "CALL", "INLINE", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NOT", "NEG", "INV", "LIT", "IDREF", "IDLIST", "LABEL", "GOTO", "BLOCK", "TUPLE", "LABELSTMT", "ID", "EQUALS", "SEMI", "COLON", "COLON_EQUALS", "LBRACKET", "RBRACKET", "COMMA", "LBRACE", "RBRACE", "FOR", "IN", "LPAREN", "RPAREN", "RETURNS", "NULL", "QUESTION", "AMP", "AT", "SELECT", "BAR_BAR", "THEN", "ELSE", "COMPOR", "COMPAND", "COMPEQ", "COMPNE", "COMPLE", "COMPGE", "LESS", "GREATER", "BAR", "CARET", "LSHIFT", "RSHIFT", "URSHIFT", "PLUS", "MINUS", "STAR", "SLASH", "BACKSLASH", "PERCENT", "UMOD", "EXCL", "TILDE", "NUMBER", "FALSE", "TRUE", "CHAR_LITERAL", "STRING_LITERAL", "INVOKE", "RECURSE", "PERIOD", "COLONS", "LBRACE_LPAREN", "LBRACE_STAR", "LBRACE_STAR_LPAREN", "COLON_COLON_EQUALS", "HASH", "ARROW", "DATA", "IDSUFFIX", "LETTERLIKE", "DIGIT", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT"
    };
    public static final int CONDTEST=21;
    public static final int STAR=83;
    public static final int RECURSE=96;
    public static final int MOD=33;
    public static final int ARGLIST=10;
    public static final int EQUALS=46;
    public static final int EXCL=88;
    public static final int NOT=34;
    public static final int EOF=-1;
    public static final int TYPE=18;
    public static final int CODE=6;
    public static final int LBRACKET=50;
    public static final int TUPLE=43;
    public static final int RPAREN=58;
    public static final int STRING_LITERAL=94;
    public static final int GREATER=75;
    public static final int COMPLE=72;
    public static final int CARET=77;
    public static final int LESS=74;
    public static final int LBRACE_STAR_LPAREN=101;
    public static final int COMPAND=69;
    public static final int GOTO=41;
    public static final int SELECT=64;
    public static final int LABELSTMT=44;
    public static final int RBRACE=54;
    public static final int STMTEXPR=19;
    public static final int PERIOD=97;
    public static final int UMOD=87;
    public static final int INV=36;
    public static final int LSHIFT=78;
    public static final int NULL=60;
    public static final int ELSE=67;
    public static final int LBRACE_LPAREN=99;
    public static final int LIT=37;
    public static final int UDIV=32;
    public static final int NUMBER=90;
    public static final int LIST=17;
    public static final int MUL=30;
    public static final int ARGDEF=11;
    public static final int WS=110;
    public static final int BITOR=26;
    public static final int STMTLIST=8;
    public static final int ALLOC=13;
    public static final int IDLIST=39;
    public static final int INLINE=23;
    public static final int CALL=22;
    public static final int INVOKE=95;
    public static final int FALSE=91;
    public static final int BACKSLASH=85;
    public static final int BAR_BAR=65;
    public static final int LBRACE_STAR=100;
    public static final int AMP=62;
    public static final int LBRACE=53;
    public static final int MULTI_COMMENT=112;
    public static final int FOR=55;
    public static final int SUB=29;
    public static final int ID=45;
    public static final int DEFINE=15;
    public static final int BITAND=25;
    public static final int LPAREN=57;
    public static final int COLONS=98;
    public static final int COLON_COLON_EQUALS=102;
    public static final int AT=63;
    public static final int CONDLIST=20;
    public static final int IDSUFFIX=106;
    public static final int EXPR=16;
    public static final int SLASH=84;
    public static final int IN=56;
    public static final int THEN=66;
    public static final int SCOPE=4;
    public static final int COMMA=52;
    public static final int BITXOR=27;
    public static final int TILDE=89;
    public static final int PLUS=81;
    public static final int SINGLE_COMMENT=111;
    public static final int DIGIT=108;
    public static final int RBRACKET=51;
    public static final int RSHIFT=79;
    public static final int RETURNS=59;
    public static final int ADD=28;
    public static final int COMPGE=73;
    public static final int PERCENT=86;
    public static final int LETTERLIKE=107;
    public static final int LIST_COMPREHENSION=5;
    public static final int COMPOR=68;
    public static final int HASH=103;
    public static final int MINUS=82;
    public static final int TRUE=92;
    public static final int SEMI=47;
    public static final int REF=12;
    public static final int COLON=48;
    public static final int COLON_EQUALS=49;
    public static final int NEWLINE=109;
    public static final int QUESTION=61;
    public static final int CHAR_LITERAL=93;
    public static final int LABEL=40;
    public static final int BLOCK=42;
    public static final int NEG=35;
    public static final int ASSIGN=14;
    public static final int URSHIFT=80;
    public static final int ARROW=104;
    public static final int COMPEQ=70;
    public static final int IDREF=38;
    public static final int DIV=31;
    public static final int COND=24;
    public static final int MACRO=7;
    public static final int PROTO=9;
    public static final int COMPNE=71;
    public static final int DATA=105;
    public static final int BAR=76;

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

                if ( ((LA1_0>=CODE && LA1_0<=MACRO)||LA1_0==ID||LA1_0==COLON||LA1_0==LBRACE||LA1_0==LPAREN||LA1_0==NULL||LA1_0==SELECT||(LA1_0>=MINUS && LA1_0<=STAR)||(LA1_0>=EXCL && LA1_0<=RECURSE)||LA1_0==COLONS) ) {
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:1: toplevelstat : ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) | ID COLON type ( EQUALS toplevelvalue )? SEMI -> ^( ALLOC ID type ( toplevelvalue )? ) | ID COLON_EQUALS rhsExpr SEMI -> ^( ALLOC ID TYPE rhsExpr ) | rhsExpr SEMI -> ^( EXPR rhsExpr ) | xscope );
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:13: ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) | ID COLON type ( EQUALS toplevelvalue )? SEMI -> ^( ALLOC ID type ( toplevelvalue )? ) | ID COLON_EQUALS rhsExpr SEMI -> ^( ALLOC ID TYPE rhsExpr ) | rhsExpr SEMI -> ^( EXPR rhsExpr ) | xscope )
            int alt3=5;
            switch ( input.LA(1) ) {
            case ID:
                {
                switch ( input.LA(2) ) {
                case EQUALS:
                    {
                    alt3=1;
                    }
                    break;
                case COLON:
                    {
                    alt3=2;
                    }
                    break;
                case COLON_EQUALS:
                    {
                    alt3=3;
                    }
                    break;
                case SEMI:
                case LPAREN:
                case QUESTION:
                case AMP:
                case COMPOR:
                case COMPAND:
                case COMPEQ:
                case COMPNE:
                case COMPLE:
                case COMPGE:
                case LESS:
                case GREATER:
                case BAR:
                case CARET:
                case LSHIFT:
                case RSHIFT:
                case URSHIFT:
                case PLUS:
                case MINUS:
                case STAR:
                case SLASH:
                case BACKSLASH:
                case PERCENT:
                case UMOD:
                case PERIOD:
                    {
                    alt3=4;
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
            case CODE:
            case MACRO:
            case COLON:
            case LPAREN:
            case NULL:
            case SELECT:
            case MINUS:
            case STAR:
            case EXCL:
            case TILDE:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case INVOKE:
            case RECURSE:
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:17: ID EQUALS toplevelvalue SEMI
                    {
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat369); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID4);

                    EQUALS5=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat371); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS5);

                    pushFollow(FOLLOW_toplevelvalue_in_toplevelstat373);
                    toplevelvalue6=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat379); if (state.failed) return retval; 
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
                    // 96:51: -> ^( DEFINE ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:96:54: ^( DEFINE ID toplevelvalue )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:7: ID COLON type ( EQUALS toplevelvalue )? SEMI
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat398); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID8);

                    COLON9=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelstat400); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON9);

                    pushFollow(FOLLOW_type_in_toplevelstat402);
                    type10=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type10.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:21: ( EQUALS toplevelvalue )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==EQUALS) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:22: EQUALS toplevelvalue
                            {
                            EQUALS11=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat405); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS11);

                            pushFollow(FOLLOW_toplevelvalue_in_toplevelstat407);
                            toplevelvalue12=toplevelvalue();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue12.getTree());

                            }
                            break;

                    }

                    SEMI13=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat415); if (state.failed) return retval; 
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
                    // 97:55: -> ^( ALLOC ID type ( toplevelvalue )? )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:58: ^( ALLOC ID type ( toplevelvalue )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:74: ( toplevelvalue )?
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:98:7: ID COLON_EQUALS rhsExpr SEMI
                    {
                    ID14=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat437); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID14);

                    COLON_EQUALS15=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelstat439); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS15);

                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat441);
                    rhsExpr16=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr16.getTree());
                    SEMI17=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat444); if (state.failed) return retval; 
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
                    // 98:38: -> ^( ALLOC ID TYPE rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:98:41: ^( ALLOC ID TYPE rhsExpr )
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
                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat465);
                    rhsExpr18=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr18.getTree());
                    SEMI19=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat484); if (state.failed) return retval; 
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

                    pushFollow(FOLLOW_xscope_in_toplevelstat501);
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

                    pushFollow(FOLLOW_xscope_in_toplevelvalue514);
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

                    pushFollow(FOLLOW_proto_in_toplevelvalue539);
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

                    pushFollow(FOLLOW_selector_in_toplevelvalue552);
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

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue560);
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
            LBRACKET25=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector573); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET25);

            pushFollow(FOLLOW_selectors_in_selector575);
            selectors26=selectors();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectors.add(selectors26.getTree());
            RBRACKET27=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector577); if (state.failed) return retval; 
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
                    pushFollow(FOLLOW_selectoritem_in_selectors603);
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
                    	    COMMA29=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors607); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA29);

                    	    pushFollow(FOLLOW_selectoritem_in_selectors609);
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
                            COMMA31=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors614); if (state.failed) return retval; 
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

                    pushFollow(FOLLOW_listCompr_in_selectoritem643);
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

                    pushFollow(FOLLOW_code_in_selectoritem647);
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

                    pushFollow(FOLLOW_macro_in_selectoritem651);
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
            LBRACE35=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope662); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE35);

            pushFollow(FOLLOW_toplevelstmts_in_xscope664);
            toplevelstmts36=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmts.add(toplevelstmts36.getTree());
            RBRACE37=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope666); if (state.failed) return retval; 
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
            	    pushFollow(FOLLOW_forIn_in_listCompr693);
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

            COLON39=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr696); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON39);

            pushFollow(FOLLOW_listiterable_in_listCompr698);
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
            FOR41=(Token)match(input,FOR,FOLLOW_FOR_in_forIn730); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR41);

            pushFollow(FOLLOW_idlist_in_forIn732);
            idlist42=idlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idlist.add(idlist42.getTree());
            IN43=(Token)match(input,IN,FOLLOW_IN_in_forIn734); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN43);

            pushFollow(FOLLOW_list_in_forIn736);
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
            ID45=(Token)match(input,ID,FOLLOW_ID_in_idlist761); if (state.failed) return retval; 
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
            	    COMMA46=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist764); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA46);

            	    ID47=(Token)match(input,ID,FOLLOW_ID_in_idlist766); if (state.failed) return retval; 
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
                    pushFollow(FOLLOW_code_in_listiterable795);
                    code48=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code48.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:25: macro
                    {
                    pushFollow(FOLLOW_macro_in_listiterable799);
                    macro49=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro49.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:33: proto
                    {
                    pushFollow(FOLLOW_proto_in_listiterable803);
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
            LBRACKET51=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list818); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET51);

            pushFollow(FOLLOW_listitems_in_list820);
            listitems52=listitems();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listitems.add(listitems52.getTree());
            RBRACKET53=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list822); if (state.failed) return retval; 
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

            if ( ((LA14_0>=CODE && LA14_0<=MACRO)||LA14_0==ID||LA14_0==COLON||LA14_0==LBRACKET||LA14_0==LBRACE||LA14_0==LPAREN||LA14_0==NULL||LA14_0==SELECT||(LA14_0>=MINUS && LA14_0<=STAR)||(LA14_0>=EXCL && LA14_0<=RECURSE)||LA14_0==COLONS) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems852);
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

                            if ( ((LA12_1>=CODE && LA12_1<=MACRO)||LA12_1==ID||LA12_1==COLON||LA12_1==LBRACKET||LA12_1==LBRACE||LA12_1==LPAREN||LA12_1==NULL||LA12_1==SELECT||(LA12_1>=MINUS && LA12_1<=STAR)||(LA12_1>=EXCL && LA12_1<=RECURSE)||LA12_1==COLONS) ) {
                                alt12=1;
                            }


                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:24: COMMA listitem
                    	    {
                    	    COMMA55=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems856); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA55_tree = (CommonTree)adaptor.create(COMMA55);
                    	    adaptor.addChild(root_0, COMMA55_tree);
                    	    }
                    	    pushFollow(FOLLOW_listitem_in_listitems858);
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
                            COMMA57=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems863); if (state.failed) return retval;
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

            pushFollow(FOLLOW_toplevelvalue_in_listitem889);
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
            CODE59=(Token)match(input,CODE,FOLLOW_CODE_in_code907); if (state.failed) return retval; 
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
                    LPAREN60=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_code911); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN60);

                    pushFollow(FOLLOW_optargdefs_in_code913);
                    optargdefs61=optargdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdefs.add(optargdefs61.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:33: ( xreturns )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==RETURNS) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:33: xreturns
                            {
                            pushFollow(FOLLOW_xreturns_in_code915);
                            xreturns62=xreturns();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_xreturns.add(xreturns62.getTree());

                            }
                            break;

                    }

                    RPAREN63=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_code918); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN63);


                    }
                    break;

            }

            LBRACE64=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_code924); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE64);

            pushFollow(FOLLOW_codestmtlist_in_code926);
            codestmtlist65=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist65.getTree());
            RBRACE66=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code928); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE66);



            // AST REWRITE
            // elements: CODE, xreturns, codestmtlist, optargdefs
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
            MACRO67=(Token)match(input,MACRO,FOLLOW_MACRO_in_macro963); if (state.failed) return retval; 
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
                    LPAREN68=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_macro967); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN68);

                    pushFollow(FOLLOW_optargdefs_in_macro969);
                    optargdefs69=optargdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdefs.add(optargdefs69.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:35: ( xreturns )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==RETURNS) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:35: xreturns
                            {
                            pushFollow(FOLLOW_xreturns_in_macro971);
                            xreturns70=xreturns();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_xreturns.add(xreturns70.getTree());

                            }
                            break;

                    }

                    RPAREN71=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_macro974); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN71);


                    }
                    break;

            }

            LBRACE72=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_macro980); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE72);

            pushFollow(FOLLOW_codestmtlist_in_macro982);
            codestmtlist73=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist73.getTree());
            RBRACE74=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_macro984); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE74);



            // AST REWRITE
            // elements: MACRO, codestmtlist, xreturns, optargdefs
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
            LPAREN75=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto1019); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN75);

            pushFollow(FOLLOW_argdefs_in_proto1021);
            argdefs76=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs76.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:24: ( xreturns )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==RETURNS) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:24: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto1023);
                    xreturns77=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns77.getTree());

                    }
                    break;

            }

            RPAREN78=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto1026); if (state.failed) return retval; 
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
                    pushFollow(FOLLOW_argdef_in_argdefs1068);
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
                    	    COMMA80=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1072); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA80);

                    	    pushFollow(FOLLOW_argdef_in_argdefs1074);
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
                            COMMA82=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1078); if (state.failed) return retval; 
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
                    MACRO83=(Token)match(input,MACRO,FOLLOW_MACRO_in_argdef1122); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MACRO.add(MACRO83);


                    }
                    break;

            }

            ID84=(Token)match(input,ID,FOLLOW_ID_in_argdef1125); if (state.failed) return retval; 
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
                    COLON85=(Token)match(input,COLON,FOLLOW_COLON_in_argdef1128); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON85);

                    pushFollow(FOLLOW_type_in_argdef1130);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:1: xreturns : ( RETURNS type -> type | RETURNS argtuple -> argtuple | RETURNS NULL -> ^( TYPE NULL ) );
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURNS87=null;
        Token RETURNS89=null;
        Token RETURNS91=null;
        Token NULL92=null;
        EulangParser.type_return type88 = null;

        EulangParser.argtuple_return argtuple90 = null;


        CommonTree RETURNS87_tree=null;
        CommonTree RETURNS89_tree=null;
        CommonTree RETURNS91_tree=null;
        CommonTree NULL92_tree=null;
        RewriteRuleTokenStream stream_NULL=new RewriteRuleTokenStream(adaptor,"token NULL");
        RewriteRuleTokenStream stream_RETURNS=new RewriteRuleTokenStream(adaptor,"token RETURNS");
        RewriteRuleSubtreeStream stream_argtuple=new RewriteRuleSubtreeStream(adaptor,"rule argtuple");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:9: ( RETURNS type -> type | RETURNS argtuple -> argtuple | RETURNS NULL -> ^( TYPE NULL ) )
            int alt25=3;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==RETURNS) ) {
                switch ( input.LA(2) ) {
                case NULL:
                    {
                    alt25=3;
                    }
                    break;
                case LPAREN:
                    {
                    alt25=2;
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:11: RETURNS type
                    {
                    RETURNS87=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_xreturns1160); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RETURNS.add(RETURNS87);

                    pushFollow(FOLLOW_type_in_xreturns1162);
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
                    // 162:29: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:5: RETURNS argtuple
                    {
                    RETURNS89=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_xreturns1177); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RETURNS.add(RETURNS89);

                    pushFollow(FOLLOW_argtuple_in_xreturns1179);
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
                    // 163:32: -> argtuple
                    {
                        adaptor.addChild(root_0, stream_argtuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:5: RETURNS NULL
                    {
                    RETURNS91=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_xreturns1199); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RETURNS.add(RETURNS91);

                    NULL92=(Token)match(input,NULL,FOLLOW_NULL_in_xreturns1201); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NULL.add(NULL92);



                    // AST REWRITE
                    // elements: NULL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 164:29: -> ^( TYPE NULL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:32: ^( TYPE NULL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_NULL.nextNode());

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
            LPAREN93=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_argtuple1231); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN93);

            pushFollow(FOLLOW_tupleargdefs_in_argtuple1233);
            tupleargdefs94=tupleargdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdefs.add(tupleargdefs94.getTree());
            RPAREN95=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_argtuple1235); if (state.failed) return retval; 
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
            pushFollow(FOLLOW_tupleargdef_in_tupleargdefs1257);
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
            	    COMMA97=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleargdefs1261); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA97);

            	    pushFollow(FOLLOW_tupleargdef_in_tupleargdefs1263);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:1: tupleargdef : ( type -> type | QUESTION -> ^( TYPE NULL ) | -> ^( TYPE NULL ) );
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:12: ( type -> type | QUESTION -> ^( TYPE NULL ) | -> ^( TYPE NULL ) )
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
                    pushFollow(FOLLOW_type_in_tupleargdef1308);
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
                    QUESTION100=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_tupleargdef1321); if (state.failed) return retval; 
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
                    // 174:21: -> ^( TYPE NULL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:24: ^( TYPE NULL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NULL, "NULL"));

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
                    // 175:21: -> ^( TYPE NULL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:24: ^( TYPE NULL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(NULL, "NULL"));

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
                    pushFollow(FOLLOW_optargdef_in_optargdefs1378);
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
                    	    COMMA102=(Token)match(input,COMMA,FOLLOW_COMMA_in_optargdefs1382); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA102);

                    	    pushFollow(FOLLOW_optargdef_in_optargdefs1384);
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
                            COMMA104=(Token)match(input,COMMA,FOLLOW_COMMA_in_optargdefs1388); if (state.failed) return retval; 
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
                    ID105=(Token)match(input,ID,FOLLOW_ID_in_optargdef1432); if (state.failed) return retval; 
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
                            COLON106=(Token)match(input,COLON,FOLLOW_COLON_in_optargdef1435); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON106);

                            pushFollow(FOLLOW_type_in_optargdef1437);
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
                    MACRO108=(Token)match(input,MACRO,FOLLOW_MACRO_in_optargdef1461); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MACRO.add(MACRO108);

                    ID109=(Token)match(input,ID,FOLLOW_ID_in_optargdef1463); if (state.failed) return retval; 
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
                            COLON110=(Token)match(input,COLON,FOLLOW_COLON_in_optargdef1466); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON110);

                            pushFollow(FOLLOW_type_in_optargdef1468);
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
                            EQUALS112=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_optargdef1473); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS112);

                            pushFollow(FOLLOW_rhsExpr_in_optargdef1477);
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
                    pushFollow(FOLLOW_idOrScopeRef_in_type1515);
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
                            AMP114=(Token)match(input,AMP,FOLLOW_AMP_in_type1530); if (state.failed) return retval; 
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
                    CODE115=(Token)match(input,CODE,FOLLOW_CODE_in_type1556); if (state.failed) return retval; 
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
                            pushFollow(FOLLOW_proto_in_type1558);
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

            if ( ((LA40_0>=CODE && LA40_0<=MACRO)||LA40_0==ID||LA40_0==COLON||LA40_0==LBRACE||LA40_0==LPAREN||LA40_0==NULL||(LA40_0>=AT && LA40_0<=SELECT)||(LA40_0>=MINUS && LA40_0<=STAR)||(LA40_0>=EXCL && LA40_0<=RECURSE)||LA40_0==COLONS) ) {
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
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist1586);
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
                    	    SEMI118=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist1589); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI118);

                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:50: ( codeStmt )?
                    	    int alt38=2;
                    	    int LA38_0 = input.LA(1);

                    	    if ( ((LA38_0>=CODE && LA38_0<=MACRO)||LA38_0==ID||LA38_0==COLON||LA38_0==LBRACE||LA38_0==LPAREN||LA38_0==NULL||(LA38_0>=AT && LA38_0<=SELECT)||(LA38_0>=MINUS && LA38_0<=STAR)||(LA38_0>=EXCL && LA38_0<=RECURSE)||LA38_0==COLONS) ) {
                    	        alt38=1;
                    	    }
                    	    switch (alt38) {
                    	        case 1 :
                    	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:50: codeStmt
                    	            {
                    	            pushFollow(FOLLOW_codeStmt_in_codestmtlist1591);
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

            if ( (LA41_0==AT) ) {
                int LA41_1 = input.LA(2);

                if ( (LA41_1==ID) ) {
                    int LA41_3 = input.LA(3);

                    if ( (LA41_3==COLON) ) {
                        alt41=1;
                    }
                    else if ( (LA41_3==SEMI||LA41_3==RBRACE||LA41_3==PERIOD) ) {
                        alt41=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 41, 3, input);

                        throw nvae;
                    }
                }
                else if ( (LA41_1==COLON||LA41_1==COLONS) ) {
                    alt41=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 41, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA41_0>=CODE && LA41_0<=MACRO)||LA41_0==ID||LA41_0==COLON||LA41_0==LBRACE||LA41_0==LPAREN||LA41_0==NULL||LA41_0==SELECT||(LA41_0>=MINUS && LA41_0<=STAR)||(LA41_0>=EXCL && LA41_0<=RECURSE)||LA41_0==COLONS) ) {
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
                    pushFollow(FOLLOW_labelStmt_in_codeStmt1640);
                    labelStmt120=labelStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labelStmt.add(labelStmt120.getTree());
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt1642);
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
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt1663);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:1: codeStmtExpr : ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt );
    public final EulangParser.codeStmtExpr_return codeStmtExpr() throws RecognitionException {
        EulangParser.codeStmtExpr_return retval = new EulangParser.codeStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl123 = null;

        EulangParser.assignStmt_return assignStmt124 = null;

        EulangParser.rhsExpr_return rhsExpr125 = null;

        EulangParser.blockStmt_return blockStmt126 = null;

        EulangParser.gotoStmt_return gotoStmt127 = null;


        RewriteRuleSubtreeStream stream_assignStmt=new RewriteRuleSubtreeStream(adaptor,"rule assignStmt");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_blockStmt=new RewriteRuleSubtreeStream(adaptor,"rule blockStmt");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:14: ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt )
            int alt42=5;
            alt42 = dfa42.predict(input);
            switch (alt42) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:16: varDecl
                    {
                    pushFollow(FOLLOW_varDecl_in_codeStmtExpr1682);
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
                    pushFollow(FOLLOW_assignStmt_in_codeStmtExpr1699);
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
                    pushFollow(FOLLOW_rhsExpr_in_codeStmtExpr1723);
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
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:202:26: ^( STMTEXPR rhsExpr )
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
                    pushFollow(FOLLOW_blockStmt_in_codeStmtExpr1747);
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
                    pushFollow(FOLLOW_gotoStmt_in_codeStmtExpr1776);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:209:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID128=null;
        Token COLON_EQUALS129=null;
        Token COLON_EQUALS132=null;
        Token ID134=null;
        Token COLON135=null;
        Token EQUALS137=null;
        Token COLON140=null;
        Token EQUALS142=null;
        EulangParser.assignExpr_return assignExpr130 = null;

        EulangParser.idTuple_return idTuple131 = null;

        EulangParser.assignExpr_return assignExpr133 = null;

        EulangParser.type_return type136 = null;

        EulangParser.assignExpr_return assignExpr138 = null;

        EulangParser.idTuple_return idTuple139 = null;

        EulangParser.type_return type141 = null;

        EulangParser.assignExpr_return assignExpr143 = null;


        CommonTree ID128_tree=null;
        CommonTree COLON_EQUALS129_tree=null;
        CommonTree COLON_EQUALS132_tree=null;
        CommonTree ID134_tree=null;
        CommonTree COLON135_tree=null;
        CommonTree EQUALS137_tree=null;
        CommonTree COLON140_tree=null;
        CommonTree EQUALS142_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:209:8: ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) )
            int alt45=4;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:209:10: ID COLON_EQUALS assignExpr
                    {
                    ID128=(Token)match(input,ID,FOLLOW_ID_in_varDecl1806); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID128);

                    COLON_EQUALS129=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1808); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS129);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1810);
                    assignExpr130=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr130.getTree());


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
                    // 209:45: -> ^( ALLOC ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:209:48: ^( ALLOC ID TYPE assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:7: idTuple COLON_EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_varDecl1838);
                    idTuple131=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple131.getTree());
                    COLON_EQUALS132=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1840); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS132);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1842);
                    assignExpr133=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr133.getTree());


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
                    // 210:47: -> ^( ALLOC idTuple TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:50: ^( ALLOC idTuple TYPE assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID134=(Token)match(input,ID,FOLLOW_ID_in_varDecl1870); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID134);

                    COLON135=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1872); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON135);

                    pushFollow(FOLLOW_type_in_varDecl1874);
                    type136=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type136.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:21: ( EQUALS assignExpr )?
                    int alt43=2;
                    int LA43_0 = input.LA(1);

                    if ( (LA43_0==EQUALS) ) {
                        alt43=1;
                    }
                    switch (alt43) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:22: EQUALS assignExpr
                            {
                            EQUALS137=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1877); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS137);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1879);
                            assignExpr138=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr138.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, assignExpr, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 211:43: -> ^( ALLOC ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:46: ^( ALLOC ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:62: ( assignExpr )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:7: idTuple COLON type ( EQUALS assignExpr )?
                    {
                    pushFollow(FOLLOW_idTuple_in_varDecl1903);
                    idTuple139=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple139.getTree());
                    COLON140=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1905); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON140);

                    pushFollow(FOLLOW_type_in_varDecl1907);
                    type141=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type141.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:26: ( EQUALS assignExpr )?
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==EQUALS) ) {
                        alt44=1;
                    }
                    switch (alt44) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:27: EQUALS assignExpr
                            {
                            EQUALS142=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1910); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS142);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1912);
                            assignExpr143=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr143.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: assignExpr, type, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 212:48: -> ^( ALLOC idTuple type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:51: ^( ALLOC idTuple type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:72: ( assignExpr )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:1: assignStmt : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) );
    public final EulangParser.assignStmt_return assignStmt() throws RecognitionException {
        EulangParser.assignStmt_return retval = new EulangParser.assignStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS145=null;
        Token EQUALS148=null;
        EulangParser.idOrScopeRef_return idOrScopeRef144 = null;

        EulangParser.assignExpr_return assignExpr146 = null;

        EulangParser.idTuple_return idTuple147 = null;

        EulangParser.assignExpr_return assignExpr149 = null;


        CommonTree EQUALS145_tree=null;
        CommonTree EQUALS148_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignStmt1950);
                    idOrScopeRef144=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef144.getTree());
                    EQUALS145=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt1952); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS145);

                    pushFollow(FOLLOW_assignExpr_in_assignStmt1954);
                    assignExpr146=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr146.getTree());


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
                    // 218:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:55: ^( ASSIGN idOrScopeRef assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:7: idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignStmt1979);
                    idTuple147=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple147.getTree());
                    EQUALS148=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt1981); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS148);

                    pushFollow(FOLLOW_assignExpr_in_assignStmt1983);
                    assignExpr149=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr149.getTree());


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
                    // 219:47: -> ^( ASSIGN idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:50: ^( ASSIGN idTuple assignExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS151=null;
        Token EQUALS154=null;
        EulangParser.idOrScopeRef_return idOrScopeRef150 = null;

        EulangParser.assignExpr_return assignExpr152 = null;

        EulangParser.idTuple_return idTuple153 = null;

        EulangParser.assignExpr_return assignExpr155 = null;

        EulangParser.rhsExpr_return rhsExpr156 = null;


        CommonTree EQUALS151_tree=null;
        CommonTree EQUALS154_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr )
            int alt47=3;
            alt47 = dfa47.predict(input);
            switch (alt47) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignExpr2026);
                    idOrScopeRef150=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef150.getTree());
                    EQUALS151=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr2028); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS151);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr2030);
                    assignExpr152=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr152.getTree());


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
                    // 222:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:55: ^( ASSIGN idOrScopeRef assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:223:7: idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignExpr2055);
                    idTuple153=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple153.getTree());
                    EQUALS154=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr2057); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS154);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr2059);
                    assignExpr155=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr155.getTree());


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
                    // 223:47: -> ^( ASSIGN idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:223:50: ^( ASSIGN idTuple assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr2091);
                    rhsExpr156=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr156.getTree());


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
                    // 224:43: -> rhsExpr
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

    public static class labelStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "labelStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:1: labelStmt : AT ID COLON -> ^( LABEL ID ) ;
    public final EulangParser.labelStmt_return labelStmt() throws RecognitionException {
        EulangParser.labelStmt_return retval = new EulangParser.labelStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT157=null;
        Token ID158=null;
        Token COLON159=null;

        CommonTree AT157_tree=null;
        CommonTree ID158_tree=null;
        CommonTree COLON159_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:10: ( AT ID COLON -> ^( LABEL ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:12: AT ID COLON
            {
            AT157=(Token)match(input,AT,FOLLOW_AT_in_labelStmt2135); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT157);

            ID158=(Token)match(input,ID,FOLLOW_ID_in_labelStmt2137); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID158);

            COLON159=(Token)match(input,COLON,FOLLOW_COLON_in_labelStmt2139); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON159);



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
            // 227:43: -> ^( LABEL ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:46: ^( LABEL ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:1: gotoStmt : AT idOrScopeRef -> ^( GOTO idOrScopeRef ) ;
    public final EulangParser.gotoStmt_return gotoStmt() throws RecognitionException {
        EulangParser.gotoStmt_return retval = new EulangParser.gotoStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT160=null;
        EulangParser.idOrScopeRef_return idOrScopeRef161 = null;


        CommonTree AT160_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:9: ( AT idOrScopeRef -> ^( GOTO idOrScopeRef ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:11: AT idOrScopeRef
            {
            AT160=(Token)match(input,AT,FOLLOW_AT_in_gotoStmt2176); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT160);

            pushFollow(FOLLOW_idOrScopeRef_in_gotoStmt2178);
            idOrScopeRef161=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef161.getTree());


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
            // 230:42: -> ^( GOTO idOrScopeRef )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:45: ^( GOTO idOrScopeRef )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(GOTO, "GOTO"), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:1: blockStmt : LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) ;
    public final EulangParser.blockStmt_return blockStmt() throws RecognitionException {
        EulangParser.blockStmt_return retval = new EulangParser.blockStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE162=null;
        Token RBRACE164=null;
        EulangParser.codestmtlist_return codestmtlist163 = null;


        CommonTree LBRACE162_tree=null;
        CommonTree RBRACE164_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:10: ( LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:12: LBRACE codestmtlist RBRACE
            {
            LBRACE162=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_blockStmt2213); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE162);

            pushFollow(FOLLOW_codestmtlist_in_blockStmt2215);
            codestmtlist163=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist163.getTree());
            RBRACE164=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_blockStmt2217); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE164);



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
            // 233:43: -> ^( BLOCK codestmtlist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:46: ^( BLOCK codestmtlist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:1: tuple : LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) ;
    public final EulangParser.tuple_return tuple() throws RecognitionException {
        EulangParser.tuple_return retval = new EulangParser.tuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN165=null;
        Token RPAREN167=null;
        EulangParser.tupleEntries_return tupleEntries166 = null;


        CommonTree LPAREN165_tree=null;
        CommonTree RPAREN167_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule tupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:7: ( LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:9: LPAREN tupleEntries RPAREN
            {
            LPAREN165=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_tuple2240); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN165);

            pushFollow(FOLLOW_tupleEntries_in_tuple2242);
            tupleEntries166=tupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleEntries.add(tupleEntries166.getTree());
            RPAREN167=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_tuple2244); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN167);



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
            // 236:41: -> ^( TUPLE ( tupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:44: ^( TUPLE ( tupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:239:1: tupleEntries : assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ ;
    public final EulangParser.tupleEntries_return tupleEntries() throws RecognitionException {
        EulangParser.tupleEntries_return retval = new EulangParser.tupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA169=null;
        EulangParser.assignExpr_return assignExpr168 = null;

        EulangParser.assignExpr_return assignExpr170 = null;


        CommonTree COMMA169_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:239:14: ( assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:239:16: assignExpr ( COMMA assignExpr )+
            {
            pushFollow(FOLLOW_assignExpr_in_tupleEntries2272);
            assignExpr168=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr168.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:239:27: ( COMMA assignExpr )+
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:239:28: COMMA assignExpr
            	    {
            	    COMMA169=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleEntries2275); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA169);

            	    pushFollow(FOLLOW_assignExpr_in_tupleEntries2277);
            	    assignExpr170=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr170.getTree());

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
            // 239:48: -> ( assignExpr )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:1: idTuple : LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) ;
    public final EulangParser.idTuple_return idTuple() throws RecognitionException {
        EulangParser.idTuple_return retval = new EulangParser.idTuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN171=null;
        Token RPAREN173=null;
        EulangParser.idTupleEntries_return idTupleEntries172 = null;


        CommonTree LPAREN171_tree=null;
        CommonTree RPAREN173_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_idTupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule idTupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:9: ( LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:11: LPAREN idTupleEntries RPAREN
            {
            LPAREN171=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_idTuple2296); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN171);

            pushFollow(FOLLOW_idTupleEntries_in_idTuple2298);
            idTupleEntries172=idTupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTupleEntries.add(idTupleEntries172.getTree());
            RPAREN173=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_idTuple2300); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN173);



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
            // 242:45: -> ^( TUPLE ( idTupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:48: ^( TUPLE ( idTupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:1: idTupleEntries : idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ ;
    public final EulangParser.idTupleEntries_return idTupleEntries() throws RecognitionException {
        EulangParser.idTupleEntries_return retval = new EulangParser.idTupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA175=null;
        EulangParser.idOrScopeRef_return idOrScopeRef174 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef176 = null;


        CommonTree COMMA175_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:16: ( idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:18: idOrScopeRef ( COMMA idOrScopeRef )+
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries2328);
            idOrScopeRef174=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef174.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:31: ( COMMA idOrScopeRef )+
            int cnt49=0;
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==COMMA) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:32: COMMA idOrScopeRef
            	    {
            	    COMMA175=(Token)match(input,COMMA,FOLLOW_COMMA_in_idTupleEntries2331); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA175);

            	    pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries2333);
            	    idOrScopeRef176=idOrScopeRef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef176.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt49 >= 1 ) break loop49;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(49, input);
                        throw eee;
                }
                cnt49++;
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
            // 245:54: -> ( idOrScopeRef )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:1: rhsExpr : condStar -> condStar ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.condStar_return condStar177 = null;


        RewriteRuleSubtreeStream stream_condStar=new RewriteRuleSubtreeStream(adaptor,"rule condStar");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:9: ( condStar -> condStar )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:13: condStar
            {
            pushFollow(FOLLOW_condStar_in_rhsExpr2354);
            condStar177=condStar();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStar.add(condStar177.getTree());


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
            // 248:47: -> condStar
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:1: funcCall : idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN179=null;
        Token RPAREN181=null;
        EulangParser.idOrScopeRef_return idOrScopeRef178 = null;

        EulangParser.arglist_return arglist180 = null;


        CommonTree LPAREN179_tree=null;
        CommonTree RPAREN181_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:10: ( idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:12: idOrScopeRef LPAREN arglist RPAREN
            {
            pushFollow(FOLLOW_idOrScopeRef_in_funcCall2400);
            idOrScopeRef178=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef178.getTree());
            LPAREN179=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall2402); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN179);

            pushFollow(FOLLOW_arglist_in_funcCall2404);
            arglist180=arglist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arglist.add(arglist180.getTree());
            RPAREN181=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall2406); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN181);



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
            // 251:49: -> ^( CALL idOrScopeRef arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:56: ^( CALL idOrScopeRef arglist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA183=null;
        Token COMMA185=null;
        EulangParser.arg_return arg182 = null;

        EulangParser.arg_return arg184 = null;


        CommonTree COMMA183_tree=null;
        CommonTree COMMA185_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( ((LA52_0>=CODE && LA52_0<=MACRO)||LA52_0==ID||LA52_0==COLON||LA52_0==LBRACE||LA52_0==LPAREN||LA52_0==NULL||(LA52_0>=AT && LA52_0<=SELECT)||(LA52_0>=MINUS && LA52_0<=STAR)||(LA52_0>=EXCL && LA52_0<=RECURSE)||LA52_0==COLONS) ) {
                alt52=1;
            }
            switch (alt52) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist2437);
                    arg182=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg182.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:15: ( COMMA arg )*
                    loop50:
                    do {
                        int alt50=2;
                        int LA50_0 = input.LA(1);

                        if ( (LA50_0==COMMA) ) {
                            int LA50_1 = input.LA(2);

                            if ( ((LA50_1>=CODE && LA50_1<=MACRO)||LA50_1==ID||LA50_1==COLON||LA50_1==LBRACE||LA50_1==LPAREN||LA50_1==NULL||(LA50_1>=AT && LA50_1<=SELECT)||(LA50_1>=MINUS && LA50_1<=STAR)||(LA50_1>=EXCL && LA50_1<=RECURSE)||LA50_1==COLONS) ) {
                                alt50=1;
                            }


                        }


                        switch (alt50) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:17: COMMA arg
                    	    {
                    	    COMMA183=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist2441); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA183);

                    	    pushFollow(FOLLOW_arg_in_arglist2443);
                    	    arg184=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg184.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop50;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:29: ( COMMA )?
                    int alt51=2;
                    int LA51_0 = input.LA(1);

                    if ( (LA51_0==COMMA) ) {
                        alt51=1;
                    }
                    switch (alt51) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:29: COMMA
                            {
                            COMMA185=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist2447); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA185);


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
            // 255:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:1: arg : ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) );
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE187=null;
        Token RBRACE189=null;
        EulangParser.assignExpr_return assignExpr186 = null;

        EulangParser.codestmtlist_return codestmtlist188 = null;

        EulangParser.gotoStmt_return gotoStmt190 = null;


        CommonTree LBRACE187_tree=null;
        CommonTree RBRACE189_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:4: ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) )
            int alt53=3;
            switch ( input.LA(1) ) {
            case CODE:
            case MACRO:
            case ID:
            case COLON:
            case LPAREN:
            case NULL:
            case SELECT:
            case MINUS:
            case STAR:
            case EXCL:
            case TILDE:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case INVOKE:
            case RECURSE:
            case COLONS:
                {
                alt53=1;
                }
                break;
            case LBRACE:
                {
                alt53=2;
                }
                break;
            case AT:
                {
                alt53=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;
            }

            switch (alt53) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:7: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_arg2496);
                    assignExpr186=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr186.getTree());


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
                    // 258:37: -> ^( EXPR assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:40: ^( EXPR assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:5: LBRACE codestmtlist RBRACE
                    {
                    LBRACE187=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_arg2529); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE187);

                    pushFollow(FOLLOW_codestmtlist_in_arg2531);
                    codestmtlist188=codestmtlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist188.getTree());
                    RBRACE189=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_arg2533); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE189);



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
                    // 259:37: -> ^( EXPR ^( CODE codestmtlist ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:40: ^( EXPR ^( CODE codestmtlist ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:47: ^( CODE codestmtlist )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:5: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_arg2557);
                    gotoStmt190=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt190.getTree());


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
                    // 260:37: -> ^( EXPR gotoStmt )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:40: ^( EXPR gotoStmt )
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

    public static class condStar_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condStar"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:1: condStar : ( cond -> cond | SELECT LBRACKET condTests RBRACKET -> condTests | SELECT condTestExprs -> condTestExprs );
    public final EulangParser.condStar_return condStar() throws RecognitionException {
        EulangParser.condStar_return retval = new EulangParser.condStar_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SELECT192=null;
        Token LBRACKET193=null;
        Token RBRACKET195=null;
        Token SELECT196=null;
        EulangParser.cond_return cond191 = null;

        EulangParser.condTests_return condTests194 = null;

        EulangParser.condTestExprs_return condTestExprs197 = null;


        CommonTree SELECT192_tree=null;
        CommonTree LBRACKET193_tree=null;
        CommonTree RBRACKET195_tree=null;
        CommonTree SELECT196_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_SELECT=new RewriteRuleTokenStream(adaptor,"token SELECT");
        RewriteRuleSubtreeStream stream_condTestExprs=new RewriteRuleSubtreeStream(adaptor,"rule condTestExprs");
        RewriteRuleSubtreeStream stream_condTests=new RewriteRuleSubtreeStream(adaptor,"rule condTests");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:9: ( cond -> cond | SELECT LBRACKET condTests RBRACKET -> condTests | SELECT condTestExprs -> condTestExprs )
            int alt54=3;
            int LA54_0 = input.LA(1);

            if ( ((LA54_0>=CODE && LA54_0<=MACRO)||LA54_0==ID||LA54_0==COLON||LA54_0==LPAREN||LA54_0==NULL||(LA54_0>=MINUS && LA54_0<=STAR)||(LA54_0>=EXCL && LA54_0<=RECURSE)||LA54_0==COLONS) ) {
                alt54=1;
            }
            else if ( (LA54_0==SELECT) ) {
                int LA54_2 = input.LA(2);

                if ( (LA54_2==LBRACKET) ) {
                    alt54=2;
                }
                else if ( ((LA54_2>=CODE && LA54_2<=MACRO)||LA54_2==ID||LA54_2==COLON||LA54_2==LPAREN||LA54_2==NULL||(LA54_2>=MINUS && LA54_2<=STAR)||(LA54_2>=EXCL && LA54_2<=RECURSE)||LA54_2==COLONS) ) {
                    alt54=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 54, 2, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;
            }
            switch (alt54) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:11: cond
                    {
                    pushFollow(FOLLOW_cond_in_condStar2607);
                    cond191=cond();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cond.add(cond191.getTree());


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
                    // 271:16: -> cond
                    {
                        adaptor.addChild(root_0, stream_cond.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:6: SELECT LBRACKET condTests RBRACKET
                    {
                    SELECT192=(Token)match(input,SELECT,FOLLOW_SELECT_in_condStar2618); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SELECT.add(SELECT192);

                    LBRACKET193=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_condStar2620); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET193);

                    pushFollow(FOLLOW_condTests_in_condStar2622);
                    condTests194=condTests();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_condTests.add(condTests194.getTree());
                    RBRACKET195=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_condStar2624); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET195);



                    // AST REWRITE
                    // elements: condTests
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 272:41: -> condTests
                    {
                        adaptor.addChild(root_0, stream_condTests.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:273:6: SELECT condTestExprs
                    {
                    SELECT196=(Token)match(input,SELECT,FOLLOW_SELECT_in_condStar2635); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SELECT.add(SELECT196);

                    pushFollow(FOLLOW_condTestExprs_in_condStar2637);
                    condTestExprs197=condTestExprs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_condTestExprs.add(condTestExprs197.getTree());


                    // AST REWRITE
                    // elements: condTestExprs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 273:27: -> condTestExprs
                    {
                        adaptor.addChild(root_0, stream_condTestExprs.nextTree());

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

    public static class condTests_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condTests"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:1: condTests : condTest ( BAR_BAR condTest )* ( BAR_BAR )? condFinalOrEmpty -> ^( CONDLIST ( condTest )* condFinalOrEmpty ) ;
    public final EulangParser.condTests_return condTests() throws RecognitionException {
        EulangParser.condTests_return retval = new EulangParser.condTests_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR_BAR199=null;
        Token BAR_BAR201=null;
        EulangParser.condTest_return condTest198 = null;

        EulangParser.condTest_return condTest200 = null;

        EulangParser.condFinalOrEmpty_return condFinalOrEmpty202 = null;


        CommonTree BAR_BAR199_tree=null;
        CommonTree BAR_BAR201_tree=null;
        RewriteRuleTokenStream stream_BAR_BAR=new RewriteRuleTokenStream(adaptor,"token BAR_BAR");
        RewriteRuleSubtreeStream stream_condTest=new RewriteRuleSubtreeStream(adaptor,"rule condTest");
        RewriteRuleSubtreeStream stream_condFinalOrEmpty=new RewriteRuleSubtreeStream(adaptor,"rule condFinalOrEmpty");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:11: ( condTest ( BAR_BAR condTest )* ( BAR_BAR )? condFinalOrEmpty -> ^( CONDLIST ( condTest )* condFinalOrEmpty ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:13: condTest ( BAR_BAR condTest )* ( BAR_BAR )? condFinalOrEmpty
            {
            pushFollow(FOLLOW_condTest_in_condTests2653);
            condTest198=condTest();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condTest.add(condTest198.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:22: ( BAR_BAR condTest )*
            loop55:
            do {
                int alt55=2;
                int LA55_0 = input.LA(1);

                if ( (LA55_0==BAR_BAR) ) {
                    int LA55_1 = input.LA(2);

                    if ( ((LA55_1>=CODE && LA55_1<=MACRO)||LA55_1==ID||LA55_1==COLON||LA55_1==LPAREN||LA55_1==NULL||(LA55_1>=MINUS && LA55_1<=STAR)||(LA55_1>=EXCL && LA55_1<=RECURSE)||LA55_1==COLONS) ) {
                        alt55=1;
                    }


                }


                switch (alt55) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:23: BAR_BAR condTest
            	    {
            	    BAR_BAR199=(Token)match(input,BAR_BAR,FOLLOW_BAR_BAR_in_condTests2656); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR_BAR.add(BAR_BAR199);

            	    pushFollow(FOLLOW_condTest_in_condTests2658);
            	    condTest200=condTest();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_condTest.add(condTest200.getTree());

            	    }
            	    break;

            	default :
            	    break loop55;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:42: ( BAR_BAR )?
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==BAR_BAR) ) {
                alt56=1;
            }
            switch (alt56) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:42: BAR_BAR
                    {
                    BAR_BAR201=(Token)match(input,BAR_BAR,FOLLOW_BAR_BAR_in_condTests2662); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_BAR_BAR.add(BAR_BAR201);


                    }
                    break;

            }

            pushFollow(FOLLOW_condFinalOrEmpty_in_condTests2665);
            condFinalOrEmpty202=condFinalOrEmpty();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condFinalOrEmpty.add(condFinalOrEmpty202.getTree());


            // AST REWRITE
            // elements: condFinalOrEmpty, condTest
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 275:68: -> ^( CONDLIST ( condTest )* condFinalOrEmpty )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:71: ^( CONDLIST ( condTest )* condFinalOrEmpty )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDLIST, "CONDLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:82: ( condTest )*
                while ( stream_condTest.hasNext() ) {
                    adaptor.addChild(root_1, stream_condTest.nextTree());

                }
                stream_condTest.reset();
                adaptor.addChild(root_1, stream_condFinalOrEmpty.nextTree());

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
    // $ANTLR end "condTests"

    public static class condTest_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condTest"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:1: condTest : ( cond THEN )=> cond THEN arg -> ^( CONDTEST cond arg ) ;
    public final EulangParser.condTest_return condTest() throws RecognitionException {
        EulangParser.condTest_return retval = new EulangParser.condTest_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token THEN204=null;
        EulangParser.cond_return cond203 = null;

        EulangParser.arg_return arg205 = null;


        CommonTree THEN204_tree=null;
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:10: ( ( cond THEN )=> cond THEN arg -> ^( CONDTEST cond arg ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:12: ( cond THEN )=> cond THEN arg
            {
            pushFollow(FOLLOW_cond_in_condTest2698);
            cond203=cond();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_cond.add(cond203.getTree());
            THEN204=(Token)match(input,THEN,FOLLOW_THEN_in_condTest2700); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_THEN.add(THEN204);

            pushFollow(FOLLOW_arg_in_condTest2702);
            arg205=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(arg205.getTree());


            // AST REWRITE
            // elements: arg, cond
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 277:41: -> ^( CONDTEST cond arg )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:44: ^( CONDTEST cond arg )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                adaptor.addChild(root_1, stream_cond.nextTree());
                adaptor.addChild(root_1, stream_arg.nextTree());

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
    // $ANTLR end "condTest"

    public static class condFinal_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condFinal"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:1: condFinal : ELSE arg -> ^( CONDTEST ^( LIT TRUE ) arg ) ;
    public final EulangParser.condFinal_return condFinal() throws RecognitionException {
        EulangParser.condFinal_return retval = new EulangParser.condFinal_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELSE206=null;
        EulangParser.arg_return arg207 = null;


        CommonTree ELSE206_tree=null;
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:11: ( ELSE arg -> ^( CONDTEST ^( LIT TRUE ) arg ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:13: ELSE arg
            {
            ELSE206=(Token)match(input,ELSE,FOLLOW_ELSE_in_condFinal2722); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ELSE.add(ELSE206);

            pushFollow(FOLLOW_arg_in_condFinal2724);
            arg207=arg();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arg.add(arg207.getTree());


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
            // 279:22: -> ^( CONDTEST ^( LIT TRUE ) arg )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:25: ^( CONDTEST ^( LIT TRUE ) arg )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:36: ^( LIT TRUE )
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
    // $ANTLR end "condFinal"

    public static class condFinalOrEmpty_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condFinalOrEmpty"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:1: condFinalOrEmpty : ( condFinal -> condFinal | -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NULL ) ) );
    public final EulangParser.condFinalOrEmpty_return condFinalOrEmpty() throws RecognitionException {
        EulangParser.condFinalOrEmpty_return retval = new EulangParser.condFinalOrEmpty_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.condFinal_return condFinal208 = null;


        RewriteRuleSubtreeStream stream_condFinal=new RewriteRuleSubtreeStream(adaptor,"rule condFinal");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:18: ( condFinal -> condFinal | -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NULL ) ) )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==ELSE) ) {
                alt57=1;
            }
            else if ( (LA57_0==RBRACKET) ) {
                alt57=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;
            }
            switch (alt57) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:20: condFinal
                    {
                    pushFollow(FOLLOW_condFinal_in_condFinalOrEmpty2750);
                    condFinal208=condFinal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_condFinal.add(condFinal208.getTree());


                    // AST REWRITE
                    // elements: condFinal
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 281:30: -> condFinal
                    {
                        adaptor.addChild(root_0, stream_condFinal.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:7: 
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
                    // 282:7: -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NULL ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:10: ^( CONDTEST ^( LIT TRUE ) ^( LIT NULL ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:21: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:33: ^( LIT NULL )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(NULL, "NULL"));

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
    // $ANTLR end "condFinalOrEmpty"

    public static class condTestExprs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condTestExprs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:1: condTestExprs : condTest ( BAR_BAR condTest )* condFinal -> ^( CONDLIST ( condTest )* condFinal ) ;
    public final EulangParser.condTestExprs_return condTestExprs() throws RecognitionException {
        EulangParser.condTestExprs_return retval = new EulangParser.condTestExprs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR_BAR210=null;
        EulangParser.condTest_return condTest209 = null;

        EulangParser.condTest_return condTest211 = null;

        EulangParser.condFinal_return condFinal212 = null;


        CommonTree BAR_BAR210_tree=null;
        RewriteRuleTokenStream stream_BAR_BAR=new RewriteRuleTokenStream(adaptor,"token BAR_BAR");
        RewriteRuleSubtreeStream stream_condFinal=new RewriteRuleSubtreeStream(adaptor,"rule condFinal");
        RewriteRuleSubtreeStream stream_condTest=new RewriteRuleSubtreeStream(adaptor,"rule condTest");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:15: ( condTest ( BAR_BAR condTest )* condFinal -> ^( CONDLIST ( condTest )* condFinal ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:17: condTest ( BAR_BAR condTest )* condFinal
            {
            pushFollow(FOLLOW_condTest_in_condTestExprs2791);
            condTest209=condTest();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condTest.add(condTest209.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:26: ( BAR_BAR condTest )*
            loop58:
            do {
                int alt58=2;
                int LA58_0 = input.LA(1);

                if ( (LA58_0==BAR_BAR) ) {
                    alt58=1;
                }


                switch (alt58) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:27: BAR_BAR condTest
            	    {
            	    BAR_BAR210=(Token)match(input,BAR_BAR,FOLLOW_BAR_BAR_in_condTestExprs2794); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR_BAR.add(BAR_BAR210);

            	    pushFollow(FOLLOW_condTest_in_condTestExprs2796);
            	    condTest211=condTest();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_condTest.add(condTest211.getTree());

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);

            pushFollow(FOLLOW_condFinal_in_condTestExprs2800);
            condFinal212=condFinal();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condFinal.add(condFinal212.getTree());


            // AST REWRITE
            // elements: condTest, condFinal
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 285:56: -> ^( CONDLIST ( condTest )* condFinal )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:59: ^( CONDLIST ( condTest )* condFinal )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDLIST, "CONDLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:70: ( condTest )*
                while ( stream_condTest.hasNext() ) {
                    adaptor.addChild(root_1, stream_condTest.nextTree());

                }
                stream_condTest.reset();
                adaptor.addChild(root_1, stream_condFinal.nextTree());

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
    // $ANTLR end "condTestExprs"

    public static class cond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:1: cond : ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION214=null;
        Token COLON215=null;
        EulangParser.logor_return t = null;

        EulangParser.logor_return f = null;

        EulangParser.logor_return logor213 = null;


        CommonTree QUESTION214_tree=null;
        CommonTree COLON215_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logor=new RewriteRuleSubtreeStream(adaptor,"rule logor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:5: ( ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:10: ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:10: ( logor -> logor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:12: logor
            {
            pushFollow(FOLLOW_logor_in_cond2830);
            logor213=logor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logor.add(logor213.getTree());


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
            // 288:19: -> logor
            {
                adaptor.addChild(root_0, stream_logor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:289:7: ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==QUESTION) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:289:9: QUESTION t= logor COLON f= logor
            	    {
            	    QUESTION214=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond2847); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION214);

            	    pushFollow(FOLLOW_logor_in_cond2851);
            	    t=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(t.getTree());
            	    COLON215=(Token)match(input,COLON,FOLLOW_COLON_in_cond2853); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON215);

            	    pushFollow(FOLLOW_logor_in_cond2857);
            	    f=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(f.getTree());


            	    // AST REWRITE
            	    // elements: f, cond, t
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
            	    // 289:40: -> ^( COND $cond $t $f)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:289:43: ^( COND $cond $t $f)
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
            	    break loop59;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:1: logor : ( logand -> logand ) ( COMPOR r= logand -> ^( COMPOR $logor $r) )* ;
    public final EulangParser.logor_return logor() throws RecognitionException {
        EulangParser.logor_return retval = new EulangParser.logor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPOR217=null;
        EulangParser.logand_return r = null;

        EulangParser.logand_return logand216 = null;


        CommonTree COMPOR217_tree=null;
        RewriteRuleTokenStream stream_COMPOR=new RewriteRuleTokenStream(adaptor,"token COMPOR");
        RewriteRuleSubtreeStream stream_logand=new RewriteRuleSubtreeStream(adaptor,"rule logand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:7: ( ( logand -> logand ) ( COMPOR r= logand -> ^( COMPOR $logor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:9: ( logand -> logand ) ( COMPOR r= logand -> ^( COMPOR $logor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:9: ( logand -> logand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:11: logand
            {
            pushFollow(FOLLOW_logand_in_logor2887);
            logand216=logand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logand.add(logand216.getTree());


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
            // 292:19: -> logand
            {
                adaptor.addChild(root_0, stream_logand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:7: ( COMPOR r= logand -> ^( COMPOR $logor $r) )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( (LA60_0==COMPOR) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:9: COMPOR r= logand
            	    {
            	    COMPOR217=(Token)match(input,COMPOR,FOLLOW_COMPOR_in_logor2904); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPOR.add(COMPOR217);

            	    pushFollow(FOLLOW_logand_in_logor2908);
            	    r=logand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: logor, r, COMPOR
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
            	    // 293:25: -> ^( COMPOR $logor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:28: ^( COMPOR $logor $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPOR.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop60;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:1: logand : ( comp -> comp ) ( COMPAND r= comp -> ^( COMPAND $logand $r) )* ;
    public final EulangParser.logand_return logand() throws RecognitionException {
        EulangParser.logand_return retval = new EulangParser.logand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPAND219=null;
        EulangParser.comp_return r = null;

        EulangParser.comp_return comp218 = null;


        CommonTree COMPAND219_tree=null;
        RewriteRuleTokenStream stream_COMPAND=new RewriteRuleTokenStream(adaptor,"token COMPAND");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:8: ( ( comp -> comp ) ( COMPAND r= comp -> ^( COMPAND $logand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:10: ( comp -> comp ) ( COMPAND r= comp -> ^( COMPAND $logand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:10: ( comp -> comp )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:12: comp
            {
            pushFollow(FOLLOW_comp_in_logand2939);
            comp218=comp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_comp.add(comp218.getTree());


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
            // 295:17: -> comp
            {
                adaptor.addChild(root_0, stream_comp.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:7: ( COMPAND r= comp -> ^( COMPAND $logand $r) )*
            loop61:
            do {
                int alt61=2;
                int LA61_0 = input.LA(1);

                if ( (LA61_0==COMPAND) ) {
                    alt61=1;
                }


                switch (alt61) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:9: COMPAND r= comp
            	    {
            	    COMPAND219=(Token)match(input,COMPAND,FOLLOW_COMPAND_in_logand2955); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPAND.add(COMPAND219);

            	    pushFollow(FOLLOW_comp_in_logand2959);
            	    r=comp();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_comp.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPAND, r, logand
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
            	    // 296:24: -> ^( COMPAND $logand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:27: ^( COMPAND $logand $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPAND.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop61;
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

    public static class comp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:1: comp : ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ221=null;
        Token COMPNE222=null;
        Token COMPLE223=null;
        Token COMPGE224=null;
        Token LESS225=null;
        Token GREATER226=null;
        EulangParser.bitor_return r = null;

        EulangParser.bitor_return bitor220 = null;


        CommonTree COMPEQ221_tree=null;
        CommonTree COMPNE222_tree=null;
        CommonTree COMPLE223_tree=null;
        CommonTree COMPGE224_tree=null;
        CommonTree LESS225_tree=null;
        CommonTree GREATER226_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_bitor=new RewriteRuleSubtreeStream(adaptor,"rule bitor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:5: ( ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:9: ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:9: ( bitor -> bitor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:11: bitor
            {
            pushFollow(FOLLOW_bitor_in_comp3009);
            bitor220=bitor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitor.add(bitor220.getTree());


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
            // 301:24: -> bitor
            {
                adaptor.addChild(root_0, stream_bitor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:7: ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            loop62:
            do {
                int alt62=7;
                switch ( input.LA(1) ) {
                case COMPEQ:
                    {
                    alt62=1;
                    }
                    break;
                case COMPNE:
                    {
                    alt62=2;
                    }
                    break;
                case COMPLE:
                    {
                    alt62=3;
                    }
                    break;
                case COMPGE:
                    {
                    alt62=4;
                    }
                    break;
                case LESS:
                    {
                    alt62=5;
                    }
                    break;
                case GREATER:
                    {
                    alt62=6;
                    }
                    break;

                }

                switch (alt62) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:9: COMPEQ r= bitor
            	    {
            	    COMPEQ221=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_comp3042); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ221);

            	    pushFollow(FOLLOW_bitor_in_comp3046);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPEQ, r, comp
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
            	    // 302:24: -> ^( COMPEQ $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:27: ^( COMPEQ $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:9: COMPNE r= bitor
            	    {
            	    COMPNE222=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_comp3068); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE222);

            	    pushFollow(FOLLOW_bitor_in_comp3072);
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
            	    // 303:24: -> ^( COMPNE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:27: ^( COMPNE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:304:9: COMPLE r= bitor
            	    {
            	    COMPLE223=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp3094); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE223);

            	    pushFollow(FOLLOW_bitor_in_comp3098);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, COMPLE, r
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
            	    // 304:27: -> ^( COMPLE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:304:30: ^( COMPLE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:305:9: COMPGE r= bitor
            	    {
            	    COMPGE224=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp3123); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE224);

            	    pushFollow(FOLLOW_bitor_in_comp3127);
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
            	    // 305:27: -> ^( COMPGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:305:30: ^( COMPGE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:9: LESS r= bitor
            	    {
            	    LESS225=(Token)match(input,LESS,FOLLOW_LESS_in_comp3152); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS225);

            	    pushFollow(FOLLOW_bitor_in_comp3156);
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
            	    // 306:26: -> ^( LESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:29: ^( LESS $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:9: GREATER r= bitor
            	    {
            	    GREATER226=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp3182); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER226);

            	    pushFollow(FOLLOW_bitor_in_comp3186);
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
            	    // 307:28: -> ^( GREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:31: ^( GREATER $comp $r)
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
            	    break loop62;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:1: bitor : ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* ;
    public final EulangParser.bitor_return bitor() throws RecognitionException {
        EulangParser.bitor_return retval = new EulangParser.bitor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR228=null;
        EulangParser.bitxor_return r = null;

        EulangParser.bitxor_return bitxor227 = null;


        CommonTree BAR228_tree=null;
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_bitxor=new RewriteRuleSubtreeStream(adaptor,"rule bitxor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:6: ( ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:8: ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:8: ( bitxor -> bitxor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:10: bitxor
            {
            pushFollow(FOLLOW_bitxor_in_bitor3236);
            bitxor227=bitxor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitxor.add(bitxor227.getTree());


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
            // 312:22: -> bitxor
            {
                adaptor.addChild(root_0, stream_bitxor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:7: ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            loop63:
            do {
                int alt63=2;
                int LA63_0 = input.LA(1);

                if ( (LA63_0==BAR) ) {
                    alt63=1;
                }


                switch (alt63) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:9: BAR r= bitxor
            	    {
            	    BAR228=(Token)match(input,BAR,FOLLOW_BAR_in_bitor3264); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR228);

            	    pushFollow(FOLLOW_bitxor_in_bitor3268);
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
            	    // 313:23: -> ^( BITOR $bitor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:26: ^( BITOR $bitor $r)
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
            	    break loop63;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:315:1: bitxor : ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* ;
    public final EulangParser.bitxor_return bitxor() throws RecognitionException {
        EulangParser.bitxor_return retval = new EulangParser.bitxor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CARET230=null;
        EulangParser.bitand_return r = null;

        EulangParser.bitand_return bitand229 = null;


        CommonTree CARET230_tree=null;
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_bitand=new RewriteRuleSubtreeStream(adaptor,"rule bitand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:315:7: ( ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:315:9: ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:315:9: ( bitand -> bitand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:315:11: bitand
            {
            pushFollow(FOLLOW_bitand_in_bitxor3294);
            bitand229=bitand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitand.add(bitand229.getTree());


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
            // 315:23: -> bitand
            {
                adaptor.addChild(root_0, stream_bitand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:7: ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            loop64:
            do {
                int alt64=2;
                int LA64_0 = input.LA(1);

                if ( (LA64_0==CARET) ) {
                    alt64=1;
                }


                switch (alt64) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:9: CARET r= bitand
            	    {
            	    CARET230=(Token)match(input,CARET,FOLLOW_CARET_in_bitxor3322); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET230);

            	    pushFollow(FOLLOW_bitand_in_bitxor3326);
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
            	    // 316:25: -> ^( BITXOR $bitxor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:28: ^( BITXOR $bitxor $r)
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
            	    break loop64;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:1: bitand : ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* ;
    public final EulangParser.bitand_return bitand() throws RecognitionException {
        EulangParser.bitand_return retval = new EulangParser.bitand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP232=null;
        EulangParser.shift_return r = null;

        EulangParser.shift_return shift231 = null;


        CommonTree AMP232_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:7: ( ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:9: ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:9: ( shift -> shift )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:11: shift
            {
            pushFollow(FOLLOW_shift_in_bitand3351);
            shift231=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(shift231.getTree());


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
            // 318:22: -> shift
            {
                adaptor.addChild(root_0, stream_shift.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:7: ( AMP r= shift -> ^( BITAND $bitand $r) )*
            loop65:
            do {
                int alt65=2;
                int LA65_0 = input.LA(1);

                if ( (LA65_0==AMP) ) {
                    alt65=1;
                }


                switch (alt65) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:9: AMP r= shift
            	    {
            	    AMP232=(Token)match(input,AMP,FOLLOW_AMP_in_bitand3379); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP232);

            	    pushFollow(FOLLOW_shift_in_bitand3383);
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
            	    // 319:22: -> ^( BITAND $bitand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:25: ^( BITAND $bitand $r)
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
    // $ANTLR end "bitand"

    public static class shift_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:1: shift : ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT234=null;
        Token RSHIFT235=null;
        Token URSHIFT236=null;
        EulangParser.factor_return r = null;

        EulangParser.factor_return factor233 = null;


        CommonTree LSHIFT234_tree=null;
        CommonTree RSHIFT235_tree=null;
        CommonTree URSHIFT236_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:6: ( ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:9: ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:9: ( factor -> factor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:11: factor
            {
            pushFollow(FOLLOW_factor_in_shift3410);
            factor233=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(factor233.getTree());


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
            // 322:25: -> factor
            {
                adaptor.addChild(root_0, stream_factor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:7: ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            loop66:
            do {
                int alt66=4;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt66=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt66=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt66=3;
                    }
                    break;

                }

                switch (alt66) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:11: LSHIFT r= factor
            	    {
            	    LSHIFT234=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift3444); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT234);

            	    pushFollow(FOLLOW_factor_in_shift3448);
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
            	    // 323:29: -> ^( LSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:32: ^( LSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:11: RSHIFT r= factor
            	    {
            	    RSHIFT235=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift3477); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT235);

            	    pushFollow(FOLLOW_factor_in_shift3481);
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
            	    // 324:29: -> ^( RSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:32: ^( RSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:11: URSHIFT r= factor
            	    {
            	    URSHIFT236=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift3509); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT236);

            	    pushFollow(FOLLOW_factor_in_shift3513);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: URSHIFT, r, shift
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
            	    // 325:30: -> ^( URSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:33: ^( URSHIFT $shift $r)
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
    // $ANTLR end "shift"

    public static class factor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "factor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:1: factor : ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS238=null;
        Token MINUS239=null;
        EulangParser.term_return r = null;

        EulangParser.term_return term237 = null;


        CommonTree PLUS238_tree=null;
        CommonTree MINUS239_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:5: ( ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:7: ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:7: ( term -> term )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:9: term
            {
            pushFollow(FOLLOW_term_in_factor3555);
            term237=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(term237.getTree());


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
            // 329:27: -> term
            {
                adaptor.addChild(root_0, stream_term.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:9: ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            loop67:
            do {
                int alt67=3;
                int LA67_0 = input.LA(1);

                if ( (LA67_0==PLUS) ) {
                    alt67=1;
                }
                else if ( (LA67_0==MINUS) && (synpred3_Eulang())) {
                    alt67=2;
                }


                switch (alt67) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:13: PLUS r= term
            	    {
            	    PLUS238=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor3588); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS238);

            	    pushFollow(FOLLOW_term_in_factor3592);
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
            	    // 330:33: -> ^( ADD $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:36: ^( ADD $factor $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:12: ( MINUS term )=> MINUS r= term
            	    {
            	    MINUS239=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor3634); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS239);

            	    pushFollow(FOLLOW_term_in_factor3638);
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
            	    // 331:49: -> ^( SUB $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:52: ^( SUB $factor $r)
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
    // $ANTLR end "factor"

    public static class term_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:1: term : ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR241=null;
        Token SLASH242=null;
        Token BACKSLASH243=null;
        Token PERCENT244=null;
        Token UMOD245=null;
        EulangParser.unary_return r = null;

        EulangParser.unary_return unary240 = null;


        CommonTree STAR241_tree=null;
        CommonTree SLASH242_tree=null;
        CommonTree BACKSLASH243_tree=null;
        CommonTree PERCENT244_tree=null;
        CommonTree UMOD245_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:6: ( ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:8: ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:8: ( unary -> unary )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:10: unary
            {
            pushFollow(FOLLOW_unary_in_term3683);
            unary240=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(unary240.getTree());


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
            // 335:33: -> unary
            {
                adaptor.addChild(root_0, stream_unary.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:9: ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            loop68:
            do {
                int alt68=6;
                int LA68_0 = input.LA(1);

                if ( (LA68_0==STAR) && (synpred4_Eulang())) {
                    alt68=1;
                }
                else if ( (LA68_0==SLASH) ) {
                    alt68=2;
                }
                else if ( (LA68_0==BACKSLASH) ) {
                    alt68=3;
                }
                else if ( (LA68_0==PERCENT) ) {
                    alt68=4;
                }
                else if ( (LA68_0==UMOD) ) {
                    alt68=5;
                }


                switch (alt68) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:11: ( STAR unary )=> STAR r= unary
            	    {
            	    STAR241=(Token)match(input,STAR,FOLLOW_STAR_in_term3727); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR241);

            	    pushFollow(FOLLOW_unary_in_term3731);
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
            	    // 336:52: -> ^( MUL $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:55: ^( MUL $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:11: SLASH r= unary
            	    {
            	    SLASH242=(Token)match(input,SLASH,FOLLOW_SLASH_in_term3767); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH242);

            	    pushFollow(FOLLOW_unary_in_term3771);
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
            	    // 337:36: -> ^( DIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:39: ^( DIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:338:11: BACKSLASH r= unary
            	    {
            	    BACKSLASH243=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_term3806); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BACKSLASH.add(BACKSLASH243);

            	    pushFollow(FOLLOW_unary_in_term3810);
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
            	    // 338:40: -> ^( UDIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:338:43: ^( UDIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:339:11: PERCENT r= unary
            	    {
            	    PERCENT244=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_term3845); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT244);

            	    pushFollow(FOLLOW_unary_in_term3849);
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
            	    // 339:38: -> ^( MOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:339:41: ^( MOD $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:340:11: UMOD r= unary
            	    {
            	    UMOD245=(Token)match(input,UMOD,FOLLOW_UMOD_in_term3884); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UMOD.add(UMOD245);

            	    pushFollow(FOLLOW_unary_in_term3888);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r, UMOD
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
            	    // 340:35: -> ^( UMOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:340:38: ^( UMOD $term $r)
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
            	    break loop68;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:1: unary : ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | EXCL u= unary -> ^( NOT $u) | TILDE u= unary -> ^( INV $u) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS247=null;
        Token EXCL248=null;
        Token TILDE249=null;
        EulangParser.unary_return u = null;

        EulangParser.atom_return atom246 = null;


        CommonTree MINUS247_tree=null;
        CommonTree EXCL248_tree=null;
        CommonTree TILDE249_tree=null;
        RewriteRuleTokenStream stream_EXCL=new RewriteRuleTokenStream(adaptor,"token EXCL");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:6: ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | EXCL u= unary -> ^( NOT $u) | TILDE u= unary -> ^( INV $u) )
            int alt69=4;
            switch ( input.LA(1) ) {
            case CODE:
            case MACRO:
            case ID:
            case COLON:
            case LPAREN:
            case NULL:
            case STAR:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case INVOKE:
            case RECURSE:
            case COLONS:
                {
                alt69=1;
                }
                break;
            case MINUS:
                {
                alt69=2;
                }
                break;
            case EXCL:
                {
                alt69=3;
                }
                break;
            case TILDE:
                {
                alt69=4;
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:11: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:11: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:13: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary3965);
                    atom246=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom246.getTree());


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
                    // 345:25: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:9: MINUS u= unary
                    {
                    MINUS247=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary3996); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS247);

                    pushFollow(FOLLOW_unary_in_unary4000);
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
                    // 346:23: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:26: ^( NEG $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:347:9: EXCL u= unary
                    {
                    EXCL248=(Token)match(input,EXCL,FOLLOW_EXCL_in_unary4020); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EXCL.add(EXCL248);

                    pushFollow(FOLLOW_unary_in_unary4024);
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
                    // 347:26: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:347:29: ^( NOT $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:9: TILDE u= unary
                    {
                    TILDE249=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary4048); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE249);

                    pushFollow(FOLLOW_unary_in_unary4052);
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
                    // 348:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:30: ^( INV $u)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:350:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NULL -> ^( LIT NULL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | INVOKE -> ^( INVOKE ) | RECURSE LPAREN arglist RPAREN -> ^( RECURSE arglist ) | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER250=null;
        Token FALSE251=null;
        Token TRUE252=null;
        Token CHAR_LITERAL253=null;
        Token STRING_LITERAL254=null;
        Token NULL255=null;
        Token STAR256=null;
        Token INVOKE258=null;
        Token RECURSE259=null;
        Token LPAREN260=null;
        Token RPAREN262=null;
        Token LPAREN265=null;
        Token RPAREN267=null;
        EulangParser.funcCall_return f = null;

        EulangParser.funcCall_return funcCall257 = null;

        EulangParser.arglist_return arglist261 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef263 = null;

        EulangParser.tuple_return tuple264 = null;

        EulangParser.assignExpr_return assignExpr266 = null;

        EulangParser.code_return code268 = null;

        EulangParser.macro_return macro269 = null;


        CommonTree NUMBER250_tree=null;
        CommonTree FALSE251_tree=null;
        CommonTree TRUE252_tree=null;
        CommonTree CHAR_LITERAL253_tree=null;
        CommonTree STRING_LITERAL254_tree=null;
        CommonTree NULL255_tree=null;
        CommonTree STAR256_tree=null;
        CommonTree INVOKE258_tree=null;
        CommonTree RECURSE259_tree=null;
        CommonTree LPAREN260_tree=null;
        CommonTree RPAREN262_tree=null;
        CommonTree LPAREN265_tree=null;
        CommonTree RPAREN267_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_RECURSE=new RewriteRuleTokenStream(adaptor,"token RECURSE");
        RewriteRuleTokenStream stream_INVOKE=new RewriteRuleTokenStream(adaptor,"token INVOKE");
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NULL=new RewriteRuleTokenStream(adaptor,"token NULL");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_macro=new RewriteRuleSubtreeStream(adaptor,"rule macro");
        RewriteRuleSubtreeStream stream_tuple=new RewriteRuleSubtreeStream(adaptor,"rule tuple");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_code=new RewriteRuleSubtreeStream(adaptor,"rule code");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_funcCall=new RewriteRuleSubtreeStream(adaptor,"rule funcCall");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:350:6: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NULL -> ^( LIT NULL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | INVOKE -> ^( INVOKE ) | RECURSE LPAREN arglist RPAREN -> ^( RECURSE arglist ) | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro )
            int alt70=15;
            alt70 = dfa70.predict(input);
            switch (alt70) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:7: NUMBER
                    {
                    NUMBER250=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom4080); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER250);



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
                    // 351:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:42: ^( LIT NUMBER )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:9: FALSE
                    {
                    FALSE251=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom4123); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE251);



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
                    // 352:39: -> ^( LIT FALSE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:42: ^( LIT FALSE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:9: TRUE
                    {
                    TRUE252=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom4165); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE252);



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
                    // 353:39: -> ^( LIT TRUE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:42: ^( LIT TRUE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL253=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom4208); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL253);



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
                    // 354:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:42: ^( LIT CHAR_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:355:9: STRING_LITERAL
                    {
                    STRING_LITERAL254=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom4243); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL254);



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
                    // 355:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:355:42: ^( LIT STRING_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:9: NULL
                    {
                    NULL255=(Token)match(input,NULL,FOLLOW_NULL_in_atom4276); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NULL.add(NULL255);



                    // AST REWRITE
                    // elements: NULL
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 356:39: -> ^( LIT NULL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:356:42: ^( LIT NULL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_1);

                        adaptor.addChild(root_1, stream_NULL.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:9: ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall
                    {
                    STAR256=(Token)match(input,STAR,FOLLOW_STAR_in_atom4330); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STAR.add(STAR256);

                    pushFollow(FOLLOW_funcCall_in_atom4334);
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
                    // 357:57: -> ^( INLINE $f)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:60: ^( INLINE $f)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:9: ( idOrScopeRef LPAREN )=> funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom4363);
                    funcCall257=funcCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_funcCall.add(funcCall257.getTree());


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
                    // 358:46: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:9: INVOKE
                    {
                    INVOKE258=(Token)match(input,INVOKE,FOLLOW_INVOKE_in_atom4379); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_INVOKE.add(INVOKE258);



                    // AST REWRITE
                    // elements: INVOKE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 359:39: -> ^( INVOKE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:42: ^( INVOKE )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_INVOKE.nextNode(), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:9: RECURSE LPAREN arglist RPAREN
                    {
                    RECURSE259=(Token)match(input,RECURSE,FOLLOW_RECURSE_in_atom4418); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RECURSE.add(RECURSE259);

                    LPAREN260=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom4420); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN260);

                    pushFollow(FOLLOW_arglist_in_atom4422);
                    arglist261=arglist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arglist.add(arglist261.getTree());
                    RPAREN262=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom4424); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN262);



                    // AST REWRITE
                    // elements: arglist, RECURSE
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 360:41: -> ^( RECURSE arglist )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:44: ^( RECURSE arglist )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_RECURSE.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_arglist.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:9: idOrScopeRef
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_atom4445);
                    idOrScopeRef263=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef263.getTree());


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
                    // 361:39: -> idOrScopeRef
                    {
                        adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 12 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_atom4484);
                    tuple264=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple264.getTree());


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
                    // 362:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 13 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN265=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom4523); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN265);

                    pushFollow(FOLLOW_assignExpr_in_atom4525);
                    assignExpr266=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr266.getTree());
                    RPAREN267=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom4527); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN267);



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
                    // 363:48: -> assignExpr
                    {
                        adaptor.addChild(root_0, stream_assignExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 14 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:9: code
                    {
                    pushFollow(FOLLOW_code_in_atom4555);
                    code268=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_code.add(code268.getTree());


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
                    // 364:40: -> code
                    {
                        adaptor.addChild(root_0, stream_code.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 15 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:9: macro
                    {
                    pushFollow(FOLLOW_macro_in_atom4598);
                    macro269=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_macro.add(macro269.getTree());


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
                    // 365:41: -> macro
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:1: idOrScopeRef : ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID270=null;
        Token PERIOD271=null;
        Token ID272=null;
        Token ID273=null;
        Token PERIOD274=null;
        Token ID275=null;
        EulangParser.colons_return c = null;


        CommonTree ID270_tree=null;
        CommonTree PERIOD271_tree=null;
        CommonTree ID272_tree=null;
        CommonTree ID273_tree=null;
        CommonTree PERIOD274_tree=null;
        CommonTree ID275_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:14: ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) )
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==ID) ) {
                alt73=1;
            }
            else if ( (LA73_0==COLON||LA73_0==COLONS) ) {
                alt73=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                throw nvae;
            }
            switch (alt73) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:16: ID ( PERIOD ID )*
                    {
                    ID270=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef4644); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID270);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:19: ( PERIOD ID )*
                    loop71:
                    do {
                        int alt71=2;
                        int LA71_0 = input.LA(1);

                        if ( (LA71_0==PERIOD) ) {
                            alt71=1;
                        }


                        switch (alt71) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:21: PERIOD ID
                    	    {
                    	    PERIOD271=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef4648); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD271);

                    	    ID272=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef4650); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID272);


                    	    }
                    	    break;

                    	default :
                    	    break loop71;
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
                    // 368:35: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:38: ^( IDREF ( ID )+ )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:9: c= colons ID ( PERIOD ID )*
                    {
                    pushFollow(FOLLOW_colons_in_idOrScopeRef4705);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID273=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef4707); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID273);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:21: ( PERIOD ID )*
                    loop72:
                    do {
                        int alt72=2;
                        int LA72_0 = input.LA(1);

                        if ( (LA72_0==PERIOD) ) {
                            alt72=1;
                        }


                        switch (alt72) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:23: PERIOD ID
                    	    {
                    	    PERIOD274=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef4711); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD274);

                    	    ID275=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef4713); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID275);


                    	    }
                    	    break;

                    	default :
                    	    break loop72;
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
                    // 373:37: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:40: ^( IDREF ( ID )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:1: colons : ( COLON | COLONS )+ ;
    public final EulangParser.colons_return colons() throws RecognitionException {
        EulangParser.colons_return retval = new EulangParser.colons_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set276=null;

        CommonTree set276_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:8: ( ( COLON | COLONS )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:10: ( COLON | COLONS )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:10: ( COLON | COLONS )+
            int cnt74=0;
            loop74:
            do {
                int alt74=2;
                int LA74_0 = input.LA(1);

                if ( (LA74_0==COLON||LA74_0==COLONS) ) {
                    alt74=1;
                }


                switch (alt74) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            	    {
            	    set276=(Token)input.LT(1);
            	    if ( input.LA(1)==COLON||input.LA(1)==COLONS ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set276));
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
            	    if ( cnt74 >= 1 ) break loop74;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(74, input);
                        throw eee;
                }
                cnt74++;
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
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:104:7: ( LPAREN ( RPAREN | ID ) )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:104:10: LPAREN ( RPAREN | ID )
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred1_Eulang525); if (state.failed) return ;
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
    // $ANTLR end synpred1_Eulang

    // $ANTLR start synpred3_Eulang
    public final void synpred3_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:12: ( MINUS term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:14: MINUS term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred3_Eulang3627); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred3_Eulang3629);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Eulang

    // $ANTLR start synpred4_Eulang
    public final void synpred4_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:11: ( STAR unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:13: STAR unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred4_Eulang3720); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred4_Eulang3722);
        unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Eulang

    // $ANTLR start synpred5_Eulang
    public final void synpred5_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:9: ( STAR idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:11: STAR idOrScopeRef LPAREN
        {
        match(input,STAR,FOLLOW_STAR_in_synpred5_Eulang4321); if (state.failed) return ;
        pushFollow(FOLLOW_idOrScopeRef_in_synpred5_Eulang4323);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred5_Eulang4325); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Eulang

    // $ANTLR start synpred6_Eulang
    public final void synpred6_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:9: ( idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:10: idOrScopeRef LPAREN
        {
        pushFollow(FOLLOW_idOrScopeRef_in_synpred6_Eulang4355);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred6_Eulang4357); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Eulang

    // $ANTLR start synpred7_Eulang
    public final void synpred7_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred7_Eulang4478);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Eulang

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


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA42 dfa42 = new DFA42(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA47 dfa47 = new DFA47(this);
    protected DFA70 dfa70 = new DFA70(this);
    static final String DFA4_eotS =
        "\22\uffff";
    static final String DFA4_eofS =
        "\22\uffff";
    static final String DFA4_minS =
        "\1\6\1\uffff\1\6\2\uffff\1\56\1\55\3\uffff\1\6\1\0\1\uffff\1\56"+
        "\1\55\1\0\2\uffff";
    static final String DFA4_maxS =
        "\1\142\1\uffff\1\142\2\uffff\1\141\1\71\3\uffff\1\142\1\0\1\uffff"+
        "\1\141\1\71\1\0\2\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\uffff\1\3\1\4\2\uffff\3\2\2\uffff\1\2\3\uffff\2\2";
    static final String DFA4_specialS =
        "\2\uffff\1\5\2\uffff\1\3\1\6\3\uffff\1\2\1\7\1\uffff\1\4\1\0\1\1"+
        "\2\uffff}>";
    static final String[] DFA4_transitionS = {
            "\2\4\45\uffff\1\4\2\uffff\1\4\1\uffff\1\3\2\uffff\1\1\3\uffff"+
            "\1\2\2\uffff\1\4\3\uffff\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff"+
            "\1\4",
            "",
            "\1\4\1\6\45\uffff\1\5\2\uffff\1\4\10\uffff\1\4\1\10\1\7\1\4"+
            "\3\uffff\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\4",
            "",
            "",
            "\1\4\1\uffff\1\11\3\uffff\1\12\4\uffff\1\4\1\13\1\7\1\uffff"+
            "\2\4\5\uffff\24\4\11\uffff\1\4",
            "\1\14\7\uffff\1\4\3\uffff\1\4",
            "",
            "",
            "",
            "\1\4\1\16\45\uffff\1\15\2\uffff\1\4\10\uffff\1\4\1\10\1\7\1"+
            "\4\3\uffff\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\4",
            "\1\uffff",
            "",
            "\1\4\1\uffff\1\20\3\uffff\1\12\4\uffff\1\4\1\17\1\7\1\uffff"+
            "\2\4\5\uffff\24\4\11\uffff\1\4",
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
                        int LA4_14 = input.LA(1);

                         
                        int index4_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_14==ID) && (synpred1_Eulang())) {s = 17;}

                        else if ( (LA4_14==LBRACE||LA4_14==LPAREN) ) {s = 4;}

                         
                        input.seek(index4_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_15 = input.LA(1);

                         
                        int index4_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index4_15);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA4_10 = input.LA(1);

                         
                        int index4_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_10==RETURNS) && (synpred1_Eulang())) {s = 7;}

                        else if ( (LA4_10==RPAREN) && (synpred1_Eulang())) {s = 8;}

                        else if ( (LA4_10==ID) ) {s = 13;}

                        else if ( (LA4_10==CODE||LA4_10==COLON||LA4_10==LPAREN||LA4_10==NULL||LA4_10==SELECT||(LA4_10>=MINUS && LA4_10<=STAR)||(LA4_10>=EXCL && LA4_10<=RECURSE)||LA4_10==COLONS) ) {s = 4;}

                        else if ( (LA4_10==MACRO) ) {s = 14;}

                         
                        input.seek(index4_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA4_5 = input.LA(1);

                         
                        int index4_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_5==EQUALS||LA4_5==LPAREN||(LA4_5>=QUESTION && LA4_5<=AMP)||(LA4_5>=COMPOR && LA4_5<=UMOD)||LA4_5==PERIOD) ) {s = 4;}

                        else if ( (LA4_5==COLON) && (synpred1_Eulang())) {s = 9;}

                        else if ( (LA4_5==COMMA) ) {s = 10;}

                        else if ( (LA4_5==RETURNS) && (synpred1_Eulang())) {s = 7;}

                        else if ( (LA4_5==RPAREN) ) {s = 11;}

                         
                        input.seek(index4_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA4_13 = input.LA(1);

                         
                        int index4_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_13==EQUALS||LA4_13==LPAREN||(LA4_13>=QUESTION && LA4_13<=AMP)||(LA4_13>=COMPOR && LA4_13<=UMOD)||LA4_13==PERIOD) ) {s = 4;}

                        else if ( (LA4_13==RPAREN) ) {s = 15;}

                        else if ( (LA4_13==COMMA) ) {s = 10;}

                        else if ( (LA4_13==COLON) && (synpred1_Eulang())) {s = 16;}

                        else if ( (LA4_13==RETURNS) && (synpred1_Eulang())) {s = 7;}

                         
                        input.seek(index4_13);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA4_2 = input.LA(1);

                         
                        int index4_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_2==ID) ) {s = 5;}

                        else if ( (LA4_2==CODE||LA4_2==COLON||LA4_2==LPAREN||LA4_2==NULL||LA4_2==SELECT||(LA4_2>=MINUS && LA4_2<=STAR)||(LA4_2>=EXCL && LA4_2<=RECURSE)||LA4_2==COLONS) ) {s = 4;}

                        else if ( (LA4_2==MACRO) ) {s = 6;}

                        else if ( (LA4_2==RETURNS) && (synpred1_Eulang())) {s = 7;}

                        else if ( (LA4_2==RPAREN) && (synpred1_Eulang())) {s = 8;}

                         
                        input.seek(index4_2);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA4_6 = input.LA(1);

                         
                        int index4_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_6==ID) && (synpred1_Eulang())) {s = 12;}

                        else if ( (LA4_6==LBRACE||LA4_6==LPAREN) ) {s = 4;}

                         
                        input.seek(index4_6);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA4_11 = input.LA(1);

                         
                        int index4_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index4_11);
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
        "\36\uffff";
    static final String DFA42_eofS =
        "\36\uffff";
    static final String DFA42_minS =
        "\1\6\1\56\1\6\1\55\4\uffff\1\55\1\uffff\1\56\1\55\2\56\1\55\1\6"+
        "\1\56\1\55\2\56\2\55\1\56\1\55\4\56\1\55\1\56";
    static final String DFA42_maxS =
        "\1\142\1\141\2\142\4\uffff\1\55\1\uffff\1\141\1\142\2\141\1\55\1"+
        "\142\1\141\1\55\2\141\1\142\1\55\1\141\1\55\1\127\3\141\1\55\1\141";
    static final String DFA42_acceptS =
        "\4\uffff\1\3\1\4\1\5\1\1\1\uffff\1\2\24\uffff";
    static final String DFA42_specialS =
        "\36\uffff}>";
    static final String[] DFA42_transitionS = {
            "\2\4\45\uffff\1\1\2\uffff\1\3\4\uffff\1\5\3\uffff\1\2\2\uffff"+
            "\1\4\2\uffff\1\6\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\3",
            "\1\11\1\4\2\7\4\uffff\1\4\2\uffff\1\4\3\uffff\2\4\5\uffff\24"+
            "\4\11\uffff\1\10",
            "\2\4\45\uffff\1\12\2\uffff\1\13\10\uffff\1\4\2\uffff\1\4\3"+
            "\uffff\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\13",
            "\1\14\2\uffff\1\3\61\uffff\1\3",
            "",
            "",
            "",
            "",
            "\1\15",
            "",
            "\1\4\5\uffff\1\17\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\16",
            "\1\20\2\uffff\1\13\61\uffff\1\13",
            "\1\11\1\4\6\uffff\1\4\2\uffff\1\4\3\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\21",
            "\1\11\1\4\6\uffff\1\4\2\uffff\1\4\3\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\10",
            "\1\22",
            "\2\4\45\uffff\1\23\2\uffff\1\24\10\uffff\1\4\2\uffff\1\4\3"+
            "\uffff\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\24",
            "\1\4\5\uffff\1\17\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\25",
            "\1\26",
            "\1\4\5\uffff\1\17\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\16",
            "\1\4\5\uffff\1\17\4\uffff\1\4\1\30\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\27",
            "\1\31\2\uffff\1\24\61\uffff\1\24",
            "\1\32",
            "\1\11\1\4\6\uffff\1\4\2\uffff\1\4\3\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\21",
            "\1\33",
            "\1\11\1\4\2\7\4\uffff\1\4\6\uffff\2\4\5\uffff\24\4",
            "\1\4\5\uffff\1\17\4\uffff\1\4\1\30\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\34",
            "\1\4\5\uffff\1\17\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\25",
            "\1\4\5\uffff\1\17\4\uffff\1\4\1\30\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\27",
            "\1\35",
            "\1\4\5\uffff\1\17\4\uffff\1\4\1\30\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\34"
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
            return "199:1: codeStmtExpr : ( varDecl -> varDecl | assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | gotoStmt -> gotoStmt );";
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
        "\1\71\1\61\1\142\2\uffff\1\141\1\142\1\55\1\142\3\141\1\142\2\55"+
        "\1\61\3\141\2\uffff\1\55\1\141";
    static final String DFA45_acceptS =
        "\3\uffff\1\1\1\3\16\uffff\1\4\1\2\2\uffff";
    static final String DFA45_specialS =
        "\27\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\1\13\uffff\1\2",
            "\1\4\1\3",
            "\1\5\2\uffff\1\6\61\uffff\1\6",
            "",
            "",
            "\1\10\54\uffff\1\7",
            "\1\11\2\uffff\1\6\61\uffff\1\6",
            "\1\12",
            "\1\13\2\uffff\1\14\61\uffff\1\14",
            "\1\10\54\uffff\1\15",
            "\1\10\54\uffff\1\7",
            "\1\10\5\uffff\1\17\46\uffff\1\16",
            "\1\20\2\uffff\1\14\61\uffff\1\14",
            "\1\21",
            "\1\22",
            "\1\23\1\24",
            "\1\10\5\uffff\1\17\46\uffff\1\25",
            "\1\10\54\uffff\1\15",
            "\1\10\5\uffff\1\17\46\uffff\1\16",
            "",
            "",
            "\1\26",
            "\1\10\5\uffff\1\17\46\uffff\1\25"
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
            return "209:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | idTuple COLON_EQUALS assignExpr -> ^( ALLOC idTuple TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) | idTuple COLON type ( EQUALS assignExpr )? -> ^( ALLOC idTuple type ( assignExpr )* ) );";
        }
    }
    static final String DFA47_eotS =
        "\34\uffff";
    static final String DFA47_eofS =
        "\34\uffff";
    static final String DFA47_minS =
        "\1\6\1\56\1\55\1\6\1\uffff\1\55\1\uffff\2\56\1\55\1\56\2\55\1\6"+
        "\4\56\3\55\4\56\1\uffff\1\55\1\56";
    static final String DFA47_maxS =
        "\1\142\1\141\2\142\1\uffff\1\55\1\uffff\2\141\1\142\1\141\2\55\1"+
        "\142\4\141\1\142\2\55\1\127\3\141\1\uffff\1\55\1\141";
    static final String DFA47_acceptS =
        "\4\uffff\1\3\1\uffff\1\1\22\uffff\1\2\2\uffff";
    static final String DFA47_specialS =
        "\34\uffff}>";
    static final String[] DFA47_transitionS = {
            "\2\4\45\uffff\1\1\2\uffff\1\2\10\uffff\1\3\2\uffff\1\4\3\uffff"+
            "\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\2",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\2\4\2"+
            "\uffff\1\4\1\uffff\25\4\11\uffff\1\5",
            "\1\7\2\uffff\1\2\61\uffff\1\2",
            "\2\4\45\uffff\1\10\2\uffff\1\11\10\uffff\1\4\2\uffff\1\4\3"+
            "\uffff\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\11",
            "",
            "\1\12",
            "",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\2\4\2"+
            "\uffff\1\4\1\uffff\25\4\11\uffff\1\13",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\14",
            "\1\16\2\uffff\1\11\61\uffff\1\11",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\2\4\2"+
            "\uffff\1\4\1\uffff\25\4\11\uffff\1\5",
            "\1\17",
            "\1\20",
            "\2\4\45\uffff\1\21\2\uffff\1\22\10\uffff\1\4\2\uffff\1\4\3"+
            "\uffff\1\4\21\uffff\2\4\4\uffff\11\4\1\uffff\1\22",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\23",
            "\1\6\1\4\3\uffff\2\4\1\uffff\1\4\2\uffff\3\4\1\uffff\2\4\2"+
            "\uffff\1\4\1\uffff\25\4\11\uffff\1\13",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\14",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\24",
            "\1\26\2\uffff\1\22\61\uffff\1\22",
            "\1\27",
            "\1\30",
            "\1\31\1\4\3\uffff\2\4\1\uffff\1\4\3\uffff\2\4\1\uffff\2\4\2"+
            "\uffff\1\4\1\uffff\25\4",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\32",
            "\1\4\5\uffff\1\15\4\uffff\2\4\2\uffff\2\4\5\uffff\24\4\11\uffff"+
            "\1\23",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\24",
            "",
            "\1\33",
            "\1\4\5\uffff\1\15\4\uffff\1\4\1\25\2\uffff\2\4\5\uffff\24\4"+
            "\11\uffff\1\32"
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
            return "222:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | idTuple EQUALS assignExpr -> ^( ASSIGN idTuple assignExpr ) | rhsExpr -> rhsExpr );";
        }
    }
    static final String DFA70_eotS =
        "\23\uffff";
    static final String DFA70_eofS =
        "\23\uffff";
    static final String DFA70_minS =
        "\1\6\7\uffff\2\0\2\uffff\1\0\6\uffff";
    static final String DFA70_maxS =
        "\1\142\7\uffff\2\0\2\uffff\1\0\6\uffff";
    static final String DFA70_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\2\uffff\1\11\1\12\1\uffff\1"+
        "\16\1\17\1\10\1\13\1\14\1\15";
    static final String DFA70_specialS =
        "\1\0\7\uffff\1\1\1\2\2\uffff\1\3\6\uffff}>";
    static final String[] DFA70_transitionS = {
            "\1\15\1\16\45\uffff\1\10\2\uffff\1\11\10\uffff\1\14\2\uffff"+
            "\1\6\26\uffff\1\7\6\uffff\1\1\1\2\1\3\1\4\1\5\1\12\1\13\1\uffff"+
            "\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA70_eot = DFA.unpackEncodedString(DFA70_eotS);
    static final short[] DFA70_eof = DFA.unpackEncodedString(DFA70_eofS);
    static final char[] DFA70_min = DFA.unpackEncodedStringToUnsignedChars(DFA70_minS);
    static final char[] DFA70_max = DFA.unpackEncodedStringToUnsignedChars(DFA70_maxS);
    static final short[] DFA70_accept = DFA.unpackEncodedString(DFA70_acceptS);
    static final short[] DFA70_special = DFA.unpackEncodedString(DFA70_specialS);
    static final short[][] DFA70_transition;

    static {
        int numStates = DFA70_transitionS.length;
        DFA70_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA70_transition[i] = DFA.unpackEncodedString(DFA70_transitionS[i]);
        }
    }

    class DFA70 extends DFA {

        public DFA70(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 70;
            this.eot = DFA70_eot;
            this.eof = DFA70_eof;
            this.min = DFA70_min;
            this.max = DFA70_max;
            this.accept = DFA70_accept;
            this.special = DFA70_special;
            this.transition = DFA70_transition;
        }
        public String getDescription() {
            return "350:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NULL -> ^( LIT NULL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | INVOKE -> ^( INVOKE ) | RECURSE LPAREN arglist RPAREN -> ^( RECURSE arglist ) | idOrScopeRef -> idOrScopeRef | ( tuple )=> tuple -> tuple | LPAREN assignExpr RPAREN -> assignExpr | code -> code | macro -> macro );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA70_0 = input.LA(1);

                         
                        int index70_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA70_0==NUMBER) ) {s = 1;}

                        else if ( (LA70_0==FALSE) ) {s = 2;}

                        else if ( (LA70_0==TRUE) ) {s = 3;}

                        else if ( (LA70_0==CHAR_LITERAL) ) {s = 4;}

                        else if ( (LA70_0==STRING_LITERAL) ) {s = 5;}

                        else if ( (LA70_0==NULL) ) {s = 6;}

                        else if ( (LA70_0==STAR) && (synpred5_Eulang())) {s = 7;}

                        else if ( (LA70_0==ID) ) {s = 8;}

                        else if ( (LA70_0==COLON||LA70_0==COLONS) ) {s = 9;}

                        else if ( (LA70_0==INVOKE) ) {s = 10;}

                        else if ( (LA70_0==RECURSE) ) {s = 11;}

                        else if ( (LA70_0==LPAREN) ) {s = 12;}

                        else if ( (LA70_0==CODE) ) {s = 13;}

                        else if ( (LA70_0==MACRO) ) {s = 14;}

                         
                        input.seek(index70_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA70_8 = input.LA(1);

                         
                        int index70_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Eulang()) ) {s = 15;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index70_8);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA70_9 = input.LA(1);

                         
                        int index70_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Eulang()) ) {s = 15;}

                        else if ( (true) ) {s = 16;}

                         
                        input.seek(index70_9);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA70_12 = input.LA(1);

                         
                        int index70_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 18;}

                         
                        input.seek(index70_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 70, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog304 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts335 = new BitSet(new long[]{0x12212000000000C2L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_ID_in_toplevelstat369 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat371 = new BitSet(new long[]{0x12252000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat373 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat398 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_toplevelstat400 = new BitSet(new long[]{0x0001200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_type_in_toplevelstat402 = new BitSet(new long[]{0x0000C00000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat405 = new BitSet(new long[]{0x12252000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat407 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat437 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelstat439 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat441 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat444 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat465 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstat501 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_toplevelvalue539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector573 = new BitSet(new long[]{0x12892000000000C0L,0x00000005FC080000L});
    public static final BitSet FOLLOW_selectors_in_selector575 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors603 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_selectors607 = new BitSet(new long[]{0x12812000000000C0L,0x00000005FC080000L});
    public static final BitSet FOLLOW_selectoritem_in_selectors609 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_selectors614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listCompr_in_selectoritem643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_selectoritem647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_selectoritem651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope662 = new BitSet(new long[]{0x12212000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope664 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope666 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr693 = new BitSet(new long[]{0x0081000000000000L});
    public static final BitSet FOLLOW_COLON_in_listCompr696 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FC080000L});
    public static final BitSet FOLLOW_listiterable_in_listCompr698 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn730 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_idlist_in_forIn732 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_IN_in_forIn734 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_list_in_forIn736 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist761 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_idlist764 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idlist766 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_code_in_listiterable795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_listiterable799 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_listiterable803 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list818 = new BitSet(new long[]{0x122D2000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_listitems_in_list820 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_list822 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems852 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems856 = new BitSet(new long[]{0x12252000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_listitem_in_listitems858 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems863 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_code907 = new BitSet(new long[]{0x0220000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_code911 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_optargdefs_in_code913 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_code915 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_code918 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_code924 = new BitSet(new long[]{0x92612000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_codestmtlist_in_code926 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_code928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_macro963 = new BitSet(new long[]{0x0220000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_macro967 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_optargdefs_in_macro969 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_macro971 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_macro974 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_macro980 = new BitSet(new long[]{0x92612000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_codestmtlist_in_macro982 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_macro984 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto1019 = new BitSet(new long[]{0x0C00200000000080L});
    public static final BitSet FOLLOW_argdefs_in_proto1021 = new BitSet(new long[]{0x0C00000000000000L});
    public static final BitSet FOLLOW_xreturns_in_proto1023 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_proto1026 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdef_in_argdefs1068 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1072 = new BitSet(new long[]{0x0000200000000080L});
    public static final BitSet FOLLOW_argdef_in_argdefs1074 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_argdef1122 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_argdef1125 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COLON_in_argdef1128 = new BitSet(new long[]{0x0001200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_type_in_argdef1130 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_xreturns1160 = new BitSet(new long[]{0x0001200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_type_in_xreturns1162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_xreturns1177 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_argtuple_in_xreturns1179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_xreturns1199 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_NULL_in_xreturns1201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_argtuple1231 = new BitSet(new long[]{0x2011200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_tupleargdefs_in_argtuple1233 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_argtuple1235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs1257 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_tupleargdefs1261 = new BitSet(new long[]{0x2011200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs1263 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_type_in_tupleargdef1308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_tupleargdef1321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_optargdef_in_optargdefs1378 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_optargdefs1382 = new BitSet(new long[]{0x0000200000000080L});
    public static final BitSet FOLLOW_optargdef_in_optargdefs1384 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_optargdefs1388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_optargdef1432 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COLON_in_optargdef1435 = new BitSet(new long[]{0x0001200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_type_in_optargdef1437 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_optargdef1461 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_optargdef1463 = new BitSet(new long[]{0x0001400000000002L});
    public static final BitSet FOLLOW_COLON_in_optargdef1466 = new BitSet(new long[]{0x0001200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_type_in_optargdef1468 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_optargdef1473 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_rhsExpr_in_optargdef1477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_type1515 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_AMP_in_type1530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_type1556 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_proto_in_type1558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1586 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist1589 = new BitSet(new long[]{0x9221A000000000C2L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1591 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_labelStmt_in_codeStmt1640 = new BitSet(new long[]{0x92212000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt1642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt1663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmtExpr1682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_codeStmtExpr1699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_codeStmtExpr1723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blockStmt_in_codeStmtExpr1747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_codeStmtExpr1776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1806 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1808 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_varDecl1838 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1840 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1870 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl1872 = new BitSet(new long[]{0x0001200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_type_in_varDecl1874 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1877 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_varDecl1903 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl1905 = new BitSet(new long[]{0x0001200000000040L,0x0000000400000000L});
    public static final BitSet FOLLOW_type_in_varDecl1907 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1910 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignStmt1950 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt1952 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt1954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignStmt1979 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt1981 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt1983 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignExpr2026 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr2028 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr2030 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignExpr2055 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr2057 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr2059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr2091 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_labelStmt2135 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_labelStmt2137 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_labelStmt2139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_gotoStmt2176 = new BitSet(new long[]{0x0001200000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_gotoStmt2178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_blockStmt2213 = new BitSet(new long[]{0x92612000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_codestmtlist_in_blockStmt2215 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_blockStmt2217 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tuple2240 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_tupleEntries_in_tuple2242 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_tuple2244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries2272 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_tupleEntries2275 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries2277 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_idTuple2296 = new BitSet(new long[]{0x0001200000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_idTupleEntries_in_idTuple2298 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_idTuple2300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries2328 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_COMMA_in_idTupleEntries2331 = new BitSet(new long[]{0x0001200000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries2333 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_condStar_in_rhsExpr2354 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_funcCall2400 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall2402 = new BitSet(new long[]{0x96212000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_arglist_in_funcCall2404 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall2406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist2437 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist2441 = new BitSet(new long[]{0x92212000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_arg_in_arglist2443 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist2447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg2496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_arg2529 = new BitSet(new long[]{0x92612000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_codestmtlist_in_arg2531 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_arg2533 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_arg2557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_condStar2607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_condStar2618 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_LBRACKET_in_condStar2620 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_condTests_in_condStar2622 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_condStar2624 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_condStar2635 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_condTestExprs_in_condStar2637 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condTest_in_condTests2653 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000AL});
    public static final BitSet FOLLOW_BAR_BAR_in_condTests2656 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_condTest_in_condTests2658 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000AL});
    public static final BitSet FOLLOW_BAR_BAR_in_condTests2662 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_condFinalOrEmpty_in_condTests2665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_condTest2698 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_THEN_in_condTest2700 = new BitSet(new long[]{0x92212000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_arg_in_condTest2702 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELSE_in_condFinal2722 = new BitSet(new long[]{0x92212000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_arg_in_condFinal2724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condFinal_in_condFinalOrEmpty2750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condTest_in_condTestExprs2791 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000AL});
    public static final BitSet FOLLOW_BAR_BAR_in_condTestExprs2794 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_condTest_in_condTestExprs2796 = new BitSet(new long[]{0x0000000000000000L,0x000000000000000AL});
    public static final BitSet FOLLOW_condFinal_in_condTestExprs2800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logor_in_cond2830 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond2847 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_logor_in_cond2851 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_COLON_in_cond2853 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_logor_in_cond2857 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_logand_in_logor2887 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_COMPOR_in_logor2904 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_logand_in_logor2908 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_comp_in_logand2939 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_COMPAND_in_logand2955 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_comp_in_logand2959 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_bitor_in_comp3009 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000FC0L});
    public static final BitSet FOLLOW_COMPEQ_in_comp3042 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitor_in_comp3046 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000FC0L});
    public static final BitSet FOLLOW_COMPNE_in_comp3068 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitor_in_comp3072 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000FC0L});
    public static final BitSet FOLLOW_COMPLE_in_comp3094 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitor_in_comp3098 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000FC0L});
    public static final BitSet FOLLOW_COMPGE_in_comp3123 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitor_in_comp3127 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000FC0L});
    public static final BitSet FOLLOW_LESS_in_comp3152 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitor_in_comp3156 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000FC0L});
    public static final BitSet FOLLOW_GREATER_in_comp3182 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitor_in_comp3186 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000FC0L});
    public static final BitSet FOLLOW_bitxor_in_bitor3236 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_BAR_in_bitor3264 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitxor_in_bitor3268 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_bitand_in_bitxor3294 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_CARET_in_bitxor3322 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_bitand_in_bitxor3326 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_shift_in_bitand3351 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_AMP_in_bitand3379 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_shift_in_bitand3383 = new BitSet(new long[]{0x4000000000000002L});
    public static final BitSet FOLLOW_factor_in_shift3410 = new BitSet(new long[]{0x0000000000000002L,0x000000000001C000L});
    public static final BitSet FOLLOW_LSHIFT_in_shift3444 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_factor_in_shift3448 = new BitSet(new long[]{0x0000000000000002L,0x000000000001C000L});
    public static final BitSet FOLLOW_RSHIFT_in_shift3477 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_factor_in_shift3481 = new BitSet(new long[]{0x0000000000000002L,0x000000000001C000L});
    public static final BitSet FOLLOW_URSHIFT_in_shift3509 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_factor_in_shift3513 = new BitSet(new long[]{0x0000000000000002L,0x000000000001C000L});
    public static final BitSet FOLLOW_term_in_factor3555 = new BitSet(new long[]{0x0000000000000002L,0x0000000000060000L});
    public static final BitSet FOLLOW_PLUS_in_factor3588 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_term_in_factor3592 = new BitSet(new long[]{0x0000000000000002L,0x0000000000060000L});
    public static final BitSet FOLLOW_MINUS_in_factor3634 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_term_in_factor3638 = new BitSet(new long[]{0x0000000000000002L,0x0000000000060000L});
    public static final BitSet FOLLOW_unary_in_term3683 = new BitSet(new long[]{0x0000000000000002L,0x0000000000F80000L});
    public static final BitSet FOLLOW_STAR_in_term3727 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_term3731 = new BitSet(new long[]{0x0000000000000002L,0x0000000000F80000L});
    public static final BitSet FOLLOW_SLASH_in_term3767 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_term3771 = new BitSet(new long[]{0x0000000000000002L,0x0000000000F80000L});
    public static final BitSet FOLLOW_BACKSLASH_in_term3806 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_term3810 = new BitSet(new long[]{0x0000000000000002L,0x0000000000F80000L});
    public static final BitSet FOLLOW_PERCENT_in_term3845 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_term3849 = new BitSet(new long[]{0x0000000000000002L,0x0000000000F80000L});
    public static final BitSet FOLLOW_UMOD_in_term3884 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_term3888 = new BitSet(new long[]{0x0000000000000002L,0x0000000000F80000L});
    public static final BitSet FOLLOW_atom_in_unary3965 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_unary3996 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_unary4000 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCL_in_unary4020 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_unary4024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary4048 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_unary4052 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom4080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_atom4123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_atom4165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom4208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom4243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_atom4276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_atom4330 = new BitSet(new long[]{0x0001200000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_funcCall_in_atom4334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom4363 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INVOKE_in_atom4379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RECURSE_in_atom4418 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_atom4420 = new BitSet(new long[]{0x96212000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_arglist_in_atom4422 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_atom4424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_atom4445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_atom4484 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom4523 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0001L});
    public static final BitSet FOLLOW_assignExpr_in_atom4525 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_atom4527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_atom4555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_atom4598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef4644 = new BitSet(new long[]{0x0000000000000002L,0x0000000200000000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef4648 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef4650 = new BitSet(new long[]{0x0000000000000002L,0x0000000200000000L});
    public static final BitSet FOLLOW_colons_in_idOrScopeRef4705 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef4707 = new BitSet(new long[]{0x0000000000000002L,0x0000000200000000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef4711 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef4713 = new BitSet(new long[]{0x0000000000000002L,0x0000000200000000L});
    public static final BitSet FOLLOW_set_in_colons4744 = new BitSet(new long[]{0x0001000000000002L,0x0000000400000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred1_Eulang525 = new BitSet(new long[]{0x0400200000000000L});
    public static final BitSet FOLLOW_set_in_synpred1_Eulang527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred3_Eulang3627 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_term_in_synpred3_Eulang3629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred4_Eulang3720 = new BitSet(new long[]{0x12012000000000C0L,0x00000005FF0C0000L});
    public static final BitSet FOLLOW_unary_in_synpred4_Eulang3722 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred5_Eulang4321 = new BitSet(new long[]{0x0001200000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred5_Eulang4323 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred5_Eulang4325 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred6_Eulang4355 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred6_Eulang4357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred7_Eulang4478 = new BitSet(new long[]{0x0000000000000002L});

}