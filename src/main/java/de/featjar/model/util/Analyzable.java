/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of feature-model.
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
 * See <https://github.com/FeatJAR/model> for further information.
 */
package de.featjar.model.util;

import java.util.function.Consumer;

/**
 * An object that can be analyzed with an {@link Analyzer}. todo Analysis
 * results are cached when o.analyze() is used. Mutation may invalidate analysis
 * results.
 *
 * @param <T> the type of the analyzable object
 * @param <U> the type of the analyzer object
 * @author Elias Kuiter
 */
public interface Analyzable<T, U extends Analyzer<T>> {
    U getAnalyzer();

    void setAnalyzer(U analyzer);

    default U analyze() {
        return getAnalyzer();
    }

    default T analyze(Consumer<U> analyzerConsumer) {
        analyzerConsumer.accept(getAnalyzer());
        return getAnalyzer().getAnalyzable();
    }
}
