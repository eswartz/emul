// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-03-28 19:48:09

package org.ejs.eulang.parser;
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCOPE", "LIST_COMPREHENSION", "CODE", "MACRO", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "ALLOC", "ASSIGN", "DEFINE", "EXPR", "LIST", "TYPE", "STMTEXPR", "CALL", "INLINE", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NOT", "NEG", "INV", "LIT", "IDREF", "IDLIST", "LABEL", "GOTO", "BLOCK", "ID", "EQUALS", "SEMI", "COLON", "COLON_EQUALS", "LBRACKET", "RBRACKET", "COMMA", "LBRACE", "RBRACE", "FOR", "IN", "LPAREN", "RPAREN", "RETURNS", "AMP", "RETURN", "AT", "QUESTION", "COMPOR", "COMPAND", "COMPEQ", "COMPNE", "COMPLE", "COMPGE", "LESS", "GREATER", "BAR", "CARET", "LSHIFT", "RSHIFT", "URSHIFT", "PLUS", "MINUS", "STAR", "SLASH", "BACKSLASH", "PERCENT", "UMOD", "EXCL", "TILDE", "NUMBER", "FALSE", "TRUE", "CHAR_LITERAL", "STRING_LITERAL", "PERIOD", "COLONS", "LBRACE_LPAREN", "LBRACE_STAR", "LBRACE_STAR_LPAREN", "COLON_COLON_EQUALS", "HASH", "DATA", "IDSUFFIX", "LETTERLIKE", "DIGIT", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT"
    };
    public static final int STAR=75;
    public static final int MOD=31;
    public static final int ARGLIST=10;
    public static final int EQUALS=42;
    public static final int EXCL=80;
    public static final int NOT=32;
    public static final int EOF=-1;
    public static final int TYPE=18;
    public static final int CODE=6;
    public static final int LBRACKET=46;
    public static final int RPAREN=54;
    public static final int GREATER=67;
    public static final int STRING_LITERAL=86;
    public static final int COMPLE=64;
    public static final int CARET=69;
    public static final int RETURN=57;
    public static final int LESS=66;
    public static final int LBRACE_STAR_LPAREN=91;
    public static final int COMPAND=61;
    public static final int GOTO=39;
    public static final int RBRACE=50;
    public static final int STMTEXPR=19;
    public static final int UMOD=79;
    public static final int PERIOD=87;
    public static final int INV=34;
    public static final int LSHIFT=70;
    public static final int LIT=35;
    public static final int UDIV=30;
    public static final int NUMBER=82;
    public static final int LBRACE_LPAREN=89;
    public static final int LIST=17;
    public static final int MUL=28;
    public static final int ARGDEF=11;
    public static final int WS=99;
    public static final int BITOR=24;
    public static final int STMTLIST=8;
    public static final int ALLOC=13;
    public static final int IDLIST=37;
    public static final int INLINE=21;
    public static final int CALL=20;
    public static final int FALSE=83;
    public static final int BACKSLASH=77;
    public static final int AMP=56;
    public static final int LBRACE_STAR=90;
    public static final int LBRACE=49;
    public static final int MULTI_COMMENT=101;
    public static final int FOR=51;
    public static final int SUB=27;
    public static final int ID=41;
    public static final int DEFINE=15;
    public static final int BITAND=23;
    public static final int LPAREN=53;
    public static final int COLONS=88;
    public static final int COLON_COLON_EQUALS=92;
    public static final int AT=58;
    public static final int IDSUFFIX=95;
    public static final int EXPR=16;
    public static final int SLASH=76;
    public static final int IN=52;
    public static final int SCOPE=4;
    public static final int COMMA=48;
    public static final int BITXOR=25;
    public static final int TILDE=81;
    public static final int PLUS=73;
    public static final int SINGLE_COMMENT=100;
    public static final int DIGIT=97;
    public static final int RBRACKET=47;
    public static final int RSHIFT=71;
    public static final int RETURNS=55;
    public static final int ADD=26;
    public static final int COMPGE=65;
    public static final int PERCENT=78;
    public static final int LETTERLIKE=96;
    public static final int LIST_COMPREHENSION=5;
    public static final int COMPOR=60;
    public static final int HASH=93;
    public static final int MINUS=74;
    public static final int SEMI=43;
    public static final int TRUE=84;
    public static final int REF=12;
    public static final int COLON=44;
    public static final int COLON_EQUALS=45;
    public static final int NEWLINE=98;
    public static final int QUESTION=59;
    public static final int CHAR_LITERAL=85;
    public static final int LABEL=38;
    public static final int BLOCK=40;
    public static final int NEG=33;
    public static final int ASSIGN=14;
    public static final int URSHIFT=72;
    public static final int COMPEQ=62;
    public static final int IDREF=36;
    public static final int DIV=29;
    public static final int COND=22;
    public static final int MACRO=7;
    public static final int PROTO=9;
    public static final int COMPNE=63;
    public static final int DATA=94;
    public static final int BAR=68;

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:84:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:84:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:84:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog275);
            toplevelstmts1=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog277); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:87:1: toplevelstmts : ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstat_return toplevelstat3 = null;


        RewriteRuleSubtreeStream stream_toplevelstat=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:87:14: ( ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:87:16: ( toplevelstat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:87:16: ( toplevelstat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==CODE||LA1_0==ID||LA1_0==COLON||LA1_0==LBRACE||LA1_0==LPAREN||(LA1_0>=MINUS && LA1_0<=STAR)||(LA1_0>=EXCL && LA1_0<=STRING_LITERAL)||LA1_0==COLONS) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:87:16: toplevelstat
            	    {
            	    pushFollow(FOLLOW_toplevelstat_in_toplevelstmts306);
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
            // 87:35: -> ^( STMTLIST ( toplevelstat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:87:38: ^( STMTLIST ( toplevelstat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:87:49: ( toplevelstat )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:90:1: toplevelstat : ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) | ID COLON type ( EQUALS toplevelvalue )? SEMI -> ^( ALLOC ID type ( toplevelvalue )? ) | ID COLON_EQUALS rhsExpr SEMI -> ^( ALLOC ID TYPE rhsExpr ) | rhsExpr SEMI -> ^( EXPR rhsExpr ) | xscope );
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:90:13: ( ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) | ID COLON type ( EQUALS toplevelvalue )? SEMI -> ^( ALLOC ID type ( toplevelvalue )? ) | ID COLON_EQUALS rhsExpr SEMI -> ^( ALLOC ID TYPE rhsExpr ) | rhsExpr SEMI -> ^( EXPR rhsExpr ) | xscope )
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
                case AMP:
                case QUESTION:
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
            case COLON:
            case LPAREN:
            case MINUS:
            case STAR:
            case EXCL:
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:90:17: ID EQUALS toplevelvalue SEMI
                    {
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat340); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID4);

                    EQUALS5=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat342); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS5);

                    pushFollow(FOLLOW_toplevelvalue_in_toplevelstat344);
                    toplevelvalue6=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat350); if (state.failed) return retval; 
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
                    // 90:51: -> ^( DEFINE ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:90:54: ^( DEFINE ID toplevelvalue )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:91:7: ID COLON type ( EQUALS toplevelvalue )? SEMI
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat369); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID8);

                    COLON9=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelstat371); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON9);

                    pushFollow(FOLLOW_type_in_toplevelstat373);
                    type10=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type10.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:91:21: ( EQUALS toplevelvalue )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==EQUALS) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:91:22: EQUALS toplevelvalue
                            {
                            EQUALS11=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelstat376); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS11);

                            pushFollow(FOLLOW_toplevelvalue_in_toplevelstat378);
                            toplevelvalue12=toplevelvalue();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue12.getTree());

                            }
                            break;

                    }

                    SEMI13=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat386); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI13);



                    // AST REWRITE
                    // elements: ID, toplevelvalue, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 91:55: -> ^( ALLOC ID type ( toplevelvalue )? )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:91:58: ^( ALLOC ID type ( toplevelvalue )? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:91:74: ( toplevelvalue )?
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:92:7: ID COLON_EQUALS rhsExpr SEMI
                    {
                    ID14=(Token)match(input,ID,FOLLOW_ID_in_toplevelstat408); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID14);

                    COLON_EQUALS15=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelstat410); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS15);

                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat412);
                    rhsExpr16=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr16.getTree());
                    SEMI17=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat415); if (state.failed) return retval; 
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
                    // 92:38: -> ^( ALLOC ID TYPE rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:92:41: ^( ALLOC ID TYPE rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:7: rhsExpr SEMI
                    {
                    pushFollow(FOLLOW_rhsExpr_in_toplevelstat436);
                    rhsExpr18=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr18.getTree());
                    SEMI19=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat455); if (state.failed) return retval; 
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
                    // 93:38: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:93:41: ^( EXPR rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:94:7: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelstat472);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:1: toplevelvalue : ( xscope | macro | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr );
    public final EulangParser.toplevelvalue_return toplevelvalue() throws RecognitionException {
        EulangParser.toplevelvalue_return retval = new EulangParser.toplevelvalue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.xscope_return xscope21 = null;

        EulangParser.macro_return macro22 = null;

        EulangParser.proto_return proto23 = null;

        EulangParser.selector_return selector24 = null;

        EulangParser.rhsExpr_return rhsExpr25 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:15: ( xscope | macro | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr )
            int alt4=5;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:97:17: xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelvalue485);
                    xscope21=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope21.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:99:7: macro
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_macro_in_toplevelvalue498);
                    macro22=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro22.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:100:7: ( LPAREN ( RPAREN | ID ) )=> proto
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_proto_in_toplevelvalue523);
                    proto23=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, proto23.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:101:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_toplevelvalue536);
                    selector24=selector();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selector24.getTree());

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:102:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue544);
                    rhsExpr25=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr25.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:106:1: selector : LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET26=null;
        Token RBRACKET28=null;
        EulangParser.selectors_return selectors27 = null;


        CommonTree LBRACKET26_tree=null;
        CommonTree RBRACKET28_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectors=new RewriteRuleSubtreeStream(adaptor,"rule selectors");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:106:9: ( LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:106:11: LBRACKET selectors RBRACKET
            {
            LBRACKET26=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector557); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET26);

            pushFollow(FOLLOW_selectors_in_selector559);
            selectors27=selectors();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectors.add(selectors27.getTree());
            RBRACKET28=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector561); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET28);



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
            // 106:42: -> ^( LIST ( selectors )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:106:45: ^( LIST ( selectors )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:106:52: ( selectors )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:1: selectors : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? ;
    public final EulangParser.selectors_return selectors() throws RecognitionException {
        EulangParser.selectors_return retval = new EulangParser.selectors_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA30=null;
        Token COMMA32=null;
        EulangParser.selectoritem_return selectoritem29 = null;

        EulangParser.selectoritem_return selectoritem31 = null;


        CommonTree COMMA30_tree=null;
        CommonTree COMMA32_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:10: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=CODE && LA7_0<=MACRO)||LA7_0==FOR) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:13: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectors587);
                    selectoritem29=selectoritem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selectoritem29.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:26: ( COMMA selectoritem )*
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
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:28: COMMA selectoritem
                    	    {
                    	    COMMA30=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors591); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA30_tree = (CommonTree)adaptor.create(COMMA30);
                    	    adaptor.addChild(root_0, COMMA30_tree);
                    	    }
                    	    pushFollow(FOLLOW_selectoritem_in_selectors593);
                    	    selectoritem31=selectoritem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, selectoritem31.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:50: ( COMMA )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==COMMA) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:109:50: COMMA
                            {
                            COMMA32=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors598); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA32_tree = (CommonTree)adaptor.create(COMMA32);
                            adaptor.addChild(root_0, COMMA32_tree);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:112:1: selectoritem : ( listCompr | code | macro );
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.listCompr_return listCompr33 = null;

        EulangParser.code_return code34 = null;

        EulangParser.macro_return macro35 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:112:13: ( listCompr | code | macro )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:112:15: listCompr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_listCompr_in_selectoritem623);
                    listCompr33=listCompr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listCompr33.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:112:27: code
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_code_in_selectoritem627);
                    code34=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code34.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:112:34: macro
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_macro_in_selectoritem631);
                    macro35=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro35.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:1: xscope : LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) ;
    public final EulangParser.xscope_return xscope() throws RecognitionException {
        EulangParser.xscope_return retval = new EulangParser.xscope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE36=null;
        Token RBRACE38=null;
        EulangParser.toplevelstmts_return toplevelstmts37 = null;


        CommonTree LBRACE36_tree=null;
        CommonTree RBRACE38_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmts=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmts");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:8: ( LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:10: LBRACE toplevelstmts RBRACE
            {
            LBRACE36=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope642); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE36);

            pushFollow(FOLLOW_toplevelstmts_in_xscope644);
            toplevelstmts37=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmts.add(toplevelstmts37.getTree());
            RBRACE38=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope646); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE38);



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
            // 116:41: -> ^( SCOPE ( toplevelstmts )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:44: ^( SCOPE ( toplevelstmts )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:116:52: ( toplevelstmts )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:121:1: listCompr : ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) ;
    public final EulangParser.listCompr_return listCompr() throws RecognitionException {
        EulangParser.listCompr_return retval = new EulangParser.listCompr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON40=null;
        EulangParser.forIn_return forIn39 = null;

        EulangParser.listiterable_return listiterable41 = null;


        CommonTree COLON40_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_forIn=new RewriteRuleSubtreeStream(adaptor,"rule forIn");
        RewriteRuleSubtreeStream stream_listiterable=new RewriteRuleSubtreeStream(adaptor,"rule listiterable");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:121:10: ( ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:121:12: ( forIn )+ COLON listiterable
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:121:12: ( forIn )+
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:121:12: forIn
            	    {
            	    pushFollow(FOLLOW_forIn_in_listCompr673);
            	    forIn39=forIn();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_forIn.add(forIn39.getTree());

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

            COLON40=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr676); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON40);

            pushFollow(FOLLOW_listiterable_in_listCompr678);
            listiterable41=listiterable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listiterable.add(listiterable41.getTree());


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
            // 121:42: -> ^( LIST_COMPREHENSION ( forIn )+ listiterable )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:121:45: ^( LIST_COMPREHENSION ( forIn )+ listiterable )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:124:1: forIn : FOR idlist IN list -> ^( FOR idlist list ) ;
    public final EulangParser.forIn_return forIn() throws RecognitionException {
        EulangParser.forIn_return retval = new EulangParser.forIn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR42=null;
        Token IN44=null;
        EulangParser.idlist_return idlist43 = null;

        EulangParser.list_return list45 = null;


        CommonTree FOR42_tree=null;
        CommonTree IN44_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_list=new RewriteRuleSubtreeStream(adaptor,"rule list");
        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:124:7: ( FOR idlist IN list -> ^( FOR idlist list ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:124:9: FOR idlist IN list
            {
            FOR42=(Token)match(input,FOR,FOLLOW_FOR_in_forIn710); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR42);

            pushFollow(FOLLOW_idlist_in_forIn712);
            idlist43=idlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idlist.add(idlist43.getTree());
            IN44=(Token)match(input,IN,FOLLOW_IN_in_forIn714); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN44);

            pushFollow(FOLLOW_list_in_forIn716);
            list45=list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_list.add(list45.getTree());


            // AST REWRITE
            // elements: list, idlist, FOR
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 124:33: -> ^( FOR idlist list )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:124:36: ^( FOR idlist list )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:126:1: idlist : ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) ;
    public final EulangParser.idlist_return idlist() throws RecognitionException {
        EulangParser.idlist_return retval = new EulangParser.idlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID46=null;
        Token COMMA47=null;
        Token ID48=null;

        CommonTree ID46_tree=null;
        CommonTree COMMA47_tree=null;
        CommonTree ID48_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:126:8: ( ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:126:10: ID ( COMMA ID )*
            {
            ID46=(Token)match(input,ID,FOLLOW_ID_in_idlist741); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID46);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:126:13: ( COMMA ID )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==COMMA) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:126:14: COMMA ID
            	    {
            	    COMMA47=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist744); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA47);

            	    ID48=(Token)match(input,ID,FOLLOW_ID_in_idlist746); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID48);


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
            // 126:28: -> ^( IDLIST ( ID )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:126:31: ^( IDLIST ( ID )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:1: listiterable : ( code | macro | proto ) ;
    public final EulangParser.listiterable_return listiterable() throws RecognitionException {
        EulangParser.listiterable_return retval = new EulangParser.listiterable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code49 = null;

        EulangParser.macro_return macro50 = null;

        EulangParser.proto_return proto51 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:14: ( ( code | macro | proto ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:16: ( code | macro | proto )
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:16: ( code | macro | proto )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:18: code
                    {
                    pushFollow(FOLLOW_code_in_listiterable775);
                    code49=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, code49.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:25: macro
                    {
                    pushFollow(FOLLOW_macro_in_listiterable779);
                    macro50=macro();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, macro50.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:33: proto
                    {
                    pushFollow(FOLLOW_proto_in_listiterable783);
                    proto51=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, proto51.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:131:1: list : LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) ;
    public final EulangParser.list_return list() throws RecognitionException {
        EulangParser.list_return retval = new EulangParser.list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET52=null;
        Token RBRACKET54=null;
        EulangParser.listitems_return listitems53 = null;


        CommonTree LBRACKET52_tree=null;
        CommonTree RBRACKET54_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_listitems=new RewriteRuleSubtreeStream(adaptor,"rule listitems");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:131:6: ( LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:131:8: LBRACKET listitems RBRACKET
            {
            LBRACKET52=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list798); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET52);

            pushFollow(FOLLOW_listitems_in_list800);
            listitems53=listitems();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listitems.add(listitems53.getTree());
            RBRACKET54=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list802); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET54);



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
            // 131:40: -> ^( LIST ( listitems )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:131:43: ^( LIST ( listitems )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:131:50: ( listitems )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:1: listitems : ( listitem ( COMMA listitem )* ( COMMA )? )? ;
    public final EulangParser.listitems_return listitems() throws RecognitionException {
        EulangParser.listitems_return retval = new EulangParser.listitems_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA56=null;
        Token COMMA58=null;
        EulangParser.listitem_return listitem55 = null;

        EulangParser.listitem_return listitem57 = null;


        CommonTree COMMA56_tree=null;
        CommonTree COMMA58_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:10: ( ( listitem ( COMMA listitem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( ((LA14_0>=CODE && LA14_0<=MACRO)||LA14_0==ID||LA14_0==COLON||LA14_0==LBRACKET||LA14_0==LBRACE||LA14_0==LPAREN||(LA14_0>=MINUS && LA14_0<=STAR)||(LA14_0>=EXCL && LA14_0<=STRING_LITERAL)||LA14_0==COLONS) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems832);
                    listitem55=listitem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem55.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:22: ( COMMA listitem )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==COMMA) ) {
                            int LA12_1 = input.LA(2);

                            if ( ((LA12_1>=CODE && LA12_1<=MACRO)||LA12_1==ID||LA12_1==COLON||LA12_1==LBRACKET||LA12_1==LBRACE||LA12_1==LPAREN||(LA12_1>=MINUS && LA12_1<=STAR)||(LA12_1>=EXCL && LA12_1<=STRING_LITERAL)||LA12_1==COLONS) ) {
                                alt12=1;
                            }


                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:24: COMMA listitem
                    	    {
                    	    COMMA56=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems836); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA56_tree = (CommonTree)adaptor.create(COMMA56);
                    	    adaptor.addChild(root_0, COMMA56_tree);
                    	    }
                    	    pushFollow(FOLLOW_listitem_in_listitems838);
                    	    listitem57=listitem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem57.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:42: ( COMMA )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==COMMA) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:42: COMMA
                            {
                            COMMA58=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems843); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA58_tree = (CommonTree)adaptor.create(COMMA58);
                            adaptor.addChild(root_0, COMMA58_tree);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:1: listitem : toplevelvalue ;
    public final EulangParser.listitem_return listitem() throws RecognitionException {
        EulangParser.listitem_return retval = new EulangParser.listitem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelvalue_return toplevelvalue59 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:10: ( toplevelvalue )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:12: toplevelvalue
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelvalue_in_listitem869);
            toplevelvalue59=toplevelvalue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelvalue59.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:1: code : CODE ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE60=null;
        Token LPAREN61=null;
        Token RPAREN64=null;
        Token LBRACE65=null;
        Token RBRACE67=null;
        EulangParser.optargdefs_return optargdefs62 = null;

        EulangParser.xreturns_return xreturns63 = null;

        EulangParser.codestmtlist_return codestmtlist66 = null;


        CommonTree CODE60_tree=null;
        CommonTree LPAREN61_tree=null;
        CommonTree RPAREN64_tree=null;
        CommonTree LBRACE65_tree=null;
        CommonTree RBRACE67_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        RewriteRuleSubtreeStream stream_optargdefs=new RewriteRuleSubtreeStream(adaptor,"rule optargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:6: ( CODE ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:8: CODE ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE
            {
            CODE60=(Token)match(input,CODE,FOLLOW_CODE_in_code887); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CODE.add(CODE60);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:13: ( LPAREN optargdefs ( xreturns )? RPAREN )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==LPAREN) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:15: LPAREN optargdefs ( xreturns )? RPAREN
                    {
                    LPAREN61=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_code891); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN61);

                    pushFollow(FOLLOW_optargdefs_in_code893);
                    optargdefs62=optargdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdefs.add(optargdefs62.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:33: ( xreturns )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==RETURNS) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:33: xreturns
                            {
                            pushFollow(FOLLOW_xreturns_in_code895);
                            xreturns63=xreturns();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_xreturns.add(xreturns63.getTree());

                            }
                            break;

                    }

                    RPAREN64=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_code898); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN64);


                    }
                    break;

            }

            LBRACE65=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_code904); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE65);

            pushFollow(FOLLOW_codestmtlist_in_code906);
            codestmtlist66=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist66.getTree());
            RBRACE67=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code908); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE67);



            // AST REWRITE
            // elements: optargdefs, xreturns, codestmtlist, CODE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 142:81: -> ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:84: ^( CODE ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:91: ^( PROTO ( xreturns )? ( optargdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:99: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:109: ( optargdefs )*
                while ( stream_optargdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_optargdefs.nextTree());

                }
                stream_optargdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:122: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:1: macro : MACRO ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) ;
    public final EulangParser.macro_return macro() throws RecognitionException {
        EulangParser.macro_return retval = new EulangParser.macro_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MACRO68=null;
        Token LPAREN69=null;
        Token RPAREN72=null;
        Token LBRACE73=null;
        Token RBRACE75=null;
        EulangParser.optargdefs_return optargdefs70 = null;

        EulangParser.xreturns_return xreturns71 = null;

        EulangParser.codestmtlist_return codestmtlist74 = null;


        CommonTree MACRO68_tree=null;
        CommonTree LPAREN69_tree=null;
        CommonTree RPAREN72_tree=null;
        CommonTree LBRACE73_tree=null;
        CommonTree RBRACE75_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_MACRO=new RewriteRuleTokenStream(adaptor,"token MACRO");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        RewriteRuleSubtreeStream stream_optargdefs=new RewriteRuleSubtreeStream(adaptor,"rule optargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:7: ( MACRO ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE -> ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:9: MACRO ( LPAREN optargdefs ( xreturns )? RPAREN )? LBRACE codestmtlist RBRACE
            {
            MACRO68=(Token)match(input,MACRO,FOLLOW_MACRO_in_macro943); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_MACRO.add(MACRO68);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:15: ( LPAREN optargdefs ( xreturns )? RPAREN )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==LPAREN) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:17: LPAREN optargdefs ( xreturns )? RPAREN
                    {
                    LPAREN69=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_macro947); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN69);

                    pushFollow(FOLLOW_optargdefs_in_macro949);
                    optargdefs70=optargdefs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdefs.add(optargdefs70.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:35: ( xreturns )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==RETURNS) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:35: xreturns
                            {
                            pushFollow(FOLLOW_xreturns_in_macro951);
                            xreturns71=xreturns();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_xreturns.add(xreturns71.getTree());

                            }
                            break;

                    }

                    RPAREN72=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_macro954); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN72);


                    }
                    break;

            }

            LBRACE73=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_macro960); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE73);

            pushFollow(FOLLOW_codestmtlist_in_macro962);
            codestmtlist74=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist74.getTree());
            RBRACE75=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_macro964); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE75);



            // AST REWRITE
            // elements: optargdefs, MACRO, codestmtlist, xreturns
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 146:83: -> ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:86: ^( MACRO ^( PROTO ( xreturns )? ( optargdefs )* ) ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_MACRO.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:94: ^( PROTO ( xreturns )? ( optargdefs )* )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_2);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:102: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_2, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:112: ( optargdefs )*
                while ( stream_optargdefs.hasNext() ) {
                    adaptor.addChild(root_2, stream_optargdefs.nextTree());

                }
                stream_optargdefs.reset();

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:146:125: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:1: proto : LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN76=null;
        Token RPAREN79=null;
        EulangParser.argdefs_return argdefs77 = null;

        EulangParser.xreturns_return xreturns78 = null;


        CommonTree LPAREN76_tree=null;
        CommonTree RPAREN79_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:7: ( LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:9: LPAREN argdefs ( xreturns )? RPAREN
            {
            LPAREN76=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto998); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN76);

            pushFollow(FOLLOW_argdefs_in_proto1000);
            argdefs77=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs77.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:24: ( xreturns )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==RETURNS) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:24: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto1002);
                    xreturns78=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns78.getTree());

                    }
                    break;

            }

            RPAREN79=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto1005); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN79);



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
            // 149:59: -> ^( PROTO ( xreturns )? ( argdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:62: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:70: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:80: ( argdefs )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:1: argdefs : ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* ;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:8: ( ( argdef ( COMMA argdef )* ( COMMA )? )? -> ( argdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:10: ( argdef ( COMMA argdef )* ( COMMA )? )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==ID) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:11: argdef ( COMMA argdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_argdef_in_argdefs1047);
                    argdef80=argdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_argdef.add(argdef80.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:18: ( COMMA argdef )*
                    loop20:
                    do {
                        int alt20=2;
                        int LA20_0 = input.LA(1);

                        if ( (LA20_0==COMMA) ) {
                            int LA20_1 = input.LA(2);

                            if ( (LA20_1==ID) ) {
                                alt20=1;
                            }


                        }


                        switch (alt20) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:20: COMMA argdef
                    	    {
                    	    COMMA81=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1051); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA81);

                    	    pushFollow(FOLLOW_argdef_in_argdefs1053);
                    	    argdef82=argdef();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_argdef.add(argdef82.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop20;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:35: ( COMMA )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==COMMA) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:35: COMMA
                            {
                            COMMA83=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefs1057); if (state.failed) return retval; 
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
            // 151:67: -> ( argdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:70: ( argdef )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:1: argdef : ID ( COLON type )? -> ^( ARGDEF ID ( type )* ) ;
    public final EulangParser.argdef_return argdef() throws RecognitionException {
        EulangParser.argdef_return retval = new EulangParser.argdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID84=null;
        Token COLON85=null;
        EulangParser.type_return type86 = null;


        CommonTree ID84_tree=null;
        CommonTree COLON85_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:7: ( ID ( COLON type )? -> ^( ARGDEF ID ( type )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:9: ID ( COLON type )?
            {
            ID84=(Token)match(input,ID,FOLLOW_ID_in_argdef1101); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID84);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:12: ( COLON type )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==COLON) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:13: COLON type
                    {
                    COLON85=(Token)match(input,COLON,FOLLOW_COLON_in_argdef1104); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON85);

                    pushFollow(FOLLOW_type_in_argdef1106);
                    type86=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type86.getTree());

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
            // 154:29: -> ^( ARGDEF ID ( type )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:32: ^( ARGDEF ID ( type )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:44: ( type )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:1: xreturns : RETURNS type -> type ;
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURNS87=null;
        EulangParser.type_return type88 = null;


        CommonTree RETURNS87_tree=null;
        RewriteRuleTokenStream stream_RETURNS=new RewriteRuleTokenStream(adaptor,"token RETURNS");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:9: ( RETURNS type -> type )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:11: RETURNS type
            {
            RETURNS87=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_xreturns1133); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RETURNS.add(RETURNS87);

            pushFollow(FOLLOW_type_in_xreturns1135);
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
            // 157:29: -> type
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

    public static class optargdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "optargdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:1: optargdefs : ( optargdef ( COMMA optargdef )* ( COMMA )? )? -> ( optargdef )* ;
    public final EulangParser.optargdefs_return optargdefs() throws RecognitionException {
        EulangParser.optargdefs_return retval = new EulangParser.optargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA90=null;
        Token COMMA92=null;
        EulangParser.optargdef_return optargdef89 = null;

        EulangParser.optargdef_return optargdef91 = null;


        CommonTree COMMA90_tree=null;
        CommonTree COMMA92_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_optargdef=new RewriteRuleSubtreeStream(adaptor,"rule optargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:11: ( ( optargdef ( COMMA optargdef )* ( COMMA )? )? -> ( optargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:13: ( optargdef ( COMMA optargdef )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:13: ( optargdef ( COMMA optargdef )* ( COMMA )? )?
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==ID) ) {
                alt26=1;
            }
            switch (alt26) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:14: optargdef ( COMMA optargdef )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_optargdef_in_optargdefs1155);
                    optargdef89=optargdef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optargdef.add(optargdef89.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:24: ( COMMA optargdef )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==COMMA) ) {
                            int LA24_1 = input.LA(2);

                            if ( (LA24_1==ID) ) {
                                alt24=1;
                            }


                        }


                        switch (alt24) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:26: COMMA optargdef
                    	    {
                    	    COMMA90=(Token)match(input,COMMA,FOLLOW_COMMA_in_optargdefs1159); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA90);

                    	    pushFollow(FOLLOW_optargdef_in_optargdefs1161);
                    	    optargdef91=optargdef();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_optargdef.add(optargdef91.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:44: ( COMMA )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==COMMA) ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:44: COMMA
                            {
                            COMMA92=(Token)match(input,COMMA,FOLLOW_COMMA_in_optargdefs1165); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA92);


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
            // 160:76: -> ( optargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:79: ( optargdef )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:1: optargdef : ID ( COLON type )? ( EQUALS init= rhsExpr )? -> ^( ARGDEF ID ( type )* ( $init)? ) ;
    public final EulangParser.optargdef_return optargdef() throws RecognitionException {
        EulangParser.optargdef_return retval = new EulangParser.optargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID93=null;
        Token COLON94=null;
        Token EQUALS96=null;
        EulangParser.rhsExpr_return init = null;

        EulangParser.type_return type95 = null;


        CommonTree ID93_tree=null;
        CommonTree COLON94_tree=null;
        CommonTree EQUALS96_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:10: ( ID ( COLON type )? ( EQUALS init= rhsExpr )? -> ^( ARGDEF ID ( type )* ( $init)? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:12: ID ( COLON type )? ( EQUALS init= rhsExpr )?
            {
            ID93=(Token)match(input,ID,FOLLOW_ID_in_optargdef1209); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID93);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:15: ( COLON type )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==COLON) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:16: COLON type
                    {
                    COLON94=(Token)match(input,COLON,FOLLOW_COLON_in_optargdef1212); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON94);

                    pushFollow(FOLLOW_type_in_optargdef1214);
                    type95=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type95.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:29: ( EQUALS init= rhsExpr )?
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==EQUALS) ) {
                alt28=1;
            }
            switch (alt28) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:30: EQUALS init= rhsExpr
                    {
                    EQUALS96=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_optargdef1219); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS96);

                    pushFollow(FOLLOW_rhsExpr_in_optargdef1223);
                    init=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(init.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: ID, type, init
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
            // 163:55: -> ^( ARGDEF ID ( type )* ( $init)? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:58: ^( ARGDEF ID ( type )* ( $init)? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:70: ( type )*
                while ( stream_type.hasNext() ) {
                    adaptor.addChild(root_1, stream_type.nextTree());

                }
                stream_type.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:76: ( $init)?
                if ( stream_init.hasNext() ) {
                    adaptor.addChild(root_1, stream_init.nextTree());

                }
                stream_init.reset();

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
    // $ANTLR end "optargdef"

    public static class type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:1: type : ( ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )? | CODE ( proto )? -> ^( TYPE proto ) );
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP98=null;
        Token CODE99=null;
        EulangParser.idOrScopeRef_return idOrScopeRef97 = null;

        EulangParser.proto_return proto100 = null;


        CommonTree AMP98_tree=null;
        CommonTree CODE99_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:6: ( ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )? | CODE ( proto )? -> ^( TYPE proto ) )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==ID||LA31_0==COLON||LA31_0==COLONS) ) {
                alt31=1;
            }
            else if ( (LA31_0==CODE) ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:9: ( idOrScopeRef -> ^( TYPE idOrScopeRef ) ) ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )?
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:9: ( idOrScopeRef -> ^( TYPE idOrScopeRef ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:11: idOrScopeRef
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_type1260);
                    idOrScopeRef97=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef97.getTree());


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
                    // 167:24: -> ^( TYPE idOrScopeRef )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:27: ^( TYPE idOrScopeRef )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:51: ( AMP -> ^( TYPE ^( REF idOrScopeRef ) ) )?
                    int alt29=2;
                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==AMP) ) {
                        alt29=1;
                    }
                    switch (alt29) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:53: AMP
                            {
                            AMP98=(Token)match(input,AMP,FOLLOW_AMP_in_type1275); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_AMP.add(AMP98);



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
                            // 167:57: -> ^( TYPE ^( REF idOrScopeRef ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:60: ^( TYPE ^( REF idOrScopeRef ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:67: ^( REF idOrScopeRef )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:8: CODE ( proto )?
                    {
                    CODE99=(Token)match(input,CODE,FOLLOW_CODE_in_type1301); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CODE.add(CODE99);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:13: ( proto )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==LPAREN) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:13: proto
                            {
                            pushFollow(FOLLOW_proto_in_type1303);
                            proto100=proto();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_proto.add(proto100.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: proto
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 168:21: -> ^( TYPE proto )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:24: ^( TYPE proto )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_proto.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:1: codestmtlist : ( ( codeStmt )=> ( ( codeStmt )+ ) -> ^( STMTLIST ( codeStmt )* ) | rhsExpr -> ^( STMTLIST ^( RETURN rhsExpr ) ) | -> ^( STMTLIST ) );
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.codeStmt_return codeStmt101 = null;

        EulangParser.rhsExpr_return rhsExpr102 = null;


        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:13: ( ( codeStmt )=> ( ( codeStmt )+ ) -> ^( STMTLIST ( codeStmt )* ) | rhsExpr -> ^( STMTLIST ^( RETURN rhsExpr ) ) | -> ^( STMTLIST ) )
            int alt33=3;
            alt33 = dfa33.predict(input);
            switch (alt33) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:16: ( codeStmt )=> ( ( codeStmt )+ )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:31: ( ( codeStmt )+ )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:32: ( codeStmt )+
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:32: ( codeStmt )+
                    int cnt32=0;
                    loop32:
                    do {
                        int alt32=2;
                        int LA32_0 = input.LA(1);

                        if ( (LA32_0==CODE||LA32_0==GOTO||LA32_0==ID||LA32_0==COLON||LA32_0==LBRACE||LA32_0==LPAREN||(LA32_0>=RETURN && LA32_0<=AT)||(LA32_0>=MINUS && LA32_0<=STAR)||(LA32_0>=EXCL && LA32_0<=STRING_LITERAL)||LA32_0==COLONS) ) {
                            alt32=1;
                        }


                        switch (alt32) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:32: codeStmt
                    	    {
                    	    pushFollow(FOLLOW_codeStmt_in_codestmtlist1333);
                    	    codeStmt101=codeStmt();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt101.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt32 >= 1 ) break loop32;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(32, input);
                                throw eee;
                        }
                        cnt32++;
                    } while (true);


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
                    // 171:44: -> ^( STMTLIST ( codeStmt )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:48: ^( STMTLIST ( codeStmt )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:59: ( codeStmt )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_codestmtlist1354);
                    rhsExpr102=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr102.getTree());


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
                    // 172:24: -> ^( STMTLIST ^( RETURN rhsExpr ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:27: ^( STMTLIST ^( RETURN rhsExpr ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:38: ^( RETURN rhsExpr )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(RETURN, "RETURN"), root_2);

                        adaptor.addChild(root_2, stream_rhsExpr.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:7: 
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
                    // 173:7: -> ^( STMTLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:10: ^( STMTLIST )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:1: codeStmt : ( varDecl SEMI -> varDecl | assignStmt SEMI -> assignStmt | returnStmt SEMI -> returnStmt | gotoStmt SEMI -> gotoStmt | rhsExpr SEMI -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | labelStmt -> labelStmt );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI104=null;
        Token SEMI106=null;
        Token SEMI108=null;
        Token SEMI110=null;
        Token SEMI112=null;
        EulangParser.varDecl_return varDecl103 = null;

        EulangParser.assignStmt_return assignStmt105 = null;

        EulangParser.returnStmt_return returnStmt107 = null;

        EulangParser.gotoStmt_return gotoStmt109 = null;

        EulangParser.rhsExpr_return rhsExpr111 = null;

        EulangParser.blockStmt_return blockStmt113 = null;

        EulangParser.labelStmt_return labelStmt114 = null;


        CommonTree SEMI104_tree=null;
        CommonTree SEMI106_tree=null;
        CommonTree SEMI108_tree=null;
        CommonTree SEMI110_tree=null;
        CommonTree SEMI112_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_assignStmt=new RewriteRuleSubtreeStream(adaptor,"rule assignStmt");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_labelStmt=new RewriteRuleSubtreeStream(adaptor,"rule labelStmt");
        RewriteRuleSubtreeStream stream_blockStmt=new RewriteRuleSubtreeStream(adaptor,"rule blockStmt");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_returnStmt=new RewriteRuleSubtreeStream(adaptor,"rule returnStmt");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:10: ( varDecl SEMI -> varDecl | assignStmt SEMI -> assignStmt | returnStmt SEMI -> returnStmt | gotoStmt SEMI -> gotoStmt | rhsExpr SEMI -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | labelStmt -> labelStmt )
            int alt34=7;
            alt34 = dfa34.predict(input);
            switch (alt34) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:12: varDecl SEMI
                    {
                    pushFollow(FOLLOW_varDecl_in_codeStmt1405);
                    varDecl103=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl103.getTree());
                    SEMI104=(Token)match(input,SEMI,FOLLOW_SEMI_in_codeStmt1407); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI104);



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
                    // 176:27: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:177:9: assignStmt SEMI
                    {
                    pushFollow(FOLLOW_assignStmt_in_codeStmt1423);
                    assignStmt105=assignStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignStmt.add(assignStmt105.getTree());
                    SEMI106=(Token)match(input,SEMI,FOLLOW_SEMI_in_codeStmt1425); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI106);



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
                    // 177:27: -> assignStmt
                    {
                        adaptor.addChild(root_0, stream_assignStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:178:9: returnStmt SEMI
                    {
                    pushFollow(FOLLOW_returnStmt_in_codeStmt1441);
                    returnStmt107=returnStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_returnStmt.add(returnStmt107.getTree());
                    SEMI108=(Token)match(input,SEMI,FOLLOW_SEMI_in_codeStmt1443); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI108);



                    // AST REWRITE
                    // elements: returnStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 178:27: -> returnStmt
                    {
                        adaptor.addChild(root_0, stream_returnStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:9: gotoStmt SEMI
                    {
                    pushFollow(FOLLOW_gotoStmt_in_codeStmt1459);
                    gotoStmt109=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt109.getTree());
                    SEMI110=(Token)match(input,SEMI,FOLLOW_SEMI_in_codeStmt1461); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI110);



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
                    // 179:27: -> gotoStmt
                    {
                        adaptor.addChild(root_0, stream_gotoStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:9: rhsExpr SEMI
                    {
                    pushFollow(FOLLOW_rhsExpr_in_codeStmt1479);
                    rhsExpr111=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr111.getTree());
                    SEMI112=(Token)match(input,SEMI,FOLLOW_SEMI_in_codeStmt1481); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI112);



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
                    // 180:27: -> ^( STMTEXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:30: ^( STMTEXPR rhsExpr )
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
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:9: blockStmt
                    {
                    pushFollow(FOLLOW_blockStmt_in_codeStmt1504);
                    blockStmt113=blockStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_blockStmt.add(blockStmt113.getTree());


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
                    // 181:27: -> blockStmt
                    {
                        adaptor.addChild(root_0, stream_blockStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:9: labelStmt
                    {
                    pushFollow(FOLLOW_labelStmt_in_codeStmt1526);
                    labelStmt114=labelStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labelStmt.add(labelStmt114.getTree());


                    // AST REWRITE
                    // elements: labelStmt
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 182:23: -> labelStmt
                    {
                        adaptor.addChild(root_0, stream_labelStmt.nextTree());

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

    public static class varDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "varDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:1: varDecl : ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID115=null;
        Token COLON_EQUALS116=null;
        Token ID118=null;
        Token COLON119=null;
        Token EQUALS121=null;
        EulangParser.assignExpr_return assignExpr117 = null;

        EulangParser.type_return type120 = null;

        EulangParser.assignExpr_return assignExpr122 = null;


        CommonTree ID115_tree=null;
        CommonTree COLON_EQUALS116_tree=null;
        CommonTree ID118_tree=null;
        CommonTree COLON119_tree=null;
        CommonTree EQUALS121_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:8: ( ID COLON_EQUALS assignExpr -> ^( ALLOC ID TYPE assignExpr ) | ID COLON type ( EQUALS assignExpr )? -> ^( ALLOC ID type ( assignExpr )* ) )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==ID) ) {
                int LA36_1 = input.LA(2);

                if ( (LA36_1==COLON_EQUALS) ) {
                    alt36=1;
                }
                else if ( (LA36_1==COLON) ) {
                    alt36=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 36, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:10: ID COLON_EQUALS assignExpr
                    {
                    ID115=(Token)match(input,ID,FOLLOW_ID_in_varDecl1548); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID115);

                    COLON_EQUALS116=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_varDecl1550); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS116);

                    pushFollow(FOLLOW_assignExpr_in_varDecl1552);
                    assignExpr117=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr117.getTree());


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
                    // 185:45: -> ^( ALLOC ID TYPE assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:48: ^( ALLOC ID TYPE assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:7: ID COLON type ( EQUALS assignExpr )?
                    {
                    ID118=(Token)match(input,ID,FOLLOW_ID_in_varDecl1580); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID118);

                    COLON119=(Token)match(input,COLON,FOLLOW_COLON_in_varDecl1582); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON119);

                    pushFollow(FOLLOW_type_in_varDecl1584);
                    type120=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type120.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:21: ( EQUALS assignExpr )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==EQUALS) ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:22: EQUALS assignExpr
                            {
                            EQUALS121=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_varDecl1587); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS121);

                            pushFollow(FOLLOW_assignExpr_in_varDecl1589);
                            assignExpr122=assignExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr122.getTree());

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
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 186:43: -> ^( ALLOC ID type ( assignExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:46: ^( ALLOC ID type ( assignExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:62: ( assignExpr )*
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

    public static class returnStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "returnStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:1: returnStmt : RETURN ( assignExpr )? -> ^( RETURN ( assignExpr )? ) ;
    public final EulangParser.returnStmt_return returnStmt() throws RecognitionException {
        EulangParser.returnStmt_return retval = new EulangParser.returnStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token RETURN123=null;
        EulangParser.assignExpr_return assignExpr124 = null;


        CommonTree RETURN123_tree=null;
        RewriteRuleTokenStream stream_RETURN=new RewriteRuleTokenStream(adaptor,"token RETURN");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:12: ( RETURN ( assignExpr )? -> ^( RETURN ( assignExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:14: RETURN ( assignExpr )?
            {
            RETURN123=(Token)match(input,RETURN,FOLLOW_RETURN_in_returnStmt1618); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RETURN.add(RETURN123);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:21: ( assignExpr )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==CODE||LA37_0==ID||LA37_0==COLON||LA37_0==LPAREN||(LA37_0>=MINUS && LA37_0<=STAR)||(LA37_0>=EXCL && LA37_0<=STRING_LITERAL)||LA37_0==COLONS) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:21: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_returnStmt1620);
                    assignExpr124=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr124.getTree());

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
            // 189:43: -> ^( RETURN ( assignExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:46: ^( RETURN ( assignExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_RETURN.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:55: ( assignExpr )?
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
    // $ANTLR end "returnStmt"

    public static class assignStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:1: assignStmt : idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) ;
    public final EulangParser.assignStmt_return assignStmt() throws RecognitionException {
        EulangParser.assignStmt_return retval = new EulangParser.assignStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS126=null;
        EulangParser.idOrScopeRef_return idOrScopeRef125 = null;

        EulangParser.assignExpr_return assignExpr127 = null;


        CommonTree EQUALS126_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:14: idOrScopeRef EQUALS assignExpr
            {
            pushFollow(FOLLOW_idOrScopeRef_in_assignStmt1655);
            idOrScopeRef125=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef125.getTree());
            EQUALS126=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt1657); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS126);

            pushFollow(FOLLOW_assignExpr_in_assignStmt1659);
            assignExpr127=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr127.getTree());


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
            // 192:52: -> ^( ASSIGN idOrScopeRef assignExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:192:55: ^( ASSIGN idOrScopeRef assignExpr )
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

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS129=null;
        EulangParser.idOrScopeRef_return idOrScopeRef128 = null;

        EulangParser.assignExpr_return assignExpr130 = null;

        EulangParser.rhsExpr_return rhsExpr131 = null;


        CommonTree EQUALS129_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:12: ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | rhsExpr -> rhsExpr )
            int alt38=2;
            alt38 = dfa38.predict(input);
            switch (alt38) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:14: idOrScopeRef EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idOrScopeRef_in_assignExpr1695);
                    idOrScopeRef128=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef128.getTree());
                    EQUALS129=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr1697); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS129);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr1699);
                    assignExpr130=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr130.getTree());


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
                    // 195:52: -> ^( ASSIGN idOrScopeRef assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:195:55: ^( ASSIGN idOrScopeRef assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:196:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr1724);
                    rhsExpr131=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr131.getTree());


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
                    // 196:43: -> rhsExpr
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:1: labelStmt : AT ID COLON -> ^( LABEL ID ) ;
    public final EulangParser.labelStmt_return labelStmt() throws RecognitionException {
        EulangParser.labelStmt_return retval = new EulangParser.labelStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT132=null;
        Token ID133=null;
        Token COLON134=null;

        CommonTree AT132_tree=null;
        CommonTree ID133_tree=null;
        CommonTree COLON134_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:10: ( AT ID COLON -> ^( LABEL ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:12: AT ID COLON
            {
            AT132=(Token)match(input,AT,FOLLOW_AT_in_labelStmt1768); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT132);

            ID133=(Token)match(input,ID,FOLLOW_ID_in_labelStmt1770); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID133);

            COLON134=(Token)match(input,COLON,FOLLOW_COLON_in_labelStmt1772); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON134);



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
            // 199:43: -> ^( LABEL ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:199:46: ^( LABEL ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:1: gotoStmt : GOTO idOrScopeRef ( COMMA rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) ;
    public final EulangParser.gotoStmt_return gotoStmt() throws RecognitionException {
        EulangParser.gotoStmt_return retval = new EulangParser.gotoStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token GOTO135=null;
        Token COMMA137=null;
        EulangParser.idOrScopeRef_return idOrScopeRef136 = null;

        EulangParser.rhsExpr_return rhsExpr138 = null;


        CommonTree GOTO135_tree=null;
        CommonTree COMMA137_tree=null;
        RewriteRuleTokenStream stream_GOTO=new RewriteRuleTokenStream(adaptor,"token GOTO");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:9: ( GOTO idOrScopeRef ( COMMA rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:11: GOTO idOrScopeRef ( COMMA rhsExpr )?
            {
            GOTO135=(Token)match(input,GOTO,FOLLOW_GOTO_in_gotoStmt1808); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GOTO.add(GOTO135);

            pushFollow(FOLLOW_idOrScopeRef_in_gotoStmt1810);
            idOrScopeRef136=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef136.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:29: ( COMMA rhsExpr )?
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==COMMA) ) {
                alt39=1;
            }
            switch (alt39) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:31: COMMA rhsExpr
                    {
                    COMMA137=(Token)match(input,COMMA,FOLLOW_COMMA_in_gotoStmt1814); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA137);

                    pushFollow(FOLLOW_rhsExpr_in_gotoStmt1816);
                    rhsExpr138=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr138.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: GOTO, idOrScopeRef, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 201:58: -> ^( GOTO idOrScopeRef ( rhsExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:61: ^( GOTO idOrScopeRef ( rhsExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_GOTO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:201:81: ( rhsExpr )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:1: blockStmt : LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) ;
    public final EulangParser.blockStmt_return blockStmt() throws RecognitionException {
        EulangParser.blockStmt_return retval = new EulangParser.blockStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE139=null;
        Token RBRACE141=null;
        EulangParser.codestmtlist_return codestmtlist140 = null;


        CommonTree LBRACE139_tree=null;
        CommonTree RBRACE141_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:10: ( LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:12: LBRACE codestmtlist RBRACE
            {
            LBRACE139=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_blockStmt1852); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE139);

            pushFollow(FOLLOW_codestmtlist_in_blockStmt1854);
            codestmtlist140=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist140.getTree());
            RBRACE141=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_blockStmt1856); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE141);



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
            // 204:43: -> ^( BLOCK codestmtlist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:46: ^( BLOCK codestmtlist )
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

    public static class rhsExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rhsExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:207:1: rhsExpr : cond -> cond ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.cond_return cond142 = null;


        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:207:9: ( cond -> cond )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:207:13: cond
            {
            pushFollow(FOLLOW_cond_in_rhsExpr1881);
            cond142=cond();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_cond.add(cond142.getTree());


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
            // 207:43: -> cond
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:1: funcCall : idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) ;
    public final EulangParser.funcCall_return funcCall() throws RecognitionException {
        EulangParser.funcCall_return retval = new EulangParser.funcCall_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN144=null;
        Token RPAREN146=null;
        EulangParser.idOrScopeRef_return idOrScopeRef143 = null;

        EulangParser.arglist_return arglist145 = null;


        CommonTree LPAREN144_tree=null;
        CommonTree RPAREN146_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:10: ( idOrScopeRef LPAREN arglist RPAREN -> ^( CALL idOrScopeRef arglist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:12: idOrScopeRef LPAREN arglist RPAREN
            {
            pushFollow(FOLLOW_idOrScopeRef_in_funcCall1927);
            idOrScopeRef143=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef143.getTree());
            LPAREN144=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_funcCall1929); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN144);

            pushFollow(FOLLOW_arglist_in_funcCall1931);
            arglist145=arglist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_arglist.add(arglist145.getTree());
            RPAREN146=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_funcCall1933); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN146);



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
            // 210:49: -> ^( CALL idOrScopeRef arglist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:56: ^( CALL idOrScopeRef arglist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA148=null;
        Token COMMA150=null;
        EulangParser.arg_return arg147 = null;

        EulangParser.arg_return arg149 = null;


        CommonTree COMMA148_tree=null;
        CommonTree COMMA150_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==CODE||LA42_0==ID||LA42_0==COLON||LA42_0==LPAREN||(LA42_0>=MINUS && LA42_0<=STAR)||(LA42_0>=EXCL && LA42_0<=STRING_LITERAL)||LA42_0==COLONS) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist1964);
                    arg147=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg147.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:15: ( COMMA arg )*
                    loop40:
                    do {
                        int alt40=2;
                        int LA40_0 = input.LA(1);

                        if ( (LA40_0==COMMA) ) {
                            int LA40_1 = input.LA(2);

                            if ( (LA40_1==CODE||LA40_1==ID||LA40_1==COLON||LA40_1==LPAREN||(LA40_1>=MINUS && LA40_1<=STAR)||(LA40_1>=EXCL && LA40_1<=STRING_LITERAL)||LA40_1==COLONS) ) {
                                alt40=1;
                            }


                        }


                        switch (alt40) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:17: COMMA arg
                    	    {
                    	    COMMA148=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist1968); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA148);

                    	    pushFollow(FOLLOW_arg_in_arglist1970);
                    	    arg149=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg149.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop40;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:29: ( COMMA )?
                    int alt41=2;
                    int LA41_0 = input.LA(1);

                    if ( (LA41_0==COMMA) ) {
                        alt41=1;
                    }
                    switch (alt41) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:29: COMMA
                            {
                            COMMA150=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist1974); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA150);


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
            // 214:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:1: arg : assignExpr -> ^( EXPR assignExpr ) ;
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr151 = null;


        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:4: ( assignExpr -> ^( EXPR assignExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:7: assignExpr
            {
            pushFollow(FOLLOW_assignExpr_in_arg2023);
            assignExpr151=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr151.getTree());


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
            // 217:37: -> ^( EXPR assignExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:217:40: ^( EXPR assignExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:1: cond : ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION153=null;
        Token COLON154=null;
        EulangParser.logor_return t = null;

        EulangParser.logor_return f = null;

        EulangParser.logor_return logor152 = null;


        CommonTree QUESTION153_tree=null;
        CommonTree COLON154_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logor=new RewriteRuleSubtreeStream(adaptor,"rule logor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:5: ( ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:10: ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:10: ( logor -> logor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:224:12: logor
            {
            pushFollow(FOLLOW_logor_in_cond2071);
            logor152=logor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logor.add(logor152.getTree());


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
            // 224:19: -> logor
            {
                adaptor.addChild(root_0, stream_logor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:7: ( QUESTION t= logor COLON f= logor -> ^( COND $cond $t $f) )*
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==QUESTION) ) {
                    alt43=1;
                }


                switch (alt43) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:9: QUESTION t= logor COLON f= logor
            	    {
            	    QUESTION153=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond2088); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION153);

            	    pushFollow(FOLLOW_logor_in_cond2092);
            	    t=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(t.getTree());
            	    COLON154=(Token)match(input,COLON,FOLLOW_COLON_in_cond2094); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON154);

            	    pushFollow(FOLLOW_logor_in_cond2098);
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
            	    // 225:40: -> ^( COND $cond $t $f)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:43: ^( COND $cond $t $f)
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
    // $ANTLR end "cond"

    public static class logor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:1: logor : ( logand -> logand ) ( COMPOR r= logand -> ^( COMPOR $logor $r) )* ;
    public final EulangParser.logor_return logor() throws RecognitionException {
        EulangParser.logor_return retval = new EulangParser.logor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPOR156=null;
        EulangParser.logand_return r = null;

        EulangParser.logand_return logand155 = null;


        CommonTree COMPOR156_tree=null;
        RewriteRuleTokenStream stream_COMPOR=new RewriteRuleTokenStream(adaptor,"token COMPOR");
        RewriteRuleSubtreeStream stream_logand=new RewriteRuleSubtreeStream(adaptor,"rule logand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:7: ( ( logand -> logand ) ( COMPOR r= logand -> ^( COMPOR $logor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:9: ( logand -> logand ) ( COMPOR r= logand -> ^( COMPOR $logor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:9: ( logand -> logand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:11: logand
            {
            pushFollow(FOLLOW_logand_in_logor2128);
            logand155=logand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logand.add(logand155.getTree());


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
            // 228:19: -> logand
            {
                adaptor.addChild(root_0, stream_logand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:229:7: ( COMPOR r= logand -> ^( COMPOR $logor $r) )*
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==COMPOR) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:229:9: COMPOR r= logand
            	    {
            	    COMPOR156=(Token)match(input,COMPOR,FOLLOW_COMPOR_in_logor2145); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPOR.add(COMPOR156);

            	    pushFollow(FOLLOW_logand_in_logor2149);
            	    r=logand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, COMPOR, logor
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
            	    // 229:25: -> ^( COMPOR $logor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:229:28: ^( COMPOR $logor $r)
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
    // $ANTLR end "logor"

    public static class logand_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "logand"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:1: logand : ( comp -> comp ) ( COMPAND r= comp -> ^( COMPAND $logand $r) )* ;
    public final EulangParser.logand_return logand() throws RecognitionException {
        EulangParser.logand_return retval = new EulangParser.logand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPAND158=null;
        EulangParser.comp_return r = null;

        EulangParser.comp_return comp157 = null;


        CommonTree COMPAND158_tree=null;
        RewriteRuleTokenStream stream_COMPAND=new RewriteRuleTokenStream(adaptor,"token COMPAND");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:8: ( ( comp -> comp ) ( COMPAND r= comp -> ^( COMPAND $logand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:10: ( comp -> comp ) ( COMPAND r= comp -> ^( COMPAND $logand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:10: ( comp -> comp )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:231:12: comp
            {
            pushFollow(FOLLOW_comp_in_logand2180);
            comp157=comp();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_comp.add(comp157.getTree());


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
            // 231:17: -> comp
            {
                adaptor.addChild(root_0, stream_comp.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:232:7: ( COMPAND r= comp -> ^( COMPAND $logand $r) )*
            loop45:
            do {
                int alt45=2;
                int LA45_0 = input.LA(1);

                if ( (LA45_0==COMPAND) ) {
                    alt45=1;
                }


                switch (alt45) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:232:9: COMPAND r= comp
            	    {
            	    COMPAND158=(Token)match(input,COMPAND,FOLLOW_COMPAND_in_logand2196); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPAND.add(COMPAND158);

            	    pushFollow(FOLLOW_comp_in_logand2200);
            	    r=comp();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_comp.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, COMPAND, logand
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
            	    // 232:24: -> ^( COMPAND $logand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:232:27: ^( COMPAND $logand $r)
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
    // $ANTLR end "logand"

    public static class comp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:1: comp : ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ160=null;
        Token COMPNE161=null;
        Token COMPLE162=null;
        Token COMPGE163=null;
        Token LESS164=null;
        Token GREATER165=null;
        EulangParser.bitor_return r = null;

        EulangParser.bitor_return bitor159 = null;


        CommonTree COMPEQ160_tree=null;
        CommonTree COMPNE161_tree=null;
        CommonTree COMPLE162_tree=null;
        CommonTree COMPGE163_tree=null;
        CommonTree LESS164_tree=null;
        CommonTree GREATER165_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_bitor=new RewriteRuleSubtreeStream(adaptor,"rule bitor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:5: ( ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:9: ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:9: ( bitor -> bitor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:11: bitor
            {
            pushFollow(FOLLOW_bitor_in_comp2250);
            bitor159=bitor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitor.add(bitor159.getTree());


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
            // 237:24: -> bitor
            {
                adaptor.addChild(root_0, stream_bitor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:7: ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) )*
            loop46:
            do {
                int alt46=7;
                switch ( input.LA(1) ) {
                case COMPEQ:
                    {
                    alt46=1;
                    }
                    break;
                case COMPNE:
                    {
                    alt46=2;
                    }
                    break;
                case COMPLE:
                    {
                    alt46=3;
                    }
                    break;
                case COMPGE:
                    {
                    alt46=4;
                    }
                    break;
                case LESS:
                    {
                    alt46=5;
                    }
                    break;
                case GREATER:
                    {
                    alt46=6;
                    }
                    break;

                }

                switch (alt46) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:9: COMPEQ r= bitor
            	    {
            	    COMPEQ160=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_comp2283); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ160);

            	    pushFollow(FOLLOW_bitor_in_comp2287);
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
            	    // 238:24: -> ^( COMPEQ $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:27: ^( COMPEQ $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:239:9: COMPNE r= bitor
            	    {
            	    COMPNE161=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_comp2309); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE161);

            	    pushFollow(FOLLOW_bitor_in_comp2313);
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
            	    // 239:24: -> ^( COMPNE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:239:27: ^( COMPNE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:9: COMPLE r= bitor
            	    {
            	    COMPLE162=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp2335); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE162);

            	    pushFollow(FOLLOW_bitor_in_comp2339);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, comp, COMPLE
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
            	    // 240:27: -> ^( COMPLE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:240:30: ^( COMPLE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:9: COMPGE r= bitor
            	    {
            	    COMPGE163=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp2364); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE163);

            	    pushFollow(FOLLOW_bitor_in_comp2368);
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
            	    // 241:27: -> ^( COMPGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:30: ^( COMPGE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:9: LESS r= bitor
            	    {
            	    LESS164=(Token)match(input,LESS,FOLLOW_LESS_in_comp2393); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS164);

            	    pushFollow(FOLLOW_bitor_in_comp2397);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: LESS, r, comp
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
            	    // 242:26: -> ^( LESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:29: ^( LESS $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:243:9: GREATER r= bitor
            	    {
            	    GREATER165=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp2423); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER165);

            	    pushFollow(FOLLOW_bitor_in_comp2427);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: GREATER, r, comp
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
            	    // 243:28: -> ^( GREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:243:31: ^( GREATER $comp $r)
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
    // $ANTLR end "comp"

    public static class bitor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:1: bitor : ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* ;
    public final EulangParser.bitor_return bitor() throws RecognitionException {
        EulangParser.bitor_return retval = new EulangParser.bitor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR167=null;
        EulangParser.bitxor_return r = null;

        EulangParser.bitxor_return bitxor166 = null;


        CommonTree BAR167_tree=null;
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_bitxor=new RewriteRuleSubtreeStream(adaptor,"rule bitxor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:6: ( ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:8: ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:8: ( bitxor -> bitxor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:10: bitxor
            {
            pushFollow(FOLLOW_bitxor_in_bitor2477);
            bitxor166=bitxor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitxor.add(bitxor166.getTree());


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
            // 248:22: -> bitxor
            {
                adaptor.addChild(root_0, stream_bitxor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:249:7: ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==BAR) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:249:9: BAR r= bitxor
            	    {
            	    BAR167=(Token)match(input,BAR,FOLLOW_BAR_in_bitor2505); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR167);

            	    pushFollow(FOLLOW_bitxor_in_bitor2509);
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
            	    // 249:23: -> ^( BITOR $bitor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:249:26: ^( BITOR $bitor $r)
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
            	    break loop47;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:1: bitxor : ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* ;
    public final EulangParser.bitxor_return bitxor() throws RecognitionException {
        EulangParser.bitxor_return retval = new EulangParser.bitxor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CARET169=null;
        EulangParser.bitand_return r = null;

        EulangParser.bitand_return bitand168 = null;


        CommonTree CARET169_tree=null;
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_bitand=new RewriteRuleSubtreeStream(adaptor,"rule bitand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:7: ( ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:9: ( bitand -> bitand ) ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:9: ( bitand -> bitand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:11: bitand
            {
            pushFollow(FOLLOW_bitand_in_bitxor2535);
            bitand168=bitand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitand.add(bitand168.getTree());


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
            // 251:23: -> bitand
            {
                adaptor.addChild(root_0, stream_bitand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:252:7: ( CARET r= bitand -> ^( BITXOR $bitxor $r) )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==CARET) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:252:9: CARET r= bitand
            	    {
            	    CARET169=(Token)match(input,CARET,FOLLOW_CARET_in_bitxor2563); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET169);

            	    pushFollow(FOLLOW_bitand_in_bitxor2567);
            	    r=bitand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, bitxor
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
            	    // 252:25: -> ^( BITXOR $bitxor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:252:28: ^( BITXOR $bitxor $r)
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
            	    break loop48;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:1: bitand : ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* ;
    public final EulangParser.bitand_return bitand() throws RecognitionException {
        EulangParser.bitand_return retval = new EulangParser.bitand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP171=null;
        EulangParser.shift_return r = null;

        EulangParser.shift_return shift170 = null;


        CommonTree AMP171_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:7: ( ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:9: ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:9: ( shift -> shift )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:11: shift
            {
            pushFollow(FOLLOW_shift_in_bitand2592);
            shift170=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(shift170.getTree());


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
            // 254:22: -> shift
            {
                adaptor.addChild(root_0, stream_shift.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:7: ( AMP r= shift -> ^( BITAND $bitand $r) )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==AMP) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:9: AMP r= shift
            	    {
            	    AMP171=(Token)match(input,AMP,FOLLOW_AMP_in_bitand2620); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP171);

            	    pushFollow(FOLLOW_shift_in_bitand2624);
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
            	    // 255:22: -> ^( BITAND $bitand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:255:25: ^( BITAND $bitand $r)
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
            	    break loop49;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:1: shift : ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT173=null;
        Token RSHIFT174=null;
        Token URSHIFT175=null;
        EulangParser.factor_return r = null;

        EulangParser.factor_return factor172 = null;


        CommonTree LSHIFT173_tree=null;
        CommonTree RSHIFT174_tree=null;
        CommonTree URSHIFT175_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:6: ( ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:9: ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:9: ( factor -> factor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:258:11: factor
            {
            pushFollow(FOLLOW_factor_in_shift2651);
            factor172=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(factor172.getTree());


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
            // 258:25: -> factor
            {
                adaptor.addChild(root_0, stream_factor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:7: ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) )*
            loop50:
            do {
                int alt50=4;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt50=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt50=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt50=3;
                    }
                    break;

                }

                switch (alt50) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:11: LSHIFT r= factor
            	    {
            	    LSHIFT173=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift2685); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT173);

            	    pushFollow(FOLLOW_factor_in_shift2689);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: LSHIFT, r, shift
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
            	    // 259:29: -> ^( LSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:32: ^( LSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:11: RSHIFT r= factor
            	    {
            	    RSHIFT174=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift2718); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT174);

            	    pushFollow(FOLLOW_factor_in_shift2722);
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
            	    // 260:29: -> ^( RSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:32: ^( RSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:11: URSHIFT r= factor
            	    {
            	    URSHIFT175=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift2750); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT175);

            	    pushFollow(FOLLOW_factor_in_shift2754);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: URSHIFT, shift, r
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
            	    // 261:30: -> ^( URSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:261:33: ^( URSHIFT $shift $r)
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
            	    break loop50;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:264:1: factor : ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS177=null;
        Token MINUS178=null;
        EulangParser.term_return r = null;

        EulangParser.term_return term176 = null;


        CommonTree PLUS177_tree=null;
        CommonTree MINUS178_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:5: ( ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:7: ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:7: ( term -> term )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:9: term
            {
            pushFollow(FOLLOW_term_in_factor2796);
            term176=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(term176.getTree());


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
            // 265:27: -> term
            {
                adaptor.addChild(root_0, stream_term.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:9: ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            loop51:
            do {
                int alt51=3;
                int LA51_0 = input.LA(1);

                if ( (LA51_0==PLUS) ) {
                    alt51=1;
                }
                else if ( (LA51_0==MINUS) && (synpred3_Eulang())) {
                    alt51=2;
                }


                switch (alt51) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:13: PLUS r= term
            	    {
            	    PLUS177=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor2829); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS177);

            	    pushFollow(FOLLOW_term_in_factor2833);
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
            	    // 266:33: -> ^( ADD $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:266:36: ^( ADD $factor $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:267:12: ( MINUS term )=> MINUS r= term
            	    {
            	    MINUS178=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor2875); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS178);

            	    pushFollow(FOLLOW_term_in_factor2879);
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
            	    // 267:49: -> ^( SUB $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:267:52: ^( SUB $factor $r)
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
            	    break loop51;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:1: term : ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR180=null;
        Token SLASH181=null;
        Token BACKSLASH182=null;
        Token PERCENT183=null;
        Token UMOD184=null;
        EulangParser.unary_return r = null;

        EulangParser.unary_return unary179 = null;


        CommonTree STAR180_tree=null;
        CommonTree SLASH181_tree=null;
        CommonTree BACKSLASH182_tree=null;
        CommonTree PERCENT183_tree=null;
        CommonTree UMOD184_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_PERCENT=new RewriteRuleTokenStream(adaptor,"token PERCENT");
        RewriteRuleTokenStream stream_UMOD=new RewriteRuleTokenStream(adaptor,"token UMOD");
        RewriteRuleTokenStream stream_BACKSLASH=new RewriteRuleTokenStream(adaptor,"token BACKSLASH");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:6: ( ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:8: ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:8: ( unary -> unary )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:10: unary
            {
            pushFollow(FOLLOW_unary_in_term2924);
            unary179=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(unary179.getTree());


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
            // 271:33: -> unary
            {
                adaptor.addChild(root_0, stream_unary.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:9: ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | BACKSLASH r= unary -> ^( UDIV $term $r) | PERCENT r= unary -> ^( MOD $term $r) | UMOD r= unary -> ^( UMOD $term $r) )*
            loop52:
            do {
                int alt52=6;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==STAR) && (synpred4_Eulang())) {
                    alt52=1;
                }
                else if ( (LA52_0==SLASH) ) {
                    alt52=2;
                }
                else if ( (LA52_0==BACKSLASH) ) {
                    alt52=3;
                }
                else if ( (LA52_0==PERCENT) ) {
                    alt52=4;
                }
                else if ( (LA52_0==UMOD) ) {
                    alt52=5;
                }


                switch (alt52) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:11: ( STAR unary )=> STAR r= unary
            	    {
            	    STAR180=(Token)match(input,STAR,FOLLOW_STAR_in_term2968); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR180);

            	    pushFollow(FOLLOW_unary_in_term2972);
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
            	    // 272:52: -> ^( MUL $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:55: ^( MUL $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:273:11: SLASH r= unary
            	    {
            	    SLASH181=(Token)match(input,SLASH,FOLLOW_SLASH_in_term3008); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH181);

            	    pushFollow(FOLLOW_unary_in_term3012);
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
            	    // 273:36: -> ^( DIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:273:39: ^( DIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:274:11: BACKSLASH r= unary
            	    {
            	    BACKSLASH182=(Token)match(input,BACKSLASH,FOLLOW_BACKSLASH_in_term3047); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BACKSLASH.add(BACKSLASH182);

            	    pushFollow(FOLLOW_unary_in_term3051);
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
            	    // 274:40: -> ^( UDIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:274:43: ^( UDIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:11: PERCENT r= unary
            	    {
            	    PERCENT183=(Token)match(input,PERCENT,FOLLOW_PERCENT_in_term3086); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERCENT.add(PERCENT183);

            	    pushFollow(FOLLOW_unary_in_term3090);
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
            	    // 275:38: -> ^( MOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:41: ^( MOD $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:11: UMOD r= unary
            	    {
            	    UMOD184=(Token)match(input,UMOD,FOLLOW_UMOD_in_term3125); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UMOD.add(UMOD184);

            	    pushFollow(FOLLOW_unary_in_term3129);
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
            	    // 276:35: -> ^( UMOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:38: ^( UMOD $term $r)
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
            	    break loop52;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:1: unary : ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | EXCL u= unary -> ^( NOT $u) | TILDE u= unary -> ^( INV $u) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS186=null;
        Token EXCL187=null;
        Token TILDE188=null;
        EulangParser.unary_return u = null;

        EulangParser.atom_return atom185 = null;


        CommonTree MINUS186_tree=null;
        CommonTree EXCL187_tree=null;
        CommonTree TILDE188_tree=null;
        RewriteRuleTokenStream stream_EXCL=new RewriteRuleTokenStream(adaptor,"token EXCL");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:6: ( ( atom -> atom ) | MINUS u= unary -> ^( NEG $u) | EXCL u= unary -> ^( NOT $u) | TILDE u= unary -> ^( INV $u) )
            int alt53=4;
            switch ( input.LA(1) ) {
            case CODE:
            case ID:
            case COLON:
            case LPAREN:
            case STAR:
            case NUMBER:
            case FALSE:
            case TRUE:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case COLONS:
                {
                alt53=1;
                }
                break;
            case MINUS:
                {
                alt53=2;
                }
                break;
            case EXCL:
                {
                alt53=3;
                }
                break;
            case TILDE:
                {
                alt53=4;
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:11: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:11: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:281:13: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary3206);
                    atom185=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom185.getTree());


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
                    // 281:25: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:9: MINUS u= unary
                    {
                    MINUS186=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary3237); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS186);

                    pushFollow(FOLLOW_unary_in_unary3241);
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
                    // 282:23: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:26: ^( NEG $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:9: EXCL u= unary
                    {
                    EXCL187=(Token)match(input,EXCL,FOLLOW_EXCL_in_unary3261); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EXCL.add(EXCL187);

                    pushFollow(FOLLOW_unary_in_unary3265);
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
                    // 283:26: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:29: ^( NOT $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:9: TILDE u= unary
                    {
                    TILDE188=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary3289); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE188);

                    pushFollow(FOLLOW_unary_in_unary3293);
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
                    // 284:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:30: ^( INV $u)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:1: atom options {k=2; } : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef | LPAREN assignExpr RPAREN -> assignExpr | code -> code );
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER189=null;
        Token FALSE190=null;
        Token TRUE191=null;
        Token CHAR_LITERAL192=null;
        Token STRING_LITERAL193=null;
        Token STAR194=null;
        Token LPAREN197=null;
        Token RPAREN199=null;
        EulangParser.funcCall_return f = null;

        EulangParser.funcCall_return funcCall195 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef196 = null;

        EulangParser.assignExpr_return assignExpr198 = null;

        EulangParser.code_return code200 = null;


        CommonTree NUMBER189_tree=null;
        CommonTree FALSE190_tree=null;
        CommonTree TRUE191_tree=null;
        CommonTree CHAR_LITERAL192_tree=null;
        CommonTree STRING_LITERAL193_tree=null;
        CommonTree STAR194_tree=null;
        CommonTree LPAREN197_tree=null;
        CommonTree RPAREN199_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_code=new RewriteRuleSubtreeStream(adaptor,"rule code");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_funcCall=new RewriteRuleSubtreeStream(adaptor,"rule funcCall");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:23: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef | LPAREN assignExpr RPAREN -> assignExpr | code -> code )
            int alt54=10;
            alt54 = dfa54.predict(input);
            switch (alt54) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:287:7: NUMBER
                    {
                    NUMBER189=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom3330); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER189);



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
                    // 287:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:287:42: ^( LIT NUMBER )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:9: FALSE
                    {
                    FALSE190=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom3373); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE190);



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
                    // 288:39: -> ^( LIT FALSE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:42: ^( LIT FALSE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:289:9: TRUE
                    {
                    TRUE191=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom3415); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE191);



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
                    // 289:39: -> ^( LIT TRUE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:289:42: ^( LIT TRUE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:290:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL192=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom3458); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL192);



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
                    // 290:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:290:42: ^( LIT CHAR_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:9: STRING_LITERAL
                    {
                    STRING_LITERAL193=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom3493); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL193);



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
                    // 291:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:291:42: ^( LIT STRING_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:9: ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall
                    {
                    STAR194=(Token)match(input,STAR,FOLLOW_STAR_in_atom3537); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STAR.add(STAR194);

                    pushFollow(FOLLOW_funcCall_in_atom3541);
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
                    // 292:57: -> ^( INLINE $f)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:60: ^( INLINE $f)
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
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:9: ( idOrScopeRef LPAREN )=> funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atom3570);
                    funcCall195=funcCall();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_funcCall.add(funcCall195.getTree());


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
                    // 293:46: -> funcCall
                    {
                        adaptor.addChild(root_0, stream_funcCall.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:9: idOrScopeRef
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_idOrScopeRef_in_atom3586);
                    idOrScopeRef196=idOrScopeRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, idOrScopeRef196.getTree());

                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:9: LPAREN assignExpr RPAREN
                    {
                    LPAREN197=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom3596); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN197);

                    pushFollow(FOLLOW_assignExpr_in_atom3598);
                    assignExpr198=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr198.getTree());
                    RPAREN199=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom3600); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN199);



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
                    // 295:48: -> assignExpr
                    {
                        adaptor.addChild(root_0, stream_assignExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:9: code
                    {
                    pushFollow(FOLLOW_code_in_atom3628);
                    code200=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_code.add(code200.getTree());


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
                    // 296:40: -> code
                    {
                        adaptor.addChild(root_0, stream_code.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:1: idOrScopeRef : ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID201=null;
        Token PERIOD202=null;
        Token ID203=null;
        Token ID204=null;
        Token PERIOD205=null;
        Token ID206=null;
        EulangParser.colons_return c = null;


        CommonTree ID201_tree=null;
        CommonTree PERIOD202_tree=null;
        CommonTree ID203_tree=null;
        CommonTree ID204_tree=null;
        CommonTree PERIOD205_tree=null;
        CommonTree ID206_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:14: ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==ID) ) {
                alt57=1;
            }
            else if ( (LA57_0==COLON||LA57_0==COLONS) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:16: ID ( PERIOD ID )*
                    {
                    ID201=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef3674); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID201);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:19: ( PERIOD ID )*
                    loop55:
                    do {
                        int alt55=2;
                        int LA55_0 = input.LA(1);

                        if ( (LA55_0==PERIOD) ) {
                            alt55=1;
                        }


                        switch (alt55) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:21: PERIOD ID
                    	    {
                    	    PERIOD202=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef3678); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD202);

                    	    ID203=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef3680); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID203);


                    	    }
                    	    break;

                    	default :
                    	    break loop55;
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
                    // 299:35: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:38: ^( IDREF ( ID )+ )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:304:9: c= colons ID ( PERIOD ID )*
                    {
                    pushFollow(FOLLOW_colons_in_idOrScopeRef3735);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID204=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef3737); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID204);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:304:21: ( PERIOD ID )*
                    loop56:
                    do {
                        int alt56=2;
                        int LA56_0 = input.LA(1);

                        if ( (LA56_0==PERIOD) ) {
                            alt56=1;
                        }


                        switch (alt56) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:304:23: PERIOD ID
                    	    {
                    	    PERIOD205=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idOrScopeRef3741); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD205);

                    	    ID206=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef3743); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID206);


                    	    }
                    	    break;

                    	default :
                    	    break loop56;
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
                    // 304:37: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:304:40: ^( IDREF ( ID )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:1: colons : ( COLON | COLONS )+ ;
    public final EulangParser.colons_return colons() throws RecognitionException {
        EulangParser.colons_return retval = new EulangParser.colons_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set207=null;

        CommonTree set207_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:8: ( ( COLON | COLONS )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:10: ( COLON | COLONS )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:10: ( COLON | COLONS )+
            int cnt58=0;
            loop58:
            do {
                int alt58=2;
                int LA58_0 = input.LA(1);

                if ( (LA58_0==COLON||LA58_0==COLONS) ) {
                    alt58=1;
                }


                switch (alt58) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            	    {
            	    set207=(Token)input.LT(1);
            	    if ( input.LA(1)==COLON||input.LA(1)==COLONS ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set207));
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
            	    if ( cnt58 >= 1 ) break loop58;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(58, input);
                        throw eee;
                }
                cnt58++;
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
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:100:7: ( LPAREN ( RPAREN | ID ) )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:100:10: LPAREN ( RPAREN | ID )
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred1_Eulang509); if (state.failed) return ;
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

    // $ANTLR start synpred2_Eulang
    public final void synpred2_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:16: ( codeStmt )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:171:17: codeStmt
        {
        pushFollow(FOLLOW_codeStmt_in_synpred2_Eulang1326);
        codeStmt();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Eulang

    // $ANTLR start synpred3_Eulang
    public final void synpred3_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:267:12: ( MINUS term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:267:14: MINUS term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred3_Eulang2868); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred3_Eulang2870);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Eulang

    // $ANTLR start synpred4_Eulang
    public final void synpred4_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:11: ( STAR unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:272:13: STAR unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred4_Eulang2961); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred4_Eulang2963);
        unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Eulang

    // $ANTLR start synpred5_Eulang
    public final void synpred5_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:9: ( STAR idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:292:11: STAR idOrScopeRef LPAREN
        {
        match(input,STAR,FOLLOW_STAR_in_synpred5_Eulang3528); if (state.failed) return ;
        pushFollow(FOLLOW_idOrScopeRef_in_synpred5_Eulang3530);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred5_Eulang3532); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Eulang

    // $ANTLR start synpred6_Eulang
    public final void synpred6_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:9: ( idOrScopeRef LPAREN )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:10: idOrScopeRef LPAREN
        {
        pushFollow(FOLLOW_idOrScopeRef_in_synpred6_Eulang3562);
        idOrScopeRef();

        state._fsp--;
        if (state.failed) return ;
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred6_Eulang3564); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Eulang

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
    protected DFA33 dfa33 = new DFA33(this);
    protected DFA34 dfa34 = new DFA34(this);
    protected DFA38 dfa38 = new DFA38(this);
    protected DFA54 dfa54 = new DFA54(this);
    static final String DFA4_eotS =
        "\14\uffff";
    static final String DFA4_eofS =
        "\14\uffff";
    static final String DFA4_minS =
        "\1\6\2\uffff\1\6\2\uffff\1\52\4\uffff\1\0";
    static final String DFA4_maxS =
        "\1\130\2\uffff\1\130\2\uffff\1\127\4\uffff\1\0";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\uffff\4\3\1\uffff";
    static final String DFA4_specialS =
        "\3\uffff\1\1\2\uffff\1\0\4\uffff\1\2}>";
    static final String[] DFA4_transitionS = {
            "\1\5\1\2\41\uffff\1\5\2\uffff\1\5\1\uffff\1\4\2\uffff\1\1\3"+
            "\uffff\1\3\24\uffff\2\5\4\uffff\7\5\1\uffff\1\5",
            "",
            "",
            "\1\5\42\uffff\1\6\2\uffff\1\5\10\uffff\1\5\1\10\1\7\22\uffff"+
            "\2\5\4\uffff\7\5\1\uffff\1\5",
            "",
            "",
            "\1\5\1\uffff\1\11\3\uffff\1\12\4\uffff\1\5\1\13\1\7\1\5\2\uffff"+
            "\25\5\7\uffff\1\5",
            "",
            "",
            "",
            "",
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
            return "97:1: toplevelvalue : ( xscope | macro | ( LPAREN ( RPAREN | ID ) )=> proto | selector | rhsExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_6 = input.LA(1);

                         
                        int index4_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_6==COLON) && (synpred1_Eulang())) {s = 9;}

                        else if ( (LA4_6==COMMA) && (synpred1_Eulang())) {s = 10;}

                        else if ( (LA4_6==RETURNS) && (synpred1_Eulang())) {s = 7;}

                        else if ( (LA4_6==RPAREN) ) {s = 11;}

                        else if ( (LA4_6==EQUALS||LA4_6==LPAREN||LA4_6==AMP||(LA4_6>=QUESTION && LA4_6<=UMOD)||LA4_6==PERIOD) ) {s = 5;}

                         
                        input.seek(index4_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_3 = input.LA(1);

                         
                        int index4_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA4_3==ID) ) {s = 6;}

                        else if ( (LA4_3==RETURNS) && (synpred1_Eulang())) {s = 7;}

                        else if ( (LA4_3==RPAREN) && (synpred1_Eulang())) {s = 8;}

                        else if ( (LA4_3==CODE||LA4_3==COLON||LA4_3==LPAREN||(LA4_3>=MINUS && LA4_3<=STAR)||(LA4_3>=EXCL && LA4_3<=STRING_LITERAL)||LA4_3==COLONS) ) {s = 5;}

                         
                        input.seek(index4_3);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA4_11 = input.LA(1);

                         
                        int index4_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Eulang()) ) {s = 10;}

                        else if ( (true) ) {s = 5;}

                         
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
    static final String DFA33_eotS =
        "\24\uffff";
    static final String DFA33_eofS =
        "\24\uffff";
    static final String DFA33_minS =
        "\1\6\2\0\2\uffff\13\0\4\uffff";
    static final String DFA33_maxS =
        "\1\130\2\0\2\uffff\13\0\4\uffff";
    static final String DFA33_acceptS =
        "\3\uffff\2\1\13\uffff\2\1\1\3\1\2";
    static final String DFA33_specialS =
        "\1\0\1\1\1\2\2\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\4\uffff}>";
    static final String[] DFA33_transitionS = {
            "\1\14\40\uffff\1\4\1\uffff\1\1\2\uffff\1\2\4\uffff\1\20\1\22"+
            "\2\uffff\1\13\3\uffff\1\3\1\21\17\uffff\1\15\1\12\4\uffff\1"+
            "\16\1\17\1\5\1\6\1\7\1\10\1\11\1\uffff\1\2",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            return "171:1: codestmtlist : ( ( codeStmt )=> ( ( codeStmt )+ ) -> ^( STMTLIST ( codeStmt )* ) | rhsExpr -> ^( STMTLIST ^( RETURN rhsExpr ) ) | -> ^( STMTLIST ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA33_0 = input.LA(1);

                         
                        int index33_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA33_0==ID) ) {s = 1;}

                        else if ( (LA33_0==COLON||LA33_0==COLONS) ) {s = 2;}

                        else if ( (LA33_0==RETURN) && (synpred2_Eulang())) {s = 3;}

                        else if ( (LA33_0==GOTO) && (synpred2_Eulang())) {s = 4;}

                        else if ( (LA33_0==NUMBER) ) {s = 5;}

                        else if ( (LA33_0==FALSE) ) {s = 6;}

                        else if ( (LA33_0==TRUE) ) {s = 7;}

                        else if ( (LA33_0==CHAR_LITERAL) ) {s = 8;}

                        else if ( (LA33_0==STRING_LITERAL) ) {s = 9;}

                        else if ( (LA33_0==STAR) ) {s = 10;}

                        else if ( (LA33_0==LPAREN) ) {s = 11;}

                        else if ( (LA33_0==CODE) ) {s = 12;}

                        else if ( (LA33_0==MINUS) ) {s = 13;}

                        else if ( (LA33_0==EXCL) ) {s = 14;}

                        else if ( (LA33_0==TILDE) ) {s = 15;}

                        else if ( (LA33_0==LBRACE) && (synpred2_Eulang())) {s = 16;}

                        else if ( (LA33_0==AT) && (synpred2_Eulang())) {s = 17;}

                        else if ( (LA33_0==RBRACE) ) {s = 18;}

                         
                        input.seek(index33_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA33_1 = input.LA(1);

                         
                        int index33_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA33_2 = input.LA(1);

                         
                        int index33_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA33_5 = input.LA(1);

                         
                        int index33_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_5);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA33_6 = input.LA(1);

                         
                        int index33_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_6);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA33_7 = input.LA(1);

                         
                        int index33_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_7);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA33_8 = input.LA(1);

                         
                        int index33_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_8);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA33_9 = input.LA(1);

                         
                        int index33_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_9);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA33_10 = input.LA(1);

                         
                        int index33_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_10);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA33_11 = input.LA(1);

                         
                        int index33_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_11);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA33_12 = input.LA(1);

                         
                        int index33_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_12);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA33_13 = input.LA(1);

                         
                        int index33_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_13);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA33_14 = input.LA(1);

                         
                        int index33_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_14);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA33_15 = input.LA(1);

                         
                        int index33_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Eulang()) ) {s = 17;}

                        else if ( (true) ) {s = 19;}

                         
                        input.seek(index33_15);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 33, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA34_eotS =
        "\17\uffff";
    static final String DFA34_eofS =
        "\17\uffff";
    static final String DFA34_minS =
        "\1\6\1\52\1\51\6\uffff\1\51\1\uffff\2\52\1\51\1\52";
    static final String DFA34_maxS =
        "\1\130\1\127\1\130\6\uffff\1\51\1\uffff\2\127\1\51\1\127";
    static final String DFA34_acceptS =
        "\3\uffff\1\3\1\4\1\5\1\6\1\7\1\1\1\uffff\1\2\4\uffff";
    static final String DFA34_specialS =
        "\17\uffff}>";
    static final String[] DFA34_transitionS = {
            "\1\5\40\uffff\1\4\1\uffff\1\1\2\uffff\1\2\4\uffff\1\6\3\uffff"+
            "\1\5\3\uffff\1\3\1\7\17\uffff\2\5\4\uffff\7\5\1\uffff\1\2",
            "\1\12\1\5\2\10\7\uffff\1\5\2\uffff\1\5\2\uffff\25\5\7\uffff"+
            "\1\11",
            "\1\13\2\uffff\1\2\53\uffff\1\2",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\14",
            "",
            "\1\12\1\5\11\uffff\1\5\2\uffff\1\5\2\uffff\25\5\7\uffff\1\15",
            "\1\12\1\5\11\uffff\1\5\2\uffff\1\5\2\uffff\25\5\7\uffff\1\11",
            "\1\16",
            "\1\12\1\5\11\uffff\1\5\2\uffff\1\5\2\uffff\25\5\7\uffff\1\15"
    };

    static final short[] DFA34_eot = DFA.unpackEncodedString(DFA34_eotS);
    static final short[] DFA34_eof = DFA.unpackEncodedString(DFA34_eofS);
    static final char[] DFA34_min = DFA.unpackEncodedStringToUnsignedChars(DFA34_minS);
    static final char[] DFA34_max = DFA.unpackEncodedStringToUnsignedChars(DFA34_maxS);
    static final short[] DFA34_accept = DFA.unpackEncodedString(DFA34_acceptS);
    static final short[] DFA34_special = DFA.unpackEncodedString(DFA34_specialS);
    static final short[][] DFA34_transition;

    static {
        int numStates = DFA34_transitionS.length;
        DFA34_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA34_transition[i] = DFA.unpackEncodedString(DFA34_transitionS[i]);
        }
    }

    class DFA34 extends DFA {

        public DFA34(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 34;
            this.eot = DFA34_eot;
            this.eof = DFA34_eof;
            this.min = DFA34_min;
            this.max = DFA34_max;
            this.accept = DFA34_accept;
            this.special = DFA34_special;
            this.transition = DFA34_transition;
        }
        public String getDescription() {
            return "176:1: codeStmt : ( varDecl SEMI -> varDecl | assignStmt SEMI -> assignStmt | returnStmt SEMI -> returnStmt | gotoStmt SEMI -> gotoStmt | rhsExpr SEMI -> ^( STMTEXPR rhsExpr ) | blockStmt -> blockStmt | labelStmt -> labelStmt );";
        }
    }
    static final String DFA38_eotS =
        "\12\uffff";
    static final String DFA38_eofS =
        "\12\uffff";
    static final String DFA38_minS =
        "\1\6\1\52\1\51\1\uffff\1\51\1\uffff\2\52\1\51\1\52";
    static final String DFA38_maxS =
        "\1\130\1\127\1\130\1\uffff\1\51\1\uffff\2\127\1\51\1\127";
    static final String DFA38_acceptS =
        "\3\uffff\1\2\1\uffff\1\1\4\uffff";
    static final String DFA38_specialS =
        "\12\uffff}>";
    static final String[] DFA38_transitionS = {
            "\1\3\42\uffff\1\1\2\uffff\1\2\10\uffff\1\3\24\uffff\2\3\4\uffff"+
            "\7\3\1\uffff\1\2",
            "\1\5\1\3\4\uffff\1\3\4\uffff\2\3\1\uffff\1\3\2\uffff\25\3\7"+
            "\uffff\1\4",
            "\1\6\2\uffff\1\2\53\uffff\1\2",
            "",
            "\1\7",
            "",
            "\1\5\1\3\4\uffff\1\3\4\uffff\2\3\1\uffff\1\3\2\uffff\25\3\7"+
            "\uffff\1\10",
            "\1\5\1\3\4\uffff\1\3\4\uffff\2\3\1\uffff\1\3\2\uffff\25\3\7"+
            "\uffff\1\4",
            "\1\11",
            "\1\5\1\3\4\uffff\1\3\4\uffff\2\3\1\uffff\1\3\2\uffff\25\3\7"+
            "\uffff\1\10"
    };

    static final short[] DFA38_eot = DFA.unpackEncodedString(DFA38_eotS);
    static final short[] DFA38_eof = DFA.unpackEncodedString(DFA38_eofS);
    static final char[] DFA38_min = DFA.unpackEncodedStringToUnsignedChars(DFA38_minS);
    static final char[] DFA38_max = DFA.unpackEncodedStringToUnsignedChars(DFA38_maxS);
    static final short[] DFA38_accept = DFA.unpackEncodedString(DFA38_acceptS);
    static final short[] DFA38_special = DFA.unpackEncodedString(DFA38_specialS);
    static final short[][] DFA38_transition;

    static {
        int numStates = DFA38_transitionS.length;
        DFA38_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA38_transition[i] = DFA.unpackEncodedString(DFA38_transitionS[i]);
        }
    }

    class DFA38 extends DFA {

        public DFA38(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 38;
            this.eot = DFA38_eot;
            this.eof = DFA38_eof;
            this.min = DFA38_min;
            this.max = DFA38_max;
            this.accept = DFA38_accept;
            this.special = DFA38_special;
            this.transition = DFA38_transition;
        }
        public String getDescription() {
            return "195:1: assignExpr : ( idOrScopeRef EQUALS assignExpr -> ^( ASSIGN idOrScopeRef assignExpr ) | rhsExpr -> rhsExpr );";
        }
    }
    static final String DFA54_eotS =
        "\55\uffff";
    static final String DFA54_eofS =
        "\7\uffff\1\15\45\uffff";
    static final String DFA54_minS =
        "\1\6\6\uffff\1\53\1\51\2\uffff\1\0\37\uffff\2\0";
    static final String DFA54_maxS =
        "\1\130\6\uffff\1\127\1\130\2\uffff\1\0\37\uffff\2\0";
    static final String DFA54_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\2\uffff\1\11\1\12\1\uffff\1\7\1"+
        "\10\37\uffff";
    static final String DFA54_specialS =
        "\1\0\6\uffff\1\1\3\uffff\1\2\37\uffff\1\3\1\4}>";
    static final String[] DFA54_transitionS = {
            "\1\12\42\uffff\1\7\2\uffff\1\10\10\uffff\1\11\25\uffff\1\6\6"+
            "\uffff\1\1\1\2\1\3\1\4\1\5\1\uffff\1\10",
            "",
            "",
            "",
            "",
            "",
            "",
            "\2\15\2\uffff\2\15\1\uffff\1\15\2\uffff\1\14\3\15\2\uffff\25"+
            "\15\7\uffff\1\13",
            "\1\53\2\uffff\1\54\53\uffff\1\54",
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
            "\1\uffff",
            "\1\uffff"
    };

    static final short[] DFA54_eot = DFA.unpackEncodedString(DFA54_eotS);
    static final short[] DFA54_eof = DFA.unpackEncodedString(DFA54_eofS);
    static final char[] DFA54_min = DFA.unpackEncodedStringToUnsignedChars(DFA54_minS);
    static final char[] DFA54_max = DFA.unpackEncodedStringToUnsignedChars(DFA54_maxS);
    static final short[] DFA54_accept = DFA.unpackEncodedString(DFA54_acceptS);
    static final short[] DFA54_special = DFA.unpackEncodedString(DFA54_specialS);
    static final short[][] DFA54_transition;

    static {
        int numStates = DFA54_transitionS.length;
        DFA54_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA54_transition[i] = DFA.unpackEncodedString(DFA54_transitionS[i]);
        }
    }

    class DFA54 extends DFA {

        public DFA54(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 54;
            this.eot = DFA54_eot;
            this.eof = DFA54_eof;
            this.min = DFA54_min;
            this.max = DFA54_max;
            this.accept = DFA54_accept;
            this.special = DFA54_special;
            this.transition = DFA54_transition;
        }
        public String getDescription() {
            return "286:1: atom options {k=2; } : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | ( STAR idOrScopeRef LPAREN )=> STAR f= funcCall -> ^( INLINE $f) | ( idOrScopeRef LPAREN )=> funcCall -> funcCall | idOrScopeRef | LPAREN assignExpr RPAREN -> assignExpr | code -> code );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA54_0 = input.LA(1);

                         
                        int index54_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA54_0==NUMBER) ) {s = 1;}

                        else if ( (LA54_0==FALSE) ) {s = 2;}

                        else if ( (LA54_0==TRUE) ) {s = 3;}

                        else if ( (LA54_0==CHAR_LITERAL) ) {s = 4;}

                        else if ( (LA54_0==STRING_LITERAL) ) {s = 5;}

                        else if ( (LA54_0==STAR) && (synpred5_Eulang())) {s = 6;}

                        else if ( (LA54_0==ID) ) {s = 7;}

                        else if ( (LA54_0==COLON||LA54_0==COLONS) ) {s = 8;}

                        else if ( (LA54_0==LPAREN) ) {s = 9;}

                        else if ( (LA54_0==CODE) ) {s = 10;}

                         
                        input.seek(index54_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA54_7 = input.LA(1);

                         
                        int index54_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA54_7==PERIOD) ) {s = 11;}

                        else if ( (LA54_7==LPAREN) && (synpred6_Eulang())) {s = 12;}

                        else if ( (LA54_7==EOF||(LA54_7>=SEMI && LA54_7<=COLON)||(LA54_7>=RBRACKET && LA54_7<=COMMA)||LA54_7==RBRACE||(LA54_7>=RPAREN && LA54_7<=AMP)||(LA54_7>=QUESTION && LA54_7<=UMOD)) ) {s = 13;}

                         
                        input.seek(index54_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA54_11 = input.LA(1);

                         
                        int index54_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 13;}

                         
                        input.seek(index54_11);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA54_43 = input.LA(1);

                         
                        int index54_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 13;}

                         
                        input.seek(index54_43);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA54_44 = input.LA(1);

                         
                        int index54_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 13;}

                         
                        input.seek(index54_44);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 54, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog275 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog277 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts306 = new BitSet(new long[]{0x0022120000000042L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_ID_in_toplevelstat340 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat342 = new BitSet(new long[]{0x00225200000000C0L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat344 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat369 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_toplevelstat371 = new BitSet(new long[]{0x0000120000000040L,0x0000000001000000L});
    public static final BitSet FOLLOW_type_in_toplevelstat373 = new BitSet(new long[]{0x00000C0000000000L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelstat376 = new BitSet(new long[]{0x00225200000000C0L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_toplevelvalue_in_toplevelstat378 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelstat408 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelstat410 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat412 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstat436 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstat472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_toplevelvalue498 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_toplevelvalue523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector557 = new BitSet(new long[]{0x00289200000000C0L,0x00000000017C0800L});
    public static final BitSet FOLLOW_selectors_in_selector559 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector561 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors587 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_selectors591 = new BitSet(new long[]{0x00281200000000C0L,0x00000000017C0800L});
    public static final BitSet FOLLOW_selectoritem_in_selectors593 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_selectors598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listCompr_in_selectoritem623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_selectoritem627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_selectoritem631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope642 = new BitSet(new long[]{0x0022120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope644 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr673 = new BitSet(new long[]{0x0008100000000000L});
    public static final BitSet FOLLOW_COLON_in_listCompr676 = new BitSet(new long[]{0x00201200000000C0L,0x00000000017C0800L});
    public static final BitSet FOLLOW_listiterable_in_listCompr678 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn710 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_idlist_in_forIn712 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_IN_in_forIn714 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_list_in_forIn716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist741 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_idlist744 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_ID_in_idlist746 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_code_in_listiterable775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_macro_in_listiterable779 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_proto_in_listiterable783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list798 = new BitSet(new long[]{0x0022D200000000C0L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_listitems_in_list800 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_list802 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems832 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems836 = new BitSet(new long[]{0x00225200000000C0L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_listitem_in_listitems838 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_listitems843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem869 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_code887 = new BitSet(new long[]{0x0022000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_code891 = new BitSet(new long[]{0x00C0020000000000L});
    public static final BitSet FOLLOW_optargdefs_in_code893 = new BitSet(new long[]{0x00C0000000000000L});
    public static final BitSet FOLLOW_xreturns_in_code895 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_code898 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_code904 = new BitSet(new long[]{0x0626128000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_codestmtlist_in_code906 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_code908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MACRO_in_macro943 = new BitSet(new long[]{0x0022000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_macro947 = new BitSet(new long[]{0x00C0020000000000L});
    public static final BitSet FOLLOW_optargdefs_in_macro949 = new BitSet(new long[]{0x00C0000000000000L});
    public static final BitSet FOLLOW_xreturns_in_macro951 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_macro954 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_macro960 = new BitSet(new long[]{0x0626128000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_codestmtlist_in_macro962 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_macro964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto998 = new BitSet(new long[]{0x00C0020000000000L});
    public static final BitSet FOLLOW_argdefs_in_proto1000 = new BitSet(new long[]{0x00C0000000000000L});
    public static final BitSet FOLLOW_xreturns_in_proto1002 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_proto1005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdef_in_argdefs1047 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1051 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_argdef_in_argdefs1053 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_argdefs1057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdef1101 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_COLON_in_argdef1104 = new BitSet(new long[]{0x0000120000000040L,0x0000000001000000L});
    public static final BitSet FOLLOW_type_in_argdef1106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_xreturns1133 = new BitSet(new long[]{0x0000120000000040L,0x0000000001000000L});
    public static final BitSet FOLLOW_type_in_xreturns1135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_optargdef_in_optargdefs1155 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_optargdefs1159 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_optargdef_in_optargdefs1161 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_optargdefs1165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_optargdef1209 = new BitSet(new long[]{0x0000140000000002L});
    public static final BitSet FOLLOW_COLON_in_optargdef1212 = new BitSet(new long[]{0x0000120000000040L,0x0000000001000000L});
    public static final BitSet FOLLOW_type_in_optargdef1214 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_EQUALS_in_optargdef1219 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_rhsExpr_in_optargdef1223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_type1260 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_AMP_in_type1275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_type1301 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_proto_in_type1303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist1333 = new BitSet(new long[]{0x0622128000000042L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_rhsExpr_in_codestmtlist1354 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmt1405 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_codeStmt1407 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_codeStmt1423 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_codeStmt1425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_returnStmt_in_codeStmt1441 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_codeStmt1443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_codeStmt1459 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_codeStmt1461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_codeStmt1479 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_SEMI_in_codeStmt1481 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blockStmt_in_codeStmt1504 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_labelStmt_in_codeStmt1526 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1548 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_varDecl1550 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl1580 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_varDecl1582 = new BitSet(new long[]{0x0000120000000040L,0x0000000001000000L});
    public static final BitSet FOLLOW_type_in_varDecl1584 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl1587 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_assignExpr_in_varDecl1589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_returnStmt1618 = new BitSet(new long[]{0x0020120000000042L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_assignExpr_in_returnStmt1620 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignStmt1655 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt1657 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_assignExpr_in_assignStmt1659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_assignExpr1695 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr1697 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr1699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr1724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_labelStmt1768 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_ID_in_labelStmt1770 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_labelStmt1772 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GOTO_in_gotoStmt1808 = new BitSet(new long[]{0x0000120000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_gotoStmt1810 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_gotoStmt1814 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_rhsExpr_in_gotoStmt1816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_blockStmt1852 = new BitSet(new long[]{0x0626128000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_codestmtlist_in_blockStmt1854 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_blockStmt1856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_rhsExpr1881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_funcCall1927 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_funcCall1929 = new BitSet(new long[]{0x0060120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_arglist_in_funcCall1931 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_funcCall1933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist1964 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist1968 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_arg_in_arglist1970 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_COMMA_in_arglist1974 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg2023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logor_in_cond2071 = new BitSet(new long[]{0x0800000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_cond2088 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_logor_in_cond2092 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_cond2094 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_logor_in_cond2098 = new BitSet(new long[]{0x0800000000000002L});
    public static final BitSet FOLLOW_logand_in_logor2128 = new BitSet(new long[]{0x1000000000000002L});
    public static final BitSet FOLLOW_COMPOR_in_logor2145 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_logand_in_logor2149 = new BitSet(new long[]{0x1000000000000002L});
    public static final BitSet FOLLOW_comp_in_logand2180 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_COMPAND_in_logand2196 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_comp_in_logand2200 = new BitSet(new long[]{0x2000000000000002L});
    public static final BitSet FOLLOW_bitor_in_comp2250 = new BitSet(new long[]{0xC000000000000002L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPEQ_in_comp2283 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitor_in_comp2287 = new BitSet(new long[]{0xC000000000000002L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPNE_in_comp2309 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitor_in_comp2313 = new BitSet(new long[]{0xC000000000000002L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPLE_in_comp2335 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitor_in_comp2339 = new BitSet(new long[]{0xC000000000000002L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPGE_in_comp2364 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitor_in_comp2368 = new BitSet(new long[]{0xC000000000000002L,0x000000000000000FL});
    public static final BitSet FOLLOW_LESS_in_comp2393 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitor_in_comp2397 = new BitSet(new long[]{0xC000000000000002L,0x000000000000000FL});
    public static final BitSet FOLLOW_GREATER_in_comp2423 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitor_in_comp2427 = new BitSet(new long[]{0xC000000000000002L,0x000000000000000FL});
    public static final BitSet FOLLOW_bitxor_in_bitor2477 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_BAR_in_bitor2505 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitxor_in_bitor2509 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_bitand_in_bitxor2535 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_CARET_in_bitxor2563 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_bitand_in_bitxor2567 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_shift_in_bitand2592 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_AMP_in_bitand2620 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_shift_in_bitand2624 = new BitSet(new long[]{0x0100000000000002L});
    public static final BitSet FOLLOW_factor_in_shift2651 = new BitSet(new long[]{0x0000000000000002L,0x00000000000001C0L});
    public static final BitSet FOLLOW_LSHIFT_in_shift2685 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_factor_in_shift2689 = new BitSet(new long[]{0x0000000000000002L,0x00000000000001C0L});
    public static final BitSet FOLLOW_RSHIFT_in_shift2718 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_factor_in_shift2722 = new BitSet(new long[]{0x0000000000000002L,0x00000000000001C0L});
    public static final BitSet FOLLOW_URSHIFT_in_shift2750 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_factor_in_shift2754 = new BitSet(new long[]{0x0000000000000002L,0x00000000000001C0L});
    public static final BitSet FOLLOW_term_in_factor2796 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000600L});
    public static final BitSet FOLLOW_PLUS_in_factor2829 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_term_in_factor2833 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000600L});
    public static final BitSet FOLLOW_MINUS_in_factor2875 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_term_in_factor2879 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000600L});
    public static final BitSet FOLLOW_unary_in_term2924 = new BitSet(new long[]{0x0000000000000002L,0x000000000000F800L});
    public static final BitSet FOLLOW_STAR_in_term2968 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_term2972 = new BitSet(new long[]{0x0000000000000002L,0x000000000000F800L});
    public static final BitSet FOLLOW_SLASH_in_term3008 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_term3012 = new BitSet(new long[]{0x0000000000000002L,0x000000000000F800L});
    public static final BitSet FOLLOW_BACKSLASH_in_term3047 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_term3051 = new BitSet(new long[]{0x0000000000000002L,0x000000000000F800L});
    public static final BitSet FOLLOW_PERCENT_in_term3086 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_term3090 = new BitSet(new long[]{0x0000000000000002L,0x000000000000F800L});
    public static final BitSet FOLLOW_UMOD_in_term3125 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_term3129 = new BitSet(new long[]{0x0000000000000002L,0x000000000000F800L});
    public static final BitSet FOLLOW_atom_in_unary3206 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_unary3237 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_unary3241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXCL_in_unary3261 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_unary3265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary3289 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_unary3293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom3330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_atom3373 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_atom3415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom3458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom3493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_atom3537 = new BitSet(new long[]{0x0000120000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_funcCall_in_atom3541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atom3570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_atom3586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_atom3596 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_assignExpr_in_atom3598 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_atom3600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_atom3628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef3674 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef3678 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef3680 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_colons_in_idOrScopeRef3735 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef3737 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_PERIOD_in_idOrScopeRef3741 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef3743 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
    public static final BitSet FOLLOW_set_in_colons3774 = new BitSet(new long[]{0x0000100000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred1_Eulang509 = new BitSet(new long[]{0x0040020000000000L});
    public static final BitSet FOLLOW_set_in_synpred1_Eulang511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_synpred2_Eulang1326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred3_Eulang2868 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_term_in_synpred3_Eulang2870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred4_Eulang2961 = new BitSet(new long[]{0x0020120000000040L,0x00000000017F0C00L});
    public static final BitSet FOLLOW_unary_in_synpred4_Eulang2963 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred5_Eulang3528 = new BitSet(new long[]{0x0000120000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred5_Eulang3530 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred5_Eulang3532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_synpred6_Eulang3562 = new BitSet(new long[]{0x0020000000000000L});
    public static final BitSet FOLLOW_LPAREN_in_synpred6_Eulang3564 = new BitSet(new long[]{0x0000000000000002L});

}