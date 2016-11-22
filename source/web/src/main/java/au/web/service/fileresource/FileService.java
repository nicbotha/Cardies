package au.web.service.fileresource;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.Document;

public interface FileService {
	
	public void doPost(HttpServletRequest request, HttpServletResponse response);
	
	public void doGet(HttpServletRequest request, HttpServletResponse response);
	
	public Document create(byte[] content) throws NamingException;
	
	public void delete(String identifier) throws NamingException;
}
