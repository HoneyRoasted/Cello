package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Namespace;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarBytecodeProvider implements BytecodeProvider {
    private Path jar;

    public JarBytecodeProvider(Path jar) {
        this.jar = jar;
    }

    @Override
    public Verification<byte[]> provide(Namespace namespace) {
        JarFile file = null;
        try {
            file = new JarFile(this.jar.toFile());

            JarEntry entry = file.getJarEntry(namespace.internalName() + ".class");
            if (entry != null) {
                byte[] arr = file.getInputStream(entry).readAllBytes();
                return Verification.success(arr);
            } else {
                return Verification.<byte[]>builder()
                        .errorCode(Verification.ErrorCode.TYPE_NOT_FOUND_ERROR)
                        .message("Could not locate class " + namespace.name() + " in jar " + this.jar)
                        .typeNotFoundError(namespace)
                        .build();
            }
        } catch (IOException e) {
            return Verification.<byte[]>builder()
                    .errorCode(Verification.ErrorCode.TYPE_NOT_FOUND_ERROR)
                    .message("Encountered error while loading source for class " + namespace.name() + " in jar " + this.jar + ": " + e.getMessage())
                    .error(e)
                    .build();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ignore) {

                }
            }
        }
    }

}
