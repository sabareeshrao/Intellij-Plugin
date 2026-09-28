package com.sabareeshrao.intellijlesson.runtime;

import com.google.gson.JsonObject;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.nio.file.Path;

public final class TypeCodeActionExecutor implements LessonActionExecutor {
    private static final int DEFAULT_DELAY_MS = 10;

    @Override
    public String actionName() {
        return "typeCode";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        JsonObject data = action.data();
        if (data == null) {
            return Execution.fail("typeCode is missing action data.");
        }

        String text = data.has("text") ? data.get("text").getAsString()
                : data.has("content") ? data.get("content").getAsString() : null;
        if (text == null) {
            return Execution.fail("typeCode requires data.text or data.content");
        }

        FileEditorManager manager = FileEditorManager.getInstance(project);
        if (data.has("file") && !data.get("file").isJsonNull()) {
            String relative = data.get("file").getAsString();
            Path target = ProjectMutationSupport.safePath(project, relative);
            if (target == null) {
                return Execution.fail("Rejected typeCode path outside the opened project: " + relative);
            }
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
            if (file == null || file.isDirectory()) {
                return Execution.fail("typeCode file not found: " + relative);
            }
            manager.openFile(file, true);
        }

        Editor editor = manager.getSelectedTextEditor();
        if (editor == null) {
            return Execution.fail("No active real IntelliJ text editor is available.");
        }

        int delay = data.has("delayMs") ? data.get("delayMs").getAsInt() : DEFAULT_DELAY_MS;
        boolean started = project.getService(LessonTypingService.class)
                .typeIntoEditor(editor, text, delay);
        return started
                ? Execution.ok("Auto-typing code into the real IntelliJ editor.")
                : Execution.fail("Another auto-typing action is still running.");
    }
}
