package honeyroasted.cello.node.instruction.util;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.properties.AbstractPropertyHolder;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public abstract class AbstractNode extends AbstractPropertyHolder implements Node {
    private TypeInformal type = Types.VOID;
    private Verification<TypeInformal> verification = Verification.success(this, Types.VOID);

    private TypeInformal expected;

    @Override
    public Verification<TypeInformal> verify(Environment environment, CodeContext context) {
        doExpected(environment, context);

        Verification<Node> childrenPre = doChildren(Child.PRE, environment, context);

        if (childrenPre.success()) {
            Verification<TypeInformal> verification = this.doVerify(environment, context);
            this.verification = verification;
            verification.value().ifPresent(t -> this.type = t);

            if (verification.success()) {
                Verification<Node> childrenPost = doChildren(Child.POST, environment, context);

                if (childrenPost.success()) {
                    return verification.toBuilder()
                            .source(this)
                            .children(childrenPre, childrenPost)
                            .build();
                } else {
                    return verification.toBuilder()
                            .source(this)
                            .children(childrenPre, childrenPost)
                            .andChildren()
                            .build();
                }
            }

            return verification.toBuilder().child(childrenPre).build();
        } else {
            Verification<TypeInformal> verification = Verification.<TypeInformal>builder()
                    .child(childrenPre)
                    .andChildren()
                    .build();
            this.verification = verification;
            return verification;
        }
    }

    private Verification<Node> doChildren(int order, Environment environment, CodeContext context) {
        VerificationBuilder<Node> builder = Verification.builder();
        builder.source(this).value(this);

        List<Field> fields = new ArrayList<>();
        walkFields(this.getClass(), fields);

        for (Field field : fields) {
            if (field.isAnnotationPresent(Child.class)) {
                Child child = field.getAnnotation(Child.class);
                if (child.order() == order) {
                    Class<?> type = field.getType();
                    field.trySetAccessible();

                    try {
                        if (Node.class.isAssignableFrom(type)) {
                            Node node = (Node) field.get(this);

                            if (node != null) {
                                CodeContext codeContext = child.scope() == Child.SUB_SCOPE || child.scope() == Child.SHARED_SUB_SCOPE ? context.childScope() : context;
                                codeContext = child.instance() == Child.INSTANCE || child.instance() == Child.SHARED_INSTANCE ? codeContext.copy() : codeContext;

                                Verification<TypeInformal> childVerify = node.verify(environment, codeContext);
                                builder.child(childVerify);
                                if (!childVerify.success() && child.optional() != Child.OPTIONAL) {
                                    builder.error(Verify.Code.CHILD_FAILED_ERROR, "One or more required children failed");
                                }
                            }
                        } else if (Iterable.class.isAssignableFrom(type)) {
                            Iterable<?> list = (Iterable<?>) field.get(this);
                            if (list != null) {
                                if (StreamSupport.stream(list.spliterator(), false).allMatch(n -> n instanceof Node)) {
                                    Iterable<Node> nodes = (Iterable<Node>) list;

                                    CodeContext codeContext = child.scope() == Child.SHARED_SUB_SCOPE ? context.childScope() : context;
                                    codeContext = child.instance() == Child.SHARED_INSTANCE ? codeContext.copy() : context;

                                    boolean success = child.optional() != Child.ONE_REQUIRED;

                                    for (Node node : nodes) {
                                        if (node != null) {
                                            CodeContext myContext = child.scope() == Child.SUB_SCOPE ? codeContext.childScope() : codeContext;
                                            myContext = child.instance() == Child.INSTANCE ? myContext.copy() : myContext;

                                            Verification<TypeInformal> childVerify = node.verify(environment, myContext);
                                            builder.child(childVerify);

                                            if (child.optional() == Child.ONE_REQUIRED && childVerify.success()) {
                                                success = true;
                                            } else if (child.optional() == Child.REQUIRED) {
                                                success &= childVerify.success();
                                            }
                                        }
                                    }

                                    if (!success) {
                                        if (child.optional() == Child.ONE_REQUIRED) {
                                            builder.error(Verify.Code.CHILD_FAILED_ERROR, "All children failed");
                                        } else if (child.optional() == Child.REQUIRED) {
                                            builder.error(Verify.Code.CHILD_FAILED_ERROR, "One or more children failed");
                                        }
                                    }
                                } else {
                                    throw new InvalidNodeException("@Child annotated Iterable " + field.getDeclaringClass().getName() + "#" + field.getName() +
                                            " must only contain values of type Node, but non-node values were found");
                                }
                            }
                        } else {
                            throw new InvalidNodeException("@Child annotated field " + field.getDeclaringClass().getName() + "#" + field.getName() +
                                    " must be of type Node or Iterable<Node>, but was of type " + type.getName());
                        }
                    } catch (IllegalAccessException ex) {
                        throw new InvalidNodeException("@Child annotated field " + field.getDeclaringClass().getName() + "#" + field.getName() +
                                "was not accessible", ex);
                    }
                }
            }
        }

        return builder.build();
    }

    private void walkFields(Class<?> cls, List<Field> fields) {
        Collections.addAll(fields, cls.getDeclaredFields());

        if (cls.getSuperclass() != null) {
            walkFields(cls.getSuperclass(), fields);
        }

        for (Class<?> inter : cls.getInterfaces()) {
            walkFields(inter, fields);
        }
    }

    protected abstract Verification<TypeInformal> doVerify(Environment environment, CodeContext context);

    @Override
    public void apply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        this.doApply(adapter, environment, context);
    }

    protected abstract void doApply(InstructionAdapter adapter, Environment environment, CodeContext context);

    @Override
    public Verification<TypeInformal> verification() {
        return this.verification;
    }

    @Override
    public TypeInformal type() {
        return this.type;
    }

    @Override
    public void setExpected(TypeInformal type) {
        this.expected = type;
    }

    @Override
    public TypeInformal expected() {
        return this.expected;
    }

    protected void doExpected(Environment environment, CodeContext context) {

    }

}
