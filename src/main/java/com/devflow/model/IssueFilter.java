package com.devflow.model;

/** Search / filter / sort / paging options for the issue list. */
public class IssueFilter {
    private Integer projectId, assigneeId, reporterId;
    private String type, status, priority, q, sort = "created", dir = "desc";
    private boolean openOnly;
    private int page = 1, size = 10;

    public Integer getProjectId() { return projectId; }   public void setProjectId(Integer v) { projectId = v; }
    public Integer getAssigneeId() { return assigneeId; } public void setAssigneeId(Integer v) { assigneeId = v; }
    public Integer getReporterId() { return reporterId; } public void setReporterId(Integer v) { reporterId = v; }
    public String getType() { return type; }              public void setType(String v) { type = v; }
    public String getStatus() { return status; }          public void setStatus(String v) { status = v; }
    public String getPriority() { return priority; }      public void setPriority(String v) { priority = v; }
    public String getQ() { return q; }                    public void setQ(String v) { q = v; }
    public String getSort() { return sort; }              public void setSort(String v) { sort = v; }
    public String getDir() { return dir; }                public void setDir(String v) { dir = v; }
    public boolean isOpenOnly() { return openOnly; }      public void setOpenOnly(boolean v) { openOnly = v; }
    public int getPage() { return page; }                 public void setPage(int v) { page = Math.max(1, v); }
    public int getSize() { return size; }                 public void setSize(int v) { size = Math.min(50, Math.max(1, v)); }
}
