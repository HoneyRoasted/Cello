package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;

import java.io.IOException;
import java.io.InputStream;

public class RuntimeBytecodeProvider implements BytecodeProvider {

    @Override
    public Verification<byte[]> provide(Namespace namespace) {
        try {
            Class<?> cls = Class.forName(namespace.internalName().replace('/', '.'));
            try (InputStream resource = cls.getResourceAsStream(namespace.className().replace('.', '$') + ".class")) {
                if (resource != null) {
                    return Verification.success(this, resource.readAllBytes());
                } else {
                    return Verification.error(this, Verify.Code.TYPE_NOT_FOUND_ERROR, "Could not find classfile for runtime-available class '%s'", cls.getName());
                }
            }
        } catch (ClassNotFoundException e) {
            return Verification.error(this, Verify.Code.TYPE_NOT_FOUND_ERROR, "Class '%s' is not available at runtime", e, namespace.className());
        } catch (IOException e) {
            return Verification.error(this, Verify.Code.TYPE_NOT_FOUND_ERROR, "Encountered error while loading classfile for class '%s'", e, namespace.name());
        }
    }

}
