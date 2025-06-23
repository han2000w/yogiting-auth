package com.yogiting.auth.member.controller;

import com.yogiting.auth.common.auth.JwtTokenProvider;
import com.yogiting.auth.member.domain.Member;
import com.yogiting.auth.member.domain.SocialType;
import com.yogiting.auth.member.dto.*;
import com.yogiting.auth.member.service.GoogleService;
import com.yogiting.auth.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;

    public AuthController(MemberService memberService, JwtTokenProvider jwtTokenProvider, GoogleService googleService) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleService = googleService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody MemberSignUpReqDto memberSignUpReqDto) {
        memberService.signUp(memberSignUpReqDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberLoginReqDto memberLoginReqDto) {
        Member member = memberService.login(memberLoginReqDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getId());
        String jwtRefreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getId());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", jwtRefreshToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleRedirectReqDto googleRedirectReqDto) {

        GoogleAccessTokenDto googleAccessTokenDto = googleService.getAccessToken(googleRedirectReqDto.getCode());

        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(googleAccessTokenDto.getAccess_token());

        Member member = memberService.getMemberBySocialId(googleProfileDto.getSub());

        if (member == null) {
            Long newMemberId = memberService.signUpByOauth(googleProfileDto.getSub(), googleProfileDto.getEmail(), SocialType.GOOGLE, googleProfileDto.getName());
            member = memberService.getMember(newMemberId);
        }

        String jwtToken = jwtTokenProvider.createToken(googleProfileDto.getEmail(), member.getId());
        String jwtRefreshToken = jwtTokenProvider.createRefreshToken(googleProfileDto.getEmail(), member.getId());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", jwtRefreshToken);

        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestParam String email) {
        Member member = memberService.getUserInfo(email);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", member.getId());
        userInfo.put("nickname", member.getNickname());
        userInfo.put("profileImageUrl",member.getProfileImageUrl());

        return new ResponseEntity<>(userInfo, HttpStatus.OK);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshTokenDto memberRefreshTokenDto) {
        String refreshToken = memberRefreshTokenDto.getRefreshToken();
        String newToken = jwtTokenProvider.createTokenWithRefreshToken(refreshToken);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newToken);
        System.out.println("새로운 토큰발급: " + newToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
