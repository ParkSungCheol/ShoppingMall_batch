package com.example.batch.Domain;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Product")
public class Product {
    private String productCode;
    private String productName;
    private int productPrice;
    private String productImage;
    private String productImage100;
    private String productImage110;
    private String productImage120;
    private String productImage130;
    private String productImage140;
    private String productImage150;
    private String productImage170;
    private String productImage200;
    private String productImage250;
    private String productImage270;
    private String productImage300;
    private String text1;
    private String text2;
    private String sellerNick;
    private String seller;
    private int sellerGrd;
    private int rating;
    private String detailPageUrl;
    private int salePrice;
    private String delivery;
    private int reviewCount;
    private int buySatisfy;
    private String minorYn;
    private Benefit benefit;

    public String getProductCode() {
        return productCode;
    }

    @XmlElement(name = "ProductCode")
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    @XmlElement(name = "ProductName")
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductPrice() {
        return productPrice;
    }

    @XmlElement(name = "ProductPrice")
    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    @XmlElement(name = "ProductImage")
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getProductImage100() {
		return productImage100;
	}

    @XmlElement(name = "ProductImage100")
	public void setProductImage100(String productImage100) {
		this.productImage100 = productImage100;
	}

	public String getProductImage110() {
		return productImage110;
	}

	@XmlElement(name = "ProductImage110")
	public void setProductImage110(String productImage110) {
		this.productImage110 = productImage110;
	}

	public String getProductImage120() {
		return productImage120;
	}

	@XmlElement(name = "ProductImage120")
	public void setProductImage120(String productImage120) {
		this.productImage120 = productImage120;
	}

	public String getProductImage130() {
		return productImage130;
	}

	@XmlElement(name = "ProductImage130")
	public void setProductImage130(String productImage130) {
		this.productImage130 = productImage130;
	}

	public String getProductImage140() {
		return productImage140;
	}

	@XmlElement(name = "ProductImage140")
	public void setProductImage140(String productImage140) {
		this.productImage140 = productImage140;
	}

	public String getProductImage150() {
		return productImage150;
	}

	@XmlElement(name = "ProductImage150")
	public void setProductImage150(String productImage150) {
		this.productImage150 = productImage150;
	}

	public String getProductImage170() {
		return productImage170;
	}

	@XmlElement(name = "ProductImage170")
	public void setProductImage170(String productImage170) {
		this.productImage170 = productImage170;
	}

	public String getProductImage200() {
		return productImage200;
	}

	@XmlElement(name = "ProductImage200")
	public void setProductImage200(String productImage200) {
		this.productImage200 = productImage200;
	}

	public String getProductImage250() {
		return productImage250;
	}

	@XmlElement(name = "ProductImage250")
	public void setProductImage250(String productImage250) {
		this.productImage250 = productImage250;
	}

	public String getProductImage270() {
		return productImage270;
	}

	@XmlElement(name = "ProductImage270")
	public void setProductImage270(String productImage270) {
		this.productImage270 = productImage270;
	}

	public String getProductImage300() {
		return productImage300;
	}

	@XmlElement(name = "ProductImage300")
	public void setProductImage300(String productImage300) {
		this.productImage300 = productImage300;
	}

	public String getText1() {
		return text1;
	}

	@XmlElement(name = "Text1")
	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getText2() {
		return text2;
	}

	@XmlElement(name = "Text2")
	public void setText2(String text2) {
		this.text2 = text2;
	}

	public String getSellerNick() {
		return sellerNick;
	}

	@XmlElement(name = "SellerNick")
	public void setSellerNick(String sellerNick) {
		this.sellerNick = sellerNick;
	}

	public String getSeller() {
		return seller;
	}

	@XmlElement(name = "Seller")
	public void setSeller(String seller) {
		this.seller = seller;
	}

	public int getSellerGrd() {
		return sellerGrd;
	}

	@XmlElement(name = "SellerGrd")
	public void setSellerGrd(int sellerGrd) {
		this.sellerGrd = sellerGrd;
	}

	public int getRating() {
		return rating;
	}

	@XmlElement(name = "Rating")
	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getDetailPageUrl() {
		return detailPageUrl;
	}

	@XmlElement(name = "DetailPageUrl")
	public void setDetailPageUrl(String detailPageUrl) {
		this.detailPageUrl = detailPageUrl;
	}

	public int getSalePrice() {
		return salePrice;
	}

	@XmlElement(name = "SalePrice")
	public void setSalePrice(int salePrice) {
		this.salePrice = salePrice;
	}

	public String getDelivery() {
		return delivery;
	}

	@XmlElement(name = "Delivery")
	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	@XmlElement(name = "ReviewCount")
	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}

	public int getBuySatisfy() {
		return buySatisfy;
	}

	@XmlElement(name = "BuySatisfy")
	public void setBuySatisfy(int buySatisfy) {
		this.buySatisfy = buySatisfy;
	}

	public String getMinorYn() {
		return minorYn;
	}

	@XmlElement(name = "MinorYn")
	public void setMinorYn(String minorYn) {
		this.minorYn = minorYn;
	}

	public Benefit getBenefit() {
        return benefit;
    }

    @XmlElement(name = "Benefit")
    public void setBenefit(Benefit benefit) {
        this.benefit = benefit;
    }
}