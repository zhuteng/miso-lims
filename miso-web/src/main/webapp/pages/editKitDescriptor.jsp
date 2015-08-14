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
        <form:form action="/miso/kitdescriptor" method="POST" commandName="kitDescriptor" autocomplete="off">

            <sessionConversation:insertSessionConversationId attributeName="kitDescriptor"/>

            <h1>New Kit
                <button type="submit" class="fg-button ui-state-default ui-corner-all">Save</button>
            </h1>
            <h2>Information</h2>
            <table class="in">
                <tr>
                    <td class="h">Name:</td>
                    <td><form:input path="name"/></td>
                </tr>
                <tr>
                    <td class="h">Version:</td>
                    <td><form:input path="version"/></td>
                </tr>
                <tr>
                    <td class="h">Manufacturer:</td>
                    <td><form:input path="manufacturer"/></td>
                </tr>
                <tr>
                    <td class="h">Part Number:</td>
                    <td><form:input path="partNumber"/></td>
                </tr>
                <tr>
                    <td class="h">Units:</td>
                    <td><form:input path="units"/></td>
                </tr>
                <tr>
                    <td class="h">Value:</td>
                    <td><form:input path="kitValue"/></td>
                </tr>
                <tr>
                    <td>Type:</td>
                    <td>
                        <form:select id="kitTypes" path="kitType" items="${kitTypes}"/>
                    </td>
                </tr>
                <tr>
                    <td>Platform:</td>
                    <td>
                        <form:select id="platformTypes" path="platformType" items="${platformTypes}"/>
                    </td>
                </tr>
            </table>

            <button type="submit" class="fg-button ui-state-default ui-corner-all">Save and proceed to add components
            </button>
        </form:form>


    </div>
</div>

<%@ include file="adminsub.jsp" %>

<%@ include file="../footer.jsp" %>