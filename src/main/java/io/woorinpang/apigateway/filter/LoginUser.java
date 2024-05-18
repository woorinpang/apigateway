package io.woorinpang.apigateway.filter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LoginUser {
    private long userId;
    private String username;
    private String email;
    private String name;
    private String role;
}
