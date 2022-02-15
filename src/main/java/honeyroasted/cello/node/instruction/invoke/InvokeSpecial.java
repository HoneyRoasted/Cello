package honeyroasted.cello.node.instruction.invoke;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.instruction.Node;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.util.AbstractNode;
import honeyroasted.cello.node.instruction.util.Child;
import honeyroasted.cello.node.instruction.val.Convert;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.Verify;
import honeyroasted.javatype.Namespace;
import honeyroasted.javatype.informal.TypeArray;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeInformal;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvokeSpecial extends AbstractNode implements Node {
    @Child(order = Child.BOTH)
    private Node source;
    private Namespace sourceType;
    private String name;
    @Child(order = Child.BOTH)
    private List<Node> parameters;

    public InvokeSpecial(Node source, Namespace sourceType, String name, List<Node> parameters) {
        this.source = source;
        this.sourceType = sourceType;
        this.name = name;
        this.parameters = parameters;
    }

    private FilledMethod target;

    @Override
    protected Verification<TypeInformal> doVerify(Environment environment, CodeContext context) {


        Verification<FilledMethod> target = InvokeVirtual.lookupMethod(this, this.sourceType, this.name, this.parameters, environment, context, false, true);
        if (target.success() && target.value().isPresent()) {
            Optional<TypeClass> supertype = this.source.type().supertype(target.value().get().method().owner().parameterizedType());
            if(supertype.isPresent()) {
                this.source = Nodes.convert(this.source, supertype.get());
            } else {
                return Verification.error(Verify.Code.TYPE_ERROR, "%s is not assignable to %s", this.source.type().externalName(), target.value().get().method().owner().externalName());
            }

            this.target = target.value().get();
            List<TypeInformal> paramTypes = this.target.params();
            if (this.target.method().modifiers().has(Modifier.VARARGS) && !paramTypes.isEmpty()) {
                paramTypes.remove(paramTypes.size() - 1);
            }

            List<Node> convertParams = new ArrayList<>();
            for (int i = 0; i < paramTypes.size(); i++) {
                convertParams.add(Nodes.convert(this.parameters.get(i), paramTypes.get(i)));
            }

            if (this.target.method().modifiers().has(Modifier.VARARGS)) {
                TypeInformal vararg = this.target.params().get(this.target.params().size() - 1);
                if (vararg instanceof TypeArray arr) {
                    TypeInformal element = arr.element();
                    for (int i = paramTypes.size(); i < this.parameters.size(); i++) {
                        convertParams.add(Nodes.convert(this.parameters.get(i), element));
                    }
                }
            }

            this.parameters = convertParams;
        }
        return target.map(FilledMethod::ret);
    }

    @Override
    protected void doApply(InstructionAdapter adapter, Environment environment, CodeContext context) {
        int size = this.target.params().size();
        if (this.target.method().modifiers().has(Modifier.VARARGS)) {
            size -= 1;
        }

        for (int i = 0; i < size; i++) {
            this.parameters.get(i).apply(adapter, environment, context);
        }

        if (this.target.method().modifiers().has(Modifier.VARARGS)) {
            int arrSize = this.parameters.size() - size;
            adapter.iconst(arrSize);
            TypeArray arr = (TypeArray) this.target.method().erased().parameters().get(size);
            adapter.newarray(TypeUtil.asmType(arr.element()));
            for (int i = 0; i < arrSize; i++) {
                adapter.dup();
                adapter.iconst(i);
                Node curr = this.parameters.get(i + size);
                curr.apply(adapter, environment, context);
                adapter.astore(TypeUtil.asmType(arr.element()));
            }
        }

        this.source.apply(adapter, environment, context);
        adapter.invokespecial(this.target.method().owner().parameterizedType().internalName(), this.name,
                this.target.method().type().descriptor(),
                this.target.method().owner().modifiers().has(Modifier.INTERFACE));
    }

}
