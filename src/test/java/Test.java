import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.BytecodeEnvironment;
import honeyroasted.cello.environment.bytecode.provider.RuntimeBytecodeProvider;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.MethodNode;

import java.io.Serializable;
import java.util.Optional;

public class Test implements Serializable {

    public static void main(String[] args) {
        Environment environment = new BytecodeEnvironment(new RuntimeBytecodeProvider());

        ClassNode listCls = environment.lookup(Optional.class).value().get();

        System.out.println(listCls.lookupMethods(m -> m.name().equals("empty")).stream().map(MethodNode::externalName).toList());
    }

}
