package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirBytecodeProvider implements BytecodeProvider {
    private Path root;

    public DirBytecodeProvider(Path root) {
        this.root = root;
    }

    @Override
    public Verification<byte[]> provide(Namespace namespace) {
        Path path = this.root.resolve(namespace.internalName() + ".class");
        if (Files.exists(path)) {
            try {
                return Verification.success(this, Files.readAllBytes(path));
            } catch (IOException e) {
                return Verification.error(this, Verify.Code.TYPE_NOT_FOUND_ERROR, "Encountered error while loading classfile for class '%s' at path '%s'", e, namespace.name(), path);
            }
        } else {
            return Verification.error(this, Verify.Code.TYPE_NOT_FOUND_ERROR, "Could not locate class '%s' at path '%s'", namespace.name(), path);
        }
    }

}
