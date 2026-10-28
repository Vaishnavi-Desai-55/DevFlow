<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${project.name}"/><c:set var="nav" value="projects"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="crumbs"><a href="${ctx}/projects">Projects</a> / <c:out value="${project.key}"/></div>
<div class="page-head">
  <div>
    <h1><c:out value="${project.name}"/> <span class="keychip"><c:out value="${project.key}"/></span>
      <c:if test="${project.archived}"><span class="badge st-OPEN">Archived</span></c:if></h1>
    <p>Created by <c:out value="${project.createdByName}"/> on <fmt:formatDate value="${project.createdAt}" pattern="dd MMM yyyy"/>
       · Your role: <span class="role role-${project.myRole}">${roleLabels[project.myRole]}</span></p>
  </div>
  <div class="actions">
    <a class="btn" href="${ctx}/projects/board?id=${project.id}">Kanban board</a>
    <a class="btn btn-ghost" href="${ctx}/issues?project=${project.id}">All issues</a>
    <a class="btn btn-ghost" href="${ctx}/projects/members?id=${project.id}">Team</a>
    <a class="btn btn-ghost" href="${ctx}/analytics?project=${project.id}">Analytics</a>
    <c:if test="${isProjectAdmin}"><a class="btn btn-ghost" href="${ctx}/projects/edit?id=${project.id}">Edit</a></c:if>
    <c:if test="${canWrite and not project.archived}"><a class="btn btn-ghost" href="${ctx}/issues/new?projectId=${project.id}">+ Issue</a></c:if>
  </div>
</div>

<section class="stats">
  <div class="stat"><span>Total issues</span><strong>${stats.totalIssues}</strong></div>
  <div class="stat"><span>Open</span><strong>${stats.open}</strong></div>
  <div class="stat info"><span>In progress</span><strong>${stats.inProgress}</strong></div>
  <div class="stat info"><span>In review / testing</span><strong>${stats.inReview + stats.testing}</strong></div>
  <div class="stat ok"><span>Done</span><strong>${stats.done}</strong></div>
  <div class="stat warn"><span>Overdue</span><strong>${stats.overdue}</strong></div>
</section>

<div class="grid" style="grid-template-columns:minmax(0,2fr) minmax(0,1fr)">
  <div>
    <section class="card"><h2>About</h2>
      <c:choose>
        <c:when test="${not empty project.description}"><p class="prose"><c:out value="${project.description}"/></p></c:when>
        <c:otherwise><p class="muted">No description yet.</p></c:otherwise>
      </c:choose>
    </section>
    <section class="card"><h2>Recently updated <a class="small" href="${ctx}/issues?project=${project.id}">View all</a></h2>
      <c:choose>
        <c:when test="${empty recentIssues}"><div class="empty"><strong>No issues yet</strong>Create the first issue for this project.</div></c:when>
        <c:otherwise><c:set var="rows" value="${recentIssues}"/><%@ include file="/WEB-INF/views/_issue-table.jspf" %></c:otherwise>
      </c:choose>
    </section>
  </div>
  <section class="card"><h2>Team <a class="small" href="${ctx}/projects/members?id=${project.id}">Manage</a></h2>
    <c:forEach var="m" items="${members}">
      <div class="row-between" style="padding:.4rem 0">
        <span class="person"><span class="avatar sm"><c:out value="${m.initials}"/></span><c:out value="${m.fullName}"/></span>
        <span class="role role-${m.projectRole}">${roleLabels[m.projectRole]}</span>
      </div>
    </c:forEach>
  </section>
</div>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
