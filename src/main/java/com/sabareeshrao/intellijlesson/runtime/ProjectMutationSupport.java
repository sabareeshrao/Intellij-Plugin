package com.sabareeshrao.intellijlesson.runtime;

import com.google.gson.JsonObject;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectMutationSupport {
    private ProjectMutationSupport() {}

    public record Snapshot(String relativeFile, boolean existed, String previousContent) {
        public static Snapshot none() {
            return new Snapshot("", false, "");
        }

        public boolean present() {
            return relativeFile != null && !relativeFile.isBlank();
        }
    }

    public static Snapshot capture(Project project, LessonCourse.Action action) {
        if (action == null || action.data() == null) {
            return Snapshot.none();
        }

        String path = mutationPath(project, action);
        if (path == null || path.isBlank()) {
            return Snapshot.none();
        }

        Path target = safePath(project, path);
        if (target == null) {
            return Snapshot.none();
        }

        try {
            boolean existed = Files.isRegularFile(target);
            String content = existed ? Files.readString(target, StandardCharsets.UTF_8) : "";
            return new Snapshot(path.replace('\\', '/'), existed, content);
        } catch (IOException ignored) {
            return Snapshot.none();
        }
    }

    public static void restore(Project project, Snapshot snapshot) {
        if (snapshot == null || !snapshot.present()) {
            return;
        }

        Path target = safePath(project, snapshot.relativeFile());
        if (target == null) {
            return;
        }

        try {
            VirtualFile existing = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
            if (existing != null) {
                FileEditorManager.getInstance(project).closeFile(existing);
            }

            if (snapshot.existed()) {
                Files.createDirectories(target.getParent());
                Files.writeString(target, snapshot.previousContent(), StandardCharsets.UTF_8);
            } else {
                Files.deleteIfExists(target);
            }

            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(
                    snapshot.existed() ? target : target.getParent()
            );
        } catch (IOException ignored) {
            // Previous still restores lesson/editor navigation even if filesystem restoration fails.
        }
    }

    public static Path safePath(Project project, String relative) {
        String basePath = project.getBasePath();
        if (basePath == null || relative == null || relative.isBlank()) {
            return null;
        }
        Path root = Path.of(basePath).toAbsolutePath().normalize();
        Path target = root.resolve(relative).normalize();
        return target.startsWith(root) ? target : null;
    }

    private static String mutationPath(Project project, LessonCourse.Action action) {
        JsonObject data = action.data();
        return switch (action.name()) {
            case "createFile", "deleteResource" ->
                    data.has("path") && !data.get("path").isJsonNull() ? data.get("path").getAsString() : null;
            case "typeCode" -> {
                if (data.has("file") && !data.get("file").isJsonNull()) {
                    yield data.get("file").getAsString();
                }
                VirtualFile[] selected = FileEditorManager.getInstance(project).getSelectedFiles();
                if (selected.length == 0 || project.getBasePath() == null) {
                    yield null;
                }
                Path root = Path.of(project.getBasePath()).toAbsolutePath().normalize();
                Path selectedPath = Path.of(selected[0].getPath()).toAbsolutePath().normalize();
                yield selectedPath.startsWith(root)
                        ? root.relativize(selectedPath).toString().replace('\\', '/')
                        : null;
            }
            default -> null;
        };
    }
}
