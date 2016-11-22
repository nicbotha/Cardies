package au.web.service.fileresource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.web.service.cmis.CMISService;

public class FileResourceService implements FileService {

	protected final Logger log = LoggerFactory.getLogger(FileResourceService.class);

	@Autowired
	protected CMISService cmisService;
	
	protected String rootFolderPath;
	
	protected String resourceFolderPath;

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		ObjectMapper objectMapper = new ObjectMapper();

		byte[] content;
		try {
			content = IOUtils.toByteArray(request.getInputStream());
		} catch (IOException e) {
			log.error("Error while reading input stream.", e);
			return;
		}
		Document document = create(content);

		FileResourceResponseObject responseObj = new FileResourceResponseObject();
		responseObj.setDocStoreId(document.getId());
		responseObj.setDocStorePreviewId(document.getId());
		responseObj.setContentType(document.getContentStreamMimeType());
		responseObj.setFileName(request.getHeader("Content-FileName"));

		String responseStr;
		try {
			responseStr = objectMapper.writeValueAsString(responseObj);
			response.getWriter().write(responseStr);
		} catch (JsonProcessingException e) {
			log.error("JSon could not parse response to String", e);
		} catch (IOException e) {
			log.error("Error occured while sending response.", e);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {

		String id = request.getParameter("ID");

		try (OutputStream output = response.getOutputStream()) {

			Document document = (Document) this.cmisService.getObjectById(id);
			InputStream stream = document.getContentStream().getStream();
			response.setHeader("Content-Type", document.getContentStreamMimeType());

			byte[] buffer = new byte[10240];

			for (int length = 0; (length = stream.read(buffer)) > 0;) {
				output.write(buffer, 0, length);
			}

		} catch (IOException e) {
			log.error("Could not create document.", e);
		}
	}

	@Override
	public Document create(byte[] content) {
		Folder folder = getDocumentFolder();
		return createDocument(folder, content, UUID.randomUUID().toString());
	}

	@Override
	public void delete(String identifier) {
		this.cmisService.deleteDocument(identifier);
	}

	protected Document createDocument(Folder folder, byte[] content, String documentname) {
		String documentId = this.cmisService.createDocumentByFolderPath(getFolderPath(), content, "UTF-8", documentname);

		return (Document) this.cmisService.getObjectById(documentId);
	}

	protected Folder getDocumentFolder() {
		Folder folder = this.cmisService.getFolderByPath(getFolderPath());

		if (folder == null) {
			try {
				Folder parentFolder = getRootFolder();
				this.cmisService.createFolder(getResourceFolderPath(), parentFolder.getId());
				folder = getDocumentFolder();
			} catch (CmisNameConstraintViolationException e) {
				// Folder exists already, nothing to do
			}
		}
		return folder;
	}

	private Folder getRootFolder() {
		Folder parentFolder = this.cmisService.getFolderByPath(getRootFolderPath());
		if (parentFolder == null) {
			this.cmisService.createFolderStructure(getRootFolderPath());
			parentFolder = getRootFolder();
		}
		return parentFolder;
	}

	private String getFolderPath() {
		return getRootFolderPath().concat("/").concat(getResourceFolderPath());
	}

	public String getRootFolderPath() {
		return rootFolderPath;
	}

	public void setRootFolderPath(String rootFolderPath) {
		this.rootFolderPath = rootFolderPath;
	}

	public String getResourceFolderPath() {
		return resourceFolderPath;
	}

	public void setResourceFolderPath(String resourceFolderPath) {
		this.resourceFolderPath = resourceFolderPath;
	}

	

	

}
