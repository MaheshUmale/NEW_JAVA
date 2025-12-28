# UI/UX Requirements for AlphaPulse Dashboard

## 1. Core Design Philosophy: "The Bloomberg Terminal"

The UI must be dense with information but immediately scannable. It should feel like a professional-grade financial tool, not a consumer web application.

- **Theme:** Dark Mode is mandatory.
- **Layout:** Multi-pane, fixed layout. No scrolling pages.
- **Font:** Monospaced (e.g., 'Fira Code', 'Roboto Mono') for all numerical data to ensure alignment.

## 2. Color Palette

- **Background:** `#0a0a0a` (Near-black)
- **Primary Text:** `#e0e0e0` (Off-white)
- **Accent (Green):** `#00ff88` (Bright, vibrant green for positive indicators, e.g., price up)
- **Accent (Red):** `#ff4d4d` (Slightly desaturated red for negative indicators, e.g., price down)
- **Borders/Panes:** `#333333` (Subtle gray to delineate sections)

## 3. Key UI Components

### Component 1: The "Tape" (Live Tick Feed)

- **Purpose:** Display every single incoming tick for the Spot and Option symbols.
- **Layout:** A continuously scrolling vertical list. New ticks appear at the bottom.
- **Columns:**
    1.  `Timestamp` (Format: `HH:mm:ss.SSS`)
    2.  `Symbol` (e.g., "NIFTY 50", "NFO_OPT_18500_CE")
    3.  `Price` (Colored green if price > last price, red if price < last price)
    4.  `Volume`

### Component 2: The "AlphaGauge" (Real-time Ratio)

- **Purpose:** Visualize the calculated Alpha value.
- **Layout:** A speedometer-style gauge.
- **Range:** 0.0 to 2.0
- **Color Coding:**
    - `0.0 - 0.8`: Needle points to a red zone (Indicates "Absorption")
    - `0.8 - 1.2`: Needle points to a yellow zone (Neutral)
    - `1.2+`: Needle points to a green zone (Trending)
- **Text:** The exact Alpha value (e.g., "0.76") should be displayed numerically below the gauge.

### Component 3: The "Signal Log"

- **Purpose:** A permanent, non-scrolling log of every "FAILED_AUCTION_SIGNAL" event.
- **Layout:** A simple, reverse-chronological list.
- **Columns:**
    1.  `Signal Timestamp`
    2.  `Signal Type` ("FAILED_AUCTION_SIGNAL")
    3.  `Alpha at Signal`
    4.  `Spot Price at Signal`

## 4. Technical Stack (for AI Agent Implementation)

- **Framework:** React (Vite for speed)
- **Styling:** Tailwind CSS (for rapid, utility-first styling that matches our color palette)
- **Charting:** `lightweight-charts` (for potential future price chart implementations)
- **State Management:** Zustand or React Context (keep it simple)

## 5. WebSocket Integration

- The frontend must connect to a WebSocket server (a simple Python `websockets` server will suffice for replay) running on `localhost:8080`.
- It will receive JSON messages in the `MarketTick` format and update the UI components in real-time. No batching; the UI should update on every single message to feel "live".
