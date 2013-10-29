<#assign project_id="gs-validating-form-input">
This guide walks you through the process of configuring a web application form to support validation.

What you'll build
-----------------

You'll build a simple Spring MVC application that take user input and checks the input using standard validation annotations. You'll also see how to display the error message on the screen so the user can re-enter a valid input.


What you'll need
----------------

 - About 15 minutes
 - <@prereq_editor_jdk_buildtools/>


## <@how_to_complete_this_guide jump_ahead='Create a Person object'/>


<a name="scratch"></a>
Set up the project
------------------

<@build_system_intro/>

<@create_directory_structure_hello/>


<@create_both_builds/>

<@bootstrap_starter_pom_disclaimer/>


<a name="initial"></a>
Create a Person object
------------------------
The application involves validating a user's age, so first you need to create a class to represent a person.

    <@snippet path="src/main/java/hello/Person.java" prefix="complete"/>
    
The `Person` class only has one attribute, `age`. It is flagged with standard validation annotations:
- `@NotNull` won't allow an empty value
- `@Min(18)` won't allow if the age is less than 18

In addition to that, you can also see getters/setters for `age` as well as a convenient `toString()` method.


Create a web controller
-------------------------
Now that you have defined an entity, it's time to create a simple web controller.

    <@snippet path="src/main/java/hello/WebController.java" prefix="complete"/>

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

    <@snippet path="src/main/webapp/form.html" prefix="complete"/>
  
The page contains a simple form with each field in a separate slot of a table. The form is geared to post towards `/enterAge`. It is marked as being backed up by the `person` object that you saw in the GET method in the web controller. This is known as a **bean-backed form**. There is only one field in the `Person` bean, and you can see it tagged with <#noparse>`th:field="*{age}"`</#noparse>.

Right next to that entry field is a `<div>` with <#noparse>`th:text="${error}"`</#noparse>. This gives you a place to insert an error message.

Finally, you have a button to submit. In general, if the user enters an age that violates the `@Valid` constraints, it will bounce back to this page with the error message on display. If a valid age is entered, the user is routed to the next web page.

    <@snippet path="src/main/webapp/results.html" prefix="complete"/>
    
> **Note:** In this simple example, these web pages don't have any sophisticated CSS JavaScript. But for any professional web sites, it's very valuable to learn how to style your web pages.


Create an Application class
---------------------------
For this application, you are using the template language of [Thymeleaf](http://www.thymeleaf.org/doc/html/Thymeleaf-Spring3.html). This application needs more than raw HTML.

    <@snippet path="src/main/java/hello/Application.java" prefix="complete"/>
    
To activate Spring MVC, you would normally add `@EnableWebMvc` to the `Application` class. But Spring Boot's `@EnableAutoConfiguration` already adds this annotation when it detects **spring-webmvc** on your classpath. The application also has `@ComponentScan` to find the annotated `@Controller` class and its methods.

The extra beans shown in this configuration are used to wire up Thymeleaf and integrate it with Spring MVC. The first one takes view names, appends `.html`, and looks for that file in `src/main/webapp/`.  The rest are used to perform proper resolution and rendering.

<@build_an_executable_jar_subhead/>

<@build_an_executable_jar_with_both/>


<@run_the_application_with_both module="web application"/>

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
