package com.model2.mvc.web.purchase;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.purchase.PurchaseService;

@RestController
@RequestMapping("/purchase") // 공통 prefix
public class PurchaseRestController {

    @Autowired
    @Qualifier("purchaseServiceImpl")
    private PurchaseService purchaseService;

    public PurchaseRestController() {
        System.out.println("==> PurchaseRestController : " + this.getClass());
    }

    /** 구매 등록 : 본문에 Purchase를 보내는 것이 기본.
     *  세션/파라미터 보조(buyerId, prodNo)도 지원해서 테스트 편하게. */
    @PostMapping("json/addPurchase")
    public boolean addPurchase(@RequestBody Purchase purchase,
                               @RequestParam(value="buyerId", required=false) String buyerId,
                               @RequestParam(value="prodNo",  required=false) Integer prodNo,
                               HttpSession session) throws Exception {

        // 세션/파라미터로 보조 세팅(본문에 없을 때만)
        if (purchase.getBuyer() == null) {
            User buyer = null;
            Object u = session.getAttribute("user");
            if (u instanceof User) buyer = (User)u;
            if (buyer == null && buyerId != null) { buyer = new User(); buyer.setUserId(buyerId); }
            purchase.setBuyer(buyer);
        }
        if (purchase.getPurchaseProd() == null && prodNo != null) {
            Product p = new Product(); p.setProdNo(prodNo);
            purchase.setPurchaseProd(p);
        }

        purchaseService.addPurchase(purchase);
        return true;
    }

    /** 단건 조회 */
    @GetMapping("json/getPurchase/{tranNo}")
    public Purchase getPurchase(@PathVariable int tranNo) throws Exception {
        return purchaseService.getPurchase(tranNo);
    }

    /** 구매내역(구매자 기준) */
    @PostMapping("json/getPurchaseListByBuyer")
    public Map<String,Object> getPurchaseListByBuyer(@RequestParam String buyerId,
                                                     @RequestBody Search search) throws Exception {
        return purchaseService.getPurchaseListByBuyer(buyerId, search);
    }

    /** 판매내역(판매자/관리자 화면용) */
    @PostMapping("json/getSaleList")
    public Map<String,Object> getSaleList(@RequestBody Search search) throws Exception {
        return purchaseService.getSaleList(search);
    }

    /** 거래상태 변경(주문/배송/완료 등) */
    @PostMapping("json/updateTranCode")
    public boolean updateTranCode(@RequestParam int tranNo,
                                  @RequestParam String tranCode) throws Exception {
        purchaseService.updateTranCode(tranNo, tranCode);
        return true;
    }

    /** 특정 상품 기준 거래상태 일괄/최근건 변경(서비스 구현에 따름) */
    @PostMapping("json/updateTranCodeByProd")
    public boolean updateTranCodeByProd(@RequestParam int prodNo,
                                        @RequestParam String tranCode) throws Exception {
        purchaseService.updateTranCodeByProd(prodNo, tranCode);
        return true;
    }

    /** 구매 여부 체크(후기/버튼 노출 등에서 사용) */
    @GetMapping("json/hasPurchasedProduct")
    public boolean hasPurchasedProduct(@RequestParam String userId,
                                       @RequestParam int prodNo) throws Exception {
        return purchaseService.hasPurchasedProduct(userId, prodNo);
    }

    /** 최근 거래번호 조회(구매자+상품) */
    @GetMapping("json/getLatestTranNoByBuyerAndProd")
    public int getLatestTranNoByBuyerAndProd(@RequestParam String buyerId,
                                             @RequestParam int prodNo) throws Exception {
        return purchaseService.getLatestTranNoByBuyerAndProd(buyerId, prodNo);
    }
}
