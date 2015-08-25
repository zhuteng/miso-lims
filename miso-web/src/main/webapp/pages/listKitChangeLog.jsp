
<%@ include file="../header.jsp" %>



<div id="maincontent">
  <div id="contentcolumn">



    <div id="changeLogTableDiv">

      <table id="changeLogTable" cellpadding="0" cellspacing="0" border="0"  class="display"></table>
    </div>



  </div>
</div>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.css"/>

<script type="text/javascript" src="https://cdn.datatables.net/r/dt/dt-1.10.8,b-1.0.0,b-colvis-1.0.0/datatables.min.js"></script>
<script type="text/javascript" src="http://jquery-datatables-column-filter.googlecode.com/svn/trunk/media/js/jquery.dataTables.columnFilter.js"></script>



<script>
  var changeLog;
  var table;


  jQuery(document).ready(function(){
    listChangeLogForTable();

  });




  function listChangeLogForTable(){

    Fluxion.doAjax(
            'kitComponentControllerHelperService',
            'getKitChangeLog',

            {
              'url': ajaxurl
            },
            {'doOnSuccess': function (json) {


              changeLog = json.changeLog;

              prepareAndShowTable(changeLog);

            }

            });
  };

  function prepareAndShowTable(changeLog){
    table = jQuery("#changeLogTable").DataTable({

      data: changeLog,
      columns:[
        { data: 'userId', title: "User ID"},
        { data: 'kitComponentId', title: "Kit Component ID"},
        { data: 'exhausted', title: "Exhausted"},
        { data: 'locationBarcodeOld', title: "Location Barcode Before Change"},
        { data: 'locationBarcodeNew', title: "Location Barcode After Change"},
        { data: 'logDate', title: "Date of Change"}
      ],
      paging: false



    })};





</script>

<%@ include file="adminsub.jsp" %>
<%@ include file="../footer.jsp" %>