<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>상품상세</title>
  <link rel="stylesheet" href="<c:url value='/css/admin.css'/>" type="text/css">
  <script src="http://code.jquery.com/jquery-2.1.4.min.js"></script>

<!-- 상태/코드 정규화 -->
<c:set var="status" value="${empty product.status ? '' : fn:toUpperCase(fn:trim(product.status))}"/>
<c:set var="code"   value="${empty product.tranCode ? '' : fn:toUpperCase(fn:trim(product.tranCode))}"/>
<c:set var="active" value="${empty activeFlag ? '' : fn:toUpperCase(fn:trim(activeFlag))}"/>


<c:set var="isAdmin" value="${not empty user and user.role eq 'admin'}"/>

<!-- isOnSale : status가 오면 그걸 사용, 없으면 기본 '판매중' 취급 -->
<c:set var="isOnSale" value="${status == 'ON_SALE' or empty status}"/>

<%-- ★정책 A: 일괄판매(거래가 한번이라도 있으면 구매불가) --%>
<c:set var="buyable" value="${isOnSale and empty code}"/>

<%-- (선택) 정책 B: 완료(CMP)면 재판매 허용일 때는 위 한 줄 대신 아래 사용
<c:set var="buyable" value="${isOnSale and (empty code or code=='CMP' or code=='003' or code=='3')}"/>
--%>

<c:set var="menuVal" value="${empty param.menu ? 'search' : param.menu}"/>
<c:set var="roleVal" value="${not empty user ? user.role : ''}"/>

</head>

<body data-role="${roleVal}">
  <!-- 타이틀 -->
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr><td class="ct_ttl01">상품상세</td></tr>
  </table>

  <!-- 상세 -->
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:12px;">
    <tr><td width="120" class="ct_write">상품번호</td><td class="ct_write01">${product.prodNo}</td></tr>
    <tr><td class="ct_write">상품명</td><td class="ct_write01">${product.prodName}</td></tr>
    <tr><td class="ct_write">상세</td><td class="ct_write01">${product.prodDetail}</td></tr>
    <tr><td class="ct_write">제조일</td><td class="ct_write01">${product.manufactureDay}</td></tr>
    <tr><td class="ct_write">가격</td><td class="ct_write01">${product.price}</td></tr>
    <tr><td class="ct_write">등록일</td><td class="ct_write01">${product.regDate}</td></tr>

    <!-- 이미지 1~3 -->
    <c:if test="${not empty product.imageFile}">
      <tr>
        <td class="ct_write">이미지1</td>
        <td class="ct_write01">
          <img src="<c:url value='/uploads/${product.imageFile}'/>" style="max-width:240px;" alt="상품 이미지1">
        </td>
      </tr>
    </c:if>

    <c:if test="${not empty product.imageFile2}">
      <tr>
        <td class="ct_write">이미지2</td>
        <td class="ct_write01">
          <img src="<c:url value='/uploads/${product.imageFile2}'/>" style="max-width:240px;" alt="상품 이미지2">
        </td>
      </tr>
    </c:if>

    <c:if test="${not empty product.imageFile3}">
      <tr>
        <td class="ct_write">이미지3</td>
        <td class="ct_write01">
          <img src="<c:url value='/uploads/${product.imageFile3}'/>" style="max-width:240px;" alt="상품 이미지3">
        </td>
      </tr>
    </c:if>
  </table>

  <!-- 액션 -->


<c:if test="${isAdmin}">
  <a class="ct_btn02"
     href="<c:url value='/product/updateProductView?prodNo=${product.prodNo}&menu=${menuVal}'/>"
     target="rightFrame">수정</a>
  &nbsp;&nbsp;

  <c:choose>
  
    <c:when test="${active eq 'Y'}">
      <form method="post" action="<c:url value='/product/remove'/>"
            style="display:inline" target="rightFrame">
        <input type="hidden" name="prodNo" value="${product.prodNo}"/>
        <button type="submit" class="ct_btn02"
                onclick="return confirm('이 상품을 비활성화(삭제) 하시겠습니까?');">
          삭제(비활성화)
        </button>
      </form>
    </c:when>


    <c:otherwise>
      <span class="ct_btn02" style="color:#999; cursor:default;">삭제완료</span>
    </c:otherwise>
  </c:choose>

  &nbsp;&nbsp;
</c:if>






    <!-- 버튼은 항상 보이되, 구매불가면 href 제거 -->
    <a class="ct_btn02 buy-btn ${ (buyable or isAdmin) ? '' : 'disabled' }"
       data-sold="${ (buyable or isAdmin) ? 'N' : 'Y' }"
       <c:if test="${buyable or isAdmin}">
         href="<c:url value='/purchase/addPurchaseView?prodNo=${product.prodNo}'/>"
       </c:if>
       target="rightFrame">
      ${ (buyable or isAdmin) ? '구매하기' : '구매불가' }
    </a>

    &nbsp;&nbsp;

    <a class="ct_btn02"
       href="<c:url value='/product/listProduct?menu=${menuVal}'/>"
       target="rightFrame">목록</a>
  </div>

  <!-- 스크립트: 구매버튼 2중 방어 + 최근 본 상품 기록 -->
  <script>
  (function($){
    $(function(){
      var role = ($('body').attr('data-role') || '').toLowerCase();
      var isAdmin = (role === 'admin');

      // 초기 보정: 비활성화면 href 제거 및 표시 변경
      $('.buy-btn').each(function(){
        var $b = $(this);
        var disabled = $b.hasClass('disabled') || $b.data('sold') === 'Y';
        if(disabled && !isAdmin){
          $b.removeAttr('href')
            .addClass('disabled')
            .attr('aria-disabled','true')
            .text('구매불가');
        }
      });

      // 클릭 차단
      $(document).on('click', '.buy-btn', function(e){
        var $b = $(this);
        var disabled = $b.hasClass('disabled') || $b.data('sold') === 'Y';
        if(disabled && !isAdmin){
          e.preventDefault();
          e.stopPropagation();
          alert('이미 판매(구매불가) 상태의 상품입니다.');
          return false;
        }
      });

      // 최근 본 상품 cookie 기록
      (function(){
        var prodNo = '${product.prodNo}';
        if(!prodNo) return;

        function getCookie(name){
          var m = document.cookie.match(new RegExp('(?:^|;\\s*)'+name+'=([^;]*)'));
          return m ? decodeURIComponent(m[1].replace(/\+/g, '%20')) : '';
        }
        function setCookie(name, value, days){
          var sec = days*24*60*60;
          document.cookie = name + '=' + encodeURIComponent(value) + '; path=/; max-age=' + sec;
        }

        var raw = getCookie('history');                  // "10270,10269,..." 형태
        var arr = raw ? raw.split(',').filter(Boolean) : [];
        arr = arr.filter(function(v){ return v !== String(prodNo); }); // 중복 제거
        arr.unshift(String(prodNo));
        if(arr.length > 20) arr = arr.slice(0, 20);
        setCookie('history', arr.join(','), 30);
      })();
    });
  })(jQuery);
  </script>
</body>
</html>
