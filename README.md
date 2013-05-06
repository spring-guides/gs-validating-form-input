Getting Started: Validating Form Input
======================================

This Getting Started guide will walk you through the process of configuring your form to support validation. On top of that, we'll show to display the error message on the screen so the user can re-enter a valid input.

To help you get started, we've provided an initial project structure as well as the completed project for you in GitHub:

```sh
$ git clone git@github.com:springframework-meta/gs-validating-form-input.git
```

In the `start` folder, you'll find a bare project, ready for you to copy-n-paste code snippets from this document. In the `complete` folder, you'll find the complete project code.

Before we can write a form validator, let's setup the key parts of a web application. Or, you can skip straight to the [fun part](#defining-an-object-that-needs-validation).

Selecting Dependencies
----------------------
The sample in this Getting Started Guide will leverage Spring MVC, Hibernate's JSR-303 Validator, and Tomcat's embedded servlet container.

- org.springframework:spring-webmvc:3.2.2.RELEASE
- javax.validation:validation-api:1.0.0.GA
- org.apache.tomcat:tomcat-catalina:7.0.39
- org.apache.tomcat.embed:tomcat-embed-core:7.0.39
- org.apache.tomcat:tomcat-jasper:7.0.39
- org.hibernate:hibernate-validator:5.0.0.Final
- org.thymeleaf:thymeleaf-spring3:2.0.16
- org.slf4j:slf4j-log4j12:1.7.5

Refer to the [Gradle Getting Started Guide]() or the [Maven Getting Started Guide]() for details on how to include these dependencies in your build.

Setting up an embedded Tomcat server
------------------------------------
To serve up forms that can be validated, we need a server process. While we could use a production version of tc Server, it's a lot easier to use embedded Tomcat for initial development.

```java
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
```

Our server is setup to listen on `localhost:8080/`. It will look in `src/main/webapp` for any template files.

If you'll notice, our servlet container is initialized with a `WebAppInitializer`.

Creating an application initializer
-----------------------------------
`WebAppInitializer` is the source of our configuration.

```java
package validatingforminput;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return null;
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { Config.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}
	
	

}
```

This supports servlet 3.0 standard configuration by extending the `AbstractAnnotationConfigDispatcherServletInitializer`. It references `Config` as the source for various Spring beans needed for our app.

Configuring our application
---------------------------
For this application, we are going to use the templating language of [Thymeleaf[(http://www.thymeleaf.org/doc/html/Thymeleaf-Spring3.html). Basically, we need more than raw HTML to serve our needs.

```java
package validatingforminput;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@Configuration
@EnableWebMvc
@ComponentScan
public class Config {
	
	@Bean
	ServletContextTemplateResolver templateResolver() {
		ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML5");
		return resolver;
	}
	
	@Bean
	SpringTemplateEngine engine() {
		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(templateResolver());
		return engine;
	}
	
	@Bean
	ThymeleafViewResolver viewResolver() {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine(engine());
		return viewResolver;
	}

}
```

To activate Spring MVC, we need `@EnableWebMvc` added to the class. We also include `@ComponentScan` to find the annotated `@Controller` classes and their methods.

The beans we have in this configuration are used to wire up Thymeleaf and integrate it with Spring MVC. The first one take view names, appends `.html`, and looks for that file in `src/main/webapp/`.  The rest are used to perform proper resolution.

Finally, let's include some logging messages to make it easier to see what is going on with **log4j.properties**.

```txt
# Set root logger level to DEBUG and its only appender to A1.
log4j.rootLogger=WARN, A1

# A1 is set to be a ConsoleAppender.
log4j.appender.A1=org.apache.log4j.ConsoleAppender

# A1 uses PatternLayout.
log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

log4j.category.org.springframework=INFO
log4j.category.org.hibernate.validator=DEBUG
```

Defining an object that needs validation
----------------------------------------
Before we build a form and web pages, let's define the core object we want to handle and some validation criteria.

```java
package validatingforminput;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Person {
	@NotNull
	@Min(18)
	private Integer age;

	public String toString() {
		return "Person(" + age + ")";
	}
	
	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
}
```

Here we have a `Person` object with one field: `age`. It is marked up with JSR-303 `@NotNull` and `@Min` annotations. These aren't the only ones, but they should convey the idea. To support operations, we have the standard getters and setters as well as a convenient `toString()` method.

Creating a web controller
-------------------------
Now that we have defined our entity, it's time to create a simple web controller.

```java
package validatingforminput;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

	@RequestMapping(value="/", method=RequestMethod.GET)
	public String showForm(Person person) {
		return "form";
	}
	
	@RequestMapping(value="/", method=RequestMethod.POST)
	public String enterAge(@Valid Person person, BindingResult bindingResult, 
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", bindingResult.getFieldError().getDefaultMessage());
			return "redirect:/";
		}
		return "results";
	}

}
```

This controller has a GET and a POST method, both mapped to `/`. 

The `showForm` method returns the `form` template. It includes a `Person` in it's method signature so the template can associate form attributes with the `Person` class.

The `enterAge` method accepts three arguments:
- a `person` object marked up with `@Valid` to gather the attributes filled out in the form we're about to build
- a `bindingResult` object so we can test for and retrieve validation errors 
- a `redirectAttributes` object so we can create a flash-scoped error message to show the user what went wrong in the event of an error.
That way, we can retrieve all the attributes from the form bound to the `Person` object. In the code, we test if there were any errors, and if so, add a flash attribute named `error`, and redirect the user back to the `/` page. If there are no errors, then we return the `results` template.

Building an HTML front end
--------------------------
First of all, we need to build the "main" page.

```html
<html>
	<body>
		<form action="#" th:action="@{/}" th:object="${person}" method="post">
			<table>
				<tr>
					<td>How old are you?</td>
					<td><input type="text" th:field="*{age}" /></td>
					<td><div id="errors" th:text="${error}" /></td>
				</tr>
				<tr>
					<td><button type="submit">Submit</button></td>
				</tr>
			</table>
		</form>
	</body>
</html>
```

It contains a simple form with each field in a separate slot of a table. The form is geared to post towards `/enterAge`. It is marked as being backed up by the `person` object that we saw in the GET method in the web controller. This is known as a **bean-backed form**. There is only one field in our `Person` bean, and we can see it tagged with `th:field="*{age}"`.

Right next to that entry field is a `<div>` with `th:text="${error}"`. This gives us a place to insert an error message.

Finally, we have a button to submit. In general, if the user enteres an age that violates the `@Valid` constraints, it will bounce back to this page with the error message on display. If a valid age is entered, then the user is routed to the next web page.

```html
<html>
	<body>
		Congratulations! You are old enough to sign up for this site.
	</body>
</html>
```
> These web pages don't have any sophisticated CSS JavaScript, because we are trying to keep things simple. But for any professional web sites, it's very valuable to learn how to style your web pages.

Running the application
-----------------------
Now that we have everything setup, let's run the application.

```sh
$ ./gradlew run
```

We should expect to see the following on the screen:

```sh
May 06, 2013 5:02:44 PM org.apache.catalina.core.AprLifecycleListener init
INFO: The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: /Users/gturnquist/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.
May 06, 2013 5:02:44 PM org.apache.coyote.AbstractProtocol init
INFO: Initializing ProtocolHandler ["http-bio-8080"]
May 06, 2013 5:02:44 PM org.apache.catalina.core.StandardService startInternal
INFO: Starting service Tomcat
May 06, 2013 5:02:44 PM org.apache.catalina.core.StandardEngine startInternal
INFO: Starting Servlet Engine: Apache Tomcat/7.0.39
May 06, 2013 5:02:44 PM org.apache.catalina.startup.ContextConfig getDefaultWebXmlFragment
INFO: No global web.xml found
May 06, 2013 5:02:46 PM org.apache.catalina.core.ApplicationContext log
INFO: Spring WebApplicationInitializers detected on classpath: [validatingforminput.WebAppInitializer@5a71cd96]
May 06, 2013 5:02:46 PM org.apache.catalina.core.ApplicationContext log
INFO: No Spring WebApplicationInitializer types detected on classpath
May 06, 2013 5:02:46 PM org.apache.catalina.core.ApplicationContext log
INFO: Initializing Spring FrameworkServlet 'dispatcher'
0    [localhost-startStop-1] INFO  org.springframework.web.servlet.DispatcherServlet  - FrameworkServlet 'dispatcher': initialization started
6    [localhost-startStop-1] INFO  org.springframework.web.context.support.AnnotationConfigWebApplicationContext  - Refreshing WebApplicationContext for namespace 'dispatcher-servlet': startup date [Mon May 06 17:02:46 CDT 2013]; root of context hierarchy
69   [localhost-startStop-1] INFO  org.springframework.web.context.support.AnnotationConfigWebApplicationContext  - Registering annotated classes: [class validatingforminput.Config]
554  [localhost-startStop-1] INFO  org.springframework.beans.factory.support.DefaultListableBeanFactory  - Pre-instantiating singletons in org.springframework.beans.factory.support.DefaultListableBeanFactory@24cd41c4: defining beans [org.springframework.context.annotation.internalConfigurationAnnotationProcessor,org.springframework.context.annotation.internalAutowiredAnnotationProcessor,org.springframework.context.annotation.internalRequiredAnnotationProcessor,org.springframework.context.annotation.internalCommonAnnotationProcessor,config,org.springframework.context.annotation.ConfigurationClassPostProcessor.importAwareProcessor,webController,org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration,requestMappingHandlerMapping,mvcContentNegotiationManager,viewControllerHandlerMapping,beanNameHandlerMapping,resourceHandlerMapping,defaultServletHandlerMapping,requestMappingHandlerAdapter,mvcConversionService,mvcValidator,httpRequestHandlerAdapter,simpleControllerHandlerAdapter,handlerExceptionResolver,templateResolver,viewResolver,engine]; root of factory hierarchy
732  [localhost-startStop-1] INFO  org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping  - Mapped "{[/],methods=[GET],params=[],headers=[],consumes=[],produces=[],custom=[]}" onto public java.lang.String validatingforminput.WebController.showForm(validatingforminput.Person)
732  [localhost-startStop-1] INFO  org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping  - Mapped "{[/],methods=[POST],params=[],headers=[],consumes=[],produces=[],custom=[]}" onto public java.lang.String validatingforminput.WebController.enterAge(validatingforminput.Person,org.springframework.validation.BindingResult,org.springframework.web.servlet.mvc.support.RedirectAttributes)
1018 [localhost-startStop-1] INFO  org.hibernate.validator.internal.util.Version  - HV000001: Hibernate Validator 5.0.0.Final
1034 [localhost-startStop-1] DEBUG org.hibernate.validator.internal.engine.resolver.DefaultTraversableResolver  - Cannot find javax.persistence.Persistence on classpath. Assuming non JPA 2 environment. All properties will per default be traversable.
1036 [localhost-startStop-1] DEBUG org.hibernate.validator.internal.engine.ConfigurationImpl  - Setting custom MessageInterpolator of type org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator
1037 [localhost-startStop-1] DEBUG org.hibernate.validator.internal.engine.ConfigurationImpl  - Setting custom ConstraintValidatorFactory of type org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory
1041 [localhost-startStop-1] DEBUG org.hibernate.validator.internal.xml.ValidationXmlParser  - Trying to load META-INF/validation.xml for XML based Validator configuration.
1047 [localhost-startStop-1] DEBUG org.hibernate.validator.internal.xml.ValidationXmlParser  - No META-INF/validation.xml found. Using annotation based configuration only.
1407 [localhost-startStop-1] INFO  org.springframework.web.servlet.DispatcherServlet  - FrameworkServlet 'dispatcher': initialization completed in 1406 ms
May 06, 2013 5:02:47 PM org.apache.coyote.AbstractProtocol start
INFO: Starting ProtocolHandler ["http-bio-8080"]
50881 [http-bio-8080-exec-2] WARN  org.springframework.web.servlet.PageNotFound  - No mapping found for HTTP request with URI [/favicon.ico] in DispatcherServlet with name 'dispatcher'
```

If we visit <http://localhost:8080/>, we should see something like this:

![](images/valid-01.png)

What happens if we enter **15** and click on **Submit**?

![](images/valid-02.png)

![](images/valid-03.png)

Here we can see that because it violated the constraints in the `Person` class, we get bounced back to the "main" page. If we click on submit with nothing in the entry box, we get a different error.

![](images/valid-04.png)

Finally, if we enter a valid age, then we end up on the `results` page!

![](images/valid-05.png)

Congratulations! You have coded a simple web application with validation built into a domain object. This way we can ensure the data meets certain criteria and leverage the user to input it correctly.

Next Steps
----------
