package com.model2.mvc.service.product.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.dao.ProductDao;

@Repository("productDao")
public class ProductDaoImpl implements ProductDao {

    @Autowired
    private SqlSession sqlSession;

    private static final String NS = "ProductMapper.";

    @Override
    public void insertProduct(Product product) throws Exception {
        sqlSession.insert(NS + "addProduct", product);
    }

    @Override
    public Product findProduct(int prodNo) throws Exception {
        return sqlSession.selectOne(NS + "getProduct", prodNo);
    }

    @Override
    public List<Product> findProduct(String prodNoKeyword) throws Exception {
        return sqlSession.selectList(NS + "findProduct", prodNoKeyword);
    }

    @Override
    public void updateProduct(Product product) throws Exception {
        sqlSession.update(NS + "updateProduct", product);
    }

    @Override
    public List<Product> getProductList(Search search) throws Exception {
        return sqlSession.selectList(NS + "getProductList", search);
    }

    @Override
    public int getTotalCount(Search search) throws Exception {
        return sqlSession.selectOne(NS + "getTotalCount", search);
    }

 // ProductDaoImpl.java
    @Override
    public List<Product> getProductList(Map<String,Object> param) {
        return sqlSession.selectList(NS + "getProductList", param);
    }
    @Override
    public int getTotalCount(Map<String,Object> param) {
        return sqlSession.selectOne(NS + "getTotalCount", param);
    }

    
    
    /** 호출 ID는 유지하지만, 매퍼에서 'UPDATE status=N'로 구현 */
    @Override
    public void removeProduct(int prodNo) throws Exception {
        sqlSession.update(NS + "removeProduct", prodNo);
    }

    // === status 전용 ===
    @Override
    public String getProductStatus(int prodNo) {
        return sqlSession.selectOne(NS + "getProductStatus", prodNo);
    }

    @Override
    public int deactivateProduct(int prodNo) {
        return sqlSession.update(NS + "deactivateProduct", prodNo);
    }

    @Override
    public int activateProduct(int prodNo) {
        return sqlSession.update(NS + "activateProduct", prodNo);
    }
}
