package com.example.batch.Domain;

import jakarta.xml.bind.annotation.XmlElement;

// XML정보를 매핑할 CLASS
public class Benefit {
    private int discount;
    private int mileage;

    public int getDiscount() {
        return discount;
    }

    @XmlElement(name = "Discount")
    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getMileage() {
        return mileage;
    }

    @XmlElement(name = "Mileage")
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
}
