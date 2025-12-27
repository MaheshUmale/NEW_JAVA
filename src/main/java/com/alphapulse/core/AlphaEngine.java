package com.alphapulse.core;

import com.alphapulse.model.MarketTick;

/**
 * The core logic unit of the AlphaPulse system.
 * This class will be responsible for calculating the alpha ratio, managing state,
 * and generating trading signals.
 *
 * For now, this is a placeholder to allow the GzFileReplayer to function.
 */
public class AlphaEngine {

    /**
     * The entry point for all market data into the engine.
     * This method will be called by the harvesters (live or replay) for each new tick.
     *
     * @param tick The market tick to be processed.
     */
    public void onTick(MarketTick tick) {
        // For now, we'll just print the tick to the console to verify the data flow.
        // In the future, this method will contain the core trading logic.
        System.out.println("AlphaEngine received tick: " + tick);
    }
}
