package ru.morkamo.kontrolbankdata.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.morkamo.kontrolbankdata.model.UserAccount;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, Short> {

    Optional<UserAccount> findByUsername(String username);

    List<UserAccount> findAllByDepartmentIdNotOrderByIdAsc(Integer departmentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET id = :newId WHERE id = :oldId", nativeQuery = true)
    void changeId(@Param("oldId") Short oldId, @Param("newId") Short newId);
}
