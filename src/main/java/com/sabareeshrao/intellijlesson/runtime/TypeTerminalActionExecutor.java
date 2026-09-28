package com.sabareeshrao.intellijlesson.runtime;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTab;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

public final class TypeTerminalActionExecutor implements LessonActionExecutor {
    private static final int DEFAULT_DELAY_MS = 28;

    @Override
    public String actionName() {
        return "typeTerminal";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        JsonObject data = action.data();
        if (data == null || !data.has("command") || data.get("command").isJsonNull()) {
            return Execution.fail("typeTerminal requires data.command");
        }

        String command = data.get("command").getAsString();
        TerminalToolWindowTab tab = project.getService(LessonTerminalService.class).ensureTerminal();
        int delay = data.has("delayMs") ? data.get("delayMs").getAsInt() : DEFAULT_DELAY_MS;

        boolean started = project.getService(LessonTypingService.class)
                .typeAndExecuteInTerminal(tab.getView(), command, delay);

        return started
                ? Execution.ok("Auto-typing and executing in the real IntelliJ Terminal: " + command)
                : Execution.fail("Another auto-typing action is still running.");
    }
}
