/**
 * 
 */
package v9t9.server.tcf.services.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;

import v9t9.base.utils.Pair;
import v9t9.common.machine.IMachine;

/**
 * Base implementation of the target-side services, with a convenient
 * abstraction to reduce the rote typing around command parsing.
 * @author ejs
 *
 */
public abstract class BaseServiceImpl implements IService, IChannel.ICommandServer {

	private final String serviceName;
	protected final IChannel channel;
	private final Map<String, Pair<Integer, Integer>> commandInfo = new HashMap<String, Pair<Integer,Integer>>();
	
	protected final IMachine machine;
	
	/**
	 * @param machine 
	 * 
	 */
	public BaseServiceImpl(IMachine machine, IChannel channel, String serviceName) {
		this.machine = machine;
		this.channel = channel;
		this.serviceName = serviceName;
		
		channel.addCommandServer(this, this);
	}
	
	/**
	 * Register a valid command
	 * @param name
	 * @param numInArgs number of arguments expected
	 * @param numOutArgs output arguments EXCLUDING ERROR
	 */
	protected void registerCommand(String name, int numInArgs, int numOutArgs) {
		commandInfo.put(name, new Pair<Integer, Integer>(numInArgs, numOutArgs));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	@Override
	public String getName() {
		return serviceName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IChannel.ICommandServer#command(org.eclipse.tm.tcf.protocol.IToken, java.lang.String, byte[])
	 */
	@Override
	public void command(IToken token, String name, byte[] data) {
		if (!commandInfo.containsKey(name)) {
			channel.rejectCommand(token);
			return;
		}
		
		Pair<Integer, Integer> inOut = commandInfo.get(name);
        try {
			doCommand(token, name, data, inOut);
		} catch (IOException e) {
			channel.terminate(e);
		}
    }

	/**
	 * @param token
	 * @param name
	 * @param data
	 * @param inOut
	 * @throws IOException
	 */
	protected void doCommand(IToken token, String name, byte[] data,
			Pair<Integer, Integer> inOut) throws IOException {
		try {
            Object[] args = JSON.parseSequence(data);
            if (args.length != inOut.first) {
            	channel.sendResult(token, JSON.toJSONSequence(
            			new Object[] { new ErrorReport("Expected " + inOut.first + " arguments",
            					IErrorReport.TCF_ERROR_INV_FORMAT)
            			}));
            	return;
            }
			Object[] result = handleCommand(name, args);
			
            if (result.length != inOut.second) {
            	throw new IllegalStateException("Expected " + inOut.second + " results");
            }

            channel.sendResult(token, JSON.toJSONSequence(result, true));
            
        } catch (ErrorReport e) {
        	Object[] results = new Object[inOut.second];
        	results[0] = e;
        	
            channel.sendResult(token, JSON.toJSONSequence(results));  

        } catch (Throwable x) {
        	Protocol.log("Failed to handle command " + serviceName + "#" + name, x);
        }
	}

	/**
	 * Handle a command.  Should return Object[] array in case of success
	 * and throw {@link ErrorReport} in case of error.
	 * @param name command name
	 * @param args arguments
	 * @return result arguments, excluding error in first position
	 */
	protected abstract Object[] handleCommand(String name, Object[] args) throws ErrorReport, Exception;

}
