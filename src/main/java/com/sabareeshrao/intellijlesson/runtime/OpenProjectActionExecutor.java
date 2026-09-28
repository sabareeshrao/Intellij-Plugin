package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.project.Project;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

public final class OpenProjectActionExecutor implements LessonActionExecutor {
    @Override
    public String actionName() {
        return "openProject";
    }

    @Override
    public Execution execute(Project project, LessonCourse.Action action) {
        return project.getBasePath() == null
                ? Execution.fail("No local IntelliJ project is open.")
                : Execution.ok("The lesson project is already open in real IntelliJ.");
    }
}
