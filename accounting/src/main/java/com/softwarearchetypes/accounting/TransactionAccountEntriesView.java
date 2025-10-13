package com.softwarearchetypes.accounting;

import java.util.List;

public record TransactionAccountEntriesView(AccountMetadataView account, List<EntryView> entries) {

}

