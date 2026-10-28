package com.devflow.model;

import java.util.List;

/** Result of the rule-based priority engine: a score, a suggested priority and the reasons behind it. */
public class Recommendation {
    private final int score;
    private final String suggestedPriority;
    private final List<String> reasons;

    public Recommendation(int score, String suggestedPriority, List<String> reasons) {
        this.score = score; this.suggestedPriority = suggestedPriority; this.reasons = reasons;
    }
    public int getScore() { return score; }
    public String getSuggestedPriority() { return suggestedPriority; }
    public List<String> getReasons() { return reasons; }
}
