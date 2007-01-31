package ${configs.rootpackagename}.${namingConvention.subapplicationrootpackagename}.${namingConvention.subapplicationpackagename} {

	import mx.rpc.AsyncToken;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.events.FaultEvent;
	import flash.events.Event;
	import ${configs.dtopackagename}.${configs.dtoname};
	import ${configs.rootpackagename}.${namingConvention.subapplicationrootpackagename}.AbstractPage;
	import ${configs.rootpackagename}.${namingConvention.subapplicationrootpackagename}.AppMode;

	[Bindable]
	public class ${configs.mxml}${namingConvention.pageSuffix} extends AbstractPage {

		public var model: ${configs.dtoname};

		public var appMode: int;

		override public function onCreationComplete(event: Event): void {
			super.onCreationComplete(event);
			setInitEntryMode();
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
	}
}