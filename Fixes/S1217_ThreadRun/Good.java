package Fixes.S1217_ThreadRun;

/**
 * Created by kanna on 7/31/17.
 */
public class Good {
    public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();
        Thread myThread = new Thread(runnable);
        myThread.start();
    }

    public static class MyRunnable implements Runnable {

        @Override
        public void run() {
            System.out.println("Running my run, in a correct way");
        }

    }
}
