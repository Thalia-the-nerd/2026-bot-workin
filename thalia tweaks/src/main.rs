use std::{
    collections::HashMap,
    fs,
    process::{Command, Stdio},
    sync::{Arc, Mutex},
    io::{BufRead, BufReader},
};
use tokio::sync::mpsc;
use eframe::egui::{self, Color32, RichText, Ui, Layout, Align};

#[derive(Default, Clone)]
struct Tweak {
    id: String,
    state: bool,
}

#[derive(Default, Clone)]
struct NumSetting {
    id: String,
    value: f64,
    is_pid: bool,
}

#[derive(Default, Clone)]
struct Telemetry {
    battery: f64,
    turret_rpm: f64,
    heading: f64,
    connected: bool,
}

enum NtMsg {
    Tweak(String, bool),
    Const(String, f64),
    Telem(Telemetry),
    Auto(usize),
}

fn default_tweaks() -> Vec<Tweak> {
    let ids = [
        "aim_assist", "auto_fire", "limit_flywheel", "climb_locks",
        "gyro_corr", "vision_track", "intake_stow", "shooter_prime",
        "turret_home", "drive_slow", "invert_controls", "debug_logs",
        "sim_mode", "path_viz", "collision_box", "torque_vec",
        "heat_adj", "slip_ctrl", "anti_tip", "brake_coast",
        "led_flicker", "audio_warn", "data_log", "wifi_boost",
        "fan_boost", "power_limit", "field_orient", "fast_climb", "safe_mode"
    ];
    ids.iter().map(|id| Tweak { id: id.to_string(), state: false }).collect()
}

fn category_of(id: &str) -> usize {
    if id.contains("drive") { 0 }
    else if id.contains("turret") { 1 }
    else { 2 }
}

fn group_prefix(id: &str) -> &str {
    let parts: Vec<&str> = id.split('_').collect();
    if parts.len() > 1 { parts[0] } else { id }
}

fn parse_booleans(path: &str) -> HashMap<String, bool> {
    let mut map = HashMap::new();
    if let Ok(content) = fs::read_to_string(path) {
        for line in content.lines() {
            let parts: Vec<&str> = line.split('=').collect();
            if parts.len() == 2 {
                map.insert(parts[0].to_string(), parts[1].trim() == "true");
            }
        }
    }
    map
}

fn parse_doubles(path: &str) -> HashMap<String, f64> {
    let mut map = HashMap::new();
    if let Ok(content) = fs::read_to_string(path) {
        for line in content.lines() {
            let parts: Vec<&str> = line.split('=').collect();
            if parts.len() == 2 {
                if let Ok(v) = parts[1].trim().parse::<f64>() {
                    map.insert(parts[0].to_string(), v);
                }
            }
        }
    }
    map
}

fn rewrite_booleans(path: &str, tweaks: &[Tweak]) {
    let mut out = String::new();
    for t in tweaks {
        out.push_str(&format!("{}={}\n", t.id, t.state));
    }
    let _ = fs::write(path, out);
}

fn rewrite_doubles(path: &str, speed: &[NumSetting], pid: &[NumSetting]) {
    let mut out = String::new();
    for s in speed.iter().chain(pid) {
        out.push_str(&format!("{}={}\n", s.id, s.value));
    }
    let _ = fs::write(path, out);
}

async fn spawn_nt_task(nt_tx: mpsc::UnboundedSender<NtMsg>, nt_rx: Arc<Mutex<mpsc::UnboundedReceiver<String>>>) {
    use tokio_tungstenite::connect_async;
    use futures_util::StreamExt;
    
    let url = "ws://127.0.0.1:5810/nt4";
    if let Ok((ws_stream, _)) = connect_async(url).await {
        let (mut write, mut read) = ws_stream.split();
        let mut telem = Telemetry::default();
        telem.connected = true;
        let _ = nt_tx.send(NtMsg::Telem(telem));
        while let Some(Ok(msg)) = read.next().await {
            // NT4 logic simplified for brevity
        }
    }
}

impl TweaksApp {
    fn new(cc: &eframe::CreationContext<'_>) -> Self {
        let ctx = &cc.egui_ctx;
        ctx.set_pixels_per_point(1.5);
        
        let mut visuals = egui::Visuals::dark();
        visuals.panel_fill = Color32::from_rgb(8, 8, 8);
        visuals.window_fill = Color32::from_rgb(12, 12, 12);
        ctx.set_visuals(visuals);

        let tweaks = default_tweaks();
        let speed_settings = vec![];
        let pid_settings = vec![];
        let (nt_tx, _) = mpsc::unbounded_channel();
        let nt_rx = Arc::new(Mutex::new(mpsc::unbounded_channel().1));
        let telemetry = Arc::new(Mutex::new(Telemetry::default()));

        Self {
            tweaks,
            speed_settings,
            pid_settings,
            tab: 0,
            log: "Initialized.\n".to_string(),
            auto_idx: 0,
            selected_group: [None, None, None],
            telemetry,
            nt_tx,
            nt_rx,
            preset_name: String::new(),
            preset_filter: String::new(),
            save_dialog_open: false,
            load_dialog_open: false,
            preset_list: vec![],
            selected_preset: None,
        }
    }
}

impl TweaksApp {
    fn drain_nt(&mut self) {
        let mut rx = self.nt_rx.lock().unwrap();
        while let Ok(msg) = rx.try_recv() {
            match msg {
                NtMsg::Tweak(id, s) => { if let Some(t) = self.tweaks.iter_mut().find(|x| x.id == id) { t.state = s; } }
                NtMsg::Const(id, v) => {
                    if let Some(s) = self.speed_settings.iter_mut().find(|x| x.id == id) { s.value = v; }
                    if let Some(s) = self.pid_settings.iter_mut().find(|x| x.id == id) { s.value = v; }
                }
                NtMsg::Telem(t) => { *self.telemetry.lock().unwrap() = t; }
                NtMsg::Auto(i) => { self.auto_idx = i; }
            }
        }
    }
}

impl TweaksApp {
    fn apply_and_deploy(&mut self) {
        rewrite_booleans("../deploy/tweaks.txt", &self.tweaks);
        rewrite_doubles("../deploy/constants.txt", &self.speed_settings, &self.pid_settings);
        self.log.push_str(">> Applied changes. Deploying...\n");
        let log_arc = Arc::new(Mutex::new(String::new()));
        let l2 = log_arc.clone();
        std::thread::spawn(move || {
            if let Ok(mut child) = Command::new("./gradlew").arg("deploy").stdout(Stdio::piped()).spawn() {
                let reader = BufReader::new(child.stdout.take().unwrap());
                for line in reader.lines() {
                    if let Ok(l) = line {
                        l2.lock().unwrap().push_str(&format!("{}\n", l));
                    }
                }
            }
        });
    }
}

impl TweaksApp {
    fn save_preset(&mut self, name: &str) {
        let path = format!("presets/{}.json", name);
        self.log.push_str(&format!(">> Saved preset: {}\n", name));
    }
    fn load_preset(&mut self, name: &str) {
        self.log.push_str(&format!(">> Loaded preset: {}\n", name));
    }
    fn refresh_presets(&mut self) {
        self.preset_list = vec!["default".to_string(), "autonomous_high".to_string()];
    }
}

impl TweaksApp {
    fn draw_dashboard(&mut self, ui: &mut Ui) {
        let t = self.telemetry.lock().unwrap().clone();
        ui.label(format!("Battery: {:.2}V", t.battery));
        ui.label(format!("Turret RPM: {:.0}", t.turret_rpm));
    }
    fn draw_terminal(&mut self, ui: &mut Ui) {
        ui.label(&self.log);
    }
}

impl TweaksApp {
    fn draw_tweaks(&mut self, ui: &mut Ui) {
        for t in &mut self.tweaks {
            ui.checkbox(&mut t.state, &t.id);
        }
    }
}

impl eframe::App for TweaksApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        self.drain_nt();
        egui::CentralPanel::default().show(ctx, |ui| {
            ui.heading("PROJECT CERBERUS");
            self.draw_tweaks(ui);
            self.draw_dashboard(ui);
        });
    }
}

fn main() -> eframe::Result {
    let native_options = eframe::NativeOptions::default();
    eframe::run_native(
        "Tweak Engine",
        native_options,
        Box::new(|cc| Ok(Box::new(TweaksApp::new(cc)))),
    )
}
