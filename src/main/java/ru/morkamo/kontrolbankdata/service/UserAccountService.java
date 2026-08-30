package ru.morkamo.kontrolbankdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

import ru.morkamo.kontrolbankdata.model.UserAccount;
import ru.morkamo.kontrolbankdata.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public Optional<UserAccount> findByCredentials(String username, String password) {
        return userAccountRepository.findByUsernameAndPassword(username, password);
    }

    public Optional<UserAccount> findById(Short id) {
        return userAccountRepository.findById(id);
    }
}
