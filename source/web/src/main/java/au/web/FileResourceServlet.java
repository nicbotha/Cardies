package au.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.web.service.fileresource.FileService;

public class FileResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private WebApplicationContext context = null;
	protected static final Logger log = LoggerFactory.getLogger(FileResourceServlet.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			FileService fileService = (FileService) context.getBean("FileResourceService");
			fileService.doGet(request, response);
		} catch (RuntimeException e) {
			log.error("Server Error={}", e);
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			FileService fileService = (FileService) context.getBean("FileResourceService");
			fileService.doPost(request, response);
		} catch (RuntimeException e) {
			log.error("Server Error={}", e);
			throw new ServletException(e);
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
	}
}