package com.softwarearchetypes.accounting.postingrules;

import java.util.List;

import com.softwarearchetypes.accounting.Transaction;

public interface PostingCalculator {

    List<Transaction> calculate(TargetAccounts accounts, PostingContext context);
}