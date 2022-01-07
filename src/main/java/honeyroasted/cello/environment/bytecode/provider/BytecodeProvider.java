package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;

public interface BytecodeProvider {

    Verification<byte[]> provide(Namespace namespace);

}
