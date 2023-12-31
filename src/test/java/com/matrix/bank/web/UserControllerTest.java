package com.matrix.bank.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matrix.bank.config.dummy.DummyObject;
import com.matrix.bank.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.persistence.EntityManager;

import static com.matrix.bank.dto.user.UserReqDto.JoinReqDto;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * author         : Jason Lee
 * date           : 2023-07-23
 * description    :
 */
@ActiveProfiles("test")
@Sql("classpath:db/teardown.sql")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class UserControllerTest extends DummyObject {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    EntityManager em;

    @BeforeEach
    public void setUp() {
        userRepository.save(newUser("bank", "돈이좋아"));
        em.clear();
    }

    @Test
    void join_success_test() throws Exception {
        // Given
        JoinReqDto joinReqDto = new JoinReqDto();
        joinReqDto.setUsername("love");
        joinReqDto.setPassword("1234");
        joinReqDto.setEmail("love@nate.com");
        joinReqDto.setFullname("러브");

        String requestBody = om.writeValueAsString(joinReqDto);
//        System.out.println("테스트 : " + requestBody);

        // When
        ResultActions resultActions = mvc.perform(post("/api/join")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
//        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
//        System.out.println("테스트 : " + responseBody);

        // Then
        resultActions.andExpect(status().isCreated());
    }

    @Test
    void join_fail_test() throws Exception {
        // Given
        JoinReqDto joinReqDto = new JoinReqDto();
        joinReqDto.setUsername("bank");
        joinReqDto.setPassword("1234");
        joinReqDto.setEmail("bank@nate.com");
        joinReqDto.setFullname("돈이좋아");

        String requestBody = om.writeValueAsString(joinReqDto);
//        System.out.println("테스트 : " + requestBody);

        // When
        ResultActions resultActions = mvc.perform(post("/api/join")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
//        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
//        System.out.println("테스트 : " + responseBody);

        // Then
        resultActions.andExpect(status().isBadRequest());
    }

    private void dataSetting() {

    }
}
