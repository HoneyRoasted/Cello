package honeyroasted.cello.environment.context;

import honeyroasted.cello.node.structure.MethodNode;

import java.util.Optional;

public class CodeContext {
    private MethodNode owner;
    private LocalScope scope;
    private CommonContext common;
    private boolean withinTryFinally;

    public CodeContext(MethodNode owner, LocalScope scope, CommonContext common, boolean withinTryFinally) {
        this.owner = owner;
        this.scope = scope;
        this.withinTryFinally = withinTryFinally;
        this.common = common;
    }

    public MethodNode owner() {
        return this.owner;
    }

    public LocalScope scope() {
        return this.scope;
    }

    public CodeContext copy() {
        return new CodeContext(this.owner, this.scope.copy(), this.common, this.withinTryFinally);
    }

    public CodeContext childScope() {
        return new CodeContext(this.owner, this.scope.child(), this.common, this.withinTryFinally);
    }

    public CommonContext common() {
        return this.common;
    }

    public boolean withinTryFinally() {
        return this.withinTryFinally;
    }

    public CodeContext withinTryFinally(boolean withinTryFinally) {
        this.withinTryFinally = withinTryFinally;
        return this;
    }
}
