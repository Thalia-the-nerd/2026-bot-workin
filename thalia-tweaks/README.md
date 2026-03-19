# Thalia Tweaks — Rust Edition

A complete rewrite of the C++ GTK tweak GUI in Rust using [`eframe`](https://github.com/emilk/egui/tree/master/crates/eframe) / [`egui`](https://github.com/emilk/egui).

## Features

- **Tweak Constants** — toggle all `TweakConstants.java` booleans with checkboxes
- **Speed / PID Constants** — grouped sliders that write back to `SpeedConstants.java` / `PIDConstants.java`
- **Live Dashboard** — NT4 battery voltage and turret RPM readout over WebSocket
- **Autonomous Selector** — push auto routine override to NetworkTables
- **Preset Save/Load** — `.cfg` files saved to `presets/`
- **Deploy Terminal** — runs `./gradlew deploy` in-process and streams output

## Building

Requires [Rust](https://rustup.rs/) `1.80+`.

```bash
cd "thalia tweaks"
cargo build --release
./target/release/thalia-tweaks
```

Or just run in development mode:

```bash
cargo run
```

> **Note:** The robot must be on the network for NT4 telemetry to connect (`ws://127.0.0.1:5810`). Constants editing and deploy works offline.

## Architecture

| File | Purpose |
|------|---------|
| `src/main.rs` | Entire application (egui app, file I/O, NT4 client) |
| `Cargo.toml` | Dependencies |

No native libs required. No GTK. No C++.
