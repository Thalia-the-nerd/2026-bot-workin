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
