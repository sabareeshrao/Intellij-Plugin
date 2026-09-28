package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.sabareeshrao.intellijlesson.course.LessonCourseLoader;
import com.sabareeshrao.intellijlesson.model.LessonCourse;
import com.sabareeshrao.intellijlesson.state.LessonProgressService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service(Service.Level.PROJECT)
public final class LessonRuntimeService {
    private final Project project;
    private final LessonProgressService progress;
    private final LessonActionDispatcher dispatcher = new LessonActionDispatcher();

    private LessonCourse course;
    private String loadError = "";

    public LessonRuntimeService(Project project) {
        this.project = project;
        this.progress = project.getService(LessonProgressService.class);
        reload();
    }

    public void reload() {
        String basePath = project.getBasePath();
        if (basePath == null) {
            course = null;
            loadError = "Open a local Java-Practice-Project checkout before loading lessons.";
            return;
        }

        try {
            course = new LessonCourseLoader().load(Path.of(basePath));
            loadError = "";
            normalizeSavedPosition();
        } catch (IOException | RuntimeException error) {
            course = null;
            loadError = "Unable to load simulation/course.json: " + error.getMessage();
        }
    }

    public Optional<CurrentStep> current() {
        if (course == null || course.lessons().isEmpty()) {
            return Optional.empty();
        }

        int lessonIndex = course.lessonIndex(progress.lessonNumber());
        if (lessonIndex < 0) {
            lessonIndex = 0;
        }

        LessonCourse.Lesson lesson = course.lessons().get(lessonIndex);
        int stepIndex = Math.min(Math.max(0, progress.stepIndex()), lesson.steps().size() - 1);
        LessonCourse.Step step = lesson.steps().get(stepIndex);
        String bookTitle = course.bookTitleForChapter(lesson.chapterOrdinal());

        return Optional.of(new CurrentStep(bookTitle, lesson, stepIndex, step));
    }

    public Result next() {
        Optional<CurrentStep> current = current();
        if (current.isEmpty()) {
            return Result.fail(loadError);
        }

        Position next = nextPosition(current.get());
        if (next == null) {
            return Result.fail("End of the currently loaded course.");
        }

        LessonCourse.Lesson nextLesson = course.lessons().get(next.lessonIndex());
        LessonCourse.Step nextStep = nextLesson.steps().get(next.stepIndex());
        EditorStateSupport.Snapshot snapshot = EditorStateSupport.capture(project);
        LessonActionExecutor.Execution execution = dispatcher.execute(project, nextStep.action());

        if (!execution.success()) {
            return Result.fail(execution.message());
        }

        progress.pushHistory(new LessonProgressService.HistoryEntry(
                current.get().lesson().lessonNumber(),
                current.get().stepIndex(),
                snapshot.relativeFile(),
                snapshot.caretOffset()
        ));
        progress.setPosition(nextLesson.lessonNumber(), next.stepIndex());
        return Result.ok(execution.message());
    }

    public Result previous() {
        Optional<LessonProgressService.HistoryEntry> history = progress.popHistory();
        if (history.isPresent()) {
            LessonProgressService.HistoryEntry entry = history.get();
            progress.setPosition(entry.lessonNumber, entry.stepIndex);
            EditorStateSupport.restore(
                    project,
                    new EditorStateSupport.Snapshot(entry.activeFile, entry.caretOffset)
            );
            return Result.ok("Restored previous lesson position and recorded editor state.");
        }

        Optional<CurrentStep> current = current();
        if (current.isEmpty()) {
            return Result.fail(loadError);
        }

        Position previous = previousPosition(current.get());
        if (previous == null) {
            return Result.fail("Already at the first lesson step.");
        }

        LessonCourse.Lesson lesson = course.lessons().get(previous.lessonIndex());
        progress.setPosition(lesson.lessonNumber(), previous.stepIndex());
        return Result.ok("Moved to the previous lesson position. No editor snapshot was available.");
    }

    public Result replay() {
        Optional<CurrentStep> current = current();
        if (current.isEmpty()) {
            return Result.fail(loadError);
        }

        LessonActionExecutor.Execution execution = dispatcher.execute(project, current.get().step().action());
        return execution.success() ? Result.ok(execution.message()) : Result.fail(execution.message());
    }

    public Result jumpToLesson(int lessonNumber) {
        if (course == null) {
            return Result.fail(loadError);
        }
        int index = course.lessonIndex(lessonNumber);
        if (index < 0) {
            return Result.fail("Lesson not found: " + lessonNumber);
        }
        progress.clearHistory();
        progress.setPosition(lessonNumber, 0);
        return Result.ok("Selected Lesson " + lessonNumber + ". Press Replay to execute its current action.");
    }

    public List<LessonCourse.Lesson> lessons() {
        return course == null ? List.of() : course.lessons();
    }

    public String courseTitle() {
        return course == null ? "AeroTopo Lessons" : course.title();
    }

    public String loadError() {
        return loadError;
    }

    private void normalizeSavedPosition() {
        if (course == null || course.lessons().isEmpty()) {
            return;
        }

        int lessonIndex = course.lessonIndex(progress.lessonNumber());
        if (lessonIndex < 0) {
            LessonCourse.Lesson first = course.lessons().get(0);
            progress.setPosition(first.lessonNumber(), 0);
            return;
        }

        LessonCourse.Lesson lesson = course.lessons().get(lessonIndex);
        if (progress.stepIndex() >= lesson.steps().size()) {
            progress.setPosition(lesson.lessonNumber(), lesson.steps().size() - 1);
        }
    }

    private Position nextPosition(CurrentStep current) {
        int lessonIndex = course.lessonIndex(current.lesson().lessonNumber());
        if (current.stepIndex() + 1 < current.lesson().steps().size()) {
            return new Position(lessonIndex, current.stepIndex() + 1);
        }
        if (lessonIndex + 1 < course.lessons().size()) {
            return new Position(lessonIndex + 1, 0);
        }
        return null;
    }

    private Position previousPosition(CurrentStep current) {
        int lessonIndex = course.lessonIndex(current.lesson().lessonNumber());
        if (current.stepIndex() > 0) {
            return new Position(lessonIndex, current.stepIndex() - 1);
        }
        if (lessonIndex > 0) {
            LessonCourse.Lesson previousLesson = course.lessons().get(lessonIndex - 1);
            return new Position(lessonIndex - 1, previousLesson.steps().size() - 1);
        }
        return null;
    }

    private record Position(int lessonIndex, int stepIndex) {}

    public record CurrentStep(
            String bookTitle,
            LessonCourse.Lesson lesson,
            int stepIndex,
            LessonCourse.Step step
    ) {
        public boolean finalStep() {
            return stepIndex == lesson.steps().size() - 1;
        }
    }

    public record Result(boolean success, String message) {
        public static Result ok(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message == null || message.isBlank() ? "Action failed." : message);
        }
    }
}
