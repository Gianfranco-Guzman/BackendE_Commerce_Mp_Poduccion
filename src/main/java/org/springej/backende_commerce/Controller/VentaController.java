package org.springej.backende_commerce.Controller;

import org.springej.backende_commerce.Model.Venta;
import org.springej.backende_commerce.Service.VentaService;
import org.springej.backende_commerce.DTO.VentaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ventas")
public class VentaController {

    private static final Logger logger = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private VentaService ventaService;

    // POST /ventas → Registrar una venta nueva
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody @Valid VentaDTO ventaDTO, BindingResult result) {
        logger.info("Iniciando registro de nueva venta para usuario ID: {}", ventaDTO.getIdUsuario());

        // Validar errores de entrada
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errores.put(error.getField(), error.getDefaultMessage())
            );
            logger.warn("Errores de validación en registro de venta: {}", errores);
            return ResponseEntity.badRequest().body(errores);
        }

        try {
            Venta nuevaVenta = ventaService.registrarVenta(ventaDTO);
            logger.info("Venta registrada exitosamente con ID: {} para usuario: {}",
                    nuevaVenta.getId(), ventaDTO.getIdUsuario());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaVenta);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al registrar venta: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error interno al registrar venta para usuario {}: {}",
                    ventaDTO.getIdUsuario(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // GET /ventas/usuario/{idUsuario} → Obtener todas las ventas de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> obtenerVentasPorUsuario(@PathVariable Long idUsuario) {
        logger.info("Consultando todas las ventas para usuario ID: {}", idUsuario);

        try {
            List<Venta> ventas = ventaService.obtenerVentasPorUsuario(idUsuario);

            if (ventas.isEmpty()) {
                logger.info("No se encontraron ventas para el usuario ID: {}", idUsuario);
                return ResponseEntity.noContent().build();
            }

            logger.info("Se encontraron {} ventas para el usuario ID: {}", ventas.size(), idUsuario);
            return ResponseEntity.ok(ventas);

        } catch (Exception e) {
            logger.error("Error al consultar ventas del usuario {}: {}", idUsuario, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al consultar las ventas"));
        }
    }
}