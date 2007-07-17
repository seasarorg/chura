<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja" lang="ja">
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
<#if mapping.isPrimaryKey() = true || isVersionColumn(mapping) = true>
    <td><div id="isNotCreate-${mapping.javaFieldName}Label"><label id="${mapping.javaFieldName}Label">${mapping.javaFieldName}</label></div></td>
	<td><div id="isNotCreate-${mapping.javaFieldName}Hidden">
			<span id="${mapping.javaFieldName}">${mapping.javaFieldName}</span><input type="hidden" id="${mapping.javaFieldName}-hidden" />
		</div></td>
<#else>
    <td><label id="${mapping.javaFieldName}Label">${mapping.javaFieldName}</label></td>
	<td><span id="${mapping.javaFieldName}">${mapping.javaFieldName}</span><input type="hidden" id="${mapping.javaFieldName}-hidden" /></td>
</#if>
	<td><span id="${mapping.javaFieldName}Message"></span></td>
</tr>
</#list>
</table>
<div id="isComeFromList">
	<input type="button" id="jump${configs.table_capitalize}List" value="Previous" 
		onclick="location.href='${configs.table_capitalize}List.html'"
	/>
</div>
<div id="isNotComeFromList">
	<input type="button" id="jump${configs.table_capitalize}Edit" value="Previous"
		onclick="location.href='${configs.table_capitalize}Edit.html'" style="display: none;"/>
</div>
<div id="isNotRead">
<div id="isCreate"><input type="button" id="doCreate" value="Finish" onclick="location.href='DeptList.html'" /></div>
<div id="isNotCreate"><input type="button" id="doUpdate" value="Finish" onclick="location.href='DeptList.html'" style="display: none;"/></div>
</div>
</form>
</body></html>