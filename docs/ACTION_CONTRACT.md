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


## M2 real IDE actions

### createFile

Creates a new real project file, opens it in IntelliJ, and progressively types the supplied `data.content` into the editor. Existing non-matching files are never overwritten.

### typeCode

Progressively inserts `data.text` or `data.content` at the caret in the selected real editor. An optional `data.file` opens the target file first.

### highlightTarget

For line targets, opens the real file, validates `expected_text`, falls back to finding that text when the stored line moved, scrolls to it, moves the caret, and applies a real yellow line highlight.

### openIntegratedTerminal

Creates/focuses the dedicated **AeroTopo Lesson** tab in IntelliJ's Reworked Terminal.

### typeTerminal

Uses the Reworked Terminal API to type `data.command` character-by-character into the real shell and executes it automatically after the final character.

### deleteResource

Deletes a lesson-created real project resource after closing it in the editor.

### Reversibility

Before `createFile`, `typeCode`, or `deleteResource`, the runtime records the prior file existence/content. Previous restores that filesystem snapshot as well as the prior editor/caret.
