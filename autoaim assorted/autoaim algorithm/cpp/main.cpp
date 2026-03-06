#include <iostream>
#include <iomanip>
#include <cmath>
#include "../include/autoaim.h"

// Helper function to simulate a robot driving around a square target
void test_aim(double robot_x, double robot_y, double target_x, double target_y, double target_size, double target_rotation) {
    double angle = 0.0;
    double distance = 0.0;
    double rpm = 0.0;
    
    std::cout << "Robot @ (" << std::setw(5) << robot_x << ", " << std::setw(5) << robot_y << ")\t-> ";
    
    bool can_hit = calculate_aim_angle(
        robot_x, robot_y, 
        target_x, target_y, 
        target_size, target_rotation, 
        &angle, &distance, &rpm
    );
    
    if (can_hit) {
        // Convert radians to degrees for easier reading
        double angle_deg = angle * 180.0 / M_PI;
        std::cout << "[SUCCESS] Turf angle set to " << std::fixed << std::setprecision(2) << angle_deg 
                  << " deg | Dist: " << distance << " | RPM: " << rpm << std::endl;
    } else {
        std::cout << "[FAILED]  No line of sight to the designated face!" << std::endl;
    }
}

int main() {
    std::cout << "--- Autoaim Targeting Test Algorithm ---" << std::endl;
    
    // Target is a 2x2 square situated at the origin (0, 0), rotation 0 radians.
    // So the designated "front" face center is at (1, 0) and points towards the +X direction.
    double tx = 0.0;
    double ty = 0.0;
    double size = 2.0;
    double rot = 0.0; // Facing East (+X)
    
    std::cout << "Target: Size " << size << " at (0, 0), facing East (+X axis)\n" << std::endl;

    // Test 1: Robot is directly in front of the face (should be able to hit, shooting backwards/west)
    test_aim(5.0, 0.0, tx, ty, size, rot);
    
    // Test 2: Robot is strictly behind the target (cannot hit the front face)
    test_aim(-5.0, 0.0, tx, ty, size, rot);
    
    // Test 3: Robot is off to the diagonal front-right side
    test_aim(3.0, 3.0, tx, ty, size, rot);

    // Test 4: Robot is off to the diagonal back-left side (blocked by the body of the square)
    test_aim(-3.0, -3.0, tx, ty, size, rot);
    
    // Test 5: Target is rotated by 90 degrees (facing North / +Y axis)
    // Front face center is now at (0, 1) and points North.
    std::cout << "\nRotating target by 90 degrees (facing North, +Y axis)..." << std::endl;
    rot = M_PI / 2.0;
    
    // Robot is at (0, 5), directly North of target. Should hit successfully pointing South.
    test_aim(0.0, 5.0, tx, ty, size, rot);
    
    // Robot is at (5, 0), strictly East of target. Cannot hit the North face because it's parallel/blocked.
    test_aim(5.0, 0.0, tx, ty, size, rot);

    return 0;
}
