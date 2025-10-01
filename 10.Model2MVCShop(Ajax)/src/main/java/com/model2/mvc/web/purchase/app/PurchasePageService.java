package com.model2.mvc.web.purchase.app;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.purchase.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * PurchasePageService
 * - 컨트롤러(Controller)에서 화면에 바인딩할 데이터(모델)를 채워 넣는 "페이지 조립" 전담 서비스.
 * - 비즈니스 로직(구매 생성/조회/목록 등)은 PurchaseService에 위임하고,
 *   여기서는 HttpServletRequest에 setAttribute()로 JSP에서 쓸 값만 정리해 담는다.
 * - 장점: 컨트롤러는 라우팅/네비게이션에 집중하고, 화면 바인딩 규칙은 한 곳에 모여 재사용/유지보수 용이.
 */
@Service
public class PurchasePageService {

    // 구매 도메인 비즈니스 서비스 주입
    // - 실제 DB 접근/트랜잭션/검증 등 비즈니스 로직은 PurchaseService가 책임진다.
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 구매 작성 화면 진입 시 필요한 값 바인딩
     * @param req       HttpServletRequest (JSP에서 꺼내 쓸 request scope)
     * @param prodNoStr 파라미터로 넘어온 상품번호 문자열(널/공백 가능)
     *
     * 동작:
     * - 현재는 단순히 상품번호만 request에 저장.
     * - "필요 시" 주석처럼, 상품 상세 조회를 더 해 request.setAttribute("product", ...) 로 담아두면
     *   폼에서 상품명/가격 등을 미리 보여줄 수 있다.
     */
    public void bindAddPurchaseView(HttpServletRequest req, String prodNoStr) {
        // TODO(확장 포인트): productService.getProduct(...) 로 상세 조회 후 "product" 속성 추가 가능
        req.setAttribute("prodNo", prodNoStr);
    }

    /**
     * 구매 단건 상세 조회 바인딩
     * @param req        HttpServletRequest
     * @param tranNoStr  트랜잭션(구매) 번호 문자열
     * @throws Exception 내부적으로 purchaseService.getPurchase() 에서 발생 가능한 예외 전파
     *
     * 동작:
     * - tranNoStr를 안전하게 int로 변환(safeInt) 후, 서비스에서 Purchase를 조회해 "purchase"로 바인딩.
     * - JSP에서는 ${purchase.프로퍼티} 형태로 상세 정보 출력 가능.
     */
    public void bindPurchaseDetail(HttpServletRequest req, String tranNoStr) throws Exception {
        int tranNo = safeInt(tranNoStr, 0); // 숫자 변환 실패 시 0(의미 없는 기본값)
        req.setAttribute("purchase", purchaseService.getPurchase(tranNo));
    }

    /**
     * 내가 산 목록(구매자별 구매 리스트) 바인딩
     * @param req          HttpServletRequest
     * @param buyerId      명시적 구매자 ID(널 가능) — 없으면 세션의 로그인 사용자에서 추출
     * @param pageStr      현재 페이지 문자열(널/공백 가능)
     * @param pageSizeStr  페이지 사이즈 문자열(널/공백 가능)
     * @param cond         검색조건(예: "0","1","2" 등), 구현체 규칙에 따름
     * @param keyword      검색어(널 가능)
     * @throws Exception   서비스 호출 중 예외 전파
     *
     * 동작:
     * 1) buyerId 우선순위:
     *    - 파라미터 buyerId가 있으면 그대로 사용
     *    - 없으면 세션의 "user" 속성에서 User 객체를 꺼내 userId 사용
     *    - 둘 다 없으면 null → 서비스 구현에 따라 NPE/검증 실패 가능(주의)
     * 2) Search 객체에 페이징/검색 조건 주입
     * 3) purchaseService.getPurchaseListByBuyer(...) 호출
     * 4) 결과 Map을 "map", 검색조건을 "search"로 request에 바인딩
     *    - 관례적으로 map에는 "list", "totalCount" 등이 들어있다(구현체에 따라 상이)
     * 5) JSP에서 ${map.list}, ${map.totalCount}, ${search.currentPage} 등 접근
     */
    public void bindPurchaseListByBuyer(HttpServletRequest req, String buyerId,
            String pageStr, String pageSizeStr,
            String cond, String keyword) throws Exception {

String id = (buyerId!=null ? buyerId :
(req.getSession().getAttribute("user")!=null ?
((com.model2.mvc.service.domain.User)req.getSession().getAttribute("user")).getUserId() : null));
System.out.println("[listPurchase] buyerId=" + id); 
Search search = new Search();
search.setCurrentPage(safeInt(pageStr, 1));
search.setPageSize(safeInt(pageSizeStr, 10));
search.setSearchCondition(cond);
search.setSearchKeyword(keyword);

Map<String,Object> map = purchaseService.getPurchaseListByBuyer(id, search);

// 👉 resultPage 흉내낼 데이터 채워주기
int totalCount = (int)map.get("totalCount");
int currentPage = search.getCurrentPage();
int pageSize = search.getPageSize();

Map<String,Object> resultPage = new java.util.HashMap<>();
resultPage.put("totalCount", totalCount);
resultPage.put("currentPage", currentPage);
resultPage.put("pageSize", pageSize);
resultPage.put("pageUnit", 5); // 고정값 (원하면 param으로 조정 가능)

req.setAttribute("map", map);
req.setAttribute("search", search);
req.setAttribute("resultPage", resultPage); // ✅ JSP에서 그대로 사용 가능
}

    /**
     * 판매 내역(판매자 관점 리스트) 바인딩
     * @param req          HttpServletRequest
     * @param pageStr      현재 페이지 문자열
     * @param pageSizeStr  페이지 사이즈 문자열
     * @throws Exception   서비스 호출 중 예외 전파
     *
     * 동작:
     * - purchaseService.getSaleList(search) 호출 결과를 "map"과 "search"로 바인딩.
     * - 보통 관리자/판매자 화면에서 사용.
     */
    public void bindSaleList(HttpServletRequest req, String pageStr, String pageSizeStr) throws Exception {
        Search search = new Search();
        search.setCurrentPage(safeInt(pageStr, 1));
        search.setPageSize(safeInt(pageSizeStr, 10));
        Map<String,Object> map = purchaseService.getSaleList(search);
        req.setAttribute("map", map);
        req.setAttribute("search", search);
    }

    /**
     * 구매 수정 화면 진입용 바인딩
     * @param req       HttpServletRequest
     * @param tranNoStr 트랜잭션 번호 문자열
     * @throws Exception 내부 조회 예외 전파
     *
     * 동작:
     * - 실질적으로 상세 바인딩과 동일하므로 bindPurchaseDetail을 재사용(위임).
     * - 수정 폼에서 기존 값을 미리 채워두기 위한 용도.
     */
    public void bindUpdateView(HttpServletRequest req, String tranNoStr) throws Exception {
        bindPurchaseDetail(req, tranNoStr);
    }

    // ----------------------------------------------------------
    // 유틸리티: 안전한 int 파싱
    // ----------------------------------------------------------
    /**
     * 문자열을 int로 변환하되, null/공백/숫자 아님 → 기본값 반환
     * @param s   변환할 문자열
     * @param def 변환 실패 시 반환할 기본값
     * @return    변환된 int 또는 def
     *
     * 특징:
     * - 트림 후 빈 문자열도 기본값 처리
     * - NumberFormatException 등 모든 예외를 잡아 기본값 반환
     * - 파라미터 신뢰도가 낮은 웹 레이어에서 매우 유용
     */
    private static int safeInt(String s, int def){
        try{
            return (s==null||s.trim().isEmpty()) ? def : Integer.parseInt(s.trim());
        }catch(Exception e){
            return def;
        }
    }
}
