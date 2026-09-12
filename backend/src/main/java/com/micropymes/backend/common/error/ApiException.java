package com.micropymes.backend.common.error;

public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getTitle());
        this.errorCode = errorCode;
    }

    public ApiException(
            ErrorCode errorCode,
            String detail
    ) {
        super(detail);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}