package com.jsp.Allocation.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppResponseDto<T> {
        private String code;
        private String errorMessage;
        private String status;
        private T data;
}
