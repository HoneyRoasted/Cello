package honeyroasted.cello.environment.context;

import java.util.Optional;

public class CommonContext {
    private Var delayedReturn;

    public Optional<Var> delayedReturn() {
        return Optional.ofNullable(this.delayedReturn);
    }

    public CommonContext delayedReturn(Var delayedReturn) {
        this.delayedReturn = delayedReturn;
        return this;
    }
}
