package ru.morkamo.kontrolbankdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.morkamo.kontrolbankdata.model.SedRecall;
import ru.morkamo.kontrolbankdata.repository.SedRecallRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SedRecallService {

    private final SedRecallRepository sedRecallRepository;

    public List<SedRecall> search(String pensionCaseNumber, String pensionerName, Integer month, Integer year) {
        return sedRecallRepository.search(pensionCaseNumber, pensionerName, month, year);
    }

    public SedRecall create(SedRecall sedRecall) {
        return sedRecallRepository.save(sedRecall);
    }

    public void delete(Integer id) {
        sedRecallRepository.deleteById(id);
    }
}
