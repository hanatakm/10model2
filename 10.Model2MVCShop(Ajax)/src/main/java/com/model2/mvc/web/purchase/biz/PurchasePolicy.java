package com.model2.mvc.web.purchase.biz;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.purchase.PurchaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class PurchasePolicy {

    @Autowired
    private PurchaseService purchaseService;

    // AddPurchase: 로그인/파라미터 검증 + 도메인 생성 + 저장 + request 바인딩
    public void handleAddPurchase(HttpServletRequest req,
                                  String prodNoStr, String paymentOption,
                                  String receiverName, String receiverPhone,
                                  String divyAddr, String divyDate, String divyRequest) throws Exception {

        User user = (User) req.getSession().getAttribute("user");
        if (user == null) throw new IllegalStateException("LOGIN_REQUIRED");

        int prodNo = safeInt(prodNoStr, 0);

        Purchase vo = new Purchase();
        Product p  = new Product(); p.setProdNo(prodNo);
        vo.setPurchaseProd(p);
        vo.setBuyer(user);
        vo.setPaymentOption(nz(paymentOption));
        vo.setReceiverName(nz(receiverName));
        vo.setReceiverPhone(nz(receiverPhone));
        vo.setDivyAddr(nz(divyAddr));
        vo.setDivyDate(nz(divyDate));
        vo.setDivyRequest(nz(divyRequest));

        purchaseService.addPurchase(vo); // tranNo 세팅
        req.setAttribute("purchase", vo);
    }

    // UpdateTranCode: 권한/전이검증 + 저장 + (ajax면 직접응답) + 리다이렉트 경로 반환
    public String handleUpdateTranCode(HttpServletRequest req, HttpServletResponse res,
                                       String tranNoStr, String toCode, String ajaxFlag) throws Exception {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) return "/user/loginView.jsp";

        int tranNo = safeInt(tranNoStr, 0);
        boolean isAjax = "1".equals(ajaxFlag) ||
                "XMLHttpRequest".equalsIgnoreCase(nz(req.getHeader("X-Requested-With")));

        Purchase order = purchaseService.getPurchase(tranNo);
        if (order == null) {
            if (isAjax) { res.setStatus(404); res.getWriter().write("notfound"); return null; }
            return "/purchase/listPurchase";
        }

        boolean isAdmin = "admin".equalsIgnoreCase(nz(user.getRole()));
        boolean isBuyer = user.getUserId().equals(order.getBuyer().getUserId());
        String cur = nz(order.getTranCode());

        // 전이 규칙
        if ("DLV".equalsIgnoreCase(toCode)) {
            if (!(isAdmin && ("ODR".equalsIgnoreCase(cur) || "ORD".equalsIgnoreCase(cur)))) {
                return forbidden(res, isAjax, isAdmin?"/purchase/listSale":"/purchase/listPurchase");
            }
        } else if ("CMP".equalsIgnoreCase(toCode)) {
            if (!((isBuyer || isAdmin) && "DLV".equalsIgnoreCase(cur))) {
                return forbidden(res, isAjax, isAdmin?"/purchase/listSale":"/purchase/listPurchase");
            }
        } else {
            if (isAjax) { res.setStatus(400); res.getWriter().write("badcode"); return null; }
            return "/index.jsp";
        }

        purchaseService.updateTranCode(tranNo, toCode);

        if (isAjax) { res.setContentType("text/plain; charset=UTF-8"); res.getWriter().write("ok"); return null; }
        String ref = req.getHeader("Referer");
        return (ref!=null) ? ref : (isAdmin ? "/purchase/listSale" : "/purchase/listPurchase");
    }

    public void handleUpdateTranCodeByProd(HttpServletRequest req, String prodNoStr, String tranCode) throws Exception {
        int prodNo = safeInt(prodNoStr, 0);
        purchaseService.updateTranCodeByProd(prodNo, tranCode);
    }

    // ===== util =====
    private static String nz(String s){ return (s==null)?"":s; }
    private static int safeInt(String s, int def){
        try{ return (s==null||s.trim().isEmpty())?def:Integer.parseInt(s.trim()); }catch(Exception e){ return def; }
    }
    private String forbidden(HttpServletResponse res, boolean isAjax, String nonAjaxRedirect) throws Exception{
        if (isAjax){ res.setStatus(403); res.getWriter().write("forbidden"); return null; }
        return nonAjaxRedirect;
    }
}
