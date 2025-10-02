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

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    // 카카오 콜백
    @GetMapping("/callback")
    public String kakaoCallback(@RequestParam("code") String code, HttpSession session) throws Exception {

        // 1. code -> access_token
        RestTemplate rest = new RestTemplate();
        String tokenUrl = "https://kauth.kakao.com/oauth/token"
                + "?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&code=" + code;

        JsonNode tokenNode = rest.postForObject(tokenUrl, null, JsonNode.class);
        String accessToken = tokenNode.get("access_token").asText();

        // 2. access_token -> 사용자 정보
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

        JsonNode userNode = rest.exchange("https://kapi.kakao.com/v2/user/me",
                org.springframework.http.HttpMethod.GET,
                entity,
                JsonNode.class).getBody();

        String kakaoId = userNode.get("id").asText();
        String nickname = userNode.path("properties").path("nickname").asText("");

        // 3. DB 조회 -> 없으면 신규 생성
        User user = userService.getUserByKakaoId(kakaoId);
        if (user == null) {
            user = new User();
            user.setUserId("kakao_" + kakaoId);  // PK 제약 있으니 prefix 붙이기
            user.setUserName(nickname);
            user.setPassword("SOCIAL"); // 의미상 placeholder
            userService.addUser(user);
            userService.linkKakaoId(user.getUserId(), kakaoId);
        }

        // 4. 세션 저장
        session.setAttribute("user", user);

        return "redirect:/index.jsp";
    }
}
