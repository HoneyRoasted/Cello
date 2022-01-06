package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.cello.verify.Verification;
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
                return Verification.success(Files.readAllBytes(path));
            } catch (IOException e) {
                return Verification.<byte[]>builder()
                        .errorCode(Verification.ErrorCode.TYPE_NOT_FOUND_ERROR)
                        .message("Encountered error while loading source for class " + namespace.name() + " in directory " + this.root + ": " + e.getMessage())
                        .error(e)
                        .build();
            }
        } else {
            return Verification.<byte[]>builder()
                    .errorCode(Verification.ErrorCode.TYPE_NOT_FOUND_ERROR)
                    .message("Could not locate class " + namespace.name() + " at path " + path)
                    .build();
        }
    }

}
