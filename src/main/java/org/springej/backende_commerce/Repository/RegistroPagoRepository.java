package org.springej.backende_commerce.Repository;

import org.springej.backende_commerce.Model.RegistroPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistroPagoRepository extends JpaRepository<RegistroPago, Long> {
    Optional<RegistroPago> findByMpPaymentId(String mpPaymentId);
}
