<%@ include file="/bcid/header.jsp" %>
<div class="section">
    <div class="sectioncontent">
        <table>
            <tr>
                <td align="right">Request that failed:</td>
                <td>${errorInfo.getRequestURI()}</td>
            </tr>

            <tr>
                <td align="right">Status Code:</td>
                <td>${errorInfo.getStatusCode()}</td>
            </tr>

            <tr>
                <td align="right">Exception:</td>
                <td>${errorInfo.getThrowable()}</td>
            </tr>

            <tr>
                <td align="right">Message:</td>
                <td>${errorInfo.getMessage()}</td>
            </tr>
            <c:remove var="errorInfo" scope="session" />
        </table>
    </div>
</div>

<script>
</script>
<%@ include file="/bcid/footer.jsp" %>