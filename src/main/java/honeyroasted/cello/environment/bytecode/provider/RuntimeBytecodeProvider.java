package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class RuntimeBytecodeProvider implements BytecodeProvider {

    @Override
    public Verification<byte[]> provide(Namespace namespace) {
        try {
            Class<?> cls = Class.forName(namespace.internalName().replace('/', '.'));
            try (InputStream resource = cls.getResourceAsStream(namespace.className().replace('.', '$') + ".class")) {
                if (resource != null) {
                    return Verification.success(resource.readAllBytes());
                } else {
                    return Verification.<byte[]>builder()
                            .errorCode(Verification.ErrorCode.TYPE_NOT_FOUND_ERROR)
                            .message("Could not find source for runtime-available class " + cls.getName())
                            .build();
                }
            }
        } catch (ClassNotFoundException e) {
            return Verification.<byte[]>builder()
                    .errorCode(Verification.ErrorCode.TYPE_NOT_FOUND_ERROR)
                    .message("Class " + namespace.name() + " is not available at runtime")
                    .error(e)
                    .build();
        } catch (IOException e) {
            return Verification.<byte[]>builder()
                    .errorCode(Verification.ErrorCode.TYPE_NOT_FOUND_ERROR)
                    .message("Encountered error while loading source for runtime-available class " + namespace.name() + ": " + e.getMessage())
                    .error(e)
                    .build();
        }
    }

}
