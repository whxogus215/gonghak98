package com.example.gimmegonghakauth.common.constant;

public enum AbeekTypeConst {

    PROFESSIONAL_NON_MAJOR("전문교양"),
    NON_MAJOR("교양"),
    MSC("MSC"),
    MAJOR("전공"),
    DESIGN("설계"),
    MINIMUM_CERTI("최소 이수학점"),
    BSM("BSM");

    private final String typeMessage;

    AbeekTypeConst(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public static AbeekTypeConst getCourseCategoryType(String courseCategory) {
        switch (courseCategory) {
            case "전공":
                return AbeekTypeConst.MAJOR;
            case "전문교양":
                return AbeekTypeConst.PROFESSIONAL_NON_MAJOR;
            case "MSC":
                return AbeekTypeConst.MSC;
            case "BSM":
                return AbeekTypeConst.BSM;
            default:
                return null;
        }
    }

    public String getTypeMessage() {
        return typeMessage;
    }
}
