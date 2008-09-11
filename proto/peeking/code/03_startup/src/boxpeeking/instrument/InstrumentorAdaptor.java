package boxpeeking.instrument;

import java.util.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.lang.annotation.*;
import java.lang.instrument.*;
import java.io.*;
import java.security.ProtectionDomain;
import boxpeeking.instrument.bcel.*;

public class InstrumentorAdaptor implements ClassFileTransformer
{
	static {
		Attribute.addAttributeReader("RuntimeVisibleAnnotations", new AnnotationReader());
		Attribute.addAttributeReader("RuntimeInvisibleAnnotations", new AnnotationReader());
	};

	public static void premain (String className, Instrumentation i)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Class instClass = Class.forName(className);
		Instrumentor inst = (Instrumentor)instClass.newInstance();
		i.addTransformer(new InstrumentorAdaptor(inst));
	}

	private Instrumentor instrumentor;

	public InstrumentorAdaptor (Instrumentor instrumentor)
	{
		this.instrumentor = instrumentor;
	}

	public byte[] transform (ClassLoader cl, String className, Class classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
	{
		try {
			ClassParser cp = new ClassParser(new ByteArrayInputStream(classfileBuffer), className + ".java");
			JavaClass jc = cp.parse();

			ClassGen cg = new ClassGen(jc);

			for (Annotation an : getAnnotations(jc.getAttributes())) {
				instrumentor.instrumentClass(cg, an);
			}

			for (org.apache.bcel.classfile.Method m : cg.getMethods()) {
				for (Annotation an : getAnnotations(m.getAttributes())) {
					ConstantPoolGen cpg = cg.getConstantPool();

					MethodGen mg = new MethodGen(m, className, cpg);
					
					instrumentor.instrumentMethod(cg, mg, an);

					mg.setMaxStack();
					mg.setMaxLocals();

					cg.replaceMethod(m, mg.getMethod());
				}
			}

			JavaClass jcNew = cg.getJavaClass();
			return jcNew.getBytes();
		} catch (Exception ex) {
			throw new RuntimeException("instrumenting " + className, ex);
		}
	}

	private static Collection<Annotation> getAnnotations (Attribute[] attrs)
	{
		Collection<Annotation> anns = new ArrayList<Annotation>();
		
		for (Attribute a : attrs) {
			if (a instanceof AnnotationsAttribute) {
				AnnotationsAttribute aa = (AnnotationsAttribute)a;

				for (Map m : aa.getAnnotations()) {
					anns.add(AnnotationReader.getAnnotation(m));
				}
			}
		}
		return anns;
	}
}
