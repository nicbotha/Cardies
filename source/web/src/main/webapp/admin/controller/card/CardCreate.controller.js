sap.ui.define([
	"cardies/adminapp/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/comp/valuehelpdialog/ValueHelpDialog",
	"sap/m/MessageBox",
	"sap/m/MessageToast",
	"cardies/adminapp/common/TemplateSelectDialog",
	"cardies/adminapp/common/ChannelSelectDialog",
], function (BaseController, JSONModel, ValueHelpDialog, MessageBox, MessageToast, TemplateSelectDialog, ChannelSelectDialog) {
	"use strict";
	
	return BaseController.extend("cardies.adminapp.controller.card.CardCreate", {
		onInit: function () {
			var oRouter = this.getRouter();
			oRouter.getRoute("cardCreate").attachMatched(this._onRouteMatched, this);
			
			this._wizard = this.getView().byId("createWizard");
			this._oNavContainer = this.getView().byId("wizardNavContainer");
			this._oWizardContentPage = this.getView().byId("wizardContentPage");
			this._oWizardReviewPage = sap.ui.xmlfragment("cardies.adminapp.view.card.CardCreateReviewStep", this);
			this._oNavContainer.addPage(this._oWizardReviewPage);
		}, 
		
		_onRouteMatched : function (oEvent) {
			this.handleNavigationToStep(0);
			this._wizard.discardProgress(this._wizard.getSteps()[0]);
			this._wizard.getSteps()[0].setValidated(false);
			
			this.model = new JSONModel();
			this.model.setData({
				CardNameState:"None",
				Card: {
					Name: '',
					PublishDate: '',
					Template: '',
					CardParameters:[]
				}	
			});
			this.getView().setModel(this.model);
		},
		
		/*****************************************************
		 * Event Handling
		 *****************************************************/
		backToWizardContent : function () {
			this._oNavContainer.backToPage(this._oWizardContentPage.getId());
		},
		
		editStepOne : function () {
			this.handleNavigationToStep(0);
		},
		editStepTwo : function () {
			this.handleNavigationToStep(1);
		},
		editStepThree : function () {
			this.handleNavigationToStep(2);
		},
		editStepFour : function () {
			this.handleNavigationToStep(3);
		},
		
		onWizardCompleted: function () {
			this._oNavContainer.to(this._oWizardReviewPage);
		},
		
		onWizardSubmit : function () {
			var oBundle = this.getView().getModel("i18n").getResourceBundle();					
			var msg = oBundle.getText("Label.MessageBox", ['submit', 'Card?']);
			this.handleMessageBoxSubmit(msg, "confirm");
		},
		
		onWizardCancel : function () {
			var oBundle = this.getView().getModel("i18n").getResourceBundle();					
			var msg = oBundle.getText("Label.MessageBox", ['cancel','Card?']);
			this.handleMessageBoxOpen(msg, "warning");
		},
		
		onChangeTemplatePress: function(oEvent){
			var that = this;
			
			this.onJsonGet(
				"Templates", 
				function(mData){
					that.handleTemplateDialog(mData);
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
		
		/*****************************************************
		 * Business Logic
		 *****************************************************/		
		handleNavigationToStep : function (iStepNumber) {
			var that = this;
			function fnAfterNavigate () {
				that._wizard.goToStep(that._wizard.getSteps()[iStepNumber], true);
				that._oNavContainer.detachAfterNavigate(fnAfterNavigate);
			}

			this._oNavContainer.attachAfterNavigate(fnAfterNavigate);
			this.backToWizardContent();
		},
		
		handleMessageBoxOpen : function (sMessage, sMessageBoxType) {
			var that = this;
			MessageBox[sMessageBoxType](sMessage, {
				actions: [MessageBox.Action.YES, MessageBox.Action.NO],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.YES) {
						that.getRouter().navTo("card");
					}
				},
			});
		},
		
		handleMessageBoxSubmit : function (sMessage, sMessageBoxType) {
			var that = this;
			MessageBox[sMessageBoxType](sMessage, {
				actions: [MessageBox.Action.YES, MessageBox.Action.NO],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.YES) {
						that.doCreateCard();
					}
				},
			});
		},
		
		/**
		 * Dialog will open only on the successful return of JQuery -> Templates
		 * */
		handleTemplateDialog: function(mData){
			var that = this;
			var _mData = mData;
			
			var _templateDialog = new TemplateSelectDialog(new JSONModel(mData),
				function(oObject){
					that.getView().byId("CardTemplate").setValue(oObject.ID);
					that.getView().getModel().setProperty("/Card/Template", oObject);
					that.doUdateParameters();
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
					that.getView().getModel().setProperty("/Card/Channel", oObject);
					that.onChannelChangeValidate();
				}
			);	
			_channelDialog.open();
		},
		
		doCreateCard: function(){
			this._oDataCopy = jQuery.extend(true,{}, this.getView().getModel().getData());
			var _oModelCopy = new JSONModel(this._oDataCopy.Card);
			var that = this;
						
			_oModelCopy.setProperty("/Template@odata.bind", "Templates("+ this.getView().getModel().getProperty("/Card/Template").ID+")");
			delete _oModelCopy.getData()["Template"];
			
			_oModelCopy.setProperty("/Channel@odata.bind", "Channels("+ this.getView().getModel().getProperty("/Card/Channel").ID+")");
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

			var oBundle = this.getView().getModel("i18n").getResourceBundle();			
			var msg = oBundle.getText("Label.Create.success", [this._oDataCopy.Card.Name])
			
			this.onJsonPost(
				"Cards", 
				_oModelCopy.getData(),
				function(mData){
					that._oDataCopy = null;
					that._oModelCopy = null;
					that.getRouter().navTo("card");	
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
		 * As user selected a new Template the parameters has to be refreshed.
		 * */
		doUdateParameters: function(){
			var oView = this.getView();	
			var oModel = oView.getModel();
			var that = this;
			this.onJsonGet(
				"Templates("+oModel.getProperty("/Card/Template").ID+")/TemplateParameters", 
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
					oModel.setProperty("/Card/CardParameters", oCardParameters);
					that.onTemplateChangeValidate();
				}, 
				function(){
					oView.setBusy(false);
					router.getTargets().display(Nav_NotFound);
				}
			);			
		},
		
		
		enableTableControl : function(value) {
		    return !!value;
		},

        disableTableControl : function(value) {
		    return !value;
		},
		
		/******************************************************
		 * Validation
		 *****************************************************/
		onInfoChangeValidate : function () {
			var name = this.getView().byId("CardName").getValue();
			var publishDate = this.getView().byId("CardPublishDate").getValue();

			name.length==0 ?  this.model.setProperty("/CardNameState", "Error") : this.model.setProperty("/CardNameState", "None");
			name.length>50 ?  this.model.setProperty("/CardNameState", "Error") : this.model.setProperty("/CardNameState", "None");
			publishDate.length>250 ?  this.model.setProperty("/CardPublishDateState", "Error") : this.model.setProperty("/CardPublishDateState", "None");
			if (name.length==0 || name.length>49 || publishDate.length>250)
				this._wizard.invalidateStep(this.getView().byId("CardInfoStep"));
			else
				this._wizard.validateStep(this.getView().byId("CardInfoStep"));
		},
		
		onChannelChangeValidate: function(){
			var channel = this.getView().byId("CardChannel").getValue();
			channel.length==0 ?  this.model.setProperty("/CardChannelState", "Error") : this.model.setProperty("/CardChannelState", "None");
			
			if (channel.length==0)
				this._wizard.invalidateStep(this.getView().byId("CardChannelStep"));
			else
				this._wizard.validateStep(this.getView().byId("CardChannelStep"));
		},
		
		onTemplateChangeValidate: function(){
			var template = this.getView().byId("CardTemplate").getValue();
			template.length==0 ?  this.model.setProperty("/CardTemplateState", "Error") : this.model.setProperty("/CardTemplateState", "None");
			
			if (template.length==0)
				this._wizard.invalidateStep(this.getView().byId("CardTemplateStep"));
			else
				this._wizard.validateStep(this.getView().byId("CardTemplateStep"));
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
	
	return WizardController;
});