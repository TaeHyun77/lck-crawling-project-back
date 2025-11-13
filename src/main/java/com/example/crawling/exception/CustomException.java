
package com.example.crawling.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException{

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final String detail;

    public CustomException(HttpStatus status, ErrorCode errorCode, String detail) {
        this.status = status;
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public CustomException(HttpStatus status, ErrorCode errorCode) {
        this.status = status;
        this.errorCode = errorCode;
        this.detail = "";
    }
}
