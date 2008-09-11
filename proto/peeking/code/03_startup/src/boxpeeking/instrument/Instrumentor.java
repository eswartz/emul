package boxpeeking.instrument;

import java.lang.annotation.Annotation;
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;

public interface Instrumentor
{
	public void instrumentClass (ClassGen classGen, Annotation a);
	public void instrumentMethod (ClassGen classGen, MethodGen methodGen, Annotation a);
}
