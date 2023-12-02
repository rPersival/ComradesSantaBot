package org.rpersival;

import org.rpersival.bot.interaction.Command;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Util {
    public static <T> List<T> getTargetFields(Class<?> targetClass, Class<T> fieldClass) {
        return Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> {
                    int modifiers = field.getModifiers();
                    return Modifier.isStatic(modifiers) &&
                            Modifier.isPublic(modifiers) &&
                            field.isAnnotationPresent(Command.class);
                })
                .map(field -> {
                    try {
                        return fieldClass.cast(field.get(targetClass));
                    } catch (IllegalAccessException ignored) {
                        // ignored
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}