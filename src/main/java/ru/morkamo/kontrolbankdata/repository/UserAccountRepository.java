package ru.morkamo.kontrolbankdata.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.morkamo.kontrolbankdata.model.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Short> {

    Optional<UserAccount> findByUsernameAndPassword(String username, String password);
}
