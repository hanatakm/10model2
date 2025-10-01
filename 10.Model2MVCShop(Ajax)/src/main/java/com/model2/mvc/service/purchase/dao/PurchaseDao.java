package com.model2.mvc.service.purchase.dao;

import java.util.Map;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Purchase;

public interface PurchaseDao {

    void insertPurchase(Purchase purchase) throws Exception;
    
    int getLatestTranNoByBuyerAndProd(String buyerId, int prodNo) throws Exception;
   

    Purchase getPurchase(int tranNo) throws Exception;

    Map<String, Object> getPurchaseListByBuyer(String buyerId, Search search) throws Exception;

    Map<String, Object> getSaleList(Search search) throws Exception;

    void updateTranCode(int tranNo, String tranCode) throws Exception;

    void updateTranCodeByProd(int prodNo, String tranCode) throws Exception;

    boolean hasPurchasedProduct(String userId, int prodNo) throws Exception;
}
