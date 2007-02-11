package ${configs.rootpackagename}.${configs.entitypackagename} {
	
	[Bindable]
	[RemoteClass(alias="${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize}")]
	public class ${configs.table_capitalize} {

<#list mappings as mapping>
		public var ${mapping.javaFieldName}: ${toAsType(mapping)};
</#list>
	}
}