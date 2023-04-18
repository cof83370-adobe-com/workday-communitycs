package com.workday.community.aem.core.pojos;

import java.util.ArrayList;
import java.util.List;

public class BookPageBean {
    String mainPagePath;
    String mainPageTitle;
    List<SecondPageBean> secondPageBeanList;

    public class SecondPageBean {
        String secondPagePath;
        String secondPageTitle;
        List<ThirdPageBean> thirdPageBeanList;

        public String getSecondPagePath() {
            return secondPagePath;
        }

        public void setSecondPagePath(String secondPagePath) {
            this.secondPagePath = secondPagePath;
        }

        public String getSecondPageTitle() {
            return secondPageTitle;
        }

        public void setSecondPageTitle(String secondPageTitle) {
            this.secondPageTitle = secondPageTitle;
        }

        public List<ThirdPageBean> getThirdPageBeanList() {
            if(this.thirdPageBeanList == null){
                thirdPageBeanList = new ArrayList<ThirdPageBean>();
            }
            return thirdPageBeanList;
        }

        public void setThirdPageBeanList(List<ThirdPageBean> thirdPageBeanList) {
            this.thirdPageBeanList = thirdPageBeanList;
        }
    }
    public class ThirdPageBean {
        String thirdPagePath;
        String thirdPageTitle;

        public String getThirdPagePath() {
            return thirdPagePath;
        }

        public void setThirdPagePath(String thirdPagePath) {
            this.thirdPagePath = thirdPagePath;
        }

        public String getThirdPageTitle() {
            return thirdPageTitle;
        }

        public void setThirdPageTitle(String thirdPageTitle) {
            this.thirdPageTitle = thirdPageTitle;
        }
    }

    public String getMainPagePath() {
        return mainPagePath;
    }

    public void setMainPagePath(String mainPagePath) {
        this.mainPagePath = mainPagePath;
    }

    public String getMainPageTitle() {
        return mainPageTitle;
    }

    public void setMainPageTitle(String mainPageTitle) {
        this.mainPageTitle = mainPageTitle;
    }

    public List<SecondPageBean> getSecondPageBeanList() {
        if(this.secondPageBeanList == null){
            secondPageBeanList = new ArrayList<SecondPageBean>();
        }
        return secondPageBeanList;
    }

    public void setSecondPageBeanList(List<SecondPageBean> secondPageBeanList) {
        this.secondPageBeanList = secondPageBeanList;
    }
}
