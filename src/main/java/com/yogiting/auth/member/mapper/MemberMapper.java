package com.yogiting.auth.member.mapper;

import com.yogiting.auth.member.domain.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    void signUp(Member member);
    Member findMemberByEmail(String email);
    Member findMemberById(Long id);
    Member findMemberBySocialId(String socialId);
    void signUpByOauth(Member member);

}