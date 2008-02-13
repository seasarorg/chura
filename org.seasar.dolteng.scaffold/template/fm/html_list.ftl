<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="ja" lang="ja">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../../css/global.css"/>
</head>
<body>
<form id="${configs.table_capitalize}ListForm">
<input type="button" id="doCreate" value="Create" onclick="location.href='${configs.table}Edit.html'"/><br/>
<table id="${configs.table}GridXY" height="200px" border="1">
	<colgroup>
		<col span="1" width="60px" class="T_leftFixed" />
	</colgroup>
	<thead>
		<tr height="50px">
<#list mappings as mapping>
			<th<#if mapping.isNumeric() = true> class="right"</#if>><label id="${mapping.javaFieldName}Label">${mapping.javaFieldName}</label></th>
</#list>
			<th><br/></th>
		</tr>
	</thead>
	<tbody>
		<tr class="row_even">
<#list mappings as mapping>
			<td<#if mapping.isNumeric() = true> class="right"</#if>><span id="${mapping.javaFieldName}">${mapping.javaFieldName}</span></td>
</#list>
			<td><a id="go${configs.table_capitalize}Edit-edit" href="${configs.table}Edit.html?fixed_crudType=2${createPkeyLink(true)}">Edit</a>
			<a id="go${configs.table_capitalize}Confirm" href="${configs.table}Confirm.html?fixed_crudType=3${createPkeyLink(true)}">Delete</a>
			<a id="go${configs.table_capitalize}Confirm-confirm" href="${configs.table}Confirm.html?fixed_crudType=1${createPkeyLink(true)}">Inquire</a>
			</td>
		</tr>
	</tbody>
</table>
</form>
</body></html>