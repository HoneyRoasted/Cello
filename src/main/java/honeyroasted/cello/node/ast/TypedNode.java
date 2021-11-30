package honeyroasted.cello.node.ast;

import honeyroasted.cello.environment.Environment;
import honeyroasted.cello.environment.Scope;
import honeyroasted.javatype.informal.TypeInformal;

public interface TypedNode extends CodeNode {

    TypeInformal type(Environment env, Scope scope);

}
