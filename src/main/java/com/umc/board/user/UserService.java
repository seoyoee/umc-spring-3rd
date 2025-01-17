package com.umc.board.user;

import com.umc.board.config.Secret;
import com.umc.board.user.model.*;
import com.umc.board.utils.AES128;
import com.umc.board.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserDao userDao;
    private final JwtService jwtService;

    @Autowired
    public UserService(UserDao userDao, JwtService jwtService) {
        this.userDao = userDao;
        this.jwtService = jwtService;
    }

    // 이메일 중복 확인
    public int checkEmail(String email) throws Exception{
        try {
            return userDao.checkEmail(email);
        } catch (Exception e) {
            throw new Exception("데이터베이스 오류");
        }
    }

    // 회원 가입
    public PostUserRes createUser(PostUserReq postUserReq) throws Exception{
        if (checkEmail(postUserReq.getEmail()) == 1) {
            throw new Exception("이미 존재하는 이메일");
        }

        try {
            String password = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(postUserReq.getPassword());
            postUserReq.setPassword(password);
        } catch (Exception e) {
            throw new Exception("비밀번호 암호화 오류");
        }

        try {
            int userIdx = userDao.createUser(postUserReq);
            String jwt = jwtService.createJwt(userIdx);
            return new PostUserRes(userIdx, jwt);
        } catch (Exception e) {
            throw new Exception("회원 가입 실패");
        }
    }

    // 회원 정보 수정
    public void modifyUser(PatchUserReq patchUserReq) throws Exception{
        try {
            int result = userDao.modifyUser(patchUserReq);
            if (result == 0) {
                throw new Exception("회원정보 수정 실패");
            }
        } catch (Exception e) {
            throw new Exception("데이터베이스 오류");
        }
    }

    // 로그인
    public PostLoginRes logIn(PostLoginReq postLoginReq) throws Exception {
        User user = userDao.getPassword(postLoginReq);
        try {
            String password = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(user.getPassword());
        } catch (Exception e) {
            throw new Exception("비밀번호 암호화 오류");
        }

        if (postLoginReq.getPassword().equals(user.getPassword())) {
            int userIdx = userDao.getPassword(postLoginReq).getUserIdx();
            String jwt = jwtService.createJwt(userIdx);
            return new PostLoginRes(userIdx, jwt);
        } else {
            throw new Exception("로그인 실패");
        }
    }

    // 전체 유저 정보 조회
    public List<GetUserRes> getUsers() throws Exception{
        try {
            return userDao.getUsers();
        } catch (Exception e) {
            throw new Exception("유저 정보 조회 실패");
        }
    }

    // 유저 아이디로 정보 조회
    public GetUserRes getUser(int userIdx) throws Exception {
        try {
            return userDao.getUser(userIdx);
        } catch (Exception e) {
            throw new Exception("유저 정보 조회 실패");
        }
    }
}
