# AlphaPulse Trading Engine

AlphaPulse is a high-fidelity, deterministic trading engine designed to detect institutional absorption and failed auctions in the Nifty 50 options market. It's built with Java 21 and optimized for low-latency performance.

## Core Features

- **Deterministic Processing:** Ensures identical results whether processing live WebSocket data or replaying historical data from `.gz` files.
- **Low-Latency Architecture:** Employs a decoupled, three-chamber design for efficient data ingestion, logic processing, and persistence.
- **Advanced Analytics:** Calculates the **AlphaPulse Ratio (α)** to identify potential market reversals.
- **Robust Error Handling:** Implements multiple "shields" to prevent common issues like divide-by-zero errors, stale data, and memory overruns.
- **Crash-Proof Recovery:** Features a "Recoup" protocol to warm up the engine's state from persisted data on restart.

## Architecture

The system follows a modular, layered architecture to ensure stability and maintainability.

- **Ingestion Layer (Harvester):** Connects to data sources (live or replay) and normalizes data into a universal `MarketTick` format.
- **Logic Layer (Engine):** Processes ticks, calculates the AlphaPulse Ratio, and identifies trading signals.
- **Persistence Layer (Vault):** Asynchronously writes all incoming data to QuestDB for analysis and recovery.

## Getting Started

### Prerequisites

- Java 21
- Maven
- QuestDB

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-repo/alphapulse.git
   cd alphapulse
   ```
2. **Install dependencies:**
   ```bash
   mvn clean install
   ```
3. **Download historical data:**
    - The replay mode requires gzipped JSON data files. An example `file5.json.gz` is included in the `data` directory.

### Running the Engine

AlphaPulse can run in two modes: `REPLAY` (default) and `LIVE`.

- **Replay Mode:**
  ```bash
  mvn compile exec:java
  ```
- **Live Mode:**
  ```bash
  mvn compile exec:java -Dexec.args="LIVE"
  ```
## Development

We welcome contributions! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
