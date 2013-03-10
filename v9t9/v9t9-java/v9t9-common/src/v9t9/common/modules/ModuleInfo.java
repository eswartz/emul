/**
 * 
 */
package v9t9.common.modules;


/**
 * @author ejs
 *
 */
public class ModuleInfo {

	private String name;
	private String imageName;
	private String descr;


	public static ModuleInfo createForModule(IModule module) {
		ModuleInfo moduleInfo = new ModuleInfo();
		moduleInfo.setName(module.getName());
		return moduleInfo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return filename of screenshot image
	 */
	public String getImagePath() {
		return imageName;
	}
	
	/**
	 * @param filename of screenshot image
	 */
	public void setImagePath(String imageName) {
		this.imageName = imageName;
	}

	/**
	 * @param descr
	 */
	public void setDescription(String descr) {
		this.descr = descr;
	}
 
	/**
	 * @return the descr
	 */
	public String getDescription() {
		return descr;
	}
	
}
