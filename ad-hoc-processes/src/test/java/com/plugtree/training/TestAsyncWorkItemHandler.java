package com.plugtree.training;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class TestAsyncWorkItemHandler implements WorkItemHandler {

	private WorkItem item;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		this.item = workItem;
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		this.item = null;
	}
	
	public WorkItem getLastItem() {
		WorkItem retval = item;
		item = null;
		return retval;
	}

}
