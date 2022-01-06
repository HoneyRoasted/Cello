package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.javatype.Namespace;

import java.util.Optional;

public interface BytecodeProvider {

    Optional<byte[]> provide(Namespace namespace);

}
