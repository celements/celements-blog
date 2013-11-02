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

Event.observe(window, 'load', function(){
  setObjectValues();
  var formElem = $('articleform') || $('edit');
  formElem.observe('click', setObjectValues);
  formElem.observe('submit', setObjectValues);
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
