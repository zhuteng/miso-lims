<%--
  ~ Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
  ~ MISO project contacts: Robert Davey, Mario Caccamo @ TGAC
  ~ **********************************************************************
  ~
  ~ This file is part of MISO.
  ~
  ~ MISO is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ MISO is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with MISO.  If not, see <http://www.gnu.org/licenses/>.
  ~
  ~ **********************************************************************
  --%>
<%@ include file="../header.jsp" %>
<div id="maincontent">
    <div id="contentcolumn">


        <form:form action="/miso/kitcomponentdescriptor" method="POST" commandName="kitComponentDescriptor"
                   autocomplete="off">
            <sessionConversation:insertSessionConversationId attributeName="kitComponentDescriptor"/>
            <h1>Kit ID: ${kitDescriptor.id}</h1>

            <h2>Information</h2>
            <table class="in">
                <tr>
                    <td class="h">ID:</td>
                    <td>${kitDescriptor.id}</td>
                </tr>
                <tr>
                    <td class="h">Name:</td>
                    <td>${kitDescriptor.name}</td>
                </tr>
                <tr>
                    <td class="h">Version:</td>
                    <td>${kitDescriptor.version}</td>
                </tr>
                <tr>
                    <td class="h">Manufacturer:</td>
                    <td>${kitDescriptor.manufacturer}</td>
                </tr>
                <tr>
                    <td class="h">Part Number:</td>
                    <td>${kitDescriptor.partNumber}</td>
                </tr>
                <tr>
                    <td class="h">Units:</td>
                    <td>${kitDescriptor.units}</td>
                </tr>
                <td class="h">Value:</td>
                <td>${kitDescriptor.kitValue}</td>
                </tr>
                <tr>
                    <td class="h">Type:</td>
                    <td>${kitDescriptor.kitType}</td>
                </tr>
                <tr>
                    <td class="h">Platform:</td>
                    <td>${kitDescriptor.platformType}</td>
                </tr>
            </table>

            <h1>Components</h1>
            <c:forEach items="${kitComponentDescriptors}" var="component">
                <table class="in">
                    <tr>
                        <td class="h">Name:</td>
                        <td>${component.name}</td>
                    </tr>
                    <tr>
                        <td class="h">Reference Number:</td>
                        <td>${component.referenceNumber}</td>
                    </tr>
                </table>
                <br>
            </c:forEach>


            <h2>Add a new component</h2>
            <table class="in">
                <tr>
                    <td class="h">Name:</td>
                    <td><form:input path="name"/></td>
                </tr>

                <tr>
                    <td class="h">Reference Number:</td>
                    <td><form:input path="referenceNumber"/></td>
                </tr>


            </table>

            <button type="submit" class="fg-button ui-state-default ui-corner-all">Add</button>
        </form:form>
        <br>
        <button type="button" id="done">Save and return to Home</button>
    </div>
</div>

<script>

    jQuery("#done").click(function(){
        //redirect to Home
        jQuery(location).attr('href', '/miso/mainMenu');
    })
</script>

<%@ include file="adminsub.jsp" %>

<%@ include file="../footer.jsp" %>