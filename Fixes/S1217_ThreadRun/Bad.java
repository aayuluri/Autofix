package Fixes.S1217_ThreadRun;

/**
 * Created by kanna on 7/31/17.
 */
public class Bad {
    public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();
        Thread myThread = new Thread(runnable);
        myThread.run();
    }

    public static class MyRunnable implements Runnable {

        @Override
        public void run() {
            System.out.println("Running my run, but not a correct way");
        }

    }
}
