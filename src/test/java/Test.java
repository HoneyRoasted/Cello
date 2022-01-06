import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.BytecodeEnvironment;
import honeyroasted.cello.environment.bytecode.provider.DirBytecodeProvider;
import honeyroasted.cello.environment.bytecode.provider.RuntimeBytecodeProvider;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationFormatter;
import honeyroasted.javatype.Namespace;

import java.nio.file.Paths;

public class Test {

    public static void main(String[] args) {
        Environment environment = new BytecodeEnvironment(new RuntimeBytecodeProvider(), new DirBytecodeProvider(Paths.get("/docs")));
        Verification<ClassNode> lookup = environment.lookup(Namespace.of("does.not.Exist"));
        System.out.println(VerificationFormatter.format(Verification.builder().message("Oops").child(lookup).success(false).build(), false));
    }

}
