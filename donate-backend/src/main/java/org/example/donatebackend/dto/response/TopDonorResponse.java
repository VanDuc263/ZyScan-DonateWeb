package org.example.donatebackend.dto.response;


public class TopDonorResponse {
    private String donorName;
    private Double totalAmount;



    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "TopDonorResponse{" +
                "donorName='" + donorName + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
