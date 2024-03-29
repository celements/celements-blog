jQuery("input").each(
  function(index, value) {
    var regexp = /^((.*?)_\d+_)(.*)$/;
    var prefix = value.id.replace(regexp, "$2");
    var suffix = value.id.replace(regexp, "$3");
    if ((prefix == "XWiki.ArticleClass") && ((suffix == "publishdate") || 
        (suffix == "archivedate"))) {
      var replaceId = value.id.replace(".", "\\.");
      jQuery("#" + replaceId).datetimepicker({
        lang : Validation.messages.get("admin-language"),
        scrollInput: false,
        dayOfWeekStart : 1,
        format : 'd.m.Y H:i'
      });
      var siblingSuffix = 'archivedate';
      if (suffix == 'archivedate') {
        siblingSuffix = "publishdate";
      }
      var siblingInputId = replaceId.replace(regexp, "$1") + siblingSuffix;
      jQuery("#" + replaceId).blur( function() {
        var endDate = null;
        var startDate = null;
        var endDateStr = "";
        var startDateStr = "";
        if (suffix == 'archivedate') {
          endDateStr = jQuery("#" + replaceId).val();
        startDateStr = jQuery("#" + siblingInputId).val();
        } else {
          endDateStr = jQuery("#" + siblingInputId).val();
          startDateStr = jQuery("#" + replaceId).val();
        }
        endDate = parseStringToDate(endDateStr);
        startDate = parseStringToDate(startDateStr);
        if ((endDate < startDate)
            && (endDate != "" && startDate != "")) {
          var errorMesgDialog = getCelementsTabEditor()._getModalDialog();
          errorMesgDialog.setHeader(Validation.messages.get("validate-warning-header"));
          var validationMessage = Validation.messages.get("validate-blog-dateRange"
              ).replace(/<FROM_DATE>/g, startDateStr).replace(/<TO_DATE>/g, endDateStr);
          errorMesgDialog.setBody(validationMessage);
          errorMesgDialog.cfg.setProperty("icon",
          YAHOO.widget.SimpleDialog.ICON_WARN);
          errorMesgDialog.cfg.queueProperty("buttons", [ {
            text : "OK",
            handler : function() {
              this.cancel();
            }
          } ]);
          errorMesgDialog.render();
          errorMesgDialog.show();
        }
      });
    }
  }
);

function parseStringToDate(dateString) {
  if (dateString != "") {
    var reggie = /(\d{2}).(\d{2}).(\d{4}) (\d{2}):(\d{2})/;
    var dateArray = reggie.exec(dateString);
    var dateObject = new Date(
        (+dateArray[3]),
        (+dateArray[2]) - 1, // month starts at 0!
        (+dateArray[1]),
        (+dateArray[4]));
    return dateObject;
  }
  return "";
}