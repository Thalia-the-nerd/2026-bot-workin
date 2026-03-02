import pygame
import sys
import os

# Allow background events so the window doesn't have to be focused strictly
os.environ['SDL_JOYSTICK_ALLOW_BACKGROUND_EVENTS'] = '1'

try:
    pygame.init()
    pygame.joystick.init()
except Exception as e:
    print(f"Error initializing pygame: {e}")
    sys.exit(1)

# Set up the display
width, height = 700, 500
try:
    screen = pygame.display.set_mode((width, height))
    pygame.display.set_caption("Turret/Fire Controller Input Tester")
except Exception as e:
    print(f"Error setting display mode: {e}. Are you on a headless server?")
    sys.exit(1)

clock = pygame.time.Clock()
font = pygame.font.SysFont(None, 24)
title_font = pygame.font.SysFont(None, 32)
bold_font = pygame.font.SysFont(None, 24, bold=True)

joysticks = [pygame.joystick.Joystick(x) for x in range(pygame.joystick.get_count())]
for joy in joysticks:
    joy.init()

running = True
print("Starting Turret Joystick Tester window. Press Ctrl+C in terminal or close window to exit.")

# Define custom labels for the Turret/Fire setup
# Pygame is 0-indexed. Physical button N usually maps to index N-1.
CUSTOM_LABELS = {
    "axis_0": "Turret Turn Axis (X)",
    "axis_1": "Fire Motor Speed (Y)",
    "axis_2": "Intake Speed Slider (Throttle)",
    "axis_3": "Intake Speed Slider (Throttle)",
    "btn_0": "1: Fire Trigger",
    "btn_1": "2: AutoAim Toggle",
    "btn_5": "6: Preset -180",
    "btn_6": "7: Preset -90",
    "btn_7": "8: Preset 0",
    "btn_8": "9: Preset 90",
    "btn_9": "10: Preset 180",
    "btn_10": "11: Preset 270",
    "btn_11": "12: Intake Unjam"
}

while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        # Detect hotplugging
        if event.type == pygame.JOYDEVICEADDED or event.type == pygame.JOYDEVICEREMOVED:
            joysticks = [pygame.joystick.Joystick(x) for x in range(pygame.joystick.get_count())]
            for joy in joysticks:
                joy.init()
            
    screen.fill((30, 30, 40))
    
    y_offset = 20
    text = title_font.render(f"Controllers connected: {len(joysticks)}", True, (255, 255, 255))
    screen.blit(text, (20, y_offset))
    y_offset += 40
    
    for i, joy in enumerate(joysticks):
        name = joy.get_name()
        text = font.render(f"Controller {i} - {name}", True, (150, 200, 255))
        screen.blit(text, (20, y_offset))
        y_offset += 30
        
        # Draw Axes
        axes_text = "Axes:  "
        for a in range(joy.get_numaxes()):
            val = joy.get_axis(a)
            label = CUSTOM_LABELS.get(f"axis_{a}", f"{a}")
            axes_text += f"{label}: {val:>5.2f}    "
            
            # wrap text for readability
            if len(axes_text) > 60:
                text = font.render(axes_text, True, (255, 180, 180))
                screen.blit(text, (40, y_offset))
                y_offset += 25
                axes_text = "       "

        if axes_text.strip():
            text = font.render(axes_text, True, (255, 180, 180))
            screen.blit(text, (40, y_offset))
            y_offset += 30
        
        # Draw Hats (D-pad)
        hats_text = "Hats:  "
        for h in range(joy.get_numhats()):
            val = joy.get_hat(h)
            hats_text += f"{h}: {val}    "
        if joy.get_numhats() > 0:
            text = font.render(hats_text, True, (200, 200, 150))
            screen.blit(text, (40, y_offset))
            y_offset += 30
        
        # Draw Buttons
        y_offset += 10
        text = font.render("Buttons:", True, (180, 255, 180))
        screen.blit(text, (40, y_offset))
        y_offset += 25

        btns_text = "   "
        for b in range(joy.get_numbuttons()):
            val = joy.get_button(b)
            # Fetch custom zero-based label, default to Pygame + 1 so "btn_0" reads "Button 1"
            label = CUSTOM_LABELS.get(f"btn_{b}", f"Button {b+1}")
            
            if val:
                btns_text += f"[{label}] "
            else:
                btns_text += f" {label}  "
            
            # wrap text 
            if len(btns_text) > 70:
                text = font.render(btns_text, True, (180, 255, 180))
                screen.blit(text, (40, y_offset))
                y_offset += 25
                btns_text = "   "

        if btns_text.strip():
            text = font.render(btns_text, True, (180, 255, 180))
            screen.blit(text, (40, y_offset))
            y_offset += 40

    if len(joysticks) == 0:
        text = font.render("Please plug in your controller!", True, (255, 100, 100))
        screen.blit(text, (20, y_offset))
        
    pygame.display.flip()
    clock.tick(30)

pygame.quit()
sys.exit()
