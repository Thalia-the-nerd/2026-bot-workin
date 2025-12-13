# Commit Message Guide

> "Commit early, commit often, commit with purpose."

## Why Good Commit Messages Matter

Your commit history tells the story of your code. When something breaks at competition, clear commits help you find and fix problems fast. Future you (and your teammates) will thank present you.

## The Format

We use a simplified Conventional Commits format:

```
[Type] Short description
```

That's it. Simple. Clean. Professional.

## Commit Types

Choose the type that best describes your change:

| Type | When to Use | Example |
|------|-------------|---------|
| `[Feat]` | New feature or capability | `[Feat] Add field-oriented drive` |
| `[Fix]` | Bug fix or correction | `[Fix] Correct module 3 encoder offset` |
| `[Refactor]` | Code cleanup without changing behavior | `[Refactor] Simplify PID controller logic` |
| `[Docs]` | Documentation changes only | `[Docs] Update wiring diagram` |
| `[Chore]` | Build, deps, or tool updates | `[Chore] Update Phoenix6 to v25.0.0` |
| `[Test]` | Adding or fixing tests | `[Test] Add unit tests for shooter` |

## Writing Good Descriptions

### Rule 1: Use Imperative Mood

Write like you're giving Git a command. Pretend your commit message completes this sentence:

*"If applied, this commit will..."*

✅ **Good:**
- `[Feat] Add vision processing`
- `[Fix] Invert left drive motors`
- `[Refactor] Extract constants to Constants.java`

❌ **Bad:**
- `[Feat] Added vision processing` (past tense)
- `[Feat] Adds vision processing` (present tense)
- `[Fix] fixing motors` (gerund, lowercase)

### Rule 2: Be Specific But Concise

- **Keep it under 50 characters** when possible
- Say *what* changed, not *how* you changed it
- Name the component if it's not obvious

✅ **Good:**
- `[Fix] Correct inverted shooter motor direction`
- `[Feat] Implement auto-balance command`

❌ **Bad:**
- `[Fix] Fix bug` (too vague)
- `[Feat] Add code that makes the robot auto-balance by using gyro pitch data to drive backwards or forwards` (way too long)

### Rule 3: Add Context When Needed

For complex changes, add a blank line after the subject and write a body paragraph:

```
[Feat] Implement vision-based targeting

Added AprilTag detection using PhotonVision. The turret now
auto-aims at detected tags within 4 meters. Falls back to
manual control if no tags are visible.
```

## Real Examples

Here's what good commit messages look like in practice:

```
[Feat] Add swerve drive kinematics
[Feat] Implement intake subsystem
[Feat] Create 3-piece auto routine
[Fix] Correct module angle offsets
[Fix] Resolve brownout during arm extension
[Refactor] Move magic numbers to Constants.java
[Refactor] Consolidate PID tuning methods
[Docs] Add CAN bus wiring diagram
[Docs] Update build instructions for 2025 season
[Chore] Update YAGSL to latest version
[Chore] Add REVLib vendor dependency
[Test] Add trajectory following unit tests
```

## Atomic Commits: One Change, One Commit

**Don't mix unrelated changes.** Each commit should represent one logical change.

❌ **Bad:**
```
[Feat] Add shooter subsystem and fix drive motor inversions
```
This is two unrelated changes. Split them up.

✅ **Good:**
```
[Fix] Invert left-side drive motors
[Feat] Add shooter subsystem
```

**Why?** If the shooter code has bugs, you can revert it without losing the motor fix.

## Quick Reference

```
[Type] Short imperative description (< 50 chars)

Optional body paragraph explaining the "why" and providing
context for complex changes. Wrap at 72 characters.
```

## Pro Tips

1. **Commit often.** Small commits are easier to review and debug.
2. **Test before you commit.** Make sure your code compiles.
3. **One feature, multiple commits is fine.** Break big features into logical steps.
4. **Use `git log --oneline`** to see if your commits tell a clear story.

---

*"A well-crafted commit message is a love letter to your future self."* 
