package com.yogiting.auth.member.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
public class Member {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private String profileImageUrl;

    private SocialType socialType;
    private String socialId;




}
