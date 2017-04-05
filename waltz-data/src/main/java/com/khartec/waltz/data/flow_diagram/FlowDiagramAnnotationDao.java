/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.flow_diagram;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.flow_diagram.FlowDiagramAnnotation;
import com.khartec.waltz.model.flow_diagram.ImmutableFlowDiagramAnnotation;
import com.khartec.waltz.schema.tables.records.FlowDiagramAnnotationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.FLOW_DIAGRAM_ANNOTATION;

@Repository
public class FlowDiagramAnnotationDao {


    private static final RecordMapper<Record, FlowDiagramAnnotation> TO_DOMAIN_MAPPER = r -> {
        FlowDiagramAnnotationRecord record = r.into(FLOW_DIAGRAM_ANNOTATION);
        return ImmutableFlowDiagramAnnotation.builder()
                .diagramId(record.getDiagramId())
                .entityReference(mkRef(
                        EntityKind.valueOf(record.getEntityKind()),
                        record.getEntityId()))
                .note(record.getNote())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public FlowDiagramAnnotationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<FlowDiagramAnnotation> findByDiagramId(long diagramId) {
        return dsl
                .selectFrom(FLOW_DIAGRAM_ANNOTATION)
                .where(FLOW_DIAGRAM_ANNOTATION.DIAGRAM_ID.eq(diagramId))
                .fetch(TO_DOMAIN_MAPPER);
    }

}
