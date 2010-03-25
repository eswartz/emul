/**
 * 
 */
package v9t9.tools.tinyc.frontend;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.NullCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.eclipse.core.runtime.CoreException;

/**
 * @author ejs
 * 
 */
public class Parser {
	public Parser() {

	}

	public IASTTranslationUnit parse(String text) {

		CodeReader reader = new CodeReader(text.toCharArray()); //$NON-NLS-1$ //$NON-NLS-2$

		IScannerInfo scannerInfo = new ScannerInfo(); // creates an empty
		// scanner info
		IScanner scanner = new CPreprocessor(reader, scannerInfo,
				ParserLanguage.CPP, new NullLogService(),
				GCCScannerExtensionConfiguration.getInstance(),
				NullCodeReaderFactory.getInstance());
		ISourceCodeParser parser = new GNUCPPSourceParser(scanner,
				ParserMode.COMPLETE_PARSE, new NullLogService(),
				GPPParserExtensionConfiguration.getInstance(), null);
		IASTTranslationUnit ast = parser.parse();
		return ast;
	}

}
