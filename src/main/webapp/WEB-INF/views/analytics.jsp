<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Analytics"/><c:set var="nav" value="projects"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="crumbs"><a href="${ctx}/projects">Projects</a> / <a href="${ctx}/projects/view?id=${project.id}"><c:out value="${project.key}"/></a> / Analytics</div>
<div class="page-head"><div><h1>Analytics</h1><p><c:out value="${project.name}"/> · charts refresh automatically.</p></div>
  <div class="actions"><a class="btn btn-ghost" href="${ctx}/projects/board?id=${project.id}">Kanban board</a></div></div>

<section class="stats">
  <div class="stat"><span>Total issues</span><strong data-stat="totalIssues">${stats.totalIssues}</strong></div>
  <div class="stat"><span>Open</span><strong data-stat="open">${stats.open}</strong></div>
  <div class="stat info"><span>In progress</span><strong data-stat="inProgress">${stats.inProgress}</strong></div>
  <div class="stat info"><span>In review</span><strong data-stat="inReview">${stats.inReview}</strong></div>
  <div class="stat ok"><span>Completed</span><strong data-stat="done">${stats.done}</strong></div>
  <div class="stat warn"><span>Overdue</span><strong data-stat="overdue">${stats.overdue}</strong></div>
</section>

<section id="charts" class="grid grid-2" data-url="${ctx}/analytics/data?project=${project.id}">
  <div class="card"><h2>Issues by status</h2><div class="chart-box"><canvas id="chartStatus"></canvas></div></div>
  <div class="card"><h2>Issues by priority</h2><div class="chart-box"><canvas id="chartPriority"></canvas></div></div>
  <div class="card"><h2>Workload by assignee</h2><div class="chart-box"><canvas id="chartAssignee"></canvas></div></div>
  <div class="card"><h2>Completion</h2><div class="chart-box"><canvas id="chartProjects"></canvas></div></div>
</section>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
<script src="${ctx}/assets/js/charts.js"></script>
</body></html>
