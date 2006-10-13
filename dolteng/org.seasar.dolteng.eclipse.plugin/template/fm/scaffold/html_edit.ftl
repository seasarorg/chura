<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../global.css"/>
</head>
<body>
<form id="${configs.table_capitalize}EditForm"><input type="hidden" id="crudType" />
<div>
<span id="messages"></span>
</div>
<table class="tablebg">
<#list mappings as mapping>
<tr>
    <td><label id="${mapping.javaFieldName}Label">${mapping.javaFieldName}</label></td>
	<td>
<#if mapping.isPrimaryKey() = true>
	<div id="isCreate">
		<input type="text" id="${mapping.javaFieldName}" />
	</div>
	<div id="isNotCreate" style="display: none;">
		<span id="${mapping.javaFieldName}-out">${mapping.javaFieldName}</span><input type="hidden" id="${mapping.javaFieldName}-hidden" />
	</div>
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