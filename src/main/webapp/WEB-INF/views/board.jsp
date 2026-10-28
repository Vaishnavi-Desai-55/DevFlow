<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="${project.name} board"/><c:set var="nav" value="projects"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="crumbs"><a href="${ctx}/projects">Projects</a> / <a href="${ctx}/projects/view?id=${project.id}"><c:out value="${project.key}"/></a> / Board</div>
<div class="page-head">
  <div><h1><c:out value="${project.name}"/> board</h1>
    <p><c:choose><c:when test="${canWrite}">Drag cards between columns. The server checks every move against the workflow.</c:when><c:otherwise>You have read-only access, so cards cannot be moved.</c:otherwise></c:choose></p></div>
  <div class="actions">
    <c:if test="${canWrite and not project.archived}"><a class="btn" href="${ctx}/issues/new?projectId=${project.id}">+ New issue</a></c:if>
    <a class="btn btn-ghost" href="${ctx}/projects/view?id=${project.id}">Project overview</a>
  </div>
</div>

<div class="toolbar">
  <input id="boardSearch" type="search" placeholder="Search this board">
  <select id="boardType"><option value="">All types</option><c:forEach var="t" items="${issueTypes}"><option value="${t.name}">${t.label}</option></c:forEach></select>
  <select id="boardPriority"><option value="">All priorities</option><c:forEach var="p" items="${priorities}"><option value="${p.name}">${p.label}</option></c:forEach></select>
  <select id="boardAssignee"><option value="">Everyone</option><c:forEach var="m" items="${members}"><option value="${m.userId}"><c:out value="${m.fullName}"/></option></c:forEach></select>
</div>

<div id="board" class="board" data-transition-url="${ctx}/workflow/transition" data-can-move="${canWrite}">
  <c:forEach var="col" items="${columns}">
    <section class="column" data-status="${col.key.name}">
      <header><h3>${col.key.label}</h3><span class="count">${fn:length(col.value)}</span></header>
      <div class="column-body">
        <c:forEach var="i" items="${col.value}">
          <article class="issue-card" draggable="${canWrite}" data-id="${i.id}" data-key="<c:out value='${i.issueKey}'/>" data-title="<c:out value='${i.title}'/>"
                   data-type="${i.issueType}" data-priority="${i.priority}" data-assignee="${i.assigneeId}">
            <div class="row-between"><a class="key" href="${ctx}/issues/view?id=${i.id}"><c:out value="${i.issueKey}"/></a>
              <span class="prio prio-${i.priority}">${priorityLabels[i.priority]}</span></div>
            <a class="ttl" href="${ctx}/issues/view?id=${i.id}"><c:out value="${i.title}"/></a>
            <div class="meta">
              <span class="tags"><span class="type type-${i.issueType}">${typeLabels[i.issueType]}</span>
                <c:if test="${i.dueDate != null}"><span class="small due ${i.overdue ? 'overdue' : 'muted'}"><fmt:formatDate value="${i.dueDate}" pattern="dd MMM"/></span></c:if></span>
              <c:choose>
                <c:when test="${not empty i.assigneeName}"><span class="avatar sm" title="<c:out value='${i.assigneeName}'/>"><c:out value="${fn:toUpperCase(fn:substring(i.assigneeName, 0, 1))}"/></span></c:when>
                <c:otherwise><span class="small muted">Unassigned</span></c:otherwise>
              </c:choose>
            </div>
          </article>
        </c:forEach>
      </div>
    </section>
  </c:forEach>
</div>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
<script src="${ctx}/assets/js/board.js"></script>
</body></html>
