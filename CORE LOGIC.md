

# AlphaPulse — Core Design & Requirements Document

## 1\. Project Goal (WHY this exists)

AlphaPulse is a **real-time + replay-capable market microstructure analysis engine** based on **Auction Market Theory (AMT)**.

Its primary purpose is to:

*   Consume **high-frequency market data**
    
*   Build **price–volume distributions**
    
*   Detect **auction failures (FAILED AUCTION)**
    
*   Emit **signals** to downstream consumers (UI, execution, analytics)
    

The system must:

*   Be **broker-agnostic**
    
*   Support **deterministic replay**
    
*   Be **modular and replaceable**
    
*   Run **headless** (no UI required)
    

- - -

## 2\. Core Principles (NON-NEGOTIABLE)

1.  **Separation of concerns**
    
    *   Core logic ≠ Broker ≠ UI ≠ Storage
        
2.  **Determinism**
    
    *   Replay must behave identically to live feed (given same ticks)
        
3.  **Per-symbol isolation**
    
    *   No cross-symbol state (for now)
        
4.  **Replaceability**
    
    *   Upstox can be replaced
        
    *   UI can be replaced
        
    *   Storage can be replaced
        
5.  **No hard binding**
    
    *   Core must not depend on UI or infra libraries
        

- - -

## 3\. High-Level Architecture

markdown

Copy code

          `┌────────────┐           │  Broker WS │  (Upstox / Other)           └─────┬──────┘                 │                 ▼           ┌────────────┐           │  Adapters  │  (broker-specific)           └─────┬──────┘                 │  SimpleTick                 ▼         ┌──────────────────┐         │   AlphaEngine    │   ← CORE         │ (AMT + Signals)  │         └─────┬─────┬──────┘               │     │         SignalEvent  │               │     │         ┌─────▼───┐ │         │  UI WS  │ │         └─────────┘ │                      ▼                Storage / Replay`

- - -

## 4\. Modules (Multi-Module Maven)

### 4.1 `alphapulse-core` (MOST IMPORTANT)

**Purpose:**  
Pure domain logic. Must compile and run without UI, broker, or DB.

**Contains:**

*   Market models
    
*   AMT ladder
    
*   Alpha logic
    
*   Signal emission
    
*   Replay logic (file-based)
    

**Must NOT contain:**

*   WebSocket servers
    
*   HTTP
    
*   Jakarta / Tyrus
    
*   Broker SDKs
    

- - -

### 4.2 `alphapulse-ui`

**Purpose:**  
Presentation layer only.

**Contains:**

*   WebSocket server
    
*   JSON serialization
    
*   Browser-facing contracts
    

**Depends on:**

*   `alphapulse-core`
    

**Must NOT contain:**

*   Market logic
    
*   AMT
    
*   Alpha computation
    

- - -

### 4.3 `alphapulse-app`

**Purpose:**  
Wiring / bootstrap.

**Contains:**

*   `Main.java`
    
*   Mode selection (live / replay)
    
*   Dependency wiring:
    
    *   engine → UI
        
    *   engine → replay
        
    *   engine → broker
        

**Zero business logic.**

- - -

## 5\. Core Domain Objects (alphapulse-core)

### 5.1 `SimpleTick`

**What:**  
Normalized tick abstraction.

**Why:**  
To decouple AlphaEngine from any broker or feed format.

**Fields:**

*   `symbol` (instrument\_key)
    
*   `price`
    
*   `volume`
    
*   `exchangeTs`
    

**Rules:**

*   Volume already normalized
    
*   Index ticks may have synthetic volume
    
*   Immutable
    

- - -

### 5.2 `InstrumentMeta`

**What:**  
Static metadata for instruments.

**Why:**  
Correct handling of:

*   INDEX vs EQ vs FUT
    
*   Expiry
    
*   Underlying relationships
    

**Used for:**

*   Identifying index
    
*   Resolving front-month futures
    

- - -

### 5.3 `InstrumentRegistry`

**What:**  
In-memory lookup for `InstrumentMeta`.

**Why:**  
Fast, deterministic metadata access without DB.

- - -

## 6\. AMT Layer

### 6.1 `AmtLadder`

**What:**  
Per-symbol Auction Market Theory ladder.

**Why:**  
AMT requires volume aggregation by price.

**Responsibilities:**

*   Track `price → volume`
    
*   Maintain **Point of Control (POC)**
    
*   Update incrementally per tick
    

**Explicitly does NOT do (yet):**

*   VAH / VAL
    
*   Imbalances
    
*   Session resets
    

- - -

### 6.2 `AmtState` (internal)

**What:**  
Internal mutable state per symbol.

**Why:**  
Keep ladder logic clean and isolated.

- - -

## 7\. Index Volume Handling

### 7.1 `IndexVolumeTracker`

**Problem:**

*   Index feeds have **no volume**
    
*   AMT requires volume
    

**Solution:**

*   Use **front-month FUT volume**
    
*   Map FUT → underlying INDEX
    
*   Aggregate FUT volume
    
*   Inject into index ticks
    

**Rules:**

*   FUT instrument key = `NSE_FO|<id>`
    
*   Must match:
    
    *   underlying index
        
    *   nearest expiry
        

- - -

## 8\. AlphaEngine (CORE)

### 8.1 Responsibility

AlphaEngine is the **heart of the system**.

It:

*   Receives `SimpleTick`
    
*   Updates AMT ladder
    
*   Computes alpha
    
*   Emits signals
    

### 8.2 Scope

*   **Stateful per symbol only**
    
*   No cross-symbol logic
    
*   No broker knowledge
    
*   No UI knowledge
    

- - -

### 8.3 Alpha Logic (Current)

**Signal:** FAILED AUCTION

**Formula:**

ini

Copy code

`alpha = |price − POC| / POC`

**Condition:**

nginx

Copy code

`alpha ≥ threshold`

**Threshold:**

*   Configurable constant
    
*   Static for now
    

- - -

## 9\. Signal Emission

### 9.1 `SignalEvent`

**What:**  
Domain-level signal emitter.

**Why:**  
Decouple AlphaEngine from consumers.

**Correct Design:**

*   Emits domain event
    
*   Not tied to UI or storage
    

**Future consumers:**

*   UI
    
*   Execution
    
*   Logger
    
*   Kafka / Redis
    

- - -

## 10\. Replay Engine (alphapulse-core)

### 10.1 Purpose

To **replay recorded WebSocket feeds** deterministically.

### 10.2 Input

*   `.json.gz`
    
*   Each line = one WS message (Upstox V3 format)
    

### 10.3 Pipeline

javascript

Copy code

`.gz → JSON → SimpleTick → AlphaEngine`

### 10.4 Timing

*   Sleep based on **exchange timestamp deltas**
    
*   Safety cap to avoid long stalls
    

- - -

## 11\. Broker Adapter (Upstox) — ISOLATED

**Lives outside core.**

Responsibilities:

*   Connect WS
    
*   Authenticate
    
*   Parse feed
    
*   Emit `SimpleTick`
    

Core does **not** know this exists.

- - -

## 12\. UI WebSocket Module (alphapulse-ui)

### 12.1 Purpose

*   Broadcast ticks and signals to browser
    
*   Nothing else
    

### 12.2 Transport

*   WebSocket only
    
*   No HTTP pages
    
*   No REST
    

### 12.3 Messages

*   Tick JSON
    
*   Signal JSON
    

Browser decides rendering.

- - -

## 13\. Configuration Philosophy

*   Mode selection via:
    
    *   args / env / config
        
*   NOT hard-coded flags
    
*   Replay vs Live resolved in `alphapulse-app`
    

- - -

## 14\. What Is EXPLICITLY OUT OF SCOPE (for now)

*   Order execution
    
*   Risk management
    
*   Strategy portfolios
    
*   Cross-symbol alpha
    
*   Persistence optimizations
    
*   UI framework
    

- - -

## 15\. Current Status

✅ Core AMT + Alpha logic designed  
✅ Replay architecture defined  
✅ UI separation identified (needs refactor)  
❗ Multi-module Maven refactor pending

- - -

## 16\. Next Correct Step (after this doc)

Only **after you confirm this document is correct**, we proceed with:

> **Multi-module refactor (core / ui / app)**

No more incremental patching.  
Everything will align to this document.
