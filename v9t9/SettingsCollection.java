/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** A collection of settings.  These are hierarchically arranged.
 * Settings are registered in collections to define where they live.
 * A collection has a parent, which contain more global settings.
 * Searches for variables (on 'get') proceed upwards and then
 * throw an assertion if not found.  When variables are set, they
 * likewise search upward for a candidate or fail.  A setting
 * can be registered twice in a tree; the more local version
 * will override the more global version.  
 * 
 * @author ejs
 */
public class SettingsCollection {
    /* map of strings to Setting objects */
    private Map map;
    private SettingsCollection parent;

    public SettingsCollection() {
        this(null);
    }

    SettingsCollection(SettingsCollection parent) {
        this.map = new TreeMap();
        this.parent = parent;
    }
    
    public void register(String var, Setting setting) {
        map.put(var, setting);
    }

    public Setting find(String var) {
        SettingsCollection coll = this;
        Setting setting = null;
        while (coll != null) {
            setting = (Setting) map.get(var);
            if (setting != null)
                break;
            coll = coll.parent;
        }
        return setting;
    }

    public SettingsCollection findCollection(String var) {
        SettingsCollection coll = this;
        Setting setting = null;
        while (coll != null) {
            setting = (Setting) map.get(var);
            if (setting != null)
                break;
            coll = coll.parent;
        }
        return coll;
    }

    public SettingsCollection findCollection(Setting setting) {
        SettingsCollection coll = this;
        while (coll != null) {
            if (map.containsKey(setting))
                break;
            coll = coll.parent;
        }
        return coll;
    }

    public Object get(String var) {
        Setting setting = find(var);
        if (setting == null)
            throw new AssertionError("unknown variable " + var);
        return setting.get();
    }
    public boolean getBool(String var) {
        return ((Boolean)get(var)).booleanValue();
    }
    public int getInt(String var) {
        return ((Integer)get(var)).intValue();
    }
    public String getString(String var) {
        return ((String)get(var));
    }

    public void set(String var, Object val) {
        Setting setting = find(var);
        if (setting == null)
            throw new AssertionError("unknown variable " + var);
        setting.set(val);
    }
    public void setInt(String var, int val) {
        set(var, new Integer(val));
    }
    public void setBool(String var, boolean val) {
        set(var, new Boolean(val));
    }
    public void setString(String var, String val) {
        set(var, val);
    }
}
