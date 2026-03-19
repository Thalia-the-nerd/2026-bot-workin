use std::{
    collections::HashMap,
    fs,
    process::{Command, Stdio},
    sync::{Arc, Mutex},
    io::{BufRead, BufReader},
};
use eframe::egui::{self, Color32, RichText, Ui};
use regex::Regex;
use tokio::sync::mpsc;

const SPEED_FILE: &str = "../src/main/java/frc/robot/constants/SpeedConstants.java";
const TWEAK_FILE: &str = "../src/main/java/frc/robot/constants/TweakConstants.java";
const PID_FILE: &str = "../src/main/java/frc/robot/constants/PIDConstants.java";

const AUTO_MODES: &[&str] = &["DoNothing", "DriveStraight", "TwoPieceAmp", "ThreePieceSource"];

#[derive(Clone)]
struct Tweak {
    name: &'static str,
    id: &'static str,
    state: bool,
    category: &'static str,
    desc: &'static str,
}

#[derive(Clone)]
struct NumSetting {
    id: String,
    value: f64,
    is_pid: bool,
}

#[derive(Default, Clone)]
struct Telemetry {
    battery: f64,
    turret_rpm: f64,
    connected: bool,
}

enum NtMsg {
    Battery(f64),
    TurretRpm(f64),
    Connected(bool),
}

struct TweaksApp {
    tweaks: Vec<Tweak>,
    speed_settings: Vec<NumSetting>,
    pid_settings: Vec<NumSetting>,

    tab: usize,
    log: String,
    auto_idx: usize,

    // per-tab selected group (0=Drive, 1=Turret, 2=Cargo)
    selected_group: [Option<String>; 3],

    telemetry: Arc<Mutex<Telemetry>>,
    nt_tx: mpsc::UnboundedSender<String>,
    nt_rx: Arc<Mutex<mpsc::UnboundedReceiver<NtMsg>>>,

    preset_name: String,
    preset_filter: String,
    save_dialog_open: bool,
    load_dialog_open: bool,
    preset_list: Vec<String>,
    selected_preset: Option<String>,
    tweak_search: String,
}

fn default_tweaks() -> Vec<Tweak> {
    vec![
        Tweak { name: "Enable Tuning Mode",               id: "TUNING_MODE",                       state: false, category: "System", desc: "Unlocks advanced PID configuration panes for motor testing without restarting." },
        Tweak { name: "Limit Drive Speed to 75%",         id: "LIMIT_DRIVE_SPEED_TO_75",           state: false, category: "Drive", desc: "Restricts maximum drivetrain output to 75% for driver safety and training." },
        Tweak { name: "Invert Drive Controls",            id: "INVERT_DRIVE_CONTROLS",             state: false, category: "Drive", desc: "Flips the logical forward/backward orientation of the drivetrain." },
        Tweak { name: "Boost Mode Override",              id: "BOOST_MODE_OVERRIDE",               state: false, category: "Drive", desc: "Bypasses strict speed filters and enables 100% unrestricted motor output." },
        Tweak { name: "Slow Mode Modifier Active",        id: "SLOW_MODE_MODIFIER_ACTIVE",         state: false, category: "Drive", desc: "Allows precision mode trigger to limit drive speed to 30% for fine alignment." },
        Tweak { name: "Enable Dynamic Braking",           id: "ENABLE_DYNAMIC_BRAKING",            state: true , category: "Drive", desc: "Engages SparkMax Brake mode to instantly stop drift instead of Coasting." },
        Tweak { name: "Enable LED Diagnostics",           id: "ENABLE_LED_DIAGNOSTICS",            state: true , category: "Telemetry & Logging", desc: "Projects subsystem health flags to the external addressable LED strips." },
        Tweak { name: "Record Telemetry to USB",          id: "RECORD_TELEMETRY_TO_USB",           state: true , category: "Telemetry & Logging", desc: "Saves high-density WPILib DataLog records to /media/sda1 flash drives." },
        Tweak { name: "Mute Dashboard Alerts",            id: "MUTE_DASHBOARD_ALERTS",             state: false, category: "Telemetry & Logging", desc: "Suppresses non-critical warnings from flooding the driver station logs." },
        Tweak { name: "Enable Pit Health Check",          id: "ENABLE_PIT_HEALTH_CHECK_ON_START",  state: true , category: "Safety & Hardware", desc: "Runs a diagnostic sweep on all motors when deploying to verify connections." },
        Tweak { name: "Enable Turret Unwind",             id: "ENABLE_TURRET_UNWIND",              state: true , category: "Turret & Shooter", desc: "Automatically returns the turret to 0 to prevent cable-wrap destruction." },
        Tweak { name: "Use Rust Lead Calculator",         id: "USE_RUST_LEAD_CALCULATOR",          state: true , category: "Vision & Sensors", desc: "Delegates heavy target matrix math to the C++ FFI library for lower latency." },
        Tweak { name: "Allow Fire While Moving",          id: "ALLOW_FIRE_WHILE_MOVING",           state: false, category: "Turret & Shooter", desc: "Bypasses the safety interlock that stops the flywheel when driving." },
        Tweak { name: "Disable Intake During Fire",       id: "DISABLE_INTAKE_DURING_FIRE",        state: true , category: "Intake & Loader", desc: "Prevents feeding more games pieces while the shooter sequence is active." },
        Tweak { name: "Smooth Loader Motors",             id: "SMOOTH_LOADER_MOTORS",              state: true , category: "Intake & Loader", desc: "Applies a SlewRateLimiter to ease impacts on the internal cargo indexing wheels." },
        Tweak { name: "Use Profiled PID For Turret",      id: "USE_PROFILED_PID_FOR_TURRET",       state: true , category: "Turret & Shooter", desc: "Enables MAXMotion smooth acceleration/deceleration on the targeting turret." },
        Tweak { name: "Reverse Turret Direction",         id: "REVERSE_TURRET_DIRECTION",          state: false, category: "Turret & Shooter", desc: "Flips encoder interpretations if the turret gear chain is mounted backwards." },
        Tweak { name: "Disable Brownout Protection",      id: "DISABLE_BROWNOUT_PROTECTION",       state: false, category: "Safety & Hardware", desc: "DANGEROUS: Ignores RoboRIO voltage dropping; could reboot the whole robot!" },
        Tweak { name: "Enable Stall Detection",           id: "ENABLE_STALL_DETECTION",            state: true , category: "Intake & Loader", desc: "Auto-reverses intake rollers if amperage spikes indicate a jammed piece." },
        Tweak { name: "Ignore Limit Switches",            id: "IGNORE_LIMIT_SWITCHES",             state: false, category: "Safety & Hardware", desc: "Bypasses physical hardstops on the turret; useful if a switch is broken." },
        Tweak { name: "Testing Mode Bypass Sensors",      id: "TESTING_MODE_BYPASS_SENSORS",       state: false, category: "Safety & Hardware", desc: "Forces logical inputs 'true' for simulated testing without physical hardware." },
        Tweak { name: "Override Battery Sense",           id: "OVERRIDE_BATTERY_SENSE",            state: false, category: "Safety & Hardware", desc: "Forces the system to ignore bad voltage readings caused by faulty sensors." },
        Tweak { name: "Enable Haptic Feedback",           id: "ENABLE_HAPTIC_FEEDBACK",            state: true , category: "System", desc: "Rumbles the Xbox controller when the vision target algorithm achieves a lock." },
        Tweak { name: "Force Red Alliance Mode",          id: "FORCE_RED_ALLIANCE_MODE",           state: false, category: "System", desc: "Overrides FMS to assume Red Alliance coordinates for AprilTag fields." },
        Tweak { name: "Battery Sagging Alert",            id: "BATTERY_SAGGING_ALERT",             state: true , category: "Safety & Hardware", desc: "Flashes the dashboard red if driving causes voltage drops below 9.0V." },
        Tweak { name: "Ignore Spinup Time",               id: "IGNORE_SPINUP_TIME",                state: false, category: "Turret & Shooter", desc: "Commands the loader to fire pieces immediately regardless of Flywheel RPM." },
        Tweak { name: "Auto Home Turret On Disable",      id: "AUTO_HOME_TURRET_ON_DISABLE",       state: true , category: "Turret & Shooter", desc: "Commands the turret forward vector to zero-out upon match conclusion." },
        Tweak { name: "Display Birdseye Map Dashboard",   id: "DISPLAY_BIRDSEYE_MAP_DASHBOARD",    state: true , category: "Telemetry & Logging", desc: "Streams full 2D odometry coordinates to the AdvantageKit map view." },
        Tweak { name: "Fast Boot Rio Mode",               id: "FAST_BOOT_RIO_MODE",                state: false, category: "System", desc: "Skips non-critical init sequences to save 2-3 seconds during booting." },
        // --- NEW FEATURES ---
        Tweak { name: "Enable AI Target Prediction",      id: "ENABLE_AI_TARGET_PREDICTION",       state: false, category: "Vision & Sensors", desc: "Compensates for robot drive-speed by adjusting the firing vectors via ChassisSpeeds." },
        Tweak { name: "Kinematic Drive Smoothing",        id: "KINEMATIC_DRIVE_SMOOTHING",         state: false, category: "Drive", desc: "Employs an advanced SlewRateLimiter specifically for joystick velocity profiles." },
    ]
}

fn parse_booleans(path: &str) -> HashMap<String, bool> {
    let mut map = HashMap::new();
    let Ok(text) = fs::read_to_string(path) else { return map; };
    let re = Regex::new(r"public static boolean ([A-Z0-9_]+) = (true|false);").unwrap();
    for cap in re.captures_iter(&text) {
        map.insert(cap[1].to_string(), &cap[2] == "true");
    }
    map
}

fn parse_doubles(path: &str) -> Vec<NumSetting> {
    let mut out = Vec::new();
    let Ok(text) = fs::read_to_string(path) else { return out; };
    let re = Regex::new(r"public static double ([A-Za-z0-9_]+) = ([0-9.]+);").unwrap();
    for cap in re.captures_iter(&text) {
        out.push(NumSetting {
            id: cap[1].to_string(),
            value: cap[2].parse().unwrap_or(0.0),
            is_pid: false,
        });
    }
    out
}

fn rewrite_booleans(path: &str, map: &HashMap<String, bool>) {
    let Ok(text) = fs::read_to_string(path) else { return; };
    let re = Regex::new(r"public static boolean ([A-Z0-9_]+) = (true|false);").unwrap();
    let result = re.replace_all(&text, |caps: &regex::Captures| {
        let id = &caps[1];
        let val = map.get(id).copied().unwrap_or(false);
        format!("public static boolean {} = {};", id, val)
    });
    let _ = fs::write(path, result.as_ref());
}

fn rewrite_doubles(path: &str, settings: &[NumSetting], precision: usize) {
    let Ok(text) = fs::read_to_string(path) else { return; };
    let re = Regex::new(r"public static double ([A-Za-z0-9_]+) = ([0-9.]+);").unwrap();
    let result = re.replace_all(&text, |caps: &regex::Captures| {
        let id = &caps[1];
        if let Some(s) = settings.iter().find(|s| s.id == id) {
            format!("public static double {} = {:.prec$};", id, s.value, prec = precision)
        } else {
            caps[0].to_string()
        }
    });
    let _ = fs::write(path, result.as_ref());
}

fn category_of(id: &str) -> usize {
    let u = id.to_uppercase();
    if u.contains("FRONT") || u.contains("BACK") || u.contains("DRIVE") || u.contains("TURN") { 0 }
    else if u.contains("TURRET") || u.contains("AIM") || u.contains("HOOD") { 1 }
    else { 2 }
}

fn group_prefix(id: &str) -> &str {
    let suffixes = ["_MAX_SPEED","_SENSITIVITY","_KP","_KI","_KD","_FF"];
    for suf in &suffixes {
        if let Some(pos) = id.to_uppercase().rfind(suf) {
            return &id[..pos];
        }
    }
    id
}

fn spawn_nt_task(rx_cmd: mpsc::UnboundedReceiver<String>, tx_event: mpsc::UnboundedSender<NtMsg>, ctx: egui::Context) {
    tokio::spawn(async move {
        use tokio_tungstenite::connect_async;
        use futures_util::StreamExt;

        let _ = rx_cmd;
        let url = "ws://127.0.0.1:5810/nt/thalia-tweaks";

        loop {
            tx_event.send(NtMsg::Connected(false)).ok();
            ctx.request_repaint();
            if let Ok((ws, _)) = connect_async(url).await {
                tx_event.send(NtMsg::Connected(true)).ok();
                ctx.request_repaint();
                let (_, mut read) = ws.split();
                while let Some(Ok(msg)) = read.next().await {
                    if let Ok(text) = msg.to_text() {
                        if let Ok(v) = serde_json::from_str::<serde_json::Value>(text) {
                            if let Some(arr) = v.as_array() {
                                for item in arr {
                                    let key = item.get("k").and_then(|k| k.as_str()).unwrap_or("");
                                    let val  = item.get("v");
                                    if key.contains("BatteryVoltage") {
                                        if let Some(n) = val.and_then(|v| v.as_f64()) {
                                            tx_event.send(NtMsg::Battery(n)).ok();
                                        }
                                    } else if key.contains("TurretRPM") {
                                        if let Some(n) = val.and_then(|v| v.as_f64()) {
                                            tx_event.send(NtMsg::TurretRpm(n)).ok();
                                        }
                                    }
                                }
                            }
                            ctx.request_repaint();
                        }
                    }
                }
            }
            tokio::time::sleep(tokio::time::Duration::from_secs(2)).await;
        }
    });
}

impl TweaksApp {
    fn new(cc: &eframe::CreationContext<'_>) -> Self {
        let ctx = &cc.egui_ctx;
        ctx.set_pixels_per_point(1.5);

        let mut visuals = egui::Visuals::dark();
        visuals.panel_fill         = Color32::from_rgb(8, 8, 8);
        visuals.window_fill        = Color32::from_rgb(14, 14, 14);
        visuals.faint_bg_color     = Color32::from_rgb(16, 16, 16);
        visuals.extreme_bg_color   = Color32::from_rgb(0, 0, 0);
        visuals.override_text_color = Some(Color32::from_rgb(220, 220, 220));
        visuals.selection.bg_fill  = Color32::from_rgb(60, 60, 60);
        visuals.selection.stroke   = egui::Stroke::new(1.0, Color32::WHITE);
        visuals.widgets.active.bg_fill   = Color32::WHITE;
        visuals.widgets.active.fg_stroke = egui::Stroke::new(1.0, Color32::BLACK);
        visuals.widgets.hovered.bg_fill  = Color32::from_rgb(40, 40, 40);
        visuals.widgets.hovered.fg_stroke = egui::Stroke::new(1.0, Color32::WHITE);
        visuals.widgets.inactive.bg_fill = Color32::from_rgb(20, 20, 20);
        visuals.widgets.inactive.fg_stroke = egui::Stroke::new(1.0, Color32::from_rgb(100, 100, 100));
        visuals.widgets.noninteractive.bg_fill = Color32::from_rgb(14, 14, 14);
        visuals.widgets.noninteractive.fg_stroke = egui::Stroke::new(1.0, Color32::from_rgb(60, 60, 60));
        visuals.popup_shadow      = egui::Shadow::NONE;
        visuals.window_shadow     = egui::Shadow::NONE;

        let mut style = (*ctx.style()).clone();
        style.visuals = visuals;
        style.spacing.item_spacing    = egui::vec2(10.0, 8.0);
        style.spacing.button_padding  = egui::vec2(14.0, 8.0);
        style.spacing.slider_width    = 180.0;
        style.text_styles.insert(
            egui::TextStyle::Body,
            egui::FontId::new(13.0, egui::FontFamily::Monospace),
        );
        style.text_styles.insert(
            egui::TextStyle::Button,
            egui::FontId::new(13.0, egui::FontFamily::Monospace),
        );
        style.text_styles.insert(
            egui::TextStyle::Small,
            egui::FontId::new(11.0, egui::FontFamily::Monospace),
        );
        ctx.set_style(style);

        let mut tweaks = default_tweaks();
        let bool_map = parse_booleans(TWEAK_FILE);
        for t in &mut tweaks {
            if let Some(&v) = bool_map.get(t.id) { t.state = v; }
        }
        let tuning = parse_booleans(SPEED_FILE);
        if let Some(&v) = tuning.get("TUNING_MODE") {
            if let Some(t) = tweaks.iter_mut().find(|t| t.id == "TUNING_MODE") { t.state = v; }
        }

        let mut speed_settings = parse_doubles(SPEED_FILE);
        speed_settings.retain(|s| s.id != "TUNING_MODE");

        let mut pid_settings = parse_doubles(PID_FILE);
        for p in &mut pid_settings { p.is_pid = true; }

        let telemetry = Arc::new(Mutex::new(Telemetry::default()));
        let (nt_tx, rx_cmd) = mpsc::unbounded_channel::<String>();
        let (tx_event, rx_event) = mpsc::unbounded_channel::<NtMsg>();
        let nt_rx = Arc::new(Mutex::new(rx_event));

        spawn_nt_task(rx_cmd, tx_event, cc.egui_ctx.clone());

        Self {
            tweaks,
            speed_settings,
            pid_settings,
            tab: 0,
            log: "Initialized Thalia Tweak Engine (Rust).\nWaiting for deploy...\n\n".to_string(),
            auto_idx: 0,
            selected_group: [None, None, None],
            telemetry,
            nt_tx,
            nt_rx,
            preset_name: String::new(),
            preset_filter: String::new(),
            save_dialog_open: false,
            load_dialog_open: false,
            preset_list: Vec::new(),
            selected_preset: None,
            tweak_search: String::new(),
        }
    }

    fn drain_nt(&mut self) {
        if let Ok(mut rx) = self.nt_rx.try_lock() {
            while let Ok(msg) = rx.try_recv() {
                if let Ok(mut t) = self.telemetry.lock() {
                    match msg {
                        NtMsg::Battery(v)    => t.battery = v,
                        NtMsg::TurretRpm(v)  => t.turret_rpm = v,
                        NtMsg::Connected(v)  => t.connected = v,
                    }
                }
            }
        }
    }

    fn apply_and_deploy(&mut self) {
        self.tab = 4;
        self.log.push_str("\n>> Syncing tweaks & constants to Java files...\n");

        let mut bool_map: HashMap<String, bool> = HashMap::new();
        for t in &self.tweaks { bool_map.insert(t.id.to_string(), t.state); }

        let speed_bool_map: HashMap<String, bool> = bool_map
            .iter()
            .filter(|(k, _)| *k == "TUNING_MODE")
            .map(|(k, v)| (k.clone(), *v))
            .collect();

        rewrite_booleans(TWEAK_FILE, &bool_map);
        rewrite_booleans(SPEED_FILE, &speed_bool_map);
        rewrite_doubles(SPEED_FILE, &self.speed_settings, 1);
        rewrite_doubles(PID_FILE, &self.pid_settings, 3);

        self.log.push_str(">> Saved SpeedConstants.java, TweakConstants.java, PIDConstants.java.\n");
        self.log.push_str(">> Running ./gradlew deploy...\n");

        let child = Command::new("sh")
            .arg("-c")
            .arg("cd .. && ./gradlew deploy 2>&1")
            .stdout(Stdio::piped())
            .stderr(Stdio::null())
            .spawn();

        match child {
            Err(e) => self.log.push_str(&format!("!! Failed to start gradle: {e}\n")),
            Ok(mut c) => {
                if let Some(stdout) = c.stdout.take() {
                    for line in BufReader::new(stdout).lines().flatten() {
                        self.log.push_str(&line);
                        self.log.push('\n');
                    }
                }
                self.log.push_str(">> Deploy process terminated.\n");
            }
        }
    }

    fn save_preset(&self, name: &str) {
        let _ = fs::create_dir_all("presets");
        let path = format!("presets/{name}.cfg");
        let mut out = String::new();
        for t in &self.tweaks {
            out.push_str(&format!("{}={}\n", t.id, t.state));
        }
        for s in self.speed_settings.iter().chain(&self.pid_settings) {
            out.push_str(&format!("{}={}\n", s.id, s.value));
        }
        let _ = fs::write(&path, out);
    }

    fn load_preset(&mut self, path: &str) {
        let Ok(text) = fs::read_to_string(path) else { return; };
        let re = Regex::new(r"([^=]+)=(.*)").unwrap();
        for cap in re.captures_iter(&text) {
            let id = &cap[1];
            let val = &cap[2];
            for t in &mut self.tweaks {
                if t.id == id { t.state = val == "true"; }
            }
            for s in self.speed_settings.iter_mut().chain(self.pid_settings.iter_mut()) {
                if s.id == id { s.value = val.parse().unwrap_or(s.value); }
            }
        }
    }

    fn refresh_presets(&mut self) {
        self.preset_list.clear();
        if let Ok(entries) = fs::read_dir("presets") {
            for e in entries.flatten() {
                let name = e.file_name().to_string_lossy().to_string();
                if name.ends_with(".cfg") {
                    let base = name.trim_end_matches(".cfg").to_string();
                    if self.preset_filter.is_empty() || base.to_lowercase().contains(&self.preset_filter.to_lowercase()) {
                        self.preset_list.push(base);
                    }
                }
            }
        }
        self.preset_list.sort();
    }

    fn draw_tweaks(&mut self, ui: &mut Ui) {
        let accent    = Color32::WHITE;
        let dim       = Color32::from_rgb(80, 80, 80);
        let card_bg   = Color32::from_rgb(14, 14, 14);
        let card_on   = Color32::from_rgb(22, 22, 22);
        let border_on = Color32::WHITE;
        let border_off= Color32::from_rgb(45, 45, 45);

        // --- NEW SEARCH BAR ---
        ui.horizontal(|ui| {
            ui.label(RichText::new("SEARCH: ").monospace().color(dim));
            ui.add(egui::TextEdit::singleline(&mut self.tweak_search).desired_width(200.0));
        });
        ui.add_space(8.0);
        // ----------------------

        egui::ScrollArea::vertical().show(ui, |ui| {
            ui.vertical(|ui| {
                let available_w = ui.available_width();
                
                let query = self.tweak_search.to_lowercase();
                let mut grouped_tweaks: std::collections::BTreeMap<&'static str, Vec<usize>> = std::collections::BTreeMap::new();
                
                for (i, tweak) in self.tweaks.iter().enumerate() {
                    if query.is_empty() || tweak.name.to_lowercase().contains(&query) || tweak.desc.to_lowercase().contains(&query) {
                        grouped_tweaks.entry(tweak.category).or_default().push(i);
                    }
                }

                for (category, indices) in grouped_tweaks {
                    ui.label(RichText::new(format!("[ {} ]", category.to_uppercase())).monospace().size(13.0).color(Color32::from_rgb(255, 170, 0)).strong());
                    ui.add_space(4.0);

                    for idx in indices {
                        let tweak = &mut self.tweaks[idx];
                        let bg     = if tweak.state { card_on } else { card_bg };
                        let border = if tweak.state { border_on } else { border_off };
                        let name_col = if tweak.state { accent } else { Color32::from_rgb(160, 160, 160) };

                        let (rect, response) = ui.allocate_exact_size(
                            egui::vec2(available_w - 18.0, 74.0),
                            egui::Sense::click(),
                        );
                        if response.clicked() { tweak.state = !tweak.state; }

                        let painter = ui.painter();
                        painter.rect_filled(rect, egui::CornerRadius::ZERO, bg);
                        painter.rect_stroke(rect, egui::CornerRadius::ZERO, egui::Stroke::new(1.5, border), egui::StrokeKind::Outside);

                        if tweak.state {
                            let dot_rect = egui::Rect::from_min_size(
                                rect.min,
                                egui::vec2(4.0, rect.height()),
                            );
                            painter.rect_filled(dot_rect, egui::CornerRadius::ZERO, Color32::WHITE);
                        }

                        let text_origin = rect.min + egui::vec2(16.0, 10.0);
                        painter.text(
                            text_origin,
                            egui::Align2::LEFT_TOP,
                            tweak.id,
                            egui::FontId::new(9.0, egui::FontFamily::Monospace),
                            dim,
                        );
                        painter.text(
                            text_origin + egui::vec2(0.0, 20.0),
                            egui::Align2::LEFT_TOP,
                            tweak.name,
                            egui::FontId::new(14.0, egui::FontFamily::Monospace),
                            name_col,
                        );
                        painter.text(
                            text_origin + egui::vec2(0.0, 42.0),
                            egui::Align2::LEFT_TOP,
                            tweak.desc,
                            egui::FontId::new(11.0, egui::FontFamily::Proportional),
                            Color32::from_rgb(150, 150, 150),
                        );

                        ui.add_space(8.0);
                    }
                    
                    ui.add_space(16.0);
                }
            });
        });
    }

    fn draw_constants_tab(&mut self, ui: &mut Ui, cat: usize) {
        let speed_len = self.speed_settings.len();

        // Build sorted group map for this category
        let all: Vec<(usize, NumSetting)> = self.speed_settings.iter()
            .chain(&self.pid_settings)
            .cloned()
            .enumerate()
            .filter(|(_, s)| category_of(&s.id) == cat)
            .collect();

        let mut groups: std::collections::BTreeMap<String, Vec<(usize, NumSetting)>> =
            std::collections::BTreeMap::new();
        for (i, s) in all {
            let prefix = group_prefix(&s.id).to_string();
            groups.entry(prefix).or_default().push((i, s));
        }

        if groups.is_empty() {
            ui.label(RichText::new("No constants found for this category.").monospace().color(Color32::from_rgb(80, 80, 80)));
            return;
        }

        let group_names: Vec<String> = groups.keys().cloned().collect();

        // Auto-select first group if nothing selected
        if self.selected_group[cat].is_none() || !groups.contains_key(self.selected_group[cat].as_deref().unwrap_or("")) {
            self.selected_group[cat] = Some(group_names[0].clone());
        }

        let selected = self.selected_group[cat].clone().unwrap();

        let dim       = Color32::from_rgb(80, 80, 80);
        let accent    = Color32::from_rgb(160, 160, 160);
        let card      = Color32::from_rgb(14, 14, 14);
        let bord      = Color32::from_rgb(45, 45, 45);
        let bord_sel  = Color32::from_rgb(100, 100, 100);

        // ── Dropdown row ──────────────────────────────────────
        ui.horizontal(|ui| {
            ui.label(RichText::new("GROUP").monospace().size(10.0).color(dim));
            egui::ComboBox::from_id_salt(format!("group_sel_{cat}"))
                .selected_text(RichText::new(&selected).monospace().size(12.0).color(Color32::WHITE).strong())
                .width(340.0)
                .show_ui(ui, |ui| {
                    for name in &group_names {
                        let is_sel = *name == selected;
                        let label = RichText::new(name)
                            .monospace().size(12.0)
                            .color(if is_sel { Color32::WHITE } else { accent });
                        if ui.selectable_label(is_sel, label).clicked() {
                            self.selected_group[cat] = Some(name.clone());
                        }
                    }
                });
        });

        ui.add_space(8.0);
        ui.separator();
        ui.add_space(8.0);

        // ── Group pills / quick-select row ────────────────────
        ui.horizontal_wrapped(|ui| {
            ui.style_mut().spacing.item_spacing = egui::vec2(6.0, 6.0);
            for name in &group_names {
                let active = *name == selected;
                let (bg, fg, br) = if active {
                    (Color32::WHITE, Color32::BLACK, Color32::WHITE)
                } else {
                    (card, accent, bord)
                };
                let pill = egui::Button::new(
                    RichText::new(name).monospace().size(10.0).color(fg)
                ).fill(bg).stroke(egui::Stroke::new(1.0, br)).corner_radius(0.0);
                if ui.add(pill).clicked() {
                    self.selected_group[cat] = Some(name.clone());
                }
            }
        });

        ui.add_space(10.0);

        // ── Detail panel for selected group ───────────────────
        if let Some(settings) = groups.get(&selected) {
            egui::Frame::NONE
                .fill(card)
                .stroke(egui::Stroke::new(1.5, bord_sel))
                .inner_margin(egui::Margin::same(16))
                .show(ui, |ui| {
                    ui.set_width(ui.available_width());
                    ui.label(
                        RichText::new(format!("[ {} ]", selected))
                            .monospace().size(13.0)
                            .color(Color32::WHITE).strong(),
                    );
                    ui.add_space(8.0);

                    egui::Grid::new(format!("detail_grid_{cat}"))
                        .num_columns(2)
                        .spacing([16.0, 10.0])
                        .min_col_width(160.0)
                        .show(ui, |ui| {
                            for (i, s) in settings {
                                let suffix = s.id
                                    .strip_prefix(selected.as_str())
                                    .unwrap_or(&s.id)
                                    .trim_start_matches('_')
                                    .to_string();
                                let label = if suffix.is_empty() { s.id.clone() } else { suffix };

                                ui.label(
                                    RichText::new(&label)
                                        .monospace().size(13.0)
                                        .color(Color32::WHITE).strong(),
                                );

                                let (min, max, step) = if s.is_pid {
                                    (0.0f64, 1.0f64, 0.001f64)
                                } else {
                                    (0.0f64, 100.0f64, 0.5f64)
                                };
                                let decimals = if s.is_pid { 3 } else { 1 };

                                let value = if *i < speed_len {
                                    &mut self.speed_settings[*i].value
                                } else {
                                    &mut self.pid_settings[*i - speed_len].value
                                };

                                ui.add(
                                    egui::Slider::new(value, min..=max)
                                        .step_by(step)
                                        .fixed_decimals(decimals)
                                        .trailing_fill(true)
                                        .clamp_to_range(false),
                                );
                                ui.end_row();
                            }
                        });
                });
        }
    }

    fn draw_dashboard(&mut self, ui: &mut Ui) {
        let tele = self.telemetry.lock().unwrap().clone();

        ui.group(|ui| {
            ui.label(RichText::new("[ AUTONOMOUS ROUTINE SELECTOR ]").monospace().color(Color32::from_rgb(170, 170, 170)).strong());
            ui.separator();
            ui.horizontal(|ui| {
                ui.label("Force NT4 Override:");
                egui::ComboBox::from_id_salt("auto_sel")
                    .selected_text(AUTO_MODES[self.auto_idx])
                    .show_ui(ui, |ui| {
                        for (i, name) in AUTO_MODES.iter().enumerate() {
                            ui.selectable_value(&mut self.auto_idx, i, *name);
                        }
                    });
                if ui.button("Push").clicked() {
                    let _ = self.nt_tx.send(format!("auto:{}", AUTO_MODES[self.auto_idx]));
                    self.log.push_str(&format!(">> Pushed AutoMode: {}\n", AUTO_MODES[self.auto_idx]));
                }
            });
        });

        ui.add_space(12.0);

        ui.group(|ui| {
            ui.label(RichText::new("[ LIVE SYSTEM READOUTS ]").monospace().color(Color32::from_rgb(170, 170, 170)).strong());
            ui.separator();

            let conn_color = if tele.connected { Color32::GREEN } else { Color32::RED };
            ui.label(RichText::new(if tele.connected { "NT4 CONNECTED" } else { "NT4 DISCONNECTED" }).color(conn_color).monospace().strong());
            ui.add_space(8.0);

            ui.columns(2, |cols| {
                cols[0].label(RichText::new("BATTERY POTENTIAL").monospace().color(Color32::from_rgb(136, 136, 136)));
                let bv_color = if tele.battery > 12.0 { Color32::GREEN } else if tele.battery > 11.5 { Color32::YELLOW } else { Color32::RED };
                cols[0].label(RichText::new(format!("{:.2} V", tele.battery)).color(bv_color).monospace().size(28.0).strong());

                cols[1].label(RichText::new("TURRET VELOCITY").monospace().color(Color32::from_rgb(136, 136, 136)));
                cols[1].label(RichText::new(format!("{:.1} RPM", tele.turret_rpm)).color(Color32::from_rgb(255, 170, 0)).monospace().size(28.0).strong());
            });
        });
    }

    fn draw_terminal(&mut self, ui: &mut Ui) {
        egui::ScrollArea::vertical()
            .stick_to_bottom(true)
            .show(ui, |ui| {
                ui.add(
                    egui::TextEdit::multiline(&mut self.log.as_str())
                        .font(egui::TextStyle::Monospace)
                        .desired_width(f32::INFINITY)
                        .desired_rows(30),
                );
            });
    }

    fn draw_save_dialog(&mut self, ctx: &egui::Context) {
        if !self.save_dialog_open { return; }
        let mut open = true;
        egui::Window::new("Save Preset")
            .collapsible(false)
            .resizable(false)
            .open(&mut open)
            .show(ctx, |ui| {
                ui.label("Enter preset name:");
                ui.text_edit_singleline(&mut self.preset_name);
                ui.horizontal(|ui| {
                    if ui.button("Save").clicked() && !self.preset_name.is_empty() {
                        let name = self.preset_name.clone();
                        self.save_preset(&name);
                        self.log.push_str(&format!(">> Saved preset: {}\n", name));
                        self.save_dialog_open = false;
                    }
                    if ui.button("Cancel").clicked() {
                        self.save_dialog_open = false;
                    }
                });
            });
        if !open { self.save_dialog_open = false; }
    }

    fn draw_load_dialog(&mut self, ctx: &egui::Context) {
        if !self.load_dialog_open { return; }
        let mut open = true;
        egui::Window::new("Load Preset")
            .collapsible(false)
            .min_width(320.0)
            .open(&mut open)
            .show(ctx, |ui| {
                ui.label("Search:");
                if ui.text_edit_singleline(&mut self.preset_filter).changed() {
                    self.refresh_presets();
                }
                ui.separator();
                egui::ScrollArea::vertical().max_height(220.0).show(ui, |ui| {
                    let list = self.preset_list.clone();
                    for name in &list {
                        let sel = self.selected_preset.as_deref() == Some(name.as_str());
                        if ui.selectable_label(sel, name).clicked() {
                            self.selected_preset = Some(name.clone());
                        }
                    }
                });
                ui.separator();
                ui.horizontal(|ui| {
                    if ui.button("Load").clicked() {
                        if let Some(name) = &self.selected_preset.clone() {
                            let path = format!("presets/{name}.cfg");
                            self.load_preset(&path);
                            self.log.push_str(&format!(">> Loaded preset: {}\n", name));
                            self.load_dialog_open = false;
                        }
                    }
                    if ui.button("Cancel").clicked() {
                        self.load_dialog_open = false;
                    }
                });
            });
        if !open { self.load_dialog_open = false; }
    }
}

impl eframe::App for TweaksApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        self.drain_nt();
        ctx.request_repaint_after(std::time::Duration::from_millis(50));

        self.draw_save_dialog(ctx);
        self.draw_load_dialog(ctx);

        egui::CentralPanel::default()
            .frame(egui::Frame::NONE.fill(Color32::from_rgb(8, 8, 8)).inner_margin(egui::Margin::symmetric(20, 16)))
            .show(ctx, |ui| {

            // ── Title bar ────────────────────────────────────────────
            ui.horizontal(|ui| {
                ui.label(
                    RichText::new("[ SYSTEM CONFIGURATION CORE ]")
                        .monospace().size(18.0).color(Color32::WHITE).strong(),
                );
                ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                    let tele = self.telemetry.lock().unwrap().clone();
                    let (dot, label) = if tele.connected {
                        (Color32::from_rgb(0, 220, 80), "NT4 CONNECTED")
                    } else {
                        (Color32::from_rgb(200, 50, 50), "NT4 DISCONNECTED")
                    };
                    ui.label(RichText::new(label).monospace().small().color(dot));
                    let (r, painter) = ui.allocate_painter(egui::vec2(8.0, 8.0), egui::Sense::hover());
                    painter.circle_filled(r.rect.center(), 4.0, dot);
                });
            });

            ui.add_space(2.0);
            ui.separator();
            ui.add_space(4.0);

            // ── Tab bar ──────────────────────────────────────────────
            let tabs = ["TWEAK CONSTANTS", "DRIVE", "TURRET", "CARGO", "LIVE DASHBOARD", "DEPLOY TERMINAL"];
            ui.horizontal(|ui| {
                ui.style_mut().spacing.item_spacing.x = 2.0;
                for (i, label) in tabs.iter().enumerate() {
                    let active = self.tab == i;
                    let (bg, fg, stroke_col) = if active {
                        (Color32::WHITE, Color32::BLACK, Color32::WHITE)
                    } else {
                        (Color32::from_rgb(18, 18, 18), Color32::from_rgb(140, 140, 140), Color32::from_rgb(45, 45, 45))
                    };
                    let btn = egui::Button::new(
                        RichText::new(*label).monospace().size(11.0).color(fg).strong()
                    )
                    .fill(bg)
                    .stroke(egui::Stroke::new(1.0, stroke_col))
                    .corner_radius(0.0);
                    if ui.add(btn).clicked() { self.tab = i; }
                }
            });

            ui.add_space(6.0);
            ui.separator();
            ui.add_space(6.0);

            // ── Content (leave room for footer) ──────────────────────
            let footer_h = 76.0;
            let avail = ui.available_height() - footer_h;
            ui.allocate_ui(egui::vec2(ui.available_width(), avail), |ui| {
                match self.tab {
                    0 => self.draw_tweaks(ui),
                    1 => self.draw_constants_tab(ui, 0),
                    2 => self.draw_constants_tab(ui, 1),
                    3 => self.draw_constants_tab(ui, 2),
                    4 => self.draw_dashboard(ui),
                    5 => self.draw_terminal(ui),
                    _ => {}
                }
            });

            // ── Footer ───────────────────────────────────────────────
            ui.separator();
            ui.add_space(4.0);
            ui.horizontal(|ui| {
                let btn_size = egui::vec2(180.0, 36.0);
                let load_btn = egui::Button::new(
                    RichText::new("⬆  LOAD PRESET").monospace().size(12.0).color(Color32::from_rgb(200, 200, 200))
                ).fill(Color32::from_rgb(22, 22, 22)).stroke(egui::Stroke::new(1.0, Color32::from_rgb(80, 80, 80))).corner_radius(0.0);
                if ui.add_sized(btn_size, load_btn).clicked() {
                    self.refresh_presets();
                    self.selected_preset = None;
                    self.load_dialog_open = true;
                }

                let save_btn = egui::Button::new(
                    RichText::new("💾  SAVE PRESET").monospace().size(12.0).color(Color32::from_rgb(200, 200, 200))
                ).fill(Color32::from_rgb(22, 22, 22)).stroke(egui::Stroke::new(1.0, Color32::from_rgb(80, 80, 80))).corner_radius(0.0);
                if ui.add_sized(btn_size, save_btn).clicked() {
                    self.preset_name.clear();
                    self.save_dialog_open = true;
                }

                let deploy_btn = egui::Button::new(
                    RichText::new("▶  APPLY TO ROBOT").monospace().size(12.0).color(Color32::BLACK).strong()
                ).fill(Color32::WHITE).stroke(egui::Stroke::new(1.5, Color32::WHITE)).corner_radius(0.0);
                if ui.add_sized(egui::vec2(200.0, 36.0), deploy_btn).clicked() {
                    self.apply_and_deploy();
                }

                ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                    ui.label(
                        RichText::new("built by thalia  //  project cerberus 2026")
                            .monospace().size(10.0).color(Color32::from_rgb(55, 55, 55))
                    );
                });
            });
        });
    }
}

#[tokio::main]
async fn main() -> eframe::Result<()> {
    let options = eframe::NativeOptions {
        viewport: egui::ViewportBuilder::default()
            .with_inner_size([1400.0, 700.0])
            .with_title("Project Cerberus: Tweak Engine"),
        ..Default::default()
    };
    eframe::run_native(
        "thalia-tweaks",
        options,
        Box::new(|cc| Ok(Box::new(TweaksApp::new(cc)))),
    )
}
