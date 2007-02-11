package ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.${configs.table} {

	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.events.FaultEvent;
	import flash.events.Event;
	import ${configs.rootpackagename}.${configs.entitypackagename}.${configs.table_capitalize};
	import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.AbstractPage;
	import ${configs.rootpackagename}.${configs.subapplicationrootpackagename}.AppMode;

	[Bindable]
	public class ${configs.table_capitalize}${namingConvention.pageSuffix} extends AbstractPage {

		public var model: ${configs.table_capitalize};

		public var appMode: int;

		override public function onCreationComplete(event: Event): void {
			super.onCreationComplete(event);
			setInitEntryMode();
			selectAll();
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
		
		public function selectAll():void {
			remoteCall(service.selectAll(), selectAllOnSuccess, selectAllOnFault);
		}
		public function selectAllOnSuccess(e:ResultEvent, token:Object=null):void {
			document.dg.dataProvider=e.result;
		}
		public function selectAllOnFault(e:FaultEvent, token:Object=null):void {
			Alert.show("selectAll is fault");
		}
		public function selectById():void {
			var id : int = model.id;
			remoteCall(service.selectById(id), selectByIdOnSuccess, selectByIdOnFault);
		}
		public function selectByIdOnSuccess(e:ResultEvent, token:Object=null):void {
		}
		public function selectByIdOnFault(e:FaultEvent, token:Object=null):void {
			Alert.show("selectById is fault");
		}
	}
}