package com.alphapulse.infra;

import com.alphapulse.model.GzJsonTick;
import com.alphapulse.model.MarketTick;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads historical data from QuestDB.
 * This class is a key component of the system's "recoup" protocol.
 */
public class QuestDBReader {

    /**
     * Fetches the last N minutes of tick data from QuestDB.
     *
     * @param minutes The number of minutes of historical data to fetch.
     * @return A list of MarketTick objects.
     */
    public List<MarketTick> fetchLastNMinutes(int minutes) {
        System.out.printf("QuestDBReader: Fetching last %d minutes of data (simulation mode).%n", minutes);

        // In a real implementation, you would query QuestDB here.
        // For this simulation, we will return a fixed list of ticks to warm up the engine.
        List<MarketTick> ticks = new ArrayList<>();
        long now = Instant.now().toEpochMilli() * 1_000_000L; // Nanoseconds
        double spotPrice = 17950.0;
        double optionPrice = 80.0;

        for (int i = 0; i < 20; i++) { // Generate 20 ticks for warm-up
            long timestamp = now - (20 - i) * 1_000_000_000L; // Ticks every second
            spotPrice += Math.random() - 0.4;
            optionPrice += Math.random() - 0.4;
            ticks.add(new GzJsonTick("NIFTY_50", spotPrice, 100, timestamp));
            ticks.add(new GzJsonTick("NIFTY_50_OPT", optionPrice, 50, timestamp));
        }

        System.out.printf("QuestDBReader: Found %d ticks for warm-up.%n", ticks.size());
        return ticks;
    }
}
