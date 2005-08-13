package tests.dynamic_classgen_with_bcel;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.Constants;

public class BCELCalls extends TimeCalls
{
    /** Parameter type array for call with single <code>int</code> value. */
    private static final Type[] INT_ARGS = { Type.INT };
    
    /**
     * Create access class for getting and setting a bean-style property value.
     * This creates a class that implements a get/set interface, with the actual
     * implementations of the get and set methods simply calling the supplied
     * target class methods, returning the bytecode array. As written, this only
     * works with <code>int</code> property values, but could easily be modified
     * for any other type.
     *
     * @param tclas target class (may inherit get and set methods, or implement
     * directly)
     * @param gmeth get method (must take nothing, return <code>int</code>)
     * @param smeth set method (must take <code>int</code>, return void)
     * @param cname name for constructed access class
     * @return instance of class implementing the object interface
     */
     
    protected byte[] createAccess(Class tclas,
        java.lang.reflect.Method gmeth, java.lang.reflect.Method smeth,
        String cname) {
        
        // build generators for the new class
        String tname = tclas.getName();
        String iaccessname = this.getClass().getPackage().getName()+".IAccess";
        ClassGen cgen = new ClassGen(cname, "java.lang.Object", cname + ".java",
            Constants.ACC_PUBLIC, new String[] { iaccessname });
        InstructionFactory ifact = new InstructionFactory(cgen);
        ConstantPoolGen pgen = cgen.getConstantPool();
        
        //. add target object field to class
        FieldGen fgen = new FieldGen(Constants.ACC_PRIVATE,
            new ObjectType(tname), "m_target", pgen);
        cgen.addField(fgen.getField());
        int findex = pgen.addFieldref(cname, "m_target",
            Utility.getSignature(tname));
        
        // create instruction list for default constructor
        InstructionList ilist = new InstructionList();
        ilist.append(InstructionConstants.ALOAD_0);
        ilist.append(ifact.createInvoke("java.lang.Object", "<init>", Type.VOID,
            Type.NO_ARGS, Constants.INVOKESPECIAL));
        ilist.append(InstructionFactory.createReturn(Type.VOID));
    
        // add public default constructor method to class
        MethodGen mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
            Type.NO_ARGS, null, "<init>", cname, ilist, pgen);
        addMethod(mgen, cgen);
        
        // create instruction list for <code>setTarget</code> method
        ilist = new InstructionList();
        ilist.append(InstructionConstants.ALOAD_0);
        ilist.append(InstructionConstants.ALOAD_1);
        ilist.append(new CHECKCAST(pgen.addClass(tname)));
        ilist.append(new PUTFIELD(findex));
        ilist.append(InstructionConstants.RETURN);
        
        // add public <code>setTarget</code> method
        mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
            new Type[] { Type.OBJECT }, null, "setTarget", cname, ilist, pgen);
        addMethod(mgen, cgen);
        
        // create instruction list for <code>getValue</code> method
        ilist = new InstructionList();
        ilist.append(InstructionConstants.ALOAD_0);
        ilist.append(new GETFIELD(findex));
        ilist.append(ifact.createInvoke(tname, gmeth.getName(), Type.INT,
            Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        ilist.append(InstructionConstants.IRETURN);
        
        // add public <code>getValue</code> method
        mgen = new MethodGen(Constants.ACC_PUBLIC, Type.INT,
            Type.NO_ARGS, null, "getValue", cname, ilist, pgen);
        addMethod(mgen, cgen);
        
        // create instruction list for <code>setValue</code> method
        ilist = new InstructionList();
        ilist.append(InstructionConstants.ALOAD_0);
        ilist.append(new GETFIELD(findex));
        ilist.append(InstructionConstants.ILOAD_1);
        ilist.append(ifact.createInvoke(tname, smeth.getName(), Type.VOID,
            INT_ARGS, Constants.INVOKEVIRTUAL));
        ilist.append(InstructionConstants.RETURN);
        
        // add public <code>setValue</code> method
        mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID,
            INT_ARGS, null, "setValue", cname, ilist, pgen);
        addMethod(mgen, cgen);
        
        // return bytecode of completed class
        return cgen.getJavaClass().getBytes();
    }
    
    /** Utility method for adding constructed method to class. */
    private static void addMethod(MethodGen mgen, ClassGen cgen) {
        mgen.setMaxStack();
        mgen.setMaxLocals();
        InstructionList ilist = mgen.getInstructionList();
        Method method = mgen.getMethod();
        ilist.dispose();
        cgen.addMethod(method);
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            BCELCalls inst = new BCELCalls();
            int lcnt = Integer.parseInt(args[0]);
            inst.run("value1", lcnt);
            inst.run("value2", lcnt);
        } else {
            System.out.println("Usage: BCELCalls loop-count");
        }
    }
}