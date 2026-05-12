package org.example.donatebackend.dto.request;

public class GenerateQrRequest {
    private Long methodId;
    private Double amount;

    public Long getMethodId() {
        return methodId;
    }

    public void setMethodId(Long methodId) {
        this.methodId = methodId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


}
