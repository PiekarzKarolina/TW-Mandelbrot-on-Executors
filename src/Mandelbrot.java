import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import javax.swing.*;

public class Mandelbrot extends JFrame {
    private BufferedImage I;

    private Mandelbrot(){
        final int MAX_ITER = 15570;
        final double ZOOM = 150;
        double zx, zy, cX, cY, tmp;

        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
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
                I.setRGB(x, y, iter | (iter << 8));
            }
        }
    }


    private Mandelbrot(int param, String type) {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        int h;
        ExecutorService pool;
        Set<Future<Map<Point, Integer>>> set = new HashSet<>();

        switch(type){
            case "SingleThreadExecutor":
                pool = Executors.newSingleThreadExecutor();

                h = getHeight()/10;

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
                {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * 10, getHeight());
                Future<Map<Point, Integer>> future = pool.submit(callable);
                set.add(future);
                }
                break;
            case "FixedThreadPool":
                int nThreads = param;

                pool = Executors.newFixedThreadPool(nThreads);

                h = getHeight()/(nThreads*10);

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
                {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * nThreads * 10, getHeight());
                Future<Map<Point, Integer>> future = pool.submit(callable);
                set.add(future);
                }
                break;
            case "WorkStealingPool":
                int parallelism = param;

                pool = Executors.newWorkStealingPool(parallelism);

                h = getHeight()/(parallelism*10);

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
                {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * parallelism * 10, getHeight());
                Future<Map<Point, Integer>> future = pool.submit(callable);
                set.add(future);
                }
                break;
            case "CachedThreadPool":
                pool = Executors.newCachedThreadPool();

                h = getHeight()/10;

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
            {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * 10, getHeight());
                Future<Map<Point, Integer>> future = pool.submit(callable);
                set.add(future);
            }
                break;
            default:
        }

        for (Future<Map<Point, Integer>> future : set) {
            try {
                Map<Point, Integer> map = future.get();
                for(Map.Entry<Point, Integer> entry : map.entrySet()){
                    int iter = entry.getValue();
                    int x = entry.getKey().getX();
                    int y = entry.getKey().getY();
                    I.setRGB(x, y,iter | (iter << 8));
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void paint(Graphics g) {
        g.drawImage(I, 0, 0, this);
    }

    public static void main(String[] args) {

        long t0 = System.nanoTime();
        new Mandelbrot(0, "SingleThreadExecutor").setVisible(true);
        long t1 = System.nanoTime();
        System.out.println("SingleThreadExecutor    " + (t1-t0));

        t0 = System.nanoTime();
        new Mandelbrot(5, "FixedThreadPool").setVisible(true);
        t1 = System.nanoTime();
        System.out.println("FixedThreadPool         " + (t1-t0));

        t0 = System.nanoTime();
        new Mandelbrot(5, "WorkStealingPool").setVisible(true);
        t1 = System.nanoTime();
        System.out.println("WorkStealingPool        " + (t1-t0));

        t0 = System.nanoTime();
        new Mandelbrot(0, "CachedThreadPool").setVisible(true);
        t1 = System.nanoTime();
        System.out.println("CachedThreadPool        " + (t1-t0));

        t0 = System.nanoTime();
        new Mandelbrot().setVisible(true);
        t1 = System.nanoTime();
        System.out.println("noExecutors             " + (t1-t0));
    }
}