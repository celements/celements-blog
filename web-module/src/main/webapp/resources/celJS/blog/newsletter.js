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

  if (typeof $ === "undefined") { window.$ = document.getElementById; }

  const stopEvent = function(event) {
    event.stopPropagation();
    event.preventDefault();
  };

  Event.observe(window, 'load', function() {
    if ($('newsletter_subscribe')) {
      $('newsletter_subscribe').addEventListener('submit', function(event) {
        newsletterajax($('newsletter_subscribe'), $('newsletter_subscribe_answer'));
        stopEvent(event);
      });
    }
    if ($('newsletter_unsubscribe')) {
      $('newsletter_unsubscribe').addEventListener('submit', function(event) {
        newsletterajax($('newsletter_unsubscribe'), $('newsletter_unsubscribe_answer'));
        stopEvent(event);
      });
    }
    if ($('newsletter_activate')) {
      $('newsletter_activate').addEventListener('submit', function(event) {
        newsletterajax($('newsletter_activate'), $('newsletter_activate_answer'));
        stopEvent(event);
      });
    }
    if ($('newsletter_send')) {
      $('newsletter_send').observe('celValidation:submitFormAfterValidation',
        submitNewsletterFormHandler);
      document.querySelectorAll('#newsletter_send .celNLsubmitButton').forEach(
        registerSubmitButtonListener);
    }
  });

  const registerSubmitButtonListener = function(button) {
    button.addEventListener('click', celNLsubmitButtonListener);
  };

  const celNLsubmitButtonListener = function(event) {
    const button = event.target;
    $('testBox').value = button.dataset.testSend;
  };

  const submitNewsletterFormHandler = function(event) {
    event.stop();
    const isTest = ($('testBox').value === "1");
    const answerBox = isTest ? $('testResultBox') : $('newsletter_send_answer');
    newsletterajax($('newsletter_send'), answerBox);
  };

  const newsletterajax = function(form, answer) {
    const isTest = ($('testBox').value === "1");
    const buttonBox = isTest ? answer : form;
    const progressBarElem = buttonBox.next('.nlProgressBar');
    if (isTest || confirm($('cel_newsletter_confirm_send_message').value)) {
      buttonBox.style.display = "none";
      progressBarElem.style.display = "";
      const url = form.action || '?';
      new Ajax.Request(url, {
        parameters: form.serialize(true),
        method: "post",
        onComplete: function(transport) {
          answer.innerHTML = transport.responseText;
          buttonBox.style.display = "";
          progressBarElem.style.display = "none";
          if (!isTest) {
            form.reset();
          }
        }
      });
    }
  };

})(window);
