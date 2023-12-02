package org.rpersival.bot.interaction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    enum Scope {
        GUILD, GLOBAL
    }

    Scope scope() default Scope.GUILD;
}
