package au.web;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ODataServlet extends HttpServlet {

	private static final long serialVersionUID = 993870267037039415L;
	protected static final Logger log = LoggerFactory.getLogger(ODataServlet.class);
	private WebApplicationContext context = null;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doOData(req, resp);
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doOData(req, resp);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doOData(req, resp);
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doOData(req, resp);
	}
	
	private void doOData(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			CsdlEdmProvider csdlEdmProvider = (CsdlEdmProvider) context.getBean("GenericEdmProvider");
			Processor processor = (Processor) context.getBean("GenericEntityProcessor");
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(csdlEdmProvider, new ArrayList<EdmxReference>());
			ODataHttpHandler handler = odata.createHandler(edm);
			handler.register(processor);
			handler.process(req, resp);
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
