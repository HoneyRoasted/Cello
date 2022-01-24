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

import java.io.Serializable;
import java.util.Arrays;

public class Test implements Serializable {

    public static void main(String[] args) {
        Environment environment = new BytecodeEnvironment(new RuntimeBytecodeProvider());

        ClassNode strClass = environment.lookup(String.class).value().get();

        CodeContext context = new CodeContext(strClass.methods().get(0), new LocalScope());

        Node source = Nodes.constant("Hello ");
        Node param = Nodes.constant(" world");
        param.verify(environment, context);
        source.verify(environment, context).format();

        Verification<MethodNode> v = InvokeVirtual.lookupMethod(new NoOp(), source.type(), "concat", Arrays.asList(param), environment, context, false, false);
        System.out.println(v.format());
    }

}
