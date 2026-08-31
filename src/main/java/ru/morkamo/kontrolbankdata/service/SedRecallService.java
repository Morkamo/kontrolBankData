package ru.morkamo.kontrolbankdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.morkamo.kontrolbankdata.model.SedRecall;
import ru.morkamo.kontrolbankdata.repository.SedRecallRepository;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SedRecallService {

    private final SedRecallRepository sedRecallRepository;

    public List<SedRecall> search(String pensionCaseNumber, String pensionerName, Integer month, Integer year) {
        return sedRecallRepository
                .search(pensionCaseNumber, pensionerName, month, year);
    }

    public void save(SedRecall sedRecall) {
        sedRecallRepository.save(sedRecall);
    }

    public boolean existsById(Integer id) {
        return id != null && sedRecallRepository.existsById(id);
    }

    public SedRecall getById(Integer id) {
        return sedRecallRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена"));
    }

    public void delete(Integer id) {
        sedRecallRepository.deleteById(id);
    }
}
