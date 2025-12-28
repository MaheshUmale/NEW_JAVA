package com.alphapulse.events;

/**
 * A data transfer object representing a calculated alpha signal.
 * This object is immutable.
 */
public record AlphaSignalEvent(
    long time,
    double niftySpot,
    double optionPremium,
    double alpha
) {}
