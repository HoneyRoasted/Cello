package honeyroasted.cello;

import honeyroasted.cello.environment.bytecode.BytecodeEnvironment;
import honeyroasted.cello.environment.bytecode.provider.RuntimeBytecodeProvider;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;

public class Test {

    public static void main(String[] args) {
        BytecodeEnvironment environment = new BytecodeEnvironment();
        environment.providers().add(new RuntimeBytecodeProvider());

        Verification<ClassNode> verification = environment.lookup(Namespace.of(String.class));
        System.out.println(verification.success());
    }

}
