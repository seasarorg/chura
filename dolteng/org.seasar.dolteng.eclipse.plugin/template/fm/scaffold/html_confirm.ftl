<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../global.css"/>
</head>
<body>
<form>
<div>
<span id="messages"></span>
</div>
<table>
<#list mappings as mapping>
<tr>
    <td class="label">${mapping.javaFieldName}</td>
	<td>
	<span id="${mapping.javaFieldName}">${mapping.javaFieldName}<input type="hidden" id="${mapping.javaFieldName}-hidden" /></span>
	</td>
	<td><span id="${mapping.javaFieldName}Message"></span></td>
</tr>
</#list>
</table>
<span id="isRead">
	<input type="button" id="jump${configs.table_capitalize}List" value="Previous" 
		onclick="location.href='${configs.table_capitalize}List.html'"
	/>
</span>
<span id="isNotRead">
	<input type="button" id="jump${configs.table_capitalize}Edit" value="Previous"
		onclick="location.href='${configs.table_capitalize}Edit.html'"
	/>
</span>
<span id="isCreate">
	<input type="button" id="go${configs.table_capitalize}List-create" value="Create"
		onclick="location.href='${configs.table_capitalize}List.html'"
	/>
</span>
<span id="isUpdate">
	<input type="button" id="go${configs.table_capitalize}List-update" value="Update"
		onclick="location.href='${configs.table_capitalize}List.html'"
	/>
</span>
<span id="isDelete">
	<input type="button" id="go${configs.table_capitalize}List-delete" value="Delete"
		onclick="location.href='${configs.table_capitalize}List.html'"
	/>
</span>
</form>
</body></html>