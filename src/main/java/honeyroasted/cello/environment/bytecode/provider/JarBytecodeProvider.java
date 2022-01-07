package honeyroasted.cello.environment.bytecode.provider;

import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;

import java.io.IOException;
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
                return Verification.success(this, arr);
            } else {
                return Verification.error(this, Verify.Code.TYPE_NOT_FOUND_ERROR, "Could not locate class '%s' in jar '%s'", namespace.name(), this.jar);
            }
        } catch (IOException e) {
            return Verification.error(this, Verify.Code.TYPE_NOT_FOUND_ERROR, "Encountered error while loading classfile for class '%s' in jar '%s'", e, namespace.name(), this.jar);
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
