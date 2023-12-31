package com.matrix.bank.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matrix.bank.config.dummy.DummyObject;
import com.matrix.bank.domain.account.Account;
import com.matrix.bank.domain.account.AccountRepository;
import com.matrix.bank.domain.transaction.Transaction;
import com.matrix.bank.domain.transaction.TransactionRepository;
import com.matrix.bank.domain.user.User;
import com.matrix.bank.domain.user.UserRepository;
import com.matrix.bank.handler.ex.CustomApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.persistence.EntityManager;

import static com.matrix.bank.dto.account.AccountReqDto.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * author         : Jason Lee
 * date           : 2023-07-26
 * description    :
 */
@ActiveProfiles("test")
@Sql("classpath:db/teardown.sql")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AccountControllerTest extends DummyObject {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    public void setUp() {
        dataSetting();
        em.clear();
    }

    // jwt token -> 인증필터 -> 시큐리티 세션생성
    // setupBefore=TEST_METHOD (setUp 메소드 실행 전에 실행)
    // setupBefore = TEST_EXECUTION (save_account_test() 메서드 실행전에 수행)
    @WithUserDetails(value = "bank", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    // DB에서 username = bank 조회를 해서 세션에 담아주는 어노테이션
    @Test
    void save_account_test() throws Exception {
        // Given
        AccountSaveReqDto accountSaveReqDto = new AccountSaveReqDto(9999L, 1234L);
        String requestBody = om.writeValueAsString(accountSaveReqDto);
        System.out.println("테스트 : " + requestBody);

        // When
        // 시큐리티에 세션이 존재한다면 "/api/s/account"로 접근 가능
        ResultActions resultActions = mvc.perform(post("/api/s/account")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // Then
        resultActions.andExpect(status().isCreated());
    }

    @WithUserDetails(value = "bank", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void find_user_account_test() throws Exception {
        // Given

        // When
        ResultActions resultActions = mvc.perform(get("/api/s/account/login-user"));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // Then
        resultActions.andExpect(status().isOk());
    }

    /**
     * 테스트시에는 insert 한 것들이 전부 Persistence Context에 올라간다.(영속화)
     * 영속화 된 것들을 초기화 해주는 것이 개발 모드와 동일한 환경으로 테스트를 할 수 있게 해준다.
     * 최초 select는 쿼리가 발생하지만 PC에 있으면 1차 캐시를 한다.
     * Lazy 로딩은 PC에 있다면 쿼리도 발생하지 않는다. 그러나 PC에 없다면 쿼리가 발생한다.
     */
    @WithUserDetails(value = "bank", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void delete_account_test() throws Exception {
        // Given
        Long number = 1111L;

        // When
        // delete 요청은 body가 없고 따라서 body를 설명하는 content-type도 필요 없다.
        ResultActions resultActions = mvc.perform(delete("/api/s/account/" + number));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 체크 : " + responseBody);

        // Then
        // Junit test에서 delete 쿼리는 DB 관련(DML)으로 가장 마지막에 실행되면 발동 안함.
        assertThrows(CustomApiException.class, () ->
                accountRepository.findByNumber(number).orElseThrow(
                        () -> new CustomApiException("계좌를 찾을 수 없습니다."))
        );
    }

    @Test
    void deposit_account_test() throws Exception {
        // Given
        AccountDepositReqDto accountDepositReqDto =
                AccountDepositReqDto.builder()
                        .number(1111L)
                        .amount(100L)
                        .classify("DEPOSIT")
                        .tel("010123345678")
                        .build();
        String requestBody = om.writeValueAsString(accountDepositReqDto);
        System.out.println("테스트 : " + requestBody);

        // When
        ResultActions resultActions = mvc.perform(post("/api/account/deposit")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // Then
        resultActions.andExpect(status().isCreated()); // Dto가 잘 만들어졌는지 확인
        // 입금 후 잔액이 맞는지 여부는 Service에서 테스트 하고 와야 한다.
    }

    @WithUserDetails(value = "bank", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void withdraw_account_test() throws Exception {
        // given
        AccountWithdrawReqDto accountWithdrawReqDto =
                AccountWithdrawReqDto.builder()
                        .number(1111L)
                        .password(1234L)
                        .amount(100L)
                        .classify("WITHDRAW")
                        .build();

        String requestBody = om.writeValueAsString(accountWithdrawReqDto);
        System.out.println("테스트 : " + requestBody);

        // when
        ResultActions resultActions = mvc.perform(post("/api/s/account/withdraw")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // then
        resultActions.andExpect(status().isCreated());
    }

    @WithUserDetails(value = "bank", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void transfer_account_test() throws Exception {
        // given
        AccountTransferReqDto accountTransferReqDto =
                AccountTransferReqDto.builder()
                        .withdrawNumber(1111L)
                        .depositNumber(2222L)
                        .withdrawPassword(1234L)
                        .amount(100L)
                        .classify("TRANSFER")
                        .build();

        String requestBody = om.writeValueAsString(accountTransferReqDto);
        System.out.println("테스트 : " + requestBody);

        // when
        ResultActions resultActions = mvc.perform(post("/api/s/account/transfer")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // then
        resultActions.andExpect(status().isCreated());
    }

    @WithUserDetails(value = "bank", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void findDetailAccount_test() throws Exception {
        // given
        Long number = 1111L;
        String page = "0";

        // when
        ResultActions resultActions = mvc.perform(get("/api/s/account/" + number)
                .param("page", page));
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // then
        resultActions.andExpect(jsonPath("$.data.transactions[0].balance").value(900L));
        resultActions.andExpect(jsonPath("$.data.transactions[1].balance").value(800L));
        resultActions.andExpect(jsonPath("$.data.transactions[2].balance").value(700L));
        resultActions.andExpect(jsonPath("$.data.transactions[3].balance").value(800L));
    }

    private void dataSetting() {
        User bank = userRepository.save(newUser("bank", "돈이좋아"));
        User matrix = userRepository.save(newUser("matrix", "매트릭스"));
        User mind = userRepository.save(newUser("mind", "마인드"));
        User admin = userRepository.save(newUser("admin", "관리자"));

        Account bankAccount1 = accountRepository.save(newAccount(1111L, bank));
        Account matrixAccount = accountRepository.save(newAccount(2222L, matrix));
        Account mindAccount = accountRepository.save(newAccount(3333L, mind));
        Account bankAccount2 = accountRepository.save(newAccount(4444L, bank));

        Transaction withdrawTransaction1 = transactionRepository
                .save(newWithdrawTransaction(bankAccount1, accountRepository));
        Transaction depositTransaction1 = transactionRepository
                .save(newDepositTransaction(matrixAccount, accountRepository));
        Transaction transferTransaction1 = transactionRepository
                .save(newTransferTransaction(bankAccount1, matrixAccount, accountRepository));
        Transaction transferTransaction2 = transactionRepository
                .save(newTransferTransaction(bankAccount1, mindAccount, accountRepository));
        Transaction transferTransaction3 = transactionRepository
                .save(newTransferTransaction(matrixAccount, bankAccount1, accountRepository));
    }
}
