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
