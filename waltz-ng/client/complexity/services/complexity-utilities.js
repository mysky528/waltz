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

import _ from 'lodash';
import {notEmpty} from '../../common';


export function calcComplexitySummary(complexity = []) {
    if (!complexity) return;
    const cumulativeScore = _.sumBy(complexity, "overallScore");
    const averageScore = notEmpty(complexity) ? cumulativeScore / complexity.length : 0;

    return {
        cumulativeScore,
        averageScore
    };
}

export function findDefaultComplexityKind(complexityKinds = []) {
    const defaultKind = _.find(complexityKinds, d => d.isDefault);
    return defaultKind
        ? defaultKind
        : _.first(complexityKinds);
}

export function enrichComplexitiesWithKind(complexities, complexityKinds) {
    const complexityKindsById = _.keyBy(complexityKinds, d => d.id);
    return _
        .chain(complexities)
        .map(d => Object.assign(
            {},
            d,
            {complexityKind: _.get(complexityKindsById, [d.complexityKindId], 'Unknown')}))
        .orderBy(d => d.complexityKind.name)
        .value();
}