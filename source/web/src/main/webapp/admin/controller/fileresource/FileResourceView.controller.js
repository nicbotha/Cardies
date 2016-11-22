sap.ui.define([
	"cardies/adminapp/controller/BaseController",
	"sap/m/Dialog",
	"sap/m/MessageToast",
	"sap/m/Button",
	"sap/m/Text",
	"sap/ui/model/json/JSONModel"
], function (BaseController, Dialog, MessageToast, Button, Text, JSONModel) {
	"use strict";
	var entity = "FileResources";
	var Nav_SomeList = "fileresourceList";
	var Nav_NotFound = "notFound";
	var Nav_Some = "fileresourceView";

	return BaseController.extend("cardies.adminapp.controller.fileresource.FileResourceView", {

		onInit: function () {
			var oRouter = this.getRouter();
			oRouter.getRoute(Nav_Some).attachMatched(this._onRouteMatched, this);
		},

		_onRouteMatched : function (oEvent) {
			var oArgs, oView;
			var oAppModel = new sap.ui.model.json.JSONModel();
			
			oArgs = oEvent.getParameter("arguments");
			var oView = this.getView();	
			var that = this;

			oView.setBusy(true);
			this._hidePreview();
			
			this.onJsonGet(
				entity + "("+oArgs.id+")", 
				function(mData){
					oAppModel.setData(mData);
					oView.setModel(oAppModel);
					oView.bindElement("/");	
					that._toggleButtonsAndView(false);
					that._showPreview(oAppModel);
					oView.setBusy(false);
				}, 
				function(){
					that.getRouter().getTargets().display(Nav_NotFound);
				}
			);
		},
		
		handleUploadComplete: function(oEvent){
			var sResponse = oEvent.getParameter("responseText");
			var oAppModel = new sap.ui.model.json.JSONModel();
			var oView = this.getView();
			var oModel = this.getView().getModel();
			
			oAppModel.setJSON(sResponse);
			this._hidePreview();
			oModel.getData().DocStoreId = oAppModel.getData().docStoreId;
			oModel.getData().Name =  oAppModel.getData().fileName;
			oAppModel.getData().contentType == "image/png" ? oModel.getData().Type = "IMAGE" : null;
			oAppModel.getData().contentType == "image/jpeg" ? oModel.getData().Type = "IMAGE" : null;
			oAppModel.getData().contentType == "text/html" ? oModel.getData().Type="HTML" : null;
			this._showPreview(oModel);
			oModel.refresh(true);
		},
		
		_hidePreview(){
				this.getView().byId("previewImgId").setVisible(false);
				this.getView().byId("previewLinkId").setVisible(false);
				this.getView().byId("fileUpload").setValue("");
		},
		
		_showPreview(oModel){
			if(oModel.getData().contentType == "image/png" || oModel.getData().Type == "IMAGE" || oModel.getData().contentType == "image/jpeg"){
				var oImage = this.getView().byId("previewImgId");
				oImage.setSrc("/web/fileresource?ID="+ oModel.getData().DocStoreId);
				oImage.setVisible(true);
			}else if(oModel.getData().contentType == "text/html" || oModel.getData().Type == "HTML"){
				var oLink = this.getView().byId("previewLinkId");
				oLink.setHref("/web/fileresource?ID="+ oModel.getData().DocStoreId);
				oLink.setText(oModel.getData().Name);
				oLink.setVisible(true); 
			}	
		},
				
		onEditPress : function(){
			this._oDataCopy = jQuery.extend({}, this.getView().getModel().getData());
			this._toggleButtonsAndView(true);
		},
		
		onCancelPress : function () {

			//Restore the data
			var oModel = this.getView().getModel();
			var oData = oModel.getData();

			oData = this._oDataCopy;

			oModel.setData(oData);
			this._toggleButtonsAndView(false);

		},

		onSavePress : function () {
			this._doSave();
			this._toggleButtonsAndView(false);
		},
		
		onDeletePress: function(){
			var oBundle = this.getView().getModel("i18n").getResourceBundle();
			var that = this;  
			var dialog = new Dialog({
				type: 'Message',			
				afterClose: function() {
					dialog.destroy();
				}
			});
			var name = this.getView().getModel().getProperty("/Name");
			var oMessage = new Text({text: oBundle.getText("Some.delete.message", [name])});
	        var oBeginButton = new Button({
	        	text: oBundle.getText("Label.Delete"),
	        	press: function(){
	        		that._doDelete();
	        		dialog.close();
	        	}
	        });
	        var oEndButton = new Button({
	        	text: oBundle.getText("Label.Cancel"),
	        	press: function(){
	        		dialog.close();
	        	}
	        });
			
	        dialog.setTitle(oBundle.getText("Label.Confirm"));
			dialog.addContent(oMessage);
			dialog.setBeginButton(oBeginButton);
			dialog.setEndButton(oEndButton);

			dialog.open();
		},
		
		_doDelete : function(){
			var oModel = this.getView().getModel();
			var oData = oModel.getData();
			var oBundle = this.getView().getModel("i18n").getResourceBundle();
			
			var msg = oBundle.getText("Label.Delete.success", [oData.Name])
			var that = this;
			
			this.onJsonDelete(
				entity+ "("+oData.ID+")", 				
				function(mData){
					MessageToast.show(msg);		
					that.getRouter().navTo(Nav_SomeList);
				}, 
				function(){
					that.getRouter().getTargets().display(Nav_NotFound, {
						fromTarget : Nav_Some
					});
				}
			);	
		},
		
		_doSave(){
			var oModel = this.getView().getModel();
			var oData = oModel.getData();
			var oBundle = this.getView().getModel("i18n").getResourceBundle();
			
			var that = this;
			
			this.onJsonPut(
				entity+ "("+oData.ID+")", 
				oData,
				function(mData){
					MessageToast.show(oBundle.getText('Label.Save.success', [oData.Name]));
				}, 
				function(){
					MessageToast.show(oBundle.getText('Label.Save.error', [oData.Name]));
				}
			);			
		},
		
		_toggleButtonsAndView : function (bEdit) {
			var oView = this.getView();

			// Show the appropriate action buttons
			oView.byId("edit").setVisible(!bEdit);
			oView.byId("delete").setVisible(!bEdit);
			oView.byId("save").setVisible(bEdit);
			oView.byId("cancel").setVisible(bEdit);
			
			oView.byId("fileUpload").setVisible(bEdit);
			oView.byId("Name").setEnabled(bEdit);
			oView.byId("Description").setEnabled(bEdit);
			oView.byId("Tags").setEnabled(bEdit);
		}
		
	});

});
