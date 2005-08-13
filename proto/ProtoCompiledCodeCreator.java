
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class ProtoCompiledCodeCreator implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;

  public ProtoCompiledCodeCreator() {
    _cg = new ClassGen("ProtoCompiledCode", "v9t9.cpu.CompiledCode", "ProtoCompiledCode.java", ACC_PUBLIC | ACC_SUPER, new String[] {  });

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
  }

  public void create(OutputStream out) throws IOException {
    createMethod_0();
    createMethod_1();
    _cg.getJavaClass().dump(out);
  }

  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "ProtoCompiledCode", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("v9t9.cpu.CompiledCode", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.BOOLEAN, Type.NO_ARGS, new String[] {  }, "run", "ProtoCompiledCode", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("ProtoCompiledCode", "cpu", new ObjectType("v9t9.cpu.Cpu"), Constants.GETFIELD));
    il.append(_factory.createInvoke("v9t9.cpu.Cpu", "getPC", Type.SHORT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
    il.append(_factory.createStore(Type.INT, 1));
    InstructionHandle ih_8 = il.append(_factory.createLoad(Type.INT, 1));
        Select tableswitch_9 = new TABLESWITCH(new int[] { 256, 257, 258 }, new InstructionHandle[] { null, null, null}, null);
    il.append(tableswitch_9);
    InstructionHandle ih_36 = il.append(new PUSH(_cp, 1));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_38 = il.append(new PUSH(_cp, 0));
    il.append(_factory.createReturn(Type.INT));
    InstructionHandle ih_40 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createFieldAccess("ProtoCompiledCode", "cpu", new ObjectType("v9t9.cpu.Cpu"), Constants.GETFIELD));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke("v9t9.cpu.Cpu", "setPC", Type.VOID, new Type[] { Type.SHORT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_48 = il.append(new PUSH(_cp, 0));
    InstructionHandle ih_49 = il.append(_factory.createReturn(Type.INT));
    tableswitch_9.setTarget(ih_40);
    tableswitch_9.setTarget(0, ih_36);
    tableswitch_9.setTarget(1, ih_40);
    tableswitch_9.setTarget(2, ih_38);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  public static void main(String[] args) throws Exception {
    ProtoCompiledCodeCreator creator = new ProtoCompiledCodeCreator();
    creator.create(new FileOutputStream("ProtoCompiledCode.class"));
  }
}
