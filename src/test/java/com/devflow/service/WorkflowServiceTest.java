package com.devflow.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Pure rule tests: no database needed. */
class WorkflowServiceTest {
    private final Map<String, List<String>> t = WorkflowService.defaultTransitions();

    @Test void allowsEveryTransitionFromTheSpec() {
        String[][] allowed = {
            {"OPEN", "IN_PROGRESS"}, {"IN_PROGRESS", "IN_REVIEW"}, {"IN_REVIEW", "IN_PROGRESS"},
            {"IN_REVIEW", "TESTING"}, {"TESTING", "IN_PROGRESS"}, {"TESTING", "DONE"}, {"DONE", "OPEN"}};
        for (String[] pair : allowed) assertTrue(WorkflowService.isAllowed(t, pair[0], pair[1]), pair[0] + " to " + pair[1]);
    }

    @Test void rejectsSkippingSteps() {
        assertFalse(WorkflowService.isAllowed(t, "OPEN", "DONE"));
        assertFalse(WorkflowService.isAllowed(t, "OPEN", "IN_REVIEW"));
        assertFalse(WorkflowService.isAllowed(t, "IN_PROGRESS", "DONE"));
        assertFalse(WorkflowService.isAllowed(t, "IN_PROGRESS", "OPEN"));
    }

    @Test void rejectsUnknownStatuses() {
        assertFalse(WorkflowService.isAllowed(t, "OPEN", "BANANA"));
        assertFalse(WorkflowService.isAllowed(t, "BANANA", "OPEN"));
    }

    @Test void totalTransitionCountMatchesSpec() {
        assertEquals(7, t.values().stream().mapToInt(List::size).sum());
    }
}
