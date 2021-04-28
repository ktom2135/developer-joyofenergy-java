package uk.tw.energy.controller;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

public class CostController {

    private final AccountService accountService;
    private final MeterReadingService meterReadingService;
    private final PricePlanService pricePlanService;

    public CostController(AccountService accountService,
        MeterReadingService meterReadingService,
        PricePlanService pricePlanService) {
        this.accountService = accountService;
        this.meterReadingService = meterReadingService;
        this.pricePlanService = pricePlanService;
    }

    public ResponseEntity getLastWeekCost(String meterId) {
        if (StringUtils.isEmpty(this.accountService.getPricePlanIdForSmartMeterId(meterId))) {
            return ResponseEntity.badRequest().body("error");
        }
        String pricePlanIdForSmartMeterId = accountService.getPricePlanIdForSmartMeterId(meterId);

        PricePlan pricePlanById = pricePlanService.getPricePlanById(pricePlanIdForSmartMeterId);
        BigDecimal bigDecimal = pricePlanService.calculateCost(meterReadingService.getReadings(meterId).get(), pricePlanById);
        return ResponseEntity.ok().body(bigDecimal);
    }
}
