package com.model2.mvc.service.domain;

import java.io.Serializable;
import java.sql.Date;

public class Purchase implements Serializable {
	private static final long serialVersionUID = 1L;

	private User buyer; // buyer_id
	private String divyAddr; // DEMAILADDR (스키마 철자 확인!)
	private String divyDate; // dlvy_date (YYYYMMDD 문자열이면 String 유지)
	private String divyRequest; // dlvy_request
	private Date orderDate; // order_date (JDBC용이면 java.sql.Date OK)
	private String paymentOption; // payment_option: "CSH","CRD" 등
	private Product purchaseProd; // prod_no
	private String receiverName; // receiver_name
	private String receiverPhone; // receiver_phone
	private String tranCode; // tran_status_code: "ODR","DLV","CMP" 등
	private int tranNo; // tran_no (PK)

	public Purchase() {
	}

	// ===== 선택: enum 보조 =====
	public enum TranCode {
		ORDERED("ODR"), SHIPPING("DLV"), COMPLETED("CMP");

		public final String code;

		TranCode(String c) {
			this.code = c;
		}

		public static TranCode from(String c) {
			for (TranCode t : values())
				if (t.code.equals(c))
					return t;
			return null;
		}
	}

	public TranCode getTranCodeEnum() {
		return TranCode.from(tranCode);
	}

	public void setTranCodeEnum(TranCode t) {
		this.tranCode = (t != null ? t.code : null);
	}

	// ===== getters/setters (네 코드 그대로 유지) =====
	public User getBuyer() {
		return buyer;
	}

	public void setBuyer(User buyer) {
		this.buyer = buyer;
	}

	public String getDivyAddr() {
		return divyAddr;
	}

	public void setDivyAddr(String divyAddr) {
		this.divyAddr = divyAddr;
	}

	public String getDivyDate() {
		return divyDate;
	}

	public void setDivyDate(String divyDate) {
		this.divyDate = divyDate;
	}

	public String getDivyRequest() {
		return divyRequest;
	}

	public void setDivyRequest(String divyRequest) {
		this.divyRequest = divyRequest;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public String getPaymentOption() {
		return paymentOption;
	}

	public void setPaymentOption(String paymentOption) {
		this.paymentOption = paymentOption;
	}

	public Product getPurchaseProd() {
		return purchaseProd;
	}

	public void setPurchaseProd(Product purchaseProd) {
		this.purchaseProd = purchaseProd;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getReceiverPhone() {
		return receiverPhone;
	}

	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}

	public String getTranCode() {
		return tranCode;
	}

	public void setTranCode(String tranCode) {
		this.tranCode = tranCode;
	}

	public int getTranNo() {
		return tranNo;
	}

	public void setTranNo(int tranNo) {
		this.tranNo = tranNo;
	}

	@Override
	public String toString() {
		return "Purchase [buyer=" + buyer + ", divyAddr=" + divyAddr + ", divyDate=" + divyDate + ", divyRequest="
				+ divyRequest + ", orderDate=" + orderDate + ", paymentOption=" + paymentOption + ", purchaseProd="
				+ purchaseProd + ", receiverName=" + receiverName + ", receiverPhone=" + receiverPhone + ", tranCode="
				+ tranCode + ", tranNo=" + tranNo + "]";
	}
}
