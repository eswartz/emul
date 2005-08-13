package tests.dynamic_classgen_with_bcel;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;

/**
 * Program demonstrating how runtime class generation can be used to replace
 * reflection, with a substantial increase in performance for repeated calls.
 * This base class must be extended by subclasses that handle the actual runtime
 * code generation. Each subclass must also implement a <code>main()</code>
 * method that creates an instance of the subclass and then passes the command
 * line parameters to the base class {@link #run} method for running the actual
 * class generation and timing comparison code.
 */

public abstract class TimeCalls
{
    /** Class loader for access classes */
    protected DirectLoader s_classLoader = new DirectLoader();
    
    /**
     * Create access class for getting and setting a bean-style property value.
     * This creates a class that implements a get/set interface, with the actual
     * implementations of the get and set methods simply calling the supplied
     * target class methods, returning the bytecode array.
     *
     * @param tclas target class (may inherit get and set methods, or implement
     * directly)
     * @param gmeth get method (must take nothing, return <code>int</code>)
     * @param smeth set method (must take <code>int</code>, return void)
     * @param cname name for constructed access class
     * @return instance of class implementing the object interface
     * @throws Exception on error generating class
     */
     
    protected abstract byte[] createAccess(Class tclas, Method gmeth,
        Method smeth, String cname) throws Exception;
    
    /** Run timed loop using reflection for access to value. */
    private int runReflection(int num, Method gmeth, Method smeth, Object obj) {
        int value = 0;
        try {
            Object[] gargs = new Object[0];
            Object[] sargs = new Object[1];
            for (int i = 0; i < num; i++) {
                
                // messy usage of <code>Integer</code> values required in loop
                Object result = gmeth.invoke(obj, gargs);
                value = ((Integer)result).intValue() + 1;
                sargs[0] = new Integer(value);
                smeth.invoke(obj, sargs);
                
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
        return value;
    }
    
    /** Run timed loop using generated class for access to value. */
    private int runAccess(int num, IAccess access, Object obj) {
        access.setTarget(obj);
        int value = 0;
        for (int i = 0; i < num; i++) {
            value = access.getValue() + 1;
            access.setValue(value);
        }
        return value;
    }
    
    public void run(String name, int count) throws Exception {
        
        // get instance and access methods
        HolderBean bean = new HolderBean();
        String pname = name;
        char lead = pname.charAt(0);
        pname = Character.toUpperCase(lead) + pname.substring(1);
        Method gmeth = null;
        Method smeth = null;
        try {
            gmeth = HolderBean.class.getDeclaredMethod("get" + pname,
                new Class[0]);
            smeth = HolderBean.class.getDeclaredMethod("set" + pname,
                new Class[] { int.class });
        } catch (Exception ex) {
            System.err.println("No methods found for property " + pname);
            ex.printStackTrace(System.err);
            return;
        }
        
        // create the access class as a byte array
        long base = System.currentTimeMillis();
        String cname = this.getClass().getPackage().getName()+".IAccess$impl_HolderBean_" + gmeth.getName() +
            "_" + smeth.getName();
        byte[] bytes = createAccess(HolderBean.class, gmeth, smeth, cname);
        
        // load and construct an instance of the class
        Class clas = s_classLoader.load(cname, bytes);
        IAccess access = null;
        try {
            access = (IAccess)clas.newInstance();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        } catch (InstantiationException ex) {
            ex.printStackTrace(System.err);
            System.exit(1);
        }
        System.out.println("Generate and load time of " +
            (System.currentTimeMillis()-base) + " ms.");
        
        // run the timing comparison
        long start = System.currentTimeMillis();
        int result = runReflection(count, gmeth, smeth, bean);
        long time = System.currentTimeMillis() - start;
        System.out.println("Reflection took " + time + " ms. with result " +
            result + " (" + bean.getValue1() + ", " + bean.getValue2() + ")");
        bean.setValue1(0);
        bean.setValue2(0);
        start = System.currentTimeMillis();
        result = runAccess(count, access, bean);
        time = System.currentTimeMillis() - start;
        System.out.println("Generated took " + time + " ms. with result " +
            result + " (" + bean.getValue1() + ", " + bean.getValue2() + ")");
    }
    
    /** Simple-minded loader for constructed classes. */
    protected static class DirectLoader extends SecureClassLoader
    {
        protected DirectLoader() {
            super(TimeCalls.class.getClassLoader());
        }
        
        protected Class load(String name, byte[] data) {
            return super.defineClass(name, data, 0, data.length);
        }
    }
}