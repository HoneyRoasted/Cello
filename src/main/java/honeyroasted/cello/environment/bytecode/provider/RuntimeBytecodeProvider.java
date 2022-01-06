package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.javatype.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class RuntimeBytecodeProvider implements BytecodeProvider {

    @Override
    public Optional<byte[]> provide(Namespace namespace) {
        try {
            Class<?> cls = Class.forName(namespace.internalName().replace('/', '.'));
            try (InputStream resource = cls.getResourceAsStream(namespace.className().replace('.', '$') + ".class")) {
                if (resource != null) {
                    return Optional.of(resource.readAllBytes());
                } else {
                    return Optional.empty();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            return Optional.empty();
        }
    }

}
