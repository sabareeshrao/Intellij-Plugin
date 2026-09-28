# IntelliJ Plugin Project State

## Purpose

This repository is the native IntelliJ IDEA execution engine for the AeroTopo curriculum.

- `sabareeshrao/Intellij-Plugin`: real IntelliJ runtime.
- `sabareeshrao/Java-Practice-Project`: AeroTopo source plus Books → Chapters → Lessons → Steps.
- `sabareeshrao/Experiment-VS-Code`: browser simulator.

## Current milestone

**M1 — Native lesson foundation: IMPLEMENTED AND BUILD-VALIDATED**

Validated core commit: `f8532e046e698e255f8c62f579d12a1fe6991086`.

Implemented:

1. IntelliJ Platform plugin project using Java 21 and IntelliJ Platform Gradle Plugin 2.19.0.
2. Gradle 9 wrapper checked into the repository and validated by CI.
3. Native Tool Window: **AeroTopo Lessons**.
4. Loader for the opened project's `simulation/course.json` and every referenced lesson JSON.
5. Source Book / Chapter / Lesson / Step metadata remains in `Java-Practice-Project`; it is not copied here.
6. Project-workspace persistence for current lesson and step.
7. Persistent navigation history containing the previous lesson position, active editor file, and caret offset.
8. **Previous / Replay / Next** controls.
9. Lesson selector for jumping directly to a lesson.
10. Explicit action dispatcher. Unsupported actions display an error and do **not** silently advance.
11. First real executor: `openFile`.
12. `openFile` resolves only paths inside the opened IntelliJ project and opens the actual `VirtualFile` through `FileEditorManager`.
13. **Previous** restores the editor file/caret snapshot recorded before the last successful action.
14. JUnit coverage verifies the real course/lesson JSON loading contract.
15. CI now builds an installable plugin ZIP and uploads it as the **AeroTopo-Lesson-Runner** workflow artifact.

## First real-IDE proof

Open a local checkout of `Java-Practice-Project` in IntelliJ IDEA with this plugin installed.

1. Open **View → Tool Windows → AeroTopo Lessons**.
2. The plugin loads Lesson 1 Step 1 directly from `simulation/course.json`.
3. Step 1 is `openProject`; M1 intentionally does not auto-run it.
4. Press **Next**.
5. The plugin advances to Lesson 1 Step 2 and executes its existing `openFile` action.
6. The real project `pom.xml` opens in the real IntelliJ editor.
7. Press **Previous**.
8. Lesson position returns to Step 1 and the recorded prior editor/caret is restored when available.
9. Press **Next** again to prove deterministic forward navigation.
10. Restart IntelliJ; lesson position/history are stored in the project workspace.

## Deliberately unsupported in M1

These are planned milestones, not silent omissions:

- `openProject`
- `highlightTarget`
- yellow editor range highlighting
- progressive `typeCode` auto-typing
- semantic Java PSI targeting
- `createFile` / `deleteResource`
- terminal/Maven/JUnit execution
- floating/draggable explanation window
- 5-second UI guidance
- reversible file-content snapshots
- large random-jump checkpoints

## Next milestone

**M2 — Real editor automation**

Order:

1. semantic file/class/method target resolver,
2. real yellow line/range highlighting,
3. caret + scroll-to-target behavior,
4. progressive real editor auto-typing,
5. reversible file-content snapshots,
6. then terminal/run actions.

## Compatibility anchor

See `LESSON_ENGINE_REF`. The plugin must never silently guess a new curriculum schema.


## Automatic update channel

A custom IntelliJ plugin repository is now published from GitHub Actions.

Repository URL:

`https://raw.githubusercontent.com/sabareeshrao/Intellij-Plugin/plugin-repository/updatePlugins.xml`

Each successful `main` push gets version `0.1.<workflow run number>`, builds a new ZIP, regenerates the repository XML, and deploys it to the dedicated `plugin-repository` branch. The plugin ID remains stable so an existing installation receives later versions as updates.

The first update-channel build also adds persistent lesson text scaling (80%–180%), A− / 100% / A+ controls, top-stacked lesson paragraphs, viewport-width word wrapping, and a permanently disabled horizontal scrollbar.


### Update-channel publication note

The first Pages-based attempt built and tested plugin version 0.1.8 successfully but could not deploy because Pages was not enabled on the new repository. The final architecture removes that dependency: GitHub Actions now publishes `updatePlugins.xml` and the versioned plugin ZIP directly to the dedicated `plugin-repository` branch, served over HTTPS by `raw.githubusercontent.com`.
