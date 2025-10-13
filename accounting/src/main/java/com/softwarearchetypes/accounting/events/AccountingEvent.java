package com.softwarearchetypes.accounting.events;

import com.softwarearchetypes.common.events.PublishedEvent;

sealed public interface AccountingEvent extends PublishedEvent permits CreditEntryRegistered, DebitEntryRegistered {

}
