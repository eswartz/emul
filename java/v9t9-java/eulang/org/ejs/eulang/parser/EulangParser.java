// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g 2010-06-12 21:44:37

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SCOPE", "ADDSCOPE", "EXTENDSCOPE", "ATTRS", "LIST_COMPREHENSION", "CODE", "STMTLIST", "PROTO", "ARGLIST", "ARGDEF", "REF", "ALLOC", "ALLOC_TUPLE", "ASSIGN", "DEFINE", "EXPR", "LIST", "TYPE", "STMTEXPR", "CONDLIST", "CONDTEST", "CALL", "INLINE", "CAST", "COND", "BITAND", "BITOR", "BITXOR", "ADD", "SUB", "MUL", "DIV", "UDIV", "MOD", "NEG", "INV", "POSTINC", "POSTDEC", "PREINC", "PREDEC", "LIT", "IDREF", "IDLIST", "LABEL", "GOTO", "BLOCK", "TUPLE", "TUPLETYPE", "LABELSTMT", "BINDING", "FIELDREF", "ARRAY", "INDEX", "POINTER", "DEREF", "ADDRREF", "ADDROF", "SIZEOF", "TYPEOF", "INITEXPR", "INITLIST", "INSTANCE", "GENERIC", "SEMI", "FORWARD", "ID", "COMMA", "COLON_EQUALS", "COLON", "EQUALS", "PLUS", "PLUS_EQ", "LBRACKET", "RBRACKET", "EQUALS_COLON", "LBRACE", "RBRACE", "FOR", "IN", "ATTR", "LPAREN", "RPAREN", "ARROW", "NIL", "QUESTION", "CARET", "MINUS_EQ", "STAR_EQ", "SLASH_EQ", "REM_EQ", "UDIV_EQ", "UREM_EQ", "MOD_EQ", "AND_EQ", "OR_EQ", "XOR_EQ", "LSHIFT_EQ", "RSHIFT_EQ", "URSHIFT_EQ", "CLSHIFT_EQ", "CRSHIFT_EQ", "PERIOD", "DO", "WHILE", "REPEAT", "AND", "BY", "AT", "BREAK", "ATSIGN", "IF", "THEN", "ELIF", "ELSE", "FI", "OR", "NOT", "COMPEQ", "COMPNE", "COMPLE", "COMPGE", "COMPULE", "COMPUGE", "LESS", "ULESS", "GREATER", "UGREATER", "BAR", "TILDE", "AMP", "LSHIFT", "RSHIFT", "URSHIFT", "CRSHIFT", "CLSHIFT", "MINUS", "STAR", "SLASH", "REM", "UREM", "PLUSPLUS", "MINUSMINUS", "NUMBER", "CHAR_LITERAL", "STRING_LITERAL", "AS", "FALSE", "TRUE", "COLONS", "DATA", "STATIC", "COLON_COLON_EQUALS", "HASH", "POINTS", "BAR_BAR", "SELECT", "WHEN", "UNTIL", "WITH", "END", "IDSUFFIX", "LETTERLIKE", "DIGIT", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT"
    };
    public static final int CAST=27;
    public static final int CONDTEST=24;
    public static final int STAR=140;
    public static final int WHILE=107;
    public static final int GENERIC=66;
    public static final int MOD=37;
    public static final int POINTER=57;
    public static final int LSHIFT_EQ=100;
    public static final int PREDEC=43;
    public static final int REM_EQ=93;
    public static final int DEREF=58;
    public static final int MINUSMINUS=145;
    public static final int DO=106;
    public static final int ARGLIST=12;
    public static final int EQUALS=73;
    public static final int NOT=120;
    public static final int EOF=-1;
    public static final int BREAK=112;
    public static final int TYPE=21;
    public static final int CODE=9;
    public static final int LBRACKET=76;
    public static final int TUPLE=50;
    public static final int RPAREN=85;
    public static final int STRING_LITERAL=148;
    public static final int GREATER=129;
    public static final int ADDRREF=59;
    public static final int ADDSCOPE=5;
    public static final int UREM_EQ=95;
    public static final int EXTENDSCOPE=6;
    public static final int COMPLE=123;
    public static final int AND_EQ=97;
    public static final int CARET=89;
    public static final int LESS=127;
    public static final int XOR_EQ=99;
    public static final int INITEXPR=63;
    public static final int INITLIST=64;
    public static final int ATSIGN=113;
    public static final int GOTO=48;
    public static final int SELECT=159;
    public static final int CLSHIFT_EQ=103;
    public static final int ARRAY=55;
    public static final int LABELSTMT=52;
    public static final int CRSHIFT=137;
    public static final int RBRACE=80;
    public static final int STMTEXPR=22;
    public static final int STATIC=154;
    public static final int PERIOD=105;
    public static final int LSHIFT=134;
    public static final int INV=39;
    public static final int ADDROF=60;
    public static final int ELSE=117;
    public static final int NUMBER=146;
    public static final int UDIV=36;
    public static final int LIT=44;
    public static final int CRSHIFT_EQ=104;
    public static final int UDIV_EQ=94;
    public static final int LIST=20;
    public static final int PLUS_EQ=75;
    public static final int MUL=34;
    public static final int RSHIFT_EQ=101;
    public static final int ARGDEF=13;
    public static final int FI=118;
    public static final int MINUS_EQ=90;
    public static final int ELIF=116;
    public static final int WS=168;
    public static final int OR_EQ=98;
    public static final int BITOR=30;
    public static final int NIL=87;
    public static final int TYPEOF=62;
    public static final int UNTIL=161;
    public static final int STMTLIST=10;
    public static final int OR=119;
    public static final int SIZEOF=61;
    public static final int ALLOC=15;
    public static final int IDLIST=46;
    public static final int REPEAT=108;
    public static final int INLINE=26;
    public static final int CALL=25;
    public static final int POSTINC=40;
    public static final int END=163;
    public static final int FALSE=150;
    public static final int COMPULE=125;
    public static final int POSTDEC=41;
    public static final int MOD_EQ=96;
    public static final int BINDING=53;
    public static final int FORWARD=68;
    public static final int BAR_BAR=158;
    public static final int AMP=133;
    public static final int POINTS=157;
    public static final int PLUSPLUS=144;
    public static final int UGREATER=130;
    public static final int LBRACE=79;
    public static final int MULTI_COMMENT=170;
    public static final int FIELDREF=54;
    public static final int FOR=81;
    public static final int SUB=33;
    public static final int AND=109;
    public static final int ID=69;
    public static final int DEFINE=18;
    public static final int UREM=143;
    public static final int BITAND=29;
    public static final int LPAREN=84;
    public static final int IF=114;
    public static final int COLONS=152;
    public static final int COLON_COLON_EQUALS=155;
    public static final int AT=111;
    public static final int AS=149;
    public static final int INDEX=56;
    public static final int CONDLIST=23;
    public static final int IDSUFFIX=164;
    public static final int SLASH=141;
    public static final int EXPR=19;
    public static final int THEN=115;
    public static final int IN=82;
    public static final int SCOPE=4;
    public static final int COMMA=70;
    public static final int PREINC=42;
    public static final int BITXOR=31;
    public static final int TILDE=132;
    public static final int PLUS=74;
    public static final int SINGLE_COMMENT=169;
    public static final int DIGIT=166;
    public static final int RBRACKET=77;
    public static final int RSHIFT=135;
    public static final int ATTR=83;
    public static final int WITH=162;
    public static final int ADD=32;
    public static final int EQUALS_COLON=78;
    public static final int COMPGE=124;
    public static final int URSHIFT_EQ=102;
    public static final int ULESS=128;
    public static final int BY=110;
    public static final int LETTERLIKE=165;
    public static final int LIST_COMPREHENSION=8;
    public static final int HASH=156;
    public static final int CLSHIFT=138;
    public static final int ATTRS=7;
    public static final int STAR_EQ=91;
    public static final int REM=142;
    public static final int MINUS=139;
    public static final int TRUE=151;
    public static final int SEMI=67;
    public static final int REF=14;
    public static final int COLON=72;
    public static final int TUPLETYPE=51;
    public static final int COLON_EQUALS=71;
    public static final int NEWLINE=167;
    public static final int QUESTION=88;
    public static final int CHAR_LITERAL=147;
    public static final int LABEL=47;
    public static final int WHEN=160;
    public static final int INSTANCE=65;
    public static final int BLOCK=49;
    public static final int NEG=38;
    public static final int ASSIGN=17;
    public static final int URSHIFT=136;
    public static final int ARROW=86;
    public static final int COMPEQ=121;
    public static final int IDREF=45;
    public static final int DIV=35;
    public static final int COND=28;
    public static final int PROTO=11;
    public static final int COMPNE=122;
    public static final int DATA=153;
    public static final int BAR=131;
    public static final int COMPUGE=126;
    public static final int ALLOC_TUPLE=16;
    public static final int SLASH_EQ=92;

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:1: prog : toplevelstmts EOF ;
    public final EulangParser.prog_return prog() throws RecognitionException {
        EulangParser.prog_return retval = new EulangParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        EulangParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:129:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog440);
            toplevelstmts1=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog442); if (state.failed) return retval;

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:1: toplevelstmts : ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) ;
    public final EulangParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        EulangParser.toplevelstmts_return retval = new EulangParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstat_return toplevelstat3 = null;


        RewriteRuleSubtreeStream stream_toplevelstat=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstat");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:14: ( ( toplevelstat )* -> ^( STMTLIST ( toplevelstat )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:16: ( toplevelstat )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:16: ( toplevelstat )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==CODE||(LA1_0>=SIZEOF && LA1_0<=TYPEOF)||(LA1_0>=FORWARD && LA1_0<=ID)||LA1_0==COLON||LA1_0==LBRACE||LA1_0==LPAREN||LA1_0==NIL||LA1_0==IF||LA1_0==NOT||(LA1_0>=TILDE && LA1_0<=AMP)||LA1_0==MINUS||(LA1_0>=PLUSPLUS && LA1_0<=STRING_LITERAL)||(LA1_0>=FALSE && LA1_0<=COLONS)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:16: toplevelstat
            	    {
            	    pushFollow(FOLLOW_toplevelstat_in_toplevelstmts471);
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
            // 132:35: -> ^( STMTLIST ( toplevelstat )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:38: ^( STMTLIST ( toplevelstat )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:132:49: ( toplevelstat )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:1: toplevelstmtsNoAlloc : ( toplevelstatNoAlloc )* -> ^( STMTLIST ( toplevelstatNoAlloc )* ) ;
    public final EulangParser.toplevelstmtsNoAlloc_return toplevelstmtsNoAlloc() throws RecognitionException {
        EulangParser.toplevelstmtsNoAlloc_return retval = new EulangParser.toplevelstmtsNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc4 = null;


        RewriteRuleSubtreeStream stream_toplevelstatNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstatNoAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:21: ( ( toplevelstatNoAlloc )* -> ^( STMTLIST ( toplevelstatNoAlloc )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:23: ( toplevelstatNoAlloc )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:23: ( toplevelstatNoAlloc )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==CODE||(LA2_0>=SIZEOF && LA2_0<=TYPEOF)||(LA2_0>=FORWARD && LA2_0<=ID)||LA2_0==COLON||LA2_0==LBRACE||LA2_0==LPAREN||LA2_0==NIL||LA2_0==IF||LA2_0==NOT||(LA2_0>=TILDE && LA2_0<=AMP)||LA2_0==MINUS||(LA2_0>=PLUSPLUS && LA2_0<=STRING_LITERAL)||(LA2_0>=FALSE && LA2_0<=COLONS)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:23: toplevelstatNoAlloc
            	    {
            	    pushFollow(FOLLOW_toplevelstatNoAlloc_in_toplevelstmtsNoAlloc498);
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
            // 134:49: -> ^( STMTLIST ( toplevelstatNoAlloc )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:52: ^( STMTLIST ( toplevelstatNoAlloc )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:134:63: ( toplevelstatNoAlloc )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:1: toplevelstat : ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc );
    public final EulangParser.toplevelstat_return toplevelstat() throws RecognitionException {
        EulangParser.toplevelstat_return retval = new EulangParser.toplevelstat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI7=null;
        EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc5 = null;

        EulangParser.toplevelAlloc_return toplevelAlloc6 = null;


        CommonTree SEMI7_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_toplevelstatNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstatNoAlloc");
        RewriteRuleSubtreeStream stream_toplevelAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:14: ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc )
            int alt3=2;
            alt3 = dfa3.predict(input);
            switch (alt3) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:137:16: toplevelstatNoAlloc
                    {
                    pushFollow(FOLLOW_toplevelstatNoAlloc_in_toplevelstat527);
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
                    // 137:36: -> toplevelstatNoAlloc
                    {
                        adaptor.addChild(root_0, stream_toplevelstatNoAlloc.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:138:7: toplevelAlloc SEMI
                    {
                    pushFollow(FOLLOW_toplevelAlloc_in_toplevelstat540);
                    toplevelAlloc6=toplevelAlloc();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelAlloc.add(toplevelAlloc6.getTree());
                    SEMI7=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstat542); if (state.failed) return retval; 
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
                    // 138:26: -> toplevelAlloc
                    {
                        adaptor.addChild(root_0, stream_toplevelAlloc.nextTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:1: toplevelstatNoAlloc : ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | rhsExpr SEMI -> ^( EXPR rhsExpr ) | ( LBRACE )=> xscope ( SEMI )? );
    public final EulangParser.toplevelstatNoAlloc_return toplevelstatNoAlloc() throws RecognitionException {
        EulangParser.toplevelstatNoAlloc_return retval = new EulangParser.toplevelstatNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI10=null;
        Token FORWARD11=null;
        Token ID12=null;
        Token COMMA13=null;
        Token ID14=null;
        Token SEMI15=null;
        Token SEMI17=null;
        Token SEMI19=null;
        EulangParser.defineStmt_return defineStmt8 = null;

        EulangParser.scopeExtension_return scopeExtension9 = null;

        EulangParser.rhsExpr_return rhsExpr16 = null;

        EulangParser.xscope_return xscope18 = null;


        CommonTree SEMI10_tree=null;
        CommonTree FORWARD11_tree=null;
        CommonTree ID12_tree=null;
        CommonTree COMMA13_tree=null;
        CommonTree ID14_tree=null;
        CommonTree SEMI15_tree=null;
        CommonTree SEMI17_tree=null;
        CommonTree SEMI19_tree=null;
        RewriteRuleTokenStream stream_FORWARD=new RewriteRuleTokenStream(adaptor,"token FORWARD");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_scopeExtension=new RewriteRuleSubtreeStream(adaptor,"rule scopeExtension");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:20: ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | rhsExpr SEMI -> ^( EXPR rhsExpr ) | ( LBRACE )=> xscope ( SEMI )? )
            int alt7=5;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:140:22: defineStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_defineStmt_in_toplevelstatNoAlloc557);
                    defineStmt8=defineStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defineStmt8.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:7: scopeExtension ( SEMI )?
                    {
                    pushFollow(FOLLOW_scopeExtension_in_toplevelstatNoAlloc565);
                    scopeExtension9=scopeExtension();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_scopeExtension.add(scopeExtension9.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:22: ( SEMI )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==SEMI) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:141:22: SEMI
                            {
                            SEMI10=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc567); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_SEMI.add(SEMI10);


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
                    // 141:28: -> scopeExtension
                    {
                        adaptor.addChild(root_0, stream_scopeExtension.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:7: FORWARD ID ( COMMA ID )* SEMI
                    {
                    FORWARD11=(Token)match(input,FORWARD,FOLLOW_FORWARD_in_toplevelstatNoAlloc581); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FORWARD.add(FORWARD11);

                    ID12=(Token)match(input,ID,FOLLOW_ID_in_toplevelstatNoAlloc583); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID12);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:18: ( COMMA ID )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:19: COMMA ID
                    	    {
                    	    COMMA13=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelstatNoAlloc586); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA13);

                    	    ID14=(Token)match(input,ID,FOLLOW_ID_in_toplevelstatNoAlloc588); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID14);


                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);

                    SEMI15=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc592); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI15);



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
                    // 142:35: -> ( ^( FORWARD ID ) )+
                    {
                        if ( !(stream_ID.hasNext()||stream_FORWARD.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext()||stream_FORWARD.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:142:38: ^( FORWARD ID )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:143:7: rhsExpr SEMI
                    {
                    pushFollow(FOLLOW_rhsExpr_in_toplevelstatNoAlloc609);
                    rhsExpr16=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr16.getTree());
                    SEMI17=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc628); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI17);



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
                    // 143:38: -> ^( EXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:143:41: ^( EXPR rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:7: ( LBRACE )=> xscope ( SEMI )?
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelstatNoAlloc651);
                    xscope18=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope18.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:26: ( SEMI )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==SEMI) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:26: SEMI
                            {
                            SEMI19=(Token)match(input,SEMI,FOLLOW_SEMI_in_toplevelstatNoAlloc653); if (state.failed) return retval;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:1: toplevelAlloc : ( toplevelSingleVarDecl | toplevelTupleVarDecl );
    public final EulangParser.toplevelAlloc_return toplevelAlloc() throws RecognitionException {
        EulangParser.toplevelAlloc_return retval = new EulangParser.toplevelAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelSingleVarDecl_return toplevelSingleVarDecl20 = null;

        EulangParser.toplevelTupleVarDecl_return toplevelTupleVarDecl21 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:15: ( toplevelSingleVarDecl | toplevelTupleVarDecl )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ID) ) {
                alt8=1;
            }
            else if ( (LA8_0==LPAREN) ) {
                alt8=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:17: toplevelSingleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_toplevelSingleVarDecl_in_toplevelAlloc668);
                    toplevelSingleVarDecl20=toplevelSingleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelSingleVarDecl20.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:147:41: toplevelTupleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_toplevelTupleVarDecl_in_toplevelAlloc672);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:1: toplevelSingleVarDecl : ID ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ID TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ID type ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) ) ;
    public final EulangParser.toplevelSingleVarDecl_return toplevelSingleVarDecl() throws RecognitionException {
        EulangParser.toplevelSingleVarDecl_return retval = new EulangParser.toplevelSingleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID22=null;
        Token COLON_EQUALS23=null;
        Token COLON25=null;
        Token EQUALS27=null;
        Token COMMA29=null;
        Token ID30=null;
        Token COLON_EQUALS31=null;
        Token PLUS32=null;
        Token COMMA34=null;
        Token COLON36=null;
        Token EQUALS38=null;
        Token PLUS39=null;
        Token COMMA41=null;
        EulangParser.rhsExprOrInitList_return rhsExprOrInitList24 = null;

        EulangParser.type_return type26 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList28 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList33 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList35 = null;

        EulangParser.type_return type37 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList40 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList42 = null;


        CommonTree ID22_tree=null;
        CommonTree COLON_EQUALS23_tree=null;
        CommonTree COLON25_tree=null;
        CommonTree EQUALS27_tree=null;
        CommonTree COMMA29_tree=null;
        CommonTree ID30_tree=null;
        CommonTree COLON_EQUALS31_tree=null;
        CommonTree PLUS32_tree=null;
        CommonTree COMMA34_tree=null;
        CommonTree COLON36_tree=null;
        CommonTree EQUALS38_tree=null;
        CommonTree PLUS39_tree=null;
        CommonTree COMMA41_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_rhsExprOrInitList=new RewriteRuleSubtreeStream(adaptor,"rule rhsExprOrInitList");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:149:22: ( ID ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ID TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ID type ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:5: ID ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ID TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ID type ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) )
            {
            ID22=(Token)match(input,ID,FOLLOW_ID_in_toplevelSingleVarDecl683); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID22);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:150:8: ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ID TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ID type ( rhsExprOrInitList )* ) ) | ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) ) )
            int alt17=3;
            switch ( input.LA(1) ) {
            case COLON_EQUALS:
                {
                alt17=1;
                }
                break;
            case COLON:
                {
                alt17=2;
                }
                break;
            case COMMA:
                {
                alt17=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }

            switch (alt17) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ID TYPE rhsExprOrInitList ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC ID TYPE rhsExprOrInitList ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:11: COLON_EQUALS rhsExprOrInitList
                    {
                    COLON_EQUALS23=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl697); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS23);

                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl699);
                    rhsExprOrInitList24=rhsExprOrInitList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList24.getTree());


                    // AST REWRITE
                    // elements: ID, rhsExprOrInitList
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 151:50: -> ^( ALLOC ID TYPE rhsExprOrInitList )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:151:53: ^( ALLOC ID TYPE rhsExprOrInitList )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ID type ( rhsExprOrInitList )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC ID type ( rhsExprOrInitList )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:11: COLON type ( EQUALS rhsExprOrInitList )?
                    {
                    COLON25=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelSingleVarDecl733); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON25);

                    pushFollow(FOLLOW_type_in_toplevelSingleVarDecl735);
                    type26=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type26.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:22: ( EQUALS rhsExprOrInitList )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==EQUALS) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:23: EQUALS rhsExprOrInitList
                            {
                            EQUALS27=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelSingleVarDecl738); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS27);

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl740);
                            rhsExprOrInitList28=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList28.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: rhsExprOrInitList, type, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 152:51: -> ^( ALLOC ID type ( rhsExprOrInitList )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:54: ^( ALLOC ID type ( rhsExprOrInitList )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:152:70: ( rhsExprOrInitList )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:9: ( COMMA ID )+ ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:9: ( COMMA ID )+
                    int cnt10=0;
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0==COMMA) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:153:11: COMMA ID
                    	    {
                    	    COMMA29=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl770); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA29);

                    	    ID30=(Token)match(input,ID,FOLLOW_ID_in_toplevelSingleVarDecl772); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID30);


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt10 >= 1 ) break loop10;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(10, input);
                                throw eee;
                        }
                        cnt10++;
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:9: ( ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* ) -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) ) | ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? ) )
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==COLON_EQUALS) ) {
                        alt16=1;
                    }
                    else if ( (LA16_0==COLON) ) {
                        alt16=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 16, 0, input);

                        throw nvae;
                    }
                    switch (alt16) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:12: ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:12: ( COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:14: COLON_EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )*
                            {
                            COLON_EQUALS31=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl791); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS31);

                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:27: ( PLUS )?
                            int alt11=2;
                            int LA11_0 = input.LA(1);

                            if ( (LA11_0==PLUS) ) {
                                alt11=1;
                            }
                            switch (alt11) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:27: PLUS
                                    {
                                    PLUS32=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelSingleVarDecl793); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS32);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl796);
                            rhsExprOrInitList33=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList33.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:51: ( COMMA rhsExprOrInitList )*
                            loop12:
                            do {
                                int alt12=2;
                                int LA12_0 = input.LA(1);

                                if ( (LA12_0==COMMA) ) {
                                    alt12=1;
                                }


                                switch (alt12) {
                            	case 1 :
                            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:154:52: COMMA rhsExprOrInitList
                            	    {
                            	    COMMA34=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl799); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA34);

                            	    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl801);
                            	    rhsExprOrInitList35=rhsExprOrInitList();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList35.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop12;
                                }
                            } while (true);


                            }



                            // AST REWRITE
                            // elements: rhsExprOrInitList, ID, PLUS
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 155:15: -> ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:18: ^( ALLOC ^( LIST ( ID )+ ) TYPE ( PLUS )? ^( LIST ( rhsExprOrInitList )+ ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:26: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:43: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:155:49: ^( LIST ( rhsExprOrInitList )+ )
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
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:12: ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:12: ( COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )? )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:14: COLON type ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )?
                            {
                            COLON36=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelSingleVarDecl860); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON36);

                            pushFollow(FOLLOW_type_in_toplevelSingleVarDecl862);
                            type37=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type37.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:25: ( EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )* )?
                            int alt15=2;
                            int LA15_0 = input.LA(1);

                            if ( (LA15_0==EQUALS) ) {
                                alt15=1;
                            }
                            switch (alt15) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:26: EQUALS ( PLUS )? rhsExprOrInitList ( COMMA rhsExprOrInitList )*
                                    {
                                    EQUALS38=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelSingleVarDecl865); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS38);

                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:33: ( PLUS )?
                                    int alt13=2;
                                    int LA13_0 = input.LA(1);

                                    if ( (LA13_0==PLUS) ) {
                                        alt13=1;
                                    }
                                    switch (alt13) {
                                        case 1 :
                                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:33: PLUS
                                            {
                                            PLUS39=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelSingleVarDecl867); if (state.failed) return retval; 
                                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS39);


                                            }
                                            break;

                                    }

                                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl870);
                                    rhsExprOrInitList40=rhsExprOrInitList();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList40.getTree());
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:57: ( COMMA rhsExprOrInitList )*
                                    loop14:
                                    do {
                                        int alt14=2;
                                        int LA14_0 = input.LA(1);

                                        if ( (LA14_0==COMMA) ) {
                                            alt14=1;
                                        }


                                        switch (alt14) {
                                    	case 1 :
                                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:156:58: COMMA rhsExprOrInitList
                                    	    {
                                    	    COMMA41=(Token)match(input,COMMA,FOLLOW_COMMA_in_toplevelSingleVarDecl873); if (state.failed) return retval; 
                                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA41);

                                    	    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl875);
                                    	    rhsExprOrInitList42=rhsExprOrInitList();

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList42.getTree());

                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop14;
                                        }
                                    } while (true);


                                    }
                                    break;

                            }


                            }



                            // AST REWRITE
                            // elements: type, rhsExprOrInitList, PLUS, ID
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 157:15: -> ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:18: ^( ALLOC ^( LIST ( ID )+ ) type ( PLUS )? ( ^( LIST ( rhsExprOrInitList )+ ) )? )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:26: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:43: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:49: ( ^( LIST ( rhsExprOrInitList )+ ) )?
                                if ( stream_rhsExprOrInitList.hasNext() ) {
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:157:49: ^( LIST ( rhsExprOrInitList )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:1: toplevelTupleVarDecl : idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* ) ) ) ;
    public final EulangParser.toplevelTupleVarDecl_return toplevelTupleVarDecl() throws RecognitionException {
        EulangParser.toplevelTupleVarDecl_return retval = new EulangParser.toplevelTupleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON_EQUALS44=null;
        Token COLON46=null;
        Token EQUALS48=null;
        EulangParser.idTuple_return idTuple43 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList45 = null;

        EulangParser.type_return type47 = null;

        EulangParser.rhsExprOrInitList_return rhsExprOrInitList49 = null;


        CommonTree COLON_EQUALS44_tree=null;
        CommonTree COLON46_tree=null;
        CommonTree EQUALS48_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_rhsExprOrInitList=new RewriteRuleSubtreeStream(adaptor,"rule rhsExprOrInitList");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:162:21: ( idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:163:5: idTuple ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* ) ) )
            {
            pushFollow(FOLLOW_idTuple_in_toplevelTupleVarDecl966);
            idTuple43=idTuple();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTuple.add(idTuple43.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:164:7: ( ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList ) ) | ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* ) ) )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==COLON_EQUALS) ) {
                alt19=1;
            }
            else if ( (LA19_0==COLON) ) {
                alt19=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:9: ( COLON_EQUALS rhsExprOrInitList -> ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:11: COLON_EQUALS rhsExprOrInitList
                    {
                    COLON_EQUALS44=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_toplevelTupleVarDecl988); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS44);

                    pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl990);
                    rhsExprOrInitList45=rhsExprOrInitList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList45.getTree());


                    // AST REWRITE
                    // elements: rhsExprOrInitList, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 165:50: -> ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:165:53: ^( ALLOC_TUPLE idTuple TYPE rhsExprOrInitList )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_rhsExprOrInitList.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:9: ( COLON type ( EQUALS rhsExprOrInitList )? -> ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:11: COLON type ( EQUALS rhsExprOrInitList )?
                    {
                    COLON46=(Token)match(input,COLON,FOLLOW_COLON_in_toplevelTupleVarDecl1024); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON46);

                    pushFollow(FOLLOW_type_in_toplevelTupleVarDecl1026);
                    type47=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type47.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:22: ( EQUALS rhsExprOrInitList )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==EQUALS) ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:23: EQUALS rhsExprOrInitList
                            {
                            EQUALS48=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_toplevelTupleVarDecl1029); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS48);

                            pushFollow(FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1031);
                            rhsExprOrInitList49=rhsExprOrInitList();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rhsExprOrInitList.add(rhsExprOrInitList49.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: idTuple, type, rhsExprOrInitList
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 166:51: -> ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:54: ^( ALLOC_TUPLE idTuple type ( rhsExprOrInitList )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:166:81: ( rhsExprOrInitList )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:1: rhsExprOrInitList : ( rhsExpr | initList );
    public final EulangParser.rhsExprOrInitList_return rhsExprOrInitList() throws RecognitionException {
        EulangParser.rhsExprOrInitList_return retval = new EulangParser.rhsExprOrInitList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr50 = null;

        EulangParser.initList_return initList51 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:19: ( rhsExpr | initList )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==CODE||(LA20_0>=SIZEOF && LA20_0<=TYPEOF)||LA20_0==ID||LA20_0==COLON||LA20_0==LPAREN||LA20_0==NIL||LA20_0==IF||LA20_0==NOT||(LA20_0>=TILDE && LA20_0<=AMP)||LA20_0==MINUS||(LA20_0>=PLUSPLUS && LA20_0<=STRING_LITERAL)||(LA20_0>=FALSE && LA20_0<=COLONS)) ) {
                alt20=1;
            }
            else if ( (LA20_0==LBRACKET) ) {
                alt20=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:21: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_rhsExprOrInitList1074);
                    rhsExpr50=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr50.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:170:31: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_rhsExprOrInitList1078);
                    initList51=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList51.getTree());

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

    public static class scopeExtension_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "scopeExtension"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:1: scopeExtension : namespaceRef PLUS_EQ xscopeNoAlloc -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc ) ;
    public final EulangParser.scopeExtension_return scopeExtension() throws RecognitionException {
        EulangParser.scopeExtension_return retval = new EulangParser.scopeExtension_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS_EQ53=null;
        EulangParser.namespaceRef_return namespaceRef52 = null;

        EulangParser.xscopeNoAlloc_return xscopeNoAlloc54 = null;


        CommonTree PLUS_EQ53_tree=null;
        RewriteRuleTokenStream stream_PLUS_EQ=new RewriteRuleTokenStream(adaptor,"token PLUS_EQ");
        RewriteRuleSubtreeStream stream_xscopeNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule xscopeNoAlloc");
        RewriteRuleSubtreeStream stream_namespaceRef=new RewriteRuleSubtreeStream(adaptor,"rule namespaceRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:16: ( namespaceRef PLUS_EQ xscopeNoAlloc -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:18: namespaceRef PLUS_EQ xscopeNoAlloc
            {
            pushFollow(FOLLOW_namespaceRef_in_scopeExtension1087);
            namespaceRef52=namespaceRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef52.getTree());
            PLUS_EQ53=(Token)match(input,PLUS_EQ,FOLLOW_PLUS_EQ_in_scopeExtension1089); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_PLUS_EQ.add(PLUS_EQ53);

            pushFollow(FOLLOW_xscopeNoAlloc_in_scopeExtension1091);
            xscopeNoAlloc54=xscopeNoAlloc();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_xscopeNoAlloc.add(xscopeNoAlloc54.getTree());


            // AST REWRITE
            // elements: namespaceRef, xscopeNoAlloc
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 172:53: -> ^( EXTENDSCOPE namespaceRef xscopeNoAlloc )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:172:56: ^( EXTENDSCOPE namespaceRef xscopeNoAlloc )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:1: defineStmt : ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) );
    public final EulangParser.defineStmt_return defineStmt() throws RecognitionException {
        EulangParser.defineStmt_return retval = new EulangParser.defineStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID55=null;
        Token EQUALS56=null;
        Token LBRACKET57=null;
        Token RBRACKET59=null;
        Token SEMI61=null;
        Token ID62=null;
        Token EQUALS_COLON63=null;
        Token SEMI65=null;
        Token ID66=null;
        Token EQUALS67=null;
        Token SEMI69=null;
        EulangParser.idlistOrEmpty_return idlistOrEmpty58 = null;

        EulangParser.toplevelvalue_return toplevelvalue60 = null;

        EulangParser.type_return type64 = null;

        EulangParser.toplevelvalue_return toplevelvalue68 = null;


        CommonTree ID55_tree=null;
        CommonTree EQUALS56_tree=null;
        CommonTree LBRACKET57_tree=null;
        CommonTree RBRACKET59_tree=null;
        CommonTree SEMI61_tree=null;
        CommonTree ID62_tree=null;
        CommonTree EQUALS_COLON63_tree=null;
        CommonTree SEMI65_tree=null;
        CommonTree ID66_tree=null;
        CommonTree EQUALS67_tree=null;
        CommonTree SEMI69_tree=null;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:12: ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) )
            int alt21=3;
            alt21 = dfa21.predict(input);
            switch (alt21) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:14: ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI
                    {
                    ID55=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1120); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID55);

                    EQUALS56=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_defineStmt1122); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS56);

                    LBRACKET57=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_defineStmt1124); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET57);

                    pushFollow(FOLLOW_idlistOrEmpty_in_defineStmt1126);
                    idlistOrEmpty58=idlistOrEmpty();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idlistOrEmpty.add(idlistOrEmpty58.getTree());
                    RBRACKET59=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_defineStmt1128); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET59);

                    pushFollow(FOLLOW_toplevelvalue_in_defineStmt1131);
                    toplevelvalue60=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue60.getTree());
                    SEMI61=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1137); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI61);



                    // AST REWRITE
                    // elements: idlistOrEmpty, toplevelvalue, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 174:105: -> ^( DEFINE ID idlistOrEmpty toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:108: ^( DEFINE ID idlistOrEmpty toplevelvalue )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:7: ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI
                    {
                    ID62=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1167); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID62);

                    EQUALS_COLON63=(Token)match(input,EQUALS_COLON,FOLLOW_EQUALS_COLON_in_defineStmt1169); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS_COLON.add(EQUALS_COLON63);

                    pushFollow(FOLLOW_type_in_defineStmt1171);
                    type64=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type64.getTree());
                    SEMI65=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1177); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI65);



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
                    // 175:59: -> ^( DEFINE ID type )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:62: ^( DEFINE ID type )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:7: ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI
                    {
                    ID66=(Token)match(input,ID,FOLLOW_ID_in_defineStmt1204); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID66);

                    EQUALS67=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_defineStmt1206); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS67);

                    pushFollow(FOLLOW_toplevelvalue_in_defineStmt1208);
                    toplevelvalue68=toplevelvalue();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_toplevelvalue.add(toplevelvalue68.getTree());
                    SEMI69=(Token)match(input,SEMI,FOLLOW_SEMI_in_defineStmt1214); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI69);



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
                    // 176:56: -> ^( DEFINE ID toplevelvalue )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:59: ^( DEFINE ID toplevelvalue )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:1: toplevelvalue : ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data );
    public final EulangParser.toplevelvalue_return toplevelvalue() throws RecognitionException {
        EulangParser.toplevelvalue_return retval = new EulangParser.toplevelvalue_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS72=null;
        Token PLUS75=null;
        EulangParser.xscope_return xscope70 = null;

        EulangParser.namespaceRef_return namespaceRef71 = null;

        EulangParser.data_return data73 = null;

        EulangParser.namespaceRef_return namespaceRef74 = null;

        EulangParser.xscope_return xscope76 = null;

        EulangParser.selector_return selector77 = null;

        EulangParser.rhsExpr_return rhsExpr78 = null;

        EulangParser.data_return data79 = null;


        CommonTree PLUS72_tree=null;
        CommonTree PLUS75_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleSubtreeStream stream_xscope=new RewriteRuleSubtreeStream(adaptor,"rule xscope");
        RewriteRuleSubtreeStream stream_data=new RewriteRuleSubtreeStream(adaptor,"rule data");
        RewriteRuleSubtreeStream stream_namespaceRef=new RewriteRuleSubtreeStream(adaptor,"rule namespaceRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:15: ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data )
            int alt22=6;
            alt22 = dfa22.predict(input);
            switch (alt22) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:17: ( LBRACE )=> xscope
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_xscope_in_toplevelvalue1242);
                    xscope70=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, xscope70.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:7: namespaceRef PLUS data
                    {
                    pushFollow(FOLLOW_namespaceRef_in_toplevelvalue1250);
                    namespaceRef71=namespaceRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef71.getTree());
                    PLUS72=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelvalue1252); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS72);

                    pushFollow(FOLLOW_data_in_toplevelvalue1254);
                    data73=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_data.add(data73.getTree());


                    // AST REWRITE
                    // elements: namespaceRef, data
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 180:30: -> ^( ADDSCOPE namespaceRef data )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:180:33: ^( ADDSCOPE namespaceRef data )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:7: namespaceRef PLUS xscope
                    {
                    pushFollow(FOLLOW_namespaceRef_in_toplevelvalue1272);
                    namespaceRef74=namespaceRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_namespaceRef.add(namespaceRef74.getTree());
                    PLUS75=(Token)match(input,PLUS,FOLLOW_PLUS_in_toplevelvalue1274); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS75);

                    pushFollow(FOLLOW_xscope_in_toplevelvalue1276);
                    xscope76=xscope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xscope.add(xscope76.getTree());


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
                    // 181:32: -> ^( ADDSCOPE namespaceRef xscope )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:181:35: ^( ADDSCOPE namespaceRef xscope )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:182:7: selector
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_selector_in_toplevelvalue1294);
                    selector77=selector();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, selector77.getTree());

                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:183:7: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_toplevelvalue1302);
                    rhsExpr78=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr78.getTree());

                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:184:7: data
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_data_in_toplevelvalue1310);
                    data79=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, data79.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:1: selector : LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) ;
    public final EulangParser.selector_return selector() throws RecognitionException {
        EulangParser.selector_return retval = new EulangParser.selector_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET80=null;
        Token RBRACKET82=null;
        EulangParser.selectors_return selectors81 = null;


        CommonTree LBRACKET80_tree=null;
        CommonTree RBRACKET82_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_selectors=new RewriteRuleSubtreeStream(adaptor,"rule selectors");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:9: ( LBRACKET selectors RBRACKET -> ^( LIST ( selectors )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:11: LBRACKET selectors RBRACKET
            {
            LBRACKET80=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_selector1323); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET80);

            pushFollow(FOLLOW_selectors_in_selector1325);
            selectors81=selectors();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_selectors.add(selectors81.getTree());
            RBRACKET82=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_selector1327); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET82);



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
            // 188:42: -> ^( LIST ( selectors )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:45: ^( LIST ( selectors )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:188:52: ( selectors )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:1: selectors : ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* ;
    public final EulangParser.selectors_return selectors() throws RecognitionException {
        EulangParser.selectors_return retval = new EulangParser.selectors_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA84=null;
        Token COMMA86=null;
        EulangParser.selectoritem_return selectoritem83 = null;

        EulangParser.selectoritem_return selectoritem85 = null;


        CommonTree COMMA84_tree=null;
        CommonTree COMMA86_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_selectoritem=new RewriteRuleSubtreeStream(adaptor,"rule selectoritem");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:10: ( ( selectoritem ( COMMA selectoritem )* ( COMMA )? )? -> ( selectoritem )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:12: ( selectoritem ( COMMA selectoritem )* ( COMMA )? )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==CODE||(LA25_0>=SIZEOF && LA25_0<=TYPEOF)||LA25_0==ID||LA25_0==COLON||LA25_0==LPAREN||LA25_0==NIL||LA25_0==IF||LA25_0==NOT||(LA25_0>=TILDE && LA25_0<=AMP)||LA25_0==MINUS||(LA25_0>=PLUSPLUS && LA25_0<=STRING_LITERAL)||(LA25_0>=FALSE && LA25_0<=COLONS)) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:13: selectoritem ( COMMA selectoritem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_selectoritem_in_selectors1353);
                    selectoritem83=selectoritem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem83.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:26: ( COMMA selectoritem )*
                    loop23:
                    do {
                        int alt23=2;
                        int LA23_0 = input.LA(1);

                        if ( (LA23_0==COMMA) ) {
                            int LA23_1 = input.LA(2);

                            if ( (LA23_1==CODE||(LA23_1>=SIZEOF && LA23_1<=TYPEOF)||LA23_1==ID||LA23_1==COLON||LA23_1==LPAREN||LA23_1==NIL||LA23_1==IF||LA23_1==NOT||(LA23_1>=TILDE && LA23_1<=AMP)||LA23_1==MINUS||(LA23_1>=PLUSPLUS && LA23_1<=STRING_LITERAL)||(LA23_1>=FALSE && LA23_1<=COLONS)) ) {
                                alt23=1;
                            }


                        }


                        switch (alt23) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:28: COMMA selectoritem
                    	    {
                    	    COMMA84=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors1357); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA84);

                    	    pushFollow(FOLLOW_selectoritem_in_selectors1359);
                    	    selectoritem85=selectoritem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_selectoritem.add(selectoritem85.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:50: ( COMMA )?
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==COMMA) ) {
                        alt24=1;
                    }
                    switch (alt24) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:50: COMMA
                            {
                            COMMA86=(Token)match(input,COMMA,FOLLOW_COMMA_in_selectors1364); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA86);


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
            // 191:62: -> ( selectoritem )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:191:65: ( selectoritem )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:1: selectoritem : rhsExpr ;
    public final EulangParser.selectoritem_return selectoritem() throws RecognitionException {
        EulangParser.selectoritem_return retval = new EulangParser.selectoritem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr87 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:14: ( rhsExpr )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:194:17: rhsExpr
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_rhsExpr_in_selectoritem1395);
            rhsExpr87=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr87.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:1: xscope : LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) ;
    public final EulangParser.xscope_return xscope() throws RecognitionException {
        EulangParser.xscope_return retval = new EulangParser.xscope_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE88=null;
        Token RBRACE90=null;
        EulangParser.toplevelstmts_return toplevelstmts89 = null;


        CommonTree LBRACE88_tree=null;
        CommonTree RBRACE90_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmts=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmts");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:8: ( LBRACE toplevelstmts RBRACE -> ^( SCOPE ( toplevelstmts )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:10: LBRACE toplevelstmts RBRACE
            {
            LBRACE88=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscope1406); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE88);

            pushFollow(FOLLOW_toplevelstmts_in_xscope1408);
            toplevelstmts89=toplevelstmts();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmts.add(toplevelstmts89.getTree());
            RBRACE90=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscope1410); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE90);



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
            // 198:41: -> ^( SCOPE ( toplevelstmts )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:44: ^( SCOPE ( toplevelstmts )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:198:52: ( toplevelstmts )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:1: xscopeNoAlloc : LBRACE toplevelstmtsNoAlloc RBRACE -> ^( SCOPE ( toplevelstmtsNoAlloc )* ) ;
    public final EulangParser.xscopeNoAlloc_return xscopeNoAlloc() throws RecognitionException {
        EulangParser.xscopeNoAlloc_return retval = new EulangParser.xscopeNoAlloc_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE91=null;
        Token RBRACE93=null;
        EulangParser.toplevelstmtsNoAlloc_return toplevelstmtsNoAlloc92 = null;


        CommonTree LBRACE91_tree=null;
        CommonTree RBRACE93_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_toplevelstmtsNoAlloc=new RewriteRuleSubtreeStream(adaptor,"rule toplevelstmtsNoAlloc");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:15: ( LBRACE toplevelstmtsNoAlloc RBRACE -> ^( SCOPE ( toplevelstmtsNoAlloc )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:17: LBRACE toplevelstmtsNoAlloc RBRACE
            {
            LBRACE91=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_xscopeNoAlloc1435); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE91);

            pushFollow(FOLLOW_toplevelstmtsNoAlloc_in_xscopeNoAlloc1437);
            toplevelstmtsNoAlloc92=toplevelstmtsNoAlloc();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_toplevelstmtsNoAlloc.add(toplevelstmtsNoAlloc92.getTree());
            RBRACE93=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_xscopeNoAlloc1439); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE93);



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
            // 200:55: -> ^( SCOPE ( toplevelstmtsNoAlloc )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:58: ^( SCOPE ( toplevelstmtsNoAlloc )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SCOPE, "SCOPE"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:200:66: ( toplevelstmtsNoAlloc )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:1: listCompr : ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) ;
    public final EulangParser.listCompr_return listCompr() throws RecognitionException {
        EulangParser.listCompr_return retval = new EulangParser.listCompr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON95=null;
        EulangParser.forIn_return forIn94 = null;

        EulangParser.listiterable_return listiterable96 = null;


        CommonTree COLON95_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_forIn=new RewriteRuleSubtreeStream(adaptor,"rule forIn");
        RewriteRuleSubtreeStream stream_listiterable=new RewriteRuleSubtreeStream(adaptor,"rule listiterable");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:10: ( ( forIn )+ COLON listiterable -> ^( LIST_COMPREHENSION ( forIn )+ listiterable ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:12: ( forIn )+ COLON listiterable
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:12: ( forIn )+
            int cnt26=0;
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==FOR) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:12: forIn
            	    {
            	    pushFollow(FOLLOW_forIn_in_listCompr1466);
            	    forIn94=forIn();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_forIn.add(forIn94.getTree());

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

            COLON95=(Token)match(input,COLON,FOLLOW_COLON_in_listCompr1469); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON95);

            pushFollow(FOLLOW_listiterable_in_listCompr1471);
            listiterable96=listiterable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listiterable.add(listiterable96.getTree());


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
            // 205:42: -> ^( LIST_COMPREHENSION ( forIn )+ listiterable )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:205:45: ^( LIST_COMPREHENSION ( forIn )+ listiterable )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:1: forIn : FOR idlist IN list -> ^( FOR idlist list ) ;
    public final EulangParser.forIn_return forIn() throws RecognitionException {
        EulangParser.forIn_return retval = new EulangParser.forIn_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR97=null;
        Token IN99=null;
        EulangParser.idlist_return idlist98 = null;

        EulangParser.list_return list100 = null;


        CommonTree FOR97_tree=null;
        CommonTree IN99_tree=null;
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_list=new RewriteRuleSubtreeStream(adaptor,"rule list");
        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:7: ( FOR idlist IN list -> ^( FOR idlist list ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:9: FOR idlist IN list
            {
            FOR97=(Token)match(input,FOR,FOLLOW_FOR_in_forIn1503); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR97);

            pushFollow(FOLLOW_idlist_in_forIn1505);
            idlist98=idlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idlist.add(idlist98.getTree());
            IN99=(Token)match(input,IN,FOLLOW_IN_in_forIn1507); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN99);

            pushFollow(FOLLOW_list_in_forIn1509);
            list100=list();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_list.add(list100.getTree());


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
            // 208:33: -> ^( FOR idlist list )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:208:36: ^( FOR idlist list )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:1: idlist : ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) ;
    public final EulangParser.idlist_return idlist() throws RecognitionException {
        EulangParser.idlist_return retval = new EulangParser.idlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID101=null;
        Token COMMA102=null;
        Token ID103=null;

        CommonTree ID101_tree=null;
        CommonTree COMMA102_tree=null;
        CommonTree ID103_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:8: ( ID ( COMMA ID )* -> ^( IDLIST ( ID )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:10: ID ( COMMA ID )*
            {
            ID101=(Token)match(input,ID,FOLLOW_ID_in_idlist1534); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID101);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:13: ( COMMA ID )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==COMMA) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:14: COMMA ID
            	    {
            	    COMMA102=(Token)match(input,COMMA,FOLLOW_COMMA_in_idlist1537); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA102);

            	    ID103=(Token)match(input,ID,FOLLOW_ID_in_idlist1539); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID103);


            	    }
            	    break;

            	default :
            	    break loop27;
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
            // 210:28: -> ^( IDLIST ( ID )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:210:31: ^( IDLIST ( ID )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:1: idlistOrEmpty : ( idlist -> idlist | -> ^( IDLIST ) );
    public final EulangParser.idlistOrEmpty_return idlistOrEmpty() throws RecognitionException {
        EulangParser.idlistOrEmpty_return retval = new EulangParser.idlistOrEmpty_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.idlist_return idlist104 = null;


        RewriteRuleSubtreeStream stream_idlist=new RewriteRuleSubtreeStream(adaptor,"rule idlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:15: ( idlist -> idlist | -> ^( IDLIST ) )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( (LA28_0==ID) ) {
                alt28=1;
            }
            else if ( (LA28_0==RBRACKET) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:17: idlist
                    {
                    pushFollow(FOLLOW_idlist_in_idlistOrEmpty1565);
                    idlist104=idlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idlist.add(idlist104.getTree());


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
                    // 212:24: -> idlist
                    {
                        adaptor.addChild(root_0, stream_idlist.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:36: 
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
                    // 212:36: -> ^( IDLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:212:39: ^( IDLIST )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:1: listiterable : code ;
    public final EulangParser.listiterable_return listiterable() throws RecognitionException {
        EulangParser.listiterable_return retval = new EulangParser.listiterable_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.code_return code105 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:14: ( code )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:214:16: code
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_code_in_listiterable1586);
            code105=code();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, code105.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:1: list : LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) ;
    public final EulangParser.list_return list() throws RecognitionException {
        EulangParser.list_return retval = new EulangParser.list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET106=null;
        Token RBRACKET108=null;
        EulangParser.listitems_return listitems107 = null;


        CommonTree LBRACKET106_tree=null;
        CommonTree RBRACKET108_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_listitems=new RewriteRuleSubtreeStream(adaptor,"rule listitems");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:6: ( LBRACKET listitems RBRACKET -> ^( LIST ( listitems )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:8: LBRACKET listitems RBRACKET
            {
            LBRACKET106=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_list1600); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET106);

            pushFollow(FOLLOW_listitems_in_list1602);
            listitems107=listitems();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_listitems.add(listitems107.getTree());
            RBRACKET108=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_list1604); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET108);



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
            // 216:40: -> ^( LIST ( listitems )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:43: ^( LIST ( listitems )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:216:50: ( listitems )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:1: listitems : ( listitem ( COMMA listitem )* ( COMMA )? )? ;
    public final EulangParser.listitems_return listitems() throws RecognitionException {
        EulangParser.listitems_return retval = new EulangParser.listitems_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA110=null;
        Token COMMA112=null;
        EulangParser.listitem_return listitem109 = null;

        EulangParser.listitem_return listitem111 = null;


        CommonTree COMMA110_tree=null;
        CommonTree COMMA112_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:10: ( ( listitem ( COMMA listitem )* ( COMMA )? )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:12: ( listitem ( COMMA listitem )* ( COMMA )? )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==CODE||(LA31_0>=SIZEOF && LA31_0<=TYPEOF)||LA31_0==ID||LA31_0==COLON||LA31_0==LBRACKET||LA31_0==LBRACE||LA31_0==LPAREN||LA31_0==NIL||LA31_0==IF||LA31_0==NOT||(LA31_0>=TILDE && LA31_0<=AMP)||LA31_0==MINUS||(LA31_0>=PLUSPLUS && LA31_0<=STRING_LITERAL)||(LA31_0>=FALSE && LA31_0<=DATA)) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:13: listitem ( COMMA listitem )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_listitem_in_listitems1634);
                    listitem109=listitem();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem109.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:22: ( COMMA listitem )*
                    loop29:
                    do {
                        int alt29=2;
                        int LA29_0 = input.LA(1);

                        if ( (LA29_0==COMMA) ) {
                            int LA29_1 = input.LA(2);

                            if ( (LA29_1==CODE||(LA29_1>=SIZEOF && LA29_1<=TYPEOF)||LA29_1==ID||LA29_1==COLON||LA29_1==LBRACKET||LA29_1==LBRACE||LA29_1==LPAREN||LA29_1==NIL||LA29_1==IF||LA29_1==NOT||(LA29_1>=TILDE && LA29_1<=AMP)||LA29_1==MINUS||(LA29_1>=PLUSPLUS && LA29_1<=STRING_LITERAL)||(LA29_1>=FALSE && LA29_1<=DATA)) ) {
                                alt29=1;
                            }


                        }


                        switch (alt29) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:24: COMMA listitem
                    	    {
                    	    COMMA110=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems1638); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA110_tree = (CommonTree)adaptor.create(COMMA110);
                    	    adaptor.addChild(root_0, COMMA110_tree);
                    	    }
                    	    pushFollow(FOLLOW_listitem_in_listitems1640);
                    	    listitem111=listitem();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, listitem111.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop29;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:42: ( COMMA )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0==COMMA) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:219:42: COMMA
                            {
                            COMMA112=(Token)match(input,COMMA,FOLLOW_COMMA_in_listitems1645); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA112_tree = (CommonTree)adaptor.create(COMMA112);
                            adaptor.addChild(root_0, COMMA112_tree);
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:1: listitem : toplevelvalue ;
    public final EulangParser.listitem_return listitem() throws RecognitionException {
        EulangParser.listitem_return retval = new EulangParser.listitem_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.toplevelvalue_return toplevelvalue113 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:10: ( toplevelvalue )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:222:12: toplevelvalue
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelvalue_in_listitem1671);
            toplevelvalue113=toplevelvalue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, toplevelvalue113.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:1: code : CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* ) ;
    public final EulangParser.code_return code() throws RecognitionException {
        EulangParser.code_return retval = new EulangParser.code_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE114=null;
        Token LBRACE117=null;
        Token RBRACE119=null;
        EulangParser.attrs_return attrs115 = null;

        EulangParser.proto_return proto116 = null;

        EulangParser.codestmtlist_return codestmtlist118 = null;


        CommonTree CODE114_tree=null;
        CommonTree LBRACE117_tree=null;
        CommonTree RBRACE119_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:6: ( CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:8: CODE ( attrs )? ( proto )? LBRACE codestmtlist RBRACE
            {
            CODE114=(Token)match(input,CODE,FOLLOW_CODE_in_code1689); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_CODE.add(CODE114);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:13: ( attrs )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==ATTR) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:13: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_code1691);
                    attrs115=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs115.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:20: ( proto )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==LPAREN) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:20: proto
                    {
                    pushFollow(FOLLOW_proto_in_code1694);
                    proto116=proto();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_proto.add(proto116.getTree());

                    }
                    break;

            }

            LBRACE117=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_code1697); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE117);

            pushFollow(FOLLOW_codestmtlist_in_code1699);
            codestmtlist118=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist118.getTree());
            RBRACE119=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_code1701); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE119);



            // AST REWRITE
            // elements: CODE, codestmtlist, attrs, proto
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 227:54: -> ^( CODE ( attrs )? ( proto )? ( codestmtlist )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:57: ^( CODE ( attrs )? ( proto )? ( codestmtlist )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:64: ( attrs )?
                if ( stream_attrs.hasNext() ) {
                    adaptor.addChild(root_1, stream_attrs.nextTree());

                }
                stream_attrs.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:71: ( proto )?
                if ( stream_proto.hasNext() ) {
                    adaptor.addChild(root_1, stream_proto.nextTree());

                }
                stream_proto.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:227:78: ( codestmtlist )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:1: attrs : ( ATTR )+ -> ^( ATTRS ( ATTR )+ ) ;
    public final EulangParser.attrs_return attrs() throws RecognitionException {
        EulangParser.attrs_return retval = new EulangParser.attrs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATTR120=null;

        CommonTree ATTR120_tree=null;
        RewriteRuleTokenStream stream_ATTR=new RewriteRuleTokenStream(adaptor,"token ATTR");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:7: ( ( ATTR )+ -> ^( ATTRS ( ATTR )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:9: ( ATTR )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:9: ( ATTR )+
            int cnt34=0;
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==ATTR) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:9: ATTR
            	    {
            	    ATTR120=(Token)match(input,ATTR,FOLLOW_ATTR_in_attrs1731); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ATTR.add(ATTR120);


            	    }
            	    break;

            	default :
            	    if ( cnt34 >= 1 ) break loop34;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(34, input);
                        throw eee;
                }
                cnt34++;
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
            // 230:17: -> ^( ATTRS ( ATTR )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:230:19: ^( ATTRS ( ATTR )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:1: argdefs options {backtrack=true; } : ( | argdefsWithTypes | argdefWithType | argdefsWithNames );
    public final EulangParser.argdefs_return argdefs() throws RecognitionException {
        EulangParser.argdefs_return retval = new EulangParser.argdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.argdefsWithTypes_return argdefsWithTypes121 = null;

        EulangParser.argdefWithType_return argdefWithType122 = null;

        EulangParser.argdefsWithNames_return argdefsWithNames123 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:235:40: ( | argdefsWithTypes | argdefWithType | argdefsWithNames )
            int alt35=4;
            int LA35_0 = input.LA(1);

            if ( ((LA35_0>=RPAREN && LA35_0<=ARROW)) ) {
                alt35=1;
            }
            else if ( (LA35_0==ID) ) {
                int LA35_3 = input.LA(2);

                if ( (synpred7_Eulang()) ) {
                    alt35=2;
                }
                else if ( (synpred8_Eulang()) ) {
                    alt35=3;
                }
                else if ( (true) ) {
                    alt35=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 35, 3, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }
            switch (alt35) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:3: 
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:5: argdefsWithTypes
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithTypes_in_argdefs1775);
                    argdefsWithTypes121=argdefsWithTypes();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithTypes121.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:5: argdefWithType
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefWithType_in_argdefs1782);
                    argdefWithType122=argdefWithType();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefWithType122.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:238:6: argdefsWithNames
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argdefsWithNames_in_argdefs1789);
                    argdefsWithNames123=argdefsWithNames();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argdefsWithNames123.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:1: argdefsWithTypes : ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* ;
    public final EulangParser.argdefsWithTypes_return argdefsWithTypes() throws RecognitionException {
        EulangParser.argdefsWithTypes_return retval = new EulangParser.argdefsWithTypes_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI125=null;
        Token SEMI127=null;
        EulangParser.argdefWithType_return argdefWithType124 = null;

        EulangParser.argdefWithType_return argdefWithType126 = null;


        CommonTree SEMI125_tree=null;
        CommonTree SEMI127_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_argdefWithType=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithType");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:17: ( ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? ) -> ( argdefWithType )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:19: ( argdefWithType ( SEMI argdefWithType )+ ( SEMI )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:20: argdefWithType ( SEMI argdefWithType )+ ( SEMI )?
            {
            pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1805);
            argdefWithType124=argdefWithType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType124.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:35: ( SEMI argdefWithType )+
            int cnt36=0;
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==SEMI) ) {
                    int LA36_1 = input.LA(2);

                    if ( (LA36_1==ID) ) {
                        alt36=1;
                    }


                }


                switch (alt36) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:37: SEMI argdefWithType
            	    {
            	    SEMI125=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1809); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI125);

            	    pushFollow(FOLLOW_argdefWithType_in_argdefsWithTypes1811);
            	    argdefWithType126=argdefWithType();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithType.add(argdefWithType126.getTree());

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

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:59: ( SEMI )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==SEMI) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:59: SEMI
                    {
                    SEMI127=(Token)match(input,SEMI,FOLLOW_SEMI_in_argdefsWithTypes1815); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI127);


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
            // 241:73: -> ( argdefWithType )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:241:76: ( argdefWithType )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:1: argdefWithType : ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+ ;
    public final EulangParser.argdefWithType_return argdefWithType() throws RecognitionException {
        EulangParser.argdefWithType_return retval = new EulangParser.argdefWithType_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID128=null;
        Token COMMA129=null;
        Token ID130=null;
        Token COLON132=null;
        Token EQUALS134=null;
        EulangParser.rhsExpr_return init = null;

        EulangParser.attrs_return attrs131 = null;

        EulangParser.type_return type133 = null;


        CommonTree ID128_tree=null;
        CommonTree COMMA129_tree=null;
        CommonTree ID130_tree=null;
        CommonTree COLON132_tree=null;
        CommonTree EQUALS134_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:15: ( ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )? -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:18: ID ( COMMA ID )* ( attrs )? ( COLON type )? ( EQUALS init= rhsExpr )?
            {
            ID128=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1844); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID128);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:21: ( COMMA ID )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==COMMA) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:22: COMMA ID
            	    {
            	    COMMA129=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefWithType1847); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA129);

            	    ID130=(Token)match(input,ID,FOLLOW_ID_in_argdefWithType1849); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID130);


            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:33: ( attrs )?
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==ATTR) ) {
                alt39=1;
            }
            switch (alt39) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:33: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_argdefWithType1853);
                    attrs131=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs131.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:40: ( COLON type )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==COLON) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:41: COLON type
                    {
                    COLON132=(Token)match(input,COLON,FOLLOW_COLON_in_argdefWithType1857); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON132);

                    pushFollow(FOLLOW_type_in_argdefWithType1859);
                    type133=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type133.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:54: ( EQUALS init= rhsExpr )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==EQUALS) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:55: EQUALS init= rhsExpr
                    {
                    EQUALS134=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_argdefWithType1864); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS134);

                    pushFollow(FOLLOW_rhsExpr_in_argdefWithType1868);
                    init=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(init.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: attrs, init, ID, type
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
            // 245:78: -> ( ^( ARGDEF ID ( attrs )? ( type )* ( $init)? ) )+
            {
                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:81: ^( ARGDEF ID ( attrs )? ( type )* ( $init)? )
                    {
                    CommonTree root_1 = (CommonTree)adaptor.nil();
                    root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                    adaptor.addChild(root_1, stream_ID.nextNode());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:93: ( attrs )?
                    if ( stream_attrs.hasNext() ) {
                        adaptor.addChild(root_1, stream_attrs.nextTree());

                    }
                    stream_attrs.reset();
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:100: ( type )*
                    while ( stream_type.hasNext() ) {
                        adaptor.addChild(root_1, stream_type.nextTree());

                    }
                    stream_type.reset();
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:245:106: ( $init)?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:1: argdefsWithNames : ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* ;
    public final EulangParser.argdefsWithNames_return argdefsWithNames() throws RecognitionException {
        EulangParser.argdefsWithNames_return retval = new EulangParser.argdefsWithNames_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA136=null;
        Token COMMA138=null;
        EulangParser.argdefWithName_return argdefWithName135 = null;

        EulangParser.argdefWithName_return argdefWithName137 = null;


        CommonTree COMMA136_tree=null;
        CommonTree COMMA138_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_argdefWithName=new RewriteRuleSubtreeStream(adaptor,"rule argdefWithName");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:18: ( ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? ) -> ( argdefWithName )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:21: ( argdefWithName ( COMMA argdefWithName )+ ( COMMA )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:22: argdefWithName ( COMMA argdefWithName )+ ( COMMA )?
            {
            pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1903);
            argdefWithName135=argdefWithName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName135.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:37: ( COMMA argdefWithName )+
            int cnt42=0;
            loop42:
            do {
                int alt42=2;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==COMMA) ) {
                    int LA42_1 = input.LA(2);

                    if ( (LA42_1==ID) ) {
                        alt42=1;
                    }


                }


                switch (alt42) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:39: COMMA argdefWithName
            	    {
            	    COMMA136=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1907); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA136);

            	    pushFollow(FOLLOW_argdefWithName_in_argdefsWithNames1909);
            	    argdefWithName137=argdefWithName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_argdefWithName.add(argdefWithName137.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt42 >= 1 ) break loop42;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(42, input);
                        throw eee;
                }
                cnt42++;
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:62: ( COMMA )?
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==COMMA) ) {
                alt43=1;
            }
            switch (alt43) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:62: COMMA
                    {
                    COMMA138=(Token)match(input,COMMA,FOLLOW_COMMA_in_argdefsWithNames1913); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COMMA.add(COMMA138);


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
            // 248:73: -> ( argdefWithName )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:248:76: ( argdefWithName )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:1: argdefWithName : ID ( attrs )? -> ^( ARGDEF ID ( attrs )? ) ;
    public final EulangParser.argdefWithName_return argdefWithName() throws RecognitionException {
        EulangParser.argdefWithName_return retval = new EulangParser.argdefWithName_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID139=null;
        EulangParser.attrs_return attrs140 = null;


        CommonTree ID139_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:15: ( ID ( attrs )? -> ^( ARGDEF ID ( attrs )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:17: ID ( attrs )?
            {
            ID139=(Token)match(input,ID,FOLLOW_ID_in_argdefWithName1935); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID139);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:20: ( attrs )?
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==ATTR) ) {
                alt44=1;
            }
            switch (alt44) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:20: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_argdefWithName1937);
                    attrs140=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs140.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: attrs, ID
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 250:28: -> ^( ARGDEF ID ( attrs )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:31: ^( ARGDEF ID ( attrs )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGDEF, "ARGDEF"), root_1);

                adaptor.addChild(root_1, stream_ID.nextNode());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:250:43: ( attrs )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:1: proto : LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) ;
    public final EulangParser.proto_return proto() throws RecognitionException {
        EulangParser.proto_return retval = new EulangParser.proto_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN141=null;
        Token RPAREN144=null;
        EulangParser.argdefs_return argdefs142 = null;

        EulangParser.xreturns_return xreturns143 = null;


        CommonTree LPAREN141_tree=null;
        CommonTree RPAREN144_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_argdefs=new RewriteRuleSubtreeStream(adaptor,"rule argdefs");
        RewriteRuleSubtreeStream stream_xreturns=new RewriteRuleSubtreeStream(adaptor,"rule xreturns");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:7: ( LPAREN argdefs ( xreturns )? RPAREN -> ^( PROTO ( xreturns )? ( argdefs )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:9: LPAREN argdefs ( xreturns )? RPAREN
            {
            LPAREN141=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_proto1962); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN141);

            pushFollow(FOLLOW_argdefs_in_proto1964);
            argdefs142=argdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_argdefs.add(argdefs142.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:24: ( xreturns )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==ARROW) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:24: xreturns
                    {
                    pushFollow(FOLLOW_xreturns_in_proto1966);
                    xreturns143=xreturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_xreturns.add(xreturns143.getTree());

                    }
                    break;

            }

            RPAREN144=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_proto1969); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN144);



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
            // 254:59: -> ^( PROTO ( xreturns )? ( argdefs )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:62: ^( PROTO ( xreturns )? ( argdefs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(PROTO, "PROTO"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:70: ( xreturns )?
                if ( stream_xreturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_xreturns.nextTree());

                }
                stream_xreturns.reset();
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:254:80: ( argdefs )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:257:1: xreturns : ( ARROW type -> type | ARROW NIL -> ^( TYPE NIL ) );
    public final EulangParser.xreturns_return xreturns() throws RecognitionException {
        EulangParser.xreturns_return retval = new EulangParser.xreturns_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ARROW145=null;
        Token ARROW147=null;
        Token NIL148=null;
        EulangParser.type_return type146 = null;


        CommonTree ARROW145_tree=null;
        CommonTree ARROW147_tree=null;
        CommonTree NIL148_tree=null;
        RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
        RewriteRuleTokenStream stream_NIL=new RewriteRuleTokenStream(adaptor,"token NIL");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:257:10: ( ARROW type -> type | ARROW NIL -> ^( TYPE NIL ) )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==ARROW) ) {
                int LA46_1 = input.LA(2);

                if ( (LA46_1==NIL) ) {
                    alt46=2;
                }
                else if ( (LA46_1==CODE||LA46_1==ID||LA46_1==COLON||LA46_1==LPAREN||(LA46_1>=COLONS && LA46_1<=DATA)) ) {
                    alt46=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 46, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:257:12: ARROW type
                    {
                    ARROW145=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns2012); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW145);

                    pushFollow(FOLLOW_type_in_xreturns2014);
                    type146=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type146.getTree());


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
                    // 257:28: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:5: ARROW NIL
                    {
                    ARROW147=(Token)match(input,ARROW,FOLLOW_ARROW_in_xreturns2031); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ARROW.add(ARROW147);

                    NIL148=(Token)match(input,NIL,FOLLOW_NIL_in_xreturns2033); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL148);



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
                    // 259:26: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:259:29: ^( TYPE NIL )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:1: argtuple : LPAREN tupleargdefs RPAREN -> ^( TUPLETYPE tupleargdefs ) ;
    public final EulangParser.argtuple_return argtuple() throws RecognitionException {
        EulangParser.argtuple_return retval = new EulangParser.argtuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN149=null;
        Token RPAREN151=null;
        EulangParser.tupleargdefs_return tupleargdefs150 = null;


        CommonTree LPAREN149_tree=null;
        CommonTree RPAREN151_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleargdefs=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdefs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:10: ( LPAREN tupleargdefs RPAREN -> ^( TUPLETYPE tupleargdefs ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:12: LPAREN tupleargdefs RPAREN
            {
            LPAREN149=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_argtuple2063); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN149);

            pushFollow(FOLLOW_tupleargdefs_in_argtuple2065);
            tupleargdefs150=tupleargdefs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdefs.add(tupleargdefs150.getTree());
            RPAREN151=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_argtuple2067); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN151);



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
            // 262:42: -> ^( TUPLETYPE tupleargdefs )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:262:45: ^( TUPLETYPE tupleargdefs )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:1: tupleargdefs : ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* ;
    public final EulangParser.tupleargdefs_return tupleargdefs() throws RecognitionException {
        EulangParser.tupleargdefs_return retval = new EulangParser.tupleargdefs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA153=null;
        EulangParser.tupleargdef_return tupleargdef152 = null;

        EulangParser.tupleargdef_return tupleargdef154 = null;


        CommonTree COMMA153_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_tupleargdef=new RewriteRuleSubtreeStream(adaptor,"rule tupleargdef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:13: ( ( tupleargdef ( COMMA tupleargdef )+ ) -> ( tupleargdef )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:15: ( tupleargdef ( COMMA tupleargdef )+ )
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:15: ( tupleargdef ( COMMA tupleargdef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:16: tupleargdef ( COMMA tupleargdef )+
            {
            pushFollow(FOLLOW_tupleargdef_in_tupleargdefs2089);
            tupleargdef152=tupleargdef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef152.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:28: ( COMMA tupleargdef )+
            int cnt47=0;
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==COMMA) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:30: COMMA tupleargdef
            	    {
            	    COMMA153=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleargdefs2093); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA153);

            	    pushFollow(FOLLOW_tupleargdef_in_tupleargdefs2095);
            	    tupleargdef154=tupleargdef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_tupleargdef.add(tupleargdef154.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt47 >= 1 ) break loop47;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(47, input);
                        throw eee;
                }
                cnt47++;
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
            // 265:75: -> ( tupleargdef )*
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:265:78: ( tupleargdef )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:1: tupleargdef : ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) );
    public final EulangParser.tupleargdef_return tupleargdef() throws RecognitionException {
        EulangParser.tupleargdef_return retval = new EulangParser.tupleargdef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION156=null;
        EulangParser.type_return type155 = null;


        CommonTree QUESTION156_tree=null;
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:12: ( type -> type | QUESTION -> ^( TYPE NIL ) | -> ^( TYPE NIL ) )
            int alt48=3;
            switch ( input.LA(1) ) {
            case CODE:
            case ID:
            case COLON:
            case LPAREN:
            case COLONS:
            case DATA:
                {
                alt48=1;
                }
                break;
            case QUESTION:
                {
                alt48=2;
                }
                break;
            case COMMA:
            case RPAREN:
                {
                alt48=3;
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:268:14: type
                    {
                    pushFollow(FOLLOW_type_in_tupleargdef2140);
                    type155=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type155.getTree());


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
                    // 268:22: -> type
                    {
                        adaptor.addChild(root_0, stream_type.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:5: QUESTION
                    {
                    QUESTION156=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_tupleargdef2153); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION156);



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
                    // 269:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:269:24: ^( TYPE NIL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:270:21: 
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
                    // 270:21: -> ^( TYPE NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:270:24: ^( TYPE NIL )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:1: type : ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )* ;
    public final EulangParser.type_return type() throws RecognitionException {
        EulangParser.type_return retval = new EulangParser.type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET159=null;
        Token COMMA161=null;
        Token RBRACKET163=null;
        Token CARET164=null;
        EulangParser.nonArrayType_return nonArrayType157 = null;

        EulangParser.arraySuff_return arraySuff158 = null;

        EulangParser.rhsExpr_return rhsExpr160 = null;

        EulangParser.rhsExpr_return rhsExpr162 = null;


        CommonTree LBRACKET159_tree=null;
        CommonTree COMMA161_tree=null;
        CommonTree RBRACKET163_tree=null;
        CommonTree CARET164_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_CARET=new RewriteRuleTokenStream(adaptor,"token CARET");
        RewriteRuleSubtreeStream stream_arraySuff=new RewriteRuleSubtreeStream(adaptor,"rule arraySuff");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_nonArrayType=new RewriteRuleSubtreeStream(adaptor,"rule nonArrayType");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:276:6: ( ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:5: ( nonArrayType -> nonArrayType ) ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:5: ( nonArrayType -> nonArrayType )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:277:6: nonArrayType
            {
            pushFollow(FOLLOW_nonArrayType_in_type2218);
            nonArrayType157=nonArrayType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_nonArrayType.add(nonArrayType157.getTree());


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
            // 277:19: -> nonArrayType
            {
                adaptor.addChild(root_0, stream_nonArrayType.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:278:6: ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*
            loop51:
            do {
                int alt51=4;
                alt51 = dfa51.predict(input);
                switch (alt51) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:7: ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:279:7: ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:9: ( ( arraySuff )+ )=> ( arraySuff )+
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:25: ( arraySuff )+
            	    int cnt49=0;
            	    loop49:
            	    do {
            	        int alt49=2;
            	        int LA49_0 = input.LA(1);

            	        if ( (LA49_0==LBRACKET) ) {
            	            alt49=1;
            	        }


            	        switch (alt49) {
            	    	case 1 :
            	    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:25: arraySuff
            	    	    {
            	    	    pushFollow(FOLLOW_arraySuff_in_type2256);
            	    	    arraySuff158=arraySuff();

            	    	    state._fsp--;
            	    	    if (state.failed) return retval;
            	    	    if ( state.backtracking==0 ) stream_arraySuff.add(arraySuff158.getTree());

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
            	    // elements: type, arraySuff
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 280:36: -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:39: ^( TYPE ^( ARRAY $type ( arraySuff )+ ) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:46: ^( ARRAY $type ( arraySuff )+ )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:8: ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:283:8: ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:9: LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET
            	    {
            	    LBRACKET159=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_type2311); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET159);

            	    pushFollow(FOLLOW_rhsExpr_in_type2313);
            	    rhsExpr160=rhsExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr160.getTree());
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:26: ( COMMA rhsExpr )+
            	    int cnt50=0;
            	    loop50:
            	    do {
            	        int alt50=2;
            	        int LA50_0 = input.LA(1);

            	        if ( (LA50_0==COMMA) ) {
            	            alt50=1;
            	        }


            	        switch (alt50) {
            	    	case 1 :
            	    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:28: COMMA rhsExpr
            	    	    {
            	    	    COMMA161=(Token)match(input,COMMA,FOLLOW_COMMA_in_type2317); if (state.failed) return retval; 
            	    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA161);

            	    	    pushFollow(FOLLOW_rhsExpr_in_type2319);
            	    	    rhsExpr162=rhsExpr();

            	    	    state._fsp--;
            	    	    if (state.failed) return retval;
            	    	    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr162.getTree());

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

            	    RBRACKET163=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_type2324); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET163);



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
            	    // 284:54: -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:57: ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:284:64: ^( ARRAY $type ( rhsExpr )+ )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:287:8: ( CARET -> ^( TYPE ^( POINTER $type) ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:287:8: ( CARET -> ^( TYPE ^( POINTER $type) ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:10: CARET
            	    {
            	    CARET164=(Token)match(input,CARET,FOLLOW_CARET_in_type2383); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET164);



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
            	    // 288:16: -> ^( TYPE ^( POINTER $type) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:19: ^( TYPE ^( POINTER $type) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:288:26: ^( POINTER $type)
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
    // $ANTLR end "type"

    public static class nonArrayType_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nonArrayType"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:1: nonArrayType : ( ( idExpr -> ^( TYPE idExpr ) ) | ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) ) | data -> ^( TYPE data ) | argtuple );
    public final EulangParser.nonArrayType_return nonArrayType() throws RecognitionException {
        EulangParser.nonArrayType_return retval = new EulangParser.nonArrayType_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token CODE166=null;
        EulangParser.idExpr_return idExpr165 = null;

        EulangParser.proto_return proto167 = null;

        EulangParser.data_return data168 = null;

        EulangParser.argtuple_return argtuple169 = null;


        CommonTree CODE166_tree=null;
        RewriteRuleTokenStream stream_CODE=new RewriteRuleTokenStream(adaptor,"token CODE");
        RewriteRuleSubtreeStream stream_idExpr=new RewriteRuleSubtreeStream(adaptor,"rule idExpr");
        RewriteRuleSubtreeStream stream_data=new RewriteRuleSubtreeStream(adaptor,"rule data");
        RewriteRuleSubtreeStream stream_proto=new RewriteRuleSubtreeStream(adaptor,"rule proto");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:293:14: ( ( idExpr -> ^( TYPE idExpr ) ) | ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) ) | data -> ^( TYPE data ) | argtuple )
            int alt53=4;
            switch ( input.LA(1) ) {
            case ID:
            case COLON:
            case COLONS:
                {
                alt53=1;
                }
                break;
            case CODE:
                {
                alt53=2;
                }
                break;
            case DATA:
                {
                alt53=3;
                }
                break;
            case LPAREN:
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:4: ( idExpr -> ^( TYPE idExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:4: ( idExpr -> ^( TYPE idExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:6: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_nonArrayType2435);
                    idExpr165=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr165.getTree());


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
                    // 294:13: -> ^( TYPE idExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:294:16: ^( TYPE idExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:5: ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:5: ( CODE ( proto )? -> ^( TYPE ^( CODE ( proto )? ) ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:7: CODE ( proto )?
                    {
                    CODE166=(Token)match(input,CODE,FOLLOW_CODE_in_nonArrayType2453); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CODE.add(CODE166);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:12: ( proto )?
                    int alt52=2;
                    int LA52_0 = input.LA(1);

                    if ( (LA52_0==LPAREN) ) {
                        alt52=1;
                    }
                    switch (alt52) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:12: proto
                            {
                            pushFollow(FOLLOW_proto_in_nonArrayType2455);
                            proto167=proto();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_proto.add(proto167.getTree());

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
                    // 295:19: -> ^( TYPE ^( CODE ( proto )? ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:22: ^( TYPE ^( CODE ( proto )? ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(TYPE, "TYPE"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:29: ^( CODE ( proto )? )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot(stream_CODE.nextNode(), root_2);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:295:36: ( proto )?
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:5: data
                    {
                    pushFollow(FOLLOW_data_in_nonArrayType2478);
                    data168=data();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_data.add(data168.getTree());


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
                    // 296:10: -> ^( TYPE data )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:296:13: ^( TYPE data )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:297:5: argtuple
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_argtuple_in_nonArrayType2494);
                    argtuple169=argtuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, argtuple169.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:1: arraySuff : ( LBRACKET rhsExpr RBRACKET -> rhsExpr | LBRACKET RBRACKET -> FALSE );
    public final EulangParser.arraySuff_return arraySuff() throws RecognitionException {
        EulangParser.arraySuff_return retval = new EulangParser.arraySuff_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET170=null;
        Token RBRACKET172=null;
        Token LBRACKET173=null;
        Token RBRACKET174=null;
        EulangParser.rhsExpr_return rhsExpr171 = null;


        CommonTree LBRACKET170_tree=null;
        CommonTree RBRACKET172_tree=null;
        CommonTree LBRACKET173_tree=null;
        CommonTree RBRACKET174_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:11: ( LBRACKET rhsExpr RBRACKET -> rhsExpr | LBRACKET RBRACKET -> FALSE )
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0==LBRACKET) ) {
                int LA54_1 = input.LA(2);

                if ( (LA54_1==RBRACKET) ) {
                    alt54=2;
                }
                else if ( (LA54_1==CODE||(LA54_1>=SIZEOF && LA54_1<=TYPEOF)||LA54_1==ID||LA54_1==COLON||LA54_1==LPAREN||LA54_1==NIL||LA54_1==IF||LA54_1==NOT||(LA54_1>=TILDE && LA54_1<=AMP)||LA54_1==MINUS||(LA54_1>=PLUSPLUS && LA54_1<=STRING_LITERAL)||(LA54_1>=FALSE && LA54_1<=COLONS)) ) {
                    alt54=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 54, 1, input);

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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:299:13: LBRACKET rhsExpr RBRACKET
                    {
                    LBRACKET170=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arraySuff2510); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET170);

                    pushFollow(FOLLOW_rhsExpr_in_arraySuff2512);
                    rhsExpr171=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr171.getTree());
                    RBRACKET172=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arraySuff2514); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET172);



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
                    // 299:39: -> rhsExpr
                    {
                        adaptor.addChild(root_0, stream_rhsExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:300:7: LBRACKET RBRACKET
                    {
                    LBRACKET173=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arraySuff2526); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET173);

                    RBRACKET174=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arraySuff2528); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET174);



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
                    // 300:25: -> FALSE
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:1: codestmtlist : ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) );
    public final EulangParser.codestmtlist_return codestmtlist() throws RecognitionException {
        EulangParser.codestmtlist_return retval = new EulangParser.codestmtlist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI176=null;
        EulangParser.codeStmt_return codeStmt175 = null;

        EulangParser.codeStmt_return codeStmt177 = null;


        CommonTree SEMI176_tree=null;
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:13: ( codeStmt ( SEMI ( codeStmt )? )* -> ^( STMTLIST ( codeStmt )* ) | -> ^( STMTLIST ) )
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==CODE||LA57_0==GOTO||(LA57_0>=SIZEOF && LA57_0<=TYPEOF)||LA57_0==ID||LA57_0==COLON||LA57_0==LBRACE||LA57_0==FOR||LA57_0==LPAREN||LA57_0==NIL||(LA57_0>=DO && LA57_0<=REPEAT)||(LA57_0>=ATSIGN && LA57_0<=IF)||LA57_0==NOT||(LA57_0>=TILDE && LA57_0<=AMP)||LA57_0==MINUS||(LA57_0>=PLUSPLUS && LA57_0<=STRING_LITERAL)||(LA57_0>=FALSE && LA57_0<=COLONS)) ) {
                alt57=1;
            }
            else if ( (LA57_0==RBRACE) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:16: codeStmt ( SEMI ( codeStmt )? )*
                    {
                    pushFollow(FOLLOW_codeStmt_in_codestmtlist2544);
                    codeStmt175=codeStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt175.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:25: ( SEMI ( codeStmt )? )*
                    loop56:
                    do {
                        int alt56=2;
                        int LA56_0 = input.LA(1);

                        if ( (LA56_0==SEMI) ) {
                            alt56=1;
                        }


                        switch (alt56) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:26: SEMI ( codeStmt )?
                    	    {
                    	    SEMI176=(Token)match(input,SEMI,FOLLOW_SEMI_in_codestmtlist2547); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI176);

                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:31: ( codeStmt )?
                    	    int alt55=2;
                    	    int LA55_0 = input.LA(1);

                    	    if ( (LA55_0==CODE||LA55_0==GOTO||(LA55_0>=SIZEOF && LA55_0<=TYPEOF)||LA55_0==ID||LA55_0==COLON||LA55_0==LBRACE||LA55_0==FOR||LA55_0==LPAREN||LA55_0==NIL||(LA55_0>=DO && LA55_0<=REPEAT)||(LA55_0>=ATSIGN && LA55_0<=IF)||LA55_0==NOT||(LA55_0>=TILDE && LA55_0<=AMP)||LA55_0==MINUS||(LA55_0>=PLUSPLUS && LA55_0<=STRING_LITERAL)||(LA55_0>=FALSE && LA55_0<=COLONS)) ) {
                    	        alt55=1;
                    	    }
                    	    switch (alt55) {
                    	        case 1 :
                    	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:31: codeStmt
                    	            {
                    	            pushFollow(FOLLOW_codeStmt_in_codestmtlist2549);
                    	            codeStmt177=codeStmt();

                    	            state._fsp--;
                    	            if (state.failed) return retval;
                    	            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt177.getTree());

                    	            }
                    	            break;

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop56;
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
                    // 302:44: -> ^( STMTLIST ( codeStmt )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:48: ^( STMTLIST ( codeStmt )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(STMTLIST, "STMTLIST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:302:59: ( codeStmt )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:7: 
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
                    // 303:7: -> ^( STMTLIST )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:303:10: ^( STMTLIST )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:1: codeStmt : ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr );
    public final EulangParser.codeStmt_return codeStmt() throws RecognitionException {
        EulangParser.codeStmt_return retval = new EulangParser.codeStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.labelStmt_return labelStmt178 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr179 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr180 = null;


        RewriteRuleSubtreeStream stream_labelStmt=new RewriteRuleSubtreeStream(adaptor,"rule labelStmt");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:10: ( labelStmt codeStmtExpr -> ^( LABELSTMT labelStmt codeStmtExpr ) | codeStmtExpr -> codeStmtExpr )
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==ATSIGN) ) {
                alt58=1;
            }
            else if ( (LA58_0==CODE||LA58_0==GOTO||(LA58_0>=SIZEOF && LA58_0<=TYPEOF)||LA58_0==ID||LA58_0==COLON||LA58_0==LBRACE||LA58_0==FOR||LA58_0==LPAREN||LA58_0==NIL||(LA58_0>=DO && LA58_0<=REPEAT)||LA58_0==IF||LA58_0==NOT||(LA58_0>=TILDE && LA58_0<=AMP)||LA58_0==MINUS||(LA58_0>=PLUSPLUS && LA58_0<=STRING_LITERAL)||(LA58_0>=FALSE && LA58_0<=COLONS)) ) {
                alt58=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;
            }
            switch (alt58) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:12: labelStmt codeStmtExpr
                    {
                    pushFollow(FOLLOW_labelStmt_in_codeStmt2593);
                    labelStmt178=labelStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labelStmt.add(labelStmt178.getTree());
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt2595);
                    codeStmtExpr179=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr179.getTree());


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
                    // 306:36: -> ^( LABELSTMT labelStmt codeStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:306:39: ^( LABELSTMT labelStmt codeStmtExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:307:9: codeStmtExpr
                    {
                    pushFollow(FOLLOW_codeStmtExpr_in_codeStmt2616);
                    codeStmtExpr180=codeStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr180.getTree());


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
                    // 307:22: -> codeStmtExpr
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:310:1: codeStmtExpr : ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );
    public final EulangParser.codeStmtExpr_return codeStmtExpr() throws RecognitionException {
        EulangParser.codeStmtExpr_return retval = new EulangParser.codeStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.varDecl_return varDecl181 = null;

        EulangParser.assignStmt_return assignStmt182 = null;

        EulangParser.rhsExpr_return rhsExpr183 = null;

        EulangParser.blockStmt_return blockStmt184 = null;

        EulangParser.gotoStmt_return gotoStmt185 = null;

        EulangParser.controlStmt_return controlStmt186 = null;


        RewriteRuleSubtreeStream stream_assignStmt=new RewriteRuleSubtreeStream(adaptor,"rule assignStmt");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_blockStmt=new RewriteRuleSubtreeStream(adaptor,"rule blockStmt");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_controlStmt=new RewriteRuleSubtreeStream(adaptor,"rule controlStmt");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:310:14: ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt )
            int alt59=6;
            alt59 = dfa59.predict(input);
            switch (alt59) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:7: ( varDecl )=> varDecl
                    {
                    pushFollow(FOLLOW_varDecl_in_codeStmtExpr2648);
                    varDecl181=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl181.getTree());


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
                    // 311:32: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:9: ( assignStmt )=> assignStmt
                    {
                    pushFollow(FOLLOW_assignStmt_in_codeStmtExpr2671);
                    assignStmt182=assignStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignStmt.add(assignStmt182.getTree());


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
                    // 312:39: -> assignStmt
                    {
                        adaptor.addChild(root_0, stream_assignStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:9: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_codeStmtExpr2688);
                    rhsExpr183=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr183.getTree());


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
                    // 313:23: -> ^( STMTEXPR rhsExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:313:27: ^( STMTEXPR rhsExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:314:9: ( LBRACE )=> blockStmt
                    {
                    pushFollow(FOLLOW_blockStmt_in_codeStmtExpr2721);
                    blockStmt184=blockStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_blockStmt.add(blockStmt184.getTree());


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
                    // 314:41: -> blockStmt
                    {
                        adaptor.addChild(root_0, stream_blockStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:315:9: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_codeStmtExpr2743);
                    gotoStmt185=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt185.getTree());


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
                    // 315:23: -> gotoStmt
                    {
                        adaptor.addChild(root_0, stream_gotoStmt.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:317:9: controlStmt
                    {
                    pushFollow(FOLLOW_controlStmt_in_codeStmtExpr2769);
                    controlStmt186=controlStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_controlStmt.add(controlStmt186.getTree());


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
                    // 317:26: -> controlStmt
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:1: varDecl : ( singleVarDecl | tupleVarDecl );
    public final EulangParser.varDecl_return varDecl() throws RecognitionException {
        EulangParser.varDecl_return retval = new EulangParser.varDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.singleVarDecl_return singleVarDecl187 = null;

        EulangParser.tupleVarDecl_return tupleVarDecl188 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:8: ( singleVarDecl | tupleVarDecl )
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==ID) ) {
                alt60=1;
            }
            else if ( (LA60_0==LPAREN) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:10: singleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_singleVarDecl_in_varDecl2792);
                    singleVarDecl187=singleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, singleVarDecl187.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:320:26: tupleVarDecl
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_tupleVarDecl_in_varDecl2796);
                    tupleVarDecl188=tupleVarDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, tupleVarDecl188.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:1: singleVarDecl : ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) ) ;
    public final EulangParser.singleVarDecl_return singleVarDecl() throws RecognitionException {
        EulangParser.singleVarDecl_return retval = new EulangParser.singleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID189=null;
        Token COLON_EQUALS191=null;
        Token COLON194=null;
        Token EQUALS196=null;
        Token COMMA198=null;
        Token ID199=null;
        Token COLON_EQUALS201=null;
        Token PLUS202=null;
        Token COMMA204=null;
        Token COLON206=null;
        Token EQUALS208=null;
        Token PLUS209=null;
        Token COMMA211=null;
        EulangParser.attrs_return attrs190 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr192 = null;

        EulangParser.attrs_return attrs193 = null;

        EulangParser.type_return type195 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr197 = null;

        EulangParser.attrs_return attrs200 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr203 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr205 = null;

        EulangParser.type_return type207 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr210 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr212 = null;


        CommonTree ID189_tree=null;
        CommonTree COLON_EQUALS191_tree=null;
        CommonTree COLON194_tree=null;
        CommonTree EQUALS196_tree=null;
        CommonTree COMMA198_tree=null;
        CommonTree ID199_tree=null;
        CommonTree COLON_EQUALS201_tree=null;
        CommonTree PLUS202_tree=null;
        CommonTree COMMA204_tree=null;
        CommonTree COLON206_tree=null;
        CommonTree EQUALS208_tree=null;
        CommonTree PLUS209_tree=null;
        CommonTree COMMA211_tree=null;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:322:14: ( ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:5: ID ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )
            {
            ID189=(Token)match(input,ID,FOLLOW_ID_in_singleVarDecl2808); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID189);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:323:8: ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )
            int alt72=3;
            alt72 = dfa72.predict(input);
            switch (alt72) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:9: ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:9: ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:11: ( attrs )? COLON_EQUALS assignOrInitExpr
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:11: ( attrs )?
                    int alt61=2;
                    int LA61_0 = input.LA(1);

                    if ( (LA61_0==ATTR) ) {
                        alt61=1;
                    }
                    switch (alt61) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:11: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2822);
                            attrs190=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs190.getTree());

                            }
                            break;

                    }

                    COLON_EQUALS191=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_singleVarDecl2825); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS191);

                    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2827);
                    assignOrInitExpr192=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr192.getTree());


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
                    // 324:56: -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:59: ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:324:70: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:9: ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:9: ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:11: ( attrs )? COLON type ( EQUALS assignOrInitExpr )?
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:11: ( attrs )?
                    int alt62=2;
                    int LA62_0 = input.LA(1);

                    if ( (LA62_0==ATTR) ) {
                        alt62=1;
                    }
                    switch (alt62) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:11: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2864);
                            attrs193=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs193.getTree());

                            }
                            break;

                    }

                    COLON194=(Token)match(input,COLON,FOLLOW_COLON_in_singleVarDecl2867); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON194);

                    pushFollow(FOLLOW_type_in_singleVarDecl2869);
                    type195=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type195.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:29: ( EQUALS assignOrInitExpr )?
                    int alt63=2;
                    int LA63_0 = input.LA(1);

                    if ( (LA63_0==EQUALS) ) {
                        alt63=1;
                    }
                    switch (alt63) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:30: EQUALS assignOrInitExpr
                            {
                            EQUALS196=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_singleVarDecl2872); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS196);

                            pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2874);
                            assignOrInitExpr197=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr197.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: type, assignOrInitExpr, attrs, ID
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 325:57: -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:60: ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                        adaptor.addChild(root_1, stream_ID.nextNode());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:71: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:325:83: ( assignOrInitExpr )*
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:9: ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:9: ( COMMA ID )+
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
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:11: COMMA ID
                    	    {
                    	    COMMA198=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl2907); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA198);

                    	    ID199=(Token)match(input,ID,FOLLOW_ID_in_singleVarDecl2909); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID199);


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

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:23: ( attrs )?
                    int alt65=2;
                    int LA65_0 = input.LA(1);

                    if ( (LA65_0==ATTR) ) {
                        alt65=1;
                    }
                    switch (alt65) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:326:23: attrs
                            {
                            pushFollow(FOLLOW_attrs_in_singleVarDecl2914);
                            attrs200=attrs();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_attrs.add(attrs200.getTree());

                            }
                            break;

                    }

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:9: ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) )
                    int alt71=2;
                    int LA71_0 = input.LA(1);

                    if ( (LA71_0==COLON_EQUALS) ) {
                        alt71=1;
                    }
                    else if ( (LA71_0==COLON) ) {
                        alt71=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 71, 0, input);

                        throw nvae;
                    }
                    switch (alt71) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:12: ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:12: ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:14: COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                            {
                            COLON_EQUALS201=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_singleVarDecl2932); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS201);

                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:27: ( PLUS )?
                            int alt66=2;
                            int LA66_0 = input.LA(1);

                            if ( (LA66_0==PLUS) ) {
                                alt66=1;
                            }
                            switch (alt66) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:27: PLUS
                                    {
                                    PLUS202=(Token)match(input,PLUS,FOLLOW_PLUS_in_singleVarDecl2934); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS202);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2937);
                            assignOrInitExpr203=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr203.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:50: ( COMMA assignOrInitExpr )*
                            loop67:
                            do {
                                int alt67=2;
                                int LA67_0 = input.LA(1);

                                if ( (LA67_0==COMMA) ) {
                                    alt67=1;
                                }


                                switch (alt67) {
                            	case 1 :
                            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:327:51: COMMA assignOrInitExpr
                            	    {
                            	    COMMA204=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl2940); if (state.failed) return retval; 
                            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA204);

                            	    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl2942);
                            	    assignOrInitExpr205=assignOrInitExpr();

                            	    state._fsp--;
                            	    if (state.failed) return retval;
                            	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr205.getTree());

                            	    }
                            	    break;

                            	default :
                            	    break loop67;
                                }
                            } while (true);


                            }



                            // AST REWRITE
                            // elements: assignOrInitExpr, attrs, PLUS, ID
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 328:15: -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:18: ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:26: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:38: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:328:56: ^( LIST ( assignOrInitExpr )+ )
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
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:12: ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? )
                            {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:12: ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? )
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:14: COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )?
                            {
                            COLON206=(Token)match(input,COLON,FOLLOW_COLON_in_singleVarDecl3004); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COLON.add(COLON206);

                            pushFollow(FOLLOW_type_in_singleVarDecl3006);
                            type207=type();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_type.add(type207.getTree());
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:25: ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )?
                            int alt70=2;
                            int LA70_0 = input.LA(1);

                            if ( (LA70_0==EQUALS) ) {
                                alt70=1;
                            }
                            switch (alt70) {
                                case 1 :
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:26: EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                                    {
                                    EQUALS208=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_singleVarDecl3009); if (state.failed) return retval; 
                                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS208);

                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:33: ( PLUS )?
                                    int alt68=2;
                                    int LA68_0 = input.LA(1);

                                    if ( (LA68_0==PLUS) ) {
                                        alt68=1;
                                    }
                                    switch (alt68) {
                                        case 1 :
                                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:33: PLUS
                                            {
                                            PLUS209=(Token)match(input,PLUS,FOLLOW_PLUS_in_singleVarDecl3011); if (state.failed) return retval; 
                                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS209);


                                            }
                                            break;

                                    }

                                    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl3014);
                                    assignOrInitExpr210=assignOrInitExpr();

                                    state._fsp--;
                                    if (state.failed) return retval;
                                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr210.getTree());
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:56: ( COMMA assignOrInitExpr )*
                                    loop69:
                                    do {
                                        int alt69=2;
                                        int LA69_0 = input.LA(1);

                                        if ( (LA69_0==COMMA) ) {
                                            alt69=1;
                                        }


                                        switch (alt69) {
                                    	case 1 :
                                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:329:57: COMMA assignOrInitExpr
                                    	    {
                                    	    COMMA211=(Token)match(input,COMMA,FOLLOW_COMMA_in_singleVarDecl3017); if (state.failed) return retval; 
                                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA211);

                                    	    pushFollow(FOLLOW_assignOrInitExpr_in_singleVarDecl3019);
                                    	    assignOrInitExpr212=assignOrInitExpr();

                                    	    state._fsp--;
                                    	    if (state.failed) return retval;
                                    	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr212.getTree());

                                    	    }
                                    	    break;

                                    	default :
                                    	    break loop69;
                                        }
                                    } while (true);


                                    }
                                    break;

                            }


                            }



                            // AST REWRITE
                            // elements: PLUS, assignOrInitExpr, ID, attrs, type
                            // token labels: 
                            // rule labels: retval
                            // token list labels: 
                            // rule list labels: 
                            // wildcard labels: 
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (CommonTree)adaptor.nil();
                            // 330:15: -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? )
                            {
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:18: ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? )
                                {
                                CommonTree root_1 = (CommonTree)adaptor.nil();
                                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:26: ^( LIST ( ID )+ )
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
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:38: ( attrs )?
                                if ( stream_attrs.hasNext() ) {
                                    adaptor.addChild(root_1, stream_attrs.nextTree());

                                }
                                stream_attrs.reset();
                                adaptor.addChild(root_1, stream_type.nextTree());
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:50: ( PLUS )?
                                if ( stream_PLUS.hasNext() ) {
                                    adaptor.addChild(root_1, stream_PLUS.nextNode());

                                }
                                stream_PLUS.reset();
                                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:56: ( ^( LIST ( assignOrInitExpr )+ ) )?
                                if ( stream_assignOrInitExpr.hasNext() ) {
                                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:330:56: ^( LIST ( assignOrInitExpr )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:1: tupleVarDecl : idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* ) ) ) ;
    public final EulangParser.tupleVarDecl_return tupleVarDecl() throws RecognitionException {
        EulangParser.tupleVarDecl_return retval = new EulangParser.tupleVarDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COLON_EQUALS215=null;
        Token COLON217=null;
        Token EQUALS219=null;
        EulangParser.idTuple_return idTuple213 = null;

        EulangParser.attrs_return attrs214 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr216 = null;

        EulangParser.type_return type218 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr220 = null;


        CommonTree COLON_EQUALS215_tree=null;
        CommonTree COLON217_tree=null;
        CommonTree EQUALS219_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COLON_EQUALS=new RewriteRuleTokenStream(adaptor,"token COLON_EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_attrs=new RewriteRuleSubtreeStream(adaptor,"rule attrs");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_type=new RewriteRuleSubtreeStream(adaptor,"rule type");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:334:13: ( idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* ) ) ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:5: idTuple ( attrs )? ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* ) ) )
            {
            pushFollow(FOLLOW_idTuple_in_tupleVarDecl3108);
            idTuple213=idTuple();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTuple.add(idTuple213.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:13: ( attrs )?
            int alt73=2;
            int LA73_0 = input.LA(1);

            if ( (LA73_0==ATTR) ) {
                alt73=1;
            }
            switch (alt73) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:335:13: attrs
                    {
                    pushFollow(FOLLOW_attrs_in_tupleVarDecl3110);
                    attrs214=attrs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_attrs.add(attrs214.getTree());

                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:7: ( ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr ) ) | ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* ) ) )
            int alt75=2;
            int LA75_0 = input.LA(1);

            if ( (LA75_0==COLON_EQUALS) ) {
                alt75=1;
            }
            else if ( (LA75_0==COLON) ) {
                alt75=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 75, 0, input);

                throw nvae;
            }
            switch (alt75) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:10: ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:10: ( COLON_EQUALS assignOrInitExpr -> ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:12: COLON_EQUALS assignOrInitExpr
                    {
                    COLON_EQUALS215=(Token)match(input,COLON_EQUALS,FOLLOW_COLON_EQUALS_in_tupleVarDecl3125); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON_EQUALS.add(COLON_EQUALS215);

                    pushFollow(FOLLOW_assignOrInitExpr_in_tupleVarDecl3127);
                    assignOrInitExpr216=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr216.getTree());


                    // AST REWRITE
                    // elements: assignOrInitExpr, attrs, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 336:50: -> ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:53: ^( ALLOC_TUPLE idTuple ( attrs )? TYPE assignOrInitExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:336:75: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, (CommonTree)adaptor.create(TYPE, "TYPE"));
                        adaptor.addChild(root_1, stream_assignOrInitExpr.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:9: ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* ) )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:9: ( COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* ) )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:11: COLON type ( EQUALS assignOrInitExpr )?
                    {
                    COLON217=(Token)match(input,COLON,FOLLOW_COLON_in_tupleVarDecl3164); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON217);

                    pushFollow(FOLLOW_type_in_tupleVarDecl3166);
                    type218=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type218.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:22: ( EQUALS assignOrInitExpr )?
                    int alt74=2;
                    int LA74_0 = input.LA(1);

                    if ( (LA74_0==EQUALS) ) {
                        alt74=1;
                    }
                    switch (alt74) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:23: EQUALS assignOrInitExpr
                            {
                            EQUALS219=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_tupleVarDecl3169); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS219);

                            pushFollow(FOLLOW_assignOrInitExpr_in_tupleVarDecl3171);
                            assignOrInitExpr220=assignOrInitExpr();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr220.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: idTuple, type, assignOrInitExpr, attrs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 337:50: -> ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:53: ^( ALLOC_TUPLE idTuple ( attrs )? type ( assignOrInitExpr )* )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC_TUPLE, "ALLOC_TUPLE"), root_1);

                        adaptor.addChild(root_1, stream_idTuple.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:75: ( attrs )?
                        if ( stream_attrs.hasNext() ) {
                            adaptor.addChild(root_1, stream_attrs.nextTree());

                        }
                        stream_attrs.reset();
                        adaptor.addChild(root_1, stream_type.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:337:87: ( assignOrInitExpr )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:1: assignStmt : ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) );
    public final EulangParser.assignStmt_return assignStmt() throws RecognitionException {
        EulangParser.assignStmt_return retval = new EulangParser.assignStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS225=null;
        Token COMMA228=null;
        Token PLUS231=null;
        Token COMMA233=null;
        EulangParser.lhs_return lhs221 = null;

        EulangParser.assignEqOp_return assignEqOp222 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr223 = null;

        EulangParser.idTuple_return idTuple224 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr226 = null;

        EulangParser.lhs_return lhs227 = null;

        EulangParser.lhs_return lhs229 = null;

        EulangParser.assignEqOp_return assignEqOp230 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr232 = null;

        EulangParser.assignOrInitExpr_return assignOrInitExpr234 = null;


        CommonTree EQUALS225_tree=null;
        CommonTree COMMA228_tree=null;
        CommonTree PLUS231_tree=null;
        CommonTree COMMA233_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignEqOp=new RewriteRuleSubtreeStream(adaptor,"rule assignEqOp");
        RewriteRuleSubtreeStream stream_assignOrInitExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignOrInitExpr");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:12: ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) )
            int alt79=3;
            alt79 = dfa79.predict(input);
            switch (alt79) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:14: ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr
                    {
                    pushFollow(FOLLOW_lhs_in_assignStmt3222);
                    lhs221=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs221.getTree());
                    pushFollow(FOLLOW_assignEqOp_in_assignStmt3224);
                    assignEqOp222=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp222.getTree());
                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3226);
                    assignOrInitExpr223=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr223.getTree());


                    // AST REWRITE
                    // elements: assignOrInitExpr, assignEqOp, lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 342:73: -> ^( ASSIGN assignEqOp lhs assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:76: ^( ASSIGN assignEqOp lhs assignOrInitExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:7: idTuple EQUALS assignOrInitExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignStmt3253);
                    idTuple224=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple224.getTree());
                    EQUALS225=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignStmt3255); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS225);

                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3257);
                    assignOrInitExpr226=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr226.getTree());


                    // AST REWRITE
                    // elements: assignOrInitExpr, EQUALS, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 343:53: -> ^( ASSIGN EQUALS idTuple assignOrInitExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:343:56: ^( ASSIGN EQUALS idTuple assignOrInitExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:7: ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )*
                    {
                    pushFollow(FOLLOW_lhs_in_assignStmt3312);
                    lhs227=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs227.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:45: ( COMMA lhs )+
                    int cnt76=0;
                    loop76:
                    do {
                        int alt76=2;
                        int LA76_0 = input.LA(1);

                        if ( (LA76_0==COMMA) ) {
                            alt76=1;
                        }


                        switch (alt76) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:46: COMMA lhs
                    	    {
                    	    COMMA228=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt3315); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA228);

                    	    pushFollow(FOLLOW_lhs_in_assignStmt3317);
                    	    lhs229=lhs();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_lhs.add(lhs229.getTree());

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt76 >= 1 ) break loop76;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(76, input);
                                throw eee;
                        }
                        cnt76++;
                    } while (true);

                    pushFollow(FOLLOW_assignEqOp_in_assignStmt3321);
                    assignEqOp230=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp230.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:69: ( PLUS )?
                    int alt77=2;
                    int LA77_0 = input.LA(1);

                    if ( (LA77_0==PLUS) ) {
                        alt77=1;
                    }
                    switch (alt77) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:69: PLUS
                            {
                            PLUS231=(Token)match(input,PLUS,FOLLOW_PLUS_in_assignStmt3323); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS231);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3326);
                    assignOrInitExpr232=assignOrInitExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr232.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:92: ( COMMA assignOrInitExpr )*
                    loop78:
                    do {
                        int alt78=2;
                        int LA78_0 = input.LA(1);

                        if ( (LA78_0==COMMA) ) {
                            alt78=1;
                        }


                        switch (alt78) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:93: COMMA assignOrInitExpr
                    	    {
                    	    COMMA233=(Token)match(input,COMMA,FOLLOW_COMMA_in_assignStmt3329); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA233);

                    	    pushFollow(FOLLOW_assignOrInitExpr_in_assignStmt3331);
                    	    assignOrInitExpr234=assignOrInitExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_assignOrInitExpr.add(assignOrInitExpr234.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop78;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: assignOrInitExpr, PLUS, lhs, assignEqOp
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 346:9: -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:12: ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ASSIGN, "ASSIGN"), root_1);

                        adaptor.addChild(root_1, stream_assignEqOp.nextTree());
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:32: ^( LIST ( lhs )+ )
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
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:45: ( PLUS )?
                        if ( stream_PLUS.hasNext() ) {
                            adaptor.addChild(root_1, stream_PLUS.nextNode());

                        }
                        stream_PLUS.reset();
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:346:51: ^( LIST ( assignOrInitExpr )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:1: assignOrInitExpr : ( assignExpr | initList );
    public final EulangParser.assignOrInitExpr_return assignOrInitExpr() throws RecognitionException {
        EulangParser.assignOrInitExpr_return retval = new EulangParser.assignOrInitExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.assignExpr_return assignExpr235 = null;

        EulangParser.initList_return initList236 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:18: ( assignExpr | initList )
            int alt80=2;
            int LA80_0 = input.LA(1);

            if ( (LA80_0==CODE||(LA80_0>=SIZEOF && LA80_0<=TYPEOF)||LA80_0==ID||LA80_0==COLON||LA80_0==LPAREN||LA80_0==NIL||LA80_0==IF||LA80_0==NOT||(LA80_0>=TILDE && LA80_0<=AMP)||LA80_0==MINUS||(LA80_0>=PLUSPLUS && LA80_0<=STRING_LITERAL)||(LA80_0>=FALSE && LA80_0<=COLONS)) ) {
                alt80=1;
            }
            else if ( (LA80_0==LBRACKET) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:20: assignExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignExpr_in_assignOrInitExpr3392);
                    assignExpr235=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignExpr235.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:349:33: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_assignOrInitExpr3396);
                    initList236=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList236.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:1: assignExpr : ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr );
    public final EulangParser.assignExpr_return assignExpr() throws RecognitionException {
        EulangParser.assignExpr_return retval = new EulangParser.assignExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS241=null;
        EulangParser.lhs_return lhs237 = null;

        EulangParser.assignEqOp_return assignEqOp238 = null;

        EulangParser.assignExpr_return assignExpr239 = null;

        EulangParser.idTuple_return idTuple240 = null;

        EulangParser.assignExpr_return assignExpr242 = null;

        EulangParser.rhsExpr_return rhsExpr243 = null;


        CommonTree EQUALS241_tree=null;
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleSubtreeStream stream_idTuple=new RewriteRuleSubtreeStream(adaptor,"rule idTuple");
        RewriteRuleSubtreeStream stream_assignEqOp=new RewriteRuleSubtreeStream(adaptor,"rule assignEqOp");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        RewriteRuleSubtreeStream stream_lhs=new RewriteRuleSubtreeStream(adaptor,"rule lhs");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:12: ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr )
            int alt81=3;
            alt81 = dfa81.predict(input);
            switch (alt81) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:14: ( lhs assignEqOp )=> lhs assignEqOp assignExpr
                    {
                    pushFollow(FOLLOW_lhs_in_assignExpr3414);
                    lhs237=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs237.getTree());
                    pushFollow(FOLLOW_assignEqOp_in_assignExpr3416);
                    assignEqOp238=assignEqOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignEqOp.add(assignEqOp238.getTree());
                    pushFollow(FOLLOW_assignExpr_in_assignExpr3418);
                    assignExpr239=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr239.getTree());


                    // AST REWRITE
                    // elements: assignEqOp, assignExpr, lhs
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 352:67: -> ^( ASSIGN assignEqOp lhs assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:70: ^( ASSIGN assignEqOp lhs assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:7: ( idTuple EQUALS )=> idTuple EQUALS assignExpr
                    {
                    pushFollow(FOLLOW_idTuple_in_assignExpr3453);
                    idTuple240=idTuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idTuple.add(idTuple240.getTree());
                    EQUALS241=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignExpr3455); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS241);

                    pushFollow(FOLLOW_assignExpr_in_assignExpr3457);
                    assignExpr242=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr242.getTree());


                    // AST REWRITE
                    // elements: assignExpr, EQUALS, idTuple
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 353:67: -> ^( ASSIGN EQUALS idTuple assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:70: ^( ASSIGN EQUALS idTuple assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:354:7: rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_assignExpr3491);
                    rhsExpr243=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr243.getTree());


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
                    // 354:43: -> rhsExpr
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:1: assignOp : ( PLUS_EQ | MINUS_EQ | STAR_EQ | SLASH_EQ | REM_EQ | UDIV_EQ | UREM_EQ | MOD_EQ | AND_EQ | OR_EQ | XOR_EQ | LSHIFT_EQ | RSHIFT_EQ | URSHIFT_EQ | CLSHIFT_EQ | CRSHIFT_EQ );
    public final EulangParser.assignOp_return assignOp() throws RecognitionException {
        EulangParser.assignOp_return retval = new EulangParser.assignOp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set244=null;

        CommonTree set244_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:357:10: ( PLUS_EQ | MINUS_EQ | STAR_EQ | SLASH_EQ | REM_EQ | UDIV_EQ | UREM_EQ | MOD_EQ | AND_EQ | OR_EQ | XOR_EQ | LSHIFT_EQ | RSHIFT_EQ | URSHIFT_EQ | CLSHIFT_EQ | CRSHIFT_EQ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set244=(Token)input.LT(1);
            if ( input.LA(1)==PLUS_EQ||(input.LA(1)>=MINUS_EQ && input.LA(1)<=CRSHIFT_EQ) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set244));
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:1: assignEqOp : ( EQUALS | assignOp );
    public final EulangParser.assignEqOp_return assignEqOp() throws RecognitionException {
        EulangParser.assignEqOp_return retval = new EulangParser.assignEqOp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EQUALS245=null;
        EulangParser.assignOp_return assignOp246 = null;


        CommonTree EQUALS245_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:12: ( EQUALS | assignOp )
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( (LA82_0==EQUALS) ) {
                alt82=1;
            }
            else if ( (LA82_0==PLUS_EQ||(LA82_0>=MINUS_EQ && LA82_0<=CRSHIFT_EQ)) ) {
                alt82=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;
            }
            switch (alt82) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:14: EQUALS
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    EQUALS245=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_assignEqOp3606); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUALS245_tree = (CommonTree)adaptor.create(EQUALS245);
                    adaptor.addChild(root_0, EQUALS245_tree);
                    }

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:359:23: assignOp
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_assignOp_in_assignEqOp3610);
                    assignOp246=assignOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, assignOp246.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:1: initList : LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET -> ^( INITLIST ( initExpr )* ) ;
    public final EulangParser.initList_return initList() throws RecognitionException {
        EulangParser.initList_return retval = new EulangParser.initList_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET247=null;
        Token COMMA249=null;
        Token RBRACKET251=null;
        EulangParser.initExpr_return initExpr248 = null;

        EulangParser.initExpr_return initExpr250 = null;


        CommonTree LBRACKET247_tree=null;
        CommonTree COMMA249_tree=null;
        CommonTree RBRACKET251_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_initExpr=new RewriteRuleSubtreeStream(adaptor,"rule initExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:10: ( LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET -> ^( INITLIST ( initExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:12: LBRACKET ( initExpr ( COMMA initExpr )* )? RBRACKET
            {
            LBRACKET247=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_initList3619); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET247);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:21: ( initExpr ( COMMA initExpr )* )?
            int alt84=2;
            int LA84_0 = input.LA(1);

            if ( (LA84_0==CODE||(LA84_0>=SIZEOF && LA84_0<=TYPEOF)||LA84_0==ID||LA84_0==COLON||LA84_0==LBRACKET||LA84_0==LPAREN||LA84_0==NIL||LA84_0==PERIOD||LA84_0==IF||LA84_0==NOT||(LA84_0>=TILDE && LA84_0<=AMP)||LA84_0==MINUS||(LA84_0>=PLUSPLUS && LA84_0<=STRING_LITERAL)||(LA84_0>=FALSE && LA84_0<=COLONS)) ) {
                alt84=1;
            }
            switch (alt84) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:22: initExpr ( COMMA initExpr )*
                    {
                    pushFollow(FOLLOW_initExpr_in_initList3622);
                    initExpr248=initExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initExpr.add(initExpr248.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:31: ( COMMA initExpr )*
                    loop83:
                    do {
                        int alt83=2;
                        int LA83_0 = input.LA(1);

                        if ( (LA83_0==COMMA) ) {
                            alt83=1;
                        }


                        switch (alt83) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:32: COMMA initExpr
                    	    {
                    	    COMMA249=(Token)match(input,COMMA,FOLLOW_COMMA_in_initList3625); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA249);

                    	    pushFollow(FOLLOW_initExpr_in_initList3627);
                    	    initExpr250=initExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_initExpr.add(initExpr250.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop83;
                        }
                    } while (true);


                    }
                    break;

            }

            RBRACKET251=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_initList3633); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET251);



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
            // 361:64: -> ^( INITLIST ( initExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:67: ^( INITLIST ( initExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(INITLIST, "INITLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:361:78: ( initExpr )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:362:1: initExpr : ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList );
    public final EulangParser.initExpr_return initExpr() throws RecognitionException {
        EulangParser.initExpr_return retval = new EulangParser.initExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PERIOD252=null;
        Token ID253=null;
        Token EQUALS254=null;
        Token LBRACKET255=null;
        Token RBRACKET256=null;
        Token EQUALS257=null;
        EulangParser.rhsExpr_return e = null;

        EulangParser.initElement_return ei = null;

        EulangParser.rhsExpr_return i = null;

        EulangParser.initList_return initList258 = null;


        CommonTree PERIOD252_tree=null;
        CommonTree ID253_tree=null;
        CommonTree EQUALS254_tree=null;
        CommonTree LBRACKET255_tree=null;
        CommonTree RBRACKET256_tree=null;
        CommonTree EQUALS257_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_EQUALS=new RewriteRuleTokenStream(adaptor,"token EQUALS");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_initElement=new RewriteRuleSubtreeStream(adaptor,"rule initElement");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:5: ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList )
            int alt85=4;
            alt85 = dfa85.predict(input);
            switch (alt85) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:7: ( rhsExpr )=>e= rhsExpr
                    {
                    pushFollow(FOLLOW_rhsExpr_in_initExpr3668);
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
                    // 363:75: -> ^( INITEXPR $e)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:78: ^( INITEXPR $e)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:7: PERIOD ID EQUALS ei= initElement
                    {
                    PERIOD252=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_initExpr3731); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD252);

                    ID253=(Token)match(input,ID,FOLLOW_ID_in_initExpr3733); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID253);

                    EQUALS254=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_initExpr3735); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS254);

                    pushFollow(FOLLOW_initElement_in_initExpr3739);
                    ei=initElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initElement.add(ei.getTree());


                    // AST REWRITE
                    // elements: ID, ei
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
                    // 364:72: -> ^( INITEXPR $ei ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:364:75: ^( INITEXPR $ei ID )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:7: ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement
                    {
                    LBRACKET255=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_initExpr3804); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET255);

                    pushFollow(FOLLOW_rhsExpr_in_initExpr3808);
                    i=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(i.getTree());
                    RBRACKET256=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_initExpr3810); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET256);

                    EQUALS257=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_initExpr3812); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_EQUALS.add(EQUALS257);

                    pushFollow(FOLLOW_initElement_in_initExpr3816);
                    ei=initElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_initElement.add(ei.getTree());


                    // AST REWRITE
                    // elements: ei, i
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
                    // 365:107: -> ^( INITEXPR $ei $i)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:110: ^( INITEXPR $ei $i)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:366:7: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_initExpr3853);
                    initList258=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList258.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:1: initElement : ( rhsExpr | initList );
    public final EulangParser.initElement_return initElement() throws RecognitionException {
        EulangParser.initElement_return retval = new EulangParser.initElement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.rhsExpr_return rhsExpr259 = null;

        EulangParser.initList_return initList260 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:13: ( rhsExpr | initList )
            int alt86=2;
            int LA86_0 = input.LA(1);

            if ( (LA86_0==CODE||(LA86_0>=SIZEOF && LA86_0<=TYPEOF)||LA86_0==ID||LA86_0==COLON||LA86_0==LPAREN||LA86_0==NIL||LA86_0==IF||LA86_0==NOT||(LA86_0>=TILDE && LA86_0<=AMP)||LA86_0==MINUS||(LA86_0>=PLUSPLUS && LA86_0<=STRING_LITERAL)||(LA86_0>=FALSE && LA86_0<=COLONS)) ) {
                alt86=1;
            }
            else if ( (LA86_0==LBRACKET) ) {
                alt86=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 86, 0, input);

                throw nvae;
            }
            switch (alt86) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:15: rhsExpr
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rhsExpr_in_initElement3867);
                    rhsExpr259=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rhsExpr259.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:369:25: initList
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_initList_in_initElement3871);
                    initList260=initList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, initList260.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:1: controlStmt : ( doWhile | whileDo | repeat | forIter );
    public final EulangParser.controlStmt_return controlStmt() throws RecognitionException {
        EulangParser.controlStmt_return retval = new EulangParser.controlStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.doWhile_return doWhile261 = null;

        EulangParser.whileDo_return whileDo262 = null;

        EulangParser.repeat_return repeat263 = null;

        EulangParser.forIter_return forIter264 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:13: ( doWhile | whileDo | repeat | forIter )
            int alt87=4;
            switch ( input.LA(1) ) {
            case DO:
                {
                alt87=1;
                }
                break;
            case WHILE:
                {
                alt87=2;
                }
                break;
            case REPEAT:
                {
                alt87=3;
                }
                break;
            case FOR:
                {
                alt87=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 87, 0, input);

                throw nvae;
            }

            switch (alt87) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:15: doWhile
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_doWhile_in_controlStmt3883);
                    doWhile261=doWhile();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, doWhile261.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:25: whileDo
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_whileDo_in_controlStmt3887);
                    whileDo262=whileDo();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, whileDo262.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:35: repeat
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_repeat_in_controlStmt3891);
                    repeat263=repeat();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, repeat263.getTree());

                    }
                    break;
                case 4 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:371:44: forIter
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_forIter_in_controlStmt3895);
                    forIter264=forIter();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, forIter264.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:1: doWhile : DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) ;
    public final EulangParser.doWhile_return doWhile() throws RecognitionException {
        EulangParser.doWhile_return retval = new EulangParser.doWhile_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DO265=null;
        Token WHILE267=null;
        EulangParser.codeStmtExpr_return codeStmtExpr266 = null;

        EulangParser.rhsExpr_return rhsExpr268 = null;


        CommonTree DO265_tree=null;
        CommonTree WHILE267_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:9: ( DO codeStmtExpr WHILE rhsExpr -> ^( DO codeStmtExpr rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:11: DO codeStmtExpr WHILE rhsExpr
            {
            DO265=(Token)match(input,DO,FOLLOW_DO_in_doWhile3904); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DO.add(DO265);

            pushFollow(FOLLOW_codeStmtExpr_in_doWhile3906);
            codeStmtExpr266=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr266.getTree());
            WHILE267=(Token)match(input,WHILE,FOLLOW_WHILE_in_doWhile3908); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE267);

            pushFollow(FOLLOW_rhsExpr_in_doWhile3910);
            rhsExpr268=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr268.getTree());


            // AST REWRITE
            // elements: codeStmtExpr, DO, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 373:43: -> ^( DO codeStmtExpr rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:373:46: ^( DO codeStmtExpr rhsExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:1: whileDo : WHILE rhsExpr ( COLON | DO ) codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) ;
    public final EulangParser.whileDo_return whileDo() throws RecognitionException {
        EulangParser.whileDo_return retval = new EulangParser.whileDo_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token WHILE269=null;
        Token COLON271=null;
        Token DO272=null;
        EulangParser.rhsExpr_return rhsExpr270 = null;

        EulangParser.codeStmtExpr_return codeStmtExpr273 = null;


        CommonTree WHILE269_tree=null;
        CommonTree COLON271_tree=null;
        CommonTree DO272_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleSubtreeStream stream_codeStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule codeStmtExpr");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:9: ( WHILE rhsExpr ( COLON | DO ) codeStmtExpr -> ^( WHILE rhsExpr codeStmtExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:11: WHILE rhsExpr ( COLON | DO ) codeStmtExpr
            {
            WHILE269=(Token)match(input,WHILE,FOLLOW_WHILE_in_whileDo3933); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_WHILE.add(WHILE269);

            pushFollow(FOLLOW_rhsExpr_in_whileDo3935);
            rhsExpr270=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr270.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:25: ( COLON | DO )
            int alt88=2;
            int LA88_0 = input.LA(1);

            if ( (LA88_0==COLON) ) {
                alt88=1;
            }
            else if ( (LA88_0==DO) ) {
                alt88=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 88, 0, input);

                throw nvae;
            }
            switch (alt88) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:26: COLON
                    {
                    COLON271=(Token)match(input,COLON,FOLLOW_COLON_in_whileDo3938); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON271);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:32: DO
                    {
                    DO272=(Token)match(input,DO,FOLLOW_DO_in_whileDo3940); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO272);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmtExpr_in_whileDo3943);
            codeStmtExpr273=codeStmtExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmtExpr.add(codeStmtExpr273.getTree());


            // AST REWRITE
            // elements: codeStmtExpr, rhsExpr, WHILE
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 376:51: -> ^( WHILE rhsExpr codeStmtExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:376:54: ^( WHILE rhsExpr codeStmtExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:1: repeat : REPEAT rhsExpr ( COLON | DO ) codeStmt -> ^( REPEAT rhsExpr codeStmt ) ;
    public final EulangParser.repeat_return repeat() throws RecognitionException {
        EulangParser.repeat_return retval = new EulangParser.repeat_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token REPEAT274=null;
        Token COLON276=null;
        Token DO277=null;
        EulangParser.rhsExpr_return rhsExpr275 = null;

        EulangParser.codeStmt_return codeStmt278 = null;


        CommonTree REPEAT274_tree=null;
        CommonTree COLON276_tree=null;
        CommonTree DO277_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_REPEAT=new RewriteRuleTokenStream(adaptor,"token REPEAT");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:8: ( REPEAT rhsExpr ( COLON | DO ) codeStmt -> ^( REPEAT rhsExpr codeStmt ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:10: REPEAT rhsExpr ( COLON | DO ) codeStmt
            {
            REPEAT274=(Token)match(input,REPEAT,FOLLOW_REPEAT_in_repeat3968); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_REPEAT.add(REPEAT274);

            pushFollow(FOLLOW_rhsExpr_in_repeat3970);
            rhsExpr275=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr275.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:25: ( COLON | DO )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0==COLON) ) {
                alt89=1;
            }
            else if ( (LA89_0==DO) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:26: COLON
                    {
                    COLON276=(Token)match(input,COLON,FOLLOW_COLON_in_repeat3973); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON276);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:32: DO
                    {
                    DO277=(Token)match(input,DO,FOLLOW_DO_in_repeat3975); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO277);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmt_in_repeat3978);
            codeStmt278=codeStmt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt278.getTree());


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
            // 379:53: -> ^( REPEAT rhsExpr codeStmt )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:379:56: ^( REPEAT rhsExpr codeStmt )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:1: forIter : FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt ) ;
    public final EulangParser.forIter_return forIter() throws RecognitionException {
        EulangParser.forIter_return retval = new EulangParser.forIter_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token FOR279=null;
        Token IN282=null;
        Token COLON284=null;
        Token DO285=null;
        EulangParser.forIds_return forIds280 = null;

        EulangParser.forMovement_return forMovement281 = null;

        EulangParser.rhsExpr_return rhsExpr283 = null;

        EulangParser.codeStmt_return codeStmt286 = null;


        CommonTree FOR279_tree=null;
        CommonTree IN282_tree=null;
        CommonTree COLON284_tree=null;
        CommonTree DO285_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_FOR=new RewriteRuleTokenStream(adaptor,"token FOR");
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleSubtreeStream stream_forMovement=new RewriteRuleSubtreeStream(adaptor,"rule forMovement");
        RewriteRuleSubtreeStream stream_codeStmt=new RewriteRuleSubtreeStream(adaptor,"rule codeStmt");
        RewriteRuleSubtreeStream stream_forIds=new RewriteRuleSubtreeStream(adaptor,"rule forIds");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:9: ( FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:11: FOR forIds ( forMovement )? IN rhsExpr ( COLON | DO ) codeStmt
            {
            FOR279=(Token)match(input,FOR,FOLLOW_FOR_in_forIter4008); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_FOR.add(FOR279);

            pushFollow(FOLLOW_forIds_in_forIter4010);
            forIds280=forIds();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_forIds.add(forIds280.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:22: ( forMovement )?
            int alt90=2;
            int LA90_0 = input.LA(1);

            if ( ((LA90_0>=BY && LA90_0<=AT)) ) {
                alt90=1;
            }
            switch (alt90) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:22: forMovement
                    {
                    pushFollow(FOLLOW_forMovement_in_forIter4012);
                    forMovement281=forMovement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_forMovement.add(forMovement281.getTree());

                    }
                    break;

            }

            IN282=(Token)match(input,IN,FOLLOW_IN_in_forIter4015); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_IN.add(IN282);

            pushFollow(FOLLOW_rhsExpr_in_forIter4017);
            rhsExpr283=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr283.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:46: ( COLON | DO )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:47: COLON
                    {
                    COLON284=(Token)match(input,COLON,FOLLOW_COLON_in_forIter4020); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON284);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:53: DO
                    {
                    DO285=(Token)match(input,DO,FOLLOW_DO_in_forIter4022); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_DO.add(DO285);


                    }
                    break;

            }

            pushFollow(FOLLOW_codeStmt_in_forIter4025);
            codeStmt286=codeStmt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codeStmt.add(codeStmt286.getTree());


            // AST REWRITE
            // elements: forIds, rhsExpr, codeStmt, forMovement, FOR
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 382:72: -> ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:75: ^( FOR ^( LIST forIds ) ( forMovement )? rhsExpr codeStmt )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_FOR.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:81: ^( LIST forIds )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_2);

                adaptor.addChild(root_2, stream_forIds.nextTree());

                adaptor.addChild(root_1, root_2);
                }
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:382:96: ( forMovement )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:1: forIds : ID ( AND ID )* -> ( ID )+ ;
    public final EulangParser.forIds_return forIds() throws RecognitionException {
        EulangParser.forIds_return retval = new EulangParser.forIds_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID287=null;
        Token AND288=null;
        Token ID289=null;

        CommonTree ID287_tree=null;
        CommonTree AND288_tree=null;
        CommonTree ID289_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:8: ( ID ( AND ID )* -> ( ID )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:10: ID ( AND ID )*
            {
            ID287=(Token)match(input,ID,FOLLOW_ID_in_forIds4062); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID287);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:13: ( AND ID )*
            loop92:
            do {
                int alt92=2;
                int LA92_0 = input.LA(1);

                if ( (LA92_0==AND) ) {
                    alt92=1;
                }


                switch (alt92) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:385:14: AND ID
            	    {
            	    AND288=(Token)match(input,AND,FOLLOW_AND_in_forIds4065); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND288);

            	    ID289=(Token)match(input,ID,FOLLOW_ID_in_forIds4067); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID289);


            	    }
            	    break;

            	default :
            	    break loop92;
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
            // 385:23: -> ( ID )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:1: forMovement : ( atId | stepping );
    public final EulangParser.forMovement_return forMovement() throws RecognitionException {
        EulangParser.forMovement_return retval = new EulangParser.forMovement_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.atId_return atId290 = null;

        EulangParser.stepping_return stepping291 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:13: ( atId | stepping )
            int alt93=2;
            int LA93_0 = input.LA(1);

            if ( (LA93_0==AT) ) {
                alt93=1;
            }
            else if ( (LA93_0==BY) ) {
                alt93=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 93, 0, input);

                throw nvae;
            }
            switch (alt93) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:15: atId
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_atId_in_forMovement4083);
                    atId290=atId();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, atId290.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:387:22: stepping
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_stepping_in_forMovement4087);
                    stepping291=stepping();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, stepping291.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:1: stepping : BY rhsExpr -> ^( BY rhsExpr ) ;
    public final EulangParser.stepping_return stepping() throws RecognitionException {
        EulangParser.stepping_return retval = new EulangParser.stepping_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BY292=null;
        EulangParser.rhsExpr_return rhsExpr293 = null;


        CommonTree BY292_tree=null;
        RewriteRuleTokenStream stream_BY=new RewriteRuleTokenStream(adaptor,"token BY");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:10: ( BY rhsExpr -> ^( BY rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:12: BY rhsExpr
            {
            BY292=(Token)match(input,BY,FOLLOW_BY_in_stepping4096); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BY.add(BY292);

            pushFollow(FOLLOW_rhsExpr_in_stepping4098);
            rhsExpr293=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr293.getTree());


            // AST REWRITE
            // elements: rhsExpr, BY
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 389:23: -> ^( BY rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:389:26: ^( BY rhsExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:1: atId : AT ID -> ^( AT ID ) ;
    public final EulangParser.atId_return atId() throws RecognitionException {
        EulangParser.atId_return retval = new EulangParser.atId_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AT294=null;
        Token ID295=null;

        CommonTree AT294_tree=null;
        CommonTree ID295_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:6: ( AT ID -> ^( AT ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:8: AT ID
            {
            AT294=(Token)match(input,AT,FOLLOW_AT_in_atId4115); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_AT.add(AT294);

            ID295=(Token)match(input,ID,FOLLOW_ID_in_atId4117); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID295);



            // AST REWRITE
            // elements: ID, AT
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 391:17: -> ^( AT ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:391:20: ^( AT ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:1: breakStmt : BREAK rhsExpr -> ^( BREAK rhsExpr ) ;
    public final EulangParser.breakStmt_return breakStmt() throws RecognitionException {
        EulangParser.breakStmt_return retval = new EulangParser.breakStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BREAK296=null;
        EulangParser.rhsExpr_return rhsExpr297 = null;


        CommonTree BREAK296_tree=null;
        RewriteRuleTokenStream stream_BREAK=new RewriteRuleTokenStream(adaptor,"token BREAK");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:11: ( BREAK rhsExpr -> ^( BREAK rhsExpr ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:13: BREAK rhsExpr
            {
            BREAK296=(Token)match(input,BREAK,FOLLOW_BREAK_in_breakStmt4145); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_BREAK.add(BREAK296);

            pushFollow(FOLLOW_rhsExpr_in_breakStmt4147);
            rhsExpr297=rhsExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr297.getTree());


            // AST REWRITE
            // elements: BREAK, rhsExpr
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 395:27: -> ^( BREAK rhsExpr )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:395:31: ^( BREAK rhsExpr )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:1: labelStmt : ATSIGN ID COLON -> ^( LABEL ID ) ;
    public final EulangParser.labelStmt_return labelStmt() throws RecognitionException {
        EulangParser.labelStmt_return retval = new EulangParser.labelStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ATSIGN298=null;
        Token ID299=null;
        Token COLON300=null;

        CommonTree ATSIGN298_tree=null;
        CommonTree ID299_tree=null;
        CommonTree COLON300_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ATSIGN=new RewriteRuleTokenStream(adaptor,"token ATSIGN");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:10: ( ATSIGN ID COLON -> ^( LABEL ID ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:12: ATSIGN ID COLON
            {
            ATSIGN298=(Token)match(input,ATSIGN,FOLLOW_ATSIGN_in_labelStmt4175); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ATSIGN.add(ATSIGN298);

            ID299=(Token)match(input,ID,FOLLOW_ID_in_labelStmt4177); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID299);

            COLON300=(Token)match(input,COLON,FOLLOW_COLON_in_labelStmt4179); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_COLON.add(COLON300);



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
            // 403:47: -> ^( LABEL ID )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:403:50: ^( LABEL ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:1: gotoStmt : GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) ;
    public final EulangParser.gotoStmt_return gotoStmt() throws RecognitionException {
        EulangParser.gotoStmt_return retval = new EulangParser.gotoStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token GOTO301=null;
        Token IF303=null;
        EulangParser.idOrScopeRef_return idOrScopeRef302 = null;

        EulangParser.rhsExpr_return rhsExpr304 = null;


        CommonTree GOTO301_tree=null;
        CommonTree IF303_tree=null;
        RewriteRuleTokenStream stream_GOTO=new RewriteRuleTokenStream(adaptor,"token GOTO");
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_rhsExpr=new RewriteRuleSubtreeStream(adaptor,"rule rhsExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:9: ( GOTO idOrScopeRef ( IF rhsExpr )? -> ^( GOTO idOrScopeRef ( rhsExpr )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:11: GOTO idOrScopeRef ( IF rhsExpr )?
            {
            GOTO301=(Token)match(input,GOTO,FOLLOW_GOTO_in_gotoStmt4215); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GOTO.add(GOTO301);

            pushFollow(FOLLOW_idOrScopeRef_in_gotoStmt4217);
            idOrScopeRef302=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef302.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:29: ( IF rhsExpr )?
            int alt94=2;
            int LA94_0 = input.LA(1);

            if ( (LA94_0==IF) ) {
                alt94=1;
            }
            switch (alt94) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:30: IF rhsExpr
                    {
                    IF303=(Token)match(input,IF,FOLLOW_IF_in_gotoStmt4220); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF303);

                    pushFollow(FOLLOW_rhsExpr_in_gotoStmt4222);
                    rhsExpr304=rhsExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rhsExpr.add(rhsExpr304.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: idOrScopeRef, rhsExpr, GOTO
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 405:53: -> ^( GOTO idOrScopeRef ( rhsExpr )? )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:56: ^( GOTO idOrScopeRef ( rhsExpr )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_GOTO.nextNode(), root_1);

                adaptor.addChild(root_1, stream_idOrScopeRef.nextTree());
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:405:76: ( rhsExpr )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:1: blockStmt : LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) ;
    public final EulangParser.blockStmt_return blockStmt() throws RecognitionException {
        EulangParser.blockStmt_return retval = new EulangParser.blockStmt_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE305=null;
        Token RBRACE307=null;
        EulangParser.codestmtlist_return codestmtlist306 = null;


        CommonTree LBRACE305_tree=null;
        CommonTree RBRACE307_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:10: ( LBRACE codestmtlist RBRACE -> ^( BLOCK codestmtlist ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:12: LBRACE codestmtlist RBRACE
            {
            LBRACE305=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_blockStmt4257); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE305);

            pushFollow(FOLLOW_codestmtlist_in_blockStmt4259);
            codestmtlist306=codestmtlist();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist306.getTree());
            RBRACE307=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_blockStmt4261); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE307);



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
            // 408:43: -> ^( BLOCK codestmtlist )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:408:46: ^( BLOCK codestmtlist )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:1: tuple : LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) ;
    public final EulangParser.tuple_return tuple() throws RecognitionException {
        EulangParser.tuple_return retval = new EulangParser.tuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN308=null;
        Token RPAREN310=null;
        EulangParser.tupleEntries_return tupleEntries309 = null;


        CommonTree LPAREN308_tree=null;
        CommonTree RPAREN310_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_tupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule tupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:7: ( LPAREN tupleEntries RPAREN -> ^( TUPLE ( tupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:9: LPAREN tupleEntries RPAREN
            {
            LPAREN308=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_tuple4284); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN308);

            pushFollow(FOLLOW_tupleEntries_in_tuple4286);
            tupleEntries309=tupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_tupleEntries.add(tupleEntries309.getTree());
            RPAREN310=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_tuple4288); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN310);



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
            // 411:41: -> ^( TUPLE ( tupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:411:44: ^( TUPLE ( tupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:1: tupleEntries : assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ ;
    public final EulangParser.tupleEntries_return tupleEntries() throws RecognitionException {
        EulangParser.tupleEntries_return retval = new EulangParser.tupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA312=null;
        EulangParser.assignExpr_return assignExpr311 = null;

        EulangParser.assignExpr_return assignExpr313 = null;


        CommonTree COMMA312_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:14: ( assignExpr ( COMMA assignExpr )+ -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:16: assignExpr ( COMMA assignExpr )+
            {
            pushFollow(FOLLOW_assignExpr_in_tupleEntries4316);
            assignExpr311=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr311.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:27: ( COMMA assignExpr )+
            int cnt95=0;
            loop95:
            do {
                int alt95=2;
                int LA95_0 = input.LA(1);

                if ( (LA95_0==COMMA) ) {
                    alt95=1;
                }


                switch (alt95) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:414:28: COMMA assignExpr
            	    {
            	    COMMA312=(Token)match(input,COMMA,FOLLOW_COMMA_in_tupleEntries4319); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA312);

            	    pushFollow(FOLLOW_assignExpr_in_tupleEntries4321);
            	    assignExpr313=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr313.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt95 >= 1 ) break loop95;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(95, input);
                        throw eee;
                }
                cnt95++;
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
            // 414:48: -> ( assignExpr )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:1: idTuple : LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) ;
    public final EulangParser.idTuple_return idTuple() throws RecognitionException {
        EulangParser.idTuple_return retval = new EulangParser.idTuple_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LPAREN314=null;
        Token RPAREN316=null;
        EulangParser.idTupleEntries_return idTupleEntries315 = null;


        CommonTree LPAREN314_tree=null;
        CommonTree RPAREN316_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_idTupleEntries=new RewriteRuleSubtreeStream(adaptor,"rule idTupleEntries");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:9: ( LPAREN idTupleEntries RPAREN -> ^( TUPLE ( idTupleEntries )+ ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:11: LPAREN idTupleEntries RPAREN
            {
            LPAREN314=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_idTuple4340); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN314);

            pushFollow(FOLLOW_idTupleEntries_in_idTuple4342);
            idTupleEntries315=idTupleEntries();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idTupleEntries.add(idTupleEntries315.getTree());
            RPAREN316=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_idTuple4344); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN316);



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
            // 417:45: -> ^( TUPLE ( idTupleEntries )+ )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:417:48: ^( TUPLE ( idTupleEntries )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:1: idTupleEntries : idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ ;
    public final EulangParser.idTupleEntries_return idTupleEntries() throws RecognitionException {
        EulangParser.idTupleEntries_return retval = new EulangParser.idTupleEntries_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA318=null;
        EulangParser.idOrScopeRef_return idOrScopeRef317 = null;

        EulangParser.idOrScopeRef_return idOrScopeRef319 = null;


        CommonTree COMMA318_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:16: ( idOrScopeRef ( COMMA idOrScopeRef )+ -> ( idOrScopeRef )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:18: idOrScopeRef ( COMMA idOrScopeRef )+
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries4372);
            idOrScopeRef317=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef317.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:31: ( COMMA idOrScopeRef )+
            int cnt96=0;
            loop96:
            do {
                int alt96=2;
                int LA96_0 = input.LA(1);

                if ( (LA96_0==COMMA) ) {
                    alt96=1;
                }


                switch (alt96) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:420:32: COMMA idOrScopeRef
            	    {
            	    COMMA318=(Token)match(input,COMMA,FOLLOW_COMMA_in_idTupleEntries4375); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA318);

            	    pushFollow(FOLLOW_idOrScopeRef_in_idTupleEntries4377);
            	    idOrScopeRef319=idOrScopeRef();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef319.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt96 >= 1 ) break loop96;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(96, input);
                        throw eee;
                }
                cnt96++;
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
            // 420:54: -> ( idOrScopeRef )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:1: rhsExpr : condStar -> condStar ;
    public final EulangParser.rhsExpr_return rhsExpr() throws RecognitionException {
        EulangParser.rhsExpr_return retval = new EulangParser.rhsExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.condStar_return condStar320 = null;


        RewriteRuleSubtreeStream stream_condStar=new RewriteRuleSubtreeStream(adaptor,"rule condStar");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:9: ( condStar -> condStar )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:423:13: condStar
            {
            pushFollow(FOLLOW_condStar_in_rhsExpr4398);
            condStar320=condStar();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_condStar.add(condStar320.getTree());


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
            // 423:22: -> condStar
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:1: arglist : ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) ;
    public final EulangParser.arglist_return arglist() throws RecognitionException {
        EulangParser.arglist_return retval = new EulangParser.arglist_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMMA322=null;
        Token COMMA324=null;
        EulangParser.arg_return arg321 = null;

        EulangParser.arg_return arg323 = null;


        CommonTree COMMA322_tree=null;
        CommonTree COMMA324_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arg=new RewriteRuleSubtreeStream(adaptor,"rule arg");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:8: ( ( arg ( COMMA arg )* ( COMMA )? )? -> ^( ARGLIST ( arg )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:10: ( arg ( COMMA arg )* ( COMMA )? )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:10: ( arg ( COMMA arg )* ( COMMA )? )?
            int alt99=2;
            int LA99_0 = input.LA(1);

            if ( (LA99_0==CODE||LA99_0==GOTO||(LA99_0>=SIZEOF && LA99_0<=TYPEOF)||LA99_0==ID||LA99_0==COLON||LA99_0==LBRACE||LA99_0==LPAREN||LA99_0==NIL||LA99_0==IF||LA99_0==NOT||(LA99_0>=TILDE && LA99_0<=AMP)||LA99_0==MINUS||(LA99_0>=PLUSPLUS && LA99_0<=STRING_LITERAL)||(LA99_0>=FALSE && LA99_0<=COLONS)) ) {
                alt99=1;
            }
            switch (alt99) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:11: arg ( COMMA arg )* ( COMMA )?
                    {
                    pushFollow(FOLLOW_arg_in_arglist4419);
                    arg321=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_arg.add(arg321.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:15: ( COMMA arg )*
                    loop97:
                    do {
                        int alt97=2;
                        int LA97_0 = input.LA(1);

                        if ( (LA97_0==COMMA) ) {
                            int LA97_1 = input.LA(2);

                            if ( (LA97_1==CODE||LA97_1==GOTO||(LA97_1>=SIZEOF && LA97_1<=TYPEOF)||LA97_1==ID||LA97_1==COLON||LA97_1==LBRACE||LA97_1==LPAREN||LA97_1==NIL||LA97_1==IF||LA97_1==NOT||(LA97_1>=TILDE && LA97_1<=AMP)||LA97_1==MINUS||(LA97_1>=PLUSPLUS && LA97_1<=STRING_LITERAL)||(LA97_1>=FALSE && LA97_1<=COLONS)) ) {
                                alt97=1;
                            }


                        }


                        switch (alt97) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:17: COMMA arg
                    	    {
                    	    COMMA322=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist4423); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA322);

                    	    pushFollow(FOLLOW_arg_in_arglist4425);
                    	    arg323=arg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_arg.add(arg323.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop97;
                        }
                    } while (true);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:29: ( COMMA )?
                    int alt98=2;
                    int LA98_0 = input.LA(1);

                    if ( (LA98_0==COMMA) ) {
                        alt98=1;
                    }
                    switch (alt98) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:29: COMMA
                            {
                            COMMA324=(Token)match(input,COMMA,FOLLOW_COMMA_in_arglist4429); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_COMMA.add(COMMA324);


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
            // 426:61: -> ^( ARGLIST ( arg )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:64: ^( ARGLIST ( arg )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:426:74: ( arg )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:1: arg : ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) );
    public final EulangParser.arg_return arg() throws RecognitionException {
        EulangParser.arg_return retval = new EulangParser.arg_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACE326=null;
        Token RBRACE328=null;
        EulangParser.assignExpr_return assignExpr325 = null;

        EulangParser.codestmtlist_return codestmtlist327 = null;

        EulangParser.gotoStmt_return gotoStmt329 = null;


        CommonTree LBRACE326_tree=null;
        CommonTree RBRACE328_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_gotoStmt=new RewriteRuleSubtreeStream(adaptor,"rule gotoStmt");
        RewriteRuleSubtreeStream stream_codestmtlist=new RewriteRuleSubtreeStream(adaptor,"rule codestmtlist");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:4: ( assignExpr -> ^( EXPR assignExpr ) | LBRACE codestmtlist RBRACE -> ^( EXPR ^( CODE codestmtlist ) ) | gotoStmt -> ^( EXPR gotoStmt ) )
            int alt100=3;
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
                alt100=1;
                }
                break;
            case LBRACE:
                {
                alt100=2;
                }
                break;
            case GOTO:
                {
                alt100=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 100, 0, input);

                throw nvae;
            }

            switch (alt100) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:7: assignExpr
                    {
                    pushFollow(FOLLOW_assignExpr_in_arg4478);
                    assignExpr325=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr325.getTree());


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
                    // 429:37: -> ^( EXPR assignExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:429:40: ^( EXPR assignExpr )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:5: LBRACE codestmtlist RBRACE
                    {
                    LBRACE326=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_arg4511); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE326);

                    pushFollow(FOLLOW_codestmtlist_in_arg4513);
                    codestmtlist327=codestmtlist();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_codestmtlist.add(codestmtlist327.getTree());
                    RBRACE328=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_arg4515); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE328);



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
                    // 430:37: -> ^( EXPR ^( CODE codestmtlist ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:40: ^( EXPR ^( CODE codestmtlist ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(EXPR, "EXPR"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:430:47: ^( CODE codestmtlist )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:431:5: gotoStmt
                    {
                    pushFollow(FOLLOW_gotoStmt_in_arg4539);
                    gotoStmt329=gotoStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_gotoStmt.add(gotoStmt329.getTree());


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
                    // 431:37: -> ^( EXPR gotoStmt )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:431:40: ^( EXPR gotoStmt )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:452:1: condStar : ( cond -> cond | IF ifExprs -> ifExprs );
    public final EulangParser.condStar_return condStar() throws RecognitionException {
        EulangParser.condStar_return retval = new EulangParser.condStar_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token IF331=null;
        EulangParser.cond_return cond330 = null;

        EulangParser.ifExprs_return ifExprs332 = null;


        CommonTree IF331_tree=null;
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleSubtreeStream stream_ifExprs=new RewriteRuleSubtreeStream(adaptor,"rule ifExprs");
        RewriteRuleSubtreeStream stream_cond=new RewriteRuleSubtreeStream(adaptor,"rule cond");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:452:9: ( cond -> cond | IF ifExprs -> ifExprs )
            int alt101=2;
            int LA101_0 = input.LA(1);

            if ( (LA101_0==CODE||(LA101_0>=SIZEOF && LA101_0<=TYPEOF)||LA101_0==ID||LA101_0==COLON||LA101_0==LPAREN||LA101_0==NIL||LA101_0==NOT||(LA101_0>=TILDE && LA101_0<=AMP)||LA101_0==MINUS||(LA101_0>=PLUSPLUS && LA101_0<=STRING_LITERAL)||(LA101_0>=FALSE && LA101_0<=COLONS)) ) {
                alt101=1;
            }
            else if ( (LA101_0==IF) ) {
                alt101=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 101, 0, input);

                throw nvae;
            }
            switch (alt101) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:452:11: cond
                    {
                    pushFollow(FOLLOW_cond_in_condStar4600);
                    cond330=cond();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_cond.add(cond330.getTree());


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
                    // 452:16: -> cond
                    {
                        adaptor.addChild(root_0, stream_cond.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:453:6: IF ifExprs
                    {
                    IF331=(Token)match(input,IF,FOLLOW_IF_in_condStar4611); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_IF.add(IF331);

                    pushFollow(FOLLOW_ifExprs_in_condStar4613);
                    ifExprs332=ifExprs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ifExprs.add(ifExprs332.getTree());


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
                    // 453:17: -> ifExprs
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:1: ifExprs : thenClause elses -> ^( CONDLIST thenClause elses ) ;
    public final EulangParser.ifExprs_return ifExprs() throws RecognitionException {
        EulangParser.ifExprs_return retval = new EulangParser.ifExprs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.thenClause_return thenClause333 = null;

        EulangParser.elses_return elses334 = null;


        RewriteRuleSubtreeStream stream_thenClause=new RewriteRuleSubtreeStream(adaptor,"rule thenClause");
        RewriteRuleSubtreeStream stream_elses=new RewriteRuleSubtreeStream(adaptor,"rule elses");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:9: ( thenClause elses -> ^( CONDLIST thenClause elses ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:11: thenClause elses
            {
            pushFollow(FOLLOW_thenClause_in_ifExprs4632);
            thenClause333=thenClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_thenClause.add(thenClause333.getTree());
            pushFollow(FOLLOW_elses_in_ifExprs4634);
            elses334=elses();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elses.add(elses334.getTree());


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
            // 459:28: -> ^( CONDLIST thenClause elses )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:459:31: ^( CONDLIST thenClause elses )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:461:1: thenClause : t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) ;
    public final EulangParser.thenClause_return thenClause() throws RecognitionException {
        EulangParser.thenClause_return retval = new EulangParser.thenClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token THEN335=null;
        Token COLON336=null;
        EulangParser.assignExpr_return t = null;

        EulangParser.condStmtExpr_return v = null;


        CommonTree THEN335_tree=null;
        CommonTree COLON336_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:461:12: (t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:461:14: t= assignExpr ( THEN | COLON ) v= condStmtExpr
            {
            pushFollow(FOLLOW_assignExpr_in_thenClause4656);
            t=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(t.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:3: ( THEN | COLON )
            int alt102=2;
            int LA102_0 = input.LA(1);

            if ( (LA102_0==THEN) ) {
                alt102=1;
            }
            else if ( (LA102_0==COLON) ) {
                alt102=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 102, 0, input);

                throw nvae;
            }
            switch (alt102) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:4: THEN
                    {
                    THEN335=(Token)match(input,THEN,FOLLOW_THEN_in_thenClause4662); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN335);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:9: COLON
                    {
                    COLON336=(Token)match(input,COLON,FOLLOW_COLON_in_thenClause4664); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON336);


                    }
                    break;

            }

            pushFollow(FOLLOW_condStmtExpr_in_thenClause4669);
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
            // 462:33: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:462:36: ^( CONDTEST $t $v)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:1: elses : ( elif )* elseClause -> ( elif )* elseClause ;
    public final EulangParser.elses_return elses() throws RecognitionException {
        EulangParser.elses_return retval = new EulangParser.elses_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.elif_return elif337 = null;

        EulangParser.elseClause_return elseClause338 = null;


        RewriteRuleSubtreeStream stream_elseClause=new RewriteRuleSubtreeStream(adaptor,"rule elseClause");
        RewriteRuleSubtreeStream stream_elif=new RewriteRuleSubtreeStream(adaptor,"rule elif");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:7: ( ( elif )* elseClause -> ( elif )* elseClause )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:9: ( elif )* elseClause
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:9: ( elif )*
            loop103:
            do {
                int alt103=2;
                int LA103_0 = input.LA(1);

                if ( (LA103_0==ELIF) ) {
                    alt103=1;
                }


                switch (alt103) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:9: elif
            	    {
            	    pushFollow(FOLLOW_elif_in_elses4697);
            	    elif337=elif();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_elif.add(elif337.getTree());

            	    }
            	    break;

            	default :
            	    break loop103;
                }
            } while (true);

            pushFollow(FOLLOW_elseClause_in_elses4700);
            elseClause338=elseClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elseClause.add(elseClause338.getTree());


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
            // 464:29: -> ( elif )* elseClause
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:464:32: ( elif )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:1: elif : ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) ;
    public final EulangParser.elif_return elif() throws RecognitionException {
        EulangParser.elif_return retval = new EulangParser.elif_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELIF339=null;
        Token THEN340=null;
        Token COLON341=null;
        EulangParser.assignExpr_return t = null;

        EulangParser.condStmtExpr_return v = null;


        CommonTree ELIF339_tree=null;
        CommonTree THEN340_tree=null;
        CommonTree COLON341_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_ELIF=new RewriteRuleTokenStream(adaptor,"token ELIF");
        RewriteRuleTokenStream stream_THEN=new RewriteRuleTokenStream(adaptor,"token THEN");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:6: ( ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr -> ^( CONDTEST $t $v) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:8: ELIF t= assignExpr ( THEN | COLON ) v= condStmtExpr
            {
            ELIF339=(Token)match(input,ELIF,FOLLOW_ELIF_in_elif4723); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ELIF.add(ELIF339);

            pushFollow(FOLLOW_assignExpr_in_elif4727);
            t=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(t.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:26: ( THEN | COLON )
            int alt104=2;
            int LA104_0 = input.LA(1);

            if ( (LA104_0==THEN) ) {
                alt104=1;
            }
            else if ( (LA104_0==COLON) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:27: THEN
                    {
                    THEN340=(Token)match(input,THEN,FOLLOW_THEN_in_elif4730); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_THEN.add(THEN340);


                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:32: COLON
                    {
                    COLON341=(Token)match(input,COLON,FOLLOW_COLON_in_elif4732); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_COLON.add(COLON341);


                    }
                    break;

            }

            pushFollow(FOLLOW_condStmtExpr_in_elif4737);
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
            // 466:55: -> ^( CONDTEST $t $v)
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:466:58: ^( CONDTEST $t $v)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:1: elseClause : ( ELSE condStmtExpr -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) );
    public final EulangParser.elseClause_return elseClause() throws RecognitionException {
        EulangParser.elseClause_return retval = new EulangParser.elseClause_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ELSE342=null;
        Token FI344=null;
        EulangParser.condStmtExpr_return condStmtExpr343 = null;


        CommonTree ELSE342_tree=null;
        CommonTree FI344_tree=null;
        RewriteRuleTokenStream stream_FI=new RewriteRuleTokenStream(adaptor,"token FI");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_condStmtExpr=new RewriteRuleSubtreeStream(adaptor,"rule condStmtExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:12: ( ELSE condStmtExpr -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr ) | FI -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) ) )
            int alt105=2;
            int LA105_0 = input.LA(1);

            if ( (LA105_0==ELSE) ) {
                alt105=1;
            }
            else if ( (LA105_0==FI) ) {
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:14: ELSE condStmtExpr
                    {
                    ELSE342=(Token)match(input,ELSE,FOLLOW_ELSE_in_elseClause4763); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ELSE.add(ELSE342);

                    pushFollow(FOLLOW_condStmtExpr_in_elseClause4765);
                    condStmtExpr343=condStmtExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_condStmtExpr.add(condStmtExpr343.getTree());


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
                    // 468:38: -> ^( CONDTEST ^( LIT TRUE ) condStmtExpr )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:41: ^( CONDTEST ^( LIT TRUE ) condStmtExpr )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:468:52: ^( LIT TRUE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:6: FI
                    {
                    FI344=(Token)match(input,FI,FOLLOW_FI_in_elseClause4792); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FI.add(FI344);



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
                    // 469:9: -> ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:12: ^( CONDTEST ^( LIT TRUE ) ^( LIT NIL ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:23: ^( LIT TRUE )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIT, "LIT"), root_2);

                        adaptor.addChild(root_2, (CommonTree)adaptor.create(TRUE, "TRUE"));

                        adaptor.addChild(root_1, root_2);
                        }
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:469:35: ^( LIT NIL )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:1: condStmtExpr : ( arg | breakStmt );
    public final EulangParser.condStmtExpr_return condStmtExpr() throws RecognitionException {
        EulangParser.condStmtExpr_return retval = new EulangParser.condStmtExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.arg_return arg345 = null;

        EulangParser.breakStmt_return breakStmt346 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:14: ( arg | breakStmt )
            int alt106=2;
            int LA106_0 = input.LA(1);

            if ( (LA106_0==CODE||LA106_0==GOTO||(LA106_0>=SIZEOF && LA106_0<=TYPEOF)||LA106_0==ID||LA106_0==COLON||LA106_0==LBRACE||LA106_0==LPAREN||LA106_0==NIL||LA106_0==IF||LA106_0==NOT||(LA106_0>=TILDE && LA106_0<=AMP)||LA106_0==MINUS||(LA106_0>=PLUSPLUS && LA106_0<=STRING_LITERAL)||(LA106_0>=FALSE && LA106_0<=COLONS)) ) {
                alt106=1;
            }
            else if ( (LA106_0==BREAK) ) {
                alt106=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 106, 0, input);

                throw nvae;
            }
            switch (alt106) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:16: arg
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_arg_in_condStmtExpr4823);
                    arg345=arg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, arg345.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:472:22: breakStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_breakStmt_in_condStmtExpr4827);
                    breakStmt346=breakStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, breakStmt346.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:1: cond : ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )* ;
    public final EulangParser.cond_return cond() throws RecognitionException {
        EulangParser.cond_return retval = new EulangParser.cond_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token QUESTION348=null;
        Token COLON349=null;
        EulangParser.logor_return t = null;

        EulangParser.logor_return f = null;

        EulangParser.logor_return logor347 = null;


        CommonTree QUESTION348_tree=null;
        CommonTree COLON349_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");
        RewriteRuleSubtreeStream stream_logor=new RewriteRuleSubtreeStream(adaptor,"rule logor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:5: ( ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:10: ( logor -> logor ) ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:10: ( logor -> logor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:474:12: logor
            {
            pushFollow(FOLLOW_logor_in_cond4844);
            logor347=logor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logor.add(logor347.getTree());


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
            // 474:19: -> logor
            {
                adaptor.addChild(root_0, stream_logor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:7: ( QUESTION t= logor COLON f= logor -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) ) )*
            loop107:
            do {
                int alt107=2;
                int LA107_0 = input.LA(1);

                if ( (LA107_0==QUESTION) ) {
                    alt107=1;
                }


                switch (alt107) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:9: QUESTION t= logor COLON f= logor
            	    {
            	    QUESTION348=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_cond4861); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION348);

            	    pushFollow(FOLLOW_logor_in_cond4865);
            	    t=logor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logor.add(t.getTree());
            	    COLON349=(Token)match(input,COLON,FOLLOW_COLON_in_cond4867); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COLON.add(COLON349);

            	    pushFollow(FOLLOW_logor_in_cond4871);
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
            	    // 475:40: -> ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:43: ^( CONDLIST ^( CONDTEST $cond $t) ^( CONDTEST ^( LIT TRUE ) $f) )
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDLIST, "CONDLIST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:54: ^( CONDTEST $cond $t)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_2);

            	        adaptor.addChild(root_2, stream_retval.nextTree());
            	        adaptor.addChild(root_2, stream_t.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:75: ^( CONDTEST ^( LIT TRUE ) $f)
            	        {
            	        CommonTree root_2 = (CommonTree)adaptor.nil();
            	        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONDTEST, "CONDTEST"), root_2);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:475:86: ^( LIT TRUE )
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
            	    break loop107;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:1: logor : ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* ;
    public final EulangParser.logor_return logor() throws RecognitionException {
        EulangParser.logor_return retval = new EulangParser.logor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token OR351=null;
        EulangParser.logand_return r = null;

        EulangParser.logand_return logand350 = null;


        CommonTree OR351_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_logand=new RewriteRuleSubtreeStream(adaptor,"rule logand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:7: ( ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:9: ( logand -> logand ) ( OR r= logand -> ^( OR $logor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:9: ( logand -> logand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:478:11: logand
            {
            pushFollow(FOLLOW_logand_in_logor4915);
            logand350=logand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_logand.add(logand350.getTree());


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
            // 478:19: -> logand
            {
                adaptor.addChild(root_0, stream_logand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:479:7: ( OR r= logand -> ^( OR $logor $r) )*
            loop108:
            do {
                int alt108=2;
                int LA108_0 = input.LA(1);

                if ( (LA108_0==OR) ) {
                    alt108=1;
                }


                switch (alt108) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:479:9: OR r= logand
            	    {
            	    OR351=(Token)match(input,OR,FOLLOW_OR_in_logor4932); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_OR.add(OR351);

            	    pushFollow(FOLLOW_logand_in_logor4936);
            	    r=logand();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_logand.add(r.getTree());


            	    // AST REWRITE
            	    // elements: OR, r, logor
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
            	    // 479:21: -> ^( OR $logor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:479:24: ^( OR $logor $r)
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
            	    break loop108;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:1: logand : ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* ;
    public final EulangParser.logand_return logand() throws RecognitionException {
        EulangParser.logand_return retval = new EulangParser.logand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AND353=null;
        EulangParser.not_return r = null;

        EulangParser.not_return not352 = null;


        CommonTree AND353_tree=null;
        RewriteRuleTokenStream stream_AND=new RewriteRuleTokenStream(adaptor,"token AND");
        RewriteRuleSubtreeStream stream_not=new RewriteRuleSubtreeStream(adaptor,"rule not");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:8: ( ( not -> not ) ( AND r= not -> ^( AND $logand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:10: ( not -> not ) ( AND r= not -> ^( AND $logand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:10: ( not -> not )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:481:12: not
            {
            pushFollow(FOLLOW_not_in_logand4967);
            not352=not();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_not.add(not352.getTree());


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
            // 481:16: -> not
            {
                adaptor.addChild(root_0, stream_not.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:482:7: ( AND r= not -> ^( AND $logand $r) )*
            loop109:
            do {
                int alt109=2;
                int LA109_0 = input.LA(1);

                if ( (LA109_0==AND) ) {
                    alt109=1;
                }


                switch (alt109) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:482:9: AND r= not
            	    {
            	    AND353=(Token)match(input,AND,FOLLOW_AND_in_logand4983); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AND.add(AND353);

            	    pushFollow(FOLLOW_not_in_logand4987);
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
            	    // 482:19: -> ^( AND $logand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:482:22: ^( AND $logand $r)
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
            	    break loop109;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:1: not : ( comp -> comp | NOT u= comp -> ^( NOT $u) );
    public final EulangParser.not_return not() throws RecognitionException {
        EulangParser.not_return retval = new EulangParser.not_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NOT355=null;
        EulangParser.comp_return u = null;

        EulangParser.comp_return comp354 = null;


        CommonTree NOT355_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_comp=new RewriteRuleSubtreeStream(adaptor,"rule comp");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:5: ( comp -> comp | NOT u= comp -> ^( NOT $u) )
            int alt110=2;
            int LA110_0 = input.LA(1);

            if ( (LA110_0==CODE||(LA110_0>=SIZEOF && LA110_0<=TYPEOF)||LA110_0==ID||LA110_0==COLON||LA110_0==LPAREN||LA110_0==NIL||(LA110_0>=TILDE && LA110_0<=AMP)||LA110_0==MINUS||(LA110_0>=PLUSPLUS && LA110_0<=STRING_LITERAL)||(LA110_0>=FALSE && LA110_0<=COLONS)) ) {
                alt110=1;
            }
            else if ( (LA110_0==NOT) ) {
                alt110=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 110, 0, input);

                throw nvae;
            }
            switch (alt110) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:485:8: comp
                    {
                    pushFollow(FOLLOW_comp_in_not5033);
                    comp354=comp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comp.add(comp354.getTree());


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
                    // 485:17: -> comp
                    {
                        adaptor.addChild(root_0, stream_comp.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:486:7: NOT u= comp
                    {
                    NOT355=(Token)match(input,NOT,FOLLOW_NOT_in_not5049); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NOT.add(NOT355);

                    pushFollow(FOLLOW_comp_in_not5053);
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
                    // 486:22: -> ^( NOT $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:486:25: ^( NOT $u)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:1: comp : ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )* ;
    public final EulangParser.comp_return comp() throws RecognitionException {
        EulangParser.comp_return retval = new EulangParser.comp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token COMPEQ357=null;
        Token COMPNE358=null;
        Token COMPLE359=null;
        Token COMPGE360=null;
        Token COMPULE361=null;
        Token COMPUGE362=null;
        Token LESS363=null;
        Token ULESS364=null;
        Token GREATER365=null;
        Token UGREATER366=null;
        EulangParser.bitor_return r = null;

        EulangParser.bitor_return bitor356 = null;


        CommonTree COMPEQ357_tree=null;
        CommonTree COMPNE358_tree=null;
        CommonTree COMPLE359_tree=null;
        CommonTree COMPGE360_tree=null;
        CommonTree COMPULE361_tree=null;
        CommonTree COMPUGE362_tree=null;
        CommonTree LESS363_tree=null;
        CommonTree ULESS364_tree=null;
        CommonTree GREATER365_tree=null;
        CommonTree UGREATER366_tree=null;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:5: ( ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:9: ( bitor -> bitor ) ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:9: ( bitor -> bitor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:489:11: bitor
            {
            pushFollow(FOLLOW_bitor_in_comp5087);
            bitor356=bitor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitor.add(bitor356.getTree());


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
            // 489:24: -> bitor
            {
                adaptor.addChild(root_0, stream_bitor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:490:7: ( COMPEQ r= bitor -> ^( COMPEQ $comp $r) | COMPNE r= bitor -> ^( COMPNE $comp $r) | COMPLE r= bitor -> ^( COMPLE $comp $r) | COMPGE r= bitor -> ^( COMPGE $comp $r) | COMPULE r= bitor -> ^( COMPULE $comp $r) | COMPUGE r= bitor -> ^( COMPUGE $comp $r) | LESS r= bitor -> ^( LESS $comp $r) | ULESS r= bitor -> ^( ULESS $comp $r) | GREATER r= bitor -> ^( GREATER $comp $r) | UGREATER r= bitor -> ^( UGREATER $comp $r) )*
            loop111:
            do {
                int alt111=11;
                switch ( input.LA(1) ) {
                case COMPEQ:
                    {
                    alt111=1;
                    }
                    break;
                case COMPNE:
                    {
                    alt111=2;
                    }
                    break;
                case COMPLE:
                    {
                    alt111=3;
                    }
                    break;
                case COMPGE:
                    {
                    alt111=4;
                    }
                    break;
                case COMPULE:
                    {
                    alt111=5;
                    }
                    break;
                case COMPUGE:
                    {
                    alt111=6;
                    }
                    break;
                case LESS:
                    {
                    alt111=7;
                    }
                    break;
                case ULESS:
                    {
                    alt111=8;
                    }
                    break;
                case GREATER:
                    {
                    alt111=9;
                    }
                    break;
                case UGREATER:
                    {
                    alt111=10;
                    }
                    break;

                }

                switch (alt111) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:490:9: COMPEQ r= bitor
            	    {
            	    COMPEQ357=(Token)match(input,COMPEQ,FOLLOW_COMPEQ_in_comp5120); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPEQ.add(COMPEQ357);

            	    pushFollow(FOLLOW_bitor_in_comp5124);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPEQ, comp, r
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
            	    // 490:24: -> ^( COMPEQ $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:490:27: ^( COMPEQ $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:491:9: COMPNE r= bitor
            	    {
            	    COMPNE358=(Token)match(input,COMPNE,FOLLOW_COMPNE_in_comp5146); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPNE.add(COMPNE358);

            	    pushFollow(FOLLOW_bitor_in_comp5150);
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
            	    // 491:24: -> ^( COMPNE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:491:27: ^( COMPNE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:492:9: COMPLE r= bitor
            	    {
            	    COMPLE359=(Token)match(input,COMPLE,FOLLOW_COMPLE_in_comp5172); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPLE.add(COMPLE359);

            	    pushFollow(FOLLOW_bitor_in_comp5176);
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
            	    // 492:27: -> ^( COMPLE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:492:30: ^( COMPLE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:493:9: COMPGE r= bitor
            	    {
            	    COMPGE360=(Token)match(input,COMPGE,FOLLOW_COMPGE_in_comp5201); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPGE.add(COMPGE360);

            	    pushFollow(FOLLOW_bitor_in_comp5205);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: comp, r, COMPGE
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
            	    // 493:27: -> ^( COMPGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:493:30: ^( COMPGE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:494:9: COMPULE r= bitor
            	    {
            	    COMPULE361=(Token)match(input,COMPULE,FOLLOW_COMPULE_in_comp5230); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPULE.add(COMPULE361);

            	    pushFollow(FOLLOW_bitor_in_comp5234);
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
            	    // 494:28: -> ^( COMPULE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:494:31: ^( COMPULE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:9: COMPUGE r= bitor
            	    {
            	    COMPUGE362=(Token)match(input,COMPUGE,FOLLOW_COMPUGE_in_comp5259); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMPUGE.add(COMPUGE362);

            	    pushFollow(FOLLOW_bitor_in_comp5263);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: COMPUGE, r, comp
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
            	    // 495:28: -> ^( COMPUGE $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:495:31: ^( COMPUGE $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:496:9: LESS r= bitor
            	    {
            	    LESS363=(Token)match(input,LESS,FOLLOW_LESS_in_comp5288); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LESS.add(LESS363);

            	    pushFollow(FOLLOW_bitor_in_comp5292);
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
            	    // 496:26: -> ^( LESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:496:29: ^( LESS $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:497:9: ULESS r= bitor
            	    {
            	    ULESS364=(Token)match(input,ULESS,FOLLOW_ULESS_in_comp5318); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ULESS.add(ULESS364);

            	    pushFollow(FOLLOW_bitor_in_comp5322);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, ULESS, comp
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
            	    // 497:27: -> ^( ULESS $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:497:30: ^( ULESS $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:498:9: GREATER r= bitor
            	    {
            	    GREATER365=(Token)match(input,GREATER,FOLLOW_GREATER_in_comp5348); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_GREATER.add(GREATER365);

            	    pushFollow(FOLLOW_bitor_in_comp5352);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, comp, GREATER
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
            	    // 498:28: -> ^( GREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:498:31: ^( GREATER $comp $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:499:9: UGREATER r= bitor
            	    {
            	    UGREATER366=(Token)match(input,UGREATER,FOLLOW_UGREATER_in_comp5377); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UGREATER.add(UGREATER366);

            	    pushFollow(FOLLOW_bitor_in_comp5381);
            	    r=bitor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_bitor.add(r.getTree());


            	    // AST REWRITE
            	    // elements: UGREATER, comp, r
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
            	    // 499:29: -> ^( UGREATER $comp $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:499:32: ^( UGREATER $comp $r)
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
    // $ANTLR end "comp"

    public static class bitor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:1: bitor : ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* ;
    public final EulangParser.bitor_return bitor() throws RecognitionException {
        EulangParser.bitor_return retval = new EulangParser.bitor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token BAR368=null;
        EulangParser.bitxor_return r = null;

        EulangParser.bitxor_return bitxor367 = null;


        CommonTree BAR368_tree=null;
        RewriteRuleTokenStream stream_BAR=new RewriteRuleTokenStream(adaptor,"token BAR");
        RewriteRuleSubtreeStream stream_bitxor=new RewriteRuleSubtreeStream(adaptor,"rule bitxor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:6: ( ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:8: ( bitxor -> bitxor ) ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:8: ( bitxor -> bitxor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:504:10: bitxor
            {
            pushFollow(FOLLOW_bitxor_in_bitor5431);
            bitxor367=bitxor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitxor.add(bitxor367.getTree());


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
            // 504:22: -> bitxor
            {
                adaptor.addChild(root_0, stream_bitxor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:7: ( BAR r= bitxor -> ^( BITOR $bitor $r) )*
            loop112:
            do {
                int alt112=2;
                int LA112_0 = input.LA(1);

                if ( (LA112_0==BAR) ) {
                    alt112=1;
                }


                switch (alt112) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:9: BAR r= bitxor
            	    {
            	    BAR368=(Token)match(input,BAR,FOLLOW_BAR_in_bitor5459); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_BAR.add(BAR368);

            	    pushFollow(FOLLOW_bitxor_in_bitor5463);
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
            	    // 505:23: -> ^( BITOR $bitor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:505:26: ^( BITOR $bitor $r)
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
    // $ANTLR end "bitor"

    public static class bitxor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "bitxor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:1: bitxor : ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )* ;
    public final EulangParser.bitxor_return bitxor() throws RecognitionException {
        EulangParser.bitxor_return retval = new EulangParser.bitxor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token TILDE370=null;
        EulangParser.bitand_return r = null;

        EulangParser.bitand_return bitand369 = null;


        CommonTree TILDE370_tree=null;
        RewriteRuleTokenStream stream_TILDE=new RewriteRuleTokenStream(adaptor,"token TILDE");
        RewriteRuleSubtreeStream stream_bitand=new RewriteRuleSubtreeStream(adaptor,"rule bitand");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:7: ( ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:9: ( bitand -> bitand ) ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:9: ( bitand -> bitand )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:507:11: bitand
            {
            pushFollow(FOLLOW_bitand_in_bitxor5489);
            bitand369=bitand();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_bitand.add(bitand369.getTree());


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
            // 507:23: -> bitand
            {
                adaptor.addChild(root_0, stream_bitand.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:508:7: ( TILDE r= bitand -> ^( BITXOR $bitxor $r) )*
            loop113:
            do {
                int alt113=2;
                int LA113_0 = input.LA(1);

                if ( (LA113_0==TILDE) ) {
                    alt113=1;
                }


                switch (alt113) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:508:9: TILDE r= bitand
            	    {
            	    TILDE370=(Token)match(input,TILDE,FOLLOW_TILDE_in_bitxor5517); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_TILDE.add(TILDE370);

            	    pushFollow(FOLLOW_bitand_in_bitxor5521);
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
            	    // 508:25: -> ^( BITXOR $bitxor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:508:28: ^( BITXOR $bitxor $r)
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
            	    break loop113;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:1: bitand : ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* ;
    public final EulangParser.bitand_return bitand() throws RecognitionException {
        EulangParser.bitand_return retval = new EulangParser.bitand_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token AMP372=null;
        EulangParser.shift_return r = null;

        EulangParser.shift_return shift371 = null;


        CommonTree AMP372_tree=null;
        RewriteRuleTokenStream stream_AMP=new RewriteRuleTokenStream(adaptor,"token AMP");
        RewriteRuleSubtreeStream stream_shift=new RewriteRuleSubtreeStream(adaptor,"rule shift");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:7: ( ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:9: ( shift -> shift ) ( AMP r= shift -> ^( BITAND $bitand $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:9: ( shift -> shift )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:510:11: shift
            {
            pushFollow(FOLLOW_shift_in_bitand5546);
            shift371=shift();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_shift.add(shift371.getTree());


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
            // 510:22: -> shift
            {
                adaptor.addChild(root_0, stream_shift.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:7: ( AMP r= shift -> ^( BITAND $bitand $r) )*
            loop114:
            do {
                int alt114=2;
                int LA114_0 = input.LA(1);

                if ( (LA114_0==AMP) ) {
                    alt114=1;
                }


                switch (alt114) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:9: AMP r= shift
            	    {
            	    AMP372=(Token)match(input,AMP,FOLLOW_AMP_in_bitand5574); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_AMP.add(AMP372);

            	    pushFollow(FOLLOW_shift_in_bitand5578);
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
            	    // 511:22: -> ^( BITAND $bitand $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:511:25: ^( BITAND $bitand $r)
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
    // $ANTLR end "bitand"

    public static class shift_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shift"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:1: shift : ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )* ;
    public final EulangParser.shift_return shift() throws RecognitionException {
        EulangParser.shift_return retval = new EulangParser.shift_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LSHIFT374=null;
        Token RSHIFT375=null;
        Token URSHIFT376=null;
        Token CRSHIFT377=null;
        Token CLSHIFT378=null;
        EulangParser.factor_return r = null;

        EulangParser.factor_return factor373 = null;


        CommonTree LSHIFT374_tree=null;
        CommonTree RSHIFT375_tree=null;
        CommonTree URSHIFT376_tree=null;
        CommonTree CRSHIFT377_tree=null;
        CommonTree CLSHIFT378_tree=null;
        RewriteRuleTokenStream stream_URSHIFT=new RewriteRuleTokenStream(adaptor,"token URSHIFT");
        RewriteRuleTokenStream stream_CLSHIFT=new RewriteRuleTokenStream(adaptor,"token CLSHIFT");
        RewriteRuleTokenStream stream_CRSHIFT=new RewriteRuleTokenStream(adaptor,"token CRSHIFT");
        RewriteRuleTokenStream stream_RSHIFT=new RewriteRuleTokenStream(adaptor,"token RSHIFT");
        RewriteRuleTokenStream stream_LSHIFT=new RewriteRuleTokenStream(adaptor,"token LSHIFT");
        RewriteRuleSubtreeStream stream_factor=new RewriteRuleSubtreeStream(adaptor,"rule factor");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:6: ( ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:9: ( factor -> factor ) ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:9: ( factor -> factor )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:514:11: factor
            {
            pushFollow(FOLLOW_factor_in_shift5605);
            factor373=factor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_factor.add(factor373.getTree());


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
            // 514:25: -> factor
            {
                adaptor.addChild(root_0, stream_factor.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:7: ( ( LSHIFT r= factor -> ^( LSHIFT $shift $r) ) | ( RSHIFT r= factor -> ^( RSHIFT $shift $r) ) | ( URSHIFT r= factor -> ^( URSHIFT $shift $r) ) | ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) ) | ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) ) )*
            loop115:
            do {
                int alt115=6;
                switch ( input.LA(1) ) {
                case LSHIFT:
                    {
                    alt115=1;
                    }
                    break;
                case RSHIFT:
                    {
                    alt115=2;
                    }
                    break;
                case URSHIFT:
                    {
                    alt115=3;
                    }
                    break;
                case CRSHIFT:
                    {
                    alt115=4;
                    }
                    break;
                case CLSHIFT:
                    {
                    alt115=5;
                    }
                    break;

                }

                switch (alt115) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:9: ( LSHIFT r= factor -> ^( LSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:11: LSHIFT r= factor
            	    {
            	    LSHIFT374=(Token)match(input,LSHIFT,FOLLOW_LSHIFT_in_shift5639); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LSHIFT.add(LSHIFT374);

            	    pushFollow(FOLLOW_factor_in_shift5643);
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
            	    // 515:29: -> ^( LSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:515:32: ^( LSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:9: ( RSHIFT r= factor -> ^( RSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:11: RSHIFT r= factor
            	    {
            	    RSHIFT375=(Token)match(input,RSHIFT,FOLLOW_RSHIFT_in_shift5672); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RSHIFT.add(RSHIFT375);

            	    pushFollow(FOLLOW_factor_in_shift5676);
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
            	    // 516:29: -> ^( RSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:516:32: ^( RSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:9: ( URSHIFT r= factor -> ^( URSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:11: URSHIFT r= factor
            	    {
            	    URSHIFT376=(Token)match(input,URSHIFT,FOLLOW_URSHIFT_in_shift5704); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_URSHIFT.add(URSHIFT376);

            	    pushFollow(FOLLOW_factor_in_shift5708);
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
            	    // 517:30: -> ^( URSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:517:33: ^( URSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:9: ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:9: ( CRSHIFT r= factor -> ^( CRSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:11: CRSHIFT r= factor
            	    {
            	    CRSHIFT377=(Token)match(input,CRSHIFT,FOLLOW_CRSHIFT_in_shift5736); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CRSHIFT.add(CRSHIFT377);

            	    pushFollow(FOLLOW_factor_in_shift5740);
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
            	    // 518:30: -> ^( CRSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:518:33: ^( CRSHIFT $shift $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:9: ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:9: ( CLSHIFT r= factor -> ^( CLSHIFT $shift $r) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:11: CLSHIFT r= factor
            	    {
            	    CLSHIFT378=(Token)match(input,CLSHIFT,FOLLOW_CLSHIFT_in_shift5768); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CLSHIFT.add(CLSHIFT378);

            	    pushFollow(FOLLOW_factor_in_shift5772);
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
            	    // 519:30: -> ^( CLSHIFT $shift $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:519:33: ^( CLSHIFT $shift $r)
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
    // $ANTLR end "shift"

    public static class factor_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "factor"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:522:1: factor : ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* ;
    public final EulangParser.factor_return factor() throws RecognitionException {
        EulangParser.factor_return retval = new EulangParser.factor_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PLUS380=null;
        Token MINUS381=null;
        EulangParser.term_return r = null;

        EulangParser.term_return term379 = null;


        CommonTree PLUS380_tree=null;
        CommonTree MINUS381_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_MINUS=new RewriteRuleTokenStream(adaptor,"token MINUS");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:5: ( ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:7: ( term -> term ) ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:7: ( term -> term )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:523:9: term
            {
            pushFollow(FOLLOW_term_in_factor5814);
            term379=term();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_term.add(term379.getTree());


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
            // 523:27: -> term
            {
                adaptor.addChild(root_0, stream_term.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:9: ( PLUS r= term -> ^( ADD $factor $r) | ( MINUS term )=> MINUS r= term -> ^( SUB $factor $r) )*
            loop116:
            do {
                int alt116=3;
                int LA116_0 = input.LA(1);

                if ( (LA116_0==PLUS) ) {
                    alt116=1;
                }
                else if ( (LA116_0==MINUS) && (synpred19_Eulang())) {
                    alt116=2;
                }


                switch (alt116) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:13: PLUS r= term
            	    {
            	    PLUS380=(Token)match(input,PLUS,FOLLOW_PLUS_in_factor5847); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PLUS.add(PLUS380);

            	    pushFollow(FOLLOW_term_in_factor5851);
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
            	    // 524:33: -> ^( ADD $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:524:36: ^( ADD $factor $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:12: ( MINUS term )=> MINUS r= term
            	    {
            	    MINUS381=(Token)match(input,MINUS,FOLLOW_MINUS_in_factor5893); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MINUS.add(MINUS381);

            	    pushFollow(FOLLOW_term_in_factor5897);
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
            	    // 525:49: -> ^( SUB $factor $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:52: ^( SUB $factor $r)
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
    // $ANTLR end "factor"

    public static class term_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:1: term : ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )* ;
    public final EulangParser.term_return term() throws RecognitionException {
        EulangParser.term_return retval = new EulangParser.term_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STAR383=null;
        Token SLASH384=null;
        Token REM385=null;
        Token UDIV386=null;
        Token UREM387=null;
        Token MOD388=null;
        EulangParser.unary_return r = null;

        EulangParser.unary_return unary382 = null;


        CommonTree STAR383_tree=null;
        CommonTree SLASH384_tree=null;
        CommonTree REM385_tree=null;
        CommonTree UDIV386_tree=null;
        CommonTree UREM387_tree=null;
        CommonTree MOD388_tree=null;
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_SLASH=new RewriteRuleTokenStream(adaptor,"token SLASH");
        RewriteRuleTokenStream stream_REM=new RewriteRuleTokenStream(adaptor,"token REM");
        RewriteRuleTokenStream stream_UREM=new RewriteRuleTokenStream(adaptor,"token UREM");
        RewriteRuleTokenStream stream_MOD=new RewriteRuleTokenStream(adaptor,"token MOD");
        RewriteRuleTokenStream stream_UDIV=new RewriteRuleTokenStream(adaptor,"token UDIV");
        RewriteRuleSubtreeStream stream_unary=new RewriteRuleSubtreeStream(adaptor,"rule unary");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:6: ( ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:8: ( unary -> unary ) ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:8: ( unary -> unary )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:529:10: unary
            {
            pushFollow(FOLLOW_unary_in_term5942);
            unary382=unary();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_unary.add(unary382.getTree());


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
            // 529:33: -> unary
            {
                adaptor.addChild(root_0, stream_unary.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:9: ( ( STAR unary )=> STAR r= unary -> ^( MUL $term $r) | SLASH r= unary -> ^( DIV $term $r) | REM r= unary -> ^( REM $term $r) | UDIV r= unary -> ^( UDIV $term $r) | UREM r= unary -> ^( UREM $term $r) | MOD r= unary -> ^( MOD $term $r) )*
            loop117:
            do {
                int alt117=7;
                int LA117_0 = input.LA(1);

                if ( (LA117_0==STAR) && (synpred20_Eulang())) {
                    alt117=1;
                }
                else if ( (LA117_0==SLASH) ) {
                    alt117=2;
                }
                else if ( (LA117_0==REM) ) {
                    alt117=3;
                }
                else if ( (LA117_0==UDIV) ) {
                    alt117=4;
                }
                else if ( (LA117_0==UREM) ) {
                    alt117=5;
                }
                else if ( (LA117_0==MOD) ) {
                    alt117=6;
                }


                switch (alt117) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:11: ( STAR unary )=> STAR r= unary
            	    {
            	    STAR383=(Token)match(input,STAR,FOLLOW_STAR_in_term5986); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_STAR.add(STAR383);

            	    pushFollow(FOLLOW_unary_in_term5990);
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
            	    // 530:52: -> ^( MUL $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:55: ^( MUL $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:11: SLASH r= unary
            	    {
            	    SLASH384=(Token)match(input,SLASH,FOLLOW_SLASH_in_term6026); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_SLASH.add(SLASH384);

            	    pushFollow(FOLLOW_unary_in_term6030);
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
            	    // 531:36: -> ^( DIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:531:39: ^( DIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:532:11: REM r= unary
            	    {
            	    REM385=(Token)match(input,REM,FOLLOW_REM_in_term6065); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_REM.add(REM385);

            	    pushFollow(FOLLOW_unary_in_term6069);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, term, REM
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
            	    // 532:34: -> ^( REM $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:532:37: ^( REM $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:533:11: UDIV r= unary
            	    {
            	    UDIV386=(Token)match(input,UDIV,FOLLOW_UDIV_in_term6104); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UDIV.add(UDIV386);

            	    pushFollow(FOLLOW_unary_in_term6108);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: r, UDIV, term
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
            	    // 533:35: -> ^( UDIV $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:533:38: ^( UDIV $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:534:11: UREM r= unary
            	    {
            	    UREM387=(Token)match(input,UREM,FOLLOW_UREM_in_term6143); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_UREM.add(UREM387);

            	    pushFollow(FOLLOW_unary_in_term6147);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: UREM, r, term
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
            	    // 534:35: -> ^( UREM $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:534:38: ^( UREM $term $r)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:11: MOD r= unary
            	    {
            	    MOD388=(Token)match(input,MOD,FOLLOW_MOD_in_term6182); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_MOD.add(MOD388);

            	    pushFollow(FOLLOW_unary_in_term6186);
            	    r=unary();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_unary.add(r.getTree());


            	    // AST REWRITE
            	    // elements: term, r, MOD
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
            	    // 535:34: -> ^( MOD $term $r)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:535:37: ^( MOD $term $r)
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
    // $ANTLR end "term"

    public static class unary_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unary"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:1: unary : ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) );
    public final EulangParser.unary_return unary() throws RecognitionException {
        EulangParser.unary_return retval = new EulangParser.unary_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token MINUS389=null;
        Token TILDE390=null;
        Token PLUSPLUS391=null;
        Token MINUSMINUS392=null;
        Token PLUSPLUS394=null;
        Token MINUSMINUS395=null;
        Token AMP396=null;
        Token SIZEOF398=null;
        Token TYPEOF400=null;
        EulangParser.unary_return u = null;

        EulangParser.lhs_return a = null;

        EulangParser.atom_return atom393 = null;

        EulangParser.lhs_return lhs397 = null;

        EulangParser.atom_return atom399 = null;

        EulangParser.atom_return atom401 = null;


        CommonTree MINUS389_tree=null;
        CommonTree TILDE390_tree=null;
        CommonTree PLUSPLUS391_tree=null;
        CommonTree MINUSMINUS392_tree=null;
        CommonTree PLUSPLUS394_tree=null;
        CommonTree MINUSMINUS395_tree=null;
        CommonTree AMP396_tree=null;
        CommonTree SIZEOF398_tree=null;
        CommonTree TYPEOF400_tree=null;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:6: ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) )
            int alt118=10;
            alt118 = dfa118.predict(input);
            switch (alt118) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:9: MINUS u= unary
                    {
                    MINUS389=(Token)match(input,MINUS,FOLLOW_MINUS_in_unary6259); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUS.add(MINUS389);

                    pushFollow(FOLLOW_unary_in_unary6263);
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
                    // 540:23: -> ^( NEG $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:540:26: ^( NEG $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:541:9: TILDE u= unary
                    {
                    TILDE390=(Token)match(input,TILDE,FOLLOW_TILDE_in_unary6283); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TILDE.add(TILDE390);

                    pushFollow(FOLLOW_unary_in_unary6287);
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
                    // 541:27: -> ^( INV $u)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:541:30: ^( INV $u)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:9: ( lhs PLUSPLUS )=>a= lhs PLUSPLUS
                    {
                    pushFollow(FOLLOW_lhs_in_unary6322);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());
                    PLUSPLUS391=(Token)match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unary6324); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUSPLUS.add(PLUSPLUS391);



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
                    // 542:44: -> ^( POSTINC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:47: ^( POSTINC $a)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:9: ( lhs MINUSMINUS )=>a= lhs MINUSMINUS
                    {
                    pushFollow(FOLLOW_lhs_in_unary6355);
                    a=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(a.getTree());
                    MINUSMINUS392=(Token)match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_unary6357); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUSMINUS.add(MINUSMINUS392);



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
                    // 543:47: -> ^( POSTDEC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:50: ^( POSTDEC $a)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:544:9: ( atom -> atom )
                    {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:544:9: ( atom -> atom )
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:544:11: atom
                    {
                    pushFollow(FOLLOW_atom_in_unary6378);
                    atom393=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom393.getTree());


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
                    // 544:23: -> atom
                    {
                        adaptor.addChild(root_0, stream_atom.nextTree());

                    }

                    retval.tree = root_0;}
                    }


                    }
                    break;
                case 6 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:545:9: PLUSPLUS a= lhs
                    {
                    PLUSPLUS394=(Token)match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unary6409); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_PLUSPLUS.add(PLUSPLUS394);

                    pushFollow(FOLLOW_lhs_in_unary6413);
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
                    // 545:26: -> ^( PREINC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:545:29: ^( PREINC $a)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:9: MINUSMINUS a= lhs
                    {
                    MINUSMINUS395=(Token)match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_unary6434); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_MINUSMINUS.add(MINUSMINUS395);

                    pushFollow(FOLLOW_lhs_in_unary6438);
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
                    // 546:26: -> ^( PREDEC $a)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:546:29: ^( PREDEC $a)
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:547:10: AMP lhs
                    {
                    AMP396=(Token)match(input,AMP,FOLLOW_AMP_in_unary6458); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AMP.add(AMP396);

                    pushFollow(FOLLOW_lhs_in_unary6460);
                    lhs397=lhs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_lhs.add(lhs397.getTree());


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
                    // 547:41: -> ^( ADDROF lhs )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:547:44: ^( ADDROF lhs )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:9: SIZEOF atom
                    {
                    SIZEOF398=(Token)match(input,SIZEOF,FOLLOW_SIZEOF_in_unary6501); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SIZEOF.add(SIZEOF398);

                    pushFollow(FOLLOW_atom_in_unary6503);
                    atom399=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom399.getTree());


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
                    // 548:27: -> ^( SIZEOF atom )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:548:30: ^( SIZEOF atom )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:9: TYPEOF atom
                    {
                    TYPEOF400=(Token)match(input,TYPEOF,FOLLOW_TYPEOF_in_unary6527); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TYPEOF.add(TYPEOF400);

                    pushFollow(FOLLOW_atom_in_unary6529);
                    atom401=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom401.getTree());


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
                    // 549:27: -> ^( TYPEOF atom )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:549:30: ^( TYPEOF atom )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:552:1: lhs : ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )? ;
    public final EulangParser.lhs_return lhs() throws RecognitionException {
        EulangParser.lhs_return retval = new EulangParser.lhs_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER404=null;
        Token CHAR_LITERAL405=null;
        Token STRING_LITERAL406=null;
        Token LPAREN407=null;
        Token RPAREN408=null;
        Token PERIOD409=null;
        Token ID410=null;
        Token LPAREN411=null;
        Token RPAREN413=null;
        Token CARET415=null;
        Token LBRACE416=null;
        Token PLUS417=null;
        Token RBRACE419=null;
        Token AS420=null;
        Token PLUS421=null;
        EulangParser.assignExpr_return a1 = null;

        EulangParser.idExpr_return idExpr402 = null;

        EulangParser.tuple_return tuple403 = null;

        EulangParser.arglist_return arglist412 = null;

        EulangParser.arrayAccess_return arrayAccess414 = null;

        EulangParser.type_return type418 = null;

        EulangParser.type_return type422 = null;


        CommonTree NUMBER404_tree=null;
        CommonTree CHAR_LITERAL405_tree=null;
        CommonTree STRING_LITERAL406_tree=null;
        CommonTree LPAREN407_tree=null;
        CommonTree RPAREN408_tree=null;
        CommonTree PERIOD409_tree=null;
        CommonTree ID410_tree=null;
        CommonTree LPAREN411_tree=null;
        CommonTree RPAREN413_tree=null;
        CommonTree CARET415_tree=null;
        CommonTree LBRACE416_tree=null;
        CommonTree PLUS417_tree=null;
        CommonTree RBRACE419_tree=null;
        CommonTree AS420_tree=null;
        CommonTree PLUS421_tree=null;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:552:5: ( ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:553:3: ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1) ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )* ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:553:3: ( idExpr -> idExpr | ( tuple )=> tuple -> tuple | NUMBER -> ^( LIT NUMBER ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | LPAREN a1= assignExpr RPAREN -> $a1)
            int alt119=6;
            switch ( input.LA(1) ) {
            case ID:
            case COLON:
            case COLONS:
                {
                alt119=1;
                }
                break;
            case LPAREN:
                {
                int LA119_3 = input.LA(2);

                if ( (synpred23_Eulang()) ) {
                    alt119=2;
                }
                else if ( (true) ) {
                    alt119=6;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 119, 3, input);

                    throw nvae;
                }
                }
                break;
            case NUMBER:
                {
                alt119=3;
                }
                break;
            case CHAR_LITERAL:
                {
                alt119=4;
                }
                break;
            case STRING_LITERAL:
                {
                alt119=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 119, 0, input);

                throw nvae;
            }

            switch (alt119) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:554:8: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_lhs6564);
                    idExpr402=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr402.getTree());


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
                    // 554:40: -> idExpr
                    {
                        adaptor.addChild(root_0, stream_idExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_lhs6611);
                    tuple403=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple403.getTree());


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
                    // 555:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:556:9: NUMBER
                    {
                    NUMBER404=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_lhs6650); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER404);



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
                    // 556:41: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:556:44: ^( LIT NUMBER )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:557:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL405=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_lhs6693); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL405);



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
                    // 557:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:557:42: ^( LIT CHAR_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:558:9: STRING_LITERAL
                    {
                    STRING_LITERAL406=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_lhs6728); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL406);



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
                    // 558:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:558:42: ^( LIT STRING_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:559:9: LPAREN a1= assignExpr RPAREN
                    {
                    LPAREN407=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_lhs6761); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN407);

                    pushFollow(FOLLOW_assignExpr_in_lhs6765);
                    a1=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(a1.getTree());
                    RPAREN408=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_lhs6767); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN408);



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
                    // 559:51: -> $a1
                    {
                        adaptor.addChild(root_0, stream_a1.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:561:5: ( ( PERIOD ID -> ^( FIELDREF $lhs ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) ) | ( CARET -> ^( DEREF $lhs) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) ) )*
            loop121:
            do {
                int alt121=6;
                int LA121_0 = input.LA(1);

                if ( (LA121_0==PERIOD) ) {
                    alt121=1;
                }
                else if ( (LA121_0==LPAREN) ) {
                    alt121=2;
                }
                else if ( (LA121_0==LBRACKET) && (synpred24_Eulang())) {
                    alt121=3;
                }
                else if ( (LA121_0==CARET) ) {
                    alt121=4;
                }
                else if ( (LA121_0==LBRACE) ) {
                    alt121=5;
                }


                switch (alt121) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:7: ( PERIOD ID -> ^( FIELDREF $lhs ID ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:7: ( PERIOD ID -> ^( FIELDREF $lhs ID ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:9: PERIOD ID
            	    {
            	    PERIOD409=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_lhs6810); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD409);

            	    ID410=(Token)match(input,ID,FOLLOW_ID_in_lhs6812); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID410);



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
            	    // 562:20: -> ^( FIELDREF $lhs ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:562:23: ^( FIELDREF $lhs ID )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:7: ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:7: ( LPAREN arglist RPAREN -> ^( CALL $lhs arglist ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:10: LPAREN arglist RPAREN
            	    {
            	    LPAREN411=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_lhs6837); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN411);

            	    pushFollow(FOLLOW_arglist_in_lhs6839);
            	    arglist412=arglist();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arglist.add(arglist412.getTree());
            	    RPAREN413=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_lhs6841); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN413);



            	    // AST REWRITE
            	    // elements: arglist, lhs
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 563:34: -> ^( CALL $lhs arglist )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:563:37: ^( CALL $lhs arglist )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $lhs arrayAccess ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:9: ( LBRACKET )=> arrayAccess
            	    {
            	    pushFollow(FOLLOW_arrayAccess_in_lhs6874);
            	    arrayAccess414=arrayAccess();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arrayAccess.add(arrayAccess414.getTree());


            	    // AST REWRITE
            	    // elements: lhs, arrayAccess
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 564:39: -> ^( INDEX $lhs arrayAccess )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:42: ^( INDEX $lhs arrayAccess )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:7: ( CARET -> ^( DEREF $lhs) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:7: ( CARET -> ^( DEREF $lhs) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:9: CARET
            	    {
            	    CARET415=(Token)match(input,CARET,FOLLOW_CARET_in_lhs6899); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET415);



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
            	    // 565:15: -> ^( DEREF $lhs)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:565:18: ^( DEREF $lhs)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $lhs) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:9: LBRACE ( PLUS )? type RBRACE
            	    {
            	    LBRACE416=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_lhs6920); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE416);

            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:16: ( PLUS )?
            	    int alt120=2;
            	    int LA120_0 = input.LA(1);

            	    if ( (LA120_0==PLUS) ) {
            	        alt120=1;
            	    }
            	    switch (alt120) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:16: PLUS
            	            {
            	            PLUS417=(Token)match(input,PLUS,FOLLOW_PLUS_in_lhs6922); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_PLUS.add(PLUS417);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_type_in_lhs6925);
            	    type418=type();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_type.add(type418.getTree());
            	    RBRACE419=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_lhs6927); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE419);



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
            	    // 566:34: -> ^( CAST ( PLUS )? type $lhs)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:37: ^( CAST ( PLUS )? type $lhs)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:566:44: ( PLUS )?
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
            	    break loop121;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:569:5: ( AS ( PLUS )? type -> ^( CAST ( PLUS )? type $lhs) )?
            int alt123=2;
            int LA123_0 = input.LA(1);

            if ( (LA123_0==AS) ) {
                alt123=1;
            }
            switch (alt123) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:7: AS ( PLUS )? type
                    {
                    AS420=(Token)match(input,AS,FOLLOW_AS_in_lhs6968); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AS.add(AS420);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:10: ( PLUS )?
                    int alt122=2;
                    int LA122_0 = input.LA(1);

                    if ( (LA122_0==PLUS) ) {
                        alt122=1;
                    }
                    switch (alt122) {
                        case 1 :
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:10: PLUS
                            {
                            PLUS421=(Token)match(input,PLUS,FOLLOW_PLUS_in_lhs6970); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_PLUS.add(PLUS421);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_type_in_lhs6973);
                    type422=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type422.getTree());


                    // AST REWRITE
                    // elements: lhs, type, PLUS
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 570:21: -> ^( CAST ( PLUS )? type $lhs)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:24: ^( CAST ( PLUS )? type $lhs)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:570:31: ( PLUS )?
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:574:1: atom : ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )? ;
    public final EulangParser.atom_return atom() throws RecognitionException {
        EulangParser.atom_return retval = new EulangParser.atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NUMBER423=null;
        Token FALSE424=null;
        Token TRUE425=null;
        Token CHAR_LITERAL426=null;
        Token STRING_LITERAL427=null;
        Token NIL428=null;
        Token LPAREN431=null;
        Token RPAREN432=null;
        Token LPAREN433=null;
        Token RPAREN434=null;
        Token PERIOD436=null;
        Token ID437=null;
        Token LPAREN438=null;
        Token RPAREN440=null;
        Token CARET442=null;
        Token LBRACE443=null;
        Token PLUS444=null;
        Token RBRACE446=null;
        Token AS447=null;
        EulangParser.varDecl_return a0 = null;

        EulangParser.assignExpr_return a1 = null;

        EulangParser.idExpr_return idExpr429 = null;

        EulangParser.tuple_return tuple430 = null;

        EulangParser.code_return code435 = null;

        EulangParser.arglist_return arglist439 = null;

        EulangParser.arrayAccess_return arrayAccess441 = null;

        EulangParser.type_return type445 = null;

        EulangParser.type_return type448 = null;


        CommonTree NUMBER423_tree=null;
        CommonTree FALSE424_tree=null;
        CommonTree TRUE425_tree=null;
        CommonTree CHAR_LITERAL426_tree=null;
        CommonTree STRING_LITERAL427_tree=null;
        CommonTree NIL428_tree=null;
        CommonTree LPAREN431_tree=null;
        CommonTree RPAREN432_tree=null;
        CommonTree LPAREN433_tree=null;
        CommonTree RPAREN434_tree=null;
        CommonTree PERIOD436_tree=null;
        CommonTree ID437_tree=null;
        CommonTree LPAREN438_tree=null;
        CommonTree RPAREN440_tree=null;
        CommonTree CARET442_tree=null;
        CommonTree LBRACE443_tree=null;
        CommonTree PLUS444_tree=null;
        CommonTree RBRACE446_tree=null;
        CommonTree AS447_tree=null;
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
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:574:6: ( ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:575:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code ) ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )* ( AS type -> ^( CAST type $atom) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:575:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code )
            int alt124=11;
            alt124 = dfa124.predict(input);
            switch (alt124) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:7: NUMBER
                    {
                    NUMBER423=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_atom7022); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NUMBER.add(NUMBER423);



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
                    // 576:39: -> ^( LIT NUMBER )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:576:42: ^( LIT NUMBER )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:577:9: FALSE
                    {
                    FALSE424=(Token)match(input,FALSE,FOLLOW_FALSE_in_atom7065); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FALSE.add(FALSE424);



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
                    // 577:39: -> ^( LIT FALSE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:577:42: ^( LIT FALSE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:578:9: TRUE
                    {
                    TRUE425=(Token)match(input,TRUE,FOLLOW_TRUE_in_atom7107); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_TRUE.add(TRUE425);



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
                    // 578:39: -> ^( LIT TRUE )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:578:42: ^( LIT TRUE )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:579:9: CHAR_LITERAL
                    {
                    CHAR_LITERAL426=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_atom7150); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_CHAR_LITERAL.add(CHAR_LITERAL426);



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
                    // 579:39: -> ^( LIT CHAR_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:579:42: ^( LIT CHAR_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:9: STRING_LITERAL
                    {
                    STRING_LITERAL427=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_atom7185); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL427);



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
                    // 580:39: -> ^( LIT STRING_LITERAL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:580:42: ^( LIT STRING_LITERAL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:9: NIL
                    {
                    NIL428=(Token)match(input,NIL,FOLLOW_NIL_in_atom7218); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_NIL.add(NIL428);



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
                    // 581:38: -> ^( LIT NIL )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:581:41: ^( LIT NIL )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:582:9: idExpr
                    {
                    pushFollow(FOLLOW_idExpr_in_atom7261);
                    idExpr429=idExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_idExpr.add(idExpr429.getTree());


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
                    // 582:41: -> idExpr
                    {
                        adaptor.addChild(root_0, stream_idExpr.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 8 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:9: ( tuple )=> tuple
                    {
                    pushFollow(FOLLOW_tuple_in_atom7308);
                    tuple430=tuple();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_tuple.add(tuple430.getTree());


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
                    // 583:53: -> tuple
                    {
                        adaptor.addChild(root_0, stream_tuple.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 9 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:9: ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN
                    {
                    LPAREN431=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7357); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN431);

                    pushFollow(FOLLOW_varDecl_in_atom7361);
                    a0=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(a0.getTree());
                    RPAREN432=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7363); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN432);



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
                    // 584:70: -> $a0
                    {
                        adaptor.addChild(root_0, stream_a0.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 10 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:585:9: LPAREN a1= assignExpr RPAREN
                    {
                    LPAREN433=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7392); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN433);

                    pushFollow(FOLLOW_assignExpr_in_atom7396);
                    a1=assignExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_assignExpr.add(a1.getTree());
                    RPAREN434=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7398); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN434);



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
                    // 585:51: -> $a1
                    {
                        adaptor.addChild(root_0, stream_a1.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 11 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:586:9: code
                    {
                    pushFollow(FOLLOW_code_in_atom7427);
                    code435=code();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_code.add(code435.getTree());


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
                    // 586:40: -> code
                    {
                        adaptor.addChild(root_0, stream_code.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:590:5: ( ( PERIOD ID -> ^( FIELDREF $atom ID ) ) | ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) ) | ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) ) | ( CARET -> ^( DEREF $atom) ) | ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) ) )*
            loop126:
            do {
                int alt126=6;
                int LA126_0 = input.LA(1);

                if ( (LA126_0==PERIOD) ) {
                    alt126=1;
                }
                else if ( (LA126_0==LPAREN) ) {
                    alt126=2;
                }
                else if ( (LA126_0==LBRACKET) && (synpred27_Eulang())) {
                    alt126=3;
                }
                else if ( (LA126_0==CARET) ) {
                    alt126=4;
                }
                else if ( (LA126_0==LBRACE) ) {
                    alt126=5;
                }


                switch (alt126) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:7: ( PERIOD ID -> ^( FIELDREF $atom ID ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:7: ( PERIOD ID -> ^( FIELDREF $atom ID ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:9: PERIOD ID
            	    {
            	    PERIOD436=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_atom7486); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD436);

            	    ID437=(Token)match(input,ID,FOLLOW_ID_in_atom7488); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID437);



            	    // AST REWRITE
            	    // elements: atom, ID
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 591:20: -> ^( FIELDREF $atom ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:591:23: ^( FIELDREF $atom ID )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:7: ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:7: ( LPAREN arglist RPAREN -> ^( CALL $atom arglist ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:10: LPAREN arglist RPAREN
            	    {
            	    LPAREN438=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_atom7513); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN438);

            	    pushFollow(FOLLOW_arglist_in_atom7515);
            	    arglist439=arglist();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arglist.add(arglist439.getTree());
            	    RPAREN440=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_atom7517); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN440);



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
            	    // 592:34: -> ^( CALL $atom arglist )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:592:37: ^( CALL $atom arglist )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:7: ( ( LBRACKET )=> arrayAccess -> ^( INDEX $atom arrayAccess ) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:9: ( LBRACKET )=> arrayAccess
            	    {
            	    pushFollow(FOLLOW_arrayAccess_in_atom7550);
            	    arrayAccess441=arrayAccess();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_arrayAccess.add(arrayAccess441.getTree());


            	    // AST REWRITE
            	    // elements: atom, arrayAccess
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 593:39: -> ^( INDEX $atom arrayAccess )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:42: ^( INDEX $atom arrayAccess )
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:7: ( CARET -> ^( DEREF $atom) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:7: ( CARET -> ^( DEREF $atom) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:9: CARET
            	    {
            	    CARET442=(Token)match(input,CARET,FOLLOW_CARET_in_atom7575); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_CARET.add(CARET442);



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
            	    // 594:15: -> ^( DEREF $atom)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:594:18: ^( DEREF $atom)
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
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) )
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:7: ( LBRACE ( PLUS )? type RBRACE -> ^( CAST ( PLUS )? type $atom) )
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:9: LBRACE ( PLUS )? type RBRACE
            	    {
            	    LBRACE443=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_atom7596); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE443);

            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:16: ( PLUS )?
            	    int alt125=2;
            	    int LA125_0 = input.LA(1);

            	    if ( (LA125_0==PLUS) ) {
            	        alt125=1;
            	    }
            	    switch (alt125) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:16: PLUS
            	            {
            	            PLUS444=(Token)match(input,PLUS,FOLLOW_PLUS_in_atom7598); if (state.failed) return retval; 
            	            if ( state.backtracking==0 ) stream_PLUS.add(PLUS444);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_type_in_atom7601);
            	    type445=type();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_type.add(type445.getTree());
            	    RBRACE446=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_atom7603); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE446);



            	    // AST REWRITE
            	    // elements: type, atom, PLUS
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 595:34: -> ^( CAST ( PLUS )? type $atom)
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:37: ^( CAST ( PLUS )? type $atom)
            	        {
            	        CommonTree root_1 = (CommonTree)adaptor.nil();
            	        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CAST, "CAST"), root_1);

            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:595:44: ( PLUS )?
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
            	    break loop126;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:598:5: ( AS type -> ^( CAST type $atom) )?
            int alt127=2;
            int LA127_0 = input.LA(1);

            if ( (LA127_0==AS) ) {
                alt127=1;
            }
            switch (alt127) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:7: AS type
                    {
                    AS447=(Token)match(input,AS,FOLLOW_AS_in_atom7644); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_AS.add(AS447);

                    pushFollow(FOLLOW_type_in_atom7646);
                    type448=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_type.add(type448.getTree());


                    // AST REWRITE
                    // elements: type, atom
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 599:15: -> ^( CAST type $atom)
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:599:18: ^( CAST type $atom)
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:1: arrayAccess : LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET -> ( assignExpr )+ ;
    public final EulangParser.arrayAccess_return arrayAccess() throws RecognitionException {
        EulangParser.arrayAccess_return retval = new EulangParser.arrayAccess_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LBRACKET449=null;
        Token COMMA451=null;
        Token RBRACKET453=null;
        EulangParser.assignExpr_return assignExpr450 = null;

        EulangParser.assignExpr_return assignExpr452 = null;


        CommonTree LBRACKET449_tree=null;
        CommonTree COMMA451_tree=null;
        CommonTree RBRACKET453_tree=null;
        RewriteRuleTokenStream stream_LBRACKET=new RewriteRuleTokenStream(adaptor,"token LBRACKET");
        RewriteRuleTokenStream stream_RBRACKET=new RewriteRuleTokenStream(adaptor,"token RBRACKET");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignExpr=new RewriteRuleSubtreeStream(adaptor,"rule assignExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:13: ( LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET -> ( assignExpr )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:15: LBRACKET assignExpr ( COMMA assignExpr )* RBRACKET
            {
            LBRACKET449=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayAccess7680); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACKET.add(LBRACKET449);

            pushFollow(FOLLOW_assignExpr_in_arrayAccess7682);
            assignExpr450=assignExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr450.getTree());
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:35: ( COMMA assignExpr )*
            loop128:
            do {
                int alt128=2;
                int LA128_0 = input.LA(1);

                if ( (LA128_0==COMMA) ) {
                    alt128=1;
                }


                switch (alt128) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:603:36: COMMA assignExpr
            	    {
            	    COMMA451=(Token)match(input,COMMA,FOLLOW_COMMA_in_arrayAccess7685); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA451);

            	    pushFollow(FOLLOW_assignExpr_in_arrayAccess7687);
            	    assignExpr452=assignExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_assignExpr.add(assignExpr452.getTree());

            	    }
            	    break;

            	default :
            	    break loop128;
                }
            } while (true);

            RBRACKET453=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayAccess7691); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACKET.add(RBRACKET453);



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
            // 603:65: -> ( assignExpr )+
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:1: idExpr : ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )? ;
    public final EulangParser.idExpr_return idExpr() throws RecognitionException {
        EulangParser.idExpr_return retval = new EulangParser.idExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token PERIOD455=null;
        Token ID456=null;
        EulangParser.idOrScopeRef_return idOrScopeRef454 = null;

        EulangParser.instantiation_return instantiation457 = null;


        CommonTree PERIOD455_tree=null;
        CommonTree ID456_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_idOrScopeRef=new RewriteRuleSubtreeStream(adaptor,"rule idOrScopeRef");
        RewriteRuleSubtreeStream stream_instantiation=new RewriteRuleSubtreeStream(adaptor,"rule instantiation");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:605:8: ( ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:5: ( idOrScopeRef -> idOrScopeRef ) ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )* ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:5: ( idOrScopeRef -> idOrScopeRef )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:606:7: idOrScopeRef
            {
            pushFollow(FOLLOW_idOrScopeRef_in_idExpr7713);
            idOrScopeRef454=idOrScopeRef();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_idOrScopeRef.add(idOrScopeRef454.getTree());


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
            // 606:20: -> idOrScopeRef
            {
                adaptor.addChild(root_0, stream_idOrScopeRef.nextTree());

            }

            retval.tree = root_0;}
            }

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:7: ( PERIOD ID -> ^( FIELDREF $idExpr ID ) )*
            loop129:
            do {
                int alt129=2;
                int LA129_0 = input.LA(1);

                if ( (LA129_0==PERIOD) ) {
                    int LA129_2 = input.LA(2);

                    if ( (LA129_2==ID) ) {
                        alt129=1;
                    }


                }


                switch (alt129) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:9: PERIOD ID
            	    {
            	    PERIOD455=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_idExpr7729); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD455);

            	    ID456=(Token)match(input,ID,FOLLOW_ID_in_idExpr7731); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID456);



            	    // AST REWRITE
            	    // elements: idExpr, ID
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    if ( state.backtracking==0 ) {
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (CommonTree)adaptor.nil();
            	    // 607:20: -> ^( FIELDREF $idExpr ID )
            	    {
            	        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:607:23: ^( FIELDREF $idExpr ID )
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
            	    break loop129;
                }
            } while (true);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:5: ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?
            int alt130=2;
            alt130 = dfa130.predict(input);
            switch (alt130) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:7: ( instantiation )=> instantiation
                    {
                    pushFollow(FOLLOW_instantiation_in_idExpr7761);
                    instantiation457=instantiation();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_instantiation.add(instantiation457.getTree());


                    // AST REWRITE
                    // elements: instantiation, idExpr
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 608:41: -> ^( INSTANCE $idExpr instantiation )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:44: ^( INSTANCE $idExpr instantiation )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:610:1: namespaceRef : ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) );
    public final EulangParser.namespaceRef_return namespaceRef() throws RecognitionException {
        EulangParser.namespaceRef_return retval = new EulangParser.namespaceRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID458=null;
        Token PERIOD459=null;
        Token ID460=null;
        Token ID461=null;
        Token PERIOD462=null;
        Token ID463=null;
        EulangParser.colons_return c = null;


        CommonTree ID458_tree=null;
        CommonTree PERIOD459_tree=null;
        CommonTree ID460_tree=null;
        CommonTree ID461_tree=null;
        CommonTree PERIOD462_tree=null;
        CommonTree ID463_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_PERIOD=new RewriteRuleTokenStream(adaptor,"token PERIOD");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:610:14: ( ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) | c= colons ID ( PERIOD ID )* -> ^( IDREF ( ID )+ ) )
            int alt133=2;
            int LA133_0 = input.LA(1);

            if ( (LA133_0==ID) ) {
                alt133=1;
            }
            else if ( (LA133_0==COLON||LA133_0==COLONS) ) {
                alt133=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 133, 0, input);

                throw nvae;
            }
            switch (alt133) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:5: ID ( PERIOD ID )*
                    {
                    ID458=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7792); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID458);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:8: ( PERIOD ID )*
                    loop131:
                    do {
                        int alt131=2;
                        int LA131_0 = input.LA(1);

                        if ( (LA131_0==PERIOD) ) {
                            alt131=1;
                        }


                        switch (alt131) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:9: PERIOD ID
                    	    {
                    	    PERIOD459=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_namespaceRef7795); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD459);

                    	    ID460=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7797); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID460);


                    	    }
                    	    break;

                    	default :
                    	    break loop131;
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
                    // 611:22: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:611:25: ^( IDREF ( ID )+ )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:7: c= colons ID ( PERIOD ID )*
                    {
                    pushFollow(FOLLOW_colons_in_namespaceRef7821);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID461=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7823); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID461);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:19: ( PERIOD ID )*
                    loop132:
                    do {
                        int alt132=2;
                        int LA132_0 = input.LA(1);

                        if ( (LA132_0==PERIOD) ) {
                            alt132=1;
                        }


                        switch (alt132) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:20: PERIOD ID
                    	    {
                    	    PERIOD462=(Token)match(input,PERIOD,FOLLOW_PERIOD_in_namespaceRef7826); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_PERIOD.add(PERIOD462);

                    	    ID463=(Token)match(input,ID,FOLLOW_ID_in_namespaceRef7828); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID463);


                    	    }
                    	    break;

                    	default :
                    	    break loop132;
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
                    // 612:33: -> ^( IDREF ( ID )+ )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:612:36: ^( IDREF ( ID )+ )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:1: instantiation : LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER -> ^( LIST ( instanceExpr )* ) ;
    public final EulangParser.instantiation_return instantiation() throws RecognitionException {
        EulangParser.instantiation_return retval = new EulangParser.instantiation_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token LESS464=null;
        Token COMMA466=null;
        Token GREATER468=null;
        EulangParser.instanceExpr_return instanceExpr465 = null;

        EulangParser.instanceExpr_return instanceExpr467 = null;


        CommonTree LESS464_tree=null;
        CommonTree COMMA466_tree=null;
        CommonTree GREATER468_tree=null;
        RewriteRuleTokenStream stream_GREATER=new RewriteRuleTokenStream(adaptor,"token GREATER");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LESS=new RewriteRuleTokenStream(adaptor,"token LESS");
        RewriteRuleSubtreeStream stream_instanceExpr=new RewriteRuleSubtreeStream(adaptor,"rule instanceExpr");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:15: ( LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER -> ^( LIST ( instanceExpr )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:17: LESS ( instanceExpr ( COMMA instanceExpr )* )? GREATER
            {
            LESS464=(Token)match(input,LESS,FOLLOW_LESS_in_instantiation7857); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LESS.add(LESS464);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:22: ( instanceExpr ( COMMA instanceExpr )* )?
            int alt135=2;
            int LA135_0 = input.LA(1);

            if ( (LA135_0==CODE||LA135_0==ID||LA135_0==COLON||LA135_0==LPAREN||LA135_0==NIL||(LA135_0>=NUMBER && LA135_0<=STRING_LITERAL)||(LA135_0>=FALSE && LA135_0<=DATA)) ) {
                alt135=1;
            }
            switch (alt135) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:23: instanceExpr ( COMMA instanceExpr )*
                    {
                    pushFollow(FOLLOW_instanceExpr_in_instantiation7860);
                    instanceExpr465=instanceExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_instanceExpr.add(instanceExpr465.getTree());
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:36: ( COMMA instanceExpr )*
                    loop134:
                    do {
                        int alt134=2;
                        int LA134_0 = input.LA(1);

                        if ( (LA134_0==COMMA) ) {
                            alt134=1;
                        }


                        switch (alt134) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:37: COMMA instanceExpr
                    	    {
                    	    COMMA466=(Token)match(input,COMMA,FOLLOW_COMMA_in_instantiation7863); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA466);

                    	    pushFollow(FOLLOW_instanceExpr_in_instantiation7865);
                    	    instanceExpr467=instanceExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_instanceExpr.add(instanceExpr467.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop134;
                        }
                    } while (true);


                    }
                    break;

            }

            GREATER468=(Token)match(input,GREATER,FOLLOW_GREATER_in_instantiation7871); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_GREATER.add(GREATER468);



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
            // 615:70: -> ^( LIST ( instanceExpr )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:73: ^( LIST ( instanceExpr )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LIST, "LIST"), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:615:80: ( instanceExpr )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:1: instanceExpr options {backtrack=true; } : ( type | atom );
    public final EulangParser.instanceExpr_return instanceExpr() throws RecognitionException {
        EulangParser.instanceExpr_return retval = new EulangParser.instanceExpr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        EulangParser.type_return type469 = null;

        EulangParser.atom_return atom470 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:41: ( type | atom )
            int alt136=2;
            alt136 = dfa136.predict(input);
            switch (alt136) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:43: type
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_type_in_instanceExpr7903);
                    type469=type();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, type469.getTree());

                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:50: atom
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_atom_in_instanceExpr7907);
                    atom470=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, atom470.getTree());

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:1: idOrScopeRef : ( ID -> ^( IDREF ID ) | c= colons ID -> ^( IDREF ID ) );
    public final EulangParser.idOrScopeRef_return idOrScopeRef() throws RecognitionException {
        EulangParser.idOrScopeRef_return retval = new EulangParser.idOrScopeRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID471=null;
        Token ID472=null;
        EulangParser.colons_return c = null;


        CommonTree ID471_tree=null;
        CommonTree ID472_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleSubtreeStream stream_colons=new RewriteRuleSubtreeStream(adaptor,"rule colons");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:14: ( ID -> ^( IDREF ID ) | c= colons ID -> ^( IDREF ID ) )
            int alt137=2;
            int LA137_0 = input.LA(1);

            if ( (LA137_0==ID) ) {
                alt137=1;
            }
            else if ( (LA137_0==COLON||LA137_0==COLONS) ) {
                alt137=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 137, 0, input);

                throw nvae;
            }
            switch (alt137) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:16: ID
                    {
                    ID471=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef7915); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID471);



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
                    // 619:20: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:619:23: ^( IDREF ID )
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
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:620:9: c= colons ID
                    {
                    pushFollow(FOLLOW_colons_in_idOrScopeRef7938);
                    c=colons();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_colons.add(c.getTree());
                    ID472=(Token)match(input,ID,FOLLOW_ID_in_idOrScopeRef7940); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID472);



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
                    // 620:21: -> ^( IDREF ID )
                    {
                        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:620:24: ^( IDREF ID )
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:1: colons : ( COLON | COLONS )+ ;
    public final EulangParser.colons_return colons() throws RecognitionException {
        EulangParser.colons_return retval = new EulangParser.colons_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set473=null;

        CommonTree set473_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:8: ( ( COLON | COLONS )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:10: ( COLON | COLONS )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:623:10: ( COLON | COLONS )+
            int cnt138=0;
            loop138:
            do {
                int alt138=2;
                int LA138_0 = input.LA(1);

                if ( (LA138_0==COLON||LA138_0==COLONS) ) {
                    alt138=1;
                }


                switch (alt138) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:
            	    {
            	    set473=(Token)input.LT(1);
            	    if ( input.LA(1)==COLON||input.LA(1)==COLONS ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set473));
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
            	    if ( cnt138 >= 1 ) break loop138;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(138, input);
                        throw eee;
                }
                cnt138++;
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:1: data : DATA LBRACE ( fieldDecl )* RBRACE -> ^( DATA ( fieldDecl )* ) ;
    public final EulangParser.data_return data() throws RecognitionException {
        EulangParser.data_return retval = new EulangParser.data_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token DATA474=null;
        Token LBRACE475=null;
        Token RBRACE477=null;
        EulangParser.fieldDecl_return fieldDecl476 = null;


        CommonTree DATA474_tree=null;
        CommonTree LBRACE475_tree=null;
        CommonTree RBRACE477_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_DATA=new RewriteRuleTokenStream(adaptor,"token DATA");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_fieldDecl=new RewriteRuleSubtreeStream(adaptor,"rule fieldDecl");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:6: ( DATA LBRACE ( fieldDecl )* RBRACE -> ^( DATA ( fieldDecl )* ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:8: DATA LBRACE ( fieldDecl )* RBRACE
            {
            DATA474=(Token)match(input,DATA,FOLLOW_DATA_in_data7983); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_DATA.add(DATA474);

            LBRACE475=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_data7985); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_LBRACE.add(LBRACE475);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:20: ( fieldDecl )*
            loop139:
            do {
                int alt139=2;
                int LA139_0 = input.LA(1);

                if ( ((LA139_0>=FORWARD && LA139_0<=ID)||LA139_0==LPAREN) ) {
                    alt139=1;
                }


                switch (alt139) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:20: fieldDecl
            	    {
            	    pushFollow(FOLLOW_fieldDecl_in_data7987);
            	    fieldDecl476=fieldDecl();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_fieldDecl.add(fieldDecl476.getTree());

            	    }
            	    break;

            	default :
            	    break loop139;
                }
            } while (true);

            RBRACE477=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_data7990); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE477);



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
            // 625:39: -> ^( DATA ( fieldDecl )* )
            {
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:42: ^( DATA ( fieldDecl )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_DATA.nextNode(), root_1);

                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:625:49: ( fieldDecl )*
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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:1: fieldDecl : ( varDecl SEMI -> varDecl | defineStmt | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ );
    public final EulangParser.fieldDecl_return fieldDecl() throws RecognitionException {
        EulangParser.fieldDecl_return retval = new EulangParser.fieldDecl_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token SEMI479=null;
        Token FORWARD481=null;
        Token ID482=null;
        Token COMMA483=null;
        Token ID484=null;
        Token SEMI485=null;
        EulangParser.varDecl_return varDecl478 = null;

        EulangParser.defineStmt_return defineStmt480 = null;


        CommonTree SEMI479_tree=null;
        CommonTree FORWARD481_tree=null;
        CommonTree ID482_tree=null;
        CommonTree COMMA483_tree=null;
        CommonTree ID484_tree=null;
        CommonTree SEMI485_tree=null;
        RewriteRuleTokenStream stream_FORWARD=new RewriteRuleTokenStream(adaptor,"token FORWARD");
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_varDecl=new RewriteRuleSubtreeStream(adaptor,"rule varDecl");
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:11: ( varDecl SEMI -> varDecl | defineStmt | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ )
            int alt141=3;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA141_1 = input.LA(2);

                if ( (LA141_1==EQUALS||LA141_1==EQUALS_COLON) ) {
                    alt141=2;
                }
                else if ( ((LA141_1>=COMMA && LA141_1<=COLON)||LA141_1==ATTR) ) {
                    alt141=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 141, 1, input);

                    throw nvae;
                }
                }
                break;
            case LPAREN:
                {
                alt141=1;
                }
                break;
            case FORWARD:
                {
                alt141=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 141, 0, input);

                throw nvae;
            }

            switch (alt141) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:627:13: varDecl SEMI
                    {
                    pushFollow(FOLLOW_varDecl_in_fieldDecl8009);
                    varDecl478=varDecl();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_varDecl.add(varDecl478.getTree());
                    SEMI479=(Token)match(input,SEMI,FOLLOW_SEMI_in_fieldDecl8011); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI479);



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
                    // 627:26: -> varDecl
                    {
                        adaptor.addChild(root_0, stream_varDecl.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:628:7: defineStmt
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_defineStmt_in_fieldDecl8024);
                    defineStmt480=defineStmt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, defineStmt480.getTree());

                    }
                    break;
                case 3 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:7: FORWARD ID ( COMMA ID )* SEMI
                    {
                    FORWARD481=(Token)match(input,FORWARD,FOLLOW_FORWARD_in_fieldDecl8032); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_FORWARD.add(FORWARD481);

                    ID482=(Token)match(input,ID,FOLLOW_ID_in_fieldDecl8034); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_ID.add(ID482);

                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:18: ( COMMA ID )*
                    loop140:
                    do {
                        int alt140=2;
                        int LA140_0 = input.LA(1);

                        if ( (LA140_0==COMMA) ) {
                            alt140=1;
                        }


                        switch (alt140) {
                    	case 1 :
                    	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:19: COMMA ID
                    	    {
                    	    COMMA483=(Token)match(input,COMMA,FOLLOW_COMMA_in_fieldDecl8037); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA483);

                    	    ID484=(Token)match(input,ID,FOLLOW_ID_in_fieldDecl8039); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_ID.add(ID484);


                    	    }
                    	    break;

                    	default :
                    	    break loop140;
                        }
                    } while (true);

                    SEMI485=(Token)match(input,SEMI,FOLLOW_SEMI_in_fieldDecl8043); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI485);



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
                    // 629:35: -> ( ^( FORWARD ID ) )+
                    {
                        if ( !(stream_ID.hasNext()||stream_FORWARD.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_ID.hasNext()||stream_FORWARD.hasNext() ) {
                            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:629:38: ^( FORWARD ID )
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

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
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

    public static class fieldIdRef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldIdRef"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:1: fieldIdRef : ID ( COMMA ID )* -> ( ^( ALLOC ID ) )+ ;
    public final EulangParser.fieldIdRef_return fieldIdRef() throws RecognitionException {
        EulangParser.fieldIdRef_return retval = new EulangParser.fieldIdRef_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID486=null;
        Token COMMA487=null;
        Token ID488=null;

        CommonTree ID486_tree=null;
        CommonTree COMMA487_tree=null;
        CommonTree ID488_tree=null;
        RewriteRuleTokenStream stream_ID=new RewriteRuleTokenStream(adaptor,"token ID");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:12: ( ID ( COMMA ID )* -> ( ^( ALLOC ID ) )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:14: ID ( COMMA ID )*
            {
            ID486=(Token)match(input,ID,FOLLOW_ID_in_fieldIdRef8065); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_ID.add(ID486);

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:17: ( COMMA ID )*
            loop142:
            do {
                int alt142=2;
                int LA142_0 = input.LA(1);

                if ( (LA142_0==COMMA) ) {
                    alt142=1;
                }


                switch (alt142) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:18: COMMA ID
            	    {
            	    COMMA487=(Token)match(input,COMMA,FOLLOW_COMMA_in_fieldIdRef8068); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA487);

            	    ID488=(Token)match(input,ID,FOLLOW_ID_in_fieldIdRef8070); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_ID.add(ID488);


            	    }
            	    break;

            	default :
            	    break loop142;
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
            // 632:29: -> ( ^( ALLOC ID ) )+
            {
                if ( !(stream_ID.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ID.hasNext() ) {
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:632:32: ^( ALLOC ID )
                    {
                    CommonTree root_1 = (CommonTree)adaptor.nil();
                    root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ALLOC, "ALLOC"), root_1);

                    adaptor.addChild(root_1, stream_ID.nextNode());

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
    // $ANTLR end "fieldIdRef"

    // $ANTLR start synpred1_Eulang
    public final void synpred1_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:7: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:144:8: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred1_Eulang646); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Eulang

    // $ANTLR start synpred2_Eulang
    public final void synpred2_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:14: ( ID EQUALS LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:174:15: ID EQUALS LBRACKET
        {
        match(input,ID,FOLLOW_ID_in_synpred2_Eulang1111); if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred2_Eulang1113); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred2_Eulang1115); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Eulang

    // $ANTLR start synpred3_Eulang
    public final void synpred3_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:7: ( ID EQUALS_COLON )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:175:8: ID EQUALS_COLON
        {
        match(input,ID,FOLLOW_ID_in_synpred3_Eulang1160); if (state.failed) return ;
        match(input,EQUALS_COLON,FOLLOW_EQUALS_COLON_in_synpred3_Eulang1162); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Eulang

    // $ANTLR start synpred4_Eulang
    public final void synpred4_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:7: ( ID EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:176:8: ID EQUALS
        {
        match(input,ID,FOLLOW_ID_in_synpred4_Eulang1197); if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred4_Eulang1199); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred4_Eulang

    // $ANTLR start synpred5_Eulang
    public final void synpred5_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:17: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:179:18: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred5_Eulang1237); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Eulang

    // $ANTLR start synpred7_Eulang
    public final void synpred7_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:5: ( argdefsWithTypes )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:236:5: argdefsWithTypes
        {
        pushFollow(FOLLOW_argdefsWithTypes_in_synpred7_Eulang1775);
        argdefsWithTypes();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Eulang

    // $ANTLR start synpred8_Eulang
    public final void synpred8_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:5: ( argdefWithType )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:237:5: argdefWithType
        {
        pushFollow(FOLLOW_argdefWithType_in_synpred8_Eulang1782);
        argdefWithType();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Eulang

    // $ANTLR start synpred9_Eulang
    public final void synpred9_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:9: ( ( arraySuff )+ )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:10: ( arraySuff )+
        {
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:10: ( arraySuff )+
        int cnt143=0;
        loop143:
        do {
            int alt143=2;
            int LA143_0 = input.LA(1);

            if ( (LA143_0==LBRACKET) ) {
                alt143=1;
            }


            switch (alt143) {
        	case 1 :
        	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:280:10: arraySuff
        	    {
        	    pushFollow(FOLLOW_arraySuff_in_synpred9_Eulang2250);
        	    arraySuff();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt143 >= 1 ) break loop143;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(143, input);
                    throw eee;
            }
            cnt143++;
        } while (true);


        }
    }
    // $ANTLR end synpred9_Eulang

    // $ANTLR start synpred10_Eulang
    public final void synpred10_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:7: ( varDecl )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:311:9: varDecl
        {
        pushFollow(FOLLOW_varDecl_in_synpred10_Eulang2643);
        varDecl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Eulang

    // $ANTLR start synpred11_Eulang
    public final void synpred11_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:9: ( assignStmt )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:312:10: assignStmt
        {
        pushFollow(FOLLOW_assignStmt_in_synpred11_Eulang2666);
        assignStmt();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred11_Eulang

    // $ANTLR start synpred12_Eulang
    public final void synpred12_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:314:9: ( LBRACE )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:314:11: LBRACE
        {
        match(input,LBRACE,FOLLOW_LBRACE_in_synpred12_Eulang2715); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred12_Eulang

    // $ANTLR start synpred13_Eulang
    public final void synpred13_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:14: ( lhs assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:342:15: lhs assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred13_Eulang3215);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_assignEqOp_in_synpred13_Eulang3217);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Eulang

    // $ANTLR start synpred14_Eulang
    public final void synpred14_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:7: ( lhs ( COMMA lhs )+ assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:8: lhs ( COMMA lhs )+ assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred14_Eulang3297);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:12: ( COMMA lhs )+
        int cnt144=0;
        loop144:
        do {
            int alt144=2;
            int LA144_0 = input.LA(1);

            if ( (LA144_0==COMMA) ) {
                alt144=1;
            }


            switch (alt144) {
        	case 1 :
        	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:345:13: COMMA lhs
        	    {
        	    match(input,COMMA,FOLLOW_COMMA_in_synpred14_Eulang3300); if (state.failed) return ;
        	    pushFollow(FOLLOW_lhs_in_synpred14_Eulang3302);
        	    lhs();

        	    state._fsp--;
        	    if (state.failed) return ;

        	    }
        	    break;

        	default :
        	    if ( cnt144 >= 1 ) break loop144;
        	    if (state.backtracking>0) {state.failed=true; return ;}
                    EarlyExitException eee =
                        new EarlyExitException(144, input);
                    throw eee;
            }
            cnt144++;
        } while (true);

        pushFollow(FOLLOW_assignEqOp_in_synpred14_Eulang3306);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Eulang

    // $ANTLR start synpred15_Eulang
    public final void synpred15_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:14: ( lhs assignEqOp )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:352:15: lhs assignEqOp
        {
        pushFollow(FOLLOW_lhs_in_synpred15_Eulang3407);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_assignEqOp_in_synpred15_Eulang3409);
        assignEqOp();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_Eulang

    // $ANTLR start synpred16_Eulang
    public final void synpred16_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:7: ( idTuple EQUALS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:353:8: idTuple EQUALS
        {
        pushFollow(FOLLOW_idTuple_in_synpred16_Eulang3446);
        idTuple();

        state._fsp--;
        if (state.failed) return ;
        match(input,EQUALS,FOLLOW_EQUALS_in_synpred16_Eulang3448); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred16_Eulang

    // $ANTLR start synpred17_Eulang
    public final void synpred17_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:7: ( rhsExpr )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:363:8: rhsExpr
        {
        pushFollow(FOLLOW_rhsExpr_in_synpred17_Eulang3661);
        rhsExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_Eulang

    // $ANTLR start synpred18_Eulang
    public final void synpred18_Eulang_fragment() throws RecognitionException {   
        EulangParser.rhsExpr_return i = null;


        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:7: ( LBRACKET i= rhsExpr RBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:365:8: LBRACKET i= rhsExpr RBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred18_Eulang3793); if (state.failed) return ;
        pushFollow(FOLLOW_rhsExpr_in_synpred18_Eulang3797);
        i=rhsExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred18_Eulang3799); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred18_Eulang

    // $ANTLR start synpred19_Eulang
    public final void synpred19_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:12: ( MINUS term )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:525:14: MINUS term
        {
        match(input,MINUS,FOLLOW_MINUS_in_synpred19_Eulang5886); if (state.failed) return ;
        pushFollow(FOLLOW_term_in_synpred19_Eulang5888);
        term();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_Eulang

    // $ANTLR start synpred20_Eulang
    public final void synpred20_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:11: ( STAR unary )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:530:13: STAR unary
        {
        match(input,STAR,FOLLOW_STAR_in_synpred20_Eulang5979); if (state.failed) return ;
        pushFollow(FOLLOW_unary_in_synpred20_Eulang5981);
        unary();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_Eulang

    // $ANTLR start synpred21_Eulang
    public final void synpred21_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:9: ( lhs PLUSPLUS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:542:11: lhs PLUSPLUS
        {
        pushFollow(FOLLOW_lhs_in_synpred21_Eulang6313);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_synpred21_Eulang6315); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred21_Eulang

    // $ANTLR start synpred22_Eulang
    public final void synpred22_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:9: ( lhs MINUSMINUS )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:543:11: lhs MINUSMINUS
        {
        pushFollow(FOLLOW_lhs_in_synpred22_Eulang6346);
        lhs();

        state._fsp--;
        if (state.failed) return ;
        match(input,MINUSMINUS,FOLLOW_MINUSMINUS_in_synpred22_Eulang6348); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred22_Eulang

    // $ANTLR start synpred23_Eulang
    public final void synpred23_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:555:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred23_Eulang6605);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred23_Eulang

    // $ANTLR start synpred24_Eulang
    public final void synpred24_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:9: ( LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:564:11: LBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred24_Eulang6868); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred24_Eulang

    // $ANTLR start synpred25_Eulang
    public final void synpred25_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:9: ( tuple )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:583:11: tuple
        {
        pushFollow(FOLLOW_tuple_in_synpred25_Eulang7302);
        tuple();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred25_Eulang

    // $ANTLR start synpred26_Eulang
    public final void synpred26_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:9: ( LPAREN varDecl )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:584:11: LPAREN varDecl
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred26_Eulang7349); if (state.failed) return ;
        pushFollow(FOLLOW_varDecl_in_synpred26_Eulang7351);
        varDecl();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred26_Eulang

    // $ANTLR start synpred27_Eulang
    public final void synpred27_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:9: ( LBRACKET )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:593:11: LBRACKET
        {
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred27_Eulang7544); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred27_Eulang

    // $ANTLR start synpred28_Eulang
    public final void synpred28_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:7: ( instantiation )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:608:8: instantiation
        {
        pushFollow(FOLLOW_instantiation_in_synpred28_Eulang7755);
        instantiation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred28_Eulang

    // $ANTLR start synpred29_Eulang
    public final void synpred29_Eulang_fragment() throws RecognitionException {   
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:43: ( type )
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/parser/Eulang.g:618:43: type
        {
        pushFollow(FOLLOW_type_in_synpred29_Eulang7903);
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


    protected DFA3 dfa3 = new DFA3(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA21 dfa21 = new DFA21(this);
    protected DFA22 dfa22 = new DFA22(this);
    protected DFA51 dfa51 = new DFA51(this);
    protected DFA59 dfa59 = new DFA59(this);
    protected DFA72 dfa72 = new DFA72(this);
    protected DFA79 dfa79 = new DFA79(this);
    protected DFA81 dfa81 = new DFA81(this);
    protected DFA85 dfa85 = new DFA85(this);
    protected DFA118 dfa118 = new DFA118(this);
    protected DFA124 dfa124 = new DFA124(this);
    protected DFA130 dfa130 = new DFA130(this);
    protected DFA136 dfa136 = new DFA136(this);
    static final String DFA3_eotS =
        "\17\uffff";
    static final String DFA3_eofS =
        "\17\uffff";
    static final String DFA3_minS =
        "\1\11\1\44\1\uffff\1\11\1\uffff\1\44\1\105\1\11\2\44\1\105\1\11"+
        "\3\44";
    static final String DFA3_maxS =
        "\1\u0098\1\u0095\1\uffff\1\u0098\1\uffff\1\u0095\2\u0098\2\u0095"+
        "\2\u0098\3\u0095";
    static final String DFA3_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\12\uffff";
    static final String DFA3_specialS =
        "\17\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\2\63\uffff\2\2\5\uffff\1\2\1\1\2\uffff\1\2\6\uffff\1\2\4"+
            "\uffff\1\3\2\uffff\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2"+
            "\5\uffff\1\2\4\uffff\5\2\1\uffff\3\2",
            "\2\2\35\uffff\1\2\2\uffff\3\4\4\2\1\uffff\2\2\4\uffff\1\2\3"+
            "\uffff\2\2\17\uffff\1\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31"+
            "\2\3\uffff\1\2",
            "",
            "\1\2\63\uffff\2\2\6\uffff\1\5\2\uffff\1\6\13\uffff\1\2\2\uffff"+
            "\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2\5\uffff\1\2\4\uffff"+
            "\5\2\1\uffff\2\2\1\6",
            "",
            "\2\2\40\uffff\1\7\6\2\2\uffff\1\2\3\uffff\3\2\2\uffff\22\2"+
            "\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff\1\2",
            "\1\10\2\uffff\1\6\117\uffff\1\6",
            "\1\2\63\uffff\2\2\6\uffff\1\11\2\uffff\1\12\13\uffff\1\2\2"+
            "\uffff\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2\5\uffff\1\2"+
            "\4\uffff\5\2\1\uffff\2\2\1\12",
            "\2\2\40\uffff\1\13\2\uffff\4\2\2\uffff\1\2\4\uffff\2\2\2\uffff"+
            "\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff\1\2",
            "\2\2\40\uffff\1\7\6\2\2\uffff\1\2\3\uffff\2\2\1\14\2\uffff"+
            "\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff\1\2",
            "\1\15\2\uffff\1\12\117\uffff\1\12",
            "\1\2\63\uffff\2\2\6\uffff\1\16\2\uffff\1\12\13\uffff\1\2\2"+
            "\uffff\1\2\32\uffff\1\2\5\uffff\1\2\13\uffff\2\2\5\uffff\1\2"+
            "\4\uffff\5\2\1\uffff\2\2\1\12",
            "\2\2\35\uffff\1\2\3\uffff\2\4\1\uffff\1\2\1\uffff\1\2\2\uffff"+
            "\1\2\4\uffff\1\2\3\uffff\2\2\17\uffff\1\2\3\uffff\1\2\11\uffff"+
            "\1\2\1\uffff\31\2\3\uffff\1\2",
            "\2\2\40\uffff\1\13\2\uffff\4\2\2\uffff\1\2\4\uffff\1\2\1\14"+
            "\2\uffff\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff"+
            "\1\2",
            "\2\2\40\uffff\1\13\2\uffff\4\2\2\uffff\1\2\4\uffff\1\2\1\14"+
            "\2\uffff\22\2\3\uffff\1\2\11\uffff\1\2\1\uffff\31\2\3\uffff"+
            "\1\2"
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "137:1: toplevelstat : ( toplevelstatNoAlloc -> toplevelstatNoAlloc | toplevelAlloc SEMI -> toplevelAlloc );";
        }
    }
    static final String DFA7_eotS =
        "\15\uffff";
    static final String DFA7_eofS =
        "\15\uffff";
    static final String DFA7_minS =
        "\1\11\1\44\1\105\4\uffff\1\105\1\uffff\2\44\1\105\1\44";
    static final String DFA7_maxS =
        "\1\u0098\1\u0095\1\u0098\4\uffff\1\105\1\uffff\2\u0095\1\105\1\u0095";
    static final String DFA7_acceptS =
        "\3\uffff\1\3\1\4\1\5\1\1\1\uffff\1\2\4\uffff";
    static final String DFA7_specialS =
        "\1\0\14\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\4\63\uffff\2\4\5\uffff\1\3\1\1\2\uffff\1\2\6\uffff\1\5\4"+
            "\uffff\1\4\2\uffff\1\4\32\uffff\1\4\5\uffff\1\4\13\uffff\2\4"+
            "\5\uffff\1\4\4\uffff\5\4\1\uffff\2\4\1\2",
            "\2\4\35\uffff\1\4\5\uffff\1\6\1\4\1\10\1\4\1\uffff\1\6\1\4"+
            "\4\uffff\1\4\3\uffff\2\4\17\uffff\1\7\3\uffff\1\4\11\uffff\1"+
            "\4\1\uffff\31\4\3\uffff\1\4",
            "\1\11\2\uffff\1\2\117\uffff\1\2",
            "",
            "",
            "",
            "",
            "\1\12",
            "",
            "\2\4\35\uffff\1\4\6\uffff\1\4\1\10\1\4\2\uffff\1\4\4\uffff"+
            "\1\4\3\uffff\2\4\17\uffff\1\13\3\uffff\1\4\11\uffff\1\4\1\uffff"+
            "\31\4\3\uffff\1\4",
            "\2\4\35\uffff\1\4\6\uffff\1\4\1\10\1\4\2\uffff\1\4\4\uffff"+
            "\1\4\3\uffff\2\4\17\uffff\1\7\3\uffff\1\4\11\uffff\1\4\1\uffff"+
            "\31\4\3\uffff\1\4",
            "\1\14",
            "\2\4\35\uffff\1\4\6\uffff\1\4\1\10\1\4\2\uffff\1\4\4\uffff"+
            "\1\4\3\uffff\2\4\17\uffff\1\13\3\uffff\1\4\11\uffff\1\4\1\uffff"+
            "\31\4\3\uffff\1\4"
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "140:1: toplevelstatNoAlloc : ( defineStmt | scopeExtension ( SEMI )? -> scopeExtension | FORWARD ID ( COMMA ID )* SEMI -> ( ^( FORWARD ID ) )+ | rhsExpr SEMI -> ^( EXPR rhsExpr ) | ( LBRACE )=> xscope ( SEMI )? );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA7_0 = input.LA(1);

                         
                        int index7_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA7_0==ID) ) {s = 1;}

                        else if ( (LA7_0==COLON||LA7_0==COLONS) ) {s = 2;}

                        else if ( (LA7_0==FORWARD) ) {s = 3;}

                        else if ( (LA7_0==CODE||(LA7_0>=SIZEOF && LA7_0<=TYPEOF)||LA7_0==LPAREN||LA7_0==NIL||LA7_0==IF||LA7_0==NOT||(LA7_0>=TILDE && LA7_0<=AMP)||LA7_0==MINUS||(LA7_0>=PLUSPLUS && LA7_0<=STRING_LITERAL)||(LA7_0>=FALSE && LA7_0<=TRUE)) ) {s = 4;}

                        else if ( (LA7_0==LBRACE) && (synpred1_Eulang())) {s = 5;}

                         
                        input.seek(index7_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 7, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA21_eotS =
        "\u00a4\uffff";
    static final String DFA21_eofS =
        "\u00a4\uffff";
    static final String DFA21_minS =
        "\1\105\1\111\1\11\1\uffff\1\11\25\uffff\1\44\1\11\22\uffff\1\11"+
        "\74\uffff\1\44\70\uffff";
    static final String DFA21_maxS =
        "\1\105\1\116\1\u0099\1\uffff\1\u0098\25\uffff\1\u0095\1\u0099\22"+
        "\uffff\1\u0098\74\uffff\1\u0095\70\uffff";
    static final String DFA21_acceptS =
        "\3\uffff\1\2\1\uffff\25\3\2\uffff\22\3\1\uffff\46\3\26\1\1\uffff"+
        "\70\3";
    static final String DFA21_specialS =
        "\1\uffff\1\6\1\0\1\uffff\1\5\25\uffff\1\2\1\1\22\uffff\1\4\74\uffff"+
        "\1\3\70\uffff}>";
    static final String[] DFA21_transitionS = {
            "\1\1",
            "\1\2\4\uffff\1\3",
            "\1\21\63\uffff\1\25\1\26\6\uffff\1\6\2\uffff\1\7\3\uffff\1"+
            "\4\2\uffff\1\5\4\uffff\1\12\2\uffff\1\20\32\uffff\1\30\5\uffff"+
            "\1\27\13\uffff\1\11\1\24\5\uffff\1\10\4\uffff\1\22\1\23\1\13"+
            "\1\14\1\15\1\uffff\1\16\1\17\1\7\1\31",
            "",
            "\1\46\63\uffff\1\52\1\53\6\uffff\1\32\2\uffff\1\36\4\uffff"+
            "\1\33\6\uffff\1\37\2\uffff\1\45\32\uffff\1\55\5\uffff\1\54\13"+
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
            "\1\72\1\74\40\uffff\1\56\3\uffff\1\75\1\uffff\1\62\1\33\1\uffff"+
            "\1\64\4\uffff\1\61\3\uffff\1\122\1\63\17\uffff\1\57\3\uffff"+
            "\1\120\11\uffff\1\121\1\uffff\1\107\1\110\1\111\1\112\1\113"+
            "\1\114\1\60\1\115\1\116\1\117\1\106\1\105\1\104\1\77\1\100\1"+
            "\101\1\102\1\103\1\76\1\67\1\70\1\71\1\73\1\66\1\123\3\uffff"+
            "\1\65",
            "\1\142\63\uffff\1\146\1\147\4\uffff\1\124\1\uffff\1\126\2\uffff"+
            "\1\127\3\uffff\1\130\2\uffff\1\125\4\uffff\1\133\2\uffff\1\141"+
            "\32\uffff\1\151\5\uffff\1\150\13\uffff\1\132\1\145\5\uffff\1"+
            "\131\4\uffff\1\143\1\144\1\134\1\135\1\136\1\uffff\1\137\1\140"+
            "\1\127\1\152",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\167\63\uffff\1\173\1\174\6\uffff\1\153\2\uffff\1\157\4\uffff"+
            "\1\154\6\uffff\1\160\2\uffff\1\166\32\uffff\1\176\5\uffff\1"+
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u008b\1\u008d\40\uffff\1\56\3\uffff\1\u008e\1\uffff\1\u0082"+
            "\1\33\1\uffff\1\u0084\4\uffff\1\u0081\3\uffff\1\u00a3\1\u0083"+
            "\17\uffff\1\177\3\uffff\1\u00a1\11\uffff\1\u00a2\1\uffff\1\u0098"+
            "\1\u0099\1\u009a\1\u009b\1\u009c\1\u009d\1\u0080\1\u009e\1\u009f"+
            "\1\u00a0\1\u0097\1\u0096\1\u0095\1\u0090\1\u0091\1\u0092\1\u0093"+
            "\1\u0094\1\u008f\1\u0088\1\u0089\1\u008a\1\u008c\1\u0087\1\u0086"+
            "\3\uffff\1\u0085",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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

    static final short[] DFA21_eot = DFA.unpackEncodedString(DFA21_eotS);
    static final short[] DFA21_eof = DFA.unpackEncodedString(DFA21_eofS);
    static final char[] DFA21_min = DFA.unpackEncodedStringToUnsignedChars(DFA21_minS);
    static final char[] DFA21_max = DFA.unpackEncodedStringToUnsignedChars(DFA21_maxS);
    static final short[] DFA21_accept = DFA.unpackEncodedString(DFA21_acceptS);
    static final short[] DFA21_special = DFA.unpackEncodedString(DFA21_specialS);
    static final short[][] DFA21_transition;

    static {
        int numStates = DFA21_transitionS.length;
        DFA21_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA21_transition[i] = DFA.unpackEncodedString(DFA21_transitionS[i]);
        }
    }

    class DFA21 extends DFA {

        public DFA21(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 21;
            this.eot = DFA21_eot;
            this.eof = DFA21_eof;
            this.min = DFA21_min;
            this.max = DFA21_max;
            this.accept = DFA21_accept;
            this.special = DFA21_special;
            this.transition = DFA21_transition;
        }
        public String getDescription() {
            return "174:1: defineStmt : ( ( ID EQUALS LBRACKET )=> ID EQUALS LBRACKET idlistOrEmpty RBRACKET toplevelvalue SEMI -> ^( DEFINE ID idlistOrEmpty toplevelvalue ) | ( ID EQUALS_COLON )=> ID EQUALS_COLON type SEMI -> ^( DEFINE ID type ) | ( ID EQUALS )=> ID EQUALS toplevelvalue SEMI -> ^( DEFINE ID toplevelvalue ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA21_2 = input.LA(1);

                         
                        int index21_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_2==LBRACKET) ) {s = 4;}

                        else if ( (LA21_2==LBRACE) && (synpred4_Eulang())) {s = 5;}

                        else if ( (LA21_2==ID) && (synpred4_Eulang())) {s = 6;}

                        else if ( (LA21_2==COLON||LA21_2==COLONS) && (synpred4_Eulang())) {s = 7;}

                        else if ( (LA21_2==MINUS) && (synpred4_Eulang())) {s = 8;}

                        else if ( (LA21_2==TILDE) && (synpred4_Eulang())) {s = 9;}

                        else if ( (LA21_2==LPAREN) && (synpred4_Eulang())) {s = 10;}

                        else if ( (LA21_2==NUMBER) && (synpred4_Eulang())) {s = 11;}

                        else if ( (LA21_2==CHAR_LITERAL) && (synpred4_Eulang())) {s = 12;}

                        else if ( (LA21_2==STRING_LITERAL) && (synpred4_Eulang())) {s = 13;}

                        else if ( (LA21_2==FALSE) && (synpred4_Eulang())) {s = 14;}

                        else if ( (LA21_2==TRUE) && (synpred4_Eulang())) {s = 15;}

                        else if ( (LA21_2==NIL) && (synpred4_Eulang())) {s = 16;}

                        else if ( (LA21_2==CODE) && (synpred4_Eulang())) {s = 17;}

                        else if ( (LA21_2==PLUSPLUS) && (synpred4_Eulang())) {s = 18;}

                        else if ( (LA21_2==MINUSMINUS) && (synpred4_Eulang())) {s = 19;}

                        else if ( (LA21_2==AMP) && (synpred4_Eulang())) {s = 20;}

                        else if ( (LA21_2==SIZEOF) && (synpred4_Eulang())) {s = 21;}

                        else if ( (LA21_2==TYPEOF) && (synpred4_Eulang())) {s = 22;}

                        else if ( (LA21_2==NOT) && (synpred4_Eulang())) {s = 23;}

                        else if ( (LA21_2==IF) && (synpred4_Eulang())) {s = 24;}

                        else if ( (LA21_2==DATA) && (synpred4_Eulang())) {s = 25;}

                         
                        input.seek(index21_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA21_27 = input.LA(1);

                         
                        int index21_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_27==SEMI) && (synpred4_Eulang())) {s = 84;}

                        else if ( (LA21_27==LBRACE) && (synpred2_Eulang())) {s = 85;}

                        else if ( (LA21_27==ID) && (synpred2_Eulang())) {s = 86;}

                        else if ( (LA21_27==COLON||LA21_27==COLONS) && (synpred2_Eulang())) {s = 87;}

                        else if ( (LA21_27==LBRACKET) && (synpred2_Eulang())) {s = 88;}

                        else if ( (LA21_27==MINUS) && (synpred2_Eulang())) {s = 89;}

                        else if ( (LA21_27==TILDE) && (synpred2_Eulang())) {s = 90;}

                        else if ( (LA21_27==LPAREN) && (synpred2_Eulang())) {s = 91;}

                        else if ( (LA21_27==NUMBER) && (synpred2_Eulang())) {s = 92;}

                        else if ( (LA21_27==CHAR_LITERAL) && (synpred2_Eulang())) {s = 93;}

                        else if ( (LA21_27==STRING_LITERAL) && (synpred2_Eulang())) {s = 94;}

                        else if ( (LA21_27==FALSE) && (synpred2_Eulang())) {s = 95;}

                        else if ( (LA21_27==TRUE) && (synpred2_Eulang())) {s = 96;}

                        else if ( (LA21_27==NIL) && (synpred2_Eulang())) {s = 97;}

                        else if ( (LA21_27==CODE) && (synpred2_Eulang())) {s = 98;}

                        else if ( (LA21_27==PLUSPLUS) && (synpred2_Eulang())) {s = 99;}

                        else if ( (LA21_27==MINUSMINUS) && (synpred2_Eulang())) {s = 100;}

                        else if ( (LA21_27==AMP) && (synpred2_Eulang())) {s = 101;}

                        else if ( (LA21_27==SIZEOF) && (synpred2_Eulang())) {s = 102;}

                        else if ( (LA21_27==TYPEOF) && (synpred2_Eulang())) {s = 103;}

                        else if ( (LA21_27==NOT) && (synpred2_Eulang())) {s = 104;}

                        else if ( (LA21_27==IF) && (synpred2_Eulang())) {s = 105;}

                        else if ( (LA21_27==DATA) && (synpred2_Eulang())) {s = 106;}

                         
                        input.seek(index21_27);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA21_26 = input.LA(1);

                         
                        int index21_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_26==COMMA) ) {s = 46;}

                        else if ( (LA21_26==RBRACKET) ) {s = 27;}

                        else if ( (LA21_26==PERIOD) && (synpred4_Eulang())) {s = 47;}

                        else if ( (LA21_26==LESS) && (synpred4_Eulang())) {s = 48;}

                        else if ( (LA21_26==LPAREN) && (synpred4_Eulang())) {s = 49;}

                        else if ( (LA21_26==LBRACKET) && (synpred4_Eulang())) {s = 50;}

                        else if ( (LA21_26==CARET) && (synpred4_Eulang())) {s = 51;}

                        else if ( (LA21_26==LBRACE) && (synpred4_Eulang())) {s = 52;}

                        else if ( (LA21_26==AS) && (synpred4_Eulang())) {s = 53;}

                        else if ( (LA21_26==PLUSPLUS) && (synpred4_Eulang())) {s = 54;}

                        else if ( (LA21_26==STAR) && (synpred4_Eulang())) {s = 55;}

                        else if ( (LA21_26==SLASH) && (synpred4_Eulang())) {s = 56;}

                        else if ( (LA21_26==REM) && (synpred4_Eulang())) {s = 57;}

                        else if ( (LA21_26==UDIV) && (synpred4_Eulang())) {s = 58;}

                        else if ( (LA21_26==UREM) && (synpred4_Eulang())) {s = 59;}

                        else if ( (LA21_26==MOD) && (synpred4_Eulang())) {s = 60;}

                        else if ( (LA21_26==PLUS) && (synpred4_Eulang())) {s = 61;}

                        else if ( (LA21_26==MINUS) && (synpred4_Eulang())) {s = 62;}

                        else if ( (LA21_26==LSHIFT) && (synpred4_Eulang())) {s = 63;}

                        else if ( (LA21_26==RSHIFT) && (synpred4_Eulang())) {s = 64;}

                        else if ( (LA21_26==URSHIFT) && (synpred4_Eulang())) {s = 65;}

                        else if ( (LA21_26==CRSHIFT) && (synpred4_Eulang())) {s = 66;}

                        else if ( (LA21_26==CLSHIFT) && (synpred4_Eulang())) {s = 67;}

                        else if ( (LA21_26==AMP) && (synpred4_Eulang())) {s = 68;}

                        else if ( (LA21_26==TILDE) && (synpred4_Eulang())) {s = 69;}

                        else if ( (LA21_26==BAR) && (synpred4_Eulang())) {s = 70;}

                        else if ( (LA21_26==COMPEQ) && (synpred4_Eulang())) {s = 71;}

                        else if ( (LA21_26==COMPNE) && (synpred4_Eulang())) {s = 72;}

                        else if ( (LA21_26==COMPLE) && (synpred4_Eulang())) {s = 73;}

                        else if ( (LA21_26==COMPGE) && (synpred4_Eulang())) {s = 74;}

                        else if ( (LA21_26==COMPULE) && (synpred4_Eulang())) {s = 75;}

                        else if ( (LA21_26==COMPUGE) && (synpred4_Eulang())) {s = 76;}

                        else if ( (LA21_26==ULESS) && (synpred4_Eulang())) {s = 77;}

                        else if ( (LA21_26==GREATER) && (synpred4_Eulang())) {s = 78;}

                        else if ( (LA21_26==UGREATER) && (synpred4_Eulang())) {s = 79;}

                        else if ( (LA21_26==AND) && (synpred4_Eulang())) {s = 80;}

                        else if ( (LA21_26==OR) && (synpred4_Eulang())) {s = 81;}

                        else if ( (LA21_26==QUESTION) && (synpred4_Eulang())) {s = 82;}

                        else if ( (LA21_26==MINUSMINUS) && (synpred4_Eulang())) {s = 83;}

                         
                        input.seek(index21_26);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA21_107 = input.LA(1);

                         
                        int index21_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_107==RBRACKET) ) {s = 27;}

                        else if ( (LA21_107==COMMA) ) {s = 46;}

                        else if ( (LA21_107==PERIOD) && (synpred4_Eulang())) {s = 127;}

                        else if ( (LA21_107==LESS) && (synpred4_Eulang())) {s = 128;}

                        else if ( (LA21_107==LPAREN) && (synpred4_Eulang())) {s = 129;}

                        else if ( (LA21_107==LBRACKET) && (synpred4_Eulang())) {s = 130;}

                        else if ( (LA21_107==CARET) && (synpred4_Eulang())) {s = 131;}

                        else if ( (LA21_107==LBRACE) && (synpred4_Eulang())) {s = 132;}

                        else if ( (LA21_107==AS) && (synpred4_Eulang())) {s = 133;}

                        else if ( (LA21_107==MINUSMINUS) && (synpred4_Eulang())) {s = 134;}

                        else if ( (LA21_107==PLUSPLUS) && (synpred4_Eulang())) {s = 135;}

                        else if ( (LA21_107==STAR) && (synpred4_Eulang())) {s = 136;}

                        else if ( (LA21_107==SLASH) && (synpred4_Eulang())) {s = 137;}

                        else if ( (LA21_107==REM) && (synpred4_Eulang())) {s = 138;}

                        else if ( (LA21_107==UDIV) && (synpred4_Eulang())) {s = 139;}

                        else if ( (LA21_107==UREM) && (synpred4_Eulang())) {s = 140;}

                        else if ( (LA21_107==MOD) && (synpred4_Eulang())) {s = 141;}

                        else if ( (LA21_107==PLUS) && (synpred4_Eulang())) {s = 142;}

                        else if ( (LA21_107==MINUS) && (synpred4_Eulang())) {s = 143;}

                        else if ( (LA21_107==LSHIFT) && (synpred4_Eulang())) {s = 144;}

                        else if ( (LA21_107==RSHIFT) && (synpred4_Eulang())) {s = 145;}

                        else if ( (LA21_107==URSHIFT) && (synpred4_Eulang())) {s = 146;}

                        else if ( (LA21_107==CRSHIFT) && (synpred4_Eulang())) {s = 147;}

                        else if ( (LA21_107==CLSHIFT) && (synpred4_Eulang())) {s = 148;}

                        else if ( (LA21_107==AMP) && (synpred4_Eulang())) {s = 149;}

                        else if ( (LA21_107==TILDE) && (synpred4_Eulang())) {s = 150;}

                        else if ( (LA21_107==BAR) && (synpred4_Eulang())) {s = 151;}

                        else if ( (LA21_107==COMPEQ) && (synpred4_Eulang())) {s = 152;}

                        else if ( (LA21_107==COMPNE) && (synpred4_Eulang())) {s = 153;}

                        else if ( (LA21_107==COMPLE) && (synpred4_Eulang())) {s = 154;}

                        else if ( (LA21_107==COMPGE) && (synpred4_Eulang())) {s = 155;}

                        else if ( (LA21_107==COMPULE) && (synpred4_Eulang())) {s = 156;}

                        else if ( (LA21_107==COMPUGE) && (synpred4_Eulang())) {s = 157;}

                        else if ( (LA21_107==ULESS) && (synpred4_Eulang())) {s = 158;}

                        else if ( (LA21_107==GREATER) && (synpred4_Eulang())) {s = 159;}

                        else if ( (LA21_107==UGREATER) && (synpred4_Eulang())) {s = 160;}

                        else if ( (LA21_107==AND) && (synpred4_Eulang())) {s = 161;}

                        else if ( (LA21_107==OR) && (synpred4_Eulang())) {s = 162;}

                        else if ( (LA21_107==QUESTION) && (synpred4_Eulang())) {s = 163;}

                         
                        input.seek(index21_107);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA21_46 = input.LA(1);

                         
                        int index21_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_46==ID) ) {s = 107;}

                        else if ( (LA21_46==RBRACKET) && (synpred4_Eulang())) {s = 108;}

                        else if ( (LA21_46==MINUS) && (synpred4_Eulang())) {s = 109;}

                        else if ( (LA21_46==TILDE) && (synpred4_Eulang())) {s = 110;}

                        else if ( (LA21_46==COLON||LA21_46==COLONS) && (synpred4_Eulang())) {s = 111;}

                        else if ( (LA21_46==LPAREN) && (synpred4_Eulang())) {s = 112;}

                        else if ( (LA21_46==NUMBER) && (synpred4_Eulang())) {s = 113;}

                        else if ( (LA21_46==CHAR_LITERAL) && (synpred4_Eulang())) {s = 114;}

                        else if ( (LA21_46==STRING_LITERAL) && (synpred4_Eulang())) {s = 115;}

                        else if ( (LA21_46==FALSE) && (synpred4_Eulang())) {s = 116;}

                        else if ( (LA21_46==TRUE) && (synpred4_Eulang())) {s = 117;}

                        else if ( (LA21_46==NIL) && (synpred4_Eulang())) {s = 118;}

                        else if ( (LA21_46==CODE) && (synpred4_Eulang())) {s = 119;}

                        else if ( (LA21_46==PLUSPLUS) && (synpred4_Eulang())) {s = 120;}

                        else if ( (LA21_46==MINUSMINUS) && (synpred4_Eulang())) {s = 121;}

                        else if ( (LA21_46==AMP) && (synpred4_Eulang())) {s = 122;}

                        else if ( (LA21_46==SIZEOF) && (synpred4_Eulang())) {s = 123;}

                        else if ( (LA21_46==TYPEOF) && (synpred4_Eulang())) {s = 124;}

                        else if ( (LA21_46==NOT) && (synpred4_Eulang())) {s = 125;}

                        else if ( (LA21_46==IF) && (synpred4_Eulang())) {s = 126;}

                         
                        input.seek(index21_46);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA21_4 = input.LA(1);

                         
                        int index21_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_4==ID) ) {s = 26;}

                        else if ( (LA21_4==RBRACKET) ) {s = 27;}

                        else if ( (LA21_4==MINUS) && (synpred4_Eulang())) {s = 28;}

                        else if ( (LA21_4==TILDE) && (synpred4_Eulang())) {s = 29;}

                        else if ( (LA21_4==COLON||LA21_4==COLONS) && (synpred4_Eulang())) {s = 30;}

                        else if ( (LA21_4==LPAREN) && (synpred4_Eulang())) {s = 31;}

                        else if ( (LA21_4==NUMBER) && (synpred4_Eulang())) {s = 32;}

                        else if ( (LA21_4==CHAR_LITERAL) && (synpred4_Eulang())) {s = 33;}

                        else if ( (LA21_4==STRING_LITERAL) && (synpred4_Eulang())) {s = 34;}

                        else if ( (LA21_4==FALSE) && (synpred4_Eulang())) {s = 35;}

                        else if ( (LA21_4==TRUE) && (synpred4_Eulang())) {s = 36;}

                        else if ( (LA21_4==NIL) && (synpred4_Eulang())) {s = 37;}

                        else if ( (LA21_4==CODE) && (synpred4_Eulang())) {s = 38;}

                        else if ( (LA21_4==PLUSPLUS) && (synpred4_Eulang())) {s = 39;}

                        else if ( (LA21_4==MINUSMINUS) && (synpred4_Eulang())) {s = 40;}

                        else if ( (LA21_4==AMP) && (synpred4_Eulang())) {s = 41;}

                        else if ( (LA21_4==SIZEOF) && (synpred4_Eulang())) {s = 42;}

                        else if ( (LA21_4==TYPEOF) && (synpred4_Eulang())) {s = 43;}

                        else if ( (LA21_4==NOT) && (synpred4_Eulang())) {s = 44;}

                        else if ( (LA21_4==IF) && (synpred4_Eulang())) {s = 45;}

                         
                        input.seek(index21_4);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA21_1 = input.LA(1);

                         
                        int index21_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA21_1==EQUALS) ) {s = 2;}

                        else if ( (LA21_1==EQUALS_COLON) && (synpred3_Eulang())) {s = 3;}

                         
                        input.seek(index21_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 21, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA22_eotS =
        "\17\uffff";
    static final String DFA22_eofS =
        "\17\uffff";
    static final String DFA22_minS =
        "\1\11\1\uffff\1\44\1\105\3\uffff\1\105\1\11\2\44\2\uffff\1\105\1"+
        "\44";
    static final String DFA22_maxS =
        "\1\u0099\1\uffff\1\u0095\1\u0098\3\uffff\1\105\1\u0099\2\u0095\2"+
        "\uffff\1\105\1\u0095";
    static final String DFA22_acceptS =
        "\1\uffff\1\1\2\uffff\1\4\1\5\1\6\4\uffff\1\3\1\2\2\uffff";
    static final String DFA22_specialS =
        "\1\0\16\uffff}>";
    static final String[] DFA22_transitionS = {
            "\1\5\63\uffff\2\5\6\uffff\1\2\2\uffff\1\3\3\uffff\1\4\2\uffff"+
            "\1\1\4\uffff\1\5\2\uffff\1\5\32\uffff\1\5\5\uffff\1\5\13\uffff"+
            "\2\5\5\uffff\1\5\4\uffff\5\5\1\uffff\2\5\1\3\1\6",
            "",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\7\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5",
            "\1\11\2\uffff\1\3\117\uffff\1\3",
            "",
            "",
            "",
            "\1\12",
            "\1\5\63\uffff\2\5\6\uffff\1\5\2\uffff\1\5\6\uffff\1\13\4\uffff"+
            "\1\5\2\uffff\1\5\54\uffff\2\5\5\uffff\1\5\4\uffff\5\5\1\uffff"+
            "\3\5\1\14",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\15\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\7\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5",
            "",
            "",
            "\1\16",
            "\2\5\35\uffff\1\5\2\uffff\1\5\3\uffff\1\10\1\uffff\2\5\1\uffff"+
            "\1\5\4\uffff\1\5\3\uffff\2\5\17\uffff\1\15\3\uffff\1\5\11\uffff"+
            "\1\5\1\uffff\31\5\3\uffff\1\5"
    };

    static final short[] DFA22_eot = DFA.unpackEncodedString(DFA22_eotS);
    static final short[] DFA22_eof = DFA.unpackEncodedString(DFA22_eofS);
    static final char[] DFA22_min = DFA.unpackEncodedStringToUnsignedChars(DFA22_minS);
    static final char[] DFA22_max = DFA.unpackEncodedStringToUnsignedChars(DFA22_maxS);
    static final short[] DFA22_accept = DFA.unpackEncodedString(DFA22_acceptS);
    static final short[] DFA22_special = DFA.unpackEncodedString(DFA22_specialS);
    static final short[][] DFA22_transition;

    static {
        int numStates = DFA22_transitionS.length;
        DFA22_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA22_transition[i] = DFA.unpackEncodedString(DFA22_transitionS[i]);
        }
    }

    class DFA22 extends DFA {

        public DFA22(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 22;
            this.eot = DFA22_eot;
            this.eof = DFA22_eof;
            this.min = DFA22_min;
            this.max = DFA22_max;
            this.accept = DFA22_accept;
            this.special = DFA22_special;
            this.transition = DFA22_transition;
        }
        public String getDescription() {
            return "179:1: toplevelvalue : ( ( LBRACE )=> xscope | namespaceRef PLUS data -> ^( ADDSCOPE namespaceRef data ) | namespaceRef PLUS xscope -> ^( ADDSCOPE namespaceRef xscope ) | selector | rhsExpr | data );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA22_0 = input.LA(1);

                         
                        int index22_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA22_0==LBRACE) && (synpred5_Eulang())) {s = 1;}

                        else if ( (LA22_0==ID) ) {s = 2;}

                        else if ( (LA22_0==COLON||LA22_0==COLONS) ) {s = 3;}

                        else if ( (LA22_0==LBRACKET) ) {s = 4;}

                        else if ( (LA22_0==CODE||(LA22_0>=SIZEOF && LA22_0<=TYPEOF)||LA22_0==LPAREN||LA22_0==NIL||LA22_0==IF||LA22_0==NOT||(LA22_0>=TILDE && LA22_0<=AMP)||LA22_0==MINUS||(LA22_0>=PLUSPLUS && LA22_0<=STRING_LITERAL)||(LA22_0>=FALSE && LA22_0<=TRUE)) ) {s = 5;}

                        else if ( (LA22_0==DATA) ) {s = 6;}

                         
                        input.seek(index22_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 22, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA51_eotS =
        "\65\uffff";
    static final String DFA51_eofS =
        "\1\1\64\uffff";
    static final String DFA51_minS =
        "\1\44\60\uffff\1\0\3\uffff";
    static final String DFA51_maxS =
        "\1\u0091\60\uffff\1\0\3\uffff";
    static final String DFA51_acceptS =
        "\1\uffff\1\4\60\uffff\1\3\1\1\1\2";
    static final String DFA51_specialS =
        "\61\uffff\1\0\3\uffff}>";
    static final String[] DFA51_transitionS = {
            "\2\1\35\uffff\1\1\2\uffff\1\1\1\uffff\4\1\1\61\1\1\2\uffff\1"+
            "\1\1\uffff\1\1\2\uffff\2\1\1\uffff\1\1\1\62\17\1\1\uffff\2\1"+
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

    static final short[] DFA51_eot = DFA.unpackEncodedString(DFA51_eotS);
    static final short[] DFA51_eof = DFA.unpackEncodedString(DFA51_eofS);
    static final char[] DFA51_min = DFA.unpackEncodedStringToUnsignedChars(DFA51_minS);
    static final char[] DFA51_max = DFA.unpackEncodedStringToUnsignedChars(DFA51_maxS);
    static final short[] DFA51_accept = DFA.unpackEncodedString(DFA51_acceptS);
    static final short[] DFA51_special = DFA.unpackEncodedString(DFA51_specialS);
    static final short[][] DFA51_transition;

    static {
        int numStates = DFA51_transitionS.length;
        DFA51_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA51_transition[i] = DFA.unpackEncodedString(DFA51_transitionS[i]);
        }
    }

    class DFA51 extends DFA {

        public DFA51(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 51;
            this.eot = DFA51_eot;
            this.eof = DFA51_eof;
            this.min = DFA51_min;
            this.max = DFA51_max;
            this.accept = DFA51_accept;
            this.special = DFA51_special;
            this.transition = DFA51_transition;
        }
        public String getDescription() {
            return "()* loopback of 278:6: ( ( ( ( arraySuff )+ )=> ( arraySuff )+ -> ^( TYPE ^( ARRAY $type ( arraySuff )+ ) ) ) | ( LBRACKET rhsExpr ( COMMA rhsExpr )+ RBRACKET -> ^( TYPE ^( ARRAY $type ( rhsExpr )+ ) ) ) | ( CARET -> ^( TYPE ^( POINTER $type) ) ) )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA51_49 = input.LA(1);

                         
                        int index51_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Eulang()) ) {s = 51;}

                        else if ( (true) ) {s = 52;}

                         
                        input.seek(index51_49);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 51, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA59_eotS =
        "\34\uffff";
    static final String DFA59_eofS =
        "\34\uffff";
    static final String DFA59_minS =
        "\1\11\6\0\25\uffff";
    static final String DFA59_maxS =
        "\1\u0098\6\0\25\uffff";
    static final String DFA59_acceptS =
        "\7\uffff\1\3\14\uffff\1\4\1\5\1\6\3\uffff\1\1\1\2";
    static final String DFA59_specialS =
        "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\25\uffff}>";
    static final String[] DFA59_transitionS = {
            "\1\7\46\uffff\1\25\14\uffff\2\7\6\uffff\1\1\2\uffff\1\3\6\uffff"+
            "\1\24\1\uffff\1\26\2\uffff\1\2\2\uffff\1\7\22\uffff\3\26\5\uffff"+
            "\1\7\5\uffff\1\7\13\uffff\2\7\5\uffff\1\7\4\uffff\2\7\1\4\1"+
            "\5\1\6\1\uffff\2\7\1\3",
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

    static final short[] DFA59_eot = DFA.unpackEncodedString(DFA59_eotS);
    static final short[] DFA59_eof = DFA.unpackEncodedString(DFA59_eofS);
    static final char[] DFA59_min = DFA.unpackEncodedStringToUnsignedChars(DFA59_minS);
    static final char[] DFA59_max = DFA.unpackEncodedStringToUnsignedChars(DFA59_maxS);
    static final short[] DFA59_accept = DFA.unpackEncodedString(DFA59_acceptS);
    static final short[] DFA59_special = DFA.unpackEncodedString(DFA59_specialS);
    static final short[][] DFA59_transition;

    static {
        int numStates = DFA59_transitionS.length;
        DFA59_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA59_transition[i] = DFA.unpackEncodedString(DFA59_transitionS[i]);
        }
    }

    class DFA59 extends DFA {

        public DFA59(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 59;
            this.eot = DFA59_eot;
            this.eof = DFA59_eof;
            this.min = DFA59_min;
            this.max = DFA59_max;
            this.accept = DFA59_accept;
            this.special = DFA59_special;
            this.transition = DFA59_transition;
        }
        public String getDescription() {
            return "310:1: codeStmtExpr : ( ( varDecl )=> varDecl -> varDecl | ( assignStmt )=> assignStmt -> assignStmt | rhsExpr -> ^( STMTEXPR rhsExpr ) | ( LBRACE )=> blockStmt -> blockStmt | gotoStmt -> gotoStmt | controlStmt -> controlStmt );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA59_0 = input.LA(1);

                         
                        int index59_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA59_0==ID) ) {s = 1;}

                        else if ( (LA59_0==LPAREN) ) {s = 2;}

                        else if ( (LA59_0==COLON||LA59_0==COLONS) ) {s = 3;}

                        else if ( (LA59_0==NUMBER) ) {s = 4;}

                        else if ( (LA59_0==CHAR_LITERAL) ) {s = 5;}

                        else if ( (LA59_0==STRING_LITERAL) ) {s = 6;}

                        else if ( (LA59_0==CODE||(LA59_0>=SIZEOF && LA59_0<=TYPEOF)||LA59_0==NIL||LA59_0==IF||LA59_0==NOT||(LA59_0>=TILDE && LA59_0<=AMP)||LA59_0==MINUS||(LA59_0>=PLUSPLUS && LA59_0<=MINUSMINUS)||(LA59_0>=FALSE && LA59_0<=TRUE)) ) {s = 7;}

                        else if ( (LA59_0==LBRACE) && (synpred12_Eulang())) {s = 20;}

                        else if ( (LA59_0==GOTO) ) {s = 21;}

                        else if ( (LA59_0==FOR||(LA59_0>=DO && LA59_0<=REPEAT)) ) {s = 22;}

                         
                        input.seek(index59_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA59_1 = input.LA(1);

                         
                        int index59_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Eulang()) ) {s = 26;}

                        else if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index59_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA59_2 = input.LA(1);

                         
                        int index59_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred10_Eulang()) ) {s = 26;}

                        else if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index59_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA59_3 = input.LA(1);

                         
                        int index59_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index59_3);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA59_4 = input.LA(1);

                         
                        int index59_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index59_4);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA59_5 = input.LA(1);

                         
                        int index59_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index59_5);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA59_6 = input.LA(1);

                         
                        int index59_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred11_Eulang()) ) {s = 27;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index59_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 59, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA72_eotS =
        "\5\uffff";
    static final String DFA72_eofS =
        "\5\uffff";
    static final String DFA72_minS =
        "\1\106\1\107\3\uffff";
    static final String DFA72_maxS =
        "\2\123\3\uffff";
    static final String DFA72_acceptS =
        "\2\uffff\1\1\1\2\1\3";
    static final String DFA72_specialS =
        "\5\uffff}>";
    static final String[] DFA72_transitionS = {
            "\1\4\1\2\1\3\12\uffff\1\1",
            "\1\2\1\3\12\uffff\1\1",
            "",
            "",
            ""
    };

    static final short[] DFA72_eot = DFA.unpackEncodedString(DFA72_eotS);
    static final short[] DFA72_eof = DFA.unpackEncodedString(DFA72_eofS);
    static final char[] DFA72_min = DFA.unpackEncodedStringToUnsignedChars(DFA72_minS);
    static final char[] DFA72_max = DFA.unpackEncodedStringToUnsignedChars(DFA72_maxS);
    static final short[] DFA72_accept = DFA.unpackEncodedString(DFA72_acceptS);
    static final short[] DFA72_special = DFA.unpackEncodedString(DFA72_specialS);
    static final short[][] DFA72_transition;

    static {
        int numStates = DFA72_transitionS.length;
        DFA72_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA72_transition[i] = DFA.unpackEncodedString(DFA72_transitionS[i]);
        }
    }

    class DFA72 extends DFA {

        public DFA72(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 72;
            this.eot = DFA72_eot;
            this.eof = DFA72_eof;
            this.min = DFA72_min;
            this.max = DFA72_max;
            this.accept = DFA72_accept;
            this.special = DFA72_special;
            this.transition = DFA72_transition;
        }
        public String getDescription() {
            return "323:8: ( ( ( attrs )? COLON_EQUALS assignOrInitExpr -> ^( ALLOC ID ( attrs )? TYPE assignOrInitExpr ) ) | ( ( attrs )? COLON type ( EQUALS assignOrInitExpr )? -> ^( ALLOC ID ( attrs )? type ( assignOrInitExpr )* ) ) | ( COMMA ID )+ ( attrs )? ( ( COLON_EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? TYPE ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) | ( COLON type ( EQUALS ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* )? ) -> ^( ALLOC ^( LIST ( ID )+ ) ( attrs )? type ( PLUS )? ( ^( LIST ( assignOrInitExpr )+ ) )? ) ) )";
        }
    }
    static final String DFA79_eotS =
        "\12\uffff";
    static final String DFA79_eofS =
        "\12\uffff";
    static final String DFA79_minS =
        "\1\105\6\0\3\uffff";
    static final String DFA79_maxS =
        "\1\u0098\6\0\3\uffff";
    static final String DFA79_acceptS =
        "\7\uffff\1\1\1\3\1\2";
    static final String DFA79_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\3\uffff}>";
    static final String[] DFA79_transitionS = {
            "\1\1\2\uffff\1\2\13\uffff\1\3\75\uffff\1\4\1\5\1\6\3\uffff\1"+
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

    static final short[] DFA79_eot = DFA.unpackEncodedString(DFA79_eotS);
    static final short[] DFA79_eof = DFA.unpackEncodedString(DFA79_eofS);
    static final char[] DFA79_min = DFA.unpackEncodedStringToUnsignedChars(DFA79_minS);
    static final char[] DFA79_max = DFA.unpackEncodedStringToUnsignedChars(DFA79_maxS);
    static final short[] DFA79_accept = DFA.unpackEncodedString(DFA79_acceptS);
    static final short[] DFA79_special = DFA.unpackEncodedString(DFA79_specialS);
    static final short[][] DFA79_transition;

    static {
        int numStates = DFA79_transitionS.length;
        DFA79_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA79_transition[i] = DFA.unpackEncodedString(DFA79_transitionS[i]);
        }
    }

    class DFA79 extends DFA {

        public DFA79(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 79;
            this.eot = DFA79_eot;
            this.eof = DFA79_eof;
            this.min = DFA79_min;
            this.max = DFA79_max;
            this.accept = DFA79_accept;
            this.special = DFA79_special;
            this.transition = DFA79_transition;
        }
        public String getDescription() {
            return "342:1: assignStmt : ( ( lhs assignEqOp )=> lhs assignEqOp assignOrInitExpr -> ^( ASSIGN assignEqOp lhs assignOrInitExpr ) | idTuple EQUALS assignOrInitExpr -> ^( ASSIGN EQUALS idTuple assignOrInitExpr ) | ( lhs ( COMMA lhs )+ assignEqOp )=> lhs ( COMMA lhs )+ assignEqOp ( PLUS )? assignOrInitExpr ( COMMA assignOrInitExpr )* -> ^( ASSIGN assignEqOp ^( LIST ( lhs )+ ) ( PLUS )? ^( LIST ( assignOrInitExpr )+ ) ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA79_1 = input.LA(1);

                         
                        int index79_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index79_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA79_2 = input.LA(1);

                         
                        int index79_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index79_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA79_3 = input.LA(1);

                         
                        int index79_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (true) ) {s = 9;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index79_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA79_4 = input.LA(1);

                         
                        int index79_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index79_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA79_5 = input.LA(1);

                         
                        int index79_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index79_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA79_6 = input.LA(1);

                         
                        int index79_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred13_Eulang()) ) {s = 7;}

                        else if ( (synpred14_Eulang()) ) {s = 8;}

                         
                        input.seek(index79_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 79, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA81_eotS =
        "\26\uffff";
    static final String DFA81_eofS =
        "\26\uffff";
    static final String DFA81_minS =
        "\1\11\6\0\17\uffff";
    static final String DFA81_maxS =
        "\1\u0098\6\0\17\uffff";
    static final String DFA81_acceptS =
        "\7\uffff\1\3\14\uffff\1\1\1\2";
    static final String DFA81_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\17\uffff}>";
    static final String[] DFA81_transitionS = {
            "\1\7\63\uffff\2\7\6\uffff\1\1\2\uffff\1\2\13\uffff\1\3\2\uffff"+
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

    static final short[] DFA81_eot = DFA.unpackEncodedString(DFA81_eotS);
    static final short[] DFA81_eof = DFA.unpackEncodedString(DFA81_eofS);
    static final char[] DFA81_min = DFA.unpackEncodedStringToUnsignedChars(DFA81_minS);
    static final char[] DFA81_max = DFA.unpackEncodedStringToUnsignedChars(DFA81_maxS);
    static final short[] DFA81_accept = DFA.unpackEncodedString(DFA81_acceptS);
    static final short[] DFA81_special = DFA.unpackEncodedString(DFA81_specialS);
    static final short[][] DFA81_transition;

    static {
        int numStates = DFA81_transitionS.length;
        DFA81_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA81_transition[i] = DFA.unpackEncodedString(DFA81_transitionS[i]);
        }
    }

    class DFA81 extends DFA {

        public DFA81(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 81;
            this.eot = DFA81_eot;
            this.eof = DFA81_eof;
            this.min = DFA81_min;
            this.max = DFA81_max;
            this.accept = DFA81_accept;
            this.special = DFA81_special;
            this.transition = DFA81_transition;
        }
        public String getDescription() {
            return "352:1: assignExpr : ( ( lhs assignEqOp )=> lhs assignEqOp assignExpr -> ^( ASSIGN assignEqOp lhs assignExpr ) | ( idTuple EQUALS )=> idTuple EQUALS assignExpr -> ^( ASSIGN EQUALS idTuple assignExpr ) | rhsExpr -> rhsExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA81_1 = input.LA(1);

                         
                        int index81_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index81_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA81_2 = input.LA(1);

                         
                        int index81_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index81_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA81_3 = input.LA(1);

                         
                        int index81_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (synpred16_Eulang()) ) {s = 21;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index81_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA81_4 = input.LA(1);

                         
                        int index81_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index81_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA81_5 = input.LA(1);

                         
                        int index81_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index81_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA81_6 = input.LA(1);

                         
                        int index81_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_Eulang()) ) {s = 20;}

                        else if ( (true) ) {s = 7;}

                         
                        input.seek(index81_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 81, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA85_eotS =
        "\30\uffff";
    static final String DFA85_eofS =
        "\30\uffff";
    static final String DFA85_minS =
        "\1\11\24\uffff\1\0\2\uffff";
    static final String DFA85_maxS =
        "\1\u0098\24\uffff\1\0\2\uffff";
    static final String DFA85_acceptS =
        "\1\uffff\23\1\1\2\1\uffff\1\3\1\4";
    static final String DFA85_specialS =
        "\1\0\24\uffff\1\1\2\uffff}>";
    static final String[] DFA85_transitionS = {
            "\1\14\63\uffff\1\20\1\21\6\uffff\1\3\2\uffff\1\4\3\uffff\1\25"+
            "\7\uffff\1\5\2\uffff\1\13\21\uffff\1\24\10\uffff\1\23\5\uffff"+
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

    static final short[] DFA85_eot = DFA.unpackEncodedString(DFA85_eotS);
    static final short[] DFA85_eof = DFA.unpackEncodedString(DFA85_eofS);
    static final char[] DFA85_min = DFA.unpackEncodedStringToUnsignedChars(DFA85_minS);
    static final char[] DFA85_max = DFA.unpackEncodedStringToUnsignedChars(DFA85_maxS);
    static final short[] DFA85_accept = DFA.unpackEncodedString(DFA85_acceptS);
    static final short[] DFA85_special = DFA.unpackEncodedString(DFA85_specialS);
    static final short[][] DFA85_transition;

    static {
        int numStates = DFA85_transitionS.length;
        DFA85_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA85_transition[i] = DFA.unpackEncodedString(DFA85_transitionS[i]);
        }
    }

    class DFA85 extends DFA {

        public DFA85(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 85;
            this.eot = DFA85_eot;
            this.eof = DFA85_eof;
            this.min = DFA85_min;
            this.max = DFA85_max;
            this.accept = DFA85_accept;
            this.special = DFA85_special;
            this.transition = DFA85_transition;
        }
        public String getDescription() {
            return "362:1: initExpr : ( ( rhsExpr )=>e= rhsExpr -> ^( INITEXPR $e) | PERIOD ID EQUALS ei= initElement -> ^( INITEXPR $ei ID ) | ( LBRACKET i= rhsExpr RBRACKET )=> LBRACKET i= rhsExpr RBRACKET EQUALS ei= initElement -> ^( INITEXPR $ei $i) | initList );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA85_0 = input.LA(1);

                         
                        int index85_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA85_0==MINUS) && (synpred17_Eulang())) {s = 1;}

                        else if ( (LA85_0==TILDE) && (synpred17_Eulang())) {s = 2;}

                        else if ( (LA85_0==ID) && (synpred17_Eulang())) {s = 3;}

                        else if ( (LA85_0==COLON||LA85_0==COLONS) && (synpred17_Eulang())) {s = 4;}

                        else if ( (LA85_0==LPAREN) && (synpred17_Eulang())) {s = 5;}

                        else if ( (LA85_0==NUMBER) && (synpred17_Eulang())) {s = 6;}

                        else if ( (LA85_0==CHAR_LITERAL) && (synpred17_Eulang())) {s = 7;}

                        else if ( (LA85_0==STRING_LITERAL) && (synpred17_Eulang())) {s = 8;}

                        else if ( (LA85_0==FALSE) && (synpred17_Eulang())) {s = 9;}

                        else if ( (LA85_0==TRUE) && (synpred17_Eulang())) {s = 10;}

                        else if ( (LA85_0==NIL) && (synpred17_Eulang())) {s = 11;}

                        else if ( (LA85_0==CODE) && (synpred17_Eulang())) {s = 12;}

                        else if ( (LA85_0==PLUSPLUS) && (synpred17_Eulang())) {s = 13;}

                        else if ( (LA85_0==MINUSMINUS) && (synpred17_Eulang())) {s = 14;}

                        else if ( (LA85_0==AMP) && (synpred17_Eulang())) {s = 15;}

                        else if ( (LA85_0==SIZEOF) && (synpred17_Eulang())) {s = 16;}

                        else if ( (LA85_0==TYPEOF) && (synpred17_Eulang())) {s = 17;}

                        else if ( (LA85_0==NOT) && (synpred17_Eulang())) {s = 18;}

                        else if ( (LA85_0==IF) && (synpred17_Eulang())) {s = 19;}

                        else if ( (LA85_0==PERIOD) ) {s = 20;}

                        else if ( (LA85_0==LBRACKET) ) {s = 21;}

                         
                        input.seek(index85_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA85_21 = input.LA(1);

                         
                        int index85_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred18_Eulang()) ) {s = 22;}

                        else if ( (true) ) {s = 23;}

                         
                        input.seek(index85_21);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 85, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA118_eotS =
        "\24\uffff";
    static final String DFA118_eofS =
        "\24\uffff";
    static final String DFA118_minS =
        "\1\11\2\uffff\6\0\13\uffff";
    static final String DFA118_maxS =
        "\1\u0098\2\uffff\6\0\13\uffff";
    static final String DFA118_acceptS =
        "\1\uffff\1\1\1\2\6\uffff\1\5\3\uffff\1\6\1\7\1\10\1\11\1\12\1\3"+
        "\1\4";
    static final String DFA118_specialS =
        "\3\uffff\1\0\1\1\1\2\1\3\1\4\1\5\13\uffff}>";
    static final String[] DFA118_transitionS = {
            "\1\11\63\uffff\1\20\1\21\6\uffff\1\3\2\uffff\1\4\13\uffff\1"+
            "\5\2\uffff\1\11\54\uffff\1\2\1\17\5\uffff\1\1\4\uffff\1\15\1"+
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

    static final short[] DFA118_eot = DFA.unpackEncodedString(DFA118_eotS);
    static final short[] DFA118_eof = DFA.unpackEncodedString(DFA118_eofS);
    static final char[] DFA118_min = DFA.unpackEncodedStringToUnsignedChars(DFA118_minS);
    static final char[] DFA118_max = DFA.unpackEncodedStringToUnsignedChars(DFA118_maxS);
    static final short[] DFA118_accept = DFA.unpackEncodedString(DFA118_acceptS);
    static final short[] DFA118_special = DFA.unpackEncodedString(DFA118_specialS);
    static final short[][] DFA118_transition;

    static {
        int numStates = DFA118_transitionS.length;
        DFA118_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA118_transition[i] = DFA.unpackEncodedString(DFA118_transitionS[i]);
        }
    }

    class DFA118 extends DFA {

        public DFA118(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 118;
            this.eot = DFA118_eot;
            this.eof = DFA118_eof;
            this.min = DFA118_min;
            this.max = DFA118_max;
            this.accept = DFA118_accept;
            this.special = DFA118_special;
            this.transition = DFA118_transition;
        }
        public String getDescription() {
            return "540:1: unary : ( MINUS u= unary -> ^( NEG $u) | TILDE u= unary -> ^( INV $u) | ( lhs PLUSPLUS )=>a= lhs PLUSPLUS -> ^( POSTINC $a) | ( lhs MINUSMINUS )=>a= lhs MINUSMINUS -> ^( POSTDEC $a) | ( atom -> atom ) | PLUSPLUS a= lhs -> ^( PREINC $a) | MINUSMINUS a= lhs -> ^( PREDEC $a) | AMP lhs -> ^( ADDROF lhs ) | SIZEOF atom -> ^( SIZEOF atom ) | TYPEOF atom -> ^( TYPEOF atom ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA118_3 = input.LA(1);

                         
                        int index118_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index118_3);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA118_4 = input.LA(1);

                         
                        int index118_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index118_4);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA118_5 = input.LA(1);

                         
                        int index118_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index118_5);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA118_6 = input.LA(1);

                         
                        int index118_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index118_6);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA118_7 = input.LA(1);

                         
                        int index118_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index118_7);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA118_8 = input.LA(1);

                         
                        int index118_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_Eulang()) ) {s = 18;}

                        else if ( (synpred22_Eulang()) ) {s = 19;}

                        else if ( (true) ) {s = 9;}

                         
                        input.seek(index118_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 118, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA124_eotS =
        "\16\uffff";
    static final String DFA124_eofS =
        "\16\uffff";
    static final String DFA124_minS =
        "\1\11\10\uffff\1\0\4\uffff";
    static final String DFA124_maxS =
        "\1\u0098\10\uffff\1\0\4\uffff";
    static final String DFA124_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\2\uffff\1\13\1\10\1\11\1\12";
    static final String DFA124_specialS =
        "\11\uffff\1\0\4\uffff}>";
    static final String[] DFA124_transitionS = {
            "\1\12\73\uffff\1\7\2\uffff\1\7\13\uffff\1\11\2\uffff\1\6\72"+
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

    static final short[] DFA124_eot = DFA.unpackEncodedString(DFA124_eotS);
    static final short[] DFA124_eof = DFA.unpackEncodedString(DFA124_eofS);
    static final char[] DFA124_min = DFA.unpackEncodedStringToUnsignedChars(DFA124_minS);
    static final char[] DFA124_max = DFA.unpackEncodedStringToUnsignedChars(DFA124_maxS);
    static final short[] DFA124_accept = DFA.unpackEncodedString(DFA124_acceptS);
    static final short[] DFA124_special = DFA.unpackEncodedString(DFA124_specialS);
    static final short[][] DFA124_transition;

    static {
        int numStates = DFA124_transitionS.length;
        DFA124_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA124_transition[i] = DFA.unpackEncodedString(DFA124_transitionS[i]);
        }
    }

    class DFA124 extends DFA {

        public DFA124(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 124;
            this.eot = DFA124_eot;
            this.eof = DFA124_eof;
            this.min = DFA124_min;
            this.max = DFA124_max;
            this.accept = DFA124_accept;
            this.special = DFA124_special;
            this.transition = DFA124_transition;
        }
        public String getDescription() {
            return "575:3: ( NUMBER -> ^( LIT NUMBER ) | FALSE -> ^( LIT FALSE ) | TRUE -> ^( LIT TRUE ) | CHAR_LITERAL -> ^( LIT CHAR_LITERAL ) | STRING_LITERAL -> ^( LIT STRING_LITERAL ) | NIL -> ^( LIT NIL ) | idExpr -> idExpr | ( tuple )=> tuple -> tuple | ( LPAREN varDecl )=> LPAREN a0= varDecl RPAREN -> $a0 | LPAREN a1= assignExpr RPAREN -> $a1 | code -> code )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA124_9 = input.LA(1);

                         
                        int index124_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_Eulang()) ) {s = 11;}

                        else if ( (synpred26_Eulang()) ) {s = 12;}

                        else if ( (true) ) {s = 13;}

                         
                        input.seek(index124_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 124, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA130_eotS =
        "\70\uffff";
    static final String DFA130_eofS =
        "\1\2\67\uffff";
    static final String DFA130_minS =
        "\1\44\1\0\66\uffff";
    static final String DFA130_maxS =
        "\1\u0095\1\0\66\uffff";
    static final String DFA130_acceptS =
        "\2\uffff\1\2\64\uffff\1\1";
    static final String DFA130_specialS =
        "\1\uffff\1\0\66\uffff}>";
    static final String[] DFA130_transitionS = {
            "\2\2\35\uffff\1\2\2\uffff\1\2\1\uffff\6\2\1\uffff\2\2\1\uffff"+
            "\1\2\1\uffff\3\2\1\uffff\24\2\1\uffff\1\2\5\uffff\5\2\1\uffff"+
            "\6\2\1\1\22\2\3\uffff\1\2",
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

    static final short[] DFA130_eot = DFA.unpackEncodedString(DFA130_eotS);
    static final short[] DFA130_eof = DFA.unpackEncodedString(DFA130_eofS);
    static final char[] DFA130_min = DFA.unpackEncodedStringToUnsignedChars(DFA130_minS);
    static final char[] DFA130_max = DFA.unpackEncodedStringToUnsignedChars(DFA130_maxS);
    static final short[] DFA130_accept = DFA.unpackEncodedString(DFA130_acceptS);
    static final short[] DFA130_special = DFA.unpackEncodedString(DFA130_specialS);
    static final short[][] DFA130_transition;

    static {
        int numStates = DFA130_transitionS.length;
        DFA130_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA130_transition[i] = DFA.unpackEncodedString(DFA130_transitionS[i]);
        }
    }

    class DFA130 extends DFA {

        public DFA130(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 130;
            this.eot = DFA130_eot;
            this.eof = DFA130_eof;
            this.min = DFA130_min;
            this.max = DFA130_max;
            this.accept = DFA130_accept;
            this.special = DFA130_special;
            this.transition = DFA130_transition;
        }
        public String getDescription() {
            return "608:5: ( ( instantiation )=> instantiation -> ^( INSTANCE $idExpr instantiation ) )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA130_1 = input.LA(1);

                         
                        int index130_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred28_Eulang()) ) {s = 55;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index130_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 130, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA136_eotS =
        "\14\uffff";
    static final String DFA136_eofS =
        "\14\uffff";
    static final String DFA136_minS =
        "\1\11\3\0\1\uffff\1\0\6\uffff";
    static final String DFA136_maxS =
        "\1\u0099\3\0\1\uffff\1\0\6\uffff";
    static final String DFA136_acceptS =
        "\4\uffff\1\1\1\uffff\1\2\5\uffff";
    static final String DFA136_specialS =
        "\1\uffff\1\0\1\1\1\2\1\uffff\1\3\6\uffff}>";
    static final String[] DFA136_transitionS = {
            "\1\3\73\uffff\1\1\2\uffff\1\2\13\uffff\1\5\2\uffff\1\6\72\uffff"+
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

    static final short[] DFA136_eot = DFA.unpackEncodedString(DFA136_eotS);
    static final short[] DFA136_eof = DFA.unpackEncodedString(DFA136_eofS);
    static final char[] DFA136_min = DFA.unpackEncodedStringToUnsignedChars(DFA136_minS);
    static final char[] DFA136_max = DFA.unpackEncodedStringToUnsignedChars(DFA136_maxS);
    static final short[] DFA136_accept = DFA.unpackEncodedString(DFA136_acceptS);
    static final short[] DFA136_special = DFA.unpackEncodedString(DFA136_specialS);
    static final short[][] DFA136_transition;

    static {
        int numStates = DFA136_transitionS.length;
        DFA136_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA136_transition[i] = DFA.unpackEncodedString(DFA136_transitionS[i]);
        }
    }

    class DFA136 extends DFA {

        public DFA136(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 136;
            this.eot = DFA136_eot;
            this.eof = DFA136_eof;
            this.min = DFA136_min;
            this.max = DFA136_max;
            this.accept = DFA136_accept;
            this.special = DFA136_special;
            this.transition = DFA136_transition;
        }
        public String getDescription() {
            return "618:1: instanceExpr options {backtrack=true; } : ( type | atom );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA136_1 = input.LA(1);

                         
                        int index136_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index136_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA136_2 = input.LA(1);

                         
                        int index136_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index136_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA136_3 = input.LA(1);

                         
                        int index136_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index136_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA136_5 = input.LA(1);

                         
                        int index136_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_Eulang()) ) {s = 4;}

                        else if ( (true) ) {s = 6;}

                         
                        input.seek(index136_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 136, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_toplevelstmts_in_prog440 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelstat_in_toplevelstmts471 = new BitSet(new long[]{0x6000000000000202L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstatNoAlloc_in_toplevelstmtsNoAlloc498 = new BitSet(new long[]{0x6000000000000202L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstatNoAlloc_in_toplevelstat527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelAlloc_in_toplevelstat540 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstat542 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defineStmt_in_toplevelstatNoAlloc557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scopeExtension_in_toplevelstatNoAlloc565 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FORWARD_in_toplevelstatNoAlloc581 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_toplevelstatNoAlloc583 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_COMMA_in_toplevelstatNoAlloc586 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_toplevelstatNoAlloc588 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc592 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelstatNoAlloc609 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelstatNoAlloc651 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_toplevelstatNoAlloc653 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelSingleVarDecl_in_toplevelAlloc668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelTupleVarDecl_in_toplevelAlloc672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_toplevelSingleVarDecl683 = new BitSet(new long[]{0x0000000000000000L,0x00000000000001C0L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl697 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_toplevelSingleVarDecl733 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_toplevelSingleVarDecl735 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelSingleVarDecl738 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl770 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_toplevelSingleVarDecl772 = new BitSet(new long[]{0x0000000000000000L,0x00000000000001C0L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelSingleVarDecl791 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901520L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_toplevelSingleVarDecl793 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl796 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl799 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl801 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_toplevelSingleVarDecl860 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_toplevelSingleVarDecl862 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelSingleVarDecl865 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901520L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_toplevelSingleVarDecl867 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl870 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_toplevelSingleVarDecl873 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelSingleVarDecl875 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_idTuple_in_toplevelTupleVarDecl966 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_toplevelTupleVarDecl988 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_toplevelTupleVarDecl1024 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_toplevelTupleVarDecl1026 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_toplevelTupleVarDecl1029 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExprOrInitList_in_toplevelTupleVarDecl1031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_rhsExprOrInitList1074 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_rhsExprOrInitList1078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_scopeExtension1087 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_PLUS_EQ_in_scopeExtension1089 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_xscopeNoAlloc_in_scopeExtension1091 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1120 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_defineStmt1122 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACKET_in_defineStmt1124 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002020L});
    public static final BitSet FOLLOW_idlistOrEmpty_in_defineStmt1126 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_defineStmt1128 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_toplevelvalue_in_defineStmt1131 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1137 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1167 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_EQUALS_COLON_in_defineStmt1169 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_defineStmt1171 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_defineStmt1204 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_defineStmt1206 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_toplevelvalue_in_defineStmt1208 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_defineStmt1214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue1242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_toplevelvalue1250 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_PLUS_in_toplevelvalue1252 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_data_in_toplevelvalue1254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceRef_in_toplevelvalue1272 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_PLUS_in_toplevelvalue1274 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_xscope_in_toplevelvalue1276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selector_in_toplevelvalue1294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_toplevelvalue1302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_data_in_toplevelvalue1310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_selector1323 = new BitSet(new long[]{0x6000000000000200L,0x0104000000902120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_selectors_in_selector1325 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_selector1327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectoritem_in_selectors1353 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_selectors1357 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_selectoritem_in_selectors1359 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_selectors1364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_selectoritem1395 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscope1406 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstmts_in_xscope1408 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_xscope1410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_xscopeNoAlloc1435 = new BitSet(new long[]{0x6000000000000200L,0x0104000000918130L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_toplevelstmtsNoAlloc_in_xscopeNoAlloc1437 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_xscopeNoAlloc1439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIn_in_listCompr1466 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020100L});
    public static final BitSet FOLLOW_COLON_in_listCompr1469 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000001DC0000L});
    public static final BitSet FOLLOW_listiterable_in_listCompr1471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIn1503 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_idlist_in_forIn1505 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_IN_in_forIn1507 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_list_in_forIn1509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idlist1534 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_idlist1537 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_idlist1539 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_idlist_in_idlistOrEmpty1565 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_code_in_listiterable1586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_list1600 = new BitSet(new long[]{0x6000000000000200L,0x010400000090B130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_listitems_in_list1602 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_list1604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listitem_in_listitems1634 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_listitems1638 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909130L,0x0000000003DF0830L});
    public static final BitSet FOLLOW_listitem_in_listitems1640 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_listitems1645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_toplevelvalue_in_listitem1671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_code1689 = new BitSet(new long[]{0x0000000000000000L,0x0000000000188000L});
    public static final BitSet FOLLOW_attrs_in_code1691 = new BitSet(new long[]{0x0000000000000000L,0x0000000000108000L});
    public static final BitSet FOLLOW_proto_in_code1694 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_LBRACE_in_code1697 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009B83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codestmtlist_in_code1699 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_code1701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATTR_in_attrs1731 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080000L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_argdefs1775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefs1782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithNames_in_argdefs1789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1805 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1809 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_argdefWithType_in_argdefsWithTypes1811 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_argdefsWithTypes1815 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1844 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080340L});
    public static final BitSet FOLLOW_COMMA_in_argdefWithType1847 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_argdefWithType1849 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080340L});
    public static final BitSet FOLLOW_attrs_in_argdefWithType1853 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000300L});
    public static final BitSet FOLLOW_COLON_in_argdefWithType1857 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_argdefWithType1859 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_argdefWithType1864 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_argdefWithType1868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1903 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1907 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_argdefWithName_in_argdefsWithNames1909 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_argdefsWithNames1913 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_argdefWithName1935 = new BitSet(new long[]{0x0000000000000002L,0x0000000000080000L});
    public static final BitSet FOLLOW_attrs_in_argdefWithName1937 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_proto1962 = new BitSet(new long[]{0x0000000000000000L,0x0000000000600020L});
    public static final BitSet FOLLOW_argdefs_in_proto1964 = new BitSet(new long[]{0x0000000000000000L,0x0000000000600000L});
    public static final BitSet FOLLOW_xreturns_in_proto1966 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_proto1969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns2012 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_xreturns2014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ARROW_in_xreturns2031 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_NIL_in_xreturns2033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_argtuple2063 = new BitSet(new long[]{0x0000000000000200L,0x0000000001100160L,0x0000000003000000L});
    public static final BitSet FOLLOW_tupleargdefs_in_argtuple2065 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_argtuple2067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs2089 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_tupleargdefs2093 = new BitSet(new long[]{0x0000000000000200L,0x0000000001100160L,0x0000000003000000L});
    public static final BitSet FOLLOW_tupleargdef_in_tupleargdefs2095 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_type_in_tupleargdef2140 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_tupleargdef2153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nonArrayType_in_type2218 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_arraySuff_in_type2256 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_LBRACKET_in_type2311 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_type2313 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_type2317 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_type2319 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_RBRACKET_in_type2324 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_CARET_in_type2383 = new BitSet(new long[]{0x0000000000000002L,0x0000000002001000L});
    public static final BitSet FOLLOW_idExpr_in_nonArrayType2435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CODE_in_nonArrayType2453 = new BitSet(new long[]{0x0000000000000002L,0x0000000000100000L});
    public static final BitSet FOLLOW_proto_in_nonArrayType2455 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_data_in_nonArrayType2478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argtuple_in_nonArrayType2494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arraySuff2510 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_arraySuff2512 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_arraySuff2514 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arraySuff2526 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_arraySuff2528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist2544 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_codestmtlist2547 = new BitSet(new long[]{0x6001000000000202L,0x01061C00009A83B8L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmt_in_codestmtlist2549 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000008L});
    public static final BitSet FOLLOW_labelStmt_in_codeStmt2593 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt2595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_codeStmtExpr_in_codeStmt2616 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_codeStmtExpr2648 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_codeStmtExpr2671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_codeStmtExpr2688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_blockStmt_in_codeStmtExpr2721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_codeStmtExpr2743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_controlStmt_in_codeStmtExpr2769 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singleVarDecl_in_varDecl2792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tupleVarDecl_in_varDecl2796 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_singleVarDecl2808 = new BitSet(new long[]{0x0000000000000000L,0x00000000000801C0L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2822 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_singleVarDecl2825 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2864 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COLON_in_singleVarDecl2867 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_singleVarDecl2869 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_singleVarDecl2872 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl2907 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_singleVarDecl2909 = new BitSet(new long[]{0x0000000000000000L,0x00000000000801C0L});
    public static final BitSet FOLLOW_attrs_in_singleVarDecl2914 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_singleVarDecl2932 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909730L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_singleVarDecl2934 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2937 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl2940 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl2942 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_singleVarDecl3004 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_singleVarDecl3006 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_singleVarDecl3009 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909730L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_singleVarDecl3011 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl3014 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_singleVarDecl3017 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_singleVarDecl3019 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_idTuple_in_tupleVarDecl3108 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080180L});
    public static final BitSet FOLLOW_attrs_in_tupleVarDecl3110 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000180L});
    public static final BitSet FOLLOW_COLON_EQUALS_in_tupleVarDecl3125 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_tupleVarDecl3127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_tupleVarDecl3164 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_tupleVarDecl3166 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_tupleVarDecl3169 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_tupleVarDecl3171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3222 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_assignStmt3224 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignStmt3253 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_assignStmt3255 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3257 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3312 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt3315 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_assignStmt3317 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A40L});
    public static final BitSet FOLLOW_assignEqOp_in_assignStmt3321 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909730L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_PLUS_in_assignStmt3323 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3326 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_assignStmt3329 = new BitSet(new long[]{0x6000000000000200L,0x0104000000909330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignOrInitExpr_in_assignStmt3331 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_assignExpr_in_assignOrInitExpr3392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_assignOrInitExpr3396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_assignExpr3414 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_assignExpr3416 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr3418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_assignExpr3453 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_assignExpr3455 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_assignExpr3457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_assignExpr3491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_assignOp0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_assignEqOp3606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignOp_in_assignEqOp3610 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_initList3619 = new BitSet(new long[]{0x6000000000000200L,0x0104020000903120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initExpr_in_initList3622 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_COMMA_in_initList3625 = new BitSet(new long[]{0x6000000000000200L,0x0104020000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initExpr_in_initList3627 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_RBRACKET_in_initList3633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_initExpr3668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PERIOD_in_initExpr3731 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_initExpr3733 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_initExpr3735 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initElement_in_initExpr3739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_initExpr3804 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_initExpr3808 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_initExpr3810 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_initExpr3812 = new BitSet(new long[]{0x6000000000000200L,0x0104000000901120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_initElement_in_initExpr3816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_initExpr3853 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_initElement3867 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_initList_in_initElement3871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_doWhile_in_controlStmt3883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whileDo_in_controlStmt3887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_repeat_in_controlStmt3891 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forIter_in_controlStmt3895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_doWhile3904 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmtExpr_in_doWhile3906 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_WHILE_in_doWhile3908 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_doWhile3910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_whileDo3933 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_whileDo3935 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000100L});
    public static final BitSet FOLLOW_COLON_in_whileDo3938 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_DO_in_whileDo3940 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmtExpr_in_whileDo3943 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REPEAT_in_repeat3968 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_repeat3970 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000100L});
    public static final BitSet FOLLOW_COLON_in_repeat3973 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_DO_in_repeat3975 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmt_in_repeat3978 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forIter4008 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_forIds_in_forIter4010 = new BitSet(new long[]{0x0000000000000000L,0x0000C00000040000L});
    public static final BitSet FOLLOW_forMovement_in_forIter4012 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_IN_in_forIter4015 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_forIter4017 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000100L});
    public static final BitSet FOLLOW_COLON_in_forIter4020 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_DO_in_forIter4022 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009A83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codeStmt_in_forIter4025 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_forIds4062 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_AND_in_forIds4065 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_forIds4067 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_atId_in_forMovement4083 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stepping_in_forMovement4087 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BY_in_stepping4096 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_stepping4098 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_atId4115 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_atId4117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_breakStmt4145 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_breakStmt4147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATSIGN_in_labelStmt4175 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_labelStmt4177 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COLON_in_labelStmt4179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GOTO_in_gotoStmt4215 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000120L,0x0000000001000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_gotoStmt4217 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_IF_in_gotoStmt4220 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_gotoStmt4222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_blockStmt4257 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009B83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codestmtlist_in_blockStmt4259 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_blockStmt4261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tuple4284 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_tupleEntries_in_tuple4286 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_tuple4288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries4316 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_tupleEntries4319 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_tupleEntries4321 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_idTuple4340 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000120L,0x0000000001000000L});
    public static final BitSet FOLLOW_idTupleEntries_in_idTuple4342 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_idTuple4344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries4372 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_idTupleEntries4375 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000120L,0x0000000001000000L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idTupleEntries4377 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_condStar_in_rhsExpr4398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_arglist4419 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_arglist4423 = new BitSet(new long[]{0x6001000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_arg_in_arglist4425 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_arglist4429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_arg4478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_arg4511 = new BitSet(new long[]{0x6001000000000200L,0x01061C00009B83B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_codestmtlist_in_arg4513 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_arg4515 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_gotoStmt_in_arg4539 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cond_in_condStar4600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_condStar4611 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_ifExprs_in_condStar4613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_thenClause_in_ifExprs4632 = new BitSet(new long[]{0x0000000000000000L,0x0070000000000000L});
    public static final BitSet FOLLOW_elses_in_ifExprs4634 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignExpr_in_thenClause4656 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000100L});
    public static final BitSet FOLLOW_THEN_in_thenClause4662 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_COLON_in_thenClause4664 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_condStmtExpr_in_thenClause4669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elif_in_elses4697 = new BitSet(new long[]{0x0000000000000000L,0x0070000000000000L});
    public static final BitSet FOLLOW_elseClause_in_elses4700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELIF_in_elif4723 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_elif4727 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000100L});
    public static final BitSet FOLLOW_THEN_in_elif4730 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_COLON_in_elif4732 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_condStmtExpr_in_elif4737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ELSE_in_elseClause4763 = new BitSet(new long[]{0x6001000000000200L,0x0105000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_condStmtExpr_in_elseClause4765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FI_in_elseClause4792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arg_in_condStmtExpr4823 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_breakStmt_in_condStmtExpr4827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logor_in_cond4844 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_QUESTION_in_cond4861 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_logor_in_cond4865 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_COLON_in_cond4867 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_logor_in_cond4871 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_logand_in_logor4915 = new BitSet(new long[]{0x0000000000000002L,0x0080000000000000L});
    public static final BitSet FOLLOW_OR_in_logor4932 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_logand_in_logor4936 = new BitSet(new long[]{0x0000000000000002L,0x0080000000000000L});
    public static final BitSet FOLLOW_not_in_logand4967 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_AND_in_logand4983 = new BitSet(new long[]{0x6000000000000200L,0x0100000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_not_in_logand4987 = new BitSet(new long[]{0x0000000000000002L,0x0000200000000000L});
    public static final BitSet FOLLOW_comp_in_not5033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_not5049 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_comp_in_not5053 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bitor_in_comp5087 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPEQ_in_comp5120 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5124 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPNE_in_comp5146 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5150 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPLE_in_comp5172 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5176 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPGE_in_comp5201 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5205 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPULE_in_comp5230 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5234 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_COMPUGE_in_comp5259 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5263 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_LESS_in_comp5288 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5292 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_ULESS_in_comp5318 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5322 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_GREATER_in_comp5348 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5352 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_UGREATER_in_comp5377 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitor_in_comp5381 = new BitSet(new long[]{0x0000000000000002L,0xFE00000000000000L,0x0000000000000007L});
    public static final BitSet FOLLOW_bitxor_in_bitor5431 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_BAR_in_bitor5459 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitxor_in_bitor5463 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_bitand_in_bitxor5489 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_TILDE_in_bitxor5517 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_bitand_in_bitxor5521 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_shift_in_bitand5546 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_AMP_in_bitand5574 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_shift_in_bitand5578 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_factor_in_shift5605 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_LSHIFT_in_shift5639 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5643 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_RSHIFT_in_shift5672 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5676 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_URSHIFT_in_shift5704 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5708 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_CRSHIFT_in_shift5736 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5740 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_CLSHIFT_in_shift5768 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_factor_in_shift5772 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x00000000000007C0L});
    public static final BitSet FOLLOW_term_in_factor5814 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L,0x0000000000000800L});
    public static final BitSet FOLLOW_PLUS_in_factor5847 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_term_in_factor5851 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L,0x0000000000000800L});
    public static final BitSet FOLLOW_MINUS_in_factor5893 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_term_in_factor5897 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000400L,0x0000000000000800L});
    public static final BitSet FOLLOW_unary_in_term5942 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_STAR_in_term5986 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term5990 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_SLASH_in_term6026 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6030 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_REM_in_term6065 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6069 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_UDIV_in_term6104 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6108 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_UREM_in_term6143 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6147 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_MOD_in_term6182 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_term6186 = new BitSet(new long[]{0x0000003000000002L,0x0000000000000000L,0x000000000000F000L});
    public static final BitSet FOLLOW_MINUS_in_unary6259 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_unary6263 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_unary6283 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_unary6287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_unary6322 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_PLUSPLUS_in_unary6324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_unary6355 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_MINUSMINUS_in_unary6357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_unary6378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUSPLUS_in_unary6409 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_unary6413 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUSMINUS_in_unary6434 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_unary6438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AMP_in_unary6458 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_unary6460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SIZEOF_in_unary6501 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000001DC0000L});
    public static final BitSet FOLLOW_atom_in_unary6503 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TYPEOF_in_unary6527 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000001DC0000L});
    public static final BitSet FOLLOW_atom_in_unary6529 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idExpr_in_lhs6564 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_tuple_in_lhs6611 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_NUMBER_in_lhs6650 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_lhs6693 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_lhs6728 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_lhs6761 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_lhs6765 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_lhs6767 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_PERIOD_in_lhs6810 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_lhs6812 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_lhs6837 = new BitSet(new long[]{0x6001000000000200L,0x0104000000B08330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_arglist_in_lhs6839 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_lhs6841 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_arrayAccess_in_lhs6874 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CARET_in_lhs6899 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LBRACE_in_lhs6920 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100520L,0x0000000003000000L});
    public static final BitSet FOLLOW_PLUS_in_lhs6922 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_lhs6925 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_lhs6927 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_AS_in_lhs6968 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100520L,0x0000000003000000L});
    public static final BitSet FOLLOW_PLUS_in_lhs6970 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_lhs6973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_atom7022 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_FALSE_in_atom7065 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_TRUE_in_atom7107 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CHAR_LITERAL_in_atom7150 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_atom7185 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_NIL_in_atom7218 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_idExpr_in_atom7261 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_tuple_in_atom7308 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7357 = new BitSet(new long[]{0x6000000000000200L,0x01040000009881B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_varDecl_in_atom7361 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7363 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7392 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_atom7396 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7398 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_code_in_atom7427 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_PERIOD_in_atom7486 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_atom7488 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LPAREN_in_atom7513 = new BitSet(new long[]{0x6001000000000200L,0x0104000000B08330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_arglist_in_atom7515 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_atom7517 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_arrayAccess_in_atom7550 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_CARET_in_atom7575 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_LBRACE_in_atom7596 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100520L,0x0000000003000000L});
    public static final BitSet FOLLOW_PLUS_in_atom7598 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_atom7601 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_RBRACE_in_atom7603 = new BitSet(new long[]{0x0000000000000002L,0x0000020002109000L,0x0000000000200000L});
    public static final BitSet FOLLOW_AS_in_atom7644 = new BitSet(new long[]{0x0000000000000200L,0x0000000000100120L,0x0000000003000000L});
    public static final BitSet FOLLOW_type_in_atom7646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_arrayAccess7680 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_arrayAccess7682 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_COMMA_in_arrayAccess7685 = new BitSet(new long[]{0x6000000000000200L,0x0104000000908330L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_assignExpr_in_arrayAccess7687 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002040L});
    public static final BitSet FOLLOW_RBRACKET_in_arrayAccess7691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idOrScopeRef_in_idExpr7713 = new BitSet(new long[]{0x0000000000000002L,0x8000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_idExpr7729 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_idExpr7731 = new BitSet(new long[]{0x0000000000000002L,0x8000020000000000L});
    public static final BitSet FOLLOW_instantiation_in_idExpr7761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7792 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_namespaceRef7795 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7797 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_colons_in_namespaceRef7821 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7823 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_PERIOD_in_namespaceRef7826 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_namespaceRef7828 = new BitSet(new long[]{0x0000000000000002L,0x0000020000000000L});
    public static final BitSet FOLLOW_LESS_in_instantiation7857 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000003DC0002L});
    public static final BitSet FOLLOW_instanceExpr_in_instantiation7860 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L,0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_instantiation7863 = new BitSet(new long[]{0x0000000000000200L,0x0000000000900120L,0x0000000003DC0000L});
    public static final BitSet FOLLOW_instanceExpr_in_instantiation7865 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L,0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_instantiation7871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_instanceExpr7903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_instanceExpr7907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef7915 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_colons_in_idOrScopeRef7938 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_idOrScopeRef7940 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_colons7966 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000100L,0x0000000001000000L});
    public static final BitSet FOLLOW_DATA_in_data7983 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_LBRACE_in_data7985 = new BitSet(new long[]{0x6000000000000200L,0x01040000009981B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_fieldDecl_in_data7987 = new BitSet(new long[]{0x6000000000000200L,0x01040000009981B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_RBRACE_in_data7990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDecl_in_fieldDecl8009 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_SEMI_in_fieldDecl8011 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_defineStmt_in_fieldDecl8024 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FORWARD_in_fieldDecl8032 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_fieldDecl8034 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_COMMA_in_fieldDecl8037 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_fieldDecl8039 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000048L});
    public static final BitSet FOLLOW_SEMI_in_fieldDecl8043 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_fieldIdRef8065 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_fieldIdRef8068 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_fieldIdRef8070 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
    public static final BitSet FOLLOW_LBRACE_in_synpred1_Eulang646 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred2_Eulang1111 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_synpred2_Eulang1113 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred2_Eulang1115 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred3_Eulang1160 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
    public static final BitSet FOLLOW_EQUALS_COLON_in_synpred3_Eulang1162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_synpred4_Eulang1197 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_synpred4_Eulang1199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred5_Eulang1237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefsWithTypes_in_synpred7_Eulang1775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_argdefWithType_in_synpred8_Eulang1782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arraySuff_in_synpred9_Eulang2250 = new BitSet(new long[]{0x0000000000000002L,0x0000000000001000L});
    public static final BitSet FOLLOW_varDecl_in_synpred10_Eulang2643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignStmt_in_synpred11_Eulang2666 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_synpred12_Eulang2715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred13_Eulang3215 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred13_Eulang3217 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred14_Eulang3297 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_COMMA_in_synpred14_Eulang3300 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100120L,0x00000000011C0000L});
    public static final BitSet FOLLOW_lhs_in_synpred14_Eulang3302 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A40L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred14_Eulang3306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred15_Eulang3407 = new BitSet(new long[]{0x0000000000000000L,0x000001FFFC000A00L});
    public static final BitSet FOLLOW_assignEqOp_in_synpred15_Eulang3409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_idTuple_in_synpred16_Eulang3446 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_EQUALS_in_synpred16_Eulang3448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rhsExpr_in_synpred17_Eulang3661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred18_Eulang3793 = new BitSet(new long[]{0x6000000000000200L,0x0104000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_rhsExpr_in_synpred18_Eulang3797 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred18_Eulang3799 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred19_Eulang5886 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_term_in_synpred19_Eulang5888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_synpred20_Eulang5979 = new BitSet(new long[]{0x6000000000000200L,0x0000000000900120L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_unary_in_synpred20_Eulang5981 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred21_Eulang6313 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_PLUSPLUS_in_synpred21_Eulang6315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_lhs_in_synpred22_Eulang6346 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_MINUSMINUS_in_synpred22_Eulang6348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred23_Eulang6605 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred24_Eulang6868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tuple_in_synpred25_Eulang7302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred26_Eulang7349 = new BitSet(new long[]{0x6000000000000200L,0x01040000009881B0L,0x0000000001DF0830L});
    public static final BitSet FOLLOW_varDecl_in_synpred26_Eulang7351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred27_Eulang7544 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_instantiation_in_synpred28_Eulang7755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_synpred29_Eulang7903 = new BitSet(new long[]{0x0000000000000002L});

}