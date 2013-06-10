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
