#!/usr/bin/env python3
import sys
import os
import csv
import argparse
import numpy as np
import subprocess

CONSTANTS_FILE = os.path.join(os.path.dirname(__file__), "../src/main/java/frc/robot/AutoAimConstants.java")
DATA_FILE = os.path.join(os.path.dirname(__file__), "autoaim_data.csv")

def load_data():
    distances = []
    angles = []
    speeds = []
    if os.path.exists(DATA_FILE):
        with open(DATA_FILE, "r") as f:
            reader = csv.DictReader(f)
            for row in reader:
                distances.append(float(row["distance"]))
                angles.append(float(row["angle"]))
                speeds.append(float(row["speed"]))
    return np.array(distances), np.array(angles), np.array(speeds)

def save_data(distance, angle, speed):
    file_exists = os.path.exists(DATA_FILE)
    with open(DATA_FILE, "a", newline='') as f:
        writer = csv.DictWriter(f, fieldnames=["distance", "angle", "speed"])
        if not file_exists:
            writer.writeheader()
        writer.writerow({"distance": distance, "angle": angle, "speed": speed})
    print(f"Added data point: distance={distance}, angle={angle}, speed={speed}")

def calculate_regression(poly_degree=2):
    X, y_angle, y_speed = load_data()
    if len(X) < poly_degree + 1:
        print(f"Not enough data to calculate polynomial degree {poly_degree}. Add more data points.")
        return None, None

    # Fit polynomials
    angle_coeffs = np.polyfit(X, y_angle, poly_degree)
    speed_coeffs = np.polyfit(X, y_speed, poly_degree)
    
    return angle_coeffs, speed_coeffs

def update_java_constants(angle_coeffs, speed_coeffs):
    if not os.path.exists(CONSTANTS_FILE):
        print(f"Error: Could not find {CONSTANTS_FILE}")
        return

    X, y_angle, y_speed = load_data()
    # Format data into a Java double array
    data_str = ",\n        ".join(f"{{{d}, {a}, {s}}}" for d, a, s in zip(X, y_angle, y_speed))
    
    content = f"""package frc.robot;

/**
 * Constants for the Auto-Aim Interpolation Map.
 * THIS FILE IS AUTOMATICALLY OVERWRITTEN BY THE PYTHON REGRESSION TOOL.
 * DO NOT EDIT MANUALLY.
 */
public final class AutoAimConstants {{
    // Array of {{distance, angle, speed}} empirical data points
    public static final double[][] RAW_DATA = {{
        {data_str}
    }};
}}
"""
    with open(CONSTANTS_FILE, "w") as f:
        f.write(content)
    print(f"Successfully updated {CONSTANTS_FILE}")
    
    print("Running Spotless formatter to ensure build compatibility...")
    try:
        project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
        subprocess.run(["./gradlew", "spotlessApply"], cwd=project_root, check=True, stdout=subprocess.DEVNULL)
        print("Spotless formatting applied successfully.")
    except Exception as e:
        print(f"Warning: Failed to run spotlessApply automatically: {e}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Auto Aim Regression Model CLI Tool")
    subparsers = parser.add_subparsers(dest="command")

    # Interactive mode
    parser_interactive = subparsers.add_parser("interactive", help="Run in interactive mode")

    # Add data point command
    parser_add = subparsers.add_parser("add", help="Add a new data point to the dataset")
    parser_add.add_argument("distance", type=float, help="Distance to the target (e.g., in meters)")
    parser_add.add_argument("angle", type=float, help="Shooter angle for successful shot")
    parser_add.add_argument("speed", type=float, help="Shooter speed (RPM) for successful shot")

    # Train command
    parser_train = subparsers.add_parser("train", help="Train the polynomial model and update Java constants")
    parser_train.add_argument("--degree", type=int, default=2, help="Degree of the polynomial (default: 2)")

    args = parser.parse_args()

    if args.command == "add":
        save_data(args.distance, args.angle, args.speed)
    elif args.command == "train":
        update_java_constants(None, None)
        print("Model translated to Interpolating Map in Java Constants!")
    elif args.command == "interactive" or not args.command:
        print("--- Auto Aim Regression Interactive Mode ---")
        while True:
            cmd = input("Command (add / train / print / quit): ").strip().lower()
            if cmd == "quit":
                break
            elif cmd == "add":
                try:
                    d = float(input("Distance: "))
                    a = float(input("Angle: "))
                    s = float(input("Speed: "))
                    save_data(d, a, s)
                except ValueError:
                    print("Invalid input. Please enter numbers.")
            elif cmd == "train":
                angle_coeffs, speed_coeffs = calculate_regression()
                if angle_coeffs is not None:
                    update_java_constants(angle_coeffs, speed_coeffs)
                    print(f"Updated Java Coefficients!")
            elif cmd == "print":
                X, ya, ys = load_data()
                print("Current dataset:")
                for dist, ang, spd in zip(X, ya, ys):
                    print(f"  Distance: {dist}, Angle: {ang}, Speed: {spd}")
            else:
                print("Unknown command.")
