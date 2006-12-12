<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../../css/global.css"/>
</head>
<body>
<form id="${configs.table_capitalize}ConfirmForm"><input type="hidden" id="crudType" />
<div>
<span id="messages"></span>
</div>
<table class="tablebg">
<#list mappings as mapping>
<tr>
    <td><label id="${mapping.javaFieldName}Label">${mapping.javaFieldName}</label></td>
	<td><span id="${mapping.javaFieldName}">${mapping.javaFieldName}</span><input type="hidden" id="${mapping.javaFieldName}-hidden" /></td>
	<td><span id="${mapping.javaFieldName}Message"></span></td>
</tr>
</#list>
</table>
<div id="isComeFromList">
	<input type="button" id="jump${configs.table_capitalize}List" value="Previous" 
		onclick="location.href='${configs.table_capitalize}List.html'"
	/>
</div>
<div id="isNotComeFromList" style="display: none;">
	<input type="button" id="jump${configs.table_capitalize}Edit" value="Previous"
		onclick="location.href='${configs.table_capitalize}Edit.html'"
	/>
</div>
<div id="isNotRead">
<input type="button" id="doFinish" value="Finish" onclick="location.href='${configs.table_capitalize}List.html'" />
</div>
</form>
</body></html>