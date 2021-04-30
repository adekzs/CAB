package com.adeks.cab.models;

import android.location.Location;

import java.util.Objects;

public class Driver {

    private String key;
    private String id;
    private String name;
    private String latitude;
   private String longitude;
    private String carDetails;
    private String imageUrl;
    private String phone;

    public Driver(String key, String id, String name, String latitude, String longitude, String carDetails, String imageUrl, String phone) {
        this.key = key;
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.carDetails = carDetails;
        this.imageUrl = imageUrl;
        this.phone = phone;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getCarDetails() {
        return carDetails;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setCarDetails(String carDetails) {
        this.carDetails = carDetails;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "key='" + key + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", carDetails='" + carDetails + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Driver driver = (Driver) o;
        return Objects.equals(getKey(), driver.getKey()) &&
                Objects.equals(getId(), driver.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getId());
    }
}
