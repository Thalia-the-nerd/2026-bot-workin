# Commit Guide

## Message Structure

We use a simplified Conventional Commits format. Every commit message should look like this:

`[Type] Description`

### Allowed Types

* `[Feat]`: A new feature (e.g., adding a new subsystem or auto routine).
* `[Fix]`: A bug fix (e.g., fixing inverted motors, correcting PID values).
* `[Refactor]`: Code change that neither fixes a bug nor adds a feature (cleanup).
* `[Docs]`: Documentation only changes (README, comments).
* `[Chore]`: Build process, dependency updates, or tool configs (Gradle, YAGSL updates).

## Writing the Description

* **Imperative Mood:** Write as if you are giving a command.
    * Good: `[Feat] Add vision processing`
    * Bad: `[Feat] Added vision processing` or `[Feat] Adds vision processing`
* **Capitalization:** Start the description with a capital letter.
* **Length:** Keep the subject line under 50 characters if possible.
* **Context:** If the change is complex, add a blank line after the subject and write a detailed body paragraph.

## Examples

* `[Feat] Implement field-oriented drive`
* `[Fix] Correct module 3 offset`
* `[Chore] Update Phoenix6 vendor dependency`
* `[Docs] Add wiring diagram to README`

## Atomic Commits

* Do not bundle multiple unrelated changes into one commit.
* If you fixed a bug in the Arm and added a new Auto path, those are **two separate commits**.
"History is a set of lies agreed upon just like this commit history" — Napoleon Bonaparte 
