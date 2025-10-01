package com.model2.mvc.web.user;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.user.UserService;
import com.model2.mvc.common.Search;

/**
 * UserRestController.java
 * 회원 관리를 위한 RESTful API 컨트롤러
 * URL Prefix : /user/json/
 */
@RestController
@RequestMapping("/user/*")
public class UserRestController {

    // UserService 주입 (userServiceImpl 빈을 주입)
    @Autowired
    @Qualifier("userServiceImpl")
    private UserService userService;

    public UserRestController() {
        System.out.println("==> UserRestController 실행됨 : " + this.getClass());
    }

    /**
     * 회원가입 요청 처리
     * @param user 회원 정보 (JSON 형식으로 전달됨)
     * @return 성공 여부 (true)
     */
    @PostMapping("json/addUser")
    public boolean addUser(@RequestBody User user) throws Exception {
        System.out.println("/user/json/addUser : POST 호출됨");
        userService.addUser(user);
        return true;
    }

    /**
     * 로그인 요청 처리
     * @param user 아이디 및 패스워드 정보 포함
     * @param session 로그인 성공 시 세션에 저장
     * @return DB에 저장된 사용자 정보 반환
     */
    @PostMapping("json/login")
    public User login(@RequestBody User user, HttpSession session) throws Exception {
        System.out.println("/user/json/login : POST 호출됨");

        // 사용자 조회
        User dbUser = userService.getUser(user.getUserId());

        // 패스워드 일치 시 세션 저장
        if (dbUser != null && user.getPassword().equals(dbUser.getPassword())) {
            session.setAttribute("user", dbUser);
        }

        return dbUser;
    }

    /**
     * 회원 상세 조회
     * @param userId 조회할 회원의 ID
     * @return 사용자 정보 반환
     */
    @GetMapping("json/getUser/{userId}")
    public User getUser(@PathVariable String userId) throws Exception {
        System.out.println("/user/json/getUser : GET 호출됨");
        return userService.getUser(userId);
    }

    /**
     * 회원 리스트 조회 (검색 + 페이징 지원)
     * @param search 검색 조건 객체 (currentPage, searchKeyword 등 포함)
     * @return 회원 목록 및 전체 수 등의 정보 포함 Map 반환
     */
    @PostMapping("json/getUserList")
    public Map<String, Object> getUserList(@RequestBody Search search) throws Exception {
        System.out.println("/user/json/getUserList : POST 호출됨");
        return userService.getUserList(search);
    }

    /**
     * 회원 정보 수정
     * @param user 수정할 사용자 정보
     * @return 성공 여부 (true)
     */
    @PostMapping("json/updateUser")
    public boolean updateUser(@RequestBody User user) throws Exception {
        System.out.println("/user/json/updateUser : POST 호출됨");
        userService.updateUser(user);
        return true;
    }

    /**
     * 아이디 중복 확인
     * @param userId 중복 체크할 아이디
     * @return 중복 여부 (true: 중복 있음, false: 중복 없음)
     */
    @GetMapping("json/checkDuplication/{userId}")
    public boolean checkDuplication(@PathVariable String userId) throws Exception {
        System.out.println("/user/json/checkDuplication : GET 호출됨");
        return userService.checkDuplication(userId);
    }
}