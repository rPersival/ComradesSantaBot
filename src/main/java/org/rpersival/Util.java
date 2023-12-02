package org.rpersival;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public class Util {
    public static <T> List<T> getTargetFields(Class<?> targetClass, Class<T> fieldClass) {
        return getPartitions(targetClass, fieldClass, null, (x) -> true).getKey();
    }

    public static <T> Map.Entry<List<T>, List<T>> getPartitions(Class<?> targetClass, Class<T> fieldClass,
                                                                Class<? extends Annotation> annotation,
                                                                Function<Field, Boolean> splitCondition) {
        List<T> first = new ArrayList<>();
        List<T> second = new ArrayList<>();

        for (Field field : targetClass.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
                    && (annotation == null || field.isAnnotationPresent(annotation))) {
                T castedField = castToTarget(targetClass, fieldClass, field);

                if (splitCondition.apply(field)) {
                    first.add(castedField);
                } else {
                    second.add(castedField);
                }
            }
        }
        return Map.entry(Collections.unmodifiableList(first), Collections.unmodifiableList(second));
    }

    private static <T> T castToTarget(Class<?> targetClass, Class<T> fieldClass, Field field) {
        try {
            return fieldClass.cast(field.get(targetClass));
        } catch (IllegalAccessException impossibleException) {
            // impossible
            throw new RuntimeException("Something went very wrong");
        }
    }
}