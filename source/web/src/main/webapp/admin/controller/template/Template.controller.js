sap.ui.define([
	"cardies/adminapp/controller/BaseController",
	"sap/m/Dialog",
	"sap/m/MessageBox",
	"sap/m/MessageToast",
	"sap/m/Button",
	"sap/m/Text",
	"sap/ui/model/json/JSONModel",
	"cardies/adminapp/common/FileResourceSelectDialog",
	"cardies/adminapp/common/DeleteConfirmationDialog"
], function (BaseController, Dialog, MessageBox, MessageToast, Button, Text, JSONModel, FileResourceSelectDialog, DeleteConfirmationDialog) {
	"use strict";
	var entity = "Templates";
	
	return BaseController.extend("cardies.adminapp.controller.template.Template", {
		onInit: function () {
			var oRouter = this.getRouter(),
			oListSelector = this.getOwnerComponent().oListSelector;
			oRouter.getRoute("template").attachMatched(this._onRouteMatched, this);
		
		},
		
		_onRouteMatched : function (oEvent) {
			var oView = this.getView();	

			//oView.byId("idIconTabBarMulti").setVisible(false);
			oView.setBusy(true);
			this._loadMasterModel();
			this._loadStateModel();
			this._toggleControls(false);
		}, 
		
		/*****************************************************************************************************
		 * Load models
		 *****************************************************************************************************/
		_loadStateModel: function(){
			var stateModel = new JSONModel({
					editState: false,
					createNewParameter: false,
					saveNewParameter:false,
					removeNewParameter:false
			});
			
			this.getView().setModel(stateModel,"stateModel");
		},
		
		_loadMasterModel: function(){
			var oAppModel = new JSONModel();			
			var oView = this.getView();	
			var that = this;
			this.onJsonGet(
				entity, 
				function(mData){
					oAppModel.setData(mData);
					oView.setModel(oAppModel);
					that._initialiseDetailView();
					oView.setBusy(false);
				}, 
				function(){
					oView.setBusy(false);
					router.getTargets().display(Nav_NotFound);
				}
			);			
		},
		
		_loadDetailModel: function(templateId){
			var that = this;
			
			this.onJsonGet(
				entity+'('+ templateId + ')?$expand=FileResource,TemplateParameters', 
				function(mData){
					that.getView().setModel(new JSONModel(mData),"Template");
					that.getView().byId("idIconTabBarMulti").setSelectedKey(0);
					that._toggleControls(false);
				}, 
				function(){
					router.getTargets().display(Nav_NotFound);
				}
			);	
		},
		
		/*****************************************************************************************************
		 * Event handling
		 *****************************************************************************************************/
		/**
		 * Selected a Template from Master list.
		 * */
		onTemplateSelect: function(oEvent){
			this._loadDetailModel(oEvent.getSource().data("template"));
			//this.getView().byId("idIconTabBarMulti").setVisible(true);
		},
		
		onCreatePress: function(oEvent){
			this.getRouter().navTo("templateCreate");
		},
		
		onEditPress: function(oEvent){
			this._oDataCopy = jQuery.extend(true,{}, this.getView().getModel("Template").getData());
			this._toggleControls(true);
		},
		
		onSavePress: function(oEvent){
			var oBundle = this.getView().getModel("i18n").getResourceBundle();
			
			if(this._validateModel()){
				var msg = oBundle.getText("Label.MessageBox", ['update', 'Template?']);
				this._confirmUpdateTemplate(msg, "confirm");
			}else{
				MessageBox["warning"](oBundle.getText("Label.Save.failvalidation", 'Template'), {
					actions: [MessageBox.Action.OK],
					title: oBundle.getText("Label.MessageBox.title.warning"),
					onClose: null
				});
			}
		},
		
		onCancelPress: function(oEvent){
			//Restore the data
			var oModel = this.getView().getModel("Template");
			var oData = oModel.getData();

			oData = this._oDataCopy;

			oModel.setData(oData);
			oModel.refresh(true);
			this._toggleControls(false);
		},
		
		onDeletePress: function(oEvent){
			var that = this;
			var oBundle = this.getView().getModel("i18n").getResourceBundle();
			var dialogModel = new JSONModel({
				"type":"Message",
				"message":oBundle.getText("Template.message.delete", [this.getView().getModel("Template").getProperty("/Name")]),
				"lblDelete":oBundle.getText("Label.Delete"),
				"lblCancel":oBundle.getText("Label.Cancel"),
				"lblTitle":oBundle.getText("Label.Confirm")
			});
			
			var _delDialog = new DeleteConfirmationDialog(dialogModel, function(){
				that._doDelete();
			});
			
			_delDialog.open();
		},		
		
		/**
		 * Event triggered when users clicks on wizard input.
		 * - Open dialog with all HTML FileResource entities;
		 * - Allow user to select one;
		 * - Close dialog and update model;
		 * */
		onChangeFileResourcePress: function(oEvent){
			var that = this;
			
			this.onJsonGet(
				"FileResources", 
				function(mData){
					that._openFileResourceDialog(mData);
				}, 
				function(){
					sap.m.MessageToast.show("Error occured.");
				}
			);
		},
		
		onAddParameterPress: function(oEvent){
			this._addEmptyParameterObject();
		},
		
		onDeleteParameterPress: function(oEvent){
			var parameterID = oEvent.getSource().data("paramid");
			var oModel = this.getView().getModel("Template");
			
	    	for(var i = 0; i < oModel.getProperty("/TemplateParameters").length; i++){
	    		if(oModel.getProperty("/TemplateParameters")[i].ID == parameterID){
	    			this._confirmDeleteParameter(oModel.getProperty("/TemplateParameters")[i], i);
	    			break;
	    		}
	    	};
		},
		
		/*****************************************************************************************************
		 * Screen logic
		 *****************************************************************************************************/
		_initialiseDetailView: function(){
			var oView = this.getView();
			var oModel = oView.getModel();
			if(oModel.getProperty("/value") && oModel.getProperty("/value")[0]){
				this._loadDetailModel(oModel.getProperty("/value")[0].ID);
				oView.byId("idIconTabBarMulti").setVisible(true);
			}else{
				oView.byId("idIconTabBarMulti").setVisible(false);
				oView.byId("edit").setVisible(false);
				oView.byId("delete").setVisible(false);
			}
		},
		/**
		 * If confirmed, proceed with delete
		 * */
		_doDelete : function(){
			var oView = this.getView();
			var oModel = oView.getModel("Template");
			var oData = oModel.getData();
			var oBundle = oView.getModel("i18n").getResourceBundle();
			//oView.byId("idIconTabBarMulti").setVisible(false);
			
			var msg = oBundle.getText("Label.Delete.success", [oData.ID])
			var that = this;
			
			this.onJsonDelete(
				entity+ "("+oData.ID+")", 				
				function(mData){
					MessageToast.show(msg);		
					that._loadMasterModel();
				}, 
				function(){
					that.getRouter().getTargets().display(Nav_NotFound, {
						fromTarget : Nav_Some
					});
				}
			);	
		},
		
		/**
		 * Template parameter logic
		 * */
		_addEmptyParameterObject : function() {
		    var oModel = this.getView().getModel("Template");
		    var aData  = oModel.getData();

		    var emptyObject = { Type: "TEXT", ID: Math.random()};

		    aData.TemplateParameters.push(emptyObject);
		    oModel.setData(aData);
		},
		
		_confirmUpdateTemplate: function (sMessage, sMessageBoxType) {
			var that = this;
			MessageBox[sMessageBoxType](sMessage, {
				actions: [MessageBox.Action.YES, MessageBox.Action.NO],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.YES) {
						that._doUpdateTemplate();
					}
				},
			});
		},
		
		_confirmDeleteParameter: function(oParameter, index){
			if(oParameter.ID <= 1){
				this._deleteParameterModel(this.getView().getModel("Template"), index);
			}else{
				var that = this;
				var oBundle = this.getView().getModel("i18n").getResourceBundle();
				var dialogModel = new JSONModel({
					"type":"Message",
					"message":oBundle.getText("Template.message.delete.parameter"),
					"lblDelete":oBundle.getText("Label.Delete"),
					"lblCancel":oBundle.getText("Label.Cancel"),
					"lblTitle":oBundle.getText("Label.Confirm")
				});
				
				var _delDialog = new DeleteConfirmationDialog(dialogModel, function(oObject){
					that._doDeleteParameter(oObject, index);
				}, oParameter);
				
				_delDialog.open();
			}
		},
		
		_doDeleteParameter: function(oParameter, index){
			var oView = this.getView();
			var oModel = oView.getModel("Template");
			var oData = oModel.getData();
			var oBundle = oView.getModel("i18n").getResourceBundle();
			
			var msg = oBundle.getText("Label.Delete.success", [oData.ID])
			var that = this;
			
			this.onJsonDelete(
				"TemplateParameters("+oParameter.ID+")", 				
				function(mData){
					MessageToast.show(msg);		
					that._deleteParameterModel(that.getView().getModel("Template"), index);
					that._deleteParameterModel(new JSONModel(that._oDataCopy), index);
				}, 
				function(){
					that.getRouter().getTargets().display(Nav_NotFound, {
						fromTarget : Nav_Some
					});
				}
			);	
		},
		
		_deleteParameterModel: function(oModel, index){
			oModel.getProperty("/TemplateParameters").splice(index, 1);
			oModel.refresh();		
		},
		
		_doUpdateTemplate: function(){
			this._oDataCopy = jQuery.extend(true,{}, this.getView().getModel("Template").getData());
			var _oModelCopy = new JSONModel(this._oDataCopy);
			var that = this;
						
			_oModelCopy.setProperty("/FileResource@odata.bind", "FileResources("+ this.getView().getModel("Template").getProperty("/FileResource").ID+")");
			delete _oModelCopy.getData()["FileResource"];
			
			//Remove the TMP Param ID
			_oModelCopy.getProperty("/TemplateParameters").forEach(function(item, index){
				if(item.ID <= 1){
					delete item["ID"];
				}
			});

			var oBundle = this.getView().getModel("i18n").getResourceBundle();			
			var msg = oBundle.getText("Label.Update.success", [this._oDataCopy.Name])
			
			this.onJsonPut(
				"Templates("+_oModelCopy.getData().ID+")", 
				_oModelCopy.getData(),
				function(mData){
					that._oDataCopy = null;
					that._oModelCopy = null;
					that._toggleControls(false);
					MessageToast.show(msg);
				}, 
				function(){
					this.getRouter().getTargets().display("notFound", {
						fromTarget : "template"
					});
				}
			)
		},
		
		
		/**
		 * Dialog will open only on the successful return of JQuery -> FileResources
		 * */
		_openFileResourceDialog: function(mData){
			var that = this;
			var _mData = mData;
			
			var _fileResourceDialog = new FileResourceSelectDialog(new JSONModel(mData),
				function(oObject){
					that.getView().byId("TemplateFileResource").setValue(oObject.ID);
					that.getView().getModel("Template").setProperty("/FileResource", oObject);
				}
			);	
			
			_fileResourceDialog.open();
			
		},
		
		/*****************************************************************************************************
		 * Toggle page object state
		 *****************************************************************************************************/
		_toggleControls: function(state){
			this.getView().getModel("stateModel").setProperty("/editState", state);
		},
		
		enableControl: function(state){
			return !!state
		},
		
		disableControl: function(state){
			return !state
		},
		
		/*****************************************************************************************************
		 * Validation logic
		 *****************************************************************************************************/
		_validateModel(){
			var oModel = this.getView().getModel("Template");
			
			if(!this._validateTemplate(oModel.getProperty("/"))){
				return false;
			}
			
			for(var i = 0; i < oModel.getProperty("/TemplateParameters").length; i++){
    			if(!this._validateParameter(oModel.getProperty("/TemplateParameters")[i])){
    				return false;
    			}
	    	};
	    	
	    	return true;
		},
		
		_validateTemplate: function(obj){
	    	if(obj.Name == null || obj.Name == "" ||  obj.Name.length == 0 || obj.Name.length > 50){
		    	return false;
		    }else if(obj.Description !== null && obj.Description > 250){
		    	return false
		    }else if(obj.DefaultValues !== null && obj.DefaultValues > 250){
		    	return false
		    }else{
		    	return true
		    }
	    },
		
		_validateParameter: function(obj){
	    	if(obj.Name == null || obj.Name == "" ||  obj.Name.length == 0 || obj.Name.length > 50){
		    	return false;
		    }else if(obj.Description !== null && obj.Description > 250){
		    	return false
		    }else if(obj.DefaultValues !== null && obj.DefaultValues > 250){
		    	return false
		    }else{
		    	return true
		    }
	    }
	});
	

});