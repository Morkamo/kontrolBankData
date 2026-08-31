package ru.morkamo.kontrolbankdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.morkamo.kontrolbankdata.model.ManualWork;
import ru.morkamo.kontrolbankdata.repository.ManualWorkRepository;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ManualWorkService {

    private final ManualWorkRepository manualWorkRepository;

    public List<ManualWork> search(String pensionCaseNumber, String pensionerName, String period) {
        return manualWorkRepository
                .search(pensionCaseNumber, pensionerName, period);
    }

    public void save(ManualWork manualWork) {
        manualWorkRepository.save(manualWork);
    }

    public boolean existsById(Integer id) {
        return id != null && manualWorkRepository.existsById(id);
    }

    public ManualWork getById(Integer id) {
        return manualWorkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена"));
    }

    public void delete(Integer id) {
        manualWorkRepository.deleteById(id);
    }
}
