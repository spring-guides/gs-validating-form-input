This guide walks you through the process of configuring a web application form to support validation.

What you'll build
-----------------

You'll build a simple Spring MVC application that take user input and checks the input using standard validation annotations. You'll also see how to display the error message on the screen so the user can re-enter a valid input.


What you'll need
----------------

 - About 15 minutes
 - A favorite text editor or IDE
 - [JDK 6][jdk] or later
 - [Gradle 1.7+][gradle] or [Maven 3.0+][mvn]

[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gradle]: http://www.gradle.org/
[mvn]: http://maven.apache.org/download.cgi


How to complete this guide
--------------------------

Like all Spring's [Getting Started guides](/guides/gs), you can start from scratch and complete each step, or you can bypass basic setup steps that are already familiar to you. Either way, you end up with working code.

To **start from scratch**, move on to [Set up the project](#scratch).

To **skip the basics**, do the following:

 - [Download][zip] and unzip the source repository for this guide, or clone it using [Git][u-git]:
`git clone https://github.com/springframework-meta/gs-validating-form-input.git`
 - cd into `gs-validating-form-input/initial`.
 - Jump ahead to [Create a Person object](#initial).

**When you're finished**, you can check your results against the code in `gs-validating-form-input/complete`.
[zip]: https://github.com/springframework-meta/gs-validating-form-input/archive/master.zip
[u-git]: /understanding/Git


<a name="scratch"></a>
Set up the project
------------------

First you set up a basic build script. You can use any build system you like when building apps with Spring, but the code you need to work with [Gradle](http://gradle.org) and [Maven](https://maven.apache.org) is included here. If you're not familiar with either, refer to [Building Java Projects with Gradle](/guides/gs/gradle/) or [Building Java Projects with Maven](/guides/gs/maven).

### Create the directory structure

In a project directory of your choosing, create the following subdirectory structure; for example, with `mkdir -p src/main/java/hello` on *nix systems:

    └── src
        └── main
            └── java
                └── hello


### Create a Gradle build file
Below is the [initial Gradle build file](https://github.com/springframework-meta/gs-validating-form-input/blob/master/initial/build.gradle). But you can also use Maven. The pom.xml file is included [right here](https://github.com/springframework-meta/gs-validating-form-input/blob/master/initial/pom.xml).

`build.gradle`
```gradle
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
}

apply plugin: 'java'
apply plugin: 'eclipse'
apply plugin: 'idea'

jar {
    baseName = 'gs-validating-form-input'
    version =  '0.1.0'
}

repositories {
    mavenCentral()
    maven { url "http://repo.springsource.org/libs-snapshot" }
}

dependencies {
    compile("org.springframework.boot:spring-boot-starter-web:0.5.0.BUILD-SNAPSHOT")
    compile("org.hibernate:hibernate-validator:4.3.1.Final")
    compile("org.thymeleaf:thymeleaf-spring3:2.0.16")
    testCompile("junit:junit:4.11")
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.7'
}
```
    
    

This guide is using [Spring Boot's starter POMs](/guides/gs/spring-boot/).


<a name="initial"></a>
Create a Person object
------------------------
The application involves validating a user's age, so first you need to create a class to represent a person.

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


Create a web controller
-------------------------
Now that you have defined an entity, it's time to create a simple web controller.

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

The `showForm` method returns the `form` template. It includes a `Person` in its method signature so the template can associate form attributes with a `Person`.

The `enterAge` method accepts three arguments:
- A `person` object marked up with `@Valid` to gather the attributes filled out in the form you're about to build.
- A `bindingResult` object so you can test for and retrieve validation errors. 
- A `redirectAttributes` object so you can create a flash-scoped error message to show the user what went wrong in the event of an error.

You can retrieve all the attributes from the form bound to the `Person` object. In the code, you test for errors, and if so, add a flash attribute named `error`, and redirect the user back to the `/` page. If there are no errors, you return the `results` template.


Build an HTML front end
--------------------------
Now you build the "main" page.

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
  
The page contains a simple form with each field in a separate slot of a table. The form is geared to post towards `/enterAge`. It is marked as being backed up by the `person` object that you saw in the GET method in the web controller. This is known as a **bean-backed form**. There is only one field in the `Person` bean, and you can see it tagged with `th:field="*{age}"`.

Right next to that entry field is a `<div>` with `th:text="${error}"`. This gives you a place to insert an error message.

Finally, you have a button to submit. In general, if the user enters an age that violates the `@Valid` constraints, it will bounce back to this page with the error message on display. If a valid age is entered, the user is routed to the next web page.

`src/main/webapp/results.html`
```html
<html>
	<body>
		Congratulations! You are old enough to sign up for this site.
	</body>
</html>
```
    
> **Note:** In this simple example, these web pages don't have any sophisticated CSS JavaScript. But for any professional web sites, it's very valuable to learn how to style your web pages.


Create an Application class
---------------------------
For this application, you are using the template language of [Thymeleaf](http://www.thymeleaf.org/doc/html/Thymeleaf-Spring3.html). This application needs more than raw HTML.

`src/main/java/hello/Application.java`
```java
package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
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
    
To activate Spring MVC, you add `@EnableWebMvc` to the `Application` class. The application also has `@ComponentScan` to find the annotated `@Controller` class and its methods, as well as `@EnableAutoConfiguration` to fire up Spring Boot.

The extra beans shown in this configuration are used to wire up Thymeleaf and integrate it with Spring MVC. The first one takes view names, appends `.html`, and looks for that file in `src/main/webapp/`.  The rest are used to perform proper resolution and rendering.


Now that your `Application` class is ready, you simply instruct the build system to create a single, executable jar containing everything. This makes it easy to ship, version, and deploy the service as an application throughout the development lifecycle, across different environments, and so forth.

Below are the Gradle steps, but if you are using Maven, you can find the updated pom.xml [right here](https://github.com/springframework-meta/gs-validating-form-input/blob/master/complete/pom.xml) and build it by typing `mvn clean package`.

Update your Gradle `build.gradle` file's `buildscript` section, so that it looks like this:

```groovy
buildscript {
    repositories {
        maven { url "http://repo.springsource.org/libs-snapshot" }
        mavenLocal()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:0.5.0.BUILD-SNAPSHOT")
    }
}
```

Further down inside `build.gradle`, add the following to the list of applied plugins:

```groovy
apply plugin: 'spring-boot'
```
You can see the final version of build.gradle [right here]((https://github.com/springframework-meta/gs-validating-form-input/blob/master/complete/build.gradle).

The [Spring Boot gradle plugin][spring-boot-gradle-plugin] collects all the jars on the classpath and builds a single "über-jar", which makes it more convenient to execute and transport your service.
It also searches for the `public static void main()` method to flag as a runnable class.

Now run the following command to produce a single executable JAR file containing all necessary dependency classes and resources:

```sh
$ ./gradlew build
```

If you are using Gradle, you can run the JAR by typing:

```sh
$ java -jar build/libs/gs-validating-form-input-0.1.0.jar
```

If you are using Maven, you can the JAR by typing:

```sh
$ java -jar target/gs-validating-form-input-0.1.0.jar
```

[spring-boot-gradle-plugin]: https://github.com/SpringSource/spring-boot/tree/master/spring-boot-tools/spring-boot-gradle-plugin

> **Note:** The procedure above will create a runnable JAR. You can also opt to [build a classic WAR file](/guides/gs/convert-jar-to-war/) instead.


Run the web application
-------------------
If you are using Gradle, you can run your web application at the command line this way:

```sh
$ ./gradlew clean build && java -jar build/libs/gs-validating-form-input-0.1.0.jar
```

If you are using Maven, you can run your web application by typing:

```sh
$ mvn clean package && java -jar target/gs-validating-form-input-0.1.0.jar
```


The application should be up and running within a few seconds.

If you visit <http://localhost:8080/>, you should see something like this:

![](images/valid-01.png)

What happens if you enter **15** and click on **Submit**?

![](images/valid-02.png)

![](images/valid-03.png)

Here you can see that because it violated the constraints in the `Person` class, you get bounced back to the "main" page. If you click on Submit with nothing in the entry box, you get a different error.

![](images/valid-04.png)

If you enter a valid age, you end up on the `results` page!

![](images/valid-05.png)


Summary
-------

Congratulations! You have coded a simple web application with validation built into a domain object. This way you can ensure the data meets certain criteria and that the user inputs it correctly.
