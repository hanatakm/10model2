package com.model2.mvc.common.web;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;

public class ProductGuardInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private ProductService productService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (!uri.contains("getProduct.do")) {
            return true;
        }

        String prodNoStr = request.getParameter("prodNo");
        int prodNo = 0;
        try { prodNo = Integer.parseInt(prodNoStr); } catch(Exception ignore){}

        if (prodNo <= 0) return true; // 컨트롤러에서 폴백

        Product p = productService.getProduct(prodNo);
        if (p == null) return true;   // 컨트롤러에서 notFound 처리

        // 컨트롤러에서 재조회하지 않도록 미리 바인딩(옵션)
        request.setAttribute("prefetchedProduct", p);

        String role = (String) request.getSession().getAttribute("userRole");
        boolean isAdmin = "admin".equalsIgnoreCase(role);
        if (!isAdmin && "SOLD_OUT".equals(p.getStatus())) {
            response.sendRedirect(request.getContextPath()+"/listProduct.do?menu=search&soldout=1");
            return false;
        }
        return true;
    }
}
