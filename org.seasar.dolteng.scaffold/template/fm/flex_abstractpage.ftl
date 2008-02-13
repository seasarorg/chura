package ${configs.rootpackagename}.${configs.subapplicationrootpackagename} {
	
	import mx.collections.XMLListCollection;
	import mx.collections.ItemResponder;
	import mx.controls.RadioButton;
	import mx.controls.RadioButtonGroup;
	import mx.core.IMXMLObject;
	import mx.rpc.AbstractOperation;
	import mx.rpc.AsyncToken;
	import mx.rpc.Fault;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.ObjectUtil;

	import flash.events.Event;
	import flash.system.ApplicationDomain;
	import flash.utils.flash_proxy;
	import flash.utils.describeType;
	import flash.utils.Proxy;
	import flash.utils.getDefinitionByName;

	import org.seasar.flex2.rpc.RemoteMessage;
	import org.seasar.flex2.rpc.remoting.S2Flex2Service;

	public class AbstractPage implements IMXMLObject {

		private var _document: Object;
		private var _id: String;
		private var _subapp: Object;
		private var _service: S2Flex2Service;
		
		private var _hadlableEvents: Array = new Array();
		
		private var _handlerForInvisibleComponents: Array = new Array();	
		
		
		public function initialized(document:Object, id:String):void {
			
			_document = document;
			_id = id;
			
			var pageInfo:XML = describeType(this);
			for each(var methodInfo: XML in pageInfo.method){
				
				var matchArray: Array;
				var str: String;
				
				var matchString: String;
				var eventName: String;

				matchArray = String(methodInfo.@name).match("^on.+");
				if (matchArray != null && 0 < matchArray.length) {
					matchString = String(matchArray[0]);
					eventName = matchString.substr(matchString.indexOf("on") + 2);
					eventName = eventName.substr(0, 1).toLowerCase() + eventName.substring(1);
					if(_hadlableEvents.indexOf(eventName) == -1) {
						_hadlableEvents.push(eventName);
					}
				}

				matchArray = String(methodInfo.@name).match(".+On.+");
				if (matchArray != null && 0 < matchArray.length) {
					matchString = String(matchArray[0]);
					eventName = matchString.substring(matchString.indexOf("On") + 2);
					eventName = eventName.substr(0, 1).toLowerCase() + eventName.substring(1);
					if(_hadlableEvents.indexOf(eventName) == -1){
						_hadlableEvents.push(eventName);
					}
				}
			}

			document.addEventListener( Event.ADDED, registerAction );
		}

        public function remoteCall(token: AsyncToken, onSuccess: *, onFault: *): void{
        	if(token != null){
	        	var operation:String = (token.message as RemoteMessage).operation;
	        	var page:Object = this;
	        	token.addResponder(new ItemResponder(
	        		function(ret: ResultEvent, token: Object=null): void {
						onSuccess.call(page, ret, token);
					}, 
					function(ret: FaultEvent, token: Object=null): void {
						if(onFault != null){
							onFault.call(page, ret, token);
						}
					}
				));
        	}
        }

		public function onCreationComplete(event: Event):void {
			service.addEventListener("netStatus", netStatusHandler);
			service.addEventListener("ioError", ioErrorHandler);
		}

		public function registerAction(event:Event): void {
			var str: String;
			if (event.target == document) {
				trace("[registAction] " + event.target);
				for(var i: int = 0; i < _hadlableEvents.length ; i++) {
					str = _hadlableEvents[i];
					str = str.substr(0, 1).toUpperCase() + str.substring(1);
					str = "on" + str;
					if(this.hasOwnProperty(str)){
						event.target.addEventListener(_hadlableEvents[i], this[str]);
					}
				}
			}

			if (event.target.hasOwnProperty("id")) {
				if (event.target.id != undefined && event.target.id != null 
					&& _document == event.target.parentDocument) {
					
					var matchArray: Array;
					matchArray = String(event.target.id).match("jump.*");
					if (matchArray != null && 0 < matchArray.length) {
						str = String(event.target.id).substring(4);
						str = str.substr(0, 1).toLowerCase() + str.substring(1);
						if (str.indexOf("$") != -1) {
							str = str.substring(0, str.indexOf("$"));
						}
						
						if (subapp.hasOwnProperty(str)) {
							event.target.addEventListener("click", 
								function (event:Event): void { 
									subapp.pageStack.selectedChild = subapp[str]; });
						}
					}

					matchArray = String(event.target.id).match("go.*");
					if (matchArray != null && 0 < matchArray.length) {
						str = String(event.target.id).substring(2);
						str = str.substr(0, 1).toLowerCase() + str.substring(1);
						if (str.indexOf("$") != -1) {
							str = str.substring(0, str.indexOf("$"));
						}

						if (subapp.hasOwnProperty(str)) {
							event.target.addEventListener("click", 
								function (event:Event): void { 

									loadFormData(event.target.parentDocument.page);

									var sourcePageInfo: XML = describeType(event.target.parentDocument.page);
									var sourceAccessorList: XMLList = sourcePageInfo.accessor.(@access == "readwrite");

									var destinationPageInfo: XML = describeType(subapp[str].page);
									var destinationAccessorList: XMLList = destinationPageInfo.accessor.(@access == "readwrite");

									for each (var sourceAccessor: XML in sourceAccessorList) {
										for each (var destinationAccessor: XML in destinationAccessorList) {
											if (sourceAccessor.@name == destinationAccessor.@name) {
												subapp[str].page[sourceAccessor.@name] = event.target.parentDocument.page[destinationAccessor.@name];
											}
										}
									}
									subapp.pageStack.selectedChild = subapp[str];
								}
							);
						}
					}

					matchArray = String(event.target.id).match("do.*");
					if (matchArray != null && 0 < matchArray.length) {

						str = String(event.target.id);
						if (str.indexOf("$") != -1) {
							str = str.substring(0, str.indexOf("$"));
						}

						if (this.hasOwnProperty(String(event.target.id))) {
							event.target.addEventListener("click", this[str]);
						}
					}

					matchArray = String(event.target.id).match("call.*");
					if (matchArray != null && 0 < matchArray.length) {

						str = String(event.target.id);
						if (str.indexOf("$") != -1) {
							str = str.substring(0, str.indexOf("$"));
						}
						
						event.target.addEventListener("click", callService);
					}

					var target:String = String(event.target.id);
					if (target.indexOf("$") != -1) {
						target = target.substring(0, target.indexOf("$"));
					}
					for(i = 0; i < _hadlableEvents.length ; i++) {
						str = _hadlableEvents[i];
						str = str.substr(0, 1).toUpperCase() + str.substring(1);
						str = target + "On" + str;
						if(this.hasOwnProperty(str)){
							event.target.addEventListener(_hadlableEvents[i], this[str]);
						}
						
						if(event.target is RadioButton){
							var realGroupName:String = event.target.groupName;
							var groupName:String = realGroupName;
							if (groupName.indexOf("$") != -1) {
								groupName = groupName.substring(0, groupName.indexOf("$"));
							}				
							if(groupName !=null){
								str = _hadlableEvents[i];
								str = str.substr(0, 1).toUpperCase() + str.substring(1);
								str = groupName + "On" + str;
								if(this.hasOwnProperty(str)
									&& _handlerForInvisibleComponents.indexOf(realGroupName + ":" + str) == -1){

									_handlerForInvisibleComponents.push(realGroupName + ":" + str);
									event.target.group.addEventListener(_hadlableEvents[i],this[str]);
								}
							}
						}
					}			
				}
			}
		}


		public function loadFormData(dto: Object): void {

			var dtoInfo: XML = describeType(dto);
			var dtoAccessorList: XMLList = dtoInfo.accessor;

			var documentInfo: XML = describeType(document);
			var documentAccessorList: XMLList = documentInfo.accessor;

			for each (var documentAccessor: XML in documentAccessorList) {
				if (documentAccessor.@declaredBy == documentInfo.@name) {
					for each (var dtoAccessor: XML in dtoAccessorList) {
						if (dtoAccessor.@name == documentAccessor.@name) {
							var s: String = dtoAccessor.@name.toString();
							if (dtoAccessor.@type == "int") {
								dto[s] = int(document[s].text);
							} else if (dtoAccessor.@type == "Number") {
								if (document[s].text == "" || document[s].text == null) {
									dto[s] = undefined;
								} else {
									dto[s] = Number(document[s].text);
								}
							} else if (dtoAccessor.@type == "String") {
								dto[s] = document[s].text;
							} else if (dtoAccessor.@type == "Date") {
								dto[s] = document[s].selectedDate;
							}
						}
					}
				}
			}
		}

		public function convert(src: Object, dest: AbstractPage): void {

			var destInfo: XML = describeType(dest);
			var destAccessorList: XMLList = destInfo.accessor.(@access == "readwrite");
			
			for each (var destAccessor: XML in destAccessorList) {
				var s: String = destAccessor.@name;
				if (src.hasOwnProperty(s)) {
					dest[s] = src[s];
				}
			}
		}
		
		public function initPage(page: AbstractPage): void {
			var pageInfo: XML = describeType(page);
			var pageAccessorList: XMLList = pageInfo.accessor;
			var documentInfo: XML = describeType(page.document);
			var documentAccessorList: XMLList = documentInfo.accessor;

			for each (var documentAccessor: XML in documentAccessorList) {
				if (documentAccessor.@declaredBy == documentInfo.@name) {
					for each (var pageAccessor: XML in pageAccessorList) {
						if (pageAccessor.@name == documentAccessor.@name) {
							var s: String = pageAccessor.@name.toString();
							this[s] = undefined;
						}
					}
				}
			}
		}
		
		public function netStatusHandler(event:Event): void {
		}
		
		public function ioErrorHandler(event:Event): void {
		}
		
		public function callService(event:Event): void {
			var id:String = event.target.id;
			var token:AsyncToken = this[id]();
			if (token != null) 	{
				token.addResponder(new ItemResponder(
					this[id + "OnSuccess"],
					this[id + "OnFault"]));
			}
		}

		public function get document(): Object {
			return _document;
		}

		public function get id(): String {
			return _id;
		}
		
		public function get subapp(): Object {
			if(_subapp != null){
				return _subapp;
			}
			var tmpDocument :Object = _document;
			for(;;){
				if(tmpDocument.hasOwnProperty("id")){
					var subappname:String = tmpDocument.id;
					var matchArray: Array;
					if(subappname != null){
						matchArray = subappname.match(".*Subapp$");
						if (matchArray != null && 0 < matchArray.length) {
							_subapp = tmpDocument;
							return _subapp;
						}
					}
				}
				if(tmpDocument == tmpDocument.parentDocument){
					return null;
				}
				tmpDocument = tmpDocument.parentDocument;
				if(tmpDocument == null){
					return null;
				}
			}
			return null;
		}
		
		public function get service(): S2Flex2Service {
			if(_service != null){
				return _service;
			}
			var tmpDocument :Object = _document;
			for(;;){
				if(tmpDocument.hasOwnProperty("service")){
					_service = tmpDocument.service;
					return _service;
				}
				if(tmpDocument == tmpDocument.parentDocument){
					return null;
				}
				tmpDocument = tmpDocument.parentDocument;
				if(tmpDocument == null){
					return null;
				}
			}
			return null;
		}	
	}
}