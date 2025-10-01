package com.model2.mvc.service.product;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;

public interface ProductService {

    // C
    void addProduct(Product product) throws Exception;

    // R
    Product getProduct(int prodNo) throws Exception;

    // 번호 부분검색
    List<Product> findProduct(String prodNoKeyword) throws Exception;

    // U
    void updateProduct(Product product) throws Exception;

    // 목록+페이징( totalCount 함께 반환 )
    Map<String, Object> getProductList(Search search) throws Exception;

    // (선택) D 필요하면 인터페이스에 추가:
     void removeProduct(int prodNo) throws Exception;
     
     
     /** product.status (Y/N) 원시 상태 조회 */
     String getProductStatus(int prodNo) throws Exception;
 

     
     void touchProductHistory(int prodNo, HttpServletRequest req) throws Exception;
   
     
     // ⬇️ 3장 지원: 파일 파라미터는 null/empty면 무시
     void addProductWithFiles(Product p,
                              MultipartFile file1,
                              MultipartFile file2,
                              MultipartFile file3,
                              HttpServletRequest req) throws Exception;

     void updateProductWithFiles(Product p,
                                 MultipartFile file1,
                                 MultipartFile file2,
                                 MultipartFile file3,
                                 String old1, String old2, String old3,
                                 HttpServletRequest req) throws Exception;
 }