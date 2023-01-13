package com.test.model;


import java.util.ArrayList;
import java.util.List;

public class ChildModel {
    private String childName;
    private String childLastName;

    private List<SubChildModel> subChildModelList = new ArrayList<>();

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getChildLastName() {
        return childLastName;
    }

    public void setChildLastName(String childLastName) {
        this.childLastName = childLastName;
    }

    public List<SubChildModel> getSubChildModelList() {
        return subChildModelList;
    }

    public void setSubChildModelList(List<SubChildModel> subChildModelList) {
        this.subChildModelList = subChildModelList;
    }
}
