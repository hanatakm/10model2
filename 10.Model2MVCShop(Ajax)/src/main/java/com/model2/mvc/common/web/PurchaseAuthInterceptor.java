package com.model2.mvc.common.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model2.mvc.service.domain.User;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class PurchaseAuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String uri = req.getRequestURI();

        // 로그인 필요: 구매작성/등록/내구매목록/상태변경
        boolean needLogin = uri.contains("/purchase/addPurchaseView")
                         || uri.contains("/purchase/addPurchase")
                         || uri.contains("/purchase/listPurchase")
                         || uri.contains("/purchase/updateTranCode")
                         || uri.contains("/purchase/updateTranCodeByProd");

        // 관리자만: 판매목록
        boolean needAdmin = uri.contains("/purchase/listSale");

        if (!needLogin && !needAdmin) return true;

        HttpSession ss = req.getSession(false);
        User user = (ss!=null) ? (User)ss.getAttribute("user") : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath()+"/user/loginView.jsp?redirectURL="+uri);
            return false;
        }

        if (needAdmin && (user.getRole()==null || !"admin".equalsIgnoreCase(user.getRole()))) {
            res.sendRedirect(req.getContextPath()+"/index.jsp");
            return false;
        }

        return true;
    }
}
