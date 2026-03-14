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
