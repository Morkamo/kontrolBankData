package ru.morkamo.kontrolbankdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.morkamo.kontrolbankdata.model.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Short> {

    boolean existsByUsernameAndPassword(String username, String password);
}
