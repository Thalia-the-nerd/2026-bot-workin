# Deploying Code and Connecting Driver Station

This guide covers how to deploy the FRC robot code and how to operate it using the Driver Station from a different device.

## 1. Deploy code to the Robot
1. Ensure your current device (where the code is located) is connected to the robot. You can connect via:
   - **USB** (using a USB A-to-B cable plugged directly into the roboRIO)
   - **Ethernet** directly to the roboRIO or radio
   - **Wi-Fi** to the robot's radio network
2. Open a terminal in the project directory (`/home/thalia/dev/projects/robotics/2026/2026-bot-main`).
3. Run the WPILib deploy command:
   ```bash
   ./gradlew deploy
   ```
4. Wait for the "BUILD SUCCESSFUL" message. The code is now on the roboRIO and it will restart automatically.

## 2. Using Driver Station on Another Device
We assume you are using a standard Windows laptop for the Driver Station application.

1. **Connect to the Robot Network**:
   - On the Driver Station device, connect to the robot's Wi-Fi network (or connect via Ethernet or USB). Ensure you have network connectivity to the roboRIO.
2. **Open the FRC Driver Station**:
   - Open the "NI Driver Station" application.
3. **Configure the Team Number**:
   - In the Driver Station settings (gear icon tab on the left), make sure your Team Number is set correctly. This allows it to automatically find the robot's IP (e.g., `10.TE.AM.2` or roborio mDNS).
4. **Attach Controllers**:
   - Plug your flight stick or gamepad into the Driver Station device. In the USB tab (usb icon) on the Driver Station, make sure your controller appears and is in the correct slot (e.g., port 0).
5. **Establish Connection**:
   - In the Driver Station's main Operation tab (steering wheel icon), look for the connection indicators. The `Communications`, `Robot Code`, and `Joysticks` lights should turn green.
6. **Enable and Drive**:
   - Select the desired mode (e.g., Teleoperated or Autonomous) and click **Enable**. The robot is now active and ready to be driven using your controller inputs sent over the network.
