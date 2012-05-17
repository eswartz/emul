/**
 * 
 */
package v9t9.engine.speech;

import v9t9.common.speech.ISpeechPhraseListener;
import ejs.base.utils.ListenerList;

public interface ILPCDataFetcher {
	int fetch(int bits);
	
	void setPhraseListeners(ListenerList<ISpeechPhraseListener> listeners);

	void reset();
}