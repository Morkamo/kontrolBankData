package ru.morkamo.kontrolbankdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.morkamo.kontrolbankdata.model.BankRecall;

public interface BankRecallRepository extends JpaRepository<BankRecall, Integer> {

    @Query("""
            select bankRecall
            from BankRecall bankRecall
            where (:pensionCaseNumber is null or :pensionCaseNumber = ''
                or bankRecall.pensionCaseNumber like concat('%', :pensionCaseNumber, '%'))
            and (:pensionerName is null or :pensionerName = ''
                or lower(bankRecall.pensionerName) like lower(concat('%', :pensionerName, '%')))
            and (:bank is null or :bank = ''
                or bankRecall.bank = :bank)
            and (:month is null or bankRecall.month = :month)
            and (:year is null or bankRecall.year = :year)
            and (:period is null or :period = ''
                or lower(bankRecall.period) like lower(concat('%', :period, '%')))
            order by bankRecall.id desc
            """)
    List<BankRecall> search(
            @Param("pensionCaseNumber") String pensionCaseNumber,
            @Param("pensionerName") String pensionerName,
            @Param("bank") String bank,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("period") String period);
}
