sap.ui.define([ "sap/ui/core/UIComponent",
            	"sap/ui/model/odata/v2/ODataModel" ], function(UIComponent, ODataModel) {
	"use strict";

	return UIComponent.extend("cardies.adminapp.Component", {

		metadata : {
			manifest : "json"
		},

		init : function() {
			// call the init function of the parent

			UIComponent.prototype.init.apply(this, arguments);
			this.getRouter().initialize();
			this._initModels();
			// additional initialization can be done here
			// this.getModel().setUseBatch(false);

		},

		_initModels : function() {
			
		}

	});
});