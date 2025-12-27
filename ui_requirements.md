
**AlphaPulse Trading UI: Functional & Technical Requirements**
==============================================================

**1\. Core Concept**
--------------------

The UI is a real-time monitoring dashboard for a high-frequency trading system. It must visualize the relationship between NIFTY Spot and Option Premiums (α Ratio) using WebSockets.

**2\. Functional Requirements (FRD)**
-------------------------------------

### **A. Primary Visualizer: Dual-Axis Overlay**

*   **Data Series:** Plot `NIFTY Spot` (Primary Y-Axis) and `ATM Option Premium` (Secondary Y-Axis).
    
*   **Normalization:** Since price scales differ (~24k vs ~200), the UI must auto-scale both axes to occupy the same vertical space to highlight **divergence**.
    
*   **Divergence Shading:** Shade the area between the lines.
    
    *   **Green:** Option is outperforming Spot (Rising Alpha).
        
    *   **Red:** Spot is rising but Option is flat/falling (Absorption/Low Alpha).
        

### **B. Alpha Oscillator Pane**

*   **Metric:** A sub-pane histogram/line showing the α ratio.
    
*   **Thresholds:** Draw a horizontal reference line at **1.0** (Neutral) and **0.8** (Signal Zone).
    
*   **Alerting:** Color bars **Bright Red** when α<0.8 at High of Day.
    

### **C. Hybrid AMT Ladder (TPO + Volume)**

*   **Vertical Ladder:** Display a price ladder for the current Nifty range.
    
*   **TPO Profile:** Horizontal bars representing time spent at price (Spot).
    
*   **Volume Profile:** Horizontal bars representing volume traded at price (Futures/Options).
    
*   **POC (Point of Control):** Highlight the price level with the highest volume in **Yellow**.
    

### **D. System Vitals & Controls**

*   **Kill Switch:** A large, persistent Red button that sends an `EMERGENCY_HALT` signal via WebSocket.
    
*   **Status Indicators:** Display `Engine Latency (ms)`, `RAM Usage (GB)`, and `WSS Connection Status`.
    

* * *

**3\. Technical Requirements (TRD)**
------------------------------------

### **A. Tech Stack**

*   **Framework:** React 19 + Vite (for fast HMR).
    
*   **Charting Library:** `Lightweight-Charts` (TradingView). _Reason: Uses HTML5 Canvas for ultra-fast rendering with zero DOM overhead._
    
*   **Communication:** WebSocket (Socket.io or standard WebSockets).
    

### **B. Performance Constraints (The "16ms" Rule)**

*   **Throttled Rendering:** The Backend may send 50 ticks/sec. The UI must **buffer** these and update at a maximum of **60 FPS** (every 16ms) to prevent browser freezing.
    
*   **Web Workers:** Offload the `Alpha` calculation and TPO aggregation logic to a **Web Worker** so the main UI thread remains responsive for the Kill Switch.
    
*   **Virtualization:** If displaying a trade log/JSON feed, use `react-window` or `react-virtuoso` to render only the visible rows.
    

* * *

**4\. AI Prompt for UI Generation**
-----------------------------------

> _"Build a React dashboard using Tailwind CSS and Lightweight-Charts. Create a WebSocket hook that listens for `Tick` objects. Implement a dual-axis chart where Nifty Spot and an Option Premium are overlaid. Ensure the chart uses a sliding window of the last 500 data points. Include a sub-chart for a ratio called 'Alpha'. Add a sidebar with a large 'KILL SWITCH' button and a 'System Vitals' card showing latency and memory. Style it with a dark 'Bloomberg-style' theme: Background #0a0a0a, Primary Text #e0e0e0, Accent Red #ff4d4d, Accent Green #00ff88."_

* * *

**5\. Logic Mapping (UI to Backend)**
-------------------------------------

UI Component

Backend Data Source

Logic Requirement

**Main Chart**

`com.alphapulse.model.Tick`

Dual series mapping on `receive_ts`.

**Alpha Bars**

`com.alphapulse.core.AlphaEngine`

Update histogram every time α is recalculated.

**AMT Ladder**

`com.alphapulse.infra.QuestDBWriter`

Initial load from DB, then incremental updates from WSS.

**Kill Switch**

`com.alphapulse.infra.OrderManager`

Emits `SHUTDOWN` event to cancel all pending orders.
