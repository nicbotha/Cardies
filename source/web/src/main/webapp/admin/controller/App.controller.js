sap.ui.define([
	"cardies/adminapp/controller/BaseController"
], function (BaseController) {
	"use strict";

	return BaseController.extend("cardies.adminapp.controller.App", {
		model : new sap.ui.model.json.JSONModel(),
		data : {
			navigation: [
				{
					title: 'Home',
					icon: 'sap-icon://home',
					key: 'home'
				},{
					title: 'File Resources',
					icon: 'sap-icon://document',
					expanded: false,
					key: 'fileresourceList'
				},{
					title: 'Templates',
					icon: 'sap-icon://attachment-html',
					expanded: false,
					key: 'template'
				},{
					title: 'Cards',
					icon: 'sap-icon://card',
					expanded: false,
					key: 'card'
				}
			],
			fixedNavigation: [{
				title: 'Profile',
				icon: 'sap-icon://employee'
			}]
		},
		
		onInit: function () {
			jQuery.sap.log.setLevel(jQuery.sap.log.Level.INFO);
			
			this.model.setData(this.data);
			this.getView().setModel(this.model);

			var oRouter = this.getRouter();

			oRouter.attachBypassed(function (oEvent) {
				var sHash = oEvent.getParameter("hash");
				jQuery.sap.log.info("Sorry, but the hash '" + sHash + "' is invalid.", "The resource was not found.");
			});

			oRouter.attachRouteMatched(function (oEvent){
				var sRouteName = oEvent.getParameter("name");
				jQuery.sap.log.info("User accessed route " + sRouteName + ", timestamp = " + new Date().getTime());
			});
			
			this.getView().byId("sideNavigation").setExpanded(false);
		},
		
		onItemSelect : function(oEvent) {
			this.getView().byId("sideNavigation").setExpanded(false);
			var item = oEvent.getParameter('item');
			this.getRouter().navTo(item.getKey());
		},
		
		onSideNavigation: function (event) {
			var sideNavigation = this.getView().byId("sideNavigation");
			sideNavigation.setExpanded(!sideNavigation.getExpanded());
		}

	});

});