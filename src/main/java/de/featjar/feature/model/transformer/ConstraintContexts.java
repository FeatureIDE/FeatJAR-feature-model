/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.transformer;

import de.featjar.feature.model.IFeatureTree;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class which can be used as a key in a HashMap which maps feature contexts to
 * constraints.
 *
 * @author Klara Surmeier
 * @author Nermine Mansour
 * @author Malena Horstmann
 */
public class ConstraintContexts {
    private final Set<IFeatureTree> contextFeatures;

    public Set<IFeatureTree> getContextFeatures() {
        return contextFeatures;
    }

    public ConstraintContexts(Set<IFeatureTree> features) {
        this.contextFeatures = new HashSet<>(features);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstraintContexts that = (ConstraintContexts) o;

        return this.contextFeatures.equals(that.contextFeatures);
    }

    @Override
    public int hashCode() {

        return Objects.hash(contextFeatures);
    }
}
