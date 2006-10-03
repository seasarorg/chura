package ${clazz.packageName};

public abstract class ${clazz.name} {

<#list fields as field>
	private ${field.declaringClassName} ${field.name};

</#list>
	public ${clazz.name}() {
	}

<#list fields as field>
	public ${field.declaringClassName} get${field.name?cap_first}() {
		return this.${field.name};
	}

	public void set${field.name?cap_first}(${field.declaringClassName} ${field.name?lower_case}) {
		this.${field.name} = ${field.name?lower_case};
	}
</#list>
}