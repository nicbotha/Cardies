sap.ui.define([
    "sap/ui/base/Object", 
    "sap/ui/model/json/JSONModel",
	"sap/ui/comp/valuehelpdialog/ValueHelpDialog"
], function(Object,JSONModel, ValueHelpDialog) {
    "use strict";

    return Object.extend("cardies.adminapp.common.PersonSelectDialog", {
    	
    	constructor: function(model, callback){
    		this._model = model;
    		this._callback = callback;
			this._initDialog();
    	},
    	
    	_initDialog: function(){
    		var that = this;
    		
    		this.oValueHelpDialog = new ValueHelpDialog({
				title: "Contacts",
				supportMultiselect: false,
				supportRanges: false,
				supportRangesOnly: false, 
				key: "ID",				
				descriptionKey: "Name",
				stretch: sap.ui.Device.system.phone, 

				ok: function(oControlEvent) {
					that._notifyItemSelected(oControlEvent.getParameter("tokens"));
					that.oValueHelpDialog.close();
					
				},

				cancel: function(oControlEvent) {
					that.oValueHelpDialog.close();
				},

				afterClose: function() {
					that.oValueHelpDialog.destroy();
				}
			});
			
			var oColModel = new sap.ui.model.json.JSONModel();
			oColModel.setData({
				cols: [
				      	{label: "Code", template: "ID"},
				        {label: "Name", template: "Name"},
				        {label: "Email", template: "Email"}
				      ]
			});
			
			this.oValueHelpDialog.getTable().setModel(oColModel, "columns");
			this.oValueHelpDialog.getTable().setModel(that._model);
			
			if (this.oValueHelpDialog.getTable().bindRows) {
				this.oValueHelpDialog.getTable().bindRows("/value"); 
			}
			
			if (this.oValueHelpDialog.getTable().bindItems) { 
				var oTable = this.oValueHelpDialog.getTable();
				
				oTable.bindAggregation("items", "/", function(sId, oContext) { 
					var aCols = oTable.getModel("columns").getData().cols;
				
					return new sap.m.ColumnListItem({
						cells: aCols.map(function (column) {
							var colname = column.template;
							return new sap.m.Label({ text: "{" + colname + "}" });
						})
					});
				});
			}
    	},
    
    	open: function(){
    		this.oValueHelpDialog.open();
			this.oValueHelpDialog.update();
    	},
    	    	
    	_notifyItemSelected: function(oTokens){
    		var _key = oTokens[0].getKey();
    		var that = this;
    		this._model.getData().value.forEach(
				function(object){
					if(object.ID == _key){
						that._callback(object);
					}
				}
			);    		
    	}
    });
});