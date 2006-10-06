<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../global.css"/>
</head>
<body>
<form>
<input type="button" id="go${configs.table_capitalize}Edit" value="Create" onclick="location.href='${configs.table}Edit.html'"/><br/>
<table id="${configs.table}Grid" width="300px" height="200px">
	<colgroup>
		<col span="2" width="60px" class="T_leftFixed" />
		<col span="4" width="70px" />
	</colgroup>
	<thead>
		<tr height="50px">
<#list mappings as mapping>
			<th><label id="${mapping.javaFieldName}Label">${mapping.javaFieldName}</label></th>
</#list>
		</tr>
	</thead>
	<tbody>
		<tr class="row_even">
<#list mappings as mapping>
			<td><span id="${mapping.javaFieldName}">${mapping.javaFieldName}<input type="hidden" id="${mapping.javaFieldName}-hidden" /></span></td>
</#list>
			<td><a id="go${configs.table_capitalize}Edit-edit" href="${configs.table}Edit.html?crudtype=2${createPkeyLink()}">Edit</a>
			<a id="go${configs.table_capitalize}Confirm" href="${configs.table}Confirm.html?crudtype=4${createPkeyLink()}">Delete</a>
			<a id="go${configs.table_capitalize}Confirm-confirm" href="${configs.table}Confirm.html?crudtype=1${createPkeyLink()}">Inquire</a>
			</td>
		</tr>
	</tbody>
</table>
</form>
</body></html>