sap.ui.define([
    "sap/ui/base/Object", 
    "sap/ui/model/json/JSONModel",
    "sap/m/Dialog",
    "sap/m/Button",
	"sap/m/Text"
], function(Object,JSONModel, Dialog,Button, Text) {
    "use strict";

    return Object.extend("cardies.adminapp.common.DeleteConfirmationDialog", {
    	
    	constructor: function(model, callback, object){
    		this._model = model;
    		this._callback = callback;
    		this._object = object;
    		this._initDialog();
    	},
    	
    	_initDialog: function(){
    		var that = this;  
			this._dialog = new Dialog({
				type: that._model.getProperty("/type"),			
				afterClose: function() {
					that._dialog.destroy();
				}
			});
			var oMessage = new Text({text: this._model.getProperty("/message")});
	        var oBeginButton = new Button({
	        	text: that._model.getProperty("/lblDelete"),
	        	press: function(){
	        		that._notifyController();
	        		that._dialog.close();
	        	}
	        });
	        var oEndButton = new Button({
	        	text: that._model.getProperty("/lblCancel"),
	        	press: function(){
	        		that._dialog.close();
	        	}
	        });
			
	        this._dialog.setTitle(this._model.getProperty("/lblTitle"));
	        this._dialog.addContent(oMessage);
	        this._dialog.setBeginButton(oBeginButton);
	        this._dialog.setEndButton(oEndButton);
    	},
    	
    	open: function(){
    		this._dialog.open();
    	},
    	
    	_notifyController: function(){
			this._callback(this._object);
    	}
    });
});