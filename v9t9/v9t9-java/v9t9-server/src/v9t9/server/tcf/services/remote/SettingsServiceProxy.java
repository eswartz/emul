/**
 * 
 */
package v9t9.server.tcf.services.remote;

import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;

import v9t9.server.tcf.services.ISettingsService;

/**
 * @author ejs
 *
 */
public class SettingsServiceProxy implements ISettingsService {

	private final IChannel channel;
	
	/**
	 * 
	 */
	public SettingsServiceProxy(IChannel channel) {
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
