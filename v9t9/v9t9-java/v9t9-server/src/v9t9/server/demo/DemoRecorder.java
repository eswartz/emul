/**
 * 
 */
package v9t9.server.demo;

import java.io.IOException;

import ejs.base.utils.ListenerList;

import v9t9.common.demo.IDemoHandler.IDemoListener;
import v9t9.common.demo.IDemoOutputStream;

/**
 * @author ejs
 *
 */
public class DemoRecorder {
	private final IDemoOutputStream os;
	private final ListenerList<IDemoListener> listeners;

	public DemoRecorder(IDemoOutputStream os, ListenerList<IDemoListener> listeners) {
		this.os = os;
		this.listeners = listeners;
	}
	
	public void stop() throws IOException {
		os.close();
	}

}
