package boxpeeking.status.instrument;

import boxpeeking.status.Status;
import boxpeeking.status.StatusManager;
import boxpeeking.instrument.Instrumentor;
import java.util.*;
import java.lang.annotation.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

public class StatusInstrumentor implements Instrumentor
{
	public void instrumentClass (ClassGen classGen, Annotation a)
	{
		// intentionally do nothing...
	}

	public void instrumentMethod (ClassGen cg, MethodGen mg, Annotation a)
	{
		Status status = (Status)a;

		String methodSignature = getSignature(mg);

		InstructionList il = mg.getInstructionList();
		InstructionFactory ifact = new InstructionFactory(cg);

		InstructionHandle start = il.getStart();
		InstructionHandle end = il.getEnd();

		Collection<JSR> jsrs = new ArrayList<JSR>();
		for (Iterator i = il.iterator(); i.hasNext(); ) {
			InstructionHandle ih = (InstructionHandle)i.next();

			if (ih.getInstruction() instanceof RETURN) {
				JSR jsr2 = new JSR(null);
				jsrs.add(jsr2);
				InstructionHandle n = il.insert(ih, jsr2);
							
				il.redirectBranches(ih, n);
			}
		}

		InstructionList il2 = new InstructionList();

		il2.append(ifact.createConstant(status.value()));
		il2.append(ifact.createInvoke(StatusManager.class.getName(), "push", Type.VOID, new Type[] { Type.STRING }, Constants.INVOKESTATIC));
		il.insert(il2);

		InstructionHandle h1 = il.append(new ASTORE(0));
		JSR jsr = new JSR(null);
		jsrs.add(jsr);
		il.append(jsr);
		InstructionHandle h2 = il.append(new ALOAD(0));
		il.append(new ATHROW());
		InstructionHandle handler = il.append(new ASTORE(1));
		il.append(ifact.createInvoke(StatusManager.class.getName(), "pop", Type.VOID, new Type[0], Constants.INVOKESTATIC));
		il.append(new RET(1));

		for (JSR j : jsrs) {
			j.setTarget(handler);
		}

		mg.addExceptionHandler(start, end, h1, null);
//  		mg.addExceptionHandler(h1, h2, h1, null);

		il.setPositions(true);
	}

	public static String getSignature (MethodGen mg)
	{
  	    StringBuffer sb = new StringBuffer();
  	    sb.append(mg.getClassName());
		sb.append(".");
  	    sb.append(mg.getName());
		sb.append(mg.getSignature());
  	    return sb.toString();
	}
}
