<?xml version="1.0" encoding="utf-8"?>
<mx:Panel xmlns:mx="http://www.adobe.com/2006/mxml" label="${configs.table_capitalize}"
    name="${configs.table_capitalize}" title="${configs.table_capitalize}"
	xmlns:seasar="http://www.seasar.org/s2flex2/mxml" 
    xmlns:${configs.table}="${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table}.*">
	<seasar:S2Flex2Service id="service" destination="${configs.table}_${configs.table}${configs.servicesuffix}" showBusyCursor="true"/>
	<${configs.table}:${configs.table_capitalize}${namingConvention.pageSuffix} id="page"/>

    <mx:Canvas>
        <mx:Canvas borderStyle="outset" height="409"
            horizontalScrollPolicy="off" left="-8" name="Panel1"
            tabIndex="10" top="0" verticalScrollPolicy="off" width="425">
            <mx:Text left="32" name="Label1" text="No" top="272"/>
            <mx:Text left="176" name="Label2" text="Name" top="272"/>
            <mx:Text left="32" name="Label3" text="Hiredate" top="320"/>
            <mx:Text left="176" name="Label4" text="Sal" top="320"/>
        </mx:Canvas>
        <mx:DataGrid id="dg" height="193" horizontalScrollPolicy="auto"
            left="24" name="DBGrid1" tabIndex="1" top="64" width="361">
        </mx:DataGrid>
        <mx:Button height="25" label="New" left="24" name="Button1"
            tabIndex="2" top="24" width="75"/>
        <mx:Button height="25" label="Cor" left="120" name="Button2"
            tabIndex="3" top="24" width="75"/>
        <mx:Button height="25" label="Del" left="312" name="Button3"
            tabIndex="4" top="24" width="75"/>
        <mx:Button height="25" label="Cancel" left="24" name="Button4"
            tabIndex="5" top="368" width="75"/>
        <mx:Button height="25" label="Update" left="312" name="Button5"
            tabIndex="6" top="368" width="75"/>
        <mx:TextInput left="24" name="Edit1" tabIndex="7" top="288" width="121"/>
        <mx:TextInput left="168" name="Edit2" tabIndex="8" top="288" width="217"/>
        <mx:TextInput left="168" name="Edit4" tabIndex="9" top="336" width="217"/>
        <mx:DateField height="20" left="24" name="DateTimePicker1"
            top="336" width="121"/>
    </mx:Canvas>
</mx:Panel>