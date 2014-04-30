package com.plugtree.training;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.io.ResourceFactory;

public class ParallelMultipleInstanceProcessTest {

    private KieSession ksession;
    
    @Before
    public void setup() throws IOException{
        this.ksession = this.createKieSession();
        //Console log. Try to analyze it first
        KieServices.Factory.get().getLoggers().newConsoleLogger((KieRuntimeEventManager) ksession);
        
        
    }

    @Test
    public void parallelMultipleNodeInstanceProcessTest(){
        
    	MyAsyncWorkItemHandler handlerA = new MyAsyncWorkItemHandler();
    	MyAsyncWorkItemHandler handlerB = new MyAsyncWorkItemHandler();
    	MyAsyncWorkItemHandler handlerC = new MyAsyncWorkItemHandler();
    	
    	ksession.getWorkItemManager().registerWorkItemHandler("A", handlerA);
    	ksession.getWorkItemManager().registerWorkItemHandler("B", handlerB);
    	ksession.getWorkItemManager().registerWorkItemHandler("C", handlerC);
        List<String> testList = new ArrayList<String>();
        testList.add("a");
        testList.add("b");
        testList.add("c");
        testList.add("d");
        testList.add("e");
        testList.add("f");
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("testList", testList);
        
        //Start the process using its id
        ProcessInstance process = ksession.startProcess("async-examples.multiinst",parameters);
        
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
        
        Assert.assertEquals(1, handlerA.getWorkItemsSize());
        WorkItem item1 = handlerA.popWorkItem();
        List<?> inList1 = (List<?>) item1.getParameter("inList");
        Assert.assertEquals(testList, inList1);
        
        item1.getResults().put("outList", inList1);
        ksession.getWorkItemManager().completeWorkItem(item1.getId(), item1.getResults());
        
        Assert.assertEquals(6, handlerB.getWorkItemsSize());
        WorkItem itemA = handlerB.popWorkItem();
        ksession.getWorkItemManager().completeWorkItem(itemA.getId(), itemA.getResults());
        Assert.assertEquals(0, handlerC.getWorkItemsSize());
        WorkItem itemB = handlerB.popWorkItem();
        ksession.getWorkItemManager().completeWorkItem(itemB.getId(), itemB.getResults());
        Assert.assertEquals(0, handlerC.getWorkItemsSize());
        WorkItem itemC = handlerB.popWorkItem();
        ksession.getWorkItemManager().completeWorkItem(itemC.getId(), itemC.getResults());
        Assert.assertEquals(0, handlerC.getWorkItemsSize());
        WorkItem itemD = handlerB.popWorkItem();
        ksession.getWorkItemManager().completeWorkItem(itemD.getId(), itemD.getResults());
        Assert.assertEquals(0, handlerC.getWorkItemsSize());
        WorkItem itemE = handlerB.popWorkItem();
        ksession.getWorkItemManager().completeWorkItem(itemE.getId(), itemE.getResults());
        Assert.assertEquals(0, handlerC.getWorkItemsSize());
        WorkItem itemF = handlerB.popWorkItem();
        ksession.getWorkItemManager().completeWorkItem(itemF.getId(), itemF.getResults());
        Assert.assertEquals(1, handlerC.getWorkItemsSize());
        
        WorkItem last = handlerC.popWorkItem();
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
        ksession.getWorkItemManager().completeWorkItem(last.getId(), last.getResults());
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
    }
    
    
    /**
     * Creates a ksession from a kbase containing process definition
     * @return 
     */
    public KieSession createKieSession() {
    	KieServices ks = KieServices.Factory.get();
    	//Create file system
    	KieFileSystem kfs = ks.newKieFileSystem();
    	//Add simpleProcess.bpmn to kfs
    	kfs.write("src/main/resources/multiinst.bpmn2", ResourceFactory.newClassPathResource("multiinst.bpmn2"));
    	//Create builder for the file system
        KieBuilder kbuilder = ks.newKieBuilder(kfs);

        System.out.println("Compiling resources");
        kbuilder.buildAll();
        
        //Check for errors
        if (kbuilder.getResults().hasMessages(Message.Level.ERROR)) {
            System.out.println(kbuilder.getResults());
            throw new RuntimeException("Error building kbase!");
        }
        //Create a module for the jar and a container for its knowledge bases and sessions
        KieModule kmodule = kbuilder.getKieModule();
        KieContainer kcontainer = ks.newKieContainer(kmodule.getReleaseId());
        
        //Create a kie session from the kcontainer
        return kcontainer.newKieSession();
    }
}
