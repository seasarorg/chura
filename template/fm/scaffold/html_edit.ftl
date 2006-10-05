<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../global.css"/>
</head>
<body>
<form><input type="hidden" id="crudType">
<div>
<span id="messages"></span>
</div>
<table class="tablebg">
<#list mappings as mapping>
<tr>
    <td class="label">${mapping.javaFieldName}</td>
	<td>
<#if mapping.isPrimaryKey() = true>
	<span id="isCreate">
		<span id="${mapping.javaFieldName}">${mapping.javaFieldName}<input type="hidden" id="${mapping.javaFieldName}-hidden" /></span>
	</span>
	<span id="isNotCreate">
		<input type="text" id="${mapping.javaFieldName}-input" />
	</span>
<#else>
	<input type="text" id="${mapping.javaFieldName}" />
</#if>
	</td>
	<td><span id="${mapping.javaFieldName}Message"></span></td>
</tr>
</#list>
</table>
<input type="button" id="jump${configs.table_capitalize}List" value="Previous"
	onclick="location.href='${configs.table_capitalize}List.html'"/>
<input type="button" id="go${configs.table_capitalize}Confirm" value="Confirm"
	onclick="location.href='${configs.table_capitalize}Confirm.html'"/>
</form>
</body></html>