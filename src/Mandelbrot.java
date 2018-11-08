import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import javax.swing.JFrame;

public class Mandelbrot extends JFrame {
    private BufferedImage I;

    Mandelbrot() {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Set<Future<Map<Point, Integer>>> set = new HashSet<>();

        int h = getHeight()/10;

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
        for (Future<Map<Point, Integer>> future : set) {
            try {
                Map<Point, Integer> map = future.get();
                for(Map.Entry<Point, Integer> entry : map.entrySet()){
                    int iter = entry.getValue();
                    int x = entry.getKey().getX();
                    int y = entry.getKey().getY();
                    I.setRGB(x, y,iter | (iter << 8));
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    Mandelbrot(int nThreads) {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        Set<Future<Map<Point, Integer>>> set = new HashSet<>();

        int h = getHeight()/(nThreads*10);

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
        for (Future<Map<Point, Integer>> future : set) {
            try {
                Map<Point, Integer> map = future.get();
                for(Map.Entry<Point, Integer> entry : map.entrySet()){
                    int iter = entry.getValue();
                    int x = entry.getKey().getX();
                    int y = entry.getKey().getY();
                    I.setRGB(x, y,iter | (iter << 8));
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    Mandelbrot(int parallelism, String name) {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        ExecutorService pool = Executors.newWorkStealingPool(parallelism);
        Set<Future<Map<Point, Integer>>> set = new HashSet<>();

        int h = getHeight()/(parallelism*10);

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
        for (Future<Map<Point, Integer>> future : set) {
            try {
                Map<Point, Integer> map = future.get();
                for(Map.Entry<Point, Integer> entry : map.entrySet()){
                    int iter = entry.getValue();
                    int x = entry.getKey().getX();
                    int y = entry.getKey().getY();
                    I.setRGB(x, y,iter | (iter << 8));
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    Mandelbrot(String name) {
        super("Mandelbrot Set");
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        ExecutorService pool = Executors.newCachedThreadPool();
        Set<Future<Map<Point, Integer>>> set = new HashSet<>();

        int h = getHeight()/10;

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
        for (Future<Map<Point, Integer>> future : set) {
            try {
                Map<Point, Integer> map = future.get();
                for(Map.Entry<Point, Integer> entry : map.entrySet()){
                    int iter = entry.getValue();
                    int x = entry.getKey().getX();
                    int y = entry.getKey().getY();
                    I.setRGB(x, y,iter | (iter << 8));
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
        new Mandelbrot().setVisible(true);
        long t1 = System.nanoTime();
        System.out.println("SingleThreadExecutor " + (t1-t0));

        t0 = System.nanoTime();
        new Mandelbrot(5).setVisible(true);
        t1 = System.nanoTime();
        System.out.println("FixedThreadPool " + (t1-t0));

        t0 = System.nanoTime();
        new Mandelbrot(5, "WorkStealingPool").setVisible(true);
        t1 = System.nanoTime();
        System.out.println("WorkStealingPool " + (t1-t0));

        t0 = System.nanoTime();
        new Mandelbrot("CachedThreadPool").setVisible(true);
        t1 = System.nanoTime();
        System.out.println("CachedThreadPool " + (t1-t0));
    }
}