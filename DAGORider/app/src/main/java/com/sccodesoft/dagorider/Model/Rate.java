package com.sccodesoft.dagorider.Model;

public class Rate {
    private String rates;
    private String comments;
    private String raterid;

    public Rate(String rates, String comments, String rater) {
        this.rates = rates;
        this.comments = comments;
        this.raterid = rater;
    }

    public Rate() {
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getRaterid() {
        return raterid;
    }

    public void setRaterid(String raterid) {
        this.raterid = raterid;
    }
}
