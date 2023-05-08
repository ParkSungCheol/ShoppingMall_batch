package com.example.batch.Domain;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Goods {
	@JsonProperty
    private int rownum;
	@JsonProperty
    private String name;
	@JsonProperty
    private int price;
	@JsonProperty
    private int amount;
	@JsonProperty
    private int discountamount;
	@JsonProperty
    private float discountrate;
	@JsonProperty
    private Integer deliveryfee;
	@JsonProperty
    private String sellid;
	@JsonProperty
	private Timestamp date;
	@JsonProperty
    private String image;
	@JsonProperty
    private String detail;
	public int getRownum() {
		return rownum;
	}
	public void setRownum(int rownum) {
		this.rownum = rownum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public int getDiscountamount() {
		return discountamount;
	}
	public void setDiscountamount(int discountamount) {
		this.discountamount = discountamount;
	}
	public float getDiscountrate() {
		return discountrate;
	}
	public void setDiscountrate(float discountrate) {
		this.discountrate = discountrate;
	}
	public Integer getDeliveryfee() {
		return deliveryfee;
	}
	public void setDeliveryfee(Integer deliveryfee) {
		this.deliveryfee = deliveryfee;
	}
	public String getSellid() {
		return sellid;
	}
	public void setSellid(String sellid) {
		this.sellid = sellid;
	}
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	@Override
	public String toString() {
		return "Goods [rownum=" + rownum + ", name=" + name + ", price=" + price + ", amount=" + amount
				+ ", discountamount=" + discountamount + ", discountrate=" + discountrate + ", deliveryfee="
				+ deliveryfee + ", sellid=" + sellid + ", date=" + date + ", image=" + image + ", detail=" + detail
				+ "]";
	}
}