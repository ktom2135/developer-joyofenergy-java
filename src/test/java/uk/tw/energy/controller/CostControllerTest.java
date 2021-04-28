package uk.tw.energy.controller;

import static java.util.Collections.emptyList;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

class CostControllerTest {

    private static final String MOST_EVIL_PRICE_PLAN_ID = "price-plan-0";
    private static final String smartMeterId = "smart-meter-0";
    CostController costController;
    AccountService accountService;
    MeterReadingService meterReadingService;
    PricePlanService pricePlanService;
    final Map<String, List<ElectricityReading>> meterAssociatedReadings = new HashMap<>();

    @BeforeEach
    public void setup() {

        final Map<String, String> smartMeterToPricePlanAccounts = new HashMap<>();
        smartMeterToPricePlanAccounts.put(smartMeterId, MOST_EVIL_PRICE_PLAN_ID);

        accountService = new AccountService(smartMeterToPricePlanAccounts);
        meterReadingService = new MeterReadingService(meterAssociatedReadings);
        final List<PricePlan> pricePlans = new ArrayList<>();
        pricePlans.add(new PricePlan(MOST_EVIL_PRICE_PLAN_ID, "Dr Evil's Dark Energy", BigDecimal.TEN, emptyList()));

        pricePlanService = new PricePlanService(pricePlans, meterReadingService);
        costController = new CostController(accountService, meterReadingService, pricePlanService);
    }

    @Test
    void givenSmartMeterIdHasNoPricePlanShouldReturnError() {
        costController.getLastWeekCost("").getStatusCode().equals(HttpStatus.BAD_REQUEST);
    }

    @Test
    void givenSmartMeterIdHasPricePlanShouldReturnOk() {
        costController.getLastWeekCost(smartMeterId).getStatusCode().equals(HttpStatus.OK);
    }

    @Test
    void givenLastWeekElectricityReadingShouldCalculateCorrect() {
        Instant lastWeedMonday = LocalDateTime.of(2021, 4, 19, 0, 0).atZone(ZoneId.systemDefault()).toInstant();
        Instant instant1 = lastWeedMonday.plusSeconds(60 * 60 * 1);
        Instant instant2 = lastWeedMonday.plusSeconds(60 * 60 * 2);
        List<ElectricityReading> electricityReadings = Arrays.asList(
            new ElectricityReading(instant1, new BigDecimal(11000)),
            new ElectricityReading(instant1, new BigDecimal(12000)));

        meterAssociatedReadings.put(smartMeterId, electricityReadings);
        costController.getLastWeekCost(smartMeterId).getBody().equals(1011100);

    }

}
