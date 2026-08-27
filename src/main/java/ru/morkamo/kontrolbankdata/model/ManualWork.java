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
@Table(name = "razovie_rr")
@Getter
@Setter
public class ManualWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"ID\"")
    private Integer id;

    @Column(name = "\"Number_VD\"", length = 10)
    private String pensionCaseNumber;

    @Column(name = "\"FIO_Recipient\"")
    private String pensionerName;

    @Column(name = "\"Uchet_V\"", length = 20)
    private String accounting;

    @Column(name = "\"Prichina_oformleniay_rv\"", length = 100)
    private String reason;

    @Column(name = "\"Type_Pay\"", length = 20)
    private String paymentType;

    @Column(name = "\"Istochnik\"", length = 20)
    private String source;

    @Column(name = "\"Amount\"", length = 20)
    private String amount;

    @Column(name = "\"Period_rv\"", length = 200)
    private String period;

    @Column(name = "\"Srochnost\"", length = 5)
    private String urgent;

    @Column(name = "\"FIO_SP_ONiVP\"")
    private String ovpSpecialist;

    @Column(name = "\"Primechanie_ONiVB\"")
    private String note;

    @Column(name = "\"Ot_ispolneniay\"", length = 20)
    private String executionMark;

    @Column(name = "\"FIO_SP_OVVBD\"")
    private String ovidSpecialist;

    @Column(name = "\"Primechanie_OVVBD\"", length = 200)
    private String ovidNote;

    @Column(name = "\"Result_kontr_1\"", length = 30)
    private String controlResult1;

    @Column(name = "\"FIO_SP_KRO_1\"")
    private String controlSpecialist1;

    @Column(name = "\"Result_kontr_2\"", length = 30)
    private String controlResult2;

    @Column(name = "\"FIO_SP_KRO_2\"")
    private String controlSpecialist2;

    @Column(name = "\"Area\"")
    private Integer district;
}
