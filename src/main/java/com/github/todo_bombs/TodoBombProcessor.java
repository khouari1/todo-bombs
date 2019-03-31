package com.github.todo_bombs;

import com.google.auto.service.AutoService;
import org.joda.time.DateTime;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@SupportedAnnotationTypes("com.github.todo_bombs.TodoBomb")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class TodoBombProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            elements.forEach(element -> {
                TodoBomb todoBomb = element.getAnnotation(TodoBomb.class);
                boolean dueDatePassed = hasDueDatePassed(todoBomb);
                if (dueDatePassed) {
                    printErrorMessage(todoBomb, element);
                }
            });
        });
        return true;
    }

    private boolean hasDueDatePassed(TodoBomb todoBomb) {
        DateTime dueDateTime = getDueDateTime(todoBomb);
        return dueDateTime.isBefore(DateTime.now());
    }

    private DateTime getDueDateTime(TodoBomb todoBomb) {
        String dueDateString = todoBomb.dueDate();
        return DateTime.parse(dueDateString);
    }

    private String createErrorMessage(TodoBomb todoBomb) {
        String message = "BOOM! A To-do bomb went off.";
        String customMessage = todoBomb.message();
        if (isNotBlank(customMessage)) {
            message += String.format(" Custom message: [%s]", customMessage);
        }
        return message;
    }

    private void printErrorMessage(TodoBomb todoBomb, Element element) {
        String message = createErrorMessage(todoBomb);
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
