<%@ page import="Process.*" %>
<html>
<body>
	<h2>Converter</h2>
	<%
		Processing pr = new Processing();
		pr.checkSQS("SQS");
	%>
</body>
</html>
