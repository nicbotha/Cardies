sap.ui.define([ "sap/ui/core/mvc/Controller", "sap/ui/core/routing/History",
		"cardies/adminapp/common/AjaxUtil" ], 
		
	function(Controller, History,AjaxUtil) {
	"use strict";

	return Controller.extend("cardies.adminapp.controller.BaseController", {

		getRouter : function() {
			return sap.ui.core.UIComponent.getRouterFor(this);
		},

		onNavBack : function(oEvent) {
			var oHistory, sPreviousHash;

			oHistory = History.getInstance();
			sPreviousHash = oHistory.getPreviousHash();

			if (sPreviousHash !== undefined) {
				window.history.go(-1);
			} else {
				this.getRouter().navTo("appHome", {}, true /* no history */);
			}
		},
		
		onJsonGet : function(entity, fnSuccess, fnError, fnAlways) {
			AjaxUtil.getJSon(this, entity, fnSuccess, fnError, fnAlways);
		},
		
		onJsonPost : function(entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback){
			AjaxUtil.postJSon(this, entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback);
		},
		
		onJsonPut : function(entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback){
			AjaxUtil.putJSon(this, entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback);
		},
		
		onJsonDelete : function(entity, fnSuccess, fnError, fnAlways) {
			AjaxUtil.deleteJson(this, entity, fnSuccess, fnError, fnAlways);
		}
	});

});
