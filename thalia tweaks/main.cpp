#include <gtk/gtk.h>
#include <iostream>
#include <fstream>
#include <string>
#include <regex>
#include <array>
#include <vector>
#include <memory> 
#include <set>
#include <sstream>
#include <iomanip>
#include <filesystem>
#include <networktables/NetworkTableInstance.h>
#include <networktables/StringTopic.h>
#include <networktables/DoubleTopic.h>

namespace fs = std::filesystem;

// Structure for managing our fake and real tweaks
struct Tweak {
    std::string name;
    std::string id;
    bool state;
    bool is_interactive; 
    GtkWidget* checkbox;
};

// Structure for managing Speed Constants dynamically
struct SpeedSetting {
    std::string id;
    double value;
    GtkWidget* slider;
    GtkWidget* spin_btn;
};

// Structure for managing PID Constants dynamically
struct PIDSetting {
    std::string id;
    double value;
    GtkWidget* slider;
    GtkWidget* spin_btn;
};

// Top tweaks generated!
std::vector<Tweak> tweaks = {
    {"Enable Tuning Mode", "TUNING_MODE", false, true, nullptr},
    {"Limit Drive Speed to 75%", "LIMIT_DRIVE_SPEED_TO_75", false, true, nullptr},
    {"Invert Drive Controls", "INVERT_DRIVE_CONTROLS", false, true, nullptr},
    {"Boost Mode Override", "BOOST_MODE_OVERRIDE", false, true, nullptr},
    {"Slow Mode Modifier Active", "SLOW_MODE_MODIFIER_ACTIVE", false, true, nullptr},
    {"Enable Dynamic Braking", "ENABLE_DYNAMIC_BRAKING", true, true, nullptr},
    {"Enable LED Diagnostics", "ENABLE_LED_DIAGNOSTICS", true, true, nullptr},
    {"Record Telemetry to USB", "RECORD_TELEMETRY_TO_USB", true, true, nullptr},
    {"Mute Dashboard Alerts", "MUTE_DASHBOARD_ALERTS", false, true, nullptr},
    {"Enable Pit Health Check", "ENABLE_PIT_HEALTH_CHECK_ON_START", true, true, nullptr},
    {"Enable Turret Unwind", "ENABLE_TURRET_UNWIND", true, true, nullptr},
    {"Use Rust Lead Calculator", "USE_RUST_LEAD_CALCULATOR", true, true, nullptr},
    {"Allow Fire While Moving", "ALLOW_FIRE_WHILE_MOVING", false, true, nullptr},
    {"Disable Intake During Fire", "DISABLE_INTAKE_DURING_FIRE", true, true, nullptr},
    {"Smooth Loader Motors", "SMOOTH_LOADER_MOTORS", true, true, nullptr},
    {"Use Profiled PID For Turret", "USE_PROFILED_PID_FOR_TURRET", true, true, nullptr},
    {"Reverse Turret Direction", "REVERSE_TURRET_DIRECTION", false, true, nullptr},
    {"Disable Brownout Protection", "DISABLE_BROWNOUT_PROTECTION", false, true, nullptr},
    {"Enable Stall Detection", "ENABLE_STALL_DETECTION", true, true, nullptr},
    {"Ignore Limit Switches", "IGNORE_LIMIT_SWITCHES", false, true, nullptr},
    {"Testing Mode Bypass Sensors", "TESTING_MODE_BYPASS_SENSORS", false, true, nullptr},
    {"Override Battery Sense", "OVERRIDE_BATTERY_SENSE", false, true, nullptr},
    {"Enable Haptic Feedback", "ENABLE_HAPTIC_FEEDBACK", true, true, nullptr},
    {"Force Red Alliance Mode", "FORCE_RED_ALLIANCE_MODE", false, true, nullptr},
    {"Battery Sagging Alert", "BATTERY_SAGGING_ALERT", true, true, nullptr},
    {"Ignore Spinup Time", "IGNORE_SPINUP_TIME", false, true, nullptr},
    {"Auto Home Turret On Disable", "AUTO_HOME_TURRET_ON_DISABLE", true, true, nullptr},
    {"Display Birdseye Map Dashboard", "DISPLAY_BIRDSEYE_MAP_DASHBOARD", true, true, nullptr},
    {"Fast Boot Rio Mode", "FAST_BOOT_RIO_MODE", false, true, nullptr}
};

std::vector<SpeedSetting> speed_settings;
std::vector<PIDSetting> pid_settings;

GtkWidget *log_view;
GtkTextBuffer *log_buffer;
GtkWidget *main_notebook;
nt::NetworkTableInstance nt_inst;
nt::StringPublisher auto_pub;
GtkWidget *telemetry_battery_lbl;
GtkWidget *telemetry_turret_lbl;

const std::string SPEED_CONSTANTS_FILE = "../src/main/java/frc/robot/constants/SpeedConstants.java";
const std::string TWEAK_CONSTANTS_FILE = "../src/main/java/frc/robot/constants/TweakConstants.java";

void append_log(const std::string& text);

// --- FILE PARSERS ---

bool read_speed_constants_tuning_mode() {
    std::ifstream file(SPEED_CONSTANTS_FILE);
    if (!file.is_open()) return false;
    std::string line;
    std::regex reg("public static boolean TUNING_MODE = (true|false);");
    std::smatch match;
    while (std::getline(file, line)) {
        if (std::regex_search(line, match, reg)) {
            return match[1] == "true";
        }
    }
    return false;
}

void read_speed_constants() {
    std::ifstream file(SPEED_CONSTANTS_FILE);
    if (!file.is_open()) return;
    std::string line;
    std::regex reg("public static double ([A-Z0-9_]+) = ([0-9\\.]+);");
    std::smatch match;
    speed_settings.clear();
    while (std::getline(file, line)) {
        if (std::regex_search(line, match, reg)) {
            SpeedSetting s;
            s.id = match[1];
            s.value = std::stod(match[2]);
            s.slider = nullptr;
            s.spin_btn = nullptr;
            speed_settings.push_back(s);
        }
    }
}

void read_pid_constants() {
    std::ifstream file("../src/main/java/frc/robot/constants/PIDConstants.java");
    if (!file.is_open()) return;
    std::string line;
    std::regex reg("public static double ([A-Z0-9_a-z]+) = ([0-9\\.]+);");
    std::smatch match;
    pid_settings.clear();
    while (std::getline(file, line)) {
        if (std::regex_search(line, match, reg)) {
            PIDSetting p;
            p.id = match[1];
            p.value = std::stod(match[2]);
            p.slider = nullptr;
            p.spin_btn = nullptr;
            pid_settings.push_back(p);
        }
    }
}

void read_tweak_constants() {
    std::ifstream file(TWEAK_CONSTANTS_FILE);
    if (!file.is_open()) return;
    std::string line;
    std::regex reg("public static boolean ([A-Z0-9_]+) = (true|false);");
    std::smatch match;
    while (std::getline(file, line)) {
        if (std::regex_search(line, match, reg)) {
            std::string id = match[1];
            bool state = (match[2] == "true");
            for (auto& t : tweaks) {
                if (t.id == id) {
                    t.state = state;
                    break;
                }
            }
        }
    }
}

// --- FILE WRITERS ---

void write_pid_constants() {
    std::ifstream filein("../src/main/java/frc/robot/constants/PIDConstants.java");
    if (!filein.is_open()) return;
    std::string content, line;
    std::regex regDouble("public static double ([A-Z0-9_a-z]+) = ([0-9\\.]+);");
    std::smatch match;
    
    while (std::getline(filein, line)) {
        if (std::regex_search(line, match, regDouble)) {
            std::string id = match[1];
            double val = -1.0;
            for(const auto& p : pid_settings) {
                if (p.id == id) val = p.value;
            }
            if (val != -1.0) {
                // Ensure double format with up to 3 decimal places for tuning precision
                std::stringstream stream;
                stream << std::fixed << std::setprecision(3) << val;
                std::string rep = "public static double " + id + " = " + stream.str() + ";";
                content += std::regex_replace(line, regDouble, rep) + "\n";
            } else {
                content += line + "\n";
            }
        } else {
            content += line + "\n";
        }
    }
    filein.close();
    std::ofstream fileout("../src/main/java/frc/robot/constants/PIDConstants.java");
    fileout << content;
    fileout.close();
}

void write_speed_constants() {
    std::ifstream filein(SPEED_CONSTANTS_FILE);
    if (!filein.is_open()) return;
    std::string content, line;
    
    // Write Tuning Mode
    bool tuning_mode_val = false;
    for (const auto& t : tweaks) {
        if (t.id == "TUNING_MODE") tuning_mode_val = t.state;
    }
    
    std::regex regBool("public static boolean TUNING_MODE = (true|false);");
    std::regex regDouble("public static double ([A-Z0-9_]+) = ([0-9\\.]+);");
    std::smatch match;
    
    while (std::getline(filein, line)) {
        if (std::regex_search(line, match, regBool)) {
            content += std::regex_replace(line, regBool, "public static boolean TUNING_MODE = " + std::string(tuning_mode_val ? "true" : "false") + ";") + "\n";
        } 
        else if (std::regex_search(line, match, regDouble)) {
            std::string id = match[1];
            double val = -1.0;
            for(const auto& s : speed_settings) {
                if (s.id == id) val = s.value;
            }
            if (val != -1.0) {
                // Ensure double format with 1 decimal place standard
                std::stringstream stream;
                stream << std::fixed << std::setprecision(1) << val;
                std::string rep = "public static double " + id + " = " + stream.str() + ";";
                content += std::regex_replace(line, regDouble, rep) + "\n";
            } else {
                content += line + "\n";
            }
        } else {
            content += line + "\n";
        }
    }
    filein.close();
    std::ofstream fileout(SPEED_CONSTANTS_FILE);
    fileout << content;
    fileout.close();
}

void write_tweak_constants() {
    std::ifstream filein(TWEAK_CONSTANTS_FILE);
    if (!filein.is_open()) return;
    std::string content, line;
    std::regex reg("public static boolean ([A-Z0-9_]+) = (true|false);");
    std::smatch match;
    while (std::getline(filein, line)) {
        if (std::regex_search(line, match, reg)) {
            std::string id = match[1];
            bool state = false;
            for (const auto& t : tweaks) {
                if (t.id == id) {
                    state = t.state;
                    break;
                }
            }
            std::string rep = "public static boolean " + id + " = " + (state ? "true" : "false") + ";";
            content += std::regex_replace(line, reg, rep) + "\n";
        } else {
            content += line + "\n";
        }
    }
    filein.close();
    std::ofstream fileout(TWEAK_CONSTANTS_FILE);
    fileout << content;
    fileout.close();
}

// --- LOGGING ---

void append_log(const std::string& text) {
    GtkTextIter end;
    gtk_text_buffer_get_end_iter(log_buffer, &end);
    gtk_text_buffer_insert(log_buffer, &end, text.c_str(), -1);
    
    // Auto-scroll to bottom
    GtkTextMark *mark = gtk_text_buffer_create_mark(log_buffer, NULL, &end, FALSE);
    gtk_text_view_scroll_mark_onscreen(GTK_TEXT_VIEW(log_view), mark);
    gtk_text_buffer_delete_mark(log_buffer, mark);
}

// --- CONFIG SAVE / LOAD ---

void save_presets_to_file(const std::string& filename) {
    std::ofstream out(filename);
    if (!out.is_open()) return;
    for (const auto& t : tweaks) {
        if (t.is_interactive && t.checkbox != nullptr) {
            bool state = gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(t.checkbox));
            out << t.id << "=" << (state ? "true" : "false") << "\n";
        }
    }
    for (const auto& s : speed_settings) {
        if (s.slider != nullptr) {
            double val = gtk_range_get_value(GTK_RANGE(s.slider));
            out << s.id << "=" << val << "\n";
        }
    }
    for (const auto& p : pid_settings) {
        if (p.slider != nullptr) {
            double val = gtk_range_get_value(GTK_RANGE(p.slider));
            out << p.id << "=" << val << "\n";
        }
    }
    out.close();
    append_log(">> Saved configuration preset to " + filename + "\n");
}

void load_presets_from_file(const std::string& filename) {
    std::ifstream in(filename);
    if (!in.is_open()) return;
    std::string line;
    std::regex reg("([^=]+)=(.*)");
    std::smatch match;
    int loaded = 0;
    while (std::getline(in, line)) {
        if (std::regex_search(line, match, reg)) {
            std::string id = match[1];
            std::string valStr = match[2];
            
            // Check tweaks
            for (auto& t : tweaks) {
                if (t.id == id && t.checkbox != nullptr) {
                    gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(t.checkbox), (valStr == "true"));
                    loaded++;
                    break;
                }
            }
            // Check speeds
            for (auto& s : speed_settings) {
                if (s.id == id && s.slider != nullptr) {
                    gtk_range_set_value(GTK_RANGE(s.slider), std::stod(valStr));
                    loaded++;
                    break;
                }
            }
            // Check PIDs
            for (auto& p : pid_settings) {
                if (p.id == id && p.slider != nullptr) {
                    gtk_range_set_value(GTK_RANGE(p.slider), std::stod(valStr));
                    loaded++;
                    break;
                }
            }
        }
    }
    append_log(">> Loaded " + std::to_string(loaded) + " values from " + filename + "\n");
}

// --- CUSTOM PRESET GUIS ---

void populate_preset_list(GtkWidget* listbox, const std::string& query) {
    GList *children, *iter;
    children = gtk_container_get_children(GTK_CONTAINER(listbox));
    for(iter = children; iter != NULL; iter = g_list_next(iter))
        gtk_widget_destroy(GTK_WIDGET(iter->data));
    g_list_free(children);

    if (fs::exists("presets")) {
        for (const auto & entry : fs::directory_iterator("presets")) {
            std::string path = entry.path().string();
            std::string filename = entry.path().filename().string();
            if (filename.find(".cfg") != std::string::npos) {
                std::string basename = filename.substr(0, filename.length() - 4);
                
                std::string b_lower = basename;
                std::string q_lower = query;
                for(auto& c : b_lower) c = tolower(c);
                for(auto& c : q_lower) c = tolower(c);

                if (query.empty() || b_lower.find(q_lower) != std::string::npos) {
                    GtkWidget *row = gtk_list_box_row_new();
                    GtkWidget *lbl = gtk_label_new(basename.c_str());
                    gtk_widget_set_halign(lbl, GTK_ALIGN_START);
                    gtk_widget_set_margin_top(lbl, 10);
                    gtk_widget_set_margin_bottom(lbl, 10);
                    gtk_widget_set_margin_start(lbl, 10);
                    gtk_container_add(GTK_CONTAINER(row), lbl);
                    g_object_set_data_full(G_OBJECT(row), "filepath", g_strdup(path.c_str()), g_free);
                    gtk_list_box_insert(GTK_LIST_BOX(listbox), row, -1);
                }
            }
        }
    }
    gtk_widget_show_all(listbox);
}

void on_search_changed(GtkSearchEntry *entry, gpointer user_data) {
    GtkWidget *listbox = GTK_WIDGET(user_data);
    std::string query = gtk_entry_get_text(GTK_ENTRY(entry));
    populate_preset_list(listbox, query);
}

static void on_save_clicked(GtkWidget *widget, gpointer data) {
    if (!fs::exists("presets")) fs::create_directory("presets");

    GtkWidget *dialog = gtk_dialog_new_with_buttons("Save Preset",
                                         GTK_WINDOW(gtk_widget_get_toplevel(widget)),
                                         (GtkDialogFlags)(GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                         "_Cancel", GTK_RESPONSE_CANCEL,
                                         "_Save", GTK_RESPONSE_ACCEPT,
                                         NULL);
    
    GtkWidget *content_area = gtk_dialog_get_content_area(GTK_DIALOG(dialog));
    GtkWidget *label = gtk_label_new("Enter Preset Name:");
    gtk_widget_set_margin_top(label, 10);
    gtk_box_pack_start(GTK_BOX(content_area), label, FALSE, FALSE, 0);

    GtkWidget *entry = gtk_entry_new();
    gtk_entry_set_placeholder_text(GTK_ENTRY(entry), "e.g. comp_auto_fast");
    gtk_widget_set_margin_bottom(entry, 10);
    gtk_box_pack_start(GTK_BOX(content_area), entry, TRUE, TRUE, 5);
    
    gtk_widget_show_all(dialog);

    if (gtk_dialog_run(GTK_DIALOG(dialog)) == GTK_RESPONSE_ACCEPT) {
        std::string preset_name = gtk_entry_get_text(GTK_ENTRY(entry));
        if (!preset_name.empty()) {
            std::string filename = "presets/" + preset_name + ".cfg";
            save_presets_to_file(filename);
        }
    }
    gtk_widget_destroy(dialog);
}

static void on_load_clicked(GtkWidget *widget, gpointer data) {
    GtkWidget *dialog = gtk_dialog_new_with_buttons("Select Preset to Load",
                                         GTK_WINDOW(gtk_widget_get_toplevel(widget)),
                                         (GtkDialogFlags)(GTK_DIALOG_MODAL | GTK_DIALOG_DESTROY_WITH_PARENT),
                                         "_Cancel", GTK_RESPONSE_CANCEL,
                                         "_Load", GTK_RESPONSE_ACCEPT,
                                         NULL);
    
    GtkWidget *content_area = gtk_dialog_get_content_area(GTK_DIALOG(dialog));
    gtk_widget_set_size_request(dialog, 400, 300);

    GtkWidget *search_entry = gtk_search_entry_new();
    gtk_widget_set_margin_top(search_entry, 10);
    gtk_box_pack_start(GTK_BOX(content_area), search_entry, FALSE, FALSE, 5);

    GtkWidget *scrolled = gtk_scrolled_window_new(NULL, NULL);
    gtk_widget_set_vexpand(scrolled, TRUE);
    gtk_widget_set_margin_bottom(scrolled, 10);
    gtk_box_pack_start(GTK_BOX(content_area), scrolled, TRUE, TRUE, 5);

    GtkWidget *listbox = gtk_list_box_new();
    gtk_container_add(GTK_CONTAINER(scrolled), listbox);
    
    g_signal_connect(search_entry, "search-changed", G_CALLBACK(on_search_changed), listbox);
    populate_preset_list(listbox, "");

    gtk_widget_show_all(dialog);

    if (gtk_dialog_run(GTK_DIALOG(dialog)) == GTK_RESPONSE_ACCEPT) {
        GtkListBoxRow *selected = gtk_list_box_get_selected_row(GTK_LIST_BOX(listbox));
        if (selected != nullptr) {
            char* path = (char*) g_object_get_data(G_OBJECT(selected), "filepath");
            if (path) load_presets_from_file(path);
        }
    }
    gtk_widget_destroy(dialog);
}

static void on_deploy_clicked(GtkWidget *widget, gpointer data) {
    // Switch to the terminal tab (index 4) automatically since we have 4 tabs now
    if (main_notebook != nullptr) {
        gtk_notebook_set_current_page(GTK_NOTEBOOK(main_notebook), 4);
    }
    
    append_log("\n>> Syncing tweaks & speeds to Java Constants...\n");
    
    // Update internal models from UI bindings
    for (auto& t : tweaks) {
        if (t.checkbox != nullptr && t.is_interactive) {
            t.state = gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(t.checkbox));
        }
    }
    for (auto& s : speed_settings) {
        if (s.slider != nullptr) {
            s.value = gtk_range_get_value(GTK_RANGE(s.slider));
        }
    }
    for (auto& p : pid_settings) {
        if (p.slider != nullptr) {
            p.value = gtk_range_get_value(GTK_RANGE(p.slider));
        }
    }
    
    // Flush to files
    write_speed_constants();
    write_tweak_constants();
    write_pid_constants();
    append_log(">> Saved SpeedConstants.java, TweakConstants.java, and PIDConstants.java successfully.\n");
    
    append_log(">> Running ./gradlew deploy in background...\n");
    while (gtk_events_pending()) gtk_main_iteration();
    
    std::array<char, 256> buffer;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen("cd .. && ./gradlew deploy 2>&1", "r"), pclose);
    if (!pipe) {
        append_log("!! Failed to start gradle deploy.\n");
        return;
    }
    
    while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
        append_log(buffer.data());
        while (gtk_events_pending()) gtk_main_iteration();
    }
    append_log("\n>> Deploy process terminated.\n");
}

static void on_auto_changed(GtkComboBoxText *widget, gpointer user_data) {
    if (auto_pub) {
        gchar* text = gtk_combo_box_text_get_active_text(widget);
        if (text) {
            auto_pub.Set(text);
            append_log(std::string(">> Pushed AutoMode: ") + text + "\n");
            g_free(text);
        }
    }
}

static gboolean update_telemetry_ui(gpointer user_data) {
    if (nt_inst) {
        auto table = nt_inst.GetTable("SmartDashboard");
        double battery = table->GetEntry("BatteryVoltage").GetDouble(0.0);
        double rpm = table->GetEntry("TurretRPM").GetDouble(0.0);
        
        std::stringstream vb;
        vb << "<span font_family='monospace' size='24000' color='#00ff00'>" << std::fixed << std::setprecision(2) << battery << " V</span>";
        gtk_label_set_markup(GTK_LABEL(telemetry_battery_lbl), vb.str().c_str());

        std::stringstream rb;
        rb << "<span font_family='monospace' size='24000' color='#ffaa00'>" << std::fixed << std::setprecision(1) << rpm << " RPM</span>";
        gtk_label_set_markup(GTK_LABEL(telemetry_turret_lbl), rb.str().c_str());
    }
    return TRUE; // Continue polling
}

static void activate(GtkApplication *app, gpointer user_data) {
    GtkWidget *window;
    GtkWidget *main_box;
    GtkWidget *notebook;
    GtkWidget *tweak_grid;
    GtkWidget *speed_grid;
    GtkWidget *scrolled_tweaks;
    GtkWidget *scrolled_speeds;
    GtkWidget *button;
    GtkWidget *scrolled_log;

    // Window Setup
    window = gtk_application_window_new(app);
    gtk_window_set_title(GTK_WINDOW(window), "Project Cerberus: Tweak Engine");
    gtk_window_set_default_size(GTK_WINDOW(window), 1400, 700);

    main_box = gtk_box_new(GTK_ORIENTATION_VERTICAL, 15);
    gtk_container_set_border_width(GTK_CONTAINER(main_box), 20);
    gtk_container_add(GTK_CONTAINER(window), main_box);

    // Title label
    GtkWidget *title_lbl = gtk_label_new("");
    gtk_label_set_markup(GTK_LABEL(title_lbl), "<span font_family='monospace' size='xx-large' weight='heavy' color='#ffffff'>[ SYSTEM CONFIGURATION CORE ]</span>");
    gtk_box_pack_start(GTK_BOX(main_box), title_lbl, FALSE, FALSE, 5);

    // Initialize Notebook Tabs
    main_notebook = gtk_notebook_new();
    gtk_box_pack_start(GTK_BOX(main_box), main_notebook, TRUE, TRUE, 0);

    // Initialize NT4
    nt_inst = nt::NetworkTableInstance::GetDefault();
    nt_inst.StartClient4("Thalia Tweak GUI");
    nt_inst.SetServer("127.0.0.1"); // Or 10.TE.AM.2
    
    auto sd_table = nt_inst.GetTable("SmartDashboard");
    auto_pub = sd_table->GetStringTopic("AutoMode").Publish();
    auto_pub.Set("DoNothing"); // Init default

    bool actual_tuning_mode = read_speed_constants_tuning_mode();
    for (auto& t : tweaks) {
        if (t.id == "TUNING_MODE") t.state = actual_tuning_mode;
    }
    read_tweak_constants();
    read_speed_constants();
    read_pid_constants();

    // ===================================
    // TAB 1: TWEAK CONSTANTS
    // ===================================
    scrolled_tweaks = gtk_scrolled_window_new(NULL, NULL);
    gtk_widget_set_size_request(scrolled_tweaks, -1, 350);
    gtk_scrolled_window_set_policy(GTK_SCROLLED_WINDOW(scrolled_tweaks), GTK_POLICY_NEVER, GTK_POLICY_AUTOMATIC);

    tweak_grid = gtk_grid_new();
    gtk_grid_set_row_spacing(GTK_GRID(tweak_grid), 20);
    gtk_grid_set_column_spacing(GTK_GRID(tweak_grid), 40);
    gtk_container_set_border_width(GTK_CONTAINER(tweak_grid), 15);
    gtk_container_add(GTK_CONTAINER(scrolled_tweaks), tweak_grid);

    // Layout checkboxes in a 5 column grid
    for (size_t i = 0; i < tweaks.size(); ++i) {
        std::string label_text = "<span font_family='monospace' size='medium' color='#888888'>" + tweaks[i].id + "</span>\n<span color='#ffffff' weight='bold' size='large'>" + tweaks[i].name + "</span>";
        GtkWidget *check = gtk_check_button_new();
        GtkWidget *lbl = gtk_label_new(NULL);
        gtk_label_set_markup(GTK_LABEL(lbl), label_text.c_str());
        gtk_container_add(GTK_CONTAINER(check), lbl);
        
        tweaks[i].checkbox = check;
        gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(check), tweaks[i].state);
        
        GtkStyleContext *context = gtk_widget_get_style_context(check);
        gtk_style_context_add_class(context, "custom-check");
        
        int p_left = i % 5;
        int p_top = i / 5;
        gtk_grid_attach(GTK_GRID(tweak_grid), check, p_left, p_top, 1, 1);
    }
    
    GtkWidget *tweak_tab_label = gtk_label_new("Tweak Constants");
    gtk_notebook_append_page(GTK_NOTEBOOK(main_notebook), scrolled_tweaks, tweak_tab_label);

    // ===================================
    // UNIFIED GROUPED CONSTANTS TABS
    // ===================================
    struct UnifiedSetting {
        std::string id;
        double* value_ptr;
        GtkWidget** slider_ptr; 
        GtkWidget** spin_ptr;
        bool is_pid;
    };

    std::vector<UnifiedSetting> all_settings;
    for(auto& s : speed_settings) all_settings.push_back({s.id, &s.value, &s.slider, &s.spin_btn, false});
    for(auto& p : pid_settings) all_settings.push_back({p.id, &p.value, &p.slider, &p.spin_btn, true});

    std::map<std::string, std::vector<UnifiedSetting>> prefix_map;
    for(auto& s : all_settings) {
        std::string prefix = s.id;
        if (prefix.find("_MAX_SPEED") != std::string::npos) prefix = prefix.substr(0, prefix.find("_MAX_SPEED"));
        else if (prefix.find("_SENSITIVITY") != std::string::npos) prefix = prefix.substr(0, prefix.find("_SENSITIVITY"));
        else if (prefix.find("_kP") != std::string::npos) prefix = prefix.substr(0, prefix.find("_kP"));
        else if (prefix.find("_kI") != std::string::npos) prefix = prefix.substr(0, prefix.find("_kI"));
        else if (prefix.find("_kD") != std::string::npos) prefix = prefix.substr(0, prefix.find("_kD"));
        else if (prefix.find("_FF") != std::string::npos) prefix = prefix.substr(0, prefix.find("_FF"));
        prefix_map[prefix].push_back(s);
    }

    std::vector<std::string> comp_categories = {"Drive Components", "Turret Components", "Cargo Components"};
    for (int cat_idx = 0; cat_idx < 3; cat_idx++) {
        GtkWidget *scrolled_cat = gtk_scrolled_window_new(NULL, NULL);
        gtk_widget_set_size_request(scrolled_cat, -1, 350);
        gtk_scrolled_window_set_policy(GTK_SCROLLED_WINDOW(scrolled_cat), GTK_POLICY_NEVER, GTK_POLICY_AUTOMATIC);

        GtkWidget *flowbox = gtk_flow_box_new();
        gtk_flow_box_set_selection_mode(GTK_FLOW_BOX(flowbox), GTK_SELECTION_NONE);
        gtk_flow_box_set_max_children_per_line(GTK_FLOW_BOX(flowbox), 3);
        gtk_flow_box_set_row_spacing(GTK_FLOW_BOX(flowbox), 15);
        gtk_flow_box_set_column_spacing(GTK_FLOW_BOX(flowbox), 20);
        gtk_container_set_border_width(GTK_CONTAINER(flowbox), 15);
        gtk_container_add(GTK_CONTAINER(scrolled_cat), flowbox);

        for (auto const& [prefix, settings] : prefix_map) {
            int belongs_to = 2; // Default to Cargo
            if (prefix.find("FRONT") != std::string::npos || 
                prefix.find("BACK") != std::string::npos ||
                prefix.find("DRIVE") != std::string::npos ||
                prefix.find("TURN") != std::string::npos) {
                belongs_to = 0;
            } else if (prefix.find("TURRET") != std::string::npos ||
                       prefix.find("AIM") != std::string::npos ||
                       prefix.find("HOOD") != std::string::npos) {
                belongs_to = 1;
            }
            if (belongs_to != cat_idx) continue;

            GtkWidget *frame = gtk_frame_new(NULL);
            std::string frame_title = "<span font_family='monospace' color='#aaaaaa' weight='bold'> [ " + prefix + " ] </span>";
            GtkWidget *flbl = gtk_label_new(NULL);
            gtk_label_set_markup(GTK_LABEL(flbl), frame_title.c_str());
            gtk_frame_set_label_widget(GTK_FRAME(frame), flbl);
            
            GtkStyleContext *f_ctx = gtk_widget_get_style_context(frame);
            gtk_style_context_add_class(f_ctx, "motor-frame");

            GtkWidget *vbox = gtk_box_new(GTK_ORIENTATION_VERTICAL, 10);
            gtk_container_set_border_width(GTK_CONTAINER(vbox), 12);
            gtk_container_add(GTK_CONTAINER(frame), vbox);

            for(auto& set : settings) {
                GtkWidget *hbox = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 10);
                
                std::string suffix = set.id.substr(prefix.length());
                if (!suffix.empty() && suffix[0] == '_') suffix = suffix.substr(1);
                
                std::string lbl_text = "<span font_family='monospace' weight='bold' color='#ffffff'>" + suffix + "</span>";
                GtkWidget *lbl = gtk_label_new(NULL);
                gtk_label_set_markup(GTK_LABEL(lbl), lbl_text.c_str());
                gtk_widget_set_size_request(lbl, 110, -1);
                gtk_label_set_xalign(GTK_LABEL(lbl), 1.0); // right align text against slider
                gtk_box_pack_start(GTK_BOX(hbox), lbl, FALSE, FALSE, 0);

                GtkAdjustment *adj;
                if (set.is_pid) {
                    adj = GTK_ADJUSTMENT(gtk_adjustment_new(*(set.value_ptr), 0.0, 1.0, 0.001, 0.01, 0.0));
                } else {
                    adj = GTK_ADJUSTMENT(gtk_adjustment_new(*(set.value_ptr), 0.0, 100.0, 1.0, 5.0, 0.0));
                }

                GtkWidget *scale = gtk_scale_new(GTK_ORIENTATION_HORIZONTAL, adj);
                gtk_widget_set_size_request(scale, 160, -1);
                *(set.slider_ptr) = scale;
                GtkStyleContext *s_ctx = gtk_widget_get_style_context(scale);
                gtk_style_context_add_class(s_ctx, "custom-scale");
                gtk_box_pack_start(GTK_BOX(hbox), scale, TRUE, TRUE, 0);

                GtkWidget *spin = gtk_spin_button_new(adj, 0.001, set.is_pid ? 3 : 1);
                GtkStyleContext *sp_ctx = gtk_widget_get_style_context(spin);
                gtk_style_context_add_class(sp_ctx, "custom-spin");
                gtk_widget_set_size_request(spin, 70, -1);
                *(set.spin_ptr) = spin;
                gtk_box_pack_start(GTK_BOX(hbox), spin, FALSE, FALSE, 0);

                gtk_box_pack_start(GTK_BOX(vbox), hbox, FALSE, FALSE, 0);
            }
            gtk_flow_box_insert(GTK_FLOW_BOX(flowbox), frame, -1);
        }
        GtkWidget *cat_tab_label = gtk_label_new(comp_categories[cat_idx].c_str());
        gtk_notebook_append_page(GTK_NOTEBOOK(main_notebook), scrolled_cat, cat_tab_label);
    }

    // ===================================
    // TAB 4: LIVE TELEMETRY DASHBOARD
    // ===================================
    GtkWidget *dash_box = gtk_box_new(GTK_ORIENTATION_VERTICAL, 20);
    gtk_container_set_border_width(GTK_CONTAINER(dash_box), 20);
    
    // Auto Mode Selector
    GtkWidget *auto_frame = gtk_frame_new(NULL);
    GtkWidget *auto_lbl = gtk_label_new(NULL);
    gtk_label_set_markup(GTK_LABEL(auto_lbl), "<span font_family='monospace' color='#aaaaaa' weight='bold'> [ AUTONOMOUS ROUTINE SELECTOR ] </span>");
    gtk_frame_set_label_widget(GTK_FRAME(auto_frame), auto_lbl);
    GtkStyleContext *af_ctx = gtk_widget_get_style_context(auto_frame);
    gtk_style_context_add_class(af_ctx, "motor-frame");
    
    GtkWidget *auto_box = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 15);
    gtk_container_set_border_width(GTK_CONTAINER(auto_box), 15);
    GtkWidget *combo = gtk_combo_box_text_new();
    gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(combo), "DoNothing");
    gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(combo), "DriveStraight");
    gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(combo), "TwoPieceAmp");
    gtk_combo_box_text_append_text(GTK_COMBO_BOX_TEXT(combo), "ThreePieceSource");
    gtk_combo_box_set_active(GTK_COMBO_BOX(combo), 0);
    g_signal_connect(combo, "changed", G_CALLBACK(on_auto_changed), NULL);
    
    gtk_box_pack_start(GTK_BOX(auto_box), gtk_label_new("Force NetworkTables Routine Override: "), FALSE, FALSE, 0);
    gtk_box_pack_start(GTK_BOX(auto_box), combo, TRUE, TRUE, 0);
    gtk_container_add(GTK_CONTAINER(auto_frame), auto_box);
    gtk_box_pack_start(GTK_BOX(dash_box), auto_frame, FALSE, FALSE, 0);

    // Live Readouts
    GtkWidget *live_frame = gtk_frame_new(NULL);
    GtkWidget *llbl = gtk_label_new(NULL);
    gtk_label_set_markup(GTK_LABEL(llbl), "<span font_family='monospace' color='#aaaaaa' weight='bold'> [ LIVE SYSTEM READOUTS ] </span>");
    gtk_frame_set_label_widget(GTK_FRAME(live_frame), llbl);
    GtkStyleContext *lf_ctx = gtk_widget_get_style_context(live_frame);
    gtk_style_context_add_class(lf_ctx, "motor-frame");

    GtkWidget *readout_grid = gtk_grid_new();
    gtk_container_set_border_width(GTK_CONTAINER(readout_grid), 20);
    gtk_grid_set_row_spacing(GTK_GRID(readout_grid), 20);
    gtk_grid_set_column_spacing(GTK_GRID(readout_grid), 40);

    GtkWidget *b_title = gtk_label_new(NULL);
    gtk_label_set_markup(GTK_LABEL(b_title), "<span font_family='monospace' size='large' color='#888888'>BATTERY POTENTIAL</span>");
    telemetry_battery_lbl = gtk_label_new(NULL);
    gtk_label_set_markup(GTK_LABEL(telemetry_battery_lbl), "<span font_family='monospace' size='24000' color='#00ff00'>-- V</span>");

    GtkWidget *t_title = gtk_label_new(NULL);
    gtk_label_set_markup(GTK_LABEL(t_title), "<span font_family='monospace' size='large' color='#888888'>TURRET VELOCITY</span>");
    telemetry_turret_lbl = gtk_label_new(NULL);
    gtk_label_set_markup(GTK_LABEL(telemetry_turret_lbl), "<span font_family='monospace' size='24000' color='#ffaa00'>-- RPM</span>");

    gtk_grid_attach(GTK_GRID(readout_grid), b_title, 0, 0, 1, 1);
    gtk_grid_attach(GTK_GRID(readout_grid), telemetry_battery_lbl, 0, 1, 1, 1);
    gtk_grid_attach(GTK_GRID(readout_grid), t_title, 1, 0, 1, 1);
    gtk_grid_attach(GTK_GRID(readout_grid), telemetry_turret_lbl, 1, 1, 1, 1);
    
    gtk_container_add(GTK_CONTAINER(live_frame), readout_grid);
    gtk_box_pack_start(GTK_BOX(dash_box), live_frame, TRUE, TRUE, 0);

    GtkWidget *dash_tab_label = gtk_label_new("Live Dashboard");
    gtk_notebook_append_page(GTK_NOTEBOOK(main_notebook), dash_box, dash_tab_label);
    
    // Poll telemetry at 20Hz (50ms)
    g_timeout_add(50, update_telemetry_ui, NULL);

    // ===================================
    // TAB 5: DEPLOYMENT TERMINAL
    // ===================================
    scrolled_log = gtk_scrolled_window_new(NULL, NULL);
    // Must set VEXPAND so it fills the notebook page nicely
    gtk_widget_set_vexpand(scrolled_log, TRUE);

    log_view = gtk_text_view_new();
    gtk_text_view_set_editable(GTK_TEXT_VIEW(log_view), FALSE);
    gtk_text_view_set_cursor_visible(GTK_TEXT_VIEW(log_view), FALSE);
    log_buffer = gtk_text_view_get_buffer(GTK_TEXT_VIEW(log_view));
    gtk_container_add(GTK_CONTAINER(scrolled_log), log_view);
    
    GtkWidget *log_tab_label = gtk_label_new("Deployment Terminal");
    gtk_notebook_append_page(GTK_NOTEBOOK(main_notebook), scrolled_log, log_tab_label);

    // ===================================
    // BOTTOM FOOTER BUTTONS
    // ===================================
    GtkWidget *button_box = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 15);
    gtk_box_set_homogeneous(GTK_BOX(button_box), TRUE);
    gtk_box_pack_start(GTK_BOX(main_box), button_box, FALSE, FALSE, 10);
    
    GtkWidget *btn_load = gtk_button_new_with_label("LOAD PRESET");
    gtk_style_context_add_class(gtk_widget_get_style_context(btn_load), "deploy-btn");
    g_signal_connect(btn_load, "clicked", G_CALLBACK(on_load_clicked), NULL);
    gtk_box_pack_start(GTK_BOX(button_box), btn_load, TRUE, TRUE, 0);

    GtkWidget *btn_save = gtk_button_new_with_label("SAVE PRESET");
    gtk_style_context_add_class(gtk_widget_get_style_context(btn_save), "deploy-btn");
    g_signal_connect(btn_save, "clicked", G_CALLBACK(on_save_clicked), NULL);
    gtk_box_pack_start(GTK_BOX(button_box), btn_save, TRUE, TRUE, 0);
    
    button = gtk_button_new_with_label("APPLY TO ROBOT");
    gtk_style_context_add_class(gtk_widget_get_style_context(button), "deploy-btn");
    g_signal_connect(button, "clicked", G_CALLBACK(on_deploy_clicked), NULL);
    gtk_box_pack_start(GTK_BOX(button_box), button, TRUE, TRUE, 0);
    
    // Stark Black and White Lockheed Martin styling
    GtkCssProvider *cssProvider = gtk_css_provider_new();
    std::string css = 
        "window { background-color: #0a0a0a; color: #ffffff; }"
        "textview text { background-color: #000000; color: #ffffff; font-family: monospace; font-size: 11pt; padding: 15px; border-top: 1px solid #333333; }"
        "notebook header { background-color: #1a1a1a; font-family: monospace; font-weight: bold; border-bottom: 2px solid #ffffff; } "
        "notebook header tab { padding: 12px; border: 1px solid #333333; color: #888888; background-color: #0a0a0a; } "
        "notebook header tab:checked { color: #ffffff; border-bottom: none; background-color: #1f1f1f; } "
        ".deploy-btn { background-color: #ffffff; color: #000000; font-weight: heavy; font-family: monospace; font-size: 14pt; border-radius: 0px; border: 1px solid #ffffff; padding: 15px; margin-top: 10px; text-transform: uppercase; }"
        ".deploy-btn:hover { background-color: #cccccc; border: 1px solid #cccccc; }"
        ".custom-check { padding: 8px; border-left: 3px solid #333333; background-color: #111111; transition: all 200ms ease; }"
        ".custom-check:hover { background-color: #1f1f1f; border-left: 3px solid #999999; }"
        ".custom-check:checked { border-left: 3px solid #ffffff; background-color: #222222; }"
        ".motor-frame { border: 1px solid #333; background-color: #111; border-radius: 4px; } "
        ".custom-spin { background-color: #000; color: #fff; border: 1px solid #444; font-family: monospace; font-weight: bold; } "
        "scale highlight { background-color: #ffffff; } "
        "scale trough { background-color: #333333; border-radius: 0px; } "
        "scale slider { background-color: #ffffff; border-radius: 0px; border: 1px solid #888888; } "
        "scrollbar slider { background-color: #555555; border-radius: 0px; min-width: 6px; margin: 2px; }"
        "scrollbar slider:hover { background-color: #888888; }";
    gtk_css_provider_load_from_data(cssProvider, css.c_str(), -1, NULL);
    gtk_style_context_add_provider_for_screen(gdk_screen_get_default(), GTK_STYLE_PROVIDER(cssProvider), GTK_STYLE_PROVIDER_PRIORITY_USER);

    append_log("Initialized Tweak Engine Core.\nWaiting for deploy command...\n\n");
    gtk_widget_show_all(window);
}

int main(int argc, char **argv) {
    GtkApplication *app;
    int status;
    app = gtk_application_new("com.thalia.tweaks", G_APPLICATION_DEFAULT_FLAGS);
    g_signal_connect(app, "activate", G_CALLBACK(activate), NULL);
    status = g_application_run(G_APPLICATION(app), argc, argv);
    g_object_unref(app);
    return status;
}
