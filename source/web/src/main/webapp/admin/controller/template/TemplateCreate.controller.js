sap.ui.define([
	"cardies/adminapp/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/comp/valuehelpdialog/ValueHelpDialog",
	"sap/m/MessageBox",
	"sap/m/MessageToast",
	"cardies/adminapp/common/FileResourceSelectDialog"
], function (BaseController, JSONModel, ValueHelpDialog, MessageBox, MessageToast, FileResourceSelectDialog) {
	"use strict";
	
	return BaseController.extend("cardies.adminapp.controller.template.TemplateCreate", {
		onInit: function () {
			var oRouter = this.getRouter();
			oRouter.getRoute("templateCreate").attachMatched(this._onRouteMatched, this);
			
			this._wizard = this.getView().byId("CreateTemplateWizard");
			this._oNavContainer = this.getView().byId("wizardNavContainer");
			this._oWizardContentPage = this.getView().byId("wizardContentPage");
			this._oWizardReviewPage = sap.ui.xmlfragment("cardies.adminapp.view.template.TemplateCreateReviewStep", this);
			this._oNavContainer.addPage(this._oWizardReviewPage);
		}, 
		
		_onRouteMatched : function (oEvent) {
			this._handleNavigationToStep(0);
			this._wizard.discardProgress(this._wizard.getSteps()[0]);
			this._wizard.getSteps()[0].setValidated(false);
			
			this.model = new JSONModel();
			this.model.setData({
				TemplateNameState:"None",
				Template: {
					Name: '',
					Description: '',
					FileResourceId: '',
					TemplateParameters:[]
				}	
			});
			this.getView().setModel(this.model);
					
			var templateParameterModel= new JSONModel();
			templateParameterModel.setData({
				"Type":"TEXT"
			});
			this.getView().setModel(templateParameterModel, "TemplateParameterModel");
			
		},
		
		/*****************************************************
		 * Navigation
		 *****************************************************/
		backToWizardContent : function () {
			this._oNavContainer.backToPage(this._oWizardContentPage.getId());
		},
		
		editStepOne : function () {
			this._handleNavigationToStep(0);
		},
		editStepTwo : function () {
			this._handleNavigationToStep(1);
		},
		editStepThree : function () {
			this._handleNavigationToStep(2);
		},
		
		wizardCompletedHandler : function () {
			this._oNavContainer.to(this._oWizardReviewPage);
		},
		
		handleWizardSubmit : function () {
			var oBundle = this.getView().getModel("i18n").getResourceBundle();					
			var msg = oBundle.getText("Label.MessageBox", ['submit', 'Template?']);
			this._handleMessageBoxSubmit(msg, "confirm");
		},
		
		_handleNavigationToStep : function (iStepNumber) {
			var that = this;
			function fnAfterNavigate () {
				that._wizard.goToStep(that._wizard.getSteps()[iStepNumber], true);
				that._oNavContainer.detachAfterNavigate(fnAfterNavigate);
			}

			this._oNavContainer.attachAfterNavigate(fnAfterNavigate);
			this.backToWizardContent();
		},
		
		_handleMessageBoxOpen : function (sMessage, sMessageBoxType) {
			var that = this;
			MessageBox[sMessageBoxType](sMessage, {
				actions: [MessageBox.Action.YES, MessageBox.Action.NO],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.YES) {
						that.getRouter().navTo("template");
					}
				},
			});
		},
		
		_handleMessageBoxSubmit : function (sMessage, sMessageBoxType) {
			var that = this;
			MessageBox[sMessageBoxType](sMessage, {
				actions: [MessageBox.Action.YES, MessageBox.Action.NO],
				onClose: function (oAction) {
					if (oAction === MessageBox.Action.YES) {
						that._handleTemplateCreate();
					}
				},
			});
		},
		
		_handleTemplateCreate: function(){
			var oData = this.getView().getModel().getData();
			var template = oData.Template;
			var that = this;
			
			var templateOData = {
				"Name": template.Name,
				"Description": template.Description,
				"FileResource@odata.bind": "FileResources("+template.FileResourceId+")",
				"TemplateParameters":[]
			};
			
			template.TemplateParameters.forEach(pushTemplateParam);
			function pushTemplateParam(item, index){
				if(item.hasOwnProperty("Name") && item.Name !== null){
					var templateParameter = {
						"Name": item.Name,
						"Description": item.Description,
						"DefaultValues": item.DefaultValues,
						"Type": item.Type
					};
					templateOData.TemplateParameters.push(templateParameter);
				}
			}
			
			var oBundle = this.getView().getModel("i18n").getResourceBundle();			
			var msg = oBundle.getText("Label.Create.success", [templateOData.Name])
			
			this.onJsonPost(
				"Templates", 
				templateOData,
				function(mData){
					MessageToast.show(msg);
					that.getRouter().navTo("template");	
				}, 
				function(){
					this.getRouter().getTargets().display("notFound", {
						fromTarget : "templateCreate"
					});
				}
			)
		},
		
		handleWizardCancel : function () {
			var oBundle = this.getView().getModel("i18n").getResourceBundle();					
			var msg = oBundle.getText("Label.MessageBox", ['cancel','Template?']);
			this._handleMessageBoxOpen(msg, "warning");
		},
	
		/**
		 * Validate Template Information Step
		 * */
		templateInfoValidation : function () {
			var name = this.getView().byId("TemplateName").getValue();
			var description = this.getView().byId("TemplateDescription").getValue();

			name.length==0 ?  this.model.setProperty("/TemplateNameState", "Error") : this.model.setProperty("/TemplateNameState", "None");
			name.length>50 ?  this.model.setProperty("/TemplateNameState", "Error") : this.model.setProperty("/TemplateNameState", "None");
			description.length>250 ?  this.model.setProperty("/TemplateDescriptionState", "Error") : this.model.setProperty("/TemplateDescriptionState", "None");
			if (name.length==0 || name.length>49 || description.length>250)
				this._wizard.invalidateStep(this.getView().byId("TemplateInfoStep"));
			else
				this._wizard.validateStep(this.getView().byId("TemplateInfoStep"));
		},
		
		
		/******************************************************
		 * Wizard Step - 2 Code to handle File resource.
		 *****************************************************/
		/**
		 * Validate Template File resource Step
		 * */		
		templateFileResourceValidation: function(){
			var fileresource = this.getView().byId("TemplateFileResource").getValue();
			fileresource.length==0 ?  this.model.setProperty("/TemplateFileResourceState", "Error") : this.model.setProperty("/TemplateFileResourceState", "None");
			
			if (fileresource.length==0)
				this._wizard.invalidateStep(this.getView().byId("TemplateFileResourceStep"));
			else
				this._wizard.validateStep(this.getView().byId("TemplateFileResourceStep"));
		},
		
		/**
		 * Event triggered when users clicks on wizard input.
		 * - Open dialog with all HTML FileResource entities;
		 * - Allow user to select one;
		 * - Close dialog and update model;
		 * */
		handleFileResourceRequest: function(oEvent){
			var that = this;
			
			this.onJsonGet(
				"FileResources", 
				function(mData){
					that.openFileResourceDialog(mData);
				}, 
				function(){
					sap.m.MessageToast.show("Error occured.");
				}
			);
		},
		
		/**
		 * Dialog will open only on the successful return of JQuery -> FileResources
		 * */
		openFileResourceDialog: function(mData){
			var that = this;
			
			var _fileResourceDialog = new FileResourceSelectDialog(new JSONModel(mData),
				function(oObject){
					that.getView().byId("TemplateFileResource").setValue(oObject.Name);
					that.getView().getModel().getData().Template.FileResourceId = oObject.ID;
					that.templateFileResourceValidation();
				}
			);	
			
			_fileResourceDialog.open();
			
		},
		
		/******************************************************
		 * Wizard Step - 3 Code to handle Template parameter.
		 *****************************************************/
		addEmptyParameterObject : function() {
		    var oModel = this.getView().getModel();
		    var aData  = oModel.getData();

		    var emptyObject = { 
		    		Name: null,
				      Description: null, 
				      Type: 'TEXT',
				      DefaultValues: null,
				      createNew: false,
				      removeNew: true,
				      saveNew: true
		    };

		    aData.Template.TemplateParameters.push(emptyObject);
		    oModel.setData(aData);
		},
		
		enableTableControl : function(value) {
		    return !!value;
		},

        disableTableControl : function(value) {
		    return !value;
		},
		
		addParameterEntry : function(oEvent) {
			this.addEmptyParameterObject();
		},

		saveParameterEntry : function(oEvent) {
		    var path = oEvent.getSource().getBindingContext().getPath();
		    var obj = oEvent.getSource().getBindingContext().getObject();
			    
		    if(this._validateParameter(obj)){
		    	var oModel = this.getView().getModel();
		    	obj.saveNew = false;
			    obj.removeNew = true;	
			    oModel.setProperty(path, obj);
	    	}else{
	    		var oBundle = this.getView().getModel("i18n").getResourceBundle();					
				var msg = oBundle.getText("Label.Save.failvalidation", 'Parameter');
				var bCompact = !!this.getView().$().closest(".sapUiSizeCompact").length;
				MessageBox.error(
					msg,
					{
						styleClass: bCompact? "sapUiSizeCompact" : ""
					}
				);
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
	    },
	    
	    removeParameterEntry: function(oEvent){
	    	var path = oEvent.getSource().getBindingContext().getPath();
		    var obj = oEvent.getSource().getBindingContext().getObject();
		    var oModel = this.getView().getModel();
		    var oData = oModel.getData();
		    
		    var index = parseInt(path.substring(path.lastIndexOf('/') + 1));
		    oData.Template.TemplateParameters.splice(index,1);
		    oModel.setData(oData);
			
		    var row = oModel.getProperty(path, path);
	    }
		  
	});
	
	return WizardController;
});