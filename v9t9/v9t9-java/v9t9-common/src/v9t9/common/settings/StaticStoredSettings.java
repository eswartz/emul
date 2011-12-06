/**
 * 
 */
package v9t9.common.settings;

/**
 * @author ejs
 *
 */
public class StaticStoredSettings extends BaseStoredSettings {

	private String configFileName;

	/**
	 * 
	 */
	public StaticStoredSettings(String context, String configFileName) {
		super(context);
		this.configFileName = configFileName;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.settings.IStoredSettings#getConfigFileName()
	 */
	@Override
	public String getConfigFileName() {
		return configFileName;
	}

	/**
	 * @param configFileName the configFileName to set
	 */
	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}
}
