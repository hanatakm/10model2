<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%
  request.setCharacterEncoding("UTF-8");
%>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>구매 등록</title>
  <script src="https://code.jquery.com/jquery-2.1.4.min.js"></script>

  <!-- (선택) jQuery UI datepicker가 있으면 자동 사용됩니다.
       없다면 포함하지 않아도 되고, 공통 캘린더 JS가 있으면 그것부터 사용해요. -->
  <!-- <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css"> -->
  <!-- <script src="//code.jquery.com/ui/1.12.1/jquery-ui.min.js"></script> -->

  <style>
    .ui-card {max-width:880px;margin:24px auto;padding:20px;border-radius:16px;box-shadow:0 8px 24px rgba(0,0,0,.08);background:rgba(255,255,255,.75);backdrop-filter:saturate(1.2) blur(6px)}
    .ui-actions{display:flex;gap:8px;justify-content:flex-end;margin-top:16px}
    .ui-btn{padding:8px 14px;border-radius:10px;border:1px solid #ddd;background:#fff;cursor:pointer}
    .ui-btn.accent{background:#DF8F6B;color:#fff;border-color:#DF8F6B}
    .row{display:grid;grid-template-columns:160px 1fr;gap:10px;align-items:center;margin:8px 0}
    input[type=text],select,textarea{width:100%;padding:8px;border:1px solid #ddd;border-radius:8px}
    small.help{color:#777}
  </style>
</head>
<body>

<div class="ui-card">
  <h2>구매 등록</h2>

  <form id="purchaseForm" method="post" action="<c:url value='/purchase/addPurchase'/>">
    <div class="row">
      <label>상품번호</label>
      <div>
        <input type="text" name="prodNo" value="${param.prodNo}" readonly />
      </div>
    </div>

    <div class="row">
      <label>결제수단</label>
      <div>
        <select name="paymentOption">
          <option value="CSH">현금(CSH)</option>
          <option value="CRD">카드(CRD)</option>
        </select>
      </div>
    </div>

    <div class="row">
      <label>수령인</label>
      <div><input type="text" name="receiverName" maxlength="50" /></div>
    </div>

    <div class="row">
      <label>수령 연락처</label>
      <div><input type="text" name="receiverPhone" maxlength="20" placeholder="010-0000-0000"/></div>
    </div>

    <div class="row">
      <label>배송지</label>
      <div><input type="text" name="divyAddr" maxlength="200" /></div>
    </div>

    <!-- ▼▼ 변경: 달력 보이는 필드 + 숨김(YYYYMMDD) 필드 -->
    <div class="row">
      <label>배송요청일</label>
      <div>
        <!-- 사용자가 보는 달력 입력(YYYY-MM-DD로 표시/선택) -->
        <input type="text" id="divyDateView" placeholder="YYYY-MM-DD" autocomplete="off" />
        <small class="help">예) 2025-10-01 (선택 시 자동 포맷)</small>
        <!-- 서버로 전송되는 진짜 값(YYYYMMDD) -->
        <input type="hidden" name="divyDate" id="divyDate" />
      </div>
    </div>
    <!-- ▲▲ 변경 끝 -->

    <div class="row">
      <label>요청사항</label>
      <div><textarea name="divyRequest" rows="3" maxlength="400"></textarea></div>
    </div>

    <div class="ui-actions">
      <button type="button" class="ui-btn" onclick="history.back()">뒤로</button>
      <button type="submit" class="ui-btn accent">구매 등록</button>
    </div>
  </form>
</div>

<script>
(function($){

  // YYYY-MM-DD -> YYYYMMDD 정규화 + 달력상 유효성 검증
  function normalizeToYmd(value){
    if(!value) return ""; // 미입력 허용(=null 전송)
    var only = (value+"").replace(/\D/g, ""); // 숫자만
    if(only.length !== 8) return null;

    var y = +only.slice(0,4), m = +only.slice(4,6), d = +only.slice(6,8);
    var dt = new Date(y, m-1, d);
    if(dt.getFullYear()!==y || (dt.getMonth()+1)!==m || dt.getDate()!==d){
      return null; // 2025-02-30 같은 비존재일
    }
    return only; // YYYYMMDD
  }

  // YYYYMMDD -> YYYY-MM-DD 표시용
  function pretty(ymd){
    if(!ymd) return "";
    var s = (ymd+"").replace(/\D/g, "");
    if(s.length !== 8) return ymd;
    return s.slice(0,4) + "-" + s.slice(4,6) + "-" + s.slice(6,8);
  }

  // 공통 캘린더 JS 우선 사용 → jQuery UI → 네이티브 date
  function attachCalendar($input){
    // 1) 네가 쓰는 공통 캘린더: 전역 객체/함수 예시(AppCalendar.attach)
    if(window.AppCalendar && typeof window.AppCalendar.attach === "function"){
      window.AppCalendar.attach($input[0], {
        format: "YYYY-MM-DD"
        // minDate: new Date() // 필요하면 주석 해제(오늘 이후만)
      });
      return;
    }
    // 2) jQuery UI datepicker
    if($.fn.datepicker){
      $input.datepicker({
        dateFormat: "yy-mm-dd",
        changeMonth: true, changeYear: true
      });
      return;
    }
    // 3) 마지막 폴백: 네이티브 date
    try {
      $input.attr("type","date");
    } catch(e) { /* 일부 구형 브라우저는 무시 */ }
  }

  // 전화번호 대략 검증(010-0000-0000 / 02-123-4567 등 허용)
  function isValidPhone(v){
    if(!v) return false;
    var s = v.replace(/\s/g, "");
    return /^\d{2,3}-?\d{3,4}-?\d{4}$/.test(s);
  }

  $(function(){

    // 달력 붙이기
    var $view = $("#divyDateView"), $hidden = $("#divyDate");
    attachCalendar($view);

    // 사용자가 날짜 바꾸면 보기/전송 값 동기화
    $view.on("change blur", function(){
      var v = $(this).val();
      // 사용자가 20251001 식으로 입력해도 예쁘게 표시
      if(/^\d{8}$/.test(v)) $(this).val(pretty(v));
      var norm = normalizeToYmd($(this).val());
      $hidden.val(norm || "");
    });

    // 기존 값이 있다면(수정화면 등) 표시 반영
    if($hidden.val()){
      $view.val(pretty($hidden.val()));
    }

    // ====== 제출 전 즉시 검증 ======
    $("#purchaseForm").on("submit", function(e){
      // 필수값
      if(!$('input[name=prodNo]').val()){ alert('상품번호가 없습니다.'); return false; }
      if(!$('input[name=receiverName]').val()){ alert('수령인을 입력하세요.'); return false; }
      if(!$('input[name=divyAddr]').val()){ alert('배송지를 입력하세요.'); return false; }

      // 연락처 형식(선택: 필요 없으면 주석)
      var phone = $('input[name=receiverPhone]').val();
      if(phone && !isValidPhone(phone)){
        alert('수령 연락처 형식을 확인하세요. 예) 010-1234-5678');
        $('input[name=receiverPhone]').focus();
        return false;
      }

      // 날짜 정규화/검증
      var norm = normalizeToYmd($view.val());
      if($view.val() && norm === null){
        alert('배송요청일을 올바르게 선택/입력하세요. 예) 2025-10-01');
        $view.focus();
        return false;
      }
      $hidden.val(norm || ""); // 최종 전송값: null 허용

      // (옵션) 과거 날짜 금지
      // if(norm){
      //   var today = new Date(), y=today.getFullYear(), m=today.getMonth()+1, d=today.getDate();
      //   var todayYmd = (""+y) + ("0"+m).slice(-2) + ("0"+d).slice(-2);
      //   if(norm < todayYmd){ alert('오늘 이전 날짜는 선택할 수 없습니다.'); return false; }
      // }

      return true;
    });

  });

})(jQuery);
</script>
</body>
</html>
