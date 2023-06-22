package com.example.batch.Domain;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Products")
public class Products {
    private int totalCount;
    private List<Product> productList;

    public int getTotalCount() {
        return totalCount;
    }

    @XmlElement(name = "TotalCount")
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Product> getProductList() {
        return productList;
    }

    @XmlElement(name = "Product")
    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }
}