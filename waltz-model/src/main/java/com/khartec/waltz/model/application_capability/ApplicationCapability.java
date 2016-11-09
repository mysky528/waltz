/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.applicationcapability;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.capabilityrating.RagRating;
import org.immutables.value.Value;

@Deprecated
@Value.Immutable
@JsonSerialize(as = ImmutableApplicationCapability.class)
@JsonDeserialize(as = ImmutableApplicationCapability.class)
public abstract class ApplicationCapability implements
        ProvenanceProvider,
        DescriptionProvider,
        LastUpdatedProvider
{

    public abstract long capabilityId();
    public abstract long applicationId();
    public abstract boolean isPrimary();
    public abstract RagRating rating();

}