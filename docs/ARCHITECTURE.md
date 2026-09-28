# Architecture

```text
Java-Practice-Project
course.json + lesson JSON + AeroTopo files
                 |
                 v
          LessonCourseLoader
                 |
                 v
        LessonRuntimeService
        /        |         \
       v         v          v
Progress      Dispatcher   Tool Window
history          |        Previous/Replay/Next
                 v
          Action Executors
                 |
                 v
           Real IntelliJ
```

The opened Java project is the curriculum and source-code authority. The plugin repository does not duplicate lesson JSON.

M1 persists the current lesson/step plus editor navigation history in IntelliJ project workspace state. Later milestones extend the same history model with file-content snapshots and semantic editor state.
