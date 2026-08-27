package ru.morkamo.kontrolbankdata.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.morkamo.kontrolbankdata.model.DeliveryOrganization;
import ru.morkamo.kontrolbankdata.repository.DeliveryOrganizationRepository;

@Service
@RequiredArgsConstructor
public class DeliveryOrganizationService {

    private final DeliveryOrganizationRepository deliveryOrganizationRepository;

    public List<DeliveryOrganization> getAll() {
        return deliveryOrganizationRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
