<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>상품 관리</title>

  <link rel="stylesheet" href="<c:url value='/css/admin.css'/>" type="text/css">
  <link rel="stylesheet" href="<c:url value='/css/listProduct.css'/>" type="text/css">
  <script src="https://code.jquery.com/jquery-2.1.4.min.js"></script>

<!-- debug -->
<div style="display:none">
  user=${sessionScope.user != null ? sessionScope.user.userId : 'null'}
  , map?=${not empty map}
  , listSize=${empty map ? 0 : map.list.size()}
  , total=${empty map ? 'NA' : map.totalCount}
</div>



  <!-- URL 베이스 사전 계산 -->
  <c:url var="listApi"   value="/product/json/getProductList"/>
  <c:url var="detailUrl" value="/product/getProduct"/>
  <c:url var="shipBase"  value="/purchase/updateTranCodeByProd"/>
</head>

<body data-login="${not empty sessionScope.user}"
      data-role="${empty sessionScope.user ? '' : fn:toLowerCase(sessionScope.user.role)}">

<!-- 로그인 체크 유지 -->
<c:if test="${empty sessionScope.user}">
  <c:redirect url="/user/loginView.jsp"/>
</c:if>

<!-- 검색/조건 (파라미터명/값 유지) -->
<form id="searchForm" method="get" action="<c:url value='/product/listProduct'/>">
  <input type="hidden" name="menu" value="${empty param.menu ? 'manage' : param.menu}"/>
  <input type="hidden" name="currentPage" value="1"/>
  <input type="hidden" name="pageSize" value="${empty param.pageSize ? 10 : param.pageSize}"/>

  <select name="searchCondition">
    <option value="0" ${param.searchCondition=='0'?'selected':''}>상품번호</option>
    <option value="1" ${param.searchCondition=='1'?'selected':''}>상품명</option>
    <option value="2" ${param.searchCondition=='2'?'selected':''}>상품가격</option>
  </select>

  <!-- ▼ 자동완성용 래퍼 -->
  <span id="ac-wrap" style="position:relative; display:inline-block;">
    <input type="text" id="searchKeyword" name="searchKeyword"
           value="${fn:escapeXml(param.searchKeyword)}" style="width:200px"/>
    
  </span>

  <button type="button" id="btnSearch" class="ct_btn01">검색</button>
</form>


<!-- 요약(초기엔 비워두고 로드 후 채움) -->
<div id="summary" class="ct_write01" style="margin-top:8px;">
  전체 <strong>0</strong> 건수, 현재 <strong>1</strong> 페이지
</div>

<!-- 목록 (thead 고정, tbody 동적 구성) -->
<div id="table-wrap">
  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:10px;">
    <tr class="ct_list_b">
      <td width="70"  align="center">No</td>
      <td class="ct_line02"></td>

      <td width="160" align="center">상품번호</td>
      <td class="ct_line02"></td>

      <td width="160" align="center">상품명</td>
      <td class="ct_line02"></td>

      <td width="140" align="center">가격</td>
      <td class="ct_line02"></td>

      <td width="170" align="center">등록일</td>
      <td class="ct_line02"></td>

      <td width="220" align="center">현재상태</td>
    </tr>
    <tr><td colspan="11" bgcolor="808285" height="1"></td></tr>
  </thead>
  <tbody id="list-body"><!-- 여기로 행이 append 됩니다 --></tbody>
</table>
</div>

<!-- 카드형 목록 컨테이너 -->
<div id="card-body" class="prod-grid"></div>



<!-- 무한스크롤 로더 / 끝 안내 -->
<div id="infinite-loader" style="display:none; text-align:center; padding:10px;">불러오는 중...</div>
<div id="infinite-end"    style="display:none; text-align:center; padding:10px;">마지막입니다</div>

<!-- 상세 이동용 폼 (GET 유지) -->
<form id="navFormProduct" method="get" data-detail="${detailUrl}">
  <input type="hidden" name="prodNo"/>
</form>

<script>
$(function(){

  // ===== 상태값 =====
  var $body = $('body'); 

  var isLogin = ($body.data('login') === true || $body.data('login') === 'true');
  var role    = ($body.data('role') || '').toString();

  var $form   = $('#searchForm');
  var $tbody  = $('#list-body');
  var $cards  = $('#card-body');           // 카드 컨테이너
  var $sum    = $('#summary');
  var $loader = $('#infinite-loader');
  var $end    = $('#infinite-end');

  // 테이블 래퍼 추가
  var $tableWrap = $('#table-wrap');
  var $cardWrap  = $('#card-body');

  // 👉 여기서 초기화 시점에 숨겨줌
  $tableWrap.hide();   // 카드만 보이게
  
  
  
  var page     = 1;
  var pageSize = parseInt($form.find('[name="pageSize"]').val() || '10', 10);
  var isLoading = false;
  var isEnd     = false;

  // ===== 유틸 =====
  function getCriteria(){
    return {
      menu:            $form.find('[name="menu"]').val() || 'manage',
      searchCondition: $form.find('[name="searchCondition"]').val() || '1',
      searchKeyword:   $form.find('[name="searchKeyword"]').val() || ''
    };
  }

  function updateSummary(resultPage){
    var total = resultPage.totalCount || 0;
    var cur   = resultPage.currentPage || page;
    $sum.html('전체 <strong>'+ total +'</strong> 건수, 현재 <strong>'+ cur +'</strong> 페이지');
  }

  function mapState(codeRaw){
    var code = (codeRaw || '').toString().trim().toUpperCase();
    if(code==='ODR' || code==='ORD' || code==='001' || code==='1'){ return 'ODR'; }
    if(code==='DLV' || code==='002' || code==='2'){ return 'DLV'; }
    if(code==='CMP' || code==='003' || code==='3'){ return 'CMP'; }
    return ''; // 기타/NULL → 판매중
  }

  function buildStatusCell(p, resultPage){
	  var menu = getCriteria().menu;
	  var mapped = mapState(p.tranCode);
	  var html = '';

	  // 🔒 삭제완료는 관리자만 보임
	  if (p.activeFlag && p.activeFlag.toUpperCase() === 'N') {
	    if (role === 'admin') {
	      return '<span style="color:red;">삭제완료</span>';
	    } else {
	      return ''; // 일반 유저는 공백 (아예 숨김)
	    }
	  }

	  if(mapped==='ODR'){
	    html += '구매완료';
	    if(menu==='manage' && role === 'admin'){
	      var shipUrl = '${shipBase}' + '?prodNo=' + encodeURIComponent(p.prodNo)
	                  + '&tranCode=DLV'
	                  + '&menu=' + encodeURIComponent(menu)
	                  + '&page=' + encodeURIComponent(resultPage.currentPage);
	      html += '&nbsp;&nbsp;<span class="ct_btn"><a href="'+ shipUrl +'">배송하기</a></span>';
	    }
	  }else if(mapped==='DLV'){
	    html += '배송중';
	    if(role !== 'admin' && isLogin){
	      var arriveUrl = '${shipBase}' + '?prodNo=' + encodeURIComponent(p.prodNo)
	                    + '&tranCode=CMP'
	                    + '&menu=' + encodeURIComponent(menu)
	                    + '&page=' + encodeURIComponent(resultPage.currentPage);
	      html += '&nbsp;&nbsp;<span class="ct_btn"><a href="'+ arriveUrl +'">물품도착</a></span>';
	    }
	  }else if(mapped==='CMP'){
	    html += '배송완료';
	  }else{
	    html += '판매중'; // tranCode null/기타
	  }
	  return html;
	}


  // ===== 테이블 행 추가(기존 유지) =====
  function appendRows(list, resultPage){
    var totalCount = resultPage.totalCount || 0;
    var curPage = resultPage.currentPage || page;

    for(var i=0; i<list.length; i++){
      var p = list[i];
      var rowNo = totalCount - ((curPage - 1) * pageSize) - i;

      var tr  = '';
      tr += '<tr class="ct_list_pop" data-goto="productDetail" data-id="'+ p.prodNo +'">';
      tr +=   '<td align="center">'+ rowNo +'</td>';
      tr +=   '<td class="ct_line02"></td>';

      tr +=   '<td align="left">'+ p.prodNo +'</td>';
      tr +=   '<td class="ct_line02"></td>';

      tr +=   '<td align="left">'+ $('<div/>').text(p.prodName || '').html() +'</td>';
      tr +=   '<td class="ct_line02"></td>';

      tr +=   '<td align="right">'+ (p.price == null ? '' : p.price) +'</td>';
      tr +=   '<td class="ct_line02"></td>';

      tr +=   '<td align="center">'+ (p.regDate || '') +'</td>';
      tr +=   '<td class="ct_line02"></td>';

      tr +=   '<td align="center">'+ buildStatusCell(p, resultPage) +'</td>';
      tr += '</tr>';

      tr += '<tr><td colspan="11" bgcolor="D6D7D6" height="1"></td></tr>';

      $tbody.append(tr);
    }
  }

  function setLoading(v){
    isLoading = !!v;
    $loader.toggle(isLoading);
  }

  function maybeEnd(list, resultPage){
    var cur = resultPage.currentPage || page;
    var max = resultPage.maxPage || cur;
    if(list.length < pageSize || cur >= max){
      isEnd = true;
      $end.show();
    }
  }

  // ===== 썸네일 카드 유틸 =====
// 업로드 베이스(네 코드에 이미 있음)
var IMG_BASE = '<c:url value="/uploads/"/>';
// [REPLACE] 404 안 나오는 실제 플레이스홀더로 지정
var NOIMG    = '<c:url value="/images/ct_btnbg02.gif"/>';

function pickImage(p){
	  // 1순위 imageFile → 2 → 3
	  var cands = [p.imageFile, p.imageFile2, p.imageFile3].filter(Boolean);
	  if(!cands.length) return NOIMG;

	  var src = (cands[0] + '').trim();

	  // 윈도우 백슬래시 보정
	  src = src.replace(/\\/g, '/');

	  // 절대 URL(http/https)이면 그대로
	  if (/^https?:\/\//i.test(src)) return src;

	  // 루트(/)로 시작하면 그대로
	  if (src.indexOf('/') === 0) return src;

	  // 'uploads/...'로 시작만 하는 경우 앞에 / 보정
	  if (/^uploads\//i.test(src)) return '/' + src;

	  // 그 외엔 업로드 베이스 붙이기 (예: '2025/09/xxx.jpg')
	  return IMG_BASE + src;
	}


  function fmtPrice(v){
    if(v==null || v==='') return '';
    var n = +v;
    if(isNaN(n)) return v;
    return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  }

  function appendCards(list, resultPage){
	  if(!Array.isArray(list) || !list.length) return;

	  var html = '';
	  for(var i=0; i<list.length; i++){
	    var p = list[i];

	    var img   = pickImage(p);                                     // 경로 정리
	    var name  = $('<div/>').text(p.prodName || '').html();        // XSS 안전
	    var price = (p.price==null ? '' : '₩ ' + fmtPrice(p.price));
	    var statusHtml = buildStatusCell(p, resultPage);

	    html += ''
	      + '<div class="prod-card" data-goto="productDetail" data-id="'+ p.prodNo +'">'
	      +   '<div class="prod-thumb">'
	      +     '<img class="thumb" src="'+ img +'" alt="'+ name +'"'
	      +          ' width="480" height="320" loading="lazy" decoding="async"'
	      +          ' onerror="this.onerror=null; this.src=\''+ NOIMG +'\';">'   // ← 폴백
	      +   '</div>'
	      +   '<div class="prod-body">'
	      +     '<div class="prod-name">'+ name +'</div>'
	      +     '<div class="prod-meta"><span>#'+ (p.prodNo==null?'':p.prodNo) +'</span></div>'
	      +     '<div class="prod-price">'+ price +'</div>'
	      +     '<div class="prod-status">'+ statusHtml +'</div>'
	      +   '</div>'
	      + '</div>';
	  }

	  // 성능: 한 번에 붙이기
	  $cards.append(html);
	}

  // ===== 데이터 로드 : 테이블 + 카드 모두 채움 =====
  function loadPage(nextPage){
    if(isLoading || isEnd) return;

    setLoading(true);

    var payload = $.extend({}, getCriteria(), {
      currentPage: nextPage,
      pageSize: pageSize
    });

    $.ajax({
      url: '${listApi}',
      type: 'POST',
      contentType: 'application/json; charset=UTF-8',
      dataType: 'json',
      data: JSON.stringify(payload)
    }).done(function(resp){
      var list = resp && resp.list ? resp.list : [];
      var rp   = resp && resp.resultPage ? resp.resultPage : { currentPage: nextPage, totalCount: 0, maxPage: nextPage };

      if(nextPage === 1){
        $tbody.empty();
        $cards.empty();
        $end.hide();
        isEnd = false;
      }

      appendRows(list, rp);   // 테이블
      appendCards(list, rp);  // 카드
      updateSummary(rp);
      maybeEnd(list, rp);

      page = rp.currentPage || nextPage;

    }).fail(function(){
      if(nextPage > 1){ page = nextPage - 1; }
      alert('목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    }).always(function(){
      setLoading(false);
      tryFillViewport();
    });
  }

  // ===== 초기 로드 =====
  loadPage(1);
  setTimeout(tryFillViewport, 0);

  function tryFillViewport() {
    var docHeight = Math.max(
      document.body.scrollHeight, document.documentElement.scrollHeight,
      document.body.offsetHeight, document.documentElement.offsetHeight,
      document.body.clientHeight, document.documentElement.clientHeight
    );
    if (!isEnd && !isLoading && docHeight <= window.innerHeight + 1) {
      loadPage(page + 1);
    }
  }

  // ===== 검색 =====
  $('#btnSearch').on('click', function(e){
    e.preventDefault();

    if(!isLogin){
      alert('로그인이 필요합니다.');
      window.location.href = '<c:url value="/user/loginView.jsp"/>';
      return;
    }

    page = 1;
    loadPage(1);
  });

  // ===== 상세 이동 (이벤트 위임) =====
  $(document).on('click','[data-goto="productDetail"]', function(){
    var prodNo = $(this).data('id');
    var $f = $('#navFormProduct');
    $f.find('[name="prodNo"]').val(prodNo);
    $f.attr({action: $f.data('detail'), method:'get'}).trigger('submit');
  });

  // ===== 무한 스크롤 (디바운스) =====
  var ticking = false;
  $(window).on('scroll', function(){
    if(ticking) return;
    ticking = true;
    setTimeout(function(){
      ticking = false;
      if(isEnd || isLoading) return;

      var scrollBottom = window.pageYOffset + window.innerHeight;
      var docHeight    = Math.max(
        document.body.scrollHeight, document.documentElement.scrollHeight,
        document.body.offsetHeight, document.documentElement.offsetHeight,
        document.body.clientHeight, document.documentElement.clientHeight
      );

      if(scrollBottom >= docHeight - 100){
        loadPage(page + 1);
      }
    }, 120);
  });

  // ===== AutoComplete (응답 정규화 패치) =====
  (function(){
    var $kw   = $('#searchKeyword');

    var $fly = $('#ac-fly');
    if ($fly.length === 0) {
      $fly = $('<div id="ac-fly" />').css({
        position:'absolute', display:'none', background:'#fff', border:'1px solid #ddd',
        maxHeight:240, overflow:'auto', zIndex: 99999, boxShadow:'0 2px 8px rgba(0,0,0,.08)'
      }).appendTo('body');
    }

    var api = '<c:url value="/product/json/findProduct"/>';
    var data = []; var ix = -1; var t=null;

    function hide(){ $fly.hide().empty(); data=[]; ix=-1; }
    function place(){
      var off = $kw.offset();
      $fly.css({ top: off.top + $kw.outerHeight(), left: off.left, width: $kw.outerWidth() });
    }

    function normalizeToArray(res){
      try{
        if (Array.isArray(res)) return res;
        if (res && Array.isArray(res.list)) return res.list;
        if (typeof res === 'string') {
          var j = JSON.parse(res);
          return normalizeToArray(j);
        }
      }catch(e){ /* ignore */ }
      return [];
    }

    function render(items){
      data = normalizeToArray(items);
      if(!data.length){ hide(); return; }
      place();
      var html = data.slice(0,8).map(function(p,i){
        var name = (p.prodName||'').replace(/</g,'&lt;').replace(/>/g,'&gt;');
        var no   = (p.prodNo==null?'':p.prodNo);
        return '<div class="ac-item" data-ix="'+i+'" style="padding:6px 8px; cursor:pointer;">'
              + '<span style="color:#888;margin-right:6px;">'+ no +'</span>'+ name +'</div>';
      }).join('');
      $fly.html(html).show();
    }

    function fetchList(q){
      if(!q || q.length<1){ hide(); return; }
      $.ajax({ url: api, type:'GET', data:{ prodNoKeyword:q }, dataType:'json' })
        .done(function(res){ render(res); })
        .fail(function(){ hide(); });
    }

    $kw.on('input', function(){
      var q = this.value.trim();
      clearTimeout(t);
      t = setTimeout(function(){ fetchList(q); }, 180);
    });

    $kw.on('keydown', function(e){
      if(!$fly.is(':visible')) return;
      var max = data.length-1;
      if(e.key==='ArrowDown'){ ix=Math.min(ix+1,max); highlight(); e.preventDefault(); }
      else if(e.key==='ArrowUp'){ ix=Math.max(ix-1,0); highlight(); e.preventDefault(); }
      else if(e.key==='Enter'){ if(ix>=0){ pick(ix); e.preventDefault(); } }
      else if(e.key==='Escape'){ hide(); }
    });

    function highlight(){ $fly.children('.ac-item').css('background','')
                               .eq(ix).css('background','#f5f7fa'); }

    function pick(i){
      if(!data[i]) return;
      $kw.val(data[i].prodName||'');
      hide();
      page = 1;
      loadPage(1);
    }

    $fly.on('mouseenter','.ac-item',function(){ ix=+$(this).data('ix'); highlight(); })
        .on('mousedown', '.ac-item',function(e){ e.preventDefault(); pick(+$(this).data('ix')); });

    $kw.on('blur', function(){ setTimeout(hide,120); });
    $(document).on('mousedown', function(e){
      if($(e.target).closest('#ac-fly, #searchKeyword').length===0) hide();
    });
    $(window).on('scroll resize', function(){ if($fly.is(':visible')) place(); });
  })();

});
</script>

</body>
</html>
