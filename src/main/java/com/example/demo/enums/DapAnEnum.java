package com.example.demo.enums;

public enum DapAnEnum {
    A("A"),
    B("B"),
    C("C"),
    D("D");

    private final String value;

    DapAnEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DapAnEnum fromValue(String value) {
        for (DapAnEnum dapAn : DapAnEnum.values()) {
            if (dapAn.value.equals(value)) {
                return dapAn;
            }
        }
        throw new IllegalArgumentException("Invalid DapAn value: " + value);
    }
}