package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.terminal.frontend.view.TerminalView;
import org.jetbrains.plugins.terminal.view.TerminalSendTextBuilder;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service(Service.Level.PROJECT)
public final class LessonTypingService {
    private final Project project;
    private final AtomicBoolean busy = new AtomicBoolean(false);

    public LessonTypingService(Project project) {
        this.project = project;
    }

    public boolean isBusy() {
        return busy.get();
    }

    public boolean typeIntoEditor(Editor editor, String text, int delayMs) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        if (!busy.compareAndSet(false, true)) {
            return false;
        }

        Runnable starter = () -> {
            final int[] index = {0};
            Timer timer = new Timer(Math.max(1, delayMs), null);
            timer.addActionListener(event -> {
                if (project.isDisposed() || editor.isDisposed()) {
                    timer.stop();
                    busy.set(false);
                    return;
                }

                try {
                    char next = text.charAt(index[0]++);
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        int offset = editor.getCaretModel().getOffset();
                        editor.getDocument().insertString(offset, String.valueOf(next));
                        editor.getCaretModel().moveToOffset(offset + 1);
                    });
                    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);

                    if (index[0] >= text.length()) {
                        timer.stop();
                        busy.set(false);
                    }
                } catch (RuntimeException failure) {
                    timer.stop();
                    busy.set(false);
                }
            });
            timer.setInitialDelay(100);
            timer.start();
        };

        if (ApplicationManager.getApplication().isDispatchThread()) {
            starter.run();
        } else {
            ApplicationManager.getApplication().invokeLater(starter);
        }
        return true;
    }

    public boolean typeAndExecuteInTerminal(TerminalView view, String command, int delayMs) {
        if (command == null || command.isBlank()) {
            return false;
        }
        if (!busy.compareAndSet(false, true)) {
            return false;
        }

        Runnable starter = () -> {
            final int[] index = {0};
            Timer timer = new Timer(Math.max(1, delayMs), null);
            timer.addActionListener(event -> {
                if (project.isDisposed()) {
                    timer.stop();
                    busy.set(false);
                    return;
                }

                try {
                    int current = index[0]++;
                    boolean last = current == command.length() - 1;
                    String character = String.valueOf(command.charAt(current));

                    if (last) {
                        TerminalSendTextBuilder builder = view.createSendTextBuilder();
                        builder.shouldExecute().send(character);
                        timer.stop();
                        busy.set(false);
                    } else {
                        view.sendText(character);
                    }
                } catch (RuntimeException failure) {
                    timer.stop();
                    busy.set(false);
                }
            });
            timer.setInitialDelay(250);
            timer.start();
        };

        if (ApplicationManager.getApplication().isDispatchThread()) {
            starter.run();
        } else {
            ApplicationManager.getApplication().invokeLater(starter);
        }
        return true;
    }
}
