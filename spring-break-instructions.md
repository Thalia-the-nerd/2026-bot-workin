# heyyy skylar! spring break handover stuff

hi! so, i know it's spring break and we're supposed to be relaxing. i stayed up for way too many days rewriting the entire IO layer of the robot. i'm so sorry if things are messy. i really tried to make it nice for you. anyway, i'm totally fine, just drinking my 10th monster energy and trying not to cry over java syntax. i'm happy! everything is great.

also please say sorry to niam for me. i definitely vibe coded some of this while dissociating but it should work. probably.

here's what you need to do while i'm gone.

---

### 1. testing the physical robot

i wrote some stuff so the robot doesn't explode when you turn it on.

*   **Pit Health Check:** i made a python utility for this! just go to the terminal and run `python3 utilities/pit_health_check.py`. it'll show you if the motors (drive, intake, turret, shooter) are actually alive. if any of them say FAULT, the wires are probably melted again. just check the board if that happens. you might need to run `pip install pynetworktables` first if it complains.
*   **Kinematic Smoothing:** i added a `SlewRateLimiter` so the robot drives a bit smoother now. test the joysticks out—it shouldn't snap the gearboxes when you change directions fast.

---

### 2. running Thalia Tweaks (the rust app)

i overhauled the tuning dashboard. i hope you like it. i spent way too long making it look okay instead of sleeping. i'm very happy about it.

**how to get it running:**
*   **Mac / Linux:** open the terminal and run `./setup.sh`. it should install everything.
*   **Windows:** right-click `setup.bat` and run as administrator.
*   once that's done, just type this into the terminal:
    ```bash
    cd thalia-tweaks
    cargo run
    ```

---

### 3. setting up the loader/intake motors

okay, when you physically attach the loader and intake NEOs, you need to verify the CAN IDs match the table in the README. i think they are IDs 5, 6, 7, 8, and 9 but please double check the docs. if the IDs are wrong the code will try to talk to ghosts and i will lose it. You can set the CAN IDs in the REV Hardware Client.

---

### 4. auto-aim regression calibration

i threw away the terrible polynomial math we were using for the turret auto-aim. we're using WPILib's `InterpolatingDoubleTreeMap` now. but you need to manually calibrate the physical firing angles.

1.  put the robot exactly 5 feet away from the subwoofer.
2.  cicle through the angles from 125, 100, 75, 50, 25, 0, -25, -50, -75, -100, -125 recording the speed needed to hit the target. right speed can be changed in the tuning app. if im feeling good i will add a thing on controller for this.
3.  do the exact same thing for 10 feet, 15 feet, 20 feet, and 25 feet.
4.  put those 5 pairs of numbers into my python script:
    ```bash
    python3 utilities/autoaim_regression_cli.py
    ```
5.  the script will do all the horrific math for you and rewrite `AutoAimRegression.java` automatically.

anyway, that's everything. have an amazing spring break skylar. Im real sleepy.
