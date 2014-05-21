package testeando;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import jodd.io.FileUtil;
import jodd.util.ClassLoaderUtil;

import org.drools.compiler.compiler.PackageBuilder;
import org.drools.compiler.compiler.PackageBuilderConfiguration;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.RuleBase;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.RuleBaseFactory;
import org.drools.core.StatefulSession;

import fluent.Persona;

public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//new Main().flui();
		new Main().flui2("./bin/");
		

	}

	private void flui1() {
		
		Persona persona1 = new Persona();
		persona1.setNombre("Diego");
		Persona persona2 = new Persona();
		persona2.setNombre("Rocio");
		PackageDescr packageDescr = DescrFactory
			.newPackage()
			.name("facts")
			.newRule().name("Xyz")
			.lhs().
			pattern(Persona.class.getName()).
			id("$foo", false).
			constraint("nombre == 'Rocio'").end()
			.end()
			.rhs( "System.out.println($foo.getNombre());" ).end().getDescr();
						
		PackageBuilder builder = new PackageBuilder();
		builder.addPackage(packageDescr);
		org.drools.core.rule.Package pkg = builder.getPackage();
				
		RuleBase ruleBase  = RuleBaseFactory.newRuleBase();
		ruleBase.addPackage(pkg);
		StatefulSession ksession = ruleBase.newStatefulSession();
		ksession.insert(persona1);
		ksession.insert(persona2);
			
		int count = ksession.fireAllRules();
		System.out.println(count);

	}

	private void flui2(String path) throws Exception {
		ClassLoader newClassLoader = crearClaseYCompilarla(path);
		Object o = newClassLoader.loadClass("com.test.Hello").newInstance();
		PackageDescr packageDescr = DescrFactory
			.newPackage()
			.name("facts")
			.newRule().name("Xyz")
			.lhs().pattern("com.test.Hello").
			id("$foo", false).
			constraint("nombre == 'Rocio'").
			constraint("$nombre: nombre").end().end()
			.rhs( "System.out.println($nombre);" ).end().getDescr();
						
		PackageBuilderConfiguration pkconf = 
			new PackageBuilderConfiguration(newClassLoader,
				getClass().getClassLoader());
		PackageBuilder builder = new PackageBuilder(pkconf);
		builder.addPackage(packageDescr);
		org.drools.core.rule.Package pkg = builder.getPackage();
		
		RuleBaseConfiguration rbconf = new RuleBaseConfiguration();
		rbconf.setClassLoader(newClassLoader, getClass().getClassLoader());
		
		RuleBase ruleBase  = RuleBaseFactory.newRuleBase(rbconf);
		ruleBase.addPackage(pkg);
		StatefulSession ksession = ruleBase.newStatefulSession();
		
		ksession.insert(o);
		
		int count = ksession.fireAllRules();
		System.out.println(count);
		
		//Me fijo como quedo el objeto por las dudas
		Class clase = o.getClass();
		Method metodo = clase.getDeclaredMethod("getNombre", null);
		System.out.println(metodo.invoke(o, null));
		
		
		
		
			
	

		
	}
	
	private ClassLoader crearClaseYCompilarla(String path) throws IOException {
		File sourceFile = new File("com/test/Hello.java");
		sourceFile.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(sourceFile);

		writer.write("package com.test; public class Hello{ private String nombre =\"Rocio\"; public String getNombre() {  System.out.println(\"Testeando metodo\"); return nombre ; } public void setNombre(String s)  { nombre =s;}}");
		writer.close();

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);
		fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
				Arrays.asList(new File(path)));
		// Compile the file
		compiler.getTask(
				null,
				fileManager,
				null,
				null,
				null,
				fileManager.getJavaFileObjectsFromFiles(Arrays
						.asList(sourceFile))).call();
		fileManager.close();

		String addonClasspath = path + "com/test/Hello.class";
		String classPath = addonClasspath;
		byte[] classBytes = FileUtil.readBytes(classPath);

		Class c = ClassLoaderUtil.defineClass("com.test.Hello", classBytes);
		/*try {
			Object o = c.newInstance();
			// System.out.println(o);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return c.getClassLoader();
	}


}
