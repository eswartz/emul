/*
  IRegistersV2.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.server.tcf.services;


import java.util.Collection;
import java.util.List;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRegisters;

import ejs.base.utils.Pair;


/**
 * This extension to the IRegisters service allows for
 * near-real-time notification of register value changes.
 * 
 * @author ejs
 *
 */
public interface IRegistersV2 extends IRegisters {
	String NAME = "RegistersV2";
	
	class RegisterChange {
		
		public final int regNum;
		public final int value;
		
		public RegisterChange(int regNum, int value) {
			this.regNum = regNum;
			this.value = value;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "RegisterChange: reg " + regNum + " = " + value;
		}
	}
	
	interface RegisterContentChangeListener {
		/**
		 * Report of groups of registers that changed during a notification
		 * quantum.  
		 * 
		 * @param notifyId
		 * @param regChanges list of changes for each affected register;
		 * the same register may be reported more than once
		 * 
		 * @json <pre>  &lt;contextId&gt; &lt;data&gt;
		 * data:Binary 
		 * 		&lt;byte: base reg #&gt;, 
		 * 		&lt;byte: regsize&gt;, 
		 * then groups of:
		 * 		&lt;int4: timestamp&gt;
		 * 		&lt;byte: # regs&gt;, 
		 * 	 followed by packed array of 
		 * 	 current register values, each encoded as:
		 * 		&lt;byte:number (IRegistersV2#PROP_NUMBER),
		 * 		&lt;big-endian value, 'size' bytes&gt;
		 * </pre>
		 */
		void contentChanged(String notifyId, List<Pair<Integer, RegisterChange[]>> regChanges);
	}
	
	/** 
	 * This attribute is added to #getContext to provide the integer number of the register
	 * for use in the contentChanged event.
	 */
	String PROP_NUMBER = "Number";
	
	String COMMAND_START_CHANGE_NOTIFY = "startChangeNotify";
	String COMMAND_STOP_CHANGE_NOTIFY = "stopChangeNotify";

	
	String EVENT_CONTENT_CHANGED = "contentChanged";
	
	interface DoneCommand {
		void done(Exception error);
	}
	
	/**
	 * Start notifying register change information for the given
	 * registers under the given group context ID.  Fires contentChanged 
	 * events on change to the given registers, each "msDelay" milliseconds.
	 * 
	 * Once registered, this command will spawn a contentChanged
	 * event that covers all the interested registers, so the client
	 * will know exactly where it is starting from.
	 * 
	 * @param notifyId id for this notification
	 * @param contextId
	 * @param regNums register numbers to track
	 * @param msDelay delay in ms between events
	 * @param granularity minimum gap in milliseconds between groups, < 0 for 
	 * only deltas at end of period
	 * @param done
	 * @return
	 */
	IToken startChangeNotify(String notifyId, String contextId, 
			Collection<Integer> regNums, 
			int msDelay, int granularity, DoneCommand done);
	/**
	 * Stop notifying full register change information for the given
	 * context ID, so no more contentChanged events will be generated.
	 * @param notifyId
	 * @param done
	 * @return
	 */
	IToken stopChangeNotify(String notifyId, DoneCommand done);
	
	void addListener(RegisterContentChangeListener listener);
	void removeListener(RegisterContentChangeListener listener);
	
}
