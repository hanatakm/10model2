package com.model2.mvc.common.web;

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class RecentViewedInterceptor extends HandlerInterceptorAdapter {

    private static final int RECENT_MAX = 10;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {

        String uri = request.getRequestURI();
        if (!uri.contains("getProduct")) return;

        String prodNoStr = request.getParameter("prodNo");
        if (prodNoStr == null || prodNoStr.trim().isEmpty()) return;

        @SuppressWarnings("unchecked")
        LinkedList<String> recent =
                (LinkedList<String>) request.getSession().getAttribute("recentProdNos");
        if (recent == null) recent = new LinkedList<>();

        recent.remove(prodNoStr);
        recent.addFirst(prodNoStr);
        while (recent.size() > RECENT_MAX) recent.removeLast();

        request.getSession().setAttribute("recentProdNos", recent);
    }
}
