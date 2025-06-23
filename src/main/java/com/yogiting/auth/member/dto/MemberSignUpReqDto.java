package com.yogiting.auth.member.dto;

import lombok.Data;

@Data
public class MemberSignUpReqDto {

    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
}
