package com.xuanthi.talentmatchingbe.enums;

public enum JobLevel {
    INTERN,
    FRESHER,
    JUNIOR,
    MIDDLE,
    SENIOR,
    LEAD,
    MANAGER;

    public int toScore() {
        return switch (this) {
            case INTERN -> 1;
            case FRESHER -> 2;
            case JUNIOR -> 3;
            case MIDDLE -> 4;
            case SENIOR -> 5;
            case LEAD -> 6;
            case MANAGER -> 7;
        };
    }
}