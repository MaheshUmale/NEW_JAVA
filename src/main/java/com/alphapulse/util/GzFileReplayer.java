package com.alphapulse.util;

import com.alphapulse.core.AlphaEngine;
import com.alphapulse.infra.QuestDBWriter;
import com.alphapulse.model.GzJsonTick;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

/**
 * A utility to replay historical market data from gzipped JSON files.
 */
public class GzFileReplayer {

    private final VirtualClock clock;
    private final AlphaEngine engine;
    private final QuestDBWriter writer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructs a new GzFileReplayer.
     *
     * @param clock  The VirtualClock to be updated with tick timestamps.
     * @param engine The AlphaEngine that will process the replayed ticks.
     * @param writer The QuestDBWriter that will persist the replayed ticks.
     */
    public GzFileReplayer(VirtualClock clock, AlphaEngine engine, QuestDBWriter writer) {
        if (clock.getMode() != VirtualClock.ClockMode.REPLAY) {
            throw new IllegalArgumentException("GzFileReplayer can only be used with a VirtualClock in REPLAY mode.");
        }
        this.clock = clock;
        this.engine = engine;
        this.writer = writer;
    }

    /**
     * Reads a gzipped file, replays ticks, and sends them to the engine and writer.
     *
     * @param filePath The path to the .gz file.
     * @throws IOException If there is an error reading the file.
     */
    public void replay(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             GZIPInputStream gzis = new GZIPInputStream(fis);
             InputStreamReader reader = new InputStreamReader(gzis);
             BufferedReader br = new BufferedReader(reader)) {

            String line;
            while ((line = br.readLine()) != null) {
                try {
                    JsonNode rootNode = objectMapper.readTree(line);
                    JsonNode feedsNode = rootNode.path("feeds");

                    feedsNode.fields().forEachRemaining(entry -> {
                        String symbol = entry.getKey();
                        JsonNode ltpcNode = entry.getValue().path("ff").path("marketFF").path("ltpc");

                        if (ltpcNode.isObject()) {
                            double price = ltpcNode.path("ltp").asDouble();
                            long volume = ltpcNode.path("ltq").asLong();
                            long exchange_ts = ltpcNode.path("ltt").asLong();

                            if (price > 0 && volume > 0 && exchange_ts > 0) {
                                GzJsonTick tick = new GzJsonTick(symbol, price, volume, exchange_ts);
                                clock.update(tick.exchange_ts());
                                engine.onTick(tick);
                                writer.write(tick);
                            }
                        }
                    });

                } catch (Exception e) {
                    System.err.println("Error processing tick line: " + line);
                    e.printStackTrace();
                }
            }
        }
    }
}
