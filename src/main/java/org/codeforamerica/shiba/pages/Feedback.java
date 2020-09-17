package org.codeforamerica.shiba.pages;

import lombok.Value;

@Value
public class Feedback {
    Sentiment sentiment;
    String feedback;
}
