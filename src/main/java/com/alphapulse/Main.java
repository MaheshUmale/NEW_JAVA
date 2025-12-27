package com.alphapulse;

import com.alphapulse.core.AlphaEngine;
import com.alphapulse.infra.QuestDBReader;
import com.alphapulse.infra.QuestDBWriter;
import com.alphapulse.infra.UpstoxHarvester;
import com.alphapulse.util.GzFileReplayer;
import com.alphapulse.util.VirtualClock;
import com.alphapulse.model.MarketTick;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The main entry point for the AlphaPulse trading system.
 */
public class Main {

    public static void main(String[] args) {
        // --- Configuration ---
        final String spotSymbol = "NIFTY_50";
        final String optionSymbol = "NIFTY_50_OPT";
        final Path dataFile = Paths.get("data", "file5.json.gz");
        final int recoupMinutes = 30;

        // --- System Initialization ---
        VirtualClock.ClockMode mode = args.length > 0 && args[0].equalsIgnoreCase("LIVE")
                ? VirtualClock.ClockMode.LIVE
                : VirtualClock.ClockMode.REPLAY;

        System.out.println("Running in " + mode + " mode.");

        VirtualClock clock = new VirtualClock(mode);
        AlphaEngine engine = new AlphaEngine(spotSymbol, optionSymbol);

        try (QuestDBWriter writer = new QuestDBWriter()) {
            if (mode == VirtualClock.ClockMode.REPLAY) {
                // --- Replay Mode ---
                GzFileReplayer replayer = new GzFileReplayer(clock, engine, writer);
                System.out.println("Starting replay of file: " + dataFile.toAbsolutePath());
                replayer.replay(dataFile);
                System.out.println("Replay finished successfully.");
            } else {
                // --- Live Mode with Recoup Protocol ---
                System.out.println("--- Starting System Recoup Protocol ---");
                QuestDBReader reader = new QuestDBReader();
                List<MarketTick> historicalTicks = reader.fetchLastNMinutes(recoupMinutes);

                System.out.println("Warming up engine with historical data...");
                for (MarketTick tick : historicalTicks) {
                    engine.onTick(tick);
                }
                System.out.println("--- Engine warm-up complete. Switching to live data. ---");

                UpstoxHarvester harvester = new UpstoxHarvester(engine, writer);
                harvester.start();

                System.out.println("Live harvester is running. Press Ctrl+C to stop.");
                Thread.sleep(10000); // Run for 10 seconds
                harvester.close();
                harvester.awaitCompletion();
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
