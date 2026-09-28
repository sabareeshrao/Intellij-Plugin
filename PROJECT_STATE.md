# IntelliJ Plugin Project State

## Purpose

This repository is the native IntelliJ IDEA execution engine for the AeroTopo curriculum.

- `Intellij-Plugin`: real IntelliJ runtime.
- `Java-Practice-Project`: AeroTopo source plus Books → Chapters → Lessons → Steps.
- `Experiment-VS-Code`: browser simulator.

## Current milestone

**M1 — Native lesson foundation**

The foundation is being implemented in small recoverable commits. The target behavior is:

1. load the opened project's `simulation/course.json`,
2. display the current lesson in a native IntelliJ Tool Window,
3. persist lesson/step progress,
4. support Previous / Replay / Next,
5. execute the first real IDE action, `openFile`,
6. restore the previous active editor/caret when navigating back.

Unsupported lesson actions must be reported visibly and must not silently advance progress.

## Compatibility anchor

See `LESSON_ENGINE_REF`. Any future curriculum-schema change must update that ref and the plugin validator/tests.
