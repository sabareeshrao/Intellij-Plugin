package com.sabareeshrao.intellijlesson.runtime;

import com.google.gson.JsonObject;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CreateFileActionExecutor implements LessonActionExecutor {
    private static final int DEFAULT_DELAY_MS = 10;

    @Override
    public String actionName() {
        return "createFile";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        JsonObject data = action.data();
        if (data == null || !data.has("path") || !data.has("content")) {
            return Execution.fail("createFile requires data.path and data.content");
        }

        String relative = data.get("path").getAsString();
        String content = data.get("content").getAsString();
        Path target = ProjectMutationSupport.safePath(project, relative);
        if (target == null) {
            return Execution.fail("Rejected createFile path outside the opened project: " + relative);
        }

        try {
            if (Files.exists(target)) {
                if (Files.isRegularFile(target) &&
                        Files.readString(target, StandardCharsets.UTF_8).equals(content)) {
                    VirtualFile existing = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
                    if (existing != null) {
                        FileEditorManager.getInstance(project).openFile(existing, true);
                    }
                    return Execution.ok("File already contains the expected lesson content: " + relative);
                }
                return Execution.fail("Refusing to overwrite existing file: " + relative);
            }

            Files.createDirectories(target.getParent());
            Files.createFile(target);
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
            if (file == null) {
                return Execution.fail("Created file but IntelliJ could not refresh it: " + relative);
            }

            FileEditorManager manager = FileEditorManager.getInstance(project);
            manager.openFile(file, true);
            Editor editor = manager.getSelectedTextEditor();
            if (editor == null) {
                return Execution.fail("Unable to obtain a real editor for: " + relative);
            }

            int delay = data.has("delayMs") ? data.get("delayMs").getAsInt() : DEFAULT_DELAY_MS;
            boolean started = project.getService(LessonTypingService.class)
                    .typeIntoEditor(editor, content, delay);
            return started
                    ? Execution.ok("Auto-typing real file content into " + relative)
                    : Execution.fail("Another auto-typing action is still running.");
        } catch (IOException error) {
            return Execution.fail("Unable to create lesson file: " + error.getMessage());
        }
    }
}
