<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Sign in · DevFlow</title>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${ctx}/assets/css/style.css">
</head>
<body class="auth">
  <section class="auth-hero">
    <a class="brand" href="${ctx}/login">Dev<span>Flow</span></a>
    <h2>Plan, track and ship software with your team.</h2>
    <ul>
      <li>Kanban boards with a validated workflow</li>
      <li>Issues, comments and a full audit history</li>
      <li>Live analytics for every project</li>
    </ul>
  </section>
  <section class="auth-panel">
    <form class="auth-card" method="post" action="${ctx}/login">
      <h1>Welcome back</h1>
      <p class="muted">Sign in to continue to DevFlow.</p>
      <c:if test="${not empty flash}"><div class="flash flash-${flashType}" role="alert"><c:out value="${flash}"/></div></c:if>
      <input type="hidden" name="_csrf" value="${csrfToken}">
      <div class="field"><label for="email">Email</label>
        <input id="email" type="email" name="email" required autofocus autocomplete="username" value="<c:out value='${param.email}'/>"></div>
      <div class="field"><label for="password">Password</label>
        <input id="password" type="password" name="password" required autocomplete="current-password"></div>
      <button class="btn btn-block" type="submit">Sign in</button>
      <p class="muted small" style="margin-top:1rem">New here? <a href="${ctx}/register">Create an account</a></p>
    </form>
  </section>
</body>
</html>
