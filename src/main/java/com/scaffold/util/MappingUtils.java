package com.scaffold.util;

import java.util.*;

import static com.scaffold.console.Definitions.ABBREV_NO;
import static com.scaffold.console.Definitions.ABBREV_YES;

public class MappingUtils {

    public static Map<String, Boolean> yesNoTrueFalseMapper() {
        Map<String, Boolean> map = new HashMap<>();
        map.put(ABBREV_YES, true);
        map.put(ABBREV_NO, false);
        return map;
    }

    public static ArrayList<String> removeDuplicates(List<String> list) {
        Set<String> set = new LinkedHashSet<>(list);
        return new ArrayList<>(set);
    }

}
