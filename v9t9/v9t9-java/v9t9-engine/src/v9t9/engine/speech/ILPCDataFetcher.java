/*
  ILPCDataFetcher.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech;

import v9t9.common.speech.ISpeechPhraseListener;
import ejs.base.utils.ListenerList;

public interface ILPCDataFetcher {
	int fetch(int bits);
	
	void setPhraseListeners(ListenerList<ISpeechPhraseListener> listeners);

	void reset();
}