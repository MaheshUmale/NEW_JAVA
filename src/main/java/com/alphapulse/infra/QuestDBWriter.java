package com.alphapulse.infra;

import com.alphapulse.model.MarketTick;
import io.questdb.client.Sender;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles writing ticks to QuestDB using the InfluxDB Line Protocol (ILP).
 * This class runs on a separate thread to ensure non-blocking writes.
 */
public class QuestDBWriter implements AutoCloseable {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Sender sender;

    public QuestDBWriter() {
        // For this simulation, we will use a null sender.
        this.sender = null;
        System.out.println("QuestDBWriter initialized (simulation mode).");
    }

    public void write(MarketTick tick) {
        executor.submit(() -> {
            try {
                if (sender != null) {
                    Instant timestamp = Instant.ofEpochSecond(0, tick.exchange_ts());
                    sender.table("ticks")
                          .symbol("symbol", tick.symbol())
                          .doubleColumn("price", tick.price())
                          .longColumn("volume", tick.volume())
                          .at(timestamp);
                    sender.flush();
                } else {
                    // Simulate the write operation
                    String ilp = String.format("ticks,symbol=%s price=%f,volume=%d %d",
                            tick.symbol(), tick.price(), tick.volume(), tick.exchange_ts());
                    System.out.println("QuestDB -> " + ilp);
                }
            } catch (Exception e) {
                System.err.println("Error writing to QuestDB: " + e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        if (sender != null) {
            sender.close();
        }
        System.out.println("QuestDBWriter shut down.");
    }
}
