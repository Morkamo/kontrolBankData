package ru.morkamo.kontrolbankdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.morkamo.kontrolbankdata.model.SedRecall;

public interface SedRecallRepository extends JpaRepository<SedRecall, Integer> {

    @Query("""
            select record
            from SedRecall record
            where (:pensionCaseNumber is null or :pensionCaseNumber = ''
                or record.pensionCaseNumber like concat('%', :pensionCaseNumber, '%'))
            and (:pensionerName is null or :pensionerName = ''
                or lower(record.pensionerName) like lower(concat('%', :pensionerName, '%')))
            and (:month is null or record.month = :month)
            and (:year is null or record.year = :year)
            order by record.id desc
            """)
    List<SedRecall> search(
            @Param("pensionCaseNumber") String pensionCaseNumber,
            @Param("pensionerName") String pensionerName,
            @Param("month") Integer month,
            @Param("year") Integer year);
}
