package com.model2.mvc.service.purchase.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.purchase.PurchaseService;
import com.model2.mvc.service.purchase.dao.PurchaseDao;
import com.model2.mvc.service.purchase.dao.impl.PurchaseDaoImpl;



@Service("purchaseServiceImpl")
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseDao purchaseDao;

    // @Autowired로 PurchaseDaoImpl을 주입받을 수 있도록 수정
    @Autowired
    public PurchaseServiceImpl(PurchaseDao purchaseDao) {
        this.purchaseDao = purchaseDao;
    }
 
   
    @Override
    public void addPurchase(Purchase purchase) throws Exception {
        if (purchase == null || purchase.getBuyer() == null || purchase.getPurchaseProd() == null) {
            throw new IllegalArgumentException("invalid purchase");
        }
        purchaseDao.insertPurchase(purchase);
    }

    @Override
    public Purchase getPurchase(int tranNo) throws Exception {
        return purchaseDao.getPurchase(tranNo);
    }

    @Override
    public Map<String, Object> getPurchaseListByBuyer(String buyerId, Search search) throws Exception {
        if (buyerId == null ||  buyerId.trim().isEmpty()) {
            throw new IllegalArgumentException("buyerId blank");
        }
        return purchaseDao.getPurchaseListByBuyer(buyerId, search);
    }

    @Override
    public Map<String, Object> getSaleList(Search search) throws Exception {
        return purchaseDao.getSaleList(search);
    }

    @Override
    public void updateTranCode(int tranNo, String tranCode) throws Exception {
        purchaseDao.updateTranCode(tranNo, tranCode);
    }

    @Override
    public void updateTranCodeByProd(int prodNo, String tranCode) throws Exception {
        purchaseDao.updateTranCodeByProd(prodNo, tranCode);
    }

    
 // PurchaseServiceImpl.java
    @Override
    public int getLatestTranNoByBuyerAndProd(String buyerId, int prodNo) throws Exception {
        return purchaseDao.getLatestTranNoByBuyerAndProd(buyerId, prodNo);
    }



 // PurchaseServiceImpl.java
    @Override
    public boolean hasPurchasedProduct(String userId, int prodNo) throws Exception {
        if (userId == null || userId.trim().isEmpty()) return false;
        return purchaseDao.hasPurchasedProduct(userId, prodNo);
    }

}
