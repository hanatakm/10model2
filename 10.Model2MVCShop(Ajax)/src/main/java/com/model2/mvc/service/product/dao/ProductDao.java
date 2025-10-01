package com.model2.mvc.service.product.dao;

import java.util.List;
import java.util.Map;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;

public interface ProductDao {

    void insertProduct(Product product) throws Exception;

    Product findProduct(int prodNo) throws Exception;

    List<Product> findProduct(String prodNoKeyword) throws Exception;

    void updateProduct(Product product) throws Exception;

    List<Product> getProductList(Search search) throws Exception;

    int getTotalCount(Search search) throws Exception;
    
    List<Product> getProductList(Map<String,Object> param) throws Exception;
    int getTotalCount(Map<String,Object> param) throws Exception;

    void removeProduct(int prodNo) throws Exception;
    
    String getProductStatus(int prodNo);
    
    int deactivateProduct(int prodNo);
    
    int activateProduct(int prodNo); // 되살리기 필요 시
}
