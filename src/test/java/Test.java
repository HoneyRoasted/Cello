import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.BytecodeEnvironment;
import honeyroasted.cello.environment.bytecode.provider.RuntimeBytecodeProvider;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;

public class Test {

    public static void main(String[] args) {
        Environment environment = new BytecodeEnvironment(new RuntimeBytecodeProvider());
        Verification<ClassNode> verification = environment.lookup(String[].class);
        System.out.println(verification.format(Verify.Level.WARNING, true));
        if (verification.success() && verification.value().isPresent()) {
            System.out.println(verification.value().get().buildType().getClass());
        }
    }

}
