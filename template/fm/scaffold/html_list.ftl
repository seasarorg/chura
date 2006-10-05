<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../global.css"/>
</head>
<body>
<form>
<input type="submit" value="Create" /><br/>
<table id="${configs.table}Grid" width="300px" height="200px">
	<colgroup>
		<col span="2" width="60px" class="T_leftFixed" />
		<col span="4" width="70px" />
	</colgroup>
	<thead>
		<tr height="50px">
<#list mappings as mapping>
			<th>${mapping.javaFieldName}</th>
</#list>
		</tr>
	</thead>
	<tbody>
		<tr>
<#list mappings as mapping>
			<td><span id="${mapping.javaFieldName}">${mapping.javaFieldName}<input type="hidden" id="${mapping.javaFieldName}-hidden" /></span></td>
</#list>
			<td><a id="go${configs.table_capitalize}Edit" href="${configs.table}Edit.html?crudtype=2&id=$id">Edit</a>
			<a id="go${configs.table_capitalize}Confirm.html" href="${configs.table}Confirm.html?crudtype=4&id=$id">Delete</a>
			<a id="go${configs.table_capitalize}Confirm.html" href="${configs.table}Confirm.html?crudtype=1&id=$id">Inquire</a>
			</td>
		</tr>
	</tbody>
</table>
</form>
</body></html>