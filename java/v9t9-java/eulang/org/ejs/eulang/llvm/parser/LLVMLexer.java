// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g 2010-06-19 12:57:42

package org.ejs.eulang.llvm.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class LLVMLexer extends Lexer {
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

      class EOFException extends RecognitionException {
        String message;
        EOFException(IntStream input, String message) {
           super(input);
           this.message = message;
        }
        public String toString() { return message; } 
      }
      


    // delegates
    // delegators

    public LLVMLexer() {;} 
    public LLVMLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public LLVMLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g"; }

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:22:7: ( 'target' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:22:9: 'target'
            {
            match("target"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:23:7: ( 'datalayout' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:23:9: 'datalayout'
            {
            match("datalayout"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:66:8: ( '=' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:66:10: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:8: ( '0' .. '9' ( NUMSUFFIX ( '.' NUMSUFFIX )? ) )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:10: '0' .. '9' ( NUMSUFFIX ( '.' NUMSUFFIX )? )
            {
            matchRange('0','9'); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:19: ( NUMSUFFIX ( '.' NUMSUFFIX )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:20: NUMSUFFIX ( '.' NUMSUFFIX )?
            {
            mNUMSUFFIX(); 
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:30: ( '.' NUMSUFFIX )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='.') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:71:32: '.' NUMSUFFIX
                    {
                    match('.'); 
                    mNUMSUFFIX(); 

                    }
                    break;

            }


            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "NAMED_ID"
    public final void mNAMED_ID() throws RecognitionException {
        try {
            int _type = NAMED_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:77:10: ( ( '%' | '@' ) NAME_SUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:77:12: ( '%' | '@' ) NAME_SUFFIX
            {
            if ( input.LA(1)=='%'||input.LA(1)=='@' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            mNAME_SUFFIX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAMED_ID"

    // $ANTLR start "UNNAMED_ID"
    public final void mUNNAMED_ID() throws RecognitionException {
        try {
            int _type = UNNAMED_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:78:12: ( ( '%' | '@' ) NUMBER_SUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:78:14: ( '%' | '@' ) NUMBER_SUFFIX
            {
            if ( input.LA(1)=='%'||input.LA(1)=='@' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            mNUMBER_SUFFIX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNNAMED_ID"

    // $ANTLR start "QUOTED_ID"
    public final void mQUOTED_ID() throws RecognitionException {
        try {
            int _type = QUOTED_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:79:11: ( ( '%' | '@' ) STRING_LITERAL_SUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:79:13: ( '%' | '@' ) STRING_LITERAL_SUFFIX
            {
            if ( input.LA(1)=='%'||input.LA(1)=='@' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            mSTRING_LITERAL_SUFFIX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUOTED_ID"

    // $ANTLR start "NAME_SUFFIX"
    public final void mNAME_SUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:81:22: ( ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '0' .. '9' )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:81:24: ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '0' .. '9' )*
            {
            if ( input.LA(1)=='$'||input.LA(1)=='.'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:81:66: ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '.' | '0' .. '9' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='$'||LA2_0=='.'||(LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            	    {
            	    if ( input.LA(1)=='$'||input.LA(1)=='.'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "NAME_SUFFIX"

    // $ANTLR start "NUMBER_SUFFIX"
    public final void mNUMBER_SUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:82:24: ( ( '0' .. '9' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:82:26: ( '0' .. '9' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:82:26: ( '0' .. '9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:82:27: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "NUMBER_SUFFIX"

    // $ANTLR start "NUMSUFFIX"
    public final void mNUMSUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:83:20: ( ( '0' .. '9' | 'A' .. 'Z' | 'a' .. 'z' )* )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:83:22: ( '0' .. '9' | 'A' .. 'Z' | 'a' .. 'z' )*
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:83:22: ( '0' .. '9' | 'A' .. 'Z' | 'a' .. 'z' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='Z')||(LA4_0>='a' && LA4_0<='z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "NUMSUFFIX"

    // $ANTLR start "CHAR_LITERAL"
    public final void mCHAR_LITERAL() throws RecognitionException {
        try {
            int _type = CHAR_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:89:14: ( '\\'' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:89:16: '\\''
            {
            match('\''); 

              while (true) {
            		 int ch = input.LA(1);
            		 if (ch == '\\') {
            		    input.consume();
            		    input.consume();
            		 } else if (ch == -1) {
                    match('\'');
            		 } else if (ch != '\'') {
            		    input.consume();
            		 } else {
            		    match('\'');
            		    break;
            		 }
              }



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CHAR_LITERAL"

    // $ANTLR start "STRING_LITERAL"
    public final void mSTRING_LITERAL() throws RecognitionException {
        try {
            int _type = STRING_LITERAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:107:16: ( STRING_LITERAL_SUFFIX )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:107:18: STRING_LITERAL_SUFFIX
            {
            mSTRING_LITERAL_SUFFIX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL"

    // $ANTLR start "STRING_LITERAL_SUFFIX"
    public final void mSTRING_LITERAL_SUFFIX() throws RecognitionException {
        try {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:109:31: ( '\"' )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:109:33: '\"'
            {
            match('\"'); 

              while (true) {
            		 int ch = input.LA(1);
            		 if (ch == '\\') {
            		    input.consume();
            		    input.consume();
                 } else if (ch == -1) {
                    match('\"');
            		 } else if (ch != '\"') {
            		    input.consume();
            		 } else {
            		    match('\"');
            		    break;
            		 }
              }


            }

        }
        finally {
        }
    }
    // $ANTLR end "STRING_LITERAL_SUFFIX"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:8: ( ( ( '\\r' )? '\\n' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:10: ( ( '\\r' )? '\\n' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:10: ( ( '\\r' )? '\\n' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\n'||LA6_0=='\r') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:11: ( '\\r' )? '\\n'
            	    {
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:11: ( '\\r' )?
            	    int alt5=2;
            	    int LA5_0 = input.LA(1);

            	    if ( (LA5_0=='\r') ) {
            	        alt5=1;
            	    }
            	    switch (alt5) {
            	        case 1 :
            	            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:128:11: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEWLINE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:129:5: ( ( ' ' | '\\t' )+ )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:129:9: ( ' ' | '\\t' )+
            {
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:129:9: ( ' ' | '\\t' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='\t'||LA7_0==' ') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "SINGLE_COMMENT"
    public final void mSINGLE_COMMENT() throws RecognitionException {
        try {
            int _type = SINGLE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:133:15: ( '//' (~ ( '\\r' | '\\n' ) )* NEWLINE )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:133:17: '//' (~ ( '\\r' | '\\n' ) )* NEWLINE
            {
            match("//"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:133:22: (~ ( '\\r' | '\\n' ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='\u0000' && LA8_0<='\t')||(LA8_0>='\u000B' && LA8_0<='\f')||(LA8_0>='\u000E' && LA8_0<='\uFFFF')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:133:22: ~ ( '\\r' | '\\n' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            mNEWLINE(); 
             skip(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SINGLE_COMMENT"

    // $ANTLR start "MULTI_COMMENT"
    public final void mMULTI_COMMENT() throws RecognitionException {
        try {
            int _type = MULTI_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:3: ( '/*' ( . )* '*/' ( NEWLINE )? )
            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:5: '/*' ( . )* '*/' ( NEWLINE )?
            {
            match("/*"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:10: ( . )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0=='*') ) {
                    int LA9_1 = input.LA(2);

                    if ( (LA9_1=='/') ) {
                        alt9=2;
                    }
                    else if ( ((LA9_1>='\u0000' && LA9_1<='.')||(LA9_1>='0' && LA9_1<='\uFFFF')) ) {
                        alt9=1;
                    }


                }
                else if ( ((LA9_0>='\u0000' && LA9_0<=')')||(LA9_0>='+' && LA9_0<='\uFFFF')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:10: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match("*/"); 

            // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:18: ( NEWLINE )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='\n'||LA10_0=='\r') ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:138:18: NEWLINE
                    {
                    mNEWLINE(); 

                    }
                    break;

            }

             skip(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULTI_COMMENT"

    public void mTokens() throws RecognitionException {
        // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:8: ( T__19 | T__20 | EQUALS | NUMBER | NAMED_ID | UNNAMED_ID | QUOTED_ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT )
        int alt11=13;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:10: T__19
                {
                mT__19(); 

                }
                break;
            case 2 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:16: T__20
                {
                mT__20(); 

                }
                break;
            case 3 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:22: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 4 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:29: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 5 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:36: NAMED_ID
                {
                mNAMED_ID(); 

                }
                break;
            case 6 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:45: UNNAMED_ID
                {
                mUNNAMED_ID(); 

                }
                break;
            case 7 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:56: QUOTED_ID
                {
                mQUOTED_ID(); 

                }
                break;
            case 8 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:66: CHAR_LITERAL
                {
                mCHAR_LITERAL(); 

                }
                break;
            case 9 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:79: STRING_LITERAL
                {
                mSTRING_LITERAL(); 

                }
                break;
            case 10 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:94: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 11 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:102: WS
                {
                mWS(); 

                }
                break;
            case 12 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:105: SINGLE_COMMENT
                {
                mSINGLE_COMMENT(); 

                }
                break;
            case 13 :
                // /home/ejs/devel/emul/java/v9t9-java/eulang/org/ejs/eulang/llvm/parser/LLVM.g:1:120: MULTI_COMMENT
                {
                mMULTI_COMMENT(); 

                }
                break;

        }

    }


    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA11_eotS =
        "\20\uffff";
    static final String DFA11_eofS =
        "\20\uffff";
    static final String DFA11_minS =
        "\1\11\4\uffff\1\42\4\uffff\1\52\5\uffff";
    static final String DFA11_maxS =
        "\1\164\4\uffff\1\172\4\uffff\1\57\5\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\uffff\1\10\1\11\1\12\1\13\1\uffff\1\6"+
        "\1\7\1\5\1\14\1\15";
    static final String DFA11_specialS =
        "\20\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\11\1\10\2\uffff\1\10\22\uffff\1\11\1\uffff\1\7\2\uffff\1"+
            "\5\1\uffff\1\6\7\uffff\1\12\12\4\3\uffff\1\3\2\uffff\1\5\43"+
            "\uffff\1\2\17\uffff\1\1",
            "",
            "",
            "",
            "",
            "\1\14\1\uffff\1\15\11\uffff\1\15\1\uffff\12\13\7\uffff\32\15"+
            "\4\uffff\1\15\1\uffff\32\15",
            "",
            "",
            "",
            "",
            "\1\17\4\uffff\1\16",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__19 | T__20 | EQUALS | NUMBER | NAMED_ID | UNNAMED_ID | QUOTED_ID | CHAR_LITERAL | STRING_LITERAL | NEWLINE | WS | SINGLE_COMMENT | MULTI_COMMENT );";
        }
    }
 

}