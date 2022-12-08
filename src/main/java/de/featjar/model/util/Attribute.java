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

import de.featjar.model.Feature;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * An attribute describes what metadata can be attached to an object. For
 * example, {@link Feature features} can have names, be abstract, or be hidden;
 * all of these are attributes. This class does not store any attribute values,
 * but merely acts as a key or descriptor.
 *
 * @param <T> the type of values that are valid for this attribute (usually
 *            String, Boolean, or Integer)
 * @author Elias Kuiter
 */
public class Attribute<T> implements Function<Map<Attribute<?>, Object>, Optional<T>> {
    public static final String DEFAULT_NAMESPACE = "<default>";

    protected final String namespace;
    protected final String name;
    protected final Class<?> type;

    public Attribute(String namespace, String name, Class<?> type) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        this.namespace = namespace;
        this.name = name;
        this.type = type;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public Optional<T> apply(Map<Attribute<?>, Object> attributeToValueMap) {
        return Optional.ofNullable((T) attributeToValueMap.get(this));
    }

    @Override
    public String toString() {
        return String.format("Attribute{namespace='%s', name='%s'}", namespace, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute<?> attribute = (Attribute<?>) o;
        return namespace.equals(attribute.namespace) && name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    public static class WithDefaultValue<T> extends Attribute<T> {
        protected final Function<Attributable, T> defaultValueFunction;

        public WithDefaultValue(
                String namespace, String name, Class<?> type, Function<Attributable, T> defaultValueFunction) {
            super(namespace, name, type);
            Objects.requireNonNull(defaultValueFunction);
            this.defaultValueFunction = defaultValueFunction;
        }

        public WithDefaultValue(String namespace, String name, Class<?> type, T defaultValue) {
            this(namespace, name, type, attributable -> defaultValue);
            Objects.requireNonNull(defaultValue);
        }

        public Function<Attributable, T> getDefaultValueFunction() {
            return defaultValueFunction;
        }

        public T getDefaultValue(Attributable attributable) {
            return defaultValueFunction.apply(attributable);
        }

        public T applyWithDefaultValue(Map<Attribute<?>, Object> attributeToValueMap, Attributable attributable) {
            return (T) attributeToValueMap.getOrDefault(this, defaultValueFunction.apply(attributable));
        }
    }

    public static class Set<T> extends WithDefaultValue<java.util.Set<T>> {
        public Set(String namespace, String name) {
            super(namespace, name, Set.class, new HashSet<>());
        }
    }
}
