package com.devflow.service;

import com.devflow.model.Issue;
import com.devflow.model.Recommendation;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PriorityRecommendationServiceTest {
    private final PriorityRecommendationService engine = new PriorityRecommendationService();

    private Issue issue(String type, String status, Integer dueInDays, int ageDays) {
        Issue i = new Issue();
        i.setIssueType(type);
        i.setStatus(status);
        i.setDueDate(dueInDays == null ? null : Date.valueOf(LocalDate.now().plusDays(dueInDays)));
        i.setCreatedAt(Timestamp.valueOf(LocalDateTime.now().minusDays(ageDays)));
        return i;
    }

    @Test void overdueBugThatBlocksOthersIsCritical() {
        Recommendation r = engine.recommend(issue("BUG", "OPEN", -3, 10), 1);
        assertEquals("CRITICAL", r.getSuggestedPriority());
        assertTrue(r.getScore() >= 80);
        assertFalse(r.getReasons().isEmpty());
    }

    @Test void quietNewSubtaskIsLowest() {
        Recommendation r = engine.recommend(issue("SUBTASK", "OPEN", null, 0), 0);
        assertEquals("LOWEST", r.getSuggestedPriority());
    }

    @Test void finishedIssuesGetNoRecommendation() {
        assertNull(engine.recommend(issue("BUG", "DONE", -5, 20), 2));
    }

    @Test void blockingMoreIssuesRaisesTheScore() {
        int none = engine.recommend(issue("TASK", "OPEN", null, 0), 0).getScore();
        int two = engine.recommend(issue("TASK", "OPEN", null, 0), 2).getScore();
        assertTrue(two > none);
    }

    @Test void thresholds() {
        assertEquals("LOWEST", PriorityRecommendationService.toPriority(14));
        assertEquals("LOW", PriorityRecommendationService.toPriority(15));
        assertEquals("MEDIUM", PriorityRecommendationService.toPriority(30));
        assertEquals("HIGH", PriorityRecommendationService.toPriority(55));
        assertEquals("CRITICAL", PriorityRecommendationService.toPriority(80));
    }
}
