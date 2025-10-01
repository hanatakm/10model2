<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions"%>

<%-- 부분 응답 모드 여부 --%>
<c:set var="isPartial" value="${param.partial eq '1'}"/>

<c:if test="${not isPartial}">
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>구매 목록</title>
  <link rel="stylesheet" href="<c:url value='/css/admin.css'/>" type="text/css">
  <script src="http://code.jquery.com/jquery-2.1.4.min.js"></script>
</head>
<body>
<div class="wrap" id="listContainer">
</c:if>

  <%-- ========== 여기서부터가 조각(fragment) 영역 ========== --%>
  <div id="listInner">

    <%-- 모델 키 보정 --%>
    <c:if test="${empty list and not empty map}">
      <c:set var="list" value="${map.list}" scope="request"/>
      <c:set var="resultPage" value="${map.resultPage}" scope="request"/>
    </c:if>
    <c:if test="${empty list and not empty result}">
      <c:set var="list" value="${result.list}" scope="request"/>
      <c:set var="resultPage" value="${result.resultPage}" scope="request"/>
    </c:if>

    <%-- 페이징 공통 변수 --%>
    <c:set var="TOTAL" value="${
       not empty resultPage.totalCount ? resultPage.totalCount
     : (not empty map.totalCount     ? map.totalCount
     : (not empty param.totalCount   ? param.totalCount : 0))
    }"/>
    <c:set var="ps" value="${not empty resultPage.pageSize ? resultPage.pageSize
                           : (not empty param.pageSize ? param.pageSize : 10)}"/>
    <c:set var="cp" value="${not empty resultPage.currentPage ? resultPage.currentPage
                           : (not empty param.page ? param.page : 1)}"/>

    <%-- ===== 상단 요약 ===== --%>
    <div class="toolbar">
      전체 <strong>${TOTAL}</strong> 건, 현재 <strong>${cp}</strong> 페이지
    </div>

    <%-- ===== 목록 테이블 ===== --%>
    <table width="100%" border="0" cellspacing="0" cellpadding="0"margin-top:10px;>
      <tr class="ct_list_b">
        <td width="80"  align="center">No</td>
        <td class="ct_line02"></td>
        <td width="80" align="center">거래번호</td>
        <td class="ct_line02"></td>
        <td width="80" align="center">상품번호</td>
        <td class="ct_line02"></td>
         <td width="140" align="center">상품명</td>
        <td class="ct_line02"></td>
        
        <td width="50" align="center">결제수단</td>
        <td class="ct_line02"></td>
        <td width="120" align="center">상태</td>
        <td class="ct_line02"></td>
        <td width="140" align="center">주문일</td>
      </tr>
      <tr><td colspan="13" bgcolor="808285" height="1"></td></tr>

      <c:forEach var="row" items="${list}" varStatus="st">
        <c:set var="rowNo" value="${TOTAL - ((cp-1)*ps) - st.index}" />

        <%-- 코드 정규화 --%>
        <c:set var="codeRaw" value="${empty row.tranCode ? '' : fn:toUpperCase(fn:trim(row.tranCode))}"/>
        <c:set var="code"
               value="${ codeRaw=='1' or codeRaw=='001' or codeRaw=='ODR' or codeRaw=='ORD' ? 'ODR'
                      : codeRaw=='2' or codeRaw=='002' or codeRaw=='DLV'                  ? 'DLV'
                      : codeRaw=='3' or codeRaw=='003' or codeRaw=='CMP'                  ? 'CMP'
                      : '' }"/>
        <c:set var="payRaw" value="${empty row.paymentOption ? '' : fn:toUpperCase(fn:trim(row.paymentOption))}"/>
        <c:set var="payTxt"
               value="${ payRaw=='CSH' or payRaw=='CASH'                       ? '현금'
                      : payRaw=='CRD' or payRaw=='CARD' or payRaw=='CAR'       ? '카드'
                      : payRaw=='BNK' or payRaw=='BANK'                        ? '계좌이체'
                      : empty payRaw ? '-' : payRaw }"/>

        <%-- 상세 URL --%>

  <%-- 상세 URL --%>
  <c:url var="detailUrl" value="/purchase/getPurchase">
    <c:param name="tranNo" value="${row.tranNo}"/>
  </c:url>

  <tr>
    <!-- 여기! 페이지마다 1,2,3... -->
    <td align="center">${st.count}</td>
    <td class="ct_line02"></td>

    <td align="left"><a href="${detailUrl}">${row.tranNo}</a></td>
    <td class="ct_line02"></td>
    <td align="left">${row.purchaseProd.prodNo}</td>
    <td class="ct_line02"></td>
    <td align="left">${fn:escapeXml(row.purchaseProd.prodName)}</td>
    <td class="ct_line02"></td>
    <td align="left">${payTxt}</td>
    <td class="ct_line02"></td>
    <td align="center">
      <c:choose>
        <c:when test="${code=='ODR'}">구매완료</c:when>
        <c:when test="${code=='DLV'}">배송중</c:when>
        <c:when test="${code=='CMP'}">배송완료</c:when>
        <c:otherwise>-</c:otherwise>
      </c:choose>
    </td>


          <td class="ct_line02"></td>
          <td align="center">
            <c:choose>
              <c:when test="${not empty row.orderDate}">
                <fmt:formatDate value="${row.orderDate}" pattern="yyyy-MM-dd"/>
              </c:when>
              <c:otherwise>-</c:otherwise>
            </c:choose>
          </td>
        </tr>
        <tr><td colspan="13" bgcolor="D6D7D6" height="1"></td></tr>
</c:forEach>
      

      <c:if test="${empty list}">
        <tr><td colspan="13" align="center">데이터가 없습니다.</td></tr>
      </c:if>
    </table>

    <%-- ===== 페이징 ===== --%>
  <%-- ===== 페이징 값 보정(FALLBACK, 정수 처리) ===== --%>
<fmt:parseNumber var="pageUnitN" value="${empty resultPage.pageUnit ? 5 : resultPage.pageUnit}" integerOnly="true"/>
<fmt:parseNumber var="psN"       value="${empty resultPage.pageSize ? (empty param.pageSize ? 10 : param.pageSize) : resultPage.pageSize}" integerOnly="true"/>
<fmt:parseNumber var="cpN"       value="${empty resultPage.currentPage ? (empty param.page ? 1 : param.page) : resultPage.currentPage}" integerOnly="true"/>
<fmt:parseNumber var="totalN"    value="${TOTAL}" integerOnly="true"/>

<c:set var="maxP"   value="${ totalN lt 1 ? 1 : ((totalN-1) div psN) + 1 }"/>
<c:set var="beginP" value="${ ((cpN-1) div pageUnitN) * pageUnitN + 1 }"/>
<c:set var="endP"   value="${ beginP + pageUnitN - 1 }"/>
<c:if test="${endP > maxP}">
  <c:set var="endP" value="${maxP}"/>
</c:if>


    <div class="pager" style="text-align:center; margin-top:10px;">
      <c:if test="${beginP > 1}">
        <c:url var="prevUrl" value="/purchase/listPurchase">
          <c:param name="buyerId" value="${sessionScope.user.userId}"/>
          <c:param name="page" value="${beginP - 1}"/>
          <c:param name="pageSize" value="${ps}"/>
        </c:url>
        <a href="${prevUrl}">이전</a>
      </c:if>

      <c:forEach var="p" begin="${beginP}" end="${endP}">
        <c:choose>
          <c:when test="${p == cpN}">
            <strong>[${p}]</strong>
          </c:when>
          <c:otherwise>
            <c:url var="pageUrl" value="/purchase/listPurchase">
              <c:param name="buyerId" value="${sessionScope.user.userId}"/>
              <c:param name="page" value="${p}"/>
              <c:param name="pageSize" value="${ps}"/>
            </c:url>
            <a href="${pageUrl}">[${p}]</a>
          </c:otherwise>
        </c:choose>
      </c:forEach>

      <c:if test="${endP < maxP}">
        <c:url var="nextUrl" value="/purchase/listPurchase">
          <c:param name="buyerId" value="${sessionScope.user.userId}"/>
          <c:param name="page" value="${endP + 1}"/>
          <c:param name="pageSize" value="${ps}"/>
        </c:url>
        <a href="${nextUrl}">다음</a>
      </c:if>
    </div>

  </div>
  <%-- ========== fragment 끝 ========== --%>

<c:if test="${not isPartial}">
</div><!-- /.wrap -->

<script>
(function($){
  function withPartial(url){
    return url + (url.indexOf('?')>-1 ? '&' : '?') + 'partial=1';
  }
  function loadFragment(url){
    $('#listContainer').load(withPartial(url));
  }
  // 페이징 링크 가로채기
  $(document).on('click','.pager a', function(e){
    e.preventDefault();
    var url = $(this).attr('href');
    loadFragment(url);
    if(history && history.replaceState){
      history.replaceState(null,'', url);
    }
  });
})(jQuery);
</script>

</body>
</html>
</c:if>
