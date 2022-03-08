package com.example.betterraycastingthethird;

import javafx.concurrent.Task;
import javafx.stage.Stage;

class Engine implements Runnable {

    Task<Integer> task = new Task<Integer>() {
        @Override protected Integer call() throws Exception {
            int iterations;
            for (iterations = 0; iterations < 100000; iterations++) {
                if (isCancelled()) {
                    break;
                }
                System.out.println("Iteration " + iterations);
            }
            return iterations;
        }
    };

    @Override
    public void run() {

    }
}
