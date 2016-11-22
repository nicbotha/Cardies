jQuery.sap.declare("cardies.adminapp.common.AjaxUtil");

cardies.adminapp.common.AjaxUtil = {
		
	getJSon : function(thiz, entity, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback) {
		var path = "/web/odata/"
		return jQuery.ajax({
			url : path + entity,
			type : "GET",
			dataType : "json",
			context : thiz
		}).success(fnSuccessCallback).error(fnErrorCallback).always(fnAlwaysCallback);
	},
	
	postJSon : function(thiz, entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback) {
		this.doJSon(thiz, "POST",entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback);
	},
	
	putJSon : function(thiz, entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback) {
		this.doJSon(thiz, "PUT",entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback);
	},	

	doJSon : function(thiz, typz,entity, oData, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback) {
		var path = "/web/odata/"
		return jQuery.ajax({
			url : path + entity,
			type : typz,
			dataType : "json",
			contentType : "application/json; charset=utf-8",
			data : JSON.stringify(oData),
			context : thiz
		}).success(fnSuccessCallback).error(fnErrorCallback).always(fnAlwaysCallback);
	},

	deleteJson : function(thiz, entity, fnSuccessCallback, fnErrorCallback, fnAlwaysCallback) {
		var path = "/web/odata/"
		return jQuery.ajax({
			url : path+entity,
			type : "DELETE",
			context : thiz
		}).success(fnSuccessCallback).error(fnErrorCallback).always(fnAlwaysCallback);
	},
};
