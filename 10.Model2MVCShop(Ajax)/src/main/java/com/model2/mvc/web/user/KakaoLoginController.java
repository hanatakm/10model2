package com.model2.mvc.web.user;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.user.UserService;

@Controller
@RequestMapping("/user/kakao")
public class KakaoLoginController {

    @Autowired
    private UserService userService;

    @Value("${kakao.client.id}")   // REST API 키
    private String clientId;

    @Value("${kakao.redirect.uri}") // Redirect URI (127 기준)
    private String redirectUri;

    // 선택사항: Client Secret (없으면 빈값)
    @Value("${kakao.client.secret:}")  
    private String clientSecret;

    @GetMapping("/callback")
    public String kakaoCallback(@RequestParam("code") String code, HttpSession session) throws Exception {

        // ===== 1. code -> access_token 요청 =====
        RestTemplate rest = new RestTemplate();
        String tokenUrl = "https://kauth.kakao.com/oauth/token"
                + "?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&code=" + code;

        // 선택적으로 secret 추가
        if (clientSecret != null && !clientSecret.isEmpty()) {
            tokenUrl += "&client_secret=" + clientSecret;
        }

        JsonNode tokenNode = rest.postForObject(tokenUrl, null, JsonNode.class);
        String accessToken = tokenNode.get("access_token").asText();

        // ===== 2. access_token -> 사용자 정보 =====
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        JsonNode userNode = rest.exchange("https://kapi.kakao.com/v2/user/me",
                org.springframework.http.HttpMethod.GET,
                entity,
                JsonNode.class).getBody();

        String kakaoId = userNode.get("id").asText();
        String nickname = userNode.path("properties").path("nickname").asText("카카오사용자");

     // 3. DB 조회 -> 없으면 신규 생성
     // 3. DB 조회 -> 없으면 신규 생성
     // KakaoLoginController 내 콜백 로직의 "DB 조회" 부분을 이렇게:
        User user = userService.getUserByKakaoId(kakaoId);

        if (user == null) {
            // ✅ 과거에 kakao_id 없이 'kakao_{id}' 형태로만 만들어진 계정 복구
            User byUserId = userService.getUser("kakao_" + kakaoId);
            if (byUserId != null) {
                // kakao_id 연결만 보정
                userService.linkKakaoId(byUserId.getUserId(), kakaoId);
                user = byUserId;  // 이걸로 로그인 진행
            } else {
                // 완전 신규면 그때만 INSERT
                user = new User();
                user.setUserId("kakao_" + kakaoId);
                user.setUserName((nickname != null && !nickname.isEmpty()) ? nickname : "카카오사용자");
                user.setPassword("SOCIAL");
                userService.addUser(user);
                userService.linkKakaoId(user.getUserId(), kakaoId);
            }
        }

        // 세션 저장
        session.setAttribute("user", user);
        return "redirect:/index.jsp";

     


      
    }
}
