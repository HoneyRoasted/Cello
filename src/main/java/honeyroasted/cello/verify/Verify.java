package honeyroasted.cello.verify;

public interface Verify {

    enum Level {
        SUCCESS(0),
        DEBUG(0),
        WARNING(5),
        ERROR(10);

        private final int weight;

        Level(int weight) {
            this.weight = weight;
        }

        public int weight() {
            return this.weight;
        }
    }

    enum Code {
        SUCCESS,

        UNKNOWN_ERROR,
        CHILD_FAILED_ERROR,
        FAILED_MAPPING_ERROR,

        TYPE_ERROR,
        VAR_NOT_FOUND_ERROR,
        VAR_ALREADY_DEFINED_ERROR,
        INVALID_TYPE_ERROR,
        INVALID_CONSTANT_ERROR,
        CONTROL_FLOW_ERROR,
        ILLEGAL_CAST_ERROR,
        INVALID_ANNOTATION_ERROR,
        TYPE_NOT_FOUND_ERROR,
        DUPLICATE_TYPE_VAR,
        INVALID_OPERATOR, LABEL_NOT_FOUND, FIELD_NOT_FOUND_ERROR, METHOD_NOT_FOUND, CATCH_TYPE_CLASH, UNREACHABLE_CODE, THIS_NOT_AVAILABLE_ERROR
    }

}
