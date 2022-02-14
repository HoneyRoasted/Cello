package honeyroasted.cello.node.instruction.util;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.properties.Properties;
import honeyroasted.cello.verify.Verification;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

public class UntypedNode implements Node {
    private Node delegate;

    public UntypedNode(Node delegate) {
        this.delegate = delegate;
    }

    @Override
    public Verification<TypeInformal> verify(Environment environment, CodeContext context) {
        return this.delegate.verify(environment, context).map(t -> Types.VOID);
    }

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.delegate.apply(adapter, environment, context);
        int size = TypeUtil.size(this.delegate.type());

        if (size % 2 != 0) {
            adapter.pop();
        }

        for (int i = 0; i < size / 2; i++) {
            adapter.pop2();
        }
    }

    @Override
    public void setExpected(TypeInformal type) {
        this.delegate.setExpected(type);
    }

    @Override
    public TypeInformal expected() {
        return this.delegate.expected();
    }

    @Override
    public Verification<TypeInformal> verification() {
        return this.delegate.verification();
    }

    @Override
    public TypeInformal type() {
        return this.delegate.type();
    }

    @Override
    public Properties properties() {
        return this.delegate.properties();
    }

    @Override
    public <T> T withProperties(Properties properties) {
        return (T) new UntypedNode(this.delegate.withProperties(properties));
    }

    @Override
    public Node toUntyped() {
        return this;
    }
}
