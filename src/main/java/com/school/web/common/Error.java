package com.school.web.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Error {
    private String fieldName;
    private String message;
}
