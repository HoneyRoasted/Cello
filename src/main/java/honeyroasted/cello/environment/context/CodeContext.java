package honeyroasted.cello.environment.context;

import honeyroasted.cello.node.structure.MethodNode;

public class CodeContext {
    private MethodNode owner;
    private LocalScope scope;

    public CodeContext(MethodNode owner, LocalScope scope) {
        this.owner = owner;
        this.scope = scope;
    }

    public MethodNode owner() {
        return this.owner;
    }

    public LocalScope scope() {
        return this.scope;
    }

    public CodeContext copy() {
        return new CodeContext(this.owner, this.scope.copy());
    }

    public CodeContext childScope() {
        return new CodeContext(this.owner, this.scope.child());
    }
}
