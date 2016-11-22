sap.ui.define([
	"cardies/adminapp/controller/BaseController",
	"sap/m/Dialog",
	"sap/m/MessageBox",
	"sap/m/MessageToast",
	"sap/m/Button",
	"sap/m/Text",
	"sap/ui/model/json/JSONModel",
	"cardies/adminapp/common/TemplateSelectDialog",
	"cardies/adminapp/common/ChannelSelectDialog",
	"cardies/adminapp/common/PersonSelectDialog",
	"cardies/adminapp/common/DeleteConfirmationDialog"
], function (BaseController, Dialog, MessageBox, MessageToast, Button, Text, JSONModel, TemplateSelectDialog, ChannelSelectDialog, PersonSelectDialog, DeleteConfirmationDialog) {
	"use strict";
	var entity = "Cards";
	
	return BaseController.extend("cardies.adminapp.controller.card.Card", {
		onInit: function () {
			var oRouter = this.getRouter(),
			oListSelector = this.getOwnerComponent().oListSelector;
			oRouter.getRoute("card").attachMatched(this._onRouteMatched, this);
		
		},
		
		_onRouteMatched : function (oEvent) {
			var oView = this.getView();	

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
		
		//OData request e.g. Cards(id)?$expand=CardParameters would not return TemplateParameter, therefor an additional request is required for each CardParameter
		_loadDetailModel: function(cardId){
			var that = this;
			
			this.onJsonGet(
				entity+'('+ cardId + ')?$expand=Template,Channel,CardParameters,Persons', 
				function(mData){
					that.getView().setModel(new JSONModel(mData),"Card");
					//Fetch additional information
					that._loadDetailCardParameterModel();
					that.getView().getModel("Card").refresh(true);
					that.getView().byId("idIconTabBarMulti").setSelectedKey(0);
					that._toggleControls(false);
				}, 
				function(){
					router.getTargets().display(Nav_NotFound);
				}
			);	
		},
		
		_loadDetailCardParameterModel: function(){
			var that = this;
			var oModel = this.getView().getModel("Card");
			
			for(var i = 0; i < oModel.getProperty("/CardParameters").length; i++){
				this.onJsonGet(
					'CardParameters('+ oModel.getProperty("/CardParameters")[i].ID + ')?$expand=TemplateParameter', 
					function(mData){
						that.onGetCardParameterModel(mData);
					}, 
					function(){
						router.getTargets().display(Nav_NotFound);
					}
				);	
	    	};			
		},
		
		/*****************************************************************************************************
		 * Event handling
		 *****************************************************************************************************/
		/**
		 * Selected a Card from Master list.
		 * */
		onCardSelect: function(oEvent){
			this._loadDetailModel(oEvent.getSource().data("card"));
		},
		
		onGetCardParameterModel: function(mData){
			var oModel = this.getView().getModel("Card");
			var oCardParameters = oModel.getProperty("/CardParameters");
			for(var i = 0; i < oCardParameters.length; i++){
				if(oCardParameters[i].ID == mData.ID){
					oCardParameters[i] = mData;
				};
			};
			oModel.setProperty("/CardParameters", oCardParameters);
		},
		
		onCreatePress: function(oEvent){
			this.getRouter().navTo("cardCreate");
		},
		
		onEditPress: function(oEvent){
			this._oDataCopy = jQuery.extend(true,{}, this.getView().getModel("Card").getData());
			this._toggleControls(true);
		},
		
		onSavePress: function(oEvent){
			var oBundle = this.getView().getModel("i18n").getResourceBundle();
			
			if(this._validateModel()){
				var msg = oBundle.getText("Label.MessageBox", ['update', 'Card?']);
				this._confirmUpdateCard(msg, "confirm");
			}else{
				MessageBox["warning"](oBundle.getText("Label.Save.failvalidation", 'Card'), {
					actions: [MessageBox.Action.OK],
					title: oBundle.getText("Label.MessageBox.title.warning"),
					onClose: null
				});
			}
		},
		
		onCancelPress: function(oEvent){
			//Restore the data
			var oModel = this.getView().getModel("Card");
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
				"message":oBundle.getText("Card.message.delete", [this.getView().getModel("Card").getProperty("/Name")]),
				"lblDelete":oBundle.getText("Label.Delete"),
				"lblCancel":oBundle.getText("Label.Cancel"),
				"lblTitle":oBundle.getText("Label.Confirm")
			});
			
			var _delDialog = new DeleteConfirmationDialog(dialogModel, function(){
				that._doDelete();
			});
			
			_delDialog.open();
		},		
		
		onChangeTemplatePress: function(oEvent){
			var that = this;
			
			this.onJsonGet(
				"Templates", 
				function(mData){
					that._openTemplateDialog(mData);
				}, 
				function(){
					sap.m.MessageToast.show("Error occured.");
				}
			);
		},
		
		onChangeChannelPress: function(oEvent){
			var that = this;
			
			this.onJsonGet(
				"Channels", 
				function(mData){
					that._openChannelDialog(mData);
				}, 
				function(){
					sap.m.MessageToast.show("Error occured.");
				}
			);
		},
		
		onAddReceiverPress: function(oEvent){
			var that = this;
						
			this.onJsonGet(
				"Persons", 
				function(mData){
					that._openSelectReceiverDialog(mData);
				}, 
				function(){
					sap.m.MessageToast.show("Error occured.");
				}
			);
		},
		
		onDeleteReceiverPress: function(oEvent){
			var personID = oEvent.getSource().data("personid");
			var oModel = this.getView().getModel("Card");
			
	    	for(var i = 0; i < oModel.getProperty("/Persons").length; i++){
	    		if(oModel.getProperty("/Persons")[i].ID == personID){
	    			oModel.getProperty("/Persons").splice(i, 1);
	    			oModel.refresh();		
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
			var oModel = oView.getModel("Card");
			var oData = oModel.getData();
			var oBundle = oView.getModel("i18n").getResourceBundle();
			
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
		
		_confirmUpdateCard: function (sMessage, sMessageBoxType) {
			var that = this;
			MessageBox[sMessageBoxType](sMessage, {
				actions: [MessageBox.Action.YES, MessageBox.Action.NO],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.YES) {
						that._doUpdateCard();
					}
				},
			});
		},
		
	
		_doUpdateCard: function(){
			this._oDataCopy = jQuery.extend(true,{}, this.getView().getModel("Card").getData());
			var _oModelCopy = new JSONModel(this._oDataCopy);
			var that = this;
						
			_oModelCopy.setProperty("/Template@odata.bind", "Templates("+ this.getView().getModel("Card").getProperty("/Template").ID+")");
			delete _oModelCopy.getData()["Template"];
			
			_oModelCopy.setProperty("Channel@odata.bind", "Channels("+ this.getView().getModel("Card").getProperty("/Channel").ID+")");
			delete _oModelCopy.getData()["Channel"];
			
			//Update CardParameters
			var oCardParameters = [];
			_oModelCopy.getProperty("/CardParameters").forEach(function(item, index){
				var oCardParameter = {
					"Value": item.Value,
					"TemplateParameter@odata.bind": "TemplateParameters("+item.TemplateParameter.ID+")"
				};
				oCardParameters.push(oCardParameter);
			});
			delete _oModelCopy.getData()["CardParameters"];
			_oModelCopy.setProperty("/CardParameters", oCardParameters);
			
			//Update Receivers
			var oReceivers = [];
			_oModelCopy.getProperty("/Persons").forEach(function(item, index){
				var oReceiver = {
					"ID": item.ID,
				};
				oReceivers.push(oReceiver);
			});
			delete _oModelCopy.getData()["Persons"];
			_oModelCopy.setProperty("/Persons", oReceivers);

			var oBundle = this.getView().getModel("i18n").getResourceBundle();			
			var msg = oBundle.getText("Label.Update.success", [this._oDataCopy.Name])
			
			this.onJsonPut(
				"Cards("+_oModelCopy.getData().ID+")", 
				_oModelCopy.getData(),
				function(mData){
					that._oDataCopy = null;
					that._oModelCopy = null;
					that._toggleControls(false);
					MessageToast.show(msg);
				}, 
				function(){
					this.getRouter().getTargets().display("notFound", {
						fromTarget : "card"
					});
				}
			)
		},
		
		
		/**
		 * Dialog will open only on the successful return of JQuery -> Templates
		 * */
		_openTemplateDialog: function(mData){
			var that = this;
			var _mData = mData;
			
			var _templateDialog = new TemplateSelectDialog(new JSONModel(mData),
				function(oObject){
					that.getView().byId("CardTemplate").setValue(oObject.ID);
					that.getView().getModel("Card").setProperty("/Template", oObject);
					that._updateParametersNewTemplate();
				}
			);	
			_templateDialog.open();
		},
		
		/**
		 * Dialog will open only on the successful return of JQuery -> Channel
		 * */
		_openChannelDialog: function(mData){
			var that = this;
			var _mData = mData;
			
			var _channelDialog = new ChannelSelectDialog(new JSONModel(mData),
				function(oObject){
					that.getView().byId("CardChannel").setValue(oObject.ID);
					that.getView().getModel("Card").setProperty("/Channel", oObject);
				}
			);	
			_channelDialog.open();
		},
		
		_openSelectReceiverDialog: function(mData){
			var that = this;
			var _mData = mData;
			
			var _receiverDialog = new PersonSelectDialog(new JSONModel(mData),
				function(oObject){
				var oModel = that.getView().getModel("Card");
			    var aData  = oModel.getData();
			    var receiverObject = { ID: oObject.ID, Name: oObject.Name, Email: oObject.Email};

			    aData.Persons.push(receiverObject);
			    oModel.setData(aData);
				}
			);	
			_receiverDialog.open();
		},
		
		/**
		 * As user selected a new Template the parameters has to be refreshed.
		 * */
		_updateParametersNewTemplate: function(){
			var oView = this.getView();	
			var oModel = oView.getModel("Card");
			var that = this;
			this.onJsonGet(
				"Templates("+oModel.getProperty("/Template").ID+")/TemplateParameters", 
				function(mData){
					var oCardParameters = [];
					if(mData.value){
						mData.value.forEach(function (item, index){
							var cardParameter = {
								"Value": "",
								"TemplateParameter": item
							}
							oCardParameters.push(cardParameter);
						});
					}
					oModel.setProperty("/CardParameters", oCardParameters);
				}, 
				function(){
					oView.setBusy(false);
					router.getTargets().display(Nav_NotFound);
				}
			);			
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
			var oModel = this.getView().getModel("Card");
			
			if(!this._validateCard(oModel.getProperty("/"))){
				return false;
			}
			
			for(var i = 0; i < oModel.getProperty("/CardParameters").length; i++){
    			if(!this._validateParameter(oModel.getProperty("/CardParameters")[i])){
    				return false;
    			}
	    	};
	    	
	    	return true;
		},
		
		_validateCard: function(obj){
	    	if(obj.Name == null || obj.Name == "" ||  obj.Name.length == 0 || obj.Name.length > 50){
		    	return false;
		    }else if(obj.PublishDate !== null && obj.PublishDate > 250){
		    	return false
		    }else{
		    	return true
		    }
	    },
		
		_validateParameter: function(obj){
	    	if(obj.Value !== null && obj.Value > 250){
		    	return false
		    }else{
		    	return true
		    }
	    }
	});
	

});