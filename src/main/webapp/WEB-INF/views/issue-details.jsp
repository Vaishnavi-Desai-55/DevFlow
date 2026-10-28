<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="i" value="${d.issue}"/>
<c:set var="rec" value="${d.recommendation}"/>
<c:set var="pageTitle" value="${i.issueKey}"/><c:set var="nav" value="projects"/>
<%@ include file="/WEB-INF/views/_top.jspf" %>

<div class="crumbs"><a href="${ctx}/projects">Projects</a> / <a href="${ctx}/projects/view?id=${i.projectId}"><c:out value="${i.projectKey}"/></a> /
  <a href="${ctx}/projects/board?id=${i.projectId}">Board</a> / <c:out value="${i.issueKey}"/></div>

<div class="page-head">
  <div>
    <div class="actions" style="margin-bottom:.4rem">
      <span class="type type-${i.issueType}">${typeLabels[i.issueType]}</span>
      <span class="key"><c:out value="${i.issueKey}"/></span>
      <span class="badge st-${i.status}">${statusLabels[i.status]}</span>
      <span class="prio prio-${i.priority}">${priorityLabels[i.priority]}</span>
      <c:if test="${i.overdue}"><span class="due overdue">Overdue</span></c:if>
    </div>
    <h1><c:out value="${i.title}"/></h1>
    <c:if test="${not empty i.parentKey}"><p>Subtask of <a href="${ctx}/issues/view?id=${i.parentIssueId}"><c:out value="${i.parentKey}"/></a></p></c:if>
  </div>
  <div class="actions">
    <c:forEach var="s" items="${d.nextStatuses}">
      <form method="post" action="${ctx}/workflow/transition">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="issueId" value="${i.id}">
        <input type="hidden" name="toStatus" value="${s.name}">
        <button class="btn btn-sm btn-ghost" type="submit">Move to ${s.label}</button>
      </form>
    </c:forEach>
    <c:if test="${d.canDelete}">
      <form method="post" action="${ctx}/issues/delete" data-confirm="Delete ${i.issueKey}? This also removes its comments and history.">
        <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="id" value="${i.id}">
        <button class="btn btn-sm btn-danger" type="submit">Delete</button>
      </form>
    </c:if>
  </div>
</div>

<div class="detail">
  <div>
    <section class="card"><h2>Description</h2>
      <c:choose>
        <c:when test="${not empty i.description}"><p class="prose"><c:out value="${i.description}"/></p></c:when>
        <c:otherwise><p class="muted">No description provided.</p></c:otherwise>
      </c:choose>
    </section>

    <c:if test="${rec != null}">
      <section class="card suggest" id="suggestBox">
        <h2>Smart priority check
          <span class="small muted">score ${rec.score}</span></h2>
        <p>Suggested priority: <span class="prio prio-${rec.suggestedPriority}">${priorityLabels[rec.suggestedPriority]}</span>
          <c:choose>
            <c:when test="${rec.suggestedPriority == i.priority}"> (matches the current priority)</c:when>
            <c:otherwise> (current: ${priorityLabels[i.priority]})</c:otherwise>
          </c:choose></p>
        <ul><c:forEach var="r" items="${rec.reasons}"><li><c:out value="${r}"/></li></c:forEach></ul>
        <c:if test="${d.canWrite and rec.suggestedPriority != i.priority}">
          <div class="actions">
            <form method="post" action="${ctx}/issues/priority">
              <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="id" value="${i.id}">
              <input type="hidden" name="priority" value="${rec.suggestedPriority}">
              <button class="btn btn-sm" type="submit">Accept suggestion</button>
            </form>
            <button class="btn btn-sm btn-ghost" type="button" data-hide="#suggestBox">Ignore</button>
          </div>
        </c:if>
        <p class="help">Your own priority is never changed automatically.</p>
      </section>
    </c:if>

    <c:if test="${not empty d.subtasks}">
      <section class="card"><h2>Subtasks</h2>
        <ul class="linklist"><c:forEach var="s" items="${d.subtasks}">
          <li><a class="key" href="${ctx}/issues/view?id=${s.id}"><c:out value="${s.issueKey}"/></a><span><c:out value="${s.title}"/></span>
              <span class="badge st-${s.status}">${statusLabels[s.status]}</span></li></c:forEach></ul>
      </section>
    </c:if>

    <section class="card"><h2>Blocking links</h2>
      <c:if test="${empty d.blocks and empty d.blockedBy}"><p class="muted">This issue does not block, and is not blocked by, anything.</p></c:if>
      <ul class="linklist">
        <c:forEach var="b" items="${d.blocks}"><li>Blocks <a class="key" href="${ctx}/issues/view?id=${b.id}"><c:out value="${b.issueKey}"/></a> <c:out value="${b.title}"/> <span class="badge st-${b.status}">${statusLabels[b.status]}</span></li></c:forEach>
        <c:forEach var="b" items="${d.blockedBy}"><li>Blocked by <a class="key" href="${ctx}/issues/view?id=${b.id}"><c:out value="${b.issueKey}"/></a> <c:out value="${b.title}"/> <span class="badge st-${b.status}">${statusLabels[b.status]}</span></li></c:forEach>
      </ul>
      <c:if test="${d.canWrite}">
        <form class="inline-form" method="post" action="${ctx}/issues/link" style="margin-top:.7rem">
          <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="id" value="${i.id}">
          <input name="blockedKey" placeholder="This blocks issue key, e.g. ${i.projectKey}-102" required maxlength="20">
          <button class="btn btn-sm btn-ghost" type="submit">Add link</button>
        </form>
      </c:if>
    </section>

    <section class="card" id="comments"><h2>Comments (${fn:length(d.comments)})</h2>
      <c:if test="${empty d.comments}"><p class="muted">No comments yet.</p></c:if>
      <c:forEach var="c" items="${d.comments}">
        <div class="comment">
          <span class="avatar"><c:out value="${fn:toUpperCase(fn:substring(c.authorName, 0, 1))}"/></span>
          <div class="body">
            <header><strong><c:out value="${c.authorName}"/></strong>
              <span class="small muted"><fmt:formatDate value="${c.createdAt}" pattern="dd MMM yyyy, HH:mm"/><c:if test="${c.edited}"> · edited</c:if></span></header>
            <p class="prose"><c:out value="${c.text}"/></p>
            <c:if test="${d.canWrite}">
              <div class="actions small">
                <c:if test="${c.userId == sessionScope.user.id}">
                  <details><summary>Edit</summary>
                    <form method="post" action="${ctx}/comments/edit">
                      <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="commentId" value="${c.id}"><input type="hidden" name="issueId" value="${i.id}">
                      <textarea name="text" required maxlength="2000"><c:out value="${c.text}"/></textarea>
                      <button class="btn btn-sm" type="submit" style="margin-top:.4rem">Save</button>
                    </form></details>
                </c:if>
                <c:if test="${c.userId == sessionScope.user.id or d.myRole == 'ADMIN'}">
                  <form method="post" action="${ctx}/comments/delete" data-confirm="Delete this comment?">
                    <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="commentId" value="${c.id}"><input type="hidden" name="issueId" value="${i.id}">
                    <button class="link-btn" type="submit">Delete</button>
                  </form>
                </c:if>
              </div>
            </c:if>
          </div>
        </div>
      </c:forEach>
      <c:if test="${d.canWrite}">
        <form method="post" action="${ctx}/comments/add" style="margin-top:1rem">
          <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="issueId" value="${i.id}">
          <div class="field"><label for="text">Add a comment</label><textarea id="text" name="text" required maxlength="2000" placeholder="Share an update or ask a question"></textarea></div>
          <button class="btn" type="submit">Comment</button>
        </form>
      </c:if>
    </section>

    <section class="card" id="history"><h2>Activity history</h2>
      <ul class="timeline">
        <c:forEach var="h" items="${d.history}">
          <li>
            <strong><c:out value="${h.userName}"/></strong> ${h.actionText}
            <c:if test="${not empty h.newValue or not empty h.oldValue}">
              <span class="chg">
                <c:if test="${not empty h.oldValue}"><del><c:out value="${fn:replace(h.oldValue, '_', ' ')}"/></del> &rarr; </c:if>
                <c:if test="${not empty h.newValue}"><ins><c:out value="${fn:replace(h.newValue, '_', ' ')}"/></ins></c:if>
              </span>
            </c:if>
            <div class="small muted"><fmt:formatDate value="${h.createdAt}" pattern="dd MMM yyyy, HH:mm:ss"/></div>
          </li>
        </c:forEach>
      </ul>
    </section>
  </div>

  <aside>
    <section class="card"><h2>Details</h2>
      <dl class="props">
        <dt>Status</dt><dd><span class="badge st-${i.status}">${statusLabels[i.status]}</span></dd>
        <dt>Priority</dt><dd><span class="prio prio-${i.priority}">${priorityLabels[i.priority]}</span></dd>
        <dt>Assignee</dt><dd><c:choose><c:when test="${not empty i.assigneeName}"><c:out value="${i.assigneeName}"/></c:when><c:otherwise><span class="muted">Unassigned</span></c:otherwise></c:choose></dd>
        <dt>Reporter</dt><dd><c:out value="${i.reporterName}"/></dd>
        <dt>Due date</dt><dd><c:choose><c:when test="${i.dueDate != null}"><span class="due ${i.overdue ? 'overdue' : ''}"><fmt:formatDate value="${i.dueDate}" pattern="dd MMM yyyy"/></span></c:when><c:otherwise><span class="muted">None</span></c:otherwise></c:choose></dd>
        <dt>Created</dt><dd><fmt:formatDate value="${i.createdAt}" pattern="dd MMM yyyy, HH:mm"/></dd>
        <dt>Updated</dt><dd><fmt:formatDate value="${i.updatedAt}" pattern="dd MMM yyyy, HH:mm"/></dd>
      </dl>
    </section>

    <c:if test="${d.canEdit}">
      <section class="card"><details><summary><strong>Edit issue</strong></summary>
        <form method="post" action="${ctx}/issues/update">
          <input type="hidden" name="_csrf" value="${csrfToken}"><input type="hidden" name="id" value="${i.id}">
          <div class="field"><label for="etitle">Title</label><input id="etitle" name="title" required minlength="3" maxlength="255" value="<c:out value='${i.title}'/>"></div>
          <div class="field"><label for="edesc">Description</label><textarea id="edesc" name="description" maxlength="5000"><c:out value="${i.description}"/></textarea></div>
          <div class="field"><label for="eprio">Priority</label>
            <select id="eprio" name="priority"><c:forEach var="p" items="${priorities}"><option value="${p.name}" ${i.priority == p.name ? 'selected' : ''}>${p.label}</option></c:forEach></select></div>
          <div class="field"><label for="eassignee">Assignee</label>
            <select id="eassignee" name="assigneeId"><option value="">Unassigned</option>
              <c:forEach var="m" items="${d.assignees}"><option value="${m.userId}" ${i.assigneeId == m.userId ? 'selected' : ''}><c:out value="${m.fullName}"/></option></c:forEach></select></div>
          <div class="field"><label for="edue">Due date</label><input id="edue" type="date" name="dueDate" value="<fmt:formatDate value='${i.dueDate}' pattern='yyyy-MM-dd'/>"></div>
          <button class="btn btn-block" type="submit">Save changes</button>
        </form></details>
      </section>
    </c:if>
  </aside>
</div>

<%@ include file="/WEB-INF/views/_bottom.jspf" %>
</body></html>
