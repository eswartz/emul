// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g 2010-06-19 12:57:42

package org.ejs.eulang.llvm.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class LLVMParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "EQUALS", "STRING_LITERAL", "NAMED_ID", "UNNAMED_ID", "QUOTED_ID", "NUMSUFFIX", "NUMBER", "NAME_SUFFIX", "NUMBER_SUFFIX", "STRING_LITERAL_SUFFIX", "CHAR_LITERAL", "NEWLINE", "WS", "SINGLE_COMMENT", "MULTI_COMMENT", "'target'", "'datalayout'"
    };
    public static final int STRING_LITERAL_SUFFIX=13;
    public static final int NUMBER_SUFFIX=12;
    public static final int T__20=20;
    public static final int MULTI_COMMENT=18;
    public static final int NUMBER=10;
    public static final int EQUALS=4;
    public static final int NAMED_ID=6;
    public static final int EOF=-1;
    public static final int QUOTED_ID=8;
    public static final int NUMSUFFIX=9;
    public static final int T__19=19;
    public static final int STRING_LITERAL=5;
    public static final int WS=16;
    public static final int NAME_SUFFIX=11;
    public static final int NEWLINE=15;
    public static final int CHAR_LITERAL=14;
    public static final int SINGLE_COMMENT=17;
    public static final int UNNAMED_ID=7;

    // delegates
    // delegators


        public LLVMParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public LLVMParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return LLVMParser.tokenNames; }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g"; }


        public String getTokenErrorDisplay(Token t) {
            return '\'' + t.getText() + '\'';
        }

        LLParserHelper helper;    
        public LLVMParser(TokenStream input, LLParserHelper helper) {
            this(input);
            this.helper = helper;
        }
      


    public static class prog_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prog"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:43:1: prog : toplevelstmts EOF ;
    public final LLVMParser.prog_return prog() throws RecognitionException {
        LLVMParser.prog_return retval = new LLVMParser.prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token EOF2=null;
        LLVMParser.toplevelstmts_return toplevelstmts1 = null;


        CommonTree EOF2_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:43:5: ( toplevelstmts EOF )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:43:9: toplevelstmts EOF
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_toplevelstmts_in_prog69);
            toplevelstmts1=toplevelstmts();

            state._fsp--;

            adaptor.addChild(root_0, toplevelstmts1.getTree());
            EOF2=(Token)match(input,EOF,FOLLOW_EOF_in_prog71); 

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
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:46:1: toplevelstmts : ( directive )* ;
    public final LLVMParser.toplevelstmts_return toplevelstmts() throws RecognitionException {
        LLVMParser.toplevelstmts_return retval = new LLVMParser.toplevelstmts_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.directive_return directive3 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:46:14: ( ( directive )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:46:17: ( directive )*
            {
            root_0 = (CommonTree)adaptor.nil();

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:46:17: ( directive )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==19) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:46:17: directive
            	    {
            	    pushFollow(FOLLOW_directive_in_toplevelstmts101);
            	    directive3=directive();

            	    state._fsp--;

            	    adaptor.addChild(root_0, directive3.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
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
    // $ANTLR end "toplevelstmts"

    public static class directive_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "directive"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:1: directive : targetDataLayoutDirective ;
    public final LLVMParser.directive_return directive() throws RecognitionException {
        LLVMParser.directive_return retval = new LLVMParser.directive_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        LLVMParser.targetDataLayoutDirective_return targetDataLayoutDirective4 = null;



        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:11: ( targetDataLayoutDirective )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:49:13: targetDataLayoutDirective
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_targetDataLayoutDirective_in_directive116);
            targetDataLayoutDirective4=targetDataLayoutDirective();

            state._fsp--;

            adaptor.addChild(root_0, targetDataLayoutDirective4.getTree());

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
    // $ANTLR end "directive"

    public static class targetDataLayoutDirective_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "targetDataLayoutDirective"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:57:1: targetDataLayoutDirective : 'target' 'datalayout' EQUALS s= STRING_LITERAL ;
    public final LLVMParser.targetDataLayoutDirective_return targetDataLayoutDirective() throws RecognitionException {
        LLVMParser.targetDataLayoutDirective_return retval = new LLVMParser.targetDataLayoutDirective_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token s=null;
        Token string_literal5=null;
        Token string_literal6=null;
        Token EQUALS7=null;

        CommonTree s_tree=null;
        CommonTree string_literal5_tree=null;
        CommonTree string_literal6_tree=null;
        CommonTree EQUALS7_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:57:27: ( 'target' 'datalayout' EQUALS s= STRING_LITERAL )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:57:29: 'target' 'datalayout' EQUALS s= STRING_LITERAL
            {
            root_0 = (CommonTree)adaptor.nil();

            string_literal5=(Token)match(input,19,FOLLOW_19_in_targetDataLayoutDirective145); 
            string_literal5_tree = (CommonTree)adaptor.create(string_literal5);
            adaptor.addChild(root_0, string_literal5_tree);

            string_literal6=(Token)match(input,20,FOLLOW_20_in_targetDataLayoutDirective147); 
            string_literal6_tree = (CommonTree)adaptor.create(string_literal6);
            adaptor.addChild(root_0, string_literal6_tree);

            EQUALS7=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_targetDataLayoutDirective149); 
            EQUALS7_tree = (CommonTree)adaptor.create(EQUALS7);
            adaptor.addChild(root_0, EQUALS7_tree);

            s=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_targetDataLayoutDirective153); 
            s_tree = (CommonTree)adaptor.create(s);
            adaptor.addChild(root_0, s_tree);

             helper.addTargetDataLayoutDirective((s!=null?s.getText():null)); 

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
    // $ANTLR end "targetDataLayoutDirective"

    public static class identifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "identifier"
    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:61:1: identifier : ( NAMED_ID | UNNAMED_ID | QUOTED_ID );
    public final LLVMParser.identifier_return identifier() throws RecognitionException {
        LLVMParser.identifier_return retval = new LLVMParser.identifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set8=null;

        CommonTree set8_tree=null;

        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:61:12: ( NAMED_ID | UNNAMED_ID | QUOTED_ID )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set8=(Token)input.LT(1);
            if ( (input.LA(1)>=NAMED_ID && input.LA(1)<=QUOTED_ID) ) {
                input.consume();
                adaptor.addChild(root_0, (CommonTree)adaptor.create(set8));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
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
    // $ANTLR end "identifier"

    // Delegated rules


 

    public static final BitSet FOLLOW_toplevelstmts_in_prog69 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_prog71 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_directive_in_toplevelstmts101 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_targetDataLayoutDirective_in_directive116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_targetDataLayoutDirective145 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_targetDataLayoutDirective147 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EQUALS_in_targetDataLayoutDirective149 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_targetDataLayoutDirective153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_identifier0 = new BitSet(new long[]{0x0000000000000002L});

}
