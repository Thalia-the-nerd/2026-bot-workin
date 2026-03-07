// =============================================================================
// Autoaim 3D Simulation
// =============================================================================
// A fully 3D interactive simulation of a robot with a turret targeting one 
// specific face of a cube target. Uses Macroquad's built-in 3D camera and 
// primitive rendering (cubes, spheres, lines).
//
// Controls:
//   WASD / Arrows  - Drive the robot on the XZ ground plane
//   Space          - Fire a projectile (affected by gravity + robot momentum)
//
// The GREEN face of the cube is the designated target face.
// The BLUE faces are the non-target sides.
//
// Projectiles that hit the green face vanish instantly (success).
// Projectiles that hit a blue face blink red for ~1 second, then vanish.
// =============================================================================

use macroquad::prelude::*;
use std::f32::consts::PI;

// --- SIMULATION CONSTANTS ---
const ROBOT_SPEED: f32 = 8.0;       // units per second on the XZ plane
const ROBOT_ROT_SPEED: f32 = 2.5;   // radians per second (chassis yaw)
const TURRET_ROT_SPEED: f32 = 5.0;  // radians per second (turret yaw tracking)
const GRAVITY: f32 = 9.81;          // m/s^2 downward (-Y in world space)
const PROJECTILE_SPEED: f32 = 20.0; // horizontal launch speed of the ball (m/s)
const ROBOT_HEIGHT: f32 = 0.5;      // height of the turret above the ground
const TARGET_SIZE: f32 = 2.0;       // side length of the cube target

// --- PROJECTILE STATE MACHINE ---
// Each projectile can be in one of three states:
//   Active   - flying through the air, affected by gravity
//   HitGreen - struck the designated front face (instant deletion)
//   HitBlue  - struck a non-target face (blinks red, then deleted)
#[derive(PartialEq)]
enum ProjectileState {
    Active,
    HitGreen,
    HitBlue,
}

struct Projectile {
    pos: Vec3,         // current world position
    vel: Vec3,         // current world velocity (gravity applied every frame)
    life: f32,         // seconds remaining before auto-despawn
    state: ProjectileState,
    blink_timer: f32,  // countdown for the red-blink effect on blue hits
    green_timer: f32,  // countdown for the green-flash effect on green hits
    trail: Vec<Vec3>,  // position history for drawing the parabolic arc
}

// --- CONTINUOUS COLLISION HELPERS ---

/// Check if a line segment (start to end) intersects a finite quad 
/// defined by four coplanar corners (in order). This prevents high-velocity 
/// projectiles from "tunneling" through thin faces between frames.
fn ray_hits_quad(
    start: Vec3, end: Vec3,
    a: Vec3, b: Vec3, c: Vec3, d: Vec3,
) -> bool {
    // Compute quad normal via cross product of two edges
    let edge1 = b - a;
    let edge2 = d - a;
    let normal = edge1.cross(edge2).normalize();

    let dir = end - start;
    let dot_dir = dir.dot(normal);

    // If parallel to plane, no intersection
    if dot_dir.abs() < 1e-6 { return false; }

    let t = (a - start).dot(normal) / dot_dir;
    
    // Check if intersection is within the segment [start, end]
    if t < 0.0 || t > 1.0 { return false; }

    let p = start + dir * t;

    // Check if point p is inside the quad using edge cross tests
    let ap = p - a;
    let bp = p - b;
    let cp = p - c;
    let dp = p - d;

    let ab = b - a;
    let bc = c - b;
    let cd = d - c;
    let da = a - d;

    let c1 = ab.cross(ap).dot(normal);
    let c2 = bc.cross(bp).dot(normal);
    let c3 = cd.cross(cp).dot(normal);
    let c4 = da.cross(dp).dot(normal);

    // Tolerance for edge grazing
    (c1 >= -0.01 && c2 >= -0.01 && c3 >= -0.01 && c4 >= -0.01)
        || (c1 <= 0.01 && c2 <= 0.01 && c3 <= 0.01 && c4 <= 0.01)
}

// --- ENTRY POINT ---
#[macroquad::main("Autoaim 3D Simulation")]
async fn main() {
    // Robot state (position on XZ plane, Y is always 0)
    let mut robot_x: f32 = 0.0;
    let mut robot_z: f32 = 10.0;
    let mut robot_yaw: f32 = -PI / 2.0; // facing towards -Z (towards target)

    // Turret yaw (world-space, independent of chassis)
    let mut turret_yaw: f32 = robot_yaw;

    // Target cube sits at the origin, 2 meters off the ground
    let target_pos = vec3(0.0, 2.0 + TARGET_SIZE / 2.0, 0.0);
    let target_rot: f32 = 0.0; // cube yaw rotation (0 = front face is +X)

    // Projectile pool
    let mut projectiles: Vec<Projectile> = vec![];

    // Camera state
    let mut cam_yaw: f32 = robot_yaw;
    let mut cam_pitch: f32 = 0.5; // rad up from horizontal
    let mut cam_dist: f32 = 12.0;

    let mut auto_fire_cooldown = 0.0;

    loop {
        let dt = get_frame_time();
        if auto_fire_cooldown > 0.0 {
            auto_fire_cooldown -= dt;
        }

        // =================================================================
        // 0. CAMERA INPUTS (Mouse Pan & Zoom)
        // =================================================================
        let mouse_delta = mouse_delta_position();
        if is_mouse_button_down(MouseButton::Right) || is_mouse_button_down(MouseButton::Left) {
            cam_yaw -= mouse_delta.x * 2.0;
            cam_pitch += mouse_delta.y * 2.0;
            cam_pitch = cam_pitch.clamp(0.1, PI / 2.0 - 0.1); // prevent flipping
        }
        let (_, scroll_y) = mouse_wheel();
        cam_dist -= scroll_y * 1.5;
        cam_dist = cam_dist.clamp(3.0, 50.0);

        // =================================================================
        // 1. DRIVING INPUTS (XZ plane movement)
        // =================================================================
        let mut robot_vx: f32 = 0.0;
        let mut robot_vz: f32 = 0.0;

        if is_key_down(KeyCode::Up) || is_key_down(KeyCode::W) {
            robot_vx = robot_yaw.cos() * ROBOT_SPEED;
            robot_vz = robot_yaw.sin() * ROBOT_SPEED;
        }
        if is_key_down(KeyCode::Down) || is_key_down(KeyCode::S) {
            robot_vx = -robot_yaw.cos() * ROBOT_SPEED;
            robot_vz = -robot_yaw.sin() * ROBOT_SPEED;
        }

        robot_x += robot_vx * dt;
        robot_z += robot_vz * dt;

        if is_key_down(KeyCode::Left) || is_key_down(KeyCode::A) {
            robot_yaw -= ROBOT_ROT_SPEED * dt;
        }
        if is_key_down(KeyCode::Right) || is_key_down(KeyCode::D) {
            robot_yaw += ROBOT_ROT_SPEED * dt;
        }

        // =================================================================
        // 2. AUTOAIM TARGETING (direct 3D quadratic lead solver)
        // =================================================================
        // The target's designated front face center (in world space):
        let face_offset_x = (TARGET_SIZE / 2.0) * target_rot.cos();
        let face_offset_z = (TARGET_SIZE / 2.0) * target_rot.sin();
        let face_center = vec3(
            target_pos.x + face_offset_x,
            target_pos.y, // center height of the cube
            target_pos.z + face_offset_z,
        );

        // Normal of the front face (pointing outward)
        let face_normal_x = target_rot.cos();
        let face_normal_z = target_rot.sin();

        // Check line of sight from robot
        let to_robot_x = robot_x - face_center.x;
        let to_robot_z = robot_z - face_center.z;
        let can_hit = (to_robot_x * face_normal_x + to_robot_z * face_normal_z) > 0.0;

        // Current actual position of the barrel tip given CURRENT turret_yaw
        let spawn_x = robot_x + turret_yaw.cos() * 1.5;
        let spawn_z = robot_z + turret_yaw.sin() * 1.5;

        // XZ distance from barrel tip to face center
        let dx = face_center.x - spawn_x;
        let dz = face_center.z - spawn_z;

        // Quadratic physical lead solver for exact time of flight (t) 
        // assuming fixed muzzle speed.
        // v_muzzle^2 * t^2 = (dx - vx * t)^2 + (dz - vz * t)^2
        // a*t^2 + b*t + c = 0
        let v_muzzle = PROJECTILE_SPEED;
        let a = v_muzzle * v_muzzle - robot_vx * robot_vx - robot_vz * robot_vz;
        let b = 2.0 * (dx * robot_vx + dz * robot_vz);
        let c = -(dx * dx + dz * dz);

        let mut desired_yaw = turret_yaw;
        let mut best_t = 0.1;
        
        let discriminant = b * b - 4.0 * a * c;
        if discriminant >= 0.0 {
            let t1 = (-b + discriminant.sqrt()) / (2.0 * a);
            let t2 = (-b - discriminant.sqrt()) / (2.0 * a);
            
            // Choose the smallest positive root (earliest hit)
            best_t = if t1 > 0.0 && t2 > 0.0 { t1.min(t2) } else if t1 > 0.0 { t1 } else { t2 };
            // Compute the target relative exit velocity direction
            if best_t > 0.0 {
                let req_vx_rel = dx / best_t - robot_vx;
                let req_vz_rel = dz / best_t - robot_vz;
                desired_yaw = req_vz_rel.atan2(req_vx_rel);
            }
        }

        // Required vertical velocity from this spawn pos to hit target height 
        // at the precise time `best_t`
        let dy = face_center.y - ROBOT_HEIGHT;
        let vy_launch = (dy + 0.5 * GRAVITY * best_t * best_t) / best_t;

        // HUD variables
        let current_distance = (dx * dx + dz * dz).sqrt() as f64;
        let fire_speed_total = (v_muzzle * v_muzzle + vy_launch * vy_launch).sqrt();
        let current_rpm = (fire_speed_total * 2.15) as f64;

        // =================================================================
        // 3. TURRET TRACKING (smooth rotation towards target)
        // =================================================================
        if can_hit {
            // Shortest angular path to desired yaw
            let mut diff = desired_yaw - turret_yaw;
            diff = (diff + PI) % (2.0 * PI) - PI;
            if diff < -PI { diff += 2.0 * PI; }

            let max_step = TURRET_ROT_SPEED * dt;
            if diff.abs() <= max_step {
                turret_yaw = desired_yaw;
            } else {
                turret_yaw += diff.signum() * max_step;
            }
        } else {
            // No target — revert turret to chassis forward
            let mut diff = robot_yaw - turret_yaw;
            diff = (diff + PI) % (2.0 * PI) - PI;
            if diff < -PI { diff += 2.0 * PI; }

            let max_step = TURRET_ROT_SPEED * dt;
            if diff.abs() <= max_step {
                turret_yaw = robot_yaw;
            } else {
                turret_yaw += diff.signum() * max_step;
            }
        }

        // Normalize turret_yaw to [-PI, PI]
        turret_yaw = (turret_yaw + PI) % (2.0 * PI) - PI;
        if turret_yaw < -PI { turret_yaw += 2.0 * PI; }

        // =================================================================
        // 4. FIRE PROJECTILE (Spacebar) — strict physically accurate shot
        // =================================================================
        // Here we perfectly combine the inherited robot velocity with the 
        // turret's relative muzzle velocity. No magnet effects—it strictly
        // relies on the turret having rotated to the correct `desired_yaw` 
        // lead calculated by the quadratic solver above.
        let is_locked = can_hit && (desired_yaw - turret_yaw).abs() < 0.05;

        // Auto-fire whenever firmly locked (with cooldown)
        if (is_locked || is_key_pressed(KeyCode::Space)) && auto_fire_cooldown <= 0.0 {
            auto_fire_cooldown = 0.25; // max 4 shots per second

            let spawn_pos = vec3(
                robot_x + turret_yaw.cos() * 1.5,
                ROBOT_HEIGHT,
                robot_z + turret_yaw.sin() * 1.5,
            );
            
            // Relative muzzle velocity is pointed strictly exactly where the turret faces
            let rel_vx = turret_yaw.cos() * PROJECTILE_SPEED;
            let rel_vz = turret_yaw.sin() * PROJECTILE_SPEED;
            
            // World horizontal velocity (inherited)
            let final_vx = robot_vx + rel_vx;
            let final_vz = robot_vz + rel_vz;

            // Compute exact vertical launch velocity using actual firing vector
            let dx_fire = face_center.x - spawn_pos.x;
            let dz_fire = face_center.z - spawn_pos.z;
            
            let speed_xz = (final_vx * final_vx + final_vz * final_vz).sqrt();
            let dist_xz = (dx_fire * dx_fire + dz_fire * dz_fire).sqrt();
            let mut t_flight = dist_xz / speed_xz.max(0.1);
            t_flight = t_flight.max(0.01); 
            
            let dy_fire = face_center.y - ROBOT_HEIGHT;
            let final_vy = (dy_fire + 0.5 * GRAVITY * t_flight * t_flight) / t_flight;

            let spawn_pos = vec3(
                robot_x + turret_yaw.cos() * 1.5,
                ROBOT_HEIGHT,
                robot_z + turret_yaw.sin() * 1.5,
            );

            projectiles.push(Projectile {
                pos: spawn_pos,
                vel: vec3(final_vx, final_vy, final_vz),
                life: 5.0,
                state: ProjectileState::Active,
                blink_timer: 1.0,
                green_timer: 0.5,
                trail: vec![spawn_pos], // start trail with spawn position
            });
        }

        // =================================================================
        // 5. COMPUTE CUBE FACE VERTICES (for collision & rendering)
        // =================================================================
        let half = TARGET_SIZE / 2.0;
        let cy = target_pos.y; // center Y of cube
        let cx = target_pos.x;
        let cz = target_pos.z;
        let rc = target_rot.cos();
        let rs = target_rot.sin();

        // Helper: rotate a local XZ offset around target center
        let rot_xz = |lx: f32, lz: f32| -> (f32, f32) {
            (cx + lx * rc - lz * rs, cz + lx * rs + lz * rc)
        };

        // Front face (+X local) — the GREEN designated face
        let (f1x, f1z) = rot_xz(half, -half);
        let (f2x, f2z) = rot_xz(half, half);
        let front_bottom_right = vec3(f1x, cy - half, f1z);
        let front_top_right    = vec3(f1x, cy + half, f1z);
        let front_top_left     = vec3(f2x, cy + half, f2z);
        let front_bottom_left  = vec3(f2x, cy - half, f2z);

        // Back face (-X local)
        let (b1x, b1z) = rot_xz(-half, half);
        let (b2x, b2z) = rot_xz(-half, -half);
        let back_bottom_right = vec3(b2x, cy - half, b2z);
        let back_top_right    = vec3(b2x, cy + half, b2z);
        let back_top_left     = vec3(b1x, cy + half, b1z);
        let back_bottom_left  = vec3(b1x, cy - half, b1z);

        // Right face (+Z local)
        let (r1x, r1z) = rot_xz(half, half);
        let (r2x, r2z) = rot_xz(-half, half);
        let right_bl = vec3(r1x, cy - half, r1z);
        let right_tl = vec3(r1x, cy + half, r1z);
        let right_tr = vec3(r2x, cy + half, r2z);
        let right_br = vec3(r2x, cy - half, r2z);

        // Left face (-Z local)
        let (l1x, l1z) = rot_xz(-half, -half);
        let (l2x, l2z) = rot_xz(half, -half);
        let left_bl = vec3(l1x, cy - half, l1z);
        let left_tl = vec3(l1x, cy + half, l1z);
        let left_tr = vec3(l2x, cy + half, l2z);
        let left_br = vec3(l2x, cy - half, l2z);

        // =================================================================
        // 6. UPDATE PROJECTILE PHYSICS & COLLISION
        // =================================================================
        for proj in &mut projectiles {
            if proj.state == ProjectileState::Active {
                let old_pos = proj.pos;

                // Apply gravity to Y velocity
                proj.vel.y -= GRAVITY * dt;

                // Integrate position
                proj.pos += proj.vel * dt;
                proj.life -= dt;

                let new_pos = proj.pos;

                // Record position for trail rendering
                proj.trail.push(new_pos);

                // Kill if it hits the ground
                if new_pos.y < 0.0 {
                    proj.state = ProjectileState::HitBlue; // treat ground hit as miss
                }

                // Check continuous ray collision against cube faces
                // Front face (GREEN) — SUCCESS
                if ray_hits_quad(old_pos, new_pos,
                    front_bottom_right, front_top_right,
                    front_top_left, front_bottom_left)
                {
                    proj.state = ProjectileState::HitGreen;
                    proj.pos = new_pos; // optionally clamp strictly to plane
                }
                // Back face
                else if ray_hits_quad(old_pos, new_pos,
                    back_bottom_right, back_top_right,
                    back_top_left, back_bottom_left)
                {
                    proj.state = ProjectileState::HitBlue;
                }
                // Right face
                else if ray_hits_quad(old_pos, new_pos,
                    right_bl, right_tl, right_tr, right_br)
                {
                    proj.state = ProjectileState::HitBlue;
                }
                // Left face
                else if ray_hits_quad(old_pos, new_pos,
                    left_bl, left_tl, left_tr, left_br)
                {
                    proj.state = ProjectileState::HitBlue;
                }
            } else if proj.state == ProjectileState::HitBlue {
                proj.blink_timer -= dt;
            } else if proj.state == ProjectileState::HitGreen {
                proj.green_timer -= dt;
            }
        }

        // Remove finished projectiles
        projectiles.retain(|p| {
            p.life > 0.0
                && (p.state != ProjectileState::HitGreen || p.green_timer > 0.0)
                && (p.state != ProjectileState::HitBlue || p.blink_timer > 0.0)
        });

        // =================================================================
        // 7. RENDERING
        // =================================================================
        clear_background(Color::new(0.15, 0.15, 0.2, 1.0));

        // --- 3D CAMERA ---
        let cam_pos = vec3(
            robot_x - cam_yaw.cos() * cam_pitch.cos() * cam_dist,
            ROBOT_HEIGHT + cam_pitch.sin() * cam_dist,
            robot_z - cam_yaw.sin() * cam_pitch.cos() * cam_dist,
        );
        let cam_target = vec3(robot_x, ROBOT_HEIGHT, robot_z);

        set_camera(&Camera3D {
            position: cam_pos,
            target: cam_target,
            up: vec3(0.0, 1.0, 0.0),
            ..Default::default()
        });

        // --- GROUND PLANE ---
        // Draw a large grid on the XZ plane
        let grid_size = 50;
        let grid_color = Color::new(0.3, 0.3, 0.35, 1.0);
        for i in -grid_size..=grid_size {
            let fi = i as f32;
            let gs = grid_size as f32;
            draw_line_3d(vec3(fi, 0.0, -gs), vec3(fi, 0.0, gs), grid_color);
            draw_line_3d(vec3(-gs, 0.0, fi), vec3(gs, 0.0, fi), grid_color);
        }

        // --- TARGET CUBE ---
        // Draw cube faces as colored lines (wireframe with thick edges)

        // Front face — GREEN (the target face)
        draw_line_3d(front_bottom_right, front_top_right, GREEN);
        draw_line_3d(front_top_right, front_top_left, GREEN);
        draw_line_3d(front_top_left, front_bottom_left, GREEN);
        draw_line_3d(front_bottom_left, front_bottom_right, GREEN);
        // X across to make it visually obvious
        draw_line_3d(front_bottom_right, front_top_left, GREEN);
        draw_line_3d(front_top_right, front_bottom_left, GREEN);

        // Back face — BLUE
        draw_line_3d(back_bottom_right, back_top_right, BLUE);
        draw_line_3d(back_top_right, back_top_left, BLUE);
        draw_line_3d(back_top_left, back_bottom_left, BLUE);
        draw_line_3d(back_bottom_left, back_bottom_right, BLUE);

        // Right face — BLUE
        draw_line_3d(right_bl, right_tl, BLUE);
        draw_line_3d(right_tl, right_tr, BLUE);
        draw_line_3d(right_tr, right_br, BLUE);
        draw_line_3d(right_br, right_bl, BLUE);

        // Left face — BLUE
        draw_line_3d(left_bl, left_tl, BLUE);
        draw_line_3d(left_tl, left_tr, BLUE);
        draw_line_3d(left_tr, left_br, BLUE);
        draw_line_3d(left_br, left_bl, BLUE);

        // Top edges — BLUE
        draw_line_3d(front_top_right, back_top_right, BLUE);
        draw_line_3d(front_top_left, back_top_left, BLUE);

        // Bottom edges — BLUE
        draw_line_3d(front_bottom_right, back_bottom_right, BLUE);
        draw_line_3d(front_bottom_left, back_bottom_left, BLUE);

        // Target center marker
        draw_sphere(target_pos, 0.1, None, RED);

        // --- PROJECTILES & TRAILS ---
        for proj in &projectiles {
            // Draw the parabolic arc trail
            if proj.trail.len() > 1 {
                for i in 1..proj.trail.len() {
                    // Fade from bright to dim along the trail
                    let alpha = (i as f32) / (proj.trail.len() as f32);
                    let trail_color = match proj.state {
                        ProjectileState::Active => Color::new(1.0, 1.0, 0.0, alpha * 0.8),
                        ProjectileState::HitGreen => Color::new(0.0, 1.0, 0.0, alpha * 0.8),
                        ProjectileState::HitBlue => Color::new(1.0, 0.0, 0.0, alpha * 0.8),
                    };
                    draw_line_3d(proj.trail[i - 1], proj.trail[i], trail_color);
                }
            }

            // Draw the projectile sphere
            match proj.state {
                ProjectileState::Active => {
                    draw_sphere(proj.pos, 0.15, None, YELLOW);
                }
                ProjectileState::HitBlue => {
                    // Blink red before vanishing
                    let blink_on = (proj.blink_timer * 10.0) as i32 % 2 == 0;
                    if blink_on {
                        draw_sphere(proj.pos, 0.2, None, RED);
                    }
                }
                ProjectileState::HitGreen => {
                    // Flash green before vanishing
                    let flash_on = (proj.green_timer * 12.0) as i32 % 2 == 0;
                    if flash_on {
                        draw_sphere(proj.pos, 0.25, None, GREEN);
                    }
                }
            }
        }

        // --- ROBOT BODY ---
        // Draw robot as a cube sitting on the ground
        let robot_pos_3d = vec3(robot_x, 0.25, robot_z);
        draw_cube(robot_pos_3d, vec3(1.0, 0.5, 1.0), None, BLUE);
        draw_cube_wires(robot_pos_3d, vec3(1.0, 0.5, 1.0), DARKBLUE);

        // --- ROBOT FORWARD INDICATOR (chassis heading) ---
        let fwd_end = vec3(
            robot_x + robot_yaw.cos() * 1.5,
            0.4,
            robot_z + robot_yaw.sin() * 1.5,
        );
        draw_line_3d(vec3(robot_x, 0.4, robot_z), fwd_end, LIGHTGRAY);

        // --- TURRET ---
        // Draw turret as a sphere on top of the robot + barrel line
        let turret_pos = vec3(robot_x, ROBOT_HEIGHT, robot_z);
        draw_sphere(turret_pos, 0.3, None, ORANGE);

        let barrel_end = vec3(
            robot_x + turret_yaw.cos() * 2.0,
            ROBOT_HEIGHT,
            robot_z + turret_yaw.sin() * 2.0,
        );
        draw_line_3d(turret_pos, barrel_end, RED);

        // =================================================================
        // 8. HUD (switch back to 2D screen space)
        // =================================================================
        set_default_camera();

        // Semi-transparent side panel
        draw_rectangle(0.0, 0.0, 320.0, screen_height(), Color::new(0.05, 0.05, 0.05, 0.85));

        draw_text("Autoaim 3D Simulation", 10.0, 30.0, 24.0, WHITE);
        draw_text("========================", 10.0, 50.0, 20.0, GRAY);

        let hud_color = if is_locked { GREEN } else if can_hit { YELLOW } else { RED };
        let lock_str = if is_locked { "LOCKED (READY)" } else if can_hit { "TRACKING" } else { "NO SIGHTLINE" };

        draw_text(&format!("Target: {}", lock_str), 10.0, 80.0, 20.0, hud_color);
        draw_text(&format!("Distance:  {:.2} m", current_distance), 10.0, 110.0, 20.0, WHITE);
        draw_text(&format!("Req. RPM:  {:.0}", current_rpm), 10.0, 140.0, 20.0, ORANGE);
        draw_text(&format!("Robot Yaw: {:.0} deg", robot_yaw.to_degrees()), 10.0, 170.0, 20.0, WHITE);
        draw_text(&format!("Turret Yaw:{:.0} deg", turret_yaw.to_degrees()), 10.0, 200.0, 20.0, ORANGE);

        draw_text("========================", 10.0, 230.0, 20.0, GRAY);
        draw_text("Controls:", 10.0, 260.0, 20.0, WHITE);
        draw_text("[WASD]  Drive Chassis", 10.0, 285.0, 18.0, LIGHTGRAY);
        draw_text("[AUTO]  Fires Automatically", 10.0, 310.0, 18.0, GREEN);
        draw_text("[MOUSE] Drag to orbit, scroll zoom", 10.0, 335.0, 18.0, LIGHTGRAY);

        draw_text(&format!("Projectiles: {}", projectiles.len()), 10.0, 370.0, 18.0, YELLOW);
        draw_text(&format!("FPS: {}", get_fps()), 10.0, 395.0, 18.0, GRAY);

        next_frame().await
    }
}
