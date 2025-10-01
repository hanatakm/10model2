<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>판매 목록</title>
  <script src="<c:url value='/javascript/jquery-2.1.4.js'/>"></script>
  <style>
    .wrap{max-width:1080px;margin:24px auto}
    table{width:100%;border-collapse:collapse;background:#fff}
    th,td{padding:10px;border-bottom:1px solid #e5e5e5}
    th{background:#fafafa;text-align:left}
    .badge{display:inline-block;padding:2px 8px;border-radius:999px;border:1px solid #ddd}
    .small{font-size:12px;color:#777}
  </style>
</head>
<body>
<div class="wrap">

  <div style="margin:12px 0;">
    전체 ${resultPage.totalCount} 건, 현재 ${resultPage.currentPage} 페이지
  </div>

  <table id="saleTable">
    <thead>
      <tr>
        <th width="80">No</th>
        <th width="120">거래번호</th>
        <th width="100">상품번호</th>
        <th>상품명</th>
        <th width="120">상태</th>
        <th width="160">상태변경</th>
        <th width="140">주문일</th>
        <th width="120">상세</th>
      </tr>
    </thead>
    <tbody>
      <c:set var="i" value="0"/>
      <c:forEach var="row" items="${list}">
        <c:set var="i" value="${i+1}"/>
        <tr data-tran="${row.tranNo}">
          <td align="center">${i}</td>
          <td>${row.tranNo}</td>
          <td>${row.purchaseProd.prodNo}</td>
          <td>${row.purchaseProd.prodName}</td>
          <td><span class="badge">${row.tranCode}</span></td>
          <td>
            <select class="to-code">
              <option value="ODR" ${row.tranCode=='ODR'?'selected':''}>ODR</option>
              <option value="DLV" ${row.tranCode=='DLV'?'selected':''}>DLV</option>
              <option value="CMP" ${row.tranCode=='CMP'?'selected':''}>CMP</option>
            </select>
            <button class="apply">적용</button>
            <div class="small result-msg"></div>
          </td>
          <td><fmt:formatDate value="${row.orderDate}" pattern="yyyy-MM-dd"/></td>
          <td>
            <a href="<c:url value='/purchase/getPurchase?tranNo='/><c:out value='${row.tranNo}'/>">보기</a>
          </td>
        </tr>
      </c:forEach>

      <c:if test="${empty list}">
        <tr><td colspan="8" align="center">데이터가 없습니다.</td></tr>
      </c:if>
    </tbody>
  </table>

  <!-- 페이징 -->
  <div style="display:flex;gap:6px;justify-content:center;margin:12px 0">
    <c:forEach var="p" begin="${resultPage.beginUnitPage}" end="${resultPage.endUnitPage}">
      <c:choose>
        <c:when test="${p == resultPage.currentPage}">
          <span><b>${p}</b></span>
        </c:when>
        <c:otherwise>
          <a href="<c:url value='/purchase/listSale?page='/><c:out value='${p}'/>&pageSize=<c:out value='${param.pageSize != null ? param.pageSize : 10}'/>">${p}</a>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </div>

</div>

<script>
$(function(){
  // 상태 변경(이벤트 위임)
  $('#saleTable').on('click', '.apply', function(){
    var $tr = $(this).closest('tr');
    var tranNo = $tr.data('tran');
    var toCode = $tr.find('.to-code').val();
    var $msg = $tr.find('.result-msg');

    $.ajax({
      url: "<c:url value='/purchase/updateTranCode'/>",
      type: "POST",
      data: { tranNo: tranNo, tranCode: toCode, ajax: "Y" }
    }).done(function(){
      $msg.text('변경 성공').css('color','#2a7');
      // 화면 배지 갱신
      $tr.find('.badge').text(toCode);
    }).fail(function(xhr){
      $msg.text('변경 실패: ' + xhr.status).css('color','#c33');
    });
  });
});
</script>
</body>
</html>
