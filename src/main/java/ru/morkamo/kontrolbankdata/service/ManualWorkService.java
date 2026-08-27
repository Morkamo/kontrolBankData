package ru.morkamo.kontrolbankdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.morkamo.kontrolbankdata.model.ManualWork;
import ru.morkamo.kontrolbankdata.repository.ManualWorkRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManualWorkService {

    private final ManualWorkRepository manualWorkRepository;

    public List<ManualWork> search(String pensionCaseNumber, String pensionerName, String period) {
        return manualWorkRepository.search(pensionCaseNumber, pensionerName, period);
    }

    public ManualWork create(ManualWork manualWork) {
        return manualWorkRepository.save(manualWork);
    }

    public void delete(Integer id) {
        manualWorkRepository.deleteById(id);
    }
}
