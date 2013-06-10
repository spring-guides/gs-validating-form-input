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
