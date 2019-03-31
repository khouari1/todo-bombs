package com.github.todo_bombs;

public @interface TodoBomb {

    String dueDate();

    String message() default "";
}
