/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9;

/** A single configurable setting.
 * @author ejs
 */
public class Setting {
    Object storage;
    public Setting(Object storage) {
        this.storage = storage;
    }
    public Object get() {
        return storage;
    }
    public int getInt() {
        return ((Integer)storage).intValue();
    }
    public boolean getBool() {
        return ((Boolean)storage).booleanValue();
    }
    public String getString() {
        return ((String)storage);
    }
    public void set(Object val) {
        storage = val;
    }
    public void setInt(int val) {
        storage = new Integer(val);
    }
    public void setBoolean(boolean val) {
        storage = new Boolean(val);
    }
    public void setString(String val) {
        storage = val;
    }
}
