package boxpeeking.status;

import java.util.Map;
import java.util.HashMap;

public class StatusManager
{
	private static StatusState state = new StatusState();

	public static void push (String msg)
	{
		state.push(msg);
	}

	public static void pop ()
	{
		state.pop();
	}

	public static void setState (StatusState st)
	{
		state.updateFrom(st);
	}

	public static void addListener (StatusListener l)
	{
		state.addListener(l);
	}

	public static void removeListener (StatusListener l)
	{
		state.removeListener(l);
	}
}
