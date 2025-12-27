package com.alphapulse.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A virtual clock that can operate in two modes: LIVE and REPLAY.
 * This is a critical component for achieving temporal determinism in the trading engine.
 */
public class VirtualClock {

    public enum ClockMode {
        LIVE,
        REPLAY
    }

    private final ClockMode mode;
    private final AtomicLong currentTime = new AtomicLong();

    /**
     * Creates a new VirtualClock.
     *
     * @param mode The mode in which the clock should operate.
     */
    public VirtualClock(ClockMode mode) {
        this.mode = mode;
        if (mode == ClockMode.LIVE) {
            this.currentTime.set(System.currentTimeMillis());
        }
    }

    /**
     * Returns the current time in milliseconds.
     * In LIVE mode, it returns the system's current time.
     * In REPLAY mode, it returns the timestamp of the last processed tick.
     *
     * @return The current time in milliseconds.
     */
    public long now() {
        if (mode == ClockMode.LIVE) {
            // In LIVE mode, we can either return the cached System.currentTimeMillis()
            // or fetch a fresh one. For ultra-low latency, the application might
            // have a dedicated thread that updates the time. For now, this is sufficient.
            return System.currentTimeMillis();
        }
        return this.currentTime.get();
    }

    /**
     * Updates the clock's time. This method is only effective in REPLAY mode.
     * It should be called by the data replayer with the timestamp of each tick.
     *
     * @param timestamp The timestamp of the tick, in nanoseconds.
     *                    It will be converted to milliseconds.
     */
    public void update(long timestamp) {
        if (mode == ClockMode.REPLAY) {
            this.currentTime.set(timestamp / 1_000_000L); // Convert nanoseconds to milliseconds
        }
    }

    /**
     * Gets the current mode of the clock.
     *
     * @return The current ClockMode.
     */
    public ClockMode getMode() {
        return mode;
    }
}
