<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>구매 상세</title>
  <script src="http://code.jquery.com/jquery-2.1.4.min.js"></script>
  <style>
    .ui-card{max-width:880px;margin:24px auto;padding:20px;border-radius:16px;box-shadow:0 8px 24px rgba(0,0,0,.08);background:rgba(255,255,255,.75)}
    .row{display:grid;grid-template-columns:160px 1fr;gap:10px;align-items:center;margin:8px 0}
    .ui-actions{display:flex;gap:8px;justify-content:flex-end;margin-top:16px}
    .ui-btn{padding:8px 14px;border-radius:10px;border:1px solid #ddd;background:#fff;cursor:pointer}
    .ui-btn.accent{background:#DF8F6B;color:#fff;border-color:#DF8F6B}
    .muted{color:#777}
  </style>
</head>
<body>

<c:set var="purchase" value="${requestScope.purchase != null ? requestScope.purchase : purchase}"/>

<div id="purchaseView" data-tran-no="${empty purchase ? '' : purchase.tranNo}">
  <div class="ui-card">
    <h2>구매 상세</h2>

    <c:choose>
      <c:when test="${empty purchase}">
        <p class="muted">구매 내역을 찾을 수 없습니다.</p>
        <div class="ui-actions">
          <a class="ui-btn" href="<c:url value='/purchase/listPurchase'/>">구매목록</a>
        </div>
      </c:when>

      <c:otherwise>
        <div class="row"><label>거래번호</label><div>${purchase.tranNo}</div></div>
        <div class="row"><label>상품번호</label><div>${purchase.purchaseProd.prodNo}</div></div>
        <div class="row"><label>상품명</label>
          <div>${empty purchase.purchaseProd.prodName ? '-' : purchase.purchaseProd.prodName}</div>
        </div>

        <!-- 결제수단: 코드 → 한글 라벨 -->
        <div class="row"><label>결제수단</label>
          <div>
            <c:choose>
              <c:when test="${purchase.paymentOption == 'CSH'}">현금</c:when>
              <c:when test="${purchase.paymentOption == 'CRD'}">카드</c:when>
              <c:when test="${purchase.paymentOption == 'BNK'}">계좌이체</c:when>
              <c:otherwise>${empty purchase.paymentOption ? '-' : purchase.paymentOption}</c:otherwise>
            </c:choose>
          </div>
        </div>

        <!-- 상태코드 행은 요청대로 제거 -->

        <!-- 날짜: 원본 값 표시 + data-date로 화면 정규화(YYYYMMDD) -->
        <div class="row"><label>주문일자</label><div><span data-date>${purchase.orderDate}</span></div></div>
        <div class="row"><label>배송요청일</label><div><span data-date>${purchase.divyDate}</span></div></div>

        <div class="row"><label>수령인</label><div>${empty purchase.receiverName ? '-' : purchase.receiverName}</div></div>

        <!-- 연락처: 화면에서 하이픈 자동삽입 -->
        <div class="row"><label>수령 연락처</label>
          <div><span data-phone>${empty purchase.receiverPhone ? '' : purchase.receiverPhone}</span></div>
        </div>

        <div class="row"><label>배송지</label><div>${empty purchase.divyAddr ? '-' : purchase.divyAddr}</div></div>
        <div class="row"><label>요청사항</label><div>${empty purchase.divyRequest ? '-' : purchase.divyRequest}</div></div>

        <div class="ui-actions">
          <a class="ui-btn" href="<c:url value='/purchase/listPurchase'/>">구매목록</a>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<script>
(function (w, $) {
  "use strict";

  // YYYYMMDD 또는 여러 형식 → YYYYMMDD
  function toYYYYMMDD(v){
    if(!v) return "";
    var s = String(v).replace(/[^0-9]/g,"");
    return s.length>=8 ? s.slice(0,8) : s;
  }

  // 01012345678 / 010-1234-5678 → 010-1234-5678
  function toPhone(v){
    if(!v) return "-";
    var d = String(v).replace(/[^0-9]/g,"");
    if(d.length===11) return d.replace(/(\d{3})(\d{4})(\d{4})/,"$1-$2-$3");
    if(d.length===10) return d.replace(/(\d{2,3})(\d{3,4})(\d{4})/,"$1-$2-$3");
    return v; // 길이가 애매하면 원본 유지
  }

  $(function(){
    var $root = $("#purchaseView");

    // 날짜 통일
    $root.find("[data-date]").each(function(){
      var t = $.trim($(this).text());
      $(this).text(toYYYYMMDD(t) || "-");
    });

    // 전화번호 통일
    $root.find("[data-phone]").each(function(){
      var t = $.trim($(this).text());
      $(this).text(toPhone(t));
    });
  });
})(window, jQuery);
</script>
</body>
</html>
