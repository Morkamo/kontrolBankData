package ru.morkamo.kontrolbankdata.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Column(name = "\"NameBank\"")
    private String bank;

    @Column(name = "\"Type_Vozv\"", length = 30)
    private String recallType;

    @Column(name = "\"Number_package\"", length = 10)
    private String packageNumber;

    @Column(name = "\"Month\"")
    private Integer month;

    @Column(name = "\"Year\"")
    private Integer year;

    @Column(name = "\"Prichina\"")
    private String reason;

    @Column(name = "\"Data_death\"", length = 100)
    private String deathDate;

    @Column(name = "\"Period\"")
    private String period;

    @Column(name = "\"Srochnost\"", length = 4)
    private String urgent;

    @Column(name = "\"Type_pay\"", length = 100)
    private String paymentType;

    @Column(name = "\"Amount_vozv\"", length = 15)
    private String recallAmount;

    @Column(name = "\"FIO_SP_ONiVB\"")
    private String ovpSpecialist;

    @Column(name = "\"Area\"")
    private Integer district;

    @Column(name = "\"Primechanie_ONiVBD\"")
    private String note;

    @Column(name = "\"Check_finish\"", length = 20)
    private String executionMark;

    @Column(name = "\"FIO_SP_OVVBD\"")
    private String ovidSpecialist;

    @Column(name = "\"Primechanie_OVVBD\"")
    private String ovidNote;

    @Column(name = "\"AgreementVozv\"")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate agreementDate;
}
