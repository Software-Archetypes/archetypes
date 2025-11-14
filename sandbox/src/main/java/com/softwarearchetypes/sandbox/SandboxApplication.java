package com.softwarearchetypes.sandbox;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandboxApplication.class, args);
        new BankAccount();
        new Customer();
        new CreditLine();
        new FleetCardAccount();
        new CustomerSegmentReport();
        new BalanceService().applyTopUp("asdf", BigDecimal.ONE);
        new BalanceService().applyTopUp("aadfasdf", BigDecimal.ZERO);
    }


}

class BankAccount {
    private String accountId;
    private BigDecimal availableBalance;
    private String currency;
    //...
}

class Customer {
    private String customerId;
    private BigDecimal remainingAmount;
    //...
}

class CreditLine {
    private String creditLineId;
    private BigDecimal availableCreditLimit;
    //...
}

class FleetCardAccount {
    private String fleetCardId;
    private BigDecimal monthlySpendingLimit;
    //...
}

class CustomerSegmentReport {
    private String customerSegment;
    private BigDecimal monthlyRevenue;
    //..
}

@interface Transactional {

}

interface BalanceRepository {
    BigDecimal get(String userId);
    void update(String userId, BigDecimal value);
}

interface EventPublisher {

    void publish(PublishedEvent event);

}

interface PublishedEvent {

}

class BalanceToppedUp implements PublishedEvent {

    public BalanceToppedUp(String userId, BigDecimal amount, BigDecimal oldBalance, BigDecimal newBalance) {

    }
}

@Transactional
class BalanceService {

    BalanceRepository balanceRepository;
    EventPublisher events;

    void applyTopUp(String userId, BigDecimal amount) {
        BigDecimal oldBalance = balanceRepository.get(userId);
        BigDecimal newBalance = oldBalance.add(amount);
        balanceRepository.update(userId, newBalance);
        //outbox
        events.publish(new BalanceToppedUp(userId, amount, oldBalance, newBalance));
    }
}

