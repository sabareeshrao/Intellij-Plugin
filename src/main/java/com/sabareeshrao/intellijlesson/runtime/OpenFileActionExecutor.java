package com.sabareeshrao.intellijlesson.runtime;

import com.google.gson.JsonObject;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.nio.file.Path;

public final class OpenFileActionExecutor implements LessonActionExecutor {
    @Override
    public String actionName() {
        return "openFile";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        JsonObject data = action.data();
        if (data == null || !data.has("file") || data.get("file").isJsonNull()) {
            return Execution.fail("openFile is missing data.file");
        }

        String basePath = project.getBasePath();
        if (basePath == null) {
            return Execution.fail("The IntelliJ project has no local base path.");
        }

        String relative = data.get("file").getAsString();
        Path root = Path.of(basePath).toAbsolutePath().normalize();
        Path target = root.resolve(relative).normalize();

        if (!target.startsWith(root)) {
            return Execution.fail("Rejected path outside the opened project: " + relative);
        }

        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
        if (file == null || file.isDirectory()) {
            return Execution.fail("File not found in the opened project: " + relative);
        }

        FileEditorManager.getInstance(project).openFile(file, true);
        return Execution.ok("Opened real IntelliJ file: " + relative);
    }
}
