package org.springej.backende_commerce.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    @Value("${mercadopago.access.token}")
    private String mpAccessToken;

    @Value("${mercadopago.base.url}")
    private String baseUrl;

    /**
     * Crea una preferencia de pago en Mercado Pago
     */
    public String createPreference(String title, BigDecimal unitPrice, int quantity,
                                   String externalRef, String notificationUrl) throws Exception {
        // Inicializa Mercado Pago SDK con el token
        MercadoPagoConfig.setAccessToken(mpAccessToken);

        PreferenceClient client = new PreferenceClient();

        // Item de la compra
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title(title)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .currencyId("ARS")
                .build();

        // URLs de retorno
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(baseUrl + "/api/payments/success")
                .failure(baseUrl + "/api/payments/failure")
                .pending(baseUrl + "/api/payments/pending")
                .build();

        // Construcción de la preferencia
        PreferenceRequest request = PreferenceRequest.builder()
                .items(List.of(item))
                .externalReference(externalRef) // ID de la Venta
                .notificationUrl(notificationUrl) // Webhook
                .backUrls(backUrls)
                .autoReturn("approved")
                .build();

        try {
            Preference preference = client.create(request);
            return preference.getInitPoint(); // ✅ En producción usamos getInitPoint()
        } catch (MPApiException e) {
            System.err.println("❌ Mercado Pago API Error:");
            System.err.println("Status Code: " + e.getStatusCode());
            if (e.getApiResponse() != null) {
                System.err.println("Response Body: " + e.getApiResponse().getContent());
            }
            throw e;
        }
    }
}
