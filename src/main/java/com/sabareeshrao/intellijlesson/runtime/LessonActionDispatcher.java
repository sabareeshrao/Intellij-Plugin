package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.project.Project;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LessonActionDispatcher {
    private final Map<String, LessonActionExecutor> executors = new LinkedHashMap<>();

    public LessonActionDispatcher() {
        register(new OpenFileActionExecutor());
    }

    private void register(LessonActionExecutor executor) {
        executors.put(executor.actionName(), executor);
    }

    public LessonActionExecutor.Execution execute(Project project, LessonCourse.Action action) {
        if (action == null || action.name() == null || action.name().isBlank()) {
            return LessonActionExecutor.Execution.fail("This lesson step does not define an executable action.");
        }

        LessonActionExecutor executor = executors.get(action.name());
        if (executor == null) {
            return LessonActionExecutor.Execution.fail("Unsupported lesson action in M1: " + action.name());
        }

        return executor.execute(project, action);
    }

    public Set<String> supportedActions() {
        return Set.copyOf(executors.keySet());
    }
}
