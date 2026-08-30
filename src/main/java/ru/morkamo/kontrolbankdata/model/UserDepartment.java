package ru.morkamo.kontrolbankdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_departments")
@Getter
@Setter
public class UserDepartment {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "department", nullable = false)
    private String department;
}
