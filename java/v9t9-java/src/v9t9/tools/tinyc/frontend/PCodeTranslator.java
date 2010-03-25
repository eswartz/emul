/**
 * 
 */
package v9t9.tools.tinyc.frontend;

import org.apache.batik.css.parser.ParseException;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.common.Block;

/**
 * @author ejs
 *
 */
public class PCodeTranslator extends ASTVisitor {

	private PCodeUnit unit;
	private int vrNum;
	private PCodeFunction function;
	private Block block;

	public PCodeTranslator() {
		shouldVisitAmbiguousNodes = true;
		shouldVisitArrayModifiers = true;
		shouldVisitBaseSpecifiers = true;
		shouldVisitDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDesignators = true;
		shouldVisitEnumerators = true;
		shouldVisitExpressions = true;
		shouldVisitImplicitNameAlternates = true;
		shouldVisitImplicitNames = true;
		shouldVisitInitializers = true;
		shouldVisitNames = true;
		shouldVisitNamespaces = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitPointerOperators = true;
		shouldVisitProblems = true;
		shouldVisitStatements = true;
		shouldVisitTemplateParameters = true;
		shouldVisitTranslationUnit = true;
		shouldVisitTypeIds = true;
		
	}
	
	public void generateHL(IASTTranslationUnit tu) {
		unit = new PCodeUnit();
		tu.accept(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTTranslationUnit)
	 */
	@Override
	public int visit(IASTTranslationUnit tu) {
		
		return super.visit(tu);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTDeclaration)
	 */
	@Override
	public int visit(IASTDeclaration declaration) {
		if (declaration instanceof IASTFunctionDefinition) {
			translateFunction((IASTFunctionDefinition) declaration);
		} else {
		}
		return super.visit(declaration);
	}
	
	/**
	 * @param declaration
	 */
	private void translateFunction(IASTFunctionDefinition declaration) {
		function = unit.addFunction(declaration.getDeclarator().getName().toString());
		translateStatement(declaration.getBody());
	}

	/**
	 * @param body
	 */
	private void translateStatement(IASTStatement stmt) {
		if (block == null)
			block = new Block(null); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ASTVisitor#visit(org.eclipse.cdt.core.dom.ast.IASTExpression)
	 */
	@Override
	public int visit(IASTExpression expression) {
		if (expression instanceof IASTLiteralExpression) {
			translateLiteral((IASTLiteralExpression) expression);
		} else {
			assert false;
		}
		return super.visit(expression);
	}

	/**
	 * @param expression
	 */
	private void translateLiteral(IASTLiteralExpression expression) {
		switch (expression.getKind()) {
		case IASTLiteralExpression.lk_integer_constant:
			generate(InstructionTable.Ili, reg(), imm(integer(expression.getValue())));
			break;
		}
		
	}

	/**
	 * @param ili
	 * @param op1
	 * @param op2
	 */
	private HLInstruction generate(int opcode, AssemblerOperand op1, AssemblerOperand op2) {
		HLInstruction inst = new HLInstruction();
		inst.setOp1(op1);
		inst.setOp2(op2);
		addInst(inst);
		return inst;
	}

	/**
	 * @param inst
	 */
	private void addInst(HLInstruction inst) {
		block.addInst(inst);
	}

	/**
	 * @param value
	 * @return
	 */
	private int integer(char[] value) {
		try {
			return Integer.parseInt(new String(value));
		} catch (NumberFormatException e) {
			throw new ParseException(e);
		}
	}

	/**
	 * @param value
	 * @return
	 */
	private NumberOperand imm(int value) {
		return new NumberOperand(value);
	}

	/**
	 * @return
	 */
	private RegisterOperand reg() {
		return new RegisterOperand(new NumberOperand(vrNum++));
	}
}
