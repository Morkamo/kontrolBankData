package ru.morkamo.kontrolbankdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.morkamo.kontrolbankdata.model.DeliveryOrganization;

public interface DeliveryOrganizationRepository extends JpaRepository<DeliveryOrganization, Integer> {

    boolean existsByName(String name);
}
