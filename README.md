# NEW_JAVA

1. Recommended Project Folder Structure
We will use the standard Maven layout. This is crucial for managing your GZ files and ensuring the "Recoup/Hydrate" logic can find its data.

AlphaPulseProject/
├── pom.xml
├── data/
│   ├── raw/                <-- Place your Friday .gz files here
│   └── master/             <-- Store instrument_key mappings (CSV/JSON)
├── src/
│   ├── main/
│   │   ├── java/com/alphapulse/
│   │   │   ├── core/       <-- AlphaPulseEngine, HybridAMT
│   │   │   ├── infra/      <-- Harvester, UpstoxMapper, QuestDBWriter
│   │   │   ├── model/      <-- TickData (JSON POJO), OptionChain
│   │   │   └── util/       <-- GzJsonReplayer, VirtualClock
│   │   └── resources/
│   │       └── application.properties
│   └── test/               <-- Your Backtesting Suite
└── logs/                   <-- Application logs


