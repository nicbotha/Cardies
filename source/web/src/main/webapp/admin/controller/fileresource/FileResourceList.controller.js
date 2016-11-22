sap.ui.define([
	"cardies/adminapp/controller/BaseController"
], function (BaseController) {
	"use strict";
	var entity = "FileResources";
	var Nav_Create = "fileresourceCreate";
	var Nav_Listener = "fileresourceList";
	var Nav_NotFound = "notFound";
	var Nav_View = "fileresourceView";
	var Nav_Home = "home";

	return BaseController.extend("cardies.adminapp.controller.fileresource.FileResourceList", {

		onInit: function () {
			var oRouter = this.getRouter();
			oRouter.getRoute(Nav_Listener).attachMatched(this._onRouteMatched, this);
		},
		
		onNavBack: function(oEvent){
			this.getRouter().navTo(Nav_Home);
		},

		_onRouteMatched : function (oEvent) {
			var oArgs, oView;
			var oAppModel = new sap.ui.model.json.JSONModel();			
			var oView = this.getView();	

			oView.setBusy(true);
			this.onJsonGet(
				entity, 
				function(mData){
					oAppModel.setData(mData);
					oView.setModel(oAppModel);
					oView.setBusy(false);
				}, 
				function(){
					router.getTargets().display(Nav_NotFound);
				}
			);
			
		},
				
		onListItemPressed : function(oEvent){
			var oItem, oCtx;

			oItem = oEvent.getSource();
			oCtx = oItem.getBindingContext();

			this.getRouter().navTo(Nav_View,{
				id : oCtx.getProperty("ID")
			});

		},
		
		onFormatIcon: function(oType){
			if(oType == "IMAGE"){
				return "sap-icon://attachment-photo";
			}else if(oType == "HTML"){
				return "sap-icon://attachment-html";
			}
		},
		
		onCreate: function(oEvent){
			this.getRouter().navTo(Nav_Create);
		}

	});

});