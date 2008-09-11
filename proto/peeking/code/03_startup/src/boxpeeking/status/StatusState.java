package boxpeeking.status;

import java.util.*;

public class StatusState
{
	private Collection<StatusListener> listeners;
	private Stack<String> messageStack;

	public StatusState ()
	{
		listeners = new ArrayList<StatusListener>();

		messageStack = new Stack<String>();
	}

	public synchronized String getTopMessage ()
	{
		if (messageStack.isEmpty()) {
			return "";
		} else {
			return messageStack.peek();
		}
	}

	public synchronized boolean isEmpty ()
	{
		return messageStack.isEmpty();
	}

	public synchronized void push (String message)
	{
		messageStack.push(message);
		doNotify();
	}

	public synchronized void pop ()
	{
		messageStack.pop();
		doNotify();
	}

	public synchronized void updateFrom (StatusState st)
	{
		messageStack.clear();
		messageStack.addAll(st.messageStack);
		doNotify();
	}

	private void doNotify ()
	{
		for (StatusListener sl : listeners)
			sl.notify(this);
	}

	public synchronized void addListener (StatusListener l)
	{
		listeners.add(l);
	}

	public synchronized void removeListener (StatusListener l)
	{
		listeners.remove(l);
	}
}
