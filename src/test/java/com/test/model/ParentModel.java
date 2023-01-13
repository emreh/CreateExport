package com.test.model;


import java.util.ArrayList;
import java.util.List;

public class ParentModel {

    private String nameParent;
    private String lastNameParent;
    private List<ChildModel> childModelList = new ArrayList<>();

    public String getNameParent() {
        return nameParent;
    }

    public void setNameParent(String nameParent) {
        this.nameParent = nameParent;
    }

    public String getLastNameParent() {
        return lastNameParent;
    }

    public void setLastNameParent(String lastNameParent) {
        this.lastNameParent = lastNameParent;
    }

    public List<ChildModel> getChildModelList() {
        return childModelList;
    }

    public void setChildModelList(List<ChildModel> childModelList) {
        this.childModelList = childModelList;
    }
}
