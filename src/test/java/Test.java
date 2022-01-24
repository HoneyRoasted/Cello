import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.BytecodeEnvironment;
import honeyroasted.cello.environment.bytecode.provider.RuntimeBytecodeProvider;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.LocalScope;
import honeyroasted.cello.node.instruction.NoOp;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.invoke.InvokeVirtual;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.MethodNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;

import java.io.Serializable;
import java.util.ArrayList;

public class Test implements Serializable {

    public static void main(String[] args) {
        Environment environment = new BytecodeEnvironment(new RuntimeBytecodeProvider());

        ClassNode strClass = environment.lookup(String.class).value().get();

        CodeContext context = new CodeContext(strClass.methods().get(0), new LocalScope());

        Node source = Nodes.cast(Nodes.cast(Nodes.constant(5), Types.OBJECT), Types.type(Test.class));
        System.out.println(source.verify(environment, context).format());

        Verification<MethodNode> v = InvokeVirtual.lookupVirtualMethod(new NoOp(), source, "toString", new ArrayList<>(), environment, context);
        System.out.println(v.format());
    }

}
