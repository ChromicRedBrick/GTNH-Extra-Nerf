package pl.kuba6000.gtnhextranerf.progression.exam;

import java.util.BitSet;

/** Bounded, client-local pencil raster. Only its four-bit answer mask is submitted to the server. */
public final class AnswerSheet {

    @FunctionalInterface
    public interface InkRun {

        void draw(int x, int y, int length);
    }

    private final int width;
    private final int height;
    private final int[][] boxes;
    private final BitSet ink;

    public AnswerSheet(int width, int height, int[][] boxes) {
        if (width < 1 || width > 512 || height < 1 || height > 32768 || boxes.length < 1 || boxes.length > 4)
            throw new IllegalArgumentException("Invalid answer sheet size");
        this.width = width;
        this.height = height;
        this.boxes = new int[boxes.length][];
        for (int i = 0; i < boxes.length; i++) {
            int[] box = boxes[i];
            if (box.length != 4 || box[0] < 0
                || box[1] < 0
                || box[2] < 1
                || box[3] < 1
                || box[0] + box[2] > width
                || box[1] + box[3] > height) throw new IllegalArgumentException("Invalid answer box");
            this.boxes[i] = box.clone();
        }
        this.ink = new BitSet(width * height);
    }

    public int height() {
        return height;
    }

    public void line(int x0, int y0, int x1, int y1, boolean erase) {
        double dx = (double) x1 - x0, dy = (double) y1 - y0;
        double[] range = { 0, 1 };
        if (!clip(-dx, x0, range) || !clip(dx, width - 1.0 - x0, range)
            || !clip(-dy, y0, range)
            || !clip(dy, height - 1.0 - y0, range)) return;
        int startX = (int) Math.round(x0 + range[0] * dx);
        int startY = (int) Math.round(y0 + range[0] * dy);
        int endX = (int) Math.round(x0 + range[1] * dx);
        int endY = (int) Math.round(y0 + range[1] * dy);
        int steps = Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
        for (int step = 0; step <= steps; step++) {
            double t = steps == 0 ? 0 : (double) step / steps;
            stamp(
                (int) Math.round(startX + (endX - startX) * t),
                (int) Math.round(startY + (endY - startY) * t),
                erase);
        }
    }

    private static boolean clip(double p, double q, double[] range) {
        if (p == 0) return q >= 0;
        double t = q / p;
        if (p < 0) {
            if (t > range[1]) return false;
            range[0] = Math.max(range[0], t);
        } else {
            if (t < range[0]) return false;
            range[1] = Math.min(range[1], t);
        }
        return true;
    }

    private void stamp(int x, int y, boolean erase) {
        int radius = erase ? 3 : 0;
        for (int row = Math.max(0, y - radius); row <= Math.min(height - 1, y + radius); row++) {
            int first = Math.max(0, x - radius), last = Math.min(width - 1, x + radius);
            if (first <= last) ink.set(row * width + first, row * width + last + 1, !erase);
        }
    }

    public int selectedMask() {
        int mask = 0;
        for (int i = 0; i < boxes.length; i++) {
            int[] box = boxes[i];
            for (int y = box[1]; y < box[1] + box[3]; y++) {
                int pixel = ink.nextSetBit(y * width + box[0]);
                if (pixel >= 0 && pixel < y * width + box[0] + box[2]) {
                    mask |= 1 << i;
                    break;
                }
            }
        }
        return mask;
    }

    public void drawInk(InkRun renderer) {
        for (int pixel = ink.nextSetBit(0); pixel >= 0;) {
            int y = pixel / width, x = pixel % width;
            int end = Math.min(ink.nextClearBit(pixel), (y + 1) * width);
            renderer.draw(x, y, end - pixel);
            pixel = ink.nextSetBit(end);
        }
    }
}
