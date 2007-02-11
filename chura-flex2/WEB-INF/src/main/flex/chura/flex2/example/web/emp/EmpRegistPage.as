package chura.flex2.example.web.emp {

	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.events.FaultEvent;
	import flash.events.Event;
	import chura.flex2.example.entity.Emp;
	import chura.flex2.example.web.AbstractPage;
	import chura.flex2.example.web.AppMode;

	[Bindable]
	public class EmpRegistPage extends AbstractPage {

		public var model: Emp;

		public var appMode: int;

		override public function onCreationComplete(event: Event): void {
			super.onCreationComplete(event);
			setInitEntryMode();
			getEmployees();
		}
		
		public function setInitEntryMode(): void {
			appMode = AppMode.NEUTRAL;
			model = null;
		}
		
		public function setNewEntryMode(): void {
			appMode = AppMode.NEW;
		}

		public function setCorEntryMode(): void {
			appMode = AppMode.COR;
		}
		public function getEmployees():void {
			remoteCall(service.getEmployees(), getEmployeesOnSuccess, getEmployeesOnFault);
		}
		public function getEmployeesOnSuccess(e:ResultEvent, token:Object=null):void {
			document.dg.dataProvider=e.result;
		}
		public function getEmployeesOnFault(e:FaultEvent, token:Object=null):void {
			Alert.show("getEmployees is fault");
		}
		public function getEmp():void {
			var empNo : int = model.empNo;
			var empName : String = model.empName;
			remoteCall(service.getEmp(empNo, empName), getEmpOnSuccess, getEmpOnFault);
		}
		public function getEmpOnSuccess(e:ResultEvent, token:Object=null):void {
		}
		public function getEmpOnFault(e:FaultEvent, token:Object=null):void {
			Alert.show("getEmp is fault");
		}
	}
}