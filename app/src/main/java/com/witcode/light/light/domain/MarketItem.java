package com.witcode.light.light.domain;

/**
 * Created by carlo on 17/03/2017.
 */

public class MarketItem {
    private String Id;
    private String Name;
    private String Lights;
    private String Discount;
    private String Info;
    private String Type;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLights() {
        return Lights;
    }

    public void setLights(String lights) {
        Lights = lights;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getInfo() {
        return Info;
    }

    public void setInfo(String info) {
        Info = info;
    }
}
