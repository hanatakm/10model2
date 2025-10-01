package com.model2.mvc.service.product;

import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.product.impl.ProductServiceImpl;

@Service
public class ProductPageService {

    @Resource(name="commonProperties")
    private Properties commonProps; // pageUnit/pageSize

    @Autowired(required=false) @Qualifier("productServiceImpl")
    private ProductService productService;
    private ProductService ps() {
        return productService != null ? productService : new ProductServiceImpl();
    }

    // getProduct 화면 바인딩(컨트롤러에서 호출)
    public void bindProductDetail(HttpServletRequest request, String prodNoStr, String menu) throws Exception {
        int prodNo = safeInt(prodNoStr, 0);
        menu = normalizeMenu(menu, "search");

        // ProductGuardInterceptor 가 preHandle에서 product를 미리 올려놓았다면 재사용
        Product product = (Product) request.getAttribute("prefetchedProduct");
        if (product == null && prodNo > 0) {
            product = ps().getProduct(prodNo);
        }

        if (product == null || prodNo <= 0) {
            request.setAttribute("redirectTo", "redirect:/listProduct.do?menu="+menu+"&notFound=1");
            return;
        }

        request.setAttribute("product", product);
        request.setAttribute("menu", menu);
        request.setAttribute("viewName", "/product/getProduct.jsp");
    }

    // listProduct 화면 바인딩(컨트롤러에서 호출)
    public void bindProductList(HttpServletRequest request, String menu, String pageStr, String condStr, String keyword) throws Exception {
        int currentPage = safeInt(pageStr, 1);
        int pageUnit = safeInt(commonProps.getProperty("pageUnit"), 5);
        int pageSize = safeInt(commonProps.getProperty("pageSize"), 3);

        Search search = new Search();
        search.setCurrentPage(currentPage);
        search.setPageSize(pageSize);
        search.setSearchCondition(condStr);
        search.setSearchKeyword(keyword);

        Map<String, Object> map = ps().getProductList(search);
        int totalCount = ((Integer) map.get("totalCount")).intValue();
        Page resultPage = new Page(currentPage, totalCount, pageUnit, pageSize);

        if (menu == null || menu.isEmpty()) menu = "manage";

        request.setAttribute("map", map);
        request.setAttribute("search", search);
        request.setAttribute("resultPage", resultPage);
        request.setAttribute("menu", menu);
        request.setAttribute("viewName", "/product/listProduct.jsp");
    }

    public void bindUpdateView(HttpServletRequest request, String prodNoStr, String menu) throws Exception {
        int prodNo = safeInt(prodNoStr, 0);
        if (menu == null || menu.isEmpty()) menu = "manage";
        Product product = ps().getProduct(prodNo);
        request.setAttribute("product", product);
        request.setAttribute("menu", menu);
        request.setAttribute("viewName", "/product/updateProductView.jsp");
    }

    // util
    private static int safeInt(String s, int def) {
        try { return (s==null||s.trim().isEmpty()) ? def : Integer.parseInt(s.trim()); }
        catch(Exception e){ return def; }
    }
    private static String normalizeMenu(String menu, String def) {
        if (menu==null || menu.isEmpty()) return def;
        if (!"manage".equals(menu) && !"search".equals(menu)) return def;
        return menu;
    }
}
