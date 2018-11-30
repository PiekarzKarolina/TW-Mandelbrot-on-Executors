import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import javax.swing.*;

public class Mandelbrot extends JFrame {
    private BufferedImage I;

    private Mandelbrot(int maxIter){
        int MAX_ITER = maxIter;
        final double ZOOM = 150;
        double zx, zy, cX, cY, tmp;
        Map<Point, Integer> m = new HashMap<>();

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
                m.put(new Point(x, y), iter);
            }
        }
        for(Map.Entry<Point, Integer> entry : m.entrySet()){
            int iter = entry.getValue();
            int x = entry.getKey().getX();
            int y = entry.getKey().getY();
            I.setRGB(x, y,iter | (iter << 8));
        }

    }


    private Mandelbrot(int param, String type, int maxIter) {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        int h;
        ExecutorService pool;
        Set<Future<Map<Point, Integer>>> set = new HashSet<>();

        switch(type) {
            case "SingleThreadExecutorNoCut":
                pool = Executors.newSingleThreadExecutor();
                {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), 0, getHeight(), maxIter);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
            break;
            case "SingleThreadExecutorCut":
                pool = Executors.newSingleThreadExecutor();

                h = getHeight()/(10*param);

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y, maxIter);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
                {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * 10, getHeight(), maxIter);
                Future<Map<Point, Integer>> future = pool.submit(callable);
                set.add(future);
                }
                break;
            case "FixedThreadPool":
                int nThreads = param;

                pool = Executors.newFixedThreadPool(nThreads);

                h = getHeight()/(nThreads*10);

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y, maxIter);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
                {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * nThreads * 10, getHeight(), maxIter);
                Future<Map<Point, Integer>> future = pool.submit(callable);
                set.add(future);
                }
                break;
            case "WorkStealingPool":
                int parallelism = param;

                pool = Executors.newWorkStealingPool(parallelism);

                h = getHeight()/(parallelism*10);

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y, maxIter);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
                {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * parallelism * 10, getHeight(), maxIter);
                Future<Map<Point, Integer>> future = pool.submit(callable);
                set.add(future);
                }
                break;
            case "CachedThreadPool":
                pool = Executors.newCachedThreadPool();

                h = getHeight()/(10*param);

                for(int y=h; y<getHeight(); y=y+h) {
                    Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), y-h, y, maxIter);
                    Future<Map<Point, Integer>> future = pool.submit(callable);
                    set.add(future);
                }
            {
                Callable<Map<Point, Integer>> callable = new PictureCallable(0, getWidth(), getHeight() - h * 10, getHeight(), maxIter);
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

    private static long getTime(int param, String executorName, int maxIter){

        long t0, t1;

        if(param==0 && executorName==null) {
            t0 = System.nanoTime();
            new Mandelbrot(maxIter).setVisible(true);
            t1 = System.nanoTime();
        }
        else{
            t0 = System.nanoTime();
            new Mandelbrot(param, executorName, maxIter).setVisible(true);
            t1 = System.nanoTime();
        }
        return t1-t0;
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(I, 0, 0, this);
    }

    public static void main(String[] args) {

        String fileName = "nowe_wyniki3.txt";

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter = new PrintWriter(fileWriter);

        int iterations = 5;

        long timeSingleThreadExecutorCut = 0;
        long timeSingleThreadExecutorNoCut = 0;
        long timeFixedThreadPool = 0;
        long timeWorkStealingPool = 0;
        long timeCachedThreadPool = 0;
        long timeNoExecutors = 0;

        for(int maxIter = 555; maxIter<5551; maxIter*=10) {
            for (int threads = 10; threads < 61; threads +=50) {
                printWriter.printf("Max iterations: %d%nThreads: %d%n", maxIter, threads);
                System.out.println("Max iterations: " + maxIter + ", threads: " + threads);

                for (int i = 0; i < iterations; i++) {
                    timeSingleThreadExecutorNoCut += getTime(threads, "SingleThreadExecutorNoCut", maxIter);
                    timeSingleThreadExecutorCut += getTime(threads, "SingleThreadExecutorCut", maxIter);
                    timeFixedThreadPool += getTime(threads, "FixedThreadPool", maxIter);
                    timeWorkStealingPool += getTime(threads, "WorkStealingPool", maxIter);
                    timeCachedThreadPool += getTime(threads, "CachedThreadPool", maxIter);
                    timeNoExecutors += getTime(0, null, maxIter);
                }

                printWriter.printf("SingleThreadExecutorNoCut    %d%n", timeSingleThreadExecutorNoCut / iterations);
                printWriter.printf("SingleThreadExecutorCut    %d%n", timeSingleThreadExecutorCut / iterations);
                printWriter.printf("FixedThreadPool         %d%n", timeFixedThreadPool / iterations);
                printWriter.printf("WorkStealingPool        %d%n", timeWorkStealingPool / iterations);
                printWriter.printf("CachedThreadPool        %d%n", timeCachedThreadPool / iterations);
                printWriter.printf("noExecutors             %d%n", timeNoExecutors / iterations);
            }
        }

        printWriter.close();
    }
}