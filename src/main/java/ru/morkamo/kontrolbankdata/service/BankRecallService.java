package ru.morkamo.kontrolbankdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.morkamo.kontrolbankdata.model.BankRecall;
import ru.morkamo.kontrolbankdata.repository.BankRecallRepository;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BankRecallService {

    private final BankRecallRepository bankRecallRepository;

    public List<BankRecall> search(
            String pensionCaseNumber,
            String pensionerName,
            String bank,
            Integer month,
            Integer year,
            String period) {
        return bankRecallRepository.search(pensionCaseNumber, pensionerName, bank, month, year, period);
    }

    public void save(BankRecall bankRecall) {
        bankRecallRepository.save(bankRecall);
    }

    public boolean existsById(Integer id) {
        return id != null && bankRecallRepository.existsById(id);
    }

    public BankRecall getById(Integer id) {
        return bankRecallRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запись не найдена"));
    }

    public void delete(Integer id) {
        bankRecallRepository.deleteById(id);
    }
}
