jQuery("input").each(function( index, value ) {
	var prefix = value.id.substr(0, value.id.indexOf('_'));
	var suffix = value.id.substr(value.id.lastIndexOf('_')+1, value.id.length);
	if(prefix == "XWiki.ArticleClass" && (suffix == "publishdate" || suffix == "archivedate")) {
		jQuery("#"+value.id.replace(".", "\\.")).datetimepicker({	     
			lang:Validation.messages.get("admin-language"),
			dayOfWeekStart: 1,
			format:'d/m/Y H:i:s'});
	}
});