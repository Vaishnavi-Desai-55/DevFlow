<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${mine ? 'My issues' : 'Issues'}"/><c:set var="nav" value="${mine ? 'mine' : 'issues'}"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="page-head">
  <div><h1>${mine ? 'My issues' : 'Issues'}</h1><p>${result.total} issue${result.total == 1 ? '' : 's'} found</p></div>
  <div class="actions"><a class="btn" href="${ctx}/issues/new<c:if test='${filter.projectId != null}'>?projectId=${filter.projectId}</c:if>">+ New issue</a></div>
</div>

<form class="card" method="get" action="${ctx}/issues">
  <div class="form-row">
    <div><label for="q">Search</label><input id="q" type="search" name="q" placeholder="Key, title or description" value="<c:out value='${filter.q}'/>"></div>
    <div><label for="project">Project</label>
      <select id="project" name="project">
        <option value="">All projects</option>
        <c:forEach var="p" items="${projects}"><option value="${p.id}" ${filter.projectId == p.id ? 'selected' : ''}><c:out value="${p.key}"/> · <c:out value="${p.name}"/></option></c:forEach>
      </select></div>
    <div><label for="type">Type</label>
      <select id="type" name="type"><option value="">Any type</option>
        <c:forEach var="t" items="${issueTypes}"><option value="${t.name}" ${filter.type == t.name ? 'selected' : ''}>${t.label}</option></c:forEach></select></div>
    <div><label for="status">Status</label>
      <select id="status" name="status"><option value="">Any status</option>
        <c:forEach var="s" items="${statuses}"><option value="${s.name}" ${filter.status == s.name ? 'selected' : ''}>${s.label}</option></c:forEach></select></div>
    <div><label for="priority">Priority</label>
      <select id="priority" name="priority"><option value="">Any priority</option>
        <c:forEach var="p" items="${priorities}"><option value="${p.name}" ${filter.priority == p.name ? 'selected' : ''}>${p.label}</option></c:forEach></select></div>
    <c:if test="${not empty members and not mine}">
      <div><label for="assignee">Assignee</label>
        <select id="assignee" name="assignee"><option value="">Anyone</option>
          <c:forEach var="m" items="${members}"><option value="${m.userId}" ${filter.assigneeId == m.userId ? 'selected' : ''}><c:out value="${m.fullName}"/></option></c:forEach></select></div>
    </c:if>
    <div><label for="sort">Sort by</label>
      <select id="sort" name="sort">
        <option value="created" ${filter.sort == 'created' ? 'selected' : ''}>Created date</option>
        <option value="updated" ${filter.sort == 'updated' ? 'selected' : ''}>Updated date</option>
        <option value="due" ${filter.sort == 'due' ? 'selected' : ''}>Due date</option>
        <option value="priority" ${filter.sort == 'priority' ? 'selected' : ''}>Priority</option></select></div>
    <div><label for="dir">Order</label>
      <select id="dir" name="dir">
        <option value="desc" ${filter.dir == 'desc' ? 'selected' : ''}>Newest / highest first</option>
        <option value="asc" ${filter.dir == 'asc' ? 'selected' : ''}>Oldest / lowest first</option></select></div>
  </div>
  <div class="actions" style="margin-top:1rem">
    <label style="display:flex;gap:.4rem;align-items:center;margin:0;font-weight:500"><input type="checkbox" name="mine" value="1" style="width:auto" ${mine ? 'checked' : ''}> Assigned to me</label>
    <span class="spacer" style="flex:1"></span>
    <a class="btn btn-ghost" href="${ctx}/issues">Reset</a>
    <button class="btn" type="submit">Apply filters</button>
  </div>
</form>

<section class="card">
  <c:choose>
    <c:when test="${empty result.items}">
      <div class="empty"><strong>No issues match</strong>Try removing a filter or searching for something else.</div>
    </c:when>
    <c:otherwise>
      <c:set var="rows" value="${result.items}"/>
      <%@ include file="/WEB-INF/views/_issue-table.jspf" %>
      <div class="pager">
        <span>Page ${result.page} of ${result.totalPages}</span>
        <span class="actions">
          <c:if test="${result.hasPrev}"><a class="btn btn-ghost btn-sm" href="${ctx}/issues?<c:out value='${qs}'/>&amp;page=${result.page - 1}">Previous</a></c:if>
          <c:if test="${result.hasNext}"><a class="btn btn-ghost btn-sm" href="${ctx}/issues?<c:out value='${qs}'/>&amp;page=${result.page + 1}">Next</a></c:if>
        </span>
      </div>
    </c:otherwise>
  </c:choose>
</section>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
