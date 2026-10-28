<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Projects"/><c:set var="nav" value="projects"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="page-head">
  <div><h1>Projects</h1><p>Every project you can access.</p></div>
  <div class="actions">
    <c:if test="${sessionScope.user.globalAdmin}"><a class="btn" href="${ctx}/projects/new">+ New project</a></c:if>
  </div>
</div>

<c:choose>
  <c:when test="${empty projects}">
    <div class="card empty">
      <strong>No projects yet</strong>
      <c:choose>
        <c:when test="${sessionScope.user.globalAdmin}">Create your first project to start tracking issues.</c:when>
        <c:otherwise>Ask a project admin to add you to a project.</c:otherwise>
      </c:choose>
    </div>
  </c:when>
  <c:otherwise>
    <div class="grid grid-3">
      <c:forEach var="p" items="${projects}">
        <article class="card project-card">
          <div class="row-between">
            <span class="keychip"><c:out value="${p.key}"/></span>
            <span class="role role-${p.myRole}">${roleLabels[p.myRole]}</span>
          </div>
          <h3><a href="${ctx}/projects/view?id=${p.id}"><c:out value="${p.name}"/></a>
              <c:if test="${p.archived}"><span class="badge st-OPEN">Archived</span></c:if></h3>
          <p><c:out value="${p.description}"/></p>
          <div class="progress" title="${p.progress}% complete"><i style="width:${p.progress}%"></i></div>
          <div class="row-between small muted">
            <span>${p.doneCount} of ${p.issueCount} issues done</span><span>${p.memberCount} members</span>
          </div>
          <div class="actions">
            <a class="btn btn-sm btn-ghost" href="${ctx}/projects/board?id=${p.id}">Board</a>
            <a class="btn btn-sm btn-ghost" href="${ctx}/issues?project=${p.id}">Issues</a>
            <a class="btn btn-sm btn-ghost" href="${ctx}/analytics?project=${p.id}">Analytics</a>
          </div>
        </article>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
