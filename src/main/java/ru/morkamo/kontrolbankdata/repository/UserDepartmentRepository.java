package ru.morkamo.kontrolbankdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.morkamo.kontrolbankdata.model.UserDepartment;

public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Integer> {
}
