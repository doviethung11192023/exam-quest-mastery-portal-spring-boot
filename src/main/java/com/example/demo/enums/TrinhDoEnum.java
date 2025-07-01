package com.example.demo.enums;

public enum TrinhDoEnum {
    A("A"), // Dễ
    B("B"), // Trung bình
    C("C"); // Khó

    private final String value;

    TrinhDoEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TrinhDoEnum fromValue(String value) {
        for (TrinhDoEnum trinhDo : TrinhDoEnum.values()) {
            if (trinhDo.value.equals(value)) {
                return trinhDo;
            }
        }
        throw new IllegalArgumentException("Invalid TrinhDo value: " + value);
    }
}