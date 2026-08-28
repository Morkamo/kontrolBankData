package ru.morkamo.kontrolbankdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserAccount {

    @Id
    private Short id;

    @Column(name = "User", length = 40, nullable = false)
    private String username;

    @Column(name = "PWD", length = 8)
    private String password;
}
