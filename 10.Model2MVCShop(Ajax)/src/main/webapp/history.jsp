<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>열어본 상품 보기</title>
  <style>
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif}
    h3{margin:12px 0 8px}
    a{display:block;margin:4px 0;text-decoration:none}
    a:hover{text-decoration:underline}
    .muted{opacity:.7}
  </style>
</head>
<body>
  <h3>당신이 열어본 상품</h3>

  <c:set var="hist" value="${sessionScope.historyList}" />
  <c:choose>
    <c:when test="${empty hist}">
      <p class="muted">기록이 없습니다.</p>
    </c:when>
    <c:otherwise>
      <c:forEach var="prodNo" items="${hist}">
        <a href="<c:url value='/product/getProduct?prodNo=${prodNo}&menu=search'/>" target="rightFrame">
          상품번호: ${prodNo}
        </a>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</body>
</html>
