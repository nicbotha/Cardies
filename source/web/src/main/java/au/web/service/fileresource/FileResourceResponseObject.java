package au.web.service.fileresource;

public class FileResourceResponseObject {

	private String DocStoreId;
	private String DocStorePreviewId;
	private String ContentType;
	private String FileName;
	
	public String getContentType() {
		return ContentType;
	}
	public void setContentType(String contentType) {
		ContentType = contentType;
	}
	public String getDocStoreId() {
		return DocStoreId;
	}
	public void setDocStoreId(String docStoreId) {
		DocStoreId = docStoreId;
	}
	public String getDocStorePreviewId() {
		return DocStorePreviewId;
	}
	public void setDocStorePreviewId(String docStorePreviewId) {
		DocStorePreviewId = docStorePreviewId;
	}
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
}
