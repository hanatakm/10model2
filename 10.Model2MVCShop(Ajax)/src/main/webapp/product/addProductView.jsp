<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>상품등록</title>
  <link rel="stylesheet" href="<c:url value='/css/admin.css'/>" type="text/css">
  <script src="http://code.jquery.com/jquery-2.1.4.min.js"></script>
  <script src="<c:url value='/javascript/calendar.js'/>"></script>
</head>
<body>

<!-- ===== 타이틀 영역 ===== -->
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="12" height="37"><img src="<c:url value='/images/ct_ttl_img01.gif'/>" width="12" height="37"/></td>
    <td align="center" background="<c:url value='/images/ct_ttl_img02.gif'/>">
      <table width="98%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td width="93%" class="ct_ttl01">상품등록</td>
          <td width="20%" align="right">&nbsp;</td>
        </tr>
      </table>
    </td>
    <td width="12" height="37"><img src="<c:url value='/images/ct_ttl_img03.gif'/>" width="12" height="37"/></td>
  </tr>
</table>

<!-- ===== 입력 폼 ===== -->
<form id="addProductForm" method="post" enctype="multipart/form-data"
      action="<c:url value='/product/addProduct'/>">
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:13px;">
    <tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>

    <!-- 상품명 -->
    <tr>
      <td width="104" class="ct_write">
        상품명 <img src="<c:url value='/images/ct_icon_red.gif'/>" width="3" height="3" align="absmiddle"/>
      </td>
      <td bgcolor="F4F4F4" class="ct_write01">
        <input type="text" name="prodName" style="width:300px;height:19px"
               maxlength="100" value="${param.prodName}" fieldTitle="상품명" required/>
      </td>
    </tr>
    <tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>

    <!-- 상세 -->
    <tr>
      <td class="ct_write">
        상세 <img src="<c:url value='/images/ct_icon_red.gif'/>" width="3" height="3" align="absmiddle"/>
      </td>
      <td bgcolor="F4F4F4" class="ct_write01">
        <textarea name="prodDetail" style="width:98%;height:80px"
                  fieldTitle="상세" required>${param.prodDetail}</textarea>
      </td>
    </tr>
    <tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>

    <!-- 제조일 -->
    <tr>
      <td class="ct_write">
        제조일 <img src="<c:url value='/images/ct_icon_red.gif'/>" width="3" height="3" align="absmiddle"/>
      </td>
      <td bgcolor="F4F4F4" class="ct_write01">
        <input type="text" name="manufactureDay" placeholder="YYYY-MM-DD"
               style="width:100px;height:19px" maxlength="10"
               value="${param.manufactureDay}" fieldTitle="제조일" required/>
        &nbsp;
        <img src="<c:url value='/images/ct_icon_date.gif'/>" width="15" height="15"
             onclick="show_calendar('document.getElementById(\'addProductForm\').manufactureDay', document.getElementById('addProductForm').manufactureDay.value)"/>
      </td>
    </tr>
    <tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>

    <!-- 가격 -->
    <tr>
      <td class="ct_write">
        가격 <img src="<c:url value='/images/ct_icon_red.gif'/>" width="3" height="3" align="absmiddle"/>
      </td>
      <td bgcolor="F4F4F4" class="ct_write01">
        <input type="text" name="price" style="width:120px;height:19px"
               value="${param.price}" fieldTitle="가격" num="n" required/>
      </td>
    </tr>
    <tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>

    <!-- 이미지 (0~3장) : 사용자용 멀티픽커 + 서버용 숨김 파일 인풋 3개 -->
    <tr>
      <td class="ct_write">이미지(최대 3장)</td>
      <td class="ct_write01" bgcolor="F4F4F4">
        <!-- 사용자 선택용 -->
        <input type="file" id="imagesPicker" multiple style="height:19px" accept="image/*"/>

        <!-- 서버 제출용(컨트롤러 파라미터 이름 유지!) -->
        <input type="file" id="uploadFile"  name="uploadFile"  style="display:none;">
        <input type="file" id="uploadFile2" name="uploadFile2" style="display:none;">
        <input type="file" id="uploadFile3" name="uploadFile3" style="display:none;">

        <div class="hint">여러 장 선택 가능, 최대 3장. 썸네일에서 X로 삭제할 수 있어요.</div>

        <!-- 미리보기 -->
        <div id="previewWrap" style="margin-top:8px;">
          <ul id="previewList" style="list-style:none;padding:0;margin:0;display:flex;gap:8px;flex-wrap:wrap;"></ul>
        </div>
      </td>
    </tr>

  </table>

  <!-- 버튼 -->
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:10px;">
    <tr>
      <td align="center">
        <button type="button" id="btnAdd" class="ct_btn02"
                data-action="<c:url value='/product/addProduct'/>"
                data-method="post" data-ctx="product">등록</button>
        &nbsp;&nbsp;
        <a class="ct_btn01" href="<c:url value='/product/listProduct?menu=manage'/>">목록</a>
      </td>
    </tr>
  </table>
</form>

<script>
  // 공통: a[href="#"] 방지
  $(document).on('click', 'a[href="#"]', function(e){ e.preventDefault(); });

  // ===== 폼 제출 전 검증(기존 공통 검증 + 보조) =====
  $(function(){
    $('#btnAdd').on('click', function(e){
      e.preventDefault();
      var $form = $('#addProductForm');

      if (typeof window.FormValidation === 'function') {
        if (!window.FormValidation($form[0])) return;
      }
      var day = $form.find('[name="manufactureDay"]').val().trim();
      var price = $form.find('[name="price"]').val().trim();
      if(!/^\d{4}-\d{2}-\d{2}$/.test(day)){ alert('제조일 형식(YYYY-MM-DD)을 확인하세요.'); return; }
      if(!/^\d+(\.\d{1,2})?$/.test(price)){ alert('가격은 숫자(소수 2자리 이내)만 입력하세요.'); return; }

      $form.attr({action: $(this).data('action'), method: $(this).data('method')}).trigger('submit');
    });
  });

  // ===== 이미지 선택/삭제/동기화 =====
  (function(){
    var picker = document.getElementById('imagesPicker');
    var list   = document.getElementById('previewList');

    var hiddenInputs = [
      document.getElementById('uploadFile'),
      document.getElementById('uploadFile2'),
      document.getElementById('uploadFile3')
    ];

    // 누적 목록(최대 3장 유지)
    var filesState = [];

    function keyOf(f){ return [f.name, f.size, f.lastModified].join(':'); }

    function syncHiddenInputs(){
      // filesState[0..2]를 각 숨김 인풋에 주입
      hiddenInputs.forEach(function(inp, i){
        if (window.DataTransfer) {
          var dt = new DataTransfer();
          if (filesState[i]) dt.items.add(filesState[i]);
          inp.files = dt.files;
        } else {
          // 구형 브라우저: 개별 삭제/동기화 미지원 → 초기화만
          if (!filesState[i]) inp.value = '';
        }
      });
    }

    function render(){
      list.innerHTML = '';
      filesState.forEach(function(file, idx){
        var li  = document.createElement('li');
        li.style.cssText='position:relative;display:inline-block;margin-right:8px;width:120px;';

        var img = document.createElement('img');
        img.style.cssText='max-width:120px;max-height:120px;border:1px solid #ddd;padding:2px;';
        img.alt = file.name;

        var reader = new FileReader();
        reader.onload = function(e){ img.src = e.target.result; };
        reader.readAsDataURL(file);

        var btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = '×';
        btn.title = '삭제';
        btn.style.cssText='position:absolute;top:0;right:0;width:22px;height:22px;border:none;border-radius:50%;background:rgba(0,0,0,.6);color:#fff;cursor:pointer;';
        btn.onclick = function(){
          filesState.splice(idx, 1);
          syncHiddenInputs();
          render();
        };

        li.appendChild(img);
        li.appendChild(btn);
        list.appendChild(li);
      });
    }

    function addFromPicker(newFiles){
      // 이미지 타입만, 5MB 제한(원하면 조정)
      var arr = Array.from(newFiles || []).filter(function(f){
        return f.type && f.type.indexOf('image/') === 0 && f.size <= 5*1024*1024;
      });
      // 중복 제거
      var seen = new Set(filesState.map(keyOf));
      arr.forEach(function(f){
        var k = keyOf(f);
        if (!seen.has(k) && filesState.length < 3) {
          filesState.push(f);
          seen.add(k);
        }
      });
      if (arr.length === 0) {
        // 선택한 파일이 전부 필터링됐을 때만 안내
        // alert('이미지 파일만 가능하며, 파일당 5MB 이하여야 합니다.');
      }
      if (filesState.length > 3) filesState = filesState.slice(0,3);

      syncHiddenInputs();
      render();
    }

    picker.addEventListener('change', function(){
      addFromPicker(picker.files);
      // picker는 그때그때 초기화(UX상 같은 파일 재선택 허용)
      picker.value = '';
    });

    // 초기 렌더
    render();
  })();
</script>

</body>
</html>
