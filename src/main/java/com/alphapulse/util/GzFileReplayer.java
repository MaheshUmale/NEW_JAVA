package com.alphapulse.util;

import com.alphapulse.core.AlphaEngine;
import com.alphapulse.model.GzJsonTick;
import com.alphapulse.model.MarketTick;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * A utility to replay historical market data from gzipped JSON files.
 * This class is essential for backtesting the trading logic against historical data.
 */
public class GzFileReplayer {

    private final VirtualClock clock;
    private final AlphaEngine engine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructs a new GzFileReplayer.
     *
     * @param clock  The VirtualClock to be updated with tick timestamps.
     * @param engine The AlphaEngine that will process the replayed ticks.
     */
    public GzFileReplayer(VirtualClock clock, AlphaEngine engine) {
        if (clock.getMode() != VirtualClock.ClockMode.REPLAY) {
            throw new IllegalArgumentException("GzFileReplayer can only be used with a VirtualClock in REPLAY mode.");
        }
        this.clock = clock;
        this.engine = engine;
    }

    /**
     * Reads a gzipped file, replays the ticks, and sends them to the engine.
     *
     * @param filePath The path to the .gz file.
     * @throws IOException If there is an error reading the file.
     */
    public void replay(Path filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
             GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    GzJsonTick tick = objectMapper.readValue(line, GzJsonTick.class);

                    // Update the virtual clock with the exchange timestamp from the tick.
                    // This is the core of the deterministic replay.
                    clock.update(tick.exchange_ts());

                    // Pass the tick to the engine for processing.
                    engine.onTick(tick);

                } catch (Exception e) {
                    // Log the error and continue processing the next line.
                    // In a real system, you might use a more sophisticated logger.
                    System.err.println("Error processing tick line: " + line);
                    e.printStackTrace();
                }
            }
        }
    }
}
