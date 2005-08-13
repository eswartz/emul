/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.tests;

import v9t9.SettingsCollection;
import v9t9.Setting;
import junit.framework.TestCase;

/**
 * @author ejs
 */
public class SettingTest extends TestCase {

    SettingsCollection settings;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SettingTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        settings = new SettingsCollection();
        settings.register("var1", new Setting(new Integer(0)));
        settings.register("var2", new Setting(new String()));
    }

    public void testOne() {
        int ol;
        String str;
        Setting stg;
        
        /* ensure a Setting value is mutable but not the setting itself */
        stg = settings.find("var1");
        assertTrue(stg != null);
        ol = stg.getInt();
        assertEquals(ol,0);
        stg.setInt(1);
        assertEquals(stg.getInt(), 1);
        assertEquals(settings.find("var1"), stg);

        /* ensure a Setting value is mutable but not the setting itself */
        stg = settings.find("var2");
        assertTrue(stg != null);
        str = stg.getString();
        assertEquals(str,"");
        stg.setString("foo");
        assertEquals(stg.getString(), "foo");
        assertEquals(settings.find("var2"), stg);
}
}
