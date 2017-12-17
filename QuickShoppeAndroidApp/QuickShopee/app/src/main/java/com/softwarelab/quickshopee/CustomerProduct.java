package com.softwarelab.quickshopee;

/**
 * Created by shailendra on 6/11/17.
 */

public class CustomerProduct {
    private String barCode;
    private String name;
    private String price;
    private String quantity;

    public CustomerProduct() {
    }


    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getBarCode() {return barCode;}

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }
}
