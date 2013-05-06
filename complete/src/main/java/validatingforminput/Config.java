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
