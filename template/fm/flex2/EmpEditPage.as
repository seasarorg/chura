package examples.chura.web.emp {

	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.events.FaultEvent;
	import flash.events.Event;
	import examples.chura.entity.Emp;
	import examples.chura.web.AbstractPage;
	import mx.managers.PopUpManager;
	import mx.events.ValidationResultEvent;
	import mx.validators.Validator;

	[Bindable]
	public class EmpEditPage extends AbstractPage {

		public var model: Emp;

		public var appMode: int;

		public var parentObject: Object;

		override public function onCreationComplete(event: Event): void {
			model = new Emp();
		}

		public function createOnClick(event: Event): void {
			if (Validator.validateAll([document.empIdV, document.empNoV]).length < 1) {
				insert();
			}
		}

		public function cancelOnClick(event: Event): void {
			PopUpManager.removePopUp(document as EmpEdit);
		}

		public function insert():void {
			remoteCall(service.insert(this.model), insertOnSuccess, insertOnFault);
		}
		public function insertOnSuccess(e:ResultEvent, token:Object=null):void {
			PopUpManager.removePopUp(document as EmpEdit);
			parentObject.page.selectAll();
		}
		public function insertOnFault(e:FaultEvent, token:Object=null):void {
			Alert.show("insert is fault");
		}
	}
}