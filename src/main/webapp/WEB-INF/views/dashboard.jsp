<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Dashboard"/><c:set var="nav" value="dashboard"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="page-head">
  <div><h1>Dashboard</h1><p>Welcome back, <c:out value="${sessionScope.user.fullName}"/>. Here is where your projects stand.</p></div>
  <div class="actions"><a class="btn btn-ghost" href="${ctx}/projects">All projects</a></div>
</div>

<section class="stats">
  <div class="stat"><span>Projects</span><strong data-stat="totalProjects">${stats.totalProjects}</strong></div>
  <div class="stat"><span>Total issues</span><strong data-stat="totalIssues">${stats.totalIssues}</strong></div>
  <div class="stat"><span>Open</span><strong data-stat="open">${stats.open}</strong></div>
  <div class="stat info"><span>In progress</span><strong data-stat="inProgress">${stats.inProgress}</strong></div>
  <div class="stat info"><span>In review</span><strong data-stat="inReview">${stats.inReview}</strong></div>
  <div class="stat ok"><span>Completed</span><strong data-stat="done">${stats.done}</strong></div>
  <div class="stat warn"><span>Overdue</span><strong data-stat="overdue">${stats.overdue}</strong></div>
  <div class="stat"><span>Assigned to me</span><strong data-stat="assignedToMe">${stats.assignedToMe}</strong></div>
</section>

<section id="charts" class="grid grid-2" data-url="${ctx}/dashboard/data">
  <div class="card"><h2>Issues by status</h2><div class="chart-box"><canvas id="chartStatus"></canvas></div></div>
  <div class="card"><h2>Issues by priority</h2><div class="chart-box"><canvas id="chartPriority"></canvas></div></div>
  <div class="card"><h2>Issues by assignee</h2><div class="chart-box"><canvas id="chartAssignee"></canvas></div></div>
  <div class="card"><h2>Project completion</h2><div class="chart-box"><canvas id="chartProjects"></canvas></div></div>
</section>

<section class="card">
  <h2>My open issues <a class="small" href="${ctx}/issues?mine=1">View all</a></h2>
  <c:choose>
    <c:when test="${empty myIssues}">
      <div class="empty"><strong>Nothing assigned to you</strong>When someone assigns you an issue it will show up here.</div>
    </c:when>
    <c:otherwise>
      <c:set var="rows" value="${myIssues}"/>
      <%@ include file="/WEB-INF/views/_issue-table.jspf" %>
    </c:otherwise>
  </c:choose>
</section>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
<script src="${ctx}/assets/js/charts.js"></script>
</body></html>
