package com.example.batch.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BatchSchedule {
	@JsonProperty
    private int batchNum;
	@JsonProperty
    private String batchName;
	@JsonProperty
	private String target;
	@JsonProperty
    private String url;
	@JsonProperty
    private String totalSelector;
	@JsonProperty
    private String titleSelector1;
	@JsonProperty
    private String titleSelector2;
	@JsonProperty
    private String titleSelector3;
	@JsonProperty
    private Integer titleLocation;
	@JsonProperty
    private String priceSelector1;
	@JsonProperty
    private String priceSelector2;
	@JsonProperty
    private String priceSelector3;
	@JsonProperty
    private Integer priceLocation;
	@JsonProperty
    private String deliveryFeeSelector1;
	@JsonProperty
    private String deliveryFeeSelector2;
	@JsonProperty
    private String deliveryFeeSelector3;
	@JsonProperty
    private String deliveryFeeSelector4;
	@JsonProperty
    private Integer deliveryFeeLocation;
	@JsonProperty
    private String sellerSelector1;
	@JsonProperty
    private String sellerSelector2;
	@JsonProperty
    private String sellerSelector3;
	@JsonProperty
    private Integer sellerLocation;
	@JsonProperty
    private String urlSelector1;
	@JsonProperty
    private String urlSelector2;
	@JsonProperty
    private String urlSelector3;
	@JsonProperty
    private String nextButtonSelector;
	@JsonProperty
    private String imageSelector;
	public int getBatchNum() {
		return batchNum;
	}
	public void setBatchNum(int batchNum) {
		this.batchNum = batchNum;
	}
	public String getBatchName() {
		return batchName;
	}
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTotalSelector() {
		return totalSelector;
	}
	public void setTotalSelector(String totalSelector) {
		this.totalSelector = totalSelector;
	}
	public String getTitleSelector1() {
		return titleSelector1;
	}
	public void setTitleSelector1(String titleSelector1) {
		this.titleSelector1 = titleSelector1;
	}
	public String getTitleSelector2() {
		return titleSelector2;
	}
	public void setTitleSelector2(String titleSelector2) {
		this.titleSelector2 = titleSelector2;
	}
	public String getTitleSelector3() {
		return titleSelector3;
	}
	public void setTitleSelector3(String titleSelector3) {
		this.titleSelector3 = titleSelector3;
	}
	public Integer getTitleLocation() {
		return titleLocation;
	}
	public void setTitleLocation(Integer titleLocation) {
		this.titleLocation = titleLocation;
	}
	public String getPriceSelector1() {
		return priceSelector1;
	}
	public void setPriceSelector1(String priceSelector1) {
		this.priceSelector1 = priceSelector1;
	}
	public String getPriceSelector2() {
		return priceSelector2;
	}
	public void setPriceSelector2(String priceSelector2) {
		this.priceSelector2 = priceSelector2;
	}
	public String getPriceSelector3() {
		return priceSelector3;
	}
	public void setPriceSelector3(String priceSelector3) {
		this.priceSelector3 = priceSelector3;
	}
	public Integer getPriceLocation() {
		return priceLocation;
	}
	public void setPriceLocation(Integer priceLocation) {
		this.priceLocation = priceLocation;
	}
	public String getDeliveryFeeSelector1() {
		return deliveryFeeSelector1;
	}
	public void setDeliveryFeeSelector1(String deliveryFeeSelector1) {
		this.deliveryFeeSelector1 = deliveryFeeSelector1;
	}
	public String getDeliveryFeeSelector2() {
		return deliveryFeeSelector2;
	}
	public void setDeliveryFeeSelector2(String deliveryFeeSelector2) {
		this.deliveryFeeSelector2 = deliveryFeeSelector2;
	}
	public String getDeliveryFeeSelector3() {
		return deliveryFeeSelector3;
	}
	public void setDeliveryFeeSelector3(String deliveryFeeSelector3) {
		this.deliveryFeeSelector3 = deliveryFeeSelector3;
	}
	public String getSellerSelector1() {
		return sellerSelector1;
	}
	public void setSellerSelector1(String sellerSelector1) {
		this.sellerSelector1 = sellerSelector1;
	}
	public String getSellerSelector2() {
		return sellerSelector2;
	}
	public void setSellerSelector2(String sellerSelector2) {
		this.sellerSelector2 = sellerSelector2;
	}
	public String getSellerSelector3() {
		return sellerSelector3;
	}
	public void setSellerSelector3(String sellerSelector3) {
		this.sellerSelector3 = sellerSelector3;
	}
	public Integer getSellerLocation() {
		return sellerLocation;
	}
	public void setSellerLocation(Integer sellerLocation) {
		this.sellerLocation = sellerLocation;
	}
	public String getUrlSelector1() {
		return urlSelector1;
	}
	public void setUrlSelector1(String urlSelector1) {
		this.urlSelector1 = urlSelector1;
	}
	public String getUrlSelector2() {
		return urlSelector2;
	}
	public void setUrlSelector2(String urlSelector2) {
		this.urlSelector2 = urlSelector2;
	}
	public String getUrlSelector3() {
		return urlSelector3;
	}
	public void setUrlSelector3(String urlSelector3) {
		this.urlSelector3 = urlSelector3;
	}
	public Integer getDeliveryFeeLocation() {
		return deliveryFeeLocation;
	}
	public void setDeliveryFeeLocation(Integer deliveryFeeLocation) {
		this.deliveryFeeLocation = deliveryFeeLocation;
	}
	public String getNextButtonSelector() {
		return nextButtonSelector;
	}
	public void setNextButtonSelector(String nextButtonSelector) {
		this.nextButtonSelector = nextButtonSelector;
	}
	public String getImageSelector() {
		return imageSelector;
	}
	public void setImageSelector(String imageSelector) {
		this.imageSelector = imageSelector;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getDeliveryFeeSelector4() {
		return deliveryFeeSelector4;
	}
	public void setDeliveryFeeSelector4(String deliveryFeeSelector4) {
		this.deliveryFeeSelector4 = deliveryFeeSelector4;
	}
	public BatchSchedule(int batchNum, String batchName, String target, String url, String totalSelector,
			String titleSelector1, String titleSelector2, String titleSelector3, Integer titleLocation,
			String priceSelector1, String priceSelector2, String priceSelector3, Integer priceLocation,
			String deliveryFeeSelector1, String deliveryFeeSelector2, String deliveryFeeSelector3,
			String deliveryFeeSelector4, Integer deliveryFeeLocation, String sellerSelector1, String sellerSelector2,
			String sellerSelector3, Integer sellerLocation, String urlSelector1, String urlSelector2,
			String urlSelector3, String nextButtonSelector, String imageSelector) {
		super();
		this.batchNum = batchNum;
		this.batchName = batchName;
		this.target = target;
		this.url = url;
		this.totalSelector = totalSelector;
		this.titleSelector1 = titleSelector1;
		this.titleSelector2 = titleSelector2;
		this.titleSelector3 = titleSelector3;
		this.titleLocation = titleLocation;
		this.priceSelector1 = priceSelector1;
		this.priceSelector2 = priceSelector2;
		this.priceSelector3 = priceSelector3;
		this.priceLocation = priceLocation;
		this.deliveryFeeSelector1 = deliveryFeeSelector1;
		this.deliveryFeeSelector2 = deliveryFeeSelector2;
		this.deliveryFeeSelector3 = deliveryFeeSelector3;
		this.deliveryFeeSelector4 = deliveryFeeSelector4;
		this.deliveryFeeLocation = deliveryFeeLocation;
		this.sellerSelector1 = sellerSelector1;
		this.sellerSelector2 = sellerSelector2;
		this.sellerSelector3 = sellerSelector3;
		this.sellerLocation = sellerLocation;
		this.urlSelector1 = urlSelector1;
		this.urlSelector2 = urlSelector2;
		this.urlSelector3 = urlSelector3;
		this.nextButtonSelector = nextButtonSelector;
		this.imageSelector = imageSelector;
	}
	public BatchSchedule() {
		
	}
}