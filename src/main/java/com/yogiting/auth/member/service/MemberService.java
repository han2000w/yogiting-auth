package com.yogiting.auth.member.service;

import com.yogiting.auth.member.domain.Member;
import com.yogiting.auth.member.domain.SocialType;
import com.yogiting.auth.member.dto.MemberLoginReqDto;
import com.yogiting.auth.member.dto.MemberSignUpReqDto;
import com.yogiting.auth.member.mapper.MemberMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public void signUp(MemberSignUpReqDto memberSignUpReqDto) {
        Member member = Member.builder()
                .email(memberSignUpReqDto.getEmail())
                .password(passwordEncoder.encode(memberSignUpReqDto.getPassword()))
                .name(memberSignUpReqDto.getName())
                .nickname(memberSignUpReqDto.getNickname())
                .phone(memberSignUpReqDto.getPhone())
                .build();
        memberMapper.signUp(member);
        createWallet(member.getId());
    }

    public Member login(MemberLoginReqDto memberLoginReqDto) {
        Member member = memberMapper.findMemberByEmail(memberLoginReqDto.getEmail());
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        if (!passwordEncoder.matches(memberLoginReqDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public Member getUserInfo(String email) {
        Member member = memberMapper.findMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        return member;
    }

    public Member getMember(Long id) {
        Member member = memberMapper.findMemberById(id);
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        return member;
    }

    public Member getMemberBySocialId(String socialId) {
        Member member = memberMapper.findMemberBySocialId(socialId);
        return member;
    }

    public Long signUpByOauth(String socialId, String email, SocialType socialType, String name) {
        Member member = Member.builder()
                .email(email)
                .socialType(socialType)
                .socialId(socialId)
                .name(name)
                .nickname(name)
                .build();
        memberMapper.signUpByOauth(member);
        createWallet(member.getId());
        return member.getId();
    }

    public void createWallet(Long memberId) {

        RestClient restClient = RestClient.create();

        ResponseEntity<?> response = restClient.post()
                .uri("http://localhost:8082/v1/api/point/wallet")
                .header("Content-Type", "application/json")
                .body(memberId)
                .retrieve()
                .toBodilessEntity();
    }
}
