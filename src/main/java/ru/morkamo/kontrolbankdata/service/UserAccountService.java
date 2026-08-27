package ru.morkamo.kontrolbankdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.morkamo.kontrolbankdata.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public boolean canLogin(String username, String password) {
        return userAccountRepository.existsByUsernameAndPassword(username, password);
    }
}
