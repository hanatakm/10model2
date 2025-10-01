package com.model2.mvc.common.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.purchase.PurchaseService;

public class PurchaseFlagPostInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private PurchaseService purchaseService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {

        String uri = request.getRequestURI();
        if (!uri.contains("listProduct")) return;

        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) return;

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) request.getAttribute("map");
        if (map == null) return;

        @SuppressWarnings("unchecked")
        List<Product> list = (List<Product>) map.get("list");
        if (list == null) return;

        for (Product p : list) {
            boolean hasPurchased = purchaseService.hasPurchasedProduct(loginUser.getUserId(), p.getProdNo());
            p.setShowArrivedButton(hasPurchased);
        }
    }
}
