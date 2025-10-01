<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>상품수정</title>
  <link rel="stylesheet" href="<c:url value='/css/admin.css'/>" type="text/css">
  <!-- jQuery (CDN) -->
  <script src="https://code.jquery.com/jquery-2.1.4.min.js"></script>
  <!-- 달력 스크립트 -->
  <script src="<c:url value='/javascript/calendar.js'/>"></script>
</head>
<body>

<form id="updateProductForm" method="post" enctype="multipart/form-data"
      action="<c:url value='/product/updateProduct'/>">

  <!-- PK -->
  <input type="hidden" name="prodNo" value="${product.prodNo}"/>

  <!-- 기존 이미지 유지용 -->
  <input type="hidden" name="currentImage1" value="${product.imageFile}"/>
  <input type="hidden" name="currentImage2" value="${product.imageFile2}"/>
  <input type="hidden" name="currentImage3" value="${product.imageFile3}"/>

  <table class="ct_table mt-13" width="100%" cellspacing="0" cellpadding="0">
    <tr><td height="1" colspan="3" class="divider"></td></tr>

    <tr>
      <td class="ct_write" width="120">상품명</td>
      <td class="ct_write01 bg-muted">
        <input type="text" name="prodName" class="ipt ipt-lg w-300"
               value="${product.prodName}" fieldTitle="상품명" required/>
      </td>
    </tr>
    <tr><td height="1" colspan="3" class="divider"></td></tr>

    <tr>
      <td class="ct_write">상세</td>
      <td class="ct_write01 bg-muted">
        <textarea name="prodDetail" class="ipt ipt-area w-98 h-80"
                  fieldTitle="상세" required>${product.prodDetail}</textarea>
      </td>
    </tr>
    <tr><td height="1" colspan="3" class="divider"></td></tr>

    <tr>
      <td class="ct_write">제조일</td>
      <td class="ct_write01 bg-muted">
        <div class="row row-inline gap-8">
          <input type="text" name="manufactureDay" placeholder="YYYYMMDD"
                 maxlength="8" class="ipt w-120"
                 value="${product.manufactureDay}" fieldTitle="제조일" required/>
          <button type="button" class="btn btn-icon" aria-label="달력"
            onclick="show_calendar(document.getElementById('updateProductForm').manufactureDay,
                                   document.getElementById('updateProductForm').manufactureDay.value)">
            <img src="<c:url value='/images/ct_icon_date.gif'/>" width="15" height="15" alt="달력"/>
          </button>
        </div>
      </td>
    </tr>
    <tr><td height="1" colspan="3" class="divider"></td></tr>

    <tr>
      <td class="ct_write">가격</td>
      <td class="ct_write01 bg-muted">
        <input type="text" name="price" class="ipt w-140" value="${product.price}"
               fieldTitle="가격" num="n" required/>
      </td>
    </tr>
    <tr><td height="1" colspan="3" class="divider"></td></tr>

    <!-- 이미지 3슬롯 -->
    <tr>
      <td class="ct_write">이미지</td>
      <td class="ct_write01 bg-muted">

        <!-- Slot 1 -->
        <div class="slot row row-inline gap-12 mt-8" data-slot="1">
          <div class="col col-120">
            <label for="uploadFile" class="lbl"><strong>이미지 1</strong></label>
            <input type="file" id="uploadFile" name="uploadFile" accept="image/*" class="ipt-file"/>
          </div>
          <div class="col">
            <span class="lbl-sub">현재</span>
            <c:choose>
              <c:when test="${not empty product.imageFile}">
                <img id="curr1" class="thumb" src="<c:url value='/uploads/${product.imageFile}'/>" alt="현재 이미지 1"/>
              </c:when>
              <c:otherwise><span id="curr1" class="text-muted">없음</span></c:otherwise>
            </c:choose>
          </div>
          <div class="col">
            <span class="lbl-sub">미리보기</span>
            <img id="preview1" class="thumb is-hidden" alt="미리보기 1"/>
          </div>
        </div>

        <!-- Slot 2 -->
        <div class="slot row row-inline gap-12 mt-8" data-slot="2">
          <div class="col col-120">
            <label for="uploadFile2" class="lbl"><strong>이미지 2</strong></label>
            <input type="file" id="uploadFile2" name="uploadFile2" accept="image/*" class="ipt-file"/>
          </div>
          <div class="col">
            <span class="lbl-sub">현재</span>
            <c:choose>
              <c:when test="${not empty product.imageFile2}">
                <img id="curr2" class="thumb" src="<c:url value='/uploads/${product.imageFile2}'/>" alt="현재 이미지 2"/>
              </c:when>
              <c:otherwise><span id="curr2" class="text-muted">없음</span></c:otherwise>
            </c:choose>
          </div>
          <div class="col">
            <span class="lbl-sub">미리보기</span>
            <img id="preview2" class="thumb is-hidden" alt="미리보기 2"/>
          </div>
        </div>

        <!-- Slot 3 -->
        <div class="slot row row-inline gap-12 mt-8" data-slot="3">
          <div class="col col-120">
            <label for="uploadFile3" class="lbl"><strong>이미지 3</strong></label>
            <input type="file" id="uploadFile3" name="uploadFile3" accept="image/*" class="ipt-file"/>
          </div>
          <div class="col">
            <span class="lbl-sub">현재</span>
            <c:choose>
              <c:when test="${not empty product.imageFile3}">
                <img id="curr3" class="thumb" src="<c:url value='/uploads/${product.imageFile3}'/>" alt="현재 이미지 3"/>
              </c:when>
              <c:otherwise><span id="curr3" class="text-muted">없음</span></c:otherwise>
            </c:choose>
          </div>
          <div class="col">
            <span class="lbl-sub">미리보기</span>
            <img id="preview3" class="thumb is-hidden" alt="미리보기 3"/>
          </div>
        </div>

        <p class="hint mt-6">* 새 파일을 선택한 슬롯만 교체됩니다(선택 안 하면 기존 유지).</p>
      </td>
    </tr>
  </table>

  <div class="actions center mt-10">
    <a href="javascript:;" id="btnSave"   class="ct_btn02">저장</a>
    <a href="javascript:;" id="btnCancel" class="ct_btn01">취소</a>
  </div>
</form>

<script>
(function($){
  var submitting = false;

  // 공통: 이미지 미리보기
  function bindPreview(inputId, previewId){
    var $inp = $('#'+inputId), $img = $('#'+previewId);
    $inp.on('change', function(){
      var f = this.files && this.files[0];
      if(!f){ $img.addClass('is-hidden').attr('src',''); return; }
      if(!(f.type && f.type.indexOf('image/')===0)){ alert('이미지 파일만 가능합니다.'); this.value=''; return; }
      if(f.size > 5*1024*1024){ alert('이미지는 5MB 이하여야 합니다.'); this.value=''; return; }
      var reader = new FileReader();
      reader.onload = function(e){ $img.attr('src', e.target.result).removeClass('is-hidden'); };
      reader.readAsDataURL(f);
    });
  }
  bindPreview('uploadFile',  'preview1');
  bindPreview('uploadFile2', 'preview2');
  bindPreview('uploadFile3', 'preview3');

  // 저장
  $('#btnSave').on('click', function(){
    var $form = $('#updateProductForm');
    if(submitting){ return; }

    // 공통 유효성
    if(typeof window.FormValidation === 'function'){
      if(!window.FormValidation($form[0])) return;
    }

    // 검증 (YYYYMMDD, 숫자)
    var day = $.trim($form.find('[name="manufactureDay"]').val());
    var price = $.trim($form.find('[name="price"]').val());
    if(!/^\d{8}$/.test(day)){ alert('제조일은 YYYYMMDD(8자리)로 입력하세요.'); return; }
    if(!/^\d+(\.\d{1,2})?$/.test(price)){ alert('가격은 숫자(소수 2자리 이내)만 입력하세요.'); return; }

    submitting = true;
    $form.trigger('submit');
  });

  // 취소 → 상세로 이동
  $('#btnCancel').on('click', function(){
    location.href = '<c:url value="/product/getProduct"/>' + '?prodNo=${product.prodNo}';
  });

})(jQuery);
</script>
</body>
</html>
