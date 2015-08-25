<%--
  Created by IntelliJ IDEA.
  User: zakm
  Date: 20/08/2015
  Time: 12:50
  To change this template use File | Settings | File Templates.
--%>



<%@ include file="../header.jsp" %>



<div id="maincontent">
  <div id="contentcolumn">

    <h1>Kits</h1>
    <h2>Visibility</h2>
    <form name="showMore" id="showMoreForm">

      <table>

        <tr>
          <th colspan="2">Show additional fields:</th>
        </tr>
        <tr>
          <td>
            <input type="checkbox" name="showMore" value="3"> Version
          </td>
          <td>
            <input type="checkbox" name="showMore" value="5"> Part Number
          </td>
          <td>
            <input type="checkbox" name="showMore" value="6"> Type
          </td>
          <td>9
            <input type="checkbox" name="showMore" value="7"> Platform
          </td>
          <td>
            <input type="checkbox" name="showMore" value="8"> Units
          </td>
          <td>
            <input type="checkbox" name="showMore" value="9"> Value
          </td>
          <td>
            <input type="checkbox" name="showMore" value="10"> Reference Number
          </td>
        </tr>v
        <tr>
          <td>
            <button type="button" id="showMoreButton">Apply</button>
          </td>
        </tr>

        <tr>
          <th>
            Exhausted:
          </th>
        </tr>
        <tr>
          <td>
            <button type="button" id='exhaustedVisibilityButton'>Show/Hide</button>
          </td>
        </tr>
        <tr>
          <th>
            Show only:
          </th>
        </tr>
        <tr>
          <td>
            <select id="selectExpiryDisplay">
              <option selected="selected" value='-1'>Show all</option>
              <option value='0'>Expired</option>
              <option value='1'>Soon to expire</option>
            </select>
          </td>
          <td>
            <button type="button" id="selectExpiryDisplayButton">Apply</button>
          </td>
        </tr>
        <tr>
          <th>Search by ID</th>
          <td>
          <input type="number" id="searchId">
          </td>
          <td>
          <button type="button" id='searchIdButton'>Find</button>
          </td>
        </tr>

      </table>


    </form>



    <div id="kitsTableDiv">

      <table id="kitsTable" cellpadding="0" cellspacing="0" border="0"  class="display"></table>
    </div>



  </div>
</div>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.css"/>

<script type="text/javascript" src="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.js"></script>
<script type="text/javascript" src="http://jquery-datatables-column-filter.googlecode.com/svn/trunk/media/js/jquery.dataTables.columnFilter.js"></script>



<script>
  var components;
  var table;
  var exhaustedToggled = true;


  jQuery("#searchIdButton").click(function(){
    var id = jQuery("#searchIdButton").val();
    table.column(0).search(id).draw(); //0 is index od ID column
  })

  jQuery(document).ready(function(){
    listAllKitComponentsForTable();

  });

  jQuery("#showMoreButton").click(function(){

    jQuery('input[type=checkbox]').each(function(){
      if(this.checked){
        table.column(jQuery(this).val()).visible(true);
      }else{
        table.column(jQuery(this).val()).visible(false);
      }
    })
  })

  jQuery("#exhaustedVisibilityButton").click(function(){


    toggleVisibilityExhausted();



  });


  jQuery("#selectExpiryDisplayButton").click(function(){
    filterByExpiry();
  })


  function filterByExpiry(){

    var selected = jQuery("#selectExpiryDisplay").val();
    console.log(selected);
    var expiryStateColumn = 17; //index (by default that columns is hidden)

    switch(selected){

      case "-1":
        //show all
        table.column(expiryStateColumn).search("").draw();
        break;
      case "0":
        //show only expired
        console.log("sdasd");
        table.column(expiryStateColumn).search("0").draw();
        break;
      case "1":
        //show only soon-to-expire
        table.column(expiryStateColumn).search("1").draw();
        break;
    }


  }

  function toggleVisibilityExhausted(){
    if(exhaustedToggled){ //16 -index of exhausted column
      table.column(16).search("false").draw();
      exhaustedToggled = false;
    }else{
      table.column(16).search("").draw();
      exhaustedToggled = true;
    }
  }

  function listAllKitComponentsForTable(){
    console.log("in");
    Fluxion.doAjax(
            'kitComponentControllerHelperService',
            'listAllKitComponentsForTable',

            {
              'url': ajaxurl
            },
            {'doOnSuccess': function (json) {

              console.log("successful ajax");
              components = json.components;

              prepareAndShowTable(json.components);
              toggleVisibilityExhausted(); //by default hide exhausted

            }

            });
  };

  function prepareAndShowTable(components){
    table = jQuery("#kitsTable").DataTable({

      data: components,
      columns:[
        { data: 'ID', title: "ID"},
        { data: 'Kit Name', title: "Kit Name"},
        { data: 'Component Name', title: "Component Name"},
        { data: 'Version', title: "Version", visible:false},
        { data: 'Manufacturer', title: "Manufacturer"},
        { data: 'Part Number', title: "Part Number", visible:false},
        { data: 'Type', title: "Type", visible:false},
        { data: 'Platform', title: "Platform", visible: false},
        { data: 'Units', title: "Units", visible:false},
        { data: 'Value', title: "Value", visible:false},
        { data: 'Reference Number', title: "Reference Number", visible:false},
        { data: 'Identification Barcode', title: "Identification Barcode"},
        { data: 'Lot Number', title: "Lot Number"},
        { data: 'Location Barcode', title: "Location Barcode"},
        { data: 'Received Date', title: "Received Date"},
        { data: 'Expiry Date', title: "Expiry Date"},
        { data: 'Exhausted', title: "Exhausted"},
        { data: 'Expiry State', title: "Expiry State", visible:false}

      ],
      paging: false



    })};





</script>

<%@ include file="adminsub.jsp" %>
<%@ include file="../footer.jsp" %>