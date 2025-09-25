package org.springej.backende_commerce.Controller;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import org.springej.backende_commerce.Model.RegistroPago;
import org.springej.backende_commerce.Model.Usuario;
import org.springej.backende_commerce.Model.Venta;
import org.springej.backende_commerce.Repository.RegistroPagoRepository;
import org.springej.backende_commerce.Repository.UsuarioRepository;
import org.springej.backende_commerce.Repository.VentaRepository;
import org.springej.backende_commerce.Service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus; // ✅ faltaba importar
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final VentaRepository ventaRepository;
    private final RegistroPagoRepository registroPagoRepository; // ✅ renombrado para ser consistente
    private final UsuarioRepository usuarioRepository;

    @Value("${mercadopago.base.url}")
    private String baseUrl;

    public PaymentController(PaymentService paymentService,
                             VentaRepository ventaRepository,
                             RegistroPagoRepository registroPagoRepository,
                             UsuarioRepository usuarioRepository) {
        this.paymentService = paymentService;
        this.ventaRepository = ventaRepository;
        this.registroPagoRepository = registroPagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody Map<String, Object> body) throws Exception {
        BigDecimal total = new BigDecimal(body.get("total").toString());

        Usuario usuario = usuarioRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Venta venta = new Venta();
        venta.setFechaVenta(LocalDate.now());
        venta.setUsuario(usuario);
        venta.setEstado("PENDIENTE"); // ✅ inicializar estado
        venta = ventaRepository.save(venta);

        // Usar el id de la venta como external_reference
        String externalRef = String.valueOf(venta.getId());

        String notificationUrl = baseUrl + "/api/payments/webhook";

        String initPoint = paymentService.createPreference(
                "Compra Ecommerce",
                total,
                1,
                externalRef,
                notificationUrl
        );

        return ResponseEntity.ok(Map.of(
                "init_point", initPoint,
                "ventaId", String.valueOf(venta.getId())
        ));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            Long paymentId = Long.valueOf(((Map<String, Object>) payload.get("data")).get("id").toString());

            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(paymentId);

            // Recuperar Venta desde external_reference
            Long ventaId = Long.valueOf(payment.getExternalReference());
            Venta venta = ventaRepository.findById(ventaId)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

            // Buscar si ya existe un registro (idempotencia)
            RegistroPago registro = registroPagoRepository.findByMpPaymentId(String.valueOf(paymentId))
                    .orElseGet(RegistroPago::new);

            registro.setMpPaymentId(String.valueOf(paymentId));
            registro.setStatus(payment.getStatus());
            registro.setAmount(payment.getTransactionAmount().floatValue());
            registro.setPaymentMethod(payment.getPaymentMethodId());

            // ✅ Conversión de OffsetDateTime a LocalDateTime
            if (payment.getDateApproved() != null) {
                registro.setDateApproved(payment.getDateApproved().toLocalDateTime());
            }

            registro.setVenta(venta);

            registroPagoRepository.save(registro);

            // Actualizar estado de la venta
            switch (payment.getStatus()) {
                case "approved" -> venta.setEstado("APROBADA");
                case "in_process", "pending" -> venta.setEstado("PENDIENTE");
                case "rejected" -> venta.setEstado("RECHAZADA");
                default -> venta.setEstado("DESCONOCIDO");
            }
            ventaRepository.save(venta);

            return ResponseEntity.ok("Webhook procesado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando webhook: " + e.getMessage());
        }
    }

    // ✅ Endpoints de prueba/listado
    @GetMapping("/pagos")
    public List<RegistroPago> listarPagos() {
        return registroPagoRepository.findAll();
    }

    @GetMapping("/ventas")
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @GetMapping("/success")
    public String pagoExitoso(@RequestParam Map<String, String> params) {
        return "✅ Pago aprobado! Datos: " + params;
    }

    @GetMapping("/failure")
    public String pagoFallido(@RequestParam Map<String, String> params) {
        return "❌ Pago fallido. Datos: " + params;
    }

    @GetMapping("/pending")
    public String pagoPendiente(@RequestParam Map<String, String> params) {
        return "⏳ Pago pendiente. Datos: " + params;
    }
}
