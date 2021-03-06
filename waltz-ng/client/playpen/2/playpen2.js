/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import template from "./playpen2.html";


function controller($http, $q) {

    const vm = Object.assign(this, {});

    vm.$onInit = () => {
        const promises = [
            $http.get("http://localhost:8443/api/enum-value").then(r => console.log("ev", r.data) || r.data),
            $http.get("http://localhost:8443/api/bookmarks/APPLICATION/840").then(r => console.log("b1", r.data) || r.data),
            $http.get("http://localhost:8443/api/bookmarks/APPLICATION/841").then(r => console.log("b2", r.data) || r.data)
        ];
        $q.all(promises)
            .then(([p1, p2, p3]) => {
                console.log({p1, p2, p3})
            });
    };
}



controller.$inject = [
    '$http', "$q"
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
