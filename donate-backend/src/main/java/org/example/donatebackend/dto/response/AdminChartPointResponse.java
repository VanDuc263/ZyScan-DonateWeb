package org.example.donatebackend.dto.response;

public class AdminChartPointResponse {
    private String month;
    private Double amount;
    private Long count;

    public AdminChartPointResponse() {}

    public AdminChartPointResponse(String month, Double amount, Long count) {
        this.month = month;
        this.amount = amount;
        this.count = count;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
