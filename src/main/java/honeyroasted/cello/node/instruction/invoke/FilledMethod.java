package honeyroasted.cello.node.instruction.invoke;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.TypeUtil;
import honeyroasted.cello.environment.context.CodeContext;
import honeyroasted.cello.node.modifier.Modifier;
import honeyroasted.cello.node.structure.MethodNode;
import honeyroasted.cello.node.structure.ParameterNode;
import honeyroasted.cello.verify.Verification;
import honeyroasted.cello.verify.VerificationBuilder;
import honeyroasted.javatype.Types;
import honeyroasted.javatype.informal.TypeArray;
import honeyroasted.javatype.informal.TypeClass;
import honeyroasted.javatype.informal.TypeFilled;
import honeyroasted.javatype.informal.TypeInformal;
import honeyroasted.javatype.method.TypeMethodFilled;
import honeyroasted.javatype.parameterized.TypeVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FilledMethod {
    private MethodNode method;
    private TypeInformal ret;
    private List<TypeInformal> params;

    public FilledMethod(MethodNode method, TypeInformal ret, List<TypeInformal> params) {
        this.method = method;
        this.ret = ret;
        this.params = params;
    }

    public MethodNode method() {
        return this.method;
    }

    public TypeInformal ret() {
        return this.ret;
    }

    public List<TypeInformal> params() {
        return this.params;
    }

    public static Verification<FilledMethod> attemptFill(MethodNode node, TypeInformal source, List<TypeInformal> params, Environment environment, CodeContext context) {
        VerificationBuilder<FilledMethod> builder = Verification.builder();

        Function<String, TypeInformal> preResolver = s -> null;
        if (!node.modifiers().has(Modifier.STATIC)) {
            Optional<TypeClass> opt = source.supertype(node.owner().parameterizedType());
            if (opt.isPresent() && opt.get() instanceof TypeFilled fld) {
                preResolver = s -> fld.argument(s).orElse(null);
            }
        }

        Map<TypeVar, List<TypeInformal>> vars = new HashMap<>();
        node.typeVars().forEach((s, t) -> vars.put(t, new ArrayList<>()));

        int size = node.parameters().size();
        if (node.modifiers().has(Modifier.VARARGS)) {
            size -= 1;
        }

        for (int i = 0; i < size; i++) {
            TypeInformal in = params.get(i);
            TypeInformal decl = node.parameters().get(i).type();
            Map<TypeVar, List<TypeInformal>> rel = Types.relativeVariables(decl, in);
            rel.forEach((t, l) -> {
                if (vars.containsKey(t)) {
                    vars.get(t).addAll(l);
                }
            });
        }

        if (node.modifiers().has(Modifier.VARARGS)) {
            List<TypeInformal> varargTypes = new ArrayList<>();
            for (int i = size; i < params.size(); i++) {
                varargTypes.add(params.get(i));
            }

            if (!varargTypes.isEmpty()) {
                Verification<TypeInformal> verif = TypeUtil.commonParent(environment, varargTypes);
                builder.child(verif);
                if (verif.success() && verif.value().isPresent()) {
                    TypeInformal type = verif.value().get();
                    ParameterNode last = node.parameters().get(node.parameters().size() - 1);
                    if (last.type() instanceof TypeArray arr) {
                        Types.relativeVariables(arr.element(), type).forEach((t, l) -> {
                            if (vars.containsKey(t)) {
                                vars.get(t).addAll(l);
                            }
                        });
                    }
                }
            }
        }

        vars.forEach((t, l) -> {
            if (l.isEmpty()) {
                l.add(Types.WILD);
            }
        });



        Map<String, TypeInformal> superVars = new HashMap<>();
        vars.forEach((var, list) -> {
            Verification<TypeInformal> parent = TypeUtil.commonParent(environment, list);
            builder.child(parent);
            if (parent.success() && parent.value().isPresent()) {
                superVars.put(var.name(), parent.value().get());
            }
        });

        Function<String, TypeInformal> finalPreResolver = preResolver;
        Function<String, TypeInformal> resolver = s -> {
            TypeInformal first = finalPreResolver.apply(s);
            if (first == null) {
                return superVars.get(s);
            }
            return first;
        };

        TypeInformal ret = node.returnType().resolveTypeVariables(resolver);
        List<TypeInformal> filledParams = node.parameters().stream().map(p -> p.type().resolveTypeVariables(resolver)).collect(Collectors.toList());
        builder.value(new FilledMethod(node, ret, filledParams));
        return builder.andChildren().build();
    }
}
