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
@Table(name = "vozv_sed")
@Getter
@Setter
public class SedRecall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"Number_VD\"", length = 7)
    private String pensionCaseNumber;

    @Column(name = "\"FIO_Recipient\"")
    private String pensionerName;

    @Column(name = "\"Number_Package\"", length = 10)
    private String packageNumber;

    @Column(name = "\"Month\"")
    private Integer month;

    @Column(name = "\"Year\"")
    private Integer year;

    @Column(name = "\"Prichina\"")
    private String reason;

    @Column(name = "\"Srochnost\"", length = 5)
    private String urgent;

    @Column(name = "\"FIO_SP_ONiVB\"")
    private String ovpSpecialist;

    @Column(name = "\"Primechanie_ONiVB\"")
    private String note;

    @Column(name = "\"Ot_Ispolnenie\"", length = 20)
    private String executionMark;

    @Column(name = "\"FIO_OVVBD\"")
    private String ovidSpecialist;

    @Column(name = "\"Primechanie_OVVBD\"")
    private String ovidNote;

    @Column(name = "\"Area\"")
    private Integer district;
}
