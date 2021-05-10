package com.adeks.cab.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class User implements Parcelable {
    private String key;
    private String id;
    private String name;
    private String imageUrl;
    private String phone;
    private final Place start;
    private Place stop;

    public User(String key, String id, String name, String imageUrl, String phone, Place start, Place stop) {
        this.key = key;
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.phone = phone;
        this.start = start;
        this.stop = stop;
    }

    protected User(Parcel in) {
        key = in.readString();
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        phone = in.readString();
        start = in.readParcelable(Place.class.getClassLoader());
        stop = in.readParcelable(Place.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Place getStart() {
        return start;
    }

    public void setStart(Place start) {
        start = start;
    }

    public Place getStop() {
        return stop;
    }

    public void setStop(Place stop) {
        this.stop = stop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return  getId().equals(user.getId()) &&
                Objects.equals(getName(), user.getName()) &&
                getStart().equals(user.getStart()) &&
                getStop().equals(user.getStop());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getId(), getName(), getStart(), getStop());
    }

    @Override
    public String toString() {
        return "User{" +
                "key='" + key + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", phone='" + phone + '\'' +
                ", start=" + start +
                ", stop=" + stop +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(phone);
        dest.writeParcelable(start, flags);
        dest.writeParcelable(stop, flags);
    }
}
