package com.example.campuslife.data;



import com.example.campuslife.entity.Department;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartmentCache {
    private static final Map<Long, String> NAME_BY_ID = new HashMap<>();

    public static void putAll(List<Department> list) {
        NAME_BY_ID.clear();
        if (list != null) {
            for (Department d : list) {
                if (d != null && d.getId() != null) NAME_BY_ID.put(d.getId(), d.getName());
            }
        }
    }

    public static Map<Long, String> getMap() {
        return Collections.unmodifiableMap(NAME_BY_ID);
    }

    public static String nameOf(Long id) {
        if (id == null) return null;
        return NAME_BY_ID.get(id);
    }

    public static boolean isLoaded() {
        return !NAME_BY_ID.isEmpty();
    }
}
