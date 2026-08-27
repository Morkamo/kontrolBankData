package ru.morkamo.kontrolbankdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vozv_bank")
@Getter
@Setter
public class BankRecall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"Number_VD\"", length = 7)
    private String pensionCaseNumber;

    @Column(name = "\"FIO_Recipient\"", length = 200)
    private String pensionerName;

    @Column(name = "\"NameBank\"", length = 255)
    private String bank;

    @Column(name = "\"Type_Vozv\"", length = 30)
    private String recallType;

    @Column(name = "\"Number_package\"", length = 10)
    private String packageNumber;

    @Column(name = "\"Month\"")
    private Integer month;

    @Column(name = "\"Year\"")
    private Integer year;

    @Column(name = "\"Prichina\"", length = 255)
    private String reason;

    @Column(name = "\"Period\"", length = 255)
    private String period;

    @Column(name = "\"Srochnost\"", length = 4)
    private String urgent;

    @Column(name = "\"Area\"")
    private Integer district;

    @Column(name = "\"Primechanie_ONiVBD\"", length = 255)
    private String note;
}
