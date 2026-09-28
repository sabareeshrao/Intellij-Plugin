package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.project.Project;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

public interface LessonActionExecutor {
    String actionName();

    Execution execute(Project project, LessonCourse.Action action);

    record Execution(boolean success, String message) {
        public static Execution ok(String message) {
            return new Execution(true, message);
        }

        public static Execution fail(String message) {
            return new Execution(false, message);
        }
    }
}
