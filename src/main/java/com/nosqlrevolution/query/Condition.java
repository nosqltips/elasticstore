package com.nosqlrevolution.query;

import java.util.Arrays;
import java.util.Objects;

/** A reusable field selector producing immutable condition values. */
public class Condition {
    private final String field;
    private final Field specialField;

    private Condition(String field, Field specialField) {
        this.field = field;
        this.specialField = specialField;
    }

    public static Condition field(String field) {
        if (field == null || field.trim().isEmpty()) throw new IllegalArgumentException("Field must not be blank");
        return new Condition(field, null);
    }

    public static Condition field(Field field) {
        Objects.requireNonNull(field, "Special field");
        if (field == Field.ALL) throw new IllegalArgumentException("Field.ALL is unsupported in Elasticsearch 8; name the searchable fields");
        return new Condition(null, field);
    }

    public static CompletedCondition and(CompletedCondition... conditions) { return group(Operator.AND, conditions); }
    public static CompletedCondition or(CompletedCondition... conditions) { return group(Operator.OR, conditions); }
    /** Negates the conjunction of its children: NOT (a AND b). */
    public static CompletedCondition not(CompletedCondition... conditions) { return group(Operator.NOT, conditions); }

    private static CompletedCondition group(Operator operator, CompletedCondition[] conditions) {
        if (conditions == null || conditions.length == 0) throw new IllegalArgumentException("Group must contain conditions");
        for (CompletedCondition condition : conditions) Objects.requireNonNull(condition, "Condition");
        return new CompletedCondition(null, null, operator, new Object[0], conditions);
    }

    public CompletedCondition equal(String value) { return equal((Object) value); }
    public CompletedCondition equal(Object value) { return leaf(Operator.EQUAL, value); }
    public CompletedCondition notEqual(String value) { return notEqual((Object) value); }
    public CompletedCondition notEqual(Object value) { return leaf(Operator.NOT_EQUAL, value); }
    public CompletedCondition gt(String value) { return gt((Object) value); }
    public CompletedCondition gt(Object value) { return leaf(Operator.GT, value); }
    public CompletedCondition gte(String value) { return gte((Object) value); }
    public CompletedCondition gte(Object value) { return leaf(Operator.GTE, value); }
    public CompletedCondition lt(String value) { return lt((Object) value); }
    public CompletedCondition lt(Object value) { return leaf(Operator.LT, value); }
    public CompletedCondition lte(String value) { return lte((Object) value); }
    public CompletedCondition lte(Object value) { return leaf(Operator.LTE, value); }
    public CompletedCondition in(String... values) { return leaf(Operator.IN, (Object[]) values); }
    public CompletedCondition in(Object... values) { return leaf(Operator.IN, values); }
    public CompletedCondition notIn(String... values) { return leaf(Operator.NOT_IN, (Object[]) values); }
    public CompletedCondition notIn(Object... values) { return leaf(Operator.NOT_IN, values); }
    public CompletedCondition match(String value) { return leaf(Operator.MATCH, value); }
    public CompletedCondition matchPhrase(String value) { return leaf(Operator.MATCH_PHRASE, value); }
    public CompletedCondition exists() { return new CompletedCondition(field, specialField, Operator.EXISTS, new Object[0], new CompletedCondition[0]); }
    /** All supplied text queries must match; each query requires all its analyzed words. */
    public CompletedCondition mustContain(String... values) { return leaf(Operator.MUST, (Object[]) values); }
    /** At least one supplied text query must match. */
    public CompletedCondition shouldContain(String... values) { return leaf(Operator.SHOULD, (Object[]) values); }
    /** Excludes documents matching any supplied text query. */
    public CompletedCondition mustNotContain(String... values) { return leaf(Operator.MUST_NOT, (Object[]) values); }

    private CompletedCondition leaf(Operator operator, Object... values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException("Condition must have values");
        for (Object value : values) {
            if (!(value instanceof String || value instanceof Boolean || value instanceof Byte
                    || value instanceof Short || value instanceof Integer || value instanceof Long
                    || value instanceof Float || value instanceof Double)) {
                throw new IllegalArgumentException("Values must be strings, booleans, or primitive numeric wrappers");
            }
            if (value instanceof Number && !Double.isFinite(((Number) value).doubleValue())) {
                throw new IllegalArgumentException("Numeric values must be finite");
            }
        }
        return new CompletedCondition(field, specialField, operator, values, new CompletedCondition[0]);
    }

    public static final class CompletedCondition {
        private final String field;
        private final Field specialField;
        private final Operator operator;
        private final Object[] values;
        private final CompletedCondition[] conditions;

        private CompletedCondition(String field, Field specialField, Operator operator,
                Object[] values, CompletedCondition[] conditions) {
            this.field = field;
            this.specialField = specialField;
            this.operator = operator;
            this.values = values.clone();
            this.conditions = conditions.clone();
        }

        public String getField() { return field; }
        public Field getSpecialField() { return specialField; }
        public Operator getOperator() { return operator; }
        /** Legacy string view; compilation uses the typed values. */
        public String getValue() { return String.valueOf(values[0]); }
        public String[] getValues() { return Arrays.stream(values).map(String::valueOf).toArray(String[]::new); }
        public Object getTypedValue() { return values[0]; }
        public Object[] getTypedValues() { return values.clone(); }
        public CompletedCondition[] getConditions() { return conditions.clone(); }
    }
}
