package com.alphapulse.core;

import com.alphapulse.events.AlphaSignalEvent;
import com.alphapulse.events.AlphaSignalListener;
import com.alphapulse.model.MarketTick;
import com.google.common.collect.EvictingQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * The core logic unit of the AlphaPulse system.
 * This class is responsible for calculating the alpha ratio, managing state,
 * and generating trading signals.
 */
public class AlphaEngine {

    private static final int WINDOW_SIZE = 10;
    private static final double ALPHA_THRESHOLD = 0.8;
    private static final double DELTA = 0.5; // Assuming a delta of 0.5 for at-the-money options
    private static final double ZERO_MOVE_THRESHOLD = 0.00001;
    private static final long STALE_DATA_THRESHOLD_NS = 1_000_000_000L; // 1 second

    private final String spotSymbol;
    private final String optionSymbol;

    private final Queue<MarketTick> spotTicks;
    private final Queue<MarketTick> optionTicks;
    private final List<AlphaSignalListener> listeners = new ArrayList<>();

    private double spotHighOfDay = Double.MIN_VALUE;

    /**
     * Constructs a new AlphaEngine.
     * @param spotSymbol   The symbol for the spot index (e.g., "NIFTY_50").
     * @param optionSymbol The symbol for the option contract.
     */
    public AlphaEngine(String spotSymbol, String optionSymbol) {
        this.spotSymbol = spotSymbol;
        this.optionSymbol = optionSymbol;
        this.spotTicks = EvictingQueue.create(WINDOW_SIZE);
        this.optionTicks = EvictingQueue.create(WINDOW_SIZE);
    }

    /**
     * Adds a listener to receive alpha signal events.
     *
     * @param listener The listener to add.
     */
    public void addListener(AlphaSignalListener listener) {
        listeners.add(listener);
    }

    /**
     * The entry point for all market data into the engine.
     *
     * @param tick The market tick to be processed.
     */
    public void onTick(MarketTick tick) {
        if (tick.symbol().equals(spotSymbol)) {
            spotTicks.add(tick);
            if (tick.price() > spotHighOfDay) {
                spotHighOfDay = tick.price();
            }
        } else if (tick.symbol().equals(optionSymbol)) {
            optionTicks.add(tick);
        }

        // Only calculate alpha if both windows are full.
        if (spotTicks.size() == WINDOW_SIZE && optionTicks.size() == WINDOW_SIZE) {
            calculateAlpha();
        }
    }

    private void calculateAlpha() {
        MarketTick oldestSpotTick = spotTicks.peek();
        MarketTick newestSpotTick = (MarketTick) spotTicks.toArray()[WINDOW_SIZE - 1];

        MarketTick oldestOptionTick = optionTicks.peek();
        MarketTick newestOptionTick = (MarketTick) optionTicks.toArray()[WINDOW_SIZE - 1];

        double spotPriceChange = newestSpotTick.price() - oldestSpotTick.price();
        double optionPriceChange = newestOptionTick.price() - oldestOptionTick.price();

        // The "Stale-Data Shield"
        long timeDifference = Math.abs(newestSpotTick.exchange_ts() - newestOptionTick.exchange_ts());
        if (timeDifference > STALE_DATA_THRESHOLD_NS) {
            System.out.println("Stale data detected. Discarding calculation.");
            return;
        }

        // The "Zero-Move Shield"
        if (isPriceChangeInsignificant(spotPriceChange) || isPriceChangeInsignificant(optionPriceChange)) {
            return;
        }

        double alpha = (spotPriceChange * DELTA) / optionPriceChange;

        // --- Event Emission ---
        AlphaSignalEvent event = new AlphaSignalEvent(
                newestSpotTick.exchange_ts() / 1_000_000, // Convert ns to ms for JS
                newestSpotTick.price(),
                newestOptionTick.price(),
                alpha
        );
        for (AlphaSignalListener listener : listeners) {
            listener.onAlphaSignal(event);
        }

        // --- Signal Logic ---
        // Logic: If α < 0.8 and Spot is at HOD, trigger a signal.
        if (alpha < ALPHA_THRESHOLD && newestSpotTick.price() >= spotHighOfDay) {
            System.out.println("!!! FAILED_AUCTION_SIGNAL !!!");
            System.out.printf("Time: %d, Alpha: %.2f, Spot Price: %.2f, HOD: %.2f%n",
                    newestSpotTick.exchange_ts(), alpha, newestSpotTick.price(), spotHighOfDay);
        }
    }

    private boolean isPriceChangeInsignificant(double priceChange) {
        return Math.abs(priceChange) < ZERO_MOVE_THRESHOLD;
    }
}
