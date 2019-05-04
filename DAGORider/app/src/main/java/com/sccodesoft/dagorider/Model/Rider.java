package com.sccodesoft.dagorider.Model;

public class Rider {
    private String name,phone,avatarUrl,rates,carType,reserved,introduceCode,myCode;

    public Rider() {
    }

    public Rider(String name, String phone, String avatarUrl, String rates, String carType, String reserved, String introduceCode, String myCode) {
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.rates = rates;
        this.carType = carType;
        this.reserved = reserved;
        this.introduceCode = introduceCode;
        this.myCode = myCode;
    }

    public String getIntroduceCode() {
        return introduceCode;
    }

    public void setIntroduceCode(String introduceCode) {
        this.introduceCode = introduceCode;
    }

    public String getMyCode() {
        return myCode;
    }

    public void setMyCode(String myCode) {
        this.myCode = myCode;
    }

    public String getName() {
        return name;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }
}
