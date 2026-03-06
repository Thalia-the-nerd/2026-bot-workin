#ifndef AUTOAIM_H
#define AUTOAIM_H

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Calculates the required turret angle to hit a specific side of a square target.
 * 
 * @param robot_x X coordinate of the robot.
 * @param robot_y Y coordinate of the robot.
 * @param target_x X coordinate of the square's center.
 * @param target_y Y coordinate of the square's center.
 * @param target_size Side length of the square target.
 * @param target_rotation_rad Rotation of the target in radians (determines where the face is).
 * @param out_angle Pointer to a double where the resulting angle (in radians) will be stored.
 * @param out_distance Pointer to a double where the resulting distance to target is stored.
 * @param out_required_rpm Pointer to a double where the required firing RPM is stored.
 * @return true if the face is visible and a valid angle was calculated, false otherwise.
 */
bool calculate_aim_angle(
    double robot_x,
    double robot_y,
    double target_x,
    double target_y,
    double target_size,
    double target_rotation_rad,
    double* out_angle,
    double* out_distance,
    double* out_required_rpm
);

#ifdef __cplusplus
}
#endif

#endif // AUTOAIM_H
