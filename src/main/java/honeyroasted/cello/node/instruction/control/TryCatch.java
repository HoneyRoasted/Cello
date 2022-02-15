package honeyroasted.cello.node.instruction.control;

import com.sun.jdi.InvalidStackFrameException;
import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.Var;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.parameterized.TypeParameterized;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.Set;

public class TryCatch extends AbstractNode implements Node {
    private static final TypeInformal THROWABLE = Types.type(Throwable.class);

    @Child(scope = Child.SUB_SCOPE)
    private Node body;
    private List<CatchBlock> catchBlocks;
    @Child(scope = Child.SUB_SCOPE)
    private Node finalBlock;

    public TryCatch(Node body, List<CatchBlock> catchBlocks, Node finalBlock) {
        this.body = body.toUntyped();
        this.catchBlocks = catchBlocks;
        this.finalBlock = finalBlock.toUntyped();
    }

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {
        VerificationBuilder<TypeInformal> builder = Verification.builder();

        this.catchBlocks.forEach(c -> {
            VerificationBuilder<?> block = Verification.builder();

            List<TypeInformal> types = c.type().stream().map(t -> (TypeInformal) t.withArguments()).toList();

            types.forEach(t -> {
                if (!t.isAssignableTo(THROWABLE)) {
                    block.child(Verification.error(Verify.Code.TYPE_ERROR, "%s is not assignable to %s", t, THROWABLE));
                }
            });

            Verification<TypeInformal> sub = c.body().verify(environment, context.childScope());
            block.child(sub);
            if (sub.success()) {
                Verification<TypeInformal> parent = TypeUtil.commonParent(environment, types);
                block.child(parent);
                if (parent.success() && parent.value().isPresent()) {
                    c.exType(parent.value().get());

                    for (int i = 0; i < types.size(); i++) {
                        for (int j = 0; j < types.size(); j++) {
                            if (i != j) {
                                TypeInformal a = types.get(i);
                                TypeInformal b = types.get(j);

                                if (a.isAssignableTo(b) || b.isAssignableTo(a)) {
                                    block.child(Verification.error(this, Verify.Code.CATCH_TYPE_CLASH, "%s clashes with %s in catch clause", a, b));
                                }
                            }
                        }
                    }
                }
            }

            builder.child(block.andChildren().build());
        });

        return builder.andChildren().build();
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        Label startBody = new Label();
        Label endBody = new Label();
        Label endTries = new Label();

        adapter.mark(startBody);
        this.body.apply(adapter, environment, context.childScope());
        if (!this.catchBlocks.isEmpty()) {
            adapter.goTo(endTries);
        }
        adapter.mark(endBody);

        this.catchBlocks.forEach(c -> {
            Label start = new Label();
            adapter.mark(start);

            CodeContext sub = context.childScope();
            Var var = sub.scope().define(c.varName(), c.exType());

            adapter.store(var.index(), TypeUtil.asmType(var.type()));
            c.body().apply(adapter, environment, sub);
            adapter.goTo(endTries);

            c.type().forEach(t -> adapter.visitTryCatchBlock(startBody, endBody, start, t.internalName()));
        });

        if (!this.catchBlocks.isEmpty()) {
            adapter.mark(endTries);
        }

        if (this.finalBlock != null) {
            Label end = this.catchBlocks.isEmpty() ? endBody : endTries;
            adapter.visitTryCatchBlock(startBody, end, end, null);
            this.finalBlock.apply(adapter, environment, context.childScope());
        }
    }

    public static class CatchBlock {
        private Set<TypeParameterized> type;
        private String varName;
        private Node body;

        private TypeInformal exType;

        public CatchBlock(Set<TypeParameterized> type, String varName, Node body) {
            this.type = type;
            this.varName = varName;
            this.body = body.toUntyped();
        }

        public TypeInformal exType() {
            return exType;
        }

        public CatchBlock exType(TypeInformal exType) {
            this.exType = exType;
            return this;
        }

        public Set<TypeParameterized> type() {
            return type;
        }

        public String varName() {
            return varName;
        }

        public Node body() {
            return body;
        }
    }

}
