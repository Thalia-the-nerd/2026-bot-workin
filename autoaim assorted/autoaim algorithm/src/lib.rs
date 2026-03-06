use std::f64::consts::PI;

/// Calculates the required turret angle to hit a specific side of a square target.
/// The square target is defined by its center (target_x, target_y), its size (side length),
/// and its rotation in radians.
/// It is assumed the target side we want to hit is the "front" face, which lies 
/// along the local +X axis direction of the target's rotation.
///
/// Returns `true` if the robot has line-of-sight to the face, and writes the required
/// angle (in radians) into `out_angle`.
/// Returns `false` if the robot is behind the face (cannot hit it).
#[no_mangle]
pub extern "C" fn calculate_aim_angle(
    robot_x: f64,
    robot_y: f64,
    target_x: f64,
    target_y: f64,
    target_size: f64,
    target_rotation_rad: f64,
    out_angle: *mut f64,
    out_distance: *mut f64,
    out_required_rpm: *mut f64,
) -> bool {
    // Determine the center point of the designated face.
    // Assuming the "front" face is at +size/2 in the direction of the target's rotation.
    let face_center_x = target_x + (target_size / 2.0) * target_rotation_rad.cos();
    let face_center_y = target_y + (target_size / 2.0) * target_rotation_rad.sin();

    // The normal vector pointing outwards from the face.
    // It's in the same direction as the target's rotation.
    let normal_x = target_rotation_rad.cos();
    let normal_y = target_rotation_rad.sin();

    // The vector from the face center to the robot.
    let dx_to_robot = robot_x - face_center_x;
    let dy_to_robot = robot_y - face_center_y;

    // Dot product determines if the robot is "in front" of the face.
    // If dot > 0, the angle between the face normal and the vector to the robot is < 90 deg.
    let dot = dx_to_robot * normal_x + dy_to_robot * normal_y;

    if dot > 0.0 {
        // We have line of sight to the face. Calculate the angle required to point
        // the turret FROM the robot TO the face center.
        let aim_dx = face_center_x - robot_x;
        let aim_dy = face_center_y - robot_y;
        
        unsafe {
            if !out_angle.is_null() {
                *out_angle = aim_dy.atan2(aim_dx);
            }
            if !out_distance.is_null() {
                *out_distance = (aim_dx * aim_dx + aim_dy * aim_dy).sqrt();
            }
            if !out_required_rpm.is_null() {
                // Simplified RPM model: Map RPM linearly or quadratically to distance
                // For this simulation: RPM = distance * 2.5 + 500
                let dist = (aim_dx * aim_dx + aim_dy * aim_dy).sqrt();
                *out_required_rpm = (dist * 2.5) + 500.0;
            }
        }
        true
    } else {
        // The robot is behind the face, or exactly parallel. It cannot hit the specified side.
        false
    }
}
