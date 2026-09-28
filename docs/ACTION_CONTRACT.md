# Lesson Action Contract

The curriculum remains in `Java-Practice-Project`. This plugin executes the same action objects in real IntelliJ IDEA.

## Rules

1. Action names are exact.
2. Unsupported actions fail visibly and do not advance progress.
3. Next captures reversible state before a successful state-changing action.
4. Previous restores recorded state instead of issuing blind Ctrl+Z.
5. Replay repeats the current action without adding another history entry.
6. Lesson paths must remain inside the opened project.

## M1 supported action

`openFile` resolves a lesson-relative path against the current IntelliJ project, rejects path traversal, opens the real `VirtualFile`, and records the previous active file/caret for Previous.

Planned later: `highlightTarget`, `typeCode`, semantic PSI targets, file creation/deletion, terminal/run actions, and floating lesson UI.
