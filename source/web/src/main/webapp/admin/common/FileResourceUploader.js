sap.ui.define([ "sap/ui/unified/FileUploader", "cardies/adminapp/common/FileResourceUploader" ], 
		
	function(FileUploader, FileResourceUploader) {
	"use strict";

	return FileUploader.extend("cardies.adminapp.common.FileResourceUploader", {

		renderer: {},
	
		upload: function(){
			var file = jQuery.sap.domById(this.getId() + "-fu").files[0];
			try {
				if (file) {
					this._bUploading = true;
					var that = this;
					var _handleSuccess = function(data, textStatus, jqXHR){
						that.fireUploadComplete(jqXHR);
						that._bUploading = false;
					};
					
					var _handleError = function(data){
                        var errorMsg = "";
                        if (data.responseText[1]){
                        	errorMsg = /<message>(.*?)<\/message>/.exec(data.responseText)[1];
                        }else{
                        	errorMsg = "Something bad happened";
                        }
                        
                        that.fireUploadComplete({"response": "Error: " + errorMsg});
    					that._bUploading = false;  
					};

					var oHeaders = {
						"Content-Type": file.type,
						"Content-FileName": file.name
					};
					
					
					jQuery.ajax({
						type: "POST",
						url: this.getUploadUrl(),
						headers: oHeaders,
						cache: false,
						contentType: false,
						processData: false,
						data: file,
						success: _handleSuccess,
						error: _handleError
					});
					
					
					jQuery.sap.log.info("File uploading to " + this.getUploadUrl());
					
					
				}; // file				
			}catch(oException){
				jQuery.sap.log.error("File upload failed:\n" + oException.message);
			}
		}
	});

});
