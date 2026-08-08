package com.mhd.alert.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhao-hao-dong
 **/
@Slf4j
public class AlertTemplateUtils {
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([\\w.]+)}");

    public static String render(String template, Map<String, Object> replaceData) {
        if (!StringUtils.hasText(template)) {
            return template;
        }
        if (replaceData == null) {
            log.warn("The render replace data is null.");
            return template;
        }
        try {
            Matcher matcher = PATTERN.matcher(template);
            StringBuilder builder = new StringBuilder();
            while (matcher.find()) {
                String propertyPath = matcher.group(1);
                Object objectValue = getNestedProperty(replaceData, propertyPath);
                String value = objectValue != null ? objectValue.toString() : "NullValue";
                matcher.appendReplacement(builder, Matcher.quoteReplacement(value));
            }
            matcher.appendTail(builder);
            return builder.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return template;
        }
    }

    private static Object getNestedProperty(Map<String, Object> replaceData, String propertyPath) {
        if (propertyPath == null || propertyPath.isEmpty()) {
            return null;
        }

        String[] parts = propertyPath.split("\\.");
        Object current = replaceData.get(parts[0]);

        if (current == null) {
            return null;
        }

        // If only one part, return the direct value
        if (parts.length == 1) {
            return current;
        }

        // Navigate through the property path
        for (int i = 1; i < parts.length; i++) {
            current = getPropertyValue(current, parts[i]);
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private static Object getPropertyValue(Object obj, String propertyName) {
        if (obj == null || propertyName == null) {
            return null;
        }

        try {
            // Handle Map objects
            if (obj instanceof Map) {
                return ((Map<?, ?>) obj).get(propertyName);
            }

            Class<?> clazz = obj.getClass();

            // Try getter method first (getPropertyName or isPropertyName for boolean)
            String getterName = "get" + capitalize(propertyName);
            String booleanGetterName = "is" + capitalize(propertyName);

            try {
                Method getter = clazz.getMethod(getterName);
                return getter.invoke(obj);
            } catch (NoSuchMethodException e) {
                // Try boolean getter
                try {
                    Method booleanGetter = clazz.getMethod(booleanGetterName);
                    return booleanGetter.invoke(obj);
                } catch (NoSuchMethodException ignored) {
                    // Continue to field access
                }
            }

            // Try direct field access
            try {
                Field field = clazz.getDeclaredField(propertyName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException ignored) {
                // Field not found
            }

            // Try parent classes
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null && !superClass.equals(Object.class)) {
                try {
                    Field field = superClass.getDeclaredField(propertyName);
                    field.setAccessible(true);
                    return field.get(obj);
                } catch (NoSuchFieldException ignored) {
                    // Continue with parent class
                }
                superClass = superClass.getSuperclass();
            }

        } catch (Exception e) {
            log.debug("Failed to access property '{}' on object of type {}: {}",
                    propertyName, obj.getClass().getSimpleName(), e.getMessage());
        }

        return null;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
