/*
  SettingsHandler.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.settings;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.BaseSettingsHandler;

/**
 * @author ejs
 *
 */
public final class SettingsHandler extends BaseSettingsHandler implements ISettingsHandler {
	public SettingsHandler(String workspaceName) {
		super(new WorkspaceSettings(workspaceName), new EmulatorSettings());
	}
}