package org.codeforamerica.shiba;


import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class PageDataBuilder {
    String pageName;
    Map<String, List<String>> pageDataMap;
}
