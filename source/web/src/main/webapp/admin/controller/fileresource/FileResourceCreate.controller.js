sap.ui.define([
	"cardies/adminapp/controller/BaseController",
	"sap/ui/core/Fragment",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageToast"
], function (BaseController, Fragment, JSONModel, MessageToast) {
	"use strict";
	var entity = "FileResources";
	var Nav_Listener = "fileresourceCreate";
	var Nav_List = "fileresourceList";
	var Nav_NotFound = "notFound";

	return BaseController.extend("cardies.adminapp.controller.fileresource.FileResourceCreate", {

		onInit: function () {
			var oRouter = this.getRouter();
			oRouter.getRoute(Nav_Listener).attachMatched(this._onRouteMatched, this);
		},

		_onRouteMatched : function (oEvent) {
			var oModel = new JSONModel();
			this.getView().setModel(oModel);
			this.getView().bindElement("/");

			this._hidePreview();
		},
		
		handleUploadComplete: function(oEvent){
			var sResponse = oEvent.getParameter("responseText");
			var oAppModel = new sap.ui.model.json.JSONModel();
			var oView = this.getView();
			var oModel = this.getView().getModel();
			
			oAppModel.setJSON(sResponse);
			this._hidePreview();
			this._showPreview(oAppModel);
			
			oModel.getData().DocStoreId = oAppModel.getData().docStoreId;
			oModel.getData().Name =  oAppModel.getData().fileName;
			oAppModel.getData().contentType == "image/png" ? oModel.getData().Type = "IMAGE" : null;
			oAppModel.getData().contentType == "image/jpeg" ? oModel.getData().Type = "IMAGE" : null;
			oAppModel.getData().contentType == "text/html" ? oModel.getData().Type="HTML" : null;
			
			oModel.refresh(true);
		},
		
		_hidePreview(){
			try{
				var _img = this.getView().byId("previewImgId");
				var _link = this.getView().byId("previewLinkId");
				_img.setVisible(false);
				_link.setVisible(false);
				this.getView().byId("fileUpload").setValue("");
			}catch(e){}
		},
		
		_showPreview(oModel){
			var _img = this.getView().byId("previewImgId");
			var _link = this.getView().byId("previewLinkId");
			if(oModel.getData().contentType == "image/png" || oModel.getData().contentType == "image/jpeg"){
				_img.setSrc("/web/fileresource?ID="+ oModel.getData().docStoreId);
				_img.setVisible(true);
			}else if(oModel.getData().contentType == "text/html"){
				_link.setHref("/web/fileresource?ID="+ oModel.getData().docStoreId);
				_link.setText(oModel.getData().fileName);
				_link.setVisible(true); 
			}	
		},
		
		onSavePress : function(oEvent){
			var oModel = this.getView().getModel();
			var oData = oModel.getData();
			var oBundle = this.getView().getModel("i18n").getResourceBundle();
			
			var msg = oBundle.getText("Label.Create.success", [oData.Name])
			var that = this;
			
			this.onJsonPost(
				entity, 
				oData,
				function(mData){
					MessageToast.show(msg);
					that.getRouter().navTo(Nav_List);	
				}, 
				function(){
					this.getRouter().getTargets().display(Nav_NotFound, {
						fromTarget : Nav_Listener
					});
				}
			);
		},
		
		onCancelPress: function(oEvent){
			this.getRouter().navTo(Nav_List);
		}
	});

});