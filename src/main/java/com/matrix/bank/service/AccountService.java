package com.matrix.bank.service;

import com.matrix.bank.domain.account.Account;
import com.matrix.bank.domain.account.AccountRepository;
import com.matrix.bank.domain.user.User;
import com.matrix.bank.domain.user.UserRepository;
import com.matrix.bank.handler.ex.CustomApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.matrix.bank.dto.account.AccountReqDto.AccountSaveReqDto;
import static com.matrix.bank.dto.account.AccountRespDto.AccountSaveRespDto;

/**
 * author         : Jason Lee
 * date           : 2023-07-25
 * description    :
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public AccountSaveRespDto createAccount(AccountSaveReqDto accountSaveReqDto, Long userId) {
        // User 로그인 되어 있는 상태. User 로그인 되어 있는지 상태 체크는 Controller 의 역할.
        // 1. User DB에 있는지 먼저 검증 겸 User entity 가져오기
        User userPS = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("유저를 찾을 수 없습니다. id = " + userId));

        // 2. 햬당 계좌가 DB에 있는지 중복 여부 체크
        Optional<Account> accountOP = accountRepository.findByNumber(accountSaveReqDto.getNumber());
        if (accountOP.isPresent()) {
            throw new CustomApiException("이미 존재하는 계좌입니다.");
        }

        // 3. 계좌 생성
        Account accountPS = accountRepository.save(accountSaveReqDto.toEntity(userPS));

        // 4. DTO를 응답
        return new AccountSaveRespDto(accountPS);
    }




}