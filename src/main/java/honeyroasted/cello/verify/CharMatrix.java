package honeyroasted.cello.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharMatrix {
    private int height = 10, length = 10;
    private char[][] matrix = new char[10][10];

    public CharMatrix() {
        for (char[] arr : matrix) {
            Arrays.fill(arr, ' ');
        }
    }

    public int height() {
        return this.height;
    }

    public int length() {
        return this.length;
    }

    public void insert(int row) {
        expand(this.height + 1, this.length);

        for (int y = matrix.length - 1; y >= row + 1; y--) {
            System.arraycopy(matrix[y - 1], 0, matrix[y], 0, length);
        }
        Arrays.fill(matrix[row], ' ');
    }

    public void set(int row, int col, char c) {
        expand(row, col);
        matrix[row][col] = c;
    }

    public char get(int row, int col) {
        expand(row, col);
        return matrix[row][col];
    }

    public void write(int row, int col, String str) {
        char[] c = str.toCharArray();
        expand(row, col + c.length);
        System.arraycopy(c, 0, matrix[row], col, c.length);
    }

    public void write(int row, String str) {
        write(row, 0, str);
    }

    public void write(int rowA, int colA, int rowB, int colB, char c) {
        expand(rowB, colB);
        for (int row = rowA; row <= rowB; row++) {
            Arrays.fill(matrix[row], colA, colB, c);
        }
    }

    public int start(int row) {
        char[] c = this.matrix[row];
        for (int i = 0; i < c.length; i++) {
            if (!Character.isWhitespace(c[i])) {
                return i;
            }
        }

        return c.length;
    }

    public int end(int row) {
        char[] c = this.matrix[row];

        for (int i = c.length - 1; i >= 0; i--) {
            if (!Character.isWhitespace(c[i])) {
                return i + 1;
            }
        }

        return 0;
    }

    public String toString() {
        List<String> lines = new ArrayList<>();
        for (int row = 0; row < this.height; row++) {
            lines.add(new String(this.matrix[row]).substring(0, end(row)));
        }

        while (!lines.isEmpty() && lines.get(lines.size() - 1).trim().isEmpty()) {
            lines.remove(lines.size() - 1);
        }

        return String.join(System.lineSeparator(), lines);
    }

    private void expand(int row, int col) {
        if (row <= this.height || col <= this.length) {
            int newHeight = Math.max(row + 1, this.height);
            int newLength = Math.max(col + 1, this.length);
            char[][] newMatrix = new char[newHeight][newLength];

            for (int i = 0; i < this.matrix.length; i++) {
                char[] src = this.matrix[i];
                char[] dst = newMatrix[i];
                System.arraycopy(src, 0, dst, 0, src.length);
                if (src.length < dst.length) {
                    Arrays.fill(dst, src.length, dst.length, ' ');
                }
            }

            for (int i = this.matrix.length; i < newMatrix.length; i++) {
                Arrays.fill(newMatrix[i], ' ');
            }

            this.matrix = newMatrix;
            this.height = newHeight;
            this.length = newLength;
        }
    }

}
