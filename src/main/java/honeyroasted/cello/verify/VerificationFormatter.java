package honeyroasted.cello.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VerificationFormatter {

    private static final int lineCount = 2;
    private static final String indentStr = "    ";

    public static String format(Verification<?> verification, boolean useUnicode, Verify.Level level) {
        if (verification.level().weight() < level.weight()) {
            return " 0. " + verification.code() + ": " + verification.message();
        }

        CharMatrix matrix = new CharMatrix();
        List<Box> boxes = new ArrayList<>();

        formatMessage(verification, 0, 0, 0, matrix, boxes, level);

        Map<Box, Integer> columns = new HashMap<>();
        walkBoxColumns(matrix, boxes, columns);
        walk(boxes, box -> {
            if (!box.children().isEmpty()) {
                int max = box.children().stream().mapToInt(columns::get).max().getAsInt();
                box.children.forEach(b -> columns.put(b, max));
            }
        });

        walk(boxes, box -> {
            int startCol = box.indent * indentStr.length();
            int endCol = columns.get(box);

            int cs1 = matrix.start(box.start());
            int ce1 = matrix.end(box.start());

            int cs2 = matrix.start(box.end());
            int ce2 = matrix.end(box.end());

            for (int col = startCol; col <= endCol; col++) {
                if (col < cs1 || col >= ce1) {
                    if (Character.isWhitespace(matrix.get(box.start(), col))) {
                        char c;
                        if (col == startCol) {
                            c = useUnicode ? '\u250c' : '#';
                        } else if (col == endCol) {
                            c = useUnicode ? '\u2510' : '#';
                        } else {
                            c = useUnicode ? '\u2500' : '-';
                        }
                        matrix.set(box.start(), col, c);
                    }
                }

                if (col <= cs2 || col >= ce2) {
                    if (Character.isWhitespace(matrix.get(box.end(), col))) {
                        char c;
                        if (col == startCol) {
                            c = useUnicode ? '\u2514' : '#';
                        } else if (col == endCol) {
                            c = useUnicode ? '\u2518' : '#';
                        } else {
                            c = useUnicode ? '\u2500' : '-';
                        }
                        matrix.set(box.end(), col, c);
                    }
                }
            }

            for (int row = box.start() + 1; row < box.end(); row++) {
                if (Character.isWhitespace(matrix.get(row, startCol))) {
                    matrix.set(row, startCol, useUnicode ? '\u2502' : '|');
                }

                if (Character.isWhitespace(matrix.get(row, endCol))) {
                    matrix.set(row, endCol, useUnicode ? '\u2502' : '|');
                }
            }

        });

        return matrix.toString();
    }

    private static void walk(List<Box> boxes, Consumer<Box> consumer) {
        for (Box box : boxes) {
            walk(box.children(), consumer);
        }

        boxes.forEach(consumer);
    }

    private static void walkBoxColumns(CharMatrix matrix, List<Box> boxes, Map<Box, Integer> columns) {
        for (Box box : boxes) {
            walkBoxColumns(matrix, box.children(), columns);
        }

        for (Box box : boxes) {
            int max = 0;
            for (int row = box.start(); row < box.end(); row++) {
                max = Math.max(max, matrix.end(row));
            }

            int[] column = {max};

            while (box.children().stream().anyMatch(b -> columns.get(b) >= column[0])) {
                column[0] += 3;
            }

            columns.put(box, column[0]);
        }
    }

    private static int formatMessage(Verification<?> verification, int indentCount, int row, int number, CharMatrix matrix, List<Box> boxes, Verify.Level level) {
        if (verification.level().weight() >= level.weight()) {
            String indent = indentStr.repeat(indentCount);
            int start = row;
            matrix.write(row++, indent + " " + number + ". " + verification.code() + ": " + verification.message());
            if (verification.exception().isPresent()) {
                Throwable err = verification.exception().get();

                while (err != null) {
                    matrix.write(row++, indent + indentStr + ": " + err.getClass().getName() + ": " + err.getMessage());
                    StackTraceElement[] elements = err.getStackTrace();
                    for (int i = 0; i < lineCount && i < elements.length; i++) {
                        matrix.write(row++, indent + indentStr + ": " + indentStr + elements[i]);
                    }
                    err = err.getCause();
                }

                for (Throwable sup : verification.exception().get().getSuppressed()) {
                    matrix.write(row++, indent + indentStr + ": " + sup.getClass().getName() + ": " + sup.getMessage());
                    StackTraceElement[] elements = sup.getStackTrace();
                    for (int i = 0; i < lineCount && i < elements.length; i++) {
                        matrix.write(row++, indent + indentStr + ": " + indentStr + elements[i]);
                    }
                }
            }

            if (!verification.children().isEmpty()) {
                matrix.write(row++, indent + "  Caused By:");

                List<Box> children = new ArrayList<>();

                int k = 1;

                for (Verification<?> v : verification.children()) {
                    row = formatMessage(v, indentCount + 1, row, k++, matrix, children, level);
                }

                int end = row;
                matrix.insert(row++);
                matrix.insert(row++);

                Box box = new Box(indentCount, start, end, children);
                boxes.add(box);
            }
        }

        return row;
    }

    private static class Box {
        private int indent, start, end;
        private List<Box> children;

        public Box(int indent, int start, int end, List<Box> children) {
            this.indent = indent;
            this.start = start;
            this.end = end;
            this.children = children;
        }

        public int indent() {
            return this.indent;
        }

        public int start() {
            return this.start;
        }

        public int end() {
            return this.end;
        }

        public List<Box> children() {
            return this.children;
        }
    }

}
