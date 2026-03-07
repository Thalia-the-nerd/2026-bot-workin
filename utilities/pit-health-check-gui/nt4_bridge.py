import asyncio
import json
import logging
from ntcore import NetworkTableInstance
import websockets

logging.basicConfig(level=logging.INFO)

# Connect to NT4
nt_inst = NetworkTableInstance.getDefault()
nt_inst.setServer("10.20.26.2") # Team 2026
nt_inst.startClient4("PitHealthCheckBridge")

# We want to forward everything in SmartDashboard to the Rust GUI
table = nt_inst.getTable("SmartDashboard")

async def send_telemetry(websocket):
    while True:
        try:
            # Gather all variables we care about
            data = {
                "connected": nt_inst.isConnected(),
                "battery_voltage": table.getNumber('Battery/Voltage', 0.0),
                "navx_connected": table.getBoolean('NavX/Connected', False),
                "turret_ok": table.getBoolean('Turret/Motor_OK', False),
                "turret_temp": table.getNumber('Turret/Motor_Temp', 0.0),
                "fire_left_ok": table.getBoolean('FireControl/Left_Motor_OK', False),
                "fire_right_ok": table.getBoolean('FireControl/Right_Motor_OK', False),
                "drive_fl_ok": table.getBoolean('Drive/FL_OK', False),
                "drive_fr_ok": table.getBoolean('Drive/FR_OK', False),
                "drive_bl_ok": table.getBoolean('Drive/BL_OK', False),
                "drive_br_ok": table.getBoolean('Drive/BR_OK', False),
            }
            await websocket.send(json.dumps(data))
            await asyncio.sleep(0.1) # 10Hz
        except websockets.exceptions.ConnectionClosed:
            break
        except Exception as e:
            logging.error(f"Error sending telemetry: {e}")
            break

async def handler(websocket):
    logging.info("Rust GUI Connected to Python NT4 Bridge")
    await send_telemetry(websocket)

async def main():
    logging.info("Starting Python NT4-to-WebSocket Bridge on port 8765...")
    async with websockets.serve(handler, "localhost", 8765):
        await asyncio.Future()  # run forever

if __name__ == "__main__":
    asyncio.run(main())
