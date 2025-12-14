# Getting Help & Support

Need assistance with the 2026-bot project? This guide explains how to report issues, ask questions, and get support.

## Opening Issues on GitHub

GitHub Issues is our primary method for tracking bugs, feature requests, and general questions. Follow the guidelines below to ensure your issue is handled efficiently.

### 🐛 Reporting Code Errors

If you've encountered a bug, crash, compilation error, or unexpected behavior in the code:

1. **Navigate to the Issues tab:**  
   Go to [https://github.com/MiamiBeachBots/2026-bot/issues](https://github.com/MiamiBeachBots/2026-bot/issues)

2. **Check for existing issues:**  
   Before creating a new issue, search to see if someone has already reported the same problem.

3. **Click "New Issue"** and provide the following information:

   - **Title:** A clear, concise summary of the problem  
     Example: `[Bug] Swerve module fails to initialize on boot`

   - **Description:** Include detailed information:
     - Steps to reproduce the error
     - Expected behavior vs. actual behavior
     - Full error messages or stack traces
     - Relevant code snippets (if applicable)
     - Environment details (Java version, WPILib version, OS, etc.)

   - **Labels:** Add appropriate labels such as `bug`, `help wanted`, or `urgent` if available.

4. **Submit the issue** and monitor for responses from maintainers.

**Example Issue for Code Errors:**

```
Title: [Bug] Robot code crashes on startup with NullPointerException

Description:
When deploying the robot code to the RoboRIO, the code crashes immediately 
after initialization with a NullPointerException in SwerveSubsystem.java.

Steps to Reproduce:
1. Deploy code using `./gradlew deploy`
2. Enable the robot via Driver Station
3. Observe crash in Driver Station console

Expected Behavior:
Robot should initialize all subsystems successfully.

Actual Behavior:
Code crashes with:
```
java.lang.NullPointerException: Cannot invoke SwerveModule.getPosition() because "this.module" is null
  at frc.robot.subsystems.SwerveSubsystem.periodic(SwerveSubsystem.java:45)
```

Environment:
- WPILib Version: 2025.1.1
- Java Version: 17
- OS: Windows 11
```
```

### 💬 Reporting Other Issues

For non-code-related matters such as documentation improvements, feature requests, organizational questions, or general discussions:

1. **Navigate to the Issues tab:**  
   Go to [https://github.com/MiamiBeachBots/2026-bot/issues](https://github.com/MiamiBeachBots/2026-bot/issues)

2. **Click "New Issue"** and provide:

   - **Title:** A descriptive summary  
     Examples:
     - `[Docs] Add CAN bus wiring diagram to README`
     - `[Feature Request] Implement auto-balancing routine`
     - `[Question] How to configure module offsets?`

   - **Description:** Explain your suggestion, question, or concern clearly.

   - **Labels:** Use labels like `documentation`, `enhancement`, `question`, or `discussion`.

3. **Submit the issue** and engage with any follow-up discussion.

**Example Issue for Other Matters:**

```
Title: [Docs] Add instructions for calibrating swerve module offsets

Description:
The current documentation doesn't explain how to calibrate the swerve module 
absolute encoder offsets. This would be helpful for new team members working 
on the drive system.

Suggested Content:
- Procedure for manually aligning wheels
- How to read encoder values
- Where to update offset values in configuration files

This would fit well in the README under the "Hardware Integration" section.
```

## Direct Contact

For urgent matters, private concerns, or questions that don't fit GitHub Issues, you can reach out directly to:

**Thalia**  
📧 Email: [thaliathenerd@proton.me](mailto:thaliathenerd@proton.me)

Please use direct contact sparingly and prefer GitHub Issues for transparency and team visibility.

---

## Additional Resources

- **[Contributing Guide](Contribguide.md)** - Guidelines for contributing code
- **[Style Guide](styleguide.md)** - Code formatting and naming conventions
- **[Commit Guide](commitguide.md)** - Git commit message standards

---

*Built by Miami Beach Bots - FRC Team 2026*
