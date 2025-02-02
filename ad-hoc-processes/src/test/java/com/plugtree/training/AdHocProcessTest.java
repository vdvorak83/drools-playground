package com.plugtree.training;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.jbpm.process.instance.event.listeners.RuleAwareProcessEventLister;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

public class AdHocProcessTest {

	@Test
	public void testAdHocProcess() {
		KieServices ks = KieServices.Factory.get();
		KieContainer kcontainer = ks.getKieClasspathContainer();
		Properties props = new Properties();
		props.setProperty("drools.workItemManagerFactory", CustomWIMFactory.class.getName());
		KieSessionConfiguration ksconf = ks.newKieSessionConfiguration(props);
		final KieSession ksession = kcontainer.newKieSession(ksconf);
		TestAsyncWorkItemHandler htHandler = new TestAsyncWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", htHandler);
		ksession.addEventListener(new RuleAwareProcessEventLister());
		ksession.addEventListener(new DefaultAgendaEventListener() {
			@Override
			public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
				ksession.fireAllRules();
			}
			@Override
			public void matchCreated(MatchCreatedEvent event) {
				ksession.fireAllRules();
			}
		});
		ProcessInstance processInstance = ksession.startProcess("com.plugtree.training.adhocProcess");
		
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
		
		WorkItem item = htHandler.getLastItem();

		Assert.assertEquals("decide what's next", item.getParameter("TaskName"));
		
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("outVar1", "SOME ACTUAL VALUE");
		ksession.getWorkItemManager().completeWorkItem(item.getId(), results);
		
		WorkItem item2 = htHandler.getLastItem();
		Assert.assertEquals("execute action", item2.getParameter("TaskName"));

		results.clear();
		results.put("outVar2", Integer.valueOf(22));
		ksession.getWorkItemManager().completeWorkItem(item2.getId(), results);
		
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
	}
}
