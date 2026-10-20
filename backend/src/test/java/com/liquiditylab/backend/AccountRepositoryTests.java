
package com.liquiditylab.backend;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AccountRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void cleanUpTestAccount() {
        accountRepository.findAll().stream()
                .filter(account ->
                        account.getAccountNumber().equals("LL-TEST-001"))
                .forEach(account ->
                        accountRepository.deleteById(account.getId()));
    }

    @Test
    void shouldSaveAndRetrieveAccount() {
        Account account = new Account(
                "LL-TEST-001",
                "LiquidityLab Test Account",
                "INR"
        );

        Account savedAccount = accountRepository.save(account);

        assertTrue(savedAccount.getId() != null);

        Account retrievedAccount = accountRepository
                .findById(savedAccount.getId())
                .orElseThrow();

        assertEquals("LL-TEST-001", retrievedAccount.getAccountNumber());
        assertEquals("LiquidityLab Test Account", retrievedAccount.getAccountName());
        assertEquals("INR", retrievedAccount.getCurrency());
    }
}
