<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="editing" value="${not empty project}"/>
<c:set var="pageTitle" value="${editing ? 'Edit project' : 'New project'}"/><c:set var="nav" value="projects"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="page-head"><div><h1>${editing ? 'Edit project' : 'Create a project'}</h1>
  <p>${editing ? 'Update the project details.' : 'The project key becomes the prefix of every issue, for example ECOM-101.'}</p></div></div>

<form class="card" method="post" action="${ctx}/projects/${editing ? 'update' : 'create'}" style="max-width:660px">
  <input type="hidden" name="_csrf" value="${csrfToken}">
  <c:if test="${editing}"><input type="hidden" name="id" value="${project.id}"></c:if>
  <div class="form-row">
    <div class="field"><label for="name">Project name</label>
      <input id="name" name="name" required maxlength="120" placeholder="E-Commerce Platform" value="<c:out value='${editing ? project.name : param.name}'/>"></div>
    <div class="field"><label for="key">Project key</label>
      <c:choose>
        <c:when test="${editing}"><input id="key" value="<c:out value='${project.key}'/>" disabled><div class="help">The key cannot be changed.</div></c:when>
        <c:otherwise><input id="key" name="key" required maxlength="10" pattern="[A-Za-z][A-Za-z0-9]{1,9}" placeholder="ECOM" style="text-transform:uppercase" value="<c:out value='${param.key}'/>">
          <div class="help">2 to 10 letters or digits, starting with a letter.</div></c:otherwise>
      </c:choose></div>
  </div>
  <div class="field"><label for="description">Description</label>
    <textarea id="description" name="description" maxlength="2000"><c:out value="${editing ? project.description : param.description}"/></textarea></div>
  <c:if test="${editing}">
    <div class="field"><label for="status">Status</label>
      <select id="status" name="status">
        <option value="ACTIVE" ${project.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
        <option value="ARCHIVED" ${project.status == 'ARCHIVED' ? 'selected' : ''}>Archived (no new issues)</option>
      </select></div>
  </c:if>
  <div class="actions">
    <button class="btn" type="submit">${editing ? 'Save changes' : 'Create project'}</button>
    <a class="btn btn-ghost" href="${ctx}/projects">Cancel</a>
  </div>
</form>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
