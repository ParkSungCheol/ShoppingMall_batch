package com.example.batch.Domain;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ProductInfoResponse")
public class ProductInfoResponse {
    private Request request;
    private Product product;

    @XmlElement(name = "Request")
    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    @XmlElement(name = "Product")
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}