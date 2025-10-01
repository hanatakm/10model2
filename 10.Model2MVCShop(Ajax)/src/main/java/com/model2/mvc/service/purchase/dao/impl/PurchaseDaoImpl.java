package com.model2.mvc.service.purchase.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.purchase.dao.PurchaseDao;

@Repository("purchaseDao")
public class PurchaseDaoImpl implements PurchaseDao {

    @Autowired
    private SqlSession sqlSession; // SqlSessionTemplate 이 주입됨

    @Override
    public void insertPurchase(Purchase purchase) throws Exception {
        sqlSession.insert("PurchaseMapper.insertPurchase", purchase);
    }

    @Override
    public Purchase getPurchase(int tranNo) throws Exception {
        return sqlSession.selectOne("PurchaseMapper.getPurchase", tranNo);
    }
 // PurchaseDaoImpl.java
    @Override
    public int getLatestTranNoByBuyerAndProd(String buyerId, int prodNo) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("buyerId", buyerId);
        param.put("prodNo", prodNo);
        return sqlSession.selectOne("PurchaseMapper.getLatestTranNoByBuyerAndProd", param);
    }


    @Override
    public Map<String, Object> getPurchaseListByBuyer(String buyerId, Search search) throws Exception {
        int currentPage = (search.getCurrentPage() == 0) ? 1 : search.getCurrentPage();
        int pageSize    = (search.getPageSize() == 0)    ? 10 : search.getPageSize();
        int startRow    = (currentPage - 1) * pageSize + 1;
        int endRow      = currentPage * pageSize;

        Map<String,Object> param = new HashMap<>();
        param.put("buyerId", buyerId);
        param.put("startRow", startRow);
        param.put("endRow", endRow);

        List<Purchase> list = sqlSession.selectList("PurchaseMapper.getPurchaseListByBuyer", param);
        int totalCount = sqlSession.selectOne("PurchaseMapper.getPurchaseListByBuyerCount", param);

        Map<String,Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        return result;
    }

    @Override
    public Map<String, Object> getSaleList(Search search) throws Exception {
        int currentPage = (search.getCurrentPage() == 0) ? 1 : search.getCurrentPage();
        int pageSize    = (search.getPageSize() == 0)    ? 10 : search.getPageSize();
        int startRow    = (currentPage - 1) * pageSize + 1;
        int endRow      = currentPage * pageSize;

        Map<String,Object> param = new HashMap<>();
        param.put("startRow", startRow);
        param.put("endRow", endRow);

        List<Purchase> list = sqlSession.selectList("PurchaseMapper.getSaleList", param);
        int totalCount      = sqlSession.selectOne("PurchaseMapper.getSaleListCount");

        Map<String,Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        return result;
    }

    @Override
    public void updateTranCode(int tranNo, String tranCode) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put("tranNo", tranNo);
        param.put("tranCode", tranCode);
        sqlSession.update("PurchaseMapper.updateTranCode", param);
    }

    @Override
    public void updateTranCodeByProd(int prodNo, String tranCode) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put("prodNo", prodNo);
        param.put("tranCode", tranCode);
        sqlSession.update("PurchaseMapper.updateTranCodeByProd", param);
    }

    @Override
    public boolean hasPurchasedProduct(String userId, int prodNo) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put("buyerId", userId);
        param.put("prodNo", prodNo);
        Integer cnt = sqlSession.selectOne("PurchaseMapper.hasPurchasedProduct", param);
        return cnt != null && cnt > 0;
    }
}
