Event.observe(window, 'load', function() {
  $$('.submitButton').each(function(ele) {
    ele.observe('click', function(event) {
      event.element().hide();
    });
  });
});