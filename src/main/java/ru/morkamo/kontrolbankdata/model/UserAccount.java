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
    @Column(name = "id")
    private Short id;

    @Column(name = "User", length = 40, nullable = false)
    private String username;

    @Column(name = "FIO", length = 40)
    private String fullName;

    @Column(name = "PWD", length = 8)
    private String password;

    @Column(name = "UserGrup")
    private Short userGroup;

    @Column(name = "Name_PC", length = 20)
    private String computerName;

    @Column(name = "SendMessage")
    private Integer sendMessage;

    @Column(name = "UserGrupForInteraction")
    private Integer userGroupForInteraction;

    @Column(name = "ActiveOfRealTime")
    private Integer activeOfRealTime;

    @Column(name = "UserGrupForONVP")
    private Integer userGroupForOnvp;

    @Column(name = "UserGrupForStatic")
    private Integer userGroupForStatic;

    @Column(name = "rang")
    private Integer rank;

    @Column(name = "stamp", length = 100)
    private String stamp;

    @Column(name = "department")
    private Integer departmentId;
}
