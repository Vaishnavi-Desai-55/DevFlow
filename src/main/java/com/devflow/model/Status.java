package com.devflow.model;

/** Issue workflow statuses. getLabel()/getName() exist so JSP EL can read them. */
public enum Status {
    OPEN("Open"), IN_PROGRESS("In Progress"), IN_REVIEW("In Review"), TESTING("Testing"), DONE("Done");

    private final String label;
    Status(String label) { this.label = label; }
    public String getLabel() { return label; }
    public String getName() { return name(); }

    /** Returns null instead of throwing when the text is not a valid status. */
    public static Status from(String s) {
        try { return valueOf(s); } catch (Exception e) { return null; }
    }
}
