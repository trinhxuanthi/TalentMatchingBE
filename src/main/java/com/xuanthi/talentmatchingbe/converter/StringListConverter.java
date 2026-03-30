package com.xuanthi.talentmatchingbe.converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final String SPLIT_CHAR = ",";

    // Trình tự 1: Biến List của Java thành String để lưu xuống MySQL
    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return "";
        }
        return String.join(SPLIT_CHAR, stringList);
    }

    // Trình tự 2: Đọc String từ MySQL lên và biến ngược lại thành List cho Frontend xài
    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string == null || string.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Cắt bằng dấu phẩy và xóa khoảng trắng dư thừa
        return Arrays.stream(string.split(SPLIT_CHAR))
                .map(String::trim)
                .toList();
    }
}