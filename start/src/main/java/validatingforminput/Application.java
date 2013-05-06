package validatingforminput;

import java.io.File;
import java.util.HashSet;

import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.SpringServletContainerInitializer;


public class Application {
	
	public static void main(String[] args) throws Exception {
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8080);
		tomcat.setBaseDir(".");
		tomcat.getHost().setAppBase("/");
		
		// Add AprLifecycleListener
		Server server = tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);
		
		Context context = tomcat.addWebapp("/", new File("src/main/webapp").getAbsolutePath());
		HashSet<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(WebAppInitializer.class);
		context.addServletContainerInitializer(new SpringServletContainerInitializer(), classes);
		tomcat.start();
		tomcat.getServer().await();
	}
	
}
