package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTab;
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager;

@Service(Service.Level.PROJECT)
public final class LessonTerminalService {
    private final Project project;
    private TerminalToolWindowTab lessonTab;

    public LessonTerminalService(Project project) {
        this.project = project;
    }

    public TerminalToolWindowTab ensureTerminal() {
        if (!ApplicationManager.getApplication().isDispatchThread()) {
            throw new IllegalStateException("Terminal creation must run on the IntelliJ UI thread.");
        }

        TerminalToolWindowTabsManager manager = TerminalToolWindowTabsManager.getInstance(project);
        if (lessonTab == null || !manager.getTabs().contains(lessonTab)) {
            lessonTab = manager.createTabBuilder()
                    .workingDirectory(project.getBasePath())
                    .tabName("AeroTopo Lesson")
                    .requestFocus(true)
                    .createTab();
        }

        ToolWindow terminal = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (terminal != null) {
            terminal.activate(null, true);
        }
        lessonTab.getView().getPreferredFocusableComponent().requestFocusInWindow();
        return lessonTab;
    }
}
