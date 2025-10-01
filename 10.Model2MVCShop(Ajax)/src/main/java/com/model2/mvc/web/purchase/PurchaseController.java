package com.model2.mvc.web.purchase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model2.mvc.web.purchase.app.PurchasePageService;
import com.model2.mvc.web.purchase.biz.PurchasePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/purchase")
public class PurchaseController {

    @Autowired(required=false)
    private PurchasePageService pageService;

    @Autowired(required=false)
    private PurchasePolicy policy;

    // ===== 화면들 =====
    @RequestMapping("/addPurchaseView")
    public ModelAndView addPurchaseView(HttpServletRequest req,
            @RequestParam(value="prodNo") String prodNo) throws Exception {
        ModelAndView mv = new ModelAndView("forward:/purchase/addPurchaseView.jsp");
        if (pageService != null) pageService.bindAddPurchaseView(req, prodNo);
        return mv;
    }

    @RequestMapping("/getPurchase")
    public ModelAndView getPurchase(HttpServletRequest req,
            @RequestParam(value="tranNo", required=false) String tranNo) throws Exception {
        ModelAndView mv = new ModelAndView("forward:/purchase/getPurchase.jsp");
        if (pageService != null) pageService.bindPurchaseDetail(req, tranNo);
        return mv;
    }

    @RequestMapping("/listPurchase")
    public ModelAndView listPurchase(HttpServletRequest req,
            @RequestParam(value="page",      required=false) String page,
            @RequestParam(value="pageSize",  required=false) String pageSize,
            @RequestParam(value="searchCondition", required=false) String cond,
            @RequestParam(value="searchKeyword",   required=false) String keyword) throws Exception {
        ModelAndView mv = new ModelAndView("forward:/purchase/listPurchase.jsp");
        if (pageService != null) pageService.bindPurchaseListByBuyer(req, null, page, pageSize, cond, keyword);
        return mv;
    }

    @RequestMapping("/listSale")
    public ModelAndView listSale(HttpServletRequest req,
            @RequestParam(value="page",     required=false) String page,
            @RequestParam(value="pageSize", required=false) String pageSize) throws Exception {
        ModelAndView mv = new ModelAndView("forward:/purchase/listSale.jsp");
        if (pageService != null) pageService.bindSaleList(req, page, pageSize);
        return mv;
    }

    @RequestMapping("/updatePurchaseView")
    public ModelAndView updatePurchaseView(HttpServletRequest req,
            @RequestParam(value="tranNo", required=false) String tranNo) throws Exception {
        ModelAndView mv = new ModelAndView("forward:/purchase/updatePurchaseView.jsp");
        if (pageService != null) pageService.bindUpdateView(req, tranNo);
        return mv;
    }

    // ===== 액션(도메인 변경) : 비즈니스는 Policy/Service가 처리 =====
    @RequestMapping("/addPurchase")
    public ModelAndView addPurchase(HttpServletRequest req,
            @RequestParam(value="prodNo",        required=false) String prodNo,
            @RequestParam(value="paymentOption", required=false) String paymentOption,
            @RequestParam(value="receiverName",  required=false) String receiverName,
            @RequestParam(value="receiverPhone", required=false) String receiverPhone,
            @RequestParam(value="divyAddr",      required=false) String divyAddr,
            @RequestParam(value="divyDate",      required=false) String divyDate,
            @RequestParam(value="divyRequest",   required=false) String divyRequest) throws Exception {
        // Policy가 로그인/파라미터/도메인 생성/저장까지 수행하고, 화면 바인딩을 위해 request에 set
        if (policy != null) policy.handleAddPurchase(req, prodNo, paymentOption, receiverName, receiverPhone, divyAddr, divyDate, divyRequest);
        return new ModelAndView("forward:/purchase/getPurchase.jsp");
    }

    @RequestMapping("/updateTranCode")
    public ModelAndView updateTranCode(HttpServletRequest req, HttpServletResponse res,
            @RequestParam(value="tranNo",   required=false) String tranNo,
            @RequestParam(value="tranCode", required=false) String toCode,
            @RequestParam(value="ajax",     required=false) String ajax) throws Exception {
        if (policy == null) return redirect("/purchase/listPurchase");
        // AJAX는 policy가 직접 응답 작성 (null 반환), 동기라면 리다이렉트 경로 리턴
        String redirect = policy.handleUpdateTranCode(req, res, tranNo, toCode, ajax);
        return (redirect == null) ? null : redirect(redirect);
    }

    @RequestMapping("/updateTranCodeByProd")
    public ModelAndView updateTranCodeByProd(HttpServletRequest req,
            @RequestParam(value="prodNo",   required=false) String prodNo,
            @RequestParam(value="tranCode", required=false) String tranCode) throws Exception {
        if (policy != null) policy.handleUpdateTranCodeByProd(req, prodNo, tranCode);
        return redirectBackOr(req, "/purchase/listSale");
    }

    @RequestMapping("/updatePurchase")
    public ModelAndView updatePurchase() {
        // 필요 시 확장: policy.handleUpdatePurchase(...)
        return redirect("/purchase/listPurchase");
    }

    // ==== 작은 헬퍼 ====
    private ModelAndView redirect(String url){ return new ModelAndView("redirect:"+url); }
    private ModelAndView redirectBackOr(HttpServletRequest req, String fallback){
        String ref = req.getHeader("Referer");
        return new ModelAndView("redirect:" + (ref!=null?ref:fallback));
    }
}
