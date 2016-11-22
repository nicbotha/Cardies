package au.web;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.web.service.template.TemplateService;

public class TemplateServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private WebApplicationContext context = null;
	protected static final Logger log = LoggerFactory.getLogger(TemplateServlet.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			TemplateService templateService = (TemplateService) context.getBean("TemplateService");
			byte[] bytes = templateService.findOneById(request.getParameter("ID"));

			try (OutputStream output = response.getOutputStream()) {

				response.setHeader("Content-Type", "text/html");
				output.write(bytes, 0, bytes.length);

			} catch (IOException e) {
				log.error("Could not create document.", e);
			}
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
