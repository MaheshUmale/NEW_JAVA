package com.alphapulse.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An immutable implementation of MarketTick specifically for parsing
 * historical data from gzipped JSON files.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class GzJsonTick implements MarketTick {

    private final String symbol;
    private final double price;
    private final long volume;
    private final long exchange_ts;

    /**
     * Constructs a new GzJsonTick.
     * This constructor is designed to be used by Jackson for JSON deserialization.
     *
     * @param symbol      The instrument symbol.
     * @param price       The tick price.
     * @param volume      The tick volume.
     * @param exchange_ts The exchange timestamp in nanoseconds.
     */
    @JsonCreator
    public GzJsonTick(@JsonProperty("symbol") String symbol,
                      @JsonProperty("price") double price,
                      @JsonProperty("v") long volume,
                      @JsonProperty("exchange_ts") long exchange_ts) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
        this.exchange_ts = exchange_ts;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public double price() {
        return price;
    }

    @Override
    public long volume() {
        return volume;
    }

    @Override
    public long exchange_ts() {
        return exchange_ts;
    }

    @Override
    public String toString() {
        return "GzJsonTick{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", volume=" + volume +
                ", exchange_ts=" + exchange_ts +
                '}';
    }
}
