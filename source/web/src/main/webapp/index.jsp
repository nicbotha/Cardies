<%@page
	import="javax.naming.InitialContext,javax.naming.Context,com.sap.cloud.account.TenantContext"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>SAP HCP - Tenant Context Demo Application</title>
</head>

<body>
	<h2>Welcome to the SAP HANA Cloud Platform</h2>
	<br></br>

	<%
		try {
			InitialContext ctx = new InitialContext();
			Context envCtx = (Context) ctx.lookup("java:comp/env");
			TenantContext tenantContext = (TenantContext) envCtx.lookup("TenantContext");
			String currentTenantId = tenantContext.getTenant().getId();
			out.println("<p><font size=\"5\"> The application was accessed on behalf of a tenant with an ID: <b>" + currentTenantId + "</b></font></p>");
		} catch (Exception e) {
			out.println("error at client");
		}
	%>

</body>
</html>