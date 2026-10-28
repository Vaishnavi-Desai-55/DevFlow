<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="New issue"/><c:set var="nav" value="issues"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="page-head"><div><h1>Create an issue</h1><p>Stories, tasks, bugs and subtasks all start here.</p></div></div>

<form class="card" method="get" action="${ctx}/issues/new" style="max-width:760px">
  <div class="field" style="margin:0"><label for="projectId">Project</label>
    <select id="projectId" name="projectId" data-autosubmit>
      <option value="">Choose a project...</option>
      <c:forEach var="p" items="${projects}"><option value="${p.id}" ${project.id == p.id ? 'selected' : ''}><c:out value="${p.key}"/> · <c:out value="${p.name}"/></option></c:forEach>
    </select></div>
  <noscript><button class="btn" type="submit">Select</button></noscript>
</form>

<c:choose>
  <c:when test="${empty project}">
    <div class="card empty" style="max-width:760px">
      <strong>Pick a project first</strong>
      <c:if test="${empty projects}">You are not a developer or admin in any active project yet.</c:if>
    </div>
  </c:when>
  <c:otherwise>
    <form class="card" method="post" action="${ctx}/issues/create" style="max-width:760px">
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <input type="hidden" name="projectId" value="${project.id}">
      <div class="field"><label for="title">Title</label>
        <input id="title" name="title" required minlength="3" maxlength="255" placeholder="Short summary of the work" value="<c:out value='${param.title}'/>"></div>
      <div class="form-row">
        <div class="field"><label for="issueType">Type</label>
          <select id="issueType" name="issueType">
            <c:forEach var="t" items="${issueTypes}"><option value="${t.name}" ${(empty param.issueType ? 'TASK' : param.issueType) == t.name ? 'selected' : ''}>${t.label}</option></c:forEach>
          </select></div>
        <div class="field"><label for="priority">Priority</label>
          <select id="priority" name="priority">
            <c:forEach var="p" items="${priorities}"><option value="${p.name}" ${(empty param.priority ? 'MEDIUM' : param.priority) == p.name ? 'selected' : ''}>${p.label}</option></c:forEach>
          </select></div>
        <div class="field"><label for="assigneeId">Assignee</label>
          <select id="assigneeId" name="assigneeId"><option value="">Unassigned</option>
            <c:forEach var="m" items="${assignees}"><option value="${m.userId}" ${param.assigneeId == m.userId ? 'selected' : ''}><c:out value="${m.fullName}"/></option></c:forEach>
          </select></div>
        <div class="field"><label for="dueDate">Due date</label>
          <input id="dueDate" type="date" name="dueDate" value="<c:out value='${param.dueDate}'/>"></div>
      </div>
      <div class="field" id="parentField"><label for="parentId">Parent issue (required for subtasks)</label>
        <select id="parentId" name="parentId"><option value="">Choose a parent...</option>
          <c:forEach var="p" items="${parents}"><option value="${p.id}" ${param.parentId == p.id ? 'selected' : ''}><c:out value="${p.issueKey}"/> · <c:out value="${p.title}"/></option></c:forEach>
        </select></div>
      <div class="field"><label for="description">Description</label>
        <textarea id="description" name="description" maxlength="5000" placeholder="What needs to happen? Steps to reproduce, acceptance criteria..."><c:out value="${param.description}"/></textarea></div>
      <div class="actions">
        <button class="btn" type="submit">Create issue</button>
        <a class="btn btn-ghost" href="${ctx}/projects/view?id=${project.id}">Cancel</a>
      </div>
    </form>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
