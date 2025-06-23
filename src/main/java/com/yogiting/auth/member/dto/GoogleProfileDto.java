package com.yogiting.auth.member.dto;

import lombok.Data;

@Data
public class GoogleProfileDto {

    private String sub;
    private String email;
    private String name;
}
