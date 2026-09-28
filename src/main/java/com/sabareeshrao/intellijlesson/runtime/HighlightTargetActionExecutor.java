package com.sabareeshrao.intellijlesson.runtime;

import com.google.gson.JsonObject;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.nio.file.Path;

public final class HighlightTargetActionExecutor implements LessonActionExecutor {
    @Override
    public String actionName() {
        return "highlightTarget";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        JsonObject data = action.data();
        if (data == null || !data.has("target") || !data.get("target").isJsonObject()) {
            return Execution.fail("highlightTarget requires data.target");
        }

        JsonObject target = data.getAsJsonObject("target");
        if (!target.has("file") || !target.has("line")) {
            return Execution.fail("M2 highlightTarget currently requires target.file and target.line");
        }

        String relative = target.get("file").getAsString();
        Path filePath = ProjectMutationSupport.safePath(project, relative);
        if (filePath == null) {
            return Execution.fail("Rejected highlight path outside the opened project: " + relative);
        }

        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(filePath);
        if (file == null || file.isDirectory()) {
            return Execution.fail("Highlight file not found: " + relative);
        }

        FileEditorManager manager = FileEditorManager.getInstance(project);
        manager.openFile(file, true);
        Editor editor = manager.getSelectedTextEditor();
        if (editor == null) {
            return Execution.fail("Unable to obtain editor for highlight: " + relative);
        }

        Document document = editor.getDocument();
        int line = Math.max(0, target.get("line").getAsInt() - 1);

        if (target.has("expected_text") && !target.get("expected_text").isJsonNull()) {
            String expected = target.get("expected_text").getAsString();
            boolean lineMatches = line < document.getLineCount()
                    && document.getText().substring(
                            document.getLineStartOffset(line),
                            document.getLineEndOffset(line)
                    ).contains(expected);

            if (!lineMatches) {
                int offset = document.getText().indexOf(expected);
                if (offset < 0) {
                    return Execution.fail("Expected highlight text not found in " + relative + ": " + expected);
                }
                line = document.getLineNumber(offset);
            }
        }

        project.getService(LessonHighlightService.class).highlightLine(editor, line);
        return Execution.ok("Highlighted the real IntelliJ code target in " + relative);
    }
}
