package com.example.batch.Domain;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Goods {
	@JsonProperty
    private int id;
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
    private String image;
	@JsonProperty
    private String detail;
	@JsonProperty
	private Timestamp modification_time;
	@JsonProperty
	private Timestamp insertion_time;
	@JsonProperty
	private String product_code;
	@JsonProperty
	private String insertion_date;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public Timestamp getModification_time() {
		return modification_time;
	}
	public void setModification_time(Timestamp modification_time) {
		this.modification_time = modification_time;
	}
	public Timestamp getInsertion_time() {
		return insertion_time;
	}
	public void setInsertion_time(Timestamp insertion_time) {
		this.insertion_time = insertion_time;
	}
	public String getProduct_code() {
		return product_code;
	}
	public void setProduct_code(String product_code) {
		this.product_code = product_code;
	}
	public String getInsertion_date() {
		return insertion_date;
	}
	public void setInsertion_date(String insertion_date) {
		this.insertion_date = insertion_date;
	}
	@Override
	public String toString() {
		return "Goods [id=" + id + ", name=" + name + ", price=" + price + ", amount=" + amount + ", discountamount="
				+ discountamount + ", discountrate=" + discountrate + ", deliveryfee=" + deliveryfee + ", sellid="
				+ sellid + ", image=" + image + ", detail=" + detail + ", modification_time=" + modification_time
				+ ", insertion_time=" + insertion_time + "]";
	}
}