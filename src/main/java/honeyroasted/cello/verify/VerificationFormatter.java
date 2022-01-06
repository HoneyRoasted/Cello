package honeyroasted.cello.verify;

import javax.swing.Box;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VerificationFormatter {

    private static final int lineCount = 2;
    private static final String indentStr = "    ";

    public static String format(Verification<?> verification) {
        return format(verification, false);
    }

    public static String format(Verification<?> verification, boolean useUnicode) {
        CharMatrix matrix = new CharMatrix();
        List<Box> boxes = new ArrayList<>();

        formatMessage(verification, 0, 0, matrix, boxes);

        Map<Box, Integer> columns = new HashMap<>();
        walkBoxColumns(matrix, boxes, columns);

        walk(boxes, box -> {
            int startCol = box.indent * indentStr.length();
            int endCol = columns.get(box);

            for (int col = startCol; col <= endCol; col++) {
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

            while (box.children().stream().anyMatch(b -> columns.get(b) == column[0])) {
                column[0] += 2;
            }

            columns.put(box, column[0]);
        }
    }
    
    private static int formatMessage(Verification<?> verification, int indentCount, int row, CharMatrix matrix, List<Box> boxes) {
        String indent = indentStr.repeat(indentCount);
        
        if (verification.success()) {
            matrix.write(row++, indent + "> " + verification.message());
        } else {
            matrix.write(row++, indent + "> " + verification.errorCode() + ": " + verification.message() + 
                    (verification.error().isPresent() ? ", Java Exception: " : ""));

            if (verification.error().isPresent()) {
                Throwable err = verification.error().get();

                while (err != null) {
                    matrix.write(row++, indent + indentStr + ": " + err.getClass().getName() + ": " + err.getMessage());
                    StackTraceElement[] elements = err.getStackTrace();
                    for (int i = 0; i < lineCount && i < elements.length; i++) {
                        matrix.write(row++, indent + indentStr + ": " + indentStr + elements[i] + (i == lineCount - 1 && elements.length > lineCount ? "..." : "."));
                    }
                    err = err.getCause();
                }

                for (Throwable sup : verification.error().get().getSuppressed()) {
                    matrix.write(row++, indent + indentStr + ": " + sup.getClass().getName() + ": " + sup.getMessage());
                    StackTraceElement[] elements = sup.getStackTrace();
                    for (int i = 0; i < lineCount && i < elements.length; i++) {
                        matrix.write(row++, indent + indentStr + ": " + indentStr + elements[i] + (i == lineCount - 1 && elements.length > lineCount ? "..." : "."));
                    }
                }
            }
        }

        if (!verification.children().isEmpty()) {
            int start = row;
            matrix.write(row++, indent + " Caused By:");

            List<Box> children = new ArrayList<>();

            for (Verification<?> v : verification.children()) {
                row = formatMessage(v, indentCount + 1, row, matrix, children);
            }

            int end = row;
            matrix.insert(row++);

            Box box = new Box(indentCount, start, end, children);
            boxes.add(box);
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
