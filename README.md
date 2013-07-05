
Getting Started: Validating Form Input
======================================


What you'll build
-----------------

This Getting Started guide walks you through the process of configuring your form to support validation. On top of that, you'll see how to display the error message on the screen so the user can re-enter a valid input.


What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Maven 3.0][mvn] or later

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[mvn]: http://maven.apache.org/download.cgi


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/getting-started), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [git](/understanding/git):
`git clone https://github.com/springframework-meta/gs-validating-form-input.git`
 - cd into `gs-validating-form-input/initial`
 - Jump ahead to [Creating a Person object](#initial).

**When you're finished**, you can check your results against the code in `gs-validating-form-input/complete`.
[zip]: https://github.com/springframework-meta/gs-validating-form-input/archive/master.zip


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Maven](https://maven.apache.org) and [Gradle](http://gradle.org) is included here. If you're not familiar with either, refer to [Getting Started with Maven](../gs-maven/README.md) or [Getting Started with Gradle](../gs-gradle/README.md).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello

### Create a Maven POM

`pom.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.springframework</groupId>
    <artifactId>gs-validating-form-input-initial</artifactId>
    <version>0.1.0</version>

    <parent>
        <groupId>org.springframework.bootstrap</groupId>
        <artifactId>spring-bootstrap-starters</artifactId>
        <version>0.5.0.BUILD-SNAPSHOT</version>
    </parent>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.bootstrap</groupId>
            <artifactId>spring-bootstrap-web-starter</artifactId>
        </dependency>
        <dependency>
        	<groupId>org.hibernate</groupId>
        	<artifactId>hibernate-validator</artifactId>
        </dependency>
        <dependency>
        	<groupId>org.thymeleaf</groupId>
        	<artifactId>thymeleaf-spring3</artifactId>
        </dependency>
    </dependencies>
    
    <!-- TODO: remove once bootstrap goes GA -->
    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>http://repo.springsource.org/milestone</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
        <pluginRepository>
            <id>spring-snapshots</id>
            <name>Spring Snapshots</name>
            <url>http://repo.springsource.org/snapshot</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```

TODO: mention that we're using Spring Bootstrap's [_starter POMs_](../gs-bootstrap-starter) here.

Note to experienced Maven users who are unaccustomed to using an external parent project: you can take it out later, it's just there to reduce the amount of code you have to write to get started.


<a name="initial"></a>
Creating a Person object
------------------------
The application involves validating a user's age, so first we need to create a class to represent a person.

`src/main/java/hello/Person.java`
```java
package hello;

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
    
The `Person` class only has one attribute, `age`. It is flagged with standard validation annotations:
- `@NotNull` won't allow an empty value
- `@Min(18)` won't allow if the age is less than 18

In addition to that, you can also see getters/setters for `age` as well as a convenient `toString()` method.


Creating a web controller
-------------------------
Now that we have defined our entity, it's time to create a simple web controller.

`src/main/java/hello/WebController.java`
```java
package hello;

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

The `showForm` method returns the `form` template. It includes a `Person` in it's method signature so the template can associate form attributes with a `Person`.

The `enterAge` method accepts three arguments:
- a `person` object marked up with `@Valid` to gather the attributes filled out in the form we're about to build
- a `bindingResult` object so we can test for and retrieve validation errors 
- a `redirectAttributes` object so we can create a flash-scoped error message to show the user what went wrong in the event of an error.

That way, we can retrieve all the attributes from the form bound to the `Person` object. In the code, we test if there were any errors, and if so, add a flash attribute named `error`, and redirect the user back to the `/` page. If there are no errors, then we return the `results` template.


Building an HTML front end
--------------------------
First of all, we need to build the "main" page.

`src/main/webapp/form.html`
```html
<html>
	<body>
		<form action="#" th:action="@{/}" th:object="<#noparse>$</#noparse>{person}" method="post">
			<table>
				<tr>
					<td>How old are you?</td>
					<td><input type="text" th:field="*{age}" /></td>
					<td><div id="errors" th:text="<#noparse>$</#noparse>{error}" /></td>
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

Finally, we have a button to submit. In general, if the user enters an age that violates the `@Valid` constraints, it will bounce back to this page with the error message on display. If a valid age is entered, then the user is routed to the next web page.

`src/main/webapp/results.html`
```html
<html>
	<body>
		Congratulations! You are old enough to sign up for this site.
	</body>
</html>
```
    
> **Note:** These web pages don't have any sophisticated CSS JavaScript, because we are trying to keep things simple. But for any professional web sites, it's very valuable to learn how to style your web pages.


Configuring the application
---------------------------
For this application, you are using the templating language of [Thymeleaf](http://www.thymeleaf.org/doc/html/Thymeleaf-Spring3.html). This application needs more than raw HTML.

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.bootstrap.SpringApplication;
import org.springframework.bootstrap.context.annotation.EnableAutoConfiguration;
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
@EnableAutoConfiguration
public class Application {
	
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
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
	
}
```
    
To activate Spring MVC, you need `@EnableWebMvc` added to the class. It also has `@ComponentScan` to find the annotated `@Controller` class and its methods. To top it off, it uses `@EnableAutoConfiguration` to fire up Spring Bootstrap.

The extra beans shown in this configuration are used to wire up Thymeleaf and integrate it with Spring MVC. The first one takes view names, appends `.html`, and looks for that file in `src/main/webapp/`.  The rest are used to perform proper resolution and rendering.

### Build an executable JAR

Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Add the following configuration to your existing Maven POM:

`pom.xml`
```xml
    <properties>
        <start-class>hello.Application</start-class>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

The `start-class` property tells Maven to create a `META-INF/MANIFEST.MF` file with a `Main-Class: hello.Application` entry. This entry enables you to run the jar with `java -jar`.

The [Maven Shade plugin][maven-shade-plugin] extracts classes from all jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.

Now run the following to produce a single executable JAR file containing all necessary dependency classes and resources:

    mvn package

[maven-shade-plugin]: https://maven.apache.org/plugins/maven-shade-plugin


Run the web application
-----------------------

Run your service with `java -jar` at the command line:

```sh
java -jar target/gs-validating-form-input-complete-0.1.0.jar
```

The application should be up and running within a few seconds.

If you visit <http://localhost:8080/>, you should see something like this:

![](images/valid-01.png)

What happens if you enter **15** and click on **Submit**?

![](images/valid-02.png)

![](images/valid-03.png)

Here you can see that because it violated the constraints in the `Person` class, you get bounced back to the "main" page. If you click on submit with nothing in the entry box, you get a different error.

![](images/valid-04.png)

Finally, if you enter a valid age, then you end up on the `results` page!

![](images/valid-05.png)


Summary
-------

Congratulations! You have coded a simple web application with validation built into a domain object. This way you can ensure the data meets certain criteria and leverages the user to input it correctly.
