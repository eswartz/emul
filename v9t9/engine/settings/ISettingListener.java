/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 31, 2005
 *
 */
package v9t9.engine.settings;

public interface ISettingListener {
    public void changed(Setting setting, Object oldValue);
    
}
