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

        <form:form action="/miso/kitcomponent" method="POST" commandName="kitcomponent" autocomplete="off">

            <sessionConversation:insertSessionConversationId attributeName="kitComponent"/>
        </form:form>
        <form>
            <h1>Log a Kit Componenta

            </h1>

            Enter reference number: <input type="text" id="referenceNumber" onkeyup="getKitInfoByReferenceNumber();"/>
        </form>

            <p id="kitInfo"></p>


        <script type="text/javascript">
            //document.getElementById("referenceNumber").onkeyup = function() {getKitInfoByReferenceNumber()};
            // jQuery('#referenceNumber').keyup('getKitInfoByReferenceNumber');
            function getKitInfoByReferenceNumber(){

                // jQuery('#kitInfo').html("test");
                var referenceNumber = jQuery("#referenceNumber").val();

                Fluxion.doAjax(
                        'kitComponentControllerHelperService',
                        'getKitInfoByReferenceNumber',

                        {
                            'referenceNumber':referenceNumber,
                            'url': ajaxurl
                        },
                        {'doOnSuccess': function (json) {

                            //TODO: error checking

                                    var htmlStrResult = "Name: \t" + json.name + "<br>" +
                                            "Component: \t" + json.componentName + "<br>" +
                                            "Version: \t" + json.version + "<br>" +
                                            "Manufacturer: \t" + json.manufacturer + "<br>" +
                                            "Part Number: \t" + json.partNumber + "<br>" +
                                            "Type: \t" + json.kitType + "<br>" +
                                            "Platform: \t" + json.platformType + "<br>" +
                                            "Units: \t" + json.units + "<br>" +
                                            "Value: \t" + json.kitValue + "<br>";


                                    jQuery('#kitInfo').html(htmlStrResult);

                        }

                        });
            }

        </script>
    </div>
</div>

<%@ include file="adminsub.jsp" %>

<%@ include file="../footer.jsp" %>