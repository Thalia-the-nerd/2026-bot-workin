use eframe::egui;
use serde::Deserialize;
use std::sync::{Arc, Mutex};
use tokio_tungstenite::connect_async;
use futures_util::StreamExt;
use url::Url;

#[derive(Deserialize, Default, Clone)]
struct TelemetryData {
    connected: bool,
    battery_voltage: f64,
    navx_connected: bool,
    turret_ok: bool,
    turret_temp: f64,
    fire_left_ok: bool,
    fire_right_ok: bool,
    drive_fl_ok: bool,
    drive_fr_ok: bool,
    drive_bl_ok: bool,
    drive_br_ok: bool,
}

struct PitHealthApp {
    telemetry: Arc<Mutex<TelemetryData>>,
}

impl PitHealthApp {
    fn new(cc: &eframe::CreationContext<'_>) -> Self {
        // Customize egui style
        let mut style = (*cc.egui_ctx.style()).clone();
        style.visuals = egui::Visuals::dark();
        cc.egui_ctx.set_style(style);

        let telemetry = Arc::new(Mutex::new(TelemetryData::default()));

        // Spawn background tokio task for websocket client
        let telemetry_clone = Arc::clone(&telemetry);
        let ctx = cc.egui_ctx.clone();

        tokio::spawn(async move {
            let url = Url::parse("ws://127.0.0.1:8765").unwrap();
            
            loop {
                if let Ok((ws_stream, _)) = connect_async(url.as_str()).await {
                    let (_, mut read) = ws_stream.split();
                    
                    while let Some(msg) = read.next().await {
                        if let Ok(msg) = msg {
                            if let Ok(text) = msg.to_text() {
                                if let Ok(data) = serde_json::from_str::<TelemetryData>(text) {
                                    if let Ok(mut t) = telemetry_clone.lock() {
                                        *t = data;
                                    }
                                    ctx.request_repaint(); // trigger UI update
                                }
                            }
                        }
                    }
                }
                // Reconnect loop delay
                tokio::time::sleep(tokio::time::Duration::from_secs(1)).await;
            }
        });

        Self { telemetry }
    }

    fn check_ui(ui: &mut egui::Ui, name: &str, is_ok: bool) {
        ui.horizontal(|ui| {
            ui.label(name);
            ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                if is_ok {
                    ui.colored_label(egui::Color32::GREEN, "✔ OK");
                } else {
                    ui.colored_label(egui::Color32::RED, "✖ FAULT");
                }
            });
        });
    }
}

impl eframe::App for PitHealthApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        let data = self.telemetry.lock().unwrap().clone();

        egui::CentralPanel::default().show(ctx, |ui| {
            ui.heading("Robot Pit Health Monitor");
            ui.separator();

            if data.connected {
                ui.colored_label(egui::Color32::GREEN, "NetworkTables: CONNECTED");
            } else {
                ui.colored_label(egui::Color32::RED, "NetworkTables: DISCONNECTED - Waiting for Bridge/Robot...");
                return;
            }

            ui.add_space(10.0);

            // --- Core Systems ---
            ui.group(|ui| {
                ui.label(egui::RichText::new("Core Systems").strong());
                ui.separator();
                
                ui.horizontal(|ui| {
                    ui.label("Battery Voltage:");
                    ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                        let color = if data.battery_voltage > 12.0 {
                            egui::Color32::GREEN
                        } else if data.battery_voltage > 11.5 {
                            egui::Color32::YELLOW
                        } else {
                            egui::Color32::RED
                        };
                        ui.colored_label(color, format!("{:.2}V", data.battery_voltage));
                    });
                });

                Self::check_ui(ui, "NavX Gyroscope", data.navx_connected);
            });

            ui.add_space(10.0);

            // --- Turret & Fire Control ---
            ui.group(|ui| {
                ui.label(egui::RichText::new("Turret & Fire Control").strong());
                ui.separator();
                
                Self::check_ui(ui, "Turret SparkMax", data.turret_ok);
                ui.horizontal(|ui| {
                    ui.label("  ↳ Temperature:");
                    ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                        let temp_color = if data.turret_temp < 60.0 { egui::Color32::GREEN } else { egui::Color32::RED };
                        ui.colored_label(temp_color, format!("{:.1}°C", data.turret_temp));
                    });
                });
                
                Self::check_ui(ui, "Fire NEO (Left)", data.fire_left_ok);
                Self::check_ui(ui, "Fire NEO (Right)", data.fire_right_ok);
            });

            ui.add_space(10.0);

            // --- Drive Base ---
            ui.group(|ui| {
                ui.label(egui::RichText::new("Drive Base").strong());
                ui.separator();
                
                Self::check_ui(ui, "Front Left Module", data.drive_fl_ok);
                Self::check_ui(ui, "Front Right Module", data.drive_fr_ok);
                Self::check_ui(ui, "Back Left Module", data.drive_bl_ok);
                Self::check_ui(ui, "Back Right Module", data.drive_br_ok);
            });
        });
    }
}

#[tokio::main]
async fn main() -> eframe::Result<()> {
    // Start the eframe GUI, utilizing Tokio runtime in the background
    let options = eframe::NativeOptions {
        viewport: egui::ViewportBuilder::default()
            .with_inner_size([400.0, 500.0])
            .with_title("Team 2026 - Pit Health Monitor"),
        ..Default::default()
    };

    eframe::run_native(
        "Pit Monitor",
        options,
        Box::new(|cc| Ok(Box::new(PitHealthApp::new(cc)))),
    )
}
