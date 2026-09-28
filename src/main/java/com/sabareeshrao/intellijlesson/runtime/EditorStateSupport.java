package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;

public final class EditorStateSupport {
    private EditorStateSupport() {}

    public record Snapshot(String relativeFile, int caretOffset) {}

    public static Snapshot capture(Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return new Snapshot("", 0);
        }

        FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile[] selected = manager.getSelectedFiles();
        String relative = "";

        if (selected.length > 0) {
            try {
                Path root = Path.of(basePath).toAbsolutePath().normalize();
                Path filePath = Path.of(selected[0].getPath()).toAbsolutePath().normalize();
                if (filePath.startsWith(root)) {
                    relative = root.relativize(filePath).toString().replace('\\', '/');
                }
            } catch (RuntimeException ignored) {
                relative = "";
            }
        }

        Editor editor = manager.getSelectedTextEditor();
        int caret = editor == null ? 0 : editor.getCaretModel().getOffset();
        return new Snapshot(relative, Math.max(0, caret));
    }

    public static void restore(Project project, Snapshot snapshot) {
        if (snapshot == null || snapshot.relativeFile() == null || snapshot.relativeFile().isBlank()) {
            return;
        }

        String basePath = project.getBasePath();
        if (basePath == null) {
            return;
        }

        Path root = Path.of(basePath).toAbsolutePath().normalize();
        Path target = root.resolve(snapshot.relativeFile()).normalize();
        if (!target.startsWith(root)) {
            return;
        }

        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(target);
        if (file == null) {
            return;
        }

        FileEditorManager manager = FileEditorManager.getInstance(project);
        manager.openFile(file, true);
        Editor editor = manager.getSelectedTextEditor();
        if (editor != null) {
            int offset = Math.min(snapshot.caretOffset(), editor.getDocument().getTextLength());
            editor.getCaretModel().moveToOffset(Math.max(0, offset));
            editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
        }
    }
}
