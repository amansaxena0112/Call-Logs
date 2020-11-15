package com.example.calllogs;

import java.io.Serializable;

public class ContactModel implements Serializable {

    private String firstName;
    private String phone;
    private String duration;
    private String createddate;
    private String call_Type;
    private String source;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCreateddate() {
        return createddate;
    }

    public void setCreateddate(String createddate) {
        this.createddate = createddate;
    }

    public String getCall_Type() {
        return call_Type;
    }

    public void setCall_Type(String call_Type) {
        this.call_Type = call_Type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
