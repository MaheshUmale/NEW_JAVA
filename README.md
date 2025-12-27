# NEW_JAVA



# Project Brief: High-Fidelity Option Trading Engine (AlphaPulse)
1. Core IntentBuild a Java-based automated trading system that detects institutional "failed auctions" by calculating the AlphaPulse Ratio ($\alpha$). The system must be deterministic, meaning it must produce the exact same results whether running on Live WebSocket data or replaying historical JSON data from .gz files.
2. The Formula ($\alpha$)The system monitors the relationship between the Underlier (NIFTY Spot) and the Option Premium.$$\alpha = \frac{\Delta \text{Option Premium}}{(\Delta \text{Nifty Spot} \times \text{Option Delta})}$$Window: 10-tick rolling average.Signal: If $\alpha < 0.8$ while Nifty is at HOD (High of Day), it indicates "Absorption" (Sellers are absorbing Buy orders), signaling a "Failed Auction" and a potential reversal.
3. System Architecture (The "Three-Chamber" Design)The agent must keep these three modules strictly decoupled:
  A. Ingestion Layer (The Harvester)Live Mode: Uses Upstox Java SDK MarketDataStreamerV3. It must decode Protobuf/JSON and map it to a universal Tick object.Replay Mode: Reads .gz files line-by-line. Each line is a JSON string.Constraint: The Engine must not know which mode is active.
  B. Logic Layer (The Engine)State Management: Stores a rolling buffer of the last $N$ ticks in memory.Hybrid AMT: Maintains a "Time Price Opportunity" (TPO) profile for the Spot and a "Volume Profile" for the Future.Time Keeping: Uses a VirtualClock.Live: System.currentTimeMillis().Replay: Extracts timestamp from the JSON tick. NEVER use now() in code.
  C. Persistence Layer (The Vault)Database: QuestDB (via InfluxDB Line Protocol).
  Requirement: Async writes. The calculation thread must never wait for the database.
4. Detailed File & API LogicFile ComponentDetailed Logic for AI AgentTick.java (Model)Immutable POJO: symbol, price, v, exchange_ts, receive_ts.UpstoxMapper.javaLoads instrument_key mappings from Upstox API on startup and caches them.GzReplayer.javaDecompresses .gz, reads line, parses JSON, calls clock.update(ts), then engine.onTick(tick).AlphaEngine.javaBuffer: EvictingQueue(10). Math: Calculates $\alpha$. Event: Fires SignalEvent to OrderManager.QuestDBWriter.javaUses io.questdb.client.Sender. Table: ticks (partitioned by Day).
5. Corner Case Handling (The "Shields")To prevent crashes and "Null Pointer" errors, the agent must implement:The Zero-Move Shield:Problem: If Nifty Spot doesn't move between two ticks, $\Delta \text{Spot} = 0$, causing a Divide-by-Zero error.Logic: if (Math.abs(deltaSpot) < 0.00001) return 1.0;The Stale-Data Shield:Problem: If the Option tick arrives but the Spot tick is 5 seconds old.Logic: Check tick.exchange_ts. If the difference between Spot and Option timestamps $> 1000ms$, discard the $\alpha$ calculation for that tick.The Memory Shield (16GB Limit):Problem: Keeping all day's ticks in a List<Tick> will cause OutOfMemory (OOM).Logic: Use QuestDB for history. Only keep the current sliding window (e.g., last 100 ticks) in JVM memory.The "Recoup" logic:Problem: System crashes at 11:00 AM.Logic: On restart, the system queries QuestDB for timestamp > 09:15:00 to "warm up" the Alpha buffers before connecting to the Live WebSocket.
6. Development Checklist for Agent
  Step 1: Create MarketTick interface and GzJsonTick implementation.
  Step 2: Build VirtualClock to handle temporal determinism.
  Step 3: Implement GzFileReplayer to verify data flows correctly from your Friday files.
  Step 4: Build AlphaEngine and log $\alpha$ values to console to verify math.



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

