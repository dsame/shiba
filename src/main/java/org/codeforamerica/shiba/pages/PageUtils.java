package org.codeforamerica.shiba.pages;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PageUtils {
    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";

    public static String getFormInputName(String name) {
        return name + WEB_INPUT_ARRAY_TOKEN;
    }

    public static String joinNonEmpty(String... strings) {
        return Arrays.stream(strings)
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.joining(", "));
    }

    public static String getTitleString(List<String> strings) {
        if (strings.size() == 1) {
            return strings.iterator().next();
        } else {
            Iterator<String> iterator = strings.iterator();
            StringBuilder stringBuilder = new StringBuilder(iterator.next());
            while (iterator.hasNext()) {
                String string = iterator.next();
                if (iterator.hasNext()) {
                    stringBuilder.append(", ");
                } else {
                    stringBuilder.append(" and ");
                }
                stringBuilder.append(string);
            }
            return stringBuilder.toString();
        }
    }

    public static String formatPhone(String phoneDigits) {
        if (phoneDigits.length() != 10) {
            throw new IllegalArgumentException("Phone must contain exactly 10 digits in order to format!");
        }
        return String.format("(%s) %s-%s", phoneDigits.substring(0,3), phoneDigits.substring(3,6), phoneDigits.substring(6,10));
    }
}
