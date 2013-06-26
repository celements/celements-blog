Event.observe('load', window, function() {
  $$('.submitButton').each(function(ele) {
    ele.observe('click', function(event) {
      event.element().hide();
    });
  });
});