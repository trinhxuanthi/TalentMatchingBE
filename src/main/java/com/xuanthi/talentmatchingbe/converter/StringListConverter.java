package com.xuanthi.talentmatchingbe.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JPA Attribute Converter for converting between List<String> and comma-separated String
 * Used for storing lists of strings in database columns
 *
 * Example: ["Java", "Spring", "SQL"] ↔ "Java,Spring,SQL"
 */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final String SPLIT_CHAR = ",";

    /**
     * Convert List<String> to database column (comma-separated string)
     * @param stringList the list of strings to convert
     * @return comma-separated string, empty string if list is null or empty
     */
    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return "";
        }
        return String.join(SPLIT_CHAR, stringList);
    }

    /**
     * Convert database column (comma-separated string) to List<String>
     * @param string the comma-separated string from database
     * @return list of strings, empty list if string is null or empty
     */
    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string == null || string.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Split by comma and trim whitespace
        return Arrays.stream(string.split(SPLIT_CHAR))
                .map(String::trim)
                .filter(s -> !s.isEmpty()) // Filter out empty strings
                .toList();
    }
}