package com.github.todo_bombs;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Set;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

@RunWith(MockitoJUnitRunner.class)
public class TodoBombProcessorTest {

    @Test
    public void shouldPassCompilationWithoutErrorsOrWarnings() {
        DateTime dueDate = DateTime.now().plusDays(1);
        JavaFileObject helloWorld = JavaFileObjects.forSourceString("HelloWorld", createSourceWithTodoBomb(dueDate));

        Compilation compilation = javac().withProcessors(new TodoBombProcessor()).compile(helloWorld);

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningCount(0);
    }

    @Test
    public void shouldPassCompilationWithAnotherAnnotationProcessor() {
        DateTime dueDate = DateTime.now().plusDays(1);
        JavaFileObject helloWorld = JavaFileObjects.forSourceString("HelloWorld", createSourceWithTodoBomb(dueDate));

        Compilation compilation = javac().withProcessors(new TodoBombProcessor(), new TestProcessor()).compile(helloWorld);

        assertThat(compilation).succeeded();
    }

    @Test
    public void shouldFailCompilation() {
        DateTime dueDate = DateTime.now().minusDays(1);
        JavaFileObject helloWorld = JavaFileObjects.forSourceString("HelloWorld", createSourceWithTodoBomb(dueDate));

        Compilation compilation = javac().withProcessors(new TodoBombProcessor()).compile(helloWorld);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("BOOM! A To-do bomb went off.");
    }

    @Test
    public void shouldFailCompilationWithCustomMessage() {
        DateTime dueDate = DateTime.now().minusDays(1);
        String customMessage = "Fix this code!";
        JavaFileObject helloWorld = JavaFileObjects.forSourceString("HelloWorld", createSourceWithTodoBomb(dueDate, customMessage));

        Compilation compilation = javac().withProcessors(new TodoBombProcessor()).compile(helloWorld);

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(String.format("BOOM! A To-do bomb went off. Custom message: [%s]", customMessage));
    }

    private String createSourceWithTodoBomb(DateTime dueDate) {
        return createSourceWithTodoBomb(dueDate, "");
    }

    private String createSourceWithTodoBomb(DateTime dueDate, String customMessage) {
        return String.format("import com.github.todo_bombs.TodoBomb;%n %s final class HelloWorld {}", createTodoBomb(dueDate, customMessage));
    }

    private String createTodoBomb(DateTime dueDate, String customMessage) {
        return String.format("@TodoBomb(dueDate=\"%s\", message=\"%s\")", dueDate, customMessage);
    }

    private class TestProcessor extends AbstractProcessor {

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Test warning", null);
            return true;
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.RELEASE_8;
        }
    }

}
