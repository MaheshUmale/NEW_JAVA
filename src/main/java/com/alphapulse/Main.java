package com.alphapulse;

import com.alphapulse.core.AlphaEngine;
import com.alphapulse.util.GzFileReplayer;
import com.alphapulse.util.VirtualClock;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The main entry point for the AlphaPulse trading system.
 * This class is responsible for initializing and orchestrating the various components of the system.
 */
public class Main {

    public static void main(String[] args) {
        // We will run in REPLAY mode for this test.
        VirtualClock clock = new VirtualClock(VirtualClock.ClockMode.REPLAY);
        AlphaEngine engine = new AlphaEngine();
        GzFileReplayer replayer = new GzFileReplayer(clock, engine);

        // This assumes you have a 'data' directory in your project root
        // with a sample 'ticks.json.gz' file.
        Path dataFile = Paths.get("data", "ticks.json.gz");

        System.out.println("Starting replay of file: " + dataFile.toAbsolutePath());

        try {
            replayer.replay(dataFile);
            System.out.println("Replay finished successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred during file replay.");
            e.printStackTrace();
        }
    }
}
