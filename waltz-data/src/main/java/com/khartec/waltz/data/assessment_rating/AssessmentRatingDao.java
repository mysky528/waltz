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

package com.khartec.waltz.data.assessment_rating;


import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.assessment_definition.AssessmentVisibility;
import com.khartec.waltz.model.assessment_rating.AssessmentRating;
import com.khartec.waltz.model.assessment_rating.ImmutableAssessmentRating;
import com.khartec.waltz.model.assessment_rating.RemoveAssessmentRatingCommand;
import com.khartec.waltz.model.assessment_rating.SaveAssessmentRatingCommand;
import com.khartec.waltz.schema.tables.records.AssessmentRatingRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.schema.tables.AssessmentDefinition.ASSESSMENT_DEFINITION;
import static com.khartec.waltz.schema.tables.AssessmentRating.ASSESSMENT_RATING;
import static java.util.Optional.ofNullable;

@Repository
public class AssessmentRatingDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            ASSESSMENT_RATING.ENTITY_ID,
            ASSESSMENT_RATING.ENTITY_KIND,
            newArrayList(EntityKind.values()));

    private static final RecordMapper<? super Record, AssessmentRating> TO_DOMAIN_MAPPER = r -> {
        AssessmentRatingRecord record = r.into(ASSESSMENT_RATING);
        return ImmutableAssessmentRating.builder()
                .entityReference(EntityReference.mkRef(EntityKind.valueOf(record.getEntityKind()), record.getEntityId()))
                .assessmentDefinitionId(record.getAssessmentDefinitionId())
                .ratingId(record.getRatingId())
                .comment(mkSafe(record.getDescription()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };

    private static final RecordUnmapper<AssessmentRating, AssessmentRatingRecord> TO_RECORD_UNMAPPER = r -> {
        AssessmentRatingRecord record = new AssessmentRatingRecord();
        record.setAssessmentDefinitionId(r.assessmentDefinitionId());
        record.setEntityKind(r.entityReference().kind().name());
        record.setEntityId(r.entityReference().id());
        record.setRatingId(r.ratingId());
        record.setDescription(r.comment());
        record.setLastUpdatedAt(Timestamp.valueOf(r.lastUpdatedAt()));
        record.setLastUpdatedBy(r.lastUpdatedBy());
        record.setProvenance(r.provenance());
        return record;
    };

    private static final RecordMapper<? super Record, AssessmentRating> TO_DOMAIN_MAPPER_WITH_ENTITY_DETAILS  = r -> {
        AssessmentRatingRecord record = r.into(ASSESSMENT_RATING);
        return ImmutableAssessmentRating.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(record.getEntityKind()))
                        .id(record.getEntityId())
                        .name(ofNullable(r.getValue(ENTITY_NAME_FIELD)))
                        .build())
                .assessmentDefinitionId(record.getAssessmentDefinitionId())
                .ratingId(record.getRatingId())
                .comment(mkSafe(record.getDescription()))
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    private final Function<SaveAssessmentRatingCommand, AssessmentRatingRecord> TO_RECORD_MAPPER = command -> {
        AssessmentRatingRecord record = new AssessmentRatingRecord();
        record.setEntityId(command.entityReference().id());
        record.setEntityKind(command.entityReference().kind().name());
        record.setAssessmentDefinitionId(command.assessmentDefinitionId());
        record.setRatingId(command.ratingId());
        record.setDescription(command.comment());
        record.setLastUpdatedAt(Timestamp.valueOf(command.lastUpdatedAt()));
        record.setLastUpdatedBy(command.lastUpdatedBy());
        record.setProvenance(command.provenance());
        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public AssessmentRatingDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<AssessmentRating> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return dsl.selectFrom(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AssessmentRating> findByEntityKind(EntityKind kind, List<AssessmentVisibility> visibilities) {
        return dsl.select(ASSESSMENT_RATING.fields())
                .from(ASSESSMENT_RATING)
                .innerJoin(ASSESSMENT_DEFINITION).on(ASSESSMENT_DEFINITION.ID.eq(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID))
                .where(ASSESSMENT_RATING.ENTITY_KIND.eq(kind.name()))
                .and(ASSESSMENT_DEFINITION.VISIBILITY.in(visibilities))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AssessmentRating> findByDefinitionId(long definitionId, List<AssessmentVisibility> visibilities) {
        return dsl.select(ASSESSMENT_RATING.fields())
                .select(ENTITY_NAME_FIELD)
                .from(ASSESSMENT_RATING)
                .innerJoin(ASSESSMENT_DEFINITION).on(ASSESSMENT_DEFINITION.ID.eq(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID))
                .and(ASSESSMENT_DEFINITION.ID.eq(definitionId))
                .and(ASSESSMENT_DEFINITION.VISIBILITY.in(visibilities))
                .fetch(TO_DOMAIN_MAPPER_WITH_ENTITY_DETAILS);
    }

    public List<AssessmentRating> findByGenericSelector(GenericSelector genericSelector) {
        return dsl
                .select(ASSESSMENT_RATING.fields())
                .from(ASSESSMENT_RATING)
                .innerJoin(ASSESSMENT_DEFINITION)
                .on(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(ASSESSMENT_DEFINITION.ID)
                        .and(ASSESSMENT_DEFINITION.ENTITY_KIND.eq(genericSelector.kind().name())))
                .where(ASSESSMENT_RATING.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.in(genericSelector.selector()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean store(SaveAssessmentRatingCommand command) {
        checkNotNull(command, "command cannot be null");
        AssessmentRatingRecord record = TO_RECORD_MAPPER.apply(command);
        EntityReference ref = command.entityReference();
        boolean isUpdate = dsl.fetchExists(dsl
                .selectFrom(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ENTITY_KIND.eq(ref.kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(ref.id()))
                .and(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(command.assessmentDefinitionId())));

        return isUpdate
                ? dsl.executeUpdate(record) == 1
                : dsl.executeInsert(record) == 1;
    }


    public boolean remove(RemoveAssessmentRatingCommand rating) {
        return dsl.deleteFrom(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ENTITY_KIND.eq(rating.entityReference().kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(rating.entityReference().id()))
                .and(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(rating.assessmentDefinitionId()))
                .execute() == 1;
    }

    public int add(Set<AssessmentRating> assessmentRatings) {
        Set<AssessmentRatingRecord> recordsToStore = mkAssessmentRatingRecords(assessmentRatings);
         return dsl.batchInsert(recordsToStore).execute().length;

    }

    public int update(Set<AssessmentRating> assessmentRatings) {
            Set<AssessmentRatingRecord> recordsToUpdate = mkAssessmentRatingRecords(assessmentRatings);
            return dsl.batchUpdate(recordsToUpdate).execute().length;
    }

    public int remove(Set<AssessmentRating> assessmentRatings) {
        Set<AssessmentRatingRecord> ratingsToRemove = mkAssessmentRatingRecords(assessmentRatings);
        return dsl.batchDelete(ratingsToRemove).execute().length;
    }


    private Set<AssessmentRatingRecord> mkAssessmentRatingRecords(Set<AssessmentRating> assessmentRatings) {
        return assessmentRatings
                .stream()
                .map(TO_RECORD_UNMAPPER::unmap)
                .collect(Collectors.toSet());
    }
}
