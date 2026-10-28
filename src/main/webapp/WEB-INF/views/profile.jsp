<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Profile"/><c:set var="nav" value="profile"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="page-head"><div><h1>Your profile</h1></div></div>
<section class="card" style="display:flex;gap:1.2rem;align-items:center;flex-wrap:wrap">
  <span class="avatar" style="width:64px;height:64px;font-size:1.3rem"><c:out value="${sessionScope.user.initials}"/></span>
  <div>
    <h2 style="margin:0"><c:out value="${sessionScope.user.fullName}"/></h2>
    <div class="muted"><c:out value="${sessionScope.user.email}"/></div>
    <div style="margin-top:.4rem"><span class="role role-${sessionScope.user.role}">${roleLabels[sessionScope.user.role]}</span>
      <span class="small muted">· member since <fmt:formatDate value="${sessionScope.user.createdAt}" pattern="dd MMM yyyy"/></span></div>
  </div>
</section>

<section class="stats">
  <div class="stat"><span>Projects</span><strong>${stats.totalProjects}</strong></div>
  <div class="stat"><span>Assigned to me</span><strong>${stats.assignedToMe}</strong></div>
  <div class="stat warn"><span>Overdue (all)</span><strong>${stats.overdue}</strong></div>
</section>

<section class="card"><h2>Your projects and roles</h2>
  <c:choose>
    <c:when test="${empty projects}"><div class="empty"><strong>No projects yet</strong>Ask a project admin to add you.</div></c:when>
    <c:otherwise>
      <div class="table-wrap"><table class="table"><thead><tr><th>Project</th><th>Key</th><th>Your role</th><th></th></tr></thead><tbody>
        <c:forEach var="p" items="${projects}"><tr>
          <td><c:out value="${p.name}"/></td><td><span class="keychip"><c:out value="${p.key}"/></span></td>
          <td><span class="role role-${p.myRole}">${roleLabels[p.myRole]}</span></td>
          <td class="right"><a href="${ctx}/projects/view?id=${p.id}">Open</a></td></tr></c:forEach>
      </tbody></table></div>
    </c:otherwise>
  </c:choose>
</section>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
