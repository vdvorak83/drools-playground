package com.plugtree.training;

import java.util.concurrent.TimeUnit;

import org.drools.core.ClockType;
import org.drools.core.audit.WorkingMemoryFileLogger;
import org.drools.core.event.AgendaEventListener;
import org.drools.core.event.WorkingMemoryEventListener;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.time.SessionPseudoClock;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.io.ResourceFactory;

import com.plugtree.training.model.Order;

public class CEPTest {

	@Test
	public void testCEP() {
		KieServices ks = KieServices.Factory.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write(ResourceFactory.newClassPathResource("com/plugtree/training/cep.drl"));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		if (kbuilder.getResults().hasMessages(Level.ERROR)) {
			throw new IllegalArgumentException(kbuilder.getResults().toString());
		}
		ReleaseId relId = kbuilder.getKieModule().getReleaseId();
		KieContainer kcontainer = ks.newKieContainer(relId);
		KieSessionConfiguration ksconf = ks.newKieSessionConfiguration();
		KieBaseConfiguration kbconf = ks.newKieBaseConfiguration();
		kbconf.setOption(EventProcessingOption.STREAM);
		ksconf.setOption(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
		KieBase kbase = kcontainer.newKieBase(kbconf);
		
		KieSession ksession = kbase.newKieSession(ksconf, null);

		SessionPseudoClock clock = ksession.getSessionClock();
		
		for (int index = 0; index < 99; index++) {
			ksession.getEntryPoint("orders").insert(new Order());
		}
		
		clock.advanceTime(1, TimeUnit.MINUTES);
		clock.advanceTime(59, TimeUnit.SECONDS);
		
		ksession.getEntryPoint("orders").insert(new Order());
		
		int rulesFired = ksession.fireAllRules();
		
		Assert.assertEquals(1, rulesFired);
	}

	@Test
	public void testCEP3() throws Exception {
		KieServices ks = KieServices.Factory.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write(ResourceFactory.newClassPathResource("com/plugtree/training/cep.drl"));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		if (kbuilder.getResults().hasMessages(Level.ERROR)) {
			throw new IllegalArgumentException(kbuilder.getResults().toString());
		}
		ReleaseId relId = kbuilder.getKieModule().getReleaseId();
		KieContainer kcontainer = ks.newKieContainer(relId);
		KieBaseConfiguration kbconf = ks.newKieBaseConfiguration();
		kbconf.setOption(EventProcessingOption.STREAM);
		KieBase kbase = kcontainer.newKieBase(kbconf);
		
		KieSession ksession = kbase.newKieSession();
		WorkingMemoryFileLogger logger = new WorkingMemoryFileLogger();
		((StatefulKnowledgeSessionImpl) ksession).session.addEventListener((AgendaEventListener) logger);
		((StatefulKnowledgeSessionImpl) ksession).session.addEventListener((WorkingMemoryEventListener) logger);
		logger.setFileName("/opt/git/medellin/cep-log.txt");

		for (int index = 0; index < 10; index++) {
			Thread.sleep(100);
			ksession.getEntryPoint("evaluations").insert(new Order("x"));
		}
		
		ksession.fireUntilHalt();
		logger.writeToDisk();
	}
	
	
	@Test
	public void testCEP2() {
		KieServices ks = KieServices.Factory.get();
		KieSession ksession = ks.getKieClasspathContainer().newKieSession("cep");

		SessionPseudoClock clock = ksession.getSessionClock();
		
		ksession.insert(new Order());
		
		clock.advanceTime(4, TimeUnit.MINUTES);
		clock.advanceTime(59, TimeUnit.SECONDS);
		
		ksession.insert(new Order());
		
		int rulesFired = ksession.fireAllRules();
		
		Assert.assertEquals(0, rulesFired);
		
		clock.advanceTime(1, TimeUnit.SECONDS);
		
		ksession.insert(new Order());
		
		rulesFired = ksession.fireAllRules();
		
		Assert.assertEquals(1, rulesFired);
	}

}
