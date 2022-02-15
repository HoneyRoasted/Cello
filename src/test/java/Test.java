import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.bytecode.BytecodeEnvironment;
import honeyroasted.cello.environment.bytecode.provider.RuntimeBytecodeProvider;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.environment.context.LocalScope;
import honeyroasted.cello.node.instruction.Nodes;
import honeyroasted.cello.node.instruction.invoke.InvokeVirtual;
import honeyroasted.cello.node.structure.ClassNode;
import honeyroasted.cello.node.structure.MethodNode;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.reflection.Token;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Test implements Serializable {

    public static void main(String[] args) {
        Environment environment = new BytecodeEnvironment(new RuntimeBytecodeProvider());

        ClassNode ownerCls = environment.lookup(Objects.class).value().get();
        MethodNode ownerMeth = ownerCls.lookupMethods(m -> m.name().equals("toString")).get(0);

        CodeContext context = new CodeContext(ownerMeth, new LocalScope());

        InvokeVirtual virtual = new InvokeVirtual(Nodes.defaultValue(new Token<List<Number>>(){}.resolve()), "add", List.of(Nodes.constant(5)));
        System.out.println(virtual.verify(environment, context).format());
        System.out.println(Nodes.convert(Nodes.constant(null), Types.type(String.class)).verify(environment, context).format());
    }

}
