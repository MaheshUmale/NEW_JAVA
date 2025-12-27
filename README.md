# NEW_JAVA


* * *

**Project Brief: High-Fidelity Option Trading Engine (AlphaPulse)**
===================================================================

### **1\. Core Intent**

Build a Java-based automated trading system that detects institutional "failed auctions" by calculating the **AlphaPulse Ratio** (α). The system must be **deterministic**, meaning it must produce the exact same results whether running on Live WebSocket data or replaying historical JSON data from `.gz` files.

### **2\. The Formula (α)**

The system monitors the relationship between the Underlier (NIFTY Spot) and the Option Premium.

α\=(ΔNifty Spot×Option Delta)ΔOption Premium​

*   **Window:** 10-tick rolling average.
    
*   **Signal:** If α<0.8 while Nifty is at HOD (High of Day), it indicates "Absorption" (Sellers are absorbing Buy orders), signaling a "Failed Auction" and a potential reversal.
    

* * *

### **3\. System Architecture (The "Three-Chamber" Design)**

The agent must keep these three modules strictly decoupled:

#### **A. Ingestion Layer (The Harvester)**

*   **Live Mode:** Uses Upstox Java SDK `MarketDataStreamerV3`. It must decode Protobuf/JSON and map it to a universal `Tick` object.
    
*   **Replay Mode:** Reads `.gz` files line-by-line. Each line is a JSON string.
    
*   **Constraint:** The Engine must not know which mode is active.
    

#### **B. Logic Layer (The Engine)**

*   **State Management:** Stores a rolling buffer of the last N ticks in memory.
    
*   **Hybrid AMT:** Maintains a "Time Price Opportunity" (TPO) profile for the Spot and a "Volume Profile" for the Future.
    
*   **Time Keeping:** Uses a `VirtualClock`.
    
    *   _Live:_ `System.currentTimeMillis()`.
        
    *   _Replay:_ Extracts timestamp from the JSON tick. **NEVER use `now()` in code.**
        

#### **C. Persistence Layer (The Vault)**

*   **Database:** QuestDB (via InfluxDB Line Protocol).
    
*   **Requirement:** Async writes. The calculation thread must never wait for the database.
    

* * *

### **4\. Detailed File & API Logic**

File Component

Detailed Logic for AI Agent

`Tick.java` (Model)

Immutable POJO: `symbol`, `price`, `v`, `exchange_ts`, `receive_ts`.

`UpstoxMapper.java`

Loads `instrument_key` mappings from Upstox API on startup and caches them.

`GzReplayer.java`

Decompresses `.gz`, reads line, parses JSON, calls `clock.update(ts)`, then `engine.onTick(tick)`.

`AlphaEngine.java`

**Buffer:** `EvictingQueue(10)`. **Math:** Calculates α. **Event:** Fires `SignalEvent` to OrderManager.

`QuestDBWriter.java`

Uses `io.questdb.client.Sender`. Table: `ticks` (partitioned by Day).

* * *

### **5\. Corner Case Handling (The "Shields")**

To prevent crashes and "Null Pointer" errors, the agent must implement:

1.  **The Zero-Move Shield:**
    
    *   _Problem:_ If Nifty Spot doesn't move between two ticks, ΔSpot\=0, causing a Divide-by-Zero error.
        
    *   _Logic:_ `if (Math.abs(deltaSpot) < 0.00001) return 1.0;`
        
2.  **The Stale-Data Shield:**
    
    *   _Problem:_ If the Option tick arrives but the Spot tick is 5 seconds old.
        
    *   _Logic:_ Check `tick.exchange_ts`. If the difference between Spot and Option timestamps \>1000ms, discard the α calculation for that tick.
        
3.  **The Memory Shield (16GB Limit):**
    
    *   _Problem:_ Keeping all day's ticks in a `List<Tick>` will cause OutOfMemory (OOM).
        
    *   _Logic:_ Use QuestDB for history. Only keep the **current sliding window** (e.g., last 100 ticks) in JVM memory.
        
4.  **The "Recoup" logic:**
    
    *   _Problem:_ System crashes at 11:00 AM.
        
    *   _Logic:_ On restart, the system queries QuestDB for `timestamp > 09:15:00` to "warm up" the Alpha buffers before connecting to the Live WebSocket.
        

* * *

### **6\. Development Checklist for Agent**

1.  **Step 1:** Create `MarketTick` interface and `GzJsonTick` implementation.
    
2.  **Step 2:** Build `VirtualClock` to handle temporal determinism.
    
3.  **Step 3:** Implement `GzFileReplayer` to verify data flows correctly from your Friday files.
    
4.  **Step 4:** Build `AlphaEngine` and log α values to console to verify math.
    


1. Recommended Project Folder Structure
We will use the standard Maven layout. This is crucial for managing your GZ files and ensuring the "Recoup/Hydrate" logic can find its data

```

AlphaPulseProject/
│
├── src/main/java/com/alphapulse/
│   ├── Main.java              <-- System Entry (Switch between LIVE/REPLAY)
│   ├── core/                  
│   │   ├── AlphaEngine.java   <-- The "Kitchen" (Alpha & AMT Math)
│   │   └── RiskManager.java   <-- Safety breaks (MTM, Max Trades)
│   ├── infra/                 
│   │   ├── UpstoxHarvester.java <-- The "Dock" (Live WebSocket Client)
│   │   ├── QuestDBWriter.java   <-- The "Vault" (Data Storage)
│   │   └── InstrumentMap.java   <-- Key mapping (Nifty -> instrument_key)
│   ├── model/                 
│   │   └── Tick.java          <-- The "Standard Package" (Universal data format)
│   └── util/                  
│       ├── GzReplayer.java    <-- The "Time Machine" (Backtest from GZ files)
│       └── VirtualClock.java  <-- Syncs system time to data time
│
└── data/                      <-- Place your .gz files here

```
To build a professional-grade trading system, we need to move beyond simple scripts. We will use a Modular Layered Architecture.

Imagine the system as three separate rooms in a house: the Dock (Data In), the Kitchen (Logic), and the Vault (Storage). Even if the Kitchen catches fire (a bug in your Alpha calculation), the Dock keeps receiving shipments (live data), and the Vault stays locked (saved data).

1. The Logical Blueprint (The "How")
We split the code into these specific layers to ensure that a bug in one part doesn't kill the whole system.

Layer,Responsibility,Safety Mechanism
Ingestion,Collects data from Upstox or GZ Files.,"Isolation: If the WebSocket drops, it reconnects without affecting the Engine."
Normalization,"Converts JSON/Protobuf into a standard ""Tick"" object.",Validation: Rejects null prices or zero volumes before they reach the math logic.
Strategy Engine,Calculates AlphaPulse and TPO Profiles.,"State Recovery: Can ""re-hydrate"" by reading the last 30 mins from the database."
Persistence,Saves every single tick to QuestDB.,Non-Blocking: Writing to disk won't slow down the calculation speed.

3. Detailed File-by-File LogicFile 1: Tick.java (The Universal Model)Every price update must be converted to this object. This ensures your math code doesn't care if the data came from Upstox or a JSON file.Logic: Stores price, volume, timestamp, and symbol.API: None. Pure Data Object.File 2: AlphaEngine.java (The Math Brain)Purpose: Calculates the AlphaPulse ratio.Detailed Logic: 1.  Receives a Tick.2.  If it's NIFTY Spot, update spotPrice. If it's the Option, update optionPrice.3.  Calculate: $\alpha = \Delta \text{Option} / (\Delta \text{Spot} \times \text{Delta})$.4.  Bug Defense: If $\Delta \text{Spot} = 0$, it skips the calculation (prevents Divide-by-Zero).File 3: GzReplayer.java (The Backtest Utility)Purpose: Reads your Friday JSON files.Detailed Logic: 1.  Opens the .gz file.2.  Reads line-by-line.3.  Parses the JSON.4.  Crucial Step: It tells the VirtualClock what time it is according to the tick.File 4: VirtualClock.java (The Time Keeper)Purpose: Prevents the "Backtest from the Future" bug.Detailed Logic: In live trading, it returns System.currentTimeMillis(). In backtesting, it returns the timestamp of the last tick read from the GZ file.File 5: QuestDBWriter.java (The Data Harvester)Purpose: Ensures you "never get a second shot at live data" by saving it instantly.Logic: Uses a high-speed "Sender" to push ticks into QuestDB. Even if the AlphaEngine crashes, this should be in a separate thread to keep recording.4. How the "Recoup" WorksIf the system crashes at 10:00 AM:Restart: Main.java starts up.Hydrate: It asks QuestDBWriter: "Give me all Nifty ticks from 09:15 to 10:00."Warm-up: The AlphaEngine processes these old ticks at lightning speed to rebuild its "memory" (the AMT profiles).Go Live: It then connects to the Upstox WebSocket and continues from 10:01 AM as if nothing happened.



* * *

### **AI Agent Master Prompt: The AlphaPulse System**

**Role:** You are a Senior Quantitative Developer specializing in low-latency Java 21 trading systems. You are building "AlphaPulse," a system designed to detect institutional absorption and failed auctions in the Nifty 50 options market.

**Primary Goal:** Create a deterministic trading engine that processes both live Upstox WebSocket feeds and historical `.gz` JSON tick logs with identical logic.

**Core Rules (Non-Negotiable):**

1.  **Temporal Determinism:** Never call `System.currentTimeMillis()` or `now()` in the logic layer. All time must be derived from the `MarketTick.exchange_ts`.
    
2.  **Memory Constraint (16GB):** Do not store a day’s worth of ticks in a `List`. Use a sliding window (e.g., `EvictingQueue`) for calculations and QuestDB for persistence.
    
3.  **Defensive Math:** Always check for zero-deltas before division (e.g., when calculating α).
    
4.  **Decoupling:** The "Harvester" (Data In) must be isolated from the "Engine" (Logic) via a standard `MarketTick` interface.
    

* * *

### **Module 1: Data Model & Infrastructure**

*   **Universal Model:** Implement an immutable `MarketTick` interface with: `symbol`, `price`, `volume`, `exchange_ts`.
    
*   **JSON Adapter:** Create a `GzJsonTick` class to map my specific Friday JSON structure to the `MarketTick` interface.
    
*   **Virtual Clock:** Implement a `Clock` utility that has two modes:
    
    *   `LIVE`: Returns system time.
        
    *   `REPLAY`: Returns the timestamp of the last processed tick.
        

### **Module 2: The AlphaPulse Engine**

*   **Sliding Window:** Maintain a rolling buffer of 10 ticks for both the Spot Index and the specific Option Premium.
    
*   **Alpha Calculation:** α\=(SpotPriceChange×Delta)OptionPriceChange​.
    
*   **Logic:** If α<0.8 and Spot is at HOD, trigger a `FAILED_AUCTION_SIGNAL`.
    
*   **Corner Case:** If `SpotPriceChange == 0`, return α\=1.0 to prevent `NaN`.
    

### **Module 3: Harvesters & Persistence**

*   **QuestDB Writer:** Use InfluxDB Line Protocol (ILP) for async, non-blocking writes to a `ticks` table.
    
*   **GZ Replayer:** Create a utility that reads `.gz` files line-by-line, updates the `VirtualClock`, and pushes ticks to the Engine.
    
*   **Recoup Logic:** On startup, the system must query QuestDB for the last 30 minutes of data to "warm up" the Alpha sliding windows before going live.
    

* * *

### **Specific Instructions for the AI Agent:**

1.  Start by defining the `MarketTick` interface and the `VirtualClock`.
    
2.  Implement the `GzFileReplayer` so I can test the logic against my historical Friday data immediately.
    
3.  Ensure the `QuestDBWriter` runs on a separate thread to keep the main logic loop ultra-fast.
    

* * *

### **System Recoup Flow (The "Crash-Proof" Protocol)**

*   **Step 1:** System detects it is starting mid-day.
    
*   **Step 2:** `QuestDBReader` fetches the last 300 ticks for the tracked Nifty symbols.
    
*   **Step 3:** The `AlphaEngine` processes these ticks at max speed to fill the rolling buffers.
    
*   **Step 4:** Once buffers are full, the `UpstoxHarvester` switches to the live WebSocket.
    

This [Market Data Replay with Upstox](https://www.youtube.com/watch?v=GZzy4-_prUw) demonstrates the live streaming mechanics you'll need to replicate in your "Harvester" module once the "Replayer" is solid.



REF: https://github.com/upstox/upstox-java

https://upstox.com/developer/api-documentation/v3/get-market-data-feed/

