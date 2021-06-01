/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

(function(window, undefined) {
  "use strict";

  Event.observe(window, 'load', function(){
    if($('newsletter_subscribe')){
      $('newsletter_subscribe').observe('submit', function(event){
        newsletterajax($('newsletter_subscribe'), $('newsletter_subscribe_answer'));
        event.stop();
      });
    }
    if($('newsletter_unsubscribe')){
      $('newsletter_unsubscribe').observe('submit', function(event){
        newsletterajax($('newsletter_unsubscribe'), $('newsletter_unsubscribe_answer'));
        event.stop();
      });
    }
    if($('newsletter_activate')){
      $('newsletter_activate').observe('submit', function(event){
        newsletterajax($('newsletter_activate'), $('newsletter_activate_answer'));
        event.stop();
      });
    }
    if($('newsletter_send')){
      $('newsletter_send').observe('submit', submitNewsletterFormHandler);
    }
  });
  
  const submitNewsletterFormHandler = function(event){
    event.stop();
    let answerBox = $('newsletter_send_answer');
    if($('testBox').value == "1"){
      answerBox = $('testResultBox');
    }
    console.warn('TESTING submitNewsletterFormHandler! newsletter sending deactivated', event);
    //newsletterajax($('newsletter_send'), answerBox);
  };

  const newsletterajax = function(form, answer){
    const isTest = ($('testBox').value == "1");
    let confirmSend = 0;
    if(isTest){
      answer.setStyle({ display : "none"});
      answer.siblings()[0].setStyle({ display : "" });
    } else {
      confirmSend = confirm($('cel_newsletter_confirm_send_message').value);
      if(confirmSend){
        form.setStyle({ display : "none"});
        form.siblings()[0].setStyle({ display : "" });
      }
    }
    
    if(isTest || confirmSend){
      let url = form.action;
      if(url == ''){
        url = "?";
      }
      console.warn('TESTING! newsletter sending deactivated', form, answer);
/*      new Ajax.Request(url, {
        parameters : form.serialize(true),
        method : "post",
        onComplete : function(transport){
          answer.innerHTML = transport.responseText;
          if(isTest){
            answer.setStyle({ display : ""});
            answer.siblings()[0].setStyle({ display : "none" });
          } else {
            form.reset();
            form.siblings()[0].setStyle({ display : "none" })
            form.setStyle({ display : "" });
          }
        }
      });
  */
    }
  };
  
})(window);
