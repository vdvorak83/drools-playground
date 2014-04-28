package com.plugtree.training;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.WorkItemManagerFactory;

public class CustomWIMFactory implements WorkItemManagerFactory {

	@Override
	public WorkItemManager createWorkItemManager(InternalKnowledgeRuntime kruntime) {
		return new CustomWorkItemManager(kruntime);
	}

}
