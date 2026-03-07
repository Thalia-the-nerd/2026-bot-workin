# Robotics Utilities: C & C++ Suggestions

Here are 25 suggestions for pure C utilities and 25 suggestions for C++ utilities that would be highly beneficial for day-to-day robot life, diagnostics, low-level interfacing, and performance.

## 25 C Utility Suggestions
*C is ideal for low-level device drivers, fast memory manipulation, determinism, and working directly with the OS/kernel.*

1. **`i2c_scanner.c`**: A lightweight utility to scan the I2C bus and list connected device addresses.
2. **`spi_debugger.c`**: Send raw hexadecimal payloads over the SPI bus and read responses for testing encoders or IMUs.
3. **`can_dump.c`**: A raw CAN bus sniffer that prints raw frames (ID, length, payload) directly from the SocketCAN interface.
4. **`can_injector.c`**: Tool to inject specific spoofed CAN frames onto the bus to test hardware behavior without the full robot code running.
5. **`gpio_toggler.c`**: A fast Memory-Mapped I/O utility to instantly toggle GPIO pins for testing LEDs, relays, and optical sensors.
6. **`ram_monitor.c`**: Scrapes `/proc/meminfo` to output lightweight free memory stats specifically tailored for the roboRIO/coprocessor.
7. **`cpu_temp_logger.c`**: Reads CPU thermal zones directly from `/sys/class/thermal/` and writes to a CSV for heat monitoring.
8. **`uart_echo.c`**: Simple serial forwarder/echo utility for testing RS-232/UART lidar or sensor connections.
9. **`udp_heartbeat.c`**: A background daemon that broadcasts a 1-byte UDP packet every 100ms to verify network link health.
10. **`usb_reset.c`**: A utility using `ioctl` commands to reset a specific USB port when a camera or peripheral hangs.
11. **`fast_crc16.c`**: A highly optimized hardware-accelerated (or lookup-table) CRC16 calculator used for validating sensor packets.
12. **`latency_tester.c`**: Sends a ping and uses highly precise POSIX `clock_gettime(CLOCK_MONOTONIC)` to measure sub-millisecond network jitter.
13. **`dmesg_filter.c`**: A custom wrapper around `dmesg` that highlights only kernel errors related to USB disconnects or network interfaces.
14. **`v4l2_configurator.c`**: Tool using V4L2 `ioctl` to manually set camera exposure, gain, and white balance straight at the driver level.
15. **`nvme_health.c`**: Reads smartmontools data or raw SMART blocks to predict disk failures on the coprocessor.
16. **`i2c_eeprom_flasher.c`**: Reads and writes raw binary images to I2C EEPROM chips.
17. **`pps_monitor.c`**: Pulse-Per-Second accuracy analyzer for verifying hardware clock synchronization to GPS or PTP.
18. **`tty_baud_tester.c`**: Iteratively cycles a serial port through baud rates and prints garbage/ASCII analysis to guess an unknown device's baud rate.
19. **`eth_link_stat.c`**: Calls `ethtool` APIs in C to detect if the physical RJ45 cable is unplugged.
20. **`watchdog_daemon.c`**: A lightweight `/dev/watchdog` ticker that reboots the coprocessor if the main robot loop hangs.
21. **`shared_mem_initializer.c`**: Creates and formats POSIX `/dev/shm` shared memory blocks for use by other programs.
22. **`mmap_register_reader.c`**: Directly reads physical memory addresses (bypassing the kernel) to dump raw register states of the processor.
23. **`pwm_generator.c`**: Generates raw PWM signals via `sysfs` for testing servos without requiring the main robot library.
24. **`kernel_module_loader.c`**: Safely uses `insmod`/`rmmod` logic to load/unload custom camera or CAN drivers based on hardware detection.
25. **`battery_voltage_adc.c`**: Reads a generic Linux ADC pin to monitor custom external 12V batteries on coprocessors.

---

## 25 C++ Utility Suggestions
*C++ is ideal for complex algorithms, object-oriented abstractions, multithreading, and high-performance vision processing.*

1. **`PoseEstimator.cpp`**: An Extended Kalman Filter (EKF) utility for fusing odometry, IMU, and vision data into a single 3D pose.
2. **`NetworkTablesLogger.cpp`**: A standalone C++ application that connects to NT4 via `ntcore` and logs all value changes to an SQLite database.
3. **`CameraCalibration.cpp`**: An OpenCV-based CLI tool to capture checkerboard images and output the camera matrix/distortion XML files.
4. **`TrajectoryGenerator.cpp`**: A standalone Quintic Hermite Spline generator that outputs JSON files containing pre-computed paths.
5. **`CollisionSimulator.cpp`**: Uses the GJK algorithm to detect bounding box collisions between the robot's CAD model and field elements.
6. **`LogParser.cpp`**: A fast `.wpilog` parser that converts WPILib data logs into CSVs or JSON for Python analysis.
7. **`LidarProcessor.cpp`**: A C++ tool utilizing PCL (Point Cloud Library) to downsample and run ICP (Iterative Closest Point) on 3D lidar scans.
8. **`ThreadProfiler.cpp`**: A custom thread-wrapping class that tracks median/max execution times of your periodic loops and warns on overruns.
9. **`FiducialTracker.cpp`**: A high-speed AprilTag detection utility using the native umich AprilTag C++ library or OpenCV Aruco.
10. **`SwerveKinematics.cpp`**: Forward and inverse kinematics math utility functions to translate `[vx, vy, omega]` to module wheel speeds and angles.
11. **`PIDTuner.cpp`**: A standalone utility that implements Ziegler-Nichols auto-tuning logic to calculate optimal P, I, and D parameters.
12. **`FFmpegStreamer.cpp`**: A hardware-accelerated H.264 video streamer that encodes OpenCV `cv::Mat` frames and sends them over WebRTC/RTSP.
13. **`PathSmoother.cpp`**: Applies gradient descent algorithms to smooth out jerky, human-drawn waypoints into drivable curves.
14. **`CAN_Bus_Load_Analyzer.cpp`**: Samples CAN traffic over time and calculates the % bus utilization, throwing alerts if it exceeds 80%.
15. **`ReplayEngine.cpp`**: A utility that reads a `.wpilog` and re-injects the telemetry back into NetworkTables to "replay" a match locally.
16. **`GeometryMath.cpp`**: A header-only library providing high-speed vectorized `Pose2d`, `Pose3d`, `Rotation2d`, `Translation2d` math.
17. **`VisionLatencyCompensator.cpp`**: Maintains a circular buffer of past robot poses to correctly apply vision measurements using their historical timestamps.
18. **`KinematicsVisualizer.cpp`**: An ImGui-based C++ application running locally to visualize the robot's swerve module vectors in real time.
19. **`ZeroMQ_Bridge.cpp`**: A ZMQ Publisher/Subscriber utility to pass high-bandwidth data (like tensors) between a Python AI script and the C++ robot code.
20. **`MemoryLeakDetector.cpp`**: A specialized allocator wrapper that tracks memory allocations and dumps a report to help find leaks in vision code.
21. **`LedController.cpp`**: An object-oriented manager for Addressable LEDs, managing complex state-machine animations (rainbows, pulsing, flashing).
22. **`JoypadServer.cpp`**: Reads standard Linux `/dev/input/js0` devices and broadcasts HTTP/JSON joystick inputs to simulation tools.
23. **`KineticEnergyCalculator.cpp`**: Utility to compute the rotational inertia and kinetic energy of the shooter flywheels for spin-up predictability.
24. **`UnitTester.cpp`**: A GoogleTest (gtest) suite validating all core math routines (angles wrapping within -180 to 180, kinematics output).
25. **`StateMachineCompiler.cpp`**: A utility that reads a custom JSON/YAML file describing a complex autonomous routine and compiles it into C++ command hierarchies.
