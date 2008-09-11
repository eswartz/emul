package boxpeeking.instrument.ant;

import boxpeeking.instrument.Instrumentor;
import boxpeeking.instrument.bcel.AnnotationReader;
import boxpeeking.instrument.bcel.AnnotationsAttribute;
import java.io.*;
import java.util.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.lang.annotation.*;
import java.lang.instrument.*;
import java.net.URLClassLoader;
import java.net.URL;

public class InstrumentTask extends Task
{
	static {
		Attribute.addAttributeReader("RuntimeVisibleAnnotations", new AnnotationReader());
		Attribute.addAttributeReader("RuntimeInvisibleAnnotations", new AnnotationReader());
	};

	private FileSet fileSet;
	private String className;

	public void setClass (String className)
	{
		this.className = className;
	}

	public void addFileSet (FileSet fileSet)
	{
		this.fileSet = fileSet;
	}

    public void execute ()
		throws BuildException
	{
		if (fileSet == null) {
			throw new BuildException("Must specify an embedded <fileset>.");
		}

        log("className = " + className, Project.MSG_DEBUG);
        log("fileSet = " + fileSet, Project.MSG_DEBUG);

		long start = System.nanoTime();

		try {
			Instrumentor inst = getInstrumentor();

			DirectoryScanner ds = fileSet.getDirectoryScanner(project);
			String[] files = ds.getIncludedFiles();
			for (int i = 0; i < files.length; i++) {
				log("file: " + files[i]);

				instrumentFile(inst, files[i]);
			}
		} catch (Exception ex) {
			throw new BuildException(ex);
		}

		long end = System.nanoTime();
		System.err.printf("instrumentation took %.6f sec\n", (end - start) / 1E9D);
    }

	public void instrumentFile (Instrumentor instrumentor, String file)
		throws IOException
	{
		FileInputStream fis = new FileInputStream(file);
		ClassParser cp = new ClassParser(fis, file);
		JavaClass jc = cp.parse();
		fis.close();

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

		jc = cg.getJavaClass();

		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(jc.getBytes());
		} finally {
			fos.close();
		}
	}

	public Instrumentor getInstrumentor ()
		throws Exception
	{
		return (Instrumentor)Class.forName(className).newInstance();
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

