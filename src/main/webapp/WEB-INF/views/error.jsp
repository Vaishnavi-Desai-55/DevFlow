<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="home" value="${pageContext.request.contextPath}"/>
<c:set var="code" value="${not empty errorStatus ? errorStatus : requestScope['jakarta.servlet.error.status_code']}"/>
<c:if test="${empty code}"><c:set var="code" value="500"/></c:if>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Error · DevFlow</title>
  <link rel="stylesheet" href="${home}/assets/css/style.css">
</head>
<body>
  <main class="error-page">
    <div>
      <div class="code"><c:out value="${code}"/></div>
      <h1 style="margin:.6rem 0 .4rem">
        <c:choose>
          <c:when test="${code == 404}">We could not find that</c:when>
          <c:when test="${code == 403}">Access denied</c:when>
          <c:when test="${code == 401}">Please sign in</c:when>
          <c:otherwise>Something went wrong</c:otherwise>
        </c:choose>
      </h1>
      <p class="muted" style="max-width:440px;margin:0 auto 1.5rem">
        <c:choose>
          <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
          <c:when test="${code == 403 and not empty requestScope['jakarta.servlet.error.message']}"><c:out value="${requestScope['jakarta.servlet.error.message']}"/></c:when>
          <c:when test="${code == 404}">The page or item you asked for does not exist, or you do not have access to it.</c:when>
          <c:otherwise>An unexpected error occurred. It has been logged. Please try again.</c:otherwise>
        </c:choose>
      </p>
      <a class="btn" href="${home}/dashboard">Back to dashboard</a>
    </div>
  </main>
</body>
</html>
