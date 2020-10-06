package org.codeforamerica.shiba;

public enum County {
    Hennepin, Olmsted, Other;

    public static County fromString(String county) {
        return switch (county) {
            case "Hennepin" -> Hennepin;
            case "Olmsted" -> Olmsted;
            default -> Other;
        };
    }
}
