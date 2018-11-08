import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class PictureCallable implements Callable {
    private Map<Point, Integer> m;
    private int xp, xk, yp, yk;
    private double zx, zy, cX, cY, tmp;
    private final double ZOOM = 150;
    private final int MAX_ITER = 1570;


    public PictureCallable(int xp, int xk, int yp, int yk) {
        this.m = new HashMap<>();
        this.xp = xp;
        this.xk = xk;
        this.yp = yp;
        this.yk = yk;
    }

    public Map<Point, Integer> call() {
        for (int y = yp; y < yk; y++) {
            for (int x = xp; x < xk; x++) {
                zx = zy = 0;
                cX = (x - 400) / ZOOM;
                cY = (y - 300) / ZOOM;
                int iter = MAX_ITER;
                while (zx * zx + zy * zy < 4 && iter > 0) {
                    tmp = zx * zx - zy * zy + cX;
                    zy = 2.0 * zx * zy + cY;
                    zx = tmp;
                    iter--;
                }
                m.put(new Point(x, y), iter);
            }
        }
        return m;
    }
}