Event.observe(window, 'load', function(){
  setObjectValues();
  $('edit').observe('click', setObjectValues);
  $('edit').observe('submit', setObjectValues);
});

function setObjectValues(){
  var archivedate = $$('.archivedate_hidden_deflang')[0].value;
  $$('.archivedate_hidden_langs').each(function(elem) {
    elem.value = archivedate;
  });
  
  var publishdate = $$('.publishdate_hidden_deflang')[0].value;
  $$('.publishdate_hidden_langs').each(function(elem) {
    elem.value = publishdate;
  });
  
  var subscribable = $$('.isSubscribable_hidden_deflang')[0].value;
  $$('.isSubscribable_hidden_langs').each(function(elem) {
    elem.value = subscribable;
  });
}