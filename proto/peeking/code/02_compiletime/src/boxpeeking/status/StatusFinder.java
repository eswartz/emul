package boxpeeking.status;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;

public class StatusFinder
{
	public static List<Status> getStatus (Throwable th)
	{
		while (th.getCause() != null) {
			th = th.getCause();
		}

		return getStatus(th.getStackTrace());
	}

	public static List<Status> getStatus (StackTraceElement[] e)
	{
		List<Status> l = new ArrayList<Status>();

		for (StackTraceElement ste : e) {
			Status status = getStatus(ste);
			if (status != null) {
				l.add(status);
			}
		}

		return l;
	}

	public static Status getStatus (StackTraceElement e)
	{
		Class c;
		try {
			c = Class.forName(e.getClassName());
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("no class for " + e, ex);
		}

		while (c != null) {
			Method[] ms = c.getDeclaredMethods();
			for (int i = 0; i < ms.length; i++) {
				Method m = ms[i];

				if (m.getName().equals(e.getMethodName())) {
					Status s = m.getAnnotation(Status.class);

					if (s != null) {
						return s;
					}
				}
			}
			c = c.getSuperclass();
		}

		return null;
	}
}

