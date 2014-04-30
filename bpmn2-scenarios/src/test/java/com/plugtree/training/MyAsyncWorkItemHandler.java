package com.plugtree.training;

import java.util.LinkedList;
import java.util.Queue;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class MyAsyncWorkItemHandler implements WorkItemHandler {

	private final Queue<WorkItem> workItems = new LinkedList<WorkItem>();
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		workItems.add(workItem);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		workItems.remove(workItem);
	}

	public int getWorkItemsSize() {
		return workItems.size();
	}
	
	public WorkItem popWorkItem() {
		return workItems.poll();
	}
}
