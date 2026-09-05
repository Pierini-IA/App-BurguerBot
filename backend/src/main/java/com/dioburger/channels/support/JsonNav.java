package com.dioburger.channels.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helpers de navegación defensiva sobre el {@code Map<String, Object>} crudo
 * que produce Jackson al deserializar el JSON de un webhook de Meta.
 * Evita repetir casteos y null-checks en cada adapter.
 *
 * @author Dio Burger Team
 * @version 1.0.0
 */
public final class JsonNav {

    private JsonNav() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Object o) {
        return o instanceof Map<?, ?> ? (Map<String, Object>) o : null;
    }

    public static List<Map<String, Object>> asListOfMaps(Object o) {
        if (!(o instanceof List<?> list)) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> m = asMap(item);
            if (m != null) {
                result.add(m);
            }
        }
        return result;
    }

    public static String asString(Object o) {
        return o instanceof String s ? s : null;
    }

    /**
     * Recorre {@code entry[].changes[]} y devuelve el primer {@code value} cuyo
     * {@code field} coincida con el esperado (ej. "messages", "comments", "feed").
     * Si {@code expectedField} es {@code null}, devuelve el primer value que encuentre.
     */
    public static Map<String, Object> firstEntryChangeValue(Map<String, Object> payload, String expectedField) {
        List<Map<String, Object>> entries = asListOfMaps(payload.get("entry"));
        if (entries == null) {
            return null;
        }
        for (Map<String, Object> entry : entries) {
            List<Map<String, Object>> changes = asListOfMaps(entry.get("changes"));
            if (changes == null) {
                continue;
            }
            for (Map<String, Object> change : changes) {
                if (expectedField == null || expectedField.equals(change.get("field"))) {
                    Map<String, Object> value = asMap(change.get("value"));
                    if (value != null) {
                        return value;
                    }
                }
            }
        }
        return null;
    }
}
