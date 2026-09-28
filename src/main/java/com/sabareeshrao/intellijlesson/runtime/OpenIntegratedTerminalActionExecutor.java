package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.project.Project;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

public final class OpenIntegratedTerminalActionExecutor implements LessonActionExecutor {
    @Override
    public String actionName() {
        return "openIntegratedTerminal";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        project.getService(LessonTerminalService.class).ensureTerminal();
        return Execution.ok("Opened and focused the real IntelliJ Terminal.");
    }
}
