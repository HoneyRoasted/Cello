package honeyroasted.cello.node.instruction.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Child {
    int SUB_SCOPE = 0;
    int SHARED_SUB_SCOPE = 1;
    int NORMAL_SCOPE = 2;

    int INSTANCE = 0;
    int SHARED_INSTANCE = 1;
    int NO_INSTANCE = 2;

    int OPTIONAL = 0;
    int REQUIRED = 1;
    int ONE_REQUIRED = 2;

    int PRE = 0;
    int POST = 1;
    int BOTH = 2;

    int order() default PRE;

    int optional() default REQUIRED;

    int instance() default NO_INSTANCE;

    int scope() default NORMAL_SCOPE;

}
