package com.alphapulse.model;

/**
 * Universal interface for a market tick.
 * Ensures that all data, whether from live feeds or historical logs,
 * is treated identically by the trading engine.
 */
public interface MarketTick {
    /**
     * The symbol of the instrument.
     * @return the instrument symbol
     */
    String symbol();

    /**
     * The price of the tick.
     * @return the price
     */
    double price();

    /**
     * The volume of the tick.
     * @return the volume
     */
    long volume();

    /**
     * The timestamp from the exchange, in nanoseconds.
     * This is the definitive time for all deterministic calculations.
     * @return the exchange timestamp
     */
    long exchange_ts();
}
