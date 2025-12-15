# Contributing to 2026-bot

Thank you for your interest in contributing to the 2026-bot project! This guide will help you get started with development.

## Getting Started

### Fork and Clone the Repository

**Important:** Do not commit directly to the main repository. Instead, fork the repository and work on your fork.

1. **Fork the repository** by clicking the "Fork" button at the top of the [2026-bot repository](https://github.com/MiamiBeachBots/2026-bot) page on GitHub.

2. **Clone your fork** (replace `YOUR_USERNAME` with your GitHub username):
   ```bash
   git clone https://github.com/YOUR_USERNAME/2026-bot.git
   cd 2026-bot
   ```

3. **Add the upstream repository** to keep your fork in sync:
   ```bash
   git remote add upstream https://github.com/MiamiBeachBots/2026-bot.git
   ```

4. **Verify your remotes:**
   ```bash
   git remote -v
   ```
   You should see:
   - `origin` pointing to your fork
   - `upstream` pointing to the main repository

### Install Dependencies

This project uses YAGSL for swerve drive control. Run the following command to download all required libraries (Phoenix 6, REVLib, YAGSL):

```bash
./gradlew build
```

## Development Environment

### VS Code Setup (Windows/Mac/Linux) (Ewwwwwwwww)

1. Install the WPILib 2025 suite.
2. Open the project folder in VS Code.
3. Accept the prompt to "Import Gradle Project".

### Command Line Setup

1. Ensure you have JDK 17+ installed.
2. Use `./gradlew build` to compile the project.
3. Use `./gradlew deploy` to deploy code to the robot.

## Contributing Workflow

### Syncing Your Fork

Before starting new work, always sync your fork with the upstream repository:

```bash
git checkout main
git fetch upstream
git merge upstream/main
git push origin main
```

### Creating a Branch

Always create a new branch for your feature or bug fix. Use descriptive branch names:

```bash
git checkout -b feat/shooter-control
git checkout -b fix/inverted-motor
```

### Making Changes

1. **Follow the Style Guide:** Review `styleguide.md` before making changes.
2. **Write Clean Code:** Use proper naming conventions and add Javadocs for public methods.
3. **Test Your Changes:** Run `./gradlew test` if unit tests exist, or test in simulation.
4. **Remove Dead Code:** Do not commit commented-out code blocks.

### Committing Changes

Follow the commit message format outlined in `commitguide.md`:

```bash
git commit -m "[Feat] Add PID control to shooter"
git commit -m "[Fix] Correct inverted motor direction"
```

### Opening a Pull Request

1. Push your branch to **your fork** (not the main repository):
   ```bash
   git push origin feat/shooter-control
   ```
2. Go to the [main 2026-bot repository](https://github.com/MiamiBeachBots/2026-bot) on GitHub.
3. You should see a prompt to "Compare & pull request" for your recently pushed branch.
4. Click the button and create a Pull Request from your fork to the main repository.
5. Provide a clear description of your changes.
6. Wait for code review and address any feedback.

**Note:** Never push directly to the main repository. All contributions must go through Pull Requests from your fork.

## Code Standards

* **Formatting:** Standard Java formatting with 2-space indentation.
* **Documentation:** Add Javadocs for all public subsystems and complex methods.
* **Constants:** Keep physical constants in `Constants.java`. Avoid magic numbers.
* **Imports:** Remove unused imports and avoid wildcard imports.

For detailed guidelines, refer to `styleguide.md` and `commitguide.md`.

## Need Help?

If you have questions or need assistance, please refer to the [Assist Guide](Assist.md).
