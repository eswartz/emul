package boxpeeking.instrument;

import java.lang.annotation.Annotation;
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;

public class NullInstrumentor implements Instrumentor
{
	private static NullInstrumentor instance = new NullInstrumentor();
	
	public static Instrumentor getInstance ()
	{
		return instance;
	}

	public void instrumentClass (ClassGen classGen, Annotation a)
	{
		// intentionally do nothing...
	}

	public void instrumentMethod (ClassGen classGen, MethodGen methodGen, Annotation a)
	{
		// intentionally do nothing...
	}
}
