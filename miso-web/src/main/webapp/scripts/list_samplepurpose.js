/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO.  If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

ListTarget.samplepurpose = {
  name: "Sample Purposes",
  createUrl: function(config, projectId) {
    throw "Must be provided statically";
  },
  createBulkActions: function(config, projectId) {
    return config.isAdmin ? HotTarget.samplepurpose.bulkActions : [];
  },
  createStaticActions: function(config, projectId) {
    return config.isInternal ? [{
      "name": "Add",
      "handler": function() {

        Utils.showDialog('Create Sample Purposes', 'Create', [{
          property: 'quantity',
          type: 'int',
          label: 'Quantity',
          value: 1
        }], function(result) {
          if (result.quantity < 1) {
            Utils.showOkDialog('Create Sample Purposes', ["That's a peculiar number of sample purposes to create."]);
            return;
          }
          window.location = '/miso/samplepurpose/bulk/new?' + jQuery.param({
            quantity: result.quantity,
          });
        });
      }
    }] : [];
  },
  createColumns: function(config, projectId) {
    return [{
      "sTitle": "Alias",
      "mData": "alias",
      "include": true,
      "iSortPriority": 0
    }, ];
  }
};
