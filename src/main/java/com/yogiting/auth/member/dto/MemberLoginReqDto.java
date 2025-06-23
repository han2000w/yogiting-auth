package com.yogiting.auth.member.dto;

import lombok.Data;

@Data
public class MemberLoginReqDto {

    private String email;
    private String password;
}
