package com.woorinpang.apigateway.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(400, "E001", "error.invalid.input.value"), //Bad Request
    INVALID_TYPE_VALUE(400, "E002", "error.invalid.type.value"), //Bad Request
    ENTITY_NOT_FOUND(400, "E003", "error.entity.not.found"), //Bad Request
    UNAUTHORIZED(401, "E003", "error.unauthorized"), //The request requires an user authentication
    ACCESS_DENIED(403,"E006","error.access.denied"), //Forbidden, Access is Denied
    NOT_FOUND(404, "E010", "error.page.not.found"), //Not found
    METHOD_NOT_ALLOWED(405, "E011", "error.method.not.allowed"), //요청 방법이 서버에 의해 알려줬으나, 사용 불가능한 상태
    UNPROCESSABLE_ENTITY(422, "E009", "error.unprocessable.entity"), //Unprocessable Entity
    REQUIRE_USER_JOIN(412, "E012", "error.user.not.exists"), //Server Error
    INTERNAL_SERVER_ERROR(500, "E999", "error.internal.server"), //Server Error
    SERVICE_UNAVAILABLE(503, "E010", "error.service.unavailable") //Service Unavailable
    ;

    private final int status;
    private final String code;
    private final String message;

}
