package org.springej.backende_commerce.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_pago")
@Data
@NoArgsConstructor
public class RegistroPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRegistro_Pago")
    private Long id;

    // ID de pago que devuelve Mercado Pago
    @Column(name = "mp_payment_id", unique = true)
    private String mpPaymentId;

    // Estado del pago (approved, pending, rejected)
    @Column(name = "status")
    private String status;

    // Monto del pago
    @Column(name = "amount")
    private Float amount;

    // Método de pago (visa, mastercard, account_money, etc.)
    @Column(name = "payment_method")
    private String paymentMethod;

    // Fecha de aprobación
    @Column(name = "date_approved")
    private LocalDateTime dateApproved;

    // Relación con Venta
    @OneToOne
    @JoinColumn(name = "idVenta", nullable = false)
    private Venta venta;
}
