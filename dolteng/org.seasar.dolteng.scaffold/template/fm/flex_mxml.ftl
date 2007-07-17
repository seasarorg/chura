<?xml version="1.0" encoding="utf-8"?>
<mx:Panel xmlns:mx="http://www.adobe.com/2006/mxml" label="${configs.table_capitalize}"
    name="${configs.table_capitalize}" title="${configs.table_capitalize}"
	xmlns:seasar="http://www.seasar.org/s2flex2/mxml" 
    xmlns:${configs.table}="${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table}.*"
    width="432" height="410">
	<seasar:S2Flex2Service id="service" destination="${configs.table}_${configs.table}${configs.servicesuffix}" showBusyCursor="true"/>
	<${configs.table}:${configs.table_capitalize}${namingConvention.pageSuffix} id="page"/>

    <mx:Canvas width="403" height="361">
        <mx:DataGrid id="dg" height="308" horizontalScrollPolicy="auto" editable="true"
            left="10" name="DBGrid1" tabIndex="1" top="10" width="383">
            <mx:columns>
<#list mappings as mapping>
            	<mx:DataGridColumn dataField="${mapping.javaFieldName}" headerText="${mapping.javaFieldName}" />
</#list>
            </mx:columns>
        </mx:DataGrid>
        <mx:Button height="25" label="Update" name="Button5"
            tabIndex="6" width="75" id="update" x="231" y="326"/>
        <mx:Button height="25" label="Remove" name="Button3"
            tabIndex="4" width="75" id="remove" x="314" y="326"/>
    </mx:Canvas>
</mx:Panel>