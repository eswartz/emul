// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-06-16 20:51:59

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCOPE", "ADDSCOPE", "EXTENDSCOPE", "ATTRS", "LIST_COMPREHENSION", "CODE", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "ALLOC", "ALLOC_TUPLE", "ASSIGN", "DEFINE", "REDEF", "EXPR", "LIST", "TYPE", "STMTEXPR", "CONDLIST", "CONDTEST", "CALL", "INLINE", "CAST", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NEG", "INV", "POSTINC", "POSTDEC", "PREINC", "PREDEC", "LIT", "IDREF", "IDLIST", "LABEL", "GOTO", "BLOCK", "TUPLE", "TUPLETYPE", "LABELSTMT", "BINDING", "FIELDREF", "ARRAY", "INDEX", "POINTER", "DEREF", "ADDRREF", "ADDROF", "SIZEOF", "TYPEOF", "INITEXPR", "INITLIST", "INSTANCE", "GENERIC", "SEMI", "FORWARD", "ID", "COMMA", "COLON_EQUALS", "COLON", "EQUALS", "PLUS", "LPAREN", "LBRACE", "RBRACE", "RPAREN", "PLUS_EQ", "LBRACKET", "RBRACKET", "EQUALS_COLON", "FOR", "IN", "ATTR", "ARROW", "NIL", "QUESTION", "CARET", "MINUS_EQ", "STAR_EQ", "SLASH_EQ", "REM_EQ", "UDIV_EQ", "UREM_EQ", "MOD_EQ", "AND_EQ", "OR_EQ", "XOR_EQ", "LSHIFT_EQ", "RSHIFT_EQ", "URSHIFT_EQ", "CLSHIFT_EQ", "CRSHIFT_EQ", "PERIOD", "DO", "WHILE", "REPEAT", "AND", "BY", "AT", "BREAK", "ATSIGN", "IF", "THEN", "ELIF", "ELSE", "FI", "OR", "NOT", "COMPEQ", "COMPNE", "COMPLE", "COMPGE", "COMPULE", "COMPUGE", "LESS", "ULESS", "GREATER", "UGREATER", "BAR", "TILDE", "AMP", "LSHIFT", "RSHIFT", "URSHIFT", "CRSHIFT", "CLSHIFT", "MINUS", "STAR", "SLASH", "REM", "UREM", "PLUSPLUS", "MINUSMINUS", "NUMBER", "CHAR_LITERAL", "STRING_LITERAL", "AS", "FALSE", "TRUE", "COLONS", "DATA", "COLON_COLON_EQUALS", "STATIC", "HASH", "POINTS", "BAR_BAR", "SELECT", "WHEN", "UNTIL", "WITH", "END", "IDSUFFIX", "LETTERLIKE", "DIGIT", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT"
    };
    public static final int CAST=28;
    public static final int CONDTEST=25;
    public static final int STAR=141;
    public static final int WHILE=108;
    public static final int GENERIC=67;
    public static final int MOD=38;
    public static final int POINTER=58;
    public static final int LSHIFT_EQ=101;
    public static final int PREDEC=44;
    public static final int REM_EQ=94;
    public static final int DEREF=59;
    public static final int MINUSMINUS=146;
    public static final int DO=107;
    public static final int ARGLIST=12;
    public static final int EQUALS=74;
    public static final int NOT=121;
    public static final int EOF=-1;
    public static final int BREAK=113;
    public static final int TYPE=22;
    public static final int CODE=9;
    public static final int LBRACKET=81;
    public static final int TUPLE=51;
    public static final int RPAREN=79;
    public static final int STRING_LITERAL=149;
    public static final int GREATER=130;
    public static final int ADDRREF=60;
    public static final int ADDSCOPE=5;
    public static final int UREM_EQ=96;
    public static final int EXTENDSCOPE=6;
    public static final int COMPLE=124;
    public static final int AND_EQ=98;
    public static final int CARET=90;
    public static final int LESS=128;
    public static final int XOR_EQ=100;
    public static final int INITEXPR=64;
    public static final int INITLIST=65;
    public static final int ATSIGN=114;
    public static final int GOTO=49;
    public static final int SELECT=160;
    public static final int CLSHIFT_EQ=104;
    public static final int ARRAY=56;
    public static final int LABELSTMT=53;
    public static final int CRSHIFT=138;
    public static final int RBRACE=78;
    public static final int STMTEXPR=23;
    public static final int STATIC=156;
    public static final int PERIOD=106;
    public static final int LSHIFT=135;
    public static final int INV=40;
    public static final int ADDROF=61;
    public static final int ELSE=118;
    public static final int NUMBER=147;
    public static final int UDIV=37;
    public static final int LIT=45;
    public static final int CRSHIFT_EQ=105;
    public static final int UDIV_EQ=95;
    public static final int LIST=21;
    public static final int PLUS_EQ=80;
    public static final int MUL=35;
    public static final int RSHIFT_EQ=102;
    public static final int ARGDEF=13;
    public static final int FI=119;
    public static final int MINUS_EQ=91;
    public static final int ELIF=117;
    public static final int WS=169;
    public static final int OR_EQ=99;
    public static final int BITOR=31;
    public static final int NIL=88;
    public static final int TYPEOF=63;
    public static final int UNTIL=162;
    public static final int STMTLIST=10;
    public static final int OR=120;
    public static final int SIZEOF=62;
    public static final int ALLOC=15;
    public static final int IDLIST=47;
    public static final int REPEAT=109;
    public static final int INLINE=27;
    public static final int CALL=26;
    public static final int POSTINC=41;
    public static final int END=164;
    public static final int FALSE=151;
    public static final int COMPULE=126;
    public static final int POSTDEC=42;
    public static final int MOD_EQ=97;
    public static final int REDEF=19;
    public static final int BINDING=54;
    public static final int FORWARD=69;
    public static final int BAR_BAR=159;
    public static final int AMP=134;
    public static final int POINTS=158;
    public static final int PLUSPLUS=145;
    public static final int UGREATER=131;
    public static final int LBRACE=77;
    public static final int MULTI_COMMENT=171;
    public static final int FIELDREF=55;
    public static final int FOR=84;
    public static final int SUB=34;
    public static final int AND=110;
    public static final int ID=70;
    public static final int DEFINE=18;
    public static final int UREM=144;
    public static final int BITAND=30;
    public static final int LPAREN=76;
    public static final int IF=115;
    public static final int COLONS=153;
    public static final int COLON_COLON_EQUALS=155;
    public static final int AT=112;
    public static final int AS=150;
    public static final int INDEX=57;
    public static final int CONDLIST=24;
    public static final int IDSUFFIX=165;
    public static final int SLASH=142;
    public static final int EXPR=20;
    public static final int THEN=116;
    public static final int IN=85;
    public static final int SCOPE=4;
    public static final int COMMA=71;
    public static final int PREINC=43;
    public static final int BITXOR=32;
    public static final int TILDE=133;
    public static final int PLUS=75;
    public static final int SINGLE_COMMENT=170;
    public static final int DIGIT=167;
    public static final int RBRACKET=82;
    public static final int RSHIFT=136;
    public static final int ATTR=86;
    public static final int WITH=163;
    public static final int ADD=33;
    public static final int EQUALS_COLON=83;
    public static final int COMPGE=125;
    public static final int URSHIFT_EQ=103;
    public static final int ULESS=129;
    public static final int BY=111;
    public static final int LETTERLIKE=166;
    public static final int LIST_COMPREHENSION=8;
    public static final int HASH=157;
    public static final int CLSHIFT=139;
    public static final int ATTRS=7;
    public static final int STAR_EQ=92;
    public static final int REM=143;
    public static final int MINUS=140;
    public static final int TRUE=152;
    public static final int SEMI=68;
    public static final int REF=14;
    public static final int COLON=73;
    public static final int TUPLETYPE=52;
    public static final int COLON_EQUALS=72;
    public static final int NEWLINE=168;
    public static final int QUESTION=89;
    public static final int CHAR_LITERAL=148;
    public static final int LABEL=48;
    public static final int WHEN=161;
    public static final int INSTANCE=66;
    public static final int BLOCK=50;
    public static final int NEG=39;
    public static final int ASSIGN=17;
    public static final int URSHIFT=137;
    public static final int ARROW=87;
    public static final int COMPEQ=122;
    public static final int IDREF=46;
    public static final int DIV=36;
    public static final int COND=29;
    public static final int PROTO=11;
    public static final int COMPNE=123;
    public static final int DATA=154;
    public static final int BAR=132;
    public static final int COMPUGE=127;
    public static final int ALLOC_TUPLE=16;
    public static final int SLASH_EQ=93;

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:130:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog445);
            toplevelstmts1=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog447); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:1: toplevelstmts : ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstat_return toplevelstat3 = null;


        RewriteRuleSubtreeStream stream_toplevelstat=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:14: ( ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:16: ( toplevelstat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:16: ( toplevelstat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=FORWARD && LA1_0<=ID)||LA1_0==COLON||(LA1_0>=LPAREN && LA1_0<=LBRACE)||LA1_0==COLONS) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:16: toplevelstat
            	    {
            	    pushFollow(FOLLOW_toplevelstat_in_toplevelstmts476);
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
            // 133:35: -> ^( STMTLIST ( toplevelstat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:38: ^( STMTLIST ( toplevelstat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:133:49: ( toplevelstat )*
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

    public static class toplevelstmtsNoAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstmtsNoAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:1: toplevelstmtsNoAlloc : ( toplevelstatNoAlloc )* -> ^( STMTLIST ( toplevelstatNoAlloc )* ) ;
    public final EulangParser.toplevelstmtsNoAlloc_return toplevelstmtsNoAlloc() throws RecognitionException {
        EulangParser.toplevelstmtsNoAlloc_return retval = new EulangParser.toplevelstmtsNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc4 = null;


        RewriteRuleSubtreeStream stream_toplevelstatNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstatNoAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:21: ( ( toplevelstatNoAlloc )* -> ^( STMTLIST ( toplevelstatNoAlloc )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:23: ( toplevelstatNoAlloc )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:23: ( toplevelstatNoAlloc )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=FORWARD && LA2_0<=ID)||LA2_0==COLON||LA2_0==LBRACE||LA2_0==COLONS) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:23: toplevelstatNoAlloc
            	    {
            	    pushFollow(FOLLOW_toplevelstatNoAlloc_in_toplevelstmtsNoAlloc503);
            	    toplevelstatNoAlloc4=toplevelstatNoAlloc();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_toplevelstatNoAlloc.add(toplevelstatNoAlloc4.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);



            // AST REWRITE
            // elements: toplevelstatNoAlloc
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 135:49: -> ^( STMTLIST ( toplevelstatNoAlloc )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:52: ^( STMTLIST ( toplevelstatNoAlloc )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:135:63: ( toplevelstatNoAlloc )*
                while ( stream_toplevelstatNoAlloc.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstatNoAlloc.nextTree());

                }
                stream_toplevelstatNoAlloc.reset();

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
    // $ANTLR end "toplevelstmtsNoAlloc"

    public static class toplevelstat_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstat"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:1: toplevelstat : ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc | toplevelCode ( SEMI )? -> toplevelCode );
    public final EulangParser.toplevelstat_return toplevelstat() throws RecognitionException {
        EulangParser.toplevelstat_return retval = new EulangParser.toplevelstat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI7=null;
        Token SEMI9=null;
        EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc5 = null;

        EulangParser.toplevelAlloc_return toplevelAlloc6 = null;

        EulangParser.toplevelCode_return toplevelCode8 = null;


        CommonTree SEMI7_tree=null;
        CommonTree SEMI9_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_toplevelstatNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstatNoAlloc");
        RewriteRuleSubtreeStream stream_toplevelCode=new RewriteRuleSubtreeStream(adaptor,"rule toplevelCode");
        RewriteRuleSubtreeStream stream_toplevelAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:14: ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc | toplevelCode ( SEMI )? -> toplevelCode )
            int alt4=3;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==EQUALS||LA4_1==PLUS_EQ||LA4_1==EQUALS_COLON||LA4_1==PERIOD) ) {
                    alt4=1;
                }
                else if ( ((LA4_1>=COMMA && LA4_1<=COLON)||LA4_1==ATTR) ) {
                    alt4=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;
                }
                }
                break;
            case FORWARD:
            case COLON:
            case LBRACE:
            case COLONS:
                {
                alt4=1;
                }
                break;
            case LPAREN:
                {
                int LA4_3 = input.LA(2);

                if ( (LA4_3==LBRACE) ) {
                    alt4=3;
                }
                else if ( (LA4_3==ID||LA4_3==COLON||LA4_3==COLONS) ) {
                    alt4=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:16: toplevelstatNoAlloc
                    {
                    pushFollow(FOLLOW_toplevelstatNoAlloc_in_toplevelstat532);
                    toplevelstatNoAlloc5=toplevelstatNoAlloc();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelstatNoAlloc.add(toplevelstatNoAlloc5.getTree());


                    // AST REWRITE
                    // elements: toplevelstatNoAlloc
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 138:36: -> toplevelstatNoAlloc
                    {
                        adaptor.addChild(root_0, stream_toplevelstatNoAlloc.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:139:7: toplevelAlloc SEMI
                    {
                    pushFollow(FOLLOW_toplevelAlloc_in_toplevelstat545);
                    toplevelAlloc6=toplevelAlloc();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelAlloc.add(toplevelAlloc6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat547); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI7);



                    // AST REWRITE
                    // elements: toplevelAlloc
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 139:26: -> toplevelAlloc
                    {
                        adaptor.addChild(root_0, stream_toplevelAlloc.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:7: toplevelCode ( SEMI )?
                    {
                    pushFollow(FOLLOW_toplevelCode_in_toplevelstat559);
                    toplevelCode8=toplevelCode();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelCode.add(toplevelCode8.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:20: ( SEMI )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==SEMI) ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:20: SEMI
                            {
                            SEMI9=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat561); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SEMI.add(SEMI9);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: toplevelCode
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 140:26: -> toplevelCode
                    {
                        adaptor.addChild(root_0, stream_toplevelCode.nextTree());

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
    // $ANTLR end "toplevelstat"

    public static class toplevelstatNoAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelstatNoAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:143:1: toplevelstatNoAlloc : ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | ( LBRACE )=> xscope ( SEMI )? );
    public final EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc() throws RecognitionException {
        EulangParser.toplevelstatNoAlloc_return retval = new EulangParser.toplevelstatNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI12=null;
        Token FORWARD13=null;
        Token ID14=null;
        Token COMMA15=null;
        Token ID16=null;
        Token SEMI17=null;
        Token SEMI19=null;
        EulangParser.defineStmt_return defineStmt10 = null;

        EulangParser.scopeExtension_return scopeExtension11 = null;

        EulangParser.xscope_return xscope18 = null;


        CommonTree SEMI12_tree=null;
        CommonTree FORWARD13_tree=null;
        CommonTree ID14_tree=null;
        CommonTree COMMA15_tree=null;
        CommonTree ID16_tree=null;
        CommonTree SEMI17_tree=null;
        CommonTree SEMI19_tree=null;
        RewriteRuleTokenStream stream_FORWARD=new RewriteRuleTokenStream(adaptor,"token FORWARD");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_scopeExtension=new RewriteRuleSubtreeStream(adaptor,"rule scopeExtension");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:143:20: ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | ( LBRACE )=> xscope ( SEMI )? )
            int alt8=4;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==EQUALS||LA8_1==EQUALS_COLON) ) {
                    alt8=1;
                }
                else if ( (LA8_1==PLUS_EQ||LA8_1==PERIOD) ) {
                    alt8=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA8_0==COLON||LA8_0==COLONS) ) {
                alt8=2;
            }
            else if ( (LA8_0==FORWARD) ) {
                alt8=3;
            }
            else if ( (LA8_0==LBRACE) && (synpred1_Eulang())) {
                alt8=4;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:143:22: defineStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_defineStmt_in_toplevelstatNoAlloc582);
                    defineStmt10=defineStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defineStmt10.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:7: scopeExtension ( SEMI )?
                    {
                    pushFollow(FOLLOW_scopeExtension_in_toplevelstatNoAlloc590);
                    scopeExtension11=scopeExtension();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_scopeExtension.add(scopeExtension11.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:22: ( SEMI )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==SEMI) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:22: SEMI
                            {
                            SEMI12=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc592); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SEMI.add(SEMI12);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: scopeExtension
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 144:28: -> scopeExtension
                    {
                        adaptor.addChild(root_0, stream_scopeExtension.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:145:7: FORWARD ID ( COMMA ID )* SEMI
                    {
                    FORWARD13=(Token)match(input,FORWARD,FOLLOW_FORWARD_in_toplevelstatNoAlloc606); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FORWARD.add(FORWARD13);

                    ID14=(Token)match(input,ID,FOLLOW_ID_in_toplevelstatNoAlloc608); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID14);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:145:18: ( COMMA ID )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==COMMA) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:145:19: COMMA ID
                    	    {
                    	    COMMA15=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelstatNoAlloc611); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA15);

                    	    ID16=(Token)match(input,ID,FOLLOW_ID_in_toplevelstatNoAlloc613); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID16);


                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);

                    SEMI17=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc617); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI17);



                    // AST REWRITE
                    // elements: ID, FORWARD
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 145:35: -> ( ^( FORWARD ID ) )+
                    {
                        if ( !(stream_ID.hasNext()||stream_FORWARD.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext()||stream_FORWARD.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:145:38: ^( FORWARD ID )
                            {
                            CommonTree root_1 = (CommonTree)adaptor.nil();
                            root_1 = (CommonTree)adaptor.becomeRoot(stream_FORWARD.nextNode(), root_1);

                            adaptor.addChild(root_1, stream_ID.nextNode());

                            adaptor.addChild(root_0, root_1);
                            }

                        }
                        stream_ID.reset();
                        stream_FORWARD.reset();

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:7: ( LBRACE )=> xscope ( SEMI )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelstatNoAlloc645);
                    xscope18=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope18.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:26: ( SEMI )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==SEMI) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:26: SEMI
                            {
                            SEMI19=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc647); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            SEMI19_tree = (CommonTree)adaptor.create(SEMI19);
                            adaptor.addChild(root_0, SEMI19_tree);
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
    // $ANTLR end "toplevelstatNoAlloc"

    public static class toplevelAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:1: toplevelAlloc : ( toplevelSingleVarDecl | toplevelTupleVarDecl );
    public final EulangParser.toplevelAlloc_return toplevelAlloc() throws RecognitionException {
        EulangParser.toplevelAlloc_return retval = new EulangParser.toplevelAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelSingleVarDecl_return toplevelSingleVarDecl20 = null;

        EulangParser.toplevelTupleVarDecl_return toplevelTupleVarDecl21 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:15: ( toplevelSingleVarDecl | toplevelTupleVarDecl )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ID) ) {
                alt9=1;
            }
            else if ( (LA9_0==LPAREN) ) {
                alt9=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:17: toplevelSingleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_toplevelSingleVarDecl_in_toplevelAlloc662);
                    toplevelSingleVarDecl20=toplevelSingleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelSingleVarDecl20.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:41: toplevelTupleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_toplevelTupleVarDecl_in_toplevelAlloc666);
                    toplevelTupleVarDecl21=toplevelTupleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelTupleVarDecl21.getTree());

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
    // $ANTLR end "toplevelAlloc"

    public static class toplevelSingleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelSingleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:1: toplevelSingleVarDecl : ID ( attrs )? ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) ) ;
    public final EulangParser.toplevelSingleVarDecl_return toplevelSingleVarDecl() throws RecognitionException {
        EulangParser.toplevelSingleVarDecl_return retval = new EulangParser.toplevelSingleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID22=null;
        Token COLON_EQUALS24=null;
        Token COLON26=null;
        Token EQUALS28=null;
        Token COMMA30=null;
        Token ID31=null;
        Token COLON_EQUALS32=null;
        Token PLUS33=null;
        Token COMMA35=null;
        Token COLON37=null;
        Token EQUALS39=null;
        Token PLUS40=null;
        Token COMMA42=null;
        EulangParser.attrs_return attrs23 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList25 = null;

        EulangParser.type_return type27 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList29 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList34 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList36 = null;

        EulangParser.type_return type38 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList41 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList43 = null;


        CommonTree ID22_tree=null;
        CommonTree COLON_EQUALS24_tree=null;
        CommonTree COLON26_tree=null;
        CommonTree EQUALS28_tree=null;
        CommonTree COMMA30_tree=null;
        CommonTree ID31_tree=null;
        CommonTree COLON_EQUALS32_tree=null;
        CommonTree PLUS33_tree=null;
        CommonTree COMMA35_tree=null;
        CommonTree COLON37_tree=null;
        CommonTree EQUALS39_tree=null;
        CommonTree PLUS40_tree=null;
        CommonTree COMMA42_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_rhsExprOrInitList=new RewriteRuleSubtreeStream(adaptor,"rule rhsExprOrInitList");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:22: ( ID ( attrs )? ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:5: ID ( attrs )? ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) )
            {
            ID22=(Token)match(input,ID,FOLLOW_ID_in_toplevelSingleVarDecl677); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID22);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:8: ( attrs )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==ATTR) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:8: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_toplevelSingleVarDecl679);
                    attrs23=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs23.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:15: ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) )
            int alt19=3;
            switch ( input.LA(1) ) {
            case COLON_EQUALS:
                {
                alt19=1;
                }
                break;
            case COLON:
                {
                alt19=2;
                }
                break;
            case COMMA:
                {
                alt19=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }

            switch (alt19) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:11: COLON_EQUALS rhsExprOrInitList
                    {
                    COLON_EQUALS24=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl694); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS24);

                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl696);
                    rhsExprOrInitList25=rhsExprOrInitList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList25.getTree());


                    // AST REWRITE
                    // elements: rhsExprOrInitList, attrs, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 154:50: -> ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:53: ^( ALLOC ( attrs )? TYPE ID rhsExprOrInitList )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:61: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:11: COLON type ( EQUALS rhsExprOrInitList )?
                    {
                    COLON26=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelSingleVarDecl733); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON26);

                    pushFollow(FOLLOW_type_in_toplevelSingleVarDecl735);
                    type27=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type27.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:22: ( EQUALS rhsExprOrInitList )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==EQUALS) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:23: EQUALS rhsExprOrInitList
                            {
                            EQUALS28=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelSingleVarDecl738); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS28);

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl740);
                            rhsExprOrInitList29=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList29.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, ID, attrs, rhsExprOrInitList
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 155:51: -> ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:54: ^( ALLOC ( attrs )? type ID ( rhsExprOrInitList )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:62: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:77: ( rhsExprOrInitList )*
                        while ( stream_rhsExprOrInitList.hasNext() ) {
                            adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        }
                        stream_rhsExprOrInitList.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:9: ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:9: ( COMMA ID )+
                    int cnt12=0;
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==COMMA) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:11: COMMA ID
                    	    {
                    	    COMMA30=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl773); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA30);

                    	    ID31=(Token)match(input,ID,FOLLOW_ID_in_toplevelSingleVarDecl775); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID31);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt12 >= 1 ) break loop12;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(12, input);
                                throw eee;
                        }
                        cnt12++;
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:9: ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) )
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==COLON_EQUALS) ) {
                        alt18=1;
                    }
                    else if ( (LA18_0==COLON) ) {
                        alt18=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 18, 0, input);

                        throw nvae;
                    }
                    switch (alt18) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:12: ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:12: ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:14: COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )*
                            {
                            COLON_EQUALS32=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl794); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS32);

                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:27: ( PLUS )?
                            int alt13=2;
                            int LA13_0 = input.LA(1);

                            if ( (LA13_0==PLUS) ) {
                                alt13=1;
                            }
                            switch (alt13) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:27: PLUS
                                    {
                                    PLUS33=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelSingleVarDecl796); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS33);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl799);
                            rhsExprOrInitList34=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList34.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:51: ( COMMA rhsExprOrInitList )*
                            loop14:
                            do {
                                int alt14=2;
                                int LA14_0 = input.LA(1);

                                if ( (LA14_0==COMMA) ) {
                                    alt14=1;
                                }


                                switch (alt14) {
                            	case 1 :
                            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:52: COMMA rhsExprOrInitList
                            	    {
                            	    COMMA35=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl802); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA35);

                            	    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl804);
                            	    rhsExprOrInitList36=rhsExprOrInitList();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList36.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop14;
                                }
                            } while (true);


                            }



                            // AST REWRITE
                            // elements: rhsExprOrInitList, ID, PLUS, attrs
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 158:15: -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:158:18: ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:158:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:158:38: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:158:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:158:57: ^( LIST ( rhsExprOrInitList )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_rhsExprOrInitList.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_rhsExprOrInitList.hasNext() ) {
                                    adaptor.addChild(root_2, stream_rhsExprOrInitList.nextTree());

                                }
                                stream_rhsExprOrInitList.reset();

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:12: ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:12: ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:14: COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )?
                            {
                            COLON37=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelSingleVarDecl867); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON37);

                            pushFollow(FOLLOW_type_in_toplevelSingleVarDecl869);
                            type38=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type38.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:25: ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )?
                            int alt17=2;
                            int LA17_0 = input.LA(1);

                            if ( (LA17_0==EQUALS) ) {
                                alt17=1;
                            }
                            switch (alt17) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:26: EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )*
                                    {
                                    EQUALS39=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelSingleVarDecl872); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS39);

                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:33: ( PLUS )?
                                    int alt15=2;
                                    int LA15_0 = input.LA(1);

                                    if ( (LA15_0==PLUS) ) {
                                        alt15=1;
                                    }
                                    switch (alt15) {
                                        case 1 :
                                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:33: PLUS
                                            {
                                            PLUS40=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelSingleVarDecl874); if (state.failed) return retval; 
                                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS40);


                                            }
                                            break;

                                    }

                                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl877);
                                    rhsExprOrInitList41=rhsExprOrInitList();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList41.getTree());
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:57: ( COMMA rhsExprOrInitList )*
                                    loop16:
                                    do {
                                        int alt16=2;
                                        int LA16_0 = input.LA(1);

                                        if ( (LA16_0==COMMA) ) {
                                            alt16=1;
                                        }


                                        switch (alt16) {
                                    	case 1 :
                                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:159:58: COMMA rhsExprOrInitList
                                    	    {
                                    	    COMMA42=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl880); if (state.failed) return retval; 
                                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA42);

                                    	    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl882);
                                    	    rhsExprOrInitList43=rhsExprOrInitList();

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList43.getTree());

                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop16;
                                        }
                                    } while (true);


                                    }
                                    break;

                            }


                            }



                            // AST REWRITE
                            // elements: PLUS, ID, attrs, rhsExprOrInitList, type
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 160:15: -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:18: ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, stream_type.nextTree());
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:38: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:56: ( ^( LIST ( rhsExprOrInitList )+ ) )?
                                if ( stream_rhsExprOrInitList.hasNext() ) {
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:160:56: ^( LIST ( rhsExprOrInitList )+ )
                                    {
                                    CommonTree root_2 = (CommonTree)adaptor.nil();
                                    root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                    if ( !(stream_rhsExprOrInitList.hasNext()) ) {
                                        throw new RewriteEarlyExitException();
                                    }
                                    while ( stream_rhsExprOrInitList.hasNext() ) {
                                        adaptor.addChild(root_2, stream_rhsExprOrInitList.nextTree());

                                    }
                                    stream_rhsExprOrInitList.reset();

                                    adaptor.addChild(root_1, root_2);
                                    }

                                }
                                stream_rhsExprOrInitList.reset();

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
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
    // $ANTLR end "toplevelSingleVarDecl"

    public static class toplevelTupleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelTupleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:1: toplevelTupleVarDecl : idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) ) ;
    public final EulangParser.toplevelTupleVarDecl_return toplevelTupleVarDecl() throws RecognitionException {
        EulangParser.toplevelTupleVarDecl_return retval = new EulangParser.toplevelTupleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON_EQUALS45=null;
        Token COLON47=null;
        Token EQUALS49=null;
        EulangParser.idTuple_return idTuple44 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList46 = null;

        EulangParser.type_return type48 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList50 = null;


        CommonTree COLON_EQUALS45_tree=null;
        CommonTree COLON47_tree=null;
        CommonTree EQUALS49_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_rhsExprOrInitList=new RewriteRuleSubtreeStream(adaptor,"rule rhsExprOrInitList");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:21: ( idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:5: idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) )
            {
            pushFollow(FOLLOW_idTuple_in_toplevelTupleVarDecl976);
            idTuple44=idTuple();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTuple.add(idTuple44.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:167:7: ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) ) )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==COLON_EQUALS) ) {
                alt21=1;
            }
            else if ( (LA21_0==COLON) ) {
                alt21=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:11: COLON_EQUALS rhsExprOrInitList
                    {
                    COLON_EQUALS45=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelTupleVarDecl998); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS45);

                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1000);
                    rhsExprOrInitList46=rhsExprOrInitList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList46.getTree());


                    // AST REWRITE
                    // elements: idTuple, rhsExprOrInitList
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 168:50: -> ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:168:53: ^( ALLOC_TUPLE TYPE idTuple rhsExprOrInitList )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:11: COLON type ( EQUALS rhsExprOrInitList )?
                    {
                    COLON47=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelTupleVarDecl1034); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON47);

                    pushFollow(FOLLOW_type_in_toplevelTupleVarDecl1036);
                    type48=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type48.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:22: ( EQUALS rhsExprOrInitList )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0==EQUALS) ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:23: EQUALS rhsExprOrInitList
                            {
                            EQUALS49=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelTupleVarDecl1039); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS49);

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1041);
                            rhsExprOrInitList50=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList50.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: idTuple, rhsExprOrInitList, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 169:51: -> ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:54: ^( ALLOC_TUPLE type idTuple ( rhsExprOrInitList )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:169:81: ( rhsExprOrInitList )*
                        while ( stream_rhsExprOrInitList.hasNext() ) {
                            adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        }
                        stream_rhsExprOrInitList.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
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
    // $ANTLR end "toplevelTupleVarDecl"

    public static class rhsExprOrInitList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rhsExprOrInitList"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:1: rhsExprOrInitList : ( rhsExpr | initList );
    public final EulangParser.rhsExprOrInitList_return rhsExprOrInitList() throws RecognitionException {
        EulangParser.rhsExprOrInitList_return retval = new EulangParser.rhsExprOrInitList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr51 = null;

        EulangParser.initList_return initList52 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:19: ( rhsExpr | initList )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==CODE||(LA22_0>=SIZEOF && LA22_0<=TYPEOF)||LA22_0==ID||LA22_0==COLON||LA22_0==LPAREN||LA22_0==NIL||LA22_0==IF||LA22_0==NOT||(LA22_0>=TILDE && LA22_0<=AMP)||LA22_0==MINUS||(LA22_0>=PLUSPLUS && LA22_0<=STRING_LITERAL)||(LA22_0>=FALSE && LA22_0<=COLONS)) ) {
                alt22=1;
            }
            else if ( (LA22_0==LBRACKET) ) {
                alt22=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:21: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_rhsExprOrInitList1084);
                    rhsExpr51=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr51.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:173:31: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_rhsExprOrInitList1088);
                    initList52=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList52.getTree());

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
    // $ANTLR end "rhsExprOrInitList"

    public static class toplevelCode_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelCode"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:1: toplevelCode : LPAREN LBRACE codestmtlist RBRACE RPAREN -> codestmtlist ;
    public final EulangParser.toplevelCode_return toplevelCode() throws RecognitionException {
        EulangParser.toplevelCode_return retval = new EulangParser.toplevelCode_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN53=null;
        Token LBRACE54=null;
        Token RBRACE56=null;
        Token RPAREN57=null;
        EulangParser.codestmtlist_return codestmtlist55 = null;


        CommonTree LPAREN53_tree=null;
        CommonTree LBRACE54_tree=null;
        CommonTree RBRACE56_tree=null;
        CommonTree RPAREN57_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:14: ( LPAREN LBRACE codestmtlist RBRACE RPAREN -> codestmtlist )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:16: LPAREN LBRACE codestmtlist RBRACE RPAREN
            {
            LPAREN53=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_toplevelCode1097); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN53);

            LBRACE54=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_toplevelCode1099); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE54);

            pushFollow(FOLLOW_codestmtlist_in_toplevelCode1101);
            codestmtlist55=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist55.getTree());
            RBRACE56=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_toplevelCode1103); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE56);

            RPAREN57=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_toplevelCode1105); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN57);



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
            // 175:57: -> codestmtlist
            {
                adaptor.addChild(root_0, stream_codestmtlist.nextTree());

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
    // $ANTLR end "toplevelCode"

    public static class scopeExtension_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "scopeExtension"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:178:1: scopeExtension : namespaceRef PLUS_EQ xscopeNoAlloc -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc ) ;
    public final EulangParser.scopeExtension_return scopeExtension() throws RecognitionException {
        EulangParser.scopeExtension_return retval = new EulangParser.scopeExtension_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS_EQ59=null;
        EulangParser.namespaceRef_return namespaceRef58 = null;

        EulangParser.xscopeNoAlloc_return xscopeNoAlloc60 = null;


        CommonTree PLUS_EQ59_tree=null;
        RewriteRuleTokenStream stream_PLUS_EQ=new RewriteRuleTokenStream(adaptor,"token PLUS_EQ");
        RewriteRuleSubtreeStream stream_xscopeNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule xscopeNoAlloc");
        RewriteRuleSubtreeStream stream_namespaceRef=new RewriteRuleSubtreeStream(adaptor,"rule namespaceRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:178:16: ( namespaceRef PLUS_EQ xscopeNoAlloc -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:178:18: namespaceRef PLUS_EQ xscopeNoAlloc
            {
            pushFollow(FOLLOW_namespaceRef_in_scopeExtension1124);
            namespaceRef58=namespaceRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef58.getTree());
            PLUS_EQ59=(Token)match(input,PLUS_EQ,FOLLOW_PLUS_EQ_in_scopeExtension1126); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_PLUS_EQ.add(PLUS_EQ59);

            pushFollow(FOLLOW_xscopeNoAlloc_in_scopeExtension1128);
            xscopeNoAlloc60=xscopeNoAlloc();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_xscopeNoAlloc.add(xscopeNoAlloc60.getTree());


            // AST REWRITE
            // elements: xscopeNoAlloc, namespaceRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 178:53: -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:178:56: ^( EXTENDSCOPE namespaceRef xscopeNoAlloc )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXTENDSCOPE, "EXTENDSCOPE"), root_1);

                adaptor.addChild(root_1, stream_namespaceRef.nextTree());
                adaptor.addChild(root_1, stream_xscopeNoAlloc.nextTree());

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
    // $ANTLR end "scopeExtension"

    public static class defineStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "defineStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:1: defineStmt : ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) );
    public final EulangParser.defineStmt_return defineStmt() throws RecognitionException {
        EulangParser.defineStmt_return retval = new EulangParser.defineStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID61=null;
        Token EQUALS62=null;
        Token LBRACKET63=null;
        Token RBRACKET65=null;
        Token SEMI67=null;
        Token ID68=null;
        Token EQUALS_COLON69=null;
        Token SEMI71=null;
        Token ID72=null;
        Token EQUALS73=null;
        Token SEMI75=null;
        EulangParser.idlistOrEmpty_return idlistOrEmpty64 = null;

        EulangParser.toplevelvalue_return toplevelvalue66 = null;

        EulangParser.type_return type70 = null;

        EulangParser.toplevelvalue_return toplevelvalue74 = null;


        CommonTree ID61_tree=null;
        CommonTree EQUALS62_tree=null;
        CommonTree LBRACKET63_tree=null;
        CommonTree RBRACKET65_tree=null;
        CommonTree SEMI67_tree=null;
        CommonTree ID68_tree=null;
        CommonTree EQUALS_COLON69_tree=null;
        CommonTree SEMI71_tree=null;
        CommonTree ID72_tree=null;
        CommonTree EQUALS73_tree=null;
        CommonTree SEMI75_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_EQUALS_COLON=new RewriteRuleTokenStream(adaptor,"token EQUALS_COLON");
        RewriteRuleSubtreeStream stream_toplevelvalue=new RewriteRuleSubtreeStream(adaptor,"rule toplevelvalue");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        RewriteRuleSubtreeStream stream_idlistOrEmpty=new RewriteRuleSubtreeStream(adaptor,"rule idlistOrEmpty");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:12: ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) )
            int alt23=3;
            alt23 = dfa23.predict(input);
            switch (alt23) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:14: ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI
                    {
                    ID61=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1157); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID61);

                    EQUALS62=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_defineStmt1159); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS62);

                    LBRACKET63=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_defineStmt1161); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET63);

                    pushFollow(FOLLOW_idlistOrEmpty_in_defineStmt1163);
                    idlistOrEmpty64=idlistOrEmpty();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idlistOrEmpty.add(idlistOrEmpty64.getTree());
                    RBRACKET65=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_defineStmt1165); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET65);

                    pushFollow(FOLLOW_toplevelvalue_in_defineStmt1168);
                    toplevelvalue66=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue66.getTree());
                    SEMI67=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1174); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI67);



                    // AST REWRITE
                    // elements: idlistOrEmpty, ID, toplevelvalue
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 180:105: -> ^( DEFINE ID idlistOrEmpty toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:108: ^( DEFINE ID idlistOrEmpty toplevelvalue )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_idlistOrEmpty.nextTree());
                        adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:7: ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI
                    {
                    ID68=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1204); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID68);

                    EQUALS_COLON69=(Token)match(input,EQUALS_COLON,FOLLOW_EQUALS_COLON_in_defineStmt1206); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS_COLON.add(EQUALS_COLON69);

                    pushFollow(FOLLOW_type_in_defineStmt1208);
                    type70=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type70.getTree());
                    SEMI71=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1214); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI71);



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
                    // 181:59: -> ^( DEFINE ID type )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:62: ^( DEFINE ID type )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEFINE, "DEFINE"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:7: ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI
                    {
                    ID72=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1241); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID72);

                    EQUALS73=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_defineStmt1243); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS73);

                    pushFollow(FOLLOW_toplevelvalue_in_defineStmt1245);
                    toplevelvalue74=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue74.getTree());
                    SEMI75=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1251); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI75);



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
                    // 182:56: -> ^( DEFINE ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:59: ^( DEFINE ID toplevelvalue )
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

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "defineStmt"

    public static class toplevelvalue_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "toplevelvalue"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:1: toplevelvalue : ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data );
    public final EulangParser.toplevelvalue_return toplevelvalue() throws RecognitionException {
        EulangParser.toplevelvalue_return retval = new EulangParser.toplevelvalue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS78=null;
        Token PLUS81=null;
        EulangParser.xscope_return xscope76 = null;

        EulangParser.namespaceRef_return namespaceRef77 = null;

        EulangParser.data_return data79 = null;

        EulangParser.namespaceRef_return namespaceRef80 = null;

        EulangParser.xscope_return xscope82 = null;

        EulangParser.selector_return selector83 = null;

        EulangParser.rhsExpr_return rhsExpr84 = null;

        EulangParser.data_return data85 = null;


        CommonTree PLUS78_tree=null;
        CommonTree PLUS81_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleSubtreeStream stream_xscope=new RewriteRuleSubtreeStream(adaptor,"rule xscope");
        RewriteRuleSubtreeStream stream_data=new RewriteRuleSubtreeStream(adaptor,"rule data");
        RewriteRuleSubtreeStream stream_namespaceRef=new RewriteRuleSubtreeStream(adaptor,"rule namespaceRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:15: ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data )
            int alt24=6;
            alt24 = dfa24.predict(input);
            switch (alt24) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:17: ( LBRACE )=> xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelvalue1279);
                    xscope76=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope76.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:7: namespaceRef PLUS data
                    {
                    pushFollow(FOLLOW_namespaceRef_in_toplevelvalue1287);
                    namespaceRef77=namespaceRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef77.getTree());
                    PLUS78=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelvalue1289); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS78);

                    pushFollow(FOLLOW_data_in_toplevelvalue1291);
                    data79=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_data.add(data79.getTree());


                    // AST REWRITE
                    // elements: data, namespaceRef
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 186:30: -> ^( ADDSCOPE namespaceRef data )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:186:33: ^( ADDSCOPE namespaceRef data )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADDSCOPE, "ADDSCOPE"), root_1);

                        adaptor.addChild(root_1, stream_namespaceRef.nextTree());
                        adaptor.addChild(root_1, stream_data.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:7: namespaceRef PLUS xscope
                    {
                    pushFollow(FOLLOW_namespaceRef_in_toplevelvalue1309);
                    namespaceRef80=namespaceRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef80.getTree());
                    PLUS81=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelvalue1311); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS81);

                    pushFollow(FOLLOW_xscope_in_toplevelvalue1313);
                    xscope82=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xscope.add(xscope82.getTree());


                    // AST REWRITE
                    // elements: namespaceRef, xscope
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 187:32: -> ^( ADDSCOPE namespaceRef xscope )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:187:35: ^( ADDSCOPE namespaceRef xscope )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADDSCOPE, "ADDSCOPE"), root_1);

                        adaptor.addChild(root_1, stream_namespaceRef.nextTree());
                        adaptor.addChild(root_1, stream_xscope.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_toplevelvalue1331);
                    selector83=selector();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selector83.getTree());

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:189:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue1339);
                    rhsExpr84=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr84.getTree());

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:190:7: data
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_data_in_toplevelvalue1347);
                    data85=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, data85.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:1: selector : LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET86=null;
        Token RBRACKET88=null;
        EulangParser.selectors_return selectors87 = null;


        CommonTree LBRACKET86_tree=null;
        CommonTree RBRACKET88_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectors=new RewriteRuleSubtreeStream(adaptor,"rule selectors");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:9: ( LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:11: LBRACKET selectors RBRACKET
            {
            LBRACKET86=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector1360); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET86);

            pushFollow(FOLLOW_selectors_in_selector1362);
            selectors87=selectors();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectors.add(selectors87.getTree());
            RBRACKET88=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector1364); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET88);



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
            // 194:42: -> ^( LIST ( selectors )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:45: ^( LIST ( selectors )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:52: ( selectors )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:1: selectors : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* ;
    public final EulangParser.selectors_return selectors() throws RecognitionException {
        EulangParser.selectors_return retval = new EulangParser.selectors_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA90=null;
        Token COMMA92=null;
        EulangParser.selectoritem_return selectoritem89 = null;

        EulangParser.selectoritem_return selectoritem91 = null;


        CommonTree COMMA90_tree=null;
        CommonTree COMMA92_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_selectoritem=new RewriteRuleSubtreeStream(adaptor,"rule selectoritem");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:10: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==CODE||(LA27_0>=SIZEOF && LA27_0<=TYPEOF)||LA27_0==ID||LA27_0==COLON||LA27_0==LPAREN||LA27_0==NIL||LA27_0==IF||LA27_0==NOT||(LA27_0>=TILDE && LA27_0<=AMP)||LA27_0==MINUS||(LA27_0>=PLUSPLUS && LA27_0<=STRING_LITERAL)||(LA27_0>=FALSE && LA27_0<=COLONS)) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:13: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectors1390);
                    selectoritem89=selectoritem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem89.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:26: ( COMMA selectoritem )*
                    loop25:
                    do {
                        int alt25=2;
                        int LA25_0 = input.LA(1);

                        if ( (LA25_0==COMMA) ) {
                            int LA25_1 = input.LA(2);

                            if ( (LA25_1==CODE||(LA25_1>=SIZEOF && LA25_1<=TYPEOF)||LA25_1==ID||LA25_1==COLON||LA25_1==LPAREN||LA25_1==NIL||LA25_1==IF||LA25_1==NOT||(LA25_1>=TILDE && LA25_1<=AMP)||LA25_1==MINUS||(LA25_1>=PLUSPLUS && LA25_1<=STRING_LITERAL)||(LA25_1>=FALSE && LA25_1<=COLONS)) ) {
                                alt25=1;
                            }


                        }


                        switch (alt25) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:28: COMMA selectoritem
                    	    {
                    	    COMMA90=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors1394); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA90);

                    	    pushFollow(FOLLOW_selectoritem_in_selectors1396);
                    	    selectoritem91=selectoritem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem91.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:50: ( COMMA )?
                    int alt26=2;
                    int LA26_0 = input.LA(1);

                    if ( (LA26_0==COMMA) ) {
                        alt26=1;
                    }
                    switch (alt26) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:50: COMMA
                            {
                            COMMA92=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors1401); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA92);


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
            // 197:62: -> ( selectoritem )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:197:65: ( selectoritem )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:1: selectoritem : rhsExpr ;
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr93 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:14: ( rhsExpr )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:17: rhsExpr
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_rhsExpr_in_selectoritem1432);
            rhsExpr93=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr93.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:1: xscope : LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) ;
    public final EulangParser.xscope_return xscope() throws RecognitionException {
        EulangParser.xscope_return retval = new EulangParser.xscope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE94=null;
        Token RBRACE96=null;
        EulangParser.toplevelstmts_return toplevelstmts95 = null;


        CommonTree LBRACE94_tree=null;
        CommonTree RBRACE96_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmts=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmts");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:8: ( LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:10: LBRACE toplevelstmts RBRACE
            {
            LBRACE94=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope1443); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE94);

            pushFollow(FOLLOW_toplevelstmts_in_xscope1445);
            toplevelstmts95=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmts.add(toplevelstmts95.getTree());
            RBRACE96=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope1447); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE96);



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
            // 204:41: -> ^( SCOPE ( toplevelstmts )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:44: ^( SCOPE ( toplevelstmts )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:204:52: ( toplevelstmts )*
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

    public static class xscopeNoAlloc_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "xscopeNoAlloc"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:206:1: xscopeNoAlloc : LBRACE toplevelstmtsNoAlloc RBRACE -> ^( SCOPE ( toplevelstmtsNoAlloc )* ) ;
    public final EulangParser.xscopeNoAlloc_return xscopeNoAlloc() throws RecognitionException {
        EulangParser.xscopeNoAlloc_return retval = new EulangParser.xscopeNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE97=null;
        Token RBRACE99=null;
        EulangParser.toplevelstmtsNoAlloc_return toplevelstmtsNoAlloc98 = null;


        CommonTree LBRACE97_tree=null;
        CommonTree RBRACE99_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmtsNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmtsNoAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:206:15: ( LBRACE toplevelstmtsNoAlloc RBRACE -> ^( SCOPE ( toplevelstmtsNoAlloc )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:206:17: LBRACE toplevelstmtsNoAlloc RBRACE
            {
            LBRACE97=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscopeNoAlloc1472); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE97);

            pushFollow(FOLLOW_toplevelstmtsNoAlloc_in_xscopeNoAlloc1474);
            toplevelstmtsNoAlloc98=toplevelstmtsNoAlloc();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmtsNoAlloc.add(toplevelstmtsNoAlloc98.getTree());
            RBRACE99=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscopeNoAlloc1476); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE99);



            // AST REWRITE
            // elements: toplevelstmtsNoAlloc
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 206:55: -> ^( SCOPE ( toplevelstmtsNoAlloc )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:206:58: ^( SCOPE ( toplevelstmtsNoAlloc )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:206:66: ( toplevelstmtsNoAlloc )*
                while ( stream_toplevelstmtsNoAlloc.hasNext() ) {
                    adaptor.addChild(root_1, stream_toplevelstmtsNoAlloc.nextTree());

                }
                stream_toplevelstmtsNoAlloc.reset();

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
    // $ANTLR end "xscopeNoAlloc"

    public static class listCompr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listCompr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:1: listCompr : ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) ;
    public final EulangParser.listCompr_return listCompr() throws RecognitionException {
        EulangParser.listCompr_return retval = new EulangParser.listCompr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON101=null;
        EulangParser.forIn_return forIn100 = null;

        EulangParser.listiterable_return listiterable102 = null;


        CommonTree COLON101_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_forIn=new RewriteRuleSubtreeStream(adaptor,"rule forIn");
        RewriteRuleSubtreeStream stream_listiterable=new RewriteRuleSubtreeStream(adaptor,"rule listiterable");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:10: ( ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:12: ( forIn )+ COLON listiterable
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:12: ( forIn )+
            int cnt28=0;
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==FOR) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:12: forIn
            	    {
            	    pushFollow(FOLLOW_forIn_in_listCompr1503);
            	    forIn100=forIn();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_forIn.add(forIn100.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt28 >= 1 ) break loop28;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(28, input);
                        throw eee;
                }
                cnt28++;
            } while (true);

            COLON101=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr1506); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON101);

            pushFollow(FOLLOW_listiterable_in_listCompr1508);
            listiterable102=listiterable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listiterable.add(listiterable102.getTree());


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
            // 211:42: -> ^( LIST_COMPREHENSION ( forIn )+ listiterable )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:211:45: ^( LIST_COMPREHENSION ( forIn )+ listiterable )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:1: forIn : FOR idlist IN list -> ^( FOR idlist list ) ;
    public final EulangParser.forIn_return forIn() throws RecognitionException {
        EulangParser.forIn_return retval = new EulangParser.forIn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR103=null;
        Token IN105=null;
        EulangParser.idlist_return idlist104 = null;

        EulangParser.list_return list106 = null;


        CommonTree FOR103_tree=null;
        CommonTree IN105_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_list=new RewriteRuleSubtreeStream(adaptor,"rule list");
        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:7: ( FOR idlist IN list -> ^( FOR idlist list ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:9: FOR idlist IN list
            {
            FOR103=(Token)match(input,FOR,FOLLOW_FOR_in_forIn1540); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR103);

            pushFollow(FOLLOW_idlist_in_forIn1542);
            idlist104=idlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idlist.add(idlist104.getTree());
            IN105=(Token)match(input,IN,FOLLOW_IN_in_forIn1544); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN105);

            pushFollow(FOLLOW_list_in_forIn1546);
            list106=list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_list.add(list106.getTree());


            // AST REWRITE
            // elements: idlist, list, FOR
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 214:33: -> ^( FOR idlist list )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:36: ^( FOR idlist list )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:1: idlist : ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) ;
    public final EulangParser.idlist_return idlist() throws RecognitionException {
        EulangParser.idlist_return retval = new EulangParser.idlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID107=null;
        Token COMMA108=null;
        Token ID109=null;

        CommonTree ID107_tree=null;
        CommonTree COMMA108_tree=null;
        CommonTree ID109_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:8: ( ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:10: ID ( COMMA ID )*
            {
            ID107=(Token)match(input,ID,FOLLOW_ID_in_idlist1571); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID107);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:13: ( COMMA ID )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==COMMA) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:14: COMMA ID
            	    {
            	    COMMA108=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist1574); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA108);

            	    ID109=(Token)match(input,ID,FOLLOW_ID_in_idlist1576); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID109);


            	    }
            	    break;

            	default :
            	    break loop29;
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
            // 216:28: -> ^( IDLIST ( ID )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:31: ^( IDLIST ( ID )+ )
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

    public static class idlistOrEmpty_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idlistOrEmpty"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:1: idlistOrEmpty : ( idlist -> idlist | -> ^( IDLIST ) );
    public final EulangParser.idlistOrEmpty_return idlistOrEmpty() throws RecognitionException {
        EulangParser.idlistOrEmpty_return retval = new EulangParser.idlistOrEmpty_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.idlist_return idlist110 = null;


        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:15: ( idlist -> idlist | -> ^( IDLIST ) )
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==ID) ) {
                alt30=1;
            }
            else if ( (LA30_0==RBRACKET) ) {
                alt30=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }
            switch (alt30) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:17: idlist
                    {
                    pushFollow(FOLLOW_idlist_in_idlistOrEmpty1602);
                    idlist110=idlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idlist.add(idlist110.getTree());


                    // AST REWRITE
                    // elements: idlist
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 218:24: -> idlist
                    {
                        adaptor.addChild(root_0, stream_idlist.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:36: 
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
                    // 218:36: -> ^( IDLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:218:39: ^( IDLIST )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDLIST, "IDLIST"), root_1);

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
    // $ANTLR end "idlistOrEmpty"

    public static class listiterable_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "listiterable"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:1: listiterable : code ;
    public final EulangParser.listiterable_return listiterable() throws RecognitionException {
        EulangParser.listiterable_return retval = new EulangParser.listiterable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code111 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:14: ( code )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:220:16: code
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_code_in_listiterable1623);
            code111=code();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, code111.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:1: list : LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) ;
    public final EulangParser.list_return list() throws RecognitionException {
        EulangParser.list_return retval = new EulangParser.list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET112=null;
        Token RBRACKET114=null;
        EulangParser.listitems_return listitems113 = null;


        CommonTree LBRACKET112_tree=null;
        CommonTree RBRACKET114_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_listitems=new RewriteRuleSubtreeStream(adaptor,"rule listitems");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:6: ( LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:8: LBRACKET listitems RBRACKET
            {
            LBRACKET112=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list1637); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET112);

            pushFollow(FOLLOW_listitems_in_list1639);
            listitems113=listitems();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listitems.add(listitems113.getTree());
            RBRACKET114=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list1641); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET114);



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
            // 222:40: -> ^( LIST ( listitems )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:43: ^( LIST ( listitems )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:50: ( listitems )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:1: listitems : ( listitem ( COMMA listitem )* ( COMMA )? )? ;
    public final EulangParser.listitems_return listitems() throws RecognitionException {
        EulangParser.listitems_return retval = new EulangParser.listitems_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA116=null;
        Token COMMA118=null;
        EulangParser.listitem_return listitem115 = null;

        EulangParser.listitem_return listitem117 = null;


        CommonTree COMMA116_tree=null;
        CommonTree COMMA118_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:10: ( ( listitem ( COMMA listitem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==CODE||(LA33_0>=SIZEOF && LA33_0<=TYPEOF)||LA33_0==ID||LA33_0==COLON||(LA33_0>=LPAREN && LA33_0<=LBRACE)||LA33_0==LBRACKET||LA33_0==NIL||LA33_0==IF||LA33_0==NOT||(LA33_0>=TILDE && LA33_0<=AMP)||LA33_0==MINUS||(LA33_0>=PLUSPLUS && LA33_0<=STRING_LITERAL)||(LA33_0>=FALSE && LA33_0<=DATA)) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems1671);
                    listitem115=listitem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem115.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:22: ( COMMA listitem )*
                    loop31:
                    do {
                        int alt31=2;
                        int LA31_0 = input.LA(1);

                        if ( (LA31_0==COMMA) ) {
                            int LA31_1 = input.LA(2);

                            if ( (LA31_1==CODE||(LA31_1>=SIZEOF && LA31_1<=TYPEOF)||LA31_1==ID||LA31_1==COLON||(LA31_1>=LPAREN && LA31_1<=LBRACE)||LA31_1==LBRACKET||LA31_1==NIL||LA31_1==IF||LA31_1==NOT||(LA31_1>=TILDE && LA31_1<=AMP)||LA31_1==MINUS||(LA31_1>=PLUSPLUS && LA31_1<=STRING_LITERAL)||(LA31_1>=FALSE && LA31_1<=DATA)) ) {
                                alt31=1;
                            }


                        }


                        switch (alt31) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:24: COMMA listitem
                    	    {
                    	    COMMA116=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems1675); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA116_tree = (CommonTree)adaptor.create(COMMA116);
                    	    adaptor.addChild(root_0, COMMA116_tree);
                    	    }
                    	    pushFollow(FOLLOW_listitem_in_listitems1677);
                    	    listitem117=listitem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem117.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:42: ( COMMA )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==COMMA) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:225:42: COMMA
                            {
                            COMMA118=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems1682); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA118_tree = (CommonTree)adaptor.create(COMMA118);
                            adaptor.addChild(root_0, COMMA118_tree);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:1: listitem : toplevelvalue ;
    public final EulangParser.listitem_return listitem() throws RecognitionException {
        EulangParser.listitem_return retval = new EulangParser.listitem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelvalue_return toplevelvalue119 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:10: ( toplevelvalue )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:228:12: toplevelvalue
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelvalue_in_listitem1708);
            toplevelvalue119=toplevelvalue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelvalue119.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:1: code : CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE120=null;
        Token LBRACE123=null;
        Token RBRACE125=null;
        EulangParser.attrs_return attrs121 = null;

        EulangParser.proto_return proto122 = null;

        EulangParser.codestmtlist_return codestmtlist124 = null;


        CommonTree CODE120_tree=null;
        CommonTree LBRACE123_tree=null;
        CommonTree RBRACE125_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:6: ( CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:8: CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE
            {
            CODE120=(Token)match(input,CODE,FOLLOW_CODE_in_code1726); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CODE.add(CODE120);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:13: ( attrs )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==ATTR) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:13: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_code1728);
                    attrs121=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs121.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:20: ( proto )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==LPAREN) ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:20: proto
                    {
                    pushFollow(FOLLOW_proto_in_code1731);
                    proto122=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_proto.add(proto122.getTree());

                    }
                    break;

            }

            LBRACE123=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_code1734); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE123);

            pushFollow(FOLLOW_codestmtlist_in_code1736);
            codestmtlist124=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist124.getTree());
            RBRACE125=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code1738); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE125);



            // AST REWRITE
            // elements: codestmtlist, CODE, attrs, proto
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 233:54: -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:57: ^( CODE ( attrs )? ( proto )? ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:64: ( attrs )?
                if ( stream_attrs.hasNext() ) {
                    adaptor.addChild(root_1, stream_attrs.nextTree());

                }
                stream_attrs.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:71: ( proto )?
                if ( stream_proto.hasNext() ) {
                    adaptor.addChild(root_1, stream_proto.nextTree());

                }
                stream_proto.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:233:78: ( codestmtlist )*
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

    public static class attrs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:1: attrs : ( ATTR )+ -> ^( ATTRS ( ATTR )+ ) ;
    public final EulangParser.attrs_return attrs() throws RecognitionException {
        EulangParser.attrs_return retval = new EulangParser.attrs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATTR126=null;

        CommonTree ATTR126_tree=null;
        RewriteRuleTokenStream stream_ATTR=new RewriteRuleTokenStream(adaptor,"token ATTR");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:7: ( ( ATTR )+ -> ^( ATTRS ( ATTR )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:9: ( ATTR )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:9: ( ATTR )+
            int cnt36=0;
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==ATTR) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:9: ATTR
            	    {
            	    ATTR126=(Token)match(input,ATTR,FOLLOW_ATTR_in_attrs1768); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ATTR.add(ATTR126);


            	    }
            	    break;

            	default :
            	    if ( cnt36 >= 1 ) break loop36;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(36, input);
                        throw eee;
                }
                cnt36++;
            } while (true);



            // AST REWRITE
            // elements: ATTR
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 236:17: -> ^( ATTRS ( ATTR )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:19: ^( ATTRS ( ATTR )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ATTRS, "ATTRS"), root_1);

                if ( !(stream_ATTR.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ATTR.hasNext() ) {
                    adaptor.addChild(root_1, stream_ATTR.nextNode());

                }
                stream_ATTR.reset();

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
    // $ANTLR end "attrs"

    public static class argdefs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:1: argdefs options {backtrack=true; } : ( | argdefsWithTypes | argdefWithType | argdefsWithNames );
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.argdefsWithTypes_return argdefsWithTypes127 = null;

        EulangParser.argdefWithType_return argdefWithType128 = null;

        EulangParser.argdefsWithNames_return argdefsWithNames129 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:40: ( | argdefsWithTypes | argdefWithType | argdefsWithNames )
            int alt37=4;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==RPAREN||LA37_0==ARROW) ) {
                alt37=1;
            }
            else if ( (LA37_0==ID) ) {
                int LA37_3 = input.LA(2);

                if ( (synpred7_Eulang()) ) {
                    alt37=2;
                }
                else if ( (synpred8_Eulang()) ) {
                    alt37=3;
                }
                else if ( (true) ) {
                    alt37=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 37, 3, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:3: 
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:5: argdefsWithTypes
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithTypes_in_argdefs1812);
                    argdefsWithTypes127=argdefsWithTypes();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithTypes127.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:243:5: argdefWithType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefWithType_in_argdefs1819);
                    argdefWithType128=argdefWithType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefWithType128.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:244:6: argdefsWithNames
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithNames_in_argdefs1826);
                    argdefsWithNames129=argdefsWithNames();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithNames129.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:1: argdefsWithTypes : ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* ;
    public final EulangParser.argdefsWithTypes_return argdefsWithTypes() throws RecognitionException {
        EulangParser.argdefsWithTypes_return retval = new EulangParser.argdefsWithTypes_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI131=null;
        Token SEMI133=null;
        EulangParser.argdefWithType_return argdefWithType130 = null;

        EulangParser.argdefWithType_return argdefWithType132 = null;


        CommonTree SEMI131_tree=null;
        CommonTree SEMI133_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_argdefWithType=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithType");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:17: ( ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:20: argdefWithType ( SEMI argdefWithType )+ ( SEMI )?
            {
            pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1842);
            argdefWithType130=argdefWithType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType130.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:35: ( SEMI argdefWithType )+
            int cnt38=0;
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==SEMI) ) {
                    int LA38_1 = input.LA(2);

                    if ( (LA38_1==ID) ) {
                        alt38=1;
                    }


                }


                switch (alt38) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:37: SEMI argdefWithType
            	    {
            	    SEMI131=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1846); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI131);

            	    pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1848);
            	    argdefWithType132=argdefWithType();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType132.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt38 >= 1 ) break loop38;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(38, input);
                        throw eee;
                }
                cnt38++;
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:59: ( SEMI )?
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==SEMI) ) {
                alt39=1;
            }
            switch (alt39) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:59: SEMI
                    {
                    SEMI133=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1852); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI133);


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
            // 247:73: -> ( argdefWithType )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:247:76: ( argdefWithType )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:1: argdefWithType : ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+ ;
    public final EulangParser.argdefWithType_return argdefWithType() throws RecognitionException {
        EulangParser.argdefWithType_return retval = new EulangParser.argdefWithType_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID134=null;
        Token COMMA135=null;
        Token ID136=null;
        Token COLON138=null;
        Token EQUALS140=null;
        EulangParser.rhsExpr_return init = null;

        EulangParser.attrs_return attrs137 = null;

        EulangParser.type_return type139 = null;


        CommonTree ID134_tree=null;
        CommonTree COMMA135_tree=null;
        CommonTree ID136_tree=null;
        CommonTree COLON138_tree=null;
        CommonTree EQUALS140_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:15: ( ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:18: ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )?
            {
            ID134=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1881); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID134);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:21: ( COMMA ID )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==COMMA) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:22: COMMA ID
            	    {
            	    COMMA135=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefWithType1884); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA135);

            	    ID136=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1886); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID136);


            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:33: ( attrs )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==ATTR) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:33: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_argdefWithType1890);
                    attrs137=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs137.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:40: ( COLON type )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==COLON) ) {
                alt42=1;
            }
            switch (alt42) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:41: COLON type
                    {
                    COLON138=(Token)match(input,COLON,FOLLOW_COLON_in_argdefWithType1894); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON138);

                    pushFollow(FOLLOW_type_in_argdefWithType1896);
                    type139=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type139.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:54: ( EQUALS init= rhsExpr )?
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==EQUALS) ) {
                alt43=1;
            }
            switch (alt43) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:55: EQUALS init= rhsExpr
                    {
                    EQUALS140=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdefWithType1901); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS140);

                    pushFollow(FOLLOW_rhsExpr_in_argdefWithType1905);
                    init=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(init.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: ID, init, type, attrs
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
            // 251:78: -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+
            {
                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:81: ^( ARGDEF ID ( attrs )? ( type )* ( $init)? )
                    {
                    CommonTree root_1 = (CommonTree)adaptor.nil();
                    root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                    adaptor.addChild(root_1, stream_ID.nextNode());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:93: ( attrs )?
                    if ( stream_attrs.hasNext() ) {
                        adaptor.addChild(root_1, stream_attrs.nextTree());

                    }
                    stream_attrs.reset();
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:100: ( type )*
                    while ( stream_type.hasNext() ) {
                        adaptor.addChild(root_1, stream_type.nextTree());

                    }
                    stream_type.reset();
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:251:106: ( $init)?
                    if ( stream_init.hasNext() ) {
                        adaptor.addChild(root_1, stream_init.nextTree());

                    }
                    stream_init.reset();

                    adaptor.addChild(root_0, root_1);
                    }

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
    // $ANTLR end "argdefWithType"

    public static class argdefsWithNames_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "argdefsWithNames"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:1: argdefsWithNames : ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* ;
    public final EulangParser.argdefsWithNames_return argdefsWithNames() throws RecognitionException {
        EulangParser.argdefsWithNames_return retval = new EulangParser.argdefsWithNames_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA142=null;
        Token COMMA144=null;
        EulangParser.argdefWithName_return argdefWithName141 = null;

        EulangParser.argdefWithName_return argdefWithName143 = null;


        CommonTree COMMA142_tree=null;
        CommonTree COMMA144_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdefWithName=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithName");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:18: ( ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:22: argdefWithName ( COMMA argdefWithName )+ ( COMMA )?
            {
            pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1940);
            argdefWithName141=argdefWithName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName141.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:37: ( COMMA argdefWithName )+
            int cnt44=0;
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==COMMA) ) {
                    int LA44_1 = input.LA(2);

                    if ( (LA44_1==ID) ) {
                        alt44=1;
                    }


                }


                switch (alt44) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:39: COMMA argdefWithName
            	    {
            	    COMMA142=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1944); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA142);

            	    pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1946);
            	    argdefWithName143=argdefWithName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName143.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt44 >= 1 ) break loop44;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(44, input);
                        throw eee;
                }
                cnt44++;
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:62: ( COMMA )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==COMMA) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:62: COMMA
                    {
                    COMMA144=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1950); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA144);


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
            // 254:73: -> ( argdefWithName )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:76: ( argdefWithName )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:1: argdefWithName : ID ( attrs )? -> ^( ARGDEF ID ( attrs )? ) ;
    public final EulangParser.argdefWithName_return argdefWithName() throws RecognitionException {
        EulangParser.argdefWithName_return retval = new EulangParser.argdefWithName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID145=null;
        EulangParser.attrs_return attrs146 = null;


        CommonTree ID145_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:15: ( ID ( attrs )? -> ^( ARGDEF ID ( attrs )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:17: ID ( attrs )?
            {
            ID145=(Token)match(input,ID,FOLLOW_ID_in_argdefWithName1972); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID145);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:20: ( attrs )?
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==ATTR) ) {
                alt46=1;
            }
            switch (alt46) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:20: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_argdefWithName1974);
                    attrs146=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs146.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: ID, attrs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 256:28: -> ^( ARGDEF ID ( attrs )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:31: ^( ARGDEF ID ( attrs )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:256:43: ( attrs )?
                if ( stream_attrs.hasNext() ) {
                    adaptor.addChild(root_1, stream_attrs.nextTree());

                }
                stream_attrs.reset();

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:1: proto : LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN147=null;
        Token RPAREN150=null;
        EulangParser.argdefs_return argdefs148 = null;

        EulangParser.xreturns_return xreturns149 = null;


        CommonTree LPAREN147_tree=null;
        CommonTree RPAREN150_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:7: ( LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:9: LPAREN argdefs ( xreturns )? RPAREN
            {
            LPAREN147=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto1999); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN147);

            pushFollow(FOLLOW_argdefs_in_proto2001);
            argdefs148=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs148.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:24: ( xreturns )?
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==ARROW) ) {
                alt47=1;
            }
            switch (alt47) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:24: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto2003);
                    xreturns149=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns149.getTree());

                    }
                    break;

            }

            RPAREN150=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto2006); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN150);



            // AST REWRITE
            // elements: xreturns, argdefs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 260:59: -> ^( PROTO ( xreturns )? ( argdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:62: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:70: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:260:80: ( argdefs )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:263:1: xreturns : ( ARROW type -> type | ARROW NIL -> ^( TYPE NIL ) );
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ARROW151=null;
        Token ARROW153=null;
        Token NIL154=null;
        EulangParser.type_return type152 = null;


        CommonTree ARROW151_tree=null;
        CommonTree ARROW153_tree=null;
        CommonTree NIL154_tree=null;
        RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:263:10: ( ARROW type -> type | ARROW NIL -> ^( TYPE NIL ) )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==ARROW) ) {
                int LA48_1 = input.LA(2);

                if ( (LA48_1==NIL) ) {
                    alt48=2;
                }
                else if ( (LA48_1==CODE||LA48_1==ID||LA48_1==COLON||LA48_1==LPAREN||(LA48_1>=COLONS && LA48_1<=DATA)) ) {
                    alt48=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 48, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }
            switch (alt48) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:263:12: ARROW type
                    {
                    ARROW151=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns2049); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW151);

                    pushFollow(FOLLOW_type_in_xreturns2051);
                    type152=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type152.getTree());


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
                    // 263:28: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:5: ARROW NIL
                    {
                    ARROW153=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns2068); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW153);

                    NIL154=(Token)match(input,NIL,FOLLOW_NIL_in_xreturns2070); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL154);



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
                    // 265:26: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:29: ^( TYPE NIL )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:1: argtuple : LPAREN tupleargdefs RPAREN -> ^( TUPLETYPE tupleargdefs ) ;
    public final EulangParser.argtuple_return argtuple() throws RecognitionException {
        EulangParser.argtuple_return retval = new EulangParser.argtuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN155=null;
        Token RPAREN157=null;
        EulangParser.tupleargdefs_return tupleargdefs156 = null;


        CommonTree LPAREN155_tree=null;
        CommonTree RPAREN157_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleargdefs=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:10: ( LPAREN tupleargdefs RPAREN -> ^( TUPLETYPE tupleargdefs ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:12: LPAREN tupleargdefs RPAREN
            {
            LPAREN155=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_argtuple2100); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN155);

            pushFollow(FOLLOW_tupleargdefs_in_argtuple2102);
            tupleargdefs156=tupleargdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdefs.add(tupleargdefs156.getTree());
            RPAREN157=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_argtuple2104); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN157);



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
            // 268:42: -> ^( TUPLETYPE tupleargdefs )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:45: ^( TUPLETYPE tupleargdefs )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TUPLETYPE, "TUPLETYPE"), root_1);

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:1: tupleargdefs : ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* ;
    public final EulangParser.tupleargdefs_return tupleargdefs() throws RecognitionException {
        EulangParser.tupleargdefs_return retval = new EulangParser.tupleargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA159=null;
        EulangParser.tupleargdef_return tupleargdef158 = null;

        EulangParser.tupleargdef_return tupleargdef160 = null;


        CommonTree COMMA159_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_tupleargdef=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:13: ( ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:15: ( tupleargdef ( COMMA tupleargdef )+ )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:15: ( tupleargdef ( COMMA tupleargdef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:16: tupleargdef ( COMMA tupleargdef )+
            {
            pushFollow(FOLLOW_tupleargdef_in_tupleargdefs2126);
            tupleargdef158=tupleargdef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef158.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:28: ( COMMA tupleargdef )+
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:30: COMMA tupleargdef
            	    {
            	    COMMA159=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleargdefs2130); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA159);

            	    pushFollow(FOLLOW_tupleargdef_in_tupleargdefs2132);
            	    tupleargdef160=tupleargdef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef160.getTree());

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
            // 271:75: -> ( tupleargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:271:78: ( tupleargdef )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:274:1: tupleargdef : ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) );
    public final EulangParser.tupleargdef_return tupleargdef() throws RecognitionException {
        EulangParser.tupleargdef_return retval = new EulangParser.tupleargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION162=null;
        EulangParser.type_return type161 = null;


        CommonTree QUESTION162_tree=null;
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:274:12: ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) )
            int alt50=3;
            switch ( input.LA(1) ) {
            case CODE:
            case ID:
            case COLON:
            case LPAREN:
            case COLONS:
            case DATA:
                {
                alt50=1;
                }
                break;
            case QUESTION:
                {
                alt50=2;
                }
                break;
            case COMMA:
            case RPAREN:
                {
                alt50=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }

            switch (alt50) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:274:14: type
                    {
                    pushFollow(FOLLOW_type_in_tupleargdef2177);
                    type161=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type161.getTree());


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
                    // 274:22: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:5: QUESTION
                    {
                    QUESTION162=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_tupleargdef2190); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION162);



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
                    // 275:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:275:24: ^( TYPE NIL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:21: 
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
                    // 276:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:24: ^( TYPE NIL )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:1: type : ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )* ;
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET165=null;
        Token COMMA167=null;
        Token RBRACKET169=null;
        Token CARET170=null;
        EulangParser.nonArrayType_return nonArrayType163 = null;

        EulangParser.arraySuff_return arraySuff164 = null;

        EulangParser.rhsExpr_return rhsExpr166 = null;

        EulangParser.rhsExpr_return rhsExpr168 = null;


        CommonTree LBRACKET165_tree=null;
        CommonTree COMMA167_tree=null;
        CommonTree RBRACKET169_tree=null;
        CommonTree CARET170_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_arraySuff=new RewriteRuleSubtreeStream(adaptor,"rule arraySuff");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_nonArrayType=new RewriteRuleSubtreeStream(adaptor,"rule nonArrayType");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:282:6: ( ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:5: ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:5: ( nonArrayType -> nonArrayType )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:6: nonArrayType
            {
            pushFollow(FOLLOW_nonArrayType_in_type2255);
            nonArrayType163=nonArrayType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_nonArrayType.add(nonArrayType163.getTree());


            // AST REWRITE
            // elements: nonArrayType
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 283:19: -> nonArrayType
            {
                adaptor.addChild(root_0, stream_nonArrayType.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:6: ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*
            loop53:
            do {
                int alt53=4;
                alt53 = dfa53.predict(input);
                switch (alt53) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:7: ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:285:7: ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:9: ( ( arraySuff )+ )=> ( arraySuff )+
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:25: ( arraySuff )+
            	    int cnt51=0;
            	    loop51:
            	    do {
            	        int alt51=2;
            	        int LA51_0 = input.LA(1);

            	        if ( (LA51_0==LBRACKET) ) {
            	            alt51=1;
            	        }


            	        switch (alt51) {
            	    	case 1 :
            	    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:25: arraySuff
            	    	    {
            	    	    pushFollow(FOLLOW_arraySuff_in_type2293);
            	    	    arraySuff164=arraySuff();

            	    	    state._fsp--;
            	    	    if (state.failed) return retval;
            	    	    if ( state.backtracking==0 ) stream_arraySuff.add(arraySuff164.getTree());

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt51 >= 1 ) break loop51;
            	    	    if (state.backtracking>0) {state.failed=true; return retval;}
            	                EarlyExitException eee =
            	                    new EarlyExitException(51, input);
            	                throw eee;
            	        }
            	        cnt51++;
            	    } while (true);



            	    // AST REWRITE
            	    // elements: arraySuff, type
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 286:36: -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:39: ^( TYPE ^( ARRAY $type ( arraySuff )+ ) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:46: ^( ARRAY $type ( arraySuff )+ )
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARRAY, "ARRAY"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());
            	        if ( !(stream_arraySuff.hasNext()) ) {
            	            throw new RewriteEarlyExitException();
            	        }
            	        while ( stream_arraySuff.hasNext() ) {
            	            adaptor.addChild(root_2, stream_arraySuff.nextTree());

            	        }
            	        stream_arraySuff.reset();

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:289:8: ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:289:8: ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:290:9: LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET
            	    {
            	    LBRACKET165=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_type2348); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET165);

            	    pushFollow(FOLLOW_rhsExpr_in_type2350);
            	    rhsExpr166=rhsExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr166.getTree());
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:290:26: ( COMMA rhsExpr )+
            	    int cnt52=0;
            	    loop52:
            	    do {
            	        int alt52=2;
            	        int LA52_0 = input.LA(1);

            	        if ( (LA52_0==COMMA) ) {
            	            alt52=1;
            	        }


            	        switch (alt52) {
            	    	case 1 :
            	    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:290:28: COMMA rhsExpr
            	    	    {
            	    	    COMMA167=(Token)match(input,COMMA,FOLLOW_COMMA_in_type2354); if (state.failed) return retval; 
            	    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA167);

            	    	    pushFollow(FOLLOW_rhsExpr_in_type2356);
            	    	    rhsExpr168=rhsExpr();

            	    	    state._fsp--;
            	    	    if (state.failed) return retval;
            	    	    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr168.getTree());

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt52 >= 1 ) break loop52;
            	    	    if (state.backtracking>0) {state.failed=true; return retval;}
            	                EarlyExitException eee =
            	                    new EarlyExitException(52, input);
            	                throw eee;
            	        }
            	        cnt52++;
            	    } while (true);

            	    RBRACKET169=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_type2361); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET169);



            	    // AST REWRITE
            	    // elements: rhsExpr, type
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 290:54: -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:290:57: ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:290:64: ^( ARRAY $type ( rhsExpr )+ )
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARRAY, "ARRAY"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());
            	        if ( !(stream_rhsExpr.hasNext()) ) {
            	            throw new RewriteEarlyExitException();
            	        }
            	        while ( stream_rhsExpr.hasNext() ) {
            	            adaptor.addChild(root_2, stream_rhsExpr.nextTree());

            	        }
            	        stream_rhsExpr.reset();

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:8: ( CARET -> ^( TYPE ^( POINTER $type) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:8: ( CARET -> ^( TYPE ^( POINTER $type) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:10: CARET
            	    {
            	    CARET170=(Token)match(input,CARET,FOLLOW_CARET_in_type2420); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET170);



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
            	    // 294:16: -> ^( TYPE ^( POINTER $type) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:19: ^( TYPE ^( POINTER $type) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:26: ^( POINTER $type)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(POINTER, "POINTER"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop53;
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
    // $ANTLR end "type"

    public static class nonArrayType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nonArrayType"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:1: nonArrayType : ( ( idExpr -> ^( TYPE idExpr ) ) | ( CODE ( attrs )? ( proto )? -> ^( TYPE ^( CODE ( attrs )? ( proto )? ) ) ) | data -> ^( TYPE data ) | argtuple );
    public final EulangParser.nonArrayType_return nonArrayType() throws RecognitionException {
        EulangParser.nonArrayType_return retval = new EulangParser.nonArrayType_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE172=null;
        EulangParser.idExpr_return idExpr171 = null;

        EulangParser.attrs_return attrs173 = null;

        EulangParser.proto_return proto174 = null;

        EulangParser.data_return data175 = null;

        EulangParser.argtuple_return argtuple176 = null;


        CommonTree CODE172_tree=null;
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_idExpr=new RewriteRuleSubtreeStream(adaptor,"rule idExpr");
        RewriteRuleSubtreeStream stream_data=new RewriteRuleSubtreeStream(adaptor,"rule data");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:14: ( ( idExpr -> ^( TYPE idExpr ) ) | ( CODE ( attrs )? ( proto )? -> ^( TYPE ^( CODE ( attrs )? ( proto )? ) ) ) | data -> ^( TYPE data ) | argtuple )
            int alt56=4;
            switch ( input.LA(1) ) {
            case ID:
            case COLON:
            case COLONS:
                {
                alt56=1;
                }
                break;
            case CODE:
                {
                alt56=2;
                }
                break;
            case DATA:
                {
                alt56=3;
                }
                break;
            case LPAREN:
                {
                alt56=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 56, 0, input);

                throw nvae;
            }

            switch (alt56) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:300:4: ( idExpr -> ^( TYPE idExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:300:4: ( idExpr -> ^( TYPE idExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:300:6: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_nonArrayType2472);
                    idExpr171=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr171.getTree());


                    // AST REWRITE
                    // elements: idExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 300:13: -> ^( TYPE idExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:300:16: ^( TYPE idExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_idExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:5: ( CODE ( attrs )? ( proto )? -> ^( TYPE ^( CODE ( attrs )? ( proto )? ) ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:5: ( CODE ( attrs )? ( proto )? -> ^( TYPE ^( CODE ( attrs )? ( proto )? ) ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:7: CODE ( attrs )? ( proto )?
                    {
                    CODE172=(Token)match(input,CODE,FOLLOW_CODE_in_nonArrayType2490); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CODE.add(CODE172);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:12: ( attrs )?
                    int alt54=2;
                    int LA54_0 = input.LA(1);

                    if ( (LA54_0==ATTR) ) {
                        alt54=1;
                    }
                    switch (alt54) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:12: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_nonArrayType2492);
                            attrs173=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs173.getTree());

                            }
                            break;

                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:19: ( proto )?
                    int alt55=2;
                    int LA55_0 = input.LA(1);

                    if ( (LA55_0==LPAREN) ) {
                        alt55=1;
                    }
                    switch (alt55) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:19: proto
                            {
                            pushFollow(FOLLOW_proto_in_nonArrayType2495);
                            proto174=proto();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_proto.add(proto174.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: attrs, CODE, proto
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 301:26: -> ^( TYPE ^( CODE ( attrs )? ( proto )? ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:29: ^( TYPE ^( CODE ( attrs )? ( proto )? ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:36: ^( CODE ( attrs )? ( proto )? )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_2);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:43: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_2, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:301:50: ( proto )?
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


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:5: data
                    {
                    pushFollow(FOLLOW_data_in_nonArrayType2521);
                    data175=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_data.add(data175.getTree());


                    // AST REWRITE
                    // elements: data
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 302:10: -> ^( TYPE data )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:13: ^( TYPE data )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        adaptor.addChild(root_1, stream_data.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:5: argtuple
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argtuple_in_nonArrayType2537);
                    argtuple176=argtuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argtuple176.getTree());

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
    // $ANTLR end "nonArrayType"

    public static class arraySuff_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arraySuff"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:305:1: arraySuff : ( LBRACKET rhsExpr RBRACKET -> rhsExpr | LBRACKET RBRACKET -> FALSE );
    public final EulangParser.arraySuff_return arraySuff() throws RecognitionException {
        EulangParser.arraySuff_return retval = new EulangParser.arraySuff_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET177=null;
        Token RBRACKET179=null;
        Token LBRACKET180=null;
        Token RBRACKET181=null;
        EulangParser.rhsExpr_return rhsExpr178 = null;


        CommonTree LBRACKET177_tree=null;
        CommonTree RBRACKET179_tree=null;
        CommonTree LBRACKET180_tree=null;
        CommonTree RBRACKET181_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:305:11: ( LBRACKET rhsExpr RBRACKET -> rhsExpr | LBRACKET RBRACKET -> FALSE )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==LBRACKET) ) {
                int LA57_1 = input.LA(2);

                if ( (LA57_1==RBRACKET) ) {
                    alt57=2;
                }
                else if ( (LA57_1==CODE||(LA57_1>=SIZEOF && LA57_1<=TYPEOF)||LA57_1==ID||LA57_1==COLON||LA57_1==LPAREN||LA57_1==NIL||LA57_1==IF||LA57_1==NOT||(LA57_1>=TILDE && LA57_1<=AMP)||LA57_1==MINUS||(LA57_1>=PLUSPLUS && LA57_1<=STRING_LITERAL)||(LA57_1>=FALSE && LA57_1<=COLONS)) ) {
                    alt57=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 57, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;
            }
            switch (alt57) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:305:13: LBRACKET rhsExpr RBRACKET
                    {
                    LBRACKET177=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arraySuff2553); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET177);

                    pushFollow(FOLLOW_rhsExpr_in_arraySuff2555);
                    rhsExpr178=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr178.getTree());
                    RBRACKET179=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arraySuff2557); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET179);



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
                    // 305:39: -> rhsExpr
                    {
                        adaptor.addChild(root_0, stream_rhsExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:7: LBRACKET RBRACKET
                    {
                    LBRACKET180=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arraySuff2569); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET180);

                    RBRACKET181=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arraySuff2571); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET181);



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
                    // 306:25: -> FALSE
                    {
                        adaptor.addChild(root_0, (CommonTree)adaptor.create(FALSE, "FALSE"));

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
    // $ANTLR end "arraySuff"

    public static class codestmtlist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "codestmtlist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:1: codestmtlist : ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) );
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI183=null;
        EulangParser.codeStmt_return codeStmt182 = null;

        EulangParser.codeStmt_return codeStmt184 = null;


        CommonTree SEMI183_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:13: ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) )
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==CODE||LA60_0==GOTO||(LA60_0>=SIZEOF && LA60_0<=TYPEOF)||LA60_0==ID||LA60_0==COLON||(LA60_0>=LPAREN && LA60_0<=LBRACE)||LA60_0==FOR||LA60_0==NIL||(LA60_0>=DO && LA60_0<=REPEAT)||(LA60_0>=ATSIGN && LA60_0<=IF)||LA60_0==NOT||(LA60_0>=TILDE && LA60_0<=AMP)||LA60_0==MINUS||(LA60_0>=PLUSPLUS && LA60_0<=STRING_LITERAL)||(LA60_0>=FALSE && LA60_0<=COLONS)) ) {
                alt60=1;
            }
            else if ( (LA60_0==RBRACE) ) {
                alt60=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                throw nvae;
            }
            switch (alt60) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:16: codeStmt ( SEMI ( codeStmt )? )*
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist2587);
                    codeStmt182=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt182.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:25: ( SEMI ( codeStmt )? )*
                    loop59:
                    do {
                        int alt59=2;
                        int LA59_0 = input.LA(1);

                        if ( (LA59_0==SEMI) ) {
                            alt59=1;
                        }


                        switch (alt59) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:26: SEMI ( codeStmt )?
                    	    {
                    	    SEMI183=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist2590); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI183);

                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:31: ( codeStmt )?
                    	    int alt58=2;
                    	    int LA58_0 = input.LA(1);

                    	    if ( (LA58_0==CODE||LA58_0==GOTO||(LA58_0>=SIZEOF && LA58_0<=TYPEOF)||LA58_0==ID||LA58_0==COLON||(LA58_0>=LPAREN && LA58_0<=LBRACE)||LA58_0==FOR||LA58_0==NIL||(LA58_0>=DO && LA58_0<=REPEAT)||(LA58_0>=ATSIGN && LA58_0<=IF)||LA58_0==NOT||(LA58_0>=TILDE && LA58_0<=AMP)||LA58_0==MINUS||(LA58_0>=PLUSPLUS && LA58_0<=STRING_LITERAL)||(LA58_0>=FALSE && LA58_0<=COLONS)) ) {
                    	        alt58=1;
                    	    }
                    	    switch (alt58) {
                    	        case 1 :
                    	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:31: codeStmt
                    	            {
                    	            pushFollow(FOLLOW_codeStmt_in_codestmtlist2592);
                    	            codeStmt184=codeStmt();

                    	            state._fsp--;
                    	            if (state.failed) return retval;
                    	            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt184.getTree());

                    	            }
                    	            break;

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop59;
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
                    // 308:44: -> ^( STMTLIST ( codeStmt )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:48: ^( STMTLIST ( codeStmt )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:308:59: ( codeStmt )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:309:7: 
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
                    // 309:7: -> ^( STMTLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:309:10: ^( STMTLIST )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:1: codeStmt : ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.labelStmt_return labelStmt185 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr186 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr187 = null;


        RewriteRuleSubtreeStream stream_labelStmt=new RewriteRuleSubtreeStream(adaptor,"rule labelStmt");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:10: ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr )
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0==ATSIGN) ) {
                alt61=1;
            }
            else if ( (LA61_0==CODE||LA61_0==GOTO||(LA61_0>=SIZEOF && LA61_0<=TYPEOF)||LA61_0==ID||LA61_0==COLON||(LA61_0>=LPAREN && LA61_0<=LBRACE)||LA61_0==FOR||LA61_0==NIL||(LA61_0>=DO && LA61_0<=REPEAT)||LA61_0==IF||LA61_0==NOT||(LA61_0>=TILDE && LA61_0<=AMP)||LA61_0==MINUS||(LA61_0>=PLUSPLUS && LA61_0<=STRING_LITERAL)||(LA61_0>=FALSE && LA61_0<=COLONS)) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:12: labelStmt codeStmtExpr
                    {
                    pushFollow(FOLLOW_labelStmt_in_codeStmt2636);
                    labelStmt185=labelStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labelStmt.add(labelStmt185.getTree());
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt2638);
                    codeStmtExpr186=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr186.getTree());


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
                    // 312:36: -> ^( LABELSTMT labelStmt codeStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:39: ^( LABELSTMT labelStmt codeStmtExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:9: codeStmtExpr
                    {
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt2659);
                    codeStmtExpr187=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr187.getTree());


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
                    // 313:22: -> codeStmtExpr
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:1: codeStmtExpr : ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );
    public final EulangParser.codeStmtExpr_return codeStmtExpr() throws RecognitionException {
        EulangParser.codeStmtExpr_return retval = new EulangParser.codeStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl188 = null;

        EulangParser.assignStmt_return assignStmt189 = null;

        EulangParser.rhsExpr_return rhsExpr190 = null;

        EulangParser.blockStmt_return blockStmt191 = null;

        EulangParser.gotoStmt_return gotoStmt192 = null;

        EulangParser.controlStmt_return controlStmt193 = null;


        RewriteRuleSubtreeStream stream_assignStmt=new RewriteRuleSubtreeStream(adaptor,"rule assignStmt");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_blockStmt=new RewriteRuleSubtreeStream(adaptor,"rule blockStmt");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_controlStmt=new RewriteRuleSubtreeStream(adaptor,"rule controlStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:316:14: ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt )
            int alt62=6;
            alt62 = dfa62.predict(input);
            switch (alt62) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:317:7: ( varDecl )=> varDecl
                    {
                    pushFollow(FOLLOW_varDecl_in_codeStmtExpr2691);
                    varDecl188=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl188.getTree());


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
                    // 317:32: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:9: ( assignStmt )=> assignStmt
                    {
                    pushFollow(FOLLOW_assignStmt_in_codeStmtExpr2714);
                    assignStmt189=assignStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignStmt.add(assignStmt189.getTree());


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
                    // 318:39: -> assignStmt
                    {
                        adaptor.addChild(root_0, stream_assignStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:9: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_codeStmtExpr2731);
                    rhsExpr190=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr190.getTree());


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
                    // 319:23: -> ^( STMTEXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:319:27: ^( STMTEXPR rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:9: ( LBRACE )=> blockStmt
                    {
                    pushFollow(FOLLOW_blockStmt_in_codeStmtExpr2764);
                    blockStmt191=blockStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_blockStmt.add(blockStmt191.getTree());


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
                    // 320:41: -> blockStmt
                    {
                        adaptor.addChild(root_0, stream_blockStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:321:9: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_codeStmtExpr2786);
                    gotoStmt192=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt192.getTree());


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
                    // 321:23: -> gotoStmt
                    {
                        adaptor.addChild(root_0, stream_gotoStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:9: controlStmt
                    {
                    pushFollow(FOLLOW_controlStmt_in_codeStmtExpr2812);
                    controlStmt193=controlStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_controlStmt.add(controlStmt193.getTree());


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
                    // 323:26: -> controlStmt
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:1: varDecl : ( singleVarDecl | tupleVarDecl );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.singleVarDecl_return singleVarDecl194 = null;

        EulangParser.tupleVarDecl_return tupleVarDecl195 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:8: ( singleVarDecl | tupleVarDecl )
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==ID) ) {
                alt63=1;
            }
            else if ( (LA63_0==LPAREN) ) {
                alt63=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 63, 0, input);

                throw nvae;
            }
            switch (alt63) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:10: singleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_singleVarDecl_in_varDecl2835);
                    singleVarDecl194=singleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, singleVarDecl194.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:26: tupleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_tupleVarDecl_in_varDecl2839);
                    tupleVarDecl195=tupleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, tupleVarDecl195.getTree());

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

    public static class singleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "singleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:1: singleVarDecl : ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) ) ;
    public final EulangParser.singleVarDecl_return singleVarDecl() throws RecognitionException {
        EulangParser.singleVarDecl_return retval = new EulangParser.singleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID196=null;
        Token COLON_EQUALS198=null;
        Token COLON201=null;
        Token EQUALS203=null;
        Token COMMA205=null;
        Token ID206=null;
        Token COLON_EQUALS208=null;
        Token PLUS209=null;
        Token COMMA211=null;
        Token COLON213=null;
        Token EQUALS215=null;
        Token PLUS216=null;
        Token COMMA218=null;
        EulangParser.attrs_return attrs197 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr199 = null;

        EulangParser.attrs_return attrs200 = null;

        EulangParser.type_return type202 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr204 = null;

        EulangParser.attrs_return attrs207 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr210 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr212 = null;

        EulangParser.type_return type214 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr217 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr219 = null;


        CommonTree ID196_tree=null;
        CommonTree COLON_EQUALS198_tree=null;
        CommonTree COLON201_tree=null;
        CommonTree EQUALS203_tree=null;
        CommonTree COMMA205_tree=null;
        CommonTree ID206_tree=null;
        CommonTree COLON_EQUALS208_tree=null;
        CommonTree PLUS209_tree=null;
        CommonTree COMMA211_tree=null;
        CommonTree COLON213_tree=null;
        CommonTree EQUALS215_tree=null;
        CommonTree PLUS216_tree=null;
        CommonTree COMMA218_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:14: ( ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:5: ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )
            {
            ID196=(Token)match(input,ID,FOLLOW_ID_in_singleVarDecl2851); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID196);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:8: ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )
            int alt75=3;
            alt75 = dfa75.predict(input);
            switch (alt75) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:9: ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:9: ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:11: ( attrs )? COLON_EQUALS assignOrInitExpr
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:11: ( attrs )?
                    int alt64=2;
                    int LA64_0 = input.LA(1);

                    if ( (LA64_0==ATTR) ) {
                        alt64=1;
                    }
                    switch (alt64) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:11: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2865);
                            attrs197=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs197.getTree());

                            }
                            break;

                    }

                    COLON_EQUALS198=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_singleVarDecl2868); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS198);

                    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2870);
                    assignOrInitExpr199=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr199.getTree());


                    // AST REWRITE
                    // elements: attrs, ID, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 330:56: -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:59: ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:67: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:9: ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:9: ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:11: ( attrs )? COLON type ( EQUALS assignOrInitExpr )?
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:11: ( attrs )?
                    int alt65=2;
                    int LA65_0 = input.LA(1);

                    if ( (LA65_0==ATTR) ) {
                        alt65=1;
                    }
                    switch (alt65) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:11: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2907);
                            attrs200=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs200.getTree());

                            }
                            break;

                    }

                    COLON201=(Token)match(input,COLON,FOLLOW_COLON_in_singleVarDecl2910); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON201);

                    pushFollow(FOLLOW_type_in_singleVarDecl2912);
                    type202=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type202.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:29: ( EQUALS assignOrInitExpr )?
                    int alt66=2;
                    int LA66_0 = input.LA(1);

                    if ( (LA66_0==EQUALS) ) {
                        alt66=1;
                    }
                    switch (alt66) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:30: EQUALS assignOrInitExpr
                            {
                            EQUALS203=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_singleVarDecl2915); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS203);

                            pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2917);
                            assignOrInitExpr204=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr204.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: attrs, assignOrInitExpr, type, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 331:57: -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:60: ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:68: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:331:83: ( assignOrInitExpr )*
                        while ( stream_assignOrInitExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        }
                        stream_assignOrInitExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:332:9: ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:332:9: ( COMMA ID )+
                    int cnt67=0;
                    loop67:
                    do {
                        int alt67=2;
                        int LA67_0 = input.LA(1);

                        if ( (LA67_0==COMMA) ) {
                            alt67=1;
                        }


                        switch (alt67) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:332:11: COMMA ID
                    	    {
                    	    COMMA205=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl2950); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA205);

                    	    ID206=(Token)match(input,ID,FOLLOW_ID_in_singleVarDecl2952); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID206);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt67 >= 1 ) break loop67;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(67, input);
                                throw eee;
                        }
                        cnt67++;
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:332:23: ( attrs )?
                    int alt68=2;
                    int LA68_0 = input.LA(1);

                    if ( (LA68_0==ATTR) ) {
                        alt68=1;
                    }
                    switch (alt68) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:332:23: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2957);
                            attrs207=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs207.getTree());

                            }
                            break;

                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:9: ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) )
                    int alt74=2;
                    int LA74_0 = input.LA(1);

                    if ( (LA74_0==COLON_EQUALS) ) {
                        alt74=1;
                    }
                    else if ( (LA74_0==COLON) ) {
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
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:12: ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:12: ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:14: COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                            {
                            COLON_EQUALS208=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_singleVarDecl2975); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS208);

                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:27: ( PLUS )?
                            int alt69=2;
                            int LA69_0 = input.LA(1);

                            if ( (LA69_0==PLUS) ) {
                                alt69=1;
                            }
                            switch (alt69) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:27: PLUS
                                    {
                                    PLUS209=(Token)match(input,PLUS,FOLLOW_PLUS_in_singleVarDecl2977); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS209);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2980);
                            assignOrInitExpr210=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr210.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:50: ( COMMA assignOrInitExpr )*
                            loop70:
                            do {
                                int alt70=2;
                                int LA70_0 = input.LA(1);

                                if ( (LA70_0==COMMA) ) {
                                    alt70=1;
                                }


                                switch (alt70) {
                            	case 1 :
                            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:333:51: COMMA assignOrInitExpr
                            	    {
                            	    COMMA211=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl2983); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA211);

                            	    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2985);
                            	    assignOrInitExpr212=assignOrInitExpr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr212.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop70;
                                }
                            } while (true);


                            }



                            // AST REWRITE
                            // elements: attrs, PLUS, ID, assignOrInitExpr
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 334:15: -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:18: ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:38: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:56: ^( LIST ( assignOrInitExpr )+ )
                                {
                                CommonTree root_2 = (CommonTree)adaptor.nil();
                                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                if ( !(stream_assignOrInitExpr.hasNext()) ) {
                                    throw new RewriteEarlyExitException();
                                }
                                while ( stream_assignOrInitExpr.hasNext() ) {
                                    adaptor.addChild(root_2, stream_assignOrInitExpr.nextTree());

                                }
                                stream_assignOrInitExpr.reset();

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:12: ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:12: ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:14: COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )?
                            {
                            COLON213=(Token)match(input,COLON,FOLLOW_COLON_in_singleVarDecl3047); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON213);

                            pushFollow(FOLLOW_type_in_singleVarDecl3049);
                            type214=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type214.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:25: ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )?
                            int alt73=2;
                            int LA73_0 = input.LA(1);

                            if ( (LA73_0==EQUALS) ) {
                                alt73=1;
                            }
                            switch (alt73) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:26: EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                                    {
                                    EQUALS215=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_singleVarDecl3052); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS215);

                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:33: ( PLUS )?
                                    int alt71=2;
                                    int LA71_0 = input.LA(1);

                                    if ( (LA71_0==PLUS) ) {
                                        alt71=1;
                                    }
                                    switch (alt71) {
                                        case 1 :
                                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:33: PLUS
                                            {
                                            PLUS216=(Token)match(input,PLUS,FOLLOW_PLUS_in_singleVarDecl3054); if (state.failed) return retval; 
                                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS216);


                                            }
                                            break;

                                    }

                                    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl3057);
                                    assignOrInitExpr217=assignOrInitExpr();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr217.getTree());
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:56: ( COMMA assignOrInitExpr )*
                                    loop72:
                                    do {
                                        int alt72=2;
                                        int LA72_0 = input.LA(1);

                                        if ( (LA72_0==COMMA) ) {
                                            alt72=1;
                                        }


                                        switch (alt72) {
                                    	case 1 :
                                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:57: COMMA assignOrInitExpr
                                    	    {
                                    	    COMMA218=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl3060); if (state.failed) return retval; 
                                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA218);

                                    	    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl3062);
                                    	    assignOrInitExpr219=assignOrInitExpr();

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr219.getTree());

                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop72;
                                        }
                                    } while (true);


                                    }
                                    break;

                            }


                            }



                            // AST REWRITE
                            // elements: type, PLUS, assignOrInitExpr, attrs, ID
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 336:15: -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:18: ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:26: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, stream_type.nextTree());
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:38: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:56: ( ^( LIST ( assignOrInitExpr )+ ) )?
                                if ( stream_assignOrInitExpr.hasNext() ) {
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:56: ^( LIST ( assignOrInitExpr )+ )
                                    {
                                    CommonTree root_2 = (CommonTree)adaptor.nil();
                                    root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                                    if ( !(stream_assignOrInitExpr.hasNext()) ) {
                                        throw new RewriteEarlyExitException();
                                    }
                                    while ( stream_assignOrInitExpr.hasNext() ) {
                                        adaptor.addChild(root_2, stream_assignOrInitExpr.nextTree());

                                    }
                                    stream_assignOrInitExpr.reset();

                                    adaptor.addChild(root_1, root_2);
                                    }

                                }
                                stream_assignOrInitExpr.reset();

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
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
    // $ANTLR end "singleVarDecl"

    public static class tupleVarDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tupleVarDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:340:1: tupleVarDecl : idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) ) ;
    public final EulangParser.tupleVarDecl_return tupleVarDecl() throws RecognitionException {
        EulangParser.tupleVarDecl_return retval = new EulangParser.tupleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON_EQUALS222=null;
        Token COLON224=null;
        Token EQUALS226=null;
        EulangParser.idTuple_return idTuple220 = null;

        EulangParser.attrs_return attrs221 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr223 = null;

        EulangParser.type_return type225 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr227 = null;


        CommonTree COLON_EQUALS222_tree=null;
        CommonTree COLON224_tree=null;
        CommonTree EQUALS226_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:340:13: ( idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:341:5: idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) )
            {
            pushFollow(FOLLOW_idTuple_in_tupleVarDecl3151);
            idTuple220=idTuple();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTuple.add(idTuple220.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:341:13: ( attrs )?
            int alt76=2;
            int LA76_0 = input.LA(1);

            if ( (LA76_0==ATTR) ) {
                alt76=1;
            }
            switch (alt76) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:341:13: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_tupleVarDecl3153);
                    attrs221=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs221.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:7: ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) ) )
            int alt78=2;
            int LA78_0 = input.LA(1);

            if ( (LA78_0==COLON_EQUALS) ) {
                alt78=1;
            }
            else if ( (LA78_0==COLON) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:10: ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:10: ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:12: COLON_EQUALS assignOrInitExpr
                    {
                    COLON_EQUALS222=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_tupleVarDecl3168); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS222);

                    pushFollow(FOLLOW_assignOrInitExpr_in_tupleVarDecl3170);
                    assignOrInitExpr223=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr223.getTree());


                    // AST REWRITE
                    // elements: assignOrInitExpr, idTuple, attrs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 342:50: -> ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:53: ^( ALLOC_TUPLE ( attrs )? TYPE idTuple assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:67: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:9: ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:9: ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:11: COLON type ( EQUALS assignOrInitExpr )?
                    {
                    COLON224=(Token)match(input,COLON,FOLLOW_COLON_in_tupleVarDecl3207); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON224);

                    pushFollow(FOLLOW_type_in_tupleVarDecl3209);
                    type225=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type225.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:22: ( EQUALS assignOrInitExpr )?
                    int alt77=2;
                    int LA77_0 = input.LA(1);

                    if ( (LA77_0==EQUALS) ) {
                        alt77=1;
                    }
                    switch (alt77) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:23: EQUALS assignOrInitExpr
                            {
                            EQUALS226=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_tupleVarDecl3212); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS226);

                            pushFollow(FOLLOW_assignOrInitExpr_in_tupleVarDecl3214);
                            assignOrInitExpr227=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr227.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: idTuple, type, attrs, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 343:50: -> ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:53: ^( ALLOC_TUPLE ( attrs )? type idTuple ( assignOrInitExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:67: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:87: ( assignOrInitExpr )*
                        while ( stream_assignOrInitExpr.hasNext() ) {
                            adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        }
                        stream_assignOrInitExpr.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
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
    // $ANTLR end "tupleVarDecl"

    public static class assignStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:1: assignStmt : ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) );
    public final EulangParser.assignStmt_return assignStmt() throws RecognitionException {
        EulangParser.assignStmt_return retval = new EulangParser.assignStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS232=null;
        Token COMMA235=null;
        Token PLUS238=null;
        Token COMMA240=null;
        EulangParser.lhs_return lhs228 = null;

        EulangParser.assignEqOp_return assignEqOp229 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr230 = null;

        EulangParser.idTuple_return idTuple231 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr233 = null;

        EulangParser.lhs_return lhs234 = null;

        EulangParser.lhs_return lhs236 = null;

        EulangParser.assignEqOp_return assignEqOp237 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr239 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr241 = null;


        CommonTree EQUALS232_tree=null;
        CommonTree COMMA235_tree=null;
        CommonTree PLUS238_tree=null;
        CommonTree COMMA240_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignEqOp=new RewriteRuleSubtreeStream(adaptor,"rule assignEqOp");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:12: ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) )
            int alt82=3;
            alt82 = dfa82.predict(input);
            switch (alt82) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:14: ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr
                    {
                    pushFollow(FOLLOW_lhs_in_assignStmt3265);
                    lhs228=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs228.getTree());
                    pushFollow(FOLLOW_assignEqOp_in_assignStmt3267);
                    assignEqOp229=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp229.getTree());
                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3269);
                    assignOrInitExpr230=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr230.getTree());


                    // AST REWRITE
                    // elements: assignEqOp, lhs, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 348:73: -> ^( ASSIGN assignEqOp lhs assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:76: ^( ASSIGN assignEqOp lhs assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_assignEqOp.nextTree());
                        adaptor.addChild(root_1, stream_lhs.nextTree());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:7: idTuple EQUALS assignOrInitExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignStmt3296);
                    idTuple231=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple231.getTree());
                    EQUALS232=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt3298); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS232);

                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3300);
                    assignOrInitExpr233=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr233.getTree());


                    // AST REWRITE
                    // elements: EQUALS, idTuple, assignOrInitExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 349:53: -> ^( ASSIGN EQUALS idTuple assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:56: ^( ASSIGN EQUALS idTuple assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_EQUALS.nextNode());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:7: ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                    {
                    pushFollow(FOLLOW_lhs_in_assignStmt3355);
                    lhs234=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs234.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:45: ( COMMA lhs )+
                    int cnt79=0;
                    loop79:
                    do {
                        int alt79=2;
                        int LA79_0 = input.LA(1);

                        if ( (LA79_0==COMMA) ) {
                            alt79=1;
                        }


                        switch (alt79) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:46: COMMA lhs
                    	    {
                    	    COMMA235=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt3358); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA235);

                    	    pushFollow(FOLLOW_lhs_in_assignStmt3360);
                    	    lhs236=lhs();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_lhs.add(lhs236.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt79 >= 1 ) break loop79;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(79, input);
                                throw eee;
                        }
                        cnt79++;
                    } while (true);

                    pushFollow(FOLLOW_assignEqOp_in_assignStmt3364);
                    assignEqOp237=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp237.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:69: ( PLUS )?
                    int alt80=2;
                    int LA80_0 = input.LA(1);

                    if ( (LA80_0==PLUS) ) {
                        alt80=1;
                    }
                    switch (alt80) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:69: PLUS
                            {
                            PLUS238=(Token)match(input,PLUS,FOLLOW_PLUS_in_assignStmt3366); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS238);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3369);
                    assignOrInitExpr239=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr239.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:92: ( COMMA assignOrInitExpr )*
                    loop81:
                    do {
                        int alt81=2;
                        int LA81_0 = input.LA(1);

                        if ( (LA81_0==COMMA) ) {
                            alt81=1;
                        }


                        switch (alt81) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:93: COMMA assignOrInitExpr
                    	    {
                    	    COMMA240=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt3372); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA240);

                    	    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3374);
                    	    assignOrInitExpr241=assignOrInitExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr241.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop81;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: assignOrInitExpr, PLUS, assignEqOp, lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 352:9: -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:12: ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_assignEqOp.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:32: ^( LIST ( lhs )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_lhs.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_lhs.hasNext() ) {
                            adaptor.addChild(root_2, stream_lhs.nextTree());

                        }
                        stream_lhs.reset();

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:45: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:51: ^( LIST ( assignOrInitExpr )+ )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                        if ( !(stream_assignOrInitExpr.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_assignOrInitExpr.hasNext() ) {
                            adaptor.addChild(root_2, stream_assignOrInitExpr.nextTree());

                        }
                        stream_assignOrInitExpr.reset();

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

    public static class assignOrInitExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignOrInitExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:355:1: assignOrInitExpr : ( assignExpr | initList );
    public final EulangParser.assignOrInitExpr_return assignOrInitExpr() throws RecognitionException {
        EulangParser.assignOrInitExpr_return retval = new EulangParser.assignOrInitExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr242 = null;

        EulangParser.initList_return initList243 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:355:18: ( assignExpr | initList )
            int alt83=2;
            int LA83_0 = input.LA(1);

            if ( (LA83_0==CODE||(LA83_0>=SIZEOF && LA83_0<=TYPEOF)||LA83_0==ID||LA83_0==COLON||LA83_0==LPAREN||LA83_0==NIL||LA83_0==IF||LA83_0==NOT||(LA83_0>=TILDE && LA83_0<=AMP)||LA83_0==MINUS||(LA83_0>=PLUSPLUS && LA83_0<=STRING_LITERAL)||(LA83_0>=FALSE && LA83_0<=COLONS)) ) {
                alt83=1;
            }
            else if ( (LA83_0==LBRACKET) ) {
                alt83=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }
            switch (alt83) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:355:20: assignExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignExpr_in_assignOrInitExpr3435);
                    assignExpr242=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignExpr242.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:355:33: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_assignOrInitExpr3439);
                    initList243=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList243.getTree());

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
    // $ANTLR end "assignOrInitExpr"

    public static class assignExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:1: assignExpr : ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS248=null;
        EulangParser.lhs_return lhs244 = null;

        EulangParser.assignEqOp_return assignEqOp245 = null;

        EulangParser.assignExpr_return assignExpr246 = null;

        EulangParser.idTuple_return idTuple247 = null;

        EulangParser.assignExpr_return assignExpr249 = null;

        EulangParser.rhsExpr_return rhsExpr250 = null;


        CommonTree EQUALS248_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignEqOp=new RewriteRuleSubtreeStream(adaptor,"rule assignEqOp");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:12: ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr )
            int alt84=3;
            alt84 = dfa84.predict(input);
            switch (alt84) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:14: ( lhs assignEqOp )=> lhs assignEqOp assignExpr
                    {
                    pushFollow(FOLLOW_lhs_in_assignExpr3457);
                    lhs244=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs244.getTree());
                    pushFollow(FOLLOW_assignEqOp_in_assignExpr3459);
                    assignEqOp245=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp245.getTree());
                    pushFollow(FOLLOW_assignExpr_in_assignExpr3461);
                    assignExpr246=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr246.getTree());


                    // AST REWRITE
                    // elements: assignEqOp, lhs, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 358:67: -> ^( ASSIGN assignEqOp lhs assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:70: ^( ASSIGN assignEqOp lhs assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_assignEqOp.nextTree());
                        adaptor.addChild(root_1, stream_lhs.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:7: ( idTuple EQUALS )=> idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignExpr3496);
                    idTuple247=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple247.getTree());
                    EQUALS248=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr3498); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS248);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr3500);
                    assignExpr249=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr249.getTree());


                    // AST REWRITE
                    // elements: EQUALS, idTuple, assignExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 359:67: -> ^( ASSIGN EQUALS idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:70: ^( ASSIGN EQUALS idTuple assignExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_EQUALS.nextNode());
                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_assignExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:360:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr3534);
                    rhsExpr250=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr250.getTree());


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
                    // 360:43: -> rhsExpr
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

    public static class assignOp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignOp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:1: assignOp : ( PLUS_EQ | MINUS_EQ | STAR_EQ | SLASH_EQ | REM_EQ | UDIV_EQ | UREM_EQ | MOD_EQ | AND_EQ | OR_EQ | XOR_EQ | LSHIFT_EQ | RSHIFT_EQ | URSHIFT_EQ | CLSHIFT_EQ | CRSHIFT_EQ );
    public final EulangParser.assignOp_return assignOp() throws RecognitionException {
        EulangParser.assignOp_return retval = new EulangParser.assignOp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set251=null;

        CommonTree set251_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:10: ( PLUS_EQ | MINUS_EQ | STAR_EQ | SLASH_EQ | REM_EQ | UDIV_EQ | UREM_EQ | MOD_EQ | AND_EQ | OR_EQ | XOR_EQ | LSHIFT_EQ | RSHIFT_EQ | URSHIFT_EQ | CLSHIFT_EQ | CRSHIFT_EQ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set251=(Token)input.LT(1);
            if ( input.LA(1)==PLUS_EQ||(input.LA(1)>=MINUS_EQ && input.LA(1)<=CRSHIFT_EQ) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set251));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
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
    // $ANTLR end "assignOp"

    public static class assignEqOp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "assignEqOp"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:1: assignEqOp : ( EQUALS | assignOp );
    public final EulangParser.assignEqOp_return assignEqOp() throws RecognitionException {
        EulangParser.assignEqOp_return retval = new EulangParser.assignEqOp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS252=null;
        EulangParser.assignOp_return assignOp253 = null;


        CommonTree EQUALS252_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:12: ( EQUALS | assignOp )
            int alt85=2;
            int LA85_0 = input.LA(1);

            if ( (LA85_0==EQUALS) ) {
                alt85=1;
            }
            else if ( (LA85_0==PLUS_EQ||(LA85_0>=MINUS_EQ && LA85_0<=CRSHIFT_EQ)) ) {
                alt85=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 85, 0, input);

                throw nvae;
            }
            switch (alt85) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:14: EQUALS
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    EQUALS252=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignEqOp3649); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUALS252_tree = (CommonTree)adaptor.create(EQUALS252);
                    adaptor.addChild(root_0, EQUALS252_tree);
                    }

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:23: assignOp
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignOp_in_assignEqOp3653);
                    assignOp253=assignOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignOp253.getTree());

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
    // $ANTLR end "assignEqOp"

    public static class initList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "initList"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:1: initList : LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET -> ^( INITLIST ( initExpr )* ) ;
    public final EulangParser.initList_return initList() throws RecognitionException {
        EulangParser.initList_return retval = new EulangParser.initList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET254=null;
        Token COMMA256=null;
        Token RBRACKET258=null;
        EulangParser.initExpr_return initExpr255 = null;

        EulangParser.initExpr_return initExpr257 = null;


        CommonTree LBRACKET254_tree=null;
        CommonTree COMMA256_tree=null;
        CommonTree RBRACKET258_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_initExpr=new RewriteRuleSubtreeStream(adaptor,"rule initExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:10: ( LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET -> ^( INITLIST ( initExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:12: LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET
            {
            LBRACKET254=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_initList3662); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET254);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:21: ( initExpr ( COMMA initExpr )* )?
            int alt87=2;
            int LA87_0 = input.LA(1);

            if ( (LA87_0==CODE||(LA87_0>=SIZEOF && LA87_0<=TYPEOF)||LA87_0==ID||LA87_0==COLON||LA87_0==LPAREN||LA87_0==LBRACKET||LA87_0==NIL||LA87_0==PERIOD||LA87_0==IF||LA87_0==NOT||(LA87_0>=TILDE && LA87_0<=AMP)||LA87_0==MINUS||(LA87_0>=PLUSPLUS && LA87_0<=STRING_LITERAL)||(LA87_0>=FALSE && LA87_0<=COLONS)) ) {
                alt87=1;
            }
            switch (alt87) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:22: initExpr ( COMMA initExpr )*
                    {
                    pushFollow(FOLLOW_initExpr_in_initList3665);
                    initExpr255=initExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initExpr.add(initExpr255.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:31: ( COMMA initExpr )*
                    loop86:
                    do {
                        int alt86=2;
                        int LA86_0 = input.LA(1);

                        if ( (LA86_0==COMMA) ) {
                            alt86=1;
                        }


                        switch (alt86) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:32: COMMA initExpr
                    	    {
                    	    COMMA256=(Token)match(input,COMMA,FOLLOW_COMMA_in_initList3668); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA256);

                    	    pushFollow(FOLLOW_initExpr_in_initList3670);
                    	    initExpr257=initExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_initExpr.add(initExpr257.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop86;
                        }
                    } while (true);


                    }
                    break;

            }

            RBRACKET258=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_initList3676); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET258);



            // AST REWRITE
            // elements: initExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 367:64: -> ^( INITLIST ( initExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:67: ^( INITLIST ( initExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITLIST, "INITLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:367:78: ( initExpr )*
                while ( stream_initExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_initExpr.nextTree());

                }
                stream_initExpr.reset();

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
    // $ANTLR end "initList"

    public static class initExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "initExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:368:1: initExpr : ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList );
    public final EulangParser.initExpr_return initExpr() throws RecognitionException {
        EulangParser.initExpr_return retval = new EulangParser.initExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PERIOD259=null;
        Token ID260=null;
        Token EQUALS261=null;
        Token LBRACKET262=null;
        Token RBRACKET263=null;
        Token EQUALS264=null;
        EulangParser.rhsExpr_return e = null;

        EulangParser.initElement_return ei = null;

        EulangParser.rhsExpr_return i = null;

        EulangParser.initList_return initList265 = null;


        CommonTree PERIOD259_tree=null;
        CommonTree ID260_tree=null;
        CommonTree EQUALS261_tree=null;
        CommonTree LBRACKET262_tree=null;
        CommonTree RBRACKET263_tree=null;
        CommonTree EQUALS264_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_initElement=new RewriteRuleSubtreeStream(adaptor,"rule initElement");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:5: ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList )
            int alt88=4;
            alt88 = dfa88.predict(input);
            switch (alt88) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:7: ( rhsExpr )=>e= rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_initExpr3711);
                    e=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(e.getTree());


                    // AST REWRITE
                    // elements: e
                    // token labels: 
                    // rule labels: retval, e
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_e=new RewriteRuleSubtreeStream(adaptor,"rule e",e!=null?e.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 369:75: -> ^( INITEXPR $e)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:78: ^( INITEXPR $e)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITEXPR, "INITEXPR"), root_1);

                        adaptor.addChild(root_1, stream_e.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:370:7: PERIOD ID EQUALS ei= initElement
                    {
                    PERIOD259=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_initExpr3774); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD259);

                    ID260=(Token)match(input,ID,FOLLOW_ID_in_initExpr3776); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID260);

                    EQUALS261=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_initExpr3778); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS261);

                    pushFollow(FOLLOW_initElement_in_initExpr3782);
                    ei=initElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initElement.add(ei.getTree());


                    // AST REWRITE
                    // elements: ei, ID
                    // token labels: 
                    // rule labels: retval, ei
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ei=new RewriteRuleSubtreeStream(adaptor,"rule ei",ei!=null?ei.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 370:72: -> ^( INITEXPR $ei ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:370:75: ^( INITEXPR $ei ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITEXPR, "INITEXPR"), root_1);

                        adaptor.addChild(root_1, stream_ei.nextTree());
                        adaptor.addChild(root_1, stream_ID.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:7: ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement
                    {
                    LBRACKET262=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_initExpr3847); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET262);

                    pushFollow(FOLLOW_rhsExpr_in_initExpr3851);
                    i=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(i.getTree());
                    RBRACKET263=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_initExpr3853); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET263);

                    EQUALS264=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_initExpr3855); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS264);

                    pushFollow(FOLLOW_initElement_in_initExpr3859);
                    ei=initElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initElement.add(ei.getTree());


                    // AST REWRITE
                    // elements: i, ei
                    // token labels: 
                    // rule labels: retval, ei, i
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ei=new RewriteRuleSubtreeStream(adaptor,"rule ei",ei!=null?ei.tree:null);
                    RewriteRuleSubtreeStream stream_i=new RewriteRuleSubtreeStream(adaptor,"rule i",i!=null?i.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 371:107: -> ^( INITEXPR $ei $i)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:110: ^( INITEXPR $ei $i)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITEXPR, "INITEXPR"), root_1);

                        adaptor.addChild(root_1, stream_ei.nextTree());
                        adaptor.addChild(root_1, stream_i.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:372:7: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_initExpr3896);
                    initList265=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList265.getTree());

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
    // $ANTLR end "initExpr"

    public static class initElement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "initElement"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:1: initElement : ( rhsExpr | initList );
    public final EulangParser.initElement_return initElement() throws RecognitionException {
        EulangParser.initElement_return retval = new EulangParser.initElement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr266 = null;

        EulangParser.initList_return initList267 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:13: ( rhsExpr | initList )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0==CODE||(LA89_0>=SIZEOF && LA89_0<=TYPEOF)||LA89_0==ID||LA89_0==COLON||LA89_0==LPAREN||LA89_0==NIL||LA89_0==IF||LA89_0==NOT||(LA89_0>=TILDE && LA89_0<=AMP)||LA89_0==MINUS||(LA89_0>=PLUSPLUS && LA89_0<=STRING_LITERAL)||(LA89_0>=FALSE && LA89_0<=COLONS)) ) {
                alt89=1;
            }
            else if ( (LA89_0==LBRACKET) ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }
            switch (alt89) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:15: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_initElement3910);
                    rhsExpr266=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr266.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:375:25: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_initElement3914);
                    initList267=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList267.getTree());

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
    // $ANTLR end "initElement"

    public static class controlStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "controlStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:1: controlStmt : ( doWhile | whileDo | repeat | forIter );
    public final EulangParser.controlStmt_return controlStmt() throws RecognitionException {
        EulangParser.controlStmt_return retval = new EulangParser.controlStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.doWhile_return doWhile268 = null;

        EulangParser.whileDo_return whileDo269 = null;

        EulangParser.repeat_return repeat270 = null;

        EulangParser.forIter_return forIter271 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:13: ( doWhile | whileDo | repeat | forIter )
            int alt90=4;
            switch ( input.LA(1) ) {
            case DO:
                {
                alt90=1;
                }
                break;
            case WHILE:
                {
                alt90=2;
                }
                break;
            case REPEAT:
                {
                alt90=3;
                }
                break;
            case FOR:
                {
                alt90=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 90, 0, input);

                throw nvae;
            }

            switch (alt90) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:15: doWhile
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_doWhile_in_controlStmt3926);
                    doWhile268=doWhile();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, doWhile268.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:25: whileDo
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_whileDo_in_controlStmt3930);
                    whileDo269=whileDo();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, whileDo269.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:35: repeat
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_repeat_in_controlStmt3934);
                    repeat270=repeat();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, repeat270.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:377:44: forIter
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_forIter_in_controlStmt3938);
                    forIter271=forIter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, forIter271.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:1: doWhile : DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) ;
    public final EulangParser.doWhile_return doWhile() throws RecognitionException {
        EulangParser.doWhile_return retval = new EulangParser.doWhile_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DO272=null;
        Token WHILE274=null;
        EulangParser.codeStmtExpr_return codeStmtExpr273 = null;

        EulangParser.rhsExpr_return rhsExpr275 = null;


        CommonTree DO272_tree=null;
        CommonTree WHILE274_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:9: ( DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:11: DO codeStmtExpr WHILE rhsExpr
            {
            DO272=(Token)match(input,DO,FOLLOW_DO_in_doWhile3947); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO272);

            pushFollow(FOLLOW_codeStmtExpr_in_doWhile3949);
            codeStmtExpr273=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr273.getTree());
            WHILE274=(Token)match(input,WHILE,FOLLOW_WHILE_in_doWhile3951); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE274);

            pushFollow(FOLLOW_rhsExpr_in_doWhile3953);
            rhsExpr275=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr275.getTree());


            // AST REWRITE
            // elements: rhsExpr, DO, codeStmtExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 379:43: -> ^( DO codeStmtExpr rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:46: ^( DO codeStmtExpr rhsExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:1: whileDo : WHILE rhsExpr ( COLON | DO ) codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) ;
    public final EulangParser.whileDo_return whileDo() throws RecognitionException {
        EulangParser.whileDo_return retval = new EulangParser.whileDo_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHILE276=null;
        Token COLON278=null;
        Token DO279=null;
        EulangParser.rhsExpr_return rhsExpr277 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr280 = null;


        CommonTree WHILE276_tree=null;
        CommonTree COLON278_tree=null;
        CommonTree DO279_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:9: ( WHILE rhsExpr ( COLON | DO ) codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:11: WHILE rhsExpr ( COLON | DO ) codeStmtExpr
            {
            WHILE276=(Token)match(input,WHILE,FOLLOW_WHILE_in_whileDo3976); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE276);

            pushFollow(FOLLOW_rhsExpr_in_whileDo3978);
            rhsExpr277=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr277.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:25: ( COLON | DO )
            int alt91=2;
            int LA91_0 = input.LA(1);

            if ( (LA91_0==COLON) ) {
                alt91=1;
            }
            else if ( (LA91_0==DO) ) {
                alt91=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 91, 0, input);

                throw nvae;
            }
            switch (alt91) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:26: COLON
                    {
                    COLON278=(Token)match(input,COLON,FOLLOW_COLON_in_whileDo3981); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON278);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:32: DO
                    {
                    DO279=(Token)match(input,DO,FOLLOW_DO_in_whileDo3983); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO279);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmtExpr_in_whileDo3986);
            codeStmtExpr280=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr280.getTree());


            // AST REWRITE
            // elements: codeStmtExpr, WHILE, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 382:51: -> ^( WHILE rhsExpr codeStmtExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:54: ^( WHILE rhsExpr codeStmtExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:1: repeat : REPEAT rhsExpr ( COLON | DO ) codeStmt -> ^( REPEAT rhsExpr codeStmt ) ;
    public final EulangParser.repeat_return repeat() throws RecognitionException {
        EulangParser.repeat_return retval = new EulangParser.repeat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token REPEAT281=null;
        Token COLON283=null;
        Token DO284=null;
        EulangParser.rhsExpr_return rhsExpr282 = null;

        EulangParser.codeStmt_return codeStmt285 = null;


        CommonTree REPEAT281_tree=null;
        CommonTree COLON283_tree=null;
        CommonTree DO284_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_REPEAT=new RewriteRuleTokenStream(adaptor,"token REPEAT");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:8: ( REPEAT rhsExpr ( COLON | DO ) codeStmt -> ^( REPEAT rhsExpr codeStmt ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:10: REPEAT rhsExpr ( COLON | DO ) codeStmt
            {
            REPEAT281=(Token)match(input,REPEAT,FOLLOW_REPEAT_in_repeat4011); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_REPEAT.add(REPEAT281);

            pushFollow(FOLLOW_rhsExpr_in_repeat4013);
            rhsExpr282=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr282.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:25: ( COLON | DO )
            int alt92=2;
            int LA92_0 = input.LA(1);

            if ( (LA92_0==COLON) ) {
                alt92=1;
            }
            else if ( (LA92_0==DO) ) {
                alt92=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 92, 0, input);

                throw nvae;
            }
            switch (alt92) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:26: COLON
                    {
                    COLON283=(Token)match(input,COLON,FOLLOW_COLON_in_repeat4016); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON283);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:32: DO
                    {
                    DO284=(Token)match(input,DO,FOLLOW_DO_in_repeat4018); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO284);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmt_in_repeat4021);
            codeStmt285=codeStmt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt285.getTree());


            // AST REWRITE
            // elements: rhsExpr, REPEAT, codeStmt
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 385:53: -> ^( REPEAT rhsExpr codeStmt )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:56: ^( REPEAT rhsExpr codeStmt )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_REPEAT.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeStmt.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:1: forIter : FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt ) ;
    public final EulangParser.forIter_return forIter() throws RecognitionException {
        EulangParser.forIter_return retval = new EulangParser.forIter_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR286=null;
        Token IN289=null;
        Token COLON291=null;
        Token DO292=null;
        EulangParser.forIds_return forIds287 = null;

        EulangParser.forMovement_return forMovement288 = null;

        EulangParser.rhsExpr_return rhsExpr290 = null;

        EulangParser.codeStmt_return codeStmt293 = null;


        CommonTree FOR286_tree=null;
        CommonTree IN289_tree=null;
        CommonTree COLON291_tree=null;
        CommonTree DO292_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_forMovement=new RewriteRuleSubtreeStream(adaptor,"rule forMovement");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        RewriteRuleSubtreeStream stream_forIds=new RewriteRuleSubtreeStream(adaptor,"rule forIds");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:9: ( FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:11: FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt
            {
            FOR286=(Token)match(input,FOR,FOLLOW_FOR_in_forIter4051); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR286);

            pushFollow(FOLLOW_forIds_in_forIter4053);
            forIds287=forIds();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_forIds.add(forIds287.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:22: ( forMovement )?
            int alt93=2;
            int LA93_0 = input.LA(1);

            if ( ((LA93_0>=BY && LA93_0<=AT)) ) {
                alt93=1;
            }
            switch (alt93) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:22: forMovement
                    {
                    pushFollow(FOLLOW_forMovement_in_forIter4055);
                    forMovement288=forMovement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_forMovement.add(forMovement288.getTree());

                    }
                    break;

            }

            IN289=(Token)match(input,IN,FOLLOW_IN_in_forIter4058); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN289);

            pushFollow(FOLLOW_rhsExpr_in_forIter4060);
            rhsExpr290=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr290.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:46: ( COLON | DO )
            int alt94=2;
            int LA94_0 = input.LA(1);

            if ( (LA94_0==COLON) ) {
                alt94=1;
            }
            else if ( (LA94_0==DO) ) {
                alt94=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 94, 0, input);

                throw nvae;
            }
            switch (alt94) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:47: COLON
                    {
                    COLON291=(Token)match(input,COLON,FOLLOW_COLON_in_forIter4063); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON291);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:53: DO
                    {
                    DO292=(Token)match(input,DO,FOLLOW_DO_in_forIter4065); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO292);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmt_in_forIter4068);
            codeStmt293=codeStmt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt293.getTree());


            // AST REWRITE
            // elements: rhsExpr, forIds, FOR, forMovement, codeStmt
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 388:72: -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:75: ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:81: ^( LIST forIds )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                adaptor.addChild(root_2, stream_forIds.nextTree());

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:388:96: ( forMovement )?
                if ( stream_forMovement.hasNext() ) {
                    adaptor.addChild(root_1, stream_forMovement.nextTree());

                }
                stream_forMovement.reset();
                adaptor.addChild(root_1, stream_rhsExpr.nextTree());
                adaptor.addChild(root_1, stream_codeStmt.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:1: forIds : ID ( AND ID )* -> ( ID )+ ;
    public final EulangParser.forIds_return forIds() throws RecognitionException {
        EulangParser.forIds_return retval = new EulangParser.forIds_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID294=null;
        Token AND295=null;
        Token ID296=null;

        CommonTree ID294_tree=null;
        CommonTree AND295_tree=null;
        CommonTree ID296_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:8: ( ID ( AND ID )* -> ( ID )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:10: ID ( AND ID )*
            {
            ID294=(Token)match(input,ID,FOLLOW_ID_in_forIds4105); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID294);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:13: ( AND ID )*
            loop95:
            do {
                int alt95=2;
                int LA95_0 = input.LA(1);

                if ( (LA95_0==AND) ) {
                    alt95=1;
                }


                switch (alt95) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:14: AND ID
            	    {
            	    AND295=(Token)match(input,AND,FOLLOW_AND_in_forIds4108); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND295);

            	    ID296=(Token)match(input,ID,FOLLOW_ID_in_forIds4110); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID296);


            	    }
            	    break;

            	default :
            	    break loop95;
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
            // 391:23: -> ( ID )+
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

    public static class forMovement_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forMovement"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:1: forMovement : ( atId | stepping );
    public final EulangParser.forMovement_return forMovement() throws RecognitionException {
        EulangParser.forMovement_return retval = new EulangParser.forMovement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.atId_return atId297 = null;

        EulangParser.stepping_return stepping298 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:13: ( atId | stepping )
            int alt96=2;
            int LA96_0 = input.LA(1);

            if ( (LA96_0==AT) ) {
                alt96=1;
            }
            else if ( (LA96_0==BY) ) {
                alt96=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 96, 0, input);

                throw nvae;
            }
            switch (alt96) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:15: atId
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_atId_in_forMovement4126);
                    atId297=atId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, atId297.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:393:22: stepping
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_stepping_in_forMovement4130);
                    stepping298=stepping();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, stepping298.getTree());

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
    // $ANTLR end "forMovement"

    public static class stepping_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stepping"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:1: stepping : BY rhsExpr -> ^( BY rhsExpr ) ;
    public final EulangParser.stepping_return stepping() throws RecognitionException {
        EulangParser.stepping_return retval = new EulangParser.stepping_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BY299=null;
        EulangParser.rhsExpr_return rhsExpr300 = null;


        CommonTree BY299_tree=null;
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:10: ( BY rhsExpr -> ^( BY rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:12: BY rhsExpr
            {
            BY299=(Token)match(input,BY,FOLLOW_BY_in_stepping4139); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BY.add(BY299);

            pushFollow(FOLLOW_rhsExpr_in_stepping4141);
            rhsExpr300=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr300.getTree());


            // AST REWRITE
            // elements: BY, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 395:23: -> ^( BY rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:26: ^( BY rhsExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_BY.nextNode(), root_1);

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
    // $ANTLR end "stepping"

    public static class atId_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atId"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:397:1: atId : AT ID -> ^( AT ID ) ;
    public final EulangParser.atId_return atId() throws RecognitionException {
        EulangParser.atId_return retval = new EulangParser.atId_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT301=null;
        Token ID302=null;

        CommonTree AT301_tree=null;
        CommonTree ID302_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:397:6: ( AT ID -> ^( AT ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:397:8: AT ID
            {
            AT301=(Token)match(input,AT,FOLLOW_AT_in_atId4158); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT301);

            ID302=(Token)match(input,ID,FOLLOW_ID_in_atId4160); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID302);



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
            // 397:17: -> ^( AT ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:397:20: ^( AT ID )
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

    public static class breakStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "breakStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:401:1: breakStmt : BREAK rhsExpr -> ^( BREAK rhsExpr ) ;
    public final EulangParser.breakStmt_return breakStmt() throws RecognitionException {
        EulangParser.breakStmt_return retval = new EulangParser.breakStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BREAK303=null;
        EulangParser.rhsExpr_return rhsExpr304 = null;


        CommonTree BREAK303_tree=null;
        RewriteRuleTokenStream stream_BREAK=new RewriteRuleTokenStream(adaptor,"token BREAK");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:401:11: ( BREAK rhsExpr -> ^( BREAK rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:401:13: BREAK rhsExpr
            {
            BREAK303=(Token)match(input,BREAK,FOLLOW_BREAK_in_breakStmt4188); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BREAK.add(BREAK303);

            pushFollow(FOLLOW_rhsExpr_in_breakStmt4190);
            rhsExpr304=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr304.getTree());


            // AST REWRITE
            // elements: rhsExpr, BREAK
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 401:27: -> ^( BREAK rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:401:31: ^( BREAK rhsExpr )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_BREAK.nextNode(), root_1);

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
    // $ANTLR end "breakStmt"

    public static class labelStmt_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "labelStmt"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:409:1: labelStmt : ATSIGN ID COLON -> ^( LABEL ID ) ;
    public final EulangParser.labelStmt_return labelStmt() throws RecognitionException {
        EulangParser.labelStmt_return retval = new EulangParser.labelStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATSIGN305=null;
        Token ID306=null;
        Token COLON307=null;

        CommonTree ATSIGN305_tree=null;
        CommonTree ID306_tree=null;
        CommonTree COLON307_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ATSIGN=new RewriteRuleTokenStream(adaptor,"token ATSIGN");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:409:10: ( ATSIGN ID COLON -> ^( LABEL ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:409:12: ATSIGN ID COLON
            {
            ATSIGN305=(Token)match(input,ATSIGN,FOLLOW_ATSIGN_in_labelStmt4218); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ATSIGN.add(ATSIGN305);

            ID306=(Token)match(input,ID,FOLLOW_ID_in_labelStmt4220); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID306);

            COLON307=(Token)match(input,COLON,FOLLOW_COLON_in_labelStmt4222); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON307);



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
            // 409:47: -> ^( LABEL ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:409:50: ^( LABEL ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:1: gotoStmt : GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) ;
    public final EulangParser.gotoStmt_return gotoStmt() throws RecognitionException {
        EulangParser.gotoStmt_return retval = new EulangParser.gotoStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token GOTO308=null;
        Token IF310=null;
        EulangParser.idOrScopeRef_return idOrScopeRef309 = null;

        EulangParser.rhsExpr_return rhsExpr311 = null;


        CommonTree GOTO308_tree=null;
        CommonTree IF310_tree=null;
        RewriteRuleTokenStream stream_GOTO=new RewriteRuleTokenStream(adaptor,"token GOTO");
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:9: ( GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:11: GOTO idOrScopeRef ( IF rhsExpr )?
            {
            GOTO308=(Token)match(input,GOTO,FOLLOW_GOTO_in_gotoStmt4258); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GOTO.add(GOTO308);

            pushFollow(FOLLOW_idOrScopeRef_in_gotoStmt4260);
            idOrScopeRef309=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef309.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:29: ( IF rhsExpr )?
            int alt97=2;
            int LA97_0 = input.LA(1);

            if ( (LA97_0==IF) ) {
                alt97=1;
            }
            switch (alt97) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:30: IF rhsExpr
                    {
                    IF310=(Token)match(input,IF,FOLLOW_IF_in_gotoStmt4263); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF310);

                    pushFollow(FOLLOW_rhsExpr_in_gotoStmt4265);
                    rhsExpr311=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr311.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: rhsExpr, GOTO, idOrScopeRef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 411:53: -> ^( GOTO idOrScopeRef ( rhsExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:56: ^( GOTO idOrScopeRef ( rhsExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_GOTO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:76: ( rhsExpr )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:1: blockStmt : LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) ;
    public final EulangParser.blockStmt_return blockStmt() throws RecognitionException {
        EulangParser.blockStmt_return retval = new EulangParser.blockStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE312=null;
        Token RBRACE314=null;
        EulangParser.codestmtlist_return codestmtlist313 = null;


        CommonTree LBRACE312_tree=null;
        CommonTree RBRACE314_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:10: ( LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:12: LBRACE codestmtlist RBRACE
            {
            LBRACE312=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_blockStmt4300); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE312);

            pushFollow(FOLLOW_codestmtlist_in_blockStmt4302);
            codestmtlist313=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist313.getTree());
            RBRACE314=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_blockStmt4304); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE314);



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
            // 414:43: -> ^( BLOCK codestmtlist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:46: ^( BLOCK codestmtlist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:1: tuple : LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) ;
    public final EulangParser.tuple_return tuple() throws RecognitionException {
        EulangParser.tuple_return retval = new EulangParser.tuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN315=null;
        Token RPAREN317=null;
        EulangParser.tupleEntries_return tupleEntries316 = null;


        CommonTree LPAREN315_tree=null;
        CommonTree RPAREN317_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule tupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:7: ( LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:9: LPAREN tupleEntries RPAREN
            {
            LPAREN315=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_tuple4327); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN315);

            pushFollow(FOLLOW_tupleEntries_in_tuple4329);
            tupleEntries316=tupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleEntries.add(tupleEntries316.getTree());
            RPAREN317=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_tuple4331); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN317);



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
            // 417:41: -> ^( TUPLE ( tupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:44: ^( TUPLE ( tupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:1: tupleEntries : assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ ;
    public final EulangParser.tupleEntries_return tupleEntries() throws RecognitionException {
        EulangParser.tupleEntries_return retval = new EulangParser.tupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA319=null;
        EulangParser.assignExpr_return assignExpr318 = null;

        EulangParser.assignExpr_return assignExpr320 = null;


        CommonTree COMMA319_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:14: ( assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:16: assignExpr ( COMMA assignExpr )+
            {
            pushFollow(FOLLOW_assignExpr_in_tupleEntries4359);
            assignExpr318=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr318.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:27: ( COMMA assignExpr )+
            int cnt98=0;
            loop98:
            do {
                int alt98=2;
                int LA98_0 = input.LA(1);

                if ( (LA98_0==COMMA) ) {
                    alt98=1;
                }


                switch (alt98) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:28: COMMA assignExpr
            	    {
            	    COMMA319=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleEntries4362); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA319);

            	    pushFollow(FOLLOW_assignExpr_in_tupleEntries4364);
            	    assignExpr320=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr320.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt98 >= 1 ) break loop98;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(98, input);
                        throw eee;
                }
                cnt98++;
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
            // 420:48: -> ( assignExpr )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:1: idTuple : LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) ;
    public final EulangParser.idTuple_return idTuple() throws RecognitionException {
        EulangParser.idTuple_return retval = new EulangParser.idTuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN321=null;
        Token RPAREN323=null;
        EulangParser.idTupleEntries_return idTupleEntries322 = null;


        CommonTree LPAREN321_tree=null;
        CommonTree RPAREN323_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_idTupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule idTupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:9: ( LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:11: LPAREN idTupleEntries RPAREN
            {
            LPAREN321=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_idTuple4383); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN321);

            pushFollow(FOLLOW_idTupleEntries_in_idTuple4385);
            idTupleEntries322=idTupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTupleEntries.add(idTupleEntries322.getTree());
            RPAREN323=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_idTuple4387); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN323);



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
            // 423:45: -> ^( TUPLE ( idTupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:48: ^( TUPLE ( idTupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:1: idTupleEntries : idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ ;
    public final EulangParser.idTupleEntries_return idTupleEntries() throws RecognitionException {
        EulangParser.idTupleEntries_return retval = new EulangParser.idTupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA325=null;
        EulangParser.idOrScopeRef_return idOrScopeRef324 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef326 = null;


        CommonTree COMMA325_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:16: ( idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:18: idOrScopeRef ( COMMA idOrScopeRef )+
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries4415);
            idOrScopeRef324=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef324.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:31: ( COMMA idOrScopeRef )+
            int cnt99=0;
            loop99:
            do {
                int alt99=2;
                int LA99_0 = input.LA(1);

                if ( (LA99_0==COMMA) ) {
                    alt99=1;
                }


                switch (alt99) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:32: COMMA idOrScopeRef
            	    {
            	    COMMA325=(Token)match(input,COMMA,FOLLOW_COMMA_in_idTupleEntries4418); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA325);

            	    pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries4420);
            	    idOrScopeRef326=idOrScopeRef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef326.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt99 >= 1 ) break loop99;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(99, input);
                        throw eee;
                }
                cnt99++;
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
            // 426:54: -> ( idOrScopeRef )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:1: rhsExpr : condStar -> condStar ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.condStar_return condStar327 = null;


        RewriteRuleSubtreeStream stream_condStar=new RewriteRuleSubtreeStream(adaptor,"rule condStar");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:9: ( condStar -> condStar )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:13: condStar
            {
            pushFollow(FOLLOW_condStar_in_rhsExpr4441);
            condStar327=condStar();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStar.add(condStar327.getTree());


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
            // 429:22: -> condStar
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

    public static class arglist_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arglist"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA329=null;
        Token COMMA331=null;
        EulangParser.arg_return arg328 = null;

        EulangParser.arg_return arg330 = null;


        CommonTree COMMA329_tree=null;
        CommonTree COMMA331_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt102=2;
            int LA102_0 = input.LA(1);

            if ( (LA102_0==CODE||LA102_0==GOTO||(LA102_0>=SIZEOF && LA102_0<=TYPEOF)||LA102_0==ID||LA102_0==COLON||(LA102_0>=LPAREN && LA102_0<=LBRACE)||LA102_0==NIL||LA102_0==IF||LA102_0==NOT||(LA102_0>=TILDE && LA102_0<=AMP)||LA102_0==MINUS||(LA102_0>=PLUSPLUS && LA102_0<=STRING_LITERAL)||(LA102_0>=FALSE && LA102_0<=COLONS)) ) {
                alt102=1;
            }
            switch (alt102) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist4462);
                    arg328=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg328.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:15: ( COMMA arg )*
                    loop100:
                    do {
                        int alt100=2;
                        int LA100_0 = input.LA(1);

                        if ( (LA100_0==COMMA) ) {
                            int LA100_1 = input.LA(2);

                            if ( (LA100_1==CODE||LA100_1==GOTO||(LA100_1>=SIZEOF && LA100_1<=TYPEOF)||LA100_1==ID||LA100_1==COLON||(LA100_1>=LPAREN && LA100_1<=LBRACE)||LA100_1==NIL||LA100_1==IF||LA100_1==NOT||(LA100_1>=TILDE && LA100_1<=AMP)||LA100_1==MINUS||(LA100_1>=PLUSPLUS && LA100_1<=STRING_LITERAL)||(LA100_1>=FALSE && LA100_1<=COLONS)) ) {
                                alt100=1;
                            }


                        }


                        switch (alt100) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:17: COMMA arg
                    	    {
                    	    COMMA329=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist4466); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA329);

                    	    pushFollow(FOLLOW_arg_in_arglist4468);
                    	    arg330=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg330.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop100;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:29: ( COMMA )?
                    int alt101=2;
                    int LA101_0 = input.LA(1);

                    if ( (LA101_0==COMMA) ) {
                        alt101=1;
                    }
                    switch (alt101) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:29: COMMA
                            {
                            COMMA331=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist4472); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA331);


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
            // 432:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:432:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:435:1: arg : ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) );
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE333=null;
        Token RBRACE335=null;
        EulangParser.assignExpr_return assignExpr332 = null;

        EulangParser.codestmtlist_return codestmtlist334 = null;

        EulangParser.gotoStmt_return gotoStmt336 = null;


        CommonTree LBRACE333_tree=null;
        CommonTree RBRACE335_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:435:4: ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) )
            int alt103=3;
            switch ( input.LA(1) ) {
            case CODE:
            case SIZEOF:
            case TYPEOF:
            case ID:
            case COLON:
            case LPAREN:
            case NIL:
            case IF:
            case NOT:
            case TILDE:
            case AMP:
            case MINUS:
            case PLUSPLUS:
            case MINUSMINUS:
            case NUMBER:
            case CHAR_LITERAL:
            case STRING_LITERAL:
            case FALSE:
            case TRUE:
            case COLONS:
                {
                alt103=1;
                }
                break;
            case LBRACE:
                {
                alt103=2;
                }
                break;
            case GOTO:
                {
                alt103=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                throw nvae;
            }

            switch (alt103) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:435:7: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_arg4521);
                    assignExpr332=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr332.getTree());


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
                    // 435:37: -> ^( EXPR assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:435:40: ^( EXPR assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:436:5: LBRACE codestmtlist RBRACE
                    {
                    LBRACE333=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_arg4554); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE333);

                    pushFollow(FOLLOW_codestmtlist_in_arg4556);
                    codestmtlist334=codestmtlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist334.getTree());
                    RBRACE335=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_arg4558); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE335);



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
                    // 436:37: -> ^( EXPR ^( CODE codestmtlist ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:436:40: ^( EXPR ^( CODE codestmtlist ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:436:47: ^( CODE codestmtlist )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:437:5: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_arg4582);
                    gotoStmt336=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt336.getTree());


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
                    // 437:37: -> ^( EXPR gotoStmt )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:437:40: ^( EXPR gotoStmt )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:458:1: condStar : ( cond -> cond | IF ifExprs -> ifExprs );
    public final EulangParser.condStar_return condStar() throws RecognitionException {
        EulangParser.condStar_return retval = new EulangParser.condStar_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IF338=null;
        EulangParser.cond_return cond337 = null;

        EulangParser.ifExprs_return ifExprs339 = null;


        CommonTree IF338_tree=null;
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_ifExprs=new RewriteRuleSubtreeStream(adaptor,"rule ifExprs");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:458:9: ( cond -> cond | IF ifExprs -> ifExprs )
            int alt104=2;
            int LA104_0 = input.LA(1);

            if ( (LA104_0==CODE||(LA104_0>=SIZEOF && LA104_0<=TYPEOF)||LA104_0==ID||LA104_0==COLON||LA104_0==LPAREN||LA104_0==NIL||LA104_0==NOT||(LA104_0>=TILDE && LA104_0<=AMP)||LA104_0==MINUS||(LA104_0>=PLUSPLUS && LA104_0<=STRING_LITERAL)||(LA104_0>=FALSE && LA104_0<=COLONS)) ) {
                alt104=1;
            }
            else if ( (LA104_0==IF) ) {
                alt104=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 104, 0, input);

                throw nvae;
            }
            switch (alt104) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:458:11: cond
                    {
                    pushFollow(FOLLOW_cond_in_condStar4643);
                    cond337=cond();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cond.add(cond337.getTree());


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
                    // 458:16: -> cond
                    {
                        adaptor.addChild(root_0, stream_cond.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:6: IF ifExprs
                    {
                    IF338=(Token)match(input,IF,FOLLOW_IF_in_condStar4654); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF338);

                    pushFollow(FOLLOW_ifExprs_in_condStar4656);
                    ifExprs339=ifExprs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ifExprs.add(ifExprs339.getTree());


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
                    // 459:17: -> ifExprs
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:465:1: ifExprs : thenClause elses -> ^( CONDLIST thenClause elses ) ;
    public final EulangParser.ifExprs_return ifExprs() throws RecognitionException {
        EulangParser.ifExprs_return retval = new EulangParser.ifExprs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.thenClause_return thenClause340 = null;

        EulangParser.elses_return elses341 = null;


        RewriteRuleSubtreeStream stream_thenClause=new RewriteRuleSubtreeStream(adaptor,"rule thenClause");
        RewriteRuleSubtreeStream stream_elses=new RewriteRuleSubtreeStream(adaptor,"rule elses");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:465:9: ( thenClause elses -> ^( CONDLIST thenClause elses ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:465:11: thenClause elses
            {
            pushFollow(FOLLOW_thenClause_in_ifExprs4675);
            thenClause340=thenClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_thenClause.add(thenClause340.getTree());
            pushFollow(FOLLOW_elses_in_ifExprs4677);
            elses341=elses();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elses.add(elses341.getTree());


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
            // 465:28: -> ^( CONDLIST thenClause elses )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:465:31: ^( CONDLIST thenClause elses )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:467:1: thenClause : t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) ;
    public final EulangParser.thenClause_return thenClause() throws RecognitionException {
        EulangParser.thenClause_return retval = new EulangParser.thenClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token THEN342=null;
        Token COLON343=null;
        EulangParser.assignExpr_return t = null;

        EulangParser.condStmtExpr_return v = null;


        CommonTree THEN342_tree=null;
        CommonTree COLON343_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:467:12: (t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:467:14: t= assignExpr ( THEN | COLON ) v= condStmtExpr
            {
            pushFollow(FOLLOW_assignExpr_in_thenClause4699);
            t=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(t.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:3: ( THEN | COLON )
            int alt105=2;
            int LA105_0 = input.LA(1);

            if ( (LA105_0==THEN) ) {
                alt105=1;
            }
            else if ( (LA105_0==COLON) ) {
                alt105=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 105, 0, input);

                throw nvae;
            }
            switch (alt105) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:4: THEN
                    {
                    THEN342=(Token)match(input,THEN,FOLLOW_THEN_in_thenClause4705); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN342);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:9: COLON
                    {
                    COLON343=(Token)match(input,COLON,FOLLOW_COLON_in_thenClause4707); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON343);


                    }
                    break;

            }

            pushFollow(FOLLOW_condStmtExpr_in_thenClause4712);
            v=condStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStmtExpr.add(v.getTree());


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
            // 468:33: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:36: ^( CONDTEST $t $v)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:470:1: elses : ( elif )* elseClause -> ( elif )* elseClause ;
    public final EulangParser.elses_return elses() throws RecognitionException {
        EulangParser.elses_return retval = new EulangParser.elses_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.elif_return elif344 = null;

        EulangParser.elseClause_return elseClause345 = null;


        RewriteRuleSubtreeStream stream_elseClause=new RewriteRuleSubtreeStream(adaptor,"rule elseClause");
        RewriteRuleSubtreeStream stream_elif=new RewriteRuleSubtreeStream(adaptor,"rule elif");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:470:7: ( ( elif )* elseClause -> ( elif )* elseClause )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:470:9: ( elif )* elseClause
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:470:9: ( elif )*
            loop106:
            do {
                int alt106=2;
                int LA106_0 = input.LA(1);

                if ( (LA106_0==ELIF) ) {
                    alt106=1;
                }


                switch (alt106) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:470:9: elif
            	    {
            	    pushFollow(FOLLOW_elif_in_elses4740);
            	    elif344=elif();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_elif.add(elif344.getTree());

            	    }
            	    break;

            	default :
            	    break loop106;
                }
            } while (true);

            pushFollow(FOLLOW_elseClause_in_elses4743);
            elseClause345=elseClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elseClause.add(elseClause345.getTree());


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
            // 470:29: -> ( elif )* elseClause
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:470:32: ( elif )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:1: elif : ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) ;
    public final EulangParser.elif_return elif() throws RecognitionException {
        EulangParser.elif_return retval = new EulangParser.elif_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELIF346=null;
        Token THEN347=null;
        Token COLON348=null;
        EulangParser.assignExpr_return t = null;

        EulangParser.condStmtExpr_return v = null;


        CommonTree ELIF346_tree=null;
        CommonTree THEN347_tree=null;
        CommonTree COLON348_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ELIF=new RewriteRuleTokenStream(adaptor,"token ELIF");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:6: ( ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:8: ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr
            {
            ELIF346=(Token)match(input,ELIF,FOLLOW_ELIF_in_elif4766); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ELIF.add(ELIF346);

            pushFollow(FOLLOW_assignExpr_in_elif4770);
            t=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(t.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:26: ( THEN | COLON )
            int alt107=2;
            int LA107_0 = input.LA(1);

            if ( (LA107_0==THEN) ) {
                alt107=1;
            }
            else if ( (LA107_0==COLON) ) {
                alt107=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 107, 0, input);

                throw nvae;
            }
            switch (alt107) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:27: THEN
                    {
                    THEN347=(Token)match(input,THEN,FOLLOW_THEN_in_elif4773); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN347);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:32: COLON
                    {
                    COLON348=(Token)match(input,COLON,FOLLOW_COLON_in_elif4775); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON348);


                    }
                    break;

            }

            pushFollow(FOLLOW_condStmtExpr_in_elif4780);
            v=condStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStmtExpr.add(v.getTree());


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
            // 472:55: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:58: ^( CONDTEST $t $v)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:1: elseClause : ( ELSE condStmtExpr -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) );
    public final EulangParser.elseClause_return elseClause() throws RecognitionException {
        EulangParser.elseClause_return retval = new EulangParser.elseClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELSE349=null;
        Token FI351=null;
        EulangParser.condStmtExpr_return condStmtExpr350 = null;


        CommonTree ELSE349_tree=null;
        CommonTree FI351_tree=null;
        RewriteRuleTokenStream stream_FI=new RewriteRuleTokenStream(adaptor,"token FI");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:12: ( ELSE condStmtExpr -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) )
            int alt108=2;
            int LA108_0 = input.LA(1);

            if ( (LA108_0==ELSE) ) {
                alt108=1;
            }
            else if ( (LA108_0==FI) ) {
                alt108=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 108, 0, input);

                throw nvae;
            }
            switch (alt108) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:14: ELSE condStmtExpr
                    {
                    ELSE349=(Token)match(input,ELSE,FOLLOW_ELSE_in_elseClause4806); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE349);

                    pushFollow(FOLLOW_condStmtExpr_in_elseClause4808);
                    condStmtExpr350=condStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_condStmtExpr.add(condStmtExpr350.getTree());


                    // AST REWRITE
                    // elements: condStmtExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 474:38: -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:41: ^( CONDTEST ^( LIT TRUE ) condStmtExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:52: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        adaptor.addChild(root_1, stream_condStmtExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:6: FI
                    {
                    FI351=(Token)match(input,FI,FOLLOW_FI_in_elseClause4835); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FI.add(FI351);



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
                    // 475:9: -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:12: ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:23: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:35: ^( LIT NIL )
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

    public static class condStmtExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condStmtExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:1: condStmtExpr : ( arg | breakStmt );
    public final EulangParser.condStmtExpr_return condStmtExpr() throws RecognitionException {
        EulangParser.condStmtExpr_return retval = new EulangParser.condStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.arg_return arg352 = null;

        EulangParser.breakStmt_return breakStmt353 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:14: ( arg | breakStmt )
            int alt109=2;
            int LA109_0 = input.LA(1);

            if ( (LA109_0==CODE||LA109_0==GOTO||(LA109_0>=SIZEOF && LA109_0<=TYPEOF)||LA109_0==ID||LA109_0==COLON||(LA109_0>=LPAREN && LA109_0<=LBRACE)||LA109_0==NIL||LA109_0==IF||LA109_0==NOT||(LA109_0>=TILDE && LA109_0<=AMP)||LA109_0==MINUS||(LA109_0>=PLUSPLUS && LA109_0<=STRING_LITERAL)||(LA109_0>=FALSE && LA109_0<=COLONS)) ) {
                alt109=1;
            }
            else if ( (LA109_0==BREAK) ) {
                alt109=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 109, 0, input);

                throw nvae;
            }
            switch (alt109) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:16: arg
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_arg_in_condStmtExpr4866);
                    arg352=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arg352.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:22: breakStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_breakStmt_in_condStmtExpr4870);
                    breakStmt353=breakStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, breakStmt353.getTree());

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
    // $ANTLR end "condStmtExpr"

    public static class cond_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cond"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:480:1: cond : ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION355=null;
        Token COLON356=null;
        EulangParser.logor_return t = null;

        EulangParser.logor_return f = null;

        EulangParser.logor_return logor354 = null;


        CommonTree QUESTION355_tree=null;
        CommonTree COLON356_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logor=new RewriteRuleSubtreeStream(adaptor,"rule logor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:480:5: ( ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:480:10: ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:480:10: ( logor -> logor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:480:12: logor
            {
            pushFollow(FOLLOW_logor_in_cond4887);
            logor354=logor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logor.add(logor354.getTree());


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
            // 480:19: -> logor
            {
                adaptor.addChild(root_0, stream_logor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:7: ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )*
            loop110:
            do {
                int alt110=2;
                int LA110_0 = input.LA(1);

                if ( (LA110_0==QUESTION) ) {
                    alt110=1;
                }


                switch (alt110) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:9: QUESTION t= logor COLON f= logor
            	    {
            	    QUESTION355=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond4904); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION355);

            	    pushFollow(FOLLOW_logor_in_cond4908);
            	    t=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(t.getTree());
            	    COLON356=(Token)match(input,COLON,FOLLOW_COLON_in_cond4910); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON356);

            	    pushFollow(FOLLOW_logor_in_cond4914);
            	    f=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(f.getTree());


            	    // AST REWRITE
            	    // elements: t, f, cond
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
            	    // 481:40: -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:43: ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDLIST, "CONDLIST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:54: ^( CONDTEST $cond $t)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());
            	        adaptor.addChild(root_2, stream_t.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:75: ^( CONDTEST ^( LIT TRUE ) $f)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_2);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:86: ^( LIT TRUE )
            	        {
            	        CommonTree root_3 = (CommonTree)adaptor.nil();
            	        root_3 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_3);

            	        adaptor.addChild(root_3, (CommonTree)adaptor.create(TRUE, "TRUE"));

            	        adaptor.addChild(root_2, root_3);
            	        }
            	        adaptor.addChild(root_2, stream_f.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop110;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:484:1: logor : ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* ;
    public final EulangParser.logor_return logor() throws RecognitionException {
        EulangParser.logor_return retval = new EulangParser.logor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token OR358=null;
        EulangParser.logand_return r = null;

        EulangParser.logand_return logand357 = null;


        CommonTree OR358_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_logand=new RewriteRuleSubtreeStream(adaptor,"rule logand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:484:7: ( ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:484:9: ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:484:9: ( logand -> logand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:484:11: logand
            {
            pushFollow(FOLLOW_logand_in_logor4958);
            logand357=logand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logand.add(logand357.getTree());


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
            // 484:19: -> logand
            {
                adaptor.addChild(root_0, stream_logand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:7: ( OR r= logand -> ^( OR $logor $r) )*
            loop111:
            do {
                int alt111=2;
                int LA111_0 = input.LA(1);

                if ( (LA111_0==OR) ) {
                    alt111=1;
                }


                switch (alt111) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:9: OR r= logand
            	    {
            	    OR358=(Token)match(input,OR,FOLLOW_OR_in_logor4975); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR358);

            	    pushFollow(FOLLOW_logand_in_logor4979);
            	    r=logand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: logor, r, OR
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
            	    // 485:21: -> ^( OR $logor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:24: ^( OR $logor $r)
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
            	    break loop111;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:487:1: logand : ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* ;
    public final EulangParser.logand_return logand() throws RecognitionException {
        EulangParser.logand_return retval = new EulangParser.logand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND360=null;
        EulangParser.not_return r = null;

        EulangParser.not_return not359 = null;


        CommonTree AND360_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:487:8: ( ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:487:10: ( not -> not ) ( AND r= not -> ^( AND $logand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:487:10: ( not -> not )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:487:12: not
            {
            pushFollow(FOLLOW_not_in_logand5010);
            not359=not();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_not.add(not359.getTree());


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
            // 487:16: -> not
            {
                adaptor.addChild(root_0, stream_not.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:488:7: ( AND r= not -> ^( AND $logand $r) )*
            loop112:
            do {
                int alt112=2;
                int LA112_0 = input.LA(1);

                if ( (LA112_0==AND) ) {
                    alt112=1;
                }


                switch (alt112) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:488:9: AND r= not
            	    {
            	    AND360=(Token)match(input,AND,FOLLOW_AND_in_logand5026); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND360);

            	    pushFollow(FOLLOW_not_in_logand5030);
            	    r=not();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_not.add(r.getTree());


            	    // AST REWRITE
            	    // elements: AND, logand, r
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
            	    // 488:19: -> ^( AND $logand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:488:22: ^( AND $logand $r)
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
            	    break loop112;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:491:1: not : ( comp -> comp | NOT u= comp -> ^( NOT $u) );
    public final EulangParser.not_return not() throws RecognitionException {
        EulangParser.not_return retval = new EulangParser.not_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NOT362=null;
        EulangParser.comp_return u = null;

        EulangParser.comp_return comp361 = null;


        CommonTree NOT362_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:491:5: ( comp -> comp | NOT u= comp -> ^( NOT $u) )
            int alt113=2;
            int LA113_0 = input.LA(1);

            if ( (LA113_0==CODE||(LA113_0>=SIZEOF && LA113_0<=TYPEOF)||LA113_0==ID||LA113_0==COLON||LA113_0==LPAREN||LA113_0==NIL||(LA113_0>=TILDE && LA113_0<=AMP)||LA113_0==MINUS||(LA113_0>=PLUSPLUS && LA113_0<=STRING_LITERAL)||(LA113_0>=FALSE && LA113_0<=COLONS)) ) {
                alt113=1;
            }
            else if ( (LA113_0==NOT) ) {
                alt113=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 113, 0, input);

                throw nvae;
            }
            switch (alt113) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:491:8: comp
                    {
                    pushFollow(FOLLOW_comp_in_not5076);
                    comp361=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(comp361.getTree());


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
                    // 491:17: -> comp
                    {
                        adaptor.addChild(root_0, stream_comp.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:492:7: NOT u= comp
                    {
                    NOT362=(Token)match(input,NOT,FOLLOW_NOT_in_not5092); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT362);

                    pushFollow(FOLLOW_comp_in_not5096);
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
                    // 492:22: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:492:25: ^( NOT $u)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:1: comp : ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ364=null;
        Token COMPNE365=null;
        Token COMPLE366=null;
        Token COMPGE367=null;
        Token COMPULE368=null;
        Token COMPUGE369=null;
        Token LESS370=null;
        Token ULESS371=null;
        Token GREATER372=null;
        Token UGREATER373=null;
        EulangParser.bitor_return r = null;

        EulangParser.bitor_return bitor363 = null;


        CommonTree COMPEQ364_tree=null;
        CommonTree COMPNE365_tree=null;
        CommonTree COMPLE366_tree=null;
        CommonTree COMPGE367_tree=null;
        CommonTree COMPULE368_tree=null;
        CommonTree COMPUGE369_tree=null;
        CommonTree LESS370_tree=null;
        CommonTree ULESS371_tree=null;
        CommonTree GREATER372_tree=null;
        CommonTree UGREATER373_tree=null;
        RewriteRuleTokenStream stream_COMPGE=new RewriteRuleTokenStream(adaptor,"token COMPGE");
        RewriteRuleTokenStream stream_ULESS=new RewriteRuleTokenStream(adaptor,"token ULESS");
        RewriteRuleTokenStream stream_COMPEQ=new RewriteRuleTokenStream(adaptor,"token COMPEQ");
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMPLE=new RewriteRuleTokenStream(adaptor,"token COMPLE");
        RewriteRuleTokenStream stream_COMPNE=new RewriteRuleTokenStream(adaptor,"token COMPNE");
        RewriteRuleTokenStream stream_COMPULE=new RewriteRuleTokenStream(adaptor,"token COMPULE");
        RewriteRuleTokenStream stream_UGREATER=new RewriteRuleTokenStream(adaptor,"token UGREATER");
        RewriteRuleTokenStream stream_COMPUGE=new RewriteRuleTokenStream(adaptor,"token COMPUGE");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_bitor=new RewriteRuleSubtreeStream(adaptor,"rule bitor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:5: ( ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:9: ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:9: ( bitor -> bitor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:11: bitor
            {
            pushFollow(FOLLOW_bitor_in_comp5130);
            bitor363=bitor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitor.add(bitor363.getTree());


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
            // 495:24: -> bitor
            {
                adaptor.addChild(root_0, stream_bitor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:496:7: ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )*
            loop114:
            do {
                int alt114=11;
                switch ( input.LA(1) ) {
                case COMPEQ:
                    {
                    alt114=1;
                    }
                    break;
                case COMPNE:
                    {
                    alt114=2;
                    }
                    break;
                case COMPLE:
                    {
                    alt114=3;
                    }
                    break;
                case COMPGE:
                    {
                    alt114=4;
                    }
                    break;
                case COMPULE:
                    {
                    alt114=5;
                    }
                    break;
                case COMPUGE:
                    {
                    alt114=6;
                    }
                    break;
                case LESS:
                    {
                    alt114=7;
                    }
                    break;
                case ULESS:
                    {
                    alt114=8;
                    }
                    break;
                case GREATER:
                    {
                    alt114=9;
                    }
                    break;
                case UGREATER:
                    {
                    alt114=10;
                    }
                    break;

                }

                switch (alt114) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:496:9: COMPEQ r= bitor
            	    {
            	    COMPEQ364=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_comp5163); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ364);

            	    pushFollow(FOLLOW_bitor_in_comp5167);
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
            	    // 496:24: -> ^( COMPEQ $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:496:27: ^( COMPEQ $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:497:9: COMPNE r= bitor
            	    {
            	    COMPNE365=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_comp5189); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE365);

            	    pushFollow(FOLLOW_bitor_in_comp5193);
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
            	    // 497:24: -> ^( COMPNE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:497:27: ^( COMPNE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:498:9: COMPLE r= bitor
            	    {
            	    COMPLE366=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp5215); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE366);

            	    pushFollow(FOLLOW_bitor_in_comp5219);
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
            	    // 498:27: -> ^( COMPLE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:498:30: ^( COMPLE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:499:9: COMPGE r= bitor
            	    {
            	    COMPGE367=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp5244); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE367);

            	    pushFollow(FOLLOW_bitor_in_comp5248);
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
            	    // 499:27: -> ^( COMPGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:499:30: ^( COMPGE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:500:9: COMPULE r= bitor
            	    {
            	    COMPULE368=(Token)match(input,COMPULE,FOLLOW_COMPULE_in_comp5273); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPULE.add(COMPULE368);

            	    pushFollow(FOLLOW_bitor_in_comp5277);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, COMPULE, comp
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
            	    // 500:28: -> ^( COMPULE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:500:31: ^( COMPULE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPULE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 6 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:501:9: COMPUGE r= bitor
            	    {
            	    COMPUGE369=(Token)match(input,COMPUGE,FOLLOW_COMPUGE_in_comp5302); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPUGE.add(COMPUGE369);

            	    pushFollow(FOLLOW_bitor_in_comp5306);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, comp, COMPUGE
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
            	    // 501:28: -> ^( COMPUGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:501:31: ^( COMPUGE $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_COMPUGE.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 7 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:502:9: LESS r= bitor
            	    {
            	    LESS370=(Token)match(input,LESS,FOLLOW_LESS_in_comp5331); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS370);

            	    pushFollow(FOLLOW_bitor_in_comp5335);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: LESS, comp, r
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
            	    // 502:26: -> ^( LESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:502:29: ^( LESS $comp $r)
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
            	case 8 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:503:9: ULESS r= bitor
            	    {
            	    ULESS371=(Token)match(input,ULESS,FOLLOW_ULESS_in_comp5361); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ULESS.add(ULESS371);

            	    pushFollow(FOLLOW_bitor_in_comp5365);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, ULESS, r
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
            	    // 503:27: -> ^( ULESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:503:30: ^( ULESS $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_ULESS.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 9 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:9: GREATER r= bitor
            	    {
            	    GREATER372=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp5391); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER372);

            	    pushFollow(FOLLOW_bitor_in_comp5395);
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
            	    // 504:28: -> ^( GREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:31: ^( GREATER $comp $r)
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
            	case 10 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:9: UGREATER r= bitor
            	    {
            	    UGREATER373=(Token)match(input,UGREATER,FOLLOW_UGREATER_in_comp5420); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UGREATER.add(UGREATER373);

            	    pushFollow(FOLLOW_bitor_in_comp5424);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, comp, UGREATER
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
            	    // 505:29: -> ^( UGREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:32: ^( UGREATER $comp $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UGREATER.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop114;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:1: bitor : ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* ;
    public final EulangParser.bitor_return bitor() throws RecognitionException {
        EulangParser.bitor_return retval = new EulangParser.bitor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR375=null;
        EulangParser.bitxor_return r = null;

        EulangParser.bitxor_return bitxor374 = null;


        CommonTree BAR375_tree=null;
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_bitxor=new RewriteRuleSubtreeStream(adaptor,"rule bitxor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:6: ( ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:8: ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:8: ( bitxor -> bitxor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:10: bitxor
            {
            pushFollow(FOLLOW_bitxor_in_bitor5474);
            bitxor374=bitxor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitxor.add(bitxor374.getTree());


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
            // 510:22: -> bitxor
            {
                adaptor.addChild(root_0, stream_bitxor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:7: ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            loop115:
            do {
                int alt115=2;
                int LA115_0 = input.LA(1);

                if ( (LA115_0==BAR) ) {
                    alt115=1;
                }


                switch (alt115) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:9: BAR r= bitxor
            	    {
            	    BAR375=(Token)match(input,BAR,FOLLOW_BAR_in_bitor5502); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR375);

            	    pushFollow(FOLLOW_bitxor_in_bitor5506);
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
            	    // 511:23: -> ^( BITOR $bitor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:26: ^( BITOR $bitor $r)
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
            	    break loop115;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:513:1: bitxor : ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )* ;
    public final EulangParser.bitxor_return bitxor() throws RecognitionException {
        EulangParser.bitxor_return retval = new EulangParser.bitxor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TILDE377=null;
        EulangParser.bitand_return r = null;

        EulangParser.bitand_return bitand376 = null;


        CommonTree TILDE377_tree=null;
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_bitand=new RewriteRuleSubtreeStream(adaptor,"rule bitand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:513:7: ( ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:513:9: ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:513:9: ( bitand -> bitand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:513:11: bitand
            {
            pushFollow(FOLLOW_bitand_in_bitxor5532);
            bitand376=bitand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitand.add(bitand376.getTree());


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
            // 513:23: -> bitand
            {
                adaptor.addChild(root_0, stream_bitand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:7: ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )*
            loop116:
            do {
                int alt116=2;
                int LA116_0 = input.LA(1);

                if ( (LA116_0==TILDE) ) {
                    alt116=1;
                }


                switch (alt116) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:9: TILDE r= bitand
            	    {
            	    TILDE377=(Token)match(input,TILDE,FOLLOW_TILDE_in_bitxor5560); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_TILDE.add(TILDE377);

            	    pushFollow(FOLLOW_bitand_in_bitxor5564);
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
            	    // 514:25: -> ^( BITXOR $bitxor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:28: ^( BITXOR $bitxor $r)
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
            	    break loop116;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:1: bitand : ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* ;
    public final EulangParser.bitand_return bitand() throws RecognitionException {
        EulangParser.bitand_return retval = new EulangParser.bitand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP379=null;
        EulangParser.shift_return r = null;

        EulangParser.shift_return shift378 = null;


        CommonTree AMP379_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:7: ( ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:9: ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:9: ( shift -> shift )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:11: shift
            {
            pushFollow(FOLLOW_shift_in_bitand5589);
            shift378=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(shift378.getTree());


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
            // 516:22: -> shift
            {
                adaptor.addChild(root_0, stream_shift.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:7: ( AMP r= shift -> ^( BITAND $bitand $r) )*
            loop117:
            do {
                int alt117=2;
                int LA117_0 = input.LA(1);

                if ( (LA117_0==AMP) ) {
                    alt117=1;
                }


                switch (alt117) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:9: AMP r= shift
            	    {
            	    AMP379=(Token)match(input,AMP,FOLLOW_AMP_in_bitand5617); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP379);

            	    pushFollow(FOLLOW_shift_in_bitand5621);
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
            	    // 517:22: -> ^( BITAND $bitand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:25: ^( BITAND $bitand $r)
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
            	    break loop117;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:520:1: shift : ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT381=null;
        Token RSHIFT382=null;
        Token URSHIFT383=null;
        Token CRSHIFT384=null;
        Token CLSHIFT385=null;
        EulangParser.factor_return r = null;

        EulangParser.factor_return factor380 = null;


        CommonTree LSHIFT381_tree=null;
        CommonTree RSHIFT382_tree=null;
        CommonTree URSHIFT383_tree=null;
        CommonTree CRSHIFT384_tree=null;
        CommonTree CLSHIFT385_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_CLSHIFT=new RewriteRuleTokenStream(adaptor,"token CLSHIFT");
        RewriteRuleTokenStream stream_CRSHIFT=new RewriteRuleTokenStream(adaptor,"token CRSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:520:6: ( ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:520:9: ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:520:9: ( factor -> factor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:520:11: factor
            {
            pushFollow(FOLLOW_factor_in_shift5648);
            factor380=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(factor380.getTree());


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
            // 520:25: -> factor
            {
                adaptor.addChild(root_0, stream_factor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:521:7: ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )*
            loop118:
            do {
                int alt118=6;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt118=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt118=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt118=3;
                    }
                    break;
                case CRSHIFT:
                    {
                    alt118=4;
                    }
                    break;
                case CLSHIFT:
                    {
                    alt118=5;
                    }
                    break;

                }

                switch (alt118) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:521:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:521:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:521:11: LSHIFT r= factor
            	    {
            	    LSHIFT381=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift5682); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT381);

            	    pushFollow(FOLLOW_factor_in_shift5686);
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
            	    // 521:29: -> ^( LSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:521:32: ^( LSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:522:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:522:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:522:11: RSHIFT r= factor
            	    {
            	    RSHIFT382=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift5715); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT382);

            	    pushFollow(FOLLOW_factor_in_shift5719);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, RSHIFT, shift
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
            	    // 522:29: -> ^( RSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:522:32: ^( RSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:11: URSHIFT r= factor
            	    {
            	    URSHIFT383=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift5747); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT383);

            	    pushFollow(FOLLOW_factor_in_shift5751);
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
            	    // 523:30: -> ^( URSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:33: ^( URSHIFT $shift $r)
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
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:9: ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:9: ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:11: CRSHIFT r= factor
            	    {
            	    CRSHIFT384=(Token)match(input,CRSHIFT,FOLLOW_CRSHIFT_in_shift5779); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CRSHIFT.add(CRSHIFT384);

            	    pushFollow(FOLLOW_factor_in_shift5783);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, CRSHIFT, shift
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
            	    // 524:30: -> ^( CRSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:33: ^( CRSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_CRSHIFT.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:9: ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:9: ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:11: CLSHIFT r= factor
            	    {
            	    CLSHIFT385=(Token)match(input,CLSHIFT,FOLLOW_CLSHIFT_in_shift5811); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CLSHIFT.add(CLSHIFT385);

            	    pushFollow(FOLLOW_factor_in_shift5815);
            	    r=factor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_factor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, CLSHIFT, shift
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
            	    // 525:30: -> ^( CLSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:33: ^( CLSHIFT $shift $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_CLSHIFT.nextNode(), root_1);

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
            	    break loop118;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:528:1: factor : ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS387=null;
        Token MINUS388=null;
        EulangParser.term_return r = null;

        EulangParser.term_return term386 = null;


        CommonTree PLUS387_tree=null;
        CommonTree MINUS388_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:5: ( ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:7: ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:7: ( term -> term )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:9: term
            {
            pushFollow(FOLLOW_term_in_factor5857);
            term386=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(term386.getTree());


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
            // 529:27: -> term
            {
                adaptor.addChild(root_0, stream_term.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:9: ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            loop119:
            do {
                int alt119=3;
                int LA119_0 = input.LA(1);

                if ( (LA119_0==PLUS) ) {
                    alt119=1;
                }
                else if ( (LA119_0==MINUS) && (synpred19_Eulang())) {
                    alt119=2;
                }


                switch (alt119) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:13: PLUS r= term
            	    {
            	    PLUS387=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor5890); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS387);

            	    pushFollow(FOLLOW_term_in_factor5894);
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
            	    // 530:33: -> ^( ADD $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:36: ^( ADD $factor $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:12: ( MINUS term )=> MINUS r= term
            	    {
            	    MINUS388=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor5936); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS388);

            	    pushFollow(FOLLOW_term_in_factor5940);
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
            	    // 531:49: -> ^( SUB $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:52: ^( SUB $factor $r)
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
            	    break loop119;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:1: term : ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR390=null;
        Token SLASH391=null;
        Token REM392=null;
        Token UDIV393=null;
        Token UREM394=null;
        Token MOD395=null;
        EulangParser.unary_return r = null;

        EulangParser.unary_return unary389 = null;


        CommonTree STAR390_tree=null;
        CommonTree SLASH391_tree=null;
        CommonTree REM392_tree=null;
        CommonTree UDIV393_tree=null;
        CommonTree UREM394_tree=null;
        CommonTree MOD395_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_REM=new RewriteRuleTokenStream(adaptor,"token REM");
        RewriteRuleTokenStream stream_UREM=new RewriteRuleTokenStream(adaptor,"token UREM");
        RewriteRuleTokenStream stream_MOD=new RewriteRuleTokenStream(adaptor,"token MOD");
        RewriteRuleTokenStream stream_UDIV=new RewriteRuleTokenStream(adaptor,"token UDIV");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:6: ( ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:8: ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:8: ( unary -> unary )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:10: unary
            {
            pushFollow(FOLLOW_unary_in_term5985);
            unary389=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(unary389.getTree());


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
            // 535:33: -> unary
            {
                adaptor.addChild(root_0, stream_unary.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:536:9: ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )*
            loop120:
            do {
                int alt120=7;
                int LA120_0 = input.LA(1);

                if ( (LA120_0==STAR) && (synpred20_Eulang())) {
                    alt120=1;
                }
                else if ( (LA120_0==SLASH) ) {
                    alt120=2;
                }
                else if ( (LA120_0==REM) ) {
                    alt120=3;
                }
                else if ( (LA120_0==UDIV) ) {
                    alt120=4;
                }
                else if ( (LA120_0==UREM) ) {
                    alt120=5;
                }
                else if ( (LA120_0==MOD) ) {
                    alt120=6;
                }


                switch (alt120) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:536:11: ( STAR unary )=> STAR r= unary
            	    {
            	    STAR390=(Token)match(input,STAR,FOLLOW_STAR_in_term6029); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR390);

            	    pushFollow(FOLLOW_unary_in_term6033);
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
            	    // 536:52: -> ^( MUL $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:536:55: ^( MUL $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:537:11: SLASH r= unary
            	    {
            	    SLASH391=(Token)match(input,SLASH,FOLLOW_SLASH_in_term6069); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH391);

            	    pushFollow(FOLLOW_unary_in_term6073);
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
            	    // 537:36: -> ^( DIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:537:39: ^( DIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:538:11: REM r= unary
            	    {
            	    REM392=(Token)match(input,REM,FOLLOW_REM_in_term6108); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_REM.add(REM392);

            	    pushFollow(FOLLOW_unary_in_term6112);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r, REM
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
            	    // 538:34: -> ^( REM $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:538:37: ^( REM $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_REM.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:539:11: UDIV r= unary
            	    {
            	    UDIV393=(Token)match(input,UDIV,FOLLOW_UDIV_in_term6147); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UDIV.add(UDIV393);

            	    pushFollow(FOLLOW_unary_in_term6151);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: UDIV, r, term
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
            	    // 539:35: -> ^( UDIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:539:38: ^( UDIV $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UDIV.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:11: UREM r= unary
            	    {
            	    UREM394=(Token)match(input,UREM,FOLLOW_UREM_in_term6186); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UREM.add(UREM394);

            	    pushFollow(FOLLOW_unary_in_term6190);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, UREM, r
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
            	    // 540:35: -> ^( UREM $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:38: ^( UREM $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_UREM.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;
            	case 6 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:541:11: MOD r= unary
            	    {
            	    MOD395=(Token)match(input,MOD,FOLLOW_MOD_in_term6225); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MOD.add(MOD395);

            	    pushFollow(FOLLOW_unary_in_term6229);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: MOD, term, r
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
            	    // 541:34: -> ^( MOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:541:37: ^( MOD $term $r)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot(stream_MOD.nextNode(), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_r.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop120;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:1: unary : ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS396=null;
        Token TILDE397=null;
        Token PLUSPLUS398=null;
        Token MINUSMINUS399=null;
        Token PLUSPLUS401=null;
        Token MINUSMINUS402=null;
        Token AMP403=null;
        Token SIZEOF405=null;
        Token TYPEOF407=null;
        EulangParser.unary_return u = null;

        EulangParser.lhs_return a = null;

        EulangParser.atom_return atom400 = null;

        EulangParser.lhs_return lhs404 = null;

        EulangParser.atom_return atom406 = null;

        EulangParser.atom_return atom408 = null;


        CommonTree MINUS396_tree=null;
        CommonTree TILDE397_tree=null;
        CommonTree PLUSPLUS398_tree=null;
        CommonTree MINUSMINUS399_tree=null;
        CommonTree PLUSPLUS401_tree=null;
        CommonTree MINUSMINUS402_tree=null;
        CommonTree AMP403_tree=null;
        CommonTree SIZEOF405_tree=null;
        CommonTree TYPEOF407_tree=null;
        RewriteRuleTokenStream stream_SIZEOF=new RewriteRuleTokenStream(adaptor,"token SIZEOF");
        RewriteRuleTokenStream stream_MINUSMINUS=new RewriteRuleTokenStream(adaptor,"token MINUSMINUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleTokenStream stream_TYPEOF=new RewriteRuleTokenStream(adaptor,"token TYPEOF");
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleTokenStream stream_PLUSPLUS=new RewriteRuleTokenStream(adaptor,"token PLUSPLUS");
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:6: ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) )
            int alt121=10;
            alt121 = dfa121.predict(input);
            switch (alt121) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:9: MINUS u= unary
                    {
                    MINUS396=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary6302); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS396);

                    pushFollow(FOLLOW_unary_in_unary6306);
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
                    // 546:23: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:26: ^( NEG $u)
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
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:547:9: TILDE u= unary
                    {
                    TILDE397=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary6326); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE397);

                    pushFollow(FOLLOW_unary_in_unary6330);
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
                    // 547:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:547:30: ^( INV $u)
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
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:9: ( lhs PLUSPLUS )=>a= lhs PLUSPLUS
                    {
                    pushFollow(FOLLOW_lhs_in_unary6365);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());
                    PLUSPLUS398=(Token)match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unary6367); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUSPLUS.add(PLUSPLUS398);



                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 548:44: -> ^( POSTINC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:47: ^( POSTINC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(POSTINC, "POSTINC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:9: ( lhs MINUSMINUS )=>a= lhs MINUSMINUS
                    {
                    pushFollow(FOLLOW_lhs_in_unary6398);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());
                    MINUSMINUS399=(Token)match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_unary6400); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUSMINUS.add(MINUSMINUS399);



                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 549:47: -> ^( POSTDEC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:50: ^( POSTDEC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(POSTDEC, "POSTDEC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:550:9: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:550:9: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:550:11: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary6421);
                    atom400=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom400.getTree());


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
                    // 550:23: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:551:9: PLUSPLUS a= lhs
                    {
                    PLUSPLUS401=(Token)match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unary6452); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUSPLUS.add(PLUSPLUS401);

                    pushFollow(FOLLOW_lhs_in_unary6456);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());


                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 551:26: -> ^( PREINC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:551:29: ^( PREINC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PREINC, "PREINC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 7 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:552:9: MINUSMINUS a= lhs
                    {
                    MINUSMINUS402=(Token)match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_unary6477); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUSMINUS.add(MINUSMINUS402);

                    pushFollow(FOLLOW_lhs_in_unary6481);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());


                    // AST REWRITE
                    // elements: a
                    // token labels: 
                    // rule labels: retval, a
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a=new RewriteRuleSubtreeStream(adaptor,"rule a",a!=null?a.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 552:26: -> ^( PREDEC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:552:29: ^( PREDEC $a)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PREDEC, "PREDEC"), root_1);

                        adaptor.addChild(root_1, stream_a.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:553:10: AMP lhs
                    {
                    AMP403=(Token)match(input,AMP,FOLLOW_AMP_in_unary6501); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AMP.add(AMP403);

                    pushFollow(FOLLOW_lhs_in_unary6503);
                    lhs404=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs404.getTree());


                    // AST REWRITE
                    // elements: lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 553:41: -> ^( ADDROF lhs )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:553:44: ^( ADDROF lhs )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ADDROF, "ADDROF"), root_1);

                        adaptor.addChild(root_1, stream_lhs.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:554:9: SIZEOF atom
                    {
                    SIZEOF405=(Token)match(input,SIZEOF,FOLLOW_SIZEOF_in_unary6544); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SIZEOF.add(SIZEOF405);

                    pushFollow(FOLLOW_atom_in_unary6546);
                    atom406=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom406.getTree());


                    // AST REWRITE
                    // elements: atom, SIZEOF
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 554:27: -> ^( SIZEOF atom )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:554:30: ^( SIZEOF atom )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_SIZEOF.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_atom.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:9: TYPEOF atom
                    {
                    TYPEOF407=(Token)match(input,TYPEOF,FOLLOW_TYPEOF_in_unary6570); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TYPEOF.add(TYPEOF407);

                    pushFollow(FOLLOW_atom_in_unary6572);
                    atom408=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom408.getTree());


                    // AST REWRITE
                    // elements: atom, TYPEOF
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 555:27: -> ^( TYPEOF atom )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:30: ^( TYPEOF atom )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_TYPEOF.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_atom.nextTree());

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

    public static class lhs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lhs"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:558:1: lhs : ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )? ;
    public final EulangParser.lhs_return lhs() throws RecognitionException {
        EulangParser.lhs_return retval = new EulangParser.lhs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER411=null;
        Token CHAR_LITERAL412=null;
        Token STRING_LITERAL413=null;
        Token LPAREN414=null;
        Token RPAREN415=null;
        Token PERIOD416=null;
        Token ID417=null;
        Token LPAREN418=null;
        Token RPAREN420=null;
        Token CARET422=null;
        Token LBRACE423=null;
        Token PLUS424=null;
        Token RBRACE426=null;
        Token AS427=null;
        Token PLUS428=null;
        EulangParser.assignExpr_return a1 = null;

        EulangParser.idExpr_return idExpr409 = null;

        EulangParser.tuple_return tuple410 = null;

        EulangParser.arglist_return arglist419 = null;

        EulangParser.arrayAccess_return arrayAccess421 = null;

        EulangParser.type_return type425 = null;

        EulangParser.type_return type429 = null;


        CommonTree NUMBER411_tree=null;
        CommonTree CHAR_LITERAL412_tree=null;
        CommonTree STRING_LITERAL413_tree=null;
        CommonTree LPAREN414_tree=null;
        CommonTree RPAREN415_tree=null;
        CommonTree PERIOD416_tree=null;
        CommonTree ID417_tree=null;
        CommonTree LPAREN418_tree=null;
        CommonTree RPAREN420_tree=null;
        CommonTree CARET422_tree=null;
        CommonTree LBRACE423_tree=null;
        CommonTree PLUS424_tree=null;
        CommonTree RBRACE426_tree=null;
        CommonTree AS427_tree=null;
        CommonTree PLUS428_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_arrayAccess=new RewriteRuleSubtreeStream(adaptor,"rule arrayAccess");
        RewriteRuleSubtreeStream stream_idExpr=new RewriteRuleSubtreeStream(adaptor,"rule idExpr");
        RewriteRuleSubtreeStream stream_tuple=new RewriteRuleSubtreeStream(adaptor,"rule tuple");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:558:5: ( ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:559:3: ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:559:3: ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1)
            int alt122=6;
            switch ( input.LA(1) ) {
            case ID:
            case COLON:
            case COLONS:
                {
                alt122=1;
                }
                break;
            case LPAREN:
                {
                int LA122_3 = input.LA(2);

                if ( (synpred23_Eulang()) ) {
                    alt122=2;
                }
                else if ( (true) ) {
                    alt122=6;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 122, 3, input);

                    throw nvae;
                }
                }
                break;
            case NUMBER:
                {
                alt122=3;
                }
                break;
            case CHAR_LITERAL:
                {
                alt122=4;
                }
                break;
            case STRING_LITERAL:
                {
                alt122=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 122, 0, input);

                throw nvae;
            }

            switch (alt122) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:560:8: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_lhs6607);
                    idExpr409=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr409.getTree());


                    // AST REWRITE
                    // elements: idExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 560:40: -> idExpr
                    {
                        adaptor.addChild(root_0, stream_idExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:561:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_lhs6654);
                    tuple410=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple410.getTree());


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
                    // 561:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:9: NUMBER
                    {
                    NUMBER411=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lhs6693); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER411);



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
                    // 562:41: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:44: ^( LIT NUMBER )
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
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL412=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_lhs6736); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL412);



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
                    // 563:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:42: ^( LIT CHAR_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:9: STRING_LITERAL
                    {
                    STRING_LITERAL413=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_lhs6771); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL413);



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
                    // 564:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:42: ^( LIT STRING_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:9: LPAREN a1= assignExpr RPAREN
                    {
                    LPAREN414=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_lhs6804); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN414);

                    pushFollow(FOLLOW_assignExpr_in_lhs6808);
                    a1=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(a1.getTree());
                    RPAREN415=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_lhs6810); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN415);



                    // AST REWRITE
                    // elements: a1
                    // token labels: 
                    // rule labels: retval, a1
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a1=new RewriteRuleSubtreeStream(adaptor,"rule a1",a1!=null?a1.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 565:51: -> $a1
                    {
                        adaptor.addChild(root_0, stream_a1.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:567:5: ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )*
            loop124:
            do {
                int alt124=6;
                int LA124_0 = input.LA(1);

                if ( (LA124_0==PERIOD) ) {
                    alt124=1;
                }
                else if ( (LA124_0==LPAREN) ) {
                    alt124=2;
                }
                else if ( (LA124_0==LBRACKET) && (synpred24_Eulang())) {
                    alt124=3;
                }
                else if ( (LA124_0==CARET) ) {
                    alt124=4;
                }
                else if ( (LA124_0==LBRACE) ) {
                    alt124=5;
                }


                switch (alt124) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:568:7: ( PERIOD ID -> ^( FIELDREF $lhs ID ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:568:7: ( PERIOD ID -> ^( FIELDREF $lhs ID ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:568:9: PERIOD ID
            	    {
            	    PERIOD416=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_lhs6853); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD416);

            	    ID417=(Token)match(input,ID,FOLLOW_ID_in_lhs6855); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID417);



            	    // AST REWRITE
            	    // elements: ID, lhs
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 568:20: -> ^( FIELDREF $lhs ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:568:23: ^( FIELDREF $lhs ID )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FIELDREF, "FIELDREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_ID.nextNode());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:7: ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:7: ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:10: LPAREN arglist RPAREN
            	    {
            	    LPAREN418=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_lhs6880); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN418);

            	    pushFollow(FOLLOW_arglist_in_lhs6882);
            	    arglist419=arglist();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arglist.add(arglist419.getTree());
            	    RPAREN420=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_lhs6884); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN420);



            	    // AST REWRITE
            	    // elements: lhs, arglist
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 569:34: -> ^( CALL $lhs arglist )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:37: ^( CALL $lhs arglist )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CALL, "CALL"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arglist.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:9: ( LBRACKET )=> arrayAccess
            	    {
            	    pushFollow(FOLLOW_arrayAccess_in_lhs6917);
            	    arrayAccess421=arrayAccess();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arrayAccess.add(arrayAccess421.getTree());


            	    // AST REWRITE
            	    // elements: arrayAccess, lhs
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 570:39: -> ^( INDEX $lhs arrayAccess )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:42: ^( INDEX $lhs arrayAccess )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INDEX, "INDEX"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arrayAccess.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:571:7: ( CARET -> ^( DEREF $lhs) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:571:7: ( CARET -> ^( DEREF $lhs) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:571:9: CARET
            	    {
            	    CARET422=(Token)match(input,CARET,FOLLOW_CARET_in_lhs6942); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET422);



            	    // AST REWRITE
            	    // elements: lhs
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 571:15: -> ^( DEREF $lhs)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:571:18: ^( DEREF $lhs)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEREF, "DEREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:9: LBRACE ( PLUS )? type RBRACE
            	    {
            	    LBRACE423=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_lhs6963); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE423);

            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:16: ( PLUS )?
            	    int alt123=2;
            	    int LA123_0 = input.LA(1);

            	    if ( (LA123_0==PLUS) ) {
            	        alt123=1;
            	    }
            	    switch (alt123) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:16: PLUS
            	            {
            	            PLUS424=(Token)match(input,PLUS,FOLLOW_PLUS_in_lhs6965); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_PLUS.add(PLUS424);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_type_in_lhs6968);
            	    type425=type();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_type.add(type425.getTree());
            	    RBRACE426=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_lhs6970); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE426);



            	    // AST REWRITE
            	    // elements: PLUS, lhs, type
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 572:34: -> ^( CAST ( PLUS )? type $lhs)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:37: ^( CAST ( PLUS )? type $lhs)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:572:44: ( PLUS )?
            	        if ( stream_PLUS.hasNext() ) {
            	            adaptor.addChild(root_1, stream_PLUS.nextNode());

            	        }
            	        stream_PLUS.reset();
            	        adaptor.addChild(root_1, stream_type.nextTree());
            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop124;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:575:5: ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )?
            int alt126=2;
            int LA126_0 = input.LA(1);

            if ( (LA126_0==AS) ) {
                alt126=1;
            }
            switch (alt126) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:7: AS ( PLUS )? type
                    {
                    AS427=(Token)match(input,AS,FOLLOW_AS_in_lhs7011); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AS.add(AS427);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:10: ( PLUS )?
                    int alt125=2;
                    int LA125_0 = input.LA(1);

                    if ( (LA125_0==PLUS) ) {
                        alt125=1;
                    }
                    switch (alt125) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:10: PLUS
                            {
                            PLUS428=(Token)match(input,PLUS,FOLLOW_PLUS_in_lhs7013); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS428);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_type_in_lhs7016);
                    type429=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type429.getTree());


                    // AST REWRITE
                    // elements: PLUS, type, lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 576:21: -> ^( CAST ( PLUS )? type $lhs)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:24: ^( CAST ( PLUS )? type $lhs)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:31: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_retval.nextTree());

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
    // $ANTLR end "lhs"

    public static class atom_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atom"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )? ;
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER430=null;
        Token FALSE431=null;
        Token TRUE432=null;
        Token CHAR_LITERAL433=null;
        Token STRING_LITERAL434=null;
        Token NIL435=null;
        Token LPAREN438=null;
        Token RPAREN439=null;
        Token LPAREN440=null;
        Token RPAREN441=null;
        Token PERIOD443=null;
        Token ID444=null;
        Token LPAREN445=null;
        Token RPAREN447=null;
        Token CARET449=null;
        Token LBRACE450=null;
        Token PLUS451=null;
        Token RBRACE453=null;
        Token AS454=null;
        EulangParser.varDecl_return a0 = null;

        EulangParser.assignExpr_return a1 = null;

        EulangParser.idExpr_return idExpr436 = null;

        EulangParser.tuple_return tuple437 = null;

        EulangParser.code_return code442 = null;

        EulangParser.arglist_return arglist446 = null;

        EulangParser.arrayAccess_return arrayAccess448 = null;

        EulangParser.type_return type452 = null;

        EulangParser.type_return type455 = null;


        CommonTree NUMBER430_tree=null;
        CommonTree FALSE431_tree=null;
        CommonTree TRUE432_tree=null;
        CommonTree CHAR_LITERAL433_tree=null;
        CommonTree STRING_LITERAL434_tree=null;
        CommonTree NIL435_tree=null;
        CommonTree LPAREN438_tree=null;
        CommonTree RPAREN439_tree=null;
        CommonTree LPAREN440_tree=null;
        CommonTree RPAREN441_tree=null;
        CommonTree PERIOD443_tree=null;
        CommonTree ID444_tree=null;
        CommonTree LPAREN445_tree=null;
        CommonTree RPAREN447_tree=null;
        CommonTree CARET449_tree=null;
        CommonTree LBRACE450_tree=null;
        CommonTree PLUS451_tree=null;
        CommonTree RBRACE453_tree=null;
        CommonTree AS454_tree=null;
        RewriteRuleTokenStream stream_AS=new RewriteRuleTokenStream(adaptor,"token AS");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleTokenStream stream_CHAR_LITERAL=new RewriteRuleTokenStream(adaptor,"token CHAR_LITERAL");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_NUMBER=new RewriteRuleTokenStream(adaptor,"token NUMBER");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_FALSE=new RewriteRuleTokenStream(adaptor,"token FALSE");
        RewriteRuleTokenStream stream_TRUE=new RewriteRuleTokenStream(adaptor,"token TRUE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_arglist=new RewriteRuleSubtreeStream(adaptor,"rule arglist");
        RewriteRuleSubtreeStream stream_arrayAccess=new RewriteRuleSubtreeStream(adaptor,"rule arrayAccess");
        RewriteRuleSubtreeStream stream_idExpr=new RewriteRuleSubtreeStream(adaptor,"rule idExpr");
        RewriteRuleSubtreeStream stream_tuple=new RewriteRuleSubtreeStream(adaptor,"rule tuple");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_code=new RewriteRuleSubtreeStream(adaptor,"rule code");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:6: ( ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code )
            int alt127=11;
            alt127 = dfa127.predict(input);
            switch (alt127) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:582:7: NUMBER
                    {
                    NUMBER430=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom7065); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER430);



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
                    // 582:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:582:42: ^( LIT NUMBER )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:9: FALSE
                    {
                    FALSE431=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom7108); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE431);



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
                    // 583:39: -> ^( LIT FALSE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:42: ^( LIT FALSE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:9: TRUE
                    {
                    TRUE432=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom7150); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE432);



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
                    // 584:39: -> ^( LIT TRUE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:42: ^( LIT TRUE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:585:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL433=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom7193); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL433);



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
                    // 585:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:585:42: ^( LIT CHAR_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:586:9: STRING_LITERAL
                    {
                    STRING_LITERAL434=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom7228); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL434);



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
                    // 586:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:586:42: ^( LIT STRING_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:587:9: NIL
                    {
                    NIL435=(Token)match(input,NIL,FOLLOW_NIL_in_atom7261); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL435);



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
                    // 587:38: -> ^( LIT NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:587:41: ^( LIT NIL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:588:9: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_atom7304);
                    idExpr436=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr436.getTree());


                    // AST REWRITE
                    // elements: idExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 588:41: -> idExpr
                    {
                        adaptor.addChild(root_0, stream_idExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:589:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_atom7351);
                    tuple437=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple437.getTree());


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
                    // 589:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:590:9: ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN
                    {
                    LPAREN438=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7400); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN438);

                    pushFollow(FOLLOW_varDecl_in_atom7404);
                    a0=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(a0.getTree());
                    RPAREN439=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7406); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN439);



                    // AST REWRITE
                    // elements: a0
                    // token labels: 
                    // rule labels: retval, a0
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a0=new RewriteRuleSubtreeStream(adaptor,"rule a0",a0!=null?a0.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 590:70: -> $a0
                    {
                        adaptor.addChild(root_0, stream_a0.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:9: LPAREN a1= assignExpr RPAREN
                    {
                    LPAREN440=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7435); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN440);

                    pushFollow(FOLLOW_assignExpr_in_atom7439);
                    a1=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(a1.getTree());
                    RPAREN441=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7441); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN441);



                    // AST REWRITE
                    // elements: a1
                    // token labels: 
                    // rule labels: retval, a1
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_a1=new RewriteRuleSubtreeStream(adaptor,"rule a1",a1!=null?a1.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 591:51: -> $a1
                    {
                        adaptor.addChild(root_0, stream_a1.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:9: code
                    {
                    pushFollow(FOLLOW_code_in_atom7470);
                    code442=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_code.add(code442.getTree());


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
                    // 592:40: -> code
                    {
                        adaptor.addChild(root_0, stream_code.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:596:5: ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )*
            loop129:
            do {
                int alt129=6;
                int LA129_0 = input.LA(1);

                if ( (LA129_0==PERIOD) ) {
                    alt129=1;
                }
                else if ( (LA129_0==LPAREN) ) {
                    alt129=2;
                }
                else if ( (LA129_0==LBRACKET) && (synpred27_Eulang())) {
                    alt129=3;
                }
                else if ( (LA129_0==CARET) ) {
                    alt129=4;
                }
                else if ( (LA129_0==LBRACE) ) {
                    alt129=5;
                }


                switch (alt129) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:597:7: ( PERIOD ID -> ^( FIELDREF $atom ID ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:597:7: ( PERIOD ID -> ^( FIELDREF $atom ID ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:597:9: PERIOD ID
            	    {
            	    PERIOD443=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_atom7529); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD443);

            	    ID444=(Token)match(input,ID,FOLLOW_ID_in_atom7531); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID444);



            	    // AST REWRITE
            	    // elements: ID, atom
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 597:20: -> ^( FIELDREF $atom ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:597:23: ^( FIELDREF $atom ID )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FIELDREF, "FIELDREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_ID.nextNode());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 2 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:7: ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:7: ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:10: LPAREN arglist RPAREN
            	    {
            	    LPAREN445=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7556); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN445);

            	    pushFollow(FOLLOW_arglist_in_atom7558);
            	    arglist446=arglist();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arglist.add(arglist446.getTree());
            	    RPAREN447=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7560); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN447);



            	    // AST REWRITE
            	    // elements: atom, arglist
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 598:34: -> ^( CALL $atom arglist )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:37: ^( CALL $atom arglist )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CALL, "CALL"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arglist.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 3 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:9: ( LBRACKET )=> arrayAccess
            	    {
            	    pushFollow(FOLLOW_arrayAccess_in_atom7593);
            	    arrayAccess448=arrayAccess();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arrayAccess.add(arrayAccess448.getTree());


            	    // AST REWRITE
            	    // elements: arrayAccess, atom
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 599:39: -> ^( INDEX $atom arrayAccess )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:42: ^( INDEX $atom arrayAccess )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INDEX, "INDEX"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_arrayAccess.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 4 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:600:7: ( CARET -> ^( DEREF $atom) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:600:7: ( CARET -> ^( DEREF $atom) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:600:9: CARET
            	    {
            	    CARET449=(Token)match(input,CARET,FOLLOW_CARET_in_atom7618); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET449);



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
            	    // 600:15: -> ^( DEREF $atom)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:600:18: ^( DEREF $atom)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DEREF, "DEREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;
            	case 5 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:9: LBRACE ( PLUS )? type RBRACE
            	    {
            	    LBRACE450=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_atom7639); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE450);

            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:16: ( PLUS )?
            	    int alt128=2;
            	    int LA128_0 = input.LA(1);

            	    if ( (LA128_0==PLUS) ) {
            	        alt128=1;
            	    }
            	    switch (alt128) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:16: PLUS
            	            {
            	            PLUS451=(Token)match(input,PLUS,FOLLOW_PLUS_in_atom7641); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_PLUS.add(PLUS451);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_type_in_atom7644);
            	    type452=type();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_type.add(type452.getTree());
            	    RBRACE453=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_atom7646); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE453);



            	    // AST REWRITE
            	    // elements: atom, PLUS, type
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 601:34: -> ^( CAST ( PLUS )? type $atom)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:37: ^( CAST ( PLUS )? type $atom)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:601:44: ( PLUS )?
            	        if ( stream_PLUS.hasNext() ) {
            	            adaptor.addChild(root_1, stream_PLUS.nextNode());

            	        }
            	        stream_PLUS.reset();
            	        adaptor.addChild(root_1, stream_type.nextTree());
            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }


            	    }
            	    break;

            	default :
            	    break loop129;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:604:5: ( AS type -> ^( CAST type $atom) )?
            int alt130=2;
            int LA130_0 = input.LA(1);

            if ( (LA130_0==AS) ) {
                alt130=1;
            }
            switch (alt130) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:7: AS type
                    {
                    AS454=(Token)match(input,AS,FOLLOW_AS_in_atom7687); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AS.add(AS454);

                    pushFollow(FOLLOW_type_in_atom7689);
                    type455=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type455.getTree());


                    // AST REWRITE
                    // elements: atom, type
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 605:15: -> ^( CAST type $atom)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:18: ^( CAST type $atom)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

                        adaptor.addChild(root_1, stream_type.nextTree());
                        adaptor.addChild(root_1, stream_retval.nextTree());

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
    // $ANTLR end "atom"

    public static class arrayAccess_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "arrayAccess"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:609:1: arrayAccess : LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET -> ( assignExpr )+ ;
    public final EulangParser.arrayAccess_return arrayAccess() throws RecognitionException {
        EulangParser.arrayAccess_return retval = new EulangParser.arrayAccess_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET456=null;
        Token COMMA458=null;
        Token RBRACKET460=null;
        EulangParser.assignExpr_return assignExpr457 = null;

        EulangParser.assignExpr_return assignExpr459 = null;


        CommonTree LBRACKET456_tree=null;
        CommonTree COMMA458_tree=null;
        CommonTree RBRACKET460_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:609:13: ( LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:609:15: LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET
            {
            LBRACKET456=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayAccess7723); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET456);

            pushFollow(FOLLOW_assignExpr_in_arrayAccess7725);
            assignExpr457=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr457.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:609:35: ( COMMA assignExpr )*
            loop131:
            do {
                int alt131=2;
                int LA131_0 = input.LA(1);

                if ( (LA131_0==COMMA) ) {
                    alt131=1;
                }


                switch (alt131) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:609:36: COMMA assignExpr
            	    {
            	    COMMA458=(Token)match(input,COMMA,FOLLOW_COMMA_in_arrayAccess7728); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA458);

            	    pushFollow(FOLLOW_assignExpr_in_arrayAccess7730);
            	    assignExpr459=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr459.getTree());

            	    }
            	    break;

            	default :
            	    break loop131;
                }
            } while (true);

            RBRACKET460=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayAccess7734); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET460);



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
            // 609:65: -> ( assignExpr )+
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
    // $ANTLR end "arrayAccess"

    public static class idExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:1: idExpr : ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )? ;
    public final EulangParser.idExpr_return idExpr() throws RecognitionException {
        EulangParser.idExpr_return retval = new EulangParser.idExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PERIOD462=null;
        Token ID463=null;
        EulangParser.idOrScopeRef_return idOrScopeRef461 = null;

        EulangParser.instantiation_return instantiation464 = null;


        CommonTree PERIOD462_tree=null;
        CommonTree ID463_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_instantiation=new RewriteRuleSubtreeStream(adaptor,"rule instantiation");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:8: ( ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:5: ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:5: ( idOrScopeRef -> idOrScopeRef )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:7: idOrScopeRef
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idExpr7756);
            idOrScopeRef461=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef461.getTree());


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
            // 612:20: -> idOrScopeRef
            {
                adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:613:7: ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )*
            loop132:
            do {
                int alt132=2;
                int LA132_0 = input.LA(1);

                if ( (LA132_0==PERIOD) ) {
                    int LA132_2 = input.LA(2);

                    if ( (LA132_2==ID) ) {
                        alt132=1;
                    }


                }


                switch (alt132) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:613:9: PERIOD ID
            	    {
            	    PERIOD462=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idExpr7772); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD462);

            	    ID463=(Token)match(input,ID,FOLLOW_ID_in_idExpr7774); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID463);



            	    // AST REWRITE
            	    // elements: ID, idExpr
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 613:20: -> ^( FIELDREF $idExpr ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:613:23: ^( FIELDREF $idExpr ID )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FIELDREF, "FIELDREF"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_ID.nextNode());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;}
            	    }
            	    break;

            	default :
            	    break loop132;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:614:5: ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?
            int alt133=2;
            alt133 = dfa133.predict(input);
            switch (alt133) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:614:7: ( instantiation )=> instantiation
                    {
                    pushFollow(FOLLOW_instantiation_in_idExpr7804);
                    instantiation464=instantiation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_instantiation.add(instantiation464.getTree());


                    // AST REWRITE
                    // elements: idExpr, instantiation
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 614:41: -> ^( INSTANCE $idExpr instantiation )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:614:44: ^( INSTANCE $idExpr instantiation )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INSTANCE, "INSTANCE"), root_1);

                        adaptor.addChild(root_1, stream_retval.nextTree());
                        adaptor.addChild(root_1, stream_instantiation.nextTree());

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
    // $ANTLR end "idExpr"

    public static class namespaceRef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "namespaceRef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:616:1: namespaceRef : ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) );
    public final EulangParser.namespaceRef_return namespaceRef() throws RecognitionException {
        EulangParser.namespaceRef_return retval = new EulangParser.namespaceRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID465=null;
        Token PERIOD466=null;
        Token ID467=null;
        Token ID468=null;
        Token PERIOD469=null;
        Token ID470=null;
        EulangParser.colons_return c = null;


        CommonTree ID465_tree=null;
        CommonTree PERIOD466_tree=null;
        CommonTree ID467_tree=null;
        CommonTree ID468_tree=null;
        CommonTree PERIOD469_tree=null;
        CommonTree ID470_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:616:14: ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) )
            int alt136=2;
            int LA136_0 = input.LA(1);

            if ( (LA136_0==ID) ) {
                alt136=1;
            }
            else if ( (LA136_0==COLON||LA136_0==COLONS) ) {
                alt136=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 136, 0, input);

                throw nvae;
            }
            switch (alt136) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:617:5: ID ( PERIOD ID )*
                    {
                    ID465=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7835); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID465);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:617:8: ( PERIOD ID )*
                    loop134:
                    do {
                        int alt134=2;
                        int LA134_0 = input.LA(1);

                        if ( (LA134_0==PERIOD) ) {
                            alt134=1;
                        }


                        switch (alt134) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:617:9: PERIOD ID
                    	    {
                    	    PERIOD466=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_namespaceRef7838); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD466);

                    	    ID467=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7840); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID467);


                    	    }
                    	    break;

                    	default :
                    	    break loop134;
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
                    // 617:22: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:617:25: ^( IDREF ( ID )+ )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:7: c= colons ID ( PERIOD ID )*
                    {
                    pushFollow(FOLLOW_colons_in_namespaceRef7864);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID468=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7866); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID468);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:19: ( PERIOD ID )*
                    loop135:
                    do {
                        int alt135=2;
                        int LA135_0 = input.LA(1);

                        if ( (LA135_0==PERIOD) ) {
                            alt135=1;
                        }


                        switch (alt135) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:20: PERIOD ID
                    	    {
                    	    PERIOD469=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_namespaceRef7869); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD469);

                    	    ID470=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7871); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID470);


                    	    }
                    	    break;

                    	default :
                    	    break loop135;
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
                    // 618:33: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:36: ^( IDREF ( ID )+ )
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
    // $ANTLR end "namespaceRef"

    public static class instantiation_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "instantiation"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:1: instantiation : LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER -> ^( LIST ( instanceExpr )* ) ;
    public final EulangParser.instantiation_return instantiation() throws RecognitionException {
        EulangParser.instantiation_return retval = new EulangParser.instantiation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LESS471=null;
        Token COMMA473=null;
        Token GREATER475=null;
        EulangParser.instanceExpr_return instanceExpr472 = null;

        EulangParser.instanceExpr_return instanceExpr474 = null;


        CommonTree LESS471_tree=null;
        CommonTree COMMA473_tree=null;
        CommonTree GREATER475_tree=null;
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_instanceExpr=new RewriteRuleSubtreeStream(adaptor,"rule instanceExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:15: ( LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER -> ^( LIST ( instanceExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:17: LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER
            {
            LESS471=(Token)match(input,LESS,FOLLOW_LESS_in_instantiation7900); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LESS.add(LESS471);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:22: ( instanceExpr ( COMMA instanceExpr )* )?
            int alt138=2;
            int LA138_0 = input.LA(1);

            if ( (LA138_0==CODE||LA138_0==ID||LA138_0==COLON||LA138_0==LPAREN||LA138_0==NIL||(LA138_0>=NUMBER && LA138_0<=STRING_LITERAL)||(LA138_0>=FALSE && LA138_0<=DATA)) ) {
                alt138=1;
            }
            switch (alt138) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:23: instanceExpr ( COMMA instanceExpr )*
                    {
                    pushFollow(FOLLOW_instanceExpr_in_instantiation7903);
                    instanceExpr472=instanceExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_instanceExpr.add(instanceExpr472.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:36: ( COMMA instanceExpr )*
                    loop137:
                    do {
                        int alt137=2;
                        int LA137_0 = input.LA(1);

                        if ( (LA137_0==COMMA) ) {
                            alt137=1;
                        }


                        switch (alt137) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:37: COMMA instanceExpr
                    	    {
                    	    COMMA473=(Token)match(input,COMMA,FOLLOW_COMMA_in_instantiation7906); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA473);

                    	    pushFollow(FOLLOW_instanceExpr_in_instantiation7908);
                    	    instanceExpr474=instanceExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_instanceExpr.add(instanceExpr474.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop137;
                        }
                    } while (true);


                    }
                    break;

            }

            GREATER475=(Token)match(input,GREATER,FOLLOW_GREATER_in_instantiation7914); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GREATER.add(GREATER475);



            // AST REWRITE
            // elements: instanceExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 621:70: -> ^( LIST ( instanceExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:73: ^( LIST ( instanceExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:621:80: ( instanceExpr )*
                while ( stream_instanceExpr.hasNext() ) {
                    adaptor.addChild(root_1, stream_instanceExpr.nextTree());

                }
                stream_instanceExpr.reset();

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
    // $ANTLR end "instantiation"

    public static class instanceExpr_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "instanceExpr"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:1: instanceExpr options {backtrack=true; } : ( type | atom );
    public final EulangParser.instanceExpr_return instanceExpr() throws RecognitionException {
        EulangParser.instanceExpr_return retval = new EulangParser.instanceExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.type_return type476 = null;

        EulangParser.atom_return atom477 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:41: ( type | atom )
            int alt139=2;
            alt139 = dfa139.predict(input);
            switch (alt139) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:43: type
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_type_in_instanceExpr7946);
                    type476=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type476.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:50: atom
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_atom_in_instanceExpr7950);
                    atom477=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, atom477.getTree());

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
    // $ANTLR end "instanceExpr"

    public static class idOrScopeRef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "idOrScopeRef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:1: idOrScopeRef : ( ID -> ^( IDREF ID ) | c= colons ID -> ^( IDREF ID ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID478=null;
        Token ID479=null;
        EulangParser.colons_return c = null;


        CommonTree ID478_tree=null;
        CommonTree ID479_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:14: ( ID -> ^( IDREF ID ) | c= colons ID -> ^( IDREF ID ) )
            int alt140=2;
            int LA140_0 = input.LA(1);

            if ( (LA140_0==ID) ) {
                alt140=1;
            }
            else if ( (LA140_0==COLON||LA140_0==COLONS) ) {
                alt140=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 140, 0, input);

                throw nvae;
            }
            switch (alt140) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:16: ID
                    {
                    ID478=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef7958); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID478);



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
                    // 625:20: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:23: ^( IDREF ID )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:626:9: c= colons ID
                    {
                    pushFollow(FOLLOW_colons_in_idOrScopeRef7981);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID479=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef7983); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID479);



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
                    // 626:21: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:626:24: ^( IDREF ID )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IDREF, "IDREF"), root_1);

                        adaptor.addChild(root_1, split((c!=null?((CommonTree)c.tree):null)));
                        adaptor.addChild(root_1, stream_ID.nextNode());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:1: colons : ( COLON | COLONS )+ ;
    public final EulangParser.colons_return colons() throws RecognitionException {
        EulangParser.colons_return retval = new EulangParser.colons_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set480=null;

        CommonTree set480_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:8: ( ( COLON | COLONS )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:10: ( COLON | COLONS )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:10: ( COLON | COLONS )+
            int cnt141=0;
            loop141:
            do {
                int alt141=2;
                int LA141_0 = input.LA(1);

                if ( (LA141_0==COLON||LA141_0==COLONS) ) {
                    alt141=1;
                }


                switch (alt141) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            	    {
            	    set480=(Token)input.LT(1);
            	    if ( input.LA(1)==COLON||input.LA(1)==COLONS ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set480));
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
            	    if ( cnt141 >= 1 ) break loop141;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(141, input);
                        throw eee;
                }
                cnt141++;
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

    public static class data_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "data"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:1: data : DATA LBRACE ( fieldDecl )* RBRACE -> ^( DATA ( fieldDecl )* ) ;
    public final EulangParser.data_return data() throws RecognitionException {
        EulangParser.data_return retval = new EulangParser.data_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DATA481=null;
        Token LBRACE482=null;
        Token RBRACE484=null;
        EulangParser.fieldDecl_return fieldDecl483 = null;


        CommonTree DATA481_tree=null;
        CommonTree LBRACE482_tree=null;
        CommonTree RBRACE484_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_DATA=new RewriteRuleTokenStream(adaptor,"token DATA");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_fieldDecl=new RewriteRuleSubtreeStream(adaptor,"rule fieldDecl");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:6: ( DATA LBRACE ( fieldDecl )* RBRACE -> ^( DATA ( fieldDecl )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:8: DATA LBRACE ( fieldDecl )* RBRACE
            {
            DATA481=(Token)match(input,DATA,FOLLOW_DATA_in_data8026); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DATA.add(DATA481);

            LBRACE482=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_data8028); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE482);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:20: ( fieldDecl )*
            loop142:
            do {
                int alt142=2;
                int LA142_0 = input.LA(1);

                if ( ((LA142_0>=FORWARD && LA142_0<=ID)||LA142_0==LPAREN) ) {
                    alt142=1;
                }


                switch (alt142) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:20: fieldDecl
            	    {
            	    pushFollow(FOLLOW_fieldDecl_in_data8030);
            	    fieldDecl483=fieldDecl();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_fieldDecl.add(fieldDecl483.getTree());

            	    }
            	    break;

            	default :
            	    break loop142;
                }
            } while (true);

            RBRACE484=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_data8033); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE484);



            // AST REWRITE
            // elements: DATA, fieldDecl
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 631:39: -> ^( DATA ( fieldDecl )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:42: ^( DATA ( fieldDecl )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_DATA.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:631:49: ( fieldDecl )*
                while ( stream_fieldDecl.hasNext() ) {
                    adaptor.addChild(root_1, stream_fieldDecl.nextTree());

                }
                stream_fieldDecl.reset();

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
    // $ANTLR end "data"

    public static class fieldDecl_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldDecl"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:633:1: fieldDecl : ( varDecl SEMI -> varDecl | defineStmt | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | ID COLON_COLON_EQUALS toplevelvalue SEMI -> ^( REDEF ID toplevelvalue ) );
    public final EulangParser.fieldDecl_return fieldDecl() throws RecognitionException {
        EulangParser.fieldDecl_return retval = new EulangParser.fieldDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI486=null;
        Token FORWARD488=null;
        Token ID489=null;
        Token COMMA490=null;
        Token ID491=null;
        Token SEMI492=null;
        Token ID493=null;
        Token COLON_COLON_EQUALS494=null;
        Token SEMI496=null;
        EulangParser.varDecl_return varDecl485 = null;

        EulangParser.defineStmt_return defineStmt487 = null;

        EulangParser.toplevelvalue_return toplevelvalue495 = null;


        CommonTree SEMI486_tree=null;
        CommonTree FORWARD488_tree=null;
        CommonTree ID489_tree=null;
        CommonTree COMMA490_tree=null;
        CommonTree ID491_tree=null;
        CommonTree SEMI492_tree=null;
        CommonTree ID493_tree=null;
        CommonTree COLON_COLON_EQUALS494_tree=null;
        CommonTree SEMI496_tree=null;
        RewriteRuleTokenStream stream_COLON_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_COLON_EQUALS");
        RewriteRuleTokenStream stream_FORWARD=new RewriteRuleTokenStream(adaptor,"token FORWARD");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_toplevelvalue=new RewriteRuleSubtreeStream(adaptor,"rule toplevelvalue");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:633:11: ( varDecl SEMI -> varDecl | defineStmt | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | ID COLON_COLON_EQUALS toplevelvalue SEMI -> ^( REDEF ID toplevelvalue ) )
            int alt144=4;
            switch ( input.LA(1) ) {
            case ID:
                {
                switch ( input.LA(2) ) {
                case EQUALS:
                case EQUALS_COLON:
                    {
                    alt144=2;
                    }
                    break;
                case COLON_COLON_EQUALS:
                    {
                    alt144=4;
                    }
                    break;
                case COMMA:
                case COLON_EQUALS:
                case COLON:
                case ATTR:
                    {
                    alt144=1;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 144, 1, input);

                    throw nvae;
                }

                }
                break;
            case LPAREN:
                {
                alt144=1;
                }
                break;
            case FORWARD:
                {
                alt144=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 144, 0, input);

                throw nvae;
            }

            switch (alt144) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:633:13: varDecl SEMI
                    {
                    pushFollow(FOLLOW_varDecl_in_fieldDecl8052);
                    varDecl485=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl485.getTree());
                    SEMI486=(Token)match(input,SEMI,FOLLOW_SEMI_in_fieldDecl8054); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI486);



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
                    // 633:26: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:634:7: defineStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_defineStmt_in_fieldDecl8067);
                    defineStmt487=defineStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defineStmt487.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:635:7: FORWARD ID ( COMMA ID )* SEMI
                    {
                    FORWARD488=(Token)match(input,FORWARD,FOLLOW_FORWARD_in_fieldDecl8075); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FORWARD.add(FORWARD488);

                    ID489=(Token)match(input,ID,FOLLOW_ID_in_fieldDecl8077); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID489);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:635:18: ( COMMA ID )*
                    loop143:
                    do {
                        int alt143=2;
                        int LA143_0 = input.LA(1);

                        if ( (LA143_0==COMMA) ) {
                            alt143=1;
                        }


                        switch (alt143) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:635:19: COMMA ID
                    	    {
                    	    COMMA490=(Token)match(input,COMMA,FOLLOW_COMMA_in_fieldDecl8080); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA490);

                    	    ID491=(Token)match(input,ID,FOLLOW_ID_in_fieldDecl8082); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID491);


                    	    }
                    	    break;

                    	default :
                    	    break loop143;
                        }
                    } while (true);

                    SEMI492=(Token)match(input,SEMI,FOLLOW_SEMI_in_fieldDecl8086); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI492);



                    // AST REWRITE
                    // elements: FORWARD, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 635:35: -> ( ^( FORWARD ID ) )+
                    {
                        if ( !(stream_FORWARD.hasNext()||stream_ID.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_FORWARD.hasNext()||stream_ID.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:635:38: ^( FORWARD ID )
                            {
                            CommonTree root_1 = (CommonTree)adaptor.nil();
                            root_1 = (CommonTree)adaptor.becomeRoot(stream_FORWARD.nextNode(), root_1);

                            adaptor.addChild(root_1, stream_ID.nextNode());

                            adaptor.addChild(root_0, root_1);
                            }

                        }
                        stream_FORWARD.reset();
                        stream_ID.reset();

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:636:7: ID COLON_COLON_EQUALS toplevelvalue SEMI
                    {
                    ID493=(Token)match(input,ID,FOLLOW_ID_in_fieldDecl8103); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID493);

                    COLON_COLON_EQUALS494=(Token)match(input,COLON_COLON_EQUALS,FOLLOW_COLON_COLON_EQUALS_in_fieldDecl8105); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_COLON_EQUALS.add(COLON_COLON_EQUALS494);

                    pushFollow(FOLLOW_toplevelvalue_in_fieldDecl8107);
                    toplevelvalue495=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue495.getTree());
                    SEMI496=(Token)match(input,SEMI,FOLLOW_SEMI_in_fieldDecl8109); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI496);



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
                    // 636:48: -> ^( REDEF ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:636:51: ^( REDEF ID toplevelvalue )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(REDEF, "REDEF"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_toplevelvalue.nextTree());

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
    // $ANTLR end "fieldDecl"

    // $ANTLR start synpred1_Eulang
    public final void synpred1_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:7: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:8: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred1_Eulang640); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Eulang

    // $ANTLR start synpred2_Eulang
    public final void synpred2_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:14: ( ID EQUALS LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:15: ID EQUALS LBRACKET
        {
        match(input,ID,FOLLOW_ID_in_synpred2_Eulang1148); if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred2_Eulang1150); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred2_Eulang1152); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Eulang

    // $ANTLR start synpred3_Eulang
    public final void synpred3_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:7: ( ID EQUALS_COLON )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:8: ID EQUALS_COLON
        {
        match(input,ID,FOLLOW_ID_in_synpred3_Eulang1197); if (state.failed) return ;
        match(input,EQUALS_COLON,FOLLOW_EQUALS_COLON_in_synpred3_Eulang1199); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Eulang

    // $ANTLR start synpred4_Eulang
    public final void synpred4_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:7: ( ID EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:8: ID EQUALS
        {
        match(input,ID,FOLLOW_ID_in_synpred4_Eulang1234); if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred4_Eulang1236); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Eulang

    // $ANTLR start synpred5_Eulang
    public final void synpred5_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:17: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:185:18: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Eulang1274); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Eulang

    // $ANTLR start synpred7_Eulang
    public final void synpred7_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:5: ( argdefsWithTypes )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:242:5: argdefsWithTypes
        {
        pushFollow(FOLLOW_argdefsWithTypes_in_synpred7_Eulang1812);
        argdefsWithTypes();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Eulang

    // $ANTLR start synpred8_Eulang
    public final void synpred8_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:243:5: ( argdefWithType )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:243:5: argdefWithType
        {
        pushFollow(FOLLOW_argdefWithType_in_synpred8_Eulang1819);
        argdefWithType();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Eulang

    // $ANTLR start synpred9_Eulang
    public final void synpred9_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:9: ( ( arraySuff )+ )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:10: ( arraySuff )+
        {
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:10: ( arraySuff )+
        int cnt145=0;
        loop145:
        do {
            int alt145=2;
            int LA145_0 = input.LA(1);

            if ( (LA145_0==LBRACKET) ) {
                alt145=1;
            }


            switch (alt145) {
        	case 1 :
        	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:286:10: arraySuff
        	    {
        	    pushFollow(FOLLOW_arraySuff_in_synpred9_Eulang2287);
        	    arraySuff();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt145 >= 1 ) break loop145;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(145, input);
                    throw eee;
            }
            cnt145++;
        } while (true);


        }
    }
    // $ANTLR end synpred9_Eulang

    // $ANTLR start synpred10_Eulang
    public final void synpred10_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:317:7: ( varDecl )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:317:9: varDecl
        {
        pushFollow(FOLLOW_varDecl_in_synpred10_Eulang2686);
        varDecl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Eulang

    // $ANTLR start synpred11_Eulang
    public final void synpred11_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:9: ( assignStmt )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:318:10: assignStmt
        {
        pushFollow(FOLLOW_assignStmt_in_synpred11_Eulang2709);
        assignStmt();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Eulang

    // $ANTLR start synpred12_Eulang
    public final void synpred12_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:9: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:11: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Eulang2758); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Eulang

    // $ANTLR start synpred13_Eulang
    public final void synpred13_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:14: ( lhs assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:348:15: lhs assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred13_Eulang3258);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_assignEqOp_in_synpred13_Eulang3260);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Eulang

    // $ANTLR start synpred14_Eulang
    public final void synpred14_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:7: ( lhs ( COMMA lhs )+ assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:8: lhs ( COMMA lhs )+ assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred14_Eulang3340);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:12: ( COMMA lhs )+
        int cnt146=0;
        loop146:
        do {
            int alt146=2;
            int LA146_0 = input.LA(1);

            if ( (LA146_0==COMMA) ) {
                alt146=1;
            }


            switch (alt146) {
        	case 1 :
        	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:351:13: COMMA lhs
        	    {
        	    match(input,COMMA,FOLLOW_COMMA_in_synpred14_Eulang3343); if (state.failed) return ;
        	    pushFollow(FOLLOW_lhs_in_synpred14_Eulang3345);
        	    lhs();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt146 >= 1 ) break loop146;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(146, input);
                    throw eee;
            }
            cnt146++;
        } while (true);

        pushFollow(FOLLOW_assignEqOp_in_synpred14_Eulang3349);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Eulang

    // $ANTLR start synpred15_Eulang
    public final void synpred15_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:14: ( lhs assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:358:15: lhs assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred15_Eulang3450);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_assignEqOp_in_synpred15_Eulang3452);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Eulang

    // $ANTLR start synpred16_Eulang
    public final void synpred16_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:7: ( idTuple EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:8: idTuple EQUALS
        {
        pushFollow(FOLLOW_idTuple_in_synpred16_Eulang3489);
        idTuple();

        state._fsp--;
        if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred16_Eulang3491); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Eulang

    // $ANTLR start synpred17_Eulang
    public final void synpred17_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:7: ( rhsExpr )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:8: rhsExpr
        {
        pushFollow(FOLLOW_rhsExpr_in_synpred17_Eulang3704);
        rhsExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Eulang

    // $ANTLR start synpred18_Eulang
    public final void synpred18_Eulang_fragment() throws RecognitionException {   
        EulangParser.rhsExpr_return i = null;


        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:7: ( LBRACKET i= rhsExpr RBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:8: LBRACKET i= rhsExpr RBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred18_Eulang3836); if (state.failed) return ;
        pushFollow(FOLLOW_rhsExpr_in_synpred18_Eulang3840);
        i=rhsExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred18_Eulang3842); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Eulang

    // $ANTLR start synpred19_Eulang
    public final void synpred19_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:12: ( MINUS term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:14: MINUS term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred19_Eulang5929); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred19_Eulang5931);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Eulang

    // $ANTLR start synpred20_Eulang
    public final void synpred20_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:536:11: ( STAR unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:536:13: STAR unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred20_Eulang6022); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred20_Eulang6024);
        unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_Eulang

    // $ANTLR start synpred21_Eulang
    public final void synpred21_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:9: ( lhs PLUSPLUS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:11: lhs PLUSPLUS
        {
        pushFollow(FOLLOW_lhs_in_synpred21_Eulang6356);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_synpred21_Eulang6358); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred21_Eulang

    // $ANTLR start synpred22_Eulang
    public final void synpred22_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:9: ( lhs MINUSMINUS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:11: lhs MINUSMINUS
        {
        pushFollow(FOLLOW_lhs_in_synpred22_Eulang6389);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_synpred22_Eulang6391); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred22_Eulang

    // $ANTLR start synpred23_Eulang
    public final void synpred23_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:561:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:561:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred23_Eulang6648);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred23_Eulang

    // $ANTLR start synpred24_Eulang
    public final void synpred24_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:9: ( LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:11: LBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred24_Eulang6911); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred24_Eulang

    // $ANTLR start synpred25_Eulang
    public final void synpred25_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:589:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:589:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred25_Eulang7345);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred25_Eulang

    // $ANTLR start synpred26_Eulang
    public final void synpred26_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:590:9: ( LPAREN varDecl )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:590:11: LPAREN varDecl
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred26_Eulang7392); if (state.failed) return ;
        pushFollow(FOLLOW_varDecl_in_synpred26_Eulang7394);
        varDecl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred26_Eulang

    // $ANTLR start synpred27_Eulang
    public final void synpred27_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:9: ( LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:11: LBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred27_Eulang7587); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred27_Eulang

    // $ANTLR start synpred28_Eulang
    public final void synpred28_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:614:7: ( instantiation )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:614:8: instantiation
        {
        pushFollow(FOLLOW_instantiation_in_synpred28_Eulang7798);
        instantiation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred28_Eulang

    // $ANTLR start synpred29_Eulang
    public final void synpred29_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:43: ( type )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:624:43: type
        {
        pushFollow(FOLLOW_type_in_synpred29_Eulang7946);
        type();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred29_Eulang

    // Delegated rules

    public final boolean synpred18_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred18_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred17_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred17_Eulang_fragment(); // can never throw exception
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
    public final boolean synpred19_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred19_Eulang_fragment(); // can never throw exception
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
    public final boolean synpred25_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred25_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_Eulang_fragment(); // can never throw exception
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
    public final boolean synpred21_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred21_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred28_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred28_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred14_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred14_Eulang_fragment(); // can never throw exception
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
    public final boolean synpred22_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred22_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred15_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred15_Eulang_fragment(); // can never throw exception
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
    public final boolean synpred23_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred23_Eulang_fragment(); // can never throw exception
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
    public final boolean synpred24_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred24_Eulang_fragment(); // can never throw exception
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
    public final boolean synpred16_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred16_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred29_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred29_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred20_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred20_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred26_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred26_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred27_Eulang() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred27_Eulang_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA23 dfa23 = new DFA23(this);
    protected DFA24 dfa24 = new DFA24(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA62 dfa62 = new DFA62(this);
    protected DFA75 dfa75 = new DFA75(this);
    protected DFA82 dfa82 = new DFA82(this);
    protected DFA84 dfa84 = new DFA84(this);
    protected DFA88 dfa88 = new DFA88(this);
    protected DFA121 dfa121 = new DFA121(this);
    protected DFA127 dfa127 = new DFA127(this);
    protected DFA133 dfa133 = new DFA133(this);
    protected DFA139 dfa139 = new DFA139(this);
    static final String DFA23_eotS =
        "\u00a4\uffff";
    static final String DFA23_eofS =
        "\u00a4\uffff";
    static final String DFA23_minS =
        "\1\106\1\112\1\11\1\uffff\1\11\25\uffff\1\45\1\11\66\uffff\1\11"+
        "\30\uffff\1\45\70\uffff";
    static final String DFA23_maxS =
        "\1\106\1\123\1\u009a\1\uffff\1\u0099\25\uffff\1\u0096\1\u009a\66"+
        "\uffff\1\u0099\30\uffff\1\u0096\70\uffff";
    static final String DFA23_acceptS =
        "\3\uffff\1\2\1\uffff\25\3\2\uffff\66\3\1\uffff\2\3\26\1\1\uffff"+
        "\70\3";
    static final String DFA23_specialS =
        "\1\uffff\1\4\1\6\1\uffff\1\5\25\uffff\1\3\1\1\66\uffff\1\0\30\uffff"+
        "\1\2\70\uffff}>";
    static final String[] DFA23_transitionS = {
            "\1\1",
            "\1\2\10\uffff\1\3",
            "\1\21\64\uffff\1\25\1\26\6\uffff\1\6\2\uffff\1\7\2\uffff\1"+
            "\12\1\5\3\uffff\1\4\6\uffff\1\20\32\uffff\1\30\5\uffff\1\27"+
            "\13\uffff\1\11\1\24\5\uffff\1\10\4\uffff\1\22\1\23\1\13\1\14"+
            "\1\15\1\uffff\1\16\1\17\1\7\1\31",
            "",
            "\1\46\64\uffff\1\52\1\53\6\uffff\1\32\2\uffff\1\36\2\uffff"+
            "\1\37\5\uffff\1\33\5\uffff\1\45\32\uffff\1\55\5\uffff\1\54\13"+
            "\uffff\1\35\1\51\5\uffff\1\34\4\uffff\1\47\1\50\1\40\1\41\1"+
            "\42\1\uffff\1\43\1\44\1\36",
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
            "\1\71\1\73\40\uffff\1\122\3\uffff\1\74\1\60\1\63\3\uffff\1"+
            "\61\1\33\6\uffff\1\121\1\62\17\uffff\1\56\3\uffff\1\117\11\uffff"+
            "\1\120\1\uffff\1\106\1\107\1\110\1\111\1\112\1\113\1\57\1\114"+
            "\1\115\1\116\1\105\1\104\1\103\1\76\1\77\1\100\1\101\1\102\1"+
            "\75\1\66\1\67\1\70\1\72\1\65\1\123\3\uffff\1\64",
            "\1\142\64\uffff\1\146\1\147\4\uffff\1\124\1\uffff\1\126\2\uffff"+
            "\1\127\2\uffff\1\133\1\125\3\uffff\1\130\6\uffff\1\141\32\uffff"+
            "\1\151\5\uffff\1\150\13\uffff\1\132\1\145\5\uffff\1\131\4\uffff"+
            "\1\143\1\144\1\134\1\135\1\136\1\uffff\1\137\1\140\1\127\1\152",
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
            "\1\167\64\uffff\1\173\1\174\6\uffff\1\153\2\uffff\1\157\2\uffff"+
            "\1\160\5\uffff\1\154\5\uffff\1\166\32\uffff\1\176\5\uffff\1"+
            "\175\13\uffff\1\156\1\172\5\uffff\1\155\4\uffff\1\170\1\171"+
            "\1\161\1\162\1\163\1\uffff\1\164\1\165\1\157",
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
            "\1\u0089\1\u008b\40\uffff\1\122\3\uffff\1\u008c\1\u0081\1\u0084"+
            "\3\uffff\1\u0082\1\33\6\uffff\1\u00a1\1\u0083\17\uffff\1\177"+
            "\3\uffff\1\u009f\11\uffff\1\u00a0\1\uffff\1\u0096\1\u0097\1"+
            "\u0098\1\u0099\1\u009a\1\u009b\1\u0080\1\u009c\1\u009d\1\u009e"+
            "\1\u0095\1\u0094\1\u0093\1\u008e\1\u008f\1\u0090\1\u0091\1\u0092"+
            "\1\u008d\1\u0086\1\u0087\1\u0088\1\u008a\1\u00a2\1\u00a3\3\uffff"+
            "\1\u0085",
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

    static final short[] DFA23_eot = DFA.unpackEncodedString(DFA23_eotS);
    static final short[] DFA23_eof = DFA.unpackEncodedString(DFA23_eofS);
    static final char[] DFA23_min = DFA.unpackEncodedStringToUnsignedChars(DFA23_minS);
    static final char[] DFA23_max = DFA.unpackEncodedStringToUnsignedChars(DFA23_maxS);
    static final short[] DFA23_accept = DFA.unpackEncodedString(DFA23_acceptS);
    static final short[] DFA23_special = DFA.unpackEncodedString(DFA23_specialS);
    static final short[][] DFA23_transition;

    static {
        int numStates = DFA23_transitionS.length;
        DFA23_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA23_transition[i] = DFA.unpackEncodedString(DFA23_transitionS[i]);
        }
    }

    class DFA23 extends DFA {

        public DFA23(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 23;
            this.eot = DFA23_eot;
            this.eof = DFA23_eof;
            this.min = DFA23_min;
            this.max = DFA23_max;
            this.accept = DFA23_accept;
            this.special = DFA23_special;
            this.transition = DFA23_transition;
        }
        public String getDescription() {
            return "180:1: defineStmt : ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA23_82 = input.LA(1);

                         
                        int index23_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_82==ID) ) {s = 107;}

                        else if ( (LA23_82==RBRACKET) && (synpred4_Eulang())) {s = 108;}

                        else if ( (LA23_82==MINUS) && (synpred4_Eulang())) {s = 109;}

                        else if ( (LA23_82==TILDE) && (synpred4_Eulang())) {s = 110;}

                        else if ( (LA23_82==COLON||LA23_82==COLONS) && (synpred4_Eulang())) {s = 111;}

                        else if ( (LA23_82==LPAREN) && (synpred4_Eulang())) {s = 112;}

                        else if ( (LA23_82==NUMBER) && (synpred4_Eulang())) {s = 113;}

                        else if ( (LA23_82==CHAR_LITERAL) && (synpred4_Eulang())) {s = 114;}

                        else if ( (LA23_82==STRING_LITERAL) && (synpred4_Eulang())) {s = 115;}

                        else if ( (LA23_82==FALSE) && (synpred4_Eulang())) {s = 116;}

                        else if ( (LA23_82==TRUE) && (synpred4_Eulang())) {s = 117;}

                        else if ( (LA23_82==NIL) && (synpred4_Eulang())) {s = 118;}

                        else if ( (LA23_82==CODE) && (synpred4_Eulang())) {s = 119;}

                        else if ( (LA23_82==PLUSPLUS) && (synpred4_Eulang())) {s = 120;}

                        else if ( (LA23_82==MINUSMINUS) && (synpred4_Eulang())) {s = 121;}

                        else if ( (LA23_82==AMP) && (synpred4_Eulang())) {s = 122;}

                        else if ( (LA23_82==SIZEOF) && (synpred4_Eulang())) {s = 123;}

                        else if ( (LA23_82==TYPEOF) && (synpred4_Eulang())) {s = 124;}

                        else if ( (LA23_82==NOT) && (synpred4_Eulang())) {s = 125;}

                        else if ( (LA23_82==IF) && (synpred4_Eulang())) {s = 126;}

                         
                        input.seek(index23_82);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA23_27 = input.LA(1);

                         
                        int index23_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_27==SEMI) && (synpred4_Eulang())) {s = 84;}

                        else if ( (LA23_27==LBRACE) && (synpred2_Eulang())) {s = 85;}

                        else if ( (LA23_27==ID) && (synpred2_Eulang())) {s = 86;}

                        else if ( (LA23_27==COLON||LA23_27==COLONS) && (synpred2_Eulang())) {s = 87;}

                        else if ( (LA23_27==LBRACKET) && (synpred2_Eulang())) {s = 88;}

                        else if ( (LA23_27==MINUS) && (synpred2_Eulang())) {s = 89;}

                        else if ( (LA23_27==TILDE) && (synpred2_Eulang())) {s = 90;}

                        else if ( (LA23_27==LPAREN) && (synpred2_Eulang())) {s = 91;}

                        else if ( (LA23_27==NUMBER) && (synpred2_Eulang())) {s = 92;}

                        else if ( (LA23_27==CHAR_LITERAL) && (synpred2_Eulang())) {s = 93;}

                        else if ( (LA23_27==STRING_LITERAL) && (synpred2_Eulang())) {s = 94;}

                        else if ( (LA23_27==FALSE) && (synpred2_Eulang())) {s = 95;}

                        else if ( (LA23_27==TRUE) && (synpred2_Eulang())) {s = 96;}

                        else if ( (LA23_27==NIL) && (synpred2_Eulang())) {s = 97;}

                        else if ( (LA23_27==CODE) && (synpred2_Eulang())) {s = 98;}

                        else if ( (LA23_27==PLUSPLUS) && (synpred2_Eulang())) {s = 99;}

                        else if ( (LA23_27==MINUSMINUS) && (synpred2_Eulang())) {s = 100;}

                        else if ( (LA23_27==AMP) && (synpred2_Eulang())) {s = 101;}

                        else if ( (LA23_27==SIZEOF) && (synpred2_Eulang())) {s = 102;}

                        else if ( (LA23_27==TYPEOF) && (synpred2_Eulang())) {s = 103;}

                        else if ( (LA23_27==NOT) && (synpred2_Eulang())) {s = 104;}

                        else if ( (LA23_27==IF) && (synpred2_Eulang())) {s = 105;}

                        else if ( (LA23_27==DATA) && (synpred2_Eulang())) {s = 106;}

                         
                        input.seek(index23_27);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA23_107 = input.LA(1);

                         
                        int index23_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_107==PERIOD) && (synpred4_Eulang())) {s = 127;}

                        else if ( (LA23_107==LESS) && (synpred4_Eulang())) {s = 128;}

                        else if ( (LA23_107==LPAREN) && (synpred4_Eulang())) {s = 129;}

                        else if ( (LA23_107==LBRACKET) && (synpred4_Eulang())) {s = 130;}

                        else if ( (LA23_107==CARET) && (synpred4_Eulang())) {s = 131;}

                        else if ( (LA23_107==LBRACE) && (synpred4_Eulang())) {s = 132;}

                        else if ( (LA23_107==AS) && (synpred4_Eulang())) {s = 133;}

                        else if ( (LA23_107==STAR) && (synpred4_Eulang())) {s = 134;}

                        else if ( (LA23_107==SLASH) && (synpred4_Eulang())) {s = 135;}

                        else if ( (LA23_107==REM) && (synpred4_Eulang())) {s = 136;}

                        else if ( (LA23_107==UDIV) && (synpred4_Eulang())) {s = 137;}

                        else if ( (LA23_107==UREM) && (synpred4_Eulang())) {s = 138;}

                        else if ( (LA23_107==MOD) && (synpred4_Eulang())) {s = 139;}

                        else if ( (LA23_107==PLUS) && (synpred4_Eulang())) {s = 140;}

                        else if ( (LA23_107==MINUS) && (synpred4_Eulang())) {s = 141;}

                        else if ( (LA23_107==LSHIFT) && (synpred4_Eulang())) {s = 142;}

                        else if ( (LA23_107==RSHIFT) && (synpred4_Eulang())) {s = 143;}

                        else if ( (LA23_107==URSHIFT) && (synpred4_Eulang())) {s = 144;}

                        else if ( (LA23_107==CRSHIFT) && (synpred4_Eulang())) {s = 145;}

                        else if ( (LA23_107==CLSHIFT) && (synpred4_Eulang())) {s = 146;}

                        else if ( (LA23_107==AMP) && (synpred4_Eulang())) {s = 147;}

                        else if ( (LA23_107==TILDE) && (synpred4_Eulang())) {s = 148;}

                        else if ( (LA23_107==BAR) && (synpred4_Eulang())) {s = 149;}

                        else if ( (LA23_107==COMPEQ) && (synpred4_Eulang())) {s = 150;}

                        else if ( (LA23_107==COMPNE) && (synpred4_Eulang())) {s = 151;}

                        else if ( (LA23_107==COMPLE) && (synpred4_Eulang())) {s = 152;}

                        else if ( (LA23_107==COMPGE) && (synpred4_Eulang())) {s = 153;}

                        else if ( (LA23_107==COMPULE) && (synpred4_Eulang())) {s = 154;}

                        else if ( (LA23_107==COMPUGE) && (synpred4_Eulang())) {s = 155;}

                        else if ( (LA23_107==ULESS) && (synpred4_Eulang())) {s = 156;}

                        else if ( (LA23_107==GREATER) && (synpred4_Eulang())) {s = 157;}

                        else if ( (LA23_107==UGREATER) && (synpred4_Eulang())) {s = 158;}

                        else if ( (LA23_107==AND) && (synpred4_Eulang())) {s = 159;}

                        else if ( (LA23_107==OR) && (synpred4_Eulang())) {s = 160;}

                        else if ( (LA23_107==QUESTION) && (synpred4_Eulang())) {s = 161;}

                        else if ( (LA23_107==COMMA) ) {s = 82;}

                        else if ( (LA23_107==RBRACKET) ) {s = 27;}

                        else if ( (LA23_107==PLUSPLUS) && (synpred4_Eulang())) {s = 162;}

                        else if ( (LA23_107==MINUSMINUS) && (synpred4_Eulang())) {s = 163;}

                         
                        input.seek(index23_107);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA23_26 = input.LA(1);

                         
                        int index23_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_26==PERIOD) && (synpred4_Eulang())) {s = 46;}

                        else if ( (LA23_26==LESS) && (synpred4_Eulang())) {s = 47;}

                        else if ( (LA23_26==LPAREN) && (synpred4_Eulang())) {s = 48;}

                        else if ( (LA23_26==LBRACKET) && (synpred4_Eulang())) {s = 49;}

                        else if ( (LA23_26==CARET) && (synpred4_Eulang())) {s = 50;}

                        else if ( (LA23_26==LBRACE) && (synpred4_Eulang())) {s = 51;}

                        else if ( (LA23_26==AS) && (synpred4_Eulang())) {s = 52;}

                        else if ( (LA23_26==PLUSPLUS) && (synpred4_Eulang())) {s = 53;}

                        else if ( (LA23_26==STAR) && (synpred4_Eulang())) {s = 54;}

                        else if ( (LA23_26==SLASH) && (synpred4_Eulang())) {s = 55;}

                        else if ( (LA23_26==REM) && (synpred4_Eulang())) {s = 56;}

                        else if ( (LA23_26==UDIV) && (synpred4_Eulang())) {s = 57;}

                        else if ( (LA23_26==UREM) && (synpred4_Eulang())) {s = 58;}

                        else if ( (LA23_26==MOD) && (synpred4_Eulang())) {s = 59;}

                        else if ( (LA23_26==PLUS) && (synpred4_Eulang())) {s = 60;}

                        else if ( (LA23_26==MINUS) && (synpred4_Eulang())) {s = 61;}

                        else if ( (LA23_26==LSHIFT) && (synpred4_Eulang())) {s = 62;}

                        else if ( (LA23_26==RSHIFT) && (synpred4_Eulang())) {s = 63;}

                        else if ( (LA23_26==URSHIFT) && (synpred4_Eulang())) {s = 64;}

                        else if ( (LA23_26==CRSHIFT) && (synpred4_Eulang())) {s = 65;}

                        else if ( (LA23_26==CLSHIFT) && (synpred4_Eulang())) {s = 66;}

                        else if ( (LA23_26==AMP) && (synpred4_Eulang())) {s = 67;}

                        else if ( (LA23_26==TILDE) && (synpred4_Eulang())) {s = 68;}

                        else if ( (LA23_26==BAR) && (synpred4_Eulang())) {s = 69;}

                        else if ( (LA23_26==COMPEQ) && (synpred4_Eulang())) {s = 70;}

                        else if ( (LA23_26==COMPNE) && (synpred4_Eulang())) {s = 71;}

                        else if ( (LA23_26==COMPLE) && (synpred4_Eulang())) {s = 72;}

                        else if ( (LA23_26==COMPGE) && (synpred4_Eulang())) {s = 73;}

                        else if ( (LA23_26==COMPULE) && (synpred4_Eulang())) {s = 74;}

                        else if ( (LA23_26==COMPUGE) && (synpred4_Eulang())) {s = 75;}

                        else if ( (LA23_26==ULESS) && (synpred4_Eulang())) {s = 76;}

                        else if ( (LA23_26==GREATER) && (synpred4_Eulang())) {s = 77;}

                        else if ( (LA23_26==UGREATER) && (synpred4_Eulang())) {s = 78;}

                        else if ( (LA23_26==AND) && (synpred4_Eulang())) {s = 79;}

                        else if ( (LA23_26==OR) && (synpred4_Eulang())) {s = 80;}

                        else if ( (LA23_26==QUESTION) && (synpred4_Eulang())) {s = 81;}

                        else if ( (LA23_26==COMMA) ) {s = 82;}

                        else if ( (LA23_26==RBRACKET) ) {s = 27;}

                        else if ( (LA23_26==MINUSMINUS) && (synpred4_Eulang())) {s = 83;}

                         
                        input.seek(index23_26);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA23_1 = input.LA(1);

                         
                        int index23_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_1==EQUALS) ) {s = 2;}

                        else if ( (LA23_1==EQUALS_COLON) && (synpred3_Eulang())) {s = 3;}

                         
                        input.seek(index23_1);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA23_4 = input.LA(1);

                         
                        int index23_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_4==ID) ) {s = 26;}

                        else if ( (LA23_4==RBRACKET) ) {s = 27;}

                        else if ( (LA23_4==MINUS) && (synpred4_Eulang())) {s = 28;}

                        else if ( (LA23_4==TILDE) && (synpred4_Eulang())) {s = 29;}

                        else if ( (LA23_4==COLON||LA23_4==COLONS) && (synpred4_Eulang())) {s = 30;}

                        else if ( (LA23_4==LPAREN) && (synpred4_Eulang())) {s = 31;}

                        else if ( (LA23_4==NUMBER) && (synpred4_Eulang())) {s = 32;}

                        else if ( (LA23_4==CHAR_LITERAL) && (synpred4_Eulang())) {s = 33;}

                        else if ( (LA23_4==STRING_LITERAL) && (synpred4_Eulang())) {s = 34;}

                        else if ( (LA23_4==FALSE) && (synpred4_Eulang())) {s = 35;}

                        else if ( (LA23_4==TRUE) && (synpred4_Eulang())) {s = 36;}

                        else if ( (LA23_4==NIL) && (synpred4_Eulang())) {s = 37;}

                        else if ( (LA23_4==CODE) && (synpred4_Eulang())) {s = 38;}

                        else if ( (LA23_4==PLUSPLUS) && (synpred4_Eulang())) {s = 39;}

                        else if ( (LA23_4==MINUSMINUS) && (synpred4_Eulang())) {s = 40;}

                        else if ( (LA23_4==AMP) && (synpred4_Eulang())) {s = 41;}

                        else if ( (LA23_4==SIZEOF) && (synpred4_Eulang())) {s = 42;}

                        else if ( (LA23_4==TYPEOF) && (synpred4_Eulang())) {s = 43;}

                        else if ( (LA23_4==NOT) && (synpred4_Eulang())) {s = 44;}

                        else if ( (LA23_4==IF) && (synpred4_Eulang())) {s = 45;}

                         
                        input.seek(index23_4);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA23_2 = input.LA(1);

                         
                        int index23_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA23_2==LBRACKET) ) {s = 4;}

                        else if ( (LA23_2==LBRACE) && (synpred4_Eulang())) {s = 5;}

                        else if ( (LA23_2==ID) && (synpred4_Eulang())) {s = 6;}

                        else if ( (LA23_2==COLON||LA23_2==COLONS) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA23_2==MINUS) && (synpred4_Eulang())) {s = 8;}

                        else if ( (LA23_2==TILDE) && (synpred4_Eulang())) {s = 9;}

                        else if ( (LA23_2==LPAREN) && (synpred4_Eulang())) {s = 10;}

                        else if ( (LA23_2==NUMBER) && (synpred4_Eulang())) {s = 11;}

                        else if ( (LA23_2==CHAR_LITERAL) && (synpred4_Eulang())) {s = 12;}

                        else if ( (LA23_2==STRING_LITERAL) && (synpred4_Eulang())) {s = 13;}

                        else if ( (LA23_2==FALSE) && (synpred4_Eulang())) {s = 14;}

                        else if ( (LA23_2==TRUE) && (synpred4_Eulang())) {s = 15;}

                        else if ( (LA23_2==NIL) && (synpred4_Eulang())) {s = 16;}

                        else if ( (LA23_2==CODE) && (synpred4_Eulang())) {s = 17;}

                        else if ( (LA23_2==PLUSPLUS) && (synpred4_Eulang())) {s = 18;}

                        else if ( (LA23_2==MINUSMINUS) && (synpred4_Eulang())) {s = 19;}

                        else if ( (LA23_2==AMP) && (synpred4_Eulang())) {s = 20;}

                        else if ( (LA23_2==SIZEOF) && (synpred4_Eulang())) {s = 21;}

                        else if ( (LA23_2==TYPEOF) && (synpred4_Eulang())) {s = 22;}

                        else if ( (LA23_2==NOT) && (synpred4_Eulang())) {s = 23;}

                        else if ( (LA23_2==IF) && (synpred4_Eulang())) {s = 24;}

                        else if ( (LA23_2==DATA) && (synpred4_Eulang())) {s = 25;}

                         
                        input.seek(index23_2);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 23, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA24_eotS =
        "\17\uffff";
    static final String DFA24_eofS =
        "\17\uffff";
    static final String DFA24_minS =
        "\1\11\1\uffff\1\45\1\106\3\uffff\1\106\1\11\2\45\2\uffff\1\106\1"+
        "\45";
    static final String DFA24_maxS =
        "\1\u009a\1\uffff\1\u0096\1\u0099\3\uffff\1\106\1\u009a\2\u0096\2"+
        "\uffff\1\106\1\u0096";
    static final String DFA24_acceptS =
        "\1\uffff\1\1\2\uffff\1\4\1\5\1\6\4\uffff\1\3\1\2\2\uffff";
    static final String DFA24_specialS =
        "\1\0\16\uffff}>";
    static final String[] DFA24_transitionS = {
            "\1\5\64\uffff\2\5\6\uffff\1\2\2\uffff\1\3\2\uffff\1\5\1\1\3"+
            "\uffff\1\4\6\uffff\1\5\32\uffff\1\5\5\uffff\1\5\13\uffff\2\5"+
            "\5\uffff\1\5\4\uffff\5\5\1\uffff\2\5\1\3\1\6",
            "",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\2\5\3\uffff\2\5"+
            "\6\uffff\2\5\17\uffff\1\7\3\uffff\1\5\11\uffff\1\5\1\uffff\31"+
            "\5\3\uffff\1\5",
            "\1\11\2\uffff\1\3\117\uffff\1\3",
            "",
            "",
            "",
            "\1\12",
            "\1\5\64\uffff\2\5\6\uffff\1\5\2\uffff\1\5\2\uffff\1\5\1\13"+
            "\12\uffff\1\5\54\uffff\2\5\5\uffff\1\5\4\uffff\5\5\1\uffff\3"+
            "\5\1\14",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\2\5\3\uffff\2\5"+
            "\6\uffff\2\5\17\uffff\1\15\3\uffff\1\5\11\uffff\1\5\1\uffff"+
            "\31\5\3\uffff\1\5",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\2\5\3\uffff\2\5"+
            "\6\uffff\2\5\17\uffff\1\7\3\uffff\1\5\11\uffff\1\5\1\uffff\31"+
            "\5\3\uffff\1\5",
            "",
            "",
            "\1\16",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\2\5\3\uffff\2\5"+
            "\6\uffff\2\5\17\uffff\1\15\3\uffff\1\5\11\uffff\1\5\1\uffff"+
            "\31\5\3\uffff\1\5"
    };

    static final short[] DFA24_eot = DFA.unpackEncodedString(DFA24_eotS);
    static final short[] DFA24_eof = DFA.unpackEncodedString(DFA24_eofS);
    static final char[] DFA24_min = DFA.unpackEncodedStringToUnsignedChars(DFA24_minS);
    static final char[] DFA24_max = DFA.unpackEncodedStringToUnsignedChars(DFA24_maxS);
    static final short[] DFA24_accept = DFA.unpackEncodedString(DFA24_acceptS);
    static final short[] DFA24_special = DFA.unpackEncodedString(DFA24_specialS);
    static final short[][] DFA24_transition;

    static {
        int numStates = DFA24_transitionS.length;
        DFA24_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA24_transition[i] = DFA.unpackEncodedString(DFA24_transitionS[i]);
        }
    }

    class DFA24 extends DFA {

        public DFA24(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 24;
            this.eot = DFA24_eot;
            this.eof = DFA24_eof;
            this.min = DFA24_min;
            this.max = DFA24_max;
            this.accept = DFA24_accept;
            this.special = DFA24_special;
            this.transition = DFA24_transition;
        }
        public String getDescription() {
            return "185:1: toplevelvalue : ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA24_0 = input.LA(1);

                         
                        int index24_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA24_0==LBRACE) && (synpred5_Eulang())) {s = 1;}

                        else if ( (LA24_0==ID) ) {s = 2;}

                        else if ( (LA24_0==COLON||LA24_0==COLONS) ) {s = 3;}

                        else if ( (LA24_0==LBRACKET) ) {s = 4;}

                        else if ( (LA24_0==CODE||(LA24_0>=SIZEOF && LA24_0<=TYPEOF)||LA24_0==LPAREN||LA24_0==NIL||LA24_0==IF||LA24_0==NOT||(LA24_0>=TILDE && LA24_0<=AMP)||LA24_0==MINUS||(LA24_0>=PLUSPLUS && LA24_0<=STRING_LITERAL)||(LA24_0>=FALSE && LA24_0<=TRUE)) ) {s = 5;}

                        else if ( (LA24_0==DATA) ) {s = 6;}

                         
                        input.seek(index24_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 24, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA53_eotS =
        "\65\uffff";
    static final String DFA53_eofS =
        "\1\1\64\uffff";
    static final String DFA53_minS =
        "\1\45\60\uffff\1\0\3\uffff";
    static final String DFA53_maxS =
        "\1\u0092\60\uffff\1\0\3\uffff";
    static final String DFA53_acceptS =
        "\1\uffff\1\4\60\uffff\1\3\1\1\1\2";
    static final String DFA53_specialS =
        "\61\uffff\1\0\3\uffff}>";
    static final String[] DFA53_transitionS = {
            "\2\1\35\uffff\1\1\2\uffff\1\1\1\uffff\3\1\2\uffff\3\1\1\61\1"+
            "\1\2\uffff\1\1\1\uffff\1\1\1\uffff\1\1\1\62\17\1\1\uffff\2\1"+
            "\1\uffff\1\1\5\uffff\5\1\1\uffff\31\1",
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
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA53_eot = DFA.unpackEncodedString(DFA53_eotS);
    static final short[] DFA53_eof = DFA.unpackEncodedString(DFA53_eofS);
    static final char[] DFA53_min = DFA.unpackEncodedStringToUnsignedChars(DFA53_minS);
    static final char[] DFA53_max = DFA.unpackEncodedStringToUnsignedChars(DFA53_maxS);
    static final short[] DFA53_accept = DFA.unpackEncodedString(DFA53_acceptS);
    static final short[] DFA53_special = DFA.unpackEncodedString(DFA53_specialS);
    static final short[][] DFA53_transition;

    static {
        int numStates = DFA53_transitionS.length;
        DFA53_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA53_transition[i] = DFA.unpackEncodedString(DFA53_transitionS[i]);
        }
    }

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = DFA53_eot;
            this.eof = DFA53_eof;
            this.min = DFA53_min;
            this.max = DFA53_max;
            this.accept = DFA53_accept;
            this.special = DFA53_special;
            this.transition = DFA53_transition;
        }
        public String getDescription() {
            return "()* loopback of 284:6: ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA53_49 = input.LA(1);

                         
                        int index53_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Eulang()) ) {s = 51;}

                        else if ( (true) ) {s = 52;}

                         
                        input.seek(index53_49);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 53, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA62_eotS =
        "\34\uffff";
    static final String DFA62_eofS =
        "\34\uffff";
    static final String DFA62_minS =
        "\1\11\6\0\25\uffff";
    static final String DFA62_maxS =
        "\1\u0099\6\0\25\uffff";
    static final String DFA62_acceptS =
        "\7\uffff\1\3\14\uffff\1\4\1\5\1\6\3\uffff\1\1\1\2";
    static final String DFA62_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\25\uffff}>";
    static final String[] DFA62_transitionS = {
            "\1\7\47\uffff\1\25\14\uffff\2\7\6\uffff\1\1\2\uffff\1\3\2\uffff"+
            "\1\2\1\24\6\uffff\1\26\3\uffff\1\7\22\uffff\3\26\5\uffff\1\7"+
            "\5\uffff\1\7\13\uffff\2\7\5\uffff\1\7\4\uffff\2\7\1\4\1\5\1"+
            "\6\1\uffff\2\7\1\3",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            ""
    };

    static final short[] DFA62_eot = DFA.unpackEncodedString(DFA62_eotS);
    static final short[] DFA62_eof = DFA.unpackEncodedString(DFA62_eofS);
    static final char[] DFA62_min = DFA.unpackEncodedStringToUnsignedChars(DFA62_minS);
    static final char[] DFA62_max = DFA.unpackEncodedStringToUnsignedChars(DFA62_maxS);
    static final short[] DFA62_accept = DFA.unpackEncodedString(DFA62_acceptS);
    static final short[] DFA62_special = DFA.unpackEncodedString(DFA62_specialS);
    static final short[][] DFA62_transition;

    static {
        int numStates = DFA62_transitionS.length;
        DFA62_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA62_transition[i] = DFA.unpackEncodedString(DFA62_transitionS[i]);
        }
    }

    class DFA62 extends DFA {

        public DFA62(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 62;
            this.eot = DFA62_eot;
            this.eof = DFA62_eof;
            this.min = DFA62_min;
            this.max = DFA62_max;
            this.accept = DFA62_accept;
            this.special = DFA62_special;
            this.transition = DFA62_transition;
        }
        public String getDescription() {
            return "316:1: codeStmtExpr : ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA62_0 = input.LA(1);

                         
                        int index62_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA62_0==ID) ) {s = 1;}

                        else if ( (LA62_0==LPAREN) ) {s = 2;}

                        else if ( (LA62_0==COLON||LA62_0==COLONS) ) {s = 3;}

                        else if ( (LA62_0==NUMBER) ) {s = 4;}

                        else if ( (LA62_0==CHAR_LITERAL) ) {s = 5;}

                        else if ( (LA62_0==STRING_LITERAL) ) {s = 6;}

                        else if ( (LA62_0==CODE||(LA62_0>=SIZEOF && LA62_0<=TYPEOF)||LA62_0==NIL||LA62_0==IF||LA62_0==NOT||(LA62_0>=TILDE && LA62_0<=AMP)||LA62_0==MINUS||(LA62_0>=PLUSPLUS && LA62_0<=MINUSMINUS)||(LA62_0>=FALSE && LA62_0<=TRUE)) ) {s = 7;}

                        else if ( (LA62_0==LBRACE) && (synpred12_Eulang())) {s = 20;}

                        else if ( (LA62_0==GOTO) ) {s = 21;}

                        else if ( (LA62_0==FOR||(LA62_0>=DO && LA62_0<=REPEAT)) ) {s = 22;}

                         
                        input.seek(index62_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA62_1 = input.LA(1);

                         
                        int index62_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Eulang()) ) {s = 26;}

                        else if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index62_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA62_2 = input.LA(1);

                         
                        int index62_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Eulang()) ) {s = 26;}

                        else if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index62_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA62_3 = input.LA(1);

                         
                        int index62_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index62_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA62_4 = input.LA(1);

                         
                        int index62_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index62_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA62_5 = input.LA(1);

                         
                        int index62_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index62_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA62_6 = input.LA(1);

                         
                        int index62_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index62_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 62, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA75_eotS =
        "\5\uffff";
    static final String DFA75_eofS =
        "\5\uffff";
    static final String DFA75_minS =
        "\1\107\1\110\3\uffff";
    static final String DFA75_maxS =
        "\2\126\3\uffff";
    static final String DFA75_acceptS =
        "\2\uffff\1\1\1\2\1\3";
    static final String DFA75_specialS =
        "\5\uffff}>";
    static final String[] DFA75_transitionS = {
            "\1\4\1\2\1\3\14\uffff\1\1",
            "\1\2\1\3\14\uffff\1\1",
            "",
            "",
            ""
    };

    static final short[] DFA75_eot = DFA.unpackEncodedString(DFA75_eotS);
    static final short[] DFA75_eof = DFA.unpackEncodedString(DFA75_eofS);
    static final char[] DFA75_min = DFA.unpackEncodedStringToUnsignedChars(DFA75_minS);
    static final char[] DFA75_max = DFA.unpackEncodedStringToUnsignedChars(DFA75_maxS);
    static final short[] DFA75_accept = DFA.unpackEncodedString(DFA75_acceptS);
    static final short[] DFA75_special = DFA.unpackEncodedString(DFA75_specialS);
    static final short[][] DFA75_transition;

    static {
        int numStates = DFA75_transitionS.length;
        DFA75_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA75_transition[i] = DFA.unpackEncodedString(DFA75_transitionS[i]);
        }
    }

    class DFA75 extends DFA {

        public DFA75(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 75;
            this.eot = DFA75_eot;
            this.eof = DFA75_eof;
            this.min = DFA75_min;
            this.max = DFA75_max;
            this.accept = DFA75_accept;
            this.special = DFA75_special;
            this.transition = DFA75_transition;
        }
        public String getDescription() {
            return "329:8: ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ( attrs )? TYPE ID assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ( attrs )? type ID ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ( attrs )? TYPE ^( LIST ( ID )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ( attrs )? type ^( LIST ( ID )+ ) ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )";
        }
    }
    static final String DFA82_eotS =
        "\12\uffff";
    static final String DFA82_eofS =
        "\12\uffff";
    static final String DFA82_minS =
        "\1\106\6\0\3\uffff";
    static final String DFA82_maxS =
        "\1\u0099\6\0\3\uffff";
    static final String DFA82_acceptS =
        "\7\uffff\1\1\1\3\1\2";
    static final String DFA82_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\3\uffff}>";
    static final String[] DFA82_transitionS = {
            "\1\1\2\uffff\1\2\2\uffff\1\3\106\uffff\1\4\1\5\1\6\3\uffff\1"+
            "\2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA82_eot = DFA.unpackEncodedString(DFA82_eotS);
    static final short[] DFA82_eof = DFA.unpackEncodedString(DFA82_eofS);
    static final char[] DFA82_min = DFA.unpackEncodedStringToUnsignedChars(DFA82_minS);
    static final char[] DFA82_max = DFA.unpackEncodedStringToUnsignedChars(DFA82_maxS);
    static final short[] DFA82_accept = DFA.unpackEncodedString(DFA82_acceptS);
    static final short[] DFA82_special = DFA.unpackEncodedString(DFA82_specialS);
    static final short[][] DFA82_transition;

    static {
        int numStates = DFA82_transitionS.length;
        DFA82_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA82_transition[i] = DFA.unpackEncodedString(DFA82_transitionS[i]);
        }
    }

    class DFA82 extends DFA {

        public DFA82(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 82;
            this.eot = DFA82_eot;
            this.eof = DFA82_eof;
            this.min = DFA82_min;
            this.max = DFA82_max;
            this.accept = DFA82_accept;
            this.special = DFA82_special;
            this.transition = DFA82_transition;
        }
        public String getDescription() {
            return "348:1: assignStmt : ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA82_1 = input.LA(1);

                         
                        int index82_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index82_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA82_2 = input.LA(1);

                         
                        int index82_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index82_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA82_3 = input.LA(1);

                         
                        int index82_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (true) ) {s = 9;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index82_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA82_4 = input.LA(1);

                         
                        int index82_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index82_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA82_5 = input.LA(1);

                         
                        int index82_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index82_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA82_6 = input.LA(1);

                         
                        int index82_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index82_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 82, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA84_eotS =
        "\26\uffff";
    static final String DFA84_eofS =
        "\26\uffff";
    static final String DFA84_minS =
        "\1\11\6\0\17\uffff";
    static final String DFA84_maxS =
        "\1\u0099\6\0\17\uffff";
    static final String DFA84_acceptS =
        "\7\uffff\1\3\14\uffff\1\1\1\2";
    static final String DFA84_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\17\uffff}>";
    static final String[] DFA84_transitionS = {
            "\1\7\64\uffff\2\7\6\uffff\1\1\2\uffff\1\2\2\uffff\1\3\13\uffff"+
            "\1\7\32\uffff\1\7\5\uffff\1\7\13\uffff\2\7\5\uffff\1\7\4\uffff"+
            "\2\7\1\4\1\5\1\6\1\uffff\2\7\1\2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            ""
    };

    static final short[] DFA84_eot = DFA.unpackEncodedString(DFA84_eotS);
    static final short[] DFA84_eof = DFA.unpackEncodedString(DFA84_eofS);
    static final char[] DFA84_min = DFA.unpackEncodedStringToUnsignedChars(DFA84_minS);
    static final char[] DFA84_max = DFA.unpackEncodedStringToUnsignedChars(DFA84_maxS);
    static final short[] DFA84_accept = DFA.unpackEncodedString(DFA84_acceptS);
    static final short[] DFA84_special = DFA.unpackEncodedString(DFA84_specialS);
    static final short[][] DFA84_transition;

    static {
        int numStates = DFA84_transitionS.length;
        DFA84_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA84_transition[i] = DFA.unpackEncodedString(DFA84_transitionS[i]);
        }
    }

    class DFA84 extends DFA {

        public DFA84(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 84;
            this.eot = DFA84_eot;
            this.eof = DFA84_eof;
            this.min = DFA84_min;
            this.max = DFA84_max;
            this.accept = DFA84_accept;
            this.special = DFA84_special;
            this.transition = DFA84_transition;
        }
        public String getDescription() {
            return "358:1: assignExpr : ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA84_1 = input.LA(1);

                         
                        int index84_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index84_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA84_2 = input.LA(1);

                         
                        int index84_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index84_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA84_3 = input.LA(1);

                         
                        int index84_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (synpred16_Eulang()) ) {s = 21;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index84_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA84_4 = input.LA(1);

                         
                        int index84_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index84_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA84_5 = input.LA(1);

                         
                        int index84_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index84_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA84_6 = input.LA(1);

                         
                        int index84_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index84_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 84, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA88_eotS =
        "\30\uffff";
    static final String DFA88_eofS =
        "\30\uffff";
    static final String DFA88_minS =
        "\1\11\24\uffff\1\0\2\uffff";
    static final String DFA88_maxS =
        "\1\u0099\24\uffff\1\0\2\uffff";
    static final String DFA88_acceptS =
        "\1\uffff\23\1\1\2\1\uffff\1\3\1\4";
    static final String DFA88_specialS =
        "\1\0\24\uffff\1\1\2\uffff}>";
    static final String[] DFA88_transitionS = {
            "\1\14\64\uffff\1\20\1\21\6\uffff\1\3\2\uffff\1\4\2\uffff\1\5"+
            "\4\uffff\1\25\6\uffff\1\13\21\uffff\1\24\10\uffff\1\23\5\uffff"+
            "\1\22\13\uffff\1\2\1\17\5\uffff\1\1\4\uffff\1\15\1\16\1\6\1"+
            "\7\1\10\1\uffff\1\11\1\12\1\4",
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
            ""
    };

    static final short[] DFA88_eot = DFA.unpackEncodedString(DFA88_eotS);
    static final short[] DFA88_eof = DFA.unpackEncodedString(DFA88_eofS);
    static final char[] DFA88_min = DFA.unpackEncodedStringToUnsignedChars(DFA88_minS);
    static final char[] DFA88_max = DFA.unpackEncodedStringToUnsignedChars(DFA88_maxS);
    static final short[] DFA88_accept = DFA.unpackEncodedString(DFA88_acceptS);
    static final short[] DFA88_special = DFA.unpackEncodedString(DFA88_specialS);
    static final short[][] DFA88_transition;

    static {
        int numStates = DFA88_transitionS.length;
        DFA88_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA88_transition[i] = DFA.unpackEncodedString(DFA88_transitionS[i]);
        }
    }

    class DFA88 extends DFA {

        public DFA88(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 88;
            this.eot = DFA88_eot;
            this.eof = DFA88_eof;
            this.min = DFA88_min;
            this.max = DFA88_max;
            this.accept = DFA88_accept;
            this.special = DFA88_special;
            this.transition = DFA88_transition;
        }
        public String getDescription() {
            return "368:1: initExpr : ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA88_0 = input.LA(1);

                         
                        int index88_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA88_0==MINUS) && (synpred17_Eulang())) {s = 1;}

                        else if ( (LA88_0==TILDE) && (synpred17_Eulang())) {s = 2;}

                        else if ( (LA88_0==ID) && (synpred17_Eulang())) {s = 3;}

                        else if ( (LA88_0==COLON||LA88_0==COLONS) && (synpred17_Eulang())) {s = 4;}

                        else if ( (LA88_0==LPAREN) && (synpred17_Eulang())) {s = 5;}

                        else if ( (LA88_0==NUMBER) && (synpred17_Eulang())) {s = 6;}

                        else if ( (LA88_0==CHAR_LITERAL) && (synpred17_Eulang())) {s = 7;}

                        else if ( (LA88_0==STRING_LITERAL) && (synpred17_Eulang())) {s = 8;}

                        else if ( (LA88_0==FALSE) && (synpred17_Eulang())) {s = 9;}

                        else if ( (LA88_0==TRUE) && (synpred17_Eulang())) {s = 10;}

                        else if ( (LA88_0==NIL) && (synpred17_Eulang())) {s = 11;}

                        else if ( (LA88_0==CODE) && (synpred17_Eulang())) {s = 12;}

                        else if ( (LA88_0==PLUSPLUS) && (synpred17_Eulang())) {s = 13;}

                        else if ( (LA88_0==MINUSMINUS) && (synpred17_Eulang())) {s = 14;}

                        else if ( (LA88_0==AMP) && (synpred17_Eulang())) {s = 15;}

                        else if ( (LA88_0==SIZEOF) && (synpred17_Eulang())) {s = 16;}

                        else if ( (LA88_0==TYPEOF) && (synpred17_Eulang())) {s = 17;}

                        else if ( (LA88_0==NOT) && (synpred17_Eulang())) {s = 18;}

                        else if ( (LA88_0==IF) && (synpred17_Eulang())) {s = 19;}

                        else if ( (LA88_0==PERIOD) ) {s = 20;}

                        else if ( (LA88_0==LBRACKET) ) {s = 21;}

                         
                        input.seek(index88_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA88_21 = input.LA(1);

                         
                        int index88_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Eulang()) ) {s = 22;}

                        else if ( (true) ) {s = 23;}

                         
                        input.seek(index88_21);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 88, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA121_eotS =
        "\24\uffff";
    static final String DFA121_eofS =
        "\24\uffff";
    static final String DFA121_minS =
        "\1\11\2\uffff\6\0\13\uffff";
    static final String DFA121_maxS =
        "\1\u0099\2\uffff\6\0\13\uffff";
    static final String DFA121_acceptS =
        "\1\uffff\1\1\1\2\6\uffff\1\5\3\uffff\1\6\1\7\1\10\1\11\1\12\1\3"+
        "\1\4";
    static final String DFA121_specialS =
        "\3\uffff\1\0\1\1\1\2\1\3\1\4\1\5\13\uffff}>";
    static final String[] DFA121_transitionS = {
            "\1\11\64\uffff\1\20\1\21\6\uffff\1\3\2\uffff\1\4\2\uffff\1\5"+
            "\13\uffff\1\11\54\uffff\1\2\1\17\5\uffff\1\1\4\uffff\1\15\1"+
            "\16\1\6\1\7\1\10\1\uffff\2\11\1\4",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            ""
    };

    static final short[] DFA121_eot = DFA.unpackEncodedString(DFA121_eotS);
    static final short[] DFA121_eof = DFA.unpackEncodedString(DFA121_eofS);
    static final char[] DFA121_min = DFA.unpackEncodedStringToUnsignedChars(DFA121_minS);
    static final char[] DFA121_max = DFA.unpackEncodedStringToUnsignedChars(DFA121_maxS);
    static final short[] DFA121_accept = DFA.unpackEncodedString(DFA121_acceptS);
    static final short[] DFA121_special = DFA.unpackEncodedString(DFA121_specialS);
    static final short[][] DFA121_transition;

    static {
        int numStates = DFA121_transitionS.length;
        DFA121_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA121_transition[i] = DFA.unpackEncodedString(DFA121_transitionS[i]);
        }
    }

    class DFA121 extends DFA {

        public DFA121(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 121;
            this.eot = DFA121_eot;
            this.eof = DFA121_eof;
            this.min = DFA121_min;
            this.max = DFA121_max;
            this.accept = DFA121_accept;
            this.special = DFA121_special;
            this.transition = DFA121_transition;
        }
        public String getDescription() {
            return "546:1: unary : ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA121_3 = input.LA(1);

                         
                        int index121_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index121_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA121_4 = input.LA(1);

                         
                        int index121_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index121_4);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA121_5 = input.LA(1);

                         
                        int index121_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index121_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA121_6 = input.LA(1);

                         
                        int index121_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index121_6);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA121_7 = input.LA(1);

                         
                        int index121_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index121_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA121_8 = input.LA(1);

                         
                        int index121_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index121_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 121, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA127_eotS =
        "\16\uffff";
    static final String DFA127_eofS =
        "\16\uffff";
    static final String DFA127_minS =
        "\1\11\10\uffff\1\0\4\uffff";
    static final String DFA127_maxS =
        "\1\u0099\10\uffff\1\0\4\uffff";
    static final String DFA127_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\2\uffff\1\13\1\10\1\11\1\12";
    static final String DFA127_specialS =
        "\11\uffff\1\0\4\uffff}>";
    static final String[] DFA127_transitionS = {
            "\1\12\74\uffff\1\7\2\uffff\1\7\2\uffff\1\11\13\uffff\1\6\72"+
            "\uffff\1\1\1\4\1\5\1\uffff\1\2\1\3\1\7",
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
            ""
    };

    static final short[] DFA127_eot = DFA.unpackEncodedString(DFA127_eotS);
    static final short[] DFA127_eof = DFA.unpackEncodedString(DFA127_eofS);
    static final char[] DFA127_min = DFA.unpackEncodedStringToUnsignedChars(DFA127_minS);
    static final char[] DFA127_max = DFA.unpackEncodedStringToUnsignedChars(DFA127_maxS);
    static final short[] DFA127_accept = DFA.unpackEncodedString(DFA127_acceptS);
    static final short[] DFA127_special = DFA.unpackEncodedString(DFA127_specialS);
    static final short[][] DFA127_transition;

    static {
        int numStates = DFA127_transitionS.length;
        DFA127_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA127_transition[i] = DFA.unpackEncodedString(DFA127_transitionS[i]);
        }
    }

    class DFA127 extends DFA {

        public DFA127(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 127;
            this.eot = DFA127_eot;
            this.eof = DFA127_eof;
            this.min = DFA127_min;
            this.max = DFA127_max;
            this.accept = DFA127_accept;
            this.special = DFA127_special;
            this.transition = DFA127_transition;
        }
        public String getDescription() {
            return "581:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA127_9 = input.LA(1);

                         
                        int index127_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Eulang()) ) {s = 11;}

                        else if ( (synpred26_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 13;}

                         
                        input.seek(index127_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 127, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA133_eotS =
        "\70\uffff";
    static final String DFA133_eofS =
        "\1\2\67\uffff";
    static final String DFA133_minS =
        "\1\45\1\0\66\uffff";
    static final String DFA133_maxS =
        "\1\u0096\1\0\66\uffff";
    static final String DFA133_acceptS =
        "\2\uffff\1\2\64\uffff\1\1";
    static final String DFA133_specialS =
        "\1\uffff\1\0\66\uffff}>";
    static final String[] DFA133_transitionS = {
            "\2\2\35\uffff\1\2\2\uffff\1\2\1\uffff\12\2\2\uffff\1\2\1\uffff"+
            "\1\2\1\uffff\24\2\1\uffff\1\2\5\uffff\5\2\1\uffff\6\2\1\1\22"+
            "\2\3\uffff\1\2",
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

    static final short[] DFA133_eot = DFA.unpackEncodedString(DFA133_eotS);
    static final short[] DFA133_eof = DFA.unpackEncodedString(DFA133_eofS);
    static final char[] DFA133_min = DFA.unpackEncodedStringToUnsignedChars(DFA133_minS);
    static final char[] DFA133_max = DFA.unpackEncodedStringToUnsignedChars(DFA133_maxS);
    static final short[] DFA133_accept = DFA.unpackEncodedString(DFA133_acceptS);
    static final short[] DFA133_special = DFA.unpackEncodedString(DFA133_specialS);
    static final short[][] DFA133_transition;

    static {
        int numStates = DFA133_transitionS.length;
        DFA133_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA133_transition[i] = DFA.unpackEncodedString(DFA133_transitionS[i]);
        }
    }

    class DFA133 extends DFA {

        public DFA133(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 133;
            this.eot = DFA133_eot;
            this.eof = DFA133_eof;
            this.min = DFA133_min;
            this.max = DFA133_max;
            this.accept = DFA133_accept;
            this.special = DFA133_special;
            this.transition = DFA133_transition;
        }
        public String getDescription() {
            return "614:5: ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA133_1 = input.LA(1);

                         
                        int index133_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred28_Eulang()) ) {s = 55;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index133_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 133, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA139_eotS =
        "\14\uffff";
    static final String DFA139_eofS =
        "\14\uffff";
    static final String DFA139_minS =
        "\1\11\3\0\1\uffff\1\0\6\uffff";
    static final String DFA139_maxS =
        "\1\u009a\3\0\1\uffff\1\0\6\uffff";
    static final String DFA139_acceptS =
        "\4\uffff\1\1\1\uffff\1\2\5\uffff";
    static final String DFA139_specialS =
        "\1\uffff\1\0\1\1\1\2\1\uffff\1\3\6\uffff}>";
    static final String[] DFA139_transitionS = {
            "\1\3\74\uffff\1\1\2\uffff\1\2\2\uffff\1\5\13\uffff\1\6\72\uffff"+
            "\3\6\1\uffff\2\6\1\2\1\4",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA139_eot = DFA.unpackEncodedString(DFA139_eotS);
    static final short[] DFA139_eof = DFA.unpackEncodedString(DFA139_eofS);
    static final char[] DFA139_min = DFA.unpackEncodedStringToUnsignedChars(DFA139_minS);
    static final char[] DFA139_max = DFA.unpackEncodedStringToUnsignedChars(DFA139_maxS);
    static final short[] DFA139_accept = DFA.unpackEncodedString(DFA139_acceptS);
    static final short[] DFA139_special = DFA.unpackEncodedString(DFA139_specialS);
    static final short[][] DFA139_transition;

    static {
        int numStates = DFA139_transitionS.length;
        DFA139_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA139_transition[i] = DFA.unpackEncodedString(DFA139_transitionS[i]);
        }
    }

    class DFA139 extends DFA {

        public DFA139(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 139;
            this.eot = DFA139_eot;
            this.eof = DFA139_eof;
            this.min = DFA139_min;
            this.max = DFA139_max;
            this.accept = DFA139_accept;
            this.special = DFA139_special;
            this.transition = DFA139_transition;
        }
        public String getDescription() {
            return "624:1: instanceExpr options {backtrack=true; } : ( type | atom );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA139_1 = input.LA(1);

                         
                        int index139_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index139_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA139_2 = input.LA(1);

                         
                        int index139_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index139_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA139_3 = input.LA(1);

                         
                        int index139_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index139_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA139_5 = input.LA(1);

                         
                        int index139_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index139_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 139, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog445 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts476 = new BitSet(new long[]{0x0000000000000002L,0x0000000000003260L,0x0000000002000000L});
    public static final BitSet FOLLOW_toplevelstatNoAlloc_in_toplevelstmtsNoAlloc503 = new BitSet(new long[]{0x0000000000000002L,0x0000000000002260L,0x0000000002000000L});
    public static final BitSet FOLLOW_toplevelstatNoAlloc_in_toplevelstat532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelAlloc_in_toplevelstat545 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelCode_in_toplevelstat559 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat561 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defineStmt_in_toplevelstatNoAlloc582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scopeExtension_in_toplevelstatNoAlloc590 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FORWARD_in_toplevelstatNoAlloc606 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_toplevelstatNoAlloc608 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000090L});
    public static final BitSet FOLLOW_COMMA_in_toplevelstatNoAlloc611 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_toplevelstatNoAlloc613 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000090L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc617 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstatNoAlloc645 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelSingleVarDecl_in_toplevelAlloc662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelTupleVarDecl_in_toplevelAlloc666 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelSingleVarDecl677 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400380L});
    public static final BitSet FOLLOW_attrs_in_toplevelSingleVarDecl679 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000380L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl694 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_toplevelSingleVarDecl733 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_toplevelSingleVarDecl735 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelSingleVarDecl738 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl773 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_toplevelSingleVarDecl775 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000380L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl794 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021A40L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_PLUS_in_toplevelSingleVarDecl796 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl799 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl802 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl804 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COLON_in_toplevelSingleVarDecl867 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_toplevelSingleVarDecl869 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelSingleVarDecl872 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021A40L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_PLUS_in_toplevelSingleVarDecl874 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl877 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl880 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl882 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_idTuple_in_toplevelTupleVarDecl976 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000300L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelTupleVarDecl998 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1000 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_toplevelTupleVarDecl1034 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_toplevelTupleVarDecl1036 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelTupleVarDecl1039 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1041 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_rhsExprOrInitList1084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_rhsExprOrInitList1088 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_toplevelCode1097 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_toplevelCode1099 = new BitSet(new long[]{0xC002000000000200L,0x020C380001107240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codestmtlist_in_toplevelCode1101 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_toplevelCode1103 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_toplevelCode1105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_scopeExtension1124 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_PLUS_EQ_in_scopeExtension1126 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_xscopeNoAlloc_in_scopeExtension1128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1157 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_defineStmt1159 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_LBRACKET_in_defineStmt1161 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040040L});
    public static final BitSet FOLLOW_idlistOrEmpty_in_defineStmt1163 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACKET_in_defineStmt1165 = new BitSet(new long[]{0xC000000000000200L,0x0208000001023260L,0x0000000007BE1060L});
    public static final BitSet FOLLOW_toplevelvalue_in_defineStmt1168 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1204 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_EQUALS_COLON_in_defineStmt1206 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_defineStmt1208 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1241 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_defineStmt1243 = new BitSet(new long[]{0xC000000000000200L,0x0208000001023260L,0x0000000007BE1060L});
    public static final BitSet FOLLOW_toplevelvalue_in_defineStmt1245 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue1279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_toplevelvalue1287 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_PLUS_in_toplevelvalue1289 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_data_in_toplevelvalue1291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_toplevelvalue1309 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_PLUS_in_toplevelvalue1311 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002260L,0x0000000002000000L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue1313 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue1331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue1339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_data_in_toplevelvalue1347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector1360 = new BitSet(new long[]{0xC000000000000200L,0x0208000001041240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_selectors_in_selector1362 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector1364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors1390 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_selectors1394 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_selectoritem_in_selectors1396 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_selectors1401 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_selectoritem1432 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope1443 = new BitSet(new long[]{0x0000000000000000L,0x0000000000003260L,0x0000000002000000L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope1445 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope1447 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscopeNoAlloc1472 = new BitSet(new long[]{0x0000000000000000L,0x0000000000006260L,0x0000000002000000L});
    public static final BitSet FOLLOW_toplevelstmtsNoAlloc_in_xscopeNoAlloc1474 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_xscopeNoAlloc1476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr1503 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100200L});
    public static final BitSet FOLLOW_COLON_in_listCompr1506 = new BitSet(new long[]{0x0000000000000200L,0x0000000001001240L,0x0000000003B80000L});
    public static final BitSet FOLLOW_listiterable_in_listCompr1508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn1540 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_idlist_in_forIn1542 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_IN_in_forIn1544 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_list_in_forIn1546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist1571 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_idlist1574 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_idlist1576 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_idlist_in_idlistOrEmpty1602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_listiterable1623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list1637 = new BitSet(new long[]{0xC000000000000200L,0x0208000001063260L,0x0000000007BE1060L});
    public static final BitSet FOLLOW_listitems_in_list1639 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACKET_in_list1641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems1671 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_listitems1675 = new BitSet(new long[]{0xC000000000000200L,0x0208000001023260L,0x0000000007BE1060L});
    public static final BitSet FOLLOW_listitem_in_listitems1677 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_listitems1682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem1708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_code1726 = new BitSet(new long[]{0x0000000000000000L,0x0000000000403000L});
    public static final BitSet FOLLOW_attrs_in_code1728 = new BitSet(new long[]{0x0000000000000000L,0x0000000000003000L});
    public static final BitSet FOLLOW_proto_in_code1731 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_code1734 = new BitSet(new long[]{0xC002000000000200L,0x020C380001107240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codestmtlist_in_code1736 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_code1738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATTR_in_attrs1768 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_argdefs1812 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefs1819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithNames_in_argdefs1826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1842 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1846 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1848 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1852 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1881 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400680L});
    public static final BitSet FOLLOW_COMMA_in_argdefWithType1884 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1886 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400680L});
    public static final BitSet FOLLOW_attrs_in_argdefWithType1890 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000600L});
    public static final BitSet FOLLOW_COLON_in_argdefWithType1894 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_argdefWithType1896 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_argdefWithType1901 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_argdefWithType1905 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1940 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1944 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1946 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithName1972 = new BitSet(new long[]{0x0000000000000002L,0x0000000000400000L});
    public static final BitSet FOLLOW_attrs_in_argdefWithName1974 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto1999 = new BitSet(new long[]{0x0000000000000000L,0x0000000000808040L});
    public static final BitSet FOLLOW_argdefs_in_proto2001 = new BitSet(new long[]{0x0000000000000000L,0x0000000000808000L});
    public static final BitSet FOLLOW_xreturns_in_proto2003 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_proto2006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns2049 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_xreturns2051 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns2068 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_NIL_in_xreturns2070 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_argtuple2100 = new BitSet(new long[]{0x0000000000000200L,0x00000000020012C0L,0x0000000006000000L});
    public static final BitSet FOLLOW_tupleargdefs_in_argtuple2102 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_argtuple2104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs2126 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_tupleargdefs2130 = new BitSet(new long[]{0x0000000000000200L,0x00000000020012C0L,0x0000000006000000L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs2132 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_type_in_tupleargdef2177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_tupleargdef2190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonArrayType_in_type2255 = new BitSet(new long[]{0x0000000000000002L,0x0000000004020000L});
    public static final BitSet FOLLOW_arraySuff_in_type2293 = new BitSet(new long[]{0x0000000000000002L,0x0000000004020000L});
    public static final BitSet FOLLOW_LBRACKET_in_type2348 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_type2350 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_type2354 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_type2356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040080L});
    public static final BitSet FOLLOW_RBRACKET_in_type2361 = new BitSet(new long[]{0x0000000000000002L,0x0000000004020000L});
    public static final BitSet FOLLOW_CARET_in_type2420 = new BitSet(new long[]{0x0000000000000002L,0x0000000004020000L});
    public static final BitSet FOLLOW_idExpr_in_nonArrayType2472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_nonArrayType2490 = new BitSet(new long[]{0x0000000000000002L,0x0000000000401000L});
    public static final BitSet FOLLOW_attrs_in_nonArrayType2492 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_proto_in_nonArrayType2495 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_data_in_nonArrayType2521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argtuple_in_nonArrayType2537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arraySuff2553 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_arraySuff2555 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACKET_in_arraySuff2557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arraySuff2569 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACKET_in_arraySuff2571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist2587 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist2590 = new BitSet(new long[]{0xC002000000000202L,0x020C380001103250L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist2592 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_labelStmt_in_codeStmt2636 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt2638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt2659 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmtExpr2691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_codeStmtExpr2714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_codeStmtExpr2731 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blockStmt_in_codeStmtExpr2764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_codeStmtExpr2786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_controlStmt_in_codeStmtExpr2812 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleVarDecl_in_varDecl2835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleVarDecl_in_varDecl2839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_singleVarDecl2851 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400380L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2865 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_singleVarDecl2868 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2907 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_in_singleVarDecl2910 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_singleVarDecl2912 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_singleVarDecl2915 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl2950 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_singleVarDecl2952 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400380L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2957 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000300L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_singleVarDecl2975 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021A40L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_PLUS_in_singleVarDecl2977 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2980 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl2983 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2985 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COLON_in_singleVarDecl3047 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_singleVarDecl3049 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_singleVarDecl3052 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021A40L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_PLUS_in_singleVarDecl3054 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl3057 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl3060 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl3062 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_idTuple_in_tupleVarDecl3151 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400300L});
    public static final BitSet FOLLOW_attrs_in_tupleVarDecl3153 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000300L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_tupleVarDecl3168 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_tupleVarDecl3170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_tupleVarDecl3207 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_tupleVarDecl3209 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_tupleVarDecl3212 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_tupleVarDecl3214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3265 = new BitSet(new long[]{0x0000000000000000L,0x000003FFF8010400L});
    public static final BitSet FOLLOW_assignEqOp_in_assignStmt3267 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignStmt3296 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt3298 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3355 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt3358 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001240L,0x0000000002380000L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3360 = new BitSet(new long[]{0x0000000000000000L,0x000003FFF8010480L});
    public static final BitSet FOLLOW_assignEqOp_in_assignStmt3364 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021A40L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_PLUS_in_assignStmt3366 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3369 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt3372 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3374 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_assignExpr_in_assignOrInitExpr3435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_assignOrInitExpr3439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignExpr3457 = new BitSet(new long[]{0x0000000000000000L,0x000003FFF8010400L});
    public static final BitSet FOLLOW_assignEqOp_in_assignExpr3459 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr3461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignExpr3496 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr3498 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr3500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr3534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_assignOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_assignEqOp3649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignOp_in_assignEqOp3653 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_initList3662 = new BitSet(new long[]{0xC000000000000200L,0x0208040001061240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_initExpr_in_initList3665 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040080L});
    public static final BitSet FOLLOW_COMMA_in_initList3668 = new BitSet(new long[]{0xC000000000000200L,0x0208040001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_initExpr_in_initList3670 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040080L});
    public static final BitSet FOLLOW_RBRACKET_in_initList3676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_initExpr3711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERIOD_in_initExpr3774 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_initExpr3776 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_initExpr3778 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_initElement_in_initExpr3782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_initExpr3847 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_initExpr3851 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACKET_in_initExpr3853 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_initExpr3855 = new BitSet(new long[]{0xC000000000000200L,0x0208000001021240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_initElement_in_initExpr3859 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_initExpr3896 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_initElement3910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_initElement3914 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_doWhile_in_controlStmt3926 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whileDo_in_controlStmt3930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_repeat_in_controlStmt3934 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIter_in_controlStmt3938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_doWhile3947 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codeStmtExpr_in_doWhile3949 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_WHILE_in_doWhile3951 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_doWhile3953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_whileDo3976 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_whileDo3978 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000200L});
    public static final BitSet FOLLOW_COLON_in_whileDo3981 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_DO_in_whileDo3983 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codeStmtExpr_in_whileDo3986 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REPEAT_in_repeat4011 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_repeat4013 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000200L});
    public static final BitSet FOLLOW_COLON_in_repeat4016 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_DO_in_repeat4018 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codeStmt_in_repeat4021 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIter4051 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_forIds_in_forIter4053 = new BitSet(new long[]{0x0000000000000000L,0x0001800000200000L});
    public static final BitSet FOLLOW_forMovement_in_forIter4055 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_IN_in_forIter4058 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_forIter4060 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000200L});
    public static final BitSet FOLLOW_COLON_in_forIter4063 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_DO_in_forIter4065 = new BitSet(new long[]{0xC002000000000200L,0x020C380001103240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codeStmt_in_forIter4068 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_forIds4105 = new BitSet(new long[]{0x0000000000000002L,0x0000400000000000L});
    public static final BitSet FOLLOW_AND_in_forIds4108 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_forIds4110 = new BitSet(new long[]{0x0000000000000002L,0x0000400000000000L});
    public static final BitSet FOLLOW_atId_in_forMovement4126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stepping_in_forMovement4130 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BY_in_stepping4139 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_stepping4141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_atId4158 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_atId4160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_breakStmt4188 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_breakStmt4190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATSIGN_in_labelStmt4218 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_labelStmt4220 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_in_labelStmt4222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GOTO_in_gotoStmt4258 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000240L,0x0000000002000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_gotoStmt4260 = new BitSet(new long[]{0x0000000000000002L,0x0008000000000000L});
    public static final BitSet FOLLOW_IF_in_gotoStmt4263 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_gotoStmt4265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_blockStmt4300 = new BitSet(new long[]{0xC002000000000200L,0x020C380001107240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codestmtlist_in_blockStmt4302 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_blockStmt4304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tuple4327 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_tupleEntries_in_tuple4329 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_tuple4331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries4359 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_tupleEntries4362 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries4364 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_LPAREN_in_idTuple4383 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000240L,0x0000000002000000L});
    public static final BitSet FOLLOW_idTupleEntries_in_idTuple4385 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_idTuple4387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries4415 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_idTupleEntries4418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000240L,0x0000000002000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries4420 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_condStar_in_rhsExpr4441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist4462 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_arglist4466 = new BitSet(new long[]{0xC002000000000200L,0x0208000001003240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_arg_in_arglist4468 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_arglist4472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg4521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_arg4554 = new BitSet(new long[]{0xC002000000000200L,0x020C380001107240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_codestmtlist_in_arg4556 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_arg4558 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_arg4582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_condStar4643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_condStar4654 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_ifExprs_in_condStar4656 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_thenClause_in_ifExprs4675 = new BitSet(new long[]{0x0000000000000000L,0x00E0000000000000L});
    public static final BitSet FOLLOW_elses_in_ifExprs4677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_thenClause4699 = new BitSet(new long[]{0x0000000000000000L,0x0010000000000200L});
    public static final BitSet FOLLOW_THEN_in_thenClause4705 = new BitSet(new long[]{0xC002000000000200L,0x020A000001003240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_COLON_in_thenClause4707 = new BitSet(new long[]{0xC002000000000200L,0x020A000001003240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_condStmtExpr_in_thenClause4712 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elif_in_elses4740 = new BitSet(new long[]{0x0000000000000000L,0x00E0000000000000L});
    public static final BitSet FOLLOW_elseClause_in_elses4743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELIF_in_elif4766 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_elif4770 = new BitSet(new long[]{0x0000000000000000L,0x0010000000000200L});
    public static final BitSet FOLLOW_THEN_in_elif4773 = new BitSet(new long[]{0xC002000000000200L,0x020A000001003240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_COLON_in_elif4775 = new BitSet(new long[]{0xC002000000000200L,0x020A000001003240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_condStmtExpr_in_elif4780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELSE_in_elseClause4806 = new BitSet(new long[]{0xC002000000000200L,0x020A000001003240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_condStmtExpr_in_elseClause4808 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FI_in_elseClause4835 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_condStmtExpr4866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_breakStmt_in_condStmtExpr4870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logor_in_cond4887 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_QUESTION_in_cond4904 = new BitSet(new long[]{0xC000000000000200L,0x0200000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_logor_in_cond4908 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_COLON_in_cond4910 = new BitSet(new long[]{0xC000000000000200L,0x0200000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_logor_in_cond4914 = new BitSet(new long[]{0x0000000000000002L,0x0000000002000000L});
    public static final BitSet FOLLOW_logand_in_logor4958 = new BitSet(new long[]{0x0000000000000002L,0x0100000000000000L});
    public static final BitSet FOLLOW_OR_in_logor4975 = new BitSet(new long[]{0xC000000000000200L,0x0200000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_logand_in_logor4979 = new BitSet(new long[]{0x0000000000000002L,0x0100000000000000L});
    public static final BitSet FOLLOW_not_in_logand5010 = new BitSet(new long[]{0x0000000000000002L,0x0000400000000000L});
    public static final BitSet FOLLOW_AND_in_logand5026 = new BitSet(new long[]{0xC000000000000200L,0x0200000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_not_in_logand5030 = new BitSet(new long[]{0x0000000000000002L,0x0000400000000000L});
    public static final BitSet FOLLOW_comp_in_not5076 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_not5092 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_comp_in_not5096 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bitor_in_comp5130 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPEQ_in_comp5163 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5167 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPNE_in_comp5189 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5193 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPLE_in_comp5215 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5219 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPGE_in_comp5244 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5248 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPULE_in_comp5273 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5277 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_COMPUGE_in_comp5302 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5306 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_LESS_in_comp5331 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5335 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_ULESS_in_comp5361 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5365 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_GREATER_in_comp5391 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5395 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_UGREATER_in_comp5420 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitor_in_comp5424 = new BitSet(new long[]{0x0000000000000002L,0xFC00000000000000L,0x000000000000000FL});
    public static final BitSet FOLLOW_bitxor_in_bitor5474 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_BAR_in_bitor5502 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitxor_in_bitor5506 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_bitand_in_bitxor5532 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_TILDE_in_bitxor5560 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_bitand_in_bitxor5564 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_shift_in_bitand5589 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_AMP_in_bitand5617 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_shift_in_bitand5621 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_factor_in_shift5648 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000F80L});
    public static final BitSet FOLLOW_LSHIFT_in_shift5682 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_factor_in_shift5686 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000F80L});
    public static final BitSet FOLLOW_RSHIFT_in_shift5715 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_factor_in_shift5719 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000F80L});
    public static final BitSet FOLLOW_URSHIFT_in_shift5747 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_factor_in_shift5751 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000F80L});
    public static final BitSet FOLLOW_CRSHIFT_in_shift5779 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_factor_in_shift5783 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000F80L});
    public static final BitSet FOLLOW_CLSHIFT_in_shift5811 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_factor_in_shift5815 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000F80L});
    public static final BitSet FOLLOW_term_in_factor5857 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L,0x0000000000001000L});
    public static final BitSet FOLLOW_PLUS_in_factor5890 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_term_in_factor5894 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L,0x0000000000001000L});
    public static final BitSet FOLLOW_MINUS_in_factor5936 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_term_in_factor5940 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000800L,0x0000000000001000L});
    public static final BitSet FOLLOW_unary_in_term5985 = new BitSet(new long[]{0x0000006000000002L,0x0000000000000000L,0x000000000001E000L});
    public static final BitSet FOLLOW_STAR_in_term6029 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_term6033 = new BitSet(new long[]{0x0000006000000002L,0x0000000000000000L,0x000000000001E000L});
    public static final BitSet FOLLOW_SLASH_in_term6069 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_term6073 = new BitSet(new long[]{0x0000006000000002L,0x0000000000000000L,0x000000000001E000L});
    public static final BitSet FOLLOW_REM_in_term6108 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_term6112 = new BitSet(new long[]{0x0000006000000002L,0x0000000000000000L,0x000000000001E000L});
    public static final BitSet FOLLOW_UDIV_in_term6147 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_term6151 = new BitSet(new long[]{0x0000006000000002L,0x0000000000000000L,0x000000000001E000L});
    public static final BitSet FOLLOW_UREM_in_term6186 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_term6190 = new BitSet(new long[]{0x0000006000000002L,0x0000000000000000L,0x000000000001E000L});
    public static final BitSet FOLLOW_MOD_in_term6225 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_term6229 = new BitSet(new long[]{0x0000006000000002L,0x0000000000000000L,0x000000000001E000L});
    public static final BitSet FOLLOW_MINUS_in_unary6302 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_unary6306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary6326 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_unary6330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_unary6365 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_PLUSPLUS_in_unary6367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_unary6398 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_MINUSMINUS_in_unary6400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_unary6421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUSPLUS_in_unary6452 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001240L,0x0000000002380000L});
    public static final BitSet FOLLOW_lhs_in_unary6456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUSMINUS_in_unary6477 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001240L,0x0000000002380000L});
    public static final BitSet FOLLOW_lhs_in_unary6481 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMP_in_unary6501 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001240L,0x0000000002380000L});
    public static final BitSet FOLLOW_lhs_in_unary6503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIZEOF_in_unary6544 = new BitSet(new long[]{0x0000000000000200L,0x0000000001001240L,0x0000000003B80000L});
    public static final BitSet FOLLOW_atom_in_unary6546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TYPEOF_in_unary6570 = new BitSet(new long[]{0x0000000000000200L,0x0000000001001240L,0x0000000003B80000L});
    public static final BitSet FOLLOW_atom_in_unary6572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idExpr_in_lhs6607 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_tuple_in_lhs6654 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_NUMBER_in_lhs6693 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_lhs6736 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_lhs6771 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_LPAREN_in_lhs6804 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_lhs6808 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_lhs6810 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_PERIOD_in_lhs6853 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_lhs6855 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_LPAREN_in_lhs6880 = new BitSet(new long[]{0xC002000000000200L,0x020800000100B240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_arglist_in_lhs6882 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_lhs6884 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_arrayAccess_in_lhs6917 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_CARET_in_lhs6942 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_LBRACE_in_lhs6963 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001A40L,0x0000000006000000L});
    public static final BitSet FOLLOW_PLUS_in_lhs6965 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_lhs6968 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_lhs6970 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_AS_in_lhs7011 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001A40L,0x0000000006000000L});
    public static final BitSet FOLLOW_PLUS_in_lhs7013 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_lhs7016 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom7065 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_FALSE_in_atom7108 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_TRUE_in_atom7150 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom7193 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom7228 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_NIL_in_atom7261 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_idExpr_in_atom7304 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_tuple_in_atom7351 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7400 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001040L});
    public static final BitSet FOLLOW_varDecl_in_atom7404 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7406 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7435 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_atom7439 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7441 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_code_in_atom7470 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_PERIOD_in_atom7529 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_atom7531 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7556 = new BitSet(new long[]{0xC002000000000200L,0x020800000100B240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_arglist_in_atom7558 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7560 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_arrayAccess_in_atom7593 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_CARET_in_atom7618 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_LBRACE_in_atom7639 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001A40L,0x0000000006000000L});
    public static final BitSet FOLLOW_PLUS_in_atom7641 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_atom7644 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_atom7646 = new BitSet(new long[]{0x0000000000000002L,0x0000040004023000L,0x0000000000400000L});
    public static final BitSet FOLLOW_AS_in_atom7687 = new BitSet(new long[]{0x0000000000000200L,0x0000000000001240L,0x0000000006000000L});
    public static final BitSet FOLLOW_type_in_atom7689 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arrayAccess7723 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_arrayAccess7725 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040080L});
    public static final BitSet FOLLOW_COMMA_in_arrayAccess7728 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_assignExpr_in_arrayAccess7730 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040080L});
    public static final BitSet FOLLOW_RBRACKET_in_arrayAccess7734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idExpr7756 = new BitSet(new long[]{0x0000000000000002L,0x0000040000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_PERIOD_in_idExpr7772 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_idExpr7774 = new BitSet(new long[]{0x0000000000000002L,0x0000040000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_instantiation_in_idExpr7804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7835 = new BitSet(new long[]{0x0000000000000002L,0x0000040000000000L});
    public static final BitSet FOLLOW_PERIOD_in_namespaceRef7838 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7840 = new BitSet(new long[]{0x0000000000000002L,0x0000040000000000L});
    public static final BitSet FOLLOW_colons_in_namespaceRef7864 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7866 = new BitSet(new long[]{0x0000000000000002L,0x0000040000000000L});
    public static final BitSet FOLLOW_PERIOD_in_namespaceRef7869 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7871 = new BitSet(new long[]{0x0000000000000002L,0x0000040000000000L});
    public static final BitSet FOLLOW_LESS_in_instantiation7900 = new BitSet(new long[]{0x0000000000000200L,0x0000000001001240L,0x0000000007B80004L});
    public static final BitSet FOLLOW_instanceExpr_in_instantiation7903 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x0000000000000004L});
    public static final BitSet FOLLOW_COMMA_in_instantiation7906 = new BitSet(new long[]{0x0000000000000200L,0x0000000001001240L,0x0000000007B80000L});
    public static final BitSet FOLLOW_instanceExpr_in_instantiation7908 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L,0x0000000000000004L});
    public static final BitSet FOLLOW_GREATER_in_instantiation7914 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_instanceExpr7946 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_instanceExpr7950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef7958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_colons_in_idOrScopeRef7981 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef7983 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_colons8009 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L,0x0000000002000000L});
    public static final BitSet FOLLOW_DATA_in_data8026 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_data8028 = new BitSet(new long[]{0x0000000000000000L,0x0000000000005060L});
    public static final BitSet FOLLOW_fieldDecl_in_data8030 = new BitSet(new long[]{0x0000000000000000L,0x0000000000005060L});
    public static final BitSet FOLLOW_RBRACE_in_data8033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_fieldDecl8052 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_fieldDecl8054 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defineStmt_in_fieldDecl8067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FORWARD_in_fieldDecl8075 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_fieldDecl8077 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000090L});
    public static final BitSet FOLLOW_COMMA_in_fieldDecl8080 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_fieldDecl8082 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000090L});
    public static final BitSet FOLLOW_SEMI_in_fieldDecl8086 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_fieldDecl8103 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_COLON_COLON_EQUALS_in_fieldDecl8105 = new BitSet(new long[]{0xC000000000000200L,0x0208000001023260L,0x0000000007BE1060L});
    public static final BitSet FOLLOW_toplevelvalue_in_fieldDecl8107 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_fieldDecl8109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Eulang640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred2_Eulang1148 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_synpred2_Eulang1150 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred2_Eulang1152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred3_Eulang1197 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_EQUALS_COLON_in_synpred3_Eulang1199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred4_Eulang1234 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_synpred4_Eulang1236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Eulang1274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_synpred7_Eulang1812 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_synpred8_Eulang1819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arraySuff_in_synpred9_Eulang2287 = new BitSet(new long[]{0x0000000000000002L,0x0000000000020000L});
    public static final BitSet FOLLOW_varDecl_in_synpred10_Eulang2686 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_synpred11_Eulang2709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Eulang2758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred13_Eulang3258 = new BitSet(new long[]{0x0000000000000000L,0x000003FFF8010400L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred13_Eulang3260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred14_Eulang3340 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COMMA_in_synpred14_Eulang3343 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001240L,0x0000000002380000L});
    public static final BitSet FOLLOW_lhs_in_synpred14_Eulang3345 = new BitSet(new long[]{0x0000000000000000L,0x000003FFF8010480L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred14_Eulang3349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred15_Eulang3450 = new BitSet(new long[]{0x0000000000000000L,0x000003FFF8010400L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred15_Eulang3452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_synpred16_Eulang3489 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_EQUALS_in_synpred16_Eulang3491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_synpred17_Eulang3704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred18_Eulang3836 = new BitSet(new long[]{0xC000000000000200L,0x0208000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_rhsExpr_in_synpred18_Eulang3840 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred18_Eulang3842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred19_Eulang5929 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_term_in_synpred19_Eulang5931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred20_Eulang6022 = new BitSet(new long[]{0xC000000000000200L,0x0000000001001240L,0x0000000003BE1060L});
    public static final BitSet FOLLOW_unary_in_synpred20_Eulang6024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred21_Eulang6356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_PLUSPLUS_in_synpred21_Eulang6358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred22_Eulang6389 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_MINUSMINUS_in_synpred22_Eulang6391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred23_Eulang6648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred24_Eulang6911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred25_Eulang7345 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred26_Eulang7392 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001040L});
    public static final BitSet FOLLOW_varDecl_in_synpred26_Eulang7394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred27_Eulang7587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_instantiation_in_synpred28_Eulang7798 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_synpred29_Eulang7946 = new BitSet(new long[]{0x0000000000000002L});

}