package com.example.batch.Domain;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

@XmlRootElement(name = "ProductSearchResponse")
@XmlType(propOrder = {"request", "products"})
public class ProductSearchResponse {
    private Request request;
    private Products products;

    public Request getRequest() {
        return request;
    }

    @XmlElement(name = "Request")
    public void setRequest(Request request) {
        this.request = request;
    }

    public Products getProducts() {
        return products;
    }

    @XmlElement(name = "Products")
    public void setProducts(Products products) {
        this.products = products;
    }
}