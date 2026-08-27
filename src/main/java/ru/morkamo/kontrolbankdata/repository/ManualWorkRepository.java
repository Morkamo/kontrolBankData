package ru.morkamo.kontrolbankdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.morkamo.kontrolbankdata.model.ManualWork;

public interface ManualWorkRepository extends JpaRepository<ManualWork, Integer> {

    @Query("""
            select record
            from ManualWork record
            where (:pensionCaseNumber is null or :pensionCaseNumber = ''
                or record.pensionCaseNumber like concat('%', :pensionCaseNumber, '%'))
            and (:pensionerName is null or :pensionerName = ''
                or lower(record.pensionerName) like lower(concat('%', :pensionerName, '%')))
            and (:period is null or :period = ''
                or lower(record.period) like lower(concat('%', :period, '%')))
            order by record.id desc
            """)
    List<ManualWork> search(
            @Param("pensionCaseNumber") String pensionCaseNumber,
            @Param("pensionerName") String pensionerName,
            @Param("period") String period);
}
