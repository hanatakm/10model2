package com.model2.mvc.service.product.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.common.util.FileUploadUtil;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.product.dao.ProductDao;

@Service("productServiceImpl")
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    // ========== C ==========
    @Override
    public void addProduct(Product product) throws Exception {
        productDao.insertProduct(product);
    }

    // ========== R ==========
    @Override
    public Product getProduct(int prodNo) throws Exception {
        return productDao.findProduct(prodNo);
    }

    @Override
    public List<Product> findProduct(String prodNoKeyword) throws Exception {
        return productDao.findProduct(prodNoKeyword);
    }

    // ========== U ==========
    @Override
    public void updateProduct(Product product) throws Exception {
        productDao.updateProduct(product);
    }

    // ========== List(+paging) ==========
    @Override
    public Map<String, Object> getProductList(Search search) throws Exception {

        // 1) 현재 로그인 유저 role → 관리자만 삭제상품 포함
        boolean showInactive = isCurrentUserAdmin();

        // 2) MyBatis 파라미터 구성(기존 Search 필드 + showInactive)
        Map<String, Object> param = new HashMap<>();
        param.put("searchCondition", search.getSearchCondition());
        param.put("searchKeyword",   search.getSearchKeyword());
        param.put("startRowNum",     search.getStartRowNum());
        param.put("endRowNum",       search.getEndRowNum());
        param.put("showInactive",    showInactive);   // ★ 핵심 플래그

        // 3) 조회 (DAO는 Map 받는 오버로드 메서드가 필요)
        List<Product> list = productDao.getProductList(param);
        int total          = productDao.getTotalCount(param);

        // 4) 페이징 정보
        final int pageUnit = 10;
        Page page = new Page(search.getCurrentPage(), total, pageUnit, search.getPageSize());

        Map<String,Object> out = new HashMap<>();
        out.put("list", list);
        out.put("totalCount", total);
        out.put("resultPage", page);
        return out;
    }

    private boolean isCurrentUserAdmin() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return false;
        HttpSession session = attrs.getRequest().getSession(false);
        if (session == null) return false;
        Object u = session.getAttribute("user");
        if (u instanceof User) {
            String role = ((User) u).getRole();
            return "admin".equalsIgnoreCase(role);
        }
        return false;
    }

    // ========== D (논리삭제) ==========
    @Override
    @Transactional
    public void removeProduct(int prodNo) throws Exception {
        String status = productDao.getProductStatus(prodNo);
        if (status == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다. prodNo=" + prodNo);
        }
        if ("N".equals(status)) {
            return; // 멱등
        }
        int updated = productDao.deactivateProduct(prodNo); // WHERE status='Y'
        if (updated == 0) {
            return; // 경합으로 선행 처리됨
        }
    }

    // ===== 파일 업로드 지원 =====
    @Override @Transactional(rollbackFor = Exception.class)
    public void addProductWithFiles(Product p,
                                    MultipartFile f1,
                                    MultipartFile f2,
                                    MultipartFile f3,
                                    HttpServletRequest req) throws Exception {
        if (f1 != null && !f1.isEmpty()) p.setImageFile( FileUploadUtil.save(f1, req) );
        if (f2 != null && !f2.isEmpty()) p.setImageFile2(FileUploadUtil.save(f2, req) );
        if (f3 != null && !f3.isEmpty()) p.setImageFile3(FileUploadUtil.save(f3, req) );
        addProduct(p);
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void updateProductWithFiles(Product p,
                                       MultipartFile f1,
                                       MultipartFile f2,
                                       MultipartFile f3,
                                       String old1, String old2, String old3,
                                       HttpServletRequest req) throws Exception {
        if (f1 != null && !f1.isEmpty()) {
            String rel = FileUploadUtil.save(f1, req);
            p.setImageFile(rel);
            FileUploadUtil.deleteIfExists(old1, req);
        }
        if (f2 != null && !f2.isEmpty()) {
            String rel = FileUploadUtil.save(f2, req);
            p.setImageFile2(rel);
            FileUploadUtil.deleteIfExists(old2, req);
        }
        if (f3 != null && !f3.isEmpty()) {
            String rel = FileUploadUtil.save(f3, req);
            p.setImageFile3(rel);
            FileUploadUtil.deleteIfExists(old3, req);
        }
        updateProduct(p);
    }

    @Override
    public void touchProductHistory(int prodNo, HttpServletRequest req) {
        HttpSession session = req.getSession();
        @SuppressWarnings("unchecked")
        Deque<String> history = (Deque<String>) session.getAttribute("historyList");
        if (history == null) {
            history = new ArrayDeque<>();
        } else {
            history.remove(String.valueOf(prodNo));
        }
        history.addFirst(String.valueOf(prodNo));
        while (history.size() > 20) { history.removeLast(); }
        session.setAttribute("historyList", history);
    }

    @Override
    public String getProductStatus(int prodNo) throws Exception {
        return productDao.getProductStatus(prodNo);
    }
}
