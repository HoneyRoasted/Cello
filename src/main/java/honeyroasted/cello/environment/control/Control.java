package honeyroasted.cello.environment.control;

import org.objectweb.asm.Label;

public interface Control {

    boolean hasLabel();

    Label label();

}
