package com.plugtree.training;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.junit.Test;

import com.plugtree.training.model.Location;

public class BackwardChainingTest {

	private List<String> firedRules = new ArrayList<String>();
	
	@Test
	public void testBackwardChaining() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("rules/queries.drl"), ResourceType.DRL);
		if (kbuilder.hasErrors()) {
			for (KnowledgeBuilderError error : kbuilder.getErrors()) {
				System.out.println(error);
			}
			throw new IllegalArgumentException("Couldn't parse knowledge");
		}
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		
		//We add an AgendaEventListener to keep track of fired rules.
        ksession.addEventListener(new DefaultAgendaEventListener(){
        	@Override
        	public void afterActivationFired(AfterActivationFiredEvent event) {
                firedRules.add(event.getActivation().getRule().getName());
            }
        });
		
		ksession.insert(new Location("House", "Office"));
		ksession.insert(new Location("House", "Kitchen"));
		ksession.insert(new Location("Kitchen", "Knife"));
		ksession.insert(new Location("Kitchen", "Cheese"));
		ksession.insert(new Location("Office", "Desk"));
		ksession.insert(new Location("Office", "Chair"));
		ksession.insert(new Location("Desk", "Computer"));
		ksession.insert(new Location("Desk", "Pencil"));
		
		QueryResults results1 = ksession.getQueryResults("itContains", 
				new Object[] {"House", "Pencil"});
		Assert.assertTrue(results1.size() == 1);
		
		QueryResults results2 = ksession.getQueryResults("itContains", 
				new Object[] {"Desk", "Cheese"});
		Assert.assertTrue(results2.size() == 0);
		
		ksession.insert("go");
		ksession.fireAllRules();

		Assert.assertEquals(1,firedRules.size());
		Assert.assertTrue(firedRules.contains("go"));

		firedRules.clear();

		ksession.insert("go1");
		ksession.fireAllRules();

		Assert.assertEquals(2,firedRules.size());
		Assert.assertTrue(firedRules.contains("go"));
		Assert.assertTrue(firedRules.contains("go1"));
	}
}
