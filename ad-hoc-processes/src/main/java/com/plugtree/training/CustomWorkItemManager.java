package com.plugtree.training;

import java.util.Map;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.kie.api.runtime.KieSession;

public class CustomWorkItemManager extends DefaultWorkItemManager {

	private InternalKnowledgeRuntime kruntime;

	public CustomWorkItemManager(InternalKnowledgeRuntime kruntime) {
		super(kruntime);
		this.kruntime = kruntime;
	}

	@Override
	public void completeWorkItem(long id, Map<String, Object> results) {
		if (results != null) {
			for (Map.Entry<String, Object> entry : results.entrySet()) {
				kruntime.insert(entry.getValue());
			}
		}
		super.completeWorkItem(id, results);
		((KieSession) kruntime).fireAllRules();
	}

}
