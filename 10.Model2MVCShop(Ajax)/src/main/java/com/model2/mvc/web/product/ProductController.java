package com.model2.mvc.web.product;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductPageService;
import com.model2.mvc.service.product.ProductService;


@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired(required = false) 
    @Qualifier("productServiceImpl")
    private ProductService productService;

    @Autowired
    private ProductPageService pageService;

    // ======= 등록 화면(GET) =======
    @RequestMapping(value="/addProduct", method=RequestMethod.GET)
    public String addProductView() throws Exception {
        return "redirect:/product/addProductView.jsp";
    }

    // ======= 등록 처리(POST) =======
    @RequestMapping(value="/addProduct", method=RequestMethod.POST)
    public String addProduct(Product product,
                             @RequestParam(value="uploadFile",  required=false) MultipartFile uploadFile,
                             @RequestParam(value="uploadFile2", required=false) MultipartFile uploadFile2,
                             @RequestParam(value="uploadFile3", required=false) MultipartFile uploadFile3,
                             HttpServletRequest request) throws Exception {

        productService.addProductWithFiles(product, uploadFile, uploadFile2, uploadFile3, request);
        return "redirect:/product/getProduct?prodNo="+product.getProdNo()+"&menu=manage&from=add";
    }


    // ======= 단건 조회(GET) =======
    @RequestMapping(value="/getProduct", method={RequestMethod.GET, RequestMethod.POST})
    public String getProduct(HttpServletRequest request,
                             @RequestParam(value="prodNo", required=false) String prodNoStr,
                             @RequestParam(value="menu",   required=false) String menu,
                             Model model) throws Exception {

        int prodNo = (prodNoStr == null || prodNoStr.trim().isEmpty())
                     ? 0 : Integer.parseInt(prodNoStr.trim());

        pageService.bindProductDetail(request, prodNoStr, menu);

        Product p = productService.getProduct(prodNo);           // ← 소문자 인스턴스
        model.addAttribute("product", p);

        String activeFlag = productService.getProductStatus(prodNo);
        model.addAttribute("activeFlag", activeFlag);                // "Y"/"N"

        if (prodNo > 0) {
            productService.touchProductHistory(prodNo, request);
        }

        return "forward:/product/getProduct.jsp";
    }




    // ======= 목록 조회(GET/POST) =======
    @RequestMapping(value="/listProduct", method={RequestMethod.GET, RequestMethod.POST})
    public String listProduct(HttpServletRequest request,
                              @RequestParam(value="menu",            required=false) String menu,
                              @RequestParam(value="currentPage",            required=false) String pageStr,
                              @RequestParam(value="searchCondition", required=false) String condStr,
                              @RequestParam(value="searchKeyword",   required=false) String keyword) throws Exception {

        pageService.bindProductList(request, menu, pageStr, condStr, keyword);
        return "forward:/product/listProduct.jsp";
    }

    // ======= 수정 처리(POST) =======
    @RequestMapping(value="/updateProduct", method=RequestMethod.POST)
    public String updateProduct(@RequestParam("prodNo") String prodNoStr,
                                Product form,
                                @RequestParam(value="uploadFile",  required=false) MultipartFile uploadFile,
                                @RequestParam(value="uploadFile2", required=false) MultipartFile uploadFile2,
                                @RequestParam(value="uploadFile3", required=false) MultipartFile uploadFile3,
                                HttpServletRequest request) throws Exception {

        int prodNo = Integer.parseInt(prodNoStr.trim());
        Product current = productService.getProduct(prodNo);

        form.setProdNo(prodNo);

        // 파일 미선택이면 기존 값 유지 (Service에서 새 파일만 바뀌도록 설계돼 있음)
        if ((uploadFile  == null || uploadFile.isEmpty()) &&
            (uploadFile2 == null || uploadFile2.isEmpty()) &&
            (uploadFile3 == null || uploadFile3.isEmpty())) {
            form.setImageFile( current != null ? current.getImageFile()  : null );
            form.setImageFile2(current != null ? current.getImageFile2() : null );
            form.setImageFile3(current != null ? current.getImageFile3() : null );
            productService.updateProduct(form);
        } else {
            String old1 = current != null ? current.getImageFile()  : null;
            String old2 = current != null ? current.getImageFile2() : null;
            String old3 = current != null ? current.getImageFile3() : null;
            productService.updateProductWithFiles(form, uploadFile, uploadFile2, uploadFile3, old1, old2, old3, request);
        }

        return "redirect:/product/getProduct?prodNo="+prodNo+"&menu=manage&from=update";
    }


    // ======= 수정 화면(GET) =======
    @RequestMapping(value="/updateProductView", method=RequestMethod.GET)
    public String updateProductView(HttpServletRequest request,
                                    @RequestParam("prodNo") String prodNoStr,
                                    @RequestParam(value="menu", required=false) String menu
                                ) throws Exception {
        pageService.bindUpdateView(request, prodNoStr, menu);
        return "forward:/product/updateProductView.jsp";
    }
    


    

        // ... (기존 getProduct 등)

        /** 삭제(비활성화) */
        @PostMapping("/remove")
        public String remove(@RequestParam("prodNo") int prodNo,
                             @RequestParam(value="menu", required=false, defaultValue="manage") String menu,
                             RedirectAttributes ra) throws Exception {

            productService.removeProduct(prodNo);   // status='N'으로 논리삭제
            ra.addFlashAttribute("msg", "상품이 비활성화되었습니다.");

            // 목록으로 리다이렉트
            return "redirect:/product/listProduct?menu=" + menu;
        }
    


    // ===== util =====
    private static int safeInt(String s, int def) {
        try { 
            return (s==null || s.trim().isEmpty()) ? def : Integer.parseInt(s.trim()); 
        } catch(Exception e){ 
            return def; 
        }
    }
}
