package org.drools.examples.templates;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.drools.decisiontable.InputType;
import org.drools.template.DataProviderCompiler;
import org.drools.template.jdbc.ResultSetGenerator;
import org.drools.template.objects.ArrayDataProvider;
import org.h2.Driver;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

public class CheeseTemplateTest {

	@Test
	public void testRuleGenerationFromExcelFile() throws Exception {
		ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();
		URL excelFile = getClass().getResource("/cheese-data.csv");
		String drl = converter.compile(excelFile.openStream(), 
				getClass().getResourceAsStream("/cheese.drt"),
				InputType.CSV, 1, 1);
		fireRules(drl);
	}

	@Test
	public void testRuleGenerationFromArray() throws Exception {
		String[][] rows = new String[2][];
		rows[0] = new String[] {"42", "stilton", "Old man stilton"};
		rows[1] = new String[] {"21", "cheddar", "Young man cheddar"};
		ArrayDataProvider provider = new ArrayDataProvider(rows);
		DataProviderCompiler converter = new DataProviderCompiler();
		String drl = converter.compile(provider,
				getClass().getResourceAsStream("/cheese.drt"));
		fireRules(drl);
	}

	@Test
	public void testRuleGenerationFromDatabase() throws Exception {
		DriverManager.registerDriver(new Driver());
		Connection conn = DriverManager.getConnection("jdbc:h2:memdb", "sa", "sasa");
		conn.createStatement().execute("create table if not exists drools_datasource (" +
				"  age varchar(5), " +
				"  cheese_name varchar(15), " +
				"  log_msg varchar(40)" +
				");");
		conn.createStatement().execute("delete from drools_datasource");
		conn.createStatement().execute(
				"insert into drools_datasource(age, cheese_name, log_msg) " +
				"values ('42', 'stilton', 'Old man stilton');");
		conn.createStatement().execute(
				"insert into drools_datasource(age, cheese_name, log_msg) " +
				"values ('21', 'cheddar', 'Young man cheddar');");
		ResultSet resultSet = conn.createStatement().executeQuery(
				"select age, cheese_name, log_msg from drools_datasource");
		ResultSetGenerator converter = new ResultSetGenerator();
		String drl = converter.compile(resultSet,
				getClass().getResourceAsStream("/cheese.drt"));
		resultSet.close();
		conn.close();
		fireRules(drl);
	}
	
	private void fireRules(String drl) {
		//Validate that all rules work the same way, regardless of source
		Cheese cheese = new Cheese("stilton");
		Cheese cheddar = new Cheese("cheddar");
		Person young = new Person(21);
		Person old = new Person(42);
		
		KieServices ks = KieServices.Factory.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write("src/main/resources/rule.drl", 
				ResourceFactory.newByteArrayResource(drl.getBytes()));
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		KieContainer kc = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
		KieSession ksession = kc.newKieSession();
		List<String> result = new ArrayList<String>();
		ksession.setGlobal("list", result);
		ksession.insert(cheese);
		ksession.insert(cheddar);
		ksession.insert(young);
		ksession.insert(old);
		Assert.assertEquals(2, ksession.fireAllRules());
	}
}
