/*
  SettingsProxy.java

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
package v9t9.server.tcf.services.remote;

import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;

import v9t9.server.tcf.services.ISettings;

/**
 * @author ejs
 *
 */
public class SettingsProxy implements ISettings {

	private final IChannel channel;
	
	/**
	 * 
	 */
	public SettingsProxy(IChannel channel) {
		this.channel = channel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.ISettingsService#getSettings(v9t9.server.tcf.services.ISettingsService.DoneGetSettingsCommand)
	 */
	@Override
	public IToken queryAll(final DoneQueryAllCommand done) {
		return new Command(channel, this, "queryAll", new Object[0]) {
            @SuppressWarnings("unchecked")
			@Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                }
                done.doneQueryAllCommand(token, error, (Map<String, String>) args[1]);
            }
        }.token;
	}

	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.ISettingsService#readSetting(java.lang.String, v9t9.server.tcf.services.ISettingsService.DoneReadSettingCommand)
	 */
	@Override
	public IToken get(String name, final DoneReadSettingCommand done) {
		return new Command(channel, this, "get", new Object[0]) {
			@Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                }
                done.doneGetCommand(token, error, args[1]);
            }
        }.token;
	}

	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.ISettingsService#writeSetting(java.lang.String, java.lang.Object, v9t9.server.tcf.services.ISettingsService.DoneWriteSettingCommand)
	 */
	@Override
	public IToken set(String name, Object value,
			final DoneSetCommand done) {
		return new Command(channel, this, "set", new Object[0]) {
			@Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 2;
                    error = toError(args[0]);
                }
                done.doneSetCommand(token, error, args[1]);
            }
        }.token;
	}

}
