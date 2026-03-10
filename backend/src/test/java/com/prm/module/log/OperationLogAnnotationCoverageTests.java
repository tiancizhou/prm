package com.prm.module.log;

import com.prm.module.bug.controller.BugController;
import com.prm.module.log.annotation.OperLog;
import com.prm.module.project.controller.ProjectController;
import com.prm.module.release.controller.ReleaseController;
import com.prm.module.requirement.controller.RequirementController;
import com.prm.module.sprint.controller.SprintController;
import com.prm.module.task.controller.TaskController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OperationLogAnnotationCoverageTests {

    @Test
    void coreWriteEndpointsShouldHaveOperLogAnnotations() {
        List<Method> methods = List.of(
                method(RequirementController.class, "create"),
                method(RequirementController.class, "update"),
                method(RequirementController.class, "updateStatus"),
                method(RequirementController.class, "addReview"),
                method(RequirementController.class, "addComment"),

                method(TaskController.class, "create"),
                method(TaskController.class, "updateStatus"),
                method(TaskController.class, "assign"),
                method(TaskController.class, "logWork"),

                method(BugController.class, "create"),
                method(BugController.class, "updateStatus"),
                method(BugController.class, "assign"),
                method(BugController.class, "addComment"),
                method(BugController.class, "delete"),
                method(BugController.class, "convertToRequirement"),

                method(SprintController.class, "create"),
                method(SprintController.class, "start"),
                method(SprintController.class, "close"),

                method(ReleaseController.class, "create"),
                method(ReleaseController.class, "publish"),

                method(ProjectController.class, "create"),
                method(ProjectController.class, "update"),
                method(ProjectController.class, "archive"),
                method(ProjectController.class, "close"),
                method(ProjectController.class, "addMember"),
                method(ProjectController.class, "removeMember")
        );

        for (Method method : methods) {
            OperLog annotation = method.getAnnotation(OperLog.class);
            assertThat(annotation)
                    .withFailMessage("%s.%s should declare @OperLog", method.getDeclaringClass().getSimpleName(), method.getName())
                    .isNotNull();
            assertThat(annotation.module()).isNotBlank();
            assertThat(annotation.action()).isNotBlank();
            assertThat(annotation.bizType()).isNotBlank();
        }
    }

    private Method method(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
