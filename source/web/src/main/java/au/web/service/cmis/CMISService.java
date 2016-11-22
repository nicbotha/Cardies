package au.web.service.cmis;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CMISService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMISService.class);

	@Autowired
	protected CMISSessionManager cmisSessionManager;

	/**
	 * Creates the connection to the repository using the specified values.
	 * 
	 * @param repositoryUserName
	 *            the repository user name
	 * @param repositoryPassword
	 *            the repository password
	 * @param repositoryURL
	 *            the repository URL
	 * @param repositoryId
	 *            the repository ID
	 */
	public CMISService() {

	}

	/**
	 * Create a new folder.
	 * 
	 * @param folderName
	 *            The folder name
	 * @param parentFolderId
	 *            The CMIS ID of the parent folder
	 */
	public final ObjectId createFolder(final String folderName, final String parentFolderId) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, folderName);
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		try {
			return cmisSessionManager.getSession().createFolder(properties, cmisSessionManager.getSession().getObject(parentFolderId));
		} catch (CmisContentAlreadyExistsException e) {
			CmisObject object = cmisSessionManager.getSession().getObject(cmisSessionManager.getSession().createObjectId(parentFolderId));
			if (!(object instanceof Folder)) {
				throw new IllegalArgumentException(parentFolderId + " is not a folder");
			}
			Folder folder = (Folder) object;
			for (CmisObject o : folder.getChildren()) {
				if (o.getName().equals(folderName)) {
					return cmisSessionManager.getSession().createObjectId(o.getId());
				}
			}
			return null;
		}
	}

	/**
	 * For each folder in the given folder path, creates it if necessary. This
	 * implementation checks that the folder exists, and if not creates it.
	 * 
	 * @param folderPath
	 *            Folder structure to create
	 * 
	 * @return the last folder in the structure
	 */
	public final CmisObject createFolderStructure(final String folderPath) {
		String[] folderNames = folderPath.split("/");
		String currentObjectId = cmisSessionManager.getSession().getObjectByPath("/").getId();
		String currentPath = "/";
		for (String folder : folderNames) {
			currentPath = currentPath + folder + "/";
			CmisObject currentObject = getObjectByPath(currentPath);
			currentObjectId = currentObject != null ? currentObject.getId() : createFolder(folder, currentObjectId).getId();
		}
		return cmisSessionManager.getSession().getObject(currentObjectId);
	}

	/**
	 * Retrieves a folder from the repository.
	 * 
	 * @param folderPath
	 *            The path of the folder to retrieve
	 * @return The retrieved folder {@link Folder}
	 */
	public final Folder getFolderByPath(final String folderPath) {

		return (Folder) getObjectByPath(folderPath);
	}

	/**
	 * Retrieve an object from the repository by Id.
	 * 
	 * @param nodeId
	 *            The object's node identity in the repository
	 * @return The requested object
	 */
	public final CmisObject getObjectById(final String nodeId) {
		try {
			return cmisSessionManager.getSession().getObject(nodeId);
		} catch (CmisObjectNotFoundException e) {
			return null;
		}
	}

	/**
	 * Retrieve an object from the repository by path.
	 * 
	 * @param path
	 *            The folder path to the object in the repository
	 * @return The requested object
	 */
	public final CmisObject getObjectByPath(final String path) {
		try {
			return cmisSessionManager.getSession().getObjectByPath(path);
		} catch (CmisObjectNotFoundException e) {
			return null;
		} catch (CmisInvalidArgumentException e) {
			return null;
		} catch (CmisRuntimeException e) {
			LOGGER.info("getObjectByPath: Path to object is empty");
			return null;
		}
	}

	/**
	 * Retrieve a template from a given location.
	 * 
	 * @param path
	 *            The path to the template document
	 * @return The template document
	 */
	public final Document getTemplate(final String path) {

		try {
			Document template = (Document) cmisSessionManager.getSession().getObjectByPath(path);
			return template;
		} catch (CmisObjectNotFoundException e) {
			return null;
		}
	}

	/**
	 * Creates a document in the repository.
	 * 
	 * @param folderPath
	 *            The path to the folder in which to put the file
	 * @param fileContent
	 *            The {@link InputStream} for the file content
	 * @param mimeType
	 *            The mimeType of the file
	 * @param fileName
	 *            The filename to give to the file in the repository
	 * @return The Id of the created document
	 */
	public final String createDocumentByFolderPath(final String folderPath, final byte[] content, final String mimeType, final String fileName) {

		Folder folder = getFolderByPath(folderPath);

		return createDocumentByFolderId(folder.getId(), content, mimeType, fileName);
	}

	/**
	 * Creates a document in the repository.
	 * 
	 * @param folderId
	 *            The Id of the folder in which to put the file
	 * @param fileContent
	 *            The {@link InputStream} for the file content
	 * @param mimeType
	 *            The mimeType of the file
	 * @param fileName
	 *            The filename to give to the file in the repository
	 * @return The Id of the created document
	 */
	public final String createDocumentByFolderId(final String folderId, final byte[] content, final String mimeType, final String fileName) {
		// Insert new document
		ObjectId createdObjectId = null;
		try {
			Folder folder = (Folder) cmisSessionManager.getSession().getObject(folderId);

			Map<String, String> properties = new HashMap<String, String>();
			properties.put(PropertyIds.NAME, fileName);
			properties.put(PropertyIds.CHECKIN_COMMENT, "");
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			InputStream stream = new ByteArrayInputStream(content);
			ContentStream contentStream = this.cmisSessionManager.getSession().getObjectFactory().createContentStream(fileName, content.length, "application/octet-stream", stream);
			createdObjectId = cmisSessionManager.getSession().createDocument(properties, cmisSessionManager.getSession().createObjectId(folder.getId()), contentStream, VersioningState.MAJOR, null, null, null);

		} catch (CmisBaseException e) {
			LOGGER.info("CBE: " + e.getErrorContent());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return createdObjectId.getId();
	}

	/**
	 * Delete a folder by path.
	 * 
	 * @param folderPath
	 *            The path to the folder to delete
	 */
	public final void deleteFolder(final String folderPath) {
		try {
			Folder folder = getFolderByPath(folderPath);
			folder.deleteTree(true, null, true);

		} catch (CmisBaseException e) {
			LOGGER.info("CBE: " + e.getErrorContent());
			e.printStackTrace();
		}
	}

	/**
	 * Delete a document by name.
	 * 
	 * @param docName
	 *            The name of the document to delete
	 * @param parentFolderId
	 *            The Id to the parent folder of the document
	 */
	public final void deleteDocument(final String docName, final String parentFolderId) {
		try {
			Document doc;
			Folder folder = (Folder) getObjectById(parentFolderId);
			for (CmisObject object : folder.getChildren()) {
				if (object.getName().equals(docName)) {
					doc = (Document) object;
					doc.delete(false);
				}
			}
		} catch (CmisBaseException e) {
			LOGGER.info("CBE: " + e.getErrorContent());
			e.printStackTrace();
		}
	}
	
	public final void deleteDocument(final String documentId) {
			cmisSessionManager.getSession().delete(cmisSessionManager.getSession().createObjectId(documentId));
	}

	/**
	 * Delete a node.
	 * 
	 * @param nodeId
	 *            The Id of the object to delete
	 */
	public final void deleteNode(final String nodeId) {

		try {
			CmisObject object = cmisSessionManager.getSession().getObject(nodeId);

			if (object.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
				Document doc = (Document) object;
				doc.delete(false);
			} else {
				Folder folder = (Folder) object;
				folder.deleteTree(true, null, true);
			}

		} catch (CmisBaseException e) {
			LOGGER.info("CBE: " + e.getErrorContent());
			e.printStackTrace();
		}
	}
}
