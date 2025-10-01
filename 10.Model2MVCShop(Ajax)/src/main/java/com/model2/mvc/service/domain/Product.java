package com.model2.mvc.service.domain;

import java.sql.Date;

public class Product {

    private int    prodNo;
    private String prodName;
    private String prodDetail;
    /** YYYYMMDD 형태(예: 20250910). 입력이 2025-09-10 형태여도 Mapper/DAO에서 REPLACE 처리 */
    private String manufactureDay;
    private int    price;
    /** 요구사항: 이미지 대신 텍스트 파일 이름만 저장할 수 있음 */
    private String imageFile;
    private String imageFile2;
    private String imageFile3;
    private Date   regDate;
    private String activeFlag; 
    public String getActiveFlag() { return activeFlag; }
    public void setActiveFlag(String activeFlag) { this.activeFlag = activeFlag; }
    
    /** 파생 상태: SOLD_OUT / ON_SALE */
    private String status;
    /** 거래상태 코드(최근 거래기준): CMP/003/3 등 */
    private String tranCode;
    
    

    /** 화면 제어용(옵션): ‘도착했습니다’ 버튼 표시 여부 */
    private boolean showArrivedButton;

    public Product() {}

    // === Getter / Setter ===
    public int getProdNo() { return prodNo; }
    public void setProdNo(int prodNo) { this.prodNo = prodNo; }

    public String getProdName() { return prodName; }
    public void setProdName(String prodName) { this.prodName = prodName; }

    public String getProdDetail() { return prodDetail; }
    public void setProdDetail(String prodDetail) { this.prodDetail = prodDetail; }

    public String getManufactureDay() { return manufactureDay; }
    public void setManufactureDay(String manufactureDay) { this.manufactureDay = manufactureDay; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getImageFile() { return imageFile; }
    public void setImageFile(String imageFile) { this.imageFile = imageFile; }
    public String getImageFile2() {
        return imageFile2;
    }
    public void setImageFile2(String imageFile2) {
        this.imageFile2 = imageFile2;
    }

    public String getImageFile3() {
        return imageFile3;
    }
    public void setImageFile3(String imageFile3) {
        this.imageFile3 = imageFile3;
    }

    public Date getRegDate() { return regDate; }
    public void setRegDate(Date regDate) { this.regDate = regDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTranCode() { return tranCode; }
    public void setTranCode(String tranCode) { this.tranCode = tranCode; }

    public boolean isShowArrivedButton() { return showArrivedButton; }
    public void setShowArrivedButton(boolean showArrivedButton) { this.showArrivedButton = showArrivedButton; }

    @Override
    public String toString() {
        return "Product{prodNo=" + prodNo + ", prodName='" + prodName + "', price=" + price + "}";
    }
}
