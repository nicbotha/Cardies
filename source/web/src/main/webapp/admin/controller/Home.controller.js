sap.ui.define([
	"cardies/adminapp/controller/BaseController"
], function (BaseController) {
	"use strict";

	return BaseController.extend("cardies.adminapp.controller.Home", {

		onInit: function () {

		},
		
		onDisplayNotFound : function (oEvent) {
			// display the "notFound" target without changing the hash
			this.getRouter().getTargets().display("notFound", {
				fromTarget : "home"
			});
		},
					
		onFileResourcePress: function(){
			this.getRouter().navTo("fileresourceList");
		},
		
		onTemplatePress: function(){
			this.getRouter().navTo("template");
		},
		
		onCardPress: function(){
			this.getRouter().navTo("card");
		}

	});

});