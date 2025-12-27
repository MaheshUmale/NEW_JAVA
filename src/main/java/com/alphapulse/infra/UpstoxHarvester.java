package com.alphapulse.infra;

import com.alphapulse.core.AlphaEngine;
import com.alphapulse.model.MarketTick;
import com.alphapulse.model.GzJsonTick;
import java.util.concurrent.CountDownLatch;

/**
 * Connects to the Upstox WebSocket feed to handle live market data.
 */
public class UpstoxHarvester implements AutoCloseable {

    private final AlphaEngine engine;
    private final QuestDBWriter writer;
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean isRunning = true;

    public UpstoxHarvester(AlphaEngine engine, QuestDBWriter writer) {
        this.engine = engine;
        this.writer = writer;
    }

    public void start() {
        System.out.println("UpstoxHarvester started (simulation mode).");
        Thread simulationThread = new Thread(this::simulateDataStream);
        simulationThread.start();
    }

    private void simulateDataStream() {
        long timestamp = System.nanoTime();
        double spotPrice = 18000.0;
        double optionPrice = 100.0;

        while (isRunning) {
            try {
                Thread.sleep(500);

                spotPrice += Math.random() - 0.4;
                MarketTick spotTick = new GzJsonTick("NIFTY_50", spotPrice, 100, timestamp++);
                engine.onTick(spotTick);
                writer.write(spotTick);

                optionPrice += Math.random() - 0.4;
                MarketTick optionTick = new GzJsonTick("NIFTY_50_OPT", optionPrice, 50, timestamp++);
                engine.onTick(optionTick);
                writer.write(optionTick);

            } catch (InterruptedException e) {
                isRunning = false;
                Thread.currentThread().interrupt();
            }
        }
        latch.countDown();
    }

    public void awaitCompletion() throws InterruptedException {
        latch.await();
    }

    @Override
    public void close() {
        isRunning = false;
        System.out.println("UpstoxHarvester stopping...");
    }
}
