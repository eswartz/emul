See end for TODO.

Example DOM layout:

   123456789012345678901234
1: STRUCT FOO WORD {
2: 	BUF<10> field;
3: 	LONG values[4] = { 1 };
4:  SRLINK next;
5: }

IAstStructDeclaration[1:1 - 5:2] {
	getStructName() = IAstName [1:8 - 1:11] {
		"FOO"
	}
	getMembers() = IAstMemberDeclaration[] = {
		0: IAstMemberDeclaration [2:2 - 2:16] {
			getMemberType() = IAstStringDeclaration [2:2 - 2:9] {
				getLengthLimit() = IAstLiteralExpression [2:6 - 2:8] {
					getKind() = K_INTEGER
					getValue() = "10"
				}
				getLengthPrefixDeclarator() = null
				getKind() = K_BUF
			}
			getMemberName() = IAstName [2:10 - 2:15] {
				"field"
			}
			getInitializer() = null
			getArrayDeclarator() = null
		}
		1: IAstMemberDeclaration [3:2 - 3:25] {
			getMemberType() = IAstSimpleDeclaration [3:2 - 3:6] {
				getKind() = K_LONG
			}
			getMemberName() = IAstName [3:7 - 3:13] {
				"values"
			}
			getInitializer() = IAstExpressionList [3:19 - 3:24] {
				getExpressions() = {
					0: IAstLiteralExpression [3:21 - 3:22] { 
						getKind() = K_INTEGER
						getValue() = "1"
					}
				}
			}
			getArrayDeclarator() = IAstArrayDeclarator [3:13 - 3:16] {
				getArraySize() = IAstLiteralExpression [3:14 - 3:15] {
					getKind() = K_INTEGER
					getValue() = "4"
				}
			}
		}
		2: IAstMemberDeclaration [4:2 - 4:14] {
			getMemberType() = IAstSimpleDeclaration [4:2 - 4:8] {
				getKind() = K_SRLINK
			}
			getMemberName() = IAstName [4:9 - 4:13] {
				"next"
			}
			getInitializer() = null
			getArrayDeclarator() = null
		}
	}
	getLengthPrefixDeclarator() = IAstLengthPrefixDeclarator [1:12 - 1:16] {
		getLengthPrefix() = K_WORD_PREFIXED
	}
}

================================================================

   123456789012345678901
6: RESOURCE FOO r_foo {
7: 	values = {1,2,3}
8:  next = FOO {
9:    field="last"
10:}
11:}

IAstResourceDefinition [6:1 - 11:2] {
	getName() = IAstName [6:14 - 6:19] {
		"r_foo"
	}
	getStructType() => IAstStructDeclaration for "FOO"
	getInitializers() = IAstInitializerList [7:2 - 11:2] {
		0: IAstMemberInitializer [7:2 - 7:18] {
				getMember() => IMemberDeclaration("foo")
				getMemberExpression() = IAstIdExpression [7:2 - 7:8] = {
					getName() => "foo"
				}
				getExpression() = IAstExpressionList [7:12 - 7:17] {
					0: IAstLiteralExpression [7:12 - 7:13] {
						getKind() = K_INTEGER
						getValue() = "1"
					}
					1: IAstLiteralExpression [7:14 - 7:15] {
						getKind() = K_INTEGER
						getValue() = "2"
					}
					2: IAstLiteralExpression [7:16 - 7:17] {
						getKind() = K_INTEGER
						getValue() = "3"
					}
				}
			}
		}
		1: IAstMemberInitializer [8:2 - 10:2] {
			getMember() => "field"
			getMemberExpression() = IAstIdExpression [8:2 - 8:6] = {
				getName() => "field"
			}
			getExpression() = IAstResourceExpression [8:9 - 10:2] {
				getStructType() => IAstStructDeclaration for "FOO"
				getInitializers() = IAstInitializerList [9:4 - 9:16] {
					0: IAstLiteralExpression [9:10 - 9:16] {
						getKind() = K_STRING
						getValue() = "last"
					}
				}
			}
		}
	}
	
}

=========================================
   123456789012345
1: enum Boolean {
2: 	false = 0,
3: 	true
4: };

IAstEnumDeclaration [1:1 - 4:3] {
	getName() = "Boolean"
	getEnumerators = {
	0: IAstEnumDeclaration [2:2 - 2:12] {
		getName() = "false"
		getInitializerExpression() = IAstInitializerExpression [2:8 - 2:11] {
			getExpression() = IAstLiteralExpression [2:10 - 2:11] {
				getKind() = K_INTEGER
				getValue() = "0"
			}
		}
		getValue() = 0
	}
	1: IAstEnumDeclaration [3:2 - 3:6] {
		getName() = "true"
		getInitializerExpression() = null
		getValue() = 1
	}
}

=================

RSS DOM <--> preprocessor DOM mapping
            1         2
   12345678901234567890123
1: #include "foo.loc"
2: #include <eikon.rh>
3:
4: RESOURCE TYPE r_foo {
5: 	text = STRING_VALUE;
6: }

looks to the tokenizer like:

RESOURCE TYPE r_foo { text = "hi there"; }

The translation unit (IAstTranslationUnit) provides 

getSourceFile() = ITrackedSourceFile {
	getPreprocessorNodes() = {
		0: IAstPreprocessorIncludeDirective [1:1 - 1:19] {
			getFilename() = "foo.loc"
			isUserPath() = true
			getFile() = ILocalizedStringFile {
				getLocalizedStringBundle() = {
					getLocalizedStringCollections = {
						0: ILocalizedStringTable ...
					}
				}
				getFile() = File("path/to/foo.loc")
				getIncludePath() = File("path/to")
			}
		}
		1: IAstPreprocessorIncludeDirective [2:1 - 2:20] {
			getFilename() = "eikon.rh"
			isUserPath() = false
			getFile() = ISourceFile {
				getFile() = File("path/to/eikon.rh")
				getIncludePath() = File("path/to")
			}
		}
		2: IAstPreprocessorText [3:1 - 5:9] {
		}
		3: IAstPreprocessorMacroExpression [5:9 - 5:21] {
			... see below ...
		}
		4: IAstPreprocessorText [5:21 - 6:2] {
		}
	}
	
	getMacroExpressions() = {
		0: IAstPreprocessorMacroExpression [5:9 - 5:21] {
			getMacro() = IObjectStyleMacro {
				getName() = "STRING_VALUE"
			}
			getParameters() = null
			getAstNodesProduced() = {
				0: IAstLiteralExpression [5:9 - 5:21] {
					getKind() = K_STRING
					getValue() = "hi there"
				}
			}
		}
	}
}

getIncludeFiles() = {
	0: ILocalizedStringFile = {
		getFile() = "foo.loc"
		getText() = "..."
		getCharset() = "CP1252"
		getTopLevelNodes() = {}
		...
	}
	1: ISourceFile = {
		getFile() = "eikon.rh"
		getText() = "..."
		getCharset() = "CP1252"
	}
}

getLocalizedStringFile() = ILocalizedStringFile {
	getFile() = "foo.loc"
	getLocalizedStringBundle() = ILocalizedStringBundle {
		getLocalizedStringCollections() = {
			0: ...
			1: ...
			2: ...
		}
	}
}

getResourceDefinitions() =  {
	0: IAstResourceDefinition [4:1 - 6:2] {
		...
	}
}

=========================
TODO:

* RSS reading / RSS editor 
  -- problem nodes to describe unparseable nodes for
  representation in the Problems pane (use CDT IASTProblem?)
  (N.B.: we can still write out all the text, even problems,
  without this node, since IAstPreprocessorTextNode will cover
  all of it)
  
=================

-- Clear up include file story... just who owns their contents?
	-- Um, IAstCppStyleSourceFile can own multiple IAstCppStyleSourceFiles.
	But usually, only one instance actually has any contents due to
	include guards.  So, the #includer owns the contents.  There
	shouldn't be duplicate decls/defs, even if the same ISourceFile
	is represented so it's no problem for the ITranslationUnit
	to own multiple copies of ISourceFile via distinct IAstCppStyleSourceFiles.
	
-- Test that last version of macro is retained
	
-- preprocessor stuff

-- toString() not nearly implemented

-- test preprocessor directives

===

-- scope of toplevel nodes is ITranslationUnit

-- not testing completely whether original text is retained: this
is really a milestone 7 task (two-way RSS)

=======================

IAstPreprocessorConstantExpression
IAstPreprocessorElifDirective
IAstPreprocessorElseDirective
IAstPreprocessorEndifDirective
IAstPreprocessorIfStyleDirective
IAstPreprocessorTestDirective
IAstPreprocessorUnknownDirective
IFunctionStyleMacro
IObjectStyleMacro

