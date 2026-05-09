package org.example.donatebackend.dto.response;

public class PaymentAccountResponse {

    private String orderCode;   // ID donation (dùng để tracking)
    private String qrUrl;       // link QR
    private Double amount;        // số tiền
    private String addInfo;     // content để match webhook
    private String status;      // PENDING / PAID

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAddInfo() {
        return addInfo;
    }

    public void setAddInfo(String addInfo) {
        this.addInfo = addInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}