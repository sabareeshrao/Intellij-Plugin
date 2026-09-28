package com.sabareeshrao.intellijlesson.runtime;

import com.google.gson.JsonObject;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DeleteResourceActionExecutor implements LessonActionExecutor {
    @Override
    public String actionName() {
        return "deleteResource";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        JsonObject data = action.data();
        if (data == null || !data.has("path") || data.get("path").isJsonNull()) {
            return Execution.fail("deleteResource requires data.path");
        }

        String relative = data.get("path").getAsString();
        Path target = ProjectMutationSupport.safePath(project, relative);
        if (target == null) {
            return Execution.fail("Rejected deleteResource path outside the opened project: " + relative);
        }

        try {
            VirtualFile existing = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
            if (existing != null) {
                FileEditorManager.getInstance(project).closeFile(existing);
            }
            Files.deleteIfExists(target);
            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target.getParent());
            return Execution.ok("Deleted lesson resource: " + relative);
        } catch (IOException error) {
            return Execution.fail("Unable to delete lesson resource: " + error.getMessage());
        }
    }
}
