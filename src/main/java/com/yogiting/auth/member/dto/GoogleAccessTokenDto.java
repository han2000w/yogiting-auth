package com.yogiting.auth.member.dto;

import lombok.Data;

@Data
public class GoogleAccessTokenDto {

    private String access_token;
    private String expires_in;
    private String scope;
    private String id_token;
}
