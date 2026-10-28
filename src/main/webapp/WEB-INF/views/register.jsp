<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Create account · DevFlow</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${ctx}/assets/css/style.css">
</head>
<body class="auth">
  <section class="auth-hero">
    <a class="brand" href="${ctx}/login">Dev<span>Flow</span></a>
    <h2>Join your team on DevFlow.</h2>
    <ul>
      <li>Create an account in a minute</li>
      <li>Ask a project admin to add you to a project</li>
      <li>Developers update issues, viewers follow along</li>
    </ul>
  </section>
  <section class="auth-panel">
    <form class="auth-card" method="post" action="${ctx}/register">
      <h1>Create your account</h1>
      <p class="muted">The very first account becomes the workspace admin.</p>
      <c:if test="${not empty flash}"><div class="flash flash-${flashType}" role="alert"><c:out value="${flash}"/></div></c:if>
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <div class="field"><label for="fullName">Full name</label>
        <input id="fullName" name="fullName" required maxlength="100" value="<c:out value='${param.fullName}'/>"></div>
      <div class="field"><label for="email">Email</label>
        <input id="email" type="email" name="email" required maxlength="150" autocomplete="username" value="<c:out value='${param.email}'/>"></div>
      <div class="field"><label for="role">I will be working as</label>
        <select id="role" name="role">
          <option value="DEVELOPER" ${param.role != 'VIEWER' ? 'selected' : ''}>Developer (can create and update issues)</option>
          <option value="VIEWER" ${param.role == 'VIEWER' ? 'selected' : ''}>Viewer (read-only)</option>
        </select></div>
      <div class="form-row">
        <div class="field"><label for="password">Password</label>
          <input id="password" type="password" name="password" required minlength="8" autocomplete="new-password"></div>
        <div class="field"><label for="confirm">Confirm password</label>
          <input id="confirm" type="password" name="confirm" required minlength="8" autocomplete="new-password"></div>
      </div>
      <p class="help" style="margin:-.5rem 0 1rem">At least 8 characters, with a letter and a number.</p>
      <button class="btn btn-block" type="submit">Create account</button>
      <p class="muted small" style="margin-top:1rem">Already registered? <a href="${ctx}/login">Sign in</a></p>
    </form>
  </section>
</body>
</html>
