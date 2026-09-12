package com.micropymes.backend.common.error;

import org.springframework.http.HttpStatus;

// Enumeración que representa los códigos de error utilizados en la aplicación
public enum ErrorCode {

        INVALID_MANUAL_ACTIVITY_TYPE(
                        "INVALID_MANUAL_ACTIVITY_TYPE",
                        "Invalid manual activity type",
                        HttpStatus.CONFLICT),

        FOLLOW_UP_NOT_EDITABLE(
                        "FOLLOW_UP_NOT_EDITABLE",
                        "Follow-up is not editable",
                        HttpStatus.CONFLICT),

        FOLLOW_UP_NOT_COMPLETABLE(
                        "FOLLOW_UP_NOT_COMPLETABLE",
                        "Follow-up cannot be completed",
                        HttpStatus.CONFLICT),

        FOLLOW_UP_NOT_CANCELLABLE(
                        "FOLLOW_UP_NOT_CANCELLABLE",
                        "Follow-up cannot be cancelled",
                        HttpStatus.CONFLICT),

        ASSIGNED_MEMBER_NOT_FOUND(
                        "ASSIGNED_MEMBER_NOT_FOUND",
                        "Assigned member not found",
                        HttpStatus.NOT_FOUND),

        ASSIGNED_MEMBER_INACTIVE(
                        "ASSIGNED_MEMBER_INACTIVE",
                        "Assigned member is inactive",
                        HttpStatus.CONFLICT),

        QUOTE_NOT_EDITABLE(
                        "QUOTE_NOT_EDITABLE",
                        "Quote is not editable",
                        HttpStatus.CONFLICT),

        QUOTE_NOT_SENDABLE(
                        "QUOTE_NOT_SENDABLE",
                        "Quote cannot be sent",
                        HttpStatus.CONFLICT),

        QUOTE_NOT_ACCEPTABLE(
                        "QUOTE_NOT_ACCEPTABLE",
                        "Quote cannot be accepted",
                        HttpStatus.CONFLICT),

        QUOTE_NOT_REJECTABLE(
                        "QUOTE_NOT_REJECTABLE",
                        "Quote cannot be rejected",
                        HttpStatus.CONFLICT),

        QUOTE_ALREADY_ACCEPTED(
                        "QUOTE_ALREADY_ACCEPTED",
                        "Opportunity already has an accepted quote",
                        HttpStatus.CONFLICT),

        OPPORTUNITY_ARCHIVED(
                        "OPPORTUNITY_ARCHIVED",
                        "Opportunity is archived",
                        HttpStatus.CONFLICT),

        OPPORTUNITY_CLOSED(
                        "OPPORTUNITY_CLOSED",
                        "Opportunity is closed",
                        HttpStatus.CONFLICT),

        OPPORTUNITY_HAS_ACCEPTED_QUOTE(
                        "OPPORTUNITY_HAS_ACCEPTED_QUOTE",
                        "Opportunity has an accepted quote",
                        HttpStatus.CONFLICT),

        INVALID_OPPORTUNITY_STATUS(
                        "INVALID_OPPORTUNITY_STATUS",
                        "Invalid opportunity status",
                        HttpStatus.CONFLICT),

        CUSTOMER_ARCHIVED(
                        "CUSTOMER_ARCHIVED",
                        "Customer is archived",
                        HttpStatus.CONFLICT),

        CUSTOMER_HAS_ACTIVE_OPPORTUNITIES(
                        "CUSTOMER_HAS_ACTIVE_OPPORTUNITIES",
                        "Customer has active opportunities",
                        HttpStatus.CONFLICT),

        MEMBER_ALREADY_EXISTS(
                        "MEMBER_ALREADY_EXISTS",
                        "Member already exists",
                        HttpStatus.CONFLICT),

        MEMBER_INACTIVE(
                        "MEMBER_INACTIVE",
                        "Member is inactive",
                        HttpStatus.CONFLICT),

        LAST_OWNER_REQUIRED(
                        "LAST_OWNER_REQUIRED",
                        "Organization must have at least one active owner",
                        HttpStatus.CONFLICT),

        ACCOUNT_DISABLED(
                        "ACCOUNT_DISABLED",
                        "Account disabled",
                        HttpStatus.FORBIDDEN),

        EMAIL_ALREADY_REGISTERED(
                        "EMAIL_ALREADY_REGISTERED",
                        "Email already registered",
                        HttpStatus.CONFLICT),

        VALIDATION_FAILED(
                        "VALIDATION_FAILED",
                        "Validation failed",
                        HttpStatus.BAD_REQUEST),

        UNAUTHENTICATED(
                        "UNAUTHENTICATED",
                        "Authentication required",
                        HttpStatus.UNAUTHORIZED),

        INVALID_CREDENTIALS(
                        "INVALID_CREDENTIALS",
                        "Invalid credentials",
                        HttpStatus.UNAUTHORIZED),

        OWNER_PERMISSION_REQUIRED(
                        "OWNER_PERMISSION_REQUIRED",
                        "Owner permission required",
                        HttpStatus.FORBIDDEN),

        ORGANIZATION_NOT_FOUND(
                        "ORGANIZATION_NOT_FOUND",
                        "Organization not found",
                        HttpStatus.NOT_FOUND),

        USER_NOT_FOUND(
                        "USER_NOT_FOUND",
                        "User not found",
                        HttpStatus.NOT_FOUND),

        MEMBER_NOT_FOUND(
                        "MEMBER_NOT_FOUND",
                        "Member not found",
                        HttpStatus.NOT_FOUND),

        CUSTOMER_NOT_FOUND(
                        "CUSTOMER_NOT_FOUND",
                        "Customer not found",
                        HttpStatus.NOT_FOUND),

        OPPORTUNITY_NOT_FOUND(
                        "OPPORTUNITY_NOT_FOUND",
                        "Opportunity not found",
                        HttpStatus.NOT_FOUND),

        QUOTE_NOT_FOUND(
                        "QUOTE_NOT_FOUND",
                        "Quote not found",
                        HttpStatus.NOT_FOUND),

        FOLLOW_UP_NOT_FOUND(
                        "FOLLOW_UP_NOT_FOUND",
                        "Follow-up not found",
                        HttpStatus.NOT_FOUND),

        CONCURRENT_MODIFICATION(
                        "CONCURRENT_MODIFICATION",
                        "Concurrent modification",
                        HttpStatus.CONFLICT),

        INTERNAL_SERVER_ERROR(
                        "INTERNAL_SERVER_ERROR",
                        "Internal server error",
                        HttpStatus.INTERNAL_SERVER_ERROR);

        private final String code;
        private final String title;
        private final HttpStatus status;

        ErrorCode(
                        String code,
                        String title,
                        HttpStatus status) {
                this.code = code;
                this.title = title;
                this.status = status;
        }

        public String getCode() {
                return code;
        }

        public String getTitle() {
                return title;
        }

        public HttpStatus getStatus() {
                return status;
        }
}