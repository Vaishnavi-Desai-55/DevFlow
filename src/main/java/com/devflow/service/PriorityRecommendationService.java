package com.devflow.service;

import com.devflow.model.Issue;
import com.devflow.model.Recommendation;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple, explainable scoring rules (no AI APIs). Points are added for:
 *   issue type (bugs matter most), being overdue, a close due date, waiting time, and blocking other issues.
 * The score is turned into a suggested priority. The user's own priority is never changed automatically.
 */
public class PriorityRecommendationService {

    /** Returns null for finished issues (nothing to recommend). */
    public Recommendation recommend(Issue issue, int openIssuesBlocked) {
        if (issue.isDone()) return null;
        int score = 0;
        List<String> why = new ArrayList<>();

        switch (issue.getIssueType()) {
            case "BUG":     score += 25; why.add("It is a bug (+25)"); break;
            case "SUBTASK": score += 5;  why.add("It is a subtask (+5)"); break;
            default:        score += 10; why.add("It is a " + issue.getIssueType().toLowerCase() + " (+10)");
        }

        Long days = issue.getDaysUntilDue();
        if (days != null) {
            if (days < 0) {
                long late = -days;
                int pts = 30 + (int) Math.min(20, late * 2);
                score += pts;
                why.add("Overdue by " + late + " day" + (late == 1 ? "" : "s") + " (+" + pts + ")");
            } else if (days <= 2) {
                score += 20;
                why.add(days == 0 ? "Due today (+20)" : "Due in " + days + " day" + (days == 1 ? "" : "s") + " (+20)");
            } else if (days <= 7) {
                score += 10;
                why.add("Due within a week (+10)");
            }
        }

        long waiting = issue.getDaysWaiting();
        if (waiting >= 3) {
            int pts = (int) Math.min(15, waiting);
            score += pts;
            why.add("Open for " + waiting + " days (+" + pts + ")");
        }

        if (openIssuesBlocked > 0) {
            int pts = Math.min(30, 15 * openIssuesBlocked);
            score += pts;
            why.add("Blocks " + openIssuesBlocked + " unfinished issue" + (openIssuesBlocked == 1 ? "" : "s") + " (+" + pts + ")");
        }

        return new Recommendation(score, toPriority(score), why);
    }

    static String toPriority(int score) {
        if (score >= 80) return "CRITICAL";
        if (score >= 55) return "HIGH";
        if (score >= 30) return "MEDIUM";
        if (score >= 15) return "LOW";
        return "LOWEST";
    }
}
