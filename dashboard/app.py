import time
import threading
from flask import Flask, jsonify, render_template, request

try:
    import ntcore
    HAS_NTCORE = True
except ImportError:
    HAS_NTCORE = False
    print("WARNING: pyntcore not installed. NetworkTables connection will be simulated.")

app = Flask(__name__)

# Initialize NetworkTables
if HAS_NTCORE:
    inst = ntcore.NetworkTableInstance.getDefault()
    inst.setServerTeam(2026) # Or use inst.setServer("127.0.0.1") for local simulation
    inst.startClient4("Flask Dashboard")
    sd = inst.getTable("SmartDashboard")
else:
    sd = None

def get_nt_value(key, default=None):
    if not HAS_NTCORE or sd is None:
        return default
        
    topic = sd.getTopic(key)
    if not topic.exists():
        return default
        
    topic_type = topic.getTypeString()
    if topic_type == 'double':
        return sd.getEntry(key).getDouble(default)
    elif topic_type == 'boolean':
        return sd.getEntry(key).getBoolean(default)
    elif topic_type == 'string':
        return sd.getEntry(key).getString(default)
    elif topic_type == 'int':
        return sd.getEntry(key).getInteger(default)
        
    return default

def set_nt_value(key, value):
    if not HAS_NTCORE or sd is None:
        return
    # Depending on type, put it correctly
    if isinstance(value, float) or isinstance(value, int):
        sd.getEntry(key).setDouble(float(value))
    elif isinstance(value, bool):
        sd.getEntry(key).setBoolean(value)
    else:
        sd.getEntry(key).setString(str(value))

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/health')
def health():
    return jsonify({
        "status": "connected" if (HAS_NTCORE and inst.isConnected()) else "disconnected",
        "has_ntcore": HAS_NTCORE,
        "health": {
            "drive": {
                "average_distance": get_nt_value("Average Distance Traveled", 0.0),
                "gyro_yaw": get_nt_value("Current Gyro Yaw", 0.0),
                "gyro_calibrating": get_nt_value("Gyro Calibrating", False)
            },
            "intake": {
                "current_amps": get_nt_value("Intake Current (A)", 0.0),
                "jammed": get_nt_value("Intake Jammed", False)
            },
            "turret": {
                "position_rotations": get_nt_value("TurretPositionRotations", 0.0),
                "speed_rpm": get_nt_value("TurretVelocityRPM", 0.0)
            },
            "fire_control": {
                "speed_rpm": get_nt_value("FireVelocityRPM", 0.0)
            },
            "cameras": {
                "pose1_connected": get_nt_value("poseCamera1Connected", False),
                "pose2_connected": get_nt_value("poseCamera2Connected", False),
                "targeting_connected": get_nt_value("TargetingCamera1Connnected", False)
            }
        }
    })

SPEED_KEYS = [
    "FRONT_RIGHT_MAX_SPEED", "FRONT_RIGHT_SENSITIVITY",
    "FRONT_LEFT_MAX_SPEED", "FRONT_LEFT_SENSITIVITY",
    "INTAKE_MAIN_MAX_SPEED", "INTAKE_MAIN_SENSITIVITY",
    "INTAKE_SECONDARY_MAX_SPEED", "INTAKE_SECONDARY_SENSITIVITY",
    "LOADER_1_MAX_SPEED", "LOADER_1_SENSITIVITY",
    "LOADER_2_MAX_SPEED", "LOADER_2_SENSITIVITY",
    "LOADER_3_MAX_SPEED", "LOADER_3_SENSITIVITY",
    "TURRET_MAX_SPEED", "TURRET_SENSITIVITY",
    "FIRE_MAX_SPEED", "FIRE_SENSITIVITY"
]

@app.route('/api/speeds', methods=['GET', 'POST'])
def manage_speeds():
    if request.method == 'GET':
        speeds = {}
        for k in SPEED_KEYS:
            val = get_nt_value(f"Speed/{k}", 100.0)
            speeds[k] = val
        return jsonify(speeds)
    
    if request.method == 'POST':
        data = request.json
        if not data:
            return jsonify({"status": "error", "message": "No JSON payload"})
        
        updates = 0
        for k, v in data.items():
            if k in SPEED_KEYS:
                # Basic validation
                try:
                    vf = float(v)
                    vf = max(-100.0, min(100.0, vf)) # Limit to ranges
                    set_nt_value(f"Speed/{k}", vf)
                    updates += 1
                except ValueError:
                    continue
                    
        return jsonify({"status": "updated", "count": updates})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
