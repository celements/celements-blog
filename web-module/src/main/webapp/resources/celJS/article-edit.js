Event.observe(window, 'load', function(){
  setObjectValues();
  $('edit').observe('click', setObjectValues);
  $('edit').observe('submit', setObjectValues);
});

function setObjectValues() {
  if ($$('.archivedate_hidden_deflang').size() > 0) {
    var archivedate = $$('.archivedate_hidden_deflang')[0].value;
    $$('.archivedate_hidden_langs').each(function(elem) {
      elem.value = archivedate;
    });
  }

  if ($$('.publishdate_hidden_deflang').size() > 0) {
    var publishdate = $$('.publishdate_hidden_deflang')[0].value;
    $$('.publishdate_hidden_langs').each(function(elem) {
      elem.value = publishdate;
    });
  }

  if ($$('.isSubscribable_hidden_deflang').size() > 0) {
    var subscribable = $$('.isSubscribable_hidden_deflang')[0].value;
    $$('.isSubscribable_hidden_langs').each(function(elem) {
      elem.value = subscribable;
    });
  }
};
