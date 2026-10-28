<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Team members"/><c:set var="nav" value="projects"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="crumbs"><a href="${ctx}/projects">Projects</a> / <a href="${ctx}/projects/view?id=${project.id}"><c:out value="${project.key}"/></a> / Team</div>
<div class="page-head"><div><h1>Team members</h1><p><c:out value="${project.name}"/> has ${fn:length(members)} member${fn:length(members) == 1 ? '' : 's'}.</p></div></div>

<section class="card">
  <div class="table-wrap"><table class="table">
    <thead><tr><th>Member</th><th>Email</th><th>Project role</th><th>Joined</th><c:if test="${isProjectAdmin}"><th></th></c:if></tr></thead>
    <tbody>
    <c:forEach var="m" items="${members}">
      <tr>
        <td><span class="person"><span class="avatar"><c:out value="${m.initials}"/></span><c:out value="${m.fullName}"/></span></td>
        <td><c:out value="${m.email}"/></td>
        <td>
          <c:choose>
            <c:when test="${isProjectAdmin}">
              <form method="post" action="${ctx}/projects/members/role">
                <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="projectId" value="${project.id}"><input type="hidden" name="userId" value="${m.userId}">
                <select name="role" data-autosubmit style="width:auto">
                  <c:forEach var="r" items="${roles}"><option value="${r.name}" ${m.projectRole == r.name ? 'selected' : ''}>${r.label}</option></c:forEach>
                </select>
              </form>
            </c:when>
            <c:otherwise><span class="role role-${m.projectRole}">${roleLabels[m.projectRole]}</span></c:otherwise>
          </c:choose>
        </td>
        <td><fmt:formatDate value="${m.joinedAt}" pattern="dd MMM yyyy"/></td>
        <c:if test="${isProjectAdmin}">
          <td class="right">
            <form method="post" action="${ctx}/projects/members/remove" data-confirm="Remove this member? Their issues will become unassigned.">
              <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="projectId" value="${project.id}"><input type="hidden" name="userId" value="${m.userId}">
              <button class="btn btn-sm btn-danger" type="submit">Remove</button>
            </form>
          </td>
        </c:if>
      </tr>
    </c:forEach>
    </tbody>
  </table></div>
</section>

<c:if test="${isProjectAdmin}">
  <section class="card" style="max-width:640px"><h2>Add a member</h2>
    <form class="inline-form" method="post" action="${ctx}/projects/members/add">
      <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="projectId" value="${project.id}">
      <input type="email" name="email" required placeholder="Registered user's email">
      <select name="role" style="max-width:150px"><c:forEach var="r" items="${roles}"><option value="${r.name}" ${r.name == 'DEVELOPER' ? 'selected' : ''}>${r.label}</option></c:forEach></select>
      <button class="btn" type="submit">Add</button>
    </form>
    <p class="help">The person must already have a DevFlow account.</p>
  </section>
</c:if>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
