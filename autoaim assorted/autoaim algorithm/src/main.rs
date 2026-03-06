use autoaim::calculate_aim_angle;
use macroquad::prelude::*;
use std::f32::consts::PI;

const ROBOT_SPEED: f32 = 200.0; // pixels per second
const ROBOT_ROT_SPEED: f32 = 2.5; // rad per second
const TURRET_ROT_SPEED: f32 = 5.0; // rad per second

#[derive(PartialEq)]
enum ProjectileState {
    Active,
    HitGreen,
    HitBlue,
}

struct Projectile {
    x: f32,
    y: f32,
    vx: f32,
    vy: f32,
    life: f32,
    state: ProjectileState,
    blink_timer: f32,
}

// Math helper for line-segment to line-segment distance / collision
fn line_circle_collision(x1: f32, y1: f32, x2: f32, y2: f32, cx: f32, cy: f32, r: f32) -> bool {
    let dx = x2 - x1;
    let dy = y2 - y1;
    let l2 = dx * dx + dy * dy;
    if l2 == 0.0 {
        return (cx - x1).powi(2) + (cy - y1).powi(2) <= r * r;
    }
    let t = ((cx - x1) * dx + (cy - y1) * dy) / l2;
    let t = t.clamp(0.0, 1.0);
    let px = x1 + t * dx;
    let py = y1 + t * dy;
    (cx - px).powi(2) + (cy - py).powi(2) <= r * r
}

#[macroquad::main("Autoaim Simulation")]
async fn main() {
    let mut robot_x = screen_width() / 2.0;
    let mut robot_y = screen_height() / 2.0 + 200.0;
    let mut robot_rot: f32 = -PI / 2.0; // facing up

    let mut turret_rot: f32 = 0.0; // relative to world

    let target_x = screen_width() / 2.0;
    let target_y = screen_height() / 2.0;
    let target_size = 100.0;
    let target_rot: f32 = 0.0; // facing right (+X) is the designated front face

    let mut projectiles: Vec<Projectile> = vec![];

    // Physics output states
    let mut current_distance: f64 = 0.0;
    let mut current_rpm: f64 = 0.0;

    loop {
        let dt = get_frame_time();

        // Calculate velocity (kinematics mapping)
        let mut robot_vx = 0.0;
        let mut robot_vy = 0.0;
        
        // Driving Inputs
        if is_key_down(KeyCode::Up) || is_key_down(KeyCode::W) {
            robot_vx = robot_rot.cos() * ROBOT_SPEED;
            robot_vy = robot_rot.sin() * ROBOT_SPEED;
        }
        if is_key_down(KeyCode::Down) || is_key_down(KeyCode::S) {
            robot_vx = -robot_rot.cos() * ROBOT_SPEED;
            robot_vy = -robot_rot.sin() * ROBOT_SPEED;
        }
        
        robot_x += robot_vx * dt;
        robot_y += robot_vy * dt;
        
        if is_key_down(KeyCode::Left) || is_key_down(KeyCode::A) {
            robot_rot -= ROBOT_ROT_SPEED * dt;
        }
        if is_key_down(KeyCode::Right) || is_key_down(KeyCode::D) {
            robot_rot += ROBOT_ROT_SPEED * dt;
        }

        // Calculate autoaim using kinematic algorithm
        let mut target_angle_out: f64 = 0.0;
        let mut target_distance_out: f64 = 0.0;
        let mut target_rpm_out: f64 = 0.0;
        
        let can_hit = calculate_aim_angle(
            robot_x as f64,
            robot_y as f64,
            robot_vx as f64,
            robot_vy as f64,
            target_x as f64,
            target_y as f64,
            target_size as f64,
            target_rot as f64,
            &mut target_angle_out,
            &mut target_distance_out,
            &mut target_rpm_out,
        );

        // Move turret towards target angle respects TURRET_ROT_SPEED
        if can_hit {
            let target_angle = target_angle_out as f32;
            current_distance = target_distance_out;
            current_rpm = target_rpm_out;
            
            // Shortest path to target angle
            let mut diff = target_angle - turret_rot;
            diff = (diff + PI) % (2.0 * PI) - PI;
            if diff < -PI {
                diff += 2.0 * PI;
            }

            let max_step = TURRET_ROT_SPEED * dt;
            if diff.abs() <= max_step {
                turret_rot = target_angle;
            } else {
                turret_rot += diff.signum() * max_step;
            }
        } else {
            current_distance = 0.0;
            current_rpm = 0.0;

            // Revert turret to robot's forward facing if no target
            let mut diff = robot_rot - turret_rot;
            diff = (diff + PI) % (2.0 * PI) - PI;
            if diff < -PI {
                diff += 2.0 * PI;
            }
            let max_step = TURRET_ROT_SPEED * dt;
            if diff.abs() <= max_step {
                turret_rot = robot_rot;
            } else {
                turret_rot += diff.signum() * max_step;
            }
        }

        // Ensure turret_rot stays within -PI to PI
        turret_rot = (turret_rot + PI) % (2.0 * PI) - PI;
        if turret_rot < -PI {
            turret_rot += 2.0 * PI;
        }

        // Fire Projectiles
        if is_key_pressed(KeyCode::Space) {
            let mut firing_velocity_xy = 0.0;
            if current_distance > 0.0 {
                // --- 2D KINEMATICS EXTRACTION ---
                // The C-ABI calculate_aim_angle outputs a 3D RPM that includes Z-gravity.
                // However, our 2D top-down simulation shouldn't use Z-gravity to draw XY movement!
                // So here we locally replicate *just* the 2D XY turret exit velocity component:
                let t = (current_distance as f32) / 600.0;
                let aim_dx = target_x - robot_x;
                let aim_dy = target_y - robot_y;
                let req_vx_world = aim_dx / t;
                let req_vy_world = aim_dy / t;

                // The relative exit velocity of the turret (subtracting robot momentum)
                let fire_vx = req_vx_world - robot_vx;
                let fire_vy = req_vy_world - robot_vy;
                firing_velocity_xy = (fire_vx * fire_vx + fire_vy * fire_vy).sqrt();
            }

            // --- WORLD SPACE PROJECTILE SPAWN ---
            // A common bug is mapping the projectile's `vx` directly to `robot_vx` continually.
            // This causes the bullet to "curve" mid-air like a magnet whenever the robot turns!
            // Instead, we lock in the total world space velocity ONCE at the exact moment of firing.
            let fire_world_vx = robot_vx + turret_rot.cos() * firing_velocity_xy;
            let fire_world_vy = robot_vy + turret_rot.sin() * firing_velocity_xy;

            projectiles.push(Projectile{
                x: robot_x + turret_rot.cos() * 50.0, // spawn at turret tip
                y: robot_y + turret_rot.sin() * 50.0,
                vx: fire_world_vx,
                vy: fire_world_vy,
                life: 3.0, // dies after 3 seconds
                state: ProjectileState::Active,
                blink_timer: 1.0, // 1 second red blink on wrong hit
            });
        }

        // --- DRAW TARGET ---
        let tr_cos = target_rot.cos();
        let tr_sin = target_rot.sin();
        let half = target_size / 2.0;

        let c1 = vec2(half, half);
        let c2 = vec2(-half, half);
        let c3 = vec2(-half, -half);
        let c4 = vec2(half, -half);

        let rot_p = |p: Vec2| -> Vec2 {
            vec2(
                target_x + p.x * tr_cos - p.y * tr_sin,
                target_y + p.x * tr_sin + p.y * tr_cos,
            )
        };

        let p1 = rot_p(c1);
        let p2 = rot_p(c2);
        let p3 = rot_p(c3);
        let p4 = rot_p(c4);

        // Update Physics & Collisions
        for proj in &mut projectiles {
            if proj.state == ProjectileState::Active {
                proj.x += proj.vx * dt;
                proj.y += proj.vy * dt;
                proj.life -= dt;

                // Check collisions against sides
                let r = 6.0; // proj radius
                if line_circle_collision(p4.x, p4.y, p1.x, p1.y, proj.x, proj.y, r) {
                    // Hit Green Face!
                    proj.state = ProjectileState::HitGreen;
                } else if line_circle_collision(p1.x, p1.y, p2.x, p2.y, proj.x, proj.y, r) ||
                          line_circle_collision(p2.x, p2.y, p3.x, p3.y, proj.x, proj.y, r) ||
                          line_circle_collision(p3.x, p3.y, p4.x, p4.y, proj.x, proj.y, r) {
                    // Hit Blue Face!
                    proj.state = ProjectileState::HitBlue;
                }
            } else if proj.state == ProjectileState::HitBlue {
                proj.blink_timer -= dt;
            }
        }

        // Remove dead projectiles (or ones that hit green face, or blue face after blinking)
        projectiles.retain(|p| {
            p.life > 0.0 && 
            p.state != ProjectileState::HitGreen &&
            (p.state != ProjectileState::HitBlue || p.blink_timer > 0.0)
        });

        clear_background(DARKGRAY);

        // --- DRAW UI SIDE PANEL ---
        draw_rectangle(0.0, 0.0, 300.0, screen_height(), Color::new(0.1, 0.1, 0.1, 0.9));
        draw_text("Autoaim Subsystem Status", 10.0, 30.0, 24.0, WHITE);
        draw_text("------------------------", 10.0, 50.0, 24.0, GRAY);
        
        let hud_color = if can_hit { GREEN } else { RED };
        let lock_str = if can_hit { "LOCKED" } else { "NO SIGHTLINE" };
        
        draw_text(&format!("Target Status: {}", lock_str), 10.0, 80.0, 20.0, hud_color);
        draw_text(&format!("Dist to Target: {:.2}m", current_distance), 10.0, 110.0, 20.0, WHITE);
        draw_text(&format!("Calculated RPM: {:.0}", current_rpm), 10.0, 140.0, 20.0, ORANGE);
        draw_text(&format!("Robot Ang (deg): {:.0}", robot_rot.to_degrees()), 10.0, 170.0, 20.0, WHITE);
        draw_text(&format!("Turret Ang (deg): {:.0}", turret_rot.to_degrees()), 10.0, 200.0, 20.0, ORANGE);
        
        draw_text("------------------------", 10.0, 230.0, 24.0, GRAY);
        draw_text("Controls:", 10.0, 260.0, 20.0, WHITE);
        draw_text("[WASD] - Drive Chassis", 10.0, 290.0, 20.0, LIGHTGRAY);
        draw_text("[SPACE] - Fire Projectile", 10.0, 320.0, 20.0, LIGHTGRAY);

        draw_line(p1.x, p1.y, p2.x, p2.y, 3.0, BLUE); // Back
        draw_line(p2.x, p2.y, p3.x, p3.y, 3.0, BLUE); // Side
        draw_line(p3.x, p3.y, p4.x, p4.y, 3.0, BLUE); // Side
        draw_line(p4.x, p4.y, p1.x, p1.y, 6.0, GREEN); // Designated Front Target Face

        draw_circle(target_x, target_y, 4.0, RED);

        // --- DRAW PROJECTILES ---
        for proj in &projectiles {
            if proj.state == ProjectileState::Active {
                draw_circle(proj.x, proj.y, 6.0, YELLOW);
            } else if proj.state == ProjectileState::HitBlue {
                // Blink Red
                let blink_on = (proj.blink_timer * 10.0) as i32 % 2 == 0;
                if blink_on {
                    draw_circle(proj.x, proj.y, 8.0, RED);
                }
            }
        }

        // --- DRAW ROBOT ---
        draw_circle(robot_x, robot_y, 25.0, BLUE);
        // Forward Indicator
        draw_line(
            robot_x, robot_y,
            robot_x + robot_rot.cos() * 35.0,
            robot_y + robot_rot.sin() * 35.0,
            4.0, LIGHTGRAY,
        );

        // --- DRAW TURRET ---
        draw_circle(robot_x, robot_y, 15.0, ORANGE);
        draw_line(
            robot_x, robot_y,
            robot_x + turret_rot.cos() * 50.0,
            robot_y + turret_rot.sin() * 50.0,
            6.0, RED,
        );

        next_frame().await
    }
}
